### 2.7 sender
- XRequest 기반으로 패킹하여 원하는 XTarget 으로 메시지를 보낼 수 있는 기능을 제공한다.
- business model 에서는 원하는 XTarget 전용 BlockingQueue 에 XRequest 객체만 입력하게 되면, 이후에는 별도의 Sender Group thread 가 이를 받아서 외부채널에 요청을 보내는 방식이다.

![img.png](..%2Fimages%2F2700_sender.png)

#### 2.7.1 XSender
- XSender 는 타시스템에게 메시지 전달을 위한 추상 인터페이스로 business model 에서는 이 인터페이스에게만 의존성을 가진다. 
- XTarget 은 보낼 시스템을 의미하고, XRequest 는 보내는 데이터 구조를 의미한다. 

```java
public interface XSender {
    boolean send(XTarget target, XRequest request);
}
```

#### 2.7.2 XTarget
- XTarget 은 특정 외부 시스템을 의미하는데, 이는 Receiver 도 연관지어 생각할 수 있다. 
- 또한 Sender 에서는 미리 현재 시스템에서 보낼 Target 타 시스템에 대해서도 서버 기동시점에 미리 정의해야 한다. (application.yml)
```java
public enum XTarget {

    ORDER("MAP_ORDER", "MQ_ORDER"),
    RISK
    ;

    private final String mapName;
    private final String queueName;

    XTarget() {
        this.mapName = "MAP_" + this.name();
        this.queueName = "MQ_" + this.name();
    }
}
```

- (1) name: message 를 수신해야 되는 타 시스템 (XTarget 과 연동)
- (2) sender-count: message sender group thread 갯수
```yaml
allin:
  hazelcast:
    senders:
      - name: order # (1) message 를 수신해야 되는 타 시스템 (XTarget 과 연동) 
        sender-count: 2 # (2) message sender group thread 갯수
```


#### 2.7.3 XRequest
- XRequest 는 Receiver 에서도 사용하는 데이터 구조이지만, Sender 에서도 사용할 수 있다. 
- 빌더 패턴을 사용하여 header 와 body 데이터를 입력하여 메시지를 전달하는 형태로 사용하게 된다. 

```java
public class XRequest {

    private final Map<String, String> headers = new HashMap<>();
    private byte[] body;
    
    // (1) XRequest 객체를 byte[] 형태로 변환하는 구조
    public byte[] toByte() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            sb.append(entry.getKey())
                    .append("=")
                    .append(entry.getValue())
                    .append("\n");
        }
        sb.append("\n");
        byte[] headerByte = sb.toString().getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.wrap(new byte[headerByte.length + body.length]);
        buffer.put(headerByte);
        buffer.put(body);
        return buffer.array();
    }
    
    // (2) XRequest 를 빌더패턴을 사용하여 유연하게 생성할 수 있다. 
    public static class Builder {
        byte[] body;
        Map<String, String> headers = new HashMap<>();

        public Builder() {
        }

        public Builder body(Object body) {
            this.body = JsonUtils.toByte(body);
            return this;
        }

        public Builder body(byte[] body) {
            this.body = body;
            return this;
        }

        public Builder header(String key, String value) {
            headers.put(key, value);
            return this;
        }

        public XRequest build() {
            if(!headers.containsKey("contentType")){
                headers.put("contentType", ContentType.JSON.name());
            }
            headers.put("contentLength", body.length + "");
            return new XRequest(headers, body);
        }
    }
}
```

- XRequest 를 빌더패턴을 사용하여 유연하게 생성할 수 있다. 
```java
XRequest xRequest = new XRequest.Builder()
        .body(request)
        .header("url", "/next-api")
        .header("transactionId", UUID.randomUUID().toString())
        .build();
```

#### 2.7.4 Hazelcast Sender 
- 현재는 hazelcast IMap 을 이용한 전달방식만 제공하고 있으며, business model 에서는 blocking queue 에 입력하는 것으로 역할이 끝나고 
- 그 blocking queue 읽어서 타시스템으로 보내는 별도의 thread 가 자동으로 수행되게 된다. 
- 이 부분도 역할을 분리해서 개발하게 되면, 추가적인 sender 채널이 생기더라도 문제없이 동작하는 구조가 될 수 있을 것 같다.

```java

public class HazelcastXSender implements XSender {

    private final Map<XTarget, XBlockingQueue<XRequest>> blockingQueueMap = new HashMap<>();
    
    @Override
    public boolean send(XTarget target, XRequest request) {
        if(blockingQueueMap.containsKey(target)){
            blockingQueueMap.get(target).put(request);
            return true;
        }
        return false;
    }
    
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

    static class HzSenderBq extends AbstractXBlockingQueue<XRequest> {
        public HzSenderBq(int queueSize, int threadCount) {
            super(queueSize, threadCount);
        }
    }
}
```