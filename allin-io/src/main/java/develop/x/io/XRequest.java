package develop.x.io;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class XRequest {

    private final Map<String, String> headers = new HashMap<>();

    private byte[] body;

    public XRequest(byte[] bytes) {
        ByteArrayOutputStream lineBuffer = new ByteArrayOutputStream();

        int bodyStart = 0;
        boolean separatorFound = false;
        for (int i = 0; i < bytes.length; i++) {
            byte aByte = bytes[i];
            if (aByte == 10) {
                if (lineBuffer.size() == 0) {
                    // 빈 줄: header/body 경계. 다음 바이트부터 body.
                    bodyStart = i + 1;
                    separatorFound = true;
                    break;
                }
                String[] split = lineBuffer.toString(StandardCharsets.UTF_8).split("=", 2);
                headers.put(split[0].trim(), split.length > 1 ? split[1].trim() : "");
                lineBuffer.reset();
                continue;
            }
            lineBuffer.write(aByte);
        }

        // 경계 빈 줄이 없으면(포맷 외 입력) 헤더를 채택하지 않고 전체를 body 로 본다.
        if (!separatorFound) {
            headers.clear();
            bodyStart = 0;
        }

        try {
            lineBuffer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // body: 경계 이후 끝까지. toByte() 가 붙인 종단 개행 1개만 제거.
        int bodyEnd = bytes.length;
        if (bodyEnd > bodyStart && bytes[bodyEnd - 1] == 10) {
            bodyEnd--;
        }
        this.body = bodyStart >= bodyEnd ? new byte[0] : Arrays.copyOfRange(bytes, bodyStart, bodyEnd);
    }

    private XRequest(Map<String, String> headers, byte[] body) {
        this.headers.putAll(headers);
        this.body = body;
    }

    public byte[] toByte() {

        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            sb.append(entry.getKey())
                    .append("=")
                    .append(entry.getValue())
                    .append("\n");
        }
        sb.append("\n");
        byte[] headerByte = sb.toString().getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.wrap(new byte[headerByte.length + body.length + 1]);
        buffer.put(headerByte);
        buffer.put(body);
        buffer.put("\n".getBytes(StandardCharsets.UTF_8));
        return buffer.array();
    }

    public static class Builder {
        byte[] body;
        Map<String, String> headers = new HashMap<>();

        public Builder() {
        }

        public Builder body(Object body) {
            this.body = JsonUtils.toByte(body);
            return this;
        }

        public Builder body(byte[] body) {
            this.body = body;
            return this;
        }

        public Builder header(String key, String value) {
            headers.put(key, value);
            return this;
        }

        public XRequest build() {
            if(!headers.containsKey("contentType")){
                headers.put("contentType", "JSON");
            }
            headers.put("contentLength", body.length + "");
            return new XRequest(headers, body);
        }
    }


    @Override
    public String toString() {
        return "XRequest{" +
                "headers=" + headers +
                ", body=" + Arrays.toString(body) +
                '}';
    }
}
