package develop.x.core.boot.warmup;

import develop.x.core.boot.warmup.model.WarmupReport;

import java.util.concurrent.CompletableFuture;

public sealed interface XWarmup permits AbstractXWarmup {

    String getWarmupName();

    CompletableFuture<WarmupReport> execute();
}
