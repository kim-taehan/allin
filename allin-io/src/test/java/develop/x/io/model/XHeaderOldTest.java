package develop.x.io.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class XHeaderOldTest {

    @Test
    @DisplayName("convert() 는 40 byte 고정 길이 배열을 생성한다.")
    void convertProducesFixed40Bytes() {
        // given
        XHeaderOld header = new XHeaderOld(ContentType.JSON, "/api", "tx1");

        // when
        byte[] bytes = header.convert();

        // then
        assertThat(bytes).hasSize(40);
    }

    @Test
    @DisplayName("convert() 는 type(0)/url(10)/transactionId(30) 오프셋에 값을 배치한다.")
    void convertPlacesFieldsAtOffsets() {
        // given
        XHeaderOld header = new XHeaderOld(ContentType.JSON, "/api", "tx1");

        // when
        byte[] bytes = header.convert();

        // then : "JSON" 은 0부터, "/api" 는 10부터, "tx1" 은 30부터
        assertThat(new String(bytes, 0, 4)).isEqualTo("JSON");
        assertThat(new String(bytes, 10, 4)).isEqualTo("/api");
        assertThat(new String(bytes, 30, 3)).isEqualTo("tx1");
    }

    @Test
    @DisplayName("convert() -> of() round-trip 으로 필드가 복원된다.")
    void convertOfRoundTrip() {
        // given
        XHeaderOld original = new XHeaderOld(ContentType.SERIALIZE, "/order/create", "txn-12345");

        // when
        XHeaderOld restored = XHeaderOld.of(original.convert());

        // then
        assertThat(restored.getType()).isEqualTo(original.getType());
        assertThat(restored.getUrl()).isEqualTo(original.getUrl());
        assertThat(restored.getTransactionId()).isEqualTo(original.getTransactionId());
    }

    @Test
    @DisplayName("of() 는 zero-padding 을 trim 하여 필드를 복원한다.")
    void ofTrimsPadding() {
        // given
        byte[] bytes = new XHeaderOld(ContentType.JSON, "/x", "t").convert();

        // when
        XHeaderOld restored = XHeaderOld.of(bytes);

        // then
        assertThat(restored.getUrl()).isEqualTo("/x");
        assertThat(restored.getTransactionId()).isEqualTo("t");
    }

    @Test
    @DisplayName("toString 은 type/url/transactionId 를 포함한다.")
    void toStringContainsFields() {
        // given
        XHeaderOld header = new XHeaderOld(ContentType.JSON, "/api", "tx1");

        // when
        String text = header.toString();

        // then
        assertThat(text).contains("type=JSON").contains("url='/api'").contains("transactionId='tx1'");
    }
}
