package wohl.posthouse.store;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class PostmanRegistrationStore {
    public static Set<String> keySet = new CopyOnWriteArraySet<>();
    public static Map<String, String> publicKeyMap = new ConcurrentHashMap<>();

    public static boolean exist(String key) {
        return keySet.contains(key);
    }
}
