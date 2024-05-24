package org.posthouse.util.intf;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface RemoteDictRead {
    public abstract String readKeys() throws JsonProcessingException;
    public abstract String readKeyExp() throws JsonProcessingException;
    public abstract String readKey(String key) throws JsonProcessingException;
}
