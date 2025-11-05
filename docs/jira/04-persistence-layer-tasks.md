# 🎯 Persistence Layer 개발 태스크

## 📌 개발 순서 및 우선순위
1. **Core Entities** (Priority: P0) - 핵심 엔티티
2. **Task & Outbox Entities** (Priority: P0) - 작업 관리 엔티티
3. **Product Entities** (Priority: P0) - 상품 관련 엔티티
4. **Monitoring Entities** (Priority: P1) - 모니터링 엔티티
5. **Repository Implementations** (Priority: P0) - Repository 구현
6. **QueryDSL Implementations** (Priority: P1) - 복잡한 쿼리

---

## 📦 TASK-01: Core Entities

### Entity-01-1: MustitSellerEntity
```java
package com.ryuqq.crawlinghub.adapter.out.persistence.mustit.seller.entity;

@Entity
@Table(name = "mustit_seller")
public class MustitSellerEntity extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "seller_code", nullable = false, unique = true, length = 50)
    private String sellerCode;

    @Column(name = "seller_name", nullable = false, length = 100)
    private String sellerName;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private SellerStatus status;

    @Column(name = "total_product_count")
    private Integer totalProductCount;

    @Column(name = "last_crawled_at")
    private LocalDateTime lastCrawledAt;

    // Lombok 금지 - Pure Java Constructor & Getter
    protected MustitSellerEntity() {}

    public MustitSellerEntity(String sellerCode, String sellerName) {
        this.sellerCode = sellerCode;
        this.sellerName = sellerName;
        this.status = SellerStatus.ACTIVE;
        this.totalProductCount = 0;
    }

    // Getters only (no setters - immutability)
    public Long getId() { return id; }
    public String getSellerCode() { return sellerCode; }
    public String getSellerName() { return sellerName; }
    public SellerStatus getStatus() { return status; }
    public Integer getTotalProductCount() { return totalProductCount; }
    public LocalDateTime getLastCrawledAt() { return lastCrawledAt; }

    // Business methods for state changes
    public void updateStatus(SellerStatus newStatus) {
        this.status = newStatus;
    }

    public void updateProductCount(Integer count) {
        this.totalProductCount = count;
    }

    public void recordCrawlingComplete() {
        this.lastCrawledAt = LocalDateTime.now();
    }
}
```

### Entity-01-2: CrawlScheduleEntity
```java
@Entity
@Table(name = "crawl_schedule",
       indexes = {
           @Index(name = "idx_seller_id", columnList = "seller_id"),
           @Index(name = "idx_next_execution", columnList = "next_execution_time")
       })
public class CrawlScheduleEntity extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;  // Long FK 전략 (관계 어노테이션 금지)

    @Column(name = "cron_expression", nullable = false, length = 50)
    private String cronExpression;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ScheduleStatus status;

    @Column(name = "next_execution_time")
    private LocalDateTime nextExecutionTime;

    @Column(name = "last_executed_at")
    private LocalDateTime lastExecutedAt;

    @Column(name = "eventbridge_rule_name", length = 100)
    private String eventBridgeRuleName;

    // Constructor
    protected CrawlScheduleEntity() {}

    public CrawlScheduleEntity(Long sellerId, String cronExpression) {
        this.sellerId = sellerId;
        this.cronExpression = cronExpression;
        this.status = ScheduleStatus.ACTIVE;
        calculateNextExecution();
    }

    // Business methods
    public void updateCronExpression(String newExpression) {
        this.cronExpression = newExpression;
        calculateNextExecution();
    }

    public void markExecuted() {
        this.lastExecutedAt = LocalDateTime.now();
        calculateNextExecution();
    }

    private void calculateNextExecution() {
        // Cron 계산 로직
    }

    // Getters
    public Long getId() { return id; }
    public Long getSellerId() { return sellerId; }
    public String getCronExpression() { return cronExpression; }
    public ScheduleStatus getStatus() { return status; }
    public LocalDateTime getNextExecutionTime() { return nextExecutionTime; }
    public LocalDateTime getLastExecutedAt() { return lastExecutedAt; }
}
```

### Entity-01-3: UserAgentEntity
```java
@Entity
@Table(name = "user_agent")
public class UserAgentEntity extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_agent_string", nullable = false, length = 500)
    private String userAgentString;

    @Column(name = "current_token", length = 200)
    private String currentToken;

    @Column(name = "token_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TokenStatus tokenStatus;

    @Column(name = "remaining_requests", nullable = false)
    private Integer remainingRequests;

    @Column(name = "token_issued_at")
    private LocalDateTime tokenIssuedAt;

    @Column(name = "rate_limit_reset_at")
    private LocalDateTime rateLimitResetAt;

    @Column(name = "success_count")
    private Integer successCount;

    @Column(name = "rate_limit_count")
    private Integer rateLimitCount;

    // Constructor
    protected UserAgentEntity() {}

    public UserAgentEntity(String userAgentString) {
        this.userAgentString = userAgentString;
        this.tokenStatus = TokenStatus.IDLE;
        this.remainingRequests = 80;
        this.successCount = 0;
        this.rateLimitCount = 0;
    }

    // Business methods
    public void issueToken(String token) {
        this.currentToken = token;
        this.tokenIssuedAt = LocalDateTime.now();
        this.tokenStatus = TokenStatus.ACTIVE;
        this.remainingRequests = 80;
    }

    public void consumeRequest() {
        if (remainingRequests > 0) {
            this.remainingRequests--;
        }
    }

    public void handleRateLimit() {
        this.tokenStatus = TokenStatus.RATE_LIMITED;
        this.currentToken = null;
        this.rateLimitResetAt = LocalDateTime.now().plusHours(1);
        this.rateLimitCount++;
    }

    // Getters
    public Long getId() { return id; }
    public String getUserAgentString() { return userAgentString; }
    public String getCurrentToken() { return currentToken; }
    public TokenStatus getTokenStatus() { return tokenStatus; }
    public Integer getRemainingRequests() { return remainingRequests; }
}
```

---

## 📦 TASK-02: Task & Outbox Entities

### Entity-02-1: CrawlTaskEntity
```java
@Entity
@Table(name = "crawl_task",
       indexes = {
           @Index(name = "idx_seller_status", columnList = "seller_id, status"),
           @Index(name = "idx_idempotency_key", columnList = "idempotency_key", unique = true),
           @Index(name = "idx_scheduled_at", columnList = "scheduled_at")
       })
public class CrawlTaskEntity extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "task_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TaskType taskType;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    @Column(name = "request_url", nullable = false, length = 500)
    private String requestUrl;

    @Column(name = "page_number")
    private Integer pageNumber;

    @Column(name = "page_size")
    private Integer pageSize;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 100)
    private String idempotencyKey;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "response_data", columnDefinition = "JSON")
    private String responseData;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // Constructor
    protected CrawlTaskEntity() {}

    public static CrawlTaskEntity createMiniShopTask(Long sellerId, Integer pageNumber, Integer pageSize) {
        CrawlTaskEntity entity = new CrawlTaskEntity();
        entity.sellerId = sellerId;
        entity.taskType = TaskType.MINI_SHOP;
        entity.status = TaskStatus.WAITING;
        entity.pageNumber = pageNumber;
        entity.pageSize = pageSize;
        entity.retryCount = 0;
        entity.requestUrl = buildMiniShopUrl(sellerId, pageNumber, pageSize);
        entity.idempotencyKey = generateIdempotencyKey(sellerId, TaskType.MINI_SHOP, pageNumber);
        entity.scheduledAt = LocalDateTime.now();
        return entity;
    }

    // State transition methods
    public void markAsPublished() {
        this.status = TaskStatus.PUBLISHED;
    }

    public void markAsRunning() {
        this.status = TaskStatus.RUNNING;
        this.startedAt = LocalDateTime.now();
    }

    public void markAsCompleted(String responseData) {
        this.status = TaskStatus.SUCCESS;
        this.responseData = responseData;
        this.completedAt = LocalDateTime.now();
    }

    public void markAsFailed(String errorMessage) {
        this.status = TaskStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
    }

    public boolean canRetry() {
        return retryCount < 3;
    }

    public void incrementRetry() {
        this.retryCount++;
        this.status = TaskStatus.RETRY;
    }

    // Helper methods
    private static String generateIdempotencyKey(Long sellerId, TaskType type, Integer pageNumber) {
        return String.format("%d-%s-%d", sellerId, type, pageNumber != null ? pageNumber : 0);
    }
}
```

### Entity-02-2: SellerCrawlScheduleOutboxEntity (기존)
```java
@Entity
@Table(name = "seller_crawl_schedule_outbox",
       indexes = {
           @Index(name = "idx_wal_state", columnList = "wal_state"),
           @Index(name = "idx_created_at", columnList = "created_at")
       })
public class SellerCrawlScheduleOutboxEntity extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "schedule_id", nullable = false)
    private Long scheduleId;

    @Column(name = "wal_state", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private WalState walState;

    @Column(name = "idempotency_key", unique = true, length = 100)
    private String idempotencyKey;

    @Column(name = "payload", columnDefinition = "JSON")
    private String payload;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "retry_count")
    private Integer retryCount;

    // Business methods
    public void markAsProcessed() {
        this.walState = WalState.PROCESSED;
        this.processedAt = LocalDateTime.now();
    }

    public void markAsFailed(String error) {
        this.walState = WalState.FAILED;
        this.errorMessage = error;
        this.retryCount++;
    }

    // Getters...
}
```

### Entity-02-3: ProductSyncOutboxEntity
```java
@Entity
@Table(name = "product_sync_outbox",
       indexes = {
           @Index(name = "idx_sync_status", columnList = "sync_status"),
           @Index(name = "idx_created_at", columnList = "created_at")
       })
public class ProductSyncOutboxEntity extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "mustit_item_no", nullable = false, length = 50)
    private String mustitItemNo;

    @Column(name = "sync_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private SyncType syncType;  // NEW, UPDATE, DELETE

    @Column(name = "sync_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private SyncStatus syncStatus;  // PENDING, SENT, FAILED

    @Column(name = "change_data", columnDefinition = "JSON")
    private String changeData;

    @Column(name = "request_id", length = 100)
    private String requestId;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "response_message", columnDefinition = "TEXT")
    private String responseMessage;

    @Column(name = "retry_count")
    private Integer retryCount;

    // Business methods
    public void markAsSent(String requestId) {
        this.syncStatus = SyncStatus.SENT;
        this.requestId = requestId;
        this.sentAt = LocalDateTime.now();
    }

    public void markAsFailed(String message) {
        this.syncStatus = SyncStatus.FAILED;
        this.responseMessage = message;
        this.retryCount++;
    }
}
```

---

## 📦 TASK-03: Product Entities

### Entity-03-1: CrawledProductEntity
```java
@Entity
@Table(name = "crawled_product",
       indexes = {
           @Index(name = "idx_mustit_item_no", columnList = "mustit_item_no", unique = true),
           @Index(name = "idx_seller_id", columnList = "seller_id"),
           @Index(name = "idx_completion_status", columnList = "completion_status")
       })
public class CrawledProductEntity extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mustit_item_no", nullable = false, unique = true, length = 50)
    private String mustitItemNo;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "mini_shop_data", columnDefinition = "JSON")
    private String miniShopData;

    @Column(name = "detail_data", columnDefinition = "JSON")
    private String detailData;

    @Column(name = "option_data", columnDefinition = "JSON")
    private String optionData;

    @Column(name = "data_hash", length = 64)
    private String dataHash;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "completion_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private CompletionStatus completionStatus;

    @Column(name = "first_crawled_at")
    private LocalDateTime firstCrawledAt;

    @Column(name = "last_hash_changed_at")
    private LocalDateTime lastHashChangedAt;

    // Factory method
    public static CrawledProductEntity createNew(String mustitItemNo, Long sellerId) {
        CrawledProductEntity entity = new CrawledProductEntity();
        entity.mustitItemNo = mustitItemNo;
        entity.sellerId = sellerId;
        entity.version = 1;
        entity.completionStatus = CompletionStatus.INCOMPLETE;
        entity.firstCrawledAt = LocalDateTime.now();
        return entity;
    }

    // Business methods
    public void updateMiniShopData(String data) {
        this.miniShopData = data;
        checkCompletion();
    }

    public void updateDetailData(String data) {
        this.detailData = data;
        checkCompletion();
    }

    public void updateOptionData(String data) {
        this.optionData = data;
        checkCompletion();
    }

    private void checkCompletion() {
        if (miniShopData != null && detailData != null && optionData != null) {
            this.completionStatus = CompletionStatus.COMPLETE;
        }
    }

    public boolean hasDataChanged(String newHash) {
        return !newHash.equals(this.dataHash);
    }

    public void updateHash(String newHash) {
        if (hasDataChanged(newHash)) {
            this.dataHash = newHash;
            this.version++;
            this.lastHashChangedAt = LocalDateTime.now();
        }
    }
}
```

### Entity-03-2: ProductChangeHistoryEntity
```java
@Entity
@Table(name = "product_change_history",
       indexes = {
           @Index(name = "idx_product_id", columnList = "product_id"),
           @Index(name = "idx_detected_at", columnList = "detected_at")
       })
public class ProductChangeHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "change_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ChangeType changeType;

    @Column(name = "previous_data", columnDefinition = "JSON")
    private String previousData;

    @Column(name = "current_data", columnDefinition = "JSON")
    private String currentData;

    @Column(name = "previous_hash", length = 64)
    private String previousHash;

    @Column(name = "current_hash", length = 64)
    private String currentHash;

    @Column(name = "detected_at", nullable = false)
    private LocalDateTime detectedAt;

    @Column(name = "notified", nullable = false)
    private Boolean notified;

    // Constructor
    protected ProductChangeHistoryEntity() {}

    public static ProductChangeHistoryEntity create(
            Long productId,
            ChangeType changeType,
            String previousData,
            String currentData,
            String previousHash,
            String currentHash) {

        ProductChangeHistoryEntity entity = new ProductChangeHistoryEntity();
        entity.productId = productId;
        entity.changeType = changeType;
        entity.previousData = previousData;
        entity.currentData = currentData;
        entity.previousHash = previousHash;
        entity.currentHash = currentHash;
        entity.detectedAt = LocalDateTime.now();
        entity.notified = false;
        return entity;
    }

    public void markAsNotified() {
        this.notified = true;
    }
}
```

---

## 📦 TASK-04: Monitoring Entities

### Entity-04-1: CrawlingStatsEntity
```java
@Entity
@Table(name = "crawling_stats",
       indexes = {
           @Index(name = "idx_seller_date", columnList = "seller_id, stat_date"),
           @Index(name = "idx_stat_date", columnList = "stat_date")
       })
public class CrawlingStatsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;

    @Column(name = "stat_hour")
    private Integer statHour;  // 0-23, null for daily stats

    @Column(name = "total_tasks")
    private Integer totalTasks;

    @Column(name = "success_count")
    private Integer successCount;

    @Column(name = "failed_count")
    private Integer failedCount;

    @Column(name = "retry_count")
    private Integer retryCount;

    @Column(name = "avg_processing_time_ms")
    private Long avgProcessingTimeMs;

    @Column(name = "min_processing_time_ms")
    private Long minProcessingTimeMs;

    @Column(name = "max_processing_time_ms")
    private Long maxProcessingTimeMs;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Factory methods
    public static CrawlingStatsEntity createHourlyStats(Long sellerId, LocalDate date, Integer hour) {
        CrawlingStatsEntity entity = new CrawlingStatsEntity();
        entity.sellerId = sellerId;
        entity.statDate = date;
        entity.statHour = hour;
        entity.totalTasks = 0;
        entity.successCount = 0;
        entity.failedCount = 0;
        entity.retryCount = 0;
        entity.createdAt = LocalDateTime.now();
        return entity;
    }

    public static CrawlingStatsEntity createDailyStats(Long sellerId, LocalDate date) {
        CrawlingStatsEntity entity = new CrawlingStatsEntity();
        entity.sellerId = sellerId;
        entity.statDate = date;
        entity.statHour = null;  // Daily aggregation
        entity.totalTasks = 0;
        entity.successCount = 0;
        entity.failedCount = 0;
        entity.retryCount = 0;
        entity.createdAt = LocalDateTime.now();
        return entity;
    }

    // Business methods
    public void incrementSuccess(Long processingTime) {
        this.successCount++;
        updateProcessingTime(processingTime);
    }

    public void incrementFailed() {
        this.failedCount++;
    }

    public void incrementRetry() {
        this.retryCount++;
    }

    private void updateProcessingTime(Long processingTime) {
        // 평균, 최소, 최대 시간 업데이트 로직
    }

    public Double getSuccessRate() {
        if (totalTasks == 0) return 0.0;
        return (double) successCount / totalTasks * 100;
    }
}
```

---

## 📦 TASK-05: Repository Implementations

### Repository-05-1: MustitSellerRepository
```java
package com.ryuqq.crawlinghub.adapter.out.persistence.mustit.seller.repository;

@Repository
public interface MustitSellerJpaRepository extends JpaRepository<MustitSellerEntity, Long> {

    Optional<MustitSellerEntity> findBySellerCode(String sellerCode);

    List<MustitSellerEntity> findByStatus(SellerStatus status);

    @Query("SELECT s FROM MustitSellerEntity s WHERE s.lastCrawledAt < :threshold")
    List<MustitSellerEntity> findSellersNotCrawledSince(@Param("threshold") LocalDateTime threshold);
}

// Adapter Implementation
@Component
@RequiredArgsConstructor
public class MustitSellerPersistenceAdapter implements LoadSellerPort, SaveSellerPort {

    private final MustitSellerJpaRepository repository;
    private final MustitSellerMapper mapper;

    @Override
    public Optional<MustitSeller> findById(MustitSellerId id) {
        return repository.findById(id.getValue())
                .map(mapper::toDomain);
    }

    @Override
    public Optional<MustitSeller> findByCode(String code) {
        return repository.findBySellerCode(code)
                .map(mapper::toDomain);
    }

    @Override
    public MustitSeller save(MustitSeller seller) {
        MustitSellerEntity entity = mapper.toEntity(seller);
        MustitSellerEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }
}
```

### Repository-05-2: CrawlTaskRepository
```java
@Repository
public interface CrawlTaskJpaRepository extends JpaRepository<CrawlTaskEntity, Long> {

    @Query("SELECT t FROM CrawlTaskEntity t WHERE t.status = 'WAITING' " +
           "AND t.sellerId = :sellerId ORDER BY t.scheduledAt ASC")
    List<CrawlTaskEntity> findWaitingTasksBySeller(@Param("sellerId") Long sellerId,
                                                    Pageable pageable);

    @Query("SELECT t FROM CrawlTaskEntity t WHERE t.status = 'RUNNING' " +
           "AND t.startedAt < :threshold")
    List<CrawlTaskEntity> findTimedOutTasks(@Param("threshold") LocalDateTime threshold);

    @Query("SELECT COUNT(t) FROM CrawlTaskEntity t WHERE t.sellerId = :sellerId " +
           "AND t.status = :status")
    Long countBySellerAndStatus(@Param("sellerId") Long sellerId,
                                @Param("status") TaskStatus status);

    Optional<CrawlTaskEntity> findByIdempotencyKey(String idempotencyKey);
}
```

---

## 📦 TASK-06: QueryDSL Implementations

### QueryDSL-06-1: Complex Query Repository
```java
@Repository
@RequiredArgsConstructor
public class CrawlTaskQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Page<CrawlTaskEntity> searchTasks(TaskSearchCondition condition, Pageable pageable) {
        QCrawlTaskEntity task = QCrawlTaskEntity.crawlTaskEntity;

        BooleanBuilder builder = new BooleanBuilder();

        if (condition.sellerId() != null) {
            builder.and(task.sellerId.eq(condition.sellerId()));
        }

        if (condition.taskType() != null) {
            builder.and(task.taskType.eq(condition.taskType()));
        }

        if (condition.status() != null) {
            builder.and(task.status.eq(condition.status()));
        }

        if (condition.fromDate() != null && condition.toDate() != null) {
            builder.and(task.scheduledAt.between(condition.fromDate(), condition.toDate()));
        }

        List<CrawlTaskEntity> results = queryFactory
                .selectFrom(task)
                .where(builder)
                .orderBy(task.scheduledAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(task.count())
                .from(task)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(results, pageable, total != null ? total : 0L);
    }

    public CrawlingStatsDto calculateStats(Long sellerId, LocalDateTime from, LocalDateTime to) {
        QCrawlTaskEntity task = QCrawlTaskEntity.crawlTaskEntity;

        return queryFactory
                .select(Projections.constructor(CrawlingStatsDto.class,
                        task.count(),
                        task.status.when(TaskStatus.SUCCESS).then(1).otherwise(0).sum(),
                        task.status.when(TaskStatus.FAILED).then(1).otherwise(0).sum(),
                        task.retryCount.sum(),
                        task.completedAt.subtract(task.startedAt).avg()
                ))
                .from(task)
                .where(
                        task.sellerId.eq(sellerId),
                        task.scheduledAt.between(from, to)
                )
                .fetchOne();
    }
}
```

### QueryDSL-06-2: Product Query Repository
```java
@Repository
@RequiredArgsConstructor
public class ProductQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<ProductChangeDto> findRecentChanges(Long sellerId, Integer limit) {
        QCrawledProductEntity product = QCrawledProductEntity.crawledProductEntity;
        QProductChangeHistoryEntity history = QProductChangeHistoryEntity.productChangeHistoryEntity;

        return queryFactory
                .select(Projections.constructor(ProductChangeDto.class,
                        product.id,
                        product.mustitItemNo,
                        history.changeType,
                        history.detectedAt
                ))
                .from(history)
                .join(product).on(history.productId.eq(product.id))
                .where(
                        product.sellerId.eq(sellerId),
                        history.notified.eq(false)
                )
                .orderBy(history.detectedAt.desc())
                .limit(limit)
                .fetch();
    }
}
```

---

## 🎯 Database Migration Scripts

### V1__init_schema.sql
```sql
-- Seller table
CREATE TABLE mustit_seller (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    seller_code VARCHAR(50) NOT NULL UNIQUE,
    seller_name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    total_product_count INT DEFAULT 0,
    last_crawled_at DATETIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

-- Schedule table
CREATE TABLE crawl_schedule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    seller_id BIGINT NOT NULL,
    cron_expression VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    next_execution_time DATETIME,
    last_executed_at DATETIME,
    eventbridge_rule_name VARCHAR(100),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_seller_id (seller_id),
    INDEX idx_next_execution (next_execution_time)
);

-- CrawlTask table
CREATE TABLE crawl_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    seller_id BIGINT NOT NULL,
    task_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    request_url VARCHAR(500) NOT NULL,
    page_number INT,
    page_size INT,
    retry_count INT NOT NULL DEFAULT 0,
    idempotency_key VARCHAR(100) NOT NULL UNIQUE,
    error_message TEXT,
    response_data JSON,
    scheduled_at DATETIME,
    started_at DATETIME,
    completed_at DATETIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_seller_status (seller_id, status),
    UNIQUE INDEX idx_idempotency_key (idempotency_key),
    INDEX idx_scheduled_at (scheduled_at)
);

-- Product table
CREATE TABLE crawled_product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    mustit_item_no VARCHAR(50) NOT NULL UNIQUE,
    seller_id BIGINT NOT NULL,
    mini_shop_data JSON,
    detail_data JSON,
    option_data JSON,
    data_hash VARCHAR(64),
    version INT NOT NULL DEFAULT 1,
    completion_status VARCHAR(20) NOT NULL,
    first_crawled_at DATETIME,
    last_hash_changed_at DATETIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_seller_id (seller_id),
    INDEX idx_completion_status (completion_status)
);
```

---

## 🎯 개발 체크리스트

### Entity별 구현 사항
- [ ] Entity 클래스 생성
- [ ] BaseAuditEntity 상속
- [ ] 테이블 및 인덱스 정의
- [ ] Business methods 구현
- [ ] Factory methods 구현
- [ ] Lombok 사용 금지

### Repository별 구현 사항
- [ ] JpaRepository Interface 정의
- [ ] Custom Query 메서드
- [ ] QueryDSL 구현
- [ ] PersistenceAdapter 구현
- [ ] Mapper 구현

### 코딩 컨벤션 준수
- [ ] Long FK 전략 (관계 어노테이션 금지)
- [ ] Entity 불변성 원칙
- [ ] Pure Java (Lombok 금지)
- [ ] Business method 캡슐화
- [ ] Javadoc 작성

### 테스트 요구사항
- [ ] Repository 단위 테스트
- [ ] @DataJpaTest 활용
- [ ] Testcontainers 통합 테스트
- [ ] QueryDSL 쿼리 테스트

---

## 📊 예상 개발 일정

| Task | 예상 시간 | 우선순위 | 병렬 가능 |
|------|----------|----------|----------|
| Core Entities | 6h | P0 | ✅ |
| Task & Outbox | 6h | P0 | ✅ |
| Product Entities | 4h | P0 | ✅ |
| Monitoring Entities | 3h | P1 | ✅ |
| Repositories | 8h | P0 | ❌ (Entity 후) |
| QueryDSL | 5h | P1 | ✅ |

**총 예상 시간**: 32시간 (약 4일)

---

## 🔗 Entity 의존 관계

```
MustitSellerEntity (독립)
    ↓
CrawlScheduleEntity (seller_id FK)
    ↓
CrawlTaskEntity (seller_id FK)
    ↓
UserAgentEntity (독립, Task와 협업)
    ↓
CrawledProductEntity (seller_id FK)
    ↓
ProductChangeHistoryEntity (product_id FK)
    ↓
Outbox Entities (각 도메인별)
```

병렬 개발 가능 그룹:
- **Group 1**: MustitSeller, UserAgent Entities
- **Group 2**: CrawlSchedule, CrawlTask Entities
- **Group 3**: Product, Monitoring Entities
- **Group 4**: Repository, QueryDSL (Entity 완료 후)