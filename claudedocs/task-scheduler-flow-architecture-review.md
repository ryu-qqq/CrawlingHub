# 📋 Task/Scheduler 플로우 아키텍처 리뷰 리포트

**작성일**: 2025-11-05
**분석 대상**: EventBridge 트리거 → 서버 수신 → CrawlTask 발행 플로우
**분석 범위**: Application Layer 패키징 컨벤션 (CQRS, Assembler, Manager, Facade, Orchestration)

---

## 🎯 Executive Summary

**분석 범위**: EventBridge 트리거 → 서버 수신 → CrawlTask 발행
**핵심 발견**:
- ✅ Schedule 모듈: 90% 컨벤션 준수 (우수한 Orchestration Pattern 구현)
- ⚠️ Task 모듈: 65% 컨벤션 준수 (패키징 문제, 패턴 누락)
- ❌ **크리티컬 Gap**: adapter-in 레이어 누락 (EventBridge 트리거 수신 불가)

**총 예상 작업 시간**: 26시간
- 🔴 Priority 1 (필수): 22시간
- 🟡 Priority 2 (권장): 4시간

---

## 🔍 1. 전체 플로우 분석

### 1.1 예상 플로우 (As-Designed)

```
AWS EventBridge (스케쥴러)
    ↓ HTTP/SQS Trigger
❌ [MISSING] adapter-in/rest-api 또는 adapter-in/aws-sqs
    ↓
✅ TriggerScheduleService (application/schedule/service)
    ├─ 스케쥴 조회 및 검증
    ├─ Schedule.trigger() 호출
    ├─ 트랜잭션 커밋
    └─ TODO: InitiateCrawlingUseCase 호출 (미구현)
    ↓
⚠️ InitiateCrawlingService (application/task/service)
    ├─ Seller 조회 및 검증
    ├─ CrawlTask 생성 (MINI_SHOP, page=0, size=1)
    ├─ CrawlTask 저장 및 publish()
    └─ Outbox 저장 (트랜잭션 내)
    ↓
❌ [MISSING] TaskOutboxProcessor (application/task/orchestrator)
    ├─ Outbox 폴링 (@Scheduled)
    ├─ SQS 발행 (외부 API, 트랜잭션 외부)
    └─ WAL 상태 업데이트 (SENT/FAILED)
    ↓
AWS SQS (크롤러 태스크 큐)
```

### 1.2 현재 구현 상태

#### ✅ 구현 완료 (Schedule 모듈)

**1. ScheduleCommandFacade**
- 역할: S1 Phase (Accept) - DB 저장 + Outbox 저장 (단일 트랜잭션)
- Idempotency 체크 구현
- 즉시 202 Accepted 반환

**2. ScheduleOutboxProcessor**
- 역할: S2 Phase (Execute) - Outbox 폴링 → EventBridge API 호출
- @Scheduled(fixedDelay = 1000) 사용
- 외부 API 호출은 트랜잭션 밖에서
- ScheduleOutcome (Ok/Fail) 반환

**3. TriggerScheduleService**
- 역할: EventBridge 트리거 수신 후 처리
- ⚠️ Line 84: TODO 주석 - InitiateCrawlingUseCase 미호출

#### ⚠️ 부분 구현 (Task 모듈)

**1. InitiateCrawlingService**
- CrawlTask 생성 및 Outbox 저장
- 트랜잭션 경계 준수 ✅
- **문제**: Outbox를 폴링할 Processor 없음

#### ❌ 미구현 (크리티컬)

**1. adapter-in 레이어**
- REST API Controller 없음
- AWS SQS Consumer 없음
- **영향**: EventBridge가 우리 서버를 호출할 방법이 없음

**2. TaskOutboxProcessor**
- Outbox 폴링 로직 없음
- SQS 발행 로직 없음

---

## 🔍 2. Application Layer 패키징 컨벤션 검증

### 2.1 전체 패키지 구조

#### Schedule 모듈 (✅ 90% 준수)
```
application/schedule/
├── dto/command/        ✅ Command DTOs
├── dto/response/       ✅ Response DTOs
├── facade/             ✅ Facade Pattern
├── orchestrator/       ✅ Orchestration Pattern
├── port/in/            ✅ Input Ports
├── port/out/           ✅ Output Ports
└── service/            ✅ UseCase Implementations
```

#### Task 모듈 (⚠️ 65% 준수)
```
application/task/
├── assembler/command/  ❌ 잘못된 위치 (dto/command/로 이동 필요)
├── command/            ✅ 일부 Command 여기 (올바름)
├── port/in/            ✅ Input Ports
├── port/out/           ✅ Output Ports
├── service/            ✅ UseCase Implementations
├── [MISSING] facade/   ❌ Facade 패턴 미적용
└── [MISSING] orchestrator/ ❌ Orchestration 패턴 미적용
```

---

### 🚨 2.2 패키징 컨벤션 위반 사항

#### ❌ 위반 1: Command 위치 혼재

**문제**:
- `assembler/command/`에 6개 Command 파일 (잘못된 위치)
- `command/`에 1개 Command 파일 (올바른 위치)

**해결책**:
- 모든 Command를 `dto/command/`로 이동
- `assembler/`는 DTO ↔ Domain 변환만

**예상 시간**: 2시간

---

#### ❌ 위반 2: Orchestration Pattern 미적용

**문제**:
- TaskOutboxProcessor 없음
- InitiateCrawlingService가 Outbox 저장해도 발행 불가

**해결책**:
```java
@Component
public class TaskOutboxProcessor {

    @Scheduled(fixedDelay = 1000)
    public void processOutbox() {
        List<CrawlTaskOutbox> pendingOutboxes =
            outboxPort.findByWalStatePending();

        for (CrawlTaskOutbox outbox : pendingOutboxes) {
            processOne(outbox);
        }
    }

    private void processOne(CrawlTaskOutbox outbox) {
        try {
            sqsPublisherPort.publish(outbox.getPayload());
            outboxPort.updateWalState(outbox.getId(), WalState.SENT);
        } catch (Exception e) {
            outboxPort.updateWalState(outbox.getId(), WalState.FAILED);
        }
    }
}
```

**예상 시간**: 12시간

---

#### ❌ 위반 3: Facade 패턴 미적용

**분석**:
- Task 모듈은 현재 단일 UseCase만 존재
- **판단**: Facade 패턴 불필요 (현재는)

**향후 고려**: 여러 UseCase 조합 필요 시

---

### 2.3 CQRS Pattern 평가

**Port 레벨**: ✅ Command/Query 명확히 분리

**Adapter 레벨**: ❌ adapter-in 없음
- EventBridge 트리거 수신 불가
- **영향도**: 🔴 Critical

---

### 2.4 Assembler Pattern 검증

**Schedule 모듈**: ✅ 잘 적용됨

**Task 모듈**: ⚠️ Command와 혼재
- `assembler/command/` 정리 필요

---

### 2.5 Manager Pattern 평가

**분석**: 상태 관리가 단순함 (CREATED → PUBLISHED)

**판단**: Manager 패턴 불필요

---

## 🔍 3. Zero-Tolerance 규칙 준수 여부

### ✅ 준수 항목

1. **Lombok 금지** ✅
   - 모든 Service에서 Pure Java 생성자 사용

2. **Transaction 경계** ✅
   - InitiateCrawlingService: 외부 API 호출 없음
   - ScheduleOutboxProcessor: 외부 API는 트랜잭션 밖

3. **Record Pattern** ✅
   - ProcessTaskCommand: Record + Compact Constructor

4. **Javadoc** ✅
   - 모든 public 클래스/메서드에 포함

### ⚠️ 주의 항목

**TODO 주석 존재** (TriggerScheduleService.java:84)
- 핵심 로직 미구현
- **영향도**: 🔴 Critical

---

## 🎯 4. 우선순위별 개선 계획

### 🔴 Priority 1: Critical Gaps (22시간)

#### Task 1.1: adapter-in 레이어 구현 (8h)

**Option A: REST API Controller (추천)**
```java
@RestController
@RequestMapping("/api/v1/schedules")
public class TriggerScheduleController {

    @PostMapping("/{scheduleId}/trigger")
    public ResponseEntity<Void> trigger(@PathVariable Long scheduleId) {
        TriggerScheduleCommand command =
            new TriggerScheduleCommand(scheduleId);
        triggerScheduleUseCase.execute(command);
        return ResponseEntity.accepted().build();
    }
}
```

**Option B: AWS SQS Consumer**
```java
@Component
public class ScheduleTriggerConsumer {

    @SqsListener("${aws.sqs.schedule-trigger-queue}")
    public void consume(String message) {
        ScheduleTriggerMessage msg =
            objectMapper.readValue(message, ScheduleTriggerMessage.class);
        TriggerScheduleCommand command =
            new TriggerScheduleCommand(msg.scheduleId());
        triggerScheduleUseCase.execute(command);
    }
}
```

**권장**: Option A 우선 구현

---

#### Task 1.2: TriggerScheduleService 연결 (2h)

**Before**:
```java
// TODO: TASK-03 InitiateCrawlingUseCase에서 구현 예정
```

**After**:
```java
InitiateCrawlingCommand crawlingCommand =
    new InitiateCrawlingCommand(schedule.getSellerIdValue());
initiateCrawlingUseCase.execute(crawlingCommand);
```

---

#### Task 1.3: TaskOutboxProcessor 구현 (12h)

**구현 포인트**:
1. @Scheduled(fixedDelay = 1000) 폴링
2. WalState.PENDING Outbox 조회
3. SQS 발행 (트랜잭션 외부)
4. WalState.SENT/FAILED 업데이트
5. TaskOutcome (Ok/Fail) 반환

**추가 작업**:
- SqsPublisherPort 인터페이스 추가
- adapter-out/aws-sqs/ 구현체 추가
- @EnableScheduling 활성화 확인

---

### 🟡 Priority 2: 패키징 컨벤션 (4시간)

#### Task 2.1: Command 재배치 (2h)

**이동 대상** (6개 파일):
- InitiateCrawlingCommand.java
- ProcessCrawlTaskCommand.java
- UpdateTaskStatusCommand.java
- RetryTaskCommand.java
- CompleteTaskCommand.java
- FailTaskCommand.java

**작업**:
1. application/task/dto/command/ 생성
2. 6개 파일 이동
3. Import 경로 업데이트
4. assembler/command/ 삭제

---

#### Task 2.2: assembler 정리 (2h)

**최종 구조**:
```
application/task/
├── assembler/
│   └── TaskAssembler.java  (DTO ↔ Domain만)
└── dto/
    ├── command/
    │   └── (모든 Command 여기)
    └── response/
        └── TaskResponse.java
```

---

### 🟢 Priority 3: 선택적 개선

#### Task 3.1: Facade 패턴 (조건부)
- 현재 불필요
- 향후 복잡도 증가 시 고려

#### Task 3.2: Manager 패턴 (조건부)
- 현재 불필요
- 향후 상태 전이 복잡해질 시 고려

---

## 📊 5. 총 예상 작업 시간

| 우선순위 | Task | 시간 | 상태 |
|---------|------|------|------|
| 🔴 P1 | adapter-in 구현 | 8h | 필수 |
| 🔴 P1 | Service 연결 | 2h | 필수 |
| 🔴 P1 | Processor 구현 | 12h | 필수 |
| 🟡 P2 | Command 재배치 | 2h | 권장 |
| 🟡 P2 | assembler 정리 | 2h | 권장 |
| **합계** | **P1+P2** | **26h** | - |

---

## 🎯 6. 최종 권장 사항

### 단기 (1-2주) - Sprint 1
1. adapter-in 레이어 구현 (8h)
2. TriggerScheduleService 연결 (2h)
3. TaskOutboxProcessor 구현 (12h)

**목표**: 전체 플로우 완성

### 중기 (2-4주) - Sprint 2
4. Command 재배치 (2h)
5. assembler 정리 (2h)

**목표**: 패키징 컨벤션 95%+ 준수

### 장기 (향후)
6. Facade/Manager 패턴 (필요 시)

---

## 📝 7. 결론

### 현재 상태

**Schedule 모듈**: ✅ 90% 컨벤션 준수
- Facade Pattern ✅
- Orchestration Pattern ✅
- Transaction 경계 ✅

**Task 모듈**: ⚠️ 65% 컨벤션 준수
- Command 패키징 혼재 ⚠️
- Orchestration Pattern 미적용 ❌
- Transaction 경계 ✅

**전체 아키텍처**: ❌ 기능 미완성
- adapter-in 누락 ❌
- TaskOutboxProcessor 누락 ❌
- Service 연결 미완성 ❌

---

### 핵심 문제

1. **기능 미완성** (🔴 Critical)
   - EventBridge 트리거 수신 불가
   - CrawlTask SQS 발행 불가
   - Schedule → Task 연결 미완성

2. **패키징 혼란** (🟡 Medium)
   - Command 위치 혼재
   - Assembler 역할 불명확

---

### 우선 조치

**P1 Task (22h)**:
- adapter-in 구현
- Service 연결
- Processor 구현
- **결과**: 전체 플로우 작동

**P2 Task (4h)**:
- Command 재배치
- assembler 정리
- **결과**: 패키징 컨벤션 95%+ 준수

---

### 기대 효과

**기능 완성**:
- EventBridge → 서버 → SQS 발행 완성
- 스케쥴러 기반 자동 크롤링 가능

**코드 품질**:
- Spring Standards 컨벤션 95%+ 준수
- 명확한 패키지 구조
- 향후 확장 가능한 아키텍처

**유지보수성**:
- 신규 개발자 온보딩 용이
- Zero-Tolerance 규칙 준수
- 기술 부채 최소화

---

## 📚 참고 문서

### Spring Standards 컨벤션
- Application Layer 패키징 가이드
- Orchestration Pattern 개요
- CQRS 패턴 가이드
- Assembler 패턴 가이드
- Transaction 경계 관리

### 프로젝트 문서
- Dynamic Hooks 시스템 가이드
- Slash Commands README
- Getting Started 튜토리얼

---

**작성자**: Claude Code (SuperClaude Framework)
**검토 요청**: Backend Team Lead
**다음 액션**: Sprint 1 Planning Meeting 일정 조율
