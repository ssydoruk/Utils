/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utils.UnixProcess;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.sshd.client.ClientFactoryManager;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.PropertyResolverUtils;
import org.apache.sshd.common.SshException;
import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author stepan_sydoruk
 */
public class SSHClientWrapper {
    private final long defaultTimeoutSeconds;

    public SshClient getClient() {
        return client;
    }

    public long getDefaultTimeoutSeconds() {
        return defaultTimeoutSeconds;
    }

    private Future<?> stdInFuture;
    private static final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool(
            new BasicThreadFactory.Builder().namingPattern("sshclient-%d").build()
    );

    private static class SSHServer {
        private final String username;
        private final String password;
        private final String host;
        private final int port;


        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        public SSHServer(String username, String password, String host, int port) {
            this.username = username;
            this.password = password;
            this.host = host;
            this.port = port;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SSHServer sshServer = (SSHServer) o;

            if (port != sshServer.port) return false;
            if (!username.equals(sshServer.username)) return false;
            if (password != null ? !password.equals(sshServer.password) : sshServer.password != null) return false;
            return host.equals(sshServer.host);
        }

        @Override
        public int hashCode() {
            int result = username.hashCode();
            result = 31 * result + (password != null ? password.hashCode() : 0);
            result = 31 * result + host.hashCode();
            result = 31 * result + port;
            return result;
        }
    }

    private static class SSHSessions extends HashMap<SSHServer, ClientSession> {

        public SSHSessions() {
            super();
        }

        synchronized public ClientSession getAuthenticatedSession(SSHClientWrapper wrapper, SSHServer server) throws IOException {
            ClientSession ret = null;

            if (!isEmpty() && containsKey(server)) {
                ret = get(server);
            }
            if (ret != null && ret.isClosed()) {
                ret = null;
            }
            if (ret == null) {
                ret = startSession(wrapper, server);
                put(server, ret);
            }
            return ret;
        }

        private ClientSession startSession(SSHClientWrapper wrapper, SSHServer server) throws IOException {
//            wrapper.getClient()
//                    .connect(server.getUsername(), server.getHost(), server.getPort());
            ClientSession session = wrapper.getClient()
                    .connect(server.getUsername(), server.getHost(), server.getPort())
                    .verify(wrapper.getDefaultTimeoutSeconds(), TimeUnit.SECONDS).getClientSession();
            session.addPasswordIdentity(server.getPassword());

            /*
            String auth_sock = System.getenv().get("SSH_AUTH_SOCK");
            if (StringUtils.isNotBlank(auth_sock))
                session.getProperties().put(SshAgent.SSH_AUTHSOCKET_ENV_NAME, auth_sock);
            UserAuthPublicKeyFactory
                    factory
                    = new
                    UserAuthPublicKeyFactory();
            UserAuthPublicKey userAuth = factory.createUserAuth(session);
            session.setUserAuthFactories(Collections.singletonList(factory));
            LocalAgentFactory f = new LocalAgentFactory();


            session.addPublicKeyIdentity(userAuth);

             */

            //            session.setUserAuthFactories((List<UserAuthFactory>) new UnixAgentFactory());

            session.auth().verify(wrapper.getDefaultTimeoutSeconds(), TimeUnit.SECONDS);
            return session;
        }
    }

    private static SshClient client = null;

    SSHSessions sshSessions = new SSHSessions();

    private static final long HEARTBEAT = TimeUnit.SECONDS.toMillis(2L);

    public SSHClientWrapper(long defaultTimeoutSeconds) {
        client = SshClient.setUpDefaultClient();
//        PropertyResolverUtils.updateProperty(client, ClientFactoryManager.HEARTBEAT_INTERVAL, HEARTBEAT);
//        PropertyResolverUtils.updateProperty(client, ClientFactoryManager.SOCKET_KEEPALIVE, true);

        /*
        below block is experiment to try to authenticate via local ssh agent

        String auth_sock = System.getenv().get("SSH_AUTH_SOCK");
        if (StringUtils.isNotBlank(auth_sock)) {
            PropertyResolverUtils.updateProperty(client, SshAgent.SSH_AUTHSOCKET_ENV_NAME, auth_sock);
            client.getProperties().put(SshAgent.SSH_AUTHSOCKET_ENV_NAME, auth_sock);
        }
        org.apache.sshd.client.auth.pubkey.UserAuthPublicKeyFactory
                factory
                = new
                org.apache.sshd.client.auth.pubkey.UserAuthPublicKeyFactory();

        client.setUserAuthFactories(Collections.singletonList(factory));
        client.setAgentFactory(new LocalAgentFactory());
*/

        client.start();
        this.defaultTimeoutSeconds = defaultTimeoutSeconds;
    }


    public RemoteExecutionResult executeRemoteCommand(String username, String password,
                                                      String host, int port, String command) throws IOException, SshException {

        try {
            ClientSession session = sshSessions.getAuthenticatedSession(this, new SSHServer(username, password, host, port));

        /*
        Below synchronization should ensure sequential query to the same server.
        Different servers should be queried in parallel since session will be different for each
        combination of parameters
         */
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (session) {
                RemoteExecutionResult ret = new RemoteExecutionResult();
                ret.setRetCode(0);

                try (ByteArrayOutputStream stdOutStream = new ByteArrayOutputStream(1024);
                     ByteArrayOutputStream stdErrStream = new ByteArrayOutputStream();
                     ClientChannel channel = session.createExecChannel(command)) {
                    channel.setOut(stdOutStream);
                    channel.setErr(stdErrStream);

                    try {
                        channel.open().verify(defaultTimeoutSeconds, TimeUnit.SECONDS);
                        try (OutputStream pipedIn = channel.getInvertedIn()) {
                            pipedIn.write(command.getBytes());
                            pipedIn.flush();
                        }

                        channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED),
                                TimeUnit.SECONDS.toMillis(defaultTimeoutSeconds));
                        ret.setRetCode(channel.getExitStatus());
                        String responseString = new String(stdOutStream.toString("utf-8"));
                        if (StringUtils.isNotBlank(responseString)) {
                            ret.setStdout(new ArrayList<String>(Arrays.asList(responseString.split("\n"))));
                        }
                        responseString = new String(stdErrStream.toString("utf-8"));
                        if (StringUtils.isNotBlank(responseString)) {
                            ret.setStderr(new ArrayList<String>(Arrays.asList(responseString.split("\n"))));
                        }
                    } finally {
                        channel.close(false);
                    }
                }
                return ret;
            }
        } catch (Exception e) {
            RemoteExecutionResult ret = new RemoteExecutionResult();
            ret.setRetCode(255);
            ArrayList<String> stdErr = new ArrayList<>();
            stdErr.add(e.getMessage());
            ret.setStderr(stdErr);
            return ret;
        }
    }

    public RemoteExecutionResult executePipedRemoteCommand(String username, String password,
                                                           String host, int port, String command,
                                                           ThreadedOutputStreamReader outputReader) throws IOException {

        ClientSession session = sshSessions.getAuthenticatedSession(this, new SSHServer(username, password, host, port));

        /*
        Below synchronization should ensure sequential query to the same server.
        Different servers should be queried in parallel since session will be different for each
        combination of parameters
         */
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (session) {
            RemoteExecutionResult ret = new RemoteExecutionResult();
            ret.setRetCode(0);

            boolean externalOutStream = true;
            ByteArrayOutputStream stdOutStream = null;
            if (outputReader == null) {
                stdOutStream = new ByteArrayOutputStream();
            }

            try (
                    ByteArrayOutputStream stdErrStream = new ByteArrayOutputStream();
                    ClientChannel channel = session.createExecChannel(command)) {
                channel.setOut((outputReader != null) ? outputReader.getOutputStream() : stdOutStream);
                channel.setErr(stdErrStream);

                try {
                    channel.open().verify(defaultTimeoutSeconds, TimeUnit.SECONDS);
                    if (outputReader != null)
                        stdInFuture = executor.submit(outputReader);
                    try (OutputStream pipedIn = channel.getInvertedIn()) {
                        pipedIn.write(command.getBytes());
                        try {
                            pipedIn.flush();

                        } catch (Exception e) {
                            logger.error("Exception while flushing: " + e.getMessage());
                        }

                    }
                    // todo: better handling of timeout for file transfer completion
                    channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED),
                            TimeUnit.SECONDS.toMillis(defaultTimeoutSeconds) * 100000);
                    ret.setRetCode(channel.getExitStatus());
                    String responseString;
                    if (stdOutStream != null) {
                        responseString = new String(stdOutStream.toString("utf-8"));
                        if (StringUtils.isNotBlank(responseString)) {
                            ret.setStdout(new ArrayList<String>(Arrays.asList(responseString.split("\n"))));
                        }
                    }
                    responseString = new String(stdErrStream.toString("utf-8"));
                    if (StringUtils.isNotBlank(responseString)) {
                        ret.setStderr(new ArrayList<String>(Arrays.asList(responseString.split("\n"))));
                    }
                } finally {
                    channel.close(false);
                    if (stdOutStream != null)
                        stdOutStream.close();
                }
            }
            return ret;
        }
    }

    private static Logger logger;

//    public static void main(String[] args) throws IOException {
//        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
//        loggerContext.reset();
//        JoranConfigurator configurator = new JoranConfigurator();
//        String logback_file =
//                StringUtils.defaultIfBlank(StringUtils.defaultIfBlank(System.getenv().get("logback"), System.getProperty("logback")),
//                        "logback.xml");
//        try (InputStream configStream =
//                     FileUtils.openInputStream(new File(logback_file))) {
//            configurator.setContext(loggerContext);
//            configurator.doConfigure(configStream); // loads logback file
//        } catch (JoranException e) {
//            e.printStackTrace();
//        }
//        // assume SLF4J is bound to logback in the current environment
//        logger = LoggerFactory.getLogger("");
//
//
//        SSHClientWrapper cl = new SSHClientWrapper(20000);
//        ThreadedOutputStreamReader stdoutReader = new ThreadedUnTarGZ("/Users/stepan_sydoruk/tmp");
////        cl.executePipedRemoteCommand(
////                "ssydoruk",
////                "pq1617uw",
////                "192.168.64.10",
////                22,
////                "tar -C /applog/gcti/app_test -cz app_test.20210403_002535_895.log app_test_sip-001.20210402_233559_293.log",
////                stdoutReader);
////        cl.executePipedRemoteCommand(
////                "ssydoruk",
////                "pq1617uw",
////                "192.168.1.69",
////                22,
////                "tar -C /applog/gcti/app_test -cz app_test.20210403_002535_895.log app_test_sip-001.20210402_233559_293.log",
////                stdoutReader);
//        RemoteExecutionResult remoteExecutionResult = cl.executePipedRemoteCommand(
//                "ssydoruk",
//                "pq1617uw",
//                "192.168.1.69",
//                22,
//                "hostname",
//                null);
//        System.out.println("All done. "+remoteExecutionResult.toString());
//    }
}
