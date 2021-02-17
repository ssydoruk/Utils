/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utils.UnixProcess;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author stepansydoruk
 */
public class ThreadedReader implements Runnable {

    final static Logger logger = LoggerFactory.getLogger(ThreadedReader.class);

    private final BufferedReader stream; // no need to buffer it
    private final String cmd;
    private final String streamName;
    private boolean saveOutput = false;
    private ArrayList<String> outBuf;
    private ExtProcess.IProcessOutputRead stdinReadProc;

    public ThreadedReader(InputStream in, String cmd, String stream) {
        this.stream = new BufferedReader(new InputStreamReader(in));
        this.cmd = cmd;
        this.streamName = stream;
        logger.debug(thrID() +"started reader for cmd: " + cmd + " stream:" + streamName);
    }

    ThreadedReader(InputStream in, String cmd, String stream, boolean saveStdOut) {
        this(in, cmd, stream);
        this.saveOutput = saveStdOut;
        this.outBuf = new ArrayList<>();
    }

    public ArrayList<String> getOutBuf() {
        return outBuf;
    }

    @Override
    public void run() {
        if (stream != null) {
                    logger.debug(thrID() +"run cmd: " + cmd + " stream:" + streamName);

            String s;
            try {
                synchronized (this) {
                    while ((s = stream.readLine()) != null) {
                        logger.debug(thrID() + cmd + "_" + streamName + ": " + s);
                        if (saveOutput) {
                            synchronized (outBuf) {
                                outBuf.add(s);
                            }
                        }
                        if (stdinReadProc != null) {
                            stdinReadProc.lineRead(s);
                        }
                    }
                }
            } catch (IOException ex) {
                logger.error("", ex);
            }
            logger.debug(thrID() + cmd + "_" + streamName + ": exited");
        }
    }

    void setstdinReadProc(ExtProcess.IProcessOutputRead stdinReadProc) {
        this.stdinReadProc = stdinReadProc;

    }

    private String thrID() {
        return "thr:" + Thread.currentThread().getId() + " ";
    }

}
