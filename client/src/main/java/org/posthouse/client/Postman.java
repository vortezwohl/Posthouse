package org.posthouse.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.SneakyThrows;
import org.posthouse.client.impl.SecurePostman;

import java.io.Closeable;
import java.util.Deque;
import java.util.Map;
import java.util.Set;

/**
 * @Author 吴子豪
 */
public interface Postman extends Closeable {
    @SneakyThrows
    static Postman hire(String serverHost, int serverPort) {
        return new SecurePostman(serverHost, serverPort);
    }

    @SneakyThrows
    static Postman hire(String serverHost) {
        return new SecurePostman(serverHost);
    }

    void fire() throws InterruptedException;

    boolean createString(String key, String value, long ttl) throws InterruptedException;

    boolean createString(String key, String value) throws InterruptedException;

    boolean createMap(String key, long ttl) throws InterruptedException;

    boolean createMap(String key) throws InterruptedException;

    boolean createMapEntry(String key, String field, String value, long ttl) throws InterruptedException;

    boolean createMapEntry(String key, String field, String value) throws InterruptedException;

    boolean createDeque(String key, long ttl) throws InterruptedException;

    boolean createDeque(String key) throws InterruptedException;

    boolean createDequeItem(String key, String value, long ttl) throws InterruptedException;

    boolean createDequeItem(String key, String value) throws InterruptedException;

    boolean createSet(String key, long ttl) throws InterruptedException;

    boolean createSet(String key) throws InterruptedException;

    boolean createSetItem(String key, String value, long ttl) throws InterruptedException;

    boolean createSetItem(String key, String value) throws InterruptedException;

    boolean createObject(String key, Object object, long ttl) throws JsonProcessingException, InterruptedException;

    boolean createObject(String key, Object object) throws InterruptedException;

    boolean updateString(String key, String value) throws InterruptedException;

    boolean updateMap(String key, Map map) throws InterruptedException;

    boolean updateDeque(String key, Deque deque) throws InterruptedException;

    boolean updateSet(String key, Set set) throws InterruptedException;

    boolean expire(String key, long ttl) throws InterruptedException;

    boolean expire(String key) throws InterruptedException;

    boolean removeMapEntry(String key, String field) throws InterruptedException;

    boolean removeDequeItemAtFirstOccurrence(String key, String value) throws InterruptedException;

    boolean removeSetItem(String key, String value) throws InterruptedException;

    boolean removeKey(String key) throws InterruptedException;

    boolean remove(String key) throws InterruptedException;

    Map getKeySet() throws InterruptedException;

    Map getKeyExpirationSet() throws InterruptedException;

    String readKey(String key) throws InterruptedException;

    String get(String key) throws InterruptedException;

    Object getObject(String key, Class clazz) throws InterruptedException;

    String getString(String key) throws InterruptedException;

    Map<String, String> getMap(String key) throws InterruptedException;

    Deque<String> getDeque(String key) throws InterruptedException;

    Set<String> getSet(String key) throws InterruptedException;

    boolean thereIs(String key) throws InterruptedException;
}
