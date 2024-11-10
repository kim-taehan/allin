package develop.x.io.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JsonModelConverterTest {

    JsonModelConverter jsonModelConverter = new JsonModelConverter();
    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("json model converter 를 통해 모델을 원복할 수 있다.")
    void convertJsonByteToModel() throws JsonProcessingException {

        // given
        TestDto 홍길동 = new TestDto("홍길동", 40);
        byte[] bytes = objectMapper.writeValueAsBytes(홍길동);

        // then
        TestDto model = jsonModelConverter.toModel(TestDto.class, bytes);

        // when
        assertThat(model).isEqualTo(홍길동);
    }

    @Test
    @DisplayName("json model converter 에 잘못된 타입으로 변환하면 runtime error 가 발생한다.")
    void convertJsonByteToModelEx() throws JsonProcessingException {

        // given
        TestDto 홍길동 = new TestDto("홍길동", 40);
        byte[] bytes = objectMapper.writeValueAsBytes(홍길동);

        // then && when
        assertThatThrownBy(() -> jsonModelConverter.toModel(NoTestData.class, bytes))
                .isInstanceOf(RuntimeException.class);
    }





    static class TestDto {

        private final String username;
        private final int age;

        public TestDto(@JsonProperty("username") String username, @JsonProperty("age") int age) {
            this.username = username;
            this.age = age;
        }

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

        public String getUsername() {
            return username;
        }

        public int getAge() {
            return age;
        }
    }

    static class NoTestData {

    }

}