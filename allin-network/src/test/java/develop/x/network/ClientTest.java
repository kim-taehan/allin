package develop.x.network;

import develop.x.io.JsonUtils;
import develop.x.io.XRequest;
import lombok.Data;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientTest {

    @DisplayName("정상적인 데이터 1건 호출")
    @Test
    void callNormalOneCase() throws IOException {
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

    @DisplayName("정상적인 데이터 2건 호출")
    @Test
    void callNormalTwoCase() throws IOException {
        // given
        TestDto testDto1 = new TestDto();
        testDto1.setAge(40);
        testDto1.setName("kimtaehan");

        TestDto testDto2 = new TestDto();
        testDto2.setAge(30);
        testDto2.setName("kininho");

        XRequest xRequest1 = new XRequest.Builder()
                .header("transactionId", UUID.randomUUID().toString())
                .body(JsonUtils.toByte(testDto1))
                .build();

        XRequest xRequest2 = new XRequest.Builder()
                .header("transactionId", UUID.randomUUID().toString())
                .body(JsonUtils.toByte(testDto2))
                .build();

        // when



        // then
        Assertions.assertThat("");
    }


    @DisplayName("정상적인 데이터 100건 호출")
    @Test
    void callNormal100Case() throws IOException {
        // given
        TestDto testDto = new TestDto();
        testDto.setAge(40);
        testDto.setName("kimtaehan");


        ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
        for (int i = 0; i < 100; i++) {

            XRequest xRequest = new XRequest.Builder()
                    .header("transactionId", UUID.randomUUID().toString())
                    .body(JsonUtils.toByte(testDto))
                    .build();


            cachedThreadPool.execute(() -> {
                try (Socket socket = new Socket("127.0.0.1", 45672)) {
                    OutputStream outputStream = socket.getOutputStream();
                    outputStream.write(xRequest.toByte());
                    outputStream.flush();
                    outputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

        }
        // then
        Assertions.assertThat("");
    }

    @Data
    static class TestDto {
        private String name;
        private int age;
    }
}
