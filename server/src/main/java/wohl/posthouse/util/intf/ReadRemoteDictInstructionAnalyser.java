package wohl.posthouse.util.intf;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface ReadRemoteDictInstructionAnalyser {
    public abstract String exec(String instruction) throws JsonProcessingException;
    public abstract boolean isReadReq(String request);
}
