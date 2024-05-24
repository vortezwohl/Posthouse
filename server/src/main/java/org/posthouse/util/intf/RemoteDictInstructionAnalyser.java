package org.posthouse.util.intf;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface RemoteDictInstructionAnalyser {
    public abstract boolean exec(String instruction) throws JsonProcessingException;
}
