package develop.x.network.netty.decord;

import develop.x.core.dispatcher.XRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TestDecoder extends ByteToMessageDecoder {

    private final XRequest.Builder builder = new XRequest.Builder();

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        System.out.println("in.readableBytes() = " + in.toString(StandardCharsets.UTF_8));
        String[] split1 = in.toString(StandardCharsets.UTF_8).split("=");
        if (split1.length == 2) {
            builder.header(split1[0], split1[1]);
        }
        else {
            builder.body(in);
            out.add(builder.build());
        }
        in.clear();
    }
}