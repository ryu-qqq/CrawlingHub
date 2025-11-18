# SELLER-005: Seller Integration Test 구현

**Bounded Context**: Seller
**Sub-Context**: Seller (셀러 자체)
**Layer**: Integration Test
**브랜치**: feature/SELLER-005-integration

---

## 📝 목적

Seller E2E 시나리오 테스트.

---

## 🎯 요구사항

### 1. E2E 시나리오

#### 시나리오 1: 셀러 등록 → 활성화 → 이름 변경 → 비활성화

- [ ] **Given: 셀러 등록**
  - POST /api/v1/sellers
  - sellerId: "SELLER-001"
  - name: "테스트 셀러"
  - **초기 상태**: INACTIVE

- [ ] **Then: 등록 성공 확인**
  - 201 Created
  - SellerResponse 검증
  - status = INACTIVE

- [ ] **When: 셀러 활성화**
  - POST /api/v1/sellers/SELLER-001/activate

- [ ] **Then: 활성화 확인**
  - 200 OK
  - status = ACTIVE

- [ ] **When: 이름 변경**
  - PATCH /api/v1/sellers/SELLER-001/name
  - newName: "변경된 셀러"

- [ ] **Then: 이름 변경 확인**
  - 200 OK
  - name = "변경된 셀러"

- [ ] **When: 셀러 비활성화**
  - POST /api/v1/sellers/SELLER-001/deactivate

- [ ] **Then: 비활성화 확인**
  - 200 OK
  - status = INACTIVE

#### 시나리오 2: 중복 sellerId 등록 시도 → 409 Conflict

- [ ] **Given: 기존 셀러 존재**
  - sellerId: "SELLER-EXISTING"

- [ ] **When: 동일 ID로 등록 시도**
  - POST /api/v1/sellers
  - sellerId: "SELLER-EXISTING"

- [ ] **Then: 중복 에러 확인**
  - 409 Conflict
  - errorCode: "DUPLICATE_SELLER_ID"

#### 시나리오 3: 페이징 조회

- [ ] **Given: 10개 셀러 존재**
  - SELLER-001 ~ SELLER-010

- [ ] **When: 첫 페이지 조회 (size=5)**
  - GET /api/v1/sellers?page=0&size=5

- [ ] **Then: 페이징 응답 확인**
  - 200 OK
  - content.size() = 5
  - totalElements = 10
  - totalPages = 2

#### 시나리오 4: EventBridge 활성화 중 비활성화 시도 → 400 Bad Request

- [ ] **Given: ACTIVE Seller + 활성화된 EventBridge**
  - Seller: SELLER-002 (ACTIVE)
  - EventBridge: 활성화됨 (별도 Admin API로 등록)

- [ ] **When: 셀러 비활성화 시도**
  - POST /api/v1/sellers/SELLER-002/deactivate

- [ ] **Then: 비활성화 실패**
  - 400 Bad Request
  - errorCode: "DEACTIVATION_NOT_ALLOWED"
  - message: "Active EventBridge schedules exist"

---

### 2. Validation 테스트

#### 잘못된 입력 검증

- [ ] **sellerId null/blank**
  - 400 Bad Request
  - "sellerId: must not be blank"

- [ ] **name null/blank**
  - 400 Bad Request
  - "name: must not be blank"

- [ ] **name 길이 초과 (>100)**
  - 400 Bad Request
  - "name: size must be between 1 and 100"

---

## ✅ 완료 조건

- [ ] 4개 E2E 시나리오 테스트 통과
- [ ] Validation 테스트 통과
- [ ] TestRestTemplate 사용 완료

---

## 🔗 관련 문서

- **Plan**: docs/prd/seller/seller/plans/SELLER-005-integration-plan.md

---

## 📚 참고사항

### E2E 시나리오 1: 셀러 등록 → 활성화 → 이름 변경 → 비활성화

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class SellerE2EIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SellerCommandPort sellerCommandPort;

    @Test
    void e2e_셀러_등록_활성화_이름변경_비활성화() {
        // Given: 셀러 등록
        RegisterSellerRequest registerRequest = new RegisterSellerRequest(
            "SELLER-001",
            "테스트 셀러"
        );

        ResponseEntity<SellerResponse> registerResponse = restTemplate.postForEntity(
            "/api/v1/sellers",
            registerRequest,
            SellerResponse.class
        );

        // Then: 201 Created, INACTIVE 상태
        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(registerResponse.getBody()).isNotNull();
        assertThat(registerResponse.getBody().sellerId()).isEqualTo("SELLER-001");
        assertThat(registerResponse.getBody().name()).isEqualTo("테스트 셀러");
        assertThat(registerResponse.getBody().status()).isEqualTo("INACTIVE");

        // When: 셀러 활성화
        ResponseEntity<SellerResponse> activateResponse = restTemplate.postForEntity(
            "/api/v1/sellers/SELLER-001/activate",
            null,
            SellerResponse.class
        );

        // Then: 200 OK, ACTIVE 상태
        assertThat(activateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(activateResponse.getBody().status()).isEqualTo("ACTIVE");

        // When: 이름 변경
        UpdateSellerNameRequest updateRequest = new UpdateSellerNameRequest("변경된 셀러");

        ResponseEntity<SellerResponse> updateResponse = restTemplate.exchange(
            "/api/v1/sellers/SELLER-001/name",
            HttpMethod.PATCH,
            new HttpEntity<>(updateRequest),
            SellerResponse.class
        );

        // Then: 200 OK, 이름 변경됨
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResponse.getBody().name()).isEqualTo("변경된 셀러");

        // When: 셀러 비활성화
        ResponseEntity<SellerResponse> deactivateResponse = restTemplate.postForEntity(
            "/api/v1/sellers/SELLER-001/deactivate",
            null,
            SellerResponse.class
        );

        // Then: 200 OK, INACTIVE 상태
        assertThat(deactivateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(deactivateResponse.getBody().status()).isEqualTo("INACTIVE");
    }
}
```

### E2E 시나리오 2: 중복 sellerId 등록 시도

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class SellerDuplicateIdIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SellerCommandPort sellerCommandPort;

    @Test
    void e2e_중복_sellerId_등록_시도_실패() {
        // Given: 기존 셀러 존재
        Seller existingSeller = Seller.create(
            new SellerId("SELLER-EXISTING"),
            "기존 셀러"
        );
        sellerCommandPort.save(existingSeller);

        // When: 동일 sellerId로 등록 시도
        RegisterSellerRequest request = new RegisterSellerRequest(
            "SELLER-EXISTING",
            "새로운 셀러"
        );

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
            "/api/v1/sellers",
            request,
            ErrorResponse.class
        );

        // Then: 409 Conflict
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errorCode()).isEqualTo("DUPLICATE_SELLER_ID");
        assertThat(response.getBody().message()).contains("already exists");
    }
}
```

### E2E 시나리오 3: 페이징 조회

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class SellerPagingIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SellerCommandPort sellerCommandPort;

    @Test
    void e2e_셀러_목록_페이징_조회() {
        // Given: 10개 셀러 생성
        for (int i = 1; i <= 10; i++) {
            Seller seller = Seller.create(
                new SellerId("SELLER-" + String.format("%03d", i)),
                "셀러 " + i
            );
            sellerCommandPort.save(seller);
        }

        // When: 첫 페이지 조회 (size=5)
        ResponseEntity<RestResponsePage<SellerResponse>> response = restTemplate.exchange(
            "/api/v1/sellers?page=0&size=5",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<RestResponsePage<SellerResponse>>() {}
        );

        // Then: 200 OK, 페이징 정보 확인
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(5);
        assertThat(response.getBody().getTotalElements()).isEqualTo(10);
        assertThat(response.getBody().getTotalPages()).isEqualTo(2);
        assertThat(response.getBody().getNumber()).isEqualTo(0); // 현재 페이지
    }
}
```

### E2E 시나리오 4: EventBridge 활성화 중 비활성화 시도 실패

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class SellerDeactivationWithEventBridgeIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SellerCommandPort sellerCommandPort;

    @Autowired
    private CrawlingScheduleCommandPort crawlingScheduleCommandPort;

    @Test
    void e2e_EventBridge_활성화_중_비활성화_시도_실패() {
        // Given: ACTIVE Seller 생성
        Seller seller = Seller.create(new SellerId("SELLER-002"), "테스트 셀러");
        seller.activate(); // ACTIVE 상태로 변경
        sellerCommandPort.save(seller);

        // Given: EventBridge 등록 (별도 Admin API - 여기서는 직접 생성)
        // 실제로는 POST /api/v1/admin/schedules로 등록
        CrawlingSchedule schedule = CrawlingSchedule.create(
            new SellerId("SELLER-002"),
            new CrawlingInterval(1), // 1일 주기
            "Test Schedule"
        );
        schedule.activate(); // 활성화
        crawlingScheduleCommandPort.save(schedule);

        // When: 셀러 비활성화 시도
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
            "/api/v1/sellers/SELLER-002/deactivate",
            null,
            ErrorResponse.class
        );

        // Then: 400 Bad Request
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errorCode()).isEqualTo("DEACTIVATION_NOT_ALLOWED");
        assertThat(response.getBody().message()).contains("Active EventBridge schedules exist");
    }

    @Test
    void e2e_EventBridge_비활성화_후_셀러_비활성화_성공() {
        // Given: ACTIVE Seller + EventBridge
        Seller seller = Seller.create(new SellerId("SELLER-003"), "테스트 셀러");
        seller.activate();
        sellerCommandPort.save(seller);

        CrawlingSchedule schedule = CrawlingSchedule.create(
            new SellerId("SELLER-003"),
            new CrawlingInterval(1),
            "Test Schedule"
        );
        schedule.activate();
        crawlingScheduleCommandPort.save(schedule);

        // Given: EventBridge 비활성화 (Admin API로 처리)
        schedule.deactivate();
        crawlingScheduleCommandPort.save(schedule);

        // When: 셀러 비활성화 시도
        ResponseEntity<SellerResponse> response = restTemplate.postForEntity(
            "/api/v1/sellers/SELLER-003/deactivate",
            null,
            SellerResponse.class
        );

        // Then: 200 OK, 비활성화 성공
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo("INACTIVE");
    }
}
```

### Validation 테스트

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class SellerValidationIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void validation_sellerId_null() {
        // When: sellerId null
        RegisterSellerRequest request = new RegisterSellerRequest(
            null,
            "테스트 셀러"
        );

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
            "/api/v1/sellers",
            request,
            ErrorResponse.class
        );

        // Then: 400 Bad Request
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).contains("sellerId");
        assertThat(response.getBody().message()).contains("must not be blank");
    }

    @Test
    void validation_name_blank() {
        // When: name blank
        RegisterSellerRequest request = new RegisterSellerRequest(
            "SELLER-001",
            ""
        );

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
            "/api/v1/sellers",
            request,
            ErrorResponse.class
        );

        // Then: 400 Bad Request
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).contains("name");
        assertThat(response.getBody().message()).contains("must not be blank");
    }

    @Test
    void validation_name_길이_초과() {
        // When: name 길이 > 100
        String longName = "a".repeat(101);
        RegisterSellerRequest request = new RegisterSellerRequest(
            "SELLER-001",
            longName
        );

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
            "/api/v1/sellers",
            request,
            ErrorResponse.class
        );

        // Then: 400 Bad Request
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).contains("name");
        assertThat(response.getBody().message()).contains("size");
    }
}
```

### TestContainers 설정

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Testcontainers
class SellerIntegrationTestBase {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected SellerCommandPort sellerCommandPort;

    @BeforeEach
    void setUp() {
        // 각 테스트 전 데이터 초기화
        sellerCommandPort.deleteAll();
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

⚠️ **초기 상태 INACTIVE**:
- 셀러 등록 시 INACTIVE 상태로 생성
- 명시적 활성화 필요 (POST /activate)
