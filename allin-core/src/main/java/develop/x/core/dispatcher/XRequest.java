package develop.x.core.dispatcher;

import develop.x.core.utils.JsonUtils;
import develop.x.io.model.ContentType;
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
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (byte aByte : bytes) {
            if (aByte == 10) {
                if (outputStream.size() > 0) {
                    String[] split = outputStream.toString().split("=");
                    headers.put(split[0].trim(), split[1].trim());
                    outputStream = new ByteArrayOutputStream();
                }
                continue;
            }
            outputStream.write(aByte);
        }
        this.body = outputStream.toByteArray();

        try {
            outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
        ByteBuffer buffer = ByteBuffer.wrap(new byte[headerByte.length + body.length]);
        buffer.put(headerByte);
        buffer.put(body);
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
                headers.put("contentType", ContentType.JSON.name());
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
