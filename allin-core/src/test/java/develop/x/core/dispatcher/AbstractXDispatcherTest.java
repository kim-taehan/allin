package develop.x.core.dispatcher;

import develop.x.core.dispatcher.argumentresolver.XArgumentProvider;
import develop.x.core.dispatcher.handler.XHandler;
import develop.x.core.dispatcher.handler.XHandlerManager;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import develop.x.io.XRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AbstractXDispatcherTest {

    /** AbstractXDispatcher 의 로그를 캡처해 "조용히 삼킴"이 아님을 단언하기 위한 appender. */
    private Logger dispatcherLogger;
    private ListAppender<ILoggingEvent> logAppender;

    @BeforeEach
    void attachAppender() {
        dispatcherLogger = (Logger) LoggerFactory.getLogger(AbstractXDispatcher.class);
        logAppender = new ListAppender<>();
        logAppender.start();
        dispatcherLogger.addAppender(logAppender);
    }

    @AfterEach
    void detachAppender() {
        dispatcherLogger.detachAppender(logAppender);
    }

    /**
     * url 헤더를 가진 XRequest 가 만들어지도록 byte[] 를 생성한다.
     * XRequest 의 Builder.toByte() / 생성자 파싱이 round-trip 됨을 이용한다.
     */
    private static byte[] requestBytesWithUrl(String url) {
        return new XRequest.Builder()
                .header("url", url)
                .body(new byte[]{1, 2, 3})
                .build()
                .toByte();
    }

    /**
     * doRun 에 전달된 인자를 캡처하는 테스트용 서브클래스.
     */
    static class CapturingDispatcher extends AbstractXDispatcher {
        XHandler capturedHandler;
        Object[] capturedArguments;
        int doRunCallCount = 0;
        RuntimeException toThrow;

        CapturingDispatcher(XHandlerManager handlerManager, XArgumentProvider provider) {
            super(handlerManager, provider);
        }

        @Override
        protected void doRun(XHandler handler, Object[] arguments) {
            doRunCallCount++;
            this.capturedHandler = handler;
            this.capturedArguments = arguments;
            if (toThrow != null) {
                throw toThrow;
            }
        }
    }

    @Test
    @DisplayName("invoke 는 url 헤더로 findHandler 를 호출하고, 그 handler 와 변환된 인자를 doRun 에 그대로 전달한다.")
    void invokeRoutesHandlerAndArgumentsToDoRun() {
        // given
        XHandlerManager handlerManager = mock(XHandlerManager.class);
        XArgumentProvider provider = mock(XArgumentProvider.class);
        XHandler handler = mock(XHandler.class);
        Object[] resolvedArgs = new Object[]{"arg0", 42};

        when(handlerManager.findHandler("order.bet")).thenReturn(handler);
        when(provider.convertArguments(eq(handler), any(XRequest.class))).thenReturn(resolvedArgs);

        CapturingDispatcher dispatcher = new CapturingDispatcher(handlerManager, provider);

        // when
        dispatcher.invoke(requestBytesWithUrl("order.bet"));

        // then
        verify(handlerManager).findHandler("order.bet");

        ArgumentCaptor<XRequest> requestCaptor = ArgumentCaptor.forClass(XRequest.class);
        verify(provider).convertArguments(eq(handler), requestCaptor.capture());
        assertThat(requestCaptor.getValue().getHeaders()).containsEntry("url", "order.bet");

        assertThat(dispatcher.doRunCallCount).isEqualTo(1);
        assertThat(dispatcher.capturedHandler).isSameAs(handler);
        assertThat(dispatcher.capturedArguments).isSameAs(resolvedArgs);
    }

    @Test
    @DisplayName("doRun 의 RuntimeException 은 (의도된 인프라성 fallback 으로) 호출 스레드로 전파되지 않되, 조용히 사라지지 않고 url 컨텍스트와 예외가 ERROR 로 로깅된다.")
    void invokeSwallowsRuntimeExceptionFromDoRunButLogsIt() {
        // given
        XHandlerManager handlerManager = mock(XHandlerManager.class);
        XArgumentProvider provider = mock(XArgumentProvider.class);
        XHandler handler = mock(XHandler.class);

        when(handlerManager.findHandler(any())).thenReturn(handler);
        when(provider.convertArguments(any(), any())).thenReturn(new Object[0]);

        CapturingDispatcher dispatcher = new CapturingDispatcher(handlerManager, provider);
        IllegalStateException boom = new IllegalStateException("business boom");
        dispatcher.toThrow = boom;

        // when : 예외가 호출 스레드로 전파되지 않아야 한다(비전파는 의도된 설계).
        dispatcher.invoke(requestBytesWithUrl("order.bet"));

        // then-1 : 부수효과 — doRun 은 실제로 1회 호출되었다(예외 발생 지점에 도달).
        assertThat(dispatcher.doRunCallCount).isEqualTo(1);

        // then-2 : "조용히 삼킴"이 아님을 보장 — catch 블록이 비면 이 단언이 깨진다(회귀 탐지).
        //          ERROR 레벨로, 원 예외를 throwable 로 동반하고, url 컨텍스트를 남겨야 한다.
        assertThat(logAppender.list)
                .as("RuntimeException 삼킴 시 ERROR 로그가 반드시 남아야 한다(빈 catch 회귀 방지)")
                .anySatisfy(event -> {
                    assertThat(event.getLevel()).isEqualTo(Level.ERROR);
                    assertThat(event.getThrowableProxy()).isNotNull();
                    assertThat(event.getThrowableProxy().getMessage()).contains("business boom");
                    // url 컨텍스트(MDC 가 아닌 포맷 인자)가 메시지에 반영되었는지 확인
                    assertThat(event.getFormattedMessage()).contains("order.bet");
                });
    }

    @Test
    @DisplayName("findHandler 가 (url 부재 등으로) 예외를 던지면 try 블록 밖이라 invoke 가 그대로 전파한다.")
    void invokePropagatesFindHandlerException() {
        // given
        XHandlerManager handlerManager = mock(XHandlerManager.class);
        XArgumentProvider provider = mock(XArgumentProvider.class);

        when(handlerManager.findHandler(any()))
                .thenThrow(new IllegalStateException("핸들러 없음"));

        CapturingDispatcher dispatcher = new CapturingDispatcher(handlerManager, provider);

        // when / then
        assertThatThrownBy(() -> dispatcher.invoke(requestBytesWithUrl("unknown")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("핸들러 없음");

        assertThat(dispatcher.doRunCallCount).isZero();
    }
}
