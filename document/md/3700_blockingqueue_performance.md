
### 3.7 BlockingQueue 성능
- Blocking Queue는 동시성 프로그래밍에서 사용되는 스레드 안전한 큐이다
- Lmax Disruptor 오픈 소스에 대해서 계속해서 요청이 있어서 이와 관련되 내용을 찾다가 정리한 내용이다.

#### 3.7.1 자바에서 제공하는 Blocking Queue 구현체 
- ArrayBlockingQueue
- LinkedBlockingQueue
- PriorityBlockingQueue
- SynchronousQueue


#### 3.7.2 DisruptorBlockingQueue (LMAX DISRUPTOR 사용한 Blocking Queue)
> https://github.com/conversant/disruptor/tree/master
```xml
<dependency>
  <groupId>com.conversantmedia</groupId>
  <artifactId>disruptor</artifactId>
  <version>1.2.16</version>
</dependency>
```

#### 3.7.3 테스트 개요
> ArrayBlockingQueue, LinkedBlockingQueue, DisruptorBlockingQueue 3가지 blocking queue로 여러 상황별 테스트를 수행함  
> 위의 내용을 각각 ArrayBQ, LinkedBQ, DisruptorBQ 로 줄여서 사용   
> Producer, Consumer를 테스트를 별도로 진행후 마지막에 같이 진행하도록 함

```java
enum BlockingQueueType implements BlockingQueueFactory{
    ArrayBlockingQueue{
        @Override
        public <T> BlockingQueue<T> createBlocking(int maxData) {
            return new ArrayBlockingQueue(maxData);
        }
    }, LinkedBlockingQueue {
        @Override
        public <T> BlockingQueue<T> createBlocking(int maxData) {
            return new LinkedBlockingQueue(maxData);
        }
    }, DisruptorBlockingQueue {
        @Override
        public <T> BlockingQueue<T> createBlocking(int maxData) {
            return new DisruptorBlockingQueue(maxData);
        }
    };
}
```


##### 3.7.3.1 Single Thread 생산자 테스트

> 테스트내용: 10_000_000건의 메시지를 Single Thread 로 Blocking Queue에 offer 하는 테스트   
> 결과 분석: LinkedBlockingQueue 방식을 제외하고 비슷한 속도

|회차|ArrayBQ|LinkedBQ|DisruptorBQ|
|--|--|--|--|
|1회차|706|2,440|713|
|2회차|718|2,253|783|
|3회차|734|2,240|879|


```java
@DisplayName("Single Thread 생산자")
@CsvSource({"ArrayBlockingQueue", "LinkedBlockingQueue", "DisruptorBlockingQueue"})
@ParameterizedTest
void SingleThreadOfferTest(BlockingQueueType blockingQueueType){

    // given
    BlockingQueue<String> blockingQueue = blockingQueueType.createBlocking(MAX_DATA);
    long startTime = System.currentTimeMillis();

    // when
    IntStream.range(0, MAX_DATA).forEach(i -> {
        blockingQueue.offer(Thread.currentThread().getName() + "_" + i);
    });

    // then
    long endTime = System.currentTimeMillis();
    log.info("{} 소요시간={}", blockingQueueType.name(), (endTime-startTime));
    Assertions.assertThat(blockingQueue.size()).isEqualTo(MAX_DATA);
}
```




##### 3.7.3.2. Multi Thread 생산자 테스트
> 테스트내용: 10_000_000건의 메시지를 5개의 쓰레드로 나누어 Blocking Queue에 offer 하는 테스트   
> 결과 분석: Single 테스트와 비슷한한 결과인데, Single Thread보다 DisruptorBlockingQueue 가 ArrayBlockingQueue보다 빨라짐  
> 특이한 점: 생산자에서 일어나는 Delay가 거의 없기 때문에 Single thread보다 block이 걸리는 Multi가 더 느리다. (추가테스트)

|회차|ArrayBQ|LinkedBQ|DisruptorBQ|
|--|--|--|--|
|1회차|1,040|2,620|896|
|2회차|976|2605|971|
|3회차|1,071|2,549|1,061|
|Single delay(10ms, 10,000건)|156,352|156,213|156,174|
|Multi delay(10ms, 10,000건)|31,260|31,226|31,511| 

```java
@DisplayName("Multi Thread 생산자")
@CsvSource({"ArrayBlockingQueue", "LinkedBlockingQueue", "DisruptorBlockingQueue"})
@ParameterizedTest
void MultiThreadOfferTest(BlockingQueueType blockingQueueType){

    // given
    BlockingQueue<String> blockingQueue = blockingQueueType.createBlocking(MAX_DATA);
    int threadCount = 5;
    int 스레드별처리량 = MAX_DATA / threadCount;
    long startTime = System.currentTimeMillis();

    Runnable runnable = new Producer(blockingQueue, 스레드별처리량);

    CompletableFuture[] ret = new CompletableFuture[threadCount];
    // when
    IntStream.range(0, threadCount).forEach(i->{
        CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(runnable);
        ret[i] = completableFuture;
    });

    CompletableFuture<Void> voidCompletableFuture = CompletableFuture.allOf(ret);
    voidCompletableFuture.join();

    // then
    long endTime = System.currentTimeMillis();
    log.info("{} 소요시간={}", blockingQueueType.name(), (endTime-startTime));
    Assertions.assertThat(blockingQueue.size()).isEqualTo(MAX_DATA);

}
```

##### 3.7.3.3 Single Thread 소비자 테스트
> 테스트내용: 10_000_000건의 메시지를 미리 넣어놓고 Single Thread로 take 하는 테스트   
> 결과 분석: 소비시에는 DisruptorBlockingQueue 확실에 성능에서 좋은 점이 보였다  
> 특이한 점: LinkedBlockingQueue 는 생산시에 ArrayBlockingQueue보다 느리지만 소비는 조금 빠른것을 확인

|회차|ArrayBQ|LinkedBQ|DisruptorBQ|
|--|--|--|--|
|1회차|250|240|155| 
|2회차|276|250|156| 
|3회차|265|220|152| 

```java
@DisplayName("Multi Thread 소비자")
@CsvSource({"ArrayBlockingQueue", "LinkedBlockingQueue", "DisruptorBlockingQueue"})
@ParameterizedTest
void MultiThreadTakeTest(BlockingQueueType blockingQueueType) throws InterruptedException {

    // given
    int threadCount = 5;
    int 스레드별처리량 = MAX_DATA / threadCount;
    BlockingQueue<String> blockingQueue = blockingQueueType.createBlocking(MAX_DATA);
    initDataSetting(blockingQueue, MAX_DATA);


    // when
    long startTime = System.currentTimeMillis();
    Runnable runnable = new Consumer(blockingQueue, 스레드별처리량);


    CompletableFuture[] ret = new CompletableFuture[threadCount];
    // when
    IntStream.range(0, threadCount).forEach(i->{
        CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(runnable);
        ret[i] = completableFuture;
    });



    CompletableFuture<Void> voidCompletableFuture = CompletableFuture.allOf(ret);
    voidCompletableFuture.join();

    // then
    long endTime = System.currentTimeMillis();
    log.info("{} 소요시간={}", blockingQueueType.name(), (endTime-startTime));
    Assertions.assertThat(blockingQueue.size()).isEqualTo(0);

}
```

##### 3.7.3.4 Multi Thread 소비자 테스트
> 테스트내용: 10_000_000건의 메시지를 미리 넣어놓고 Multi Thread(5개)로 take 하는 테스트   
> 결과 분석: 유의미하게 DisruptorBlockingQueue가 성능에 도움이됨

|회차|ArrayBQ|LinkedBQ|DisruptorBQ|
|--|--|--|--|
|1회차|400|399|239| 
|2회차|407|356|263| 
|3회차|383|399|252| 

```java
@DisplayName("Multi Thread 소비자")
@CsvSource({"ArrayBlockingQueue", "LinkedBlockingQueue", "DisruptorBlockingQueue"})
@ParameterizedTest
void MultiThreadTakeTest(BlockingQueueType blockingQueueType) throws InterruptedException {

    // given
    int threadCount = 5;
    int 스레드별처리량 = MAX_DATA / threadCount;
    BlockingQueue<String> blockingQueue = blockingQueueType.createBlocking(MAX_DATA);
    initDataSetting(blockingQueue, MAX_DATA);

    // when
    long startTime = System.currentTimeMillis();
    Runnable runnable = new Consumer(blockingQueue, 스레드별처리량);


    CompletableFuture[] ret = new CompletableFuture[threadCount];
    // when
    IntStream.range(0, threadCount).forEach(i->{
        CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(runnable);
        ret[i] = completableFuture;
    });

    CompletableFuture<Void> voidCompletableFuture = CompletableFuture.allOf(ret);
    voidCompletableFuture.join();

    // then
    long endTime = System.currentTimeMillis();
    log.info("{} 소요시간={}", blockingQueueType.name(), (endTime-startTime));
    Assertions.assertThat(blockingQueue.size()).isEqualTo(0);
}
```

##### 3.7.3.5 복잡한 Multi Thread 소비자 테스트
> 테스트내용: 10_000_000건의 메시지를 미리 넣어놓고 Multi Thread(5개)로 take 하는 중간에 다른 Multi Thread(5개로) 생산(10_000_000건)도 동시에 수행
> 결과 분석: 2-3.5배 정도의 속도 차이가 발생함 (multi thread 환경에서 생산과 소비가 동시에 발생하는 경우)  
> 그렇데 Single Thread 환경에서는 유의미한 차이는 나지 않는 것으로 보입니다.

![image](https://github.com/kim-taehan/high-performance-java/assets/52950400/3ba858ea-9dce-4e2b-bd60-485fd39d040d)

|회차|ArrayBQ|LinkedBQ|DisruptorBQ|
|--|--|--|--|
|1회차|2,688|4,251|1,138| 
|2회차|2,777|4,229|853| 
|3회차|2,576|4,491|783| 
|4회차|2,554|4,786|1,035| 
|5회차|2,232|4,840|719| 
|합계|2,565|4,519|906|



```java
@DisplayName("복잡한 Multi Thread 소비자")
@CsvSource({"ArrayBlockingQueue", "LinkedBlockingQueue", "DisruptorBlockingQueue"})
@ParameterizedTest
void ComplicatedMultiThreadTakeTest(BlockingQueueType blockingQueueType) throws InterruptedException {

    // given
    int threadCount = 5;
    int 스레드별처리량 = MAX_DATA*2 / threadCount;
    BlockingQueue<String> blockingQueue = blockingQueueType.createBlocking(MAX_DATA*2);
    initDataSetting(blockingQueue, MAX_DATA);


    // when
    long startTime = System.currentTimeMillis();
    Runnable runnable = new Consumer(blockingQueue, 스레드별처리량);


    CompletableFuture[] ret = new CompletableFuture[threadCount];
    // when
    IntStream.range(0, threadCount).forEach(i->{
        CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(runnable);
        ret[i] = completableFuture;
    });


    Thread.sleep(100);
    if (blockingQueue.size() == 0) {
        throw new IllegalStateException("0이되면 안됨");
    }
    
    // 최대한 복잡하게 해보자
    int producerThread = 5;
    int producer스레드별처리량 = MAX_DATA / producerThread;
    Runnable producerRunnable = new Producer(blockingQueue, producer스레드별처리량);

    // when
    IntStream.range(0, producerThread).forEach(i->{
        CompletableFuture.runAsync(producerRunnable);
    });
    
    CompletableFuture<Void> voidCompletableFuture = CompletableFuture.allOf(ret);
    voidCompletableFuture.join();

    // then
    long endTime = System.currentTimeMillis();
    log.info("{} 소요시간={}", blockingQueueType.name(), (endTime-startTime));
    Assertions.assertThat(blockingQueue.size()).isEqualTo(0);
}
```

##### 3.7.3.6 전체 소스 공유
```java
package com.std.study.blockingqueue;

import com.conversantmedia.util.concurrent.DisruptorBlockingQueue;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.IntStream;

@Slf4j
public class SimpleBlockingQueueTest {

    public static final int MAX_DATA = 10_000_000;

    interface BlockingQueueFactory{
        <T> BlockingQueue<T> createBlocking(int maxData);
    }
    enum BlockingQueueType implements BlockingQueueFactory{
        ArrayBlockingQueue{
            @Override
            public <T> BlockingQueue<T> createBlocking(int maxData) {
                return new ArrayBlockingQueue(maxData);
            }
        }, LinkedBlockingQueue {
            @Override
            public <T> BlockingQueue<T> createBlocking(int maxData) {
                return new LinkedBlockingQueue(maxData);
            }
        }, DisruptorBlockingQueue {
            @Override
            public <T> BlockingQueue<T> createBlocking(int maxData) {
                return new DisruptorBlockingQueue(maxData);
            }
        };
    }

    static class Producer implements Runnable {

        private final BlockingQueue blockingQueue;
        private final int 스레드별처리량;
        public Producer(BlockingQueue blockingQueue, int 스레드별처리량) {
            this.blockingQueue = blockingQueue;
            this.스레드별처리량 = 스레드별처리량;
        }
        @Override
        public void run() {
            IntStream.range(0, 스레드별처리량).forEach(i -> {
                blockingQueue.offer(Thread.currentThread().getName() + "_" + i);
            });
        }
    }


    static class Consumer implements Runnable {

        private final BlockingQueue blockingQueue;
        private final int 스레드별처리량;
        public Consumer(BlockingQueue blockingQueue, int 스레드별처리량) {
            this.blockingQueue = blockingQueue;
            this.스레드별처리량 = 스레드별처리량;
        }
        @Override
        public void run() {
            IntStream.range(0, 스레드별처리량).forEach(i -> {
                try {
                    blockingQueue.take();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }


    @DisplayName("Single Thread 생산자")
    @CsvSource({"ArrayBlockingQueue", "LinkedBlockingQueue", "DisruptorBlockingQueue"})
    @ParameterizedTest
    void SingleThreadOfferTest(BlockingQueueType blockingQueueType){

        // given
        BlockingQueue<String> blockingQueue = blockingQueueType.createBlocking(MAX_DATA);
        long startTime = System.currentTimeMillis();

        // when
        IntStream.range(0, MAX_DATA).forEach(i -> {
            blockingQueue.offer(Thread.currentThread().getName() + "_" + i);
        });

        // then
        long endTime = System.currentTimeMillis();
        log.info("{} 소요시간={}", blockingQueueType.name(), (endTime-startTime));
        Assertions.assertThat(blockingQueue.size()).isEqualTo(MAX_DATA);
    }

    @DisplayName("Multi Thread 생산자")
    @CsvSource({"ArrayBlockingQueue", "LinkedBlockingQueue", "DisruptorBlockingQueue"})
    @ParameterizedTest
    void MultiThreadOfferTest(BlockingQueueType blockingQueueType){

        // given
        BlockingQueue<String> blockingQueue = blockingQueueType.createBlocking(MAX_DATA);
        int threadCount = 5;
        int 스레드별처리량 = MAX_DATA / threadCount;
        long startTime = System.currentTimeMillis();

        Runnable runnable = new Producer(blockingQueue, 스레드별처리량);

        CompletableFuture[] ret = new CompletableFuture[threadCount];
        // when
        IntStream.range(0, threadCount).forEach(i->{
            CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(runnable);
            ret[i] = completableFuture;
        });

        CompletableFuture<Void> voidCompletableFuture = CompletableFuture.allOf(ret);
        voidCompletableFuture.join();

        // then
        long endTime = System.currentTimeMillis();
        log.info("{} 소요시간={}", blockingQueueType.name(), (endTime-startTime));
        Assertions.assertThat(blockingQueue.size()).isEqualTo(MAX_DATA);
    }


    @DisplayName("Single Thread 소비자")
    @CsvSource({"ArrayBlockingQueue", "LinkedBlockingQueue", "DisruptorBlockingQueue"})
    @ParameterizedTest
    void SingleThreadTakeTest(BlockingQueueType blockingQueueType){

        // given
        BlockingQueue<String> blockingQueue = blockingQueueType.createBlocking(MAX_DATA);
        IntStream.range(0, MAX_DATA).forEach(i -> {
            blockingQueue.offer(Thread.currentThread().getName() + "_" + i);
        });

        // when
        long startTime = System.currentTimeMillis();
        IntStream.range(0, MAX_DATA).forEach(i -> {
            try {
                blockingQueue.take();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        // then
        long endTime = System.currentTimeMillis();
        log.info("{} 소요시간={}", blockingQueueType.name(), (endTime-startTime));
        Assertions.assertThat(blockingQueue.size()).isEqualTo(0);
    }

    @DisplayName("Multi Thread 소비자")
    @CsvSource({"ArrayBlockingQueue", "LinkedBlockingQueue", "DisruptorBlockingQueue"})
    @ParameterizedTest
    void MultiThreadTakeTest(BlockingQueueType blockingQueueType) throws InterruptedException {

        // given
        int threadCount = 5;
        int 스레드별처리량 = MAX_DATA / threadCount;
        BlockingQueue<String> blockingQueue = blockingQueueType.createBlocking(MAX_DATA);
        initDataSetting(blockingQueue, MAX_DATA);

        // when
        long startTime = System.currentTimeMillis();
        Runnable runnable = new Consumer(blockingQueue, 스레드별처리량);


        CompletableFuture[] ret = new CompletableFuture[threadCount];
        // when
        IntStream.range(0, threadCount).forEach(i->{
            CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(runnable);
            ret[i] = completableFuture;
        });

        CompletableFuture<Void> voidCompletableFuture = CompletableFuture.allOf(ret);
        voidCompletableFuture.join();

        // then
        long endTime = System.currentTimeMillis();
        log.info("{} 소요시간={}", blockingQueueType.name(), (endTime-startTime));
        Assertions.assertThat(blockingQueue.size()).isEqualTo(0);
    }


    private void initDataSetting(BlockingQueue<String> blockingQueue, int count) {
        IntStream.range(0, count).forEach(i -> {
            blockingQueue.offer(Thread.currentThread().getName() + "_" + i);
        });
    }

    @DisplayName("복잡한 Multi Thread 소비자")
    @CsvSource({"ArrayBlockingQueue", "LinkedBlockingQueue", "DisruptorBlockingQueue"})
    @ParameterizedTest
    void ComplicatedMultiThreadTakeTest(BlockingQueueType blockingQueueType) throws InterruptedException {

        // given
        int threadCount = 5;
        int 스레드별처리량 = MAX_DATA*2 / threadCount;
        BlockingQueue<String> blockingQueue = blockingQueueType.createBlocking(MAX_DATA*2);
        initDataSetting(blockingQueue, MAX_DATA);


        // when
        long startTime = System.currentTimeMillis();
        Runnable runnable = new Consumer(blockingQueue, 스레드별처리량);


        CompletableFuture[] ret = new CompletableFuture[threadCount];
        // when
        IntStream.range(0, threadCount).forEach(i->{
            CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(runnable);
            ret[i] = completableFuture;
        });


        Thread.sleep(100);
        if (blockingQueue.size() == 0) {
            throw new IllegalStateException("0이되면 안됨");
        }
        
        // 최대한 복잡하게 해보자
        int producerThread = 5;
        int producer스레드별처리량 = MAX_DATA / producerThread;
        Runnable producerRunnable = new Producer(blockingQueue, producer스레드별처리량);

        // when
        IntStream.range(0, producerThread).forEach(i->{
            CompletableFuture.runAsync(producerRunnable);
        });
        
        CompletableFuture<Void> voidCompletableFuture = CompletableFuture.allOf(ret);
        voidCompletableFuture.join();

        // then
        long endTime = System.currentTimeMillis();
        log.info("{} 소요시간={}", blockingQueueType.name(), (endTime-startTime));
        Assertions.assertThat(blockingQueue.size()).isEqualTo(0);
    }
}

```
