package com.wohl.posthouse.handler;

import com.wohl.posthouse.store.PostmanRegistrationStore;
import com.wohl.posthouse.util.Delimiter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.log4j.Log4j2;

/**
 * @Author 吴子豪
 * Postman registration handler has the highest priority in pipeline
 */
@Log4j2
public class PostmanRegistrationHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String msgStr = (String) msg;
        if (msgStr.contains(Delimiter.getPostmanRegisterStart())
                || msgStr.contains(Delimiter.getPostmanRegisterEnd())) {
            String[] postmanRegistrationBind = msgStr.split(Delimiter.getPostmanRegisterStart(), 2)[1].split(Delimiter.getPostmanRegisterEnd(), 2)[0].split(Delimiter.get(), 2);
            String postmanId = postmanRegistrationBind[0];
            String publicKey = postmanRegistrationBind[1];
            PostmanRegistrationStore.keySet.add(postmanId);
            PostmanRegistrationStore.publicKeyMap.put(postmanId, publicKey);
            if (PostmanRegistrationStore.exist(postmanId)) {
                log.info("Postman " + postmanId + " hired");
                ctx.channel().writeAndFlush(":)");
            } else
                ctx.channel().writeAndFlush(":(");
        } else if (msgStr.contains(Delimiter.getPostmanLogoffStart())
                || msgStr.contains(Delimiter.getPostmanLogoffEnd())) {
            String postmanId = msgStr.split(Delimiter.getPostmanLogoffStart(), 2)[1].split(Delimiter.getPostmanLogoffEnd(), 2)[0];
            PostmanRegistrationStore.keySet.remove(postmanId);
            PostmanRegistrationStore.publicKeyMap.remove(postmanId);
            if (!PostmanRegistrationStore.exist(postmanId)) {
                log.info("Postman " + postmanId + " fired");
                ctx.channel().writeAndFlush(":)");
            } else
                ctx.channel().writeAndFlush(":(");
        } else
            ctx.fireChannelRead(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error(cause.getMessage());
        ctx.channel().writeAndFlush(":(");
        ctx.channel().close();
    }
}
