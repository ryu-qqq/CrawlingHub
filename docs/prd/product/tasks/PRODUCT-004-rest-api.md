# PRODUCT-004: Product REST API Layer 구현

**Bounded Context**: Product
**Layer**: REST API Layer
**브랜치**: feature/PRODUCT-004-rest-api

---

## 📝 목적

상품 조회 API 구현.

---

## 🎯 요구사항

### 1. API 엔드포인트

#### GET /api/v1/products/{itemNo} - 상품 단건 조회
- Request: itemNo (Path Variable)
- Response: `ProductResponse`
- Status Code: 200 OK, 404 Not Found
- Authentication: JWT

#### GET /api/v1/products - 상품 목록 조회 (페이징)
- Request: `page`, `size` (Query Parameters)
- Response: `Page<ProductResponse>`
- Status Code: 200 OK
- Authentication: JWT

### 2. Response DTO

```java
public record ProductResponse(
    String itemNo,
    Long sellerId,
    Map<String, String> dataHashes,
    Boolean isComplete,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
            product.getItemNo().value(),
            product.getSellerId().value(),
            product.getDataHashes(),
            product.isComplete(),
            product.getCreatedAt(),
            product.getUpdatedAt()
        );
    }
}
```

---

## ✅ 완료 조건

- [ ] GET /api/v1/products/{itemNo} 구현 완료
- [ ] GET /api/v1/products 구현 완료
- [ ] Integration Test 완료

---

## 🔗 관련 문서

- **Plan**: docs/prd/product/plans/PRODUCT-004-rest-api-plan.md

---

## 📚 참고사항

### ProductController 구현 예시

```java
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {
    private final GetProductUseCase getProductUseCase;
    private final ListProductsUseCase listProductsUseCase;

    @GetMapping("/{itemNo}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable String itemNo) {
        ProductResponse response = getProductUseCase.execute(new ItemNo(itemNo));
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> listProducts(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponse> response = listProductsUseCase.execute(pageable);
        return ResponseEntity.ok(response);
    }
}
```

### Integration Test 예시

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class ProductControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProductCommandPort productCommandPort;

    @BeforeEach
    void setUp() {
        // Given: Product 3개 등록
        Product product1 = ProductFixture.create("ITEM-001", 1L);
        product1.updateDataHash("detail", "hash1");
        product1.updateDataHash("option", "hash2");
        product1.updateDataHash("inventory", "hash3");

        Product product2 = ProductFixture.create("ITEM-002", 1L);
        product2.updateDataHash("detail", "hash4");

        Product product3 = ProductFixture.create("ITEM-003", 2L);

        productCommandPort.save(product1);
        productCommandPort.save(product2);
        productCommandPort.save(product3);
    }

    @Test
    void 상품_단건_조회_성공() {
        // When: 상품 조회
        ResponseEntity<ProductResponse> response =
            restTemplate.getForEntity("/api/v1/products/ITEM-001", ProductResponse.class);

        // Then: 200 OK
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ProductResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.itemNo()).isEqualTo("ITEM-001");
        assertThat(body.sellerId()).isEqualTo(1L);
        assertThat(body.isComplete()).isTrue();
        assertThat(body.dataHashes()).hasSize(3);
        assertThat(body.dataHashes()).containsKeys("detail", "option", "inventory");
    }

    @Test
    void 상품_단건_조회_실패_존재하지_않는_상품() {
        // When: 존재하지 않는 상품 조회
        ResponseEntity<String> response =
            restTemplate.getForEntity("/api/v1/products/INVALID", String.class);

        // Then: 404 Not Found
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void 상품_목록_조회_성공() {
        // When: 상품 목록 조회 (page=0, size=2)
        ResponseEntity<PagedProductResponse> response =
            restTemplate.getForEntity("/api/v1/products?page=0&size=2", PagedProductResponse.class);

        // Then: 200 OK
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        PagedProductResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.content()).hasSize(2);
        assertThat(body.totalElements()).isEqualTo(3);
        assertThat(body.totalPages()).isEqualTo(2);
        assertThat(body.number()).isEqualTo(0);
    }

    // Page 응답을 위한 테스트용 DTO
    public record PagedProductResponse(
        List<ProductResponse> content,
        int totalPages,
        long totalElements,
        int number,
        int size
    ) {}
}
```

### Response 예시

#### 단건 조회 (GET /api/v1/products/ITEM-001)

```json
{
  "itemNo": "ITEM-001",
  "sellerId": 1,
  "dataHashes": {
    "detail": "a1b2c3d4e5f6...",
    "option": "f6e5d4c3b2a1...",
    "inventory": "123456789abc..."
  },
  "isComplete": true,
  "createdAt": "2025-11-13T10:00:00",
  "updatedAt": "2025-11-13T10:05:00"
}
```

#### 목록 조회 (GET /api/v1/products?page=0&size=2)

```json
{
  "content": [
    {
      "itemNo": "ITEM-001",
      "sellerId": 1,
      "dataHashes": {
        "detail": "a1b2c3d4...",
        "option": "f6e5d4c3...",
        "inventory": "12345678..."
      },
      "isComplete": true,
      "createdAt": "2025-11-13T10:00:00",
      "updatedAt": "2025-11-13T10:05:00"
    },
    {
      "itemNo": "ITEM-002",
      "sellerId": 1,
      "dataHashes": {
        "detail": "abc123..."
      },
      "isComplete": false,
      "createdAt": "2025-11-13T10:01:00",
      "updatedAt": "2025-11-13T10:01:00"
    }
  ],
  "totalPages": 2,
  "totalElements": 3,
  "number": 0,
  "size": 2
}
```
