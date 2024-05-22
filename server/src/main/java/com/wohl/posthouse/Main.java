package com.wohl.posthouse;

import com.wohl.posthouse.context.JentitiContext;
import com.wohl.posthouse.server.Server;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import lombok.extern.log4j.Log4j2;

import java.util.LinkedList;
import java.util.concurrent.ScheduledFuture;

@Log4j2
public class Main {
    public static void main(String[] args) throws InterruptedException {
        JentitiContext.init();
        ServerBootstrap server = Server.init();
        ChannelFuture channelFuture = Server.run(2386);
        LinkedList<ScheduledFuture> scheduledFutures = Server.runDaemon();
        log.info("Posthouse boots up on port " + 2386);
    }
}
