package develop.x.io.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class ContentTypeTest {

    @Test
    @DisplayName("JSON 타입은 JsonModelConverter 로 위임하여 round-trip 한다.")
    void jsonRoundTrip() {
        // given
        JsonDto dto = new JsonDto("kim", 30);

        // when
        byte[] bytes = ContentType.JSON.toByte(dto);
        Object restored = ContentType.JSON.toModel(JsonDto.class, bytes);

        // then
        assertThat(new String(bytes, StandardCharsets.UTF_8)).isEqualTo("{\"name\":\"kim\",\"age\":30}");
        assertThat(restored).isEqualTo(dto);
    }

    @Test
    @DisplayName("SERIALIZE 타입은 SerializableModelConverter 로 위임하여 round-trip 한다.")
    void serializeRoundTrip() {
        // given
        SerialDto dto = new SerialDto("lee", 40);

        // when
        byte[] bytes = ContentType.SERIALIZE.toByte(dto);
        Object restored = ContentType.SERIALIZE.toModel(SerialDto.class, bytes);

        // then
        assertThat(restored).isEqualTo(dto);
    }

    @Test
    @DisplayName("valueOf 로 enum 상수를 얻을 수 있다.")
    void valueOf() {
        assertThat(ContentType.valueOf("JSON")).isEqualTo(ContentType.JSON);
        assertThat(ContentType.valueOf("SERIALIZE")).isEqualTo(ContentType.SERIALIZE);
    }

    static class JsonDto {
        private final String name;
        private final int age;

        JsonDto(@JsonProperty("name") String name, @JsonProperty("age") int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            JsonDto dto = (JsonDto) o;
            return age == dto.age && Objects.equals(name, dto.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, age);
        }
    }

    static class SerialDto implements Serializable {
        private final String name;
        private final int age;

        SerialDto(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SerialDto dto = (SerialDto) o;
            return age == dto.age && Objects.equals(name, dto.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, age);
        }
    }
}
