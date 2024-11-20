package develop.x.network.netty.receiver.decord;

import develop.x.io.XRequest;
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
public class XRequestConvertor extends ByteToMessageDecoder {

    private final XRequest.Builder builder = new XRequest.Builder();

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

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