# EVENTBRIDGE-002: EventBridge Application Layer 구현

**Epic**: 머스트잇 셀러 크롤링 스케줄러
**Bounded Context**: EventBridge (Crawling Scheduler)
**Layer**: Application
**브랜치**: feature/EVENTBRIDGE-002-application
**Jira URL**: (sync-to-jira 후 추가)

---

## 📝 목적

EventBridge 바운더리 컨텍스트의 UseCase 및 Outbox Pattern 처리를 Application Layer에서 구현합니다.

**핵심 기능**:
- 스케줄 등록/수정/비활성화
- Outbox Pattern (Transaction 경계 엄격 관리)
- TransactionSynchronization (Primary Outbox 처리)
- OutboxEventProcessor (Fallback Outbox 처리)

---

## 🎯 요구사항

### Command UseCase

#### RegisterSchedulerUseCase

- [ ] **Input**: `RegisterSchedulerCommand(sellerId, schedulerName, cronExpression)`
- [ ] **Output**: `SchedulerResponse`
- [ ] **Transaction**: Yes (Scheduler + Outbox 저장만)
- [ ] **비즈니스 로직**:
  ```
  1. [트랜잭션 시작]
  2. Seller 조회 (ACTIVE 여부 확인)
     - INACTIVE이면 예외: SellerNotActiveException
  3. CrawlingScheduler Aggregate 생성 (Domain)
     - Cron 검증 (Domain)
     - 중복 체크 (sellerId, schedulerName)
  4. Scheduler 저장 (status: PENDING)
  5. Outbox Event 저장 (PENDING, eventType: SCHEDULER_CREATED)
  6. [트랜잭션 커밋]
  7. [별도 프로세스 - TransactionSynchronization.afterCommit]
     - Outbox Event 처리 (비동기, 다른 스레드)
     - AWS EventBridge Rule 생성
     - 성공 시:
       - Outbox → PUBLISHED
       - Scheduler.status → ACTIVE
       - Scheduler.eventBridgeRuleName 업데이트
     - 실패 시:
       - Outbox → FAILED
       - 재시도 스케줄링 (Fallback: OutboxEventProcessor)
  ```

#### UpdateSchedulerUseCase

- [ ] **Input**: `UpdateSchedulerCommand(schedulerId, schedulerName?, cronExpression?, status?)`
- [ ] **Output**: `SchedulerResponse`
- [ ] **Transaction**: Yes (Scheduler + Outbox + History 저장)
- [ ] **비즈니스 로직**:
  1. [트랜잭션 시작]
  2. Scheduler 조회
  3. 변경사항 적용 (Domain 메서드)
     - Cron 변경 시 Domain 검증
     - Name 변경 시 중복 체크
  4. History 저장 (변경 전/후 값)
  5. Outbox Event 저장 (SCHEDULER_UPDATED)
  6. [트랜잭션 커밋]
  7. [TransactionSynchronization.afterCommit]
     - AWS EventBridge Rule 업데이트
     - 성공/실패 처리

#### DeactivateSchedulerUseCase

- [ ] **Input**: `DeactivateSchedulerCommand(schedulerId)`
- [ ] **Output**: `SchedulerResponse`
- [ ] **Transaction**: Yes
- [ ] **비즈니스 로직**:
  1. Scheduler 조회
  2. Scheduler.deactivate() (Domain)
  3. History 저장 (STATUS: ACTIVE → INACTIVE)
  4. Outbox Event 저장 (SCHEDULER_DELETED)
  5. [TransactionSynchronization.afterCommit]
     - AWS EventBridge Rule Disable
     - 성공/실패 처리

### Query UseCase

#### GetSchedulerUseCase

- [ ] **Input**: `GetSchedulerQuery(schedulerId)`
- [ ] **Output**: `SchedulerDetailResponse`
- [ ] **Transaction**: ReadOnly

#### ListSchedulersUseCase

- [ ] **Input**: `ListSchedulersQuery(sellerId?, status?, page, size)`
- [ ] **Output**: `PageResponse<SchedulerSummaryResponse>`
- [ ] **Transaction**: ReadOnly
- [ ] **필터링**: sellerId, status
- [ ] **페이징**: Offset-based Pagination

#### GetSchedulerHistoryUseCase

- [ ] **Input**: `GetSchedulerHistoryQuery(schedulerId, page, size)`
- [ ] **Output**: `PageResponse<SchedulerHistoryResponse>`
- [ ] **Transaction**: ReadOnly
- [ ] **정렬**: changedAt DESC (최신순)

### Outbox Processor

#### TransactionSynchronizationAdapter (Primary)

- [ ] **트리거**: `@TransactionalEventListener(phase = AFTER_COMMIT)`
- [ ] **처리 대상**: 트랜잭션 커밋 직후 Outbox Event
- [ ] **처리 로직**:
  1. Outbox Event 조회 (PENDING)
  2. 비동기 스레드에서 AWS EventBridge API 호출
     - SCHEDULER_CREATED: CreateRule + PutTargets
     - SCHEDULER_UPDATED: UpdateRule
     - SCHEDULER_DELETED: DisableRule
  3. 성공 시:
     - Outbox.status → PUBLISHED
     - Outbox.processedAt 업데이트
     - Scheduler.status 업데이트 (PENDING → ACTIVE)
     - Scheduler.eventBridgeRuleName 업데이트
  4. 실패 시:
     - Outbox.status → FAILED
     - Outbox.errorMessage 업데이트
     - Fallback: OutboxEventProcessor가 재시도

#### OutboxEventProcessor (Fallback)

- [ ] **실행 주기**: 매 1분 (`@Scheduled(fixedDelay = 60000)`)
- [ ] **처리 대상**: PENDING 또는 FAILED 상태 Outbox (retryCount < maxRetries)
- [ ] **처리 로직**:
  ```
  1. PENDING/FAILED 상태 Outbox 조회
  2. 각 Outbox에 대해:
     - eventType에 따라 AWS EventBridge API 호출
     - 성공 시:
       - Outbox.status → PUBLISHED
       - Outbox.processedAt 업데이트
       - Scheduler.status 업데이트
     - 실패 시:
       - Outbox.retryCount++
       - Outbox.errorMessage 업데이트
       - retryCount >= maxRetries 시:
         - Outbox.status → FAILED (영구)
         - Slack 알림 발송
  ```

### Port 정의 (Out)

#### SchedulerCommandPort

- [ ] `Scheduler save(Scheduler scheduler)`
- [ ] `void delete(Long schedulerId)`

#### SchedulerQueryPort

- [ ] `Optional<Scheduler> findById(Long schedulerId)`
- [ ] `Optional<Scheduler> findBySellerIdAndSchedulerName(Long sellerId, String schedulerName)`
- [ ] `List<Scheduler> findBySellerIdAndStatus(Long sellerId, SchedulerStatus status)`
- [ ] `Page<Scheduler> findAllBySellerIdAndStatus(Long sellerId, SchedulerStatus status, Pageable pageable)`
- [ ] `int countActiveSchedulersBySellerId(Long sellerId)`
- [ ] `int countTotalSchedulersBySellerId(Long sellerId)`

#### OutboxEventCommandPort

- [ ] `OutboxEvent save(OutboxEvent event)`
- [ ] `void deleteByStatusAndCreatedAtBefore(OutboxStatus status, LocalDateTime before)`

#### OutboxEventQueryPort

- [ ] `List<OutboxEvent> findByStatusAndRetryCountLessThan(OutboxStatus status, Integer maxRetries)`
- [ ] `List<OutboxEvent> findByStatusAndCreatedAtBefore(OutboxStatus status, LocalDateTime before)`

#### SchedulerHistoryCommandPort

- [ ] `SchedulerHistory save(SchedulerHistory history)`

#### SchedulerHistoryQueryPort

- [ ] `Page<SchedulerHistory> findBySchedulerId(Long schedulerId, Pageable pageable)`

#### SellerQueryPort (Seller 바운더리 컨텍스트)

- [ ] `Optional<Seller> findById(Long sellerId)`

#### EventBridgeClientPort (Adapter-Out)

- [ ] `void createRule(String ruleName, String cronExpression, String target)`
- [ ] `void updateRule(String ruleName, String cronExpression)`
- [ ] `void disableRule(String ruleName)`

### Command/Query DTO

- [ ] **RegisterSchedulerCommand**
- [ ] **UpdateSchedulerCommand**
- [ ] **DeactivateSchedulerCommand**
- [ ] **GetSchedulerQuery**
- [ ] **ListSchedulersQuery**
- [ ] **GetSchedulerHistoryQuery**
- [ ] **SchedulerResponse**
- [ ] **SchedulerDetailResponse**
- [ ] **SchedulerSummaryResponse**
- [ ] **SchedulerHistoryResponse**

---

## ⚠️ 제약사항

### Zero-Tolerance 규칙 (매우 중요!)

- [ ] **Transaction 경계 엄격 관리**
  - Scheduler + Outbox 저장만 트랜잭션 내
  - AWS EventBridge 호출은 트랜잭션 밖 (TransactionSynchronization)
  - `@Transactional` 내 외부 API 호출 절대 금지

- [ ] **Spring Proxy 제약사항 준수**
  - Private 메서드: `@Transactional` 작동 안함
  - Final 클래스/메서드: `@Transactional` 작동 안함
  - 같은 클래스 내부 호출: `@Transactional` 작동 안함 (`this.method()`)
  - 해결: Public 메서드 + 외부 클래스에서 호출

- [ ] **Command/Query 분리 (CQRS)**
  - Command UseCase: 상태 변경 (`*CommandPort`)
  - Query UseCase: 조회만 (`*QueryPort`)
  - Port 네이밍: `Scheduler**Command**Port`, `Scheduler**Query**Port` (필수 접미사)

- [ ] **Port 의존성 역전**
  - Application Layer는 Port 인터페이스만 의존
  - Port 인터페이스는 Application Layer에 위치
  - Adapter는 Port 구현체 (Persistence Layer에 위치)

- [ ] **Assembler 패턴 사용 (필수)**
  - Domain ↔ Application DTO 변환은 **반드시** Assembler에서
  - UseCase/Facade에서 직접 변환 금지
  - Assembler는 @Component Bean (DI 가능)

### 테스트 규칙

- [ ] **ArchUnit 테스트 필수**
  - Application Layer는 Domain Layer만 의존
  - Adapter Layer 의존 금지

- [ ] **Mock Port 사용**
  - Unit Test 시 Port는 Mock 객체

- [ ] **Outbox Pattern 테스트**
  - TransactionSynchronization 테스트
  - OutboxEventProcessor 테스트
  - 재시도 로직 테스트

- [ ] **테스트 커버리지 > 80%**

---

## ✅ 완료 조건

- [ ] Command UseCase 구현 완료
  - RegisterSchedulerUseCase
  - UpdateSchedulerUseCase
  - DeactivateSchedulerUseCase

- [ ] Query UseCase 구현 완료
  - GetSchedulerUseCase
  - ListSchedulersUseCase
  - GetSchedulerHistoryUseCase

- [ ] Outbox Processor 구현 완료
  - TransactionSynchronizationAdapter (Primary)
  - OutboxEventProcessor (Fallback)

- [ ] Port 인터페이스 정의 완료

- [ ] Command/Query DTO 구현 완료

- [ ] Assembler 구현 완료

- [ ] Application Unit Test 완료

- [ ] ArchUnit 테스트 완료

- [ ] Zero-Tolerance 규칙 준수 확인

- [ ] 코드 리뷰 승인

- [ ] PR 머지 완료

---

## 🔗 관련 문서

- **PRD**: `/Users/sangwon-ryu/crawlinghub/docs/prd/mustit-seller-crawler-scheduler.md`
- **Plan**: `docs/prd/seller/eventbridge/plans/EVENTBRIDGE-002-application-plan.md`
- **Jira**: (sync-to-jira 후 추가)
- **코딩 규칙**: `docs/coding_convention/03-application-layer/`
- **선행 Task**: EVENTBRIDGE-001 (Domain)

---

## 📋 다음 단계

1. `/create-plan EVENTBRIDGE-002` - TDD Plan 생성
2. `/kb/application/go` - Application Layer TDD 시작
