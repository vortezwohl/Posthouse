package com.wohl.posthouse.util.intf;

public interface RemoteDictRemove {
    public abstract boolean removeHashItem(String key, String field);
    // remove when first occurs
    public abstract boolean removeDequeItem(String key, String value);
    public abstract boolean removeSetItem(String key, String value);
    public abstract boolean removeKey(String key);
}
