package com.wohl.posthouse.util;

import com.wohl.posthouse.store.RemoteDictStore;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GarbageCollector {
    public static void flush() {
        long now = System.currentTimeMillis();
        RemoteDictStore.keyExpMap.forEach((k, v) -> {

            String[] tokens = k.split(BasicDataType.delimiter);
            BasicDataType dataType = BasicDataType.parse(tokens[1]);
            String key = tokens[0];

            if(!RemoteDictStore.exist(key))
                return;

            // key timeout
            if(v <= now) {
                RemoteDictStore.keyExpMap.remove(k);
                RemoteDictStore.keySet.remove(key);

                if(dataType == BasicDataType.STRING)
                    RemoteDictStore.stringMap.remove(key);
                else if(dataType == BasicDataType.HASH)
                    RemoteDictStore.hashMap.remove(key);
                else if(dataType == BasicDataType.DEQUE)
                    RemoteDictStore.dequeMap.remove(key);
                else if(dataType == BasicDataType.SET)
                    RemoteDictStore.setMap.remove(key);
                log.info("key \"" + key + "\"" + " expired");
                log.debug("key="+RemoteDictStore.keySet);
                log.debug("expiring_key="+RemoteDictStore.keyExpMap);
            }
        });
    }
}
