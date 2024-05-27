package org.posthouse.util.intf;

import java.util.Deque;
import java.util.Map;
import java.util.Set;

public interface RemoteDictModify {
    public abstract boolean modifyString(String key, String value);

    public abstract boolean modifyMap(String key, Map<String, String> map);

    public abstract boolean modifyDeque(String key, Deque<String> deque);

    public abstract boolean modifySet(String key, Set<String> set);
    public abstract boolean modifyTTL(String key, long ttl);
}
