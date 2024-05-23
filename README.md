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

## Example
```java
import com.wohl.posthouse.client.Postman;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Postman postman = Postman.hire("127.0.0.1");
        postman.cst("key", "hello world");
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
