package org.posthouse.util.intf;

import java.util.Deque;
import java.util.Map;
import java.util.Set;

public interface RemoteDictModify {
    public abstract boolean modifyString(String key, String value);

    public abstract boolean modifyMap(String key, Map map);

    public abstract boolean modifyDeque(String key, Deque deque);

    public abstract boolean modifySet(String key, Set set);
    public abstract boolean modifyTTL(String key, long ttl);
}
