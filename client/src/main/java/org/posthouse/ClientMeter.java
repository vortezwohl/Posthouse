package org.posthouse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NoArgsConstructor;
import org.posthouse.client.Postman;
import org.testng.annotations.Test;

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

@NoArgsConstructor
public class ClientMeter {

    @Test
    public void test0() throws InterruptedException {
        long beforeConnect = System.currentTimeMillis();
        Postman postman = Postman.hire("127.0.0.1");
        long afterConnect = System.currentTimeMillis();
        System.out.println("Connection time: "+ (afterConnect - beforeConnect) / 1000.0 + "s");

        long start = System.currentTimeMillis();
        postman.createString("str1", "str1");
        String res = postman.get("str1");
        postman.expire("str1");
        long end = System.currentTimeMillis();
        System.out.println("First request time: "+(end - start) / 1000.0 + "s");

        LinkedList<Double> linkedList = new LinkedList<>();
        for (int i = 0; i < 3; i++) {
            start = System.currentTimeMillis();
            postman.createString("str1", "str1");
            res = postman.get("str1");
            postman.expire("str1");
            postman.remove("str1");
            end = System.currentTimeMillis();
            linkedList.addLast((end - start) / 1000.0);
        }
        double sum = 0;
        for(double d : linkedList) {
            sum += d;
        }
        System.out.println("Total time: "+ sum + "s");
        System.out.println("Request sent:" + linkedList.size());
        System.out.println("Avl request time: " + sum / (linkedList.size() + 0.0) + "s");

        long beforeDisconnect = System.currentTimeMillis();
        postman.fire();
        long afterDisconnect = System.currentTimeMillis();
        System.out.println("Disconnection time: "+ (afterDisconnect - beforeDisconnect) / 1000.0 + "s");
    }

    @Test
    // todo bug here
    public void test1() throws InterruptedException, JsonProcessingException {
        //ObjectMapper objectMapper = new ObjectMapper();
        Postman postman = Postman.hire("127.0.0.1");

        postman.createSet("set");
        postman.createSetItem("set", "item");

        for (int i = 0; i < 10; i++) {
            Set set = new HashSet();
            System.out.println(set.getClass());
            System.out.println(set);
            set.add("k" + i);
            set.add("k" + (i + 1));
            set.add("k" + (i + 2));
            postman.remove("set");
            postman.createSet("set");
            postman.updateSet("set", set);
            set = postman.getSet("set");
            System.out.println(i + ": " + set);
        }
        postman.fire();
    }

    @Test
    // todo bug here
    public void test2() throws InterruptedException {
        Postman postman = Postman.hire("127.0.0.1");
        for (int i = 0; i < 1000; i++) {
            postman.createString("str", "str");
            String str = postman.getString("str");
            str = "test" + i;
            postman.updateString("str", str);
            str = postman.getString("str");
            System.out.println(str);
        }
        postman.fire();
    }

    @Test
    public void test3() throws InterruptedException {
        ObjectMapper objectMapper = new ObjectMapper();
        Postman postman = Postman.hire("10.20.124.220");
        Deque<String> deque = new LinkedList<>();
        postman.createObject("deq", deque);
        postman.createDequeItem("deq", "t1");
        for (int i = 0; i < 1; i++) {
            Deque deq = (Deque) postman.getObject("deq", Deque.class);
            deq.addLast("test" + i);
            postman.remove("deq");
            postman.createObject("deq", deq);
            System.out.println(postman.getObject("deq", Deque.class));
        }
        postman.fire();
    }
}
