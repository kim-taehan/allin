package develop.x.io.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static java.nio.charset.StandardCharsets.UTF_8;

@Getter
@RequiredArgsConstructor
public class XHeaderOld {
    private final ContentType type;
    private final String url;
    private final String transactionId;

    public byte[] convert(){

        byte[] typeByte = type.toString().getBytes(UTF_8);
        byte[] urlByte = url.getBytes(UTF_8);
        byte[] transactionIdByte = transactionId.getBytes(UTF_8);

        byte[] data = new byte[40];
        System.arraycopy(typeByte, 0, data, 0, typeByte.length);
        System.arraycopy(urlByte, 0, data, 10, urlByte.length);
        System.arraycopy(transactionIdByte, 0, data, 30, transactionIdByte.length);
        return data;
    }

    public static XHeaderOld of(byte[] bytes) {

        byte[] typeByte = new byte[10];
        byte[] urlByte = new byte[20];
        byte[] transactionIdByte = new byte[10];
        System.arraycopy(bytes, 0, typeByte, 0, 10);
        System.arraycopy(bytes, 10, urlByte, 0, 20);
        System.arraycopy(bytes, 30, transactionIdByte, 0, 10);

        return new XHeaderOld(
                ContentType.valueOf(new String(typeByte, UTF_8).trim()),
                new String(urlByte, UTF_8).trim(),
                new String(transactionIdByte, UTF_8).trim()
        );
    }

    @Override
    public String toString() {
        return "XHeader{" +
                "type=" + type +
                ", url='" + url + '\'' +
                ", transactionId='" + transactionId + '\'' +
                '}';
    }
}
