package develop.x.core.executor;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public non-sealed abstract class AbstractXExecutor implements XExecutor {

    protected final ExecutorService executorService;

    public AbstractXExecutor(ExecutorService executorService) {
        // 불변 객체 재생성함
        this.executorService = Executors.unconfigurableExecutorService(executorService);
    }

    public void execute(Runnable command){
        executorService.execute(command);
    }

}
