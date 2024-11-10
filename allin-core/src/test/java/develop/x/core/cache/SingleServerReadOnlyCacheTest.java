package develop.x.core.cache;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SingleServerReadOnlyCacheTest {
    
    
    @DisplayName("SingleServerReadOnlyCache 데이터를 조회할 수 있다.")
    @Test
    void selectedItem(){
        
        // given
        SimpleCache simpleCache = new SimpleCache();
        
        // when
        String ret = simpleCache.get("kor");
        
        // then
        assertThat(ret).isEqualTo("한글");
    }


    @DisplayName("SingleServerReadOnlyCache clearCache 메서드를 지원하지 않는다.")
    @Test
    void clearCacheNotSupports(){

        // given
        SimpleCache simpleCache = new SimpleCache();

        // when
        // then
        Assertions.assertThatThrownBy(simpleCache::clearCache)
                .isInstanceOf(IllegalStateException.class);
    }


    static class SimpleCache extends SingleServerReadOnlyCache<String, String> {
        @Override
        protected Map<String, String> initialize() {
            return Map.of("kor", "한글", "eng", "영어");
        }
    }

}