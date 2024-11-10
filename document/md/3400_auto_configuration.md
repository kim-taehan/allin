## 3.2 AutoConfiguration(자동설정 기능)
> Spring Boot의 자동 설정 기능은 의존성만 추가가해주면 Spring Boot가 뒤에서 필요한 설정들(Bean 설정 및 생성)을 자동으로 구성한다.  
> Bet 시스템에서는 core 모듈에서 제공하는 기능에 대해 business 에서 자동으로 사용할 수 있게 하기 위해 AutoConfiguration 을 사용한다. 


### AutoConfiguration 사용 이유
- business module 에서는 component scan 으로 bean 을 생성하고 있다. 
- AutoConfiguration 사용하지 않는 경우 core 에서 구현된 class 가 bean 으로 등록되지 않는다. 
- xml 이나 java 코드를 통해 config 설정을 정의해야 된다. 

<br/>

### AutoConfiguration 사용 방법
> 1 `@EnableAutoConfiguration` 추가   
> 2 `org.springframework.boot.autoconfigure.AutoConfiguration.imports` 파일 추가  
> 3 `@AutoConfiguration` 정의   
> 4 `@Conditionalxxx` 를 통해 조건을 정리한다. 

<br/>

#### 1 `@EnableAutoConfiguration` 추가
- AutoConfiguration 기능을 사용하겠다고 spring boot 에게 알려주는 역할
- `@SpringBootApplication` 에 이미 정의된 기능이라 별도로 정의하지 않아도 됨

```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(
    excludeFilters = {@Filter(
    type = FilterType.CUSTOM,
    classes = {TypeExcludeFilter.class}
), @Filter(
    type = FilterType.CUSTOM,
    classes = {AutoConfigurationExcludeFilter.class}
)}
)
public @interface SpringBootApplication {
    // .... 생략
}
```

<br/>

#### 2 `org.springframework.boot.autoconfigure.AutoConfiguration.imports` 파일 추가
- src/resources/META-INF 하위에 위의 파일을 생성한다. 
- 파일에는 `@AutoConfiguration` 기능이 구현된 config class 에 full package + class name 을 입력한다.

```text
develop.x.core.receiver.ReceiverConfiguration

develop.x.core.executor.ExecutorConfiguration

develop.x.core.exception.advice.ExceptionAdviceConfiguration

develop.x.core.blockingqueue.BlockingQueueConfiguration

develop.x.core.dispatcher.DispatcherConfiguration

develop.x.core.boot.BootConfiguration

develop.x.core.message.MessageConfiguration

develop.x.core.sender.SenderConfiguration
```
<br/>

#### 3. `@AutoConfiguration` 정의
- `@Component` 어노테이션으로 bean 으로 등록하는 것이 아니라 `@AutoConfiguration` 정의한 설정파일에서 bean 으로 등록한다.

```java
@AutoConfiguration
public class BlackingQueueConfiguration {

    @Bean
    public BlockingQueueCommand blockingQueueCommand(ApplicationContext context){
        return new SimpleBlockingQueueCommand(context);
    }

    @Bean
    public BlockingQueueRunner blockingQueueRunner(ApplicationContext context, ExecutorService executorService, BlockingQueueCommand command) {
        return new BlockingQueueRunner(context, executorService, command);
    }
}
```
<br/>

#### 4 `@Conditionalxxx` 를 통해 조건을 정리한다.
- 컴포넌트의 Bean 등록여부에 조건을 달 수 있게하는 어노테이션이다.
- `@ConditionalOnMissingClass` 는 동일한 클래스가 bean 으로 등록되지 않은 경우 등록
- `@ConditionalOnProperty` 는 properties 값을 읽어 일치하는 경우 등록

```java
@AutoConfiguration
public class BlackingQueueConfiguration {

    @Bean
    @ConditionalOnMissingClass("BlockingQueueCommand")
    public BlockingQueueCommand blockingQueueCommand(ApplicationContext context){
        return new SimpleBlockingQueueCommand(context);
    }

    @Bean
    @ConditionalOnProperty(name = "config.runner", havingValue = "run")
    public BlockingQueueRunner blockingQueueRunner(ApplicationContext context, ExecutorService executorService, BlockingQueueCommand command) {
        return new BlockingQueueRunner(context, executorService, command);
    }
}

```
<br/>
