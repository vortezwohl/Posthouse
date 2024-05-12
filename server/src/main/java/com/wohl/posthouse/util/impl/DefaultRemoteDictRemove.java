package com.wohl.posthouse.util.impl;

import com.wohl.posthouse.store.RemoteDictStore;
import com.wohl.posthouse.util.BasicDataType;
import com.wohl.posthouse.util.intf.RemoteDictRemove;
import org.jentiti.annotation.Singleton;

@Singleton("remoteDictRemove")
public class DefaultRemoteDictRemove implements RemoteDictRemove {
    @Override
    public boolean removeHashItem(String key, String field) {
        if(RemoteDictStore.hashMap.containsKey(key)) {
            RemoteDictStore.hashMap.get(key).remove(field);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeDequeItem(String key, String value) {
        if(RemoteDictStore.dequeMap.containsKey(key)) {
            RemoteDictStore.dequeMap.get(key).removeFirstOccurrence(value);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeSetItem(String key, String value) {
        if(RemoteDictStore.setMap.containsKey(key)) {
            RemoteDictStore.setMap.get(key).remove(value);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeKey(String key) {
        if(RemoteDictStore.exist(key)) {
            BasicDataType dataType = RemoteDictStore.keySet.get(key);
            RemoteDictStore.keySet.remove(key);
            RemoteDictStore.keyExpMap.remove(key);
            if(dataType == BasicDataType.STRING) {
                RemoteDictStore.stringMap.remove(key);
                return true;
            }
            else if(dataType == BasicDataType.HASH) {
                RemoteDictStore.hashMap.remove(key);
                return true;
            }
            else if(dataType == BasicDataType.DEQUE) {
                RemoteDictStore.dequeMap.remove(key);
                return true;
            }

            else if(dataType == BasicDataType.SET) {
                RemoteDictStore.setMap.remove(key);
                return true;
            }
            else
                return false;
        }
        else
            return false;
    }
}
