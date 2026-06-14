package develop.x.core.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;


class XCacheTest {

    private final SingleServerCache<String, String> xCache = new SingleServerCache<String, String>() {
    };

    @BeforeEach
    void init() {
        xCache.clearCache();
    }


    @DisplayName("getOrDefault 은 데이터가 존재하지 않으면 대체값을 반환한다.")
    @Test
    void getOrDefault() {
        // given & when
        String item = xCache.getOrDefault("test", this::findReplacementValue);

        // then
        assertThat(item).isEqualTo(findReplacementValue());
        // getOrDefault 는 조회만 하므로 캐시에 저장되지 않아야 한다.
        assertThat(xCache.containsKey("test")).isFalse();
    }

    @DisplayName("getIfAbsent 는 키가 존재하지 않으면 supplier 값을 저장하고 그 값을 반환한다.")
    @Test
    void getIfAbsent() {
        // given & when
        String returned = xCache.getIfAbsent("item", this::findReplacementValue);

        // then
        assertAll(
                // 반환값 자체가 supplier 결과여야 한다(메서드 계약).
                () -> assertThat(returned).isEqualTo(findReplacementValue()),
                // 실제 캐시에도 저장되어야 한다.
                () -> assertThat(xCache.get("item")).isEqualTo(findReplacementValue()),
                () -> assertThat(xCache.containsKey("item")).isTrue()
        );
    }

    @DisplayName("getIfAbsent 는 키가 존재하면 supplier 를 무시하고 기존값을 반환한다(덮어쓰지 않는다).")
    @Test
    void getIfAbsentExistKey() {
        // given
        xCache.put("item", "default item");

        // when
        String returned = xCache.getIfAbsent("item", this::findReplacementValue);

        // then
        assertAll(
                // 반환값은 기존값이어야 한다(supplier 값이 아니다).
                () -> assertThat(returned).isEqualTo("default item"),
                // 캐시값도 기존값으로 유지되어 덮어써지지 않아야 한다.
                () -> assertThat(xCache.get("item")).isEqualTo("default item")
        );
    }

    @DisplayName("getIfAbsent 는 키가 존재하면 supplier 를 호출하지 않는다.")
    @Test
    void getIfAbsentDoesNotInvokeSupplierWhenPresent() {
        // given
        xCache.put("item", "default item");
        boolean[] supplierCalled = {false};

        // when
        String returned = xCache.getIfAbsent("item", () -> {
            supplierCalled[0] = true;
            return "should not be used";
        });

        // then
        assertAll(
                () -> assertThat(returned).isEqualTo("default item"),
                () -> assertThat(supplierCalled[0]).isFalse()
        );
    }


    private String findReplacementValue() {
        return "update item";
    }


}
