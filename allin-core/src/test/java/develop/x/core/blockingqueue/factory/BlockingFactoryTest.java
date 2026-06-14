package develop.x.core.blockingqueue.factory;

import com.conversantmedia.util.concurrent.DisruptorBlockingQueue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class BlockingFactoryTest {

    @Test
    @DisplayName("ArrayBlockingQueue 방식으로 bq를 생성해서 테스트")
    void createArrayBlockingQueue() throws InterruptedException {

        // given
        int queueSize = 100;

        // when
        BlockingQueue<String> blockingQueue = BlockingFactory.ARRAY_BLOCKING_QUEUE.create(queueSize);
        blockingQueue.put("item01");
        blockingQueue.put("item02");

        // then
        assertAll(
                () -> assertThat(blockingQueue.size()).isEqualTo(2),
                () -> assertThat(blockingQueue.take()).isEqualTo("item01"),
                () -> assertThat(blockingQueue).isInstanceOf(ArrayBlockingQueue.class)
        );
    }


    @Test
    @DisplayName("LinkedBlockingQueue 방식으로 bq를 생성해서 테스트")
    void createLinkedBlockingQueue() throws InterruptedException {

        // given
        int queueSize = 100;

        // when
        BlockingQueue<String> blockingQueue = BlockingFactory.LINKED_BLOCKING_QUEUE.create(queueSize);
        blockingQueue.put("item01");
        blockingQueue.put("item02");

        // then
        assertAll(
                () -> assertThat(blockingQueue.size()).isEqualTo(2),
                () -> assertThat(blockingQueue.take()).isEqualTo("item01"),
                () -> assertThat(blockingQueue).isInstanceOf(LinkedBlockingQueue.class)
        );
    }

    @Test
    @DisplayName("DisruptorBlockingQueue 방식으로 bq를 생성해서 테스트")
    void createDisruptorBlockingQueue() throws InterruptedException {

        // given
        int queueSize = 100;

        // when
        BlockingQueue<String> blockingQueue = BlockingFactory.DISRUPTOR_BLOCKING_QUEUE.create(queueSize);
        blockingQueue.put("item01");
        blockingQueue.put("item02");

        // then
        assertAll(
                () -> assertThat(blockingQueue.size()).isEqualTo(2),
                () -> assertThat(blockingQueue.take()).isEqualTo("item01"),
                () -> assertThat(blockingQueue).isInstanceOf(DisruptorBlockingQueue.class)
        );
    }

    @Test
    @DisplayName("ArrayBlockingQueue 는 지정한 queueSize 를 정확히 용량으로 반영한다.")
    void arrayBlockingQueueRespectsCapacity() {

        // given
        int queueSize = 3;

        // when
        BlockingQueue<String> blockingQueue = BlockingFactory.ARRAY_BLOCKING_QUEUE.create(queueSize);

        // then - 비어있을 때 잔여 용량은 정확히 queueSize 이다.
        assertThat(blockingQueue.remainingCapacity()).isEqualTo(queueSize);

        // offer 로 가득 채우면 그 이후 offer 는 즉시 false(블로킹 없이 거부) 여야 한다.
        assertAll(
                () -> assertThat(blockingQueue.offer("a")).isTrue(),
                () -> assertThat(blockingQueue.offer("b")).isTrue(),
                () -> assertThat(blockingQueue.offer("c")).isTrue(),
                () -> assertThat(blockingQueue.offer("overflow")).isFalse(),
                () -> assertThat(blockingQueue.remainingCapacity()).isEqualTo(0)
        );
    }

    @Test
    @DisplayName("용량이 가득 찬 BlockingQueue 에 put 하면 자리가 날 때까지 블로킹된다.")
    void putBlocksWhenFull() throws InterruptedException {

        // given - 용량 1 인 큐를 가득 채운다.
        BlockingQueue<String> blockingQueue = BlockingFactory.ARRAY_BLOCKING_QUEUE.create(1);
        blockingQueue.put("first");

        AtomicBoolean putReturned = new AtomicBoolean(false);
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch finished = new CountDownLatch(1);

        Thread producer = new Thread(() -> {
            started.countDown();
            try {
                blockingQueue.put("second"); // 가득 차 있으므로 블로킹
                putReturned.set(true);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                finished.countDown();
            }
        });
        producer.start();

        // when - producer 스레드는 put 에서 블로킹되어 아직 반환하지 못해야 한다.
        assertThat(started.await(1, TimeUnit.SECONDS)).isTrue();
        // 짧은 대기 후에도 put 이 반환되지 않았음을 확인(블로킹 증명).
        boolean finishedTooEarly = finished.await(200, TimeUnit.MILLISECONDS);
        assertThat(finishedTooEarly).as("가득 찬 큐의 put 은 블로킹되어야 한다").isFalse();
        assertThat(putReturned.get()).isFalse();

        // then - 한 건을 꺼내 자리를 만들면 put 이 풀려 정상 반환된다.
        assertThat(blockingQueue.take()).isEqualTo("first");
        assertThat(finished.await(1, TimeUnit.SECONDS)).isTrue();
        assertThat(putReturned.get()).isTrue();
        assertThat(blockingQueue.take()).isEqualTo("second");

        producer.join();
    }


}
