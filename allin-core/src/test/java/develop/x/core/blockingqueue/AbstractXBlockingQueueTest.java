package develop.x.core.blockingqueue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

class AbstractXBlockingQueueTest {

    @Test
    @DisplayName("BlockingQueue 를 생성하여 put, take 기능을 통해 다른 스레드가 message 를 전달 받을 수 있다.")
    void transferInterThreadMessage() throws InterruptedException {

        // given
        TestBlockingQueue testBlockingQueue = new TestBlockingQueue();
        ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

        // when 1 (다른 스레드에서 TestBlockingQueue에 put을 통해 message 를 전달한다.)
        for (int i = 0; i < 10; i++) {
            final String item = "item_" + i;
            cachedThreadPool.execute(()-> {
                testBlockingQueue.put(item);
            });
        }

        // when 2 (메인 스레드에서 TestBlockingQueue 데이터를 읽어온다.)
        Set items = new HashSet();
        for (int i = 0; i < 10; i++) {
            String takenItem = testBlockingQueue.take();
            items.add(takenItem);
        }

        // then
        assertThat(items.size()).isEqualTo(10);
    }

    static class TestBlockingQueue extends AbstractXBlockingQueue<String> {

    }
}