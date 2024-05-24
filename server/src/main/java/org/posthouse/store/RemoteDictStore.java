package org.posthouse.store;

import org.posthouse.util.BasicDataType;

import java.util.Deque;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RemoteDictStore {
    public static Map<String, BasicDataType> keySet = new ConcurrentHashMap<>();
    public static Map<String, Long> keyExpMap = new ConcurrentHashMap<>();

    public static Map<String, String> stringMap = new ConcurrentHashMap<>();
    public static Map<String, Map<String, String>> hashMap = new ConcurrentHashMap<>();
    public static Map<String, Deque<String>> dequeMap = new ConcurrentHashMap<>();
    public static Map<String, Set<String>> setMap = new ConcurrentHashMap<>();

    public static boolean exist(String key) {
        return keySet.containsKey(key);
    }
    public static void addExpKey(String key, Long expTimeStamp) {
        keyExpMap.put(key + BasicDataType.delimiter + keySet.get(key).toString(), expTimeStamp);
    }
    public static boolean removeExpKey(String key) {
        if(exist(key)) {
            String expKey = key + BasicDataType.delimiter + keySet.get(key).toString();
            keyExpMap.remove(expKey);
            return true;
        }
        return false;
    }
}
