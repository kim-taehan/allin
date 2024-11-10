package develop.x.core.blockingqueue;

import develop.x.core.executor.XExecutor;

import java.util.function.Consumer;

public sealed interface XBlockingQueue<T> permits AbstractXBlockingQueue {

    void put(T t);

    T take() throws InterruptedException;

    int size();

    int queueTotalSize();

    void run(XExecutor executor, Consumer<T> consumer);
}
