package develop.x.io;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class XRequestTest {

    @Test
    @DisplayName("byte[] 생성자로 여러 헤더와 body 를 파싱한다.")
    void parseMultipleHeadersAndBody() {
        // given : header1=a\nheader2=b\n\nBODY\n
        String raw = "header1=a\nheader2=b\n\nBODY\n";
        byte[] bytes = raw.getBytes(StandardCharsets.UTF_8);

        // when
        XRequest request = new XRequest(bytes);

        // then
        assertThat(request.getHeaders())
                .containsEntry("header1", "a")
                .containsEntry("header2", "b")
                .hasSize(2);
        assertThat(new String(request.getBody(), StandardCharsets.UTF_8)).isEqualTo("BODY");
    }

    @Test
    @DisplayName("단일 헤더와 body 를 파싱한다.")
    void parseSingleHeaderAndBody() {
        // given
        byte[] bytes = "only=one\n\nDATA\n".getBytes(StandardCharsets.UTF_8);

        // when
        XRequest request = new XRequest(bytes);

        // then
        assertThat(request.getHeaders()).containsExactlyEntriesOf(Map.of("only", "one"));
        assertThat(new String(request.getBody(), StandardCharsets.UTF_8)).isEqualTo("DATA");
    }

    @Test
    @DisplayName("헤더 key/value 의 좌우 공백은 trim 된다.")
    void headerValuesAreTrimmed() {
        // given
        byte[] bytes = "  key   =   value  \n\nbody\n".getBytes(StandardCharsets.UTF_8);

        // when
        XRequest request = new XRequest(bytes);

        // then
        assertThat(request.getHeaders()).containsEntry("key", "value");
    }

    @Test
    @DisplayName("선행 빈 줄(헤더 없음) 뒤의 body 를 파싱한다.")
    void parseBodyOnly() {
        // given : 와이어 포맷상 헤더가 없으면 첫 바이트가 빈 줄(\n)이고 그 뒤가 body 다.
        byte[] bytes = "\nHELLO\n".getBytes(StandardCharsets.UTF_8);

        // when
        XRequest request = new XRequest(bytes);

        // then : 헤더는 비어있고, 종단 개행 1개만 제거되어 body 가 온전히 보존된다.
        assertThat(request.getHeaders()).isEmpty();
        assertThat(new String(request.getBody(), StandardCharsets.UTF_8)).isEqualTo("HELLO");
    }

    @Test
    @DisplayName("toByte() 직렬화 후 생성자 역직렬화 round-trip 동등성을 보장한다.")
    void roundTripViaBuilder() {
        // given
        XRequest original = new XRequest.Builder()
                .header("url", "/test-api")
                .header("transactionId", "tx-123")
                .body("hello-body".getBytes(StandardCharsets.UTF_8))
                .build();

        // when
        byte[] serialized = original.toByte();
        XRequest restored = new XRequest(serialized);

        // then
        assertThat(restored.getHeaders()).isEqualTo(original.getHeaders());
        assertThat(restored.getBody()).isEqualTo(original.getBody());
    }

    // ---------------------------------------------------------------------
    // 리뷰어 M-1 보강: 수정이 겨냥한 페이로드를 생산자 전 경로
    // (Builder -> toByte() -> new XRequest(bytes)) 로 round-trip 검증.
    // 단순 byte[] 입력이 아니라 toByte() 가 실제로 만드는 와이어 포맷을
    // 다시 파싱하므로, 마지막 바이트/value '='/body 내부·종단 '\n' 수정이
    // 생산자 경로에서도 회귀하지 않음을 보장한다.
    // ---------------------------------------------------------------------
    static Stream<Arguments> roundTripBodies() {
        return Stream.of(
                Arguments.of("빈 body", new byte[0]),
                Arguments.of("body 내부 개행", "l1\nl2".getBytes(StandardCharsets.UTF_8)),
                Arguments.of("body 종단 개행", "data\n".getBytes(StandardCharsets.UTF_8)),
                Arguments.of("body 에 '=' 포함", "k=v&a=b".getBytes(StandardCharsets.UTF_8))
        );
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("roundTripBodies")
    @DisplayName("Builder->toByte()->new XRequest(bytes) 전 경로에서 headers/body 가 그대로 복원된다.")
    void roundTripViaBuilderPayloads(String label, byte[] body) {
        // given : 명시 contentType + 사용자 헤더 + 대상 페이로드
        XRequest original = new XRequest.Builder()
                .header("url", "/test-api")
                .header("transactionId", "tx-123")
                .header("contentType", "SERIALIZE")
                .body(body)
                .build();

        // when : 생산자 직렬화 -> 소비자 역직렬화
        byte[] serialized = original.toByte();
        XRequest restored = new XRequest(serialized);

        // then : headers(contentType/contentLength 포함) 와 body 바이트가 원본과 일치
        assertThat(restored.getHeaders())
                .containsEntry("contentType", "SERIALIZE")
                .containsEntry("contentLength", String.valueOf(body.length))
                .isEqualTo(original.getHeaders());
        assertThat(restored.getBody()).isEqualTo(body);
    }

    @Test
    @DisplayName("Builder 는 contentType 미지정 시 JSON 을 기본값으로 채우고 contentLength 를 계산한다.")
    void builderDefaultsContentTypeAndLength() {
        // given
        byte[] body = "abcde".getBytes(StandardCharsets.UTF_8);

        // when
        XRequest request = new XRequest.Builder().body(body).build();

        // then
        assertThat(request.getHeaders())
                .containsEntry("contentType", "JSON")
                .containsEntry("contentLength", "5");
    }

    @Test
    @DisplayName("Builder 에서 contentType 을 명시하면 기본값으로 덮어쓰지 않는다.")
    void builderKeepsExplicitContentType() {
        // given & when
        XRequest request = new XRequest.Builder()
                .header("contentType", "SERIALIZE")
                .body(new byte[]{1, 2, 3})
                .build();

        // then
        assertThat(request.getHeaders()).containsEntry("contentType", "SERIALIZE");
        assertThat(request.getHeaders()).containsEntry("contentLength", "3");
    }

    @Test
    @DisplayName("Builder.body(Object) 는 JsonUtils 로 직렬화된다.")
    void builderBodyObjectSerializesAsJson() {
        // given & when
        XRequest request = new XRequest.Builder()
                .body(Map.of("k", "v"))
                .build();

        // then
        String json = new String(request.getBody(), StandardCharsets.UTF_8);
        assertThat(json).isEqualTo("{\"k\":\"v\"}");
        assertThat(request.getHeaders()).containsEntry("contentLength", String.valueOf(json.length()));
    }

    @Test
    @DisplayName("toString 은 headers 와 body 를 포함한다.")
    void toStringContainsHeadersAndBody() {
        // given
        XRequest request = new XRequest.Builder()
                .header("url", "/x")
                .body(new byte[]{65})
                .build();

        // when
        String text = request.toString();

        // then
        assertThat(text).contains("headers=").contains("url=/x").contains("body=");
    }

    // ---------------------------------------------------------------------
    // 아래는 버그 수정 후의 올바른 동작을 검증하는 테스트.
    // (수정 전에는 현재 버그 동작을 고정하던 characterization 테스트였다.)
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("헤더 value 에 '=' 가 포함되어도 split('=',2) 로 전체 값이 보존된다.")
    void headerValueWithEqualsSignIsPreserved() {
        // given : value = "a=b=c"
        byte[] bytes = "key=a=b=c\n\nbody\n".getBytes(StandardCharsets.UTF_8);

        // when
        XRequest request = new XRequest(bytes);

        // then : split("=",2) -> ["key","a=b=c"], 값 전체 보존
        assertThat(request.getHeaders()).containsEntry("key", "a=b=c");
    }

    @Test
    @DisplayName("'=' 없는 헤더 라인은 예외 없이 빈 문자열 값으로 파싱된다.")
    void headerLineWithoutEqualsBecomesEmptyValue() {
        // given : "novalue" 라인 후 개행 -> split("=",2) -> ["novalue"], 값 없음
        byte[] bytes = "novalue\n\nbody\n".getBytes(StandardCharsets.UTF_8);

        // when
        XRequest request = new XRequest(bytes);

        // then : 예외 없이 빈 문자열 값으로 채워지고 body 도 정상 파싱된다.
        assertThat(request.getHeaders()).containsEntry("novalue", "");
        assertThat(new String(request.getBody(), StandardCharsets.UTF_8)).isEqualTo("body");
    }

    @Test
    @DisplayName("마지막 바이트(종단 개행이 아니면)는 잘리지 않고 모두 파싱된다.")
    void lastByteIsParsed() {
        // given : 선행 빈 줄(헤더 없음) 뒤 두 바이트, 종단 개행 없음
        byte[] bytes = "\nAB".getBytes(StandardCharsets.UTF_8);

        // when
        XRequest request = new XRequest(bytes);

        // then : 'A','B' 모두 body 에 보존된다.
        assertThat(new String(request.getBody(), StandardCharsets.UTF_8)).isEqualTo("AB");
    }

    @Test
    @DisplayName("body 내부의 '\\n' 은 헤더로 오파싱되지 않고 body 로 보존된다.")
    void newlineInsideBodyIsPreserved() {
        // given : 헤더 a=1, 경계 빈 줄, body 에 개행 포함("line1\nline2")
        byte[] bytes = "a=1\n\nline1\nline2\n".getBytes(StandardCharsets.UTF_8);

        // when
        XRequest request = new XRequest(bytes);

        // then : "a=1" 만 헤더, 경계 이후 전체가 body (종단 개행 1개만 제거)
        assertThat(request.getHeaders()).containsExactlyEntriesOf(Map.of("a", "1"));
        assertThat(new String(request.getBody(), StandardCharsets.UTF_8)).isEqualTo("line1\nline2");
    }
}
