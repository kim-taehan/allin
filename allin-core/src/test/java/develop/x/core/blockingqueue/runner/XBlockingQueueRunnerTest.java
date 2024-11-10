package develop.x.core.blockingqueue.runner;

import develop.x.core.blockingqueue.AbstractXBlockingQueue;
import develop.x.core.blockingqueue.annotation.XBlockingQueueMapping;
import develop.x.core.executor.AbstractXExecutor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.UUID;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

class XBlockingQueueRunnerTest {

    @Test
    @DisplayName("bq 를 사용하여 @XBlockingQueueMapping 이 정의된 메서드를 호출 할 수 있다.")
    void callXBqMappingMethodByBq() throws Exception {
        // given
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(TestConfig.class);
        TestController testController = applicationContext.getBean(TestController.class);
        TestQueue testQueue = applicationContext.getBean(TestQueue.class);
        XBlockingQueueRunner xBlockingQueueRunner = applicationContext.getBean(XBlockingQueueRunner.class);
        xBlockingQueueRunner.run(null);

        // when
        String item = UUID.randomUUID().toString();
        testQueue.put(item);

        Thread.sleep(10);

        // then
        assertThat(testController.item).isEqualTo(item);
    }



    static class TestConfig {

        @Bean
        public TestController testController(){
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
        public XBlockingQueueRunner xBlockingQueueRunner(ApplicationContext context) throws Exception {
            return new XBlockingQueueRunner(context, testXExecutor());
        }
    }

    static class TestController {
        private String item;
        @XBlockingQueueMapping(TestQueue.class)
        public void test(String item) {
            this.item = item;
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