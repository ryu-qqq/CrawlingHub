# PRD: 머스트잇 셀러 크롤링 스케줄러

**작성일**: 2025-01-18
**작성자**: sangwon-ryu
**상태**: Draft

---

## 📋 프로젝트 개요

### 비즈니스 목적
- 머스트잇 사이트의 셀러별 상품 정보를 주기적으로 크롤링
- AWS EventBridge를 통한 스케줄 기반 크롤링 자동화
- Outbox Pattern을 통한 안전한 외부 API 연동

### 주요 사용자
- 관리자 (셀러 등록, 스케줄 관리) - 단일 사용자

### 성공 기준
- 스케줄 등록 시 AWS EventBridge와 100% 동기화 (Outbox Pattern)
- 셀러 비활성화 시 관련 스케줄 모두 자동 비활성화
- 스케줄 변경 이력 100% 추적 가능
- 외부 API 실패 시 자동 재시도 (3회, Exponential Backoff)

---

## 🏗️ Layer별 요구사항

### 1. Domain Layer

---

#### Aggregate: Seller

**속성**:
- `sellerId`: Long (PK, AutoIncrement)
- `mustItSellerId`: String (머스트잇 노출 ID, Unique, Not Null)
- `sellerName`: String (Not Null, Unique)
- `status`: SellerStatus (Enum: ACTIVE, INACTIVE)
- `createdAt`: LocalDateTime
- `updatedAt`: LocalDateTime

**비즈니스 규칙**:

1. **셀러 등록 (Register Seller)**:
   - **머스트잇 셀러 ID**:
     - `mustItSellerId` 형식 제약 없음 (String)
     - Unique 제약 (중복 시 예외 발생: `DuplicateMustItSellerIdException`)
     - 변경 불가 (Immutable)

   - **셀러명**:
     - `sellerName` 필수 (Not Null)
     - Unique 제약 (중복 시 예외 발생: `DuplicateSellerNameException`)
     - 변경 불가 (Immutable)

   - **초기 상태**:
     - 기본 상태: `ACTIVE`
     - 등록 시 추가 검증 없음 (머스트잇 API 호출 X)

2. **셀러 상태 변경 (Change Status)**:

   **ACTIVE → INACTIVE 전환**:
   - **전제 조건**: 해당 셀러의 모든 스케줄이 INACTIVE 상태여야 함
     - 만약 ACTIVE 스케줄이 1개라도 있으면 예외 발생: `SellerHasActiveSchedulersException`

   - **비활성화 프로세스**:
     ```
     1. 셀러 상태 → INACTIVE (Domain)
     2. 모든 스케줄 비활성화 이벤트 발행 (Domain Event)
     3. Application Layer에서 이벤트 수신
     4. 각 스케줄에 대해 Outbox Event 생성 (SCHEDULER_DELETED)
     5. TransactionSynchronization 후 AWS EventBridge Rule 삭제 시도
     6. 실패 시 FAILED 상태로 마킹 → Scheduler가 재시도
     ```

   - **Event-Driven 순서**:
     1. 셀러 상태 변경 (Domain)
     2. `SellerDeactivatedEvent` 발행
     3. Application Layer에서 스케줄 비활성화 처리

   **INACTIVE → ACTIVE 재활성화**:
   - 재활성화 가능
   - 스케줄은 수동으로 개별 활성화 필요 (자동 활성화 X)

**Value Objects**:
- **SellerStatus**: Enum (`ACTIVE`, `INACTIVE`)

**Domain Events**:
- **SellerDeactivatedEvent**: 셀러 비활성화 시 발행
  - `sellerId`: Long
  - `occurredAt`: LocalDateTime

**Zero-Tolerance 규칙 준수**:
- ✅ Law of Demeter (Getter 체이닝 금지)
- ✅ Lombok 금지 (Pure Java/Record 사용)
- ✅ Long FK 전략 (JPA 관계 어노테이션 금지)

---

#### Aggregate: CrawlingScheduler

**속성**:
- `schedulerId`: Long (PK, AutoIncrement)
- `sellerId`: Long (FK - Long FK 전략)
- `schedulerName`: String (Not Null)
- `cronExpression`: String (Not Null, AWS EventBridge 형식)
- `status`: SchedulerStatus (Enum: PENDING, ACTIVE, INACTIVE)
- `eventBridgeRuleName`: String (Nullable, AWS Rule 이름)
- `createdAt`: LocalDateTime
- `updatedAt`: LocalDateTime

**비즈니스 규칙**:

1. **스케줄 등록 (Register Scheduler)**:

   - **스케줄러 이름 중복 처리**:
     - 동일 Seller 내에서 `schedulerName` 중복 불가
     - Unique Constraint: `(sellerId, schedulerName)`
     - 중복 시 예외 발생: `DuplicateSchedulerNameException`
     - 다른 Seller 간에는 같은 `schedulerName` 사용 가능

   - **Cron Expression 검증** (Domain Layer):
     - **검증 주체**: Domain (CrawlingScheduler Aggregate)
     - **지원 형식**: AWS EventBridge Cron 형식 (6자리)
       - 형식: `cron(분 시 일 월 요일 년도)`
       - 예시: `cron(0 0 * * ? *)` (매일 00:00)
     - **최소 실행 간격**: 1시간
       - 1시간 미만 간격 시 예외 발생: `InvalidCronExpressionException`
     - **검증 로직**:
       ```java
       // Domain Value Object
       public record CronExpression(String value) {
           public CronExpression {
               validateAwsEventBridgeFormat(value);
               validateMinimumInterval(value); // 최소 1시간
           }
       }
       ```

   - **EventBridge Rule 이름 생성 규칙**:
     - 형식: `{sellerName}-{schedulerName}-{timestamp}`
     - 예시: `nike-daily-crawler-20250118120000`
     - 생성 시점: Outbox Event 처리 시 (Application Layer)

   - **AWS EventBridge Rule 생성 실패 시 처리**:
     - **전략**: Scheduler는 저장, Outbox도 함께 저장 (PENDING)
     - **성공 시**:
       1. Outbox → PUBLISHED
       2. Scheduler.status → ACTIVE
       3. Scheduler.eventBridgeRuleName 업데이트
     - **실패 시**:
       1. Outbox → FAILED
       2. Scheduler.status → PENDING (그대로)
       3. Scheduler 기준으로 재시도 (Exponential Backoff)

   - **초기 상태**:
     - 등록 시 기본 상태: `PENDING` (AWS Rule 생성 전)
     - INACTIVE Seller의 스케줄 등록 불가
       - 예외 발생: `SellerNotActiveException`

2. **스케줄 수정 (Update Scheduler)**:

   - **수정 가능 항목**:
     - `schedulerName`: 변경 가능 (중복 체크 재수행)
     - `cronExpression`: 변경 가능 (Domain 검증 + AWS Rule 업데이트 필요)
     - `status`: 변경 가능 (ACTIVE ↔ INACTIVE, PENDING → ACTIVE)

   - **EventBridge 동기화**:
     - Cron 또는 Name 변경 시 무조건 Outbox Event 저장 (SCHEDULER_UPDATED)
     - AWS EventBridge Rule 즉시 업데이트 시도 (TransactionSynchronization)
     - 업데이트 실패 시: Outbox 재시도

   - **수정 이력 관리** (SchedulerHistory):
     - **기록 항목**:
       - `historyId`: Long
       - `schedulerId`: Long
       - `changedField`: String (CRON_EXPRESSION, SCHEDULER_NAME, STATUS)
       - `oldValue`: String
       - `newValue`: String
       - `changedAt`: LocalDateTime
     - **기록 시점**: 모든 필드 변경 시 즉시 기록 (Domain Event)

3. **스케줄 비활성화 (Deactivate Scheduler)**:

   - **개별 비활성화**:
     - 개별 스케줄 비활성화 가능
     - 비활성화 시 AWS Rule도 Disable (Outbox Pattern)
     - Outbox Event: `SCHEDULER_DELETED`

   - **Seller 비활성화에 의한 일괄 비활성화**:
     - Seller INACTIVE 시 모든 스케줄 INACTIVE
     - 히스토리에도 기록 (changedField: STATUS, oldValue: ACTIVE, newValue: INACTIVE)
     - 각 스케줄에 대해 Outbox Event 생성

**Value Objects**:
- **CronExpression**: String (AWS EventBridge 형식 검증)
- **SchedulerStatus**: Enum (`PENDING`, `ACTIVE`, `INACTIVE`)

**Domain Events**:
- **SchedulerRegisteredEvent**: 스케줄 등록 시 발행
- **SchedulerUpdatedEvent**: 스케줄 수정 시 발행
- **SchedulerDeactivatedEvent**: 스케줄 비활성화 시 발행

**Zero-Tolerance 규칙 준수**:
- ✅ Law of Demeter
- ✅ Lombok 금지
- ✅ Long FK 전략 (`private Long sellerId;`)

---

#### Aggregate: SchedulerOutboxEvent

**속성**:
- `eventId`: Long (PK, AutoIncrement)
- `eventType`: OutboxEventType (Enum)
- `schedulerId`: Long (FK)
- `payload`: String (JSON, AWS EventBridge 요청 데이터)
- `status`: OutboxStatus (Enum)
- `retryCount`: Integer (기본값 0)
- `maxRetries`: Integer (기본값 3)
- `createdAt`: LocalDateTime
- `processedAt`: LocalDateTime (Nullable)
- `errorMessage`: String (Nullable, 실패 시 에러 메시지)

**비즈니스 규칙**:

1. **재시도 전략**:
   - **재시도 간격**: Exponential Backoff with Jitter
     - 1차: 1분 후
     - 2차: 5분 후
     - 3차: 15분 후
   - **최대 재시도 횟수**: 3회
   - **모든 재시도 실패 시**:
     - Outbox.status → FAILED (영구)
     - Slack 알림 발송 (관리자에게 수동 개입 요청)

2. **Outbox 정리 전략**:
   - **PUBLISHED 상태 이벤트**: 30일 후 자동 삭제 (Scheduler)
   - **FAILED 상태 이벤트**: 영구 보관 (수동 처리 필요)
   - **PENDING 상태 이벤트**: 생성 후 24시간 경과 시 알림 (장애 의심)

**Value Objects**:
- **OutboxEventType**: Enum (`SCHEDULER_CREATED`, `SCHEDULER_UPDATED`, `SCHEDULER_DELETED`)
- **OutboxStatus**: Enum (`PENDING`, `PUBLISHED`, `FAILED`)

**Zero-Tolerance 규칙 준수**:
- ✅ Long FK 전략 (`private Long schedulerId;`)
- ✅ Lombok 금지

---

### 2. Application Layer

---

#### Command UseCase

**RegisterSellerUseCase**:
- **Input**: `RegisterSellerCommand(mustItSellerId, sellerName)`
- **Output**: `SellerResponse(sellerId, mustItSellerId, sellerName, status)`
- **Transaction**: Yes
- **비즈니스 로직**:
  1. Seller Aggregate 생성 (Domain)
  2. 중복 검증 (mustItSellerId, sellerName)
  3. Seller 저장 (PersistencePort)
  4. 트랜잭션 커밋

**ChangeSellerStatusUseCase**:
- **Input**: `ChangeSellerStatusCommand(sellerId, targetStatus)`
- **Output**: `SellerResponse`
- **Transaction**: Yes
- **비즈니스 로직** (ACTIVE → INACTIVE):
  1. Seller 조회 (존재하지 않으면 예외)
  2. ACTIVE 스케줄 존재 여부 확인
     - 있으면 예외: `SellerHasActiveSchedulersException`
  3. Seller.deactivate() (Domain 메서드)
  4. `SellerDeactivatedEvent` 발행
  5. 트랜잭션 커밋
  6. Event Handler에서 스케줄 비활성화 처리

**RegisterSchedulerUseCase**:
- **Input**: `RegisterSchedulerCommand(sellerId, schedulerName, cronExpression)`
- **Output**: `SchedulerResponse(schedulerId, schedulerName, status, ...)`
- **Transaction**: Yes (Scheduler + Outbox 저장만)
- **비즈니스 로직**:
  ```
  1. [트랜잭션 시작]
  2. Seller 조회 (ACTIVE 여부 확인)
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
       - 재시도 스케줄링 (Fallback: Scheduler가 PENDING 상태 Outbox 처리)
  ```

**UpdateSchedulerUseCase**:
- **Input**: `UpdateSchedulerCommand(schedulerId, schedulerName?, cronExpression?, status?)`
- **Output**: `SchedulerResponse`
- **Transaction**: Yes (Scheduler + Outbox + History 저장)
- **비즈니스 로직**:
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
     - 성공/실패 처리 (RegisterScheduler와 동일)

**DeactivateSchedulerUseCase**:
- **Input**: `DeactivateSchedulerCommand(schedulerId)`
- **Output**: `SchedulerResponse`
- **Transaction**: Yes
- **비즈니스 로직**:
  1. Scheduler 조회
  2. Scheduler.deactivate() (Domain)
  3. History 저장 (STATUS: ACTIVE → INACTIVE)
  4. Outbox Event 저장 (SCHEDULER_DELETED)
  5. [TransactionSynchronization.afterCommit]
     - AWS EventBridge Rule Disable
     - 성공/실패 처리

---

#### Query UseCase

**GetSellerUseCase**:
- **Input**: `GetSellerQuery(sellerId)`
- **Output**: `SellerDetailResponse(sellerId, mustItSellerId, sellerName, status, activeSchedulerCount, ...)`
- **Transaction**: ReadOnly

**ListSellersUseCase**:
- **Input**: `ListSellersQuery(status?, page, size)`
- **Output**: `PageResponse<SellerSummaryResponse>`
- **Transaction**: ReadOnly
- **필터링**: status (ACTIVE/INACTIVE)
- **페이징**: Offset-based Pagination

**GetSchedulerUseCase**:
- **Input**: `GetSchedulerQuery(schedulerId)`
- **Output**: `SchedulerDetailResponse(schedulerId, sellerId, schedulerName, cronExpression, status, ...)`
- **Transaction**: ReadOnly

**ListSchedulersUseCase**:
- **Input**: `ListSchedulersQuery(sellerId?, status?, page, size)`
- **Output**: `PageResponse<SchedulerSummaryResponse>`
- **Transaction**: ReadOnly
- **필터링**: sellerId, status
- **페이징**: Offset-based Pagination

**GetSchedulerHistoryUseCase**:
- **Input**: `GetSchedulerHistoryQuery(schedulerId, page, size)`
- **Output**: `PageResponse<SchedulerHistoryResponse>`
- **Transaction**: ReadOnly
- **정렬**: changedAt DESC (최신순)

---

#### Event Handler

**SellerDeactivatedEventHandler**:
- **Input**: `SellerDeactivatedEvent`
- **처리**:
  1. 해당 Seller의 모든 ACTIVE 스케줄 조회
  2. 각 스케줄에 대해 `DeactivateSchedulerUseCase` 호출
  3. Outbox Event 생성 (SCHEDULER_DELETED)

---

#### Outbox Processor

**OutboxEventProcessor** (Scheduled Job):
- **실행 주기**: 매 1분 (`@Scheduled(fixedDelay = 60000)`)
- **처리 대상**: PENDING 또는 FAILED 상태 Outbox
- **처리 로직**:
  ```
  1. PENDING/FAILED 상태 Outbox 조회 (retryCount < maxRetries)
  2. 각 Outbox에 대해:
     - eventType에 따라 AWS EventBridge API 호출
       - SCHEDULER_CREATED: CreateRule + PutTargets
       - SCHEDULER_UPDATED: UpdateRule
       - SCHEDULER_DELETED: DisableRule
     - 성공 시:
       - Outbox.status → PUBLISHED
       - Outbox.processedAt 업데이트
       - Scheduler.status 업데이트 (PENDING → ACTIVE)
     - 실패 시:
       - Outbox.retryCount++
       - Outbox.errorMessage 업데이트
       - retryCount >= maxRetries 시:
         - Outbox.status → FAILED
         - Slack 알림 발송
  ```

**TransactionSynchronizationAdapter** (Primary):
- **트리거**: `@TransactionalEventListener(phase = AFTER_COMMIT)`
- **처리**:
  1. Outbox Event 조회 (PENDING)
  2. 비동기 스레드에서 AWS EventBridge API 호출
  3. 성공/실패 처리
  4. 실패 시 Fallback: OutboxEventProcessor가 재시도

---

#### Zero-Tolerance 규칙 준수

- ✅ Command/Query 분리 (CQRS)
- ✅ **Transaction 경계 엄격 관리**:
  - Scheduler + Outbox 저장만 트랜잭션 내
  - AWS EventBridge 호출은 트랜잭션 밖 (TransactionSynchronization)
- ✅ `@Transactional` 내 외부 API 호출 절대 금지

---

### 3. Persistence Layer

---

#### JPA Entity

**SellerJpaEntity**:
- **테이블**: `sellers`
- **필드**:
  - `id`: Long (PK, Auto Increment)
  - `must_it_seller_id`: String (Unique, Not Null, Index)
  - `seller_name`: String (Unique, Not Null, Index)
  - `status`: String (Not Null, Index)
  - `created_at`: LocalDateTime (Not Null)
  - `updated_at`: LocalDateTime (Not Null)
- **인덱스**:
  - `idx_must_it_seller_id` (must_it_seller_id) - Unique
  - `idx_seller_name` (seller_name) - Unique
  - `idx_status` (status) - 필터링용
- **Unique Constraint**:
  - `must_it_seller_id`
  - `seller_name`

**CrawlingSchedulerJpaEntity**:
- **테이블**: `crawling_schedulers`
- **필드**:
  - `id`: Long (PK, Auto Increment)
  - `seller_id`: Long (FK, Not Null, Index)
  - `scheduler_name`: String (Not Null)
  - `cron_expression`: String (Not Null)
  - `status`: String (Not Null, Index)
  - `event_bridge_rule_name`: String (Nullable, Unique)
  - `created_at`: LocalDateTime (Not Null)
  - `updated_at`: LocalDateTime (Not Null)
- **인덱스**:
  - `idx_seller_id_scheduler_name` (seller_id, scheduler_name) - Unique Composite
  - `idx_seller_id_status` (seller_id, status) - 필터링용
  - `idx_status` (status) - Outbox Processor용
- **Unique Constraint**:
  - `(seller_id, scheduler_name)` - Composite Unique
  - `event_bridge_rule_name` (Nullable Unique)

**SchedulerHistoryJpaEntity**:
- **테이블**: `scheduler_histories`
- **필드**:
  - `id`: Long (PK, Auto Increment)
  - `scheduler_id`: Long (FK, Not Null, Index)
  - `changed_field`: String (Not Null)
  - `old_value`: String (Nullable)
  - `new_value`: String (Not Null)
  - `changed_at`: LocalDateTime (Not Null, Index)
- **인덱스**:
  - `idx_scheduler_id_changed_at` (scheduler_id, changed_at DESC) - 이력 조회용

**SchedulerOutboxEventJpaEntity**:
- **테이블**: `scheduler_outbox_events`
- **필드**:
  - `id`: Long (PK, Auto Increment)
  - `event_type`: String (Not Null)
  - `scheduler_id`: Long (FK, Not Null, Index)
  - `payload`: Text (JSON, Not Null)
  - `status`: String (Not Null, Index)
  - `retry_count`: Integer (Not Null, Default 0)
  - `max_retries`: Integer (Not Null, Default 3)
  - `created_at`: LocalDateTime (Not Null, Index)
  - `processed_at`: LocalDateTime (Nullable)
  - `error_message`: Text (Nullable)
- **인덱스**:
  - `idx_status_retry_count` (status, retry_count) - Outbox Processor용
  - `idx_status_created_at` (status, created_at) - 정리용

---

#### Repository

**SellerJpaRepository**:
```java
public interface SellerJpaRepository extends JpaRepository<SellerJpaEntity, Long> {
    Optional<SellerJpaEntity> findByMustItSellerId(String mustItSellerId);
    Optional<SellerJpaEntity> findBySellerName(String sellerName);
    boolean existsByMustItSellerId(String mustItSellerId);
    boolean existsBySellerName(String sellerName);
}
```

**SellerQueryDslRepository**:
- **메서드**: `findAllByStatus(status, Pageable)`
- **최적화**: DTO Projection

**CrawlingSchedulerJpaRepository**:
```java
public interface CrawlingSchedulerJpaRepository extends JpaRepository<CrawlingSchedulerJpaEntity, Long> {
    Optional<CrawlingSchedulerJpaEntity> findBySellerIdAndSchedulerName(Long sellerId, String schedulerName);
    List<CrawlingSchedulerJpaEntity> findBySellerIdAndStatus(Long sellerId, SchedulerStatus status);
    boolean existsBySellerIdAndSchedulerName(Long sellerId, String schedulerName);
}
```

**CrawlingSchedulerQueryDslRepository**:
- **메서드**:
  - `findAllBySellerIdAndStatus(sellerId, status, Pageable)`
  - `findAllByStatus(status, Pageable)`
- **최적화**: DTO Projection, Seller 정보 조인 (N+1 방지)

**SchedulerOutboxEventJpaRepository**:
```java
public interface SchedulerOutboxEventJpaRepository extends JpaRepository<SchedulerOutboxEventJpaEntity, Long> {
    List<SchedulerOutboxEventJpaEntity> findByStatusAndRetryCountLessThan(
        OutboxStatus status,
        Integer maxRetries
    );
    List<SchedulerOutboxEventJpaEntity> findByStatusAndCreatedAtBefore(
        OutboxStatus status,
        LocalDateTime before
    );
}
```

---

#### 동시성 제어

**Scheduler 등록 시 동시 요청 처리**:
- **전략**: Pessimistic Lock (`SELECT FOR UPDATE`)
- **이유**: 동일 Seller 내 동일 schedulerName 중복 방지
- **적용**:
  ```java
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT s FROM SellerJpaEntity s WHERE s.id = :sellerId")
  Optional<SellerJpaEntity> findByIdWithLock(@Param("sellerId") Long sellerId);
  ```

**Seller 상태 변경 시 동시 요청 처리**:
- **전략**: 동시성 제어 불필요 (단일 사용자)
- **이유**: 관리자 1명만 사용

**Outbox Event 처리 시 동시성**:
- **전략**: Optimistic Lock (`@Version`) - Spring Scheduler 중복 실행 방지
- **적용**:
  ```java
  @Version
  private Long version;
  ```

---

#### Zero-Tolerance 규칙 준수

- ✅ Long FK 전략:
  - `private Long sellerId;` (O)
  - `@ManyToOne private Seller seller;` (X)
- ✅ QueryDSL 최적화 (N+1 방지)
- ✅ Lombok 금지 (Pure Java)

---

### 4. REST API Layer

---

#### API 엔드포인트

| Method | Path | Description | Request DTO | Response DTO | Status Code |
|--------|------|-------------|-------------|--------------|-------------|
| POST | /api/v1/sellers | 셀러 등록 | RegisterSellerRequest | SellerResponse | 201 Created |
| PATCH | /api/v1/sellers/{sellerId}/status | 셀러 상태 변경 | ChangeSellerStatusRequest | SellerResponse | 200 OK |
| GET | /api/v1/sellers/{sellerId} | 셀러 조회 | - | SellerDetailResponse | 200 OK |
| GET | /api/v1/sellers | 셀러 목록 조회 | ListSellersRequest | PageResponse\<SellerSummaryResponse\> | 200 OK |
| POST | /api/v1/sellers/{sellerId}/schedulers | 스케줄 등록 | RegisterSchedulerRequest | SchedulerResponse | 201 Created |
| PATCH | /api/v1/schedulers/{schedulerId} | 스케줄 수정 | UpdateSchedulerRequest | SchedulerResponse | 200 OK |
| GET | /api/v1/schedulers/{schedulerId} | 스케줄 조회 | - | SchedulerDetailResponse | 200 OK |
| GET | /api/v1/schedulers | 스케줄 목록 조회 | ListSchedulersRequest | PageResponse\<SchedulerSummaryResponse\> | 200 OK |
| GET | /api/v1/schedulers/{schedulerId}/history | 스케줄 이력 조회 | - | PageResponse\<SchedulerHistoryResponse\> | 200 OK |

---

#### Request/Response DTO

**RegisterSellerRequest**:
```java
public record RegisterSellerRequest(
    @NotBlank String mustItSellerId,
    @NotBlank String sellerName
) {}
```

**ChangeSellerStatusRequest**:
```java
public record ChangeSellerStatusRequest(
    @NotNull SellerStatus targetStatus
) {}
```

**SellerResponse**:
```java
public record SellerResponse(
    Long sellerId,
    String mustItSellerId,
    String sellerName,
    SellerStatus status,
    LocalDateTime createdAt
) {}
```

**SellerDetailResponse**:
```java
public record SellerDetailResponse(
    Long sellerId,
    String mustItSellerId,
    String sellerName,
    SellerStatus status,
    Integer activeSchedulerCount,
    Integer totalSchedulerCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
```

**RegisterSchedulerRequest**:
```java
public record RegisterSchedulerRequest(
    @NotBlank String schedulerName,
    @NotBlank String cronExpression
) {}
```

**UpdateSchedulerRequest**:
```java
public record UpdateSchedulerRequest(
    String schedulerName,
    String cronExpression,
    SchedulerStatus status
) {
    // 최소 1개 필드는 변경되어야 함
    public boolean hasAnyChange() {
        return schedulerName != null || cronExpression != null || status != null;
    }
}
```

**SchedulerResponse**:
```java
public record SchedulerResponse(
    Long schedulerId,
    Long sellerId,
    String schedulerName,
    String cronExpression,
    SchedulerStatus status,
    String eventBridgeRuleName,
    LocalDateTime createdAt
) {}
```

**SchedulerHistoryResponse**:
```java
public record SchedulerHistoryResponse(
    Long historyId,
    Long schedulerId,
    String changedField,
    String oldValue,
    String newValue,
    LocalDateTime changedAt
) {}
```

**Error Response**:
```json
{
  "errorCode": "DUPLICATE_MUST_IT_SELLER_ID",
  "message": "이미 등록된 머스트잇 셀러 ID입니다.",
  "timestamp": "2025-01-18T12:34:56Z",
  "path": "/api/v1/sellers"
}
```

---

#### 인증/인가

- **인증**: 없음 (관리자 단일 사용자, 내부 시스템)
- **인가**: 없음

---

#### Validation

**Request Validation**:
- Bean Validation (`@NotBlank`, `@NotNull`) 사용
- Custom Validator:
  - `CronExpressionValidator`: AWS EventBridge Cron 형식 검증
  - `SchedulerNameValidator`: 특수문자 제한 (영문, 숫자, -, _ 만 허용)

**예시**:
```java
public record RegisterSchedulerRequest(
    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9-_]+$", message = "스케줄러 이름은 영문, 숫자, -, _만 사용 가능합니다.")
    String schedulerName,

    @NotBlank
    @CronExpression(type = CronType.AWS_EVENT_BRIDGE)
    String cronExpression
) {}
```

---

#### Exception Handling

**Domain Exceptions → HTTP Status Mapping**:
- `DuplicateMustItSellerIdException` → 409 Conflict
- `DuplicateSellerNameException` → 409 Conflict
- `DuplicateSchedulerNameException` → 409 Conflict
- `SellerHasActiveSchedulersException` → 400 Bad Request
- `SellerNotActiveException` → 400 Bad Request
- `InvalidCronExpressionException` → 400 Bad Request
- `SellerNotFoundException` → 404 Not Found
- `SchedulerNotFoundException` → 404 Not Found

**Global Exception Handler**:
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateMustItSellerIdException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateMustItSellerId(
        DuplicateMustItSellerIdException ex,
        HttpServletRequest request
    ) {
        ErrorResponse error = new ErrorResponse(
            "DUPLICATE_MUST_IT_SELLER_ID",
            ex.getMessage(),
            LocalDateTime.now(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // ... 기타 예외 핸들러
}
```

---

#### Zero-Tolerance 규칙 준수

- ✅ RESTful 설계 원칙
- ✅ 일관된 Error Response 형식
- ✅ Bean Validation 필수

---

## ⚠️ 제약사항

### 비기능 요구사항

**성능**:
- 셀러 등록 응답 시간: < 500ms (P95)
- 스케줄 등록 응답 시간: < 1s (P95, AWS API 호출 포함)
- 스케줄 조회 응답 시간: < 100ms (P95)
- Outbox Processor 처리 주기: 1분

**보안**:
- 내부 시스템 (인증/인가 없음)
- AWS EventBridge API 인증: IAM Role

**확장성**:
- 동시 사용자: 1명 (관리자)
- 셀러 수: 최대 1,000개
- 스케줄 수: 최대 10,000개 (셀러당 평균 10개)

**안정성**:
- Outbox Pattern으로 외부 API 실패 대응
- 재시도 3회 (Exponential Backoff)
- 실패 시 Slack 알림

---

## 🧪 테스트 전략

### Unit Test

**Domain**:
- Seller Aggregate:
  - 셀러 등록 (중복 검증)
  - 상태 변경 (ACTIVE ↔ INACTIVE)
  - Domain Event 발행 (SellerDeactivatedEvent)
- CrawlingScheduler Aggregate:
  - 스케줄 등록 (Cron 검증, 중복 검증)
  - 스케줄 수정 (필드별 검증)
  - Domain Event 발행 (SchedulerRegisteredEvent 등)
- Value Object:
  - CronExpression (AWS EventBridge 형식 검증, 최소 간격 검증)
  - SellerStatus, SchedulerStatus (상태 전환 검증)

**Application**:
- RegisterSellerUseCase (Mock PersistencePort)
- ChangeSellerStatusUseCase (Mock PersistencePort)
- RegisterSchedulerUseCase (Mock PersistencePort, Mock EventPort)
- UpdateSchedulerUseCase (Mock PersistencePort, Mock EventPort)
- SellerDeactivatedEventHandler (Mock UseCase)

### Integration Test

**Persistence**:
- SellerJpaRepository CRUD 테스트 (TestContainers MySQL)
- CrawlingSchedulerJpaRepository CRUD 테스트
- SchedulerOutboxEventJpaRepository 쿼리 테스트
- QueryDSL 복잡한 쿼리 테스트 (N+1 방지 검증)
- Unique Constraint 테스트 (중복 방지)
- Pessimistic Lock 테스트 (동시성)

**REST API**:
- SellerApiController (TestRestTemplate)
  - POST /api/v1/sellers (201 Created)
  - PATCH /api/v1/sellers/{sellerId}/status (200 OK)
  - GET /api/v1/sellers/{sellerId} (200 OK, 404 Not Found)
- CrawlingSchedulerApiController (TestRestTemplate)
  - POST /api/v1/sellers/{sellerId}/schedulers (201 Created)
  - PATCH /api/v1/schedulers/{schedulerId} (200 OK)
  - GET /api/v1/schedulers/{schedulerId}/history (200 OK, 페이징)
- Validation 테스트 (400 Bad Request)
- Exception Handling 테스트 (409 Conflict, 404 Not Found)

**Outbox Pattern**:
- TransactionSynchronization 테스트
  - 트랜잭션 커밋 후 AWS API 호출 검증
  - 성공 시 Outbox.status → PUBLISHED
  - 실패 시 Outbox.status → FAILED
- OutboxEventProcessor 테스트
  - PENDING/FAILED 상태 Outbox 처리
  - 재시도 로직 (Exponential Backoff)
  - 최대 재시도 초과 시 Slack 알림

### E2E Test

**시나리오 1: 셀러 등록 → 스케줄 등록 → 조회**:
1. POST /api/v1/sellers (셀러 등록)
2. POST /api/v1/sellers/{sellerId}/schedulers (스케줄 등록)
3. Outbox Event 처리 (AWS EventBridge Rule 생성)
4. GET /api/v1/schedulers/{schedulerId} (상태: ACTIVE)

**시나리오 2: 셀러 비활성화 → 스케줄 일괄 비활성화**:
1. POST /api/v1/sellers (셀러 등록)
2. POST /api/v1/sellers/{sellerId}/schedulers (스케줄 3개 등록)
3. PATCH /api/v1/sellers/{sellerId}/status (INACTIVE)
4. GET /api/v1/schedulers (status: INACTIVE, 3개 모두 비활성화 확인)

**시나리오 3: Outbox 재시도 (AWS API 실패)**:
1. POST /api/v1/sellers/{sellerId}/schedulers (스케줄 등록)
2. AWS EventBridge API 실패 (Mock)
3. Outbox.status → FAILED
4. OutboxEventProcessor 실행 (재시도)
5. 성공 시 Outbox.status → PUBLISHED

---

## 🚀 개발 계획

### Phase 1: Domain Layer (예상: 5일)
- [ ] Seller Aggregate 구현
  - [ ] 셀러 등록 (중복 검증)
  - [ ] 상태 변경 (ACTIVE ↔ INACTIVE)
  - [ ] SellerDeactivatedEvent 발행
- [ ] CrawlingScheduler Aggregate 구현
  - [ ] 스케줄 등록 (Cron 검증, 중복 검증)
  - [ ] 스케줄 수정
  - [ ] Domain Event 발행
- [ ] SchedulerOutboxEvent Aggregate 구현
- [ ] Value Object 구현 (CronExpression, SellerStatus, SchedulerStatus)
- [ ] Domain Unit Test (TestFixture 패턴)

### Phase 2: Application Layer (예상: 6일)
- [ ] Command UseCase 구현
  - [ ] RegisterSellerUseCase
  - [ ] ChangeSellerStatusUseCase
  - [ ] RegisterSchedulerUseCase (Outbox Pattern)
  - [ ] UpdateSchedulerUseCase (Outbox Pattern)
  - [ ] DeactivateSchedulerUseCase
- [ ] Query UseCase 구현
  - [ ] GetSellerUseCase
  - [ ] ListSellersUseCase
  - [ ] GetSchedulerUseCase
  - [ ] ListSchedulersUseCase
  - [ ] GetSchedulerHistoryUseCase
- [ ] Event Handler 구현
  - [ ] SellerDeactivatedEventHandler
- [ ] Outbox Processor 구현
  - [ ] TransactionSynchronizationAdapter
  - [ ] OutboxEventProcessor (Scheduler)
- [ ] Command/Query DTO 구현
- [ ] Application Unit Test

### Phase 3: Persistence Layer (예상: 4일)
- [ ] JPA Entity 구현
  - [ ] SellerJpaEntity
  - [ ] CrawlingSchedulerJpaEntity
  - [ ] SchedulerHistoryJpaEntity
  - [ ] SchedulerOutboxEventJpaEntity
- [ ] Repository 구현
  - [ ] SellerJpaRepository
  - [ ] SellerQueryDslRepository
  - [ ] CrawlingSchedulerJpaRepository
  - [ ] CrawlingSchedulerQueryDslRepository
  - [ ] SchedulerOutboxEventJpaRepository
- [ ] Mapper 구현 (Entity ↔ Aggregate)
- [ ] Integration Test (TestContainers MySQL)
  - [ ] CRUD 테스트
  - [ ] Unique Constraint 테스트
  - [ ] Pessimistic Lock 테스트

### Phase 4: REST API Layer (예상: 4일)
- [ ] Controller 구현
  - [ ] SellerApiController
  - [ ] CrawlingSchedulerApiController
- [ ] Request/Response DTO 구현
- [ ] Custom Validator 구현 (CronExpressionValidator)
- [ ] Global Exception Handler 구현
- [ ] REST API Integration Test (TestRestTemplate)
  - [ ] 성공 케이스 (201, 200)
  - [ ] 실패 케이스 (400, 404, 409)

### Phase 5: AWS EventBridge 연동 (예상: 3일)
- [ ] AWS EventBridge Client 구현
  - [ ] CreateRule + PutTargets
  - [ ] UpdateRule
  - [ ] DisableRule
- [ ] Outbox Pattern 통합 테스트
  - [ ] TransactionSynchronization 테스트
  - [ ] OutboxEventProcessor 테스트
  - [ ] 재시도 로직 테스트
- [ ] Slack 알림 연동 (실패 시)

### Phase 6: E2E Test & Monitoring (예상: 2일)
- [ ] E2E 테스트 작성
  - [ ] 시나리오 1: 셀러 등록 → 스케줄 등록 → 조회
  - [ ] 시나리오 2: 셀러 비활성화 → 스케줄 일괄 비활성화
  - [ ] 시나리오 3: Outbox 재시도
- [ ] Monitoring 설정
  - [ ] Outbox Event 처리 실패 알림
  - [ ] AWS EventBridge API 실패 알림

---

## 📚 참고 문서

- [Domain Layer 규칙](../../docs/coding_convention/02-domain-layer/)
- [Application Layer 규칙](../../docs/coding_convention/03-application-layer/)
- [Persistence Layer 규칙](../../docs/coding_convention/04-persistence-layer/)
- [REST API Layer 규칙](../../docs/coding_convention/01-adapter-in-layer/rest-api/)
- [AWS EventBridge Cron Expressions](https://docs.aws.amazon.com/eventbridge/latest/userguide/eb-create-rule-schedule.html)

---

## 🔄 다음 단계

1. **PRD 검토 및 승인**
2. **Jira 티켓 생성**: `/jira-from-prd docs/prd/mustit-seller-crawler-scheduler.md`
3. **Layer별 개발 시작**:
   - Domain Layer: `/kb/domain/go`
   - Application Layer: `/kb/application/go`
   - Persistence Layer: `/kb/persistence/go`
   - REST API Layer: `/kb/rest-api/go`

---

**최종 수정일**: 2025-01-18
