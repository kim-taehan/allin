---
name: allin-testing
description: allin JUnit5 테스트 작성·실행 가이드 — 단위 테스트(캐시/큐/디스패처/핸들러), 모듈 간 메시징 통합 테스트(order→risk→network), gradle 테스트 실행, ActiveMQ docker 기동. allin에서 "테스트 작성/검증/실행", 동작 확인, 회귀 방지가 필요할 때 반드시 사용.
---

# allin 테스트 가이드

allin-tester가 동작을 JUnit5로 검증하는 절차. 목표는 코드 존재 확인이 아니라 **실행으로 동작을 증명**하는 것. 프레임워크 규약은 `allin-conventions` 스킬 참조.

## 테스트 현황 (기준점)

- `allin-core`: ~26개 테스트 — 캐시(`SingleServerCacheTest`, `XCacheTest`), 큐(`AbstractXBlockingQueueTest`), 디스패처/핸들러(`XHandlerManagerTest`), 메시지(`PropertiesXMessageFinderTest`), 유틸(`ReflectionUtilsTest`, `JsonUtilsTest`). 헬퍼: `SpringBootHelper`, `CoreApplication`(테스트 부트 앱).
- `allin-io`: ~4개. `allin-network`: ~1개.
- **`allin-betting`/`allin-risk`/`allin-simulator`: 0개** — 신규 작성 우선 대상.

기존 테스트 스타일과 헬퍼를 먼저 읽고 재사용한다. 새 패턴을 들이지 않는다.

## 작업 원칙

- **재현 먼저, 통과시키기**: 버그 수정이면 버그를 재현하는 테스트를 먼저 작성. 기능 추가면 요구사항을 검증하는 테스트를 먼저.
- **강한 성공 기준**: 객관적으로 검증 가능한 assertion. "작동하면 됨"이 아니라 "이 입력에 이 출력".
- **실패 투명**: 실패는 출력(스택트레이스 요약)과 함께 보고. 숨기거나 skip하지 않는다.
- **점진적 검증**: 각 모듈/기능 완성 직후 실행(전체 완성 후 1회 아님).

## A. 단위 테스트

1. 대상 클래스의 기존 테스트가 있으면 그 패턴을 따른다.
2. 핸들러 테스트(비즈니스 모듈): `@XController` 핸들러를 직접 호출하거나 `XHandlerManager`/디스패처를 통해 url→핸들러 라우팅을 검증. DTO 역직렬화(`@XModel`), 헤더 주입(`@XParam`/`XHeader`)을 assertion.
3. 동시성 컴포넌트(큐/캐시/executor): `AbstractXBlockingQueueTest` 패턴 — 다중 스레드 produce/consume, graceful shutdown(인터럽트 후 정상 종료) 검증.
4. Spring 컨텍스트가 필요하면 `SpringBootHelper`/`CoreApplication`을 재사용.

## B. 통합 테스트 (모듈 간 메시징)

1. 흐름 정의: 예) order(betting) → risk → network. 각 단계의 입력 XRequest와 기대 결과를 명시.
2. ActiveMQ 필요 시: `cd docker && docker-compose up -d` (Artemis 3노드, test/test). 테스트 전 브로커 연결 확인.
3. Hazelcast 경로(risk)는 임베디드 인스턴스로 검증 가능.
4. 통합 테스트는 사전 조건(docker 기동 여부)을 명시하고, 인프라 미가동 시 명확히 skip 사유를 보고.

## C. 실행 & 보고

```bash
./gradlew :allin-core:test                                  # 모듈 전체
./gradlew :allin-betting:test --tests "*OrderControllerTest"  # 특정 테스트
./gradlew test                                              # 전체
```

- 실행 후: 통과/실패 수, 실패 시 원인 좌표(`path:line`)와 스택트레이스 요약 보고.
- 실패가 코드 버그면 `allin-developer`에, 테스트 자체 문제면 직접 수정 후 재실행.

## 흔한 함정

- 비즈니스 모듈은 컴포넌트 스캔/자동설정이 테스트 컨텍스트에 안 올라와 핸들러 미발견 → 테스트 부트 설정 확인.
- 통합 테스트에서 브로커 미기동 → 연결 타임아웃. 사전 조건 체크 필수.
- 가상 스레드/타이밍 의존 테스트의 flakiness → CountDownLatch/Awaitility로 동기화, 고정 sleep 지양.
