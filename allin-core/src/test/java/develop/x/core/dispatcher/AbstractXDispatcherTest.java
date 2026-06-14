package develop.x.core.dispatcher;

import develop.x.core.dispatcher.argumentresolver.XArgumentProvider;
import develop.x.core.dispatcher.handler.XHandler;
import develop.x.core.dispatcher.handler.XHandlerManager;
import develop.x.io.XRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AbstractXDispatcherTest {

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
    @DisplayName("doRun 이 RuntimeException 을 던져도 invoke 는 예외를 전파하지 않고 삼킨다(로깅만).")
    void invokeSwallowsRuntimeExceptionFromDoRun() {
        // given
        XHandlerManager handlerManager = mock(XHandlerManager.class);
        XArgumentProvider provider = mock(XArgumentProvider.class);
        XHandler handler = mock(XHandler.class);

        when(handlerManager.findHandler(any())).thenReturn(handler);
        when(provider.convertArguments(any(), any())).thenReturn(new Object[0]);

        CapturingDispatcher dispatcher = new CapturingDispatcher(handlerManager, provider);
        dispatcher.toThrow = new IllegalStateException("business boom");

        // when / then : 예외가 전파되지 않아야 한다.
        dispatcher.invoke(requestBytesWithUrl("order.bet"));

        assertThat(dispatcher.doRunCallCount).isEqualTo(1);
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
