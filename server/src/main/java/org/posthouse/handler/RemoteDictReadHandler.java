package org.posthouse.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.log4j.Log4j2;
import org.posthouse.context.JentitiContext;
import org.posthouse.util.intf.ReadRemoteDictInstructionAnalyser;

@Log4j2
public class RemoteDictReadHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws JsonProcessingException {
        String msgStr = (String) msg;
        ReadRemoteDictInstructionAnalyser readRemoteDictInstructionAnalyser = (ReadRemoteDictInstructionAnalyser) JentitiContext.ctx().get("readRemoteDictInstructionAnalyser");
        if(readRemoteDictInstructionAnalyser.isReadReq(msgStr)) {
            String result = readRemoteDictInstructionAnalyser.exec(msgStr);
            if (result != null) {
                ctx.writeAndFlush(result);
                log.info("Response=" + result);
            } else {
                ctx.writeAndFlush(":(");
                log.warn("Response=:( ERR");
            }
        } else
            ctx.fireChannelRead(msg);
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.warn("Unexpected connection loss: " + ctx.channel().remoteAddress());
    }
}
