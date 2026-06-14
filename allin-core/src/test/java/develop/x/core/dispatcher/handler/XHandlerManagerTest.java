package develop.x.core.dispatcher.handler;

import develop.x.core.dispatcher.annotation.XController;
import develop.x.core.dispatcher.annotation.XMapping;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;

import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class XHandlerManagerTest {


    @Test
    @DisplayName("정상적으로 등록된 @XMapping 메서드는 handler 로 등록된고 이를 url 정보로 찾을 수 있다.")
    void findXMappingHandler() throws InvocationTargetException, IllegalAccessException {

        // given : 한 컨텍스트에 두 컨트롤러(/order-api, /cancel-api)가 공존
        ApplicationContext context = new AnnotationConfigApplicationContext(SimpleConfig.class);
        XHandlerManager xHandlerManager = context.getBean(XHandlerManager.class);
        OrderController orderController = context.getBean(OrderController.class);
        CancelController cancelController = context.getBean(CancelController.class);

        // when : 각 url 로 핸들러를 조회해 실제 호출
        XHandler cancelHandler = xHandlerManager.findHandler("/cancel-api");
        cancelHandler.method().invoke(cancelHandler.bean());

        XHandler orderHandler = xHandlerManager.findHandler("/order-api");
        orderHandler.method().invoke(orderHandler.bean());

        // then : 각 url 이 올바른 bean/method 로 매핑되고 교차 매핑이 없어야 한다.
        assertAll(
                // cancel
                () -> assertThat(cancelHandler.method().getName()).isEqualTo("cancel"),
                () -> assertThat(cancelHandler.bean()).isSameAs(cancelController),
                () -> assertThat(cancelController.callCount).isEqualTo(1),
                () -> assertThat(orderController.callCount).isEqualTo(1),
                // order
                () -> assertThat(orderHandler.method().getName()).isEqualTo("order"),
                () -> assertThat(orderHandler.bean()).isSameAs(orderController),
                // 교차 매핑 없음: order 핸들러의 bean 이 cancelController 가 아니어야 한다
                () -> assertThat(orderHandler.bean()).isNotSameAs(cancelController)
        );
    }

    @Test
    @DisplayName("등록된 URL이 없는 경우 IllegalStateException이 발생한다.")
    void noRegisteredUrlOccursISE () throws InvocationTargetException, IllegalAccessException {

        // given
        ApplicationContext context = new AnnotationConfigApplicationContext(SimpleConfig.class);
        XHandlerManager xHandlerManager = context.getBean(XHandlerManager.class);

        // when && then
        assertThatThrownBy(() -> xHandlerManager.findHandler("/not-found-api"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("요청하신 api를 처리할 수 있는 handler가 존재하지 않습니다.");
    }

    @Test
    @DisplayName("동일한 URL 등록은 허용하지 않는다. boot 오류 (BeanCreationException)")
    void SameUrlNotAllow() {
        assertThatThrownBy(() -> new AnnotationConfigApplicationContext(DuplicatedConfig.class))
                .isInstanceOf(BeanCreationException.class)
                .hasMessageContaining("동일한 url을 등록할 수 없습니다.");
    }


    @AutoConfiguration
    static class SimpleConfig {
        @Bean
        public OrderController orderController() {
            return new OrderController();
        }

        @Bean
        public CancelController cancelController() {
            return new CancelController();
        }

        @Bean
        public XHandlerManager xHandlerManager(ApplicationContext context) {
            return new XHandlerManager(context);
        }
    }

    @AutoConfiguration
    static class DuplicatedConfig {
        @Bean
        public OrderController orderController() {
            return new OrderController();
        }

        @Bean
        public OrderControllerCopy orderControllerCopy() {
            return new OrderControllerCopy();
        }

        @Bean
        public XHandlerManager xHandlerManager(ApplicationContext context) {
            return new XHandlerManager(context);
        }
    }

    @XController
    static class OrderController {
        public int callCount = 0;

        @XMapping("/order-api")
        public void order() {
            callCount++;
        }
    }

    @XController
    static class OrderControllerCopy {
        public int callCount = 0;

        @XMapping("/order-api")
        public void order() {
            callCount++;
        }
    }

    @XController
    static class CancelController {
        public int callCount = 0;

        @XMapping("/cancel-api")
        public void cancel() {
            callCount++;
        }
    }
}