package develop.x.core.boot.warmup.model;

import lombok.Builder;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class WarmupReport {

    private final String jobName;
    private final WarmupResult warmupResult;
    private long duration;

    @Builder
    public WarmupReport(String jobName, WarmupResult warmupResult, long duration) {
        this.jobName = jobName;
        this.warmupResult = warmupResult;
        this.duration = duration;
    }

    public void updateTime(long startTime) {
        this.duration = getDuration(startTime);
    }

    private long getDuration(long startTime) {
        return System.currentTimeMillis() - startTime;
    }

    @Override
    public String toString() {
        return "{" +
                "jobName='" + jobName + '\'' +
                ", warmupResult=" + warmupResult +
                ", duration=" + duration + "ms" +
                '}';
    }
}
