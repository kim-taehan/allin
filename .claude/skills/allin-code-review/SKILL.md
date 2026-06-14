---
name: allin-code-review
description: allin 고성능 프레임워크 코드 리뷰 체크리스트 — 동시성/스레드 안전, BlockingQueue·Disruptor·가상 스레드, sealed class, AOP 예외, 프레임워크 규약 위반, 성능. allin 코드를 리뷰/검토/검증하거나 변경분 품질을 점검할 때 반드시 사용.
---

# allin 코드 리뷰 체크리스트

allin은 고성능 동시성 프레임워크다. 일반 리뷰가 놓치는 동시성·자원 수명주기·프레임워크 규약 위반을 적대적으로 찾는다. 규약 자체는 `allin-conventions` 스킬 참조.

## 리뷰 원칙

- **적대적 검증**: 각 발견은 "왜 진짜 문제인가"를 근거 코드(`path:line`)와 시나리오로 증명. 확신 없으면 "불확실(확인 필요)"로 분류.
- **경계면 교차 비교**: 호출부↔구현부, 설정(yml/imports)↔코드, 송신부↔수신부를 함께 읽어 shape/계약 불일치를 본다.
- **삭제 신중**: 자동설정·리플렉션(`@XController` 스캔)으로 쓰이는 코드를 죽은 코드로 오판하지 말 것.

## 1. 동시성 / 스레드 안전 (최우선)

- [ ] 공유 가변 상태가 동시성 컬렉션(ConcurrentHashMap 등)/불변 객체/적절한 동기화로 보호되는가? 비-thread-safe 필드를 멀티스레드 핸들러가 공유하지 않는가?
- [ ] BlockingQueue 소비 루프(`XBlockingQueueRunner`)가 **인터럽트로 graceful 종료**하는가? 인터럽트를 삼키거나(`catch InterruptedException {}`) 무한 루프로 종료를 막지 않는가?
- [ ] Executor가 shutdown 시 정상 종료되는가? 누수되는 스레드 풀/리소스가 없는가?
- [ ] Disruptor 사용: RingBuffer 크기(2의 거듭제곱), EventHandler 예외 처리, 생산/소비 시퀀스 정합성.
- [ ] 가상 스레드 경로(`VirtualThreadDispatcher`)에서 락 보유 `synchronized`/blocking으로 캐리어 pinning이 발생하지 않는가? (→ `ReentrantLock` 권장)
- [ ] double-checked locking, lazy init 등 publish 안전성.

## 2. 정합성 / 버그

- [ ] 경계 조건(빈 본문, null 헤더, contentLength 불일치), 예외 흐름.
- [ ] AOP 예외 advisor(`CoreXExceptionAdvice`)가 예외를 조용히 삼키지 않는가? 비즈니스 예외가 핸들러까지 전파되는가?
- [ ] `XRequest` 헤더(url/transactionId/contentType/contentLength) 일관성 — contentLength와 실제 본문 길이 일치, contentType과 ModelConverter 매칭.

## 3. 프레임워크 규약 준수

- [ ] sealed 인터페이스(`XDispatcher`/`XCache`/`XBlockingQueue`) 새 구현이 permits에 등록되었는가?
- [ ] `@XController`+`@XMapping`이 정확히 있어 핸들러가 발견되는가? url 키가 메시지 정의(`message.properties`/DB)에 있는가?
- [ ] 새 `*Configuration`이 `AutoConfiguration.imports`에 등록(또는 스캔 대상)되었는가?
- [ ] 모듈 의존 방향(io→core→business) 준수, 역방향/모듈 간 직접 의존 없음.

## 4. 성능

- [ ] 핫패스의 불필요한 객체 할당, 박싱, 과도한 락 범위.
- [ ] 캐시 전략 적합성: 단일 서버인데 Hazelcast `MultiServerCache`를 써서 직렬화 비용을 무는가, 또는 분산 필요한데 `SingleServerCache`인가?
- [ ] 스레드 풀 크기(yml)가 워크로드에 맞는가, 큐 backpressure 처리.

## 출력 형식

심각도별로 정리:

```
## Critical
- [path:line] 문제 요약
  근거: <왜 진짜 문제인지, 코드 인용>
  영향/재현: <어떤 상황에서 터지는지>
  수정 제안: <구체적 방향>
  확신도: 높음/중간/낮음(확인 필요)

## High / Medium / Low
...
```

발견이 없으면 "검토 항목 X개, 발견 0건"으로 명시한다. 수정이 필요한 항목은 `allin-developer`에, 테스트로 잡을 항목은 `allin-tester`에 전달한다.
