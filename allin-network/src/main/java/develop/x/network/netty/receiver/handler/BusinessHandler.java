package develop.x.network.netty.receiver.handler;

import develop.x.io.XRequest;
import develop.x.network.ipc.activemq.MessagePublisher;
import io.netty.channel.*;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@ChannelHandler.Sharable
@RequiredArgsConstructor
public class BusinessHandler extends ChannelInboundHandlerAdapter {

    private final EventExecutorGroup businessExecutorGroup;
    private final MessagePublisher messagePublisher;// 4개 스레드


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // ignore code
        XRequest xRequest = (XRequest) msg;
        log.info("readMessage = {}", xRequest);
        businessExecutorGroup.execute(() -> {
            try {
                messagePublisher.sendMessage(xRequest);
                log.info("Message sent to ActiveMQ: {} bytes", xRequest.getHeaders());
            } catch (Exception e) {
                log.error("Failed to send message to ActiveMQ", e);
            }
        });

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
}
