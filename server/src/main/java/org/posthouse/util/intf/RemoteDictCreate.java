package org.posthouse.util.intf;

import java.util.Map;

public interface RemoteDictCreate {
    public abstract Map.Entry createString(String key, String value);
    public abstract Map.Entry createMap(String key);
    public abstract Map.Entry createMapEntry(String key, String field, String value);
    public abstract Map.Entry createDeque(String key);
    public abstract Map.Entry createDequeItem(String key, String value);
    public abstract Map.Entry createSet(String key);
    public abstract Map.Entry createSetItem(String key, String value);
}
