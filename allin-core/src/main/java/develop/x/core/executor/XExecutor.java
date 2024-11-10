package develop.x.core.executor;

public sealed interface XExecutor permits AbstractXExecutor{

    void execute(Runnable command);

    void shutdown();

}
