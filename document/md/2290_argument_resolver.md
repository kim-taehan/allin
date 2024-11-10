#### 2.2.9 argument resolver
- business model handler(method)를 호출하기 위해 필요한 파라메터(argument) 값을 구하는 기능을 제공한다.
- method 의 시그니처를 분석하여 파라메터들이 원하는 값을 Object[] 형태로 반환해서 메서드를 호출할 수 있게 해준다. 

##### 2.2.9.1 XArgumentResolver
- Spring 에 ArgumentResolver 를 대체하는 XArgumentResolver 는 business model handler 를 처리하기 위한 파라메터 변환을 할 수 있게 해주는 인터페이스이다.
- support, convert 2개의 메서드만 존재하며, 이를 구현해서 추가적인 ArgumentResolver 를 만들 수 있다. 
- (1) java.lang.reflect.Parameter 객체를 전달받아 해당 파라매터를 변환할 수 있는지 알려준다.
- (2) XRequest 데이터를 java.lang.reflect.Parameter 객체가 원하는 type으로 변경한다.
```java
public interface XArgumentResolver {
    // (1) java.lang.reflect.Parameter 객체를 전달받아 해당 파라매터를 변환할 수 있는지 알려준다.
    boolean support(Parameter parameter);
    
    // (2) XRequest 데이터를 java.lang.reflect.Parameter 객체가 원하는 type으로 변경한다.
    Object convert(Parameter parameter, XRequest request);
}
```

##### 2.2.9.2 Core 에서 기본으로 제공하는 XArgumentResolver
- Spring 에서 그렇듯 공통적으로 많이 사용하는 type은 기본적으로 제공하고 있다. 

| XArgumentResolver         | 설명                                                  |
|---------------------------|-----------------------------------------------------| 
| UrlXArgumentResolver      | url 또는 전문을 구별할 수 있는 유니크한 식별자                        |
| XRequestXArgumentResolver | XRequest 전체 데이터를 받을 수 있다.                           |
| ModelXArgumentResolver    | XRequest body 데이터를 business 에서 지정한 dto 형태로 변환해준다.   |
| NullableXArgumentResolver | 다른 XArgumentResolver 가 변환할 수 없는 형태인 경우 null 을 반환한다. |


##### 2.2.9.2.1 UrlXArgumentResolver
- url 또는 전문을 구별할 수 있는 유니크한 식별자  
- 조건: Parameter 의 type 이 String 이고, `@XParam` 어노테이션을 가지고 그 value 가 ("url", "apiname") 에 포함되는 경우 
- 반환타입: String

```java
public class UrlXArgumentResolver implements XArgumentResolver {

    private final Set<String> argumentName = Set.of("url", "apiname");

    @Override
    public boolean support(Parameter parameter) {
        if (parameter.getType().equals(String.class) && parameter.getAnnotatedType().isAnnotationPresent(XParam.class)) {
            XParam xParam = parameter.getAnnotatedType().getAnnotation(XParam.class);
            return argumentName.contains(xParam.value());
        }
        return false;
    }

    @Override
    public Object convert(Parameter parameter, XRequest request) {
        return request.getHeaders().get("url");
    }
}
```

##### 2.2.9.2.2 XRequestXArgumentResolver
- XRequest 전체 데이터를 받을 수 있다.
- 조건: Parameter 의 type 이 XRequest 인 경우
- 반환타입: XRequest

```java
public class XRequestXArgumentResolver implements XArgumentResolver {

    @Override
    public boolean support(Parameter parameter) {

        return parameter.getType().equals(XRequest.class);
    }

    @Override
    public Object convert(Parameter parameter, XRequest request) {
        return request;
    }
}
```

##### 2.2.9.2.3 ModelXArgumentResolver    
- XRequest body 데이터를 business 에서 지정한 dto 형태로 변환해준다.
- 조건: Parameter 가 `@XModel` 어노테이션을 가지고 있는 경우
- 반환타입: business model에서 지정한 형태
```java
public class ModelXArgumentResolver implements XArgumentResolver {

    @Override
    public boolean support(Parameter parameter) {
        return parameter.getAnnotatedType().isAnnotationPresent(XModel.class);
    }

    @Override
    public Object convert(Parameter parameter, XRequest request) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(request.getBody(), parameter.getType());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
```

##### 2.2.9.2.4 NullableXArgumentResolver
- 다른 XArgumentResolver 가 변환할 수 없는 형태인 경우 null 을 반환한다.
- 조건: 없음
- 반환타입: null

```java
public class NullableXArgumentResolver implements XArgumentResolver {

    @Override
    public boolean support(Parameter parameter) {
        return false;
    }

    @Override
    public Object convert(Parameter parameter, XRequest request) {
        return null;
    }
}
```

##### 2.2.9.3 business 에서 특정한 형태가 필요한 경우
- core 에서 제공하지 않는 type 의 클래스를 메서드 인자로 변경하고 싶은 경우 `XArgumentResolver` 를 구현하고 이를 bean 으로 등록하면 된다.
- XRequest 를 가지고 만들 수 있는 객체거나 일부 메서드에서 공통으로 사용하고 싶은 경우 등록할 수 있다.

```java
@Component
public class CustomXArgumentResolver implements XArgumentResolver {

    @Override
    public boolean support(Parameter parameter) {
        return parameter.getType().equals(Url.class);
    }

    @Override
    public Object convert(Parameter parameter, XRequest request) {
        return new Url(request.getHeaders().get("url"));
    }
    
    static class Url {
        private final String url;
    }
}
```

##### 2.2.9.3 XArgumentProvider
- dispatcher 에게 XHandler, XRequest 를 받아서 Method 를 호출하기 위한 argument 들을 Object[] 형태로 반환해준다.
- bean 이 생성될 때, XArgumentResolver 를 구현한 bean 객체를 모두 찾아 List 형태로 저장한다.
- dispatcher 에게 요청을 받으면 각 파라메터들은 반복하면서 XArgumentResolver 를 찾아 변환한다.

```java
public class XArgumentProvider {

    private final List<XArgumentResolver> resolvers;
    private final XArgumentResolver defaultResolver = new NullableXArgumentResolver();

    public XArgumentProvider(ApplicationContext context) {
        // (1) XArgumentResolver 를 구현한 bean 객체를 모두 찾아 List 형태로 저장한다.
        Map<String, XArgumentResolver> beansOfType = context.getBeansOfType(XArgumentResolver.class);
        resolvers = beansOfType.values().stream().toList();
    }

    // (2) XHandler, XRequest 를 받아서 Method 를 호출하기 위한 argument 들을 Object[] 형태로 반환해준다.
    public Object[] convertArguments(XHandler handler, XRequest request) {
        Parameter[] parameters = handler.method().getParameters();
        List<Object> result = new ArrayList<>();
        for (Parameter parameter : parameters) {
            XArgumentResolver resolver = findArgumentResolver(parameter);
            result.add(resolver.convert(parameter, request));
        }
        return result.toArray();
    }

    // (3) Parameter 로 변환가능한 XArgumentResolver 를 찾는다.
    private XArgumentResolver findArgumentResolver(Parameter parameter) {
        for (XArgumentResolver resolver : resolvers) {
            if(resolver.support(parameter)){
                return resolver;
            }
        }
        return defaultResolver;
    }
}
```