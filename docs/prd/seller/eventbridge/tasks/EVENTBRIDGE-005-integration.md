# EVENTBRIDGE-005: EventBridge Integration Test 구현

**Bounded Context**: Seller
**Sub-Context**: EventBridge (스케줄링)
**Layer**: Integration Test
**브랜치**: feature/EVENTBRIDGE-005-integration

---

## 📝 목적

EventBridge Admin API E2E 시나리오 테스트.

---

## 🎯 요구사항

### 1. E2E 시나리오

#### 시나리오 1: 스케줄 등록 → 주기 변경 → 비활성화 → 활성화

- [ ] **Given: ACTIVE Seller 등록**
  - POST /api/v1/sellers (INACTIVE 상태 생성)
  - POST /api/v1/sellers/{id}/activate (ACTIVE 상태 변경)

- [ ] **Then: Seller 등록 확인**
  - 200 OK
  - status = ACTIVE

- [ ] **When: 스케줄 등록 (Admin API)**
  - POST /api/v1/admin/schedules
  - sellerId: "SELLER-001"
  - intervalDays: 1

- [ ] **Then: 스케줄 등록 성공**
  - 201 Created
  - status = ACTIVE
  - scheduleRule = "mustit-crawler-SELLER-001"
  - scheduleExpression = "rate(1 days)"

- [ ] **When: 스케줄 주기 변경 (1일 → 7일)**
  - PATCH /api/v1/admin/schedules/{id}/interval
  - newIntervalDays: 7

- [ ] **Then: 주기 변경 확인**
  - 200 OK
  - intervalDays = 7
  - scheduleExpression = "rate(7 days)"

- [ ] **When: 스케줄 비활성화**
  - POST /api/v1/admin/schedules/{id}/deactivate

- [ ] **Then: 비활성화 확인**
  - 200 OK
  - status = INACTIVE

- [ ] **When: 스케줄 활성화**
  - POST /api/v1/admin/schedules/{id}/activate

- [ ] **Then: 활성화 확인**
  - 200 OK
  - status = ACTIVE

#### 시나리오 2: INACTIVE Seller 스케줄 등록 시도 → 400 Bad Request

- [ ] **Given: INACTIVE Seller**
  - POST /api/v1/sellers (INACTIVE 상태 생성)
  - 활성화 하지 않음

- [ ] **When: 스케줄 등록 시도**
  - POST /api/v1/admin/schedules
  - sellerId: "SELLER-002"
  - intervalDays: 1

- [ ] **Then: 등록 실패**
  - 400 Bad Request
  - errorCode: "SELLER_NOT_ACTIVE"
  - message: "Cannot register schedule for inactive seller"

#### 시나리오 3: Seller 비활성화 조건 검증 (스케줄 모두 비활성화 필요)

- [ ] **Given: ACTIVE Seller + ACTIVE 스케줄**
  - Seller: SELLER-003 (ACTIVE)
  - Schedule: 활성화됨

- [ ] **When: Seller 비활성화 시도 (스케줄 ACTIVE 상태)**
  - POST /api/v1/sellers/SELLER-003/deactivate

- [ ] **Then: 비활성화 실패**
  - 400 Bad Request
  - errorCode: "DEACTIVATION_NOT_ALLOWED"
  - message: "Active EventBridge schedules exist"

- [ ] **When: 스케줄 비활성화**
  - POST /api/v1/admin/schedules/{id}/deactivate

- [ ] **Then: 스케줄 비활성화 성공**
  - 200 OK
  - status = INACTIVE

- [ ] **When: Seller 비활성화 재시도**
  - POST /api/v1/sellers/SELLER-003/deactivate

- [ ] **Then: 비활성화 성공**
  - 200 OK
  - status = INACTIVE

#### 시나리오 4: 스케줄 목록 조회 (필터링 및 페이징)

- [ ] **Given: 여러 Seller의 스케줄**
  - SELLER-004: 스케줄 2개 (ACTIVE 1개, INACTIVE 1개)
  - SELLER-005: 스케줄 1개 (ACTIVE)

- [ ] **When: sellerId 필터링 조회**
  - GET /api/v1/admin/schedules?sellerId=SELLER-004

- [ ] **Then: SELLER-004 스케줄만 반환**
  - 200 OK
  - content.size() = 2

- [ ] **When: status 필터링 조회**
  - GET /api/v1/admin/schedules?status=ACTIVE

- [ ] **Then: ACTIVE 스케줄만 반환**
  - 200 OK
  - 모든 content의 status = ACTIVE

#### 시나리오 5: Localstack EventBridge 통합 (Outbox 패턴)

- [ ] **Given: 스케줄 등록 (Admin API)**
  - POST /api/v1/admin/schedules
  - SchedulerOutbox 생성 (SCHEDULE_CREATED)

- [ ] **When: Outbox 배치 처리**
  - ProcessSchedulerOutboxUseCase 실행
  - EventBridge API 호출 (Localstack)

- [ ] **Then: EventBridge Rule 생성 확인**
  - Localstack EventBridge 확인
  - Rule Name: `mustit-crawler-SELLER-001`
  - Schedule Expression: `rate(1 days)`
  - Outbox 상태: COMPLETED

- [ ] **When: 스케줄 주기 변경**
  - PATCH /api/v1/admin/schedules/{id}/interval
  - SchedulerOutbox 생성 (SCHEDULE_UPDATED)

- [ ] **When: Outbox 배치 처리**
  - EventBridge Rule 업데이트

- [ ] **Then: EventBridge Rule 업데이트 확인**
  - Schedule Expression: `rate(7 days)`
  - Outbox 상태: COMPLETED

- [ ] **When: 스케줄 비활성화**
  - POST /api/v1/admin/schedules/{id}/deactivate
  - SchedulerOutbox 생성 (SCHEDULE_DELETED)

- [ ] **When: Outbox 배치 처리**
  - EventBridge Rule 삭제

- [ ] **Then: EventBridge Rule 삭제 확인**
  - Localstack에서 Rule 삭제됨
  - Outbox 상태: COMPLETED

---

### 2. Validation 테스트

#### 잘못된 입력 검증

- [ ] **sellerId null/blank**
  - 400 Bad Request
  - "sellerId: must not be blank"

- [ ] **intervalDays null**
  - 400 Bad Request
  - "intervalDays: must not be null"

- [ ] **intervalDays 범위 초과 (>365)**
  - 400 Bad Request
  - "intervalDays: must be less than or equal to 365"

- [ ] **intervalDays 0 이하**
  - 400 Bad Request
  - "intervalDays: must be greater than or equal to 1"

---

## ✅ 완료 조건

- [ ] 5개 E2E 시나리오 테스트 통과
- [ ] Validation 테스트 통과
- [ ] Localstack EventBridge 통합 테스트 통과
- [ ] TestRestTemplate 사용 완료

---

## 🔗 관련 문서

- **Plan**: docs/prd/seller/eventbridge/plans/EVENTBRIDGE-005-integration-plan.md

---

## 📚 참고사항

### E2E 시나리오 1: 스케줄 전체 라이프사이클

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class EventBridgeE2EIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SellerCommandPort sellerCommandPort;

    @Autowired
    private CrawlingScheduleCommandPort scheduleCommandPort;

    @Test
    void e2e_스케줄_전체_라이프사이클() {
        // Given: ACTIVE Seller 등록
        RegisterSellerRequest sellerRequest = new RegisterSellerRequest(
            "SELLER-001",
            "테스트 셀러"
        );
        restTemplate.postForEntity("/api/v1/sellers", sellerRequest, SellerResponse.class);
        restTemplate.postForEntity("/api/v1/sellers/SELLER-001/activate", null, SellerResponse.class);

        // When: 스케줄 등록
        RegisterScheduleRequest scheduleRequest = new RegisterScheduleRequest(
            "SELLER-001",
            1
        );
        ResponseEntity<ScheduleResponse> registerResponse = restTemplate.postForEntity(
            "/api/v1/admin/schedules",
            scheduleRequest,
            ScheduleResponse.class
        );

        // Then: 201 Created, ACTIVE 상태
        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(registerResponse.getBody()).isNotNull();
        String scheduleId = registerResponse.getBody().scheduleId();
        assertThat(registerResponse.getBody().sellerId()).isEqualTo("SELLER-001");
        assertThat(registerResponse.getBody().intervalDays()).isEqualTo(1);
        assertThat(registerResponse.getBody().status()).isEqualTo("ACTIVE");
        assertThat(registerResponse.getBody().scheduleRule()).isEqualTo("mustit-crawler-SELLER-001");
        assertThat(registerResponse.getBody().scheduleExpression()).isEqualTo("rate(1 days)");

        // When: 주기 변경 (1일 → 7일)
        UpdateScheduleIntervalRequest updateRequest = new UpdateScheduleIntervalRequest(7);
        ResponseEntity<ScheduleResponse> updateResponse = restTemplate.exchange(
            "/api/v1/admin/schedules/" + scheduleId + "/interval",
            HttpMethod.PATCH,
            new HttpEntity<>(updateRequest),
            ScheduleResponse.class
        );

        // Then: 200 OK, 주기 변경됨
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResponse.getBody().intervalDays()).isEqualTo(7);
        assertThat(updateResponse.getBody().scheduleExpression()).isEqualTo("rate(7 days)");

        // When: 스케줄 비활성화
        ResponseEntity<ScheduleResponse> deactivateResponse = restTemplate.postForEntity(
            "/api/v1/admin/schedules/" + scheduleId + "/deactivate",
            null,
            ScheduleResponse.class
        );

        // Then: 200 OK, INACTIVE 상태
        assertThat(deactivateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(deactivateResponse.getBody().status()).isEqualTo("INACTIVE");

        // When: 스케줄 활성화
        ResponseEntity<ScheduleResponse> activateResponse = restTemplate.postForEntity(
            "/api/v1/admin/schedules/" + scheduleId + "/activate",
            null,
            ScheduleResponse.class
        );

        // Then: 200 OK, ACTIVE 상태
        assertThat(activateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(activateResponse.getBody().status()).isEqualTo("ACTIVE");
    }
}
```

### E2E 시나리오 2: INACTIVE Seller 스케줄 등록 실패

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class EventBridgeInactiveSellerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SellerCommandPort sellerCommandPort;

    @Test
    void e2e_INACTIVE_Seller_스케줄_등록_실패() {
        // Given: INACTIVE Seller (등록 시 기본 상태)
        RegisterSellerRequest sellerRequest = new RegisterSellerRequest(
            "SELLER-002",
            "테스트 셀러"
        );
        restTemplate.postForEntity("/api/v1/sellers", sellerRequest, SellerResponse.class);

        // When: 스케줄 등록 시도
        RegisterScheduleRequest scheduleRequest = new RegisterScheduleRequest(
            "SELLER-002",
            1
        );
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
            "/api/v1/admin/schedules",
            scheduleRequest,
            ErrorResponse.class
        );

        // Then: 400 Bad Request
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errorCode()).isEqualTo("SELLER_NOT_ACTIVE");
        assertThat(response.getBody().message()).contains("Cannot register schedule for inactive seller");
    }
}
```

### E2E 시나리오 3: Seller 비활성화 조건 검증

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class EventBridgeSellerDeactivationIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SellerCommandPort sellerCommandPort;

    @Autowired
    private CrawlingScheduleCommandPort scheduleCommandPort;

    @Test
    void e2e_Seller_비활성화_조건_검증() {
        // Given: ACTIVE Seller + ACTIVE 스케줄
        Seller seller = Seller.create(new SellerId("SELLER-003"), "테스트 셀러");
        seller.activate();
        sellerCommandPort.save(seller);

        CrawlingSchedule schedule = CrawlingSchedule.create(
            new SellerId("SELLER-003"),
            new CrawlingInterval(1)
        );
        scheduleCommandPort.save(schedule);
        String scheduleId = schedule.getScheduleId().value();

        // When: Seller 비활성화 시도 (스케줄 ACTIVE 상태)
        ResponseEntity<ErrorResponse> deactivateFailResponse = restTemplate.postForEntity(
            "/api/v1/sellers/SELLER-003/deactivate",
            null,
            ErrorResponse.class
        );

        // Then: 400 Bad Request
        assertThat(deactivateFailResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(deactivateFailResponse.getBody()).isNotNull();
        assertThat(deactivateFailResponse.getBody().errorCode()).isEqualTo("DEACTIVATION_NOT_ALLOWED");
        assertThat(deactivateFailResponse.getBody().message()).contains("Active EventBridge schedules exist");

        // When: 스케줄 비활성화
        ResponseEntity<ScheduleResponse> scheduleDeactivateResponse = restTemplate.postForEntity(
            "/api/v1/admin/schedules/" + scheduleId + "/deactivate",
            null,
            ScheduleResponse.class
        );

        // Then: 200 OK, INACTIVE 상태
        assertThat(scheduleDeactivateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(scheduleDeactivateResponse.getBody().status()).isEqualTo("INACTIVE");

        // When: Seller 비활성화 재시도
        ResponseEntity<SellerResponse> sellerDeactivateResponse = restTemplate.postForEntity(
            "/api/v1/sellers/SELLER-003/deactivate",
            null,
            SellerResponse.class
        );

        // Then: 200 OK, 비활성화 성공
        assertThat(sellerDeactivateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(sellerDeactivateResponse.getBody().status()).isEqualTo("INACTIVE");
    }
}
```

### E2E 시나리오 4: 스케줄 목록 조회 (필터링)

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class EventBridgeScheduleListIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SellerCommandPort sellerCommandPort;

    @Autowired
    private CrawlingScheduleCommandPort scheduleCommandPort;

    @Test
    void e2e_스케줄_목록_조회_필터링() {
        // Given: 여러 Seller의 스케줄
        Seller seller1 = SellerFixture.createActive("SELLER-004", "셀러4");
        Seller seller2 = SellerFixture.createActive("SELLER-005", "셀러5");
        sellerCommandPort.save(seller1);
        sellerCommandPort.save(seller2);

        CrawlingSchedule schedule1 = CrawlingSchedule.create(new SellerId("SELLER-004"), new CrawlingInterval(1));
        CrawlingSchedule schedule2 = CrawlingSchedule.create(new SellerId("SELLER-004"), new CrawlingInterval(7));
        schedule2.deactivate(); // INACTIVE
        CrawlingSchedule schedule3 = CrawlingSchedule.create(new SellerId("SELLER-005"), new CrawlingInterval(3));
        scheduleCommandPort.save(schedule1);
        scheduleCommandPort.save(schedule2);
        scheduleCommandPort.save(schedule3);

        // When: sellerId 필터링 조회
        ResponseEntity<RestResponsePage<ScheduleResponse>> sellerFilterResponse = restTemplate.exchange(
            "/api/v1/admin/schedules?sellerId=SELLER-004&page=0&size=10",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<RestResponsePage<ScheduleResponse>>() {}
        );

        // Then: SELLER-004 스케줄만 반환 (2개)
        assertThat(sellerFilterResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(sellerFilterResponse.getBody()).isNotNull();
        assertThat(sellerFilterResponse.getBody().getContent()).hasSize(2);
        assertThat(sellerFilterResponse.getBody().getContent())
            .allMatch(s -> s.sellerId().equals("SELLER-004"));

        // When: status 필터링 조회
        ResponseEntity<RestResponsePage<ScheduleResponse>> statusFilterResponse = restTemplate.exchange(
            "/api/v1/admin/schedules?status=ACTIVE&page=0&size=10",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<RestResponsePage<ScheduleResponse>>() {}
        );

        // Then: ACTIVE 스케줄만 반환 (2개: schedule1, schedule3)
        assertThat(statusFilterResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(statusFilterResponse.getBody()).isNotNull();
        assertThat(statusFilterResponse.getBody().getContent())
            .allMatch(s -> s.status().equals("ACTIVE"));
    }
}
```

### E2E 시나리오 5: Localstack EventBridge 통합 (Outbox 패턴)

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class EventBridgeLocalstackIntegrationTest {

    @Container
    static LocalStackContainer localstack = new LocalStackContainer(
        DockerImageName.parse("localstack/localstack:latest"))
        .withServices(LocalStackContainer.Service.EVENTBRIDGE);

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("cloud.aws.eventbridge.endpoint",
            () -> localstack.getEndpointOverride(LocalStackContainer.Service.EVENTBRIDGE));
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProcessSchedulerOutboxUseCase processSchedulerOutboxUseCase;

    @Autowired
    private SchedulerOutboxQueryPort outboxQueryPort;

    @Autowired
    private EventBridgeSchedulerClient eventBridgeClient; // AWS SDK EventBridge Client

    @Test
    void e2e_Localstack_EventBridge_Rule_생성_업데이트_삭제() {
        // Given: ACTIVE Seller + 스케줄 등록
        Seller seller = SellerFixture.createActive("SELLER-001", "테스트 셀러");
        sellerCommandPort.save(seller);

        RegisterScheduleRequest request = new RegisterScheduleRequest("SELLER-001", 1);
        ResponseEntity<ScheduleResponse> registerResponse = restTemplate.postForEntity(
            "/api/v1/admin/schedules",
            request,
            ScheduleResponse.class
        );
        String scheduleId = registerResponse.getBody().scheduleId();

        // When: Outbox 배치 처리 (SCHEDULE_CREATED)
        processSchedulerOutboxUseCase.execute();

        // Then: EventBridge Rule 생성 확인 (Localstack)
        String ruleName = "mustit-crawler-SELLER-001";
        DescribeRuleResponse describeResponse = eventBridgeClient.describeRule(
            DescribeRuleRequest.builder().name(ruleName).build()
        );
        assertThat(describeResponse.name()).isEqualTo(ruleName);
        assertThat(describeResponse.scheduleExpression()).isEqualTo("rate(1 days)");

        // Then: Outbox 상태 COMPLETED
        List<SchedulerOutbox> outboxes = outboxQueryPort.findPendingOutboxes();
        assertThat(outboxes).isEmpty(); // 모두 처리됨

        // When: 스케줄 주기 변경 (1일 → 7일)
        UpdateScheduleIntervalRequest updateRequest = new UpdateScheduleIntervalRequest(7);
        restTemplate.exchange(
            "/api/v1/admin/schedules/" + scheduleId + "/interval",
            HttpMethod.PATCH,
            new HttpEntity<>(updateRequest),
            ScheduleResponse.class
        );

        // When: Outbox 배치 처리 (SCHEDULE_UPDATED)
        processSchedulerOutboxUseCase.execute();

        // Then: EventBridge Rule 업데이트 확인
        describeResponse = eventBridgeClient.describeRule(
            DescribeRuleRequest.builder().name(ruleName).build()
        );
        assertThat(describeResponse.scheduleExpression()).isEqualTo("rate(7 days)");

        // When: 스케줄 비활성화
        restTemplate.postForEntity(
            "/api/v1/admin/schedules/" + scheduleId + "/deactivate",
            null,
            ScheduleResponse.class
        );

        // When: Outbox 배치 처리 (SCHEDULE_DELETED)
        processSchedulerOutboxUseCase.execute();

        // Then: EventBridge Rule 삭제 확인
        assertThatThrownBy(() ->
            eventBridgeClient.describeRule(DescribeRuleRequest.builder().name(ruleName).build())
        ).isInstanceOf(ResourceNotFoundException.class);
    }
}
```

### Validation 테스트

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class EventBridgeValidationIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void validation_sellerId_null() {
        // When: sellerId null
        RegisterScheduleRequest request = new RegisterScheduleRequest(null, 1);
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
        RegisterScheduleRequest request = new RegisterScheduleRequest("SELLER-001", 366);
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
        RegisterScheduleRequest request = new RegisterScheduleRequest("SELLER-001", 0);
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

### TestContainers 설정

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Testcontainers
class EventBridgeIntegrationTestBase {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");

    @Container
    static LocalStackContainer localstack = new LocalStackContainer(
        DockerImageName.parse("localstack/localstack:latest"))
        .withServices(LocalStackContainer.Service.EVENTBRIDGE);

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("cloud.aws.eventbridge.endpoint",
            () -> localstack.getEndpointOverride(LocalStackContainer.Service.EVENTBRIDGE));
    }

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected SellerCommandPort sellerCommandPort;

    @Autowired
    protected CrawlingScheduleCommandPort scheduleCommandPort;

    @BeforeEach
    void setUp() {
        // 각 테스트 전 데이터 초기화
        sellerCommandPort.deleteAll();
        scheduleCommandPort.deleteAll();
    }
}
```

### 중요 변경사항

⚠️ **Admin API 경로**:
- `/api/v1/admin/schedules` 사용
- Admin 권한 검증 필요

⚠️ **ACTIVE Seller 검증**:
- INACTIVE Seller는 스케줄 등록 불가
- 400 Bad Request 응답

⚠️ **Seller 비활성화 조건**:
- 모든 스케줄이 INACTIVE 상태여야 Seller 비활성화 가능
- 위반 시 400 Bad Request

⚠️ **Outbox 패턴 통합**:
- Admin API → Domain Event → Outbox 생성
- 별도 배치에서 Outbox 처리 → EventBridge API 호출
