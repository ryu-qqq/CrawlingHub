# PRODUCT-005: Product Integration Test 구현

**Bounded Context**: Product
**Layer**: Integration Test
**브랜치**: feature/PRODUCT-005-integration

---

## 📝 목적

Product E2E 시나리오 테스트.

---

## 🎯 요구사항

### 1. E2E 시나리오

#### 시나리오: Product 변경 감지 → Outbox → 외부 전송

- [ ] **Given: Product 생성** (itemNo, sellerId)
- [ ] **When: dataHash 업데이트** (detail, option, inventory)
- [ ] **Then: ProductOutbox 생성 확인** (WAITING)
- [ ] **When: ProcessProductOutboxUseCase 실행**
- [ ] **Then: 외부 API 전송 확인** (WireMock)
- [ ] **Then: ProductOutbox COMPLETED 확인**

### 2. Outbox 재시도 테스트

- [ ] 외부 API 실패 시 FAILED 상태 확인
- [ ] retryCount 증가 확인
- [ ] Exponential Backoff 적용 확인 (nextRetryAt)
- [ ] 5회 재시도 초과 시 DEAD_LETTER 확인

---

## ✅ 완료 조건

- [ ] E2E 시나리오 테스트 통과
- [ ] Outbox 재시도 테스트 통과
- [ ] WireMock 외부 API 모킹 완료

---

## 🔗 관련 문서

- **Plan**: docs/prd/product/plans/PRODUCT-005-integration-plan.md

---

## 📚 참고사항

### E2E 시나리오: Product 변경 → Outbox → 외부 전송

```java
@SpringBootTest
@AutoConfigureTestRestTemplate
@AutoConfigureWireMock(port = 0)
class ProductChangeDetectionIntegrationTest {

    @Autowired
    private ProductCommandPort productCommandPort;

    @Autowired
    private ProductOutboxQueryPort productOutboxQueryPort;

    @Autowired
    private ProcessProductOutboxUseCase processProductOutboxUseCase;

    @Test
    void 상품_변경_감지_Outbox_외부_전송() throws JsonProcessingException {
        // Given: Product 생성
        Product product = Product.create(new ItemNo("ITEM-001"), new SellerId(1L));
        productCommandPort.save(product);

        // When: dataHash 업데이트 (detail)
        product.updateDataHash("detail", "hash1");
        productCommandPort.save(product);

        // Then: ProductOutbox 생성 확인
        List<ProductOutbox> outboxes = productOutboxQueryPort.findByProductId(product.getProductId());
        assertThat(outboxes).hasSize(1);

        ProductOutbox outbox = outboxes.get(0);
        assertThat(outbox.getStatus()).isEqualTo(OutboxStatus.WAITING);
        assertThat(outbox.getEventType()).isEqualTo(ProductEventType.PRODUCT_UPDATED);

        // Given: 외부 API WireMock 설정
        stubFor(post(urlEqualTo("/external/product/update"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"success\": true}")));

        // When: Outbox 처리
        OutboxProcessedResult result = processProductOutboxUseCase.execute();

        // Then: 외부 API 전송 확인
        verify(postRequestedFor(urlEqualTo("/external/product/update"))
            .withRequestBody(containing("ITEM-001")));

        // Then: ProductOutbox COMPLETED 확인
        ProductOutbox processed = productOutboxQueryPort.findById(outbox.getOutboxId()).orElseThrow();
        assertThat(processed.getStatus()).isEqualTo(OutboxStatus.COMPLETED);
        assertThat(processed.getProcessedAt()).isNotNull();

        assertThat(result.successCount()).isEqualTo(1);
        assertThat(result.failureCount()).isEqualTo(0);
    }
}
```

### Outbox 재시도 테스트

```java
@SpringBootTest
@AutoConfigureWireMock(port = 0)
class ProductOutboxRetryIntegrationTest {

    @Autowired
    private ProductCommandPort productCommandPort;

    @Autowired
    private ProductOutboxCommandPort productOutboxCommandPort;

    @Autowired
    private ProductOutboxQueryPort productOutboxQueryPort;

    @Autowired
    private ProcessProductOutboxUseCase processProductOutboxUseCase;

    @Test
    void 외부_API_실패_시_재시도_로직() {
        // Given: Product 및 Outbox 생성
        Product product = ProductFixture.create("ITEM-001", 1L);
        productCommandPort.save(product);

        ProductOutbox outbox = ProductOutbox.create(
            product.getProductId(),
            ProductEventType.PRODUCT_CREATED,
            "{\"itemNo\": \"ITEM-001\"}"
        );
        productOutboxCommandPort.save(outbox);

        // Given: 외부 API 실패 응답 설정 (500 Internal Server Error)
        stubFor(post(urlEqualTo("/external/product/create"))
            .willReturn(aResponse().withStatus(500)));

        // When: Outbox 처리
        processProductOutboxUseCase.execute();

        // Then: FAILED 상태 확인
        ProductOutbox failed = productOutboxQueryPort.findById(outbox.getOutboxId()).orElseThrow();
        assertThat(failed.getStatus()).isEqualTo(OutboxStatus.FAILED);
        assertThat(failed.getRetryCount()).isEqualTo(1);
        assertThat(failed.getNextRetryAt()).isNotNull();

        // Exponential Backoff 확인 (1분 후)
        assertThat(failed.getNextRetryAt()).isAfter(LocalDateTime.now().plusSeconds(50));
        assertThat(failed.getNextRetryAt()).isBefore(LocalDateTime.now().plusSeconds(70));
    }

    @Test
    void 재시도_5회_초과_시_DEAD_LETTER() {
        // Given: retryCount = 4인 Outbox
        Product product = ProductFixture.create("ITEM-001", 1L);
        productCommandPort.save(product);

        ProductOutbox outbox = ProductOutbox.create(
            product.getProductId(),
            ProductEventType.PRODUCT_CREATED,
            "{\"itemNo\": \"ITEM-001\"}"
        );
        outbox.setRetryCount(4); // 이미 4번 재시도
        productOutboxCommandPort.save(outbox);

        // Given: 외부 API 실패 응답
        stubFor(post(urlEqualTo("/external/product/create"))
            .willReturn(aResponse().withStatus(500)));

        // When: Outbox 처리 (5번째 재시도)
        processProductOutboxUseCase.execute();

        // Then: DEAD_LETTER 상태 확인
        ProductOutbox deadLetter = productOutboxQueryPort.findById(outbox.getOutboxId()).orElseThrow();
        assertThat(deadLetter.getStatus()).isEqualTo(OutboxStatus.DEAD_LETTER);
        assertThat(deadLetter.getRetryCount()).isEqualTo(5);
    }
}
```

### Data Hash 변경 감지 테스트

```java
@SpringBootTest
class ProductDataHashChangeDetectionTest {

    @Autowired
    private ProductCommandPort productCommandPort;

    @Autowired
    private ProductOutboxQueryPort productOutboxQueryPort;

    @Test
    void 데이터_Hash_변경_시_Outbox_생성() {
        // Given: Product 생성 및 초기 Hash 설정
        Product product = Product.create(new ItemNo("ITEM-001"), new SellerId(1L));
        product.updateDataHash("detail", "hash1");
        productCommandPort.save(product);

        // 기존 Outbox 제거 (테스트 격리)
        productOutboxQueryPort.findByProductId(product.getProductId())
            .forEach(outbox -> productOutboxCommandPort.delete(outbox));

        // When: 동일한 Hash 업데이트 (변경 없음)
        product.updateDataHash("detail", "hash1");
        productCommandPort.save(product);

        // Then: Outbox 생성 안 됨
        List<ProductOutbox> outboxes1 = productOutboxQueryPort.findByProductId(product.getProductId());
        assertThat(outboxes1).isEmpty();

        // When: 다른 Hash 업데이트 (변경 있음)
        product.updateDataHash("detail", "hash2_new");
        productCommandPort.save(product);

        // Then: Outbox 생성됨
        List<ProductOutbox> outboxes2 = productOutboxQueryPort.findByProductId(product.getProductId());
        assertThat(outboxes2).hasSize(1);
        assertThat(outboxes2.get(0).getEventType()).isEqualTo(ProductEventType.PRODUCT_UPDATED);
    }

    @Test
    void 모든_데이터_수집_완료_시_isComplete_True() {
        // Given: Product 생성
        Product product = Product.create(new ItemNo("ITEM-001"), new SellerId(1L));
        productCommandPort.save(product);

        // Then: 초기 상태는 불완전
        assertThat(product.isComplete()).isFalse();

        // When: detail Hash 업데이트
        product.updateDataHash("detail", "hash1");
        productCommandPort.save(product);

        // Then: 아직 불완전 (option, inventory 없음)
        assertThat(product.isComplete()).isFalse();

        // When: option, inventory Hash 업데이트
        product.updateDataHash("option", "hash2");
        product.updateDataHash("inventory", "hash3");
        productCommandPort.save(product);

        // Then: 완료 (3개 영역 모두 존재)
        Product complete = productCommandPort.findById(product.getProductId()).orElseThrow();
        assertThat(complete.isComplete()).isTrue();
    }
}
```
