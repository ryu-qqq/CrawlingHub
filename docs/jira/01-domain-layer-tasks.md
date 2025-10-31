# 🎯 Domain Layer 개발 태스크

## 📌 개발 순서 및 우선순위
1. **MustitSeller** (Priority: P0) - 셀러 관리 도메인
2. **CrawlSchedule** (Priority: P0) - 크롤링 주기 관리
3. **CrawlTask** (Priority: P0) - 크롤링 작업 도메인
4. **UserAgent** (Priority: P0) - 유저 에이전트 관리
5. **CrawledProduct** (Priority: P1) - 크롤링된 상품 정보
6. **ChangeDetection** (Priority: P1) - 변경 감지 도메인

---

## 📦 TASK-01: MustitSeller Aggregate 구현

### 개요
머스트잇 셀러 정보를 관리하는 핵심 도메인 모델

### 도메인 모델 구조
```java
package com.ryuqq.crawlinghub.domain.mustit.seller;

public class MustitSeller {
    private MustitSellerId id;           // Value Object
    private String sellerCode;           // 머스트잇 고유 ID
    private String sellerName;
    private SellerStatus status;          // ACTIVE, PAUSED, DISABLED
    private Integer totalProductCount;
    private LocalDateTime lastCrawledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 비즈니스 메서드
    public void activate() { /* 활성화 로직 */ }
    public void pause() { /* 일시정지 로직 */ }
    public void disable() { /* 비활성화 로직 */ }
    public void updateProductCount(Integer count) { /* 상품 수 업데이트 */ }
    public void recordCrawlingComplete() { /* 크롤링 완료 기록 */ }
}
```

### Value Objects
- `MustitSellerId`: 셀러 식별자
- `SellerStatus`: 상태 관리 (Enum or Sealed Class)

### Domain Events
- `SellerCreatedEvent`
- `SellerStatusChangedEvent`
- `ProductCountUpdatedEvent`

### Business Rules
- 셀러 코드는 변경 불가능 (불변)
- DISABLED 상태에서는 크롤링 불가
- 상품 수는 음수 불가

---

## 📦 TASK-02: CrawlSchedule Aggregate 구현

### 개요
셀러별 크롤링 주기를 관리하는 도메인

### 도메인 모델 구조
```java
package com.ryuqq.crawlinghub.domain.crawl.schedule;

public class CrawlSchedule {
    private CrawlScheduleId id;
    private MustitSellerId sellerId;
    private CronExpression cronExpression;  // Value Object
    private ScheduleStatus status;
    private LocalDateTime nextExecutionTime;
    private LocalDateTime lastExecutedAt;

    // 비즈니스 메서드
    public void updateSchedule(CronExpression newExpression) { }
    public void calculateNextExecution() { }
    public void markExecuted() { }
    public boolean isTimeToExecute() { }
}
```
### Value Objects
- `CrawlScheduleId`: 스케줄 식별자
- `CronExpression`: Cron 표현식 (검증 로직 포함)
- `ScheduleStatus`: ACTIVE, SUSPENDED

### Domain Services
- `ScheduleCalculator`: 다음 실행 시간 계산 서비스

### Business Rules
- 한 셀러는 하나의 활성 스케줄만 가능
- Cron 표현식은 유효해야 함
- 최소 크롤링 주기: 1시간

---

## 📦 TASK-03: CrawlTask Aggregate 구현

### 개요
개별 크롤링 작업을 나타내는 도메인

### 도메인 모델 구조
```java
package com.ryuqq.crawlinghub.domain.crawl.task;

public class CrawlTask {
    private CrawlTaskId id;
    private MustitSellerId sellerId;
    private TaskType taskType;        // MINI_SHOP, PRODUCT_DETAIL, PRODUCT_OPTION
    private TaskStatus status;        // WAITING, PUBLISHED, RUNNING, SUCCESS, FAILED, RETRY
    private String requestUrl;
    private Integer pageNumber;
    private Integer retryCount;
    private String idempotencyKey;    // (sellerId + endpoint + page) 조합
    private LocalDateTime scheduledAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    // 비즈니스 메서드
    public void publish() { }
    public void startProcessing() { }
    public void completeSuccessfully(String responseData) { }
    public void failWithError(String errorMessage) { }
    public boolean canRetry() { return retryCount < 3; }
    public void incrementRetry() { }
}
```

### Value Objects
- `CrawlTaskId`: 작업 식별자
- `TaskType`: 작업 유형 (Enum)
- `TaskStatus`: 상태 (Enum)
- `RequestUrl`: URL 검증 로직 포함

### Domain Events
- `CrawlTaskCreatedEvent`
- `CrawlTaskPublishedEvent`
- `CrawlTaskCompletedEvent`
- `CrawlTaskFailedEvent`

### Business Rules
- 최대 재시도 횟수: 3회
- 타임아웃: 10분
- RUNNING 상태 10분 초과 시 자동 RETRY
- 멱등성 키로 중복 방지

---

## 📦 TASK-04: UserAgent Aggregate 구현

### 개요
유저 에이전트와 토큰을 관리하는 도메인

### 도메인 모델 구조
```java
package com.ryuqq.crawlinghub.domain.useragent;

public class UserAgent {
    private UserAgentId id;
    private String userAgentString;
    private String currentToken;
    private TokenStatus tokenStatus;    // IDLE, ACTIVE, RATE_LIMITED, DISABLED, RECOVERED
    private Integer remainingRequests;  // 시간당 80회 제한
    private LocalDateTime tokenIssuedAt;
    private LocalDateTime rateLimitResetAt;

    // 비즈니스 메서드
    public boolean canMakeRequest() { }
    public void consumeRequest() { }
    public void issueNewToken(String token) { }
    public void handleRateLimitError() { }
    public void recoverFromRateLimit() { }
    public boolean isTokenExpired() { }
}
```

### Value Objects
- `UserAgentId`: 에이전트 식별자
- `TokenBucket`: 토큰 버킷 리미터 구현
- `TokenStatus`: 토큰 상태

### Domain Services
- `UserAgentSelector`: 로드밸런싱 알고리즘
- `TokenManager`: 토큰 생명주기 관리

### Business Rules
- 시간당 최대 80회 요청
- 429 응답 시 즉시 토큰 폐기
- 토큰 유효기간: 24시간
- DISABLED 상태 1시간 후 자동 RECOVERED

---

## 📦 TASK-05: CrawledProduct Aggregate 구현

### 개요
크롤링된 상품 정보를 관리하는 도메인

### 도메인 모델 구조
```java
package com.ryuqq.crawlinghub.domain.product;

public class CrawledProduct {
    private ProductId id;
    private String mustitItemNo;      // 머스트잇 상품 번호
    private MustitSellerId sellerId;
    private ProductData miniShopData;
    private ProductData detailData;
    private ProductData optionData;
    private String dataHash;          // 변경 감지용 해시
    private Integer version;
    private CompletionStatus status;  // INCOMPLETE, COMPLETE
    private LocalDateTime firstCrawledAt;
    private LocalDateTime lastUpdatedAt;

    // 비즈니스 메서드
    public void updateMiniShopData(ProductData data) { }
    public void updateDetailData(ProductData data) { }
    public void updateOptionData(ProductData data) { }
    public boolean isComplete() { }
    public boolean hasDataChanged(String newHash) { }
    public void incrementVersion() { }
}
```

### Value Objects
- `ProductId`: 상품 식별자
- `ProductData`: JSON 원본 데이터 래퍼
- `DataHash`: SHA-256 해시값
- `CompletionStatus`: 완성 상태

### Domain Events
- `NewProductDiscoveredEvent`
- `ProductDataChangedEvent`
- `ProductCompleteEvent`

### Business Rules
- 상품 완성 조건: 미니샵 + 상세 + 옵션 모두 존재
- 해시 불일치 시 변경 감지
- 버전은 변경 시마다 증가

---

## 📦 TASK-06: ChangeDetection Aggregate 구현

### 개요
상품 데이터 변경을 감지하고 알림을 관리하는 도메인

### 도메인 모델 구조
```java
package com.ryuqq.crawlinghub.domain.change;

public class ChangeDetection {
    private ChangeDetectionId id;
    private ProductId productId;
    private ChangeType changeType;    // PRICE, STOCK, OPTION, IMAGE
    private String previousHash;
    private String currentHash;
    private ChangeData changeDetails;
    private NotificationStatus status; // PENDING, SENT, FAILED
    private LocalDateTime detectedAt;

    // 비즈니스 메서드
    public void markAsSent() { }
    public void markAsFailed(String reason) { }
    public boolean shouldNotify() { }
    public String generateChangeMessage() { }
}
```

### Value Objects
- `ChangeDetectionId`: 변경 감지 식별자
- `ChangeType`: 변경 유형
- `ChangeData`: 변경 상세 정보
- `NotificationStatus`: 알림 상태

### Domain Services
- `ChangeDetector`: 변경 감지 알고리즘
- `HashCalculator`: 해시 계산 서비스

### Business Rules
- 중요 필드만 해시 계산 (가격, 옵션, 이미지)
- 동일 변경 24시간 내 중복 알림 방지
- FAILED 상태 3회 재시도

---

## 🎯 개발 체크리스트

### 공통 구현 사항
- [ ] Domain Entity 구현
- [ ] Value Objects 구현
- [ ] Domain Events 정의
- [ ] Business Rules 검증 로직
- [ ] Factory 메서드 구현
- [ ] Repository Interface 정의
- [ ] Domain Service 구현
- [ ] 단위 테스트 작성

### 코딩 컨벤션 준수 사항
- [ ] Lombok 사용 금지 (Pure Java)
- [ ] Law of Demeter 준수 (Getter 체이닝 금지)
- [ ] Tell, Don't Ask 패턴 적용
- [ ] 불변 객체 원칙
- [ ] Rich Domain Model
- [ ] 모든 public 메서드 Javadoc

### 테스트 요구사항
- [ ] Happy Path 테스트
- [ ] Edge Case 테스트
- [ ] Business Rule 검증 테스트
- [ ] Domain Event 발행 테스트

---

## 📊 예상 개발 일정

| Task | 예상 시간 | 담당자 | 우선순위 |
|------|----------|--------|----------|
| MustitSeller | 4h | - | P0 |
| CrawlSchedule | 4h | - | P0 |
| CrawlTask | 6h | - | P0 |
| UserAgent | 6h | - | P0 |
| CrawledProduct | 4h | - | P1 |
| ChangeDetection | 3h | - | P1 |

**총 예상 시간**: 27시간 (약 3.5일)

---

## 🔗 의존 관계

```
MustitSeller (독립)
    ↓
CrawlSchedule (MustitSellerId 참조)
    ↓
CrawlTask (MustitSellerId 참조)
    ↓
UserAgent (독립, CrawlTask와 협업)
    ↓
CrawledProduct (MustitSellerId, CrawlTask 결과)
    ↓
ChangeDetection (ProductId 참조)
```

병렬 개발 가능 그룹:
- **Group 1**: MustitSeller, UserAgent
- **Group 2**: CrawlSchedule, CrawlTask
- **Group 3**: CrawledProduct, ChangeDetection