package com.wohl.posthouse.handler;

import com.wohl.posthouse.context.JentitiContext;
import com.wohl.posthouse.util.Delimiter;
import com.wohl.posthouse.util.intf.SignatureVerifier;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class SignatureVerificationHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        SignatureVerifier signatureVerifier = (SignatureVerifier) JentitiContext.ctx().get("signatureVerifier");

        String msgStr = (String) msg;
        String[] digitalSignature = msgStr.split(Delimiter.getDigitalSignatureStart(),2)[1].split(Delimiter.getDigitalSignatureEnd(),2)[0].split(Delimiter.get(),2);
        String publicKey = digitalSignature[0];
        String signature = digitalSignature[1];
        String msgBody = msgStr.split(Delimiter.getBodyStart(),2)[1].split(Delimiter.getBodyEnd(),2)[0];
        if(signatureVerifier.verify(msgBody, signature, publicKey)){
            log.info("Signature succeeded to match");
            ctx.fireChannelRead(msg);
        }

        else {
            log.warn("Signature failed to match");
            ctx.channel().writeAndFlush(":(");
        }
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error(cause.getMessage());
        ctx.channel().writeAndFlush(":(");
        ctx.channel().close();
    }
}
