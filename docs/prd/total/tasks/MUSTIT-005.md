# MUSTIT-005: Integration Test 구현

**Epic**: 머스트잇 셀러 크롤러
**Layer**: Integration Test
**브랜치**: feature/MUSTIT-005-integration
**Jira URL**: (sync-to-jira 후 추가)

---

## 📝 목적

전체 시스템의 End-to-End 통합 테스트 구현. 모든 레이어가 함께 동작하는 실제 시나리오를 검증합니다.

**핵심 역할**:
- E2E 시나리오 테스트
- 실제 외부 시스템 연동 검증 (Localstack)
- 동시성 테스트
- 성능 테스트

---

## 🎯 요구사항

### 1. E2E 시나리오 테스트

#### 시나리오 1: 셀러 등록 → 크롤링 트리거 → 태스크 처리 → 상품 저장

- [ ] **Given: 셀러 등록**
  - POST /api/v1/sellers
  - sellerId: "seller_test_001"
  - name: "테스트 셀러"
  - crawlingIntervalDays: 1

- [ ] **When: EventBridge 크롤링 트리거**
  - POST /api/internal/crawling/trigger
  - sellerId: "seller_test_001"

- [ ] **Then: 미니샵 태스크 생성 확인**
  - CrawlerTask 조회
  - 상태: WAITING
  - taskType: MINISHOP
  - 페이지 수 검증 (totalProductCount / 500)

- [ ] **When: 태스크 발행**
  - PublishCrawlerTasksUseCase 실행
  - SQS 메시지 발행 (Localstack)

- [ ] **Then: 태스크 상태 변경 확인**
  - 상태: PUBLISHED
  - publishedAt 기록됨

- [ ] **When: 워커가 태스크 폴링 및 처리**
  - SQS 메시지 폴링 (Localstack)
  - ProcessCrawlerTaskUseCase 실행
  - 머스트잇 API 호출 (Mock)

- [ ] **Then: 크롤링 결과 저장 확인**
  - 상태: COMPLETED
  - Product 생성됨
  - ProductRawData 저장됨 (MINISHOP)

#### 시나리오 2: 상품 변경 감지 → Outbox 생성 → 외부 전송

- [ ] **Given: 기존 상품 존재**
  - itemNo: 12345
  - minishopDataHash: "old_hash"

- [ ] **When: 동일 상품 재크롤링 (데이터 변경됨)**
  - ProcessCrawlerTaskUseCase 실행
  - 새 해시값 계산: "new_hash"

- [ ] **Then: 변경 감지 및 Outbox 생성**
  - Product.hasChanged() → true
  - ProductOutbox 생성됨
  - eventType: PRODUCT_UPDATED
  - status: WAITING

- [ ] **When: Outbox 배치 처리**
  - ProcessProductOutboxUseCase 실행 (Scheduled)
  - 외부 상품 서버 API 호출 (Mock)

- [ ] **Then: 외부 전송 완료 확인**
  - 상태: COMPLETED
  - sentAt 기록됨

#### 시나리오 3: UserAgent 429 응답 처리 및 자동 복구

- [ ] **Given: UserAgent 할당**
  - UserAgentPoolManager.assignUserAgent()
  - 상태: ACTIVE
  - requestCount: 79

- [ ] **When: 429 응답 받음**
  - 머스트잇 API 호출 → 429
  - UserAgent.suspend() 호출

- [ ] **Then: UserAgent 일시 중지 확인**
  - 상태: SUSPENDED
  - token: null

- [ ] **When: 다른 UserAgent 재할당**
  - UserAgentPoolManager.assignUserAgent() 재시도
  - 다른 ACTIVE UserAgent 할당

- [ ] **Then: 크롤링 재시도 성공**
  - 태스크 상태: IN_PROGRESS → COMPLETED

- [ ] **When: 1시간 경과 후 자동 복구 (Scheduled)**
  - UserAgentPoolManager.recoverSuspendedUserAgents() 실행

- [ ] **Then: UserAgent 복구 확인**
  - 상태: SUSPENDED → ACTIVE

---

### 2. Infrastructure 통합 테스트 (Localstack)

#### EventBridge 연동 테스트

- [ ] **Rule 생성 테스트**
  - AwsEventBridgeAdapter.createRule() 실행
  - Localstack EventBridge 확인
  - Rule Name: `mustit-crawler-seller_test_001`
  - Schedule Expression: `rate(1 days)`

- [ ] **Rule 업데이트 테스트**
  - AwsEventBridgeAdapter.updateRule() 실행
  - Schedule Expression 변경: `rate(2 days)`

- [ ] **Rule 삭제 테스트**
  - AwsEventBridgeAdapter.deleteRule() 실행
  - Localstack EventBridge에서 삭제 확인

#### SQS 연동 테스트

- [ ] **메시지 발행 테스트**
  - SqsPublisherAdapter.sendBatch() 실행
  - Localstack SQS 큐 확인
  - Message Body 검증

- [ ] **메시지 폴링 테스트**
  - SqsConsumerAdapter.poll() 실행
  - 메시지 수신 확인
  - Visibility Timeout 검증 (30초)

- [ ] **Dead Letter Queue 테스트**
  - 태스크 재시도 2회 초과 → DLQ 이동
  - DLQ 메시지 확인

---

### 3. 동시성 테스트

#### UserAgent 할당 동시성 테스트

- [ ] **여러 스레드에서 동시 할당**
  - 10개 스레드에서 동시에 assignUserAgent() 호출
  - Pessimistic Lock 검증
  - Race Condition 방지 확인
  - 할당된 UserAgent가 중복되지 않음

#### 크롤링 태스크 동시 처리 테스트

- [ ] **여러 워커에서 동시 처리**
  - 5개 스레드에서 동시에 ProcessCrawlerTaskUseCase 실행
  - 동일 태스크 중복 처리 방지 확인
  - SQS Visibility Timeout 검증

---

### 4. 성능 테스트

#### Bulk Insert 성능 테스트

- [ ] **1,000개 태스크 Bulk Insert**
  - TriggerCrawlingUseCase 실행
  - totalProductCount: 500,000 (1,000 페이지)
  - Bulk Insert 시간 측정 (목표: < 5초)

#### 메트릭 집계 쿼리 성능 테스트

- [ ] **10,000개 태스크 집계 쿼리**
  - GetCrawlingMetricsUseCase 실행
  - QueryDSL 집계 쿼리 성능 측정 (목표: < 1초)
  - 인덱스 활용 검증

---

### 5. 테스트 환경 구성

#### TestContainers 설정

- [ ] **MySQL Container**
  - mysql:8.0
  - Flyway 마이그레이션 자동 실행
  - 초기 데이터 삽입 (50개 UserAgent)

- [ ] **Localstack Container**
  - localstack/localstack:latest
  - 서비스: EventBridge, SQS
  - Auto-create 큐: mustit-crawler-tasks.fifo

#### 테스트 데이터 준비

- [ ] **@Sql 스크립트**
  - 셀러 테스트 데이터
  - UserAgent 테스트 데이터
  - 상품 테스트 데이터

- [ ] **TestFixture 활용**
  - Domain 객체 생성 Fixture
  - DTO 생성 Fixture

---

### 6. Mock 외부 시스템

#### 머스트잇 API Mock

- [ ] **WireMock 사용**
  - 미니샵 API 응답 Mock
  - 상품 상세 API 응답 Mock
  - 상품 옵션 API 응답 Mock
  - 429 응답 시뮬레이션

#### 외부 상품 서버 API Mock

- [ ] **WireMock 사용**
  - POST /products/created 응답 Mock
  - POST /products/updated 응답 Mock
  - 성공/실패 시나리오 Mock

---

## ⚠️ 제약사항

### Zero-Tolerance 규칙

- [ ] **TestRestTemplate 사용 필수**
  - MockMvc 금지
  - 실제 HTTP 요청/응답 테스트

- [ ] **TestContainers 사용**
  - 실제 MySQL 환경에서 테스트
  - Localstack으로 AWS 서비스 테스트

- [ ] **Flyway 마이그레이션**
  - 테스트 시작 시 자동 실행
  - 스키마 생성 및 초기 데이터 삽입

### 테스트 규칙

- [ ] **E2E 시나리오 모두 통과**
  - 3개 주요 시나리오 완벽 동작
  - 성공/실패 경로 모두 검증

- [ ] **동시성 테스트 통과**
  - Race Condition 없음
  - Pessimistic Lock 정상 작동

- [ ] **성능 목표 달성**
  - Bulk Insert < 5초
  - 메트릭 집계 쿼리 < 1초

---

## ✅ 완료 조건

- [ ] 3개 E2E 시나리오 테스트 작성 완료
- [ ] EventBridge 통합 테스트 통과 (Localstack)
- [ ] SQS 통합 테스트 통과 (Localstack)
- [ ] UserAgent 할당 동시성 테스트 통과
- [ ] 크롤링 태스크 동시 처리 테스트 통과
- [ ] Bulk Insert 성능 테스트 통과 (< 5초)
- [ ] 메트릭 집계 쿼리 성능 테스트 통과 (< 1초)
- [ ] WireMock으로 외부 API Mock 완료
- [ ] TestContainers 환경 구성 완료
- [ ] @Sql 테스트 데이터 준비 완료
- [ ] 모든 테스트 통과 (성공률 100%)
- [ ] Zero-Tolerance 규칙 준수
- [ ] 코드 리뷰 승인
- [ ] PR 머지 완료

---

## 🔗 관련 문서

- **PRD**: docs/prd/mustit-seller-crawler.md
- **Plan**: docs/prd/plans/MUSTIT-005-integration-plan.md (create-plan 후 생성)
- **Jira**: (sync-to-jira 후 추가)
- **Integration Testing 규칙**: docs/coding_convention/05-testing/integration-testing/

---

## 📚 참고사항

### TestContainers 설정 예시

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class E2EIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");

    @Container
    static LocalStackContainer localstack = new LocalStackContainer(
        DockerImageName.parse("localstack/localstack:latest"))
        .withServices(
            LocalStackContainer.Service.EVENTBRIDGE,
            LocalStackContainer.Service.SQS
        );

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        // MySQL
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);

        // Localstack
        registry.add("cloud.aws.eventbridge.endpoint",
            () -> localstack.getEndpointOverride(LocalStackContainer.Service.EVENTBRIDGE));
        registry.add("cloud.aws.sqs.endpoint",
            () -> localstack.getEndpointOverride(LocalStackContainer.Service.SQS));
    }
}
```

### E2E 시나리오 테스트 예시

```java
@Test
void e2e_seller_registration_to_product_crawling() {
    // Given: 셀러 등록
    RegisterSellerRequest registerRequest = new RegisterSellerRequest(
        "seller_test_001",
        "테스트 셀러",
        1
    );

    ResponseEntity<SellerResponse> registerResponse = restTemplate.postForEntity(
        "/api/v1/sellers",
        registerRequest,
        SellerResponse.class
    );

    assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    // When: 크롤링 트리거
    TriggerCrawlingRequest triggerRequest = new TriggerCrawlingRequest("seller_test_001");

    ResponseEntity<CrawlingTriggeredResponse> triggerResponse = restTemplate.postForEntity(
        "/api/internal/crawling/trigger",
        triggerRequest,
        CrawlingTriggeredResponse.class
    );

    assertThat(triggerResponse.getBody().taskCount()).isGreaterThan(0);

    // Then: 태스크 생성 확인
    List<CrawlerTask> tasks = crawlerTaskRepository.findBySellerId("seller_test_001");
    assertThat(tasks).isNotEmpty();
    assertThat(tasks.get(0).getStatus()).isEqualTo(CrawlerTaskStatus.WAITING);

    // When: 태스크 발행
    List<String> taskIds = tasks.stream()
        .map(CrawlerTask::getTaskId)
        .map(TaskId::getValue)
        .toList();

    publishCrawlerTasksUseCase.execute(new PublishCrawlerTasksCommand(taskIds));

    // Then: SQS 메시지 확인 (Localstack)
    ReceiveMessageResponse sqsResponse = sqsClient.receiveMessage(
        ReceiveMessageRequest.builder()
            .queueUrl(queueUrl)
            .maxNumberOfMessages(10)
            .build()
    );

    assertThat(sqsResponse.messages()).hasSize(taskIds.size());
}
```

### WireMock 설정 예시

```java
@WireMockTest(httpPort = 8089)
class MustitApiMockTest {

    @Test
    void crawl_minishop_success() {
        // Given: 머스트잇 미니샵 API Mock
        stubFor(get(urlPathMatching("/mustit-api/facade-api/v1/searchmini-shop-search"))
            .withQueryParam("sellerId", equalTo("seller_test_001"))
            .withQueryParam("pageNo", equalTo("0"))
            .withQueryParam("pageSize", equalTo("500"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {
                        "totalCount": 1500,
                        "items": [...]
                    }
                    """)));

        // When: 크롤링 실행
        CrawlingResult result = mustitApiCrawler.crawlMinishop("seller_test_001", 0, 500);

        // Then
        assertThat(result.getTotalCount()).isEqualTo(1500);
    }
}
```

### 동시성 테스트 예시

```java
@Test
void concurrent_userAgent_assignment_no_race_condition() throws InterruptedException {
    // Given: 10개 스레드 준비
    int threadCount = 10;
    ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(threadCount);

    Set<String> assignedUserAgentIds = ConcurrentHashMap.newKeySet();

    // When: 동시에 UserAgent 할당
    for (int i = 0; i < threadCount; i++) {
        executorService.submit(() -> {
            try {
                UserAgent userAgent = userAgentPoolManager.assignUserAgent();
                assignedUserAgentIds.add(userAgent.getUserAgentId().getValue());
            } finally {
                latch.countDown();
            }
        });
    }

    latch.await(10, TimeUnit.SECONDS);
    executorService.shutdown();

    // Then: 중복 할당 없음
    assertThat(assignedUserAgentIds).hasSize(threadCount);
}
```

### 성능 테스트 예시

```java
@Test
void bulk_insert_1000_tasks_performance() {
    // Given
    String sellerId = "seller_test_001";
    int totalProductCount = 500000;  // 1,000 페이지

    // When
    long startTime = System.currentTimeMillis();

    triggerCrawlingUseCase.execute(new TriggerCrawlingCommand(sellerId));

    long endTime = System.currentTimeMillis();
    long duration = endTime - startTime;

    // Then: < 5초
    assertThat(duration).isLessThan(5000);

    List<CrawlerTask> tasks = crawlerTaskRepository.findBySellerId(sellerId);
    assertThat(tasks).hasSize(1000);
}
```
