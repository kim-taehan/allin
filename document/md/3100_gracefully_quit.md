### 3.1 우아하게 종료하기
- allin 시스템에서 Thread 를 별도로 관리하게 되면서 애플리케이션이 한번에 죽지않는 문제가 발생하기 시작하였다.
- 자바는 thread 기반으로 수행되는 언어로 종료를 하기 위해서는 데몬 형태의 스레드를 제외한 모든 스레드가 종료되어야 정상적인 종료가 이루어진다.
- Graceful shutdown 란 프로그램을 종료할 때 최대한 side effect가 없도록 로직들을 처리하고 종료하는 걸 말한다.

![3100_gracefully_quit.png](..%2Fimages%2F3100_gracefully_quit.png)
- (1) 사용자 또는 시스템에서 Spring application 에서 kill -15 시그널을 보내게 된다. 
- (2) ContextCloseEvent listener 는 각 ExecuteServer(XExecutor) 에게 shutdown 요청을 보낸다.
- (3) ExecuteServer(XExecutor) 는 내부에 thread 에게 interrupt 요청을 보내게 된다. 
- (4) 각 thread 들은 interrupt 요청을 처리할 수 있게 코딩이 되어 있어야 그 요청을 받을 수 있다. 


#### 3.1.1 작업을 하지 않은 경우 발생하는 내용
- kill -15 로는 죽지않고, kill -9 로만 프로그램을 종료할 수 있다.
  - SIGKILL(9): 프로세스를 즉시 종료. 처리중이던 작업들을 즉시 종료한다.
  - SIGTERM(15): 프로세스를 정상적으로 종료시킨다. 소프트웨어 프로세스에게 종료하라는 시그널을 발생시켜 안전하게 종료할 수 있게 해준다.
- 작업중이 thread 가 작업내용을 마무리 하지 않고 강제로 종료된다. 만약 특정 업무가 수행중이라도 그 업무를 끝내지 않은 상태에서 JVM 이 종료되게 된다.

#### 3.1.2 스프링 종료 이벤트
- Spring, java 에서는 종료 이벤트를 받을 수 있는 몇가지 방법이 존재하는데, 그 중에 allin system 은 Spring 에서 제공하는 ContextCloseEvent Listener 를 등록하는 방식을 사용한다. 
- (1) XExecutor 라는 ExecuteService wrapping 한 allin 시스템의 스레드 pool 을 사용하는 모든 시스템을 저장한다.
- (2) 종료 시그널이 왔을 때, 종료를 위한 ExecutorService 를 생성하여 이미 동작중인 ExecutorService 를 종료할 수 있게 한다.
- (3) 기존 동작중인 XExecutor 에게 종료 시그널을 보내면, 각각 XExecutor 별로 정의된 우아한 종료를 시작한다.
```java
public class ShutdownEventListener implements ApplicationListener<ContextClosedEvent> {
    
    // (1) XExecutor 라는 ExecuteService wrapping 한 allin 시스템의 스레드 pool 을 사용하는 모든 시스템을 저장한다.
    private final Collection<XExecutor> xExecutors;
    public ShutdownEventListener(ApplicationContext context) {
        this.xExecutors = context.getBeansOfType(XExecutor.class).values();
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        log.info("system 종료시도합니다...");
        // (2) 종료 시그널이 왔을 때, 종료를 위한 ExecutorService 를 생성하여 이미 동작중인 XExecutor 를 종료할 수 있게 한다.
        try (ExecutorService closeExec = Executors.newCachedThreadPool()){
            for (XExecutor xExecutor : xExecutors) {
                
                // (3) 기존 동작중인 XExecutor 에게 종료 시그널을 보내면, 각각 XExecutor 별로 정의된 우아한 종료를 시작한다.
                closeExec.execute(xExecutor::shutdown);
            }

            try {
                if (!closeExec.awaitTermination(3, TimeUnit.SECONDS)) {
                    closeExec.shutdownNow();
                    if (!closeExec.awaitTermination(20, TimeUnit.SECONDS)){
                        System.err.println("Pool did not terminate");
                    }
                }
                log.info("system 이상없이 종료되었습니다.");
            } catch (InterruptedException e) {
                closeExec.shutdownNow();
                // Preserve interrupt status
                Thread.currentThread().interrupt();
            }
        }
    }
}
```

#### 3.1.3 allin system 에서 사용하는 thread 
- XExecutor 라는 ExecuteService wrapping 한 allin 시스템의 스레드 pool 을 관리하는 프로세스가 존재한다. 
- [관련된 내용 확인하기](2800_executor.md)
- allin system core 에서 사용되는 thread 종류에는 3가지가 있고, 좀더 늘어날 가능성이 존재한다.
  - receiver IQueue 데이터를 take 하면서 기다리는 thread 
  - business model 에서 사용되는 thread (virtual thread) 
  - blocking queue 를 기다리고 있다 business model 을 호출하기 위한 thread
- 3가지 thread 모두 thread pool 을 사용하고 있고, 이 thread pool 에 존재하는 thread 가 종료되어야 시스템이 정상적으로 종료된다. 


#### 3.1.4 ExecutorService (Thread pool) 
- ExecutorService 에서 제공하는 가이드 
```java
void shutdownAndAwaitTermination(ExecutorService pool) {
  pool.shutdown();
  // Disable new tasks from being submitted    
  try {
    // Wait a while for existing tasks to terminate     
    if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
      pool.shutdownNow();
      // Cancel currently executing tasks     
      // Wait a while for tasks to respond to being cancelled      
      if (!pool.awaitTermination(60, TimeUnit.SECONDS))
        System.err.println("Pool did not terminate");
      }
  }
  catch (InterruptedException ex) {
    // (Re-)Cancel if current thread also interrupted     
    pool.shutdownNow();
    // Preserve interrupt status    
    Thread.currentThread().interrupt();
  }
}
```
- shoutdown: 현재 처리하고 있는 작업과 작업 큐에 들어있는 모든 작업을 처리한 뒤에 스레드 풀을 종료시킨다.
- shutdownNow: 현재 처리하고 있는 쓰레드를 interrupt로 중지하고, 스레드 풀을 종료시킨다. 미처리된 작업들의 리스트를 반환한다
- awaitTermination: shutdown() 메소드를 우선 호출하고, 주어진 timeout 내에 작업을 모두 완료했으면 true, 그렇지 않으면 작업중인 쓰레드를 interrupt로 종료시키고 false를 반환한다


 
