package com.wohl.posthouse.client;

import com.wohl.posthouse.client.impl.RSASignedPostman;
import lombok.SneakyThrows;

/**
 * @Author 吴子豪
 */
public interface Postman {

    @SneakyThrows
    public static Postman hire(String serverHost, int serverPort) {
        return new RSASignedPostman(serverHost, serverPort);
    }

    @SneakyThrows
    public static Postman hire(String serverHost) {
        return new RSASignedPostman(serverHost);
    }

    public abstract void fire() throws InterruptedException;

    public abstract boolean cst(String k, String v, long ttl) throws InterruptedException;
    public abstract boolean cst(String k, String v) throws InterruptedException;
    public abstract boolean cha(String k, long ttl) throws InterruptedException;
    public abstract boolean cha(String k) throws InterruptedException;
    public abstract boolean chai(String k, String field, String value, long ttl) throws InterruptedException;
    public abstract boolean chai(String k, String field, String value) throws InterruptedException;
    public abstract boolean cde(String k, long ttl) throws InterruptedException;
    public abstract boolean cde(String k) throws InterruptedException;
    public abstract boolean cdei(String k, String v, long ttl) throws InterruptedException;
    public abstract boolean cdei(String k, String v) throws InterruptedException;
    public abstract boolean cse(String k, long ttl) throws InterruptedException;
    public abstract boolean cse(String k) throws InterruptedException;
    public abstract boolean csei(String k, String v, long ttl) throws InterruptedException;
    public abstract boolean csei(String k, String v) throws InterruptedException;

    public abstract boolean mst(String k, String v) throws InterruptedException;
    public abstract boolean exp(String k, long ttl) throws InterruptedException;
    public abstract boolean exp(String k) throws InterruptedException;

    public abstract boolean dhai(String k, String field) throws InterruptedException;
    public abstract boolean ddei(String k, String v) throws InterruptedException;
    public abstract boolean dsei(String k, String v) throws InterruptedException;
    public abstract boolean dk(String k) throws InterruptedException;
    public abstract boolean remove(String k) throws InterruptedException;

    public abstract String rks() throws InterruptedException;
    public abstract String rkes() throws InterruptedException;
    public abstract String rk(String k) throws InterruptedException;
    public abstract String get(String k) throws InterruptedException;
}
