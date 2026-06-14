package develop.x.core.receiver.hazelcast;

import com.hazelcast.collection.IQueue;
import develop.x.core.blockingqueue.AbstractXBlockingQueue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * HazelcastXReceiver.ListenerRunnable#run 단독 검증.
 * iQueue 는 Mockito mock(IQueue 는 인터페이스), blockingQueue 는 sealed 인터페이스라
 * 허용된 AbstractXBlockingQueue 서브클래스(수집용 fake)를 사용한다.
 * take() 가 N회 아이템 반환 후 InterruptedException 을 던지게 stub 하여 루프 종료를 결정적으로 유도한다.
 */
class HazelcastXReceiverListenerRunnableTest {

    /** put 된 아이템을 그대로 수집하는 fake blocking queue. */
    static class CollectingBlockingQueue extends AbstractXBlockingQueue<String> {
        final List<String> collected = new CopyOnWriteArrayList<>();

        @Override
        public void put(String item) {
            collected.add(item);
        }
    }

    /**
     * 실 HzMiddleBq(AbstractXBlockingQueue.put)의 인터럽트 삼킴 경계를 모사하는 fake.
     * put 중 인터럽트가 발생하면 AbstractXBlockingQueue.put 은 InterruptedException 을 catch 하여
     * 로그만 남기고 아이템을 큐에 넣지 못한 채 반환한다(= 해당 아이템 유실, 인터럽트 플래그 재설정 안 함).
     * 이 fake 는 그 동작(아이템 유실 + 삼킴)을 결정적으로 재현한다.
     */
    static class InterruptSwallowingBlockingQueue extends AbstractXBlockingQueue<String> {
        final List<String> collected = new CopyOnWriteArrayList<>();
        final List<String> swallowed = new CopyOnWriteArrayList<>();
        private final String interruptOn;

        InterruptSwallowingBlockingQueue(String interruptOn) {
            this.interruptOn = interruptOn;
        }

        @Override
        public void put(String item) {
            if (item.equals(interruptOn)) {
                // 실 put 이 InterruptedException 을 삼키고 반환하는 경로:
                // 아이템을 큐에 넣지 못하고(유실) 인터럽트 플래그도 재설정하지 않는다.
                swallowed.add(item);
                return;
            }
            collected.add(item);
        }
    }

    @Test
    @DisplayName("run: iQueue.take() 로 받은 아이템들을 blockingQueue.put 으로 전달하고, InterruptedException 발생 시 루프를 종료한다.")
    void drainsTakenItemsThenStopsOnInterrupt() throws Exception {
        // given
        @SuppressWarnings("unchecked")
        IQueue<String> iQueue = mock(IQueue.class);
        when(iQueue.getName()).thenReturn("ORDER_QUEUE");
        // 3개 반환 후 InterruptedException 으로 루프 종료
        when(iQueue.take())
                .thenReturn("a")
                .thenReturn("b")
                .thenReturn("c")
                .thenThrow(new InterruptedException("stop"));

        CollectingBlockingQueue collector = new CollectingBlockingQueue();

        HazelcastXReceiver.ListenerRunnable runnable =
                new HazelcastXReceiver.ListenerRunnable(0, iQueue, collector);

        // when : InterruptedException 으로 인해 run() 이 스스로 종료(반환)되어야 한다.
        runnable.run();

        // then
        assertThat(collector.collected).containsExactly("a", "b", "c");
        // take() 는 정확히 4회 호출(아이템 3 + 예외 1)
        verify(iQueue, org.mockito.Mockito.times(4)).take();
    }

    @Test
    @DisplayName("run: 첫 take() 에서 곧바로 InterruptedException 이면 아무것도 put 하지 않고 즉시 종료한다.")
    void stopsImmediatelyWhenFirstTakeInterrupts() throws Exception {
        // given
        @SuppressWarnings("unchecked")
        IQueue<String> iQueue = mock(IQueue.class);
        when(iQueue.getName()).thenReturn("ORDER_QUEUE");
        when(iQueue.take()).thenThrow(new InterruptedException("stop"));

        CollectingBlockingQueue collector = new CollectingBlockingQueue();

        HazelcastXReceiver.ListenerRunnable runnable =
                new HazelcastXReceiver.ListenerRunnable(1, iQueue, collector);

        // when
        runnable.run();

        // then
        assertThat(collector.collected).isEmpty();
        verify(iQueue, org.mockito.Mockito.times(1)).take();
    }

    @Test
    @DisplayName("run: take() 는 성공했으나 blockingQueue.put 단계에서 인터럽트가 삼켜지면 해당 아이템이 유실된다(경계 케이스).")
    void losesItemWhenPutSwallowsInterrupt() throws Exception {
        // given
        @SuppressWarnings("unchecked")
        IQueue<String> iQueue = mock(IQueue.class);
        when(iQueue.getName()).thenReturn("ORDER_QUEUE");
        // "a" 는 정상 전달, "b" 는 take() 성공 후 put 에서 삼켜져 유실, 그 다음 take() 에서 루프 종료.
        when(iQueue.take())
                .thenReturn("a")
                .thenReturn("b")
                .thenThrow(new InterruptedException("stop"));

        // put("b") 에서 인터럽트를 삼키는(유실) blocking queue.
        InterruptSwallowingBlockingQueue blockingQueue = new InterruptSwallowingBlockingQueue("b");

        HazelcastXReceiver.ListenerRunnable runnable =
                new HazelcastXReceiver.ListenerRunnable(0, iQueue, blockingQueue);

        // when
        runnable.run();

        // then
        // "a" 만 정상 전달되고 "b" 는 put 인터럽트 삼킴으로 유실된다 -> in-flight 메시지 유실 경계가 드러난다.
        assertThat(blockingQueue.collected).containsExactly("a");
        assertThat(blockingQueue.swallowed)
                .as("put 단계 인터럽트로 'b' 가 큐에 들어가지 못하고 유실되어야 한다")
                .containsExactly("b");
        // take(): a, b, (InterruptedException) = 3회.
        verify(iQueue, org.mockito.Mockito.times(3)).take();
    }

    @Test
    @DisplayName("run: 스레드가 인터럽트 상태로 시작되면 take() 를 호출하지 않고 즉시 종료한다.")
    void stopsWhenThreadAlreadyInterrupted() throws Exception {
        // given
        @SuppressWarnings("unchecked")
        IQueue<String> iQueue = mock(IQueue.class);
        CollectingBlockingQueue collector = new CollectingBlockingQueue();

        HazelcastXReceiver.ListenerRunnable runnable =
                new HazelcastXReceiver.ListenerRunnable(2, iQueue, collector);

        // when : run() 을 인터럽트된 스레드에서 실행 -> while 조건이 false -> take 미호출
        List<String> result = new ArrayList<>();
        Thread t = new Thread(() -> {
            Thread.currentThread().interrupt();
            runnable.run();
            result.add("done");
        });
        t.start();
        t.join(2000);

        // then
        assertThat(result).containsExactly("done");
        assertThat(collector.collected).isEmpty();
        verify(iQueue, org.mockito.Mockito.never()).take();
    }
}
