# MUSTIT-004: REST API Layer 구현

**Epic**: 머스트잇 셀러 크롤러
**Layer**: REST API Layer (Adapter-In)
**브랜치**: feature/MUSTIT-004-rest-api
**Jira URL**: (sync-to-jira 후 추가)

---

## 📝 목적

외부 클라이언트와의 HTTP 통신을 담당하는 REST API Layer 구현. Application Layer의 Use Case를 호출하여 비즈니스 로직을 수행합니다.

**핵심 역할**:
- RESTful API 설계
- Request/Response DTO 정의
- Validation 처리
- Exception Handling
- 인증/인가

---

## 🎯 요구사항

### 1. API 엔드포인트

#### 셀러 관리 API

- [ ] **POST /api/v1/sellers - 셀러 등록**
  - Request: `RegisterSellerRequest`
  - Response: `SellerResponse`
  - Status Code: 201 Created
  - Validation: @NotBlank, @Min(1), @Max(30)

- [ ] **GET /api/v1/sellers/{sellerId} - 셀러 조회**
  - Response: `SellerDetailResponse`
  - Status Code: 200 OK
  - 에러: 404 Not Found (셀러 없음)

- [ ] **GET /api/v1/sellers - 셀러 목록 조회**
  - Request: `ListSellersRequest` (Query Parameters)
  - Response: `PageResponse<SellerSummaryResponse>`
  - Status Code: 200 OK
  - 페이징: page, size (Offset-based)

- [ ] **PATCH /api/v1/sellers/{sellerId}/interval - 셀러 주기 변경**
  - Request: `UpdateSellerIntervalRequest`
  - Response: `SellerResponse`
  - Status Code: 200 OK
  - Validation: @Min(1), @Max(30)

- [ ] **POST /api/v1/sellers/{sellerId}/activate - 셀러 활성화**
  - Response: `SellerResponse`
  - Status Code: 200 OK

- [ ] **POST /api/v1/sellers/{sellerId}/deactivate - 셀러 비활성화**
  - Response: `SellerResponse`
  - Status Code: 200 OK

#### 메트릭 조회 API

- [ ] **GET /api/v1/metrics/crawling - 크롤링 메트릭 조회**
  - Request: `GetCrawlingMetricsRequest` (Query Parameters)
  - Response: `CrawlingMetricsResponse`
  - Status Code: 200 OK
  - Query Parameters: sellerId, date

#### UserAgent 풀 상태 API

- [ ] **GET /api/v1/user-agents/status - UserAgent 풀 상태 조회**
  - Response: `UserAgentPoolStatusResponse`
  - Status Code: 200 OK

#### 내부 API (EventBridge 트리거)

- [ ] **POST /api/internal/crawling/trigger - 크롤링 트리거**
  - Request: `TriggerCrawlingRequest`
  - Response: `CrawlingTriggeredResponse`
  - Status Code: 200 OK
  - 인증: API Key (EventBridge에서 호출)

---

### 2. Request/Response DTO

#### Request DTOs

- [ ] **RegisterSellerRequest**
  ```java
  public record RegisterSellerRequest(
      @NotBlank String sellerId,
      @NotBlank String name,
      @Min(1) @Max(30) Integer crawlingIntervalDays
  ) {}
  ```

- [ ] **UpdateSellerIntervalRequest**
  ```java
  public record UpdateSellerIntervalRequest(
      @Min(1) @Max(30) Integer crawlingIntervalDays
  ) {}
  ```

- [ ] **ListSellersRequest** (Query Parameters)
  ```java
  public record ListSellersRequest(
      String status,  // Nullable
      @Min(0) Integer page,
      @Min(1) @Max(100) Integer size
  ) {}
  ```

- [ ] **GetCrawlingMetricsRequest** (Query Parameters)
  ```java
  public record GetCrawlingMetricsRequest(
      @NotBlank String sellerId,
      @NotNull LocalDate date
  ) {}
  ```

- [ ] **TriggerCrawlingRequest**
  ```java
  public record TriggerCrawlingRequest(
      @NotBlank String sellerId
  ) {}
  ```

#### Response DTOs

- [ ] **SellerResponse**
  ```java
  public record SellerResponse(
      String sellerId,
      String name,
      SellerStatus status,
      Integer crawlingIntervalDays,
      Integer totalProductCount,
      LocalDateTime createdAt,
      LocalDateTime updatedAt
  ) {}
  ```

- [ ] **SellerDetailResponse** (SellerResponse와 동일 + 추가 정보)

- [ ] **SellerSummaryResponse**
  ```java
  public record SellerSummaryResponse(
      String sellerId,
      String name,
      SellerStatus status,
      Integer crawlingIntervalDays
  ) {}
  ```

- [ ] **PageResponse<T>** (공통 페이징 응답)
  ```java
  public record PageResponse<T>(
      List<T> content,
      int page,
      int size,
      long totalElements,
      int totalPages
  ) {}
  ```

- [ ] **CrawlingMetricsResponse**
  ```java
  public record CrawlingMetricsResponse(
      String sellerId,
      LocalDate date,
      Double successRate,
      Double progressRate,
      TaskStats taskStats
  ) {
      public record TaskStats(
          Integer total,
          Integer completed,
          Integer failed,
          Integer inProgress
      ) {}
  }
  ```

- [ ] **UserAgentPoolStatusResponse**
  ```java
  public record UserAgentPoolStatusResponse(
      Integer totalCount,
      Integer activeCount,
      Integer suspendedCount,
      Integer blockedCount
  ) {}
  ```

- [ ] **CrawlingTriggeredResponse**
  ```java
  public record CrawlingTriggeredResponse(
      Integer taskCount
  ) {}
  ```

#### Error Response

- [ ] **ErrorResponse** (공통 에러 응답)
  ```java
  public record ErrorResponse(
      String errorCode,
      String message,
      LocalDateTime timestamp,
      String path
  ) {}
  ```

---

### 3. Controller 구현

- [ ] **SellerApiController**
  - 셀러 등록, 조회, 목록, 주기 변경, 활성화, 비활성화
  - `@RestController`, `@RequestMapping("/api/v1/sellers")`
  - `@Validated` 사용

- [ ] **MetricsApiController**
  - 크롤링 메트릭 조회
  - `@RestController`, `@RequestMapping("/api/v1/metrics")`

- [ ] **UserAgentApiController**
  - UserAgent 풀 상태 조회
  - `@RestController`, `@RequestMapping("/api/v1/user-agents")`

- [ ] **InternalCrawlingApiController**
  - 크롤링 트리거 (EventBridge 호출)
  - `@RestController`, `@RequestMapping("/api/internal/crawling")`

---

### 4. Validation

- [ ] **Bean Validation 사용**
  - `@NotBlank`, `@NotNull`, `@Min`, `@Max`
  - `@Validated` 어노테이션 사용

- [ ] **Custom Validator**
  - SellerIdValidator: 셀러 ID 형식 검증
  - CrawlingIntervalValidator: 주기 범위 검증 (1-30일)

- [ ] **Validation Error Response**
  - 400 Bad Request
  - 필드별 에러 메시지 포함

---

### 5. Exception Handling

- [ ] **Global Exception Handler**
  - `@ControllerAdvice`, `@ExceptionHandler`
  - 모든 컨트롤러 예외 처리

- [ ] **Exception 매핑**
  - `SellerNotFoundException` → 404 Not Found
  - `DuplicateSellerIdException` → 409 Conflict
  - `MethodArgumentNotValidException` → 400 Bad Request
  - `Exception` → 500 Internal Server Error

- [ ] **Error Response 형식 통일**
  - ErrorResponse DTO 사용
  - errorCode, message, timestamp, path 포함

---

### 6. 인증/인가

#### 관리 API (/api/v1/*)

- [ ] **JWT 인증**
  - Authorization: Bearer {token}
  - 관리자 권한 검증
  - 401 Unauthorized (인증 실패)
  - 403 Forbidden (권한 없음)

#### 내부 API (/api/internal/*)

- [ ] **API Key 인증**
  - X-API-Key: {api_key}
  - EventBridge에서 호출 시 사용
  - 401 Unauthorized (API Key 없음 또는 잘못됨)

---

### 7. HTTP Status Code 전략

- [ ] **200 OK**: 성공 (GET, PATCH 요청)
- [ ] **201 Created**: 생성 성공 (POST 셀러 등록)
- [ ] **400 Bad Request**: Validation 실패
- [ ] **401 Unauthorized**: 인증 실패
- [ ] **403 Forbidden**: 권한 없음
- [ ] **404 Not Found**: 리소스 없음
- [ ] **409 Conflict**: 비즈니스 규칙 위반 (셀러 ID 중복)
- [ ] **500 Internal Server Error**: 서버 오류

---

## ⚠️ 제약사항

### Zero-Tolerance 규칙

- [ ] **RESTful 설계 원칙**
  - ✅ 리소스 기반 URL 설계 (`/api/v1/sellers/{sellerId}`)
  - ❌ 동작 기반 URL (`/api/v1/getSeller`)
  - HTTP Method 적절히 사용 (GET, POST, PATCH, DELETE)

- [ ] **일관된 Error Response 형식**
  - 모든 에러는 ErrorResponse DTO 사용
  - errorCode, message, timestamp, path 포함

- [ ] **Validation 필수**
  - `@Valid` 또는 `@Validated` 사용
  - Request DTO에 Bean Validation 어노테이션 필수

- [ ] **MockMvc 테스트 금지**
  - TestRestTemplate 사용 필수
  - 실제 HTTP 요청/응답 테스트

### 테스트 규칙

- [ ] **Integration Test (TestRestTemplate)**
  - 실제 HTTP 요청/응답 테스트
  - 200, 400, 404, 409 등 상태 코드 검증
  - Request/Response Body 검증

- [ ] **Validation 테스트**
  - 잘못된 입력 → 400 Bad Request
  - 필드별 에러 메시지 검증

- [ ] **인증/인가 테스트**
  - JWT 없음 → 401 Unauthorized
  - 잘못된 JWT → 401 Unauthorized
  - 권한 없음 → 403 Forbidden

- [ ] **테스트 커버리지 > 80%**
  - Controller 모든 엔드포인트 테스트
  - 성공/실패 시나리오 모두 테스트

---

## ✅ 완료 조건

- [ ] 9개 API 엔드포인트 구현 완료
- [ ] 모든 Request/Response DTO 정의 완료
- [ ] 4개 Controller 구현 완료
- [ ] Bean Validation 및 Custom Validator 구현 완료
- [ ] Global Exception Handler 구현 완료
- [ ] JWT 인증/인가 구현 완료 (관리 API)
- [ ] API Key 인증 구현 완료 (내부 API)
- [ ] Integration Test 작성 완료 (TestRestTemplate, 커버리지 > 80%)
- [ ] Validation 테스트 완료
- [ ] 인증/인가 테스트 완료
- [ ] Zero-Tolerance 규칙 준수
- [ ] 코드 리뷰 승인
- [ ] PR 머지 완료

---

## 🔗 관련 문서

- **PRD**: docs/prd/mustit-seller-crawler.md
- **Plan**: docs/prd/plans/MUSTIT-004-rest-api-plan.md (create-plan 후 생성)
- **Jira**: (sync-to-jira 후 추가)
- **REST API Layer 규칙**: docs/coding_convention/01-adapter-rest-api-layer/

---

## 📚 참고사항

### Controller 예시

```java
@RestController
@RequestMapping("/api/v1/sellers")
@RequiredArgsConstructor
public class SellerApiController {
    private final RegisterSellerUseCase registerSellerUseCase;
    private final GetSellerUseCase getSellerUseCase;

    @PostMapping
    public ResponseEntity<SellerResponse> registerSeller(
        @Valid @RequestBody RegisterSellerRequest request) {

        RegisterSellerCommand command = new RegisterSellerCommand(
            request.sellerId(),
            request.name(),
            request.crawlingIntervalDays()
        );

        SellerResponse response = registerSellerUseCase.execute(command);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{sellerId}")
    public ResponseEntity<SellerDetailResponse> getSeller(
        @PathVariable String sellerId) {

        GetSellerQuery query = new GetSellerQuery(sellerId);
        SellerDetailResponse response = getSellerUseCase.execute(query);

        return ResponseEntity.ok(response);
    }
}
```

### Global Exception Handler 예시

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SellerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSellerNotFound(
        SellerNotFoundException ex, HttpServletRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
            "SELLER_NOT_FOUND",
            ex.getMessage(),
            LocalDateTime.now(),
            request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(DuplicateSellerIdException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateSellerId(
        DuplicateSellerIdException ex, HttpServletRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
            "SELLER_ALREADY_EXISTS",
            ex.getMessage(),
            LocalDateTime.now(),
            request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
        MethodArgumentNotValidException ex, HttpServletRequest request) {

        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));

        ErrorResponse errorResponse = new ErrorResponse(
            "VALIDATION_FAILED",
            message,
            LocalDateTime.now(),
            request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}
```

### TestRestTemplate 예시

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SellerApiControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void registerSeller_success() {
        // Given
        RegisterSellerRequest request = new RegisterSellerRequest(
            "seller_123",
            "셀러명",
            1
        );

        // When
        ResponseEntity<SellerResponse> response = restTemplate.postForEntity(
            "/api/v1/sellers",
            request,
            SellerResponse.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().sellerId()).isEqualTo("seller_123");
    }

    @Test
    void registerSeller_duplicateSellerId_409Conflict() {
        // Given
        RegisterSellerRequest request = new RegisterSellerRequest(
            "existing_seller",
            "셀러명",
            1
        );

        // When
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
            "/api/v1/sellers",
            request,
            ErrorResponse.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().errorCode()).isEqualTo("SELLER_ALREADY_EXISTS");
    }
}
```

### JWT 인증 예시

```java
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/internal/**").permitAll()  // API Key 인증
                .requestMatchers("/api/v1/**").authenticated()     // JWT 인증
                .anyRequest().denyAll()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt());

        return http.build();
    }
}
```

### API Key 인증 예시

```java
@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-Key";

    @Value("${api.internal.key}")
    private String validApiKey;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        if (request.getRequestURI().startsWith("/api/internal/")) {
            String apiKey = request.getHeader(API_KEY_HEADER);

            if (!validApiKey.equals(apiKey)) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
```
