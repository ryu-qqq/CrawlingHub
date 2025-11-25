# PRD: Crawl Task Trigger

**작성일**: 2025-11-21
**작성자**: Claude
**상태**: Draft

---

## 📋 프로젝트 개요

### 비즈니스 목적
EventBridge 스케줄에 의해 트리거되어 CrawlTask를 생성하고, SQS를 통해 Worker에게 전달하여 MustIt 크롤링을 수행하는 시스템 구축

### 주요 사용자
- EventBridge (자동 트리거)
- API Server (Task 생성)
- ECS Worker (Task 소비 및 크롤링 수행)

### 성공 기준
- EventBridge → API Server → SQS → Worker 파이프라인 안정적 동작
- 중복 트리거 방지 (Idempotency 보장)
- Worker 수평 확장 가능

---

## 🏗️ Layer별 요구사항

### 1. Domain Layer

#### Aggregate: CrawlTask

**속성**:
- crawlTaskId: CrawlTaskId (Value Object, UUID)
- crawlSchedulerId: Long (FK)
- sellerId: Long (FK)
- requestUrl: String (크롤링 대상 URL)
- status: CrawlTaskStatus (Enum)
- retryCount: Integer (재시도 횟수)
- createdAt: LocalDateTime
- updatedAt: LocalDateTime

**비즈니스 규칙**:

1. **Task 생성**:
   - Scheduler가 ACTIVE 상태일 때만 생성 가능 (PAUSED/DISABLED → 에러)
   - 동일 Scheduler에 WAITING/RUNNING 상태 Task 존재 시 skip (중복 방지)
   - 첫 번째 Task만 생성 (미니샵 메타데이터 크롤링)
   - Worker가 상품 수 확인 후 추가 Task 생성

2. **Task 생성 전략** (단계별):
   ```
   1단계: 미니샵 메타데이터 Task 1개 발행
   2단계: Worker가 상품 수 확인
   3단계: 상품 수 / 500 만큼 미니샵 크롤링 Task 발행
   4단계: 각 미니샵 Task 완료 후 상세/옵션 Task 발행
   ```

3. **상태 전환**:
   ```
   WAITING → PUBLISHED → RUNNING → SUCCESS
                              ↓
                           FAILED → RETRY → PUBLISHED
                              ↓
                           TIMEOUT → RETRY
   ```

**Value Objects**:
- **CrawlTaskId**: UUID 기반 Task 식별자
- **CrawlTaskStatus**: Enum (WAITING, PUBLISHED, RUNNING, SUCCESS, FAILED, RETRY, TIMEOUT)

**Zero-Tolerance 규칙 준수**:
- ✅ Law of Demeter (Getter 체이닝 금지)
- ✅ Lombok 금지 (Pure Java/Record 사용)
- ✅ Long FK 전략 (JPA 관계 어노테이션 금지)

---

#### Aggregate: CrawlTaskOutBox

**속성**:
- crawlTaskOutBoxId: CrawlTaskOutBoxId (Value Object)
- crawlTaskId: CrawlTaskId (FK)
- idempotencyKey: String (멱등성 키)
- messagePayload: String (JSON)
- status: CrawlTaskOutBoxStatus (Enum)
- retryCount: Integer
- createdAt: LocalDateTime
- processedAt: LocalDateTime

**비즈니스 규칙**:

1. **Outbox 생성**:
   - CrawlTask 저장과 동일 트랜잭션에서 생성
   - idempotencyKey = `{crawlTaskId}_{timestamp}`
   - messagePayload = JSON (taskId, requestUrl, retryCount)

2. **발행 처리**:
   - TransactionSynchronization afterCommit에서 SQS 발행
   - 발행 성공 시 COMPLETED로 상태 변경
   - 발행 실패 시 PENDING 유지 → Fallback Scheduler가 재시도

3. **Fallback 전략**:
   - 별도 Scheduler가 PENDING 상태 Outbox 폴링
   - retryCount까지 재시도 후 FAILED 처리

**Value Objects**:
- **CrawlTaskOutBoxId**: UUID 기반 식별자
- **CrawlTaskOutBoxStatus**: Enum (PENDING, COMPLETED, FAILED)

---

### 2. Application Layer

#### Command UseCase

**TriggerCrawlTaskUseCase**:
- **Input**: `TriggerCrawlTaskCommand(crawlSchedulerId)`
- **Output**: `CrawlTaskResponse(crawlTaskId, status)`
- **Transaction**: Yes (Scheduler 조회 + Task 저장 + Outbox 저장)
- **비즈니스 로직**:
  1. Scheduler 조회 (없으면 404 예외)
  2. Scheduler 상태 확인 (PAUSED/DISABLED → 예외)
  3. 기존 WAITING/RUNNING Task 확인 (있으면 skip, 중복 방지)
  4. CrawlTask 생성 (미니샵 메타데이터 URL)
  5. CrawlTaskOutBox 생성 (idempotencyKey, messagePayload)
  6. **트랜잭션 커밋**
  7. afterCommit에서 SQS 발행 (별도 스레드)
  8. Outbox 상태 COMPLETED로 업데이트

#### Query UseCase

**GetCrawlTaskUseCase**:
- **Input**: `GetCrawlTaskQuery(crawlTaskId)`
- **Output**: `CrawlTaskDetailResponse`
- **Transaction**: ReadOnly

**ListCrawlTasksBySchedulerUseCase**:
- **Input**: `ListCrawlTasksQuery(crawlSchedulerId, status, page, size)`
- **Output**: `PageResponse<CrawlTaskSummaryResponse>`
- **Transaction**: ReadOnly

#### Event Listener

**CrawlTaskCreatedEventListener**:
- **Trigger**: CrawlTask 생성 후 트랜잭션 커밋
- **Action**: SQS 메시지 발행 (afterCommit)
- **Fallback**: 실패 시 Outbox PENDING 유지

#### Scheduler

**CrawlTaskOutBoxRetryScheduler**:
- **주기**: 1분마다 실행
- **Action**: PENDING 상태 Outbox 폴링 → SQS 재발행
- **재시도 전략**: retryCount 초과 시 FAILED 처리

#### Zero-Tolerance 규칙 준수
- ✅ Command/Query 분리 (CQRS)
- ✅ **Transaction 경계 엄격 관리** (SQS 발행은 트랜잭션 밖)

---

### 3. Persistence Layer

#### JPA Entity

**CrawlTaskJpaEntity**:
- **테이블**: `crawl_tasks`
- **필드**:
  - `id`: Long (PK, Auto Increment)
  - `crawl_task_id`: String (UUID, Unique, Not Null)
  - `crawl_scheduler_id`: Long (FK, Not Null, Index)
  - `seller_id`: Long (FK, Not Null, Index)
  - `request_url`: String (Not Null)
  - `status`: String (Not Null, Index)
  - `retry_count`: Integer (Default 0)
  - `created_at`: LocalDateTime (Not Null)
  - `updated_at`: LocalDateTime (Not Null)
- **인덱스**:
  - `idx_scheduler_status` (crawl_scheduler_id, status) - 중복 체크용
  - `idx_status_created_at` (status, created_at) - 상태별 조회

**CrawlTaskOutBoxJpaEntity**:
- **테이블**: `crawl_task_outbox`
- **필드**:
  - `id`: Long (PK, Auto Increment)
  - `crawl_task_outbox_id`: String (UUID, Unique, Not Null)
  - `crawl_task_id`: String (FK, Not Null)
  - `idempotency_key`: String (Unique, Not Null)
  - `message_payload`: Text (JSON, Not Null)
  - `status`: String (Not Null, Index)
  - `retry_count`: Integer (Default 0)
  - `created_at`: LocalDateTime (Not Null)
  - `processed_at`: LocalDateTime (Nullable)
- **인덱스**:
  - `idx_status_retry` (status, retry_count) - Fallback Scheduler용

#### Repository

**CrawlTaskJpaRepository**:
```java
public interface CrawlTaskJpaRepository extends JpaRepository<CrawlTaskJpaEntity, Long> {
    Optional<CrawlTaskJpaEntity> findByCrawlTaskId(String crawlTaskId);
    boolean existsByCrawlSchedulerIdAndStatusIn(Long schedulerId, List<String> statuses);
}
```

**CrawlTaskOutBoxJpaRepository**:
```java
public interface CrawlTaskOutBoxJpaRepository extends JpaRepository<CrawlTaskOutBoxJpaEntity, Long> {
    List<CrawlTaskOutBoxJpaEntity> findByStatusAndRetryCountLessThan(String status, int maxRetry);
}
```

#### Zero-Tolerance 규칙 준수
- ✅ Long FK 전략 (관계 어노테이션 금지)
- ✅ QueryDSL 최적화 (N+1 방지)

---

### 4. REST API Layer

#### API 엔드포인트

| Method | Path | Description | Request | Response | Status |
|--------|------|-------------|---------|----------|--------|
| POST | /api/v1/crawl/trigger | 크롤링 트리거 | TriggerCrawlTaskRequest | CrawlTaskResponse | 201/409 |
| GET | /api/v1/crawl/tasks/{taskId} | Task 조회 | - | CrawlTaskDetailResponse | 200 |
| GET | /api/v1/crawl/tasks | Task 목록 조회 | ListCrawlTasksRequest | PageResponse | 200 |

#### Request/Response DTO

**TriggerCrawlTaskRequest**:
```java
public record TriggerCrawlTaskRequest(
    @NotNull Long crawlSchedulerId
) {}
```

**CrawlTaskResponse**:
```java
public record CrawlTaskResponse(
    String crawlTaskId,
    CrawlTaskStatus status,
    String requestUrl,
    LocalDateTime createdAt
) {}
```

**SQS Message Payload**:
```json
{
  "idempotencyKey": "task-uuid_1732200000",
  "taskId": "task-uuid",
  "requestUrl": "https://m.web.mustit.co.kr/mustit-api/facade-api/v1/search/mini-shop-search?sellerId=12345",
  "retryCount": 0
}
```

#### Error Response

| Status | Error Code | Description |
|--------|------------|-------------|
| 404 | SCHEDULER_NOT_FOUND | Scheduler 존재하지 않음 |
| 409 | SCHEDULER_NOT_ACTIVE | Scheduler가 비활성 상태 |
| 409 | DUPLICATE_TASK_EXISTS | 이미 진행 중인 Task 존재 |

#### 인증/인가
- **EventBridge**: IAM Role 기반 인증 (AWS Signature V4)
- **내부 호출**: API Key 또는 IAM Role

---

## ⚠️ 제약사항

### 비기능 요구사항

**성능**:
- 트리거 응답 시간: < 200ms (P95)
- SQS 발행 지연: < 100ms
- Fallback Scheduler 주기: 1분

**안정성**:
- Outbox 패턴으로 메시지 유실 방지
- Idempotency Key로 중복 발행 방지
- Fallback Scheduler로 실패 복구

**확장성**:
- Worker 수평 확장 지원 (SQS 기반)
- 동시 트리거 처리: 100 requests/sec

---

## 🧪 테스트 전략

### Unit Test

**Domain**:
- CrawlTask 생성 및 상태 전환 로직
- CrawlTaskOutBox 생성 및 상태 관리

**Application**:
- TriggerCrawlTaskUseCase (Mock Port)
- 중복 트리거 방지 로직

### Integration Test

**Persistence**:
- CrawlTaskJpaRepository CRUD (TestContainers MySQL)
- 중복 체크 쿼리 테스트

**REST API**:
- TriggerController (MockMvc)
- 에러 응답 테스트 (404, 409)

### E2E Test

- EventBridge → API → SQS 전체 파이프라인
- Outbox Fallback 시나리오

---

## 🚀 개발 계획

### Phase 1: Domain Layer (예상: 2일)
- [ ] CrawlTask Aggregate 구현
- [ ] CrawlTaskOutBox Aggregate 구현
- [ ] Value Objects (CrawlTaskId, CrawlTaskStatus 등)
- [ ] Domain Unit Test

### Phase 2: Application Layer (예상: 3일)
- [ ] TriggerCrawlTaskUseCase 구현
- [ ] CrawlTaskCreatedEventListener 구현 (afterCommit)
- [ ] CrawlTaskOutBoxRetryScheduler 구현
- [ ] Application Unit Test

### Phase 3: Persistence Layer (예상: 2일)
- [ ] CrawlTaskJpaEntity 구현
- [ ] CrawlTaskOutBoxJpaEntity 구현
- [ ] Repository 구현
- [ ] Integration Test (TestContainers)

### Phase 4: REST API Layer (예상: 2일)
- [ ] TriggerController 구현
- [ ] Request/Response DTO 구현
- [ ] Exception Handling
- [ ] REST API Integration Test

### Phase 5: Infrastructure (예상: 1일)
- [ ] SQS Queue 설정 (Terraform)
- [ ] EventBridge Rule 설정
- [ ] IAM Role/Policy 설정

---

## 📚 참고 문서

- [System Spec](../../guide/system_spec.md)
- [Domain Layer 규칙](../coding_convention/02-domain-layer/)
- [Application Layer 규칙](../coding_convention/03-application-layer/)
- [Persistence Layer 규칙](../coding_convention/04-persistence-layer/)

---

## 🔄 시퀀스 다이어그램

```
sequenceDiagram
    participant EB as EventBridge
    participant API as API Server
    participant DB as RDS
    participant SQS as SQS Queue
    participant Worker as ECS Worker

    EB->>API: POST /api/v1/crawl/trigger (schedulerId)
    API->>DB: Scheduler 조회
    alt Scheduler 비활성
        API-->>EB: 409 SCHEDULER_NOT_ACTIVE
    end
    API->>DB: 기존 WAITING/RUNNING Task 확인
    alt 중복 Task 존재
        API-->>EB: 409 DUPLICATE_TASK_EXISTS
    end
    API->>DB: CrawlTask 생성 (WAITING)
    API->>DB: CrawlTaskOutBox 생성 (PENDING)
    API->>DB: 트랜잭션 커밋
    API->>SQS: afterCommit - 메시지 발행
    API->>DB: OutBox 상태 → COMPLETED
    API-->>EB: 201 Created

    Worker->>SQS: 메시지 폴링
    Worker->>DB: Task 상태 → RUNNING
    Worker->>MustIt: 크롤링 요청
    Worker->>DB: Task 상태 → SUCCESS/FAILED
```

---

**다음 단계**: `/jira-task docs/prd/crawl-task-trigger.md`
