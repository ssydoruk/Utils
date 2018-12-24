/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utils.UnixProcess;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author stepansydoruk
 */
public class ExtProcess extends Thread {

    private ProcessBuilder pb;
    String cmd;

    Process proc = null;
    private boolean saveStdErr = false;
    private boolean saveStdOut = false;
    private int exitCode;

    public ExtProcess(List<String> tarParams) throws IOException {
        cmd = tarParams.get(0);
        pb = getProcessBuilder(tarParams);
        LogManager.getLogger().trace("Working directory :" + pb.directory());

    }

    public ExtProcess(ArrayList<String> tarParams, ExtProcess procSSH) throws IOException {
        this(tarParams);

        PipeConnector pc = new PipeConnector(procSSH.getInputStream(), proc.getOutputStream());
        pc.run();

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

    public void startProcess(boolean saveStdOut, boolean saveStdErr) throws IOException {
        proc = pb.start();
        stdIn = new ThreadedReader((proc.getInputStream()), cmd, "in", saveStdOut);
        stdIn.start();
        stdErr = new ThreadedReader((proc.getErrorStream()), cmd, "err", saveStdErr);
        stdErr.start();
    }

    private static final Pattern sp = Pattern.compile("[^\\\\]\\s");

    private static ProcessBuilder getProcessBuilder(List<String> sshParameters) throws IOException {
        if (LogManager.getLogger(ThreadedReader.class.getPackage().getName()).isDebugEnabled()) {
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
            LogManager.getLogger(ThreadedReader.class.getPackage().getName()).debug("Executing: [" + l + "]");
        }

        return new ProcessBuilder(sshParameters);

    }

    private InputStream getInputStream() {
        return proc.getInputStream();
    }

    public int waitFor() throws InterruptedException {
        exitCode = proc.waitFor();
        stdIn.interrupt();
        stdErr.interrupt();
        LogManager.getLogger().debug("Ret code: " + exitCode);
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
            Logger.getLogger(ExtProcess.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public void run() {
        try {
            startProcess();
        } catch (IOException ex) {
            Logger.getLogger(ExtProcess.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void startThread() {
        start();
    }

}
