# EVENTBRIDGE-005: EventBridge Integration Test

**Epic**: 머스트잇 셀러 크롤링 스케줄러
**Bounded Context**: EventBridge (Crawling Scheduler)
**Layer**: Integration Test
**브랜치**: feature/EVENTBRIDGE-005-integration
**Jira URL**: (sync-to-jira 후 추가)

---

## 📝 목적

EventBridge 바운더리 컨텍스트의 E2E 시나리오 테스트 및 Outbox Pattern 통합 테스트를 수행합니다.

**핵심 테스트**:
- E2E 시나리오 (셀러 등록 → 스케줄 등록 → AWS Rule 생성 → 조회)
- Outbox Pattern 통합 테스트 (TransactionSynchronization + OutboxEventProcessor)
- AWS EventBridge Mock 연동 테스트

---

## 🎯 요구사항

### E2E 테스트 시나리오

#### 시나리오 1: 스케줄 등록 → AWS Rule 생성 → 조회

- [ ] **테스트 플로우**:
  1. POST /api/v1/sellers (셀러 등록)
     - Response: 201 Created, sellerId = 1
  2. POST /api/v1/sellers/1/schedulers (스케줄 등록)
     - Request: `{ "schedulerName": "daily-crawler", "cronExpression": "cron(0 0 * * ? *)" }`
     - Response: 201 Created, schedulerId = 1, status = PENDING
  3. Outbox Event 처리 (TransactionSynchronization)
     - AWS EventBridge Rule 생성 (Mock)
     - Outbox.status → PUBLISHED
     - Scheduler.status → ACTIVE
  4. GET /api/v1/schedulers/1 (스케줄 조회)
     - Response: 200 OK, status = ACTIVE, eventBridgeRuleName = "seller-daily-crawler-20250118120000"

- [ ] **검증 항목**:
  - Scheduler.status: PENDING → ACTIVE 전환
  - Outbox.status: PENDING → PUBLISHED 전환
  - AWS EventBridge Rule 생성 API 호출 검증 (Mock)

#### 시나리오 2: 스케줄 수정 → AWS Rule 업데이트

- [ ] **테스트 플로우**:
  1. 시나리오 1 선행 (스케줄 등록)
  2. PATCH /api/v1/schedulers/1 (Cron 변경)
     - Request: `{ "cronExpression": "cron(0 12 * * ? *)" }`
     - Response: 200 OK
  3. Outbox Event 처리
     - AWS EventBridge Rule 업데이트 (Mock)
     - Outbox.status → PUBLISHED
  4. GET /api/v1/schedulers/1/history (이력 조회)
     - Response: 200 OK, changedField = CRON_EXPRESSION, oldValue = "cron(0 0 * * ? *)", newValue = "cron(0 12 * * ? *)"

- [ ] **검증 항목**:
  - SchedulerHistory 기록 검증
  - AWS EventBridge Update Rule API 호출 검증

#### 시나리오 3: 셀러 비활성화 → 스케줄 일괄 비활성화

- [ ] **테스트 플로우**:
  1. POST /api/v1/sellers (셀러 등록)
  2. POST /api/v1/sellers/1/schedulers (스케줄 3개 등록)
     - daily-crawler
     - hourly-crawler
     - weekly-crawler
  3. Outbox Event 처리 (3개 스케줄 모두 ACTIVE)
  4. PATCH /api/v1/sellers/1/status (INACTIVE)
     - Response: 200 OK
  5. GET /api/v1/schedulers?sellerId=1 (스케줄 목록 조회)
     - Response: 200 OK, 3개 모두 status = INACTIVE

- [ ] **검증 항목**:
  - 모든 스케줄 INACTIVE 전환
  - AWS EventBridge Disable Rule API 호출 검증 (3회)

### Outbox Pattern 통합 테스트

#### TransactionSynchronization 테스트

- [ ] **테스트 케이스 1: 성공 시나리오**
  - Scheduler + Outbox 저장
  - TransactionSynchronization.afterCommit 트리거
  - AWS API 호출 성공 (Mock)
  - Outbox.status → PUBLISHED
  - Scheduler.status → ACTIVE

- [ ] **테스트 케이스 2: 실패 시나리오**
  - Scheduler + Outbox 저장
  - TransactionSynchronization.afterCommit 트리거
  - AWS API 호출 실패 (Mock Exception)
  - Outbox.status → FAILED
  - Scheduler.status → PENDING (그대로)

#### OutboxEventProcessor 테스트

- [ ] **테스트 케이스 1: PENDING 상태 Outbox 재처리**
  - Outbox Event 생성 (PENDING, retryCount = 0)
  - OutboxEventProcessor 실행 (매 1분)
  - AWS API 호출 성공 (Mock)
  - Outbox.status → PUBLISHED

- [ ] **테스트 케이스 2: FAILED 상태 Outbox 재시도**
  - Outbox Event 생성 (FAILED, retryCount = 1)
  - OutboxEventProcessor 실행
  - AWS API 호출 실패 (Mock)
  - Outbox.retryCount++

- [ ] **테스트 케이스 3: 최대 재시도 초과**
  - Outbox Event 생성 (FAILED, retryCount = 3)
  - OutboxEventProcessor 실행
  - Outbox.status → FAILED (영구)
  - Slack 알림 발송 검증 (Mock)

#### Exponential Backoff 테스트

- [ ] **테스트 케이스**: 재시도 간격 검증
  - 1차 재시도: 1분 후
  - 2차 재시도: 5분 후
  - 3차 재시도: 15분 후

### AWS EventBridge Mock 연동 테스트

#### EventBridgeClientAdapter 테스트

- [ ] **CreateRule API 테스트**
  - Request: ruleName, cronExpression, target
  - Mock 응답: 성공

- [ ] **UpdateRule API 테스트**
  - Request: ruleName, cronExpression
  - Mock 응답: 성공

- [ ] **DisableRule API 테스트**
  - Request: ruleName
  - Mock 응답: 성공

- [ ] **API 실패 시나리오 테스트**
  - Mock Exception 발생
  - Outbox.status → FAILED
  - Outbox.errorMessage 업데이트

### 테스트 환경 구성

#### TestContainers 설정

- [ ] **MySQL Container**
  - Flyway Migration 자동 실행
  - 테스트 데이터 초기화
  ```java
  @Container
  static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
      .withDatabaseName("test")
      .withUsername("test")
      .withPassword("test");
  ```

#### Flyway vs @Sql 역할 구분 (중요!)

- [ ] **Flyway: 스키마 생성 (DDL)**
  - 운영 환경과 동일한 스키마
  - `src/main/resources/db/migration/V*.sql`
  - `CREATE TABLE`, `ALTER TABLE` 등
  - 테스트 시작 시 자동 실행 (1회)

- [ ] **@Sql: 테스트 데이터 삽입 (DML)**
  - 테스트 전용 데이터
  - `src/test/resources/sql/*.sql`
  - `INSERT`, `UPDATE`, `DELETE` 만 포함
  - 각 테스트 메서드 실행 전 실행
  - **DDL 작성 절대 금지** (CREATE TABLE 금지)

#### Mock Server 설정

- [ ] **AWS EventBridge Mock**
  - WireMock 또는 MockServer 사용
  - CreateRule, UpdateRule, DisableRule API Mock

#### Slack Mock 설정

- [ ] **Slack Webhook Mock**
  - 알림 발송 검증

---

## ⚠️ 제약사항

### 테스트 규칙 (Zero-Tolerance)

- [ ] **@SpringBootTest(webEnvironment = RANDOM_PORT) 필수**
  - 전체 Spring 컨텍스트 로딩
  - 실제 HTTP 서버 시작

- [ ] **TestRestTemplate 필수 (E2E)**
  - 실제 HTTP 요청/응답 검증
  - 직렬화/역직렬화 검증
  - **MockMvc 절대 금지**

- [ ] **@Transactional + @Rollback(true) 필수**
  - 테스트 격리 (각 테스트는 독립적)
  - 데이터 자동 롤백

- [ ] **Flyway 스키마 생성 필수**
  - `spring.flyway.enabled=true`
  - 운영 환경과 동일한 스키마
  - 마이그레이션 파일 재사용

- [ ] **@Sql로 테스트 데이터 삽입 (INSERT만)**
  - DDL 작성 금지 (CREATE TABLE 금지)
  - DML만 포함 (INSERT, UPDATE, DELETE)

- [ ] **@ActiveProfiles("test") 필수**
  - 테스트 전용 설정 사용
  - application-test.yml 로드

- [ ] **@Testcontainers 필수**
  - 실제 DB 사용 (H2 금지)
  - TestContainers로 MySQL 8.0 시작

- [ ] **Mock Server 사용 (외부 API)**
  - AWS EventBridge API Mock
  - Slack Webhook Mock

- [ ] **@MockBean 남발 금지**
  - 실제 Bean 사용 (통합 테스트 목적)
  - 외부 API만 WireMock으로 모킹

- [ ] **EntityManager.persist() 직접 호출 금지**
  - @Sql 사용
  - 테스트 데이터는 SQL 파일로 관리

---

## ✅ 완료 조건

- [ ] E2E 테스트 시나리오 완료
  - 시나리오 1: 스케줄 등록 → 조회
  - 시나리오 2: 스케줄 수정 → 이력 조회
  - 시나리오 3: 셀러 비활성화 → 스케줄 일괄 비활성화

- [ ] Outbox Pattern 통합 테스트 완료
  - TransactionSynchronization 테스트
  - OutboxEventProcessor 테스트
  - Exponential Backoff 테스트

- [ ] AWS EventBridge Mock 연동 테스트 완료
  - CreateRule API 테스트
  - UpdateRule API 테스트
  - DisableRule API 테스트
  - API 실패 시나리오 테스트

- [ ] TestContainers 환경 구성 완료
  - MySQL Container
  - Flyway Migration

- [ ] Mock Server 구성 완료
  - AWS EventBridge Mock
  - Slack Webhook Mock

- [ ] 모든 테스트 통과 (100%)

- [ ] 코드 리뷰 승인

- [ ] PR 머지 완료

---

## 🔗 관련 문서

- **PRD**: `/Users/sangwon-ryu/crawlinghub/docs/prd/mustit-seller-crawler-scheduler.md`
- **Plan**: `docs/prd/seller/eventbridge/plans/EVENTBRIDGE-005-integration-plan.md`
- **Jira**: (sync-to-jira 후 추가)
- **코딩 규칙**: `docs/coding_convention/05-testing/integration-testing/`
- **선행 Task**: EVENTBRIDGE-001, EVENTBRIDGE-002, EVENTBRIDGE-003, EVENTBRIDGE-004

---

## 📋 다음 단계

1. `/create-plan EVENTBRIDGE-005` - TDD Plan 생성
2. `/kb/integration/go` - Integration Test 시작
