# PRODUCT-001: Product Domain Layer 구현

**Bounded Context**: Product
**Layer**: Domain Layer
**브랜치**: feature/PRODUCT-001-domain

---

## 📝 목적

Product Aggregate의 비즈니스 핵심 로직을 담당하는 Domain Layer 구현.

**핵심 역할**:
- Product 변경 감지 (Data Hash 기반)
- 데이터 완전성 검증
- ProductOutbox 이벤트 발행 (외부 API 호출용)

---

## 🎯 요구사항

### 1. Aggregate: Product (상품)

- [ ] **Product Aggregate 구현**
  - itemNo (ItemNo VO, String) - 상품 고유 번호
  - sellerId (SellerId VO, Long FK)
  - dataHashes (Map<String, String>) - 데이터 영역별 Hash
    - `detail`: 상품 상세 정보 Hash
    - `option`: 옵션 정보 Hash
    - `inventory`: 재고 정보 Hash
  - isComplete (Boolean) - 모든 데이터 수집 완료 여부
  - createdAt (LocalDateTime)
  - updatedAt (LocalDateTime)

- [ ] **비즈니스 규칙**
  - Product 생성 시 isComplete = false
  - 3개 영역(detail, option, inventory) Hash 모두 존재 → isComplete = true
  - 기존 Hash와 다른 Hash 감지 → ProductChanged 이벤트 발행
  - ItemNo는 Seller 내 고유 (Seller당 중복 불가)

- [ ] **Value Objects**
  - ItemNo: String (상품 고유 번호)
  - ProductId: Long (내부 식별자)

- [ ] **Domain 메서드**
  - `create(itemNo, sellerId)`: Product 생성 (isComplete = false)
  - `updateDataHash(dataType, newHash)`: Hash 업데이트 및 변경 감지
    - 기존 Hash와 다르면 ProductChanged 이벤트 발행
    - 3개 영역 모두 존재하면 isComplete = true
  - `isChanged(dataType, newHash)`: Hash 변경 여부 확인
  - `markAsComplete()`: 완료 표시

### 2. Aggregate: ProductOutbox (외부 전송용 Outbox)

- [ ] **ProductOutbox Aggregate 구현**
  - outboxId (OutboxId VO, UUID)
  - productId (ProductId VO, Long FK)
  - eventType (ProductEventType Enum)
  - payload (String, JSON)
  - status (OutboxStatus Enum)
  - retryCount (Integer, 최대 5회)
  - createdAt (LocalDateTime)
  - processedAt (LocalDateTime)

- [ ] **비즈니스 규칙**
  - Outbox 생성 시 status = WAITING
  - 외부 API 전송 성공 → COMPLETED
  - 외부 API 전송 실패 → FAILED (retryCount++)
  - retryCount >= 5 → DEAD_LETTER
  - Exponential Backoff (1분 → 2분 → 4분 → 8분 → 16분)

- [ ] **Value Objects**
  - OutboxId: UUID
  - ProductEventType: Enum (PRODUCT_CREATED, PRODUCT_UPDATED)
  - OutboxStatus: Enum (WAITING, SENDING, COMPLETED, FAILED, DEAD_LETTER)

- [ ] **Domain 메서드**
  - `create(productId, eventType, payload)`: Outbox 생성
  - `markAsSending()`: 전송 중 표시
  - `markAsCompleted()`: 전송 완료
  - `markAsFailed()`: 전송 실패 (retryCount++)
  - `canRetry()`: 재시도 가능 여부 (retryCount < 5)
  - `calculateNextRetryAt()`: 다음 재시도 시각 (Exponential Backoff)

---

## ⚠️ 제약사항

### Zero-Tolerance 규칙

- [ ] **Lombok 금지**: Pure Java 또는 Record 사용
- [ ] **Law of Demeter 준수**: Getter 체이닝 금지
- [ ] **Tell Don't Ask**: 내부 상태 기반 판단
- [ ] **Long FK 전략**: 관계 어노테이션 금지

### 테스트 규칙

- [ ] **ArchUnit 테스트 필수**
- [ ] **TestFixture 패턴 사용**
- [ ] **테스트 커버리지 > 80%**

---

## ✅ 완료 조건

- [ ] Product Aggregate 구현 완료
- [ ] ProductOutbox Aggregate 구현 완료
- [ ] 모든 Value Object 구현 완료
- [ ] 모든 Domain 메서드 구현 완료
- [ ] Unit Test 작성 완료
- [ ] Zero-Tolerance 규칙 준수

---

## 🔗 관련 문서

- **Plan**: docs/prd/product/plans/PRODUCT-001-domain-plan.md
- **Domain Layer 규칙**: docs/coding_convention/02-domain-layer/

---

## 📚 참고사항

### Product Aggregate 구현 예시

```java
public class Product {
    private final ProductId productId;
    private final ItemNo itemNo;
    private final SellerId sellerId;
    private final Map<String, String> dataHashes; // detail, option, inventory
    private boolean isComplete;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static Product create(ItemNo itemNo, SellerId sellerId) {
        return new Product(
            ProductId.generate(),
            itemNo,
            sellerId,
            new HashMap<>(),
            false,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
    }

    public ProductChanged updateDataHash(String dataType, String newHash) {
        if (!isChanged(dataType, newHash)) {
            return null; // 변경 없음
        }

        // Hash 업데이트
        this.dataHashes.put(dataType, newHash);
        this.updatedAt = LocalDateTime.now();

        // 완전성 검사
        if (hasAllDataTypes()) {
            this.isComplete = true;
        }

        // ProductChanged 이벤트 발행
        return new ProductChanged(
            this.productId,
            this.itemNo,
            dataType,
            newHash,
            LocalDateTime.now()
        );
    }

    private boolean isChanged(String dataType, String newHash) {
        String currentHash = this.dataHashes.get(dataType);
        return currentHash == null || !currentHash.equals(newHash);
    }

    private boolean hasAllDataTypes() {
        return dataHashes.containsKey("detail")
            && dataHashes.containsKey("option")
            && dataHashes.containsKey("inventory");
    }
}
```

### ProductOutbox Aggregate 구현 예시

```java
public class ProductOutbox {
    private final OutboxId outboxId;
    private final ProductId productId;
    private final ProductEventType eventType;
    private final String payload;
    private OutboxStatus status;
    private int retryCount;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;

    public static ProductOutbox create(ProductId productId, ProductEventType eventType, String payload) {
        return new ProductOutbox(
            OutboxId.generate(),
            productId,
            eventType,
            payload,
            OutboxStatus.WAITING,
            0,
            LocalDateTime.now(),
            null
        );
    }

    public void markAsSending() {
        if (this.status != OutboxStatus.WAITING) {
            throw new ProductOutboxInvalidStateException("Cannot send from status: " + this.status);
        }
        this.status = OutboxStatus.SENDING;
    }

    public void markAsCompleted() {
        this.status = OutboxStatus.COMPLETED;
        this.processedAt = LocalDateTime.now();
    }

    public void markAsFailed() {
        this.retryCount++;
        if (this.retryCount >= 5) {
            this.status = OutboxStatus.DEAD_LETTER;
        } else {
            this.status = OutboxStatus.FAILED;
        }
    }

    public boolean canRetry() {
        return this.retryCount < 5 && this.status == OutboxStatus.FAILED;
    }

    public LocalDateTime calculateNextRetryAt() {
        // Exponential Backoff: 1분 → 2분 → 4분 → 8분 → 16분
        long delayMinutes = (long) Math.pow(2, this.retryCount);
        return LocalDateTime.now().plusMinutes(delayMinutes);
    }
}
```

### Data Hash 계산 예시

```java
public class DataHashCalculator {
    public static String calculate(Object data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            return DigestUtils.sha256Hex(json);
        } catch (JsonProcessingException e) {
            throw new DataHashCalculationException("Failed to calculate hash", e);
        }
    }
}
```
