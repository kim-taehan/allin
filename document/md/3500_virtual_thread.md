### 3.5 virtual thread
- virtual thread 는 자바 21 버전 부터 정식 도입된 기능 (java 21 이상 버전 사용)
- 기존의 자바의 thread (platform thread or user thread) 는 context switching 비용으로 인해 부가적인 리소스 소모가 큼
- 가상 스레드 란 기존의 전통적인 Java 스레드에 더하여 새롭게 추가되는 경량 스레드이며, 하나의 Java 프로세스가 수십만~ 수백만개의 스레드를 동시에 실행할 수 있게끔 설계
- 가상 스레드 를 사용하더라도 응답속도가 빨라지지는 않는다. (오히려 약간 느려질 수도). 다만 처리량이 늘어날 수 있다.


#### 3.5.1 virtual thread vs platform thread 
| |기존 스레드| 가상 스레드|
|--|--|--|
|메타 데이터 사이즈|약 2kb|200~300 B|
|메모리|미리 할당된 Stack 사용|필요시 마다 Heap 사용|
|컨텍스트 스위칭 비용|1~10us (커널영역에서 발생하는 작업)|ns (or 1us 미만)|


#### 3.5.2 간단한 로컬 테스트 결과
- 테스트내용: 20.000 건의 TCP/IP 요청을 보내고 서버에서는 이력만 DB에 넣고 응답을 내려줌
- 주의사항: PC 테스트라 성능이나 정확성 (애러 발생함) 에는 의미를 제한 둘 필요가 있다.
- 테스트 결과: 확실히 처리량에서는 높은 효과를 가지고 있다. 처리속도는 오히려 느려진것을 확인할 수 있다 (MTT)

|스레드 방식| 테스트 차수| Peak TPS| TPS| Mean Test Time(ms) |
|--|--|--|--|--------------------|
|가상스레드| 1차| 1,467| 753.3| 19.14              |
|플랫폼스레드| 1차| 1,063| 556.6| 11.04              |

![3520_virtual_thread.png](..%2Fimages%2F3520_virtual_thread.png)

#### 3.5.2 virtual thread 건수 제한 
- virtual thread 는 thread 생성비용이 굉장이 적기 때문에 thread pool 을 사용하여 재사용하는 것는 오히려 비효율적이다. 
- semaphore 기술을 이용해 thread 건수만 제한하고 매번 새로 생성하는 방식이 권장 된다.

```java
@Component
public class ThreadPoolBetDispatcher extends AbstractBetDispatcher{

    private final ExecutorService executorService;
    private final Semaphore semaphore;

    public ThreadPoolBetDispatcher(HandlerProvider handlerProvider, ConvertProvider convertProvider, ExecutorService executorService) {
        super(handlerProvider, convertProvider);
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();
        this.semaphore = new Semaphore(100);
    }

    @Override
    protected void doRun(Handler handler, BetReceiverDto convertedItem) {
        // 3. 호출
        try {
            semaphore.acquire();
            executorService.submit(new BusinessCallable(handler, convertedItem));
        } catch (InterruptedException e) {

        } finally {
            semaphore.release();
        }
    }
}
```