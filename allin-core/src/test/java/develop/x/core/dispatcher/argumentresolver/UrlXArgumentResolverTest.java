//package develop.x.core.dispatcher.argumentresolver;
//
//import develop.x.core.dispatcher.XRequestOld;
//import develop.x.core.dispatcher.annotation.XParam;
//import develop.x.io.model.ContentType;
//import develop.x.io.model.XHeaderOld;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//
//import java.lang.reflect.Method;
//import java.lang.reflect.Parameter;
//import java.util.UUID;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//class UrlXArgumentResolverTest {
//
//    UrlXArgumentResolver urlXArgumentResolver = new UrlXArgumentResolver();
//
//    @DisplayName("url argument resolver 기능 테스트")
//    @Test
//    void urlXArgumentResolverTest(){
//        // given
//        Parameter parameters = null;
//        for (Method method : TestService.class.getDeclaredMethods()) {
//            if (method.getName().equals("functionByUrl")) {
//                parameters = method.getParameters()[0];
//            }
//        }
//
//        XRequestOld xRequest = new XRequestOld(new XHeaderOld(ContentType.JSON, "/apiTest", UUID.randomUUID().toString().substring(10)), new byte[10]);
//
//        // when
//        assert parameters != null;
//        boolean support = urlXArgumentResolver.support(parameters);
//
//        Object url = urlXArgumentResolver.convert(parameters, xRequest);
//
//        // then
//        assertThat(support).isTrue();
//        assertThat(url).isEqualTo("/apiTest");
//    }
//
//
//    @DisplayName("파라메터에 url이 없는 경우 지원하지 않는다.")
//    @Test
//    void notSupportType() {
//
//        // given
//        Parameter parameters = null;
//        for (Method method : TestService.class.getDeclaredMethods()) {
//            if (method.getName().equals("functionByUsername")) {
//                parameters = method.getParameters()[0];
//            }
//        }
//
//        // when
//        assert parameters != null;
//        boolean support = urlXArgumentResolver.support(parameters);
//
//        // then
//        assertThat(support).isFalse();
//    }
//
//
//    static abstract class TestService {
//        abstract void functionByUrl(@XParam("url") String url);
//
//        abstract void functionByUsername(@XParam("username") String username);
//    }
//
//}