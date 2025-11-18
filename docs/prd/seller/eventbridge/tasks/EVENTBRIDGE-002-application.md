# EVENTBRIDGE-002: EventBridge Application Layer 구현

**Bounded Context**: Seller
**Sub-Context**: EventBridge (스케줄링)
**Layer**: Application Layer
**브랜치**: feature/EVENTBRIDGE-002-application

---

## 📝 목적

EventBridge 스케줄링 관련 Application Layer 구현. Admin 전용 기능으로, ACTIVE Seller에만 EventBridge를 등록/수정할 수 있도록 구현합니다.

**핵심 역할**:
- **Admin UseCase 구현** (Register, Update, Activate, Deactivate)
- **ACTIVE Seller 검증** (INACTIVE Seller는 EventBridge 등록 불가)
- Domain Event Handler 구현 (ScheduleRegistered, ScheduleUpdated, ScheduleDeactivated)
- Outbox 배치 처리 (SchedulerOutbox → EventBridge API 호출)
- Transaction 경계 관리 (DB 저장 → 외부 API 호출 분리)

---

## 🎯 요구사항

### 1. Command Use Cases (Admin 전용)

#### RegisterScheduleUseCase

- [ ] **Input**: RegisterScheduleCommand (sellerId, intervalDays)
- [ ] **Output**: ScheduleId
- [ ] **비즈니스 로직**
  1. **Seller 조회** (SellerQueryPort)
  2. **Seller ACTIVE 검증** (INACTIVE → 예외 발생)
  3. CrawlingSchedule 생성 (sellerId, CrawlingInterval)
  4. DB 저장
  5. Domain Event 발행 (ScheduleRegistered)

- [ ] **Transaction 경계**: Yes

#### UpdateScheduleIntervalUseCase

- [ ] **Input**: UpdateScheduleIntervalCommand (scheduleId, newIntervalDays)
- [ ] **Output**: void
- [ ] **비즈니스 로직**
  1. CrawlingSchedule 조회
  2. Interval 업데이트
  3. DB 저장
  4. Domain Event 발행 (ScheduleUpdated)

- [ ] **Transaction 경계**: Yes

#### ActivateScheduleUseCase

- [ ] **Input**: ActivateScheduleCommand (scheduleId)
- [ ] **Output**: void
- [ ] **비즈니스 로직**
  1. CrawlingSchedule 조회
  2. 활성화
  3. DB 저장

- [ ] **Transaction 경계**: Yes

#### DeactivateScheduleUseCase

- [ ] **Input**: DeactivateScheduleCommand (scheduleId)
- [ ] **Output**: void
- [ ] **비즈니스 로직**
  1. CrawlingSchedule 조회
  2. 비활성화
  3. DB 저장
  4. Domain Event 발행 (ScheduleDeactivated)

- [ ] **Transaction 경계**: Yes

---

### 2. Domain Event Handler

#### ScheduleRegisteredEventHandler

- [ ] **Input**: ScheduleRegistered 이벤트
- [ ] **비즈니스 로직**
  1. CrawlingSchedule 조회
  2. SchedulerOutbox 생성 (eventType: SCHEDULE_CREATED)
  3. Payload 구성:
     - ruleName: `mustit-crawler-{sellerId}`
     - scheduleExpression: `rate({intervalDays} days)`
     - targetArn: API Gateway ARN
     - input: `{"sellerId": "..."}`
  4. DB 저장 (트랜잭션 내)

- [ ] **Transaction 경계**: Yes (Outbox 생성까지만)

#### ScheduleUpdatedEventHandler

- [ ] **Input**: ScheduleUpdated 이벤트
- [ ] **비즈니스 로직**
  1. CrawlingSchedule 조회
  2. SchedulerOutbox 생성 (eventType: SCHEDULE_UPDATED)
  3. 새 scheduleExpression으로 Payload 구성
  4. DB 저장 (트랜잭션 내)

- [ ] **Transaction 경계**: Yes (Outbox 생성까지만)

#### ScheduleDeactivatedEventHandler

- [ ] **Input**: ScheduleDeactivated 이벤트
- [ ] **비즈니스 로직**
  1. SchedulerOutbox 생성 (eventType: SCHEDULE_DELETED)
  2. ruleName만 포함한 Payload 구성
  3. DB 저장 (트랜잭션 내)

- [ ] **Transaction 경계**: Yes (Outbox 생성까지만)

---

### 3. Outbox 배치 처리

#### ProcessSchedulerOutboxUseCase

- [ ] **Input/Output 정의**
  - Input: (없음, Scheduled 실행)
  - Output: OutboxProcessedResult (successCount, failedCount)

- [ ] **비즈니스 로직**
  1. WAITING 상태 Outbox 조회 (Batch, 최대 100개)
  2. 상태 SENDING으로 변경
  3. DB 저장
  4. **트랜잭션 커밋**
  5. EventBridge API 호출 (트랜잭션 밖)
     - SCHEDULE_CREATED → createRule()
     - SCHEDULE_UPDATED → updateRule()
     - SCHEDULE_DELETED → deleteRule()
  6. 트랜잭션 시작
  7. 결과에 따라 상태 변경
     - 성공: COMPLETED, sentAt 기록
     - 실패: retryCount 증가, WAITING 또는 FAILED
  8. DB 저장
  9. **트랜잭션 커밋**

- [ ] **Transaction 경계**: 2단계 (상태 업데이트 → 외부 호출 → 결과 저장)
- [ ] **스케줄링**: @Scheduled(fixedDelay = 300000) // 5분마다

---

### 4. Port 정의

#### Output Ports (Infrastructure 인터페이스)

- [ ] **Persistence Ports (Port-Out)**
  - `SellerQueryPort`: findBySellerId() ⚠️ **Seller Context와 연동**
  - `CrawlingSchedulePersistencePort`: save() // Command Port
  - `CrawlingScheduleQueryPort`: findById(), findBySellerId(), existsActiveBySellerId() // Query Port
  - `CrawlingScheduleExecutionPersistencePort`: save() // Command Port
  - `CrawlingScheduleExecutionQueryPort`: findByScheduleId(), findByStatus() // Query Port
  - `SchedulerOutboxPersistencePort`: save() // Command Port
  - `SchedulerOutboxQueryPort`: findByStatusOrderByCreatedAtAsc() // Query Port

- [ ] **Infrastructure Ports**
  - `EventBridgePort`: createRule(), updateRule(), deleteRule()

---

## ⚠️ 제약사항

### Zero-Tolerance 규칙

- [ ] **Transaction 경계 엄격 관리**
  - ✅ DB 저장/수정: 트랜잭션 내
  - ❌ EventBridge API 호출: 트랜잭션 밖 (절대 금지!)

- [ ] **ACTIVE Seller 검증 필수**
  - ✅ RegisterScheduleUseCase에서 Seller 상태 확인
  - ❌ INACTIVE Seller는 EventBridge 등록 불가

- [ ] **Domain Event는 @EventListener로 수신**
  - Spring Application Event 사용
  - 비동기 처리 가능

- [ ] **Outbox 패턴 필수**
  - Domain Event → Outbox 생성 (트랜잭션 내)
  - 배치 처리 → 외부 API 호출 (트랜잭션 밖)

### 테스트 규칙

- [ ] **Mock 테스트**
  - Port는 Mock 객체 사용
  - Domain Event는 직접 발행하여 Handler 테스트

- [ ] **테스트 커버리지 > 80%**
  - Command UseCase 모두 테스트
  - Event Handler 모두 테스트
  - Outbox 배치 처리 성공/실패 시나리오 테스트

---

## ✅ 완료 조건

- [ ] 4개 Command UseCase 구현 완료
- [ ] 3개 Domain Event Handler 구현 완료
- [ ] ProcessSchedulerOutboxUseCase 구현 완료
- [ ] 모든 Port 인터페이스 정의 완료
- [ ] Unit Test 작성 완료 (Mock, 커버리지 > 80%)
- [ ] Transaction 경계 검증 완료
- [ ] Zero-Tolerance 규칙 준수

---

## 🔗 관련 문서

- **Plan**: docs/prd/seller/eventbridge/plans/EVENTBRIDGE-002-application-plan.md

---

## 📚 참고사항

### RegisterScheduleUseCase 구현 예시

#### Port-In Interface (UseCase)

```java
public interface RegisterScheduleUseCase {
    ScheduleId execute(RegisterScheduleCommand command);
}
```

#### Service Implementation

```java
@Service
@RequiredArgsConstructor
public class RegisterScheduleService implements RegisterScheduleUseCase {
    private final SellerQueryPort sellerQueryPort; // Seller Context 연동
    private final CrawlingSchedulePersistencePort schedulePersistencePort; // Port-Out 네이밍 수정

    @Transactional
    @Override
    public ScheduleId execute(RegisterScheduleCommand command) {
        // 1. Seller 조회
        Seller seller = sellerQueryPort.findBySellerId(new SellerId(command.sellerId()))
            .orElseThrow(() -> new SellerNotFoundException("Seller not found: " + command.sellerId()));

        // 2. Seller ACTIVE 검증
        if (!seller.isActive()) {
            throw new SellerNotActiveException("Cannot register schedule for inactive seller: " + command.sellerId());
        }

        // 3. CrawlingSchedule 생성
        CrawlingInterval interval = new CrawlingInterval(command.intervalDays());
        CrawlingSchedule schedule = CrawlingSchedule.create(
            new SellerId(command.sellerId()),
            interval
        );

        // 4. DB 저장
        schedulePersistencePort.save(schedule);

        // 5. Domain Event 발행 (자동)
        // schedule.registerEvent(new ScheduleRegistered(...))

        return schedule.getScheduleId();
    }
}

public record RegisterScheduleCommand(
    String sellerId,
    Integer intervalDays
) {}
```

### UpdateScheduleIntervalUseCase 구현 예시

#### Port-In Interface

```java
public interface UpdateScheduleIntervalUseCase {
    void execute(UpdateScheduleIntervalCommand command);
}
```

#### Service Implementation

```java
@Service
@RequiredArgsConstructor
public class UpdateScheduleIntervalService implements UpdateScheduleIntervalUseCase {
    private final CrawlingScheduleQueryPort scheduleQueryPort;
    private final CrawlingSchedulePersistencePort schedulePersistencePort;

    @Transactional
    public void execute(UpdateScheduleIntervalCommand command) {
        // 1. CrawlingSchedule 조회
        CrawlingSchedule schedule = scheduleQueryPort.findById(command.scheduleId())
            .orElseThrow(() -> new ScheduleNotFoundException("Schedule not found: " + command.scheduleId()));

        // 2. Interval 업데이트
        CrawlingInterval newInterval = new CrawlingInterval(command.newIntervalDays());
        schedule.updateInterval(newInterval);

        // 3. DB 저장
        schedulePersistencePort.save(schedule);

        // 4. Domain Event 발행 (자동)
        // schedule.registerEvent(new ScheduleUpdated(...))
    }
}

public record UpdateScheduleIntervalCommand(
    String scheduleId,
    Integer newIntervalDays
) {}
```

### ActivateScheduleUseCase 구현 예시

```java
@UseCase
@RequiredArgsConstructor
public class ActivateScheduleUseCase {
    private final CrawlingScheduleQueryPort scheduleQueryPort;
    private final CrawlingScheduleCommandPort scheduleCommandPort;

    @Transactional
    public void execute(ActivateScheduleCommand command) {
        // 1. CrawlingSchedule 조회
        CrawlingSchedule schedule = scheduleQueryPort.findById(command.scheduleId())
            .orElseThrow(() -> new ScheduleNotFoundException("Schedule not found: " + command.scheduleId()));

        // 2. 활성화
        schedule.activate();

        // 3. DB 저장
        schedulePersistencePort.save(schedule);
    }
}

public record ActivateScheduleCommand(
    String scheduleId
) {}
```

### DeactivateScheduleUseCase 구현 예시

```java
@UseCase
@RequiredArgsConstructor
public class DeactivateScheduleUseCase {
    private final CrawlingScheduleQueryPort scheduleQueryPort;
    private final CrawlingScheduleCommandPort scheduleCommandPort;

    @Transactional
    public void execute(DeactivateScheduleCommand command) {
        // 1. CrawlingSchedule 조회
        CrawlingSchedule schedule = scheduleQueryPort.findById(command.scheduleId())
            .orElseThrow(() -> new ScheduleNotFoundException("Schedule not found: " + command.scheduleId()));

        // 2. 비활성화
        schedule.deactivate();

        // 3. DB 저장
        schedulePersistencePort.save(schedule);

        // 4. Domain Event 발행 (자동)
        // schedule.registerEvent(new ScheduleDeactivated(...))
    }
}

public record DeactivateScheduleCommand(
    String scheduleId
) {}
```

### Domain Event Handler 예시

```java
@Component
@RequiredArgsConstructor
public class ScheduleRegisteredEventHandler {

    private final CrawlingScheduleQueryPort scheduleQueryPort;
    private final SchedulerOutboxCommandPort outboxCommandPort;

    @EventListener
    @Transactional
    public void handle(ScheduleRegistered event) {
        // 1. Schedule 조회
        CrawlingSchedule schedule = scheduleQueryPort.findById(event.getScheduleId())
            .orElseThrow(() -> new ScheduleNotFoundException());

        // 2. Payload 구성
        String payload = buildCreateRulePayload(schedule);

        // 3. Outbox 생성
        SchedulerOutbox outbox = SchedulerOutbox.create(
            schedule.getScheduleId(),
            SchedulerEventType.SCHEDULE_CREATED,
            payload
        );

        // 4. DB 저장
        outboxCommandPort.save(outbox);
    }

    private String buildCreateRulePayload(CrawlingSchedule schedule) {
        return """
            {
              "ruleName": "mustit-crawler-%s",
              "scheduleExpression": "%s",
              "targetArn": "%s",
              "input": "{\\"sellerId\\": \\"%s\\"}"
            }
            """.formatted(
                schedule.getSellerId().value(),
                schedule.getScheduleExpression(),
                targetArn,
                schedule.getSellerId().value()
            );
    }
}
```

### Outbox 배치 처리 예시

```java
@Service
@RequiredArgsConstructor
public class ProcessSchedulerOutboxService {

    private final SchedulerOutboxQueryPort outboxQueryPort;
    private final SchedulerOutboxCommandPort outboxCommandPort;
    private final EventBridgePort eventBridgePort;

    @Scheduled(fixedDelay = 300000) // 5분마다
    public OutboxProcessedResult execute() {
        // 1. WAITING 상태 Outbox 조회
        List<SchedulerOutbox> outboxes = outboxQueryPort.findByStatusOrderByCreatedAtAsc(
            OutboxStatus.WAITING,
            PageRequest.of(0, 100)
        );

        int successCount = 0;
        int failedCount = 0;

        for (SchedulerOutbox outbox : outboxes) {
            // 2. 상태 SENDING으로 변경 (트랜잭션)
            updateToSending(outbox);

            // 3. 외부 API 호출 (트랜잭션 밖)
            try {
                callEventBridge(outbox);
                // 4. 성공 (트랜잭션)
                updateToCompleted(outbox);
                successCount++;
            } catch (Exception e) {
                // 5. 실패 (트랜잭션)
                updateToFailed(outbox, e.getMessage());
                failedCount++;
            }
        }

        return new OutboxProcessedResult(successCount, failedCount);
    }

    @Transactional
    private void updateToSending(SchedulerOutbox outbox) {
        outbox.send();
        outboxCommandPort.save(outbox);
    }

    private void callEventBridge(SchedulerOutbox outbox) {
        switch (outbox.getEventType()) {
            case SCHEDULE_CREATED -> eventBridgePort.createRule(outbox.getPayload());
            case SCHEDULE_UPDATED -> eventBridgePort.updateRule(outbox.getPayload());
            case SCHEDULE_DELETED -> eventBridgePort.deleteRule(outbox.getPayload());
        }
    }

    @Transactional
    private void updateToCompleted(SchedulerOutbox outbox) {
        outbox.complete();
        outboxCommandPort.save(outbox);
    }

    @Transactional
    private void updateToFailed(SchedulerOutbox outbox, String errorMessage) {
        outbox.fail(errorMessage);
        outboxCommandPort.save(outbox);
    }
}
```

### SellerQueryPort (Seller Context 연동)

```java
public interface SellerQueryPort {
    Optional<Seller> findBySellerId(SellerId sellerId);
    boolean existsActiveBySellerId(String sellerId); // DeactivateSeller 검증용
}
```

### CrawlingScheduleQueryPort

```java
public interface CrawlingScheduleQueryPort {
    Optional<CrawlingSchedule> findById(ScheduleId scheduleId);
    Optional<CrawlingSchedule> findBySellerId(SellerId sellerId);
    boolean existsActiveBySellerId(String sellerId); // Seller 비활성화 검증용
}
```

### 중요 변경사항

⚠️ **ACTIVE Seller 검증 추가**:
- RegisterScheduleUseCase에서 Seller 조회 후 ACTIVE 상태 확인
- INACTIVE Seller는 EventBridge 등록 불가

⚠️ **Admin 전용 기능**:
- REST API Layer에서 Admin 권한 체크 필요
- 일반 사용자는 EventBridge 수정 불가

⚠️ **Seller Context 연동**:
- SellerQueryPort를 통해 Seller 상태 확인
- Application Layer에서 Cross-Context 검증

⚠️ **Transaction 경계 엄격 관리**:
- DB 저장: 트랜잭션 내
- EventBridge API 호출: 트랜잭션 밖 (Outbox 패턴)
