package develop.x.core.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class SingleServerCacheTest {

    MonthCache monthCache = new MonthCache();

    @BeforeEach
    void init(){
        monthCache.put(1, "1월");
        monthCache.put(2, "2월");
        monthCache.put(3, "3월");
        monthCache.put(4, "4월");
        monthCache.put(5, "5월");
        monthCache.put(6, "6월");
        monthCache.put(7, "7월");
        monthCache.put(8, "8월");
        monthCache.put(9, "9월");
        monthCache.put(10, "10월");
    }


    @Test
    @DisplayName("cache data 를 조회할 수 있다.")
    void getItem(){

        // when
        String month = monthCache.get(6);

        // then
        assertThat(month).isEqualTo("6월");
    }

    @Test
    @DisplayName("key 없는 데이터는 null 을 반환한다.")
    void getEmptyItem(){

        // when
        String month = monthCache.get(11);

        // then
        assertThat(month).isNull();
    }


    @Test
    @DisplayName("containsKey 메소드로 key에 해당하는 value가 있는지 확인할 수 있다.")
    void containsKey(){

        // when
        boolean isFeb = monthCache.containsKey(2);
        boolean isDec = monthCache.containsKey(12);

        // then
        assertAll(
                () -> assertThat(isFeb).isTrue(),
                () -> assertThat(isDec).isFalse()
        );
    }


    @Test
    @DisplayName("clearCache 메소드로 해당 캐쉬 데이터를 초기화할 수 있다.")
    void clearCache() {

        // given & when
        monthCache.clearCache();

        // then
        assertThat(monthCache.size()).isEqualTo(0);
    }


    @Test
    @DisplayName("n건의 item을 한번에 입력할 수 있다.")
    void putAll() {

        // given
        Map<Integer, String> addItems = Map.of(11, "11월", 12, "12월");

        // when
        monthCache.putAll(addItems);

        // then
        assertAll(
                () -> assertThat(monthCache.size()).isEqualTo(12),
                () -> assertThat(monthCache.get(12)).isEqualTo("12월")
        );
    }

    @Test
    @DisplayName("putAll 에 기존 key 가 포함되면 해당 값을 덮어쓴다.")
    void putAllOverwritesExistingKey() {

        // given - 1월(기존)과 11월(신규)을 함께 입력. 1월 값은 덮어써야 한다.
        Map<Integer, String> addItems = Map.of(1, "January", 11, "11월");

        // when
        monthCache.putAll(addItems);

        // then
        assertAll(
                () -> assertThat(monthCache.size()).isEqualTo(11),
                () -> assertThat(monthCache.get(1)).isEqualTo("January"),
                () -> assertThat(monthCache.get(11)).isEqualTo("11월")
        );
    }

    @Test
    @DisplayName("put 으로 기존 key 의 값을 갱신할 수 있다.")
    void putUpdatesExistingKey() {

        // given & when
        monthCache.put(1, "January");

        // then
        assertAll(
                () -> assertThat(monthCache.get(1)).isEqualTo("January"),
                () -> assertThat(monthCache.size()).isEqualTo(10)
        );
    }

    static class MonthCache extends SingleServerCache<Integer, String> {
    }


}