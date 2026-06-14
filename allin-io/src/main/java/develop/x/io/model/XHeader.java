package develop.x.io.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static java.nio.charset.StandardCharsets.UTF_8;

@Getter
@RequiredArgsConstructor
public class XHeader {

    private final String url; // 20
    private final String transactionId; // 20
    private final ContentType contentType; // 10
    private final int contentLength; // 10

    // XHeader 는 fixed length 방식으로 통신에 사용된다.
    public byte[] convertByte() {

        byte[] urlByte = url.getBytes(UTF_8);
        byte[] transactionIdByte = transactionId.getBytes(UTF_8);
        byte[] contentTypeByte = contentType.name().getBytes(UTF_8);
        byte[] contentLengthByte = Integer.toString(contentLength).getBytes(UTF_8);

        validateWidth("url", urlByte, 20);
        validateWidth("transactionId", transactionIdByte, 20);
        validateWidth("contentType", contentTypeByte, 10);
        validateWidth("contentLength", contentLengthByte, 10);

        byte[] data = new byte[60];
        System.arraycopy(urlByte, 0, data, 0, urlByte.length);
        System.arraycopy(transactionIdByte, 0, data, 20, transactionIdByte.length);
        System.arraycopy(contentTypeByte, 0, data, 40, contentTypeByte.length);
        System.arraycopy(contentLengthByte, 0, data, 50, contentLengthByte.length);
        return data;
    }

    private static void validateWidth(String field, byte[] value, int width) {
        if (value.length > width) {
            throw new IllegalArgumentException(
                    "XHeader field '" + field + "' exceeds width: " + value.length + " bytes > " + width);
        }
    }

    public static XHeader of(byte[] bytes) {

        if (bytes.length < 60) {
            throw new IllegalArgumentException(
                    "XHeader requires at least 60 bytes, but got " + bytes.length);
        }

        byte[] urlByte = new byte[20];
        byte[] transactionIdByte = new byte[20];
        byte[] contentTypeByte = new byte[10];
        byte[] contentLengthByte = new byte[10];
        System.arraycopy(bytes, 0, urlByte, 0, 20);
        System.arraycopy(bytes, 20, transactionIdByte, 0, 20);
        System.arraycopy(bytes, 40, contentTypeByte, 0, 10);
        System.arraycopy(bytes, 50, contentLengthByte, 0, 10);

        return new XHeader(
                new String(urlByte, UTF_8).trim(),
                new String(transactionIdByte, UTF_8).trim(),
                ContentType.valueOf(new String(contentTypeByte, UTF_8).trim()),
                Integer.parseInt(new String(contentLengthByte, UTF_8).trim())
        );
    }
}
