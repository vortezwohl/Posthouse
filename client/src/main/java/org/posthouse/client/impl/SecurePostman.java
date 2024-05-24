package org.posthouse.client.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.posthouse.client.Postman;
import org.posthouse.client.exception.PostmanRegistrationException;
import org.posthouse.resp.Response;
import org.posthouse.util.Delimiter;

import java.security.*;
import java.util.Base64;
import java.util.Deque;
import java.util.Map;
import java.util.Set;

/**
 * @Author 吴子豪
 */
@Log4j2
public class SecurePostman implements Postman {
    private EventLoopGroup EVENT_EXECUTORS;
    private String SERVER_HOST;
    private int SERVER_PORT;
    private Bootstrap BOOTSTRAP;
    private Response RESPONSE;
    private PublicKey PUBLIC_KEY;
    private PrivateKey PRIVATE_KEY;
    private Signature SIGNER;
    private String POSTMAN_ID;
    private ObjectMapper OBJECT_MAPPER;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public SecurePostman(String host, int port) throws
            NoSuchProviderException,
            NoSuchAlgorithmException,
            InterruptedException,
            InvalidKeyException,
            SignatureException {
        KeyPair keyPair = generateRSAKeyPair();
        PUBLIC_KEY = keyPair.getPublic();
        PRIVATE_KEY = keyPair.getPrivate();
        SIGNER = Signature.getInstance("RSA", "BC");
        OBJECT_MAPPER = new ObjectMapper();
        EVENT_EXECUTORS = new NioEventLoopGroup(2);
        SERVER_HOST = host;
        SERVER_PORT = port;
        BOOTSTRAP = new Bootstrap()
                .group(EVENT_EXECUTORS)
                .option(ChannelOption.TCP_NODELAY, true)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new StringDecoder(CharsetUtil.UTF_8));
                        pipeline.addLast(new StringEncoder(CharsetUtil.UTF_8));
                        pipeline.addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                RESPONSE = new Response((String) msg);
                                ctx.channel().close();
                            }
                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                log.debug(cause.getStackTrace());
                            }
                        });
                        pipeline.addLast(new ChannelOutboundHandlerAdapter(){
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                String msgStr = (String) msg;
                                // digital signature for request body (<BODY></BODY>)
                                if (msgStr.contains(Delimiter.getBodyStart()) || msgStr.contains(Delimiter.getBodyEnd())) {
                                    String msgBody = msgStr.split(Delimiter.getBodyStart(), 2)[1].split(Delimiter.getBodyEnd(), 2)[0];
                                    String signature = sign(msgBody);
                                    // POSTMAN_ID is mapped to a specific rsa public key
                                    String digitalSignatureHeader =
                                            Delimiter.getDigitalSignatureStart() +
                                                    POSTMAN_ID + Delimiter.get() +
                                                    signature +
                                                    Delimiter.getDigitalSignatureEnd();
                                    // add header to msg string
                                    msgStr = digitalSignatureHeader + msgStr;
                                }
                                ctx.write(msgStr, promise);
                            }
                        });
                    }
                });

        // if registration failed, fire the postman (drop the connection) due to access control policy
        if (!generatePostmanIdWithRSASignature()) {
            fire();
            throw new PostmanRegistrationException("Failed to register postman in Posthouse.");
        }
    }

    public SecurePostman(String host) throws
            NoSuchProviderException,
            NoSuchAlgorithmException,
            InterruptedException,
            InvalidKeyException,
            SignatureException {
        this(host,2386);
    }

    private KeyPair generateRSAKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA", "BC");
        keyGen.initialize(512);
        return keyGen.generateKeyPair();
    }

    private String sign(String msg) throws SignatureException, InvalidKeyException {
        SIGNER.initSign(PRIVATE_KEY);
        SIGNER.update(msg.getBytes(CharsetUtil.UTF_8));
        return Base64.getEncoder().encodeToString(SIGNER.sign());
    }

    private boolean generatePostmanIdWithRSASignature() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, InterruptedException {
        // generate POSTMAN_ID
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        long timeStamp = System.currentTimeMillis();
        int randInt = secureRandom.nextInt();
        SIGNER.initSign(PRIVATE_KEY);
        SIGNER.update((String.valueOf(timeStamp) + randInt).getBytes(CharsetUtil.UTF_8));
        POSTMAN_ID = Base64.getEncoder().encodeToString(SIGNER.sign());

        // sync with server
        RESPONSE = null;
        ChannelFuture future = BOOTSTRAP.connect(SERVER_HOST, SERVER_PORT).sync();
        future.channel().writeAndFlush(
                Delimiter.getPostmanRegisterStart() +
                        POSTMAN_ID + Delimiter.get() + Base64.getEncoder().encodeToString(PUBLIC_KEY.getEncoded()) +
                        Delimiter.getPostmanRegisterEnd()
        );
        future.channel().closeFuture().sync();
        return RESPONSE.getValue().equals(":)");
    }

    @Override
    public void fire() throws InterruptedException {
        // fire the postman in Posthouse
        RESPONSE = null;
        ChannelFuture future = BOOTSTRAP.connect(SERVER_HOST, SERVER_PORT).sync();
        future.channel().writeAndFlush(
                Delimiter.getPostmanLogoffStart() +
                        POSTMAN_ID + Delimiter.getPostmanLogoffEnd()
        );
        future.channel().closeFuture().sync();
        if (RESPONSE.getValue().equals(":)"))
            EVENT_EXECUTORS.shutdownGracefully().sync();
    }

    @SneakyThrows
    @Override
    public void close() {
        fire();
    }

    @Override
    public boolean createString(String k, String v, long ttl) throws InterruptedException {
        RESPONSE = null;
        ChannelFuture future = BOOTSTRAP.connect(SERVER_HOST, SERVER_PORT).sync();
        future.channel().writeAndFlush(
                Delimiter.getBodyStart() +
                System.currentTimeMillis() + Delimiter.get() +
                        ttl + Delimiter.get() +
                        "CST" + Delimiter.get() +
                        k + Delimiter.get() +
                        v +
                        Delimiter.getBodyEnd()
        );
        future.channel().closeFuture().sync();
        return RESPONSE.getValue().equals(":)");
    }

    @Override
    public boolean createString(String k, String v) throws InterruptedException {
        return createString(k, v, -1);
    }

    @Override
    public boolean createMap(String k, long ttl) throws InterruptedException {
        RESPONSE = null;
        ChannelFuture future = BOOTSTRAP.connect(SERVER_HOST, SERVER_PORT).sync();
        future.channel().writeAndFlush(
                Delimiter.getBodyStart()+
                System.currentTimeMillis() + Delimiter.get() +
                        ttl + Delimiter.get() +
                        "CHA" + Delimiter.get() +
                        k +
                        Delimiter.getBodyEnd()
        );
        future.channel().closeFuture().sync();
        return RESPONSE.getValue().equals(":)");
    }

    @Override
    public boolean createMap(String k) throws InterruptedException {
        return createMap(k, -1);
    }

    @Override
    public boolean createMapEntry(String k, String field, String value, long ttl) throws InterruptedException {
        RESPONSE = null;
        ChannelFuture future = BOOTSTRAP.connect(SERVER_HOST, SERVER_PORT).sync();
        future.channel().writeAndFlush(
                Delimiter.getBodyStart() +
                System.currentTimeMillis() + Delimiter.get() +
                        ttl + Delimiter.get() +
                        "CHAI" + Delimiter.get() +
                        k + Delimiter.get() +
                        field + ":" +
                        value +
                        Delimiter.getBodyEnd()
        );
        future.channel().closeFuture().sync();
        return RESPONSE.getValue().equals(":)");
    }

    @Override
    public boolean createMapEntry(String k, String field, String value) throws InterruptedException {
        return createMapEntry(k, field, value, -1);
    }

    @Override
    public boolean createDeque(String k, long ttl) throws InterruptedException {
        RESPONSE = null;
        ChannelFuture future = BOOTSTRAP.connect(SERVER_HOST, SERVER_PORT).sync();
        future.channel().writeAndFlush(
                Delimiter.getBodyStart() +
                System.currentTimeMillis() + Delimiter.get() +
                        ttl + Delimiter.get() +
                        "CDE" + Delimiter.get() +
                        k +
                        Delimiter.getBodyEnd()
        );
        future.channel().closeFuture().sync();
        return RESPONSE.getValue().equals(":)");
    }

    @Override
    public boolean createDeque(String k) throws InterruptedException {
        return createDeque(k, -1);
    }

    @Override
    public boolean createDequeItem(String k, String v, long ttl) throws InterruptedException {
        RESPONSE = null;
        ChannelFuture future = BOOTSTRAP.connect(SERVER_HOST, SERVER_PORT).sync();
        future.channel().writeAndFlush(
                Delimiter.getBodyStart() +
                System.currentTimeMillis() + Delimiter.get() +
                        ttl + Delimiter.get() +
                        "CDEI" + Delimiter.get() +
                        k + Delimiter.get() +
                        v +
                        Delimiter.getBodyEnd()
        );
        future.channel().closeFuture().sync();
        return RESPONSE.getValue().equals(":)");
    }

    @Override
    public boolean createDequeItem(String k, String v) throws InterruptedException {
        return createDequeItem(k, v, -1);
    }

    @Override
    public boolean createSet(String k, long ttl) throws InterruptedException {
        RESPONSE = null;
        ChannelFuture future = BOOTSTRAP.connect(SERVER_HOST, SERVER_PORT).sync();
        future.channel().writeAndFlush(
                Delimiter.getBodyStart() +
                System.currentTimeMillis() + Delimiter.get() +
                        ttl + Delimiter.get() +
                        "CSE" + Delimiter.get() +
                        k +
                        Delimiter.getBodyEnd()
        );
        future.channel().closeFuture().sync();
        return RESPONSE.getValue().equals(":)");
    }

    @Override
    public boolean createSet(String k) throws InterruptedException {
        return createSet(k, -1);
    }

    @Override
    public boolean createSetItem(String k, String v, long ttl) throws InterruptedException {
        RESPONSE = null;
        ChannelFuture future = BOOTSTRAP.connect(SERVER_HOST, SERVER_PORT).sync();
        future.channel().writeAndFlush(
                Delimiter.getBodyStart() +
                System.currentTimeMillis() + Delimiter.get() +
                        ttl + Delimiter.get() +
                        "CSEI" + Delimiter.get() +
                        k + Delimiter.get() +
                        v +
                        Delimiter.getBodyEnd()
        );
        future.channel().closeFuture().sync();
        return RESPONSE.getValue().equals(":)");
    }

    @Override
    public boolean createSetItem(String k, String v) throws InterruptedException {
        return createSetItem(k, v, -1);
    }

    @Override
    public boolean createObject(String k, Object object, long ttl) throws InterruptedException {
        RESPONSE = null;
        try {
            return createString(k, OBJECT_MAPPER.writeValueAsString(object), ttl);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean createObject(String k, Object object) throws InterruptedException {
        return createObject(k, object, -1);
    }

    @Override
    public boolean updateString(String k, String v) throws InterruptedException {
        RESPONSE = null;
        ChannelFuture future = BOOTSTRAP.connect(SERVER_HOST, SERVER_PORT).sync();
        future.channel().writeAndFlush(
                Delimiter.getBodyStart() +
                System.currentTimeMillis() + Delimiter.get() +
                        -1 + Delimiter.get() +
                        "MST" + Delimiter.get() +
                        k + Delimiter.get() +
                        v +
                        Delimiter.getBodyEnd()
        );
        future.channel().closeFuture().sync();
        return RESPONSE.getValue().equals(":)");
    }

    @Override
    public boolean updateMap(String key, Map map) throws InterruptedException {
        RESPONSE = null;
        String v;
        try {
            v = OBJECT_MAPPER.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return false;
        }
        ChannelFuture future = BOOTSTRAP.connect(SERVER_HOST, SERVER_PORT).sync();
        future.channel().writeAndFlush(
                Delimiter.getBodyStart() +
                        System.currentTimeMillis() + Delimiter.get() +
                        -1 + Delimiter.get() +
                        "MHA" + Delimiter.get() +
                        key + Delimiter.get() +
                        v +
                        Delimiter.getBodyEnd()
        );
        future.channel().closeFuture().sync();
        return RESPONSE.getValue().equals(":)");
    }

    @Override
    public boolean updateDeque(String key, Deque deque) throws InterruptedException {
        RESPONSE = null;
        String v;
        try {
            v = OBJECT_MAPPER.writeValueAsString(deque);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return false;
        }
        ChannelFuture future = BOOTSTRAP.connect(SERVER_HOST, SERVER_PORT).sync();
        future.channel().writeAndFlush(
                Delimiter.getBodyStart() +
                        System.currentTimeMillis() + Delimiter.get() +
                        -1 + Delimiter.get() +
                        "MDE" + Delimiter.get() +
                        key + Delimiter.get() +
                        v +
                        Delimiter.getBodyEnd()
        );
        future.channel().closeFuture().sync();
        return RESPONSE.getValue().equals(":)");
    }

    @Override
    public boolean updateSet(String key, Set set) throws InterruptedException {
        RESPONSE = null;
        String v;
        try {
            v = OBJECT_MAPPER.writeValueAsString(set);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return false;
        }
        ChannelFuture future = BOOTSTRAP.connect(SERVER_HOST, SERVER_PORT).sync();
        future.channel().writeAndFlush(
                Delimiter.getBodyStart() +
                        System.currentTimeMillis() + Delimiter.get() +
                        -1 + Delimiter.get() +
                        "MSE" + Delimiter.get() +
                        key + Delimiter.get() +
                        v +
                        Delimiter.getBodyEnd()
        );
        future.channel().closeFuture().sync();
        return RESPONSE.getValue().equals(":)");
    }

    @Override
    public boolean expire(String k, long ttl) throws InterruptedException {
        RESPONSE = null;
        ChannelFuture future = BOOTSTRAP.connect(SERVER_HOST, SERVER_PORT).sync();
        future.channel().writeAndFlush(
                Delimiter.getBodyStart() +
                System.currentTimeMillis() + Delimiter.get() +
                        ttl + Delimiter.get() +
                        "EXP" + Delimiter.get() +
                        k +
                        Delimiter.getBodyEnd()
        );
        future.channel().closeFuture().sync();
        return RESPONSE.getValue().equals(":)");
    }

    @Override
    public boolean expire(String k) throws InterruptedException {
        return expire(k, 0);
    }

    @Override
    public boolean removeMapEntry(String k, String field) throws InterruptedException {
        RESPONSE = null;
        ChannelFuture future = BOOTSTRAP.connect(SERVER_HOST, SERVER_PORT).sync();
        future.channel().writeAndFlush(
                Delimiter.getBodyStart() +
                System.currentTimeMillis() + Delimiter.get() +
                        -1 + Delimiter.get() +
                        "DHAI" + Delimiter.get() +
                        k + Delimiter.get() +
                        field +
                        Delimiter.getBodyEnd()
        );
        future.channel().closeFuture().sync();
        return RESPONSE.getValue().equals(":)");
    }

    @Override
    public boolean removeDequeItemAtFirstOccurrence(String k, String v) throws InterruptedException {
        RESPONSE = null;
        ChannelFuture future = BOOTSTRAP.connect(SERVER_HOST, SERVER_PORT).sync();
        future.channel().writeAndFlush(
                Delimiter.getBodyStart() +
                System.currentTimeMillis() + Delimiter.get() +
                        -1 + Delimiter.get() +
                        "DDEI" + Delimiter.get() +
                        k + Delimiter.get() +
                        v +
                        Delimiter.getBodyEnd()
        );
        future.channel().closeFuture().sync();
        return RESPONSE.getValue().equals(":)");
    }

    @Override
    public boolean removeSetItem(String k, String v) throws InterruptedException {
        RESPONSE = null;
        ChannelFuture future = BOOTSTRAP.connect(SERVER_HOST, SERVER_PORT).sync();
        future.channel().writeAndFlush(
                Delimiter.getBodyStart() +
                System.currentTimeMillis() + Delimiter.get() +
                        -1 + Delimiter.get() +
                        "DSEI" + Delimiter.get() +
                        k + Delimiter.get() +
                        v +
                        Delimiter.getBodyEnd()
        );
        future.channel().closeFuture().sync();
        return RESPONSE.getValue().equals(":)");
    }

    @Override
    public boolean removeKey(String k) throws InterruptedException {
        RESPONSE = null;
        ChannelFuture future = BOOTSTRAP.connect(SERVER_HOST, SERVER_PORT).sync();
        future.channel().writeAndFlush(
                Delimiter.getBodyStart() +
                System.currentTimeMillis() + Delimiter.get() +
                        -1 + Delimiter.get() +
                        "DK" + Delimiter.get() +
                        k +
                        Delimiter.getBodyEnd()
        );
        future.channel().closeFuture().sync();
        return RESPONSE.getValue().equals(":)");
    }

    @Override
    public boolean remove(String k) throws InterruptedException {
        return removeKey(k);
    }

    @Override
    public Map getKeySet() throws InterruptedException {
        RESPONSE = null;
        ChannelFuture future = BOOTSTRAP.connect(SERVER_HOST, SERVER_PORT).sync();
        future.channel().writeAndFlush(
                Delimiter.getBodyStart() +
                System.currentTimeMillis() + Delimiter.get() +
                        -1 + Delimiter.get() +
                        "RKS" +
                        Delimiter.getBodyEnd()
        );
        future.channel().closeFuture().sync();
        try {
            return OBJECT_MAPPER.readValue(RESPONSE.getValue(), Map.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Map getKeyExpirationSet() throws InterruptedException {
        RESPONSE = null;
        ChannelFuture future = BOOTSTRAP.connect(SERVER_HOST, SERVER_PORT).sync();
        future.channel().writeAndFlush(
                Delimiter.getBodyStart() +
                System.currentTimeMillis() + Delimiter.get() +
                        -1 + Delimiter.get() +
                        "RKES" +
                        Delimiter.getBodyEnd()
        );
        future.channel().closeFuture().sync();
        try {
            return OBJECT_MAPPER.readValue(RESPONSE.getValue(), Map.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String readKey(String k) throws InterruptedException {
        RESPONSE = null;
        ChannelFuture future = BOOTSTRAP.connect(SERVER_HOST, SERVER_PORT).sync();
        future.channel().writeAndFlush(
                Delimiter.getBodyStart() +
                System.currentTimeMillis() + Delimiter.get() +
                        -1 + Delimiter.get() +
                        "RK" + Delimiter.get() +
                        k +
                        Delimiter.getBodyEnd()
        );
        future.channel().closeFuture().sync();
        if (!RESPONSE.getValue().equals(":("))
            return RESPONSE.getValue();
        else
            return null;
    }

    @Override
    public String get(String k) throws InterruptedException {
        return readKey(k);
    }

    @Override
    @SuppressWarnings("all")
    public Object getObject(String k, Class clazz) throws InterruptedException {
        try {
            return OBJECT_MAPPER.readValue(readKey(k), clazz);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getString(String key) throws InterruptedException {
        return get(key);
    }

    @Override
    @SuppressWarnings("all")
    public Map<String, String> getMap(String k) throws InterruptedException {
        return (Map) getObject(k, Map.class);
    }

    @Override
    @SuppressWarnings("all")
    public Deque<String> getDeque(String k) throws InterruptedException {
        return (Deque) getObject(k, Deque.class);
    }

    @Override
    @SuppressWarnings("all")
    public Set<String> getSet(String k) throws InterruptedException {
        return (Set) getObject(k, Set.class);
    }

    @Override
    public boolean thereIs(String key) throws InterruptedException {
        return getKeySet().containsKey(key);
    }
}
