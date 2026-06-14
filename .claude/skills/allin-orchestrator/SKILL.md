---
name: allin-orchestrator
description: allin 프레임워크 개발 작업을 에이전트 팀으로 조율하는 오케스트레이터. allin에서 기능 개발/구현, 코드 리뷰/품질 점검, 테스트 작성/검증, 리팩토링/분석 작업을 요청할 때 사용. "다시 실행/재실행/업데이트/수정/보완", "이전 결과 기반으로", "X만 다시"같은 후속 요청도 처리. 단순 단발 질문은 직접 응답 가능.
---

# allin 개발 하네스 오케스트레이터

allin 멀티모듈 프레임워크의 개발 작업(기능 개발·리뷰·테스트·분석)을 전문 에이전트 팀으로 조율한다.

**실행 모드: 에이전트 팀(기본) + 단독 호출.** 기능 개발처럼 협업이 필요한 작업은 팀(생성-검증 파이프라인), 단일 성격 요청(리뷰만/분석만)은 해당 전문가만 호출한다.

**모든 Agent 호출은 `model: "opus"`를 명시한다.**

## 팀 구성

| 에이전트 | 타입 | 역할 | 스킬 |
|---|---|---|---|
| `allin-analyst` | Explore | 코드/도메인 분석, 위치 탐색, 영향·성능 분석 (읽기 전용) | allin-conventions |
| `allin-developer` | general-purpose | 기능 구현, 핸들러/모듈 추가 | allin-feature-dev, allin-conventions |
| `allin-reviewer` | general-purpose | 동시성·정합성·성능 적대적 리뷰 | allin-code-review, allin-conventions |
| `allin-tester` | general-purpose | JUnit5 단위/통합 테스트 작성·실행 | allin-testing, allin-conventions |

## Phase 0: 컨텍스트 확인

1. `_workspace/` 존재 여부 확인:
   - 미존재 → **초기 실행**
   - 존재 + 사용자가 부분 수정 요청("리뷰만 다시", "X 보완") → **부분 재실행**(해당 에이전트만 재호출, 이전 산출물 입력)
   - 존재 + 새 입력 → **새 실행**(기존 `_workspace/`를 `_workspace_prev/`로 이동 후 시작)
2. 기존 `.claude/agents/`·`.claude/skills/` 구성이 이 스킬의 팀 표와 일치하는지 빠르게 확인(drift 감지).

## Phase 1: 요청 분류 & 라우팅

요청을 분류하여 실행 경로를 정한다:

| 요청 유형 | 경로 | 모드 |
|---|---|---|
| **기능 개발/구현** | analyst(필요 시) → developer → reviewer → tester | 팀(파이프라인) |
| **코드 리뷰/품질** | reviewer (필요 시 analyst 선행) | 단독 |
| **테스트 작성/검증** | tester (필요 시 analyst 선행) | 단독 |
| **리팩토링/분석** | analyst → (변경 필요 시) developer → reviewer | 팀/단독 |

판단이 모호하면 분류를 사용자에게 확인한다.

## Phase 2: 실행

### 기능 개발 (팀 파이프라인 — 핵심 경로)

1. (필요 시) `allin-analyst`로 구현 위치·영향 분석 → `_workspace/01_analyst_*.md`.
2. `allin-developer`가 구현. analyst 결과를 입력으로 받음. 변경 파일·사유 보고.
3. `allin-reviewer`가 변경분을 적대적 리뷰. 발견 시 developer에 수정 요청(1회 재시도).
4. `allin-tester`가 테스트 작성·실행. **각 모듈 완성 직후 점진 검증.**
5. 리더가 결과 종합 보고: 변경 요약, 리뷰 발견, 테스트 결과.

> 팀 모드 사용 시 `TeamCreate`로 4인 팀 구성, `TaskCreate`로 의존성 있는 작업 할당, 팀원은 `SendMessage`로 자체 조율. 산출물은 파일(`_workspace/`), 조율은 태스크/메시지.

### 단독 호출 (리뷰만 / 테스트만 / 분석만)

해당 에이전트 1명을 `Agent` 도구로 직접 호출(`model: "opus"`), 결과를 사용자에 보고. 팀 통신 오버헤드를 생략.

## Phase 3: 데이터 전달 규칙

- 중간 산출물: `_workspace/{phase}_{agent}_{artifact}.{ext}` (예: `01_analyst_impact.md`, `02_developer_changes.md`, `03_reviewer_findings.md`, `04_tester_results.md`).
- 최종 산출물(코드)은 실제 소스 경로에, 중간 파일은 `_workspace/` 보존(감사 추적).
- 단독 호출은 반환값 기반(+ 대용량은 파일).

## 에러 핸들링

- 에이전트 실패: **1회 재시도**, 재실패 시 해당 결과 없이 진행하고 보고서에 누락을 명시.
- 리뷰-수정 루프: developer↔reviewer 1회 왕복 후에도 미해결이면 미해결 항목을 사용자에 보고(무한 루프 금지).
- 상충 데이터(예: analyst와 reviewer의 판단 불일치): 삭제하지 않고 양쪽 출처 병기.
- 컴파일/테스트 실패: 숨기지 않고 출력과 함께 보고.

## 실행 후 (하네스 진화)

작업 완료 후 사용자에게 개선 기회를 제공한다: "결과나 팀 구성/워크플로우에 바꾸고 싶은 점이 있나요?" 피드백 유형별 반영:
- 결과물 품질 → 해당 스킬 수정 | 에이전트 역할 → 에이전트 `.md` | 워크플로우 순서 → 이 오케스트레이터 | 트리거 누락 → description 확장.
- 모든 변경은 `CLAUDE.md`의 변경 이력 테이블에 기록.

## 테스트 시나리오

**정상 흐름 (기능 개발):** "betting에 주문 취소 핸들러 추가해줘" → analyst가 OrderController 패턴·메시지 정의 위치 분석 → developer가 `@XMapping("/order-cancel")` 핸들러+DTO 작성 → reviewer가 동시성/규약 검토 → tester가 핸들러 단위 테스트 작성·`./gradlew :allin-betting:test` 실행 → 종합 보고.

**에러 흐름 (리뷰 실패):** reviewer가 Critical(인터럽트 삼킴) 발견 → developer에 수정 요청 → developer 1차 수정 → reviewer 재검토 통과. 만약 재검토에도 미해결이면 항목을 사용자에 보고하고 중단.
