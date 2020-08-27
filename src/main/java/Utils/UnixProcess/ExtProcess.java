/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utils.UnixProcess;

import Utils.Pair;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author stepansydoruk
 */
public class ExtProcess {

    final static Logger logger = LoggerFactory.getLogger(ExtProcess.class);
    private static final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
    private static final Pattern sp = Pattern.compile("[^\\\\]\\s");

    private static ProcessBuilder getProcessBuilder(List<String> sshParameters) throws IOException {
        if (logger.isDebugEnabled()) {
            StringBuilder l = new StringBuilder();
            for (String sshParameter : sshParameters) {
                if (l.length() > 0) {
                    l.append(" ");
                }
                boolean quotes = false;
                if (sp.matcher(sshParameter).find()) {
                    quotes = true;
                }
                if (quotes) {
                    l.append("\"");
                }
                l.append(sshParameter);
                if (quotes) {
                    l.append("\"");
                }
            }
            logger.debug("Executing: [" + l + "]");
        }

        return new ProcessBuilder(sshParameters);

    }

    public static Pair<ArrayList<String>, ArrayList<String>> executeCommand(String key, boolean saveStdOut, boolean saveStdErr) throws IOException, InterruptedException {
        ArrayList<String> cmdParams = new ArrayList<>(Arrays.asList(StringUtils.split(key)));

        logger.debug("Executing [" + StringUtils.join(cmdParams, " "));
//        logger.trace("executing: " + rsyncParams);
        ExtProcess proc = new ExtProcess(cmdParams);
        proc.startProcess(saveStdOut, saveStdErr);
        int waitFor = proc.waitFor();
        logger.debug("process terminated, result: " + waitFor);

        return (proc.getExitCode() != 255 && (saveStdOut || saveStdErr))
                ? new Pair(proc.getSTDOut(), proc.getErrBuf()) : null;

    }

    private ProcessBuilder pb;
    private String cmd;
    private long procPID;

    Process proc = null;
    private boolean saveStdErr = false;
    private boolean saveStdOut = false;
    private int exitCode;
    private Future<?> stdInFuture;
    private Future<?> stdErrFuture;
    private ExtProcess otherProc;
    private Future<?> pipeFuture;
    ThreadedReader stdIn;
    ThreadedReader stdErr;
    private IProcessOutputRead stderrReadProc;
    private IProcessOutputRead stdinReadProc;

    public ExtProcess(List<String> tarParams) throws IOException {
        cmd = tarParams.get(0);
        pb = getProcessBuilder(tarParams);
        logger.trace("Working directory :" + pb.directory());

    }

    public ExtProcess(ArrayList<String> tarParams, ExtProcess procSSH) throws IOException {
        this(tarParams);
        otherProc = procSSH;

    }

    public String getCmd() {
        return cmd;
    }

    public ArrayList<String> getSTDOut() {
        synchronized (stdIn) {
            if (stdIn != null) {
                return stdIn.getOutBuf();
            } else {
                return null;
            }
        }
    }

    public ArrayList<String> getErrBuf() {
        synchronized (stdErr) {
            if (stdErr != null) {
                return stdErr.getOutBuf();
            } else {
                return null;
            }
        }
    }

    public void startProcess() throws IOException {
        startProcess(false, false);
    }

    public void startProcess(boolean saveStdOut, boolean saveStdErr) throws IOException {
        proc = pb.start();
        procPID = tryGetPid(proc);
        logger.info("started process pid: " + procPID);
        if (otherProc != null) {
            PipeConnector pc = new PipeConnector(otherProc.getInputStream(), proc.getOutputStream());
            pipeFuture = executor.submit(pc);
        } else {
            stdIn = new ThreadedReader((proc.getInputStream()), cmd, "in", saveStdOut);
            if (stdinReadProc != null) {
                stdIn.setstdinReadProc(stdinReadProc);
            }
            stdInFuture = executor.submit(stdIn);
        }
        stdErr = new ThreadedReader((proc.getErrorStream()), cmd, "err", saveStdErr);
        if (stderrReadProc != null) {
            stdErr.setstdinReadProc(stderrReadProc);
        }
        stdErrFuture = executor.submit(stdErr);

    }

    private InputStream getInputStream() {
        return proc.getInputStream();
    }

    private void closeStreams() {
        closeStream(proc.getInputStream());
        closeStream(proc.getErrorStream());
        closeStream(proc.getOutputStream());

    }

    private void terminateChildren() {
        stdInFuture.cancel(true);
        stdErrFuture.cancel(true);
        if (pipeFuture != null) {
            pipeFuture.cancel(true);
        }
        if (proc.isAlive()) {
            proc.destroyForcibly();
        }
        closeStreams();
    }

    public int waitFor() throws InterruptedException {
        return waitFor(-1, TimeUnit.MILLISECONDS);
    }

    public int waitFor(int cnt, TimeUnit tu) throws InterruptedException {
        if (proc.isAlive()) {
            if (cnt > 0) {
                try {
                    boolean isEnded = proc.waitFor(cnt, tu);
                } catch (InterruptedException interruptedException) {
                    exitCode = 255;
                }
            } else {
                exitCode = proc.waitFor();
            }
            logger.debug("Main process terminated with code " + exitCode);
            try {
                stdErrFuture.get(5, TimeUnit.SECONDS);
            } catch (ExecutionException ex) {
                logger.error("", ex);
            } catch (TimeoutException e) {
                logger.error("Timeout while waiting for StdErr to read", e);
                stdErrFuture.cancel(true);
            }
            try {
                stdInFuture.get(5, TimeUnit.SECONDS);
            } catch (ExecutionException ex) {
                logger.error("", ex);
            } catch (TimeoutException e) {
                logger.error("Timeout while waiting for StdIn to read", e);
                stdInFuture.cancel(true);
            }
            closeStreams();
            logger.debug("Ret code: " + exitCode);
        }

        return exitCode;
    }

    public int getExitCode() {
        return exitCode;
    }

    public List<String> execOuts() {

        try {
            exitCode = waitFor();
            return getSTDOut();
        } catch (InterruptedException ex) {
            logger.error("", ex);
        }
        return null;
    }

    public IProcessOutputRead getStderrReadProc() {
        return stderrReadProc;
    }

    public void setStderrReadProc(IProcessOutputRead stderrReadProc) {
        this.stderrReadProc = stderrReadProc;
    }

    public IProcessOutputRead getStdinReadProc() {
        return stdinReadProc;
    }

    public void setStdinReadProc(IProcessOutputRead stdinReadProc) {
        this.stdinReadProc = stdinReadProc;
    }

    private void closeStream(Closeable stream) {
        try {
            stream.close();
        } catch (IOException ex) {
//            Logger.getLogger(ExtProcess.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public long tryGetPid(Process p) {
        long pid = -1;

        try {
            //for windows
            if (p.getClass().getName().equals("java.lang.Win32Process") || p.getClass().getName().equals("java.lang.ProcessImpl")) {
                Field f = p.getClass().getDeclaredField("handle");
                f.setAccessible(true);
                long handl = f.getLong(p);
                Kernel32 kernel = Kernel32.INSTANCE;
                WinNT.HANDLE hand = new WinNT.HANDLE();
                hand.setPointer(Pointer.createConstant(handl));
                pid = kernel.GetProcessId(hand);
                f.setAccessible(false);
            } //for unix based operating systems
            else if (p.getClass().getName().equals("java.lang.UNIXProcess")) {
                Field f = p.getClass().getDeclaredField("pid");
                f.setAccessible(true);
                pid = f.getLong(p);
                f.setAccessible(false);
            }
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException ex) {
            pid = -1;
        }

        return pid;
    }

    private void killByPID(long uccPid) {
        if (uccPid < 0) {
            logger.error("Cannot kill UCC by PID. PID not set.");
            return;
        }
//        synchronized (spawnProcessMutex) {
//            JavaSysMon monitor = new JavaSysMon();
//            monitor.killProcessTree(uccPid, false);
//        }
    }

    public void cancel() {
        if (proc.isAlive()) {
            proc.destroy();
            logger.info("destroying proc " + proc.toString());
            killByPID(procPID);
        }
        terminateChildren();
    }

    public static interface IProcessOutputRead {

        void lineRead(String s);
    };

}
