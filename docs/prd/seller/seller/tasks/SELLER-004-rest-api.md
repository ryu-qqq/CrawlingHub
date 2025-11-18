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

### 1. Controller 설계

**CQRS 분리 원칙**:
- **SellerCommandController**: 상태 변경 (POST, PUT, PATCH, DELETE)
- **SellerQueryController**: 조회 (GET)

### 2. API 엔드포인트

#### Command API (SellerCommandController)

**POST /api/v1/sellers - 셀러 등록**
- Request: `RegisterSellerApiRequest` (sellerId, name)
- Response: `SellerApiResponse`
- Status Code: 201 Created
- 비즈니스 규칙: INACTIVE 상태로 생성

**PATCH /api/v1/sellers/{sellerId}/name - 이름 변경**
- Request: `UpdateSellerNameApiRequest` (newName)
- Response: `SellerApiResponse`
- Status Code: 200 OK

**POST /api/v1/sellers/{sellerId}/activate - 활성화**
- Response: `SellerApiResponse`
- Status Code: 200 OK

**POST /api/v1/sellers/{sellerId}/deactivate - 비활성화**
- Response: `SellerApiResponse`
- Status Code: 200 OK, 400 Bad Request (EventBridge 활성화 중)

#### Query API (SellerQueryController)

**GET /api/v1/sellers/{sellerId} - 셀러 조회**
- Response: `SellerApiResponse`
- Status Code: 200 OK, 404 Not Found

**GET /api/v1/sellers - 셀러 목록 조회**
- Request: Query Parameters (page, size)
- Response: `Page<SellerApiResponse>`
- Status Code: 200 OK

---

## ✅ 완료 조건

- [ ] Command Controller 구현 완료 (4개 엔드포인트)
- [ ] Query Controller 구현 완료 (2개 엔드포인트)
- [ ] API Mapper 구현 완료 (@Component Bean)
- [ ] API Error Mapper 구현 완료 (@Component Bean)
- [ ] Integration Test 완료 (TestRestTemplate)

---

## 🔗 관련 문서

- **Plan**: docs/prd/seller/seller/plans/SELLER-004-rest-api-plan.md

---

## 📚 참고사항

### SellerCommandController 구현 (상태 변경)

**위치**: `adapter-in/rest-api/seller/controller/`

```java
@RestController
@RequestMapping("/api/v1/sellers")
@RequiredArgsConstructor
public class SellerCommandController {
    private final RegisterSellerUseCase registerSellerUseCase;
    private final UpdateSellerNameUseCase updateSellerNameUseCase;
    private final ActivateSellerUseCase activateSellerUseCase;
    private final DeactivateSellerUseCase deactivateSellerUseCase;
    private final GetSellerUseCase getSellerUseCase; // 응답용
    private final SellerApiMapper sellerApiMapper; // @Component Bean

    @PostMapping
    public ResponseEntity<SellerApiResponse> registerSeller(
        @Valid @RequestBody RegisterSellerApiRequest request) {

        RegisterSellerCommand command = sellerApiMapper.toCommand(request);
        SellerId sellerId = registerSellerUseCase.execute(command);

        SellerResponse useCaseResponse = getSellerUseCase.execute(sellerId.value());
        SellerApiResponse response = sellerApiMapper.toApiResponse(useCaseResponse);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{sellerId}/name")
    public ResponseEntity<SellerApiResponse> updateSellerName(
        @PathVariable String sellerId,
        @Valid @RequestBody UpdateSellerNameApiRequest request) {

        UpdateSellerNameCommand command = sellerApiMapper.toCommand(sellerId, request);
        updateSellerNameUseCase.execute(command);

        SellerResponse useCaseResponse = getSellerUseCase.execute(sellerId);
        SellerApiResponse response = sellerApiMapper.toApiResponse(useCaseResponse);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{sellerId}/activate")
    public ResponseEntity<SellerApiResponse> activateSeller(@PathVariable String sellerId) {
        ActivateSellerCommand command = new ActivateSellerCommand(sellerId);
        activateSellerUseCase.execute(command);

        SellerResponse useCaseResponse = getSellerUseCase.execute(sellerId);
        SellerApiResponse response = sellerApiMapper.toApiResponse(useCaseResponse);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{sellerId}/deactivate")
    public ResponseEntity<SellerApiResponse> deactivateSeller(@PathVariable String sellerId) {
        DeactivateSellerCommand command = new DeactivateSellerCommand(sellerId);
        deactivateSellerUseCase.execute(command);

        SellerResponse useCaseResponse = getSellerUseCase.execute(sellerId);
        SellerApiResponse response = sellerApiMapper.toApiResponse(useCaseResponse);

        return ResponseEntity.ok(response);
    }
}
```

### SellerQueryController 구현 (조회)

**위치**: `adapter-in/rest-api/seller/controller/`

```java
@RestController
@RequestMapping("/api/v1/sellers")
@RequiredArgsConstructor
public class SellerQueryController {
    private final GetSellerUseCase getSellerUseCase;
    private final ListSellersUseCase listSellersUseCase;
    private final SellerApiMapper sellerApiMapper; // @Component Bean

    @GetMapping("/{sellerId}")
    public ResponseEntity<SellerApiResponse> getSeller(@PathVariable String sellerId) {
        SellerResponse useCaseResponse = getSellerUseCase.execute(sellerId);
        SellerApiResponse response = sellerApiMapper.toApiResponse(useCaseResponse);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<SellerApiResponse>> listSellers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<SellerResponse> useCaseResponses = listSellersUseCase.execute(pageable);
        Page<SellerApiResponse> responses = useCaseResponses.map(sellerApiMapper::toApiResponse);

        return ResponseEntity.ok(responses);
    }
}
```

### Request/Response DTO (API Layer)

**네이밍 규칙**:
- Command Request: `*ApiRequest` (예: `RegisterSellerApiRequest`)
- Query Request: `*ApiRequest` (예: `SellerSearchApiRequest`)
- Response: `*ApiResponse` (예: `SellerApiResponse`)

**위치**: `adapter-in/rest-api/seller/dto/`

```java
// Command Request DTOs (dto/command/)
public record RegisterSellerApiRequest(
    @NotBlank String sellerId,
    @NotBlank String name
) {}

public record UpdateSellerNameApiRequest(
    @NotBlank String newName
) {}

// Response DTO (dto/response/)
public record SellerApiResponse(
    String sellerId,
    String name,
    String status,
    Integer totalProductCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
```

### SellerApiMapper (@Component Bean)

**위치**: `adapter-in/rest-api/seller/mapper/`

**핵심 원칙**:
- `@Component`로 DI (Static 메서드 금지)
- API DTO ↔ UseCase DTO 변환
- 의존성 주입 가능 (MessageSource, Properties 등)

```java
@Component
@RequiredArgsConstructor
public class SellerApiMapper {
    // 필요 시 의존성 주입 가능
    // private final MessageSource messageSource;

    public RegisterSellerCommand toCommand(RegisterSellerApiRequest request) {
        return new RegisterSellerCommand(
            request.sellerId(),
            request.name()
        );
    }

    public UpdateSellerNameCommand toCommand(String sellerId, UpdateSellerNameApiRequest request) {
        return new UpdateSellerNameCommand(
            sellerId,
            request.newName()
        );
    }

    public SellerApiResponse toApiResponse(SellerResponse useCaseResponse) {
        return new SellerApiResponse(
            useCaseResponse.sellerId(),
            useCaseResponse.name(),
            useCaseResponse.status(),
            useCaseResponse.totalProductCount(),
            useCaseResponse.createdAt(),
            useCaseResponse.updatedAt()
        );
    }
}
```

### Integration Test (TestRestTemplate)

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureTestRestTemplate
@Transactional
class SellerApiControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SellerPersistencePort sellerPersistencePort;

    @Test
    void 셀러_등록_성공() {
        // Given: 등록 요청
        RegisterSellerApiRequest request = new RegisterSellerApiRequest(
            "SELLER-001",
            "테스트 셀러"
        );

        // When: POST /api/v1/sellers
        ResponseEntity<SellerApiResponse> response = restTemplate.postForEntity(
            "/api/v1/sellers",
            request,
            SellerApiResponse.class
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
        sellerPersistencePort.save(seller);

        // When: PATCH /api/v1/sellers/{sellerId}/name
        UpdateSellerNameApiRequest request = new UpdateSellerNameApiRequest("새 이름");
        ResponseEntity<SellerApiResponse> response = restTemplate.exchange(
            "/api/v1/sellers/SELLER-002/name",
            HttpMethod.PATCH,
            new HttpEntity<>(request),
            SellerApiResponse.class
        );

        // Then: 200 OK, 이름 변경됨
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().name()).isEqualTo("새 이름");
    }

    @Test
    void 활성화_성공() {
        // Given: INACTIVE Seller
        Seller seller = Seller.create(new SellerId("SELLER-003"), "테스트 셀러");
        sellerPersistencePort.save(seller);

        // When: POST /api/v1/sellers/{sellerId}/activate
        ResponseEntity<SellerApiResponse> response = restTemplate.postForEntity(
            "/api/v1/sellers/SELLER-003/activate",
            null,
            SellerApiResponse.class
        );

        // Then: 200 OK, ACTIVE 상태
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().status()).isEqualTo("ACTIVE");
    }

    @Test
    void 비활성화_실패_EventBridge_활성화_중() {
        // Given: ACTIVE Seller + 활성화된 EventBridge
        Seller seller = SellerFixture.createActive("SELLER-004", "테스트 셀러");
        sellerPersistencePort.save(seller);

        // EventBridge 등록 (별도 API - EventBridge Context)
        // POST /api/v1/admin/schedules

        // When: POST /api/v1/sellers/{sellerId}/deactivate
        ResponseEntity<ErrorInfo> response = restTemplate.postForEntity(
            "/api/v1/sellers/SELLER-004/deactivate",
            null,
            ErrorInfo.class
        );

        // Then: 400 Bad Request
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().errorCode()).isEqualTo("DEACTIVATION_NOT_ALLOWED");
        assertThat(response.getBody().message()).contains("Active EventBridge schedules exist");
    }
}
```

### SellerApiErrorMapper (@Component Bean)

**위치**: `adapter-in/rest-api/seller/error/`

**핵심 원칙**:
- Domain Exception → HTTP 변환
- `@Component`로 DI (ErrorMapper 인터페이스 구현)
- ErrorMapperRegistry에 등록

```java
@Component
@RequiredArgsConstructor
public class SellerApiErrorMapper implements ErrorMapper {

    @Override
    public boolean supports(Exception exception) {
        return exception instanceof SellerNotFoundException ||
               exception instanceof DuplicateSellerIdException ||
               exception instanceof SellerDeactivationNotAllowedException;
    }

    @Override
    public ErrorInfo map(Exception exception) {
        if (exception instanceof SellerNotFoundException) {
            return ErrorInfo.of(
                "SELLER_NOT_FOUND",
                exception.getMessage(),
                HttpStatus.NOT_FOUND
            );
        }

        if (exception instanceof DuplicateSellerIdException) {
            return ErrorInfo.of(
                "DUPLICATE_SELLER_ID",
                exception.getMessage(),
                HttpStatus.CONFLICT
            );
        }

        if (exception instanceof SellerDeactivationNotAllowedException) {
            return ErrorInfo.of(
                "DEACTIVATION_NOT_ALLOWED",
                exception.getMessage(),
                HttpStatus.BAD_REQUEST
            );
        }

        throw new IllegalStateException("Unsupported exception: " + exception.getClass());
    }
}
```

### GlobalExceptionHandler (공통)

**위치**: `adapter-in/rest-api/common/controller/`

```java
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final ErrorMapperRegistry errorMapperRegistry;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorInfo> handleException(Exception ex) {
        ErrorInfo errorInfo = errorMapperRegistry.map(ex);
        return ResponseEntity.status(errorInfo.httpStatus()).body(errorInfo);
    }
}
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
