# SELLER-004: Seller REST API Layer 구현

**Bounded Context**: Seller
**Sub-Context**: Seller (셀러 자체)
**Layer**: REST API Layer
**브랜치**: feature/SELLER-004-rest-api

---

## 📝 목적

Seller 관리 API 구현.

---

## 🎯 요구사항

### 1. API 엔드포인트

#### POST /api/v1/sellers - 셀러 등록
- Request: `RegisterSellerRequest` (sellerId, name)
- Response: `SellerResponse`
- Status Code: 201 Created
- 비즈니스 규칙: INACTIVE 상태로 생성

#### GET /api/v1/sellers/{sellerId} - 셀러 조회
- Response: `SellerResponse`
- Status Code: 200 OK, 404 Not Found

#### GET /api/v1/sellers - 셀러 목록 조회
- Request: Query Parameters (page, size)
- Response: `Page<SellerResponse>`
- Status Code: 200 OK

#### PATCH /api/v1/sellers/{sellerId}/name - 이름 변경
- Request: `UpdateSellerNameRequest` (newName)
- Response: `SellerResponse`
- Status Code: 200 OK

#### POST /api/v1/sellers/{sellerId}/activate - 활성화
- Response: `SellerResponse`
- Status Code: 200 OK

#### POST /api/v1/sellers/{sellerId}/deactivate - 비활성화
- Response: `SellerResponse`
- Status Code: 200 OK, 400 Bad Request (EventBridge 활성화 중)

---

## ✅ 완료 조건

- [ ] 6개 API 엔드포인트 구현 완료
- [ ] Integration Test 완료 (TestRestTemplate)

---

## 🔗 관련 문서

- **Plan**: docs/prd/seller/seller/plans/SELLER-004-rest-api-plan.md

---

## 📚 참고사항

### SellerApiController 구현 예시

```java
@RestController
@RequestMapping("/api/v1/sellers")
@RequiredArgsConstructor
public class SellerApiController {
    private final RegisterSellerUseCase registerSellerUseCase;
    private final UpdateSellerNameUseCase updateSellerNameUseCase;
    private final ActivateSellerUseCase activateSellerUseCase;
    private final DeactivateSellerUseCase deactivateSellerUseCase;
    private final GetSellerUseCase getSellerUseCase;
    private final ListSellersUseCase listSellersUseCase;

    @PostMapping
    public ResponseEntity<SellerResponse> registerSeller(
        @Valid @RequestBody RegisterSellerRequest request) {

        RegisterSellerCommand command = new RegisterSellerCommand(
            request.sellerId(),
            request.name()
        );

        SellerId sellerId = registerSellerUseCase.execute(command);
        SellerResponse response = getSellerUseCase.execute(sellerId.value());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{sellerId}")
    public ResponseEntity<SellerResponse> getSeller(@PathVariable String sellerId) {
        SellerResponse response = getSellerUseCase.execute(sellerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<SellerResponse>> listSellers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<SellerResponse> response = listSellersUseCase.execute(pageable);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{sellerId}/name")
    public ResponseEntity<SellerResponse> updateSellerName(
        @PathVariable String sellerId,
        @Valid @RequestBody UpdateSellerNameRequest request) {

        UpdateSellerNameCommand command = new UpdateSellerNameCommand(
            sellerId,
            request.newName()
        );

        updateSellerNameUseCase.execute(command);
        SellerResponse response = getSellerUseCase.execute(sellerId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{sellerId}/activate")
    public ResponseEntity<SellerResponse> activateSeller(@PathVariable String sellerId) {
        ActivateSellerCommand command = new ActivateSellerCommand(sellerId);
        activateSellerUseCase.execute(command);

        SellerResponse response = getSellerUseCase.execute(sellerId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{sellerId}/deactivate")
    public ResponseEntity<SellerResponse> deactivateSeller(@PathVariable String sellerId) {
        DeactivateSellerCommand command = new DeactivateSellerCommand(sellerId);
        deactivateSellerUseCase.execute(command);

        SellerResponse response = getSellerUseCase.execute(sellerId);
        return ResponseEntity.ok(response);
    }
}
```

### Request/Response DTO

```java
// Request DTOs
public record RegisterSellerRequest(
    @NotBlank String sellerId,
    @NotBlank String name
) {}

public record UpdateSellerNameRequest(
    @NotBlank String newName
) {}

// Response DTO
public record SellerResponse(
    String sellerId,
    String name,
    String status,
    Integer totalProductCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
```

### Integration Test (TestRestTemplate)

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class SellerApiControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SellerCommandPort sellerCommandPort;

    @Test
    void 셀러_등록_성공() {
        // Given: 등록 요청
        RegisterSellerRequest request = new RegisterSellerRequest(
            "SELLER-001",
            "테스트 셀러"
        );

        // When: POST /api/v1/sellers
        ResponseEntity<SellerResponse> response = restTemplate.postForEntity(
            "/api/v1/sellers",
            request,
            SellerResponse.class
        );

        // Then: 201 Created, INACTIVE 상태
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().sellerId()).isEqualTo("SELLER-001");
        assertThat(response.getBody().name()).isEqualTo("테스트 셀러");
        assertThat(response.getBody().status()).isEqualTo("INACTIVE");
    }

    @Test
    void 이름_변경_성공() {
        // Given: Seller 등록
        Seller seller = Seller.create(new SellerId("SELLER-002"), "원래 이름");
        sellerCommandPort.save(seller);

        // When: PATCH /api/v1/sellers/{sellerId}/name
        UpdateSellerNameRequest request = new UpdateSellerNameRequest("새 이름");
        ResponseEntity<SellerResponse> response = restTemplate.exchange(
            "/api/v1/sellers/SELLER-002/name",
            HttpMethod.PATCH,
            new HttpEntity<>(request),
            SellerResponse.class
        );

        // Then: 200 OK, 이름 변경됨
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().name()).isEqualTo("새 이름");
    }

    @Test
    void 활성화_성공() {
        // Given: INACTIVE Seller
        Seller seller = Seller.create(new SellerId("SELLER-003"), "테스트 셀러");
        sellerCommandPort.save(seller);

        // When: POST /api/v1/sellers/{sellerId}/activate
        ResponseEntity<SellerResponse> response = restTemplate.postForEntity(
            "/api/v1/sellers/SELLER-003/activate",
            null,
            SellerResponse.class
        );

        // Then: 200 OK, ACTIVE 상태
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().status()).isEqualTo("ACTIVE");
    }

    @Test
    void 비활성화_실패_EventBridge_활성화_중() {
        // Given: ACTIVE Seller + 활성화된 EventBridge
        Seller seller = SellerFixture.createActive("SELLER-004", "테스트 셀러");
        sellerCommandPort.save(seller);

        // EventBridge 등록 (별도 API - EventBridge Context)
        // POST /api/v1/admin/schedules

        // When: POST /api/v1/sellers/{sellerId}/deactivate
        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/v1/sellers/SELLER-004/deactivate",
            null,
            String.class
        );

        // Then: 400 Bad Request
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Active EventBridge schedules exist");
    }
}
```

### Exception Handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SellerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSellerNotFound(SellerNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            "SELLER_NOT_FOUND",
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(DuplicateSellerIdException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateSellerId(DuplicateSellerIdException ex) {
        ErrorResponse error = new ErrorResponse(
            "DUPLICATE_SELLER_ID",
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(SellerDeactivationNotAllowedException.class)
    public ResponseEntity<ErrorResponse> handleDeactivationNotAllowed(SellerDeactivationNotAllowedException ex) {
        ErrorResponse error = new ErrorResponse(
            "DEACTIVATION_NOT_ALLOWED",
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}

public record ErrorResponse(
    String errorCode,
    String message,
    LocalDateTime timestamp
) {}
```

### 중요 변경사항

⚠️ **PATCH /sellers/{id}/interval 제거**:
- 스케줄링 변경은 EventBridge Context에서 관리
- Admin API: POST /api/v1/admin/schedules, PATCH /api/v1/admin/schedules/{id}

⚠️ **PATCH /sellers/{id}/name 추가**:
- Seller는 이름만 변경 가능

⚠️ **비활성화 조건 검증**:
- 모든 EventBridge가 비활성화되어야 Seller 비활성화 가능
- 위반 시 400 Bad Request
