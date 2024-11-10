package develop.x.io.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class SerializableModelConverterTest {

    SerializableModelConverter converter = new SerializableModelConverter();

    @Test
    @DisplayName("serializable model converter 를 통해 모델을 원복할 수 있다.")
    void convertJsonModelConverter(){
        // given
        TestDto 홍길동 = new TestDto("홍길동", 40);
        byte[] bytes = toByte(홍길동);

        // then
        TestDto model = converter.toModel(TestDto.class, bytes);

        // when
        assertThat(model).isEqualTo(홍길동);
    }

    private byte[] toByte(Object object){

        try(ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try(ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(object);
                //직렬화(byte array)
                return baos.toByteArray();
            }
        } catch (IOException e) {
            throw new IllegalArgumentException();
        }
    }


    static class TestDto implements Serializable {

        private final String username;
        private final int age;

        public TestDto(String username, int age) {
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