package com.wohl.posthouse.server;

import com.wohl.posthouse.util.GarbageCollector;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.CharsetUtil;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.util.LinkedList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Log4j2
public class Server {
    public static NioEventLoopGroup eventExecutors = new NioEventLoopGroup();
    public static ServerBootstrap serverBootstrap;

    // 初始化SSL上下文
    public static SslContext initSslContext() throws Exception {
        return SslContextBuilder.forServer(new File("C:\\Users\\20297\\ca.crt"), new File("C:\\Users\\20297\\ca.key")).build();
    }

    @SneakyThrows
    public static ServerBootstrap init() {
        final SslContext sslContext = initSslContext(); // 调用初始化SSL上下文的方法

        serverBootstrap = new ServerBootstrap()
                .group(eventExecutors)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) {
                        ChannelPipeline pipeline = socketChannel.pipeline();

                        // 在Pipeline的最前面添加SSL处理器
                        pipeline.addFirst("ssl", new SslHandler(sslContext.newEngine(socketChannel.alloc())));

                        pipeline.addLast(new StringDecoder(CharsetUtil.UTF_8));
                        pipeline.addLast(new StringEncoder(CharsetUtil.UTF_8));
                        pipeline.addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                                log.info("Received connection from " + ctx.channel().remoteAddress());
                                log.info("Request=" + msg);
                                ctx.fireChannelRead(msg);
                            }
                        });
                        // ... 添加其他处理器 ...
                    }
                });

        return serverBootstrap;
    }

    public static ChannelFuture run(int port) throws InterruptedException {
        return serverBootstrap.bind(port).sync();
    }

    public static LinkedList<ScheduledFuture> runDaemon() {
        LinkedList<ScheduledFuture> scheduledFutures = new LinkedList<>();
        scheduledFutures.addLast(
                eventExecutors.scheduleAtFixedRate(GarbageCollector::flush, 0, 16, TimeUnit.MILLISECONDS)
        );
        return scheduledFutures;
    }

}
