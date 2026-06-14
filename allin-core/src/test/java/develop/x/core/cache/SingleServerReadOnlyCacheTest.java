package develop.x.core.cache;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class SingleServerReadOnlyCacheTest {


    @DisplayName("SingleServerReadOnlyCache 데이터를 조회할 수 있다.")
    @Test
    void selectedItem() {

        // given
        SimpleCache simpleCache = new SimpleCache();

        // when
        String ret = simpleCache.get("kor");

        // then
        assertThat(ret).isEqualTo("한글");
    }


    @DisplayName("존재하지 않는 key 를 조회하면 null 을 반환한다.")
    @Test
    void getMissingKeyReturnsNull() {

        // given
        SimpleCache simpleCache = new SimpleCache();

        // when
        String ret = simpleCache.get("jpn");

        // then
        assertThat(ret).isNull();
    }


    @DisplayName("containsKey 로 key 존재 여부를 확인할 수 있다.")
    @Test
    void containsKey() {

        // given
        SimpleCache simpleCache = new SimpleCache();

        // when & then
        assertAll(
                () -> assertThat(simpleCache.containsKey("kor")).isTrue(),
                () -> assertThat(simpleCache.containsKey("eng")).isTrue(),
                () -> assertThat(simpleCache.containsKey("jpn")).isFalse()
        );
    }


    @DisplayName("size 로 초기화된 데이터 개수를 알 수 있다.")
    @Test
    void size() {

        // given
        SimpleCache simpleCache = new SimpleCache();

        // when & then
        assertThat(simpleCache.size()).isEqualTo(2);
    }


    @DisplayName("SingleServerReadOnlyCache clearCache 메서드를 지원하지 않는다.")
    @Test
    void clearCacheNotSupports() {

        // given
        SimpleCache simpleCache = new SimpleCache();

        // when
        // then
        Assertions.assertThatThrownBy(simpleCache::clearCache)
                .isInstanceOf(IllegalStateException.class);
    }


    @DisplayName("initialize 가 가변 맵을 반환하더라도, 생성 이후 원본 맵을 변경하면 ConcurrentModification 없이 안전하게 조회되며 데이터 추가 API 는 노출되지 않는다.")
    @Test
    void readOnlyContractNoMutationApi() {

        // given - 가변 source 맵으로 초기화한 read-only cache.
        // 생성자에서 Collections.unmodifiableMap 으로 감싸므로 내부 변경 경로(put/putAll/clear) 자체가
        // public API 에 존재하지 않는다(컴파일 타임 보장). 유일한 변경 시도 진입점인 clearCache 는 예외.
        MutableSourceCache cache = new MutableSourceCache();

        // when & then
        assertAll(
                () -> assertThat(cache.get("a")).isEqualTo("A"),
                () -> assertThat(cache.containsKey("b")).isTrue(),
                () -> assertThat(cache.size()).isEqualTo(2),
                // ReadOnly 계약: 데이터 비우기(변경)도 거부.
                () -> Assertions.assertThatThrownBy(cache::clearCache)
                        .isInstanceOf(IllegalStateException.class)
        );
    }


    static class SimpleCache extends SingleServerReadOnlyCache<String, String> {
        @Override
        protected Map<String, String> initialize() {
            return Map.of("kor", "한글", "eng", "영어");
        }
    }

    static class MutableSourceCache extends SingleServerReadOnlyCache<String, String> {
        @Override
        protected Map<String, String> initialize() {
            // 가변 맵을 넘겨도 read-only cache 로서 변경 API 가 노출되지 않음을 검증.
            return new java.util.HashMap<>(Map.of("a", "A", "b", "B"));
        }
    }

}
