package develop.x.core.utils;

import lombok.Data;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JsonUtilsTest {

    @Test
    @DisplayName("Json byte[] 형태로 변환하고 원복할 수 있다.")
    void toByteAndToObject() {
        // given
        TestDto testDto = new TestDto();
        testDto.setAge(40);
        testDto.setUsername("kimtaehan");
        // when
        byte[] aByte = JsonUtils.toByte(testDto);
        TestDto convertDto = JsonUtils.toObject(aByte, TestDto.class);

        // then
        assertThat(convertDto).isEqualTo(testDto);
    }

    @Test
    @DisplayName("깨진 byte[] 를 역직렬화하면 RuntimeException 으로 래핑되어 던져진다.")
    void toObjectThrowsOnBrokenBytes() {
        // given - 유효한 JSON 이 아닌 바이트
        byte[] brokenBytes = new byte[]{1, 2, 3};

        // when & then
        assertThatThrownBy(() -> JsonUtils.toObject(brokenBytes, TestDto.class))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("타입과 호환되지 않는 JSON 을 역직렬화하면 RuntimeException 으로 래핑되어 던져진다.")
    void toObjectThrowsOnTypeMismatch() {
        // given - age 는 int 인데 문자열을 넣어 매핑 실패를 유도
        byte[] mismatch = "{\"username\":\"x\",\"age\":\"not-a-number\"}".getBytes();

        // when & then
        assertThatThrownBy(() -> JsonUtils.toObject(mismatch, TestDto.class))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("직렬화할 수 없는 객체를 byte[] 로 변환하면 RuntimeException 으로 래핑되어 던져진다.")
    void toByteThrowsOnUnserializable() {
        // given - getter 가 예외를 던지는 객체

        // when & then
        assertThatThrownBy(() -> JsonUtils.toByte(new ThrowingGetterDto()))
                .isInstanceOf(RuntimeException.class);
    }


    @Data
    public static class TestDto {
        private String username;
        private int age;

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (object == null || getClass() != object.getClass()) return false;
            TestDto testDto = (TestDto) object;
            return age == testDto.age && Objects.equals(username, testDto.username);
        }

        @Override
        public int hashCode() {
            return Objects.hash(username, age);
        }
    }

    public static class ThrowingGetterDto {
        public String getValue() {
            throw new IllegalStateException("getter 실패");
        }
    }

}