### 3.6 Logging framework
- SLF4J(Simple Logging Facade for Java)는 다양한 로깅 프레임 워크에 대한 추상화(인터페이스) 역할을 하는 라이브러리
- Spring 에서 표준으로 사용하고 있는 Logback 을 사용하려고 했으나, Log4j2 가 더 좋은 성능을 가지고 있다고 하여 이를 사용하기로 함.
- Spring 에서 처음 logging 표준을 정할 시점에는 Log4j2 가 나오기 전이었고, 그전 버전인 Log4j 보다는 Logback 의 성능이 좋아 LogBack 이 표준이 됨


#### 3.6.1 로깅 프레임워크 선택 
- Log4j2, LogBack 2가지 로깅 프레임워크 2가지를 고민하였고, 그중 Log4j2 를 선택
- Log4j2 는 Logback 을 보안해서 더 좋은 성능 제공 (멀티 스레드환경에서 비동기 로거, Garbage Free)
- [성능에 대한 자세한 내용은 apache 공식 홈페이지](https://logging.apache.org/log4j/2.x/performance.html)

##### 3.6.2.1 자체 성능 테스트
| sl4j 구현체| 총 가상사용자| TPS| 최고 TPS| 평균 테스트시간(ms)|
| --| --| --| --| --|
| Log4j2|  20개(Ramp-Up 사용)| 238.6| 634.0| 34.42|
| Logback|  20개(Ramp-Up 사용)| 229.6| 594.5| 39.66|
- Log4j2
![3621_log4j2.png](..%2Fimages%2F3621_log4j2.png)
- Logback 
![3621_logback.png](..%2Fimages%2F3621_logback.png)
##### 3.6.2.2 log4j2 garbage free
- GC의 대상이 되는 temporary 성향의 객체들을 최소화하고 생성된 객체들은 재사용하는 방식으로 동작하는 모드
- 2.6 버전부터 Garbage Free가 Default 버전

<br/> 

#### 3.6.2 log4j2 dependency 작용
- spring-logger 에서는 Logback 을 기본 로깅 구현체로 사용하기 있기에 log4j2 를 사용하기 위해서는 기존 dependency 제거후 사용
- log4j2 설정파일을 xml이 아닌 yml 파일로 하기위해 추가로 `com.fasterxml.jackson.dataformat:jackson-dataformat-yaml` dependency 추가


- Logback 의존성 제거
```yaml
configurations { 
    all {
        exclude group: 'org.springframework.boot', module: 'spring-boot-starter-logging'
    }
}
```

- Log4j2 의존성 추가
```yaml
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-log4j2'
    implementation 'com.fasterxml.jackson.dataformat:jackson-dataformat-yaml'
}
```


#### 3.6.3 Log4j2 설정파일
- 프로젝트에서 xml 을 통해서 정의해서 사용하는데 yml 로 정의할 수 있는 방법도 있어 여기서는 이를 사용하도록 함

#### 3.6.4 환경별 로깅 전략
- 각 개발환경을 profile 로 구분하여 logging 남기는 방식을 구분
- local, dev, prd 로 구분 하여 각 환경별로 로깅전략을 다르게 유지한다.

| 환경| log level |로깅 위치| 파일 로깅 방식  |  파일저장 기간|
| --| --|-----------|-----------| --|
| local| debug     |console| N/A       |  N/A|
| dev| debug     |file| 동기 로거     |  7일        |
| prd| info     | file| 비동기 로거 | 30일       |

<br/>

##### 3.6.4.1 local logging 전략
- 개발자 PC 에서 직접 돌아가는 local 서버는 파일로 남기기보다 console 에서 확인하는 것이 도움이 된다.
- 시스템을 개발하는 과정이기 때문에 많은 정보가 있으면 도움될 것이라 생각해 debug로 결정
- Root 를 debug 로 설정하면, Spring 관련 내용도 전부 출력되는 문제가 발생 business 로직만 debug 로 설정

```yaml
Configuration:
  name: Local-Logger
  status: debug

  appenders:
    Console:
      name: Console_Appender
      target: SYSTEM_OUT
      PatternLayout:
        pattern: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{100} - %msg%n"

  Loggers:
    Root:
      level: info
      AppenderRef:
        ref: Console_Appender
    Logger:
      name: developx.betting
      additivity: false
      level: debug
      AppenderRef:
        ref: Console_Appender
```

<br/>

##### 3.6.4.2 dev logging 전략
- 개발서버에서 수행되는 dev 에서는 console 보다는 file 을 통해 확인
- 시스템을 개발하는 과정이기 때문에 많은 정보가 있으면 도움될 것이라 생각해 debug로 결정
- 동기로거로 성능보다 실시간으로 확인할 수 있으며, 파일 저장 기간은 7일

```yaml
Configuration:
    name: Dev-Logger
    status: debug

    Properties:
        Property:
            name: log-dir
            value: "logs"

    Appenders:
        RollingFile:
            name: RollingFile_Appender
            fileName: ${log-dir}/logfile.log
            filePattern: "${log-dir}logfile-%d{yyyy-MM-dd}.%i.txt"
            PatternLayout:
                pattern: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{35} - %msg%n"
            immediateFlush: true # 중요 비동기 처리 false 로 정의 필요함
            Policies:
                SizeBasedTriggeringPolicy:
                    size: "10 MB"
                TimeBasedTriggeringPolicy:
                    Interval: 1
                    modulate: true
            DefaultRollOverStrategy:
                max: 10
                Delete:
                    basePath: "${log-dir}"
                    maxDepth: "1"
                    IfLastModified:
                        age: "P7D"
    Loggers:
        Root:
            level: info
            AppenderRef:
                ref: RollingFile_Appender
        Logger:
            name: developx.betting
            additivity: false
            level: debug
            AppenderRef:
                ref: RollingFile_Appender
```

<br/>

##### 3.6.4.3 prd logging 전략
- 운영서버에서 수행되는 prd 에서는 console 보다는 file 을 통해 확인
- 비동기로거로 성능에 문제가 생기지 안도록 하였고, 파일 저장 기간은 30일
```yaml
Configuration:
    name: Prd-Logger
    status: debug

    Properties:
        Property:
            name: log-dir
            value: "logs"

    Appenders:
        RollingFile:
            name: RollingFile_Appender
            fileName: ${log-dir}/logfile.log
            filePattern: "${log-dir}logfile-%d{yyyy-MM-dd}.%i.txt"
            PatternLayout:
                pattern: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{35} - %msg%n"
            immediateFlush: false # 중요 비동기 처리 false 로 정의 필요함
            Policies:
                SizeBasedTriggeringPolicy:
                    size: "10 MB"
                TimeBasedTriggeringPolicy:
                    Interval: 1
                    modulate: true
            DefaultRollOverStrategy:
                max: 10
                Delete:
                    basePath: "${log-dir}"
                    maxDepth: "1"
                    IfLastModified:
                        age: "P30D"
    Loggers:
        Root:
            level: info
            AppenderRef:
                ref: RollingFile_Appender
        Logger:
            name: developx.betting
            additivity: false
            level: info
            AppenderRef:
                ref: RollingFile_Appender
```