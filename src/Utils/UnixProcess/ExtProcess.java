/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utils.UnixProcess;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author stepansydoruk
 */
public class ExtProcess {

    private ProcessBuilder pb;
    private String cmd;

    public String getCmd() {
        return cmd;
    }

    Process proc = null;
    private boolean saveStdErr = false;
    private boolean saveStdOut = false;
    private int exitCode;
    private Future<?> stdInFuture;
    private Future<?> stdErrFuture;
    private ExtProcess otherProc;
    private Future<?> pipeFuture;
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger();

    public ExtProcess(List<String> tarParams) throws IOException {
        cmd = tarParams.get(0);
        pb = getProcessBuilder(tarParams);
        logger.trace("Working directory :" + pb.directory());

    }

    public ExtProcess(ArrayList<String> tarParams, ExtProcess procSSH) throws IOException {
        this(tarParams);
        otherProc = procSSH;

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

    ThreadedReader stdIn;
    ThreadedReader stdErr;

    private static final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

    public void startProcess(boolean saveStdOut, boolean saveStdErr) throws IOException {
        proc = pb.start();
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
        closeStreams();
    }

    public int waitFor() throws InterruptedException {
        exitCode = proc.waitFor();
        logger.debug("Main process terminated with code " + exitCode);
        try {
            stdErrFuture.get();
        } catch (ExecutionException ex) {
            Logger.getLogger(ExtProcess.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            stdInFuture.get();
        } catch (ExecutionException ex) {
            Logger.getLogger(ExtProcess.class.getName()).log(Level.SEVERE, null, ex);
        }
        closeStreams();
        logger.debug("Ret code: " + exitCode);
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
            logger.log(org.apache.logging.log4j.Level.FATAL, ex);
        }
        return null;
    }

    private IProcessOutputRead stderrReadProc;
    private IProcessOutputRead stdinReadProc;

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

    public void cancel() {
        proc.destroyForcibly();
        terminateChildren();
    }

    public static interface IProcessOutputRead {

        void lineRead(String s);
    };

}
