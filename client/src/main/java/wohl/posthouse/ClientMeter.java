package wohl.posthouse;

import wohl.posthouse.client.Postman;

import java.util.LinkedList;

public class ClientMeter {
    public static void main(String[] args) throws InterruptedException {

        long beforeConnect = System.currentTimeMillis();
        Postman postman = Postman.hire("127.0.0.1");
        long afterConnect = System.currentTimeMillis();
        System.out.println("Connection time: "+ (afterConnect - beforeConnect) / 1000.0 + "s");

        long start = System.currentTimeMillis();
        postman.cst("str1", "str1");
        String res = postman.get("str1");
        postman.exp("str1");
        long end = System.currentTimeMillis();
        System.out.println("First request time: "+(end - start) / 1000.0 + "s");

        LinkedList<Double> linkedList = new LinkedList<>();
        for (int i = 0; i < 3; i++) {
            start = System.currentTimeMillis();
            postman.cst("str1", "str1");
            res = postman.get("str1");
            postman.exp("str1");
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
}
