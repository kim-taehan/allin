package develop.x.core.blockingqueue;

import develop.x.core.blockingqueue.factory.BlockingFactory;
import develop.x.core.executor.XExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;

import static develop.x.core.blockingqueue.factory.BlockingFactory.DISRUPTOR_BLOCKING_QUEUE;

@Slf4j
public abstract non-sealed class AbstractXBlockingQueue<T> implements XBlockingQueue<T> {

    protected static final int DEFAULT_QUEUE_SIZE = 1000;
    protected static final int DEFAULT_THREAD_COUNT = 1;
    protected final BlockingQueue<T> blockingQueue;

    protected final int queueSize;
    protected final int threadCount;

    public AbstractXBlockingQueue(BlockingFactory factory, int queueSize, int threadCount) {
        this.blockingQueue = factory.create(queueSize);
        this.queueSize = queueSize;
        this.threadCount = threadCount;
    }

    public AbstractXBlockingQueue(int queueSize, int threadCount) {
        this(DISRUPTOR_BLOCKING_QUEUE, queueSize, threadCount);
    }

    public AbstractXBlockingQueue() {
        this(DEFAULT_QUEUE_SIZE, DEFAULT_THREAD_COUNT);
    }

    @Override
    public void put(T t) {
        try {
            blockingQueue.put(t);
        } catch (InterruptedException e) {
            log.error("{} queue 에 입력중에 인터럽트가 발생했습니다.", this.getClass().getSimpleName());
        }
    }

    @Override
    public T take() throws InterruptedException {
        return blockingQueue.take();
    }

    @Override
    public int size() {
        return this.blockingQueue.size();
    }

    @Override
    public int queueTotalSize() {
        return queueSize;
    }

    @Override
    public void run(XExecutor executor, Consumer<T> consumer) {
        for (int i = 0; i < threadCount; i++) {
            threadRun(executor, consumer);
        }
    }

    private void threadRun(XExecutor executor, Consumer<T> consumer) {
        executor.execute(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    T take = blockingQueue.take();
                    consumer.accept(take);
                } catch (InterruptedException e) {
                    log.error("blocking queue interrupted = {}:{}", this.getClass().getSimpleName(), blockingQueue.size(),  e);
                    // interrupt 되더라도 Queue 에 남아있는 데이터는 모두 처리한다.
                    while(!blockingQueue.isEmpty()){
                        T poll = blockingQueue.poll();
                        if (poll == null) {
                            break;
                        }
                        consumer.accept(poll);
                    }
                    break;
                }
            }
        });
    }
}
