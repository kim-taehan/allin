package develop.x.core.receiver.hazelcast;

import com.hazelcast.collection.IQueue;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import develop.x.core.HazelcastFactory;
import develop.x.core.blockingqueue.AbstractXBlockingQueue;
import develop.x.core.blockingqueue.XBlockingQueue;
import develop.x.core.dispatcher.XDispatcher;
import develop.x.core.executor.XExecutor;
import develop.x.core.receiver.XReceiver;
import develop.x.io.network.XTarget;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;


@Slf4j
public class HazelcastXReceiver implements XReceiver {

    private final HzReceivers hzReceivers;
    private final XExecutor executor;
    private final XDispatcher xDispatcher;

    public HazelcastXReceiver(HzReceivers hzReceivers, XExecutor executor, XDispatcher xDispatcher) {
        this.hzReceivers = hzReceivers;
        this.executor = executor;
        this.xDispatcher = xDispatcher;
        bind();
    }

    @Override
    public void bind() {
        for (HzReceiver receiver : hzReceivers.receivers()) {
            createReceiver(receiver);
        }
    }

    private void createReceiver(HzReceiver receiver) {
        log.info("hazelcast receiver = {}", receiver);
        XTarget xTarget = findXTarget(receiver);

        HazelcastInstance instance = HazelcastFactory.getInstance();
        IQueue<String> queue = instance.getQueue(xTarget.getQueueName());
        IMap<String , Object> map = instance.getMap(xTarget.getMapName());

        HzMiddleBq hzMiddleBq = registerWorker(receiver, map);

        registerListener(receiver, queue, hzMiddleBq);
    }

    private static XTarget findXTarget(HzReceiver receiver) {
        String name = receiver.name();
        return XTarget.valueOf(name.toUpperCase());
    }

    private HzMiddleBq registerWorker(HzReceiver receiver, IMap<String, Object> map) {
        HzMiddleBq hzMiddleBq = new HzMiddleBq(1000, receiver.workerCount());
        hzMiddleBq.run(executor, item -> {
            Object object = map.get(item);
            xDispatcher.invoke(object);
        });
        return hzMiddleBq;
    }

    static class HzMiddleBq extends AbstractXBlockingQueue<String> {
        public HzMiddleBq(int queueSize, int threadCount) {
            super(queueSize, threadCount);
        }
        @Override
        public void run(XExecutor executor, Consumer<String> consumer) {
            super.run(executor, consumer);
        }
    }

    private void registerListener(HzReceiver receiver, IQueue<String> queue, HzMiddleBq hzMiddleBq) {
        for (int i = 0; i < receiver.listenerCount(); i++) {
            ListenerRunnable listenerRunnable = new ListenerRunnable(i, queue, hzMiddleBq);
            executor.execute(listenerRunnable);
        }
    }

    @Slf4j
    @RequiredArgsConstructor
    static class ListenerRunnable implements Runnable {
        private final int index;
        private final IQueue<String> iQueue;
        private final XBlockingQueue<String> blockingQueue;

        @Override
        public void run() {
            Thread currentThread = Thread.currentThread();
            while (!currentThread.isInterrupted()) {
                try {
                    String takenItem = iQueue.take();
                    blockingQueue.put(takenItem);
                } catch (InterruptedException e) {
                    // listener thread 는 인터럽트 발생시 별다른 조치가 필요없다.
                    log.info("{}_{} interrupted ", iQueue.getName(), index);
                    break;
                }
            }
        }
    }


    @Override
    public boolean close() {
        return true;
    }
}
