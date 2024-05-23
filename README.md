# Posthouse

Posthouse is a NoSQL database and an efficient cache for softwares which require large data access operations.

## Key features

- High speed IO operations (less then 5ms per request)
- High availability
- Data integrity protection
- Data persistence
- Eazy to use

## Quick guide

```
Instructions:
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

```java
public interface Postman {
    @SneakyThrows
    public static Postman hire(String serverHost, int serverPort) {
        return new RSASignedPostman(serverHost, serverPort);
    }
    @SneakyThrows
    public static Postman hire(String serverHost) {
        return new RSASignedPostman(serverHost);
    }
    void fire() throws InterruptedException;
    boolean cst(String k, String v, long ttl) throws InterruptedException;
    boolean cst(String k, String v) throws InterruptedException;
    boolean cha(String k, long ttl) throws InterruptedException;
    boolean cha(String k) throws InterruptedException;
    boolean chai(String k, String field, String value, long ttl) throws InterruptedException;
    boolean chai(String k, String field, String value) throws InterruptedException;
    boolean cde(String k, long ttl) throws InterruptedException;
    boolean cde(String k) throws InterruptedException;
    boolean cdei(String k, String v, long ttl) throws InterruptedException;
    boolean cdei(String k, String v) throws InterruptedException;
    boolean cse(String k, long ttl) throws InterruptedException;
    boolean cse(String k) throws InterruptedException;
    boolean csei(String k, String v, long ttl) throws InterruptedException;
    boolean csei(String k, String v) throws InterruptedException;
    boolean mst(String k, String v) throws InterruptedException;
    boolean exp(String k, long ttl) throws InterruptedException;
    boolean exp(String k) throws InterruptedException;
    boolean dhai(String k, String field) throws InterruptedException;
    boolean ddei(String k, String v) throws InterruptedException;
    boolean dsei(String k, String v) throws InterruptedException;
    boolean dk(String k) throws InterruptedException;
    boolean remove(String k) throws InterruptedException;
    String rks() throws InterruptedException;
    String rkes() throws InterruptedException;
    String rk(String k) throws InterruptedException;
    String get(String k) throws InterruptedException;
}
```
