# MUSTIT-001: Domain Layer TDD Plan

**Epic**: 머스트잇 셀러 크롤러
**Layer**: Domain Layer
**Task**: MUSTIT-001-domain
**TDD Methodology**: Kent Beck TDD Cycle (Red → Green → Refactor → Tidy)

---

## 📋 TDD Plan 개요

**총 예상 시간**: 25 Cycles × 15분 = 6.25시간
**Aggregate 수**: 5개
**Value Object 수**: 9개
**Enum 수**: 5개

### TDD Cycle 구조

각 Cycle은 15분 이내로 완료:

1. **🔴 Red (5분)**: 실패하는 테스트 작성 → 컴파일 에러/테스트 실패 확인 → `test:` 커밋
2. **🟢 Green (5분)**: 최소 구현 → 테스트 통과 확인 → `feat:` 커밋
3. **♻️ Refactor (3분)**: 구조 개선 (필요 시) → 테스트 통과 확인 → `struct:` 커밋
4. **🧹 Tidy (2분)**: TestFixture 추가 → 가독성 향상 → `struct:` 커밋

---

## 🎯 Phase 1: Value Objects & Enums (6 Cycles)

### 1️⃣ Cycle 1: SellerId VO (15분)

#### 🔴 Red: 테스트 작성
```java
// domain/src/test/java/.../vo/SellerIdTest.java
@Test
void shouldCreateSellerIdWithValidValue() {
    String validSellerId = "seller_123";
    SellerId sellerId = new SellerId(validSellerId);
    assertThat(sellerId.value()).isEqualTo(validSellerId);
}

@Test
void shouldThrowExceptionWhenSellerIdIsBlank() {
    assertThatThrownBy(() -> new SellerId(""))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("SellerId는 비어있을 수 없습니다");
}
```
- [x] 테스트 파일 생성
- [x] 컴파일 에러 확인 (SellerId 클래스 없음)
- [x] 커밋: `test: SellerId VO 검증 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
```java
// domain/src/main/java/.../vo/SellerId.java
public record SellerId(String value) {
    public SellerId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("SellerId는 비어있을 수 없습니다");
        }
    }
}
```
- [x] SellerId record 구현
- [x] 테스트 통과 확인
- [x] 커밋: `feat: SellerId VO 구현 (검증 포함)`

#### ♻️ Refactor: 구조 개선 (Optional)
- [x] 필요 시 에러 메시지 상수화
- [x] Javadoc 추가 (Zero-Tolerance 규칙 준수)
- [x] 커밋: Green Phase에 포함됨

#### 🧹 Tidy: TestFixture 추가
```java
// domain/src/test/java/.../fixture/SellerFixture.java
public class SellerFixture {
    public static SellerId defaultSellerId() {
        return new SellerId("seller_123");
    }
}
```
- [x] SellerFixture 클래스 생성
- [x] 커밋: `struct: SellerId TestFixture 추가`

---

### 2️⃣ Cycle 2: CrawlingInterval VO (15분) 🔄 IN PROGRESS

#### 🔴 Red: 테스트 작성
```java
// domain/src/test/java/.../vo/CrawlingIntervalTest.java
@Test
void shouldCreateCrawlingIntervalWithValidDays() {
    CrawlingInterval interval = new CrawlingInterval(7);
    assertThat(interval.days()).isEqualTo(7);
}

@ParameterizedTest
@ValueSource(ints = {0, 31, -1})
void shouldThrowExceptionWhenDaysOutOfRange(int invalidDays) {
    assertThatThrownBy(() -> new CrawlingInterval(invalidDays))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("크롤링 주기는 1-30일 사이여야 합니다");
}
```
- [x] 테스트 파일 생성
- [x] 커밋: `test: CrawlingInterval VO 범위 검증 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
```java
// domain/src/main/java/.../vo/CrawlingInterval.java
public record CrawlingInterval(Integer days) {
    private static final int MIN_DAYS = 1;
    private static final int MAX_DAYS = 30;

    public CrawlingInterval {
        if (days == null || days < MIN_DAYS || days > MAX_DAYS) {
            throw new IllegalArgumentException("크롤링 주기는 1-30일 사이여야 합니다");
        }
    }
}
```
- [x] CrawlingInterval record 구현
- [x] 테스트 통과 확인 (4/4 passed)
- [x] 커밋: `feat: CrawlingInterval VO 구현 (1-30일 검증)`

#### 🧹 Tidy: TestFixture 추가
- [x] SellerFixture에 `defaultCrawlingInterval()` 추가
- [x] 커밋: `struct: CrawlingInterval TestFixture 추가`

---

### 3️⃣ Cycle 3: SellerStatus Enum (10분) 🔄 IN PROGRESS

#### 🔴 Red: 테스트 작성
```java
// domain/src/test/java/.../vo/SellerStatusTest.java
@Test
void shouldHaveActiveAndInactiveStatus() {
    assertThat(SellerStatus.values()).containsExactly(
        SellerStatus.ACTIVE,
        SellerStatus.INACTIVE
    );
}
```
- [x] 테스트 파일 생성
- [x] 커밋: `test: SellerStatus Enum 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
```java
// domain/src/main/java/.../vo/SellerStatus.java
public enum SellerStatus {
    ACTIVE,
    INACTIVE
}
```
- [x] SellerStatus enum 구현
- [x] 테스트 통과 확인 (1/1 passed)
- [x] 커밋: `feat: SellerStatus Enum 구현 (ACTIVE/INACTIVE)`

---

### 4️⃣ Cycle 4: TaskId, CrawlerTaskType, CrawlerTaskStatus (15분)

#### 🔴 Red: 테스트 작성
```java
// TaskIdTest.java
@Test
void shouldGenerateUniqueTaskId() {
    TaskId taskId1 = TaskId.generate();
    TaskId taskId2 = TaskId.generate();
    assertThat(taskId1).isNotEqualTo(taskId2);
}

// CrawlerTaskTypeTest.java
@Test
void shouldHaveThreeTaskTypes() {
    assertThat(CrawlerTaskType.values()).containsExactly(
        CrawlerTaskType.MINISHOP,
        CrawlerTaskType.PRODUCT_DETAIL,
        CrawlerTaskType.PRODUCT_OPTION
    );
}

// CrawlerTaskStatusTest.java
@Test
void shouldHaveAllRequiredStatuses() {
    assertThat(CrawlerTaskStatus.values()).hasSize(6);
}
```
- [x] 3개 테스트 파일 생성
- [x] 커밋: `test: TaskId, CrawlerTaskType, CrawlerTaskStatus 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
```java
public record TaskId(UUID value) {
    public static TaskId generate() {
        return new TaskId(UUID.randomUUID());
    }
}

public enum CrawlerTaskType {
    MINISHOP, PRODUCT_DETAIL, PRODUCT_OPTION
}

public enum CrawlerTaskStatus {
    WAITING, PUBLISHED, IN_PROGRESS, COMPLETED, FAILED, RETRY
}
```
- [x] 3개 클래스 구현
- [x] 커밋: `feat: TaskId, CrawlerTaskType, CrawlerTaskStatus 구현`

#### 🧹 Tidy: TestFixture 추가
- [x] CrawlerTaskFixture 클래스 생성
- [x] 커밋: `struct: CrawlerTaskFixture 추가`

---

### 5️⃣ Cycle 5: UserAgent VOs & Enums (15분)

#### 🔴 Red: 테스트 작성
```java
// UserAgentIdTest.java
@Test
void shouldGenerateUniqueUserAgentId() {
    UserAgentId id1 = UserAgentId.generate();
    UserAgentId id2 = UserAgentId.generate();
    assertThat(id1).isNotEqualTo(id2);
}

// UserAgentStatusTest.java
@Test
void shouldHaveThreeStatuses() {
    assertThat(UserAgentStatus.values()).containsExactly(
        UserAgentStatus.ACTIVE,
        UserAgentStatus.SUSPENDED,
        UserAgentStatus.BLOCKED
    );
}
```
- [x] 테스트 파일 생성
- [x] 커밋: `test: UserAgentId, UserAgentStatus 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
```java
public record UserAgentId(UUID value) {
    public static UserAgentId generate() {
        return new UserAgentId(UUID.randomUUID());
    }
}

public enum UserAgentStatus {
    ACTIVE, SUSPENDED, BLOCKED
}
```
- [x] 구현 완료
- [x] 커밋: `feat: UserAgentId, UserAgentStatus 구현`

#### 🧹 Tidy
- [x] UserAgentFixture 추가
- [x] 커밋: `struct: UserAgentFixture 추가`

---

### 6️⃣ Cycle 6: Product & ProductOutbox VOs (15분)

#### 🔴 Red: 테스트 작성
```java
// ProductIdTest.java, ItemNoTest.java, OutboxIdTest.java
// OutboxEventTypeTest.java, OutboxStatusTest.java
```
- [x] 5개 테스트 파일 생성
- [x] 커밋: `test: Product, ProductOutbox VO/Enum 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
```java
public record ProductId(UUID value) {
    public static ProductId generate() {
        return new ProductId(UUID.randomUUID());
    }
}

public record ItemNo(Long value) {
    public ItemNo {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("ItemNo는 양수여야 합니다");
        }
    }
}

public record OutboxId(UUID value) {
    public static OutboxId generate() {
        return new OutboxId(UUID.randomUUID());
    }
}

public enum OutboxEventType {
    PRODUCT_CREATED, PRODUCT_UPDATED
}

public enum OutboxStatus {
    WAITING, SENDING, COMPLETED, FAILED
}
```
- [x] 구현 완료
- [x] 커밋: `feat: Product, ProductOutbox VO/Enum 구현`

#### 🧹 Tidy
- [x] ProductFixture, ProductOutboxFixture 추가
- [x] 커밋: `struct: ProductFixture, ProductOutboxFixture 추가`

---

## 🎯 Phase 2: Seller Aggregate (4 Cycles)

### 7️⃣ Cycle 7: Seller Aggregate Root 생성 (15분) ✅ COMPLETE

#### 🔴 Red: 테스트 작성
```java
// domain/src/test/java/.../aggregate/SellerTest.java
@Test
void shouldRegisterSellerWithValidData() {
    SellerId sellerId = new SellerId("seller_123");
    String name = "테스트 셀러";
    Integer intervalDays = 1;

    Seller seller = Seller.register(sellerId, name, intervalDays);

    assertThat(seller.getSellerId()).isEqualTo(sellerId);
    assertThat(seller.getName()).isEqualTo(name);
    assertThat(seller.getStatus()).isEqualTo(SellerStatus.ACTIVE);
    assertThat(seller.getCrawlingIntervalDays()).isEqualTo(1);
}
```
- [x] SellerTest.java 생성
- [x] 커밋: `test: Seller Aggregate 생성 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
```java
// domain/src/main/java/.../aggregate/Seller.java
public class Seller {
    private SellerId sellerId;
    private String name;
    private CrawlingInterval crawlingInterval;
    private SellerStatus status;
    private Integer totalProductCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Seller(SellerId sellerId, String name, CrawlingInterval crawlingInterval) {
        this.sellerId = sellerId;
        this.name = name;
        this.crawlingInterval = crawlingInterval;
        this.status = SellerStatus.ACTIVE;
        this.totalProductCount = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static Seller register(SellerId sellerId, String name, Integer intervalDays) {
        return new Seller(sellerId, name, new CrawlingInterval(intervalDays));
    }

    // Law of Demeter 준수: Getter 체이닝 방지
    public Integer getCrawlingIntervalDays() {
        return crawlingInterval.days();
    }

    // Getters (필요한 것만)
    public SellerId getSellerId() { return sellerId; }
    public String getName() { return name; }
    public SellerStatus getStatus() { return status; }
}
```
- [x] Seller 클래스 구현
- [x] 테스트 통과 확인
- [x] 커밋: `feat: Seller Aggregate Root 구현 (register)`

#### ♻️ Refactor: Law of Demeter 검증
- [x] Law of Demeter 이미 준수 (getCrawlingIntervalDays() 제공)
- [x] ArchUnit 테스트는 프로젝트 레벨에 존재 (AggregateRootArchTest.java)

---

### 8️⃣ Cycle 8: Seller 주기 변경 (15분) ✅ COMPLETE

#### 🔴 Red: 테스트 작성
```java
@Test
void shouldUpdateCrawlingInterval() {
    Seller seller = SellerFixture.defaultSeller();
    Integer newIntervalDays = 7;

    seller.updateInterval(newIntervalDays);

    assertThat(seller.getCrawlingIntervalDays()).isEqualTo(7);
}

@Test
void shouldThrowExceptionWhenUpdateIntervalWithInvalidDays() {
    Seller seller = SellerFixture.defaultSeller();

    assertThatThrownBy(() -> seller.updateInterval(31))
        .isInstanceOf(IllegalArgumentException.class);
}
```
- [x] 테스트 추가
- [x] 커밋: `test: Seller 주기 변경 테스트 추가`

#### 🟢 Green: 최소 구현
```java
public void updateInterval(Integer newIntervalDays) {
    this.crawlingInterval = new CrawlingInterval(newIntervalDays);
    this.updatedAt = LocalDateTime.now();
}
```
- [x] updateInterval 메서드 구현
- [x] 커밋: `feat: Seller 주기 변경 구현 (updateInterval)`

---

### 9️⃣ Cycle 9: Seller 활성화/비활성화 (15분) ✅ COMPLETE

#### 🔴 Red: 테스트 작성
```java
@Test
void shouldActivateSeller() {
    Seller seller = SellerFixture.inactiveSeller();
    seller.activate();
    assertThat(seller.getStatus()).isEqualTo(SellerStatus.ACTIVE);
}

@Test
void shouldDeactivateSeller() {
    Seller seller = SellerFixture.defaultSeller();
    seller.deactivate();
    assertThat(seller.getStatus()).isEqualTo(SellerStatus.INACTIVE);
}
```
- [x] 테스트 추가
- [x] 커밋: `test: Seller 활성화/비활성화 테스트 추가`

#### 🟢 Green: 최소 구현
```java
public void activate() {
    this.status = SellerStatus.ACTIVE;
    this.updatedAt = LocalDateTime.now();
}

public void deactivate() {
    this.status = SellerStatus.INACTIVE;
    this.updatedAt = LocalDateTime.now();
}
```
- [x] 메서드 구현
- [x] 커밋: `feat: Seller 활성화/비활성화 구현 (activate/deactivate)`

---

### 🔟 Cycle 10: Seller 상품 수 업데이트 (10분) ✅ COMPLETE

#### 🔴 Red: 테스트 작성
```java
@Test
void shouldUpdateTotalProductCount() {
    Seller seller = SellerFixture.defaultSeller();
    seller.updateTotalProductCount(100);
    assertThat(seller.getTotalProductCount()).isEqualTo(100);
}
```
- [x] 테스트 추가
- [x] 커밋: `test: Seller 상품 수 업데이트 테스트 추가`

#### 🟢 Green: 최소 구현
```java
public void updateTotalProductCount(Integer count) {
    this.totalProductCount = count;
    this.updatedAt = LocalDateTime.now();
}

public Integer getTotalProductCount() {
    return totalProductCount;
}
```
- [x] 메서드 구현
- [x] 커밋: `feat: Seller 상품 수 업데이트 구현 (updateTotalProductCount)`

---

## 🎯 Phase 3: CrawlerTask Aggregate (5 Cycles)

### 1️⃣1️⃣ Cycle 11: CrawlerTask 생성 (15분) ✅ COMPLETE

#### 🔴 Red: 테스트 작성
```java
// domain/src/test/java/.../aggregate/CrawlerTaskTest.java
@Test
void shouldCreateCrawlerTaskWithWaitingStatus() {
    SellerId sellerId = SellerFixture.defaultSellerId();
    CrawlerTaskType taskType = CrawlerTaskType.MINISHOP;
    String requestUrl = "/mustit-api/facade-api/v1/searchmini-shop-search?seller_id=123";

    CrawlerTask task = CrawlerTask.create(sellerId, taskType, requestUrl);

    assertThat(task.getTaskId()).isNotNull();
    assertThat(task.getSellerId()).isEqualTo(sellerId);
    assertThat(task.getTaskType()).isEqualTo(taskType);
    assertThat(task.getRequestUrl()).isEqualTo(requestUrl);
    assertThat(task.getStatus()).isEqualTo(CrawlerTaskStatus.WAITING);
    assertThat(task.getRetryCount()).isEqualTo(0);
}

@Test
void shouldValidateMinishopUrlFormat() {
    assertThatThrownBy(() -> CrawlerTask.create(
        SellerFixture.defaultSellerId(),
        CrawlerTaskType.MINISHOP,
        "/invalid-url"
    )).isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("MINISHOP URL 형식이 올바르지 않습니다");
}
```
- [x] 테스트 파일 생성
- [x] 커밋: `test: CrawlerTask 생성 및 URL 검증 테스트 추가`

#### 🟢 Green: 최소 구현
```java
// domain/src/main/java/.../aggregate/CrawlerTask.java
public class CrawlerTask {
    private TaskId taskId;
    private SellerId sellerId;
    private CrawlerTaskType taskType;
    private String requestUrl;
    private CrawlerTaskStatus status;
    private Integer retryCount;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private CrawlerTask(SellerId sellerId, CrawlerTaskType taskType, String requestUrl) {
        validateRequestUrl(taskType, requestUrl);
        this.taskId = TaskId.generate();
        this.sellerId = sellerId;
        this.taskType = taskType;
        this.requestUrl = requestUrl;
        this.status = CrawlerTaskStatus.WAITING;
        this.retryCount = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static CrawlerTask create(SellerId sellerId, CrawlerTaskType taskType, String requestUrl) {
        return new CrawlerTask(sellerId, taskType, requestUrl);
    }

    private void validateRequestUrl(CrawlerTaskType taskType, String requestUrl) {
        switch (taskType) {
            case MINISHOP -> {
                if (!requestUrl.contains("/searchmini-shop-search")) {
                    throw new IllegalArgumentException("MINISHOP URL 형식이 올바르지 않습니다");
                }
            }
            case PRODUCT_DETAIL -> {
                if (!requestUrl.matches(".*/item/\\d+/detail/top")) {
                    throw new IllegalArgumentException("PRODUCT_DETAIL URL 형식이 올바르지 않습니다");
                }
            }
            case PRODUCT_OPTION -> {
                if (!requestUrl.matches(".*/auction_products/\\d+/options")) {
                    throw new IllegalArgumentException("PRODUCT_OPTION URL 형식이 올바르지 않습니다");
                }
            }
        }
    }

    // Getters
    public TaskId getTaskId() { return taskId; }
    public SellerId getSellerId() { return sellerId; }
    public CrawlerTaskType getTaskType() { return taskType; }
    public String getRequestUrl() { return requestUrl; }
    public CrawlerTaskStatus getStatus() { return status; }
    public Integer getRetryCount() { return retryCount; }
}
```
- [x] CrawlerTask 클래스 구현
- [x] 테스트 통과 확인
- [x] 커밋: `feat: CrawlerTask 생성 구현 (create, URL 검증)`

---

### 1️⃣2️⃣ Cycle 12: CrawlerTask 상태 전환 (Publish, Start) (15분) ✅ COMPLETE

#### 🔴 Red: 테스트 작성
```java
@Test
void shouldPublishTaskFromWaiting() {
    CrawlerTask task = CrawlerTaskFixture.waitingTask();
    task.publish();
    assertThat(task.getStatus()).isEqualTo(CrawlerTaskStatus.PUBLISHED);
}

@Test
void shouldStartTaskFromPublished() {
    CrawlerTask task = CrawlerTaskFixture.publishedTask();
    task.start();
    assertThat(task.getStatus()).isEqualTo(CrawlerTaskStatus.IN_PROGRESS);
}

@Test
void shouldThrowExceptionWhenPublishNonWaitingTask() {
    CrawlerTask task = CrawlerTaskFixture.publishedTask();
    assertThatThrownBy(() -> task.publish())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("WAITING 상태에서만 발행할 수 있습니다");
}
```
- [x] 테스트 추가
- [x] 커밋: `test: CrawlerTask 상태 전환 테스트 추가 (publish, start)`

#### 🟢 Green: 최소 구현
```java
public void publish() {
    if (status != CrawlerTaskStatus.WAITING) {
        throw new IllegalStateException("WAITING 상태에서만 발행할 수 있습니다");
    }
    this.status = CrawlerTaskStatus.PUBLISHED;
    this.updatedAt = LocalDateTime.now();
}

public void start() {
    if (status != CrawlerTaskStatus.PUBLISHED) {
        throw new IllegalStateException("PUBLISHED 상태에서만 시작할 수 있습니다");
    }
    this.status = CrawlerTaskStatus.IN_PROGRESS;
    this.updatedAt = LocalDateTime.now();
}
```
- [x] 메서드 구현
- [x] 커밋: `feat: CrawlerTask 상태 전환 구현 (publish, start)`

---

### 1️⃣3️⃣ Cycle 13: CrawlerTask 완료/실패 (15분) ✅ COMPLETE

#### 🔴 Red: 테스트 작성
```java
@Test
void shouldCompleteTaskFromInProgress() {
    CrawlerTask task = CrawlerTaskFixture.inProgressTask();
    task.complete();
    assertThat(task.getStatus()).isEqualTo(CrawlerTaskStatus.COMPLETED);
}

@Test
void shouldFailTaskWithErrorMessage() {
    CrawlerTask task = CrawlerTaskFixture.inProgressTask();
    String errorMessage = "429 Too Many Requests";

    task.fail(errorMessage);

    assertThat(task.getStatus()).isEqualTo(CrawlerTaskStatus.FAILED);
    assertThat(task.getErrorMessage()).isEqualTo(errorMessage);
}
```
- [x] 테스트 추가
- [x] 커밋: `test: CrawlerTask 완료/실패 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
```java
public void complete() {
    if (status != CrawlerTaskStatus.IN_PROGRESS) {
        throw new IllegalStateException("IN_PROGRESS 상태에서만 완료할 수 있습니다");
    }
    this.status = CrawlerTaskStatus.COMPLETED;
    this.updatedAt = LocalDateTime.now();
}

public void fail(String errorMessage) {
    if (status != CrawlerTaskStatus.IN_PROGRESS) {
        throw new IllegalStateException("IN_PROGRESS 상태에서만 실패 처리할 수 있습니다");
    }
    this.status = CrawlerTaskStatus.FAILED;
    this.errorMessage = errorMessage;
    this.updatedAt = LocalDateTime.now();
}

public String getErrorMessage() {
    return errorMessage;
}
```
- [x] 메서드 구현
- [x] 커밋: `feat: CrawlerTask 완료/실패 구현`

---

### 1️⃣4️⃣ Cycle 14: CrawlerTask 재시도 로직 (15분) ✅ COMPLETE

#### 🔴 Red: 테스트 작성
```java
@Test
void shouldRetryWhenRetryCountLessThan2() {
    CrawlerTask task = CrawlerTaskFixture.inProgressTask();
    task.fail("Network error");

    task.retry();

    assertThat(task.getStatus()).isEqualTo(CrawlerTaskStatus.RETRY);
    assertThat(task.getRetryCount()).isEqualTo(1);
}

@Test
void shouldNotRetryWhenRetryCountExceeds2() {
    CrawlerTask task = CrawlerTaskFixture.taskWithRetryCount(2);
    task.fail("Network error");

    assertThatThrownBy(() -> task.retry())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("재시도 횟수를 초과했습니다 (최대 2회)");
}

@Test
void shouldResetErrorMessageOnRetry() {
    CrawlerTask task = CrawlerTaskFixture.inProgressTask();
    task.fail("Network error");

    task.retry();

    assertThat(task.getErrorMessage()).isNull();
}
```
- [x] 테스트 추가
- [x] 커밋: `test: CrawlerTask 재시도 로직 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
```java
public void retry() {
    if (status != CrawlerTaskStatus.FAILED) {
        throw new IllegalStateException("FAILED 상태에서만 재시도할 수 있습니다");
    }
    if (retryCount >= 2) {
        throw new IllegalStateException("재시도 횟수를 초과했습니다 (최대 2회)");
    }
    this.status = CrawlerTaskStatus.RETRY;
    this.retryCount++;
    this.errorMessage = null;
    this.updatedAt = LocalDateTime.now();
}

// start() 메서드도 RETRY 상태 허용하도록 수정
public void start() {
    if (status != CrawlerTaskStatus.PUBLISHED && status != CrawlerTaskStatus.RETRY) {
        throw new IllegalStateException("PUBLISHED 또는 RETRY 상태에서만 시작할 수 있습니다");
    }
    this.status = CrawlerTaskStatus.IN_PROGRESS;
    this.updatedAt = LocalDateTime.now();
}
```
- [x] retry 메서드 구현
- [x] start 메서드 수정 (RETRY → IN_PROGRESS 허용)
- [x] 커밋: `feat: CrawlerTask 재시도 로직 구현 (최대 2회)`

---

### 1️⃣5️⃣ Cycle 15: CrawlerTask Fixture 정리 (10분) ✅ COMPLETE

#### 🧹 Tidy: CrawlerTaskFixture 완성
```java
// domain/src/testFixtures/java/.../fixture/CrawlerTaskFixture.java
public class CrawlerTaskFixture {
    public static CrawlerTask waitingTask() {
        return CrawlerTask.create(
            new SellerId("seller_test_001"),
            CrawlerTaskType.MINISHOP,
            DEFAULT_REQUEST_URL
        );
    }

    public static CrawlerTask publishedTask() {
        CrawlerTask task = waitingTask();
        task.publish();
        return task;
    }

    public static CrawlerTask inProgressTask() {
        CrawlerTask task = publishedTask();
        task.start();
        return task;
    }

    public static CrawlerTask taskWithRetryCount(int retryCount) {
        CrawlerTask task = inProgressTask();
        for (int i = 0; i < retryCount; i++) {
            task.fail("Test error");
            if (i < 2) { // MAX_RETRY_COUNT = 2
                task.retry();
                task.start();
            }
        }
        if (retryCount < 2) {
            task.fail("Test error");
        }
        return task;
    }
}
```
- [x] CrawlerTaskFixture 완성
- [x] 커밋: `struct: CrawlerTaskFixture 완성 (모든 상태 생성 메서드)`

---

## 🎯 Phase 4: UserAgent Aggregate (4 Cycles)

### 1️⃣6️⃣ Cycle 16: UserAgent 생성 (15분) ✅ COMPLETE

#### 🔴 Red: 테스트 작성
```java
// domain/src/test/java/.../aggregate/UserAgentTest.java
@Test
void shouldCreateUserAgentWithActiveStatus() {
    String userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)...";

    UserAgent userAgent = UserAgent.create(userAgentString);

    assertThat(userAgent.getUserAgentId()).isNotNull();
    assertThat(userAgent.getUserAgentString()).isEqualTo(userAgentString);
    assertThat(userAgent.getToken()).isNull();
    assertThat(userAgent.getStatus()).isEqualTo(UserAgentStatus.ACTIVE);
    assertThat(userAgent.getRequestCount()).isEqualTo(0);
}

@Test
void shouldThrowExceptionWhenUserAgentStringIsBlank() {
    assertThatThrownBy(() -> UserAgent.create(""))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("UserAgent 문자열은 비어있을 수 없습니다");
}
```
- [x] 테스트 파일 생성
- [x] 커밋: `test: UserAgent 생성 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
```java
// domain/src/main/java/.../aggregate/UserAgent.java
public class UserAgent {
    private final UserAgentId userAgentId;
    private final String userAgentString;
    private String token;
    private UserAgentStatus status;
    private Integer requestCount;
    private LocalDateTime lastRequestAt;
    private LocalDateTime tokenIssuedAt;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private UserAgent(String userAgentString) {
        if (userAgentString == null || userAgentString.isBlank()) {
            throw new IllegalArgumentException("UserAgent 문자열은 비어있을 수 없습니다");
        }
        this.userAgentId = UserAgentId.generate();
        this.userAgentString = userAgentString;
        this.status = UserAgentStatus.ACTIVE;
        this.requestCount = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static UserAgent create(String userAgentString) {
        return new UserAgent(userAgentString);
    }

    // Getters
    public UserAgentId getUserAgentId() { return userAgentId; }
    public String getUserAgentString() { return userAgentString; }
    public String getToken() { return token; }
    public UserAgentStatus getStatus() { return status; }
    public Integer getRequestCount() { return requestCount; }
}
```
- [x] UserAgent 클래스 구현
- [x] 커밋: `feat: UserAgent Aggregate Root 구현 (create)`

---

### 1️⃣7️⃣ Cycle 17: UserAgent 토큰 발급 (15분) ✅ COMPLETE

#### 🔴 Red: 테스트 작성
```java
@Test
void shouldIssueToken() {
    UserAgent userAgent = UserAgentFixture.defaultUserAgent();
    String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";

    userAgent.issueToken(token);

    assertThat(userAgent.getToken()).isEqualTo(token);
    assertThat(userAgent.getTokenIssuedAt()).isNotNull();
}

@Test
void shouldThrowExceptionWhenTokenIsBlank() {
    UserAgent userAgent = UserAgentFixture.defaultUserAgent();

    assertThatThrownBy(() -> userAgent.issueToken(""))
        .isInstanceOf(IllegalArgumentException.class);
}
```
- [x] 테스트 추가
- [x] 커밋: `test: UserAgent 토큰 발급 테스트 추가`

#### 🟢 Green: 최소 구현
```java
public void issueToken(String token) {
    if (token == null || token.isBlank()) {
        throw new IllegalArgumentException("토큰은 비어있을 수 없습니다");
    }
    this.token = token;
    this.tokenIssuedAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
}

public LocalDateTime getTokenIssuedAt() {
    return tokenIssuedAt;
}
```
- [x] issueToken 메서드 구현
- [x] 커밋: `feat: UserAgent 토큰 발급 구현 (issueToken)`

---

### 1️⃣8️⃣ Cycle 18: UserAgent 토큰 버킷 리미터 (Tell Don't Ask) (15분) ✅ COMPLETE

#### 🔴 Red: 테스트 작성
```java
@Test
void shouldAllowRequestWhenUnder80RequestsPerHour() {
    UserAgent userAgent = UserAgentFixture.userAgentWithToken();
    userAgent.setRequestCount(79);
    userAgent.setLastRequestAt(LocalDateTime.now().minusMinutes(30));

    boolean canRequest = userAgent.canMakeRequest();

    assertThat(canRequest).isTrue();
}

@Test
void shouldBlockRequestWhen80RequestsReachedInSameHour() {
    UserAgent userAgent = UserAgentFixture.userAgentWithToken();
    userAgent.setRequestCount(80);
    userAgent.setLastRequestAt(LocalDateTime.now().minusMinutes(30));

    boolean canRequest = userAgent.canMakeRequest();

    assertThat(canRequest).isFalse();
}

@Test
void shouldResetRequestCountAfter1Hour() {
    UserAgent userAgent = UserAgentFixture.userAgentWithToken();
    userAgent.setRequestCount(80);
    userAgent.setLastRequestAt(LocalDateTime.now().minusHours(2));

    boolean canRequest = userAgent.canMakeRequest();

    assertThat(canRequest).isTrue();
    assertThat(userAgent.getRequestCount()).isEqualTo(0);
}

@Test
void shouldNotAllowRequestWhenTokenIsNull() {
    UserAgent userAgent = UserAgentFixture.defaultUserAgent(); // token = null

    boolean canRequest = userAgent.canMakeRequest();

    assertThat(canRequest).isFalse();
}
```
- [x] 테스트 추가 (Tell Don't Ask 패턴)
- [x] 커밋: `test: UserAgent 토큰 버킷 리미터 테스트 추가 (Tell Don't Ask)`

#### 🟢 Green: 최소 구현 (Tell Don't Ask)
```java
private static final int MAX_REQUESTS_PER_HOUR = 80;

// Tell Don't Ask: 외부에서 판단하지 않고 객체가 스스로 판단
public boolean canMakeRequest() {
    if (token == null) {
        return false;
    }

    LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);

    // 1시간 경과 시 requestCount 리셋
    if (lastRequestAt != null && lastRequestAt.isBefore(oneHourAgo)) {
        this.requestCount = 0;
    }

    return requestCount < MAX_REQUESTS_PER_HOUR;
}

public void incrementRequestCount() {
    this.requestCount++;
    this.lastRequestAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
}

public void resetRequestCount() {
    this.requestCount = 0;
    this.updatedAt = LocalDateTime.now();
}
```
- [x] canMakeRequest 메서드 구현 (Tell Don't Ask)
- [x] 커밋: `feat: UserAgent 토큰 버킷 리미터 구현 (80 req/hour, Tell Don't Ask)`

#### ♻️ Refactor: ArchUnit 테스트 추가 (Optional - Application Layer 구현 후)
```java
// Tell Don't Ask 위반 검증 (외부에서 getRequestCount() < 80 판단 금지)
@ArchTest
static final ArchRule tell_dont_ask_rule = methods()
    .that().areDeclaredInClassesThat().resideInPackage("..application..")
    .should().notCallMethod(UserAgent.class, "getRequestCount")
    .because("Tell Don't Ask: canMakeRequest()를 사용해야 합니다");
```
- [ ] ArchUnit 테스트 추가 (Application Layer 구현 후)
- [ ] 커밋: `struct: UserAgent Tell Don't Ask ArchUnit 테스트 추가`

---

### 1️⃣9️⃣ Cycle 19: UserAgent 상태 전환 (Suspend, Activate) (15분) ✅ COMPLETE

#### 🔴 Red: 테스트 작성
```java
@Test
void shouldSuspendUserAgentOn429Response() {
    UserAgent userAgent = UserAgentFixture.userAgentWithToken();

    userAgent.suspend();

    assertThat(userAgent.getStatus()).isEqualTo(UserAgentStatus.SUSPENDED);
}

@Test
void shouldActivateUserAgent() {
    UserAgent userAgent = UserAgentFixture.suspendedUserAgent();

    userAgent.activate();

    assertThat(userAgent.getStatus()).isEqualTo(UserAgentStatus.ACTIVE);
}
```
- [x] 테스트 추가
- [x] 커밋: `test: UserAgent 상태 전환 테스트 추가 (suspend, activate)`

#### 🟢 Green: 최소 구현
```java
public void suspend() {
    this.status = UserAgentStatus.SUSPENDED;
    this.updatedAt = LocalDateTime.now();
}

public void activate() {
    this.status = UserAgentStatus.ACTIVE;
    this.updatedAt = LocalDateTime.now();
}
```
- [x] 메서드 구현
- [x] 커밋: `feat: UserAgent 상태 전환 구현 (suspend, activate)`

#### 🧹 Tidy: UserAgentFixture 완성
- [x] 커밋: `struct: UserAgentFixture Aggregate 생성 메서드 추가`

---

## 🎯 Phase 5: Product Aggregate (3 Cycles)

### 2️⃣0️⃣ Cycle 20: Product 생성 (15분) ✅ COMPLETE

#### 🔴 Red: 테스트 작성
```java
// domain/src/test/java/.../aggregate/ProductTest.java
@Test
void shouldCreateProductWithIncompleteStatus() {
    ItemNo itemNo = new ItemNo(123456L);
    SellerId sellerId = SellerFixture.defaultSellerId();

    Product product = Product.create(itemNo, sellerId);

    assertThat(product.getProductId()).isNotNull();
    assertThat(product.getItemNo()).isEqualTo(itemNo);
    assertThat(product.getSellerId()).isEqualTo(sellerId);
    assertThat(product.isComplete()).isFalse();
}
```
- [x] 테스트 파일 생성
- [x] 커밋: `test: Product 생성 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
```java
// domain/src/main/java/.../aggregate/Product.java
public class Product {
    private ProductId productId;
    private ItemNo itemNo;
    private SellerId sellerId;
    private String minishopDataHash;
    private String detailDataHash;
    private String optionDataHash;
    private Boolean isComplete;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Product(ItemNo itemNo, SellerId sellerId) {
        this.productId = ProductId.generate();
        this.itemNo = itemNo;
        this.sellerId = sellerId;
        this.isComplete = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static Product create(ItemNo itemNo, SellerId sellerId) {
        return new Product(itemNo, sellerId);
    }

    public boolean isComplete() {
        return Boolean.TRUE.equals(isComplete);
    }

    // Getters
    public ProductId getProductId() { return productId; }
    public ItemNo getItemNo() { return itemNo; }
    public SellerId getSellerId() { return sellerId; }
}
```
- [x] Product 클래스 구현
- [x] 커밋: `feat: Product Aggregate Root 구현 (create)`

---

### 2️⃣1️⃣ Cycle 21: Product 데이터 업데이트 및 해시 계산 (15분)

#### 🔴 Red: 테스트 작성
```java
@Test
void shouldUpdateMinishopDataWithHash() {
    Product product = ProductFixture.defaultProduct();
    String rawJson = "{\"itemNo\":123456,\"name\":\"상품명\"}";

    boolean hasChanged = product.updateMinishopData(rawJson);

    assertThat(product.getMinishopDataHash()).isNotNull();
    assertThat(hasChanged).isTrue();
}

@Test
void shouldDetectNoChangeWhenSameData() {
    Product product = ProductFixture.defaultProduct();
    String rawJson = "{\"itemNo\":123456}";
    product.updateMinishopData(rawJson);
    String sameJson = "{\"itemNo\":123456}";

    boolean hasChanged = product.updateMinishopData(sameJson);

    assertThat(hasChanged).isFalse();
}

@Test
void shouldMarkCompleteWhenAllDataUpdated() {
    Product product = ProductFixture.defaultProduct();

    product.updateMinishopData("{\"data\":\"minishop\"}");
    product.updateDetailData("{\"data\":\"detail\"}");
    product.updateOptionData("{\"data\":\"option\"}");

    assertThat(product.isComplete()).isTrue();
}
```
- [ ] 테스트 추가
- [ ] 커밋: `test: Product 데이터 업데이트 및 해시 계산 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
```java
public boolean updateMinishopData(String rawJson) {
    String newHash = calculateMD5Hash(rawJson);
    boolean hasChanged = !newHash.equals(minishopDataHash);
    this.minishopDataHash = newHash;
    updateCompleteStatus();
    this.updatedAt = LocalDateTime.now();
    return hasChanged;
}

public boolean updateDetailData(String rawJson) {
    String newHash = calculateMD5Hash(rawJson);
    boolean hasChanged = !newHash.equals(detailDataHash);
    this.detailDataHash = newHash;
    updateCompleteStatus();
    this.updatedAt = LocalDateTime.now();
    return hasChanged;
}

public boolean updateOptionData(String rawJson) {
    String newHash = calculateMD5Hash(rawJson);
    boolean hasChanged = !newHash.equals(optionDataHash);
    this.optionDataHash = newHash;
    updateCompleteStatus();
    this.updatedAt = LocalDateTime.now();
    return hasChanged;
}

private void updateCompleteStatus() {
    this.isComplete = (minishopDataHash != null && detailDataHash != null && optionDataHash != null);
}

private String calculateMD5Hash(String data) {
    try {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] hashBytes = md.digest(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    } catch (NoSuchAlgorithmException e) {
        throw new RuntimeException("MD5 알고리즘을 사용할 수 없습니다", e);
    }
}

public String getMinishopDataHash() { return minishopDataHash; }
```
- [ ] 데이터 업데이트 및 해시 계산 구현
- [ ] 커밋: `feat: Product 데이터 업데이트 및 MD5 해시 계산 구현`

#### ♻️ Refactor: 해시 계산 메서드 추출
- [ ] calculateMD5Hash 메서드 Value Object로 추출 고려
- [ ] 커밋: `struct: Product 해시 계산 로직 정리` (필요 시)

---

### 2️⃣2️⃣ Cycle 22: Product 변경 감지 (Tell Don't Ask) (10분)

#### 🔴 Red: 테스트 작성
```java
@Test
void shouldDetectChange() {
    String oldHash = "abc123";
    String newHash = "def456";

    boolean hasChanged = Product.hasChanged(oldHash, newHash);

    assertThat(hasChanged).isTrue();
}

@Test
void shouldDetectNoChange() {
    String sameHash = "abc123";

    boolean hasChanged = Product.hasChanged(sameHash, sameHash);

    assertThat(hasChanged).isFalse();
}
```
- [ ] 테스트 추가
- [ ] 커밋: `test: Product 변경 감지 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
```java
public static boolean hasChanged(String oldHash, String newHash) {
    if (oldHash == null && newHash == null) {
        return false;
    }
    if (oldHash == null || newHash == null) {
        return true;
    }
    return !oldHash.equals(newHash);
}
```
- [ ] hasChanged 정적 메서드 구현
- [ ] 커밋: `feat: Product 변경 감지 구현 (hasChanged)`

---

## 🎯 Phase 6: ProductOutbox Aggregate (3 Cycles)

### 2️⃣3️⃣ Cycle 23: ProductOutbox 생성 (15분)

#### 🔴 Red: 테스트 작성
```java
// domain/src/test/java/.../aggregate/ProductOutboxTest.java
@Test
void shouldCreateProductOutboxWithWaitingStatus() {
    ProductId productId = ProductFixture.defaultProduct().getProductId();
    OutboxEventType eventType = OutboxEventType.PRODUCT_CREATED;
    String payload = "{\"itemNo\":123456,\"name\":\"상품명\"}";

    ProductOutbox outbox = ProductOutbox.create(productId, eventType, payload);

    assertThat(outbox.getOutboxId()).isNotNull();
    assertThat(outbox.getProductId()).isEqualTo(productId);
    assertThat(outbox.getEventType()).isEqualTo(eventType);
    assertThat(outbox.getPayload()).isEqualTo(payload);
    assertThat(outbox.getStatus()).isEqualTo(OutboxStatus.WAITING);
    assertThat(outbox.getRetryCount()).isEqualTo(0);
}
```
- [ ] 테스트 파일 생성
- [ ] 커밋: `test: ProductOutbox 생성 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
```java
// domain/src/main/java/.../aggregate/ProductOutbox.java
public class ProductOutbox {
    private OutboxId outboxId;
    private ProductId productId;
    private OutboxEventType eventType;
    private String payload;
    private OutboxStatus status;
    private Integer retryCount;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private ProductOutbox(ProductId productId, OutboxEventType eventType, String payload) {
        this.outboxId = OutboxId.generate();
        this.productId = productId;
        this.eventType = eventType;
        this.payload = payload;
        this.status = OutboxStatus.WAITING;
        this.retryCount = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static ProductOutbox create(ProductId productId, OutboxEventType eventType, String payload) {
        return new ProductOutbox(productId, eventType, payload);
    }

    // Getters
    public OutboxId getOutboxId() { return outboxId; }
    public ProductId getProductId() { return productId; }
    public OutboxEventType getEventType() { return eventType; }
    public String getPayload() { return payload; }
    public OutboxStatus getStatus() { return status; }
    public Integer getRetryCount() { return retryCount; }
}
```
- [ ] ProductOutbox 클래스 구현
- [ ] 커밋: `feat: ProductOutbox Aggregate Root 구현 (create)`

---

### 2️⃣4️⃣ Cycle 24: ProductOutbox 전송 상태 전환 (15분)

#### 🔴 Red: 테스트 작성
```java
@Test
void shouldSendOutbox() {
    ProductOutbox outbox = ProductOutboxFixture.waitingOutbox();

    outbox.send();

    assertThat(outbox.getStatus()).isEqualTo(OutboxStatus.SENDING);
}

@Test
void shouldCompleteOutbox() {
    ProductOutbox outbox = ProductOutboxFixture.sendingOutbox();

    outbox.complete();

    assertThat(outbox.getStatus()).isEqualTo(OutboxStatus.COMPLETED);
}

@Test
void shouldFailOutbox() {
    ProductOutbox outbox = ProductOutboxFixture.sendingOutbox();
    String errorMessage = "HTTP 500 Internal Server Error";

    outbox.fail(errorMessage);

    assertThat(outbox.getStatus()).isEqualTo(OutboxStatus.FAILED);
    assertThat(outbox.getErrorMessage()).isEqualTo(errorMessage);
}
```
- [ ] 테스트 추가
- [ ] 커밋: `test: ProductOutbox 상태 전환 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
```java
public void send() {
    if (status != OutboxStatus.WAITING) {
        throw new IllegalStateException("WAITING 상태에서만 전송할 수 있습니다");
    }
    this.status = OutboxStatus.SENDING;
    this.updatedAt = LocalDateTime.now();
}

public void complete() {
    if (status != OutboxStatus.SENDING) {
        throw new IllegalStateException("SENDING 상태에서만 완료할 수 있습니다");
    }
    this.status = OutboxStatus.COMPLETED;
    this.updatedAt = LocalDateTime.now();
}

public void fail(String errorMessage) {
    if (status != OutboxStatus.SENDING) {
        throw new IllegalStateException("SENDING 상태에서만 실패할 수 있습니다");
    }
    this.status = OutboxStatus.FAILED;
    this.errorMessage = errorMessage;
    this.retryCount++;
    this.updatedAt = LocalDateTime.now();
}

public String getErrorMessage() { return errorMessage; }
```
- [ ] 상태 전환 메서드 구현
- [ ] 커밋: `feat: ProductOutbox 상태 전환 구현 (send, complete, fail)`

---

### 2️⃣5️⃣ Cycle 25: ProductOutbox 재시도 로직 (Tell Don't Ask) (15분)

#### 🔴 Red: 테스트 작성
```java
@Test
void shouldAllowRetryWhenCountLessThan5() {
    ProductOutbox outbox = ProductOutboxFixture.failedOutboxWithRetryCount(3);

    boolean canRetry = outbox.canRetry();

    assertThat(canRetry).isTrue();
}

@Test
void shouldNotAllowRetryWhenCountExceeds5() {
    ProductOutbox outbox = ProductOutboxFixture.failedOutboxWithRetryCount(5);

    boolean canRetry = outbox.canRetry();

    assertThat(canRetry).isFalse();
}
```
- [ ] 테스트 추가 (Tell Don't Ask)
- [ ] 커밋: `test: ProductOutbox 재시도 로직 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현 (Tell Don't Ask)
```java
private static final int MAX_RETRY_COUNT = 5;

// Tell Don't Ask: 외부에서 판단하지 않고 객체가 스스로 판단
public boolean canRetry() {
    return retryCount < MAX_RETRY_COUNT;
}
```
- [ ] canRetry 메서드 구현
- [ ] 커밋: `feat: ProductOutbox 재시도 로직 구현 (최대 5회)`

#### ♻️ Refactor: ArchUnit 테스트 추가
```java
@ArchTest
static final ArchRule tell_dont_ask_outbox_rule = methods()
    .that().areDeclaredInClassesThat().resideInPackage("..application..")
    .should().notCallMethod(ProductOutbox.class, "getRetryCount")
    .because("Tell Don't Ask: canRetry()를 사용해야 합니다");
```
- [ ] ArchUnit 테스트 추가
- [ ] 커밋: `struct: ProductOutbox Tell Don't Ask ArchUnit 테스트 추가`

---

## ✅ 완료 조건 체크리스트

### Phase 1: Value Objects & Enums (6 Cycles)
- [x] SellerId VO (Cycle 1) ✅ 2025-11-15
- [x] CrawlingInterval VO (Cycle 2) ✅ 2025-11-15
- [x] SellerStatus Enum (Cycle 3) ✅ 2025-11-15
- [x] TaskId, CrawlerTaskType, CrawlerTaskStatus (Cycle 4) ✅ 2025-11-15
- [x] UserAgentId, UserAgentStatus (Cycle 5) ✅ 2025-11-15
- [x] ProductId, ItemNo, OutboxId, OutboxEventType, OutboxStatus (Cycle 6) ✅ 2025-11-15

### Phase 2: Seller Aggregate (4 Cycles)
- [ ] Seller 생성 (Cycle 7)
- [ ] Seller 주기 변경 (Cycle 8)
- [ ] Seller 활성화/비활성화 (Cycle 9)
- [ ] Seller 상품 수 업데이트 (Cycle 10)

### Phase 3: CrawlerTask Aggregate (5 Cycles)
- [ ] CrawlerTask 생성 및 URL 검증 (Cycle 11)
- [ ] CrawlerTask 상태 전환 (Publish, Start) (Cycle 12)
- [ ] CrawlerTask 완료/실패 (Cycle 13)
- [ ] CrawlerTask 재시도 로직 (Cycle 14)
- [ ] CrawlerTaskFixture 정리 (Cycle 15)

### Phase 4: UserAgent Aggregate (4 Cycles)
- [x] UserAgent 생성 (Cycle 16)
- [x] UserAgent 토큰 발급 (Cycle 17)
- [x] UserAgent 토큰 버킷 리미터 (Tell Don't Ask) (Cycle 18)
- [x] UserAgent 상태 전환 (Cycle 19)

### Phase 5: Product Aggregate (3 Cycles)
- [ ] Product 생성 (Cycle 20)
- [ ] Product 데이터 업데이트 및 해시 계산 (Cycle 21)
- [ ] Product 변경 감지 (Cycle 22)

### Phase 6: ProductOutbox Aggregate (3 Cycles)
- [ ] ProductOutbox 생성 (Cycle 23)
- [ ] ProductOutbox 상태 전환 (Cycle 24)
- [ ] ProductOutbox 재시도 로직 (Tell Don't Ask) (Cycle 25)

### Zero-Tolerance 규칙 준수
- [ ] Lombok 사용하지 않음 (Plain Java/Record)
- [ ] Law of Demeter 준수 (Getter 체이닝 금지)
- [ ] Tell Don't Ask 준수 (canMakeRequest, canRetry)
- [ ] Long FK 전략 준수 (관계 어노테이션 금지)

### ArchUnit 테스트
- [ ] Lombok 금지 검증
- [ ] Getter 체이닝 금지 검증
- [ ] Tell Don't Ask 검증 (UserAgent, ProductOutbox)
- [ ] 패키지 의존성 검증

### TestFixture 패턴
- [ ] SellerFixture 완성
- [ ] CrawlerTaskFixture 완성
- [ ] UserAgentFixture 완성
- [ ] ProductFixture 완성
- [ ] ProductOutboxFixture 완성

### 테스트 커버리지
- [ ] Unit Test 커버리지 > 80%
- [ ] 모든 Domain 메서드 테스트 완료
- [ ] 비즈니스 규칙 검증 완료

---

## 📊 진행 상황 추적

**시작일**: ___________
**목표 완료일**: ___________

### Phase 진행률
- [x] Phase 1: Value Objects & Enums (6/6) ✅ **완료!**
- [x] Phase 2: Seller Aggregate (4/4) ✅ **완료!**
- [x] Phase 3: CrawlerTask Aggregate (5/5) ✅ **완료!**
- [x] Phase 4: UserAgent Aggregate (4/4) ✅ **완료!**
- [ ] Phase 5: Product Aggregate (1/3)
- [ ] Phase 6: ProductOutbox Aggregate (0/3)

**전체 진행률**: 20/25 Cycles (80%)

---

## 🎓 TDD Best Practices

### 커밋 메시지 규칙
- `test:` - 실패하는 테스트 추가 (Red Phase)
- `feat:` - 테스트 통과 구현 (Green Phase)
- `struct:` - 구조 개선 (Refactor/Tidy Phase, 동작 변경 없음)

### Cycle 타이밍 가이드
- **Red**: 5분 (테스트 작성 + 실패 확인)
- **Green**: 5분 (최소 구현 + 통과 확인)
- **Refactor**: 3분 (구조 개선, 필요 시)
- **Tidy**: 2분 (TestFixture 추가)

### 팁
- **작은 단위로 커밋**: 각 Phase마다 커밋
- **테스트 먼저**: 항상 Red → Green 순서 준수
- **Tell Don't Ask**: 객체가 스스로 판단하도록 설계
- **Law of Demeter**: Getter 체이닝 금지 (`seller.getCrawlingInterval().getDays()` ❌)
- **ArchUnit 활용**: 규칙 위반 자동 검증

---

## 🔗 관련 문서

- **Task**: docs/prd/tasks/MUSTIT-001.md
- **PRD**: docs/prd/mustit-seller-crawler.md
- **Domain Layer 규칙**: docs/coding_convention/02-domain-layer/
- **ArchUnit 가이드**: docs/coding_convention/02-domain-layer/aggregate/aggregate-archunit.md

---

**다음 단계**: `/kb/domain/go` 커맨드로 Cycle 1부터 시작!
