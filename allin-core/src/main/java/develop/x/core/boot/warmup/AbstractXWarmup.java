package develop.x.core.boot.warmup;

import develop.x.core.boot.warmup.model.WarmupReport;
import develop.x.core.boot.warmup.model.WarmupResult;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public non-sealed abstract class AbstractXWarmup implements XWarmup {

    private static final int TIME_OUT = 3;

    @Override
    public String getWarmupName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public CompletableFuture<WarmupReport> execute() {

        long startTime = System.currentTimeMillis();
        return CompletableFuture.supplyAsync(() -> warmup(startTime))
                .orTimeout(TIME_OUT, TimeUnit.SECONDS)
                .exceptionally(ex -> {
                    log.info("test = {}", ex.getClass());
                    log.info("test = {}", ex.getMessage());
                    return WarmupReport.builder()
                            .jobName(getWarmupName())
                            .warmupResult(ex instanceof TimeoutException ? WarmupResult.TIMEOUT: WarmupResult.FAIL)
                            .duration(System.currentTimeMillis() - startTime)
                            .build();
                });
    }

    private WarmupReport warmup(long startedTime) {
        WarmupReport warmup = warmup();
        warmup.updateTime(startedTime);
        return warmup;
    }

    protected abstract WarmupReport warmup();

}
