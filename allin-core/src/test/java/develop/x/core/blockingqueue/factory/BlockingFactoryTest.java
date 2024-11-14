package develop.x.core.blockingqueue.factory;

import com.conversantmedia.util.concurrent.DisruptorBlockingQueue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class BlockingFactoryTest {

    @Test
    @DisplayName("ArrayBlockingQueue 방식으로 bq를 생성해서 테스트")
    void createArrayBlockingQueue() throws NoSuchAlgorithmException, InterruptedException {

        // given
        int queueSize = 100;

        // when
        BlockingQueue<String> blockingQueue = BlockingFactory.ARRAY_BLOCKING_QUEUE.create(queueSize);
        blockingQueue.put("item01");
        blockingQueue.put("item02");

        // then
        assertAll(
                () -> assertThat(blockingQueue.size()).isEqualTo(2),
                () -> assertThat(blockingQueue.poll(100, TimeUnit.MICROSECONDS)).isEqualTo("item01"),
                () -> assertThat(blockingQueue).isInstanceOf(ArrayBlockingQueue.class)
        );
    }


    @Test
    @DisplayName("LinkedBlockingQueue 방식으로 bq를 생성해서 테스트")
    void createLinkedBlockingQueue() throws NoSuchAlgorithmException, InterruptedException {

        // given
        int queueSize = 100;

        // when
        BlockingQueue<String> blockingQueue = BlockingFactory.LINKED_BLOCKING_QUEUE.create(queueSize);
        blockingQueue.put("item01");
        blockingQueue.put("item02");

        // then
        assertAll(
                () -> assertThat(blockingQueue.size()).isEqualTo(2),
                () -> assertThat(blockingQueue.poll(100, TimeUnit.MICROSECONDS)).isEqualTo("item01"),
                () -> assertThat(blockingQueue).isInstanceOf(LinkedBlockingQueue.class)
        );
    }
    @Test
    @DisplayName("DisruptorBlockingQueue 방식으로 bq를 생성해서 테스트")
    void createDisruptorBlockingQueue() throws NoSuchAlgorithmException, InterruptedException {

        // given
        int queueSize = 100;

        // when
        BlockingQueue<String> blockingQueue = BlockingFactory.DISRUPTOR_BLOCKING_QUEUE.create(queueSize);
        blockingQueue.put("item01");
        blockingQueue.put("item02");

        // then
        assertAll(
                () -> assertThat(blockingQueue.size()).isEqualTo(2),
                () -> assertThat(blockingQueue.poll(100, TimeUnit.MICROSECONDS)).isEqualTo("item01"),
                () -> assertThat(blockingQueue).isInstanceOf(DisruptorBlockingQueue.class)
        );
    }


}