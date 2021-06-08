/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utils.UnixProcess;

import org.apache.commons.lang3.StringUtils;
import org.apache.sshd.agent.SshAgent;
import org.apache.sshd.agent.unix.UnixAgentFactory;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ChannelDirectTcpip;
import org.apache.sshd.client.channel.ChannelShell;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.config.hosts.DefaultConfigFileHostEntryResolver;
import org.apache.sshd.client.config.hosts.HostConfigEntry;
import org.apache.sshd.client.config.hosts.HostConfigEntryResolver;
import org.apache.sshd.client.keyverifier.AcceptAllServerKeyVerifier;
import org.apache.sshd.client.session.ClientProxyConnector;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.forward.PortForwardingEventListener;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.common.util.net.SshdSocketAddress;
import org.apache.sshd.server.forward.AcceptAllForwardingFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author stepan_sydoruk
 */
public class SSHClientWrapperTest {
    private final long defaultTimeoutSeconds;

    public SshClient getClient() {
        return client;
    }

    public long getDefaultTimeoutSeconds() {
        return defaultTimeoutSeconds;
    }

    private Future<?> stdInFuture;
    private static final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

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

        synchronized public ClientSession getAuthenticatedSession(SSHClientWrapperTest wrapper, SSHServer server) throws Exception {
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

        private ClientSession startSession(SSHClientWrapperTest wrapper, SSHServer server) throws Exception {

            SshClient client = wrapper.getClient();

            HostConfigEntryResolver hostConfigEntryResolver = client.getHostConfigEntryResolver();
            HostConfigEntry hostConfigEntry = hostConfigEntryResolver.resolveEffectiveHost(server.getHost(), server.getPort(),
                    null, server.getUsername(), null);

//zusa-x-prodjumphost01.airbnb.biz
            ClientSession session;
//            if (hostConfigEntry != null) {
//                session = client.connect(hostConfigEntry).verify(wrapper.getDefaultTimeoutSeconds(), TimeUnit.SECONDS).getSession();
//
//            } else
            session = client.connect(server.getUsername(), "zusa-x-prodjumphost01.airbnb.biz", server.getPort())
                    .verify(wrapper.getDefaultTimeoutSeconds(), TimeUnit.SECONDS).getSession();

            session.auth().verify(wrapper.getDefaultTimeoutSeconds(), TimeUnit.SECONDS);
//---------------
//            session.addPortForwardingEventListener(new PortForwardingEventListener() {
//                @Override
//                public void establishedDynamicTunnel(Session session, SshdSocketAddress local,
//                                                     SshdSocketAddress boundAddress, Throwable reason) throws IOException {
//                    // TODO Auto-generated method stub
//                    PortForwardingEventListener.super.establishedDynamicTunnel(session, local, boundAddress, reason);
//                    System.out.println("Dynamic Forword Tunnel is Ready");
//                }
//            });
//
//            // this is original
//            SshdSocketAddress sshdSocketAddress = session
//                    .startRemotePortForwarding(new SshdSocketAddress(server.getHost(), 22),
//                            new SshdSocketAddress((InetSocketAddress) session.getIoSession().getLocalAddress()));
//
//            SshdSocketAddress sshdSocketAddress = session
//                    .startLocalPortForwarding(
//                            new SshdSocketAddress(server.getHost(), 22),
//                            new SshdSocketAddress((InetSocketAddress) session.getIoSession().getRemoteAddress()))
//                            ;
//
//
//            System.out.println("Host: " + sshdSocketAddress.getHostName());
//            System.out.println("Port: " + sshdSocketAddress.getPort());
////            // Create a Proxy object to work with
//            Proxy proxy = new Proxy(Proxy.Type.DIRECT,
//                    new InetSocketAddress(sshdSocketAddress.getHostName(), sshdSocketAddress.getPort()));
//--------------

//            ClientConnectionService ccs = new ClientConnectionService((AbstractClientSession) session);
//            AgentServerProxy asp = new AgentServerProxy(ccs);
//
//            //            AgentForwardSupport agentForwardSupport = ccs.getAgentForwardSupport();
//            ClientSession session1 = client.connect(server.getUsername(), server.getHost(), server.getPort())
//                    .verify(wrapper.getDefaultTimeoutSeconds(), TimeUnit.SECONDS).getSession();

//            ChannelDirectTcpip directTcpipChannel = session.createDirectTcpipChannel(new SshdSocketAddress((InetSocketAddress) session.getIoSession().getLocalAddress()),
//                    new SshdSocketAddress(server.getHost(), 22));
            ChannelShell shellChannel = session.createShellChannel();


            return session;

        }
    }

    private static SshClient client = null;

    SSHSessions sshSessions = new SSHSessions();

    private static final long HEARTBEAT = TimeUnit.SECONDS.toMillis(2L);

    public SSHClientWrapperTest(long defaultTimeoutSeconds) throws IOException {
        client = SshClient.setUpDefaultClient();
//        PropertyResolverUtils.updateProperty(client, ClientFactoryManager.HEARTBEAT_INTERVAL, HEARTBEAT);
//        PropertyResolverUtils.updateProperty(client, ClientFactoryManager.SOCKET_KEEPALIVE, true);
//        client.setSessionHeartbeat(SessionHeartbeatController.HeartbeatType.IGNORE, TimeUnit.MILLISECONDS, 500);
//        client.setAgentFactory(new LocalAgentFactory());
//        client.setServerKeyVerifier(AcceptAllServerKeyVerifier.INSTANCE);
//        client.setHostConfigEntryResolver(HostConfigEntryResolver.EMPTY);
//        client.setKeyIdentityProvider(KeyPairProvider.EMPTY_KEYPAIR_PROVIDER);

        client.setHostConfigEntryResolver(DefaultConfigFileHostEntryResolver.INSTANCE);
        client.setServerKeyVerifier(AcceptAllServerKeyVerifier.INSTANCE);

//        client.getProperties().putIfAbsent(ClientFactoryManager.HEARTBEAT_INTERVAL, TimeUnit.SECONDS.toMillis(10L));
//        client.getProperties().putIfAbsent(ClientFactoryManager.HEARTBEAT_REPLY_WAIT, TimeUnit.SECONDS.toMillis(5L));

        /*
        below block is experiment to try to authenticate via local ssh agent
*/

        String auth_sock = System.getenv().get("SSH_AUTH_SOCK");
        if (StringUtils.isNotBlank(auth_sock)) {
            client.getProperties().put(SshAgent.SSH_AUTHSOCKET_ENV_NAME, auth_sock);

//            UnixAgentFactory unixAgentFactory = new UnixAgentFactory();
//            SshAgent client1 = unixAgentFactory.createClient(client);

            client.setAgentFactory(new UnixAgentFactory());
        }
        client.setForwardingFilter(AcceptAllForwardingFilter.INSTANCE);
        client.setServerKeyVerifier(AcceptAllServerKeyVerifier.INSTANCE);

        client.start();

        this.defaultTimeoutSeconds = defaultTimeoutSeconds;
    }


    public RemoteExecutionResult executeRemoteCommand(String username, String password,
                                                      String host, int port, String command) throws Exception {

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

            String cmd="ssh "+username+"@"+host+ " \""+ command+"\"";
            try (ByteArrayOutputStream stdOutStream = new ByteArrayOutputStream(1024);
                 ByteArrayOutputStream stdErrStream = new ByteArrayOutputStream();
                 ClientChannel channel = session.createExecChannel(cmd)) {
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
    }

    public RemoteExecutionResult executePipedRemoteCommand(String username, String password,
                                                           String host, int port, String command,
                                                           ThreadedOutputStreamReader outputReader) throws Exception {

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

    public static void main(String[] args) throws Exception {

        // assume SLF4J is bound to logback in the current environment
        logger = LoggerFactory.getLogger(SSHClientWrapperTest.class);
        logger.debug("debug");
        logger.info("info");
        logger.error("error");

        SSHClientWrapperTest cl = new SSHClientWrapperTest(20000);
        ThreadedOutputStreamReader stdoutReader = new ThreadedUnTarGZ("/Users/stepan_sydoruk/tmp");
//        cl.executePipedRemoteCommand(
//                "ssydoruk",
//                "pq1617uw",
//                "192.168.64.10",
//                22,
//                "tar -C /applog/gcti/app_test -cz app_test.20210403_002535_895.log app_test_sip-001.20210402_233559_293.log",
//                stdoutReader);
//        cl.executePipedRemoteCommand(
//                "ssydoruk",
//                "pq1617uw",
//                "192.168.1.69",
//                22,
//                "tar -C /applog/gcti/app_test -cz app_test.20210403_002535_895.log app_test_sip-001.20210402_233559_293.log",
//                stdoutReader);
        RemoteExecutionResult remoteExecutionResult = cl.executeRemoteCommand(
                "stepan_sydoruk",
                "pq1617uw",
//                "zusa-x-prodjumphost01.airbnb.biz",
                "esv1-c-gvp-04t.airbnb.biz",
                22,
                "hostname; pwd; uname -a");
        System.out.println("All done. " + remoteExecutionResult.toString());
        System.exit(0);
    }
}
