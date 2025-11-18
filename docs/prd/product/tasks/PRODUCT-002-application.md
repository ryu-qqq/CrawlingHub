# PRODUCT-002: Product Application Layer 구현

**Bounded Context**: Product
**Layer**: Application Layer
**브랜치**: feature/PRODUCT-002-application

---

## 📝 목적

Product 관련 Use Case 오케스트레이션.

---

## 🎯 요구사항

### 1. Command Use Cases

#### ProcessProductOutboxUseCase
- **목적**: ProductOutbox 배치 처리 (외부 API 전송)
- **트리거**: @Scheduled (5분마다)
- **핵심 로직**:
  1. WAITING/FAILED 상태 Outbox 조회 (Limit 100)
  2. SENDING 상태 변경 (트랜잭션 내)
  3. 외부 API 전송 (트랜잭션 밖)
  4. 결과에 따라 COMPLETED/FAILED 상태 변경
  5. Exponential Backoff 적용

### 2. Query Use Cases

#### GetProductUseCase
- **입력**: ItemNo
- **출력**: ProductResponse
- **로직**: itemNo로 Product 조회

#### ListProductsUseCase
- **입력**: Pagination (page, size)
- **출력**: List<ProductResponse>
- **로직**: 전체 Product 페이징 조회

---

## ✅ 완료 조건

- [ ] ProcessProductOutboxUseCase 구현 완료
- [ ] GetProductUseCase 구현 완료
- [ ] ListProductsUseCase 구현 완료
- [ ] Transaction 경계 검증 완료
- [ ] Exponential Backoff 테스트 완료

---

## 🔗 관련 문서

- **Plan**: docs/prd/product/plans/PRODUCT-002-application-plan.md

---

## 📚 참고사항

### ProcessProductOutboxUseCase 구현 예시

```java
@UseCase
@RequiredArgsConstructor
public class ProcessProductOutboxUseCase {
    private final ProductOutboxQueryPort productOutboxQueryPort;
    private final ProductOutboxCommandPort productOutboxCommandPort;
    private final ExternalProductApiClient externalProductApiClient;

    @Scheduled(fixedDelay = 300000) // 5분마다
    public OutboxProcessedResult execute() {
        // 1. 처리 대상 Outbox 조회 (WAITING/FAILED, Limit 100)
        List<ProductOutbox> outboxes = findPendingOutboxes();

        int successCount = 0;
        int failureCount = 0;

        for (ProductOutbox outbox : outboxes) {
            try {
                // 2. SENDING 상태 변경 (트랜잭션 내)
                markAsSendingInTransaction(outbox);

                // 3. 외부 API 전송 (트랜잭션 밖)
                externalProductApiClient.send(outbox.getPayload());

                // 4. COMPLETED 상태 변경
                markAsCompletedInTransaction(outbox);
                successCount++;

            } catch (Exception e) {
                // 5. FAILED 상태 변경
                markAsFailedInTransaction(outbox);
                failureCount++;
            }
        }

        return new OutboxProcessedResult(successCount, failureCount);
    }

    private List<ProductOutbox> findPendingOutboxes() {
        LocalDateTime now = LocalDateTime.now();
        return productOutboxQueryPort.findPendingOutboxes(now, 100);
    }

    @Transactional
    private void markAsSendingInTransaction(ProductOutbox outbox) {
        outbox.markAsSending();
        productOutboxCommandPort.save(outbox);
    }

    @Transactional
    private void markAsCompletedInTransaction(ProductOutbox outbox) {
        outbox.markAsCompleted();
        productOutboxCommandPort.save(outbox);
    }

    @Transactional
    private void markAsFailedInTransaction(ProductOutbox outbox) {
        outbox.markAsFailed();
        productOutboxCommandPort.save(outbox);
    }
}
```

### GetProductUseCase 구현 예시

```java
@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetProductUseCase {
    private final ProductQueryPort productQueryPort;

    public ProductResponse execute(ItemNo itemNo) {
        Product product = productQueryPort.findByItemNo(itemNo)
            .orElseThrow(() -> new ProductNotFoundException("Product not found: " + itemNo));

        return ProductResponse.from(product);
    }
}
```

### ListProductsUseCase 구현 예시

```java
@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListProductsUseCase {
    private final ProductQueryPort productQueryPort;

    public Page<ProductResponse> execute(Pageable pageable) {
        Page<Product> products = productQueryPort.findAll(pageable);
        return products.map(ProductResponse::from);
    }
}
```

### Transaction 경계 패턴

```java
// ✅ 올바른 예시 - Transaction 분리
public void processOutbox(ProductOutbox outbox) {
    // 1. Transaction 내: SENDING 상태 변경
    markAsSendingInTransaction(outbox);

    // 2. Transaction 밖: 외부 API 호출
    try {
        externalProductApiClient.send(outbox.getPayload());
        markAsCompletedInTransaction(outbox);
    } catch (Exception e) {
        markAsFailedInTransaction(outbox);
    }
}

@Transactional
private void markAsSendingInTransaction(ProductOutbox outbox) {
    outbox.markAsSending();
    productOutboxCommandPort.save(outbox);
}

// ❌ 잘못된 예시 - Transaction 내 외부 API 호출
@Transactional
public void processOutbox(ProductOutbox outbox) {
    outbox.markAsSending();
    productOutboxCommandPort.save(outbox);

    // 외부 API 호출 (트랜잭션 내 금지!)
    externalProductApiClient.send(outbox.getPayload());

    outbox.markAsCompleted();
    productOutboxCommandPort.save(outbox);
}
```

### Exponential Backoff 적용

```java
public interface ProductOutboxQueryPort {
    // nextRetryAt 이전의 WAITING/FAILED Outbox 조회
    List<ProductOutbox> findPendingOutboxes(LocalDateTime now, int limit);
}

// QueryDSL 구현
public List<ProductOutbox> findPendingOutboxes(LocalDateTime now, int limit) {
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
        .fetch()
        .stream()
        .map(mapper::toDomain)
        .collect(Collectors.toList());
}
```

### Domain Event Handler (선택적)

```java
@Component
@RequiredArgsConstructor
public class ProductEventHandler {
    private final ProductOutboxCommandPort productOutboxCommandPort;
    private final ObjectMapper objectMapper;

    @EventListener
    @Transactional
    public void handleProductChanged(ProductChanged event) throws JsonProcessingException {
        // 1. Payload 구성
        String payload = objectMapper.writeValueAsString(event);

        // 2. Outbox 생성
        ProductOutbox outbox = ProductOutbox.create(
            event.getProductId(),
            ProductEventType.PRODUCT_UPDATED,
            payload
        );

        // 3. DB 저장 (트랜잭션 내)
        productOutboxCommandPort.save(outbox);
    }
}
```
