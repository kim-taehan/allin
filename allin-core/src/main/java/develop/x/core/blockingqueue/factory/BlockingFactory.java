package develop.x.core.blockingqueue.factory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public enum BlockingFactory {

    LINKED_BLOCKING_QUEUE {
        @Override
        public <T> BlockingQueue<T> create(int queueSize) {
            return new LinkedBlockingQueue<>(queueSize);
        }
    },
    ARRAY_BLOCKING_QUEUE {
        @Override
        public <T> BlockingQueue<T> create(int queueSize) {
            return new ArrayBlockingQueue<>(queueSize);
        }
    },
    DISRUPTOR_BLOCKING_QUEUE {
        @Override
        public <T> BlockingQueue<T> create(int queueSize) {
            // 생략함
            return new LinkedBlockingQueue<>(queueSize);
        }
    }
    ;
    public abstract <T> BlockingQueue<T> create(int queueSize);
}
