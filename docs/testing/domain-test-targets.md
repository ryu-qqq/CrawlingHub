# Domain Layer 단위 테스트 대상 목록

> 작성일: 2024-11-27
> 목표 커버리지: 80% 이상

---

## 개요

Domain Layer는 비즈니스 핵심 규칙이 위치하는 레이어입니다.
테스트는 **Aggregate Root → Value Object → Exception** 순서로 작성합니다.

### 현재 테스트 현황

| 패키지 | 소스 파일 | 테스트 파일 | 커버리지 |
|--------|----------|------------|---------|
| task/exception | 4 | 4 | ✅ 100% |
| task/identifier | 1 | 1 | ✅ 100% |
| common/exception | 2 | 1 | ✅ 100% |
| **나머지 전체** | 89 | 0 | ❌ 0% |

---

## 1. Aggregate Root 테스트 (P0 - 최우선)

### 1.1 Seller Aggregate

**파일**: `domain/seller/aggregate/Seller.java`
**테스트 파일**: `SellerTest.java`
**우선순위**: 🔴 P0

| 테스트 항목 | 메서드 | 검증 포인트 | 상태 |
|------------|--------|------------|------|
| 신규 생성 | `forNew()` | ACTIVE 상태, productCount=0, createdAt/updatedAt 설정 | ⬜ |
| ID 기반 생성 | `of()` | sellerId null 시 예외 발생 | ⬜ |
| 복원 | `reconstitute()` | 모든 필드 정상 복원 | ⬜ |
| 활성화 | `activate()` | INACTIVE → ACTIVE 전환, 이미 ACTIVE면 무시 | ⬜ |
| 비활성화 | `deactivate()` | ACTIVE → INACTIVE, SellerDeActiveEvent 발행 | ⬜ |
| 통합 수정 | `update()` | 변경된 필드만 업데이트, 상태 전환 시 이벤트 | ⬜ |
| 상품 수 업데이트 | `updateProductCount()` | 음수 검증, 변경 시 updatedAt 갱신 | ⬜ |
| 이벤트 초기화 | `clearDomainEvents()` | 이벤트 목록 비움 | ⬜ |

---

### 1.2 CrawlScheduler Aggregate

**파일**: `domain/schedule/aggregate/CrawlScheduler.java`
**테스트 파일**: `CrawlSchedulerTest.java`
**우선순위**: 🔴 P0

| 테스트 항목 | 메서드 | 검증 포인트 | 상태 |
|------------|--------|------------|------|
| 신규 생성 | `forNew()` | ACTIVE 상태, ID=null (Auto Increment) | ⬜ |
| ID 기반 생성 | `of()` | crawlSchedulerId null 시 예외 | ⬜ |
| 등록 이벤트 | `addRegisteredEvent()` | ID 미할당 시 예외, historyId null 시 예외 | ⬜ |
| 통합 수정 | `update()` | null 파라미터 예외, 이벤트 발행 조건 검증 | ⬜ |
| 이벤트 발행 조건 | `update()` | ACTIVE 상태 또는 ACTIVE→INACTIVE 시에만 발행 | ⬜ |
| 이름 비교 | `hasSameSchedulerName()` | 동일 이름 확인 | ⬜ |

---

### 1.3 CrawlTask Aggregate

**파일**: `domain/task/aggregate/CrawlTask.java`
**테스트 파일**: `CrawlTaskTest.java`
**우선순위**: 🔴 P0

| 테스트 항목 | 메서드 | 검증 포인트 | 상태 |
|------------|--------|------------|------|
| 신규 생성 | `forNew()` | WAITING 상태, RetryCount=0, Outbox=null | ⬜ |
| 복원 | `reconstitute()` | 모든 필드 정상 복원, Outbox 포함 | ⬜ |
| Idempotency Key | `generateIdempotencyKey()` | 고유 키 생성 형식 | ⬜ |
| WAITING→PUBLISHED | `markAsPublished()` | 상태 전환, 잘못된 상태에서 예외 | ⬜ |
| PUBLISHED→RUNNING | `markAsRunning()` | 상태 전환, 잘못된 상태에서 예외 | ⬜ |
| RUNNING→SUCCESS | `markAsSuccess()` | 상태 전환, 잘못된 상태에서 예외 | ⬜ |
| RUNNING→FAILED | `markAsFailed()` | 상태 전환, 잘못된 상태에서 예외 | ⬜ |
| RUNNING→TIMEOUT | `markAsTimeout()` | 상태 전환, 잘못된 상태에서 예외 | ⬜ |
| 재시도 가능 여부 | `canRetry()` | FAILED/TIMEOUT + retryCount < MAX | ⬜ |
| 재시도 수행 | `attemptRetry()` | RETRY 상태 전환, retryCount 증가 | ⬜ |
| 재시도 후 발행 | `markAsPublishedAfterRetry()` | RETRY→PUBLISHED | ⬜ |
| Outbox 초기화 | `initializeOutbox()` | 이미 초기화 시 예외 | ⬜ |
| Outbox 발행 완료 | `markOutboxAsSent()` | 미초기화 시 예외 | ⬜ |
| Outbox 발행 실패 | `markOutboxAsFailed()` | 미초기화 시 예외 | ⬜ |
| 등록 이벤트 | `addRegisteredEvent()` | ID 미할당 시 예외 | ⬜ |

---

### 1.4 CrawlExecution Aggregate

**파일**: `domain/execution/aggregate/CrawlExecution.java`
**테스트 파일**: `CrawlExecutionTest.java`
**우선순위**: 🔴 P0

| 테스트 항목 | 메서드 | 검증 포인트 | 상태 |
|------------|--------|------------|------|
| 시작 | `start()` | RUNNING 상태, empty result, duration 시작 | ⬜ |
| 복원 | `reconstitute()` | 모든 필드 정상 복원 | ⬜ |
| 성공 완료 | `completeWithSuccess()` | SUCCESS 상태, result 설정, duration 완료 | ⬜ |
| 실패 완료 | `completeWithFailure()` | FAILED 상태, errorMessage 포함 | ⬜ |
| 실패 완료 (body) | `completeWithFailure(body)` | responseBody 포함 | ⬜ |
| 타임아웃 완료 | `completeWithTimeout()` | TIMEOUT 상태 | ⬜ |
| 비RUNNING 완료 | `completeWith*()` | RUNNING 아닌 상태에서 예외 | ⬜ |
| 상태 확인 | `isRunning/Success/Failure/Completed()` | 상태별 boolean 반환 | ⬜ |
| Rate Limit | `isRateLimited()` | HTTP 429 확인 | ⬜ |

---

### 1.5 UserAgent Aggregate

**파일**: `domain/useragent/aggregate/UserAgent.java`
**테스트 파일**: `UserAgentTest.java`
**우선순위**: 🔴 P0

| 테스트 항목 | 메서드 | 검증 포인트 | 상태 |
|------------|--------|------------|------|
| 생성 | `create()` | AVAILABLE, HealthScore=100 | ⬜ |
| 복원 | `reconstitute()` | 모든 필드 정상 복원 | ⬜ |
| 사용 기록 | `markAsUsed()` | lastUsedAt, requestsPerDay 증가 | ⬜ |
| 성공 기록 | `recordSuccess()` | HealthScore +5 (최대 100) | ⬜ |
| 429 실패 | `recordFailure(429)` | HealthScore -20, 즉시 SUSPENDED | ⬜ |
| 5xx 실패 | `recordFailure(500)` | HealthScore -10, 임계값 시 SUSPENDED | ⬜ |
| 기타 실패 | `recordFailure(400)` | HealthScore -5 | ⬜ |
| 수동 정지 | `suspend()` | AVAILABLE→SUSPENDED, 아니면 예외 | ⬜ |
| 복구 | `recover()` | SUSPENDED→AVAILABLE, HealthScore=70 | ⬜ |
| 복구 불가 | `recover()` | BLOCKED에서 예외 | ⬜ |
| 영구 차단 | `block()` | →BLOCKED, 이미 BLOCKED면 예외 | ⬜ |
| 일일 초기화 | `resetDailyRequests()` | requestsPerDay=0 | ⬜ |
| 복구 대상 | `isRecoverable()` | SUSPENDED + lastUsedAt < threshold | ⬜ |

---

## 2. Value Object 테스트 (P1 - 중요)

### 2.1 Seller 도메인

| VO | 파일 | 테스트 포인트 | 상태 |
|----|------|--------------|------|
| SellerName | `vo/SellerName.java` | null/blank 검증 | ⬜ |
| MustItSellerName | `vo/MustItSellerName.java` | null/blank 검증 | ⬜ |
| SellerStatus | `vo/SellerStatus.java` | enum 값 확인 | ⬜ |
| SellerId | `identifier/SellerId.java` | 생성, 동등성 | ⬜ |

### 2.2 Schedule 도메인

| VO | 파일 | 테스트 포인트 | 상태 |
|----|------|--------------|------|
| CronExpression | `vo/CronExpression.java` | AWS 형식 검증, 최소 간격 검증 | ⬜ |
| SchedulerName | `vo/SchedulerName.java` | null/blank 검증, 동등성 | ⬜ |
| SchedulerStatus | `vo/SchedulerStatus.java` | enum 값 확인 | ⬜ |
| CrawlSchedulerId | `identifier/CrawlSchedulerId.java` | 생성, isNew() | ⬜ |

### 2.3 Task 도메인

| VO | 파일 | 테스트 포인트 | 상태 |
|----|------|--------------|------|
| CrawlTaskStatus | `vo/CrawlTaskStatus.java` | isInProgress(), isTerminal() | ✅ 완료 |
| RetryCount | `vo/RetryCount.java` | 범위 검증, canRetry(), increment() | ⬜ |
| CrawlEndpoint | `vo/CrawlEndpoint.java` | URL 검증, 생성 | ✅ 완료 |
| CrawlTaskType | `vo/CrawlTaskType.java` | enum 값 확인 | ✅ 완료 |
| CrawlTaskId | `identifier/CrawlTaskId.java` | isAssigned() | ✅ 완료 |
| OutboxStatus | `vo/OutboxStatus.java` | enum 값 확인 | ⬜ |

### 2.4 Execution 도메인

| VO | 파일 | 테스트 포인트 | 상태 |
|----|------|--------------|------|
| CrawlExecutionStatus | `vo/CrawlExecutionStatus.java` | isTerminal(), isFailure(), isSuccess() | ⬜ |
| ExecutionDuration | `vo/ExecutionDuration.java` | start(), complete(), 검증 로직 | ⬜ |
| CrawlExecutionResult | `vo/CrawlExecutionResult.java` | success/failure/timeout 생성, 상태 확인 | ⬜ |
| CrawlExecutionId | `identifier/CrawlExecutionId.java` | unassigned() | ⬜ |

### 2.5 UserAgent 도메인

| VO | 파일 | 테스트 포인트 | 상태 |
|----|------|--------------|------|
| UserAgentStatus | `vo/UserAgentStatus.java` | isAvailable(), canRecover(), isBlocked() | ⬜ |
| HealthScore | `vo/HealthScore.java` | 범위 검증, record*, isBelowThreshold() | ⬜ |
| Token | `vo/Token.java` | 생성, 동등성 | ⬜ |
| UserAgentId | `identifier/UserAgentId.java` | unassigned() | ⬜ |

---

## 3. Exception 테스트 (P2 - 권장)

### 3.1 테스트 완료

| Exception | 파일 | 상태 |
|-----------|------|------|
| CrawlTaskErrorCode | `task/exception/CrawlTaskErrorCode.java` | ✅ 완료 |
| CrawlTaskNotFoundException | `task/exception/CrawlTaskNotFoundException.java` | ✅ 완료 |
| InvalidCrawlTaskStateException | `task/exception/InvalidCrawlTaskStateException.java` | ✅ 완료 |
| DuplicateCrawlTaskException | `task/exception/DuplicateCrawlTaskException.java` | ✅ 완료 |

### 3.2 테스트 필요

| Exception | 파일 | 테스트 포인트 | 상태 |
|-----------|------|--------------|------|
| SellerNotFoundException | `seller/exception/` | 코드, 메시지, args | ⬜ |
| SellerHasActiveSchedulersException | `seller/exception/` | 코드, 메시지 | ⬜ |
| DuplicateSellerNameException | `seller/exception/` | 코드, 메시지 | ⬜ |
| CrawlSchedulerNotFoundException | `schedule/exception/` | 코드, 메시지, args | ⬜ |
| InvalidSchedulerStateException | `schedule/exception/` | 코드, 메시지 | ⬜ |
| InvalidCronExpressionException | `schedule/exception/` | 코드, 메시지 | ⬜ |
| CrawlExecutionNotFoundException | `execution/exception/` | 코드, 메시지, args | ⬜ |
| InvalidCrawlExecutionStateException | `execution/exception/` | 코드, 메시지 | ⬜ |
| UserAgentNotFoundException | `useragent/exception/` | 코드, 메시지, args | ⬜ |
| InvalidUserAgentStateException | `useragent/exception/` | 코드, 메시지 | ⬜ |
| NoAvailableUserAgentException | `useragent/exception/` | 코드, 메시지 | ⬜ |
| CircuitBreakerOpenException | `useragent/exception/` | 코드, 메시지 | ⬜ |
| RateLimitExceededException | `useragent/exception/` | 코드, 메시지 | ⬜ |

---

## 4. Domain Event 테스트 (P2 - 권장)

| Event | 파일 | 테스트 포인트 | 상태 |
|-------|------|--------------|------|
| SellerDeActiveEvent | `seller/event/` | 생성, sellerId 포함 | ⬜ |
| SchedulerRegisteredEvent | `schedule/event/` | 생성, 필드 확인 | ⬜ |
| SchedulerUpdatedEvent | `schedule/event/` | 생성, 필드 확인 | ⬜ |
| CrawlTaskRegisteredEvent | `task/event/` | 생성, 필드 확인 | ⬜ |
| SessionRequiredEvent | `useragent/event/` | 생성, 필드 확인 | ⬜ |

---

## 5. 테스트 작성 가이드

### 5.1 테스트 명명 규칙

```java
@Test
@DisplayName("[성공] 신규 Seller 생성 - ACTIVE 상태로 생성됨")
void forNew_success_createsWithActiveStatus() { ... }

@Test
@DisplayName("[실패] 비활성화 시도 - 이미 INACTIVE면 무시")
void deactivate_alreadyInactive_noChange() { ... }
```

### 5.2 Given-When-Then 패턴

```java
@Test
void deactivate_success_publishesEvent() {
    // Given
    Seller seller = Seller.forNew(mustItSellerName, sellerName, clock);

    // When
    seller.deactivate();

    // Then
    assertThat(seller.getStatus()).isEqualTo(SellerStatus.INACTIVE);
    assertThat(seller.getDomainEvents()).hasSize(1);
    assertThat(seller.getDomainEvents().get(0))
        .isInstanceOf(SellerDeActiveEvent.class);
}
```

### 5.3 테스트 Fixture 활용

```java
// domain/src/testFixtures/java/...
public class SellerFixture {
    public static Seller createActive() { ... }
    public static Seller createInactive() { ... }
}
```

---

## 6. 진행 현황 추적

### 6.1 완료 기준

- [ ] 모든 Aggregate Root 테스트 완료 (5개)
- [ ] 핵심 Value Object 테스트 완료 (15개 이상)
- [ ] Exception 테스트 완료 (13개)
- [ ] Domain Event 테스트 완료 (5개)
- [ ] 전체 커버리지 80% 이상

### 6.2 예상 테스트 파일 수

| 카테고리 | 예상 파일 수 |
|----------|-------------|
| Aggregate | 5개 |
| Value Object | 20개 |
| Exception | 13개 |
| Event | 5개 |
| **합계** | **43개** |

---

## 변경 이력

| 날짜 | 변경 내용 |
|------|----------|
| 2024-11-27 | 초안 작성 |
