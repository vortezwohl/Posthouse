package com.wohl.posthouse.util.impl;

import com.wohl.posthouse.util.BasicDataType;
import com.wohl.posthouse.util.intf.RemoteDictCreate;
import lombok.extern.log4j.Log4j2;
import com.wohl.posthouse.store.RemoteDictStore;
import org.jentiti.annotation.Singleton;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentSkipListSet;

@Log4j2
@Singleton("remoteDictCreate")
public class DefaultRemoteDictCreate implements RemoteDictCreate {

    @Override
    public Map.Entry createString(String key, String value) {
        if(RemoteDictStore.exist(key))
            return null;
        else {
            RemoteDictStore.keySet.put(key, BasicDataType.STRING);
            Map.Entry<String, String> entry = new AbstractMap.SimpleEntry<>(key, value);
            RemoteDictStore.stringMap.put(key, value);
            return entry;
        }
    }

    @Override
    public Map.Entry createMap(String key) {
        if(RemoteDictStore.exist(key))
            return null;
        else {
            RemoteDictStore.keySet.put(key, BasicDataType.HASH);
            Map<String, String> map = new ConcurrentHashMap<>();
            Map.Entry<String, Map> entry = new AbstractMap.SimpleEntry<>(key, map);
            RemoteDictStore.hashMap.put(key, map);
            return entry;
        }
    }

    @Override
    public Map.Entry createMapEntry(String key, String field, String value) {
        Map.Entry<String, String> mapEntry = new AbstractMap.SimpleEntry<>(field, value);
        if(RemoteDictStore.hashMap.get(key) != null)
            RemoteDictStore.hashMap.get(key).put(field, value);
        else {
            if(RemoteDictStore.exist(key))
                return null;
            else {
                RemoteDictStore.keySet.put(key, BasicDataType.HASH);
                Map<String, String> newMap = new ConcurrentHashMap<>();
                RemoteDictStore.hashMap.put(key, newMap);
                RemoteDictStore.hashMap.get(key).put(field, value);
            }
        }
        return mapEntry;
    }

    @Override
    public Map.Entry createDeque(String key) {
        if(RemoteDictStore.exist(key))
            return null;
        else {
            RemoteDictStore.keySet.put(key, BasicDataType.DEQUE);
            Deque<String> deque = new ConcurrentLinkedDeque<>();
            Map.Entry<String, Deque> dequeEntry = new AbstractMap.SimpleEntry<>(key, deque);
            RemoteDictStore.dequeMap.put(key, deque);
            return dequeEntry;
        }
    }

    @Override
    public Map.Entry createDequeItem(String key, String value) {
        Map.Entry<String, String> dequeItem = new AbstractMap.SimpleEntry<>(key, value);
        if(RemoteDictStore.dequeMap.get(key) != null)
            RemoteDictStore.dequeMap.get(key).addLast(value);
        else {
            if(RemoteDictStore.exist(key))
                return null;
            else {
                RemoteDictStore.keySet.put(key, BasicDataType.DEQUE);
                Deque<String> newDeque = new ConcurrentLinkedDeque<>();
                RemoteDictStore.dequeMap.put(key, newDeque);
                RemoteDictStore.dequeMap.get(key).addLast(value);
            }
        }
        return dequeItem;
    }

    @Override
    public Map.Entry createSet(String key) {
        if(RemoteDictStore.exist(key))
            return null;
        else {
            RemoteDictStore.keySet.put(key, BasicDataType.SET);
            Set<String> set = new ConcurrentSkipListSet<>();
            Map.Entry<String, Set> setEntry = new AbstractMap.SimpleEntry<>(key, set);
            RemoteDictStore.setMap.put(key, set);
            return setEntry;
        }
    }

    @Override
    public Map.Entry createSetItem(String key, String value) {
        Map.Entry<String, String> setItem = new AbstractMap.SimpleEntry<>(key, value);
        if(RemoteDictStore.setMap.get(key) != null)
            RemoteDictStore.setMap.get(key).add(value);
        else {
            if(RemoteDictStore.exist(key))
                return null;
            else {
                RemoteDictStore.keySet.put(key, BasicDataType.SET);
                Set<String> newSet = new ConcurrentSkipListSet<>();
                RemoteDictStore.setMap.put(key, newSet);
                RemoteDictStore.setMap.get(key).add(value);
            }
        }
        return setItem;
    }
}
