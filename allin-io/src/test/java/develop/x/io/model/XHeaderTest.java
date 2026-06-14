package develop.x.io.model;

import develop.x.io.id.XIdGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class XHeaderTest {

    @Test
    @DisplayName("convertByte() 는 정확히 60 byte 고정 길이 배열을 생성한다.")
    void convertByteProducesFixed60Bytes() {
        // given
        XHeader header = new XHeader("/test-api", "tx-001", ContentType.JSON, 42);

        // when
        byte[] bytes = header.convertByte();

        // then
        assertThat(bytes).hasSize(60);
    }

    @Test
    @DisplayName("convertByte() 는 url(0)/transactionId(20)/contentType(40)/contentLength(50) 오프셋에 값을 배치한다.")
    void convertBytePlacesFieldsAtOffsets() {
        // given
        XHeader header = new XHeader("/api", "tx1", ContentType.JSON, 7);

        // when
        byte[] bytes = header.convertByte();

        // then : null-byte 패딩 UTF-8 이므로 trim 으로 비교
        assertThat(new String(bytes, 0, 20).trim()).isEqualTo("/api");
        assertThat(new String(bytes, 20, 20).trim()).isEqualTo("tx1");
        assertThat(new String(bytes, 40, 10).trim()).isEqualTo("JSON");
        assertThat(new String(bytes, 50, 10).trim()).isEqualTo("7");
    }

    @Test
    @DisplayName("convertByte() -> of() round-trip 으로 모든 필드가 복원된다(일반 값).")
    void roundTripNormal() {
        // given
        XHeader original = new XHeader("/order/create", "txn-12345", ContentType.SERIALIZE, 1024);

        // when
        XHeader restored = XHeader.of(original.convertByte());

        // then
        assertThat(restored.getUrl()).isEqualTo(original.getUrl());
        assertThat(restored.getTransactionId()).isEqualTo(original.getTransactionId());
        assertThat(restored.getContentType()).isEqualTo(original.getContentType());
        assertThat(restored.getContentLength()).isEqualTo(original.getContentLength());
    }

    @Test
    @DisplayName("round-trip : url/transactionId 폭 경계값(정확히 20 byte) 도 손실 없이 복원된다.")
    void roundTripWidthBoundary() {
        // given : url 20 byte, transactionId 20 byte (정확히 폭과 동일)
        String url20 = "a".repeat(20);
        String tx20 = "b".repeat(20);
        XHeader original = new XHeader(url20, tx20, ContentType.JSON, 99);

        // when
        XHeader restored = XHeader.of(original.convertByte());

        // then
        assertThat(restored.getUrl()).isEqualTo(url20);
        assertThat(restored.getTransactionId()).isEqualTo(tx20);
        assertThat(restored.getContentType()).isEqualTo(ContentType.JSON);
        assertThat(restored.getContentLength()).isEqualTo(99);
    }

    @Test
    @DisplayName("round-trip : contentLength 0 / 큰 값(10자리 폭 경계)도 복원된다.")
    void roundTripContentLengthEdges() {
        // given : 0
        XHeader zero = XHeader.of(new XHeader("/a", "t", ContentType.JSON, 0).convertByte());
        // given : Integer.MAX_VALUE = 2147483647 (10자리, 폭 10 적합)
        XHeader large = XHeader.of(new XHeader("/a", "t", ContentType.JSON, Integer.MAX_VALUE).convertByte());

        // then
        assertThat(zero.getContentLength()).isEqualTo(0);
        assertThat(large.getContentLength()).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    @DisplayName("convertByte() : url 이 폭(20)을 초과하면 IllegalArgumentException 을 던진다.")
    void convertByteThrowsWhenUrlExceedsWidth() {
        // given : 21 byte url
        XHeader header = new XHeader("a".repeat(21), "tx1", ContentType.JSON, 1);

        // when / then
        assertThatThrownBy(header::convertByte)
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("convertByte() : transactionId 가 폭(20)을 초과하면 IllegalArgumentException 을 던진다.")
    void convertByteThrowsWhenTransactionIdExceedsWidth() {
        // given : 21 byte transactionId
        XHeader header = new XHeader("/api", "b".repeat(21), ContentType.JSON, 1);

        // when / then
        assertThatThrownBy(header::convertByte)
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("of() : 입력이 60 byte 미만(59)이면 IllegalArgumentException 을 던진다.")
    void ofThrowsWhenInputShorterThan60() {
        // given
        byte[] tooShort = new byte[59];

        // when / then
        assertThatThrownBy(() -> XHeader.of(tooShort))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Snowflake 정합성 : XIdGenerator.nextTransactionId() 로 만든 transactionId 는 round-trip 을 예외 없이 통과하고 보존된다(폭 20 적합성 회귀 방지).")
    void snowflakeTransactionIdRoundTrip() {
        // given : TSID 13자 transactionId (폭 20 적합)
        String transactionId = XIdGenerator.nextTransactionId();
        XHeader original = new XHeader("/order-api", transactionId, ContentType.JSON, 256);

        // when : convertByte()/of() round-trip 이 예외 없이 수행되어야 한다
        XHeader restored = XHeader.of(original.convertByte());

        // then
        assertThat(transactionId).hasSize(13);
        assertThat(restored.getTransactionId()).isEqualTo(transactionId);
        assertThat(restored.getUrl()).isEqualTo("/order-api");
        assertThat(restored.getContentType()).isEqualTo(ContentType.JSON);
        assertThat(restored.getContentLength()).isEqualTo(256);
    }
}
