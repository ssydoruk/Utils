package Utils.UnixProcess;

import java.util.List;

public class RemoteExecutionResult {
    private  List<String> stdout;
    private  List<String> stderr;

    public void setStdout(List<String> stdout) {
        this.stdout = stdout;
    }

    public void setStderr(List<String> stderr) {
        this.stderr = stderr;
    }

    public void setRetCode(int retCode) {
        this.retCode = retCode;
    }

    private  int retCode;

    public List<String> getStdout() {
        return stdout;
    }

    public List<String> getStderr() {
        return stderr;
    }

    public int getRetCode() {
        return retCode;
    }

    public RemoteExecutionResult() {
    }
}
