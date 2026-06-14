package develop.x.core.receiver.hazelcast;

import com.hazelcast.collection.IQueue;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import develop.x.core.dispatcher.AbstractXDispatcher;
import develop.x.core.dispatcher.handler.XHandler;
import develop.x.core.executor.AbstractXExecutor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 리팩토링(옵션 A) 후 HazelcastXReceiver 검증.
 * - 생성자는 HazelcastInstance 를 보관만 한다(생성자에서 bind() 호출 제거) -> mock 주입 가능.
 * - bind() 가 호출돼야 createReceiver 가 instance.getQueue/getMap 으로 핸들을 확보하고
 *   worker(HzMiddleBq.run -> executor.execute) + listener(executor.execute) 를 등록한다.
 *
 * XExecutor/XDispatcher 는 sealed 인터페이스라 Mockito 로 mock 할 수 없다.
 * 따라서 허용된 서브클래스(AbstractXExecutor/AbstractXDispatcher) 로 테스트 더블을 만든다:
 * - CapturingExecutor: execute() 를 오버라이드해 Runnable 을 실행하지 않고 수집만 한다
 *   -> 실제 스레드가 뜨지 않아 결정적으로 등록 횟수만 검증한다.
 * - NoopDispatcher: bind() 경로에서 worker 람다가 실행되지 않으므로 invoke 가 불릴 일이 없다.
 * (ListenerRunnable#run 의 take/put 동작은 별도 HazelcastXReceiverListenerRunnableTest 에서 검증한다.)
 */
class HazelcastXReceiverTest {

    /** Runnable 을 실행하지 않고 수집만 하는 XExecutor 테스트 더블. */
    static class CapturingExecutor extends AbstractXExecutor {
        final List<Runnable> submitted = new CopyOnWriteArrayList<>();

        CapturingExecutor() {
            // 부모 생성자 요구 충족용. 아래 execute 오버라이드로 인해 실제 사용되지 않는다.
            super(Executors.newSingleThreadExecutor());
            super.executorService.shutdownNow();
        }

        @Override
        public void execute(Runnable command) {
            submitted.add(command);
        }

        @Override
        public void shutdown() {
        }
    }

    /** invoke 가 호출될 일이 없는 XDispatcher 테스트 더블. */
    static class NoopDispatcher extends AbstractXDispatcher {
        NoopDispatcher() {
            super(null, null);
        }

        @Override
        protected void doRun(XHandler handler, Object[] arguments) {
        }
    }

    private HzReceivers receiversOf(HzReceiver... receivers) {
        return new HzReceivers(List.of(receivers));
    }

    @Test
    @DisplayName("생성자: HazelcastInstance/executor 를 주입받아 생성될 뿐, 생성 시점에는 bind() 가 호출되지 않아 어떤 상호작용도 없다.")
    void constructorDoesNotBind() {
        HazelcastInstance instance = mock(HazelcastInstance.class);
        CapturingExecutor executor = new CapturingExecutor();

        new HazelcastXReceiver(receiversOf(new HzReceiver("ORDER", 1, 1)),
                executor, new NoopDispatcher(), instance);

        // 생성자에서 bind() 호출 제거(옵션 A) -> instance 미사용, executor 에 등록된 작업 없음.
        verifyNoInteractions(instance);
        assertThat(executor.submitted).isEmpty();
    }

    @Test
    @DisplayName("bind: 각 receiver 타겟에 대해 instance.getQueue/getMap 으로 핸들을 확보한다(타겟 이름 해석 포함).")
    void bindResolvesQueueAndMapPerReceiver() {
        HazelcastInstance instance = mock(HazelcastInstance.class);
        CapturingExecutor executor = new CapturingExecutor();
        stubHandles(instance);

        HazelcastXReceiver receiver = new HazelcastXReceiver(
                receiversOf(new HzReceiver("ORDER", 1, 1)),
                executor, new NoopDispatcher(), instance);

        receiver.bind();

        // XTarget.ORDER -> queueName=MQ_ORDER, mapName=MAP_ORDER
        verify(instance).getQueue("MQ_ORDER");
        verify(instance).getMap("MAP_ORDER");
    }

    @Test
    @DisplayName("bind: worker(workerCount) + listener(listenerCount) 만큼 주입된 executor.execute 로 작업을 등록한다.")
    void bindRegistersWorkersAndListeners() {
        HazelcastInstance instance = mock(HazelcastInstance.class);
        CapturingExecutor executor = new CapturingExecutor();
        stubHandles(instance);

        // listenerCount=3, workerCount=2 -> execute 총 5회(worker 2 + listener 3)
        HazelcastXReceiver receiver = new HazelcastXReceiver(
                receiversOf(new HzReceiver("ORDER", 3, 2)),
                executor, new NoopDispatcher(), instance);

        receiver.bind();

        assertThat(executor.submitted).hasSize(5);
    }

    @Test
    @DisplayName("bind: 여러 receiver 가 있으면 각 타겟별로 핸들 확보와 등록을 반복한다.")
    void bindHandlesMultipleReceivers() {
        HazelcastInstance instance = mock(HazelcastInstance.class);
        CapturingExecutor executor = new CapturingExecutor();
        stubHandles(instance);

        HazelcastXReceiver receiver = new HazelcastXReceiver(
                receiversOf(new HzReceiver("ORDER", 1, 1), new HzReceiver("RISK", 1, 1)),
                executor, new NoopDispatcher(), instance);

        receiver.bind();

        verify(instance).getQueue("MQ_ORDER");
        verify(instance).getMap("MAP_ORDER");
        verify(instance).getQueue("MQ_RISK");
        verify(instance).getMap("MAP_RISK");
        // 타겟 2개 * (worker 1 + listener 1) = 4회
        assertThat(executor.submitted).hasSize(4);
    }

    @Test
    @DisplayName("close: 항상 true 를 반환한다.")
    void closeReturnsTrue() {
        HazelcastXReceiver receiver = new HazelcastXReceiver(
                receiversOf(new HzReceiver("ORDER", 1, 1)),
                new CapturingExecutor(), new NoopDispatcher(), mock(HazelcastInstance.class));

        assertThat(receiver.close()).isTrue();
    }

    @SuppressWarnings("unchecked")
    private void stubHandles(HazelcastInstance instance) {
        IQueue<String> queue = mock(IQueue.class);
        IMap<String, Object> map = mock(IMap.class);
        when(instance.<String>getQueue(anyString())).thenReturn(queue);
        when(instance.<String, Object>getMap(anyString())).thenReturn(map);
    }
}
