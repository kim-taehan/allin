package develop.x.core.sender.hazelcast;

import com.hazelcast.collection.IQueue;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import develop.x.core.HazelcastFactory;
import develop.x.core.blockingqueue.AbstractXBlockingQueue;
import develop.x.core.blockingqueue.XBlockingQueue;
import develop.x.io.XRequest;
import develop.x.core.executor.BusinessXExecutor;
import develop.x.core.sender.XSender;
import develop.x.io.network.XTarget;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class HazelcastXSender implements XSender {

    private final Map<XTarget, XBlockingQueue<XRequest>> blockingQueueMap = new HashMap<>();

    public HazelcastXSender(HzSenders hzSenders) {

        HazelcastInstance instance = HazelcastFactory.getInstance();
        for (HzSender sender : hzSenders.senders()) {
            log.info("sender = {}", sender);
            XTarget xTarget = XTarget.valueOf(sender.name().toUpperCase());
            IMap<String, byte[]> iMap = instance.getMap(xTarget.getMapName());
            IQueue<String> iQueue = instance.getQueue(xTarget.getQueueName());
            HzSenderBq hzSenderBq = new HzSenderBq(1000, sender.senderCount());
            hzSenderBq.run(new BusinessXExecutor(), xRequest -> {
                sendHazelcast(xRequest, iMap, iQueue);
            });
            blockingQueueMap.put(xTarget, hzSenderBq);
        }
    }

    private void sendHazelcast(XRequest xRequest, IMap<String, byte[]> iMap, IQueue<String> iQueue) {
        String transactionId = xRequest.getHeaders().get("transactionId");
        iMap.put(transactionId, xRequest.toByte());
        try {
            iQueue.put(transactionId);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean send(XTarget target, XRequest request) {
        if(blockingQueueMap.containsKey(target)){
            blockingQueueMap.get(target).put(request);
            return true;
        }
        return false;
    }

    static class HzSenderBq extends AbstractXBlockingQueue<XRequest> {
        public HzSenderBq(int queueSize, int threadCount) {
            super(queueSize, threadCount);
        }
    }
}
