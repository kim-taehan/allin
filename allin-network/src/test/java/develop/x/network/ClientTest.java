package develop.x.network;

import develop.x.core.dispatcher.XRequest;
import develop.x.core.utils.JsonUtils;
import lombok.Data;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.UUID;

public class ClientTest {

    @DisplayName("")
    @Test
    void test759() throws IOException {
        // given
        TestDto testDto = new TestDto();
        testDto.setAge(40);
        testDto.setName("kimtaehan");
        XRequest xRequest = new XRequest.Builder()
                .header("transactionId", UUID.randomUUID().toString())
                .body(JsonUtils.toByte(testDto))
                .build();

        // when
        byte[] aByte = xRequest.toByte();


        Socket socket = new Socket("127.0.0.1", 45672);
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(aByte);
        outputStream.flush();
        outputStream.close();
        socket.close();
        // then
        Assertions.assertThat("");
    }

    @Data
    static class TestDto {
        private String name;
        private int age;
    }
}
