package wohl.posthouse.util.intf;

public interface RemoteDictModify {
    public abstract boolean modifyString(String key, String value);
    public abstract boolean modifyTTL(String key, long ttl);
}
