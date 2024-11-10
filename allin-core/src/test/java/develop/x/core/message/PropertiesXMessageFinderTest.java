package develop.x.core.message;

import develop.x.core.SpringBootHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PropertiesXMessageFinderTest extends SpringBootHelper {

    @Autowired
    XMessageFinder messageFinder;

    @Test
    @DisplayName("메시지 아이디와 args 로 properties 에 정의한 메시지를 읽을 수 있다.")
    void findMessage(){
        // given
        String messageId = "TEST_MESSAGE";
        String arg = "world";

        // when
        String ret = messageFinder.find(messageId, arg).orElseThrow();

        // then
        assertThat(ret).isEqualTo("hello " + arg + "!");
    }

    @Test
    @DisplayName("정의되지 않은 메시지를 읽는 경우 optional Empty 가 반환된다.")
    void findMessageException(){
        // given
        String messageId = "NO_MESSAGE";
        String arg = "world";

        // when
        Optional<String> ret = messageFinder.find(messageId, arg);

        // then
        assertThat(ret.isEmpty()).isTrue();
    }

}