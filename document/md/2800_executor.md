### 2.8.1 executor
- web application 은 tomcat 에서 요청온 request 에 대해서 thread 를 할당시켜 요청이 처리될 수 있게 해주는 기능이 존재한다.
- allin system 에서는 그 부분을 직접 control 해야 된다. (다음부터 그냥 http 사용하자)
- allin system 에서는 executor 는 3가지 모델에서 사용되고 있다. (Business, Receiver, BlockingQueue)
- 단순한 thread-pool 이 아닌 우아하게 종료될 수 있는 기능이 필요하다.


#### 2.8.1 XExecutor
- allin system executor 최상위 인터페이스로, Runnable 을 인자로 thread 를 실행하는 execute 메서드와 종료시점에 호출할 shutdown 메서드가 존재한다. 
- execute: Runnable 인자로 받아 thread 를 실행하는 메서드
- shutdown: jvm 종료시점에 호출할 shutdown 메서드를 통해 현재 진행 중인 thread 에게 종료할 수 있는 기회를 제공한다.
```java
public sealed interface XExecutor permits AbstractXExecutor {

    void execute(Runnable command);

    void shutdown();

}
```

#### 2.8.2 AbstractXExecutor
- XExecutor 를 직접 구현할 수 있는 유일한 class 로 공통적인 execute 메서드를 구현하고 있으며, 하위 클래스에서 넘겨온 ExecutorService 를 불변 객체로 만들어준다.
- (1) ExecutorService 를 상속이 아닌 내부 구현체로 가지고 있다.
- (2) 하위 클래스에 넘어온 ExecutorService 를 불변 객체로 만든다.
- (3) 공통적으로 사용되는 execute 를 구현해 코드량을 줄여준다.
```java
public non-sealed abstract class AbstractXExecutor implements XExecutor {
    // (1) ExecutorService 를 상속이 아닌 내부 구현체로 가지고 있다.
    protected final ExecutorService executorService;

    public AbstractXExecutor(ExecutorService executorService) {
        // (2) 하위 클래스에 넘어온 ExecutorService 를 불변 객체로 만든다.
        this.executorService = Executors.unconfigurableExecutorService(executorService);
    }

    public void execute(Runnable command){
        // (3) 공통적으로 사용되는 execute 를 구현해 코드량을 줄여준다.
        executorService.execute(command);
    }

}
```

#### 2.8.3 ReceiverXExecutor
- Receiver 들의 thread 를 수행하는 클래스로 cachedThreadPool 을 사용한다. 
- cachedThreadPool 은 기본 스레드나 대기 줄이 없고, 요청이 오는 경우마다 스레드를 생성하는 방식으로 Receiver 는 최초 서버 기동시점에만 호출되므로 이를 사용한다.

```java
public class ReceiverXExecutor extends AbstractXExecutor {

    public ReceiverXExecutor() {
        // (1) cachedThreadPool 은 기본 스레드나 대기 줄이 없고, 요청이 오는 경우마다 스레드를 생성하는 방식
        super(Executors.newCachedThreadPool());
    }

    @Override
    public void shutdown() {
        try {
            // Receiver Thread 들은 BlockingQueue를 사용하지 않고, 최초 진입이므로 바로 shutdownNow 를 호출한다.
            List<Runnable> runnables = this.executorService.shutdownNow();
            // 혹시 수행중인 경우 DispatcherServlet 을 호출하거나 직접 수행하는 경우도 3초가 limit
            if (!this.executorService.awaitTermination(3, TimeUnit.SECONDS)) {
                // 혹시 그래도 종료되지 않는 스레드가 존재하는 경우
                log.error("receiver exec가 정상 종료되지 않았습니다.");
            }
        } catch (InterruptedException e) {
            this.executorService.shutdownNow();
        }
    }
}
```

#### 2.8.3 BusinessXExecutor
- Business Thread 를 관리하는 XExecutor 로 고객사 요청으로 VirtualThread 를 사용한다.
- Business Thread 는 receiver thread 와 연관이 되어 있으므로 이를 감안한 shutdown 프로세스가 호출되게 된다.

```java

public class BusinessXExecutor extends AbstractXExecutor {

    public BusinessXExecutor() {
        super(Executors.newVirtualThreadPerTaskExecutor());
    }

    @Override
    public void shutdown() {
        try {
            // 3초간 Receiver 에서 추가할 수 있게 해준다
            if (!this.executorService.awaitTermination(3, TimeUnit.SECONDS)) {
                // 추가로 요청을 받지 않는다..
                this.executorService.shutdown();

                // 비지니스 모델은 1분동안 요청되어 있는 것들에 대한 처리를 수행한다.
                if (!this.executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    List<Runnable> runnables = this.executorService.shutdownNow();
                    log.error("비지니스 모델에서 처리하지 못한 데이터가 {} 건 존재합니다.", runnables.size());

                    runnables.forEach(runnable -> {
                        if (runnable instanceof VirtualThreadDispatcher.VirtualThreadRunnable businessCallable) {
                            // 후처리 프로세스
                        }
                    });
                    // toto hazelcast 로 처리 하지 못한 데이터들에 대한 처리를 수행한다.
                }

            }
            log.error("business shutdownAndAwaitTermination end");
        } catch (InterruptedException e) {
            this.executorService.shutdownNow();
        }
    }
}
```