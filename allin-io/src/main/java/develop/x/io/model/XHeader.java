package develop.x.io.model;

import lombok.RequiredArgsConstructor;

import java.nio.ByteBuffer;

@RequiredArgsConstructor
public class XHeader {

    private final String url; // 20
    private final String transactionId; // 20
    private final ContentType contentType; // 10
    private final int contentLength; // 10

    // XHeader 는 fixed length 방식으로 통신에 사용된다.
    public byte[] convertByte() {

        ByteBuffer buffer = ByteBuffer.wrap(new byte[60]);


//            byte[] combined = new byte[header.length + body.length];
//
//            ByteBuffer buffer = ByteBuffer.wrap(combined);
//            buffer.put(header);
//            buffer.put(body);
//            return buffer.array();
        return new byte[10];
    }
}
