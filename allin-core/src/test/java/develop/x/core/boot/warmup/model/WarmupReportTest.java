package develop.x.core.boot.warmup.model;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class WarmupReportTest {

    @Test
    @DisplayName("WarmupReport 를 생성하고 updateTime method 를 통해 수행시간을 입력할 수 있다.")
    void updateTime() {

        WarmupReport job001 = WarmupReport.builder()
                .warmupResult(WarmupResult.SUCCESS)
                .jobName("job 001")
                .build();

        job001.updateTime(System.currentTimeMillis());

        assertThat(job001.getDuration()).isGreaterThan(-1);
    }

}