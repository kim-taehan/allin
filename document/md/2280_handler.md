#### 2.2.8 Handler Manager
- 요청받은 XRequest 객체를 어떤 Business model handler 가 처리 할 수 있는지 알려주는 기능
- 현재는 어노테이션 기반으로만 등록이 가능하다 (Spring과 유사한 구조) 

##### 2.2.8.1 XHandlerManager
- @XController, @XMapper 등의 어노테이션 기반으로 handler 를 찾아서 Map 타입에 객체에 가지고 있다.
- (1) bean 생성시 먼저 @XController 로 등록된 business model handler 를 찾아놓는다.
- (2) 특정 URL or 식별가능한 전문 형태를 String 타입의 인자로 받아 그 내용을 처리할 수 있는 handler 를 찾는다.
```java
public class XHandlerManager {

    private final Map<String, XHandler> handlers; 
    
    // (1) bean 생성시 먼저 @XController 로 등록된 business model handler 를 찾아놓는다.
    private HashMap<String, XHandler> initHandlers(ApplicationContext context) {
        HashMap<String, XHandler> tempHandler = new HashMap<>();
        // @XController 정의한 bean
        for (Object bean : context.getBeansWithAnnotation(XController.class).values()) {
            for (Method method : ReflectionUtils.findNoProxyClass(bean).getMethods()) {
                registerHandlerMethod(bean, method, tempHandler);
            }
        }
        return tempHandler;
    }

    // ... 생략
    // (2) 특정 URL or 식별가능한 전문 형태를 String 타입의 인자로 받아 그 내용을 처리할 수 있는 handler 를 찾는다.
    public XHandler findHandler(String url) {
        XHandler xHandler = handlers.get(url);
        if (xHandler == null) {
            throw new IllegalStateException("요청하신 api를 처리할 수 있는 handler가 존재하지 않습니다. url=" + url);
        }
        return xHandler;
    }
}
```

##### 2.2.7.1 XHandler
- XHandler 는 business model 을 처리할 수 있는 메서드 정보를 가지고 있을 수 있는 추상화 인터페이스이다.
- 자바 refection method 호출시 필요한 bean 정보와 Method 정보를 가지고 있다.
- `XHandlerManager` 에서 서버 기동시점에 미리 XHandler 객체를 Map 구조로 가지고 있다.

```java
public interface XHandler {
    Object bean();
    Method method();
}
```


##### 2.2.7.2 business model 과 연계
- 아래 OrderController 가 만약 bean 으로 등록되어 있는 경우 XHandler 역시 3객체가 생성되게 되며, XHandlerManager 객체의 handlers Map에 다음과 같은 key를 가지고 등록된다.
- (1) XHandler(key="/normal-api",   value = (bean=OrderController 가 등록된 bean 객체, method=executeNormalApi 메서드 정보)
- (2) XHandler(key="/advance-api",  value = (bean=OrderController 가 등록된 bean 객체, method=executeAdvanceApi 메서드 정보)
- (3) XHandler(key="/advance-api2", value = (bean=OrderController 가 등록된 bean 객체, method=executeAdvanceApi 메서드 정보)
```java
@XController
public class OrderController {
    @XMapping("/normal-api")
    public void executeNormalApi(@XParam("url") String url) {}

    @XMapping({"/advance-api", "/advance-api2"})
    public void executeAdvanceApi(@XParam("url") String url) {}
    
}
```