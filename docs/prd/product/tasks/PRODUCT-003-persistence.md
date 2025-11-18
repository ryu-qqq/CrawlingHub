# PRODUCT-003: Product Persistence Layer 구현

**Bounded Context**: Product
**Layer**: Persistence Layer
**브랜치**: feature/PRODUCT-003-persistence

---

## 📝 목적

Product 데이터 영속성 및 Outbox 관리.

---

## 🎯 요구사항

### 1. JPA Entity

#### ProductJpaEntity
- 테이블: `products`
- 인덱스:
  - `idx_item_no` (item_no) - Unique
  - `idx_seller_id` (seller_id) - 셀러별 상품 조회
  - `idx_is_complete` (is_complete) - 완전성 필터

#### ProductOutboxJpaEntity
- 테이블: `product_outbox`
- 인덱스:
  - `idx_outbox_id` (outbox_id) - Unique
  - `idx_status_next_retry_at` (status, next_retry_at) - Outbox 배치 처리 최적화

### 2. Repository

- ProductJpaRepository (JPA 기본)
  - `findByItemNo()` - ItemNo로 조회
  - `findBySellerId()` - Seller별 상품 조회
- ProductOutboxJpaRepository (JPA 기본)
- ProductOutboxQueryDslRepository (Outbox 조회)
  - `findPendingOutboxes()` - WAITING/FAILED 상태 + nextRetryAt 이전

### 3. Flyway

- V7__create_products_table.sql
- V8__create_product_outbox_table.sql

---

## ✅ 완료 조건

- [ ] ProductJpaEntity 구현 완료
- [ ] ProductOutboxJpaEntity 구현 완료
- [ ] Repository 구현 완료
- [ ] QueryDSL Outbox 조회 구현 완료

---

## 🔗 관련 문서

- **Plan**: docs/prd/product/plans/PRODUCT-003-persistence-plan.md

---

## 📚 참고사항

### ProductJpaEntity

```java
@Entity
@Table(
    name = "products",
    indexes = {
        @Index(name = "idx_item_no", columnList = "item_no", unique = true),
        @Index(name = "idx_seller_id", columnList = "seller_id"),
        @Index(name = "idx_is_complete", columnList = "is_complete")
    }
)
public class ProductJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_no", nullable = false, unique = true, length = 50)
    private String itemNo;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "data_hashes", columnDefinition = "JSON")
    @Convert(converter = DataHashesConverter.class)
    private Map<String, String> dataHashes;

    @Column(name = "is_complete", nullable = false)
    private Boolean isComplete;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Getters, Setters 생략
}
```

### DataHashesConverter (JSON 변환)

```java
@Converter
public class DataHashesConverter implements AttributeConverter<Map<String, String>, String> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, String> attribute) {
        try {
            return attribute == null ? null : objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting Map to JSON", e);
        }
    }

    @Override
    public Map<String, String> convertToEntityAttribute(String dbData) {
        try {
            return dbData == null ? new HashMap<>() : objectMapper.readValue(dbData, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting JSON to Map", e);
        }
    }
}
```

### ProductOutboxJpaEntity

```java
@Entity
@Table(
    name = "product_outbox",
    indexes = {
        @Index(name = "idx_outbox_id", columnList = "outbox_id", unique = true),
        @Index(name = "idx_status_next_retry_at", columnList = "status, next_retry_at")
    }
)
public class ProductOutboxJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "outbox_id", nullable = false, unique = true, length = 36)
    private String outboxId; // UUID

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private ProductEventType eventType;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OutboxStatus status;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    // Getters, Setters 생략
}
```

### ProductOutboxQueryDslRepository

```java
@Repository
@RequiredArgsConstructor
public class ProductOutboxQueryDslRepository {
    private final JPAQueryFactory queryFactory;

    public List<ProductOutboxJpaEntity> findPendingOutboxes(LocalDateTime now, int limit) {
        QProductOutboxJpaEntity outbox = QProductOutboxJpaEntity.productOutboxJpaEntity;

        return queryFactory
            .selectFrom(outbox)
            .where(
                outbox.status.in(OutboxStatus.WAITING, OutboxStatus.FAILED)
                    .and(
                        outbox.nextRetryAt.isNull()
                            .or(outbox.nextRetryAt.loe(now))
                    )
            )
            .orderBy(outbox.createdAt.asc())
            .limit(limit)
            .fetch();
    }
}
```

### Flyway V7 - Products Table

```sql
-- V7__create_products_table.sql
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_no VARCHAR(50) NOT NULL UNIQUE COMMENT '상품 고유 번호',
    seller_id BIGINT NOT NULL COMMENT '셀러 ID (Long FK)',
    data_hashes JSON COMMENT '데이터 영역별 Hash (detail, option, inventory)',
    is_complete BOOLEAN NOT NULL DEFAULT FALSE COMMENT '모든 데이터 수집 완료 여부',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_item_no (item_no),
    INDEX idx_seller_id (seller_id),
    INDEX idx_is_complete (is_complete)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='크롤링 상품';
```

### Flyway V8 - Product Outbox Table

```sql
-- V8__create_product_outbox_table.sql
CREATE TABLE product_outbox (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    outbox_id VARCHAR(36) NOT NULL UNIQUE COMMENT 'UUID',
    product_id BIGINT NOT NULL COMMENT '상품 ID (Long FK)',
    event_type VARCHAR(50) NOT NULL COMMENT 'PRODUCT_CREATED, PRODUCT_UPDATED',
    payload TEXT COMMENT '이벤트 Payload (JSON)',
    status VARCHAR(20) NOT NULL COMMENT 'WAITING, SENDING, COMPLETED, FAILED, DEAD_LETTER',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '재시도 횟수',
    next_retry_at DATETIME COMMENT '다음 재시도 시각 (Exponential Backoff)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at DATETIME COMMENT '처리 완료 시각',
    INDEX idx_outbox_id (outbox_id),
    INDEX idx_status_next_retry_at (status, next_retry_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='상품 외부 전송용 Outbox';
```
