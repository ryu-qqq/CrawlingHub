# EVENTBRIDGE-004: EventBridge REST API Layer 구현

**Bounded Context**: Seller
**Sub-Context**: EventBridge (스케줄링)
**Layer**: REST API Layer
**브랜치**: feature/EVENTBRIDGE-004-rest-api

---

## 📝 목적

EventBridge 스케줄 관리 Admin API 구현.

---

## 🎯 요구사항

### 1. API 엔드포인트 (Admin 전용)

#### POST /api/v1/admin/schedules - 스케줄 등록
- Request: `RegisterScheduleRequest` (sellerId, intervalDays)
- Response: `ScheduleResponse`
- Status Code: 201 Created, 400 Bad Request (INACTIVE Seller), 404 Not Found (Seller 없음)
- 비즈니스 규칙: **ACTIVE Seller만** 스케줄 등록 가능

#### GET /api/v1/admin/schedules/{scheduleId} - 스케줄 조회
- Response: `ScheduleResponse`
- Status Code: 200 OK, 404 Not Found

#### GET /api/v1/admin/schedules - 스케줄 목록 조회
- Request: Query Parameters (page, size, sellerId, status)
- Response: `Page<ScheduleResponse>`
- Status Code: 200 OK

#### PATCH /api/v1/admin/schedules/{scheduleId}/interval - 주기 변경
- Request: `UpdateScheduleIntervalRequest` (newIntervalDays)
- Response: `ScheduleResponse`
- Status Code: 200 OK

#### POST /api/v1/admin/schedules/{scheduleId}/activate - 활성화
- Response: `ScheduleResponse`
- Status Code: 200 OK

#### POST /api/v1/admin/schedules/{scheduleId}/deactivate - 비활성화
- Response: `ScheduleResponse`
- Status Code: 200 OK

---

## ✅ 완료 조건

- [ ] 6개 Admin API 엔드포인트 구현 완료
- [ ] Integration Test 완료 (TestRestTemplate)
- [ ] Admin 권한 검증 적용

---

## 🔗 관련 문서

- **Plan**: docs/prd/seller/eventbridge/plans/EVENTBRIDGE-004-rest-api-plan.md

---

## 📚 참고사항

### ScheduleAdminApiController 구현 예시

```java
@RestController
@RequestMapping("/api/v1/admin/schedules")
@RequiredArgsConstructor
public class ScheduleAdminApiController {
    private final RegisterScheduleUseCase registerScheduleUseCase;
    private final UpdateScheduleIntervalUseCase updateScheduleIntervalUseCase;
    private final ActivateScheduleUseCase activateScheduleUseCase;
    private final DeactivateScheduleUseCase deactivateScheduleUseCase;
    private final GetScheduleUseCase getScheduleUseCase;
    private final ListSchedulesUseCase listSchedulesUseCase;

    @PostMapping
    public ResponseEntity<ScheduleResponse> registerSchedule(
        @Valid @RequestBody RegisterScheduleRequest request) {

        RegisterScheduleCommand command = new RegisterScheduleCommand(
            request.sellerId(),
            request.intervalDays()
        );

        ScheduleId scheduleId = registerScheduleUseCase.execute(command);
        ScheduleResponse response = getScheduleUseCase.execute(scheduleId.value());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{scheduleId}")
    public ResponseEntity<ScheduleResponse> getSchedule(@PathVariable String scheduleId) {
        ScheduleResponse response = getScheduleUseCase.execute(scheduleId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<ScheduleResponse>> listSchedules(
        @RequestParam(required = false) String sellerId,
        @RequestParam(required = false) String status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ScheduleResponse> response = listSchedulesUseCase.execute(sellerId, status, pageable);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{scheduleId}/interval")
    public ResponseEntity<ScheduleResponse> updateScheduleInterval(
        @PathVariable String scheduleId,
        @Valid @RequestBody UpdateScheduleIntervalRequest request) {

        UpdateScheduleIntervalCommand command = new UpdateScheduleIntervalCommand(
            scheduleId,
            request.newIntervalDays()
        );

        updateScheduleIntervalUseCase.execute(command);
        ScheduleResponse response = getScheduleUseCase.execute(scheduleId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{scheduleId}/activate")
    public ResponseEntity<ScheduleResponse> activateSchedule(@PathVariable String scheduleId) {
        ActivateScheduleCommand command = new ActivateScheduleCommand(scheduleId);
        activateScheduleUseCase.execute(command);

        ScheduleResponse response = getScheduleUseCase.execute(scheduleId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{scheduleId}/deactivate")
    public ResponseEntity<ScheduleResponse> deactivateSchedule(@PathVariable String scheduleId) {
        DeactivateScheduleCommand command = new DeactivateScheduleCommand(scheduleId);
        deactivateScheduleUseCase.execute(command);

        ScheduleResponse response = getScheduleUseCase.execute(scheduleId);
        return ResponseEntity.ok(response);
    }
}
```

### Request/Response DTO

```java
// Request DTOs
public record RegisterScheduleRequest(
    @NotBlank String sellerId,
    @Min(1) @Max(365) Integer intervalDays
) {}

public record UpdateScheduleIntervalRequest(
    @Min(1) @Max(365) Integer newIntervalDays
) {}

// Response DTO
public record ScheduleResponse(
    String scheduleId,
    String sellerId,
    Integer intervalDays,
    String scheduleRule,
    String scheduleExpression,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
```

### Integration Test (TestRestTemplate)

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class ScheduleAdminApiControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SellerCommandPort sellerCommandPort;

    @Autowired
    private CrawlingScheduleCommandPort scheduleCommandPort;

    @Test
    void 스케줄_등록_성공_ACTIVE_Seller() {
        // Given: ACTIVE Seller
        Seller seller = Seller.create(new SellerId("SELLER-001"), "테스트 셀러");
        seller.activate(); // ACTIVE 상태로 변경
        sellerCommandPort.save(seller);

        // When: 스케줄 등록 요청
        RegisterScheduleRequest request = new RegisterScheduleRequest(
            "SELLER-001",
            1
        );

        ResponseEntity<ScheduleResponse> response = restTemplate.postForEntity(
            "/api/v1/admin/schedules",
            request,
            ScheduleResponse.class
        );

        // Then: 201 Created, ACTIVE 상태
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().sellerId()).isEqualTo("SELLER-001");
        assertThat(response.getBody().intervalDays()).isEqualTo(1);
        assertThat(response.getBody().status()).isEqualTo("ACTIVE");
        assertThat(response.getBody().scheduleRule()).isEqualTo("mustit-crawler-SELLER-001");
        assertThat(response.getBody().scheduleExpression()).isEqualTo("rate(1 days)");
    }

    @Test
    void 스케줄_등록_실패_INACTIVE_Seller() {
        // Given: INACTIVE Seller (등록 시 기본 상태)
        Seller seller = Seller.create(new SellerId("SELLER-002"), "테스트 셀러");
        sellerCommandPort.save(seller);

        // When: 스케줄 등록 시도
        RegisterScheduleRequest request = new RegisterScheduleRequest(
            "SELLER-002",
            1
        );

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
            "/api/v1/admin/schedules",
            request,
            ErrorResponse.class
        );

        // Then: 400 Bad Request
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errorCode()).isEqualTo("SELLER_NOT_ACTIVE");
        assertThat(response.getBody().message()).contains("Cannot register schedule for inactive seller");
    }

    @Test
    void 스케줄_주기_변경_성공() {
        // Given: 등록된 스케줄
        Seller seller = SellerFixture.createActive("SELLER-003", "테스트 셀러");
        sellerCommandPort.save(seller);

        CrawlingSchedule schedule = CrawlingSchedule.create(
            new SellerId("SELLER-003"),
            new CrawlingInterval(1)
        );
        scheduleCommandPort.save(schedule);

        // When: 주기 변경 (1일 → 7일)
        UpdateScheduleIntervalRequest request = new UpdateScheduleIntervalRequest(7);
        ResponseEntity<ScheduleResponse> response = restTemplate.exchange(
            "/api/v1/admin/schedules/" + schedule.getScheduleId().value() + "/interval",
            HttpMethod.PATCH,
            new HttpEntity<>(request),
            ScheduleResponse.class
        );

        // Then: 200 OK, 주기 변경됨
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().intervalDays()).isEqualTo(7);
        assertThat(response.getBody().scheduleExpression()).isEqualTo("rate(7 days)");
    }

    @Test
    void 스케줄_활성화_성공() {
        // Given: 비활성화된 스케줄
        Seller seller = SellerFixture.createActive("SELLER-004", "테스트 셀러");
        sellerCommandPort.save(seller);

        CrawlingSchedule schedule = CrawlingSchedule.create(
            new SellerId("SELLER-004"),
            new CrawlingInterval(1)
        );
        schedule.deactivate();
        scheduleCommandPort.save(schedule);

        // When: 활성화
        ResponseEntity<ScheduleResponse> response = restTemplate.postForEntity(
            "/api/v1/admin/schedules/" + schedule.getScheduleId().value() + "/activate",
            null,
            ScheduleResponse.class
        );

        // Then: 200 OK, ACTIVE 상태
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().status()).isEqualTo("ACTIVE");
    }

    @Test
    void 스케줄_비활성화_성공() {
        // Given: 활성화된 스케줄
        Seller seller = SellerFixture.createActive("SELLER-005", "테스트 셀러");
        sellerCommandPort.save(seller);

        CrawlingSchedule schedule = CrawlingSchedule.create(
            new SellerId("SELLER-005"),
            new CrawlingInterval(1)
        );
        scheduleCommandPort.save(schedule);

        // When: 비활성화
        ResponseEntity<ScheduleResponse> response = restTemplate.postForEntity(
            "/api/v1/admin/schedules/" + schedule.getScheduleId().value() + "/deactivate",
            null,
            ScheduleResponse.class
        );

        // Then: 200 OK, INACTIVE 상태
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().status()).isEqualTo("INACTIVE");
    }

    @Test
    void 스케줄_목록_조회_sellerId_필터링() {
        // Given: 2개 Seller의 스케줄
        Seller seller1 = SellerFixture.createActive("SELLER-006", "셀러1");
        Seller seller2 = SellerFixture.createActive("SELLER-007", "셀러2");
        sellerCommandPort.save(seller1);
        sellerCommandPort.save(seller2);

        CrawlingSchedule schedule1 = CrawlingSchedule.create(new SellerId("SELLER-006"), new CrawlingInterval(1));
        CrawlingSchedule schedule2 = CrawlingSchedule.create(new SellerId("SELLER-007"), new CrawlingInterval(7));
        scheduleCommandPort.save(schedule1);
        scheduleCommandPort.save(schedule2);

        // When: SELLER-006 스케줄만 조회
        ResponseEntity<RestResponsePage<ScheduleResponse>> response = restTemplate.exchange(
            "/api/v1/admin/schedules?sellerId=SELLER-006&page=0&size=10",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<RestResponsePage<ScheduleResponse>>() {}
        );

        // Then: 200 OK, SELLER-006 스케줄만 반환
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().get(0).sellerId()).isEqualTo("SELLER-006");
    }

    @Test
    void 스케줄_목록_조회_status_필터링() {
        // Given: ACTIVE/INACTIVE 스케줄
        Seller seller = SellerFixture.createActive("SELLER-008", "테스트 셀러");
        sellerCommandPort.save(seller);

        CrawlingSchedule activeSchedule = CrawlingSchedule.create(new SellerId("SELLER-008"), new CrawlingInterval(1));
        CrawlingSchedule inactiveSchedule = CrawlingSchedule.create(new SellerId("SELLER-008"), new CrawlingInterval(7));
        inactiveSchedule.deactivate();
        scheduleCommandPort.save(activeSchedule);
        scheduleCommandPort.save(inactiveSchedule);

        // When: ACTIVE 스케줄만 조회
        ResponseEntity<RestResponsePage<ScheduleResponse>> response = restTemplate.exchange(
            "/api/v1/admin/schedules?status=ACTIVE&page=0&size=10",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<RestResponsePage<ScheduleResponse>>() {}
        );

        // Then: 200 OK, ACTIVE 스케줄만 반환
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).allMatch(s -> s.status().equals("ACTIVE"));
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

    @ExceptionHandler(SellerNotActiveException.class)
    public ResponseEntity<ErrorResponse> handleSellerNotActive(SellerNotActiveException ex) {
        ErrorResponse error = new ErrorResponse(
            "SELLER_NOT_ACTIVE",
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ScheduleNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleScheduleNotFound(ScheduleNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            "SCHEDULE_NOT_FOUND",
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(ScheduleInvalidStateException.class)
    public ResponseEntity<ErrorResponse> handleScheduleInvalidState(ScheduleInvalidStateException ex) {
        ErrorResponse error = new ErrorResponse(
            "SCHEDULE_INVALID_STATE",
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

### Validation 테스트

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class ScheduleValidationIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void validation_sellerId_null() {
        // When: sellerId null
        RegisterScheduleRequest request = new RegisterScheduleRequest(
            null,
            1
        );

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
            "/api/v1/admin/schedules",
            request,
            ErrorResponse.class
        );

        // Then: 400 Bad Request
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).contains("sellerId");
        assertThat(response.getBody().message()).contains("must not be blank");
    }

    @Test
    void validation_intervalDays_범위_초과() {
        // When: intervalDays > 365
        RegisterScheduleRequest request = new RegisterScheduleRequest(
            "SELLER-001",
            366
        );

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
            "/api/v1/admin/schedules",
            request,
            ErrorResponse.class
        );

        // Then: 400 Bad Request
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).contains("intervalDays");
        assertThat(response.getBody().message()).contains("must be less than or equal to 365");
    }

    @Test
    void validation_intervalDays_0이하() {
        // When: intervalDays = 0
        RegisterScheduleRequest request = new RegisterScheduleRequest(
            "SELLER-001",
            0
        );

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
            "/api/v1/admin/schedules",
            request,
            ErrorResponse.class
        );

        // Then: 400 Bad Request
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).contains("intervalDays");
        assertThat(response.getBody().message()).contains("must be greater than or equal to 1");
    }
}
```

### 중요 변경사항

⚠️ **ACTIVE Seller만 스케줄 등록 가능**:
- RegisterScheduleUseCase에서 Seller ACTIVE 검증
- INACTIVE Seller 등록 시도 → 400 Bad Request

⚠️ **Admin 전용 API**:
- `/api/v1/admin/schedules` 경로 사용
- Admin 권한 검증 필요 (Spring Security로 구현)

⚠️ **Outbox Pattern 통합**:
- 스케줄 등록/변경/활성화/비활성화 시 SchedulerOutbox 이벤트 생성
- EventBridge API 호출은 별도 Worker에서 Outbox 읽어서 처리

⚠️ **RESTful 설계**:
- PATCH /interval (부분 업데이트)
- POST /activate, POST /deactivate (상태 전환 액션)
