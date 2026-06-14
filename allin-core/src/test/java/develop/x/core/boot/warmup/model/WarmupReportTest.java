package develop.x.core.boot.warmup.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class WarmupReportTest {

    @Test
    @DisplayName("updateTime 은 전달한 시작 시각으로부터 현재까지의 경과시간(ms)을 계산해 duration 에 반영한다.")
    void updateTime() {

        // given
        WarmupReport job001 = WarmupReport.builder()
                .warmupResult(WarmupResult.SUCCESS)
                .jobName("job 001")
                .duration(0)
                .build();

        // 과거 시각을 시작 시각으로 사용하면 경과시간은 최소 그 차이만큼은 되어야 한다.
        long elapsedAtLeast = 50;
        long startTime = System.currentTimeMillis() - elapsedAtLeast;

        // when
        long beforeUpdate = System.currentTimeMillis();
        job001.updateTime(startTime);
        long afterUpdate = System.currentTimeMillis();

        // then
        // duration == (updateTime 호출 시점의 now) - startTime 이므로
        // 하한: (beforeUpdate - startTime), 상한: (afterUpdate - startTime) 범위에 들어야 한다.
        assertAll(
                () -> assertThat(job001.getDuration()).isGreaterThanOrEqualTo(elapsedAtLeast),
                () -> assertThat(job001.getDuration()).isBetween(beforeUpdate - startTime, afterUpdate - startTime),
                () -> assertThat(job001.getJobName()).isEqualTo("job 001"),
                () -> assertThat(job001.getWarmupResult()).isEqualTo(WarmupResult.SUCCESS)
        );
    }

    @Test
    @DisplayName("updateTime 은 builder 로 초기 설정된 duration 값을 새로 계산한 값으로 덮어쓴다.")
    void updateTimeOverwritesInitialDuration() {

        // given
        WarmupReport report = WarmupReport.builder()
                .warmupResult(WarmupResult.SUCCESS)
                .jobName("overwrite")
                .duration(999_999)
                .build();
        assertThat(report.getDuration()).isEqualTo(999_999);

        // when (현재 시각을 시작 시각으로 주면 경과시간은 0 에 가깝다)
        report.updateTime(System.currentTimeMillis());

        // then (초기 999_999 가 그대로 남아있지 않고 작은 경과시간으로 갱신됨)
        assertThat(report.getDuration()).isLessThan(999_999);
        assertThat(report.getDuration()).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("FAIL 결과의 WarmupReport 도 정상적으로 생성되고 getter 로 값을 조회할 수 있다.")
    void buildFailReport() {

        // given & when
        WarmupReport report = WarmupReport.builder()
                .warmupResult(WarmupResult.FAIL)
                .jobName("fail job")
                .duration(123)
                .build();

        // then
        assertAll(
                () -> assertThat(report.getWarmupResult()).isEqualTo(WarmupResult.FAIL),
                () -> assertThat(report.getJobName()).isEqualTo("fail job"),
                () -> assertThat(report.getDuration()).isEqualTo(123)
        );
    }

}
