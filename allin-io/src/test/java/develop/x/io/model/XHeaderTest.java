package develop.x.io.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class XHeaderTest {

    /**
     * 주의: 현재 {@link XHeader#convertByte()} 는 미완성 스텁이다.
     * url/transactionId/contentType/contentLength 필드를 전혀 직렬화하지 않고
     * 무조건 길이 10의 0-바이트 배열을 반환한다.
     * (구현부의 60바이트 ByteBuffer 도 채워지지 않은 채 버려진다.)
     *
     * 따라서 이 테스트는 "현재 동작(스텁)"을 고정(characterization)하는 용도이며,
     * fixed-length 직렬화가 실제로 구현되면 반드시 갱신되어야 한다.
     * 메인 소스의 미완성 사항은 별도 보고 대상이다.
     */
    @DisplayName("convertByte 는 현재 미완성 스텁으로 길이 10의 0-바이트 배열을 반환한다.")
    @Test
    void convertByteReturnsStub() {
        XHeader header = new XHeader(
                "/test-api",
                UUID.randomUUID().toString(),
                ContentType.JSON,
                42
        );

        byte[] result = header.convertByte();

        // 스텁이므로 입력 필드와 무관하게 항상 동일한 결과
        assertThat(result).hasSize(10);
        assertThat(result).containsOnly((byte) 0);
    }
}
