# MUSTIT-001: Domain Layer 구현

**Epic**: 머스트잇 셀러 크롤러
**Layer**: Domain Layer
**브랜치**: feature/MUSTIT-001-domain
**Jira URL**: (sync-to-jira 후 추가)

---

## 📝 목적

비즈니스 핵심 로직을 담당하는 Domain Layer 구현. 외부 의존성 없이 순수한 비즈니스 규칙과 도메인 개념을 표현합니다.

**핵심 역할**:
- 비즈니스 규칙 및 불변식 구현
- Aggregate, Entity, Value Object 설계
- Tell Don't Ask 원칙 준수
- Law of Demeter 준수

---

## 🎯 요구사항

### 1. Aggregate: Seller (셀러)

- [ ] **Seller Aggregate 구현**
  - sellerId (SellerId VO)
  - name (String)
  - crawlingInterval (CrawlingInterval VO)
  - status (SellerStatus Enum)
  - totalProductCount (Integer)

- [ ] **비즈니스 규칙**
  - 셀러 ID 고유성 검증 (중복 불가)
  - 기본 크롤링 주기 1일
  - 등록 시 상태 ACTIVE
  - 크롤링 주기는 일(day) 단위만 허용 (1-30일)

- [ ] **Value Objects**
  - SellerId: String (머스트잇 셀러 ID)
  - CrawlingInterval: Integer (1-30)
  - SellerStatus: Enum (ACTIVE, INACTIVE)

- [ ] **Domain 메서드**
  - `register(sellerId, name, intervalDays)`: 셀러 등록
  - `updateInterval(newIntervalDays)`: 주기 변경
  - `activate()`: 활성화
  - `deactivate()`: 비활성화
  - `updateTotalProductCount(count)`: 총 상품 수 업데이트

### 2. Aggregate: CrawlerTask (크롤링 태스크)

- [ ] **CrawlerTask Aggregate 구현**
  - taskId (TaskId VO, UUID)
  - sellerId (SellerId VO)
  - taskType (CrawlerTaskType Enum)
  - requestUrl (RequestUrl VO) ⬅️ **변경: String → VO**
  - status (CrawlerTaskStatus Enum)
  - retryCount (Integer, 최대 2회)

- [ ] **비즈니스 규칙**
  - 태스크 생성 시 상태 WAITING
  - RequestUrl VO가 taskType에 따라 자동 검증
    - MINISHOP: `/searchmini-shop-search` 패턴 포함 확인
    - PRODUCT_DETAIL: `/item/{숫자}/detail/top` 정규식 검증
    - PRODUCT_OPTION: `/auction_products/{숫자}/options` 정규식 검증

- [ ] **상태 전환 로직**
  - WAITING → PUBLISHED → IN_PROGRESS → COMPLETED/FAILED/RETRY
  - 재시도 최대 2회
  - 재시도 초과 시 FAILED

- [ ] **Value Objects**
  - TaskId: UUID
  - RequestUrl: String (taskType 기반 URL 형식 검증) ⬅️ **신규 추가**
  - CrawlerTaskType: Enum (MINISHOP, PRODUCT_DETAIL, PRODUCT_OPTION)
  - CrawlerTaskStatus: Enum (WAITING, PUBLISHED, IN_PROGRESS, COMPLETED, FAILED, RETRY)

- [ ] **Domain 메서드**
  - `create(sellerId, taskType, requestUrl)`: 태스크 생성
  - `publish()`: 발행 상태로 전환
  - `start()`: 진행 중 상태로 전환
  - `complete()`: 완료
  - `fail(errorMessage)`: 실패 처리
  - `retry()`: 재시도 (retryCount < 2)

### 3. Aggregate: UserAgent (유저 에이전트)

- [ ] **UserAgent Aggregate 구현**
  - userAgentId (UserAgentId VO, UUID)
  - userAgentString (String)
  - token (Token VO, Nullable) ⬅️ **변경: String → VO**
  - status (UserAgentStatus Enum)
  - ~~requestCount~~ ⬅️ **삭제: Redis로 이동**
  - ~~lastRequestAt~~ ⬅️ **삭제: Redis로 이동**
  - tokenIssuedAt (LocalDateTime, Nullable)

- [ ] **비즈니스 규칙**
  - 50개 미리 정의된 UserAgent 문자열 사용
  - 생성 시 token null, status ACTIVE
  - 429 응답 시 즉시 SUSPENDED 상태 전환 + token null 처리

- [ ] **토큰 버킷 리미터 로직** ⬅️ **변경: Infrastructure Layer (Redis)로 위임**
  - ~~Domain Layer에서 제거~~ (`canMakeRequest()`, `incrementRequestCount()`)
  - **Redis Sliding Window 방식** (Lua 스크립트)
  - 시간당 80회 제한 (과거 1시간 기준 실시간 리필)
  - Application Layer (UserAgentPoolManager)에서 호출

- [ ] **Value Objects**
  - UserAgentId: UUID
  - Token: String (머스트잇 비회원 토큰, null/blank 검증) ⬅️ **신규 추가**
  - UserAgentStatus: Enum (ACTIVE, SUSPENDED, BLOCKED)

- [ ] **Domain 메서드**
  - `create(userAgentString)`: UserAgent 생성
  - `issueToken(Token)`: 토큰 발급 (VO 주입) ⬅️ **변경: String → Token VO**
  - ~~`canMakeRequest()`~~: ⬅️ **삭제: Redis로 이동**
  - ~~`incrementRequestCount()`~~: ⬅️ **삭제: Redis로 이동**
  - ~~`resetRequestCount()`~~: ⬅️ **삭제: Redis로 이동**
  - `suspend()`: 429 응답 시 일시 중지 (token null 처리)
  - `activate()`: 재활성화
  - `block()`: 관리자 수동 차단 ⬅️ **신규 추가**

### 4. Aggregate: Product (상품)

- [ ] **Product Aggregate 구현**
  - productId (ProductId VO, UUID)
  - itemNo (Long)
  - sellerId (SellerId VO)
  - minishopDataHash (String)
  - detailDataHash (String)
  - optionDataHash (String)
  - isComplete (Boolean)

- [ ] **비즈니스 규칙**
  - 미니샵 크롤링 시 itemNo 추출하여 생성
  - 초기 isComplete = false
  - 모든 해시값 존재 시 isComplete = true
  - 해시값 비교로 변경 감지 (MD5)

- [ ] **상품 완성 기준**
  - 미니샵 + 상세 + 옵션 각 1번씩 크롤링 완료
  - 모든 해시값 != null

- [ ] **Value Objects**
  - ProductId: UUID
  - ItemNo: Long

- [ ] **Domain 메서드**
  - `create(itemNo, sellerId)`: 상품 생성
  - `updateMinishopData(rawJson)`: 미니샵 데이터 업데이트 및 해시 계산
  - `updateDetailData(rawJson)`: 상세 데이터 업데이트 및 해시 계산
  - `updateOptionData(rawJson)`: 옵션 데이터 업데이트 및 해시 계산
  - `isComplete()`: 완성 여부 확인
  - `hasChanged(oldHash, newHash)`: 변경 감지

### 5. Aggregate: ProductOutbox (상품 외부 전송)

- [ ] **ProductOutbox Aggregate 구현**
  - outboxId (OutboxId VO, UUID)
  - productId (ProductId VO)
  - eventType (OutboxEventType Enum)
  - payload (String, JSON)
  - status (OutboxStatus Enum)
  - retryCount (Integer)

- [ ] **비즈니스 규칙**
  - 상품 변경 감지 시 자동 생성
  - 초기 상태 WAITING
  - 배치 처리 (5분마다)
  - 재시도 최대 5회 (Exponential Backoff)

- [ ] **상태 전환 로직**
  - WAITING → SENDING → COMPLETED/FAILED
  - 재시도 5회 초과 시 FAILED

- [ ] **Value Objects**
  - OutboxId: UUID
  - OutboxEventType: Enum (PRODUCT_CREATED, PRODUCT_UPDATED)
  - OutboxStatus: Enum (WAITING, SENDING, COMPLETED, FAILED)

- [ ] **Domain 메서드**
  - `create(productId, eventType, payload)`: Outbox 생성
  - `send()`: 전송 중 상태로 전환
  - `complete()`: 전송 완료
  - `fail(errorMessage)`: 전송 실패
  - `canRetry()`: 재시도 가능 여부 확인 (retryCount < 5)

### 6. Aggregate: CrawlingSchedule (크롤링 스케줄) ⬅️ **신규 추가**

- [ ] **CrawlingSchedule Aggregate 구현**
  - scheduleId (ScheduleId VO, UUID)
  - sellerId (SellerId VO)
  - crawlingInterval (CrawlingInterval VO)
  - scheduleRule (String, EventBridge Rule Name)
  - scheduleExpression (String, Cron 표현식)
  - status (ScheduleStatus Enum)

- [ ] **비즈니스 규칙**
  - Seller 등록 시 자동 생성 (1:1 관계)
  - 초기 상태 ACTIVE
  - scheduleRule: `mustit-crawler-{sellerId}` 형식
  - scheduleExpression: `rate({intervalDays} days)` 형식
  - Seller 주기 변경 시 자동 업데이트

- [ ] **Value Objects**
  - ScheduleId: UUID
  - ScheduleStatus: Enum (ACTIVE, INACTIVE, FAILED)

- [ ] **Domain Event 발행**
  - ScheduleRegistered: 스케줄 생성 시
  - ScheduleUpdated: 주기 변경 시
  - ScheduleDeactivated: 비활성화 시

- [ ] **Domain 메서드**
  - `create(sellerId, crawlingInterval)`: 스케줄 생성
  - `updateInterval(newInterval)`: 주기 변경 (ScheduleUpdated 이벤트)
  - `deactivate()`: 비활성화 (ScheduleDeactivated 이벤트)
  - `activate()`: 재활성화

### 7. Aggregate: CrawlingScheduleExecution (크롤링 스케줄 실행) ⬅️ **신규 추가**

- [ ] **CrawlingScheduleExecution Aggregate 구현**
  - executionId (ExecutionId VO, UUID)
  - scheduleId (ScheduleId VO)
  - sellerId (SellerId VO)
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

### 8. Aggregate: SchedulerOutbox (스케줄러 외부 전송) ⬅️ **신규 추가**

- [ ] **SchedulerOutbox Aggregate 구현**
  - outboxId (OutboxId VO, UUID)
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
  - SchedulerEventType: Enum (SCHEDULE_CREATED, SCHEDULE_UPDATED, SCHEDULE_DELETED)

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
  - ✅ `public record SellerId(String value) {}`
  - ❌ `@Value public class SellerId { ... }`

- [ ] **Law of Demeter 준수**: Getter 체이닝 금지
  - ✅ `seller.getCrawlingIntervalDays()`
  - ❌ `seller.getCrawlingInterval().getDays()`

- [ ] **Tell Don't Ask**: 내부 상태 기반 판단
  - ✅ `userAgent.canMakeRequest()` (내부에서 판단)
  - ❌ `if (userAgent.getRequestCount() < 80) { ... }` (외부에서 판단)

- [ ] **Long FK 전략**: 관계 어노테이션 금지
  - ✅ `private SellerId sellerId;`
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

- [ ] **8개 Aggregate 구현 완료** ⬅️ **변경: 5개 → 8개**
  - Seller
  - CrawlerTask (RequestUrl VO 적용)
  - UserAgent (Token VO 적용, Redis 위임)
  - Product
  - ProductOutbox
  - CrawlingSchedule (신규)
  - CrawlingScheduleExecution (신규)
  - SchedulerOutbox (신규)
- [ ] **모든 Value Object 구현 완료**
  - RequestUrl (신규)
  - Token (신규)
  - 기존 VO 포함
- [ ] 모든 Enum 구현 완료
- [ ] 모든 Domain 메서드 구현 완료
- [ ] **Domain Event 구현 완료** (신규)
  - ScheduleRegistered
  - ScheduleUpdated
  - ScheduleDeactivated
- [ ] Unit Test 작성 완료 (커버리지 > 80%)
- [ ] ArchUnit 테스트 통과
- [ ] TestFixture 패턴 적용
- [ ] Zero-Tolerance 규칙 준수
- [ ] 코드 리뷰 승인
- [ ] PR 머지 완료

---

## 🔗 관련 문서

- **PRD**: docs/prd/mustit-seller-crawler.md
- **Plan**: docs/prd/plans/MUSTIT-001-domain-plan.md (create-plan 후 생성)
- **Jira**: (sync-to-jira 후 추가)
- **Domain Layer 규칙**: docs/coding_convention/02-domain-layer/

---

## 📚 참고사항

### Domain 메서드 네이밍 규칙

- 생성: `create()`, `register()`
- 상태 전환: `activate()`, `deactivate()`, `publish()`, `start()`, `complete()`, `fail()`
- 검증: `canMakeRequest()`, `isComplete()`, `hasChanged()`
- 업데이트: `update*()`, `increment*()`, `reset*()`

### 해시 계산 전략

- **알고리즘**: MD5 (빠른 성능, 변경 감지 목적)
- **대상**: 전체 JSON 응답 (raw data)
- **구현**: `MessageDigest.getInstance("MD5")`

### 토큰 버킷 리미터 (Redis Sliding Window)

**Domain Layer → Infrastructure Layer 위임**:
- Domain에서 `canMakeRequest()`, `incrementRequestCount()` 제거
- Redis Lua 스크립트로 구현 (Atomic 보장)

**Lua 스크립트 예시**:
```lua
local key = KEYS[1]
local now = tonumber(ARGV[1])
local window = 3600  -- 1시간
local limit = 80

-- 1시간 이전 요청 제거 (실시간 리필)
redis.call('ZREMRANGEBYSCORE', key, 0, now - window)

-- 현재 요청 수 확인
local count = redis.call('ZCARD', key)

if count < limit then
    redis.call('ZADD', key, now, now)
    redis.call('EXPIRE', key, window)
    return 1  -- 허용
else
    return 0  -- 차단
end
```

**Application Layer에서 호출**:
- UserAgentPoolManager가 Redis에 요청
- Sliding Window 방식으로 Burst Attack 방지
