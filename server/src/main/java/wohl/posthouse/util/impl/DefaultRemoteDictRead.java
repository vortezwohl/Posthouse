package wohl.posthouse.util.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jentiti.annotation.Singleton;
import wohl.posthouse.store.RemoteDictStore;
import wohl.posthouse.util.BasicDataType;
import wohl.posthouse.util.intf.RemoteDictRead;

@Singleton("remoteDictRead")
public class DefaultRemoteDictRead implements RemoteDictRead {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String readKeys() throws JsonProcessingException {
        return objectMapper.writeValueAsString(RemoteDictStore.keySet);
    }

    @Override
    public String readKeyExp() throws JsonProcessingException {
        return objectMapper.writeValueAsString(RemoteDictStore.keyExpMap);
    }

   public String readKey(String key) throws JsonProcessingException {
        if(RemoteDictStore.exist(key)) {
            BasicDataType dataType = RemoteDictStore.keySet.get(key);
            if(dataType == BasicDataType.STRING)
                return objectMapper.writeValueAsString(RemoteDictStore.stringMap.get(key));
            else if(dataType == BasicDataType.HASH)
                return objectMapper.writeValueAsString(RemoteDictStore.hashMap.get(key));
            else if(dataType == BasicDataType.DEQUE)
                return objectMapper.writeValueAsString(RemoteDictStore.dequeMap.get(key));
            else if(dataType == BasicDataType.SET)
                return objectMapper.writeValueAsString(RemoteDictStore.setMap.get(key));
            else
                return null;
        }
        else
            return null;
   }
}
