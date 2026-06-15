package develop.x.simulator.game.service.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * QueryOption 의 비자명 포인트(현 상태 고정):
 *  - 상수 6개 (A001~A004, A006, A008) — A005·A007 결번.
 *  - A007 제거로 A001 과의 text 중복이 해소됨(6개 text 모두 고유).
 */
class QueryOptionTest {

    @Test
    @DisplayName("QueryOption 은 6개 상수를 가진다.(A005·A007 결번)")
    void hasSixConstants() {
        assertThat(QueryOption.values()).hasSize(6);
    }

    @Test
    @DisplayName("A005 는 정의되어 있지 않다.(결번)")
    void a005IsNotDefined() {
        assertThatThrownBy(() -> QueryOption.valueOf("A005"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("A007 은 정의되어 있지 않다.(결번 — 상수 제거)")
    void a007IsNotDefined() {
        assertThatThrownBy(() -> QueryOption.valueOf("A007"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("존재하는 상수와 getText() 매핑을 검증한다.")
    void textMapping() {
        assertThat(QueryOption.A001.getText()).isEqualTo("고정 환급률/배당률 투표권");
        assertThat(QueryOption.A002.getText()).isEqualTo("고정 환급률 게임 종목별 정보");
        assertThat(QueryOption.A003.getText()).isEqualTo("고정 배당률 기록식 종목별 정보");
        assertThat(QueryOption.A004.getText()).isEqualTo("고정 배당률 승부식 종목별 정보");
        assertThat(QueryOption.A006.getText()).isEqualTo("고정 환급률 발매가능 회차 목록");
        assertThat(QueryOption.A008.getText()).isEqualTo("고정 배당률 발매가능 회차 목록");
    }

    @Test
    @DisplayName("6개 상수의 text 는 모두 고유하다.(A001==A007 중복 해소 회귀 방지)")
    void allTextsAreUnique() {
        long distinctCount = Arrays.stream(QueryOption.values())
                .map(QueryOption::getText)
                .distinct()
                .count();
        assertThat(distinctCount).isEqualTo(QueryOption.values().length);
    }
}
