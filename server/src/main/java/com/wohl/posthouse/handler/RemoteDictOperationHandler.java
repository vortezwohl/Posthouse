package com.wohl.posthouse.handler;

import com.wohl.posthouse.store.RemoteDictStore;
import com.wohl.posthouse.context.JentitiContext;
import com.wohl.posthouse.util.intf.RemoteDictInstructionAnalyser;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class RemoteDictOperationHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        String msgStr = (String) msg;
        RemoteDictInstructionAnalyser remoteDictInstructionAnalyser = (RemoteDictInstructionAnalyser) JentitiContext.ctx().get("remoteDictInstructionAnalyser");
        if(remoteDictInstructionAnalyser.exec(msgStr)) {
            ctx.writeAndFlush(":)");
            log.info("Response=:) OK");
        }
        else {
            ctx.writeAndFlush(":(");
            log.warn("Response=:( ERR");
        }
        log.debug("key="+ RemoteDictStore.keySet);
        log.debug("expiring_key="+RemoteDictStore.keyExpMap);
        log.debug("string="+RemoteDictStore.stringMap);
        log.debug("set="+RemoteDictStore.setMap);
        log.debug("deque="+RemoteDictStore.dequeMap);
        log.debug("hash="+RemoteDictStore.hashMap);
        ctx.fireChannelRead(msg);
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.warn("Unexpected connection loss: " + ctx.channel().remoteAddress());
    }
}
