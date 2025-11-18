# EVENTBRIDGE-001: EventBridge Domain Layer 구현

**Bounded Context**: Seller
**Sub-Context**: EventBridge (스케줄링)
**Layer**: Domain Layer
**브랜치**: feature/EVENTBRIDGE-001-domain

---

## 📝 목적

EventBridge 스케줄링 관련 비즈니스 핵심 로직을 담당하는 Domain Layer 구현. Seller의 크롤링 스케줄을 AWS EventBridge Rule로 관리하기 위한 도메인 모델입니다.

**핵심 역할**:
- CrawlingSchedule, CrawlingScheduleExecution, SchedulerOutbox Aggregate 구현
- Domain Event 발행 (ScheduleRegistered, ScheduleUpdated, ScheduleDeactivated)
- 스케줄 상태 관리 및 실행 이력 추적
- Outbox 패턴으로 외부 EventBridge API 호출 관리
- **Admin 전용 기능**: ACTIVE Seller만 EventBridge 등록 가능

---

## 🎯 요구사항

### 1. Aggregate: CrawlingSchedule (크롤링 스케줄)

- [ ] **CrawlingSchedule Aggregate 구현**
  - scheduleId (ScheduleId VO, UUID)
  - sellerId (SellerId VO, Long FK)
  - crawlingInterval (crawler.vo.CrawlingInterval VO) ⚠️ **crawler context에서 import**
  - scheduleRule (String, EventBridge Rule Name)
  - scheduleExpression (String, Cron 표현식)
  - status (ScheduleStatus Enum)

- [ ] **비즈니스 규칙**
  - **Admin 수동 등록**: ACTIVE Seller만 EventBridge 등록 가능
  - **초기 상태 ACTIVE**: 등록 시 바로 활성화 (ACTIVE Seller 전제)
  - scheduleRule: `mustit-crawler-{sellerId}` 형식
  - scheduleExpression: `rate({intervalDays} days)` 형식
  - **Seller 비활성화 조건**: 모든 EventBridge 비활성화 필요

- [ ] **Value Objects**
  - ScheduleId: UUID
  - SellerId: Long FK (Seller Aggregate와 분리)
  - CrawlingInterval: `com.ryuqq.crawlinghub.domain.crawler.vo.CrawlingInterval` ⚠️
  - ScheduleStatus: Enum (ACTIVE, INACTIVE, FAILED)

- [ ] **Domain Event 발행**
  - ScheduleRegistered: 스케줄 생성 시
  - ScheduleUpdated: 주기 변경 시
  - ScheduleDeactivated: 비활성화 시

- [ ] **Domain 메서드**
  - `create(sellerId, crawlingInterval)`: 스케줄 생성 (ACTIVE 상태)
  - `updateInterval(newInterval)`: 주기 변경 (ScheduleUpdated 이벤트)
  - `deactivate()`: 비활성화 (ScheduleDeactivated 이벤트)
  - `activate()`: 재활성화

---

### 2. Aggregate: CrawlingScheduleExecution (크롤링 스케줄 실행)

- [ ] **CrawlingScheduleExecution Aggregate 구현**
  - executionId (ExecutionId VO, UUID)
  - scheduleId (ScheduleId VO)
  - sellerId (SellerId VO, Long FK)
  - status (ExecutionStatus Enum)
  - totalTasksCreated (Integer)
  - completedTasks (Integer)
  - failedTasks (Integer)
  - progressRate (Double, 계산 필드)
  - successRate (Double, 계산 필드)
  - startedAt (LocalDateTime)
  - completedAt (LocalDateTime, Nullable)
  - errorMessage (String, Nullable)

- [ ] **비즈니스 규칙**
  - EventBridge 트리거 시 자동 생성
  - 초기 상태 STARTED
  - 진행률 = completedTasks / totalTasksCreated * 100
  - 성공률 = (completedTasks - failedTasks) / completedTasks * 100

- [ ] **상태 전환 로직**
  - STARTED → IN_PROGRESS → COMPLETED/FAILED
  - 모든 태스크 완료 시 COMPLETED
  - 크롤링 중 에러 시 FAILED

- [ ] **Value Objects**
  - ExecutionId: UUID
  - ExecutionStatus: Enum (STARTED, IN_PROGRESS, COMPLETED, FAILED)

- [ ] **Domain 메서드** (Tell Don't Ask)
  - `start()`: 실행 시작 (STARTED)
  - `markInProgress(totalTasksCreated)`: 진행 중 전환
  - `updateProgress(completedCount, failedCount)`: 진행 상황 업데이트
  - `complete()`: 실행 완료 (COMPLETED)
  - `fail(errorMessage)`: 실행 실패 (FAILED)
  - `calculateProgressRate()`: 진행률 계산 (내부 메서드)
  - `calculateSuccessRate()`: 성공률 계산 (내부 메서드)

---

### 3. Aggregate: SchedulerOutbox (스케줄러 외부 전송)

- [ ] **SchedulerOutbox Aggregate 구현**
  - outboxId (SchedulerOutboxId VO, UUID)
  - scheduleId (ScheduleId VO)
  - eventType (SchedulerEventType Enum)
  - payload (String, JSON)
  - status (OutboxStatus Enum)
  - retryCount (Integer)
  - errorMessage (String, Nullable)
  - sentAt (LocalDateTime, Nullable)

- [ ] **비즈니스 규칙**
  - CrawlingSchedule Domain Event 발행 시 자동 생성
  - 초기 상태 WAITING
  - EventBridge API 호출은 트랜잭션 밖
  - 재시도 최대 5회 (Exponential Backoff)

- [ ] **상태 전환 로직**
  - WAITING → SENDING → COMPLETED/FAILED
  - 재시도 5회 초과 시 FAILED

- [ ] **Value Objects**
  - SchedulerOutboxId: UUID
  - SchedulerEventType: Enum (SCHEDULE_CREATED, SCHEDULE_UPDATED, SCHEDULE_DELETED)
  - OutboxStatus: Enum (WAITING, SENDING, COMPLETED, FAILED)

- [ ] **Payload 예시** (JSON)
  ```json
  {
    "ruleName": "mustit-crawler-seller_12345",
    "scheduleExpression": "rate(1 day)",
    "targetArn": "arn:aws:execute-api:...",
    "input": "{\"sellerId\":\"seller_12345\"}"
  }
  ```

- [ ] **Domain 메서드**
  - `create(scheduleId, eventType, payload)`: Outbox 생성
  - `send()`: 전송 중 상태로 전환
  - `complete()`: 전송 완료
  - `fail(errorMessage)`: 전송 실패
  - `canRetry()`: 재시도 가능 여부 확인 (retryCount < 5)

---

## ⚠️ 제약사항

### Zero-Tolerance 규칙

- [ ] **Lombok 금지**: Pure Java 또는 Record 사용
  - ✅ `public record ScheduleId(String value) {}`
  - ❌ `@Value public class ScheduleId { ... }`

- [ ] **Law of Demeter 준수**: Getter 체이닝 금지
  - ✅ `schedule.getScheduleExpression()`
  - ❌ `schedule.getCrawlingInterval().getDays()`

- [ ] **Tell Don't Ask**: 내부 상태 기반 판단
  - ✅ `execution.updateProgress(completed, failed)` (내부에서 계산)
  - ❌ `if (execution.getStatus() == STARTED) { ... }` (외부에서 판단)

- [ ] **Long FK 전략**: 관계 어노테이션 금지
  - ✅ `private SellerId sellerId;` (Long 값으로 참조)
  - ❌ `@ManyToOne private Seller seller;`

### 테스트 규칙

- [ ] **ArchUnit 테스트 필수**
  - Lombok 사용 금지 검증
  - Getter 체이닝 금지 검증
  - 패키지 의존성 검증

- [ ] **TestFixture 패턴 사용**
  - Domain 객체 생성을 위한 Fixture 클래스
  - 테스트 가독성 향상

- [ ] **테스트 커버리지 > 80%**
  - Domain 메서드 모두 테스트
  - 비즈니스 규칙 검증

---

## ✅ 완료 조건

- [ ] 3개 Aggregate 구현 완료 (CrawlingSchedule, CrawlingScheduleExecution, SchedulerOutbox)
- [ ] 모든 Value Object 구현 완료
- [ ] 모든 Enum 구현 완료
- [ ] Domain Event 구현 완료 (ScheduleRegistered, ScheduleUpdated, ScheduleDeactivated)
- [ ] 모든 Domain 메서드 구현 완료
- [ ] Unit Test 작성 완료 (커버리지 > 80%)
- [ ] ArchUnit 테스트 통과
- [ ] TestFixture 패턴 적용
- [ ] Zero-Tolerance 규칙 준수

---

## 🔗 관련 문서

- **Plan**: docs/prd/seller/eventbridge/plans/EVENTBRIDGE-001-domain-plan.md

---

## 📚 참고사항

### CrawlingSchedule Aggregate 구현 예시

```java
import com.ryuqq.crawlinghub.domain.crawler.vo.CrawlingInterval; // ⚠️ crawler context

public class CrawlingSchedule {
    private final ScheduleId scheduleId;
    private final SellerId sellerId; // Long FK
    private CrawlingInterval crawlingInterval; // crawler.vo
    private String scheduleRule;
    private String scheduleExpression;
    private ScheduleStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Factory Method
    public static CrawlingSchedule create(SellerId sellerId, CrawlingInterval interval) {
        ScheduleId scheduleId = new ScheduleId(UUID.randomUUID().toString());
        String scheduleRule = "mustit-crawler-" + sellerId.value();
        String scheduleExpression = buildScheduleExpression(interval);

        CrawlingSchedule schedule = new CrawlingSchedule(
            scheduleId,
            sellerId,
            interval,
            scheduleRule,
            scheduleExpression,
            ScheduleStatus.ACTIVE, // ACTIVE Seller만 등록 가능하므로 바로 ACTIVE
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        // Domain Event 발행
        schedule.registerEvent(new ScheduleRegistered(
            scheduleId,
            sellerId,
            interval,
            scheduleRule
        ));

        return schedule;
    }

    // 주기 변경
    public void updateInterval(CrawlingInterval newInterval) {
        validateInterval(newInterval);
        this.crawlingInterval = newInterval;
        this.scheduleExpression = buildScheduleExpression(newInterval);
        this.updatedAt = LocalDateTime.now();

        // Domain Event 발행
        registerEvent(new ScheduleUpdated(
            this.scheduleId,
            this.sellerId,
            newInterval
        ));
    }

    // 비활성화
    public void deactivate() {
        if (this.status == ScheduleStatus.INACTIVE) {
            throw new ScheduleInvalidStateException("Schedule is already inactive");
        }
        this.status = ScheduleStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now();

        // Domain Event 발행
        registerEvent(new ScheduleDeactivated(
            this.scheduleId,
            this.sellerId
        ));
    }

    // 활성화
    public void activate() {
        if (this.status == ScheduleStatus.ACTIVE) {
            throw new ScheduleInvalidStateException("Schedule is already active");
        }
        this.status = ScheduleStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    // Cron 표현식 생성 (내부 메서드)
    private static String buildScheduleExpression(CrawlingInterval interval) {
        return "rate(" + interval.days() + " days)";
    }

    // Getter (Law of Demeter 준수)
    public String getScheduleExpression() {
        return this.scheduleExpression;
    }

    public boolean isActive() {
        return this.status == ScheduleStatus.ACTIVE;
    }
}
```

### CrawlingScheduleExecution Aggregate 구현 예시

```java
public class CrawlingScheduleExecution {
    private final ExecutionId executionId;
    private final ScheduleId scheduleId;
    private final SellerId sellerId; // Long FK
    private ExecutionStatus status;
    private Integer totalTasksCreated;
    private Integer completedTasks;
    private Integer failedTasks;
    private final LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String errorMessage;

    // Factory Method
    public static CrawlingScheduleExecution start(ScheduleId scheduleId, SellerId sellerId) {
        return new CrawlingScheduleExecution(
            new ExecutionId(UUID.randomUUID().toString()),
            scheduleId,
            sellerId,
            ExecutionStatus.STARTED,
            0,
            0,
            0,
            LocalDateTime.now(),
            null,
            null
        );
    }

    // Tell Don't Ask 패턴
    public void markInProgress(int totalTasksCreated) {
        if (this.status != ExecutionStatus.STARTED) {
            throw new ExecutionInvalidStateException("Cannot mark in progress from " + this.status);
        }
        this.status = ExecutionStatus.IN_PROGRESS;
        this.totalTasksCreated = totalTasksCreated;
    }

    public void updateProgress(int completedCount, int failedCount) {
        this.completedTasks = completedCount;
        this.failedTasks = failedCount;
    }

    public void complete() {
        this.status = ExecutionStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void fail(String errorMessage) {
        this.status = ExecutionStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
    }

    // 진행률 계산 (내부)
    public double calculateProgressRate() {
        if (totalTasksCreated == 0) return 0.0;
        return (double) completedTasks / totalTasksCreated * 100;
    }

    // 성공률 계산 (내부)
    public double calculateSuccessRate() {
        if (completedTasks == 0) return 0.0;
        return (double) (completedTasks - failedTasks) / completedTasks * 100;
    }
}
```

### SchedulerOutbox Aggregate 구현 예시

```java
public class SchedulerOutbox {
    private final SchedulerOutboxId outboxId;
    private final ScheduleId scheduleId;
    private final SchedulerEventType eventType;
    private final String payload; // JSON
    private OutboxStatus status;
    private Integer retryCount;
    private String errorMessage;
    private LocalDateTime sentAt;

    // Factory Method
    public static SchedulerOutbox create(
        ScheduleId scheduleId,
        SchedulerEventType eventType,
        String payload
    ) {
        return new SchedulerOutbox(
            new SchedulerOutboxId(UUID.randomUUID().toString()),
            scheduleId,
            eventType,
            payload,
            OutboxStatus.WAITING,
            0,
            null,
            null
        );
    }

    public void send() {
        if (this.status != OutboxStatus.WAITING) {
            throw new OutboxInvalidStateException("Cannot send from " + this.status);
        }
        this.status = OutboxStatus.SENDING;
    }

    public void complete() {
        this.status = OutboxStatus.COMPLETED;
        this.sentAt = LocalDateTime.now();
    }

    public void fail(String errorMessage) {
        this.retryCount++;
        this.errorMessage = errorMessage;

        if (canRetry()) {
            this.status = OutboxStatus.WAITING; // 재시도 대기
        } else {
            this.status = OutboxStatus.FAILED; // 최종 실패
        }
    }

    public boolean canRetry() {
        return this.retryCount < 5;
    }
}
```

### Domain Event 발행 예시

```java
public class CrawlingSchedule {
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    public void updateInterval(CrawlingInterval newInterval) {
        this.crawlingInterval = newInterval;
        this.scheduleExpression = buildScheduleExpression(newInterval);

        // Domain Event 발행
        registerEvent(new ScheduleUpdated(
            this.scheduleId,
            this.sellerId,
            newInterval
        ));
    }

    private void registerEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
}
```

### Outbox 패턴

- **목적**: 외부 API 호출 (EventBridge)을 트랜잭션 밖에서 안전하게 처리
- **흐름**:
  1. Domain Event 발행 → SchedulerOutbox 생성 (트랜잭션 내)
  2. 배치 처리 → EventBridge API 호출 (트랜잭션 밖)
  3. 성공/실패에 따라 Outbox 상태 업데이트

### 중요 변경사항

⚠️ **CrawlingInterval 위치 변경**:
- 기존: `com.ryuqq.crawlinghub.domain.seller.vo.CrawlingInterval`
- 신규: `com.ryuqq.crawlinghub.domain.crawler.vo.CrawlingInterval`
- 이유: EventBridge는 AWS 크론 스케줄링에 사용되므로 Crawler 컨텍스트의 VO 사용

⚠️ **Seller 등록 조건 변경**:
- 기존: Seller 등록 시 자동 생성 (1:1 관계)
- 신규: Admin이 ACTIVE Seller에만 수동 등록
- 이유: Seller와 EventBridge는 별도로 관리

⚠️ **Seller 비활성화 조건**:
- 모든 EventBridge가 비활성화되어야 Seller 비활성화 가능
- Application Layer에서 CrawlingScheduleQueryPort로 검증

⚠️ **Long FK 전략**:
- SellerId는 Long 값으로 참조
- Seller Aggregate와 완전 분리
