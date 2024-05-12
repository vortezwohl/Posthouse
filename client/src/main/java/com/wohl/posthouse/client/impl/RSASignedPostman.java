package com.wohl.posthouse.client.impl;

import com.wohl.posthouse.client.Postman;
import com.wohl.posthouse.resp.Response;
import com.wohl.posthouse.util.Delimiter;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import lombok.extern.log4j.Log4j2;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.*;
import java.util.Base64;

/*
 * @Author 吴子豪
 */
@Log4j2
public class RSASignedPostman implements Postman {
    private EventLoopGroup eventExecutors;
    private String serverHost;
    private int serverPort;
    private Bootstrap bootstrap;
    private Response response;
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private Signature signer;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public RSASignedPostman(String host, int port) throws NoSuchProviderException, NoSuchAlgorithmException {

        KeyPair keyPair = generateRSAKeyPair();
        publicKey = keyPair.getPublic();
        privateKey = keyPair.getPrivate();
        signer = Signature.getInstance("RSA", "BC");

        eventExecutors = new NioEventLoopGroup(2);
        serverHost = host;
        serverPort = port;
        bootstrap = new Bootstrap()
                .group(eventExecutors)
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
                                response = new Response((String) msg);
                                ctx.channel().close();
                            }
                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                log.debug(cause.getStackTrace());
                            }
                        });
                        // digital signature for request body (<BODY></BODY>)
                        pipeline.addLast(new ChannelOutboundHandlerAdapter(){
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                String msgStr = (String) msg;
                                String msgBody = msgStr.split(Delimiter.getBodyStart(),2)[1].split(Delimiter.getBodyEnd(),2)[0];
                                String signature = sign(msgBody, privateKey);
                                String base64PublicKey = Base64.getEncoder().encodeToString(publicKey.getEncoded());
                                msg = Delimiter.getDigitalSignatureStart() + base64PublicKey + Delimiter.get() + signature + Delimiter.getDigitalSignatureEnd() + msg;
                                ctx.write(msg, promise);
                            }
                        });
                    }
                });
    }

    public RSASignedPostman(String host) throws NoSuchProviderException, NoSuchAlgorithmException {
        this(host,2386);
    }

    private KeyPair generateRSAKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA", "BC");
        keyGen.initialize(512);
        return keyGen.generateKeyPair();
    }

    private String sign(String msg,PrivateKey privateKey) throws SignatureException, InvalidKeyException {
        signer.initSign(privateKey);
        signer.update(msg.getBytes(CharsetUtil.UTF_8));
        return Base64.getEncoder().encodeToString(signer.sign());
    }

    @Override
    public void fire() throws InterruptedException {
        eventExecutors.shutdownGracefully().sync();
    }

    @Override
    public boolean cst(String k, String v, long ttl) throws InterruptedException {
        response = null;
        ChannelFuture future = bootstrap.connect(serverHost, serverPort).sync();
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
        return response.getValue().equals(":)");
    }

    @Override
    public boolean cst(String k, String v) throws InterruptedException {
        return cst(k,v,-1);
    }

    @Override
    public boolean cha(String k, long ttl) throws InterruptedException {
        response = null;
        ChannelFuture future = bootstrap.connect(serverHost, serverPort).sync();
        future.channel().writeAndFlush(
                Delimiter.getBodyStart()+
                System.currentTimeMillis() + Delimiter.get() +
                        ttl + Delimiter.get() +
                        "CHA" + Delimiter.get() +
                        k +
                        Delimiter.getBodyEnd()
        );
        future.channel().closeFuture().sync();
        return response.getValue().equals(":)");
    }

    @Override
    public boolean cha(String k) throws InterruptedException {
        return cha(k,-1);
    }

    @Override
    public boolean chai(String k, String field, String value, long ttl) throws InterruptedException {
        response = null;
        ChannelFuture future = bootstrap.connect(serverHost, serverPort).sync();
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
        return response.getValue().equals(":)");
    }

    @Override
    public boolean chai(String k, String field, String value) throws InterruptedException {
        return chai(k,field,value,-1);
    }

    @Override
    public boolean cde(String k, long ttl) throws InterruptedException {
        response = null;
        ChannelFuture future = bootstrap.connect(serverHost, serverPort).sync();
        future.channel().writeAndFlush(
                Delimiter.getBodyStart() +
                System.currentTimeMillis() + Delimiter.get() +
                        ttl + Delimiter.get() +
                        "CDE" + Delimiter.get() +
                        k +
                        Delimiter.getBodyEnd()
        );
        future.channel().closeFuture().sync();
        return response.getValue().equals(":)");
    }

    @Override
    public boolean cde(String k) throws InterruptedException {
        return cde(k,-1);
    }

    @Override
    public boolean cdei(String k, String v, long ttl) throws InterruptedException {
        response = null;
        ChannelFuture future = bootstrap.connect(serverHost, serverPort).sync();
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
        return response.getValue().equals(":)");
    }

    @Override
    public boolean cdei(String k, String v) throws InterruptedException {
        return cdei(k,v,-1);
    }

    @Override
    public boolean cse(String k, long ttl) throws InterruptedException {
        response = null;
        ChannelFuture future = bootstrap.connect(serverHost, serverPort).sync();
        future.channel().writeAndFlush(
                Delimiter.getBodyStart() +
                System.currentTimeMillis() + Delimiter.get() +
                        ttl + Delimiter.get() +
                        "CSE" + Delimiter.get() +
                        k +
                        Delimiter.getBodyEnd()
        );
        future.channel().closeFuture().sync();
        return response.getValue().equals(":)");
    }

    @Override
    public boolean cse(String k) throws InterruptedException {
        return cse(k,-1);
    }

    @Override
    public boolean csei(String k, String v, long ttl) throws InterruptedException {
        response = null;
        ChannelFuture future = bootstrap.connect(serverHost, serverPort).sync();
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
        return response.getValue().equals(":)");
    }

    @Override
    public boolean csei(String k, String v) throws InterruptedException {
        return csei(k,v,-1);
    }

    @Override
    public boolean mst(String k, String v) throws InterruptedException {
        response = null;
        ChannelFuture future = bootstrap.connect(serverHost, serverPort).sync();
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
        return response.getValue().equals(":)");
    }

    @Override
    public boolean exp(String k, long ttl) throws InterruptedException {
        response = null;
        ChannelFuture future = bootstrap.connect(serverHost, serverPort).sync();
        future.channel().writeAndFlush(
                Delimiter.getBodyStart() +
                System.currentTimeMillis() + Delimiter.get() +
                        ttl + Delimiter.get() +
                        "EXP" + Delimiter.get() +
                        k +
                        Delimiter.getBodyEnd()
        );
        future.channel().closeFuture().sync();
        return response.getValue().equals(":)");
    }

    @Override
    public boolean exp(String k) throws InterruptedException {
        return exp(k,0);
    }

    @Override
    public boolean dhai(String k, String field) throws InterruptedException {
        response = null;
        ChannelFuture future = bootstrap.connect(serverHost, serverPort).sync();
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
        return response.getValue().equals(":)");
    }

    @Override
    public boolean ddei(String k, String v) throws InterruptedException {
        response = null;
        ChannelFuture future = bootstrap.connect(serverHost, serverPort).sync();
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
        return response.getValue().equals(":)");
    }

    @Override
    public boolean dsei(String k, String v) throws InterruptedException {
        response = null;
        ChannelFuture future = bootstrap.connect(serverHost, serverPort).sync();
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
        return response.getValue().equals(":)");
    }

    @Override
    public boolean dk(String k) throws InterruptedException {
        response = null;
        ChannelFuture future = bootstrap.connect(serverHost, serverPort).sync();
        future.channel().writeAndFlush(
                Delimiter.getBodyStart() +
                System.currentTimeMillis() + Delimiter.get() +
                        -1 + Delimiter.get() +
                        "DK" + Delimiter.get() +
                        k +
                        Delimiter.getBodyEnd()
        );
        future.channel().closeFuture().sync();
        return response.getValue().equals(":)");
    }

    @Override
    public boolean remove(String k) throws InterruptedException {
        return dk(k);
    }

    @Override
    public String rks() throws InterruptedException {
        response = null;
        ChannelFuture future = bootstrap.connect(serverHost, serverPort).sync();
        future.channel().writeAndFlush(
                Delimiter.getBodyStart() +
                System.currentTimeMillis() + Delimiter.get() +
                        -1 + Delimiter.get() +
                        "RKS" +
                        Delimiter.getBodyEnd()
        );
        future.channel().closeFuture().sync();
        return response.getValue();
    }

    @Override
    public String rkes() throws InterruptedException {
        response = null;
        ChannelFuture future = bootstrap.connect(serverHost, serverPort).sync();
        future.channel().writeAndFlush(
                Delimiter.getBodyStart() +
                System.currentTimeMillis() + Delimiter.get() +
                        -1 + Delimiter.get() +
                        "RKES" +
                        Delimiter.getBodyEnd()
        );
        future.channel().closeFuture().sync();
        return response.getValue();
    }

    @Override
    public String rk(String k) throws InterruptedException {
        response = null;
        ChannelFuture future = bootstrap.connect(serverHost, serverPort).sync();
        future.channel().writeAndFlush(
                Delimiter.getBodyStart() +
                System.currentTimeMillis() + Delimiter.get() +
                        -1 + Delimiter.get() +
                        "RK" + Delimiter.get() +
                        k +
                        Delimiter.getBodyEnd()
        );
        future.channel().closeFuture().sync();
        if(!response.getValue().equals(":("))
            return response.getValue();
        else
            return null;
    }

    @Override
    public String get(String k) throws InterruptedException {
        return rk(k);
    }
}
