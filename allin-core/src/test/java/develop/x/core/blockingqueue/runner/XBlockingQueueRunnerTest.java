package develop.x.core.blockingqueue.runner;

import develop.x.core.blockingqueue.AbstractXBlockingQueue;
import develop.x.core.blockingqueue.annotation.XBlockingQueueMapping;
import develop.x.core.executor.AbstractXExecutor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class XBlockingQueueRunnerTest {

    @Test
    @DisplayName("bq 를 사용하여 @XBlockingQueueMapping 이 정의된 메서드를 호출 할 수 있다.")
    void callXBqMappingMethodByBq() throws Exception {
        // given
        try (AnnotationConfigApplicationContext applicationContext =
                     new AnnotationConfigApplicationContext(TestConfig.class)) {
            TestController testController = applicationContext.getBean(TestController.class);
            TestQueue testQueue = applicationContext.getBean(TestQueue.class);
            XBlockingQueueRunner xBlockingQueueRunner = applicationContext.getBean(XBlockingQueueRunner.class);
            xBlockingQueueRunner.run(null);

            // when
            String item = UUID.randomUUID().toString();
            testQueue.put(item);

            // 소비 스레드가 핸들러를 호출해 값을 세팅하면 latch 가 countDown 된다.
            // latch.await() 는 happens-before 경계를 형성하므로 메인 스레드가 최신 값을 읽는다.
            boolean consumed = testController.latch.await(2, TimeUnit.SECONDS);

            // then
            assertThat(consumed)
                    .as("2초 내에 @XBlockingQueueMapping 핸들러가 호출되어야 한다")
                    .isTrue();
            assertThat(testController.item).isEqualTo(item);
        }
    }


    static class TestConfig {

        @Bean
        public TestController testController() {
            return new TestController();
        }

        @Bean
        public TestQueue testQueue() {
            return new TestQueue();
        }

        @Bean
        public TestXExecutor testXExecutor() {
            return new TestXExecutor();
        }

        @Bean
        public XBlockingQueueRunner xBlockingQueueRunner(org.springframework.context.ApplicationContext context) {
            return new XBlockingQueueRunner(context, testXExecutor());
        }
    }

    static class TestController {
        final CountDownLatch latch = new CountDownLatch(1);
        volatile String item;

        @XBlockingQueueMapping(TestQueue.class)
        public void test(String item) {
            this.item = item;
            this.latch.countDown();
        }
    }

    static class TestQueue extends AbstractXBlockingQueue<String> {
    }

    static class TestXExecutor extends AbstractXExecutor {
        public TestXExecutor() {
            super(Executors.newCachedThreadPool());
        }

        @Override
        public void shutdown() {
            this.executorService.shutdownNow();
        }
    }

}
