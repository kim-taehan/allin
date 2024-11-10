package develop.x.core.dispatcher;

import develop.x.core.dispatcher.argumentresolver.XArgumentProvider;
import develop.x.core.dispatcher.handler.XHandler;
import develop.x.core.dispatcher.handler.XHandlerManager;
import develop.x.core.executor.BusinessXExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Semaphore;

@Slf4j
public class VirtualThreadDispatcher extends AbstractXDispatcher {

    private final BusinessXExecutor executor;
    private final Semaphore semaphore;

    public VirtualThreadDispatcher(XHandlerManager handlerManager, XArgumentProvider XArgumentProvider, BusinessXExecutor executor) {
        super(handlerManager, XArgumentProvider);
        this.executor = executor;
        this.semaphore = new Semaphore(100);
    }

    @Override
    protected void doRun(XHandler handler, Object[] arguments) {

        try {
            this.semaphore.acquire();
            executor.execute((new VirtualThreadRunnable(handler, arguments, semaphore)));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @RequiredArgsConstructor
    public static class VirtualThreadRunnable implements Runnable {

        private final XHandler handler;
        private final Object[] arguments;
        private final Semaphore semaphore;

        @Override
        public void run() {
            try {
                handler.method().invoke(handler.bean(), arguments);
            } catch (IllegalAccessException e) {
                log.error("virtualThreadDispatcher 요청에 실패했습니다. ", e);
            } catch (InvocationTargetException ignored) {
                // exception advice 를 통해 처리되는 부분이 있어 발생하지 않음
                log.error("business model 수행중에 애러가 발생하였습니다. ", ignored);
            } finally {
                semaphore.release();
            }
        }
    }
}
