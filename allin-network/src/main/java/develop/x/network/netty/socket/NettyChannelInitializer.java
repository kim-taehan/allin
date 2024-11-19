package develop.x.network.netty.socket;
import develop.x.network.netty.decord.TestDecoder;
import develop.x.network.netty.handler.TestHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NettyChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final TestHandler testHandler;

    // 클라이언트 소켓 채널이 생성될 때 호출
    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();

        // decoder는 @Sharable이 안 됨, Bean 객체 주입이 안 되고, 매번 새로운 객체 생성해야 함
//        TestDecoder testDecoder = new TestDecoder();
//
//        // 뒤이어 처리할 디코더 및 핸들러 추가
        pipeline.addLast(new LineBasedFrameDecoder(Integer.MAX_VALUE));

        pipeline.addLast(new TestDecoder());
//        pipeline.addLast(testHandler);
    }
}