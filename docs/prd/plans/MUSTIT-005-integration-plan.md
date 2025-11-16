# MUSTIT-005 TDD Plan

**Task**: Integration Test 구현
**Layer**: Integration Test
**브랜치**: feature/MUSTIT-005-integration
**예상 소요 시간**: 600분 (40 사이클 × 15분)

---

## 📝 TDD 사이클 체크리스트

### 1️⃣ TestContainers 환경 구성 - MySQL (Cycle 1)

#### 🔴 Red: 테스트 작성
- [ ] `TestContainersConfigTest.java` 생성
- [ ] `@Testcontainers` 어노테이션 추가
- [ ] `shouldStartMySQLContainer()` 작성
- [ ] MySQL Container 연결 테스트
- [ ] 테스트 실행 → 실패 확인 (Container 미구성)
- [ ] 커밋: `test: TestContainers MySQL 설정 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `@Container` MySQLContainer 생성
- [ ] mysql:8.0 이미지 사용
- [ ] DatabaseName, Username, Password 설정
- [ ] `@DynamicPropertySource` 설정 (JDBC URL 주입)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: TestContainers MySQL 설정 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Base Test 클래스로 추출 (재사용)
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: TestContainers MySQL 설정 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Container Configuration Fixture 생성
- [ ] 커밋: `test: TestContainers MySQL 설정 정리 (Tidy)`

---

### 2️⃣ TestContainers 환경 구성 - Localstack (Cycle 2)

#### 🔴 Red: 테스트 작성
- [ ] `shouldStartLocalstackContainer()` 작성
- [ ] LocalstackContainer 연결 테스트
- [ ] EventBridge, SQS 서비스 활성화 확인
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: TestContainers Localstack 설정 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `@Container` LocalStackContainer 생성
- [ ] localstack/localstack:latest 이미지 사용
- [ ] 서비스: EVENTBRIDGE, SQS
- [ ] `@DynamicPropertySource` 설정 (Endpoint 주입)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: TestContainers Localstack 설정 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Base Test 클래스에 통합
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: TestContainers Localstack 설정 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Localstack Configuration Fixture 생성
- [ ] 커밋: `test: TestContainers Localstack 설정 정리 (Tidy)`

---

### 3️⃣ Flyway 마이그레이션 자동 실행 테스트 (Cycle 3)

#### 🔴 Red: 테스트 작성
- [ ] `shouldRunFlywayMigrations()` 작성
- [ ] 테이블 존재 여부 확인 (seller, user_agent, product, etc.)
- [ ] 초기 데이터 삽입 확인 (50개 UserAgent)
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: Flyway 마이그레이션 자동 실행 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] Flyway 설정 활성화 (application-test.yml)
- [ ] V1-V8 마이그레이션 파일 확인
- [ ] 초기 데이터 삽입 스크립트 실행
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: Flyway 마이그레이션 자동 실행 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 마이그레이션 순서 검증
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: Flyway 마이그레이션 자동 실행 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Flyway Fixture 정리
- [ ] 커밋: `test: Flyway 마이그레이션 테스트 정리 (Tidy)`

---

### 4️⃣ @Sql 테스트 데이터 준비 (Cycle 4)

#### 🔴 Red: 테스트 작성
- [ ] `shouldLoadTestDataWithSqlScript()` 작성
- [ ] `@Sql("/test-data/sellers.sql")` 사용
- [ ] 셀러 데이터 로드 확인
- [ ] 테스트 실행 → 실패 확인 (SQL 파일 없음)
- [ ] 커밋: `test: @Sql 테스트 데이터 준비 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `src/test/resources/test-data/sellers.sql` 생성
- [ ] INSERT INTO seller 문장 작성 (3개 셀러)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: @Sql 테스트 데이터 준비 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] SQL 스크립트 정리 (가독성 개선)
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: @Sql 테스트 데이터 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] SQL Script Fixture 정리
- [ ] 커밋: `test: @Sql 테스트 데이터 정리 (Tidy)`

---

### 5️⃣ E2E Scenario 1 - Part 1: 셀러 등록 (Cycle 5)

#### 🔴 Red: 테스트 작성
- [ ] `E2EIntegrationTest.java` 생성
- [ ] `@SpringBootTest(webEnvironment = RANDOM_PORT)` 추가
- [ ] `shouldRegisterSellerSuccessfully()` 작성
- [ ] POST /api/v1/sellers 호출
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: E2E 셀러 등록 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] TestRestTemplate 주입
- [ ] RegisterSellerRequest DTO 사용
- [ ] HTTP 201 Created 응답 검증
- [ ] SellerResponse 검증
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: E2E 셀러 등록 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 테스트 가독성 개선 (Given-When-Then)
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: E2E 셀러 등록 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] RegisterSellerRequest Fixture 사용
- [ ] 커밋: `test: E2E 셀러 등록 테스트 정리 (Tidy)`

---

### 6️⃣ E2E Scenario 1 - Part 2: 크롤링 트리거 (Cycle 6)

#### 🔴 Red: 테스트 작성
- [ ] `shouldTriggerCrawlingSuccessfully()` 작성
- [ ] POST /api/internal/crawling/trigger 호출
- [ ] CrawlingTriggeredResponse 검증
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: E2E 크롤링 트리거 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] TriggerCrawlingRequest DTO 사용
- [ ] HTTP 200 OK 응답 검증
- [ ] taskCount > 0 검증
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: E2E 크롤링 트리거 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 테스트 가독성 개선
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: E2E 크롤링 트리거 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TriggerCrawlingRequest Fixture 사용
- [ ] 커밋: `test: E2E 크롤링 트리거 테스트 정리 (Tidy)`

---

### 7️⃣ E2E Scenario 1 - Part 3: 미니샵 태스크 생성 확인 (Cycle 7)

#### 🔴 Red: 테스트 작성
- [ ] `shouldCreateMinishopTasks()` 작성
- [ ] CrawlerTaskRepository 주입
- [ ] findBySellerId() 호출
- [ ] 상태: WAITING 검증
- [ ] taskType: MINISHOP 검증
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: E2E 미니샵 태스크 생성 확인 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] Repository 조회 로직 추가
- [ ] CrawlerTask 상태 검증
- [ ] 페이지 수 계산 검증 (totalProductCount / 500)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: E2E 미니샵 태스크 생성 확인 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Assertion 명확화
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: E2E 미니샵 태스크 생성 확인 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] CrawlerTask 검증 Fixture 생성
- [ ] 커밋: `test: E2E 미니샵 태스크 생성 확인 테스트 정리 (Tidy)`

---

### 8️⃣ E2E Scenario 1 - Part 4: 태스크 발행 (SQS 연동) (Cycle 8)

#### 🔴 Red: 테스트 작성
- [ ] `shouldPublishTasksToSqs()` 작성
- [ ] PublishCrawlerTasksUseCase 주입
- [ ] PublishCrawlerTasksCommand 실행
- [ ] SQS 메시지 수신 확인 (Localstack)
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: E2E 태스크 발행 SQS 연동 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] SqsClient 주입 (Localstack Endpoint)
- [ ] ReceiveMessageRequest 생성
- [ ] 메시지 수신 검증
- [ ] Message Body 파싱 및 검증
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: E2E 태스크 발행 SQS 연동 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] SQS 연동 Helper 메서드 추출
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: E2E 태스크 발행 SQS 연동 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] SQS Message Fixture 생성
- [ ] 커밋: `test: E2E 태스크 발행 SQS 연동 테스트 정리 (Tidy)`

---

### 9️⃣ WireMock 설정 - 머스트잇 API Mock (Cycle 9)

#### 🔴 Red: 테스트 작성
- [ ] `MustitApiMockTest.java` 생성
- [ ] `@WireMockTest(httpPort = 8089)` 추가
- [ ] 미니샵 API Mock 테스트 작성
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: WireMock 머스트잇 API Mock 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] WireMock stubFor() 설정
- [ ] GET /mustit-api/facade-api/v1/searchmini-shop-search Mock
- [ ] Query Parameters 검증 (sellerId, pageNo, pageSize)
- [ ] 200 OK + JSON 응답
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: WireMock 머스트잇 API Mock 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Mock 응답 JSON 파일로 분리
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: WireMock 머스트잇 API Mock 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] WireMock Stub Fixture 생성
- [ ] 커밋: `test: WireMock 머스트잇 API Mock 테스트 정리 (Tidy)`

---

### 🔟 E2E Scenario 1 - Part 5: 워커 태스크 처리 (Cycle 10)

#### 🔴 Red: 테스트 작성
- [ ] `shouldProcessTaskAndSaveProduct()` 작성
- [ ] ProcessCrawlerTaskUseCase 주입
- [ ] SQS 메시지 폴링 → UseCase 실행
- [ ] Product 생성 확인
- [ ] ProductRawData 저장 확인 (MINISHOP)
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: E2E 워커 태스크 처리 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] ProcessCrawlerTaskCommand 생성
- [ ] UseCase 실행
- [ ] ProductRepository 조회
- [ ] 상태: COMPLETED 검증
- [ ] ProductRawData 검증
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: E2E 워커 태스크 처리 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 테스트 가독성 개선
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: E2E 워커 태스크 처리 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] ProcessCrawlerTaskCommand Fixture 사용
- [ ] 커밋: `test: E2E 워커 태스크 처리 테스트 정리 (Tidy)`

---

### 1️⃣1️⃣ E2E Scenario 2 - Part 1: 기존 상품 준비 (Cycle 11)

#### 🔴 Red: 테스트 작성
- [ ] `shouldDetectProductChange()` 작성
- [ ] @Sql로 기존 상품 삽입
- [ ] itemNo: 12345, minishopDataHash: "old_hash"
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: E2E 기존 상품 준비 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `src/test/resources/test-data/products.sql` 생성
- [ ] INSERT INTO product 문장 작성
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: E2E 기존 상품 준비 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] SQL 스크립트 정리
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: E2E 기존 상품 준비 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Product SQL Fixture 정리
- [ ] 커밋: `test: E2E 기존 상품 준비 테스트 정리 (Tidy)`

---

### 1️⃣2️⃣ E2E Scenario 2 - Part 2: 상품 재크롤링 (변경 감지) (Cycle 12)

#### 🔴 Red: 테스트 작성
- [ ] `shouldDetectProductChangeAndCreateOutbox()` 작성
- [ ] ProcessCrawlerTaskUseCase 실행 (동일 itemNo, 다른 해시)
- [ ] Product.hasChanged() → true 검증
- [ ] ProductOutbox 생성 확인
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: E2E 상품 재크롤링 변경 감지 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] WireMock으로 변경된 데이터 응답
- [ ] 새 해시값 계산: "new_hash"
- [ ] ProductOutbox 생성 로직 실행
- [ ] eventType: PRODUCT_UPDATED 검증
- [ ] status: WAITING 검증
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: E2E 상품 재크롤링 변경 감지 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 변경 감지 로직 명확화
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: E2E 상품 재크롤링 변경 감지 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] ProductOutbox Fixture 사용
- [ ] 커밋: `test: E2E 상품 재크롤링 변경 감지 테스트 정리 (Tidy)`

---

### 1️⃣3️⃣ WireMock 설정 - 외부 상품 서버 API Mock (Cycle 13)

#### 🔴 Red: 테스트 작성
- [ ] `ExternalProductApiMockTest.java` 생성
- [ ] POST /products/updated Mock 테스트 작성
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: WireMock 외부 상품 서버 API Mock 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] WireMock stubFor() 설정
- [ ] POST /products/updated Mock
- [ ] Request Body 검증
- [ ] 200 OK 응답
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: WireMock 외부 상품 서버 API Mock 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Mock 응답 정리
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: WireMock 외부 상품 서버 API Mock 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] External API Stub Fixture 생성
- [ ] 커밋: `test: WireMock 외부 상품 서버 API Mock 테스트 정리 (Tidy)`

---

### 1️⃣4️⃣ E2E Scenario 2 - Part 3: Outbox 배치 처리 (Cycle 14)

#### 🔴 Red: 테스트 작성
- [ ] `shouldProcessOutboxAndSendToExternal()` 작성
- [ ] ProcessProductOutboxUseCase 주입
- [ ] 외부 API 호출 Mock 검증
- [ ] Outbox 상태: COMPLETED 검증
- [ ] sentAt 기록됨 검증
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: E2E Outbox 배치 처리 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] ProcessProductOutboxCommand 생성
- [ ] UseCase 실행
- [ ] WireMock verify() (POST /products/updated 호출 확인)
- [ ] ProductOutbox 상태 변경 검증
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: E2E Outbox 배치 처리 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Outbox 처리 로직 명확화
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: E2E Outbox 배치 처리 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] ProcessProductOutboxCommand Fixture 사용
- [ ] 커밋: `test: E2E Outbox 배치 처리 테스트 정리 (Tidy)`

---

### 1️⃣5️⃣ E2E Scenario 3 - Part 1: UserAgent 할당 (Cycle 15)

#### 🔴 Red: 테스트 작성
- [ ] `shouldAssignUserAgentSuccessfully()` 작성
- [ ] UserAgentPoolManager 주입
- [ ] assignUserAgent() 호출
- [ ] 상태: ACTIVE 검증
- [ ] requestCount 증가 검증
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: E2E UserAgent 할당 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] UserAgentPoolManager.assignUserAgent() 호출
- [ ] UserAgent 상태 검증
- [ ] requestCount 업데이트 확인
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: E2E UserAgent 할당 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] UserAgent 할당 로직 명확화
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: E2E UserAgent 할당 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] UserAgent Fixture 사용
- [ ] 커밋: `test: E2E UserAgent 할당 테스트 정리 (Tidy)`

---

### 1️⃣6️⃣ WireMock 설정 - 429 응답 시뮬레이션 (Cycle 16)

#### 🔴 Red: 테스트 작성
- [ ] `shouldSimulate429Response()` 작성
- [ ] WireMock 429 Too Many Requests 설정
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: WireMock 429 응답 시뮬레이션 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] WireMock stubFor() 설정
- [ ] 429 상태 코드 반환
- [ ] Retry-After 헤더 추가
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: WireMock 429 응답 시뮬레이션 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Mock 응답 정리
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: WireMock 429 응답 시뮬레이션 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 429 Response Stub Fixture 생성
- [ ] 커밋: `test: WireMock 429 응답 시뮬레이션 테스트 정리 (Tidy)`

---

### 1️⃣7️⃣ E2E Scenario 3 - Part 2: UserAgent 429 응답 처리 (Cycle 17)

#### 🔴 Red: 테스트 작성
- [ ] `shouldSuspendUserAgentOn429Response()` 작성
- [ ] 머스트잇 API 호출 → 429 응답
- [ ] UserAgent.suspend() 호출 검증
- [ ] 상태: SUSPENDED 검증
- [ ] token: null 검증
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: E2E UserAgent 429 응답 처리 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] WireMock 429 응답 설정
- [ ] ProcessCrawlerTaskUseCase 실행
- [ ] Exception 처리 로직
- [ ] UserAgent.suspend() 호출
- [ ] 상태 변경 검증
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: E2E UserAgent 429 응답 처리 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 429 처리 로직 명확화
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: E2E UserAgent 429 응답 처리 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 429 Scenario Fixture 생성
- [ ] 커밋: `test: E2E UserAgent 429 응답 처리 테스트 정리 (Tidy)`

---

### 1️⃣8️⃣ E2E Scenario 3 - Part 3: UserAgent 재할당 (Cycle 18)

#### 🔴 Red: 테스트 작성
- [ ] `shouldReassignDifferentUserAgent()` 작성
- [ ] 첫 번째 UserAgent SUSPENDED → 다른 UserAgent 할당
- [ ] 할당된 UserAgent가 다름 검증
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: E2E UserAgent 재할당 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] UserAgentPoolManager.assignUserAgent() 재호출
- [ ] SUSPENDED UserAgent 제외
- [ ] 다른 ACTIVE UserAgent 할당
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: E2E UserAgent 재할당 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 재할당 로직 명확화
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: E2E UserAgent 재할당 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] UserAgent Reassignment Fixture 생성
- [ ] 커밋: `test: E2E UserAgent 재할당 테스트 정리 (Tidy)`

---

### 1️⃣9️⃣ E2E Scenario 3 - Part 4: UserAgent 자동 복구 (Scheduled) (Cycle 19)

#### 🔴 Red: 테스트 작성
- [ ] `shouldRecoverSuspendedUserAgents()` 작성
- [ ] UserAgentPoolManager.recoverSuspendedUserAgents() 실행
- [ ] 1시간 경과 시뮬레이션 (@Sql로 suspendedAt 설정)
- [ ] 상태: SUSPENDED → ACTIVE 검증
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: E2E UserAgent 자동 복구 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] UserAgentPoolManager.recoverSuspendedUserAgents() 호출
- [ ] 1시간 경과한 SUSPENDED UserAgent 조회
- [ ] 상태 변경: ACTIVE
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: E2E UserAgent 자동 복구 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 복구 로직 명확화
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: E2E UserAgent 자동 복구 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Recovery Scenario Fixture 생성
- [ ] 커밋: `test: E2E UserAgent 자동 복구 테스트 정리 (Tidy)`

---

### 2️⃣0️⃣ EventBridge 연동 테스트 - Rule 생성 (Cycle 20)

#### 🔴 Red: 테스트 작성
- [ ] `EventBridgeIntegrationTest.java` 생성
- [ ] `shouldCreateEventBridgeRule()` 작성
- [ ] AwsEventBridgeAdapter.createRule() 호출
- [ ] Localstack EventBridge 확인
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: EventBridge Rule 생성 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] EventBridgeClient 주입 (Localstack Endpoint)
- [ ] PutRuleRequest 생성
- [ ] Rule Name: `mustit-crawler-{sellerId}`
- [ ] Schedule Expression: `rate(1 days)`
- [ ] Rule 생성 검증
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: EventBridge Rule 생성 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] EventBridge Helper 메서드 추출
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: EventBridge Rule 생성 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] EventBridge Rule Fixture 생성
- [ ] 커밋: `test: EventBridge Rule 생성 테스트 정리 (Tidy)`

---

### 2️⃣1️⃣ EventBridge 연동 테스트 - Rule 업데이트 (Cycle 21)

#### 🔴 Red: 테스트 작성
- [ ] `shouldUpdateEventBridgeRule()` 작성
- [ ] AwsEventBridgeAdapter.updateRule() 호출
- [ ] Schedule Expression 변경: `rate(2 days)`
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: EventBridge Rule 업데이트 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] PutRuleRequest 재생성 (동일 Rule Name)
- [ ] Schedule Expression 변경
- [ ] Rule 업데이트 검증
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: EventBridge Rule 업데이트 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Rule 업데이트 로직 명확화
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: EventBridge Rule 업데이트 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Rule Update Fixture 생성
- [ ] 커밋: `test: EventBridge Rule 업데이트 테스트 정리 (Tidy)`

---

### 2️⃣2️⃣ EventBridge 연동 테스트 - Rule 삭제 (Cycle 22)

#### 🔴 Red: 테스트 작성
- [ ] `shouldDeleteEventBridgeRule()` 작성
- [ ] AwsEventBridgeAdapter.deleteRule() 호출
- [ ] Localstack EventBridge에서 삭제 확인
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: EventBridge Rule 삭제 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] DeleteRuleRequest 생성
- [ ] Rule 삭제 실행
- [ ] Localstack 확인 (Rule 없음)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: EventBridge Rule 삭제 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Rule 삭제 로직 명확화
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: EventBridge Rule 삭제 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Rule Delete Fixture 생성
- [ ] 커밋: `test: EventBridge Rule 삭제 테스트 정리 (Tidy)`

---

### 2️⃣3️⃣ SQS 연동 테스트 - 메시지 발행 (Cycle 23)

#### 🔴 Red: 테스트 작성
- [ ] `SqsIntegrationTest.java` 생성
- [ ] `shouldPublishMessagesToSqs()` 작성
- [ ] SqsPublisherAdapter.sendBatch() 호출
- [ ] Localstack SQS 큐 확인
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: SQS 메시지 발행 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] SqsClient 주입 (Localstack Endpoint)
- [ ] SendMessageBatchRequest 생성
- [ ] Message Body 검증
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: SQS 메시지 발행 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] SQS Helper 메서드 추출
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: SQS 메시지 발행 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] SQS Message Fixture 생성
- [ ] 커밋: `test: SQS 메시지 발행 테스트 정리 (Tidy)`

---

### 2️⃣4️⃣ SQS 연동 테스트 - 메시지 폴링 (Cycle 24)

#### 🔴 Red: 테스트 작성
- [ ] `shouldPollMessagesFromSqs()` 작성
- [ ] SqsConsumerAdapter.poll() 호출
- [ ] 메시지 수신 확인
- [ ] Visibility Timeout 검증 (30초)
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: SQS 메시지 폴링 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] ReceiveMessageRequest 생성
- [ ] MaxNumberOfMessages: 10
- [ ] VisibilityTimeout: 30
- [ ] 메시지 수신 검증
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: SQS 메시지 폴링 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 폴링 로직 명확화
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: SQS 메시지 폴링 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] SQS Polling Fixture 생성
- [ ] 커밋: `test: SQS 메시지 폴링 테스트 정리 (Tidy)`

---

### 2️⃣5️⃣ SQS 연동 테스트 - Dead Letter Queue (Cycle 25)

#### 🔴 Red: 테스트 작성
- [ ] `shouldMoveToDlqAfterMaxRetries()` 작성
- [ ] 태스크 재시도 2회 초과 → DLQ 이동
- [ ] DLQ 메시지 확인
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: SQS DLQ 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] Redrive Policy 설정 (maxReceiveCount: 2)
- [ ] 메시지 수신 → 삭제 안 함 (3회 반복)
- [ ] DLQ 폴링
- [ ] DLQ 메시지 검증
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: SQS DLQ 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] DLQ 로직 명확화
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: SQS DLQ 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] DLQ Fixture 생성
- [ ] 커밋: `test: SQS DLQ 테스트 정리 (Tidy)`

---

### 2️⃣6️⃣ 동시성 테스트 - UserAgent 할당 (Cycle 26)

#### 🔴 Red: 테스트 작성
- [ ] `ConcurrencyTest.java` 생성
- [ ] `shouldAssignUserAgentConcurrentlyWithoutRaceCondition()` 작성
- [ ] 10개 스레드에서 동시 할당
- [ ] 할당된 UserAgent 중복 검증
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: UserAgent 할당 동시성 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] ExecutorService 생성 (10 스레드)
- [ ] CountDownLatch 사용
- [ ] ConcurrentHashMap으로 결과 수집
- [ ] Pessimistic Lock 검증
- [ ] 중복 없음 검증
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: UserAgent 할당 동시성 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 동시성 테스트 Helper 추출
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: UserAgent 할당 동시성 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Concurrency Test Fixture 생성
- [ ] 커밋: `test: UserAgent 할당 동시성 테스트 정리 (Tidy)`

---

### 2️⃣7️⃣ 동시성 테스트 - 크롤링 태스크 동시 처리 (Cycle 27)

#### 🔴 Red: 테스트 작성
- [ ] `shouldProcessTasksConcurrently()` 작성
- [ ] 5개 스레드에서 동시 처리
- [ ] 동일 태스크 중복 처리 방지 검증
- [ ] SQS Visibility Timeout 검증
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: 크롤링 태스크 동시 처리 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] ExecutorService 생성 (5 스레드)
- [ ] SQS 메시지 폴링 (VisibilityTimeout: 30초)
- [ ] ProcessCrawlerTaskUseCase 동시 실행
- [ ] 중복 처리 방지 검증
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: 크롤링 태스크 동시 처리 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 동시 처리 로직 명확화
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: 크롤링 태스크 동시 처리 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Concurrent Processing Fixture 생성
- [ ] 커밋: `test: 크롤링 태스크 동시 처리 테스트 정리 (Tidy)`

---

### 2️⃣8️⃣ 성능 테스트 - Bulk Insert 1,000개 태스크 (Cycle 28)

#### 🔴 Red: 테스트 작성
- [ ] `PerformanceTest.java` 생성
- [ ] `shouldBulkInsert1000TasksUnder5Seconds()` 작성
- [ ] TriggerCrawlingUseCase 실행
- [ ] totalProductCount: 500,000 (1,000 페이지)
- [ ] 시간 측정 (목표: < 5초)
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: Bulk Insert 성능 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] TriggerCrawlingCommand 생성
- [ ] Bulk Insert 실행
- [ ] System.currentTimeMillis() 시간 측정
- [ ] assertThat(duration).isLessThan(5000)
- [ ] 1,000개 태스크 생성 검증
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: Bulk Insert 성능 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 성능 측정 Helper 추출
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: Bulk Insert 성능 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Performance Test Fixture 생성
- [ ] 커밋: `test: Bulk Insert 성능 테스트 정리 (Tidy)`

---

### 2️⃣9️⃣ 성능 테스트 - 메트릭 집계 쿼리 10,000개 태스크 (Cycle 29)

#### 🔴 Red: 테스트 작성
- [ ] `shouldAggregateMetricsUnder1Second()` 작성
- [ ] @Sql로 10,000개 태스크 삽입
- [ ] GetCrawlingMetricsUseCase 실행
- [ ] 시간 측정 (목표: < 1초)
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: 메트릭 집계 쿼리 성능 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] GetCrawlingMetricsQuery 생성
- [ ] QueryDSL 집계 쿼리 실행
- [ ] System.currentTimeMillis() 시간 측정
- [ ] assertThat(duration).isLessThan(1000)
- [ ] 인덱스 활용 검증 (EXPLAIN)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: 메트릭 집계 쿼리 성능 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 쿼리 최적화
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: 메트릭 집계 쿼리 성능 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Metrics Query Fixture 생성
- [ ] 커밋: `test: 메트릭 집계 쿼리 성능 테스트 정리 (Tidy)`

---

### 3️⃣0️⃣ E2E 통합 시나리오 1 - 전체 플로우 (Cycle 30)

#### 🔴 Red: 테스트 작성
- [ ] `e2e_complete_flow_seller_to_product()` 작성
- [ ] 셀러 등록 → 크롤링 트리거 → 태스크 생성 → 발행 → 처리 → 상품 저장 (전체)
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: E2E 통합 시나리오 1 전체 플로우 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] Cycle 5-10 통합
- [ ] 전체 플로우 실행
- [ ] 각 단계 검증
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: E2E 통합 시나리오 1 전체 플로우 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 플로우 가독성 개선
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: E2E 통합 시나리오 1 전체 플로우 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] E2E Scenario 1 Fixture 정리
- [ ] 커밋: `test: E2E 통합 시나리오 1 전체 플로우 테스트 정리 (Tidy)`

---

### 3️⃣1️⃣ E2E 통합 시나리오 2 - 전체 플로우 (Cycle 31)

#### 🔴 Red: 테스트 작성
- [ ] `e2e_complete_flow_product_change_to_outbox()` 작성
- [ ] 기존 상품 → 재크롤링 → 변경 감지 → Outbox 생성 → 외부 전송 (전체)
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: E2E 통합 시나리오 2 전체 플로우 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] Cycle 11-14 통합
- [ ] 전체 플로우 실행
- [ ] 각 단계 검증
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: E2E 통합 시나리오 2 전체 플로우 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 플로우 가독성 개선
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: E2E 통합 시나리오 2 전체 플로우 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] E2E Scenario 2 Fixture 정리
- [ ] 커밋: `test: E2E 통합 시나리오 2 전체 플로우 테스트 정리 (Tidy)`

---

### 3️⃣2️⃣ E2E 통합 시나리오 3 - 전체 플로우 (Cycle 32)

#### 🔴 Red: 테스트 작성
- [ ] `e2e_complete_flow_useragent_429_recovery()` 작성
- [ ] UserAgent 할당 → 429 응답 → 일시 중지 → 재할당 → 자동 복구 (전체)
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: E2E 통합 시나리오 3 전체 플로우 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] Cycle 15-19 통합
- [ ] 전체 플로우 실행
- [ ] 각 단계 검증
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: E2E 통합 시나리오 3 전체 플로우 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 플로우 가독성 개선
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: E2E 통합 시나리오 3 전체 플로우 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] E2E Scenario 3 Fixture 정리
- [ ] 커밋: `test: E2E 통합 시나리오 3 전체 플로우 테스트 정리 (Tidy)`

---

### 3️⃣3️⃣ ArchUnit 테스트 - Integration Test 규칙 (Cycle 33)

#### 🔴 Red: 테스트 작성
- [ ] `IntegrationTestArchUnitTest.java` 생성
- [ ] `shouldUseTestRestTemplate()` 작성
- [ ] `shouldUseTestContainers()` 작성
- [ ] `shouldNotUseMockMvc()` 작성
- [ ] 테스트 실행 → 통과 확인 (이미 준수 중)
- [ ] 커밋: `test: Integration Test ArchUnit 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] ArchUnit 규칙 작성
- [ ] TestRestTemplate 사용 검증
- [ ] @Testcontainers 어노테이션 검증
- [ ] MockMvc 금지 검증
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: Integration Test ArchUnit 테스트 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 규칙 명확화
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: Integration Test ArchUnit 테스트 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] ArchUnit 테스트 정리
- [ ] 커밋: `test: Integration Test ArchUnit 테스트 정리 (Tidy)`

---

### 3️⃣4️⃣ Integration Test - 성공 시나리오 종합 (Cycle 34)

#### 🔴 Red: 테스트 작성
- [ ] `shouldPassAllSuccessScenarios()` 작성
- [ ] 모든 엔드포인트 성공 시나리오 검증
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: Integration Test 성공 시나리오 종합 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] 9개 REST API 엔드포인트 모두 성공 호출
- [ ] 200/201 응답 검증
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: Integration Test 성공 시나리오 종합 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 테스트 가독성 개선
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: Integration Test 성공 시나리오 종합 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Success Scenario Fixture 정리
- [ ] 커밋: `test: Integration Test 성공 시나리오 종합 테스트 정리 (Tidy)`

---

### 3️⃣5️⃣ Integration Test - 실패 시나리오 종합 (Cycle 35)

#### 🔴 Red: 테스트 작성
- [ ] `shouldHandleAllFailureScenarios()` 작성
- [ ] 400, 401, 404, 409, 500 에러 시나리오 검증
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: Integration Test 실패 시나리오 종합 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] 400: Validation 실패
- [ ] 401: JWT/API Key 없음
- [ ] 404: 셀러 없음
- [ ] 409: 중복 셀러 ID
- [ ] 500: 내부 서버 에러
- [ ] 각 에러 응답 검증
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `impl: Integration Test 실패 시나리오 종합 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 에러 시나리오 정리
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `refactor: Integration Test 실패 시나리오 종합 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Failure Scenario Fixture 정리
- [ ] 커밋: `test: Integration Test 실패 시나리오 종합 테스트 정리 (Tidy)`

---

### 3️⃣6️⃣ TestFixture 통합 정리 (Cycle 36-40)

#### Cycle 36: Domain TestFixture 통합
- [ ] Red → Green → Refactor → Tidy
- [ ] Domain 객체 Fixture 재사용성 개선

#### Cycle 37: DTO TestFixture 통합
- [ ] Red → Green → Refactor → Tidy
- [ ] Request/Response DTO Fixture 재사용성 개선

#### Cycle 38: Infrastructure TestFixture 통합
- [ ] Red → Green → Refactor → Tidy
- [ ] Localstack, WireMock Fixture 재사용성 개선

#### Cycle 39: E2E TestFixture 통합
- [ ] Red → Green → Refactor → Tidy
- [ ] E2E 시나리오 Fixture 재사용성 개선

#### Cycle 40: 전체 Integration Test 실행 및 검증
- [ ] Red → Green → Refactor → Tidy
- [ ] 모든 Integration Test 통과 확인
- [ ] 성능 목표 달성 확인 (Bulk Insert < 5초, 메트릭 < 1초)
- [ ] Zero-Tolerance 규칙 준수 확인

---

## ✅ 완료 조건

- [ ] 40개 TDD 사이클 모두 완료 (160개 체크박스 모두 ✅)
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
  - [ ] TestRestTemplate 사용 필수 (MockMvc 금지)
  - [ ] TestContainers 사용 (실제 MySQL, Localstack)
  - [ ] Flyway 마이그레이션 자동 실행
- [ ] ArchUnit 테스트 통과 (Integration Test 규칙)
- [ ] TestFixture 모두 정리 (Object Mother 패턴)
- [ ] 테스트 커버리지 > 80%

---

## 🔗 관련 문서

- Task: docs/prd/tasks/MUSTIT-005.md
- PRD: docs/prd/mustit-seller-crawler.md
- Integration Testing 규칙: docs/coding_convention/05-testing/integration-testing/

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
