---
name: allin-conventions
description: allin 프레임워크의 핵심 규약 참조 — 메시지 프로토콜(XRequest), 핸들러 어노테이션(@XController/@XMapping/@XModel/@XParam), sealed 인터페이스, 스레드/큐/캐시 수명주기, 자동설정, 모듈 의존성. allin 코드를 작성·리뷰·테스트·분석할 때 반드시 참조할 것.
---

# allin 프레임워크 규약

allin은 **메시지큐 기반 비동기 고성능 처리 프레임워크**다. 비즈니스 개발자가 스레딩/큐/예외처리를 신경 쓰지 않고 비즈니스 모델만 작성하도록, 프레임워크(allin-core/io)가 수신·디스패치·실행·송신을 담당한다. 이 규약을 어기면 핸들러가 발견되지 않거나, graceful shutdown이 깨지거나, 동시성 버그가 생긴다.

## 1. 모듈 구조와 의존성

```
allin-io   (base)   : 프로토콜·데이터 모델 (XRequest, XTarget, XHeader, ModelConverter)
allin-core (kernel) : 수신/디스패치/실행/송신/캐시/예외 (79 클래스)
  ├─ allin-betting  : 비즈니스 예시 (OrderController, ActiveMQ + RMI)
  ├─ allin-risk     : 비즈니스 예시 (RiskController, Hazelcast)
  └─ allin-network  : Netty TCP 수신 + ActiveMQ (allin-io 직접 의존)
allin-simulator     : 부하/테스트 클라이언트 (Spring Web MVC + H2/JPA, 독립)
```

- 의존 방향: io → core → {betting, risk, network}. **역방향 의존 금지.**
- 비즈니스 모듈은 프레임워크 빈을 import하지 않는다 — 자동설정이 발견한다(§6).

## 2. 메시지 프로토콜 (XRequest)

allin-protocol은 HTTP가 아닌 커스텀 header+body 포맷이다.

```
헤더 (key=value, 줄단위):
  url=/order-ll            ← 라우팅 키 (핸들러 매핑 대상)
  transactionId=<UUID>     ← 상관관계 ID
  contentType=JSON         ← 페이로드 타입 (JSON | SERIALIZABLE)
  contentLength=<bytes>
(빈 줄)
본문: JSON 또는 직렬화 바이트
```

- `XRequest`(allin-io `develop.x.io`): 헤더 맵 + `byte[]` 본문, Builder 패턴, `toByte()` 직렬화.
- `XTarget` enum: 송신 목적지(ORDER, RISK …)와 Hazelcast map/queue 이름을 정의. 새 목적지는 여기에 추가.
- `contentType`에 따라 `ModelConverter`(JSON: `JsonModelConverter`, 직렬화: `SerializableModelConverter`)가 본문↔모델 변환.

## 3. 핸들러 작성 규약 (Front Controller)

```java
@XController
public class OrderController {
    @XMapping("/order-ll")                     // url 헤더와 매칭
    public void handle(@XModel OrderLLRequest req,   // 본문 → DTO 역직렬화
                       @XParam("url") String url,     // 헤더값 주입
                       XHeader header) {              // 헤더 객체 주입
        // 비즈니스 로직
    }
}
```

- `@XController` + `@XMapping`이 있어야 `XHandlerManager`가 리플렉션으로 핸들러를 발견한다. 어노테이션 누락 = 핸들러 미등록.
- 인자 resolution은 `XArgumentProvider`가 담당(Spring MVC의 ArgumentResolver와 유사). 지원 어노테이션: `@XModel`(본문), `@XParam`(헤더 단일값), `XHeader`(헤더 전체).
- 디스패치 파이프라인(`AbstractXDispatcher.invoke`): ① byte[]→XRequest ② url로 핸들러 탐색 ③ 인자 resolve ④ `doRun()` 실행(template method). 가상 스레드 변형은 `VirtualThreadDispatcher`.

## 4. Sealed 인터페이스 — permits로만 확장

다음은 sealed이며, 새 구현은 반드시 `permits` 목록에 등록해야 컴파일된다:

| sealed 인터페이스 | 허용된 구현 | 용도 |
|---|---|---|
| `XDispatcher` | `AbstractXDispatcher` → `VirtualThreadDispatcher` 등 | 디스패치 전략 |
| `XCache<K,V>` | `SingleServerReadOnlyCache`, `XWritableCache` | 캐시 |
| `XBlockingQueue<T>` | `AbstractXBlockingQueue` 계열 | 큐 |

permits 미등록 구현은 컴파일 실패한다. sealed 계층을 확장할 땐 상위 인터페이스의 permits도 함께 수정한다.

## 5. 스레드/큐/캐시 수명주기

- **Executor**: `XExecutor`(graceful shutdown 시맨틱) → `AbstractXExecutor` template. 변형: `BusinessXExecutor`, `ReceiverXExecutor`, `BlockingQueueXExecutor`. 풀 크기는 모듈 yml에서 설정.
- **BlockingQueue**: `AbstractXBlockingQueue`(JDK BlockingQueue 래퍼) + `XBlockingQueueRunner`(소비 루프). 소비 루프는 **인터럽트로 graceful 종료**한다 — 인터럽트를 삼키거나 무한 루프로 막지 말 것. 고성능 대안은 **LMAX Disruptor**(`com.conversantmedia:disruptor`).
- **Cache 전략**: `SingleServerCache`(ConcurrentHashMap, 단일 서버) ↔ `MultiServerCache`(Hazelcast, 분산). 전략 교체 시 비즈니스 코드 불변이어야 한다. 분산 캐시는 일관성·직렬화 비용을 고려.
- **Sender**: `XSender.send(XTarget, XRequest)` — 목적지별 송신 스레드 풀(`SenderConfiguration`).
- **부트 수명주기**: `BootConfiguration`이 `ShutdownEventListener`(graceful 종료), `DatabaseWarmup` 등록.

## 6. 자동설정 & 예외 처리

- 프레임워크 빈은 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`로 발견된다. 새 `*Configuration`을 추가하면 이 파일에 등록(또는 컴포넌트 스캔 대상인지 확인).
- 예외: `XExceptionAdvisor` + `CoreXExceptionAdvice`(AOP)가 비즈니스 예외를 가로채 `ExceptionHandlerManager`의 핸들러로 라우팅. **예외를 조용히 삼키지 말 것** — advisor가 처리하도록 전파하거나 명시적 핸들러를 등록.

## 7. 설정 & 실행

- 모듈별 `src/main/resources/application.yml`. 메시지 정의 출처: `allin.message=property`(→ `message.properties`) 또는 `database`(→ `DatabaseXMessageFinder`).
- 진입점: `BettingApplication`, `RiskApplication`, `NetworkApplication`, `SimulatorApplication`.
- 인프라: `cd docker && docker-compose up -d` (ActiveMQ Artemis 3노드, test/test).
- 빌드/실행: `./gradlew :모듈:build`, `./gradlew :모듈:bootRun`. Java 23 toolchain, Java 21+ 가상 스레드.

## 8. 흔한 함정

- `@XController`/`@XMapping` 누락 → 핸들러 미발견(런타임에 url 매칭 실패).
- sealed permits 미등록 → 컴파일 실패.
- BlockingQueue 소비 루프에서 인터럽트 무시 → graceful shutdown 깨짐.
- 가상 스레드에서 락 보유 `synchronized` → 캐리어 스레드 pinning(성능 저하).
- 새 `*Configuration` 자동설정 미등록 → 빈 누락.
- 비즈니스 모듈 → 프레임워크 역의존 또는 모듈 간 직접 의존.
