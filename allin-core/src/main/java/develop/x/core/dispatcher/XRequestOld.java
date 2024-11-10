package develop.x.core.dispatcher;

import develop.x.io.model.XHeaderOld;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public class XRequestOld {

    private final XHeaderOld header;
    private final byte[] body;

    public XRequestOld(byte[] bytes) {

        byte[] header = new byte[40];
        System.arraycopy(bytes, 0, header, 0, 30);
        this.header = XHeaderOld.of(header);

        this.body = new byte[bytes.length - 40];
        System.arraycopy(bytes, 40, body, 0, body.length);
    }

    public String url(){
        return header.getUrl();
    }

    @Override
    public String toString() {
        return "XRequest{" +
                "header=" + header +
                ", body=" + Arrays.toString(body) +
                '}';
    }

    public Object toModel(Class<?> type) {
        return header.getType().toModel(type, body);
    }
}
