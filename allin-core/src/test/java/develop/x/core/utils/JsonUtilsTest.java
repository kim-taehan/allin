package develop.x.core.utils;

import lombok.Data;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

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

}