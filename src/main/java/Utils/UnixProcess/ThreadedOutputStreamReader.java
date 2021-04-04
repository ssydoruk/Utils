package Utils.UnixProcess;

import java.io.OutputStream;

public interface ThreadedOutputStreamReader extends Runnable{
    public OutputStream getOutputStream();
}
