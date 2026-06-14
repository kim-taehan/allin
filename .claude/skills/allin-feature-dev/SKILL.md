---
name: allin-feature-dev
description: allin 프레임워크에 새 기능을 구현하는 절차 — 비즈니스 핸들러(@XController/@XMapping) 추가, 새 비즈니스 모듈 생성, XRequest 메시지 흐름 구현, XTarget 송신 추가. allin에서 "기능 추가/구현/핸들러/엔드포인트/모듈 만들기"를 할 때 반드시 사용.
---

# allin 기능 개발 절차

allin-developer가 프레임워크 규약을 지키며 기능을 구현하는 단계별 절차. 프레임워크 규약 자체는 `allin-conventions` 스킬 참조.

## 시작 전 확인

1. **분석 선행** — 비슷한 핸들러/모듈이 이미 있는지, 어디를 고쳐야 하는지 불확실하면 `allin-analyst`에게 좌표를 받는다.
2. **변경 범위 최소화** — 프레임워크 커널(allin-core/io)을 고치는 기능인지, 비즈니스 모듈에서 끝나는지 먼저 판단. 대부분은 비즈니스 모듈에서 끝난다.

## A. 비즈니스 핸들러 추가 (가장 흔함)

1. 처리할 url 키 결정 (예: `/order-cancel`).
2. 본문 DTO 작성 — Lombok 사용, 기존 `OrderLLRequest` 스타일을 따른다.
3. 핸들러 작성:
   ```java
   @XController
   public class CancelController {
       @XMapping("/order-cancel")
       public void cancel(@XModel CancelRequest req, XHeader header) { ... }
   }
   ```
4. 컴포넌트 스캔 대상 패키지에 두어 `XHandlerManager`가 발견하도록 한다.
5. url 키를 메시지 정의에 등록(`message.properties` 또는 DB, 모듈의 `allin.message` 설정에 따라).

## B. 모듈 간 송신 추가

1. 목적지가 새로우면 `XTarget` enum에 추가(Hazelcast map/queue 이름 포함).
2. 핸들러/서비스에서 `XSender.send(XTarget.X, xRequest)` 호출.
3. `XRequest`는 Builder로 구성, 헤더에 `url`/`transactionId`/`contentType`/`contentLength`를 채운다.

## C. 새 비즈니스 모듈 생성

1. `settings.gradle`에 `include '모듈명'` 추가.
2. 모듈 `build.gradle`: `implementation project(':allin-core')`, `implementation project(':allin-io')` 의존.
3. `*Application` 진입점(@SpringBootApplication) + `application.yml`(`allin.message`, 필요 시 hazelcast/networks).
4. 핸들러는 A절을 따른다.
5. 새 프레임워크 `*Configuration`을 추가했다면 `AutoConfiguration.imports`에 등록.

## 구현 규칙

- **외과수술식 변경**: 요청된 기능에 필요한 줄만. 인접 코드 리팩토링·포맷 변경 금지.
- **기존 스타일 준수**: Lombok, sealed class, template method, Builder. 새 패턴을 들이지 않는다.
- **동시성 안전**: 핸들러가 공유 상태를 다루면 동시성 컬렉션/불변 객체 사용. 캐시는 `XCache` 전략을 통해 접근.
- **고아 정리**: 내 변경으로 안 쓰이게 된 import/필드만 제거. 기존 죽은 코드는 건드리지 않는다.

## 완료 후

- 변경 파일 목록 + 각 변경 사유를 보고.
- `allin-tester`에 테스트 대상 통지, `allin-reviewer`에 리뷰 요청.
- 컴파일 확인: `./gradlew :모듈:compileJava`.
