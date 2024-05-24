package org.posthouse.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.log4j.Log4j2;
import org.posthouse.context.JentitiContext;
import org.posthouse.store.PosthouseConfigStore;
import org.posthouse.store.RemoteDictStore;
import org.posthouse.util.Delimiter;
import org.posthouse.util.impl.LocalDataPersistenceProcessor;
import org.posthouse.util.intf.DataPersistenceProcessor;
import org.posthouse.util.intf.RemoteDictInstructionAnalyser;

@Log4j2
public class RemoteDictOperationHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws JsonProcessingException {
        String msgStr = (String) msg;
        RemoteDictInstructionAnalyser remoteDictInstructionAnalyser = (RemoteDictInstructionAnalyser) JentitiContext.ctx().get("remoteDictInstructionAnalyser");
        DataPersistenceProcessor localDataPersistenceProcessor = (DataPersistenceProcessor) JentitiContext.ctx().get(LocalDataPersistenceProcessor.class);

        // data persistence if enabled
        if (PosthouseConfigStore.POSTHOUSE_CONFIG.getPosthouse().getPersistence().isEnable())
            localDataPersistenceProcessor.append(Delimiter.getBodyStart() + msgStr.split(Delimiter.getBodyStart(), 2)[1]);

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
