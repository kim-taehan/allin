package develop.x.io;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JsonUtilsTest {

    @Test
    @DisplayName("toByte 로 객체를 JSON byte[] 로 직렬화한다.")
    void toByteSerializesToJson() {
        // given
        Dto dto = new Dto("kim", 30);

        // when
        byte[] bytes = JsonUtils.toByte(dto);

        // then
        assertThat(new String(bytes, StandardCharsets.UTF_8)).isEqualTo("{\"name\":\"kim\",\"age\":30}");
    }

    @Test
    @DisplayName("toObject 로 JSON byte[] 를 객체로 역직렬화한다.")
    void toObjectDeserializesFromJson() {
        // given
        byte[] bytes = "{\"name\":\"lee\",\"age\":40}".getBytes(StandardCharsets.UTF_8);

        // when
        Dto dto = JsonUtils.toObject(bytes, Dto.class);

        // then
        assertThat(dto).isEqualTo(new Dto("lee", 40));
    }

    @Test
    @DisplayName("toByte -> toObject round-trip 동등성을 보장한다.")
    void roundTrip() {
        // given
        Dto original = new Dto("park", 25);

        // when
        Dto restored = JsonUtils.toObject(JsonUtils.toByte(original), Dto.class);

        // then
        assertThat(restored).isEqualTo(original);
    }

    @Test
    @DisplayName("toObject 에 잘못된 JSON 을 주면 RuntimeException 을 던진다.")
    void toObjectThrowsOnInvalidJson() {
        // given
        byte[] broken = "{not-json".getBytes(StandardCharsets.UTF_8);

        // when & then
        assertThatThrownBy(() -> JsonUtils.toObject(broken, Dto.class))
                .isInstanceOf(RuntimeException.class);
    }

    static class Dto {
        private final String name;
        private final int age;

        Dto(@JsonProperty("name") String name, @JsonProperty("age") int age) {
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
            Dto dto = (Dto) o;
            return age == dto.age && Objects.equals(name, dto.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, age);
        }
    }
}
