package org.posthouse.util.impl;

import org.jentiti.annotation.Singleton;
import org.posthouse.store.RemoteDictStore;
import org.posthouse.util.intf.RemoteDictModify;

import java.util.Deque;
import java.util.Map;
import java.util.Set;

@Singleton("remoteDictModify")
public class DefaultRemoteDictModify implements RemoteDictModify {

    @Override
    public boolean modifyString(String key, String value) {
        if(RemoteDictStore.exist(key)) {
            RemoteDictStore.stringMap.replace(key, value);
            return true;
        }
        return false;
    }

    @Override
    public boolean modifyMap(String key, Map<String, String> map) {
        if (RemoteDictStore.exist(key)) {
            RemoteDictStore.hashMap.replace(key, map);
            return true;
        }
        return false;
    }

    @Override
    public boolean modifyDeque(String key, Deque<String> deque) {
        if (RemoteDictStore.exist(key)) {
            RemoteDictStore.dequeMap.replace(key, deque);
            return true;
        }
        return false;
    }

    @Override
    public boolean modifySet(String key, Set<String> set) {
        if (RemoteDictStore.exist(key)) {
            RemoteDictStore.setMap.replace(key, set);
            return true;
        }
        return false;
    }

    @Override
    public boolean modifyTTL(String key, long expTimeStamp) {
        if(RemoteDictStore.exist(key)) {
            RemoteDictStore.addExpKey(key, expTimeStamp);
            return true;
        }
        return false;
    }
}
