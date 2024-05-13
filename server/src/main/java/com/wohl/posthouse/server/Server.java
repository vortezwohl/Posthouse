package com.wohl.posthouse.server;

import com.wohl.posthouse.handler.RemoteDictOperationHandler;
import com.wohl.posthouse.handler.RemoteDictReadHandler;
import com.wohl.posthouse.handler.SignatureVerificationHandler;
import com.wohl.posthouse.util.GarbageCollector;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import java.util.LinkedList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/*
 * @Author 吴子豪
 */
@Log4j2
@Data
@NoArgsConstructor
// 服务器启动类
public class Server {
    public static NioEventLoopGroup eventExecutors = new NioEventLoopGroup();
    public static ServerBootstrap serverBootstrap;
    @SneakyThrows
    public static ServerBootstrap init() {
        serverBootstrap = new ServerBootstrap()
                .group(eventExecutors)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new StringDecoder(CharsetUtil.UTF_8));
                        pipeline.addLast(new StringEncoder(CharsetUtil.UTF_8));
                        pipeline.addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                                log.info("Received connection from " + ctx.channel().remoteAddress());
                                log.info("Request="+msg);
                                ctx.fireChannelRead(msg);
                            }
                        });
                        pipeline.addLast(new SignatureVerificationHandler());
                        pipeline.addLast(new RemoteDictReadHandler());
                        pipeline.addLast(new RemoteDictOperationHandler());
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
                eventExecutors.scheduleAtFixedRate(GarbageCollector::flush, 0,16, TimeUnit.MILLISECONDS)
        );
        return scheduledFutures;
    }
}
