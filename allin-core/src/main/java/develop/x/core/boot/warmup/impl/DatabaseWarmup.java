package develop.x.core.boot.warmup.impl;

import develop.x.core.boot.warmup.AbstractXWarmup;
import develop.x.core.boot.warmup.model.WarmupReport;
import develop.x.core.boot.warmup.model.WarmupResult;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

@Slf4j
public class DatabaseWarmup extends AbstractXWarmup {

    @Override
    public CompletableFuture<WarmupReport> execute() {
        return null;
    }

    @Override
    protected WarmupReport warmup() {
        log.info("DatabaseWarmup");

        try {
            Thread.sleep(1000); // 작업 시뮬레이션 (1초 지연)
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new WarmupReport(getWarmupName(), WarmupResult.SUCCESS);
    }
}
