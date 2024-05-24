package wohl.posthouse.server;

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
import wohl.posthouse.handler.PostmanRegistrationHandler;
import wohl.posthouse.handler.RemoteDictOperationHandler;
import wohl.posthouse.handler.RemoteDictReadHandler;
import wohl.posthouse.handler.SignatureVerificationHandler;
import wohl.posthouse.util.GarbageCollector;

import java.util.concurrent.TimeUnit;

/**
 * @Author 吴子豪
 */
@Log4j2
@Data
@NoArgsConstructor
// 服务器启动类
public class Server {
    private static NioEventLoopGroup EVENT_EXECUTORS = new NioEventLoopGroup();
    private static ServerBootstrap SERVER_BOOTSTRAP;

    @SneakyThrows
    private static ServerBootstrap init() {
        SERVER_BOOTSTRAP = new ServerBootstrap()
                .group(EVENT_EXECUTORS)
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
                        pipeline.addLast(new PostmanRegistrationHandler());
                        pipeline.addLast(new SignatureVerificationHandler());
                        pipeline.addLast(new RemoteDictReadHandler());
                        pipeline.addLast(new RemoteDictOperationHandler());
                    }
                });

        return SERVER_BOOTSTRAP;
    }

    public static ChannelFuture run(int port) throws InterruptedException {
        runDaemon();
        return init().bind(port).sync();
    }

    private static void runDaemon() {
        EVENT_EXECUTORS.scheduleAtFixedRate(GarbageCollector::flush, 0, 16, TimeUnit.MILLISECONDS);
    }
}
