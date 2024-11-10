### 2.3 blocking queue
- BlockingQueue 는 java 에서 제공하는 thread 사이에 안전한 Queue 알고리즘을 제공하는 인터페이스이다.
- allin 시스템에서는 BlockingQueue 를 많이 사용하기에 이를 wrapping 하여 손쉽게 사용할 수 있는 기능을 제공하고 있다.
- BlockingQueue 생성과 이를 처리하는 thread 등을 공통적으로 생성하고 관리하는 기능을 Spring di container 를 통해 수행하고 있다.
- 또한 (2.2) dispatcher 와 연계하여 business model을 호출할 수 있는 기능도 제공하고 있다.

#### 2.3.0 개요 
- BlockingQueue 를 직접 사용할 수 있지만, 그보다 편하게 관리하기 위해 wrapping 한 XBlockingQueue 인터페이스를 제공한다.
- BlockingFactory 클래스는 자바에서 제공하는 구현체인 LinkedBlockingQueue, ArrayBlockingQueue 와 Lmax Disruptor 오픈 소스를 사용한 DisruptorBlocking 등을 생성할 수 있는 기능을 제공한다.
- XBlockingQueueRunner 클래스는 Blocking Queue 를 처리하는 스레드를 자동으로 생성하는 기능을 제공한다. 

| type       | name                    | extends| desc                                                                                                                    |
|------------|-------------------------| --|-------------------------------------------------------------------------------------------------------------------------|
| interface  | XBlockingQueue           | N/A| BlockingQueue wrapping 하여 사용할 수 있는 인터페이스                                                                                |
| abstract   | AbstractXBlockingQueue   | XBlockingQueue| blocking queue 를 1개씩 처리하는 역할                                                                                            |
| abstract   | AbstractListXBlockingQueue   | AbstractXBlockingQueue| 비지니스 특징으로 blocking queue 를 n건씩 모아서 한번에 처리하는 역할                                                                          |
| enum       | BlockingFactory   | N/A| 자바에서 제공하는 구현체인 LinkedBlockingQueue, ArrayBlockingQueue 와 Lmax Disruptor 오픈 소스를 사용한 DisruptorBlocking 등을 생성할 수 있는 기능을 제공 |
| annotation | XBlockingQueueMapping   | N/A| business model 과 blocking queue를 연동하기 위한 커스텀 어노테이션                                                                      |
| class      | XBlockingQueueRunner   | N/A| Blocking Queue 를 처리하는 스레드를 자동으로 생성하는 기능을 제공                                                                             |
 

#### 2.3.1 XBlockingQueue
- BlockingQueue 를 직접 사용할 수 있지만, 그보다 편하게 관리하기 위해 wrapping 한 XBlockingQueue 인터페이스를 제공한다.
- sealed ~ permits 예약어를 통해서 인터페이스를 구현할 수 있는 클래스를 제한한다. 
- BlockingQueue 에 기본 기능중 꼭 필요한 기능인 put, take, size 메서드를 정의하고 있다.
- run 메소드는 BlockingQueue 에 데이터가 쌓였을 때, 처리하는 스레드를 등록하는 기능이다. 

```java
public sealed interface XBlockingQueue<T> permits AbstractXBlockingQueue {

    void put(T t);

    T take() throws InterruptedException;

    int size();

    int queueTotalSize();

    void run(XExecutor executor, Consumer<T> consumer);
}
```

#### 2.3.2 AbstractXBlockingQueue
- AbstractXBlockingQueue 는 `XBlockingQueue` 인터페이스를 유일하게 구현할 수 있는 추상화 클래스이며, 이를 상속받아 BlockingQueue 를 구현할 수 있다. 
- 하위 클래스에서 queueSize 와 해당 BlockingQueue 를 처리하는 thread 숫자를 전달한다. 
- BlockingQueue 를 상속이 아닌 내부 객체로 가지고 있어, 더 유연하게 처리할 수 있다. 

```java
public abstract non-sealed class AbstractXBlockingQueue<T> implements XBlockingQueue<T> {
    
    // (1) BlockingQueue 를 내부 객체로 가지고 있다.
    protected final BlockingQueue<T> blockingQueue;
    protected final int queueSize;
    protected final int threadCount;
    
    // ... 생략
    @Override
    public void run(XExecutor executor, Consumer<T> consumer) {

        // (2) 미리 정의한 스레드 숫자만큼 실행한다.
        for (int i = 0; i < threadCount; i++) {
            threadRun(executor, consumer);
        }
    }

    private void threadRun(XExecutor executor, Consumer<T> consumer) {
        executor.execute(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // (3) 해당 BlockingQueue 대기하고 있다.
                    T take = blockingQueue.take();
                    // (4) Consumer 를 통해 take 한 item 을 처리할 방법을 client 에서 받아온다
                    consumer.accept(take);
                } catch (InterruptedException e) {
                    log.error("blocking queue interrupted = {}:{}", this.getClass().getSimpleName(), blockingQueue.size(),  e);
                    // (5) interrupt 되더라도 Queue 에 남아있는 데이터는 모두 처리한다.
                    while(!blockingQueue.isEmpty()){
                        T poll = blockingQueue.poll();
                        if (poll == null) {
                            break;
                        }
                        consumer.accept(poll);
                    }
                    break;
                }
            }
        });
    }
}
```
- (1) BlockingQueue 를 내부 객체로 가지고 있다. : 상속보다 구현으로 객체지향적이다.
- (2) 미리 정의한 스레드 숫자만큼 실행한다. : 해당 queue 를 처리할 스레드를 수를 제어할 수 있다.
- (3) 해당 BlockingQueue 대기하고 있다.
- (4) Consumer 를 통해 take 한 item 을 처리할 방법을 client 에서 받아온다
- (5) interrupt 되더라도 Queue 에 남아있는 데이터는 모두 처리한다.


#### 2.3.3 AbstractListXBlockingQueue
- AbstractXBlockingQueue 를 상속받은 클래스로 차이점은 BlockingQueue에 쌓인 데이터를 여러건 모아서 후처리를 진행한다는 점이다. 
- take 가 아닌 poll 방식으로 추출하고, 특정시간 동안 원하는 수에 도달하지 않은 경우 현재까지 모여있는 데이터만 가지고 진행한다. 

#### 2.3.4 BlockingFactory
- BlockingQueue 생성을 한 곳에서 관리하기 위한 enum 생성자 팩토리이다. 
- DISRUPTOR_BLOCKING_QUEUE 의 경우에는 성능을 위한 DisruptorBlockingQueue 를 통해 생성하고 있다. 

```java
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
```

#### 2.3.5 XBlockingQueueMapping
- business model 과 blocking BlockingQueue 를 연동하기 위한 커스텀 어노테이션
- ElementType.METHOD 어노테이션으로 value 로 XBlockingQueue 를 입력해야 한다.

```java
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface XBlockingQueueMapping {
    Class<? extends XBlockingQueue<?>> value();
}
```

#### 2.3.6 XBlockingQueueRunner
- 앞에서 정의한 `XBlockingQueue`, `XBlockingQueueMapping` 를 연계하는 동작으로 정의한다.
- `XBlockingQueue` 에 데이터가 들어왔을 때, `XBlockingQueueMapping` 로 정의한 메서드에 내용이 별도의 thread 에서 동작한다.

```java
public class XBlockingQueueRunner implements ApplicationRunner {

    // ... 생략
    @Override
    public void run(ApplicationArguments args) throws Exception {
        initializeBlockingQueueRunner();
    }

    private void initializeBlockingQueueRunner() {
        for (String beanDefinitionName : context.getBeanDefinitionNames()) {
            Object bean = context.getBean(beanDefinitionName);
            Class<?> clazz = ReflectionUtils.findNoProxyClass(bean);
            Arrays.stream(clazz.getDeclaredMethods())
                    .filter(method -> !Objects.isNull(method.getAnnotation(XBlockingQueueMapping.class)))
                    .forEach(method -> registerMethod(bean, method));
        }
    }

    private void registerMethod(Object bean, Method method) {
        XBlockingQueueMapping xBlockingQueueMapping = method.getAnnotation(XBlockingQueueMapping.class);
        Class<? extends XBlockingQueue<?>> xBlockingQueue = xBlockingQueueMapping.value();

        try {
            Object bqBean = context.getBean(xBlockingQueue);
            if (bqBean instanceof AbstractXBlockingQueue<?> bqInstance) {
                bqInstance.run(executor, item -> {
                    try {
                        method.invoke(bean, item);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    } catch (IllegalArgumentException e) {
                        log.error("");
                    }
                });
            }
        } catch (NoSuchBeanDefinitionException e) {
            log.error("bq가 bean 으로 등록되어 있지 않습니다." + e);
        }
    }
}
```

#### 2.3.7 sample code 
- SampleQueue, SampleService class 가 bean 으로 등록되면, XBlockingQueueRunner 에서 이들을 엮어서 동작하는 thread 를 생성한다. 
- SampleQueue 에 item 이 들어오게 되면, 다른 thread 에서 SampleService.method 가 호출된다.

```java
public class SampleService {
    @XBlockingQueueMapping(SampleQueue.class)
    public void method(String item) { }
} 

public class SampleQueue extends AbstractXBlockingQueue<String> {
}
```


