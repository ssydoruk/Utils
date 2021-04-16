package Utils.UnixProcess;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

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

    @Override
    public String toString() {
        return "ret: "+retCode+
                " stdout:["+((stdout==null || stdout.isEmpty())?"<Empty>": StringUtils.join(stdout, "\n"))+"]"+
                " stdout:["+((stderr==null || stderr.isEmpty())?"<Empty>": StringUtils.join(stderr, "\n"))+"]";
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
