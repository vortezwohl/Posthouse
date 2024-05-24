# Posthouse

Posthouse is a NoSQL database and an efficient cache for softwares which require large data access operations.

## Key features

- High speed IO operations (less then 5ms per request)
- High availability
- Data integrity protection
- Data persistence
- Easy to use

## Posthouse configuration

```yaml
# posthouse.yaml
server:
  port: 2386
  persistence:
    enable: true
    retain: true
```

# Postman

Postman is a implementation of client for Posthouse

## Quick guide

```java
package wohl.posthouse.client;

import lombok.SneakyThrows;
import wohl.posthouse.client.impl.RSASignedPostman;

public interface Postman {
    @SneakyThrows
     static Postman hire(String serverHost, int serverPort) {
        return new RSASignedPostman(serverHost, serverPort);
    }
    @SneakyThrows
     static Postman hire(String serverHost) {
        return new RSASignedPostman(serverHost);
    }
    void fire() throws InterruptedException;
    boolean createString(String k, String v, long ttl) throws InterruptedException;
    boolean createString(String k, String v) throws InterruptedException;
    boolean createMap(String k, long ttl) throws InterruptedException;
    boolean createMap(String k) throws InterruptedException;
    boolean createMapEntry(String k, String field, String value, long ttl) throws InterruptedException;
    boolean createMapEntry(String k, String field, String value) throws InterruptedException;
    boolean createDeque(String k, long ttl) throws InterruptedException;
    boolean createDeque(String k) throws InterruptedException;
    boolean createDequeItem(String k, String v, long ttl) throws InterruptedException;
    boolean createDequeItem(String k, String v) throws InterruptedException;
    boolean createSet(String k, long ttl) throws InterruptedException;
    boolean createSet(String k) throws InterruptedException;
    boolean createSetItem(String k, String v, long ttl) throws InterruptedException;
    boolean createSetItem(String k, String v) throws InterruptedException;
    boolean modifyString(String k, String v) throws InterruptedException;
    boolean expire(String k, long ttl) throws InterruptedException;
    boolean expire(String k) throws InterruptedException;
    boolean removeMapEntry(String k, String field) throws InterruptedException;
    boolean removeDequeItemAtFirstOccurrence(String k, String v) throws InterruptedException;
    boolean removeSetItem(String k, String v) throws InterruptedException;
    boolean removeKey(String k) throws InterruptedException;
    boolean remove(String k) throws InterruptedException;
    String getKeySet() throws InterruptedException;
    String getKeyExpirationSet() throws InterruptedException;
    String readKey(String k) throws InterruptedException;
    String get(String k) throws InterruptedException;
}
```

```
Basic instructions:
hire: hire a Postman from Posthouse
fire: fire the Postman you hired in this session
cst: create string
cha: create hash map
chai: create entry in hash map
cde: create deque
cdei: create item in deque
cse: create set
csei: create item in set
mst: modify string
exp: set expiration of a kay
dhai: remove an entry in hash map
ddei: remove an item in deque
dsei: remove an item in set
dk: remove a key
remove: remove a key
rks: read all keys
rkes: read all expiring keys
rk: read value of a key
get: read value of a key
```

## Example
```java
import wohl.posthouse.client.Postman;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Postman postman = Postman.hire("127.0.0.1");
        postman.createString("key", "hello world");
        String res = postman.get("key");
        System.out.println(res);
        postman.remove("key");
        postman.fire();
    }
}
```

```
hello world
```
