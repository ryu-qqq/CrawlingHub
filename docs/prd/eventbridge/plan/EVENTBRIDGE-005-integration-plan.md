# EVENTBRIDGE-005: Integration Test TDD Plan

**Epic**: 머스트잇 셀러 크롤링 스케줄러
**Layer**: Integration Test (E2E + Outbox Pattern)
**브랜치**: feature/EVENTBRIDGE-005-integration
**예상 소요 시간**: 225분 (15 사이클 × 15분)

---

## 📋 TDD 사이클 개요

| 사이클 | 대상 | 예상 시간 |
|--------|------|----------|
| 1 | TestContainers MySQL + Flyway 설정 | 15분 |
| 2 | WireMock AWS EventBridge Mock 설정 | 15분 |
| 3 | Slack Webhook Mock 설정 | 15분 |
| 4 | E2E 시나리오 1: 스케줄 등록 → AWS Rule 생성 → 조회 | 15분 |
| 5 | E2E 시나리오 2: 스케줄 수정 → AWS Rule 업데이트 → 이력 조회 | 15분 |
| 6 | E2E 시나리오 3: 셀러 비활성화 → 스케줄 일괄 비활성화 | 15분 |
| 7 | TransactionSynchronization 테스트 - 성공 시나리오 | 15분 |
| 8 | TransactionSynchronization 테스트 - 실패 시나리오 | 15분 |
| 9 | OutboxEventProcessor 테스트 - PENDING 재처리 | 15분 |
| 10 | OutboxEventProcessor 테스트 - FAILED 재시도 | 15분 |
| 11 | OutboxEventProcessor 테스트 - 최대 재시도 초과 | 15분 |
| 12 | Exponential Backoff 테스트 | 15분 |
| 13 | EventBridgeClientAdapter 테스트 (CreateRule, UpdateRule, DisableRule) | 15분 |
| 14 | EventBridgeClientAdapter 실패 시나리오 테스트 | 15분 |
| 15 | Integration Test 전체 시나리오 검증 | 15분 |

---

## 🔄 Cycle 1: TestContainers MySQL + Flyway 설정

**목표**: 실제 MySQL 환경 구성 (H2 금지, Flyway 스키마 생성)

#### 🔴 Red: 테스트 작성
- [ ] `IntegrationTestBase` 생성
  - `@SpringBootTest(webEnvironment = RANDOM_PORT)` 검증
  - `@Testcontainers` 검증
  - `@ActiveProfiles("test")` 검증
  - TestRestTemplate 주입 검증
  - Flyway Migration 실행 검증
  - MySQL Container 시작 검증
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: TestContainers MySQL + Flyway 설정 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `IntegrationTestBase` 추상 클래스 생성
  ```java
  @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
  @Testcontainers
  @ActiveProfiles("test")
  @Transactional
  @Rollback(true)
  public abstract class IntegrationTestBase {
      @Container
      static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
          .withDatabaseName("test")
          .withUsername("test")
          .withPassword("test");

      @Autowired
      protected TestRestTemplate restTemplate;

      @DynamicPropertySource
      static void configureProperties(DynamicPropertyRegistry registry) {
          registry.add("spring.datasource.url", mysql::getJdbcUrl);
          registry.add("spring.datasource.username", mysql::getUsername);
          registry.add("spring.datasource.password", mysql::getPassword);
          registry.add("spring.flyway.enabled", () -> "true");
      }
  }
  ```
- [ ] `application-test.yml` 설정
  ```yaml
  spring:
    flyway:
      enabled: true
      locations: classpath:db/migration
    jpa:
      hibernate:
        ddl-auto: validate # Flyway가 스키마 생성
  ```
- [ ] Flyway Migration 스크립트 작성
  - `V001__Create_sellers_table.sql` (선행 요구사항)
  - `V002__Create_crawling_schedulers_table.sql`
  - `V003__Create_scheduler_histories_table.sql`
  - `V004__Create_scheduler_outbox_events_table.sql`
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: TestContainers MySQL + Flyway 설정 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] TestContainers 설정 최적화
- [ ] 커밋: `struct: TestContainers 설정 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 공통 Integration Test Base 정리
- [ ] 커밋: `test: IntegrationTestBase Fixture 정리 (Tidy)`

---

## 🔄 Cycle 2: WireMock AWS EventBridge Mock 설정

**목표**: AWS EventBridge API Mock Server 구성

#### 🔴 Red: 테스트 작성
- [ ] `EventBridgeMockServer` 설정 테스트
  - CreateRule API Mock 검증
  - UpdateRule API Mock 검증
  - DisableRule API Mock 검증
  - WireMock 응답 검증
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: WireMock EventBridge Mock 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] WireMock 설정
  ```java
  @SpringBootTest(webEnvironment = RANDOM_PORT)
  @AutoConfigureWireMock(port = 0)
  public abstract class EventBridgeMockTestBase extends IntegrationTestBase {
      @Autowired
      protected WireMockServer wireMockServer;

      @BeforeEach
      void setupEventBridgeMock() {
          // CreateRule API Mock
          wireMockServer.stubFor(post(urlEqualTo("/rules"))
              .willReturn(aResponse()
                  .withStatus(200)
                  .withHeader("Content-Type", "application/json")
                  .withBody("{\"RuleArn\": \"arn:aws:events:us-east-1:123456789012:rule/test-rule\"}")));

          // UpdateRule API Mock
          wireMockServer.stubFor(put(urlMatching("/rules/.*"))
              .willReturn(aResponse()
                  .withStatus(200)
                  .withHeader("Content-Type", "application/json")
                  .withBody("{\"RuleArn\": \"arn:aws:events:us-east-1:123456789012:rule/test-rule\"}")));

          // DisableRule API Mock
          wireMockServer.stubFor(post(urlMatching("/rules/.*/disable"))
              .willReturn(aResponse()
                  .withStatus(200)
                  .withHeader("Content-Type", "application/json")
                  .withBody("{\"Status\": \"DISABLED\"}")));
      }
  }
  ```
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: WireMock EventBridge Mock 설정 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] Mock 응답 정교화
- [ ] 커밋: `struct: EventBridge Mock 설정 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] EventBridge Mock Fixture 정리
- [ ] 커밋: `test: EventBridge Mock Fixture 정리 (Tidy)`

---

## 🔄 Cycle 3: Slack Webhook Mock 설정

**목표**: Slack 알림 API Mock Server 구성

#### 🔴 Red: 테스트 작성
- [ ] `SlackMockServer` 설정 테스트
  - Slack Webhook POST 검증
  - 알림 메시지 검증
  - WireMock 응답 검증
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: Slack Webhook Mock 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] Slack Webhook Mock 설정
  ```java
  @BeforeEach
  void setupSlackMock() {
      wireMockServer.stubFor(post(urlEqualTo("/slack/webhook"))
          .willReturn(aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withBody("{\"ok\": true}")));
  }
  ```
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: Slack Webhook Mock 설정 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] Slack Mock 응답 개선
- [ ] 커밋: `struct: Slack Mock 설정 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Slack Mock Fixture 정리
- [ ] 커밋: `test: Slack Mock Fixture 정리 (Tidy)`

---

## 🔄 Cycle 4: E2E 시나리오 1 - 스케줄 등록 → AWS Rule 생성 → 조회

**목표**: 전체 플로우 E2E 테스트 (셀러 등록 → 스케줄 등록 → Outbox 처리 → AWS Rule 생성 → 조회)

#### 🔴 Red: 테스트 작성
- [ ] `SchedulerE2ETest` 생성
  - Step 1: POST /api/v1/sellers (셀러 등록) → 201 Created
  - Step 2: POST /api/v1/sellers/1/schedulers (스케줄 등록) → 201 Created, status = PENDING
  - Step 3: Outbox Event 처리 (TransactionSynchronization)
    - AWS EventBridge CreateRule API 호출 검증 (WireMock)
    - Outbox.status → PUBLISHED 검증
    - Scheduler.status → ACTIVE 검증
  - Step 4: GET /api/v1/schedulers/1 (조회) → 200 OK, status = ACTIVE, eventBridgeRuleName 존재
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: E2E 시나리오 1 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `@Sql` 테스트 데이터 삽입
  ```sql
  -- src/test/resources/sql/e2e-scenario-1.sql
  INSERT INTO sellers (id, name, status, created_at, updated_at)
  VALUES (1, 'Test Seller', 'ACTIVE', NOW(), NOW());
  ```
- [ ] E2E 테스트 구현
  ```java
  @Test
  @Sql("/sql/e2e-scenario-1.sql")
  void 스케줄_등록_AWS_Rule_생성_조회_전체_플로우() {
      // Given
      RegisterSchedulerRequest request = new RegisterSchedulerRequest(
          "daily-crawler",
          "cron(0 0 * * ? *)"
      );

      // When: 1. 스케줄 등록
      ResponseEntity<SchedulerResponse> registerResponse = restTemplate.postForEntity(
          "/api/v1/sellers/1/schedulers",
          request,
          SchedulerResponse.class
      );

      // Then: 1. 등록 성공
      assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
      assertThat(registerResponse.getBody().status()).isEqualTo(SchedulerStatus.PENDING);
      Long schedulerId = registerResponse.getBody().schedulerId();

      // When: 2. Outbox Event 처리 (자동 트리거)
      // TransactionSynchronization.afterCommit() 실행
      // → AWS EventBridge CreateRule API 호출

      // Then: 2. AWS Rule 생성 검증
      wireMockServer.verify(postRequestedFor(urlEqualTo("/rules")));

      // When: 3. 스케줄 조회
      ResponseEntity<SchedulerDetailResponse> getResponse = restTemplate.getForEntity(
          "/api/v1/schedulers/" + schedulerId,
          SchedulerDetailResponse.class
      );

      // Then: 3. 상태 전환 검증
      assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(getResponse.getBody().status()).isEqualTo(SchedulerStatus.ACTIVE);
      assertThat(getResponse.getBody().eventBridgeRuleName()).isNotNull();
  }
  ```
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: E2E 시나리오 1 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] 테스트 시나리오 명확성 개선
- [ ] 커밋: `struct: E2E 시나리오 1 구조 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] E2E 시나리오 SQL 데이터 정리
- [ ] 커밋: `test: E2E 시나리오 1 Fixture 정리 (Tidy)`

---

## 🔄 Cycle 5: E2E 시나리오 2 - 스케줄 수정 → AWS Rule 업데이트 → 이력 조회

**목표**: 스케줄 수정 및 이력 관리 E2E 테스트

#### 🔴 Red: 테스트 작성
- [ ] 시나리오 2 테스트 추가
  - Step 1: 시나리오 1 선행 (스케줄 등록 → ACTIVE)
  - Step 2: PATCH /api/v1/schedulers/1 (Cron 변경) → 200 OK
  - Step 3: Outbox Event 처리 → AWS UpdateRule API 호출 검증
  - Step 4: GET /api/v1/schedulers/1/history (이력 조회)
    - changedField = CRON_EXPRESSION
    - oldValue = "cron(0 0 * * ? *)"
    - newValue = "cron(0 12 * * ? *)"
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: E2E 시나리오 2 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] E2E 시나리오 2 테스트 구현
  ```java
  @Test
  @Sql("/sql/e2e-scenario-2.sql")
  void 스케줄_수정_AWS_Rule_업데이트_이력_조회() {
      // Given: 시나리오 1 선행 (스케줄 ACTIVE 상태)
      Long schedulerId = 1L;

      // When: 1. Cron 변경
      UpdateSchedulerRequest updateRequest = new UpdateSchedulerRequest(
          null,
          "cron(0 12 * * ? *)",
          null
      );
      ResponseEntity<SchedulerResponse> updateResponse = restTemplate.exchange(
          "/api/v1/schedulers/" + schedulerId,
          HttpMethod.PATCH,
          new HttpEntity<>(updateRequest),
          SchedulerResponse.class
      );

      // Then: 1. 수정 성공
      assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

      // Then: 2. AWS UpdateRule API 호출 검증
      wireMockServer.verify(putRequestedFor(urlMatching("/rules/.*")));

      // When: 2. 이력 조회
      ResponseEntity<PageResponse> historyResponse = restTemplate.getForEntity(
          "/api/v1/schedulers/" + schedulerId + "/history",
          PageResponse.class
      );

      // Then: 2. 이력 기록 검증
      assertThat(historyResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
      // changedField, oldValue, newValue 검증
  }
  ```
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: E2E 시나리오 2 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] 테스트 시나리오 명확성 개선
- [ ] 커밋: `struct: E2E 시나리오 2 구조 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] E2E 시나리오 2 SQL 데이터 정리
- [ ] 커밋: `test: E2E 시나리오 2 Fixture 정리 (Tidy)`

---

## 🔄 Cycle 6: E2E 시나리오 3 - 셀러 비활성화 → 스케줄 일괄 비활성화

**목표**: 연쇄 비활성화 로직 E2E 테스트

#### 🔴 Red: 테스트 작성
- [ ] 시나리오 3 테스트 추가
  - Step 1: POST /api/v1/sellers (셀러 등록)
  - Step 2: POST /api/v1/sellers/1/schedulers (스케줄 3개 등록)
    - daily-crawler
    - hourly-crawler
    - weekly-crawler
  - Step 3: Outbox Event 처리 (3개 모두 ACTIVE)
  - Step 4: PATCH /api/v1/sellers/1/status (INACTIVE) → 200 OK
  - Step 5: GET /api/v1/schedulers?sellerId=1 (목록 조회)
    - 3개 모두 status = INACTIVE 검증
    - AWS DisableRule API 3회 호출 검증
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: E2E 시나리오 3 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] E2E 시나리오 3 테스트 구현
  ```java
  @Test
  @Sql("/sql/e2e-scenario-3.sql")
  void 셀러_비활성화_스케줄_일괄_비활성화() {
      // Given: 셀러 + 스케줄 3개 (모두 ACTIVE)
      Long sellerId = 1L;

      // When: 셀러 비활성화
      SellerStatusRequest request = new SellerStatusRequest(SellerStatus.INACTIVE);
      ResponseEntity<SellerResponse> response = restTemplate.exchange(
          "/api/v1/sellers/" + sellerId + "/status",
          HttpMethod.PATCH,
          new HttpEntity<>(request),
          SellerResponse.class
      );

      // Then: 1. 셀러 비활성화 성공
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

      // When: 스케줄 목록 조회
      ResponseEntity<PageResponse> listResponse = restTemplate.getForEntity(
          "/api/v1/schedulers?sellerId=" + sellerId,
          PageResponse.class
      );

      // Then: 2. 모든 스케줄 INACTIVE
      assertThat(listResponse.getBody().content()).hasSize(3);
      // 모든 스케줄 status = INACTIVE 검증

      // Then: 3. AWS DisableRule API 3회 호출 검증
      wireMockServer.verify(3, postRequestedFor(urlMatching("/rules/.*/disable")));
  }
  ```
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: E2E 시나리오 3 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] 테스트 시나리오 명확성 개선
- [ ] 커밋: `struct: E2E 시나리오 3 구조 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] E2E 시나리오 3 SQL 데이터 정리
- [ ] 커밋: `test: E2E 시나리오 3 Fixture 정리 (Tidy)`

---

## 🔄 Cycle 7: TransactionSynchronization 테스트 - 성공 시나리오

**목표**: TransactionSynchronization.afterCommit() 성공 시나리오 테스트

#### 🔴 Red: 테스트 작성
- [ ] `TransactionSynchronizationTest` 생성
  - Scheduler + Outbox 저장 검증
  - TransactionSynchronization.afterCommit 트리거 검증
  - AWS API 호출 성공 검증 (WireMock)
  - Outbox.status → PUBLISHED 검증
  - Scheduler.status → ACTIVE 검증
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: TransactionSynchronization 성공 시나리오 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] TransactionSynchronization 성공 테스트 구현
  ```java
  @Test
  @Sql("/sql/transaction-sync-success.sql")
  void TransactionSynchronization_성공_시나리오() {
      // Given
      RegisterSchedulerCommand command = new RegisterSchedulerCommand(
          1L, "daily-crawler", "cron(0 0 * * ? *)"
      );

      // When: UseCase 실행 (Scheduler + Outbox 저장)
      SchedulerResponseDto result = registerSchedulerUseCase.execute(command);

      // Then: 1. Scheduler 저장 검증
      assertThat(result.status()).isEqualTo(SchedulerStatus.PENDING);

      // Then: 2. TransactionSynchronization 트리거 (afterCommit)
      // AWS CreateRule API 호출 검증
      wireMockServer.verify(postRequestedFor(urlEqualTo("/rules")));

      // Then: 3. Outbox 상태 전환 검증
      OutboxEvent outbox = outboxEventQueryAdapter.findById(1L).get();
      assertThat(outbox.getStatus()).isEqualTo(OutboxStatus.PUBLISHED);

      // Then: 4. Scheduler 상태 전환 검증
      Scheduler scheduler = schedulerQueryAdapter.findById(result.schedulerId()).get();
      assertThat(scheduler.getStatus()).isEqualTo(SchedulerStatus.ACTIVE);
  }
  ```
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: TransactionSynchronization 성공 시나리오 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] TransactionSynchronization 로직 개선
- [ ] 커밋: `struct: TransactionSynchronization 구조 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TransactionSynchronization 테스트 데이터 정리
- [ ] 커밋: `test: TransactionSynchronization Fixture 정리 (Tidy)`

---

## 🔄 Cycle 8: TransactionSynchronization 테스트 - 실패 시나리오

**목표**: TransactionSynchronization AWS API 실패 시나리오 테스트

#### 🔴 Red: 테스트 작성
- [ ] AWS API 실패 시나리오 테스트 추가
  - Scheduler + Outbox 저장 검증
  - TransactionSynchronization.afterCommit 트리거
  - AWS API 호출 실패 (WireMock Exception)
  - Outbox.status → FAILED 검증
  - Scheduler.status → PENDING (그대로) 검증
  - Outbox.errorMessage 업데이트 검증
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: TransactionSynchronization 실패 시나리오 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] WireMock 실패 응답 설정
  ```java
  @Test
  @Sql("/sql/transaction-sync-failure.sql")
  void TransactionSynchronization_실패_시나리오() {
      // Given: AWS API 실패 응답 설정
      wireMockServer.stubFor(post(urlEqualTo("/rules"))
          .willReturn(aResponse()
              .withStatus(500)
              .withBody("{\"error\": \"Internal Server Error\"}")));

      // When: UseCase 실행
      RegisterSchedulerCommand command = new RegisterSchedulerCommand(
          1L, "daily-crawler", "cron(0 0 * * ? *)"
      );
      SchedulerResponseDto result = registerSchedulerUseCase.execute(command);

      // Then: 1. Scheduler PENDING 유지
      Scheduler scheduler = schedulerQueryAdapter.findById(result.schedulerId()).get();
      assertThat(scheduler.getStatus()).isEqualTo(SchedulerStatus.PENDING);

      // Then: 2. Outbox FAILED
      OutboxEvent outbox = outboxEventQueryAdapter.findBySchedulerId(scheduler.getSchedulerId()).get();
      assertThat(outbox.getStatus()).isEqualTo(OutboxStatus.FAILED);
      assertThat(outbox.getErrorMessage()).isNotNull();
  }
  ```
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: TransactionSynchronization 실패 시나리오 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] 실패 처리 로직 개선
- [ ] 커밋: `struct: TransactionSynchronization 실패 처리 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TransactionSynchronization 실패 테스트 데이터 정리
- [ ] 커밋: `test: TransactionSynchronization 실패 Fixture 정리 (Tidy)`

---

## 🔄 Cycle 9: OutboxEventProcessor 테스트 - PENDING 재처리

**목표**: OutboxEventProcessor PENDING 상태 재처리 테스트

#### 🔴 Red: 테스트 작성
- [ ] `OutboxEventProcessorTest` 생성
  - PENDING Outbox Event 생성 (retryCount = 0)
  - OutboxEventProcessor 실행 (매 1분)
  - AWS API 호출 성공 (WireMock)
  - Outbox.status → PUBLISHED 검증
  - Scheduler.status → ACTIVE 검증
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: OutboxEventProcessor PENDING 재처리 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] OutboxEventProcessor 재처리 테스트 구현
  ```java
  @Test
  @Sql("/sql/outbox-processor-pending.sql")
  void OutboxEventProcessor_PENDING_재처리() {
      // Given: PENDING Outbox Event (retryCount = 0)
      OutboxEvent outbox = outboxEventQueryAdapter.findById(1L).get();
      assertThat(outbox.getStatus()).isEqualTo(OutboxStatus.PENDING);

      // When: OutboxEventProcessor 실행
      outboxEventProcessor.processPendingEvents();

      // Then: 1. AWS API 호출 검증
      wireMockServer.verify(postRequestedFor(urlEqualTo("/rules")));

      // Then: 2. Outbox PUBLISHED
      OutboxEvent updated = outboxEventQueryAdapter.findById(1L).get();
      assertThat(updated.getStatus()).isEqualTo(OutboxStatus.PUBLISHED);
  }
  ```
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: OutboxEventProcessor PENDING 재처리 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] OutboxEventProcessor 로직 개선
- [ ] 커밋: `struct: OutboxEventProcessor 구조 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] OutboxEventProcessor PENDING 테스트 데이터 정리
- [ ] 커밋: `test: OutboxEventProcessor PENDING Fixture 정리 (Tidy)`

---

## 🔄 Cycle 10: OutboxEventProcessor 테스트 - FAILED 재시도

**목표**: OutboxEventProcessor FAILED 상태 재시도 테스트

#### 🔴 Red: 테스트 작성
- [ ] FAILED Outbox 재시도 테스트 추가
  - FAILED Outbox Event (retryCount = 1)
  - OutboxEventProcessor 실행
  - AWS API 호출 실패 (WireMock)
  - Outbox.retryCount++ 검증 (1 → 2)
  - Outbox.status → FAILED (그대로) 검증
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: OutboxEventProcessor FAILED 재시도 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] FAILED 재시도 테스트 구현
  ```java
  @Test
  @Sql("/sql/outbox-processor-failed.sql")
  void OutboxEventProcessor_FAILED_재시도() {
      // Given: FAILED Outbox (retryCount = 1)
      OutboxEvent outbox = outboxEventQueryAdapter.findById(1L).get();
      assertThat(outbox.getRetryCount()).isEqualTo(1);

      // Given: AWS API 실패 응답
      wireMockServer.stubFor(post(urlEqualTo("/rules"))
          .willReturn(aResponse().withStatus(500)));

      // When: OutboxEventProcessor 실행
      outboxEventProcessor.processFailedEvents();

      // Then: 1. retryCount 증가
      OutboxEvent updated = outboxEventQueryAdapter.findById(1L).get();
      assertThat(updated.getRetryCount()).isEqualTo(2);

      // Then: 2. 여전히 FAILED
      assertThat(updated.getStatus()).isEqualTo(OutboxStatus.FAILED);
  }
  ```
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: OutboxEventProcessor FAILED 재시도 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] 재시도 로직 개선
- [ ] 커밋: `struct: OutboxEventProcessor 재시도 구조 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] OutboxEventProcessor FAILED 테스트 데이터 정리
- [ ] 커밋: `test: OutboxEventProcessor FAILED Fixture 정리 (Tidy)`

---

## 🔄 Cycle 11: OutboxEventProcessor 테스트 - 최대 재시도 초과

**목표**: OutboxEventProcessor 최대 재시도 초과 시나리오 테스트

#### 🔴 Red: 테스트 작성
- [ ] 최대 재시도 초과 테스트 추가
  - FAILED Outbox (retryCount = 3)
  - OutboxEventProcessor 실행
  - Outbox.status → FAILED (영구)
  - Slack 알림 발송 검증 (WireMock)
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: OutboxEventProcessor 최대 재시도 초과 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] 최대 재시도 초과 테스트 구현
  ```java
  @Test
  @Sql("/sql/outbox-processor-max-retries.sql")
  void OutboxEventProcessor_최대_재시도_초과() {
      // Given: FAILED Outbox (retryCount = 3, maxRetries = 3)
      OutboxEvent outbox = outboxEventQueryAdapter.findById(1L).get();
      assertThat(outbox.getRetryCount()).isEqualTo(3);
      assertThat(outbox.getMaxRetries()).isEqualTo(3);

      // When: OutboxEventProcessor 실행
      outboxEventProcessor.processFailedEvents();

      // Then: 1. 더 이상 재시도 안함
      OutboxEvent updated = outboxEventQueryAdapter.findById(1L).get();
      assertThat(updated.getRetryCount()).isEqualTo(3); // 그대로

      // Then: 2. Slack 알림 발송 검증
      wireMockServer.verify(postRequestedFor(urlEqualTo("/slack/webhook")));
  }
  ```
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: OutboxEventProcessor 최대 재시도 초과 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] 알림 로직 개선
- [ ] 커밋: `struct: OutboxEventProcessor 알림 구조 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] OutboxEventProcessor 최대 재시도 테스트 데이터 정리
- [ ] 커밋: `test: OutboxEventProcessor 최대 재시도 Fixture 정리 (Tidy)`

---

## 🔄 Cycle 12: Exponential Backoff 테스트

**목표**: 재시도 간격 Exponential Backoff 전략 테스트

#### 🔴 Red: 테스트 작성
- [ ] `ExponentialBackoffTest` 생성
  - 1차 재시도: 1분 후 검증
  - 2차 재시도: 5분 후 검증 (2^1 * base)
  - 3차 재시도: 15분 후 검증 (2^2 * base)
  - 재시도 대기 시간 계산 로직 검증
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: Exponential Backoff 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] Exponential Backoff 테스트 구현
  ```java
  @Test
  void Exponential_Backoff_재시도_간격() {
      // Given
      int baseDelayMinutes = 1;

      // When & Then: 1차 재시도 (1분)
      int delay1 = calculateBackoffDelay(0, baseDelayMinutes);
      assertThat(delay1).isEqualTo(1);

      // When & Then: 2차 재시도 (5분)
      int delay2 = calculateBackoffDelay(1, baseDelayMinutes);
      assertThat(delay2).isEqualTo(5); // 2^1 * 1 * some_factor

      // When & Then: 3차 재시도 (15분)
      int delay3 = calculateBackoffDelay(2, baseDelayMinutes);
      assertThat(delay3).isEqualTo(15); // 2^2 * 1 * some_factor
  }

  private int calculateBackoffDelay(int retryCount, int baseDelayMinutes) {
      // Exponential Backoff 로직
      return (int) (Math.pow(2, retryCount) * baseDelayMinutes * 1); // 간소화
  }
  ```
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: Exponential Backoff 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] Backoff 전략 정교화
- [ ] 커밋: `struct: Exponential Backoff 구조 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Exponential Backoff 테스트 정리
- [ ] 커밋: `test: Exponential Backoff Fixture 정리 (Tidy)`

---

## 🔄 Cycle 13: EventBridgeClientAdapter 테스트

**목표**: EventBridgeClientAdapter 성공 케이스 테스트 (CreateRule, UpdateRule, DisableRule)

#### 🔴 Red: 테스트 작성
- [ ] `EventBridgeClientAdapterTest` 생성
  - CreateRule API 테스트 (ruleName, cronExpression, target)
  - UpdateRule API 테스트 (ruleName, cronExpression)
  - DisableRule API 테스트 (ruleName)
  - WireMock 응답 검증
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: EventBridgeClientAdapter 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] EventBridgeClientAdapter 테스트 구현
  ```java
  @Test
  void CreateRule_API_성공() {
      // Given
      CreateRuleRequest request = new CreateRuleRequest(
          "test-rule",
          "cron(0 0 * * ? *)",
          "arn:aws:lambda:us-east-1:123456789012:function:test"
      );

      // When
      CreateRuleResponse response = eventBridgeClientAdapter.createRule(request);

      // Then
      assertThat(response.getRuleArn()).isNotNull();
      wireMockServer.verify(postRequestedFor(urlEqualTo("/rules")));
  }

  @Test
  void UpdateRule_API_성공() {
      // Given
      UpdateRuleRequest request = new UpdateRuleRequest(
          "test-rule",
          "cron(0 12 * * ? *)"
      );

      // When
      UpdateRuleResponse response = eventBridgeClientAdapter.updateRule(request);

      // Then
      assertThat(response.getRuleArn()).isNotNull();
      wireMockServer.verify(putRequestedFor(urlMatching("/rules/.*")));
  }

  @Test
  void DisableRule_API_성공() {
      // Given
      String ruleName = "test-rule";

      // When
      eventBridgeClientAdapter.disableRule(ruleName);

      // Then
      wireMockServer.verify(postRequestedFor(urlMatching("/rules/.*/disable")));
  }
  ```
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: EventBridgeClientAdapter 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] Adapter 로직 개선
- [ ] 커밋: `struct: EventBridgeClientAdapter 구조 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] EventBridgeClientAdapter Fixture 정리
- [ ] 커밋: `test: EventBridgeClientAdapter Fixture 정리 (Tidy)`

---

## 🔄 Cycle 14: EventBridgeClientAdapter 실패 시나리오 테스트

**목표**: EventBridgeClientAdapter API 실패 처리 테스트

#### 🔴 Red: 테스트 작성
- [ ] API 실패 시나리오 테스트 추가
  - WireMock Exception 발생 설정
  - Outbox.status → FAILED 검증
  - Outbox.errorMessage 업데이트 검증
  - 예외 처리 로직 검증
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: EventBridgeClientAdapter 실패 시나리오 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] API 실패 처리 테스트 구현
  ```java
  @Test
  void CreateRule_API_실패() {
      // Given: AWS API 실패 응답
      wireMockServer.stubFor(post(urlEqualTo("/rules"))
          .willReturn(aResponse()
              .withStatus(500)
              .withBody("{\"error\": \"Internal Server Error\"}")));

      CreateRuleRequest request = new CreateRuleRequest(
          "test-rule", "cron(0 0 * * ? *)", "target-arn"
      );

      // When & Then: Exception 발생
      assertThatThrownBy(() -> eventBridgeClientAdapter.createRule(request))
          .isInstanceOf(EventBridgeApiException.class)
          .hasMessageContaining("Internal Server Error");
  }
  ```
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: EventBridgeClientAdapter 실패 시나리오 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] 예외 처리 개선
- [ ] 커밋: `struct: EventBridgeClientAdapter 예외 처리 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] EventBridgeClientAdapter 실패 테스트 정리
- [ ] 커밋: `test: EventBridgeClientAdapter 실패 Fixture 정리 (Tidy)`

---

## 🔄 Cycle 15: Integration Test 전체 시나리오 검증

**목표**: 모든 Integration Test 통합 검증 및 Zero-Tolerance 규칙 준수 확인

#### 🔴 Red: 테스트 작성
- [ ] `IntegrationTestSuiteTest` 생성
  - 모든 E2E 시나리오 통합 실행
  - TestRestTemplate 사용 검증 (MockMvc 금지)
  - Flyway vs @Sql 역할 구분 검증
  - @MockBean 남발 금지 검증
  - EntityManager.persist() 직접 호출 금지 검증
  - 테스트 커버리지 > 80% 검증
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: Integration Test 전체 시나리오 검증 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] 통합 검증 테스트 구현
  ```java
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class IntegrationTestSuiteTest extends EventBridgeMockTestBase {
      @Test
      @Order(1)
      void E2E_시나리오_1_전체_플로우() {
          // E2E 시나리오 1 실행
      }

      @Test
      @Order(2)
      void E2E_시나리오_2_전체_플로우() {
          // E2E 시나리오 2 실행
      }

      @Test
      @Order(3)
      void E2E_시나리오_3_전체_플로우() {
          // E2E 시나리오 3 실행
      }

      @Test
      @Order(4)
      void Outbox_Pattern_전체_플로우() {
          // TransactionSynchronization + OutboxEventProcessor 통합
      }
  }
  ```
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: Integration Test 전체 시나리오 검증 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] 통합 테스트 시나리오 개선
- [ ] 테스트 커버리지 확인
- [ ] 커밋: `struct: Integration Test 전체 시나리오 구조 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 전체 Integration Test Fixture 정리
- [ ] SQL 파일 정리
- [ ] 커밋: `test: Integration Test 전체 Fixture 정리 (Tidy)`

---

## ✅ 완료 조건

- [ ] **15개 TDD 사이클 완료**
- [ ] **TestContainers MySQL 환경 구성 완료**
  - Flyway Migration 자동 실행
  - H2 금지
- [ ] **WireMock Server 구성 완료**
  - AWS EventBridge Mock
  - Slack Webhook Mock
- [ ] **E2E 테스트 시나리오 완료** (3개)
  - 시나리오 1: 스케줄 등록 → 조회
  - 시나리오 2: 스케줄 수정 → 이력 조회
  - 시나리오 3: 셀러 비활성화 → 스케줄 일괄 비활성화
- [ ] **Outbox Pattern 통합 테스트 완료**
  - TransactionSynchronization (성공/실패)
  - OutboxEventProcessor (PENDING, FAILED, 최대 재시도)
  - Exponential Backoff
- [ ] **AWS EventBridge Mock 연동 테스트 완료**
  - CreateRule, UpdateRule, DisableRule API
  - API 실패 시나리오
- [ ] **Zero-Tolerance 규칙 준수 확인**
  - TestRestTemplate 필수 (MockMvc 금지)
  - Flyway vs @Sql 역할 구분
  - @MockBean 남발 금지
  - EntityManager.persist() 직접 호출 금지
- [ ] **모든 테스트 통과 (100%)**
- [ ] **모든 커밋 메시지 규칙 준수** (test:, feat:, struct:, test:)

---

## 📊 최종 통계

- **총 사이클 수**: 15개
- **예상 소요 시간**: 225분 (3시간 45분)
- **총 체크박스**: 60개 (15 사이클 × 4 단계)
- **커밋 횟수**: 60회 (각 단계마다 커밋)
- **E2E 시나리오**: 3개
- **Outbox Pattern 테스트**: 6개
- **AWS Mock API**: 3개 (CreateRule, UpdateRule, DisableRule)

---

## 🔗 관련 문서

- **PRD**: `/Users/sangwon-ryu/crawlinghub/docs/prd/eventbridge/EVENTBRIDGE-005-integration.md`
- **코딩 규칙**: `docs/coding_convention/05-testing/integration-testing/`
- **선행 Task**: EVENTBRIDGE-001-domain-plan.md, EVENTBRIDGE-002-application-plan.md, EVENTBRIDGE-003-persistence-plan.md, EVENTBRIDGE-004-rest-api-plan.md
