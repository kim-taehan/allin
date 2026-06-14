package develop.x.core.message;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;

import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PropertiesXMessageFinderTest {

    private XMessageFinder messageFinder;

    @BeforeEach
    void setUp() {
        // 순수 단위 테스트: 전체 Spring 컨텍스트를 띄우지 않고 MessageSource 만 주입한다.
        StaticMessageSource messageSource = new StaticMessageSource();
        // 인자 존재 시 MessageFormat 이 적용되도록 설정(StaticMessageSource 는 기본적으로 args 가 있으면 포맷팅).
        messageSource.addMessage("TEST_MESSAGE", Locale.KOREA, "hello {0}!");
        messageSource.addMessage("MULTI_MESSAGE", Locale.KOREA, "{0} 님 {1} 환영합니다");
        messageSource.addMessage("NO_ARG_MESSAGE", Locale.KOREA, "고정 메시지");
        this.messageFinder = new PropertiesXMessageFinder(messageSource);
    }

    @Test
    @DisplayName("메시지 아이디와 단일 arg 로 정의한 메시지를 포맷팅하여 읽을 수 있다.")
    void findMessage() {
        // given
        String messageId = "TEST_MESSAGE";
        String arg = "world";

        // when
        String ret = messageFinder.find(messageId, arg).orElseThrow();

        // then
        assertThat(ret).isEqualTo("hello " + arg + "!");
    }

    @Test
    @DisplayName("여러 개의 arg 로 메시지의 모든 플레이스홀더를 채울 수 있다.")
    void findMessageWithMultipleArgs() {
        // given
        String messageId = "MULTI_MESSAGE";

        // when
        String ret = messageFinder.find(messageId, "김태한", "어서").orElseThrow();

        // then
        assertThat(ret).isEqualTo("김태한 님 어서 환영합니다");
    }

    @Test
    @DisplayName("플레이스홀더 개수보다 arg 가 부족하면 미치환 플레이스홀더({1})가 그대로 남는다.")
    void findMessageWithMissingArg() {
        // given - MULTI_MESSAGE 는 {0}{1} 두 개인데 arg 를 한 개만 전달
        String messageId = "MULTI_MESSAGE";

        // when
        String ret = messageFinder.find(messageId, "김태한").orElseThrow();

        // then - {0} 만 치환되고 {1} 은 그대로 남는다.
        assertThat(ret).isEqualTo("김태한 님 {1} 환영합니다");
    }

    @Test
    @DisplayName("arg 없이 호출하면 플레이스홀더가 없는 메시지를 그대로 반환한다.")
    void findMessageWithoutArgs() {
        // given
        String messageId = "NO_ARG_MESSAGE";

        // when
        String ret = messageFinder.find(messageId).orElseThrow();

        // then
        assertThat(ret).isEqualTo("고정 메시지");
    }

    @Test
    @DisplayName("정의되지 않은 메시지를 읽는 경우 optional Empty 가 반환된다.")
    void findMessageException() {
        // given
        String messageId = "NO_MESSAGE";
        String arg = "world";

        // when
        Optional<String> ret = messageFinder.find(messageId, arg);

        // then
        assertThat(ret).isEmpty();
    }

}
