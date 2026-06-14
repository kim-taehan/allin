package develop.x.core.sender.hazelcast;

import com.hazelcast.collection.IQueue;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import develop.x.core.executor.AbstractXExecutor;
import develop.x.core.executor.BusinessXExecutor;
import develop.x.io.XRequest;
import develop.x.io.network.XTarget;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 리팩토링(옵션 A) 후 HazelcastXSender 검증.
 * - 생성자는 HazelcastInstance 를 보관만 한다(부작용 없음) -> mock 주입 가능.
 * - start() 가 호출돼야 instance.getMap/getQueue 로 핸들을 확보하고 워커가 기동된다.
 * - send() 는 start() 로 등록된 타겟에 대해서만 true(put), 그 외/미초기화 시 false.
 *
 * 워커 스레드는 start() 내부에서 BusinessXExecutor(주입 불가)로 기동되어 Disruptor take() 에
 * 블로킹한다. 결정적 검증을 위해 mock IMap/IQueue 의 put 에 CountDownLatch 를 걸어
 * send() -> 워커 소비 -> sendHazelcast(iMap.put -> iQueue.put) 흐름이 실제로 일어남을
 * latch.await(timeout) 으로 확인한다.
 */
class HazelcastXSenderTest {

    private HazelcastXSender sender;

    @AfterEach
    void tearDown() {
        // start() 가 만든 워커 풀(BusinessXExecutor, 고정 100스레드)의 누수 방지.
        // shutdown() 이 워커 take() 를 인터럽트해 종료시킨다.
        if (sender != null) {
            sender.shutdown();
        }
    }

    private HzSenders sendersOf(HzSender... senders) {
        return new HzSenders(List.of(senders));
    }

    @SuppressWarnings("unchecked")
    private static List<BusinessXExecutor> workerExecutorsOf(HazelcastXSender sender) throws Exception {
        Field f = HazelcastXSender.class.getDeclaredField("workerExecutors");
        f.setAccessible(true);
        return (List<BusinessXExecutor>) f.get(sender);
    }

    private static ExecutorService internalExecutorService(AbstractXExecutor executor) throws Exception {
        Field f = AbstractXExecutor.class.getDeclaredField("executorService");
        f.setAccessible(true);
        return (ExecutorService) f.get(executor);
    }

    @Test
    @DisplayName("생성자: HazelcastInstance 를 주입받아 생성될 뿐, 생성 시점에는 getMap/getQueue 등 어떤 상호작용도 하지 않는다.")
    void constructorHasNoSideEffect() {
        HazelcastInstance instance = mock(HazelcastInstance.class);
        HzSenders hzSenders = sendersOf(new HzSender("ORDER", 1));

        new HazelcastXSender(hzSenders, instance);

        // 부작용 분리(옵션 A) 검증: 생성만으로는 인스턴스를 건드리지 않는다.
        verifyNoInteractions(instance);
    }

    @Test
    @DisplayName("send: start() 를 호출하지 않은 상태에서는 blockingQueueMap 이 비어 있어 조용히 false 를 반환한다(리뷰 m1 문서화).")
    void sendReturnsFalseBeforeStart() {
        HazelcastInstance instance = mock(HazelcastInstance.class);
        sender = new HazelcastXSender(sendersOf(new HzSender("ORDER", 1)), instance);

        XRequest request = new XRequest.Builder()
                .header("transactionId", "tx-1")
                .body(new byte[]{1})
                .build();

        // start() 미호출 -> 미초기화 상태가 "타겟 없음"과 구분되지 않고 false 로 보고된다.
        assertThat(sender.send(XTarget.ORDER, request)).isFalse();
        verifyNoInteractions(instance);
    }

    @Test
    @DisplayName("start: senders() 의 각 타겟에 대해 instance.getMap/getQueue 로 핸들을 확보한다(타겟 맵/큐 이름 해석 포함).")
    void startResolvesMapAndQueuePerTarget() {
        HazelcastInstance instance = mock(HazelcastInstance.class);
        @SuppressWarnings("unchecked")
        IMap<String, byte[]> iMap = mock(IMap.class);
        @SuppressWarnings("unchecked")
        IQueue<String> iQueue = mock(IQueue.class);
        when(instance.<String, byte[]>getMap(anyString())).thenReturn(iMap);
        when(instance.<String>getQueue(anyString())).thenReturn(iQueue);

        sender = new HazelcastXSender(sendersOf(new HzSender("ORDER", 1)), instance);

        sender.start();

        // XTarget.ORDER -> mapName=MAP_ORDER, queueName=MQ_ORDER
        verify(instance).getMap("MAP_ORDER");
        verify(instance).getQueue("MQ_ORDER");
    }

    @Test
    @DisplayName("send: start() 로 등록된 타겟에 보내면 true 를 반환하고, 워커가 sendHazelcast 를 통해 iMap.put -> iQueue.put 을 수행한다.")
    void sendAfterStartRoutesThroughWorkerToMapAndQueue() throws Exception {
        HazelcastInstance instance = mock(HazelcastInstance.class);
        @SuppressWarnings("unchecked")
        IMap<String, byte[]> iMap = mock(IMap.class);
        @SuppressWarnings("unchecked")
        IQueue<String> iQueue = mock(IQueue.class);
        when(instance.<String, byte[]>getMap(anyString())).thenReturn(iMap);
        when(instance.<String>getQueue(anyString())).thenReturn(iQueue);

        CountDownLatch latch = new CountDownLatch(2);
        List<String> calls = new CopyOnWriteArrayList<>();
        doAnswer((InvocationOnMock i) -> {
            calls.add("map.put:" + i.getArgument(0));
            latch.countDown();
            return null;
        }).when(iMap).put(anyString(), any());
        doAnswer((InvocationOnMock i) -> {
            calls.add("queue.put:" + i.getArgument(0));
            latch.countDown();
            return null;
        }).when(iQueue).put(anyString());

        sender = new HazelcastXSender(sendersOf(new HzSender("ORDER", 1)), instance);
        sender.start();

        XRequest request = new XRequest.Builder()
                .header("transactionId", "tx-42")
                .body(new byte[]{9})
                .build();

        boolean accepted = sender.send(XTarget.ORDER, request);

        assertThat(accepted).isTrue();
        // 워커 스레드가 비동기로 소비 -> 결정적 종료를 위해 latch 로 대기.
        assertThat(latch.await(5, TimeUnit.SECONDS))
                .as("worker should consume the request and call iMap.put then iQueue.put")
                .isTrue();

        verify(iMap).put("tx-42", request.toByte());
        verify(iQueue).put("tx-42");
        // map.put 이 queue.put 보다 먼저 일어난다(sendHazelcast 순서).
        assertThat(calls).containsExactly("map.put:tx-42", "queue.put:tx-42");
    }

    @Test
    @DisplayName("shutdown: start() 가 타겟마다 생성한 워커 executor 가 shutdown() 호출 시 모두 종료된다(누수 해소).")
    void shutdownTerminatesWorkerExecutorsCreatedByStart() throws Exception {
        HazelcastInstance instance = mock(HazelcastInstance.class);
        @SuppressWarnings("unchecked")
        IMap<String, byte[]> iMap = mock(IMap.class);
        @SuppressWarnings("unchecked")
        IQueue<String> iQueue = mock(IQueue.class);
        when(instance.<String, byte[]>getMap(anyString())).thenReturn(iMap);
        when(instance.<String>getQueue(anyString())).thenReturn(iQueue);

        // 타겟 2개 -> start() 가 BusinessXExecutor 2개를 생성해 워커가 Disruptor take() 에 블로킹한다.
        sender = new HazelcastXSender(
                sendersOf(new HzSender("ORDER", 1), new HzSender("RISK", 1)), instance);
        sender.start();

        List<BusinessXExecutor> workers = workerExecutorsOf(sender);
        assertThat(workers)
                .as("start() 가 타겟마다 워커 executor 를 생성해 보관해야 한다")
                .hasSize(2);

        // when : shutdown() -> 보관된 각 executor 의 graceful shutdown 으로 워커 take() 가 인터럽트된다.
        sender.shutdown();

        // then : 모든 워커 풀이 종료되어 누수가 없어야 한다.
        for (BusinessXExecutor worker : workers) {
            ExecutorService internal = internalExecutorService(worker);
            assertThat(internal.isShutdown()).as("워커 executor 는 shutdown 요청 상태여야 한다").isTrue();
            assertThat(internal.awaitTermination(10, TimeUnit.SECONDS))
                    .as("워커 take() 가 인터럽트되어 풀이 완전 종료되어야 한다")
                    .isTrue();
            assertThat(internal.isTerminated()).isTrue();
        }
    }

    @Test
    @DisplayName("send: start() 이후라도 등록되지 않은 타겟(RISK)으로 보내면 false 를 반환한다.")
    void sendReturnsFalseForUnregisteredTarget() {
        HazelcastInstance instance = mock(HazelcastInstance.class);
        @SuppressWarnings("unchecked")
        IMap<String, byte[]> iMap = mock(IMap.class);
        @SuppressWarnings("unchecked")
        IQueue<String> iQueue = mock(IQueue.class);
        when(instance.<String, byte[]>getMap(anyString())).thenReturn(iMap);
        when(instance.<String>getQueue(anyString())).thenReturn(iQueue);

        // ORDER 만 등록
        sender = new HazelcastXSender(sendersOf(new HzSender("ORDER", 1)), instance);
        sender.start();

        XRequest request = new XRequest.Builder()
                .header("transactionId", "tx-2")
                .body(new byte[]{1})
                .build();

        // RISK 는 blockingQueueMap 에 없음 -> false
        assertThat(sender.send(XTarget.RISK, request)).isFalse();
    }
}
