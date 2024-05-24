package org.posthouse;

import lombok.NoArgsConstructor;
import org.posthouse.client.Postman;
import org.testng.annotations.Test;

import java.util.Deque;
import java.util.LinkedList;

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
    public void test1() throws InterruptedException {

        Postman postman = Postman.hire("127.0.0.1");

        if (!postman.thereIs("deq"))
            postman.createDeque("deq");
        Deque<String> deq = postman.getDeque("deq");
        deq.addLast("k1");
        deq.addLast("k2");
        postman.updateDeque("deq", deq);
        deq = postman.getDeque("deq");
        System.out.println(deq);
        postman.fire();
    }
}
