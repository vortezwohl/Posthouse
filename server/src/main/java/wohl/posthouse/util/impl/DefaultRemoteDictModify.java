package wohl.posthouse.util.impl;

import org.jentiti.annotation.Singleton;
import wohl.posthouse.store.RemoteDictStore;
import wohl.posthouse.util.intf.RemoteDictModify;

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
    public boolean modifyTTL(String key, long expTimeStamp) {
        if(RemoteDictStore.exist(key)) {
            RemoteDictStore.addExpKey(key, expTimeStamp);
            return true;
        }
        return false;
    }
}
