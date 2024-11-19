package develop.x.core.cache;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class XCacheTest {

    private SingleServerCache<String, String> xCache = new SingleServerCache<String, String>() {

    };

    @BeforeEach
    void init(){
        xCache.clearCache();
    }


    @DisplayName("getOrDefault 은 데이터가 존재하지 않으면 대체값으로 조회한다.")
    @Test
    void getOrDefault(){
        // given
        String item = xCache.getOrDefault("test", this::findReplacementValue);

        // when

        // then
        assertThat(item).isEqualTo(findReplacementValue());
    }

    @DisplayName("getIfAbsent 는 요청한 키에 데이터가 존재하지 않는 경우에 대체갑으로 입력하고 대체값을 반환한다.")
    @Test
    void getIfAbsent(){
        // given
        String insertItem = xCache.getIfAbsent("item", this::findReplacementValue);
        // when

        String findItem = xCache.get("item");
        // then
        assertThat(findItem).isEqualTo(findReplacementValue());
    }

    @DisplayName("getIfAbsent 는 요청한 키에 데이터가 존재하면 않는 경우에 대체갑으로 무시하고 기존값을 반환한다.")
    @Test
    void getIfAbsentExistKey(){
        // given
        xCache.put("item", "default item");
        String insertItem = xCache.getIfAbsent("item", this::findReplacementValue);
        // when

        String findItem = xCache.get("item");
        // then
        assertThat(findItem).isEqualTo("default item");
    }


    private String findReplacementValue(){
        return "update item";
    }




}