# CRAWL-005: Integration Test 구현

**Epic**: Crawl Task Trigger 시스템
**Layer**: Integration Test
**브랜치**: feature/CRAWL-005-integration
**의존성**: CRAWL-001 ~ CRAWL-004 모두 완료 후 시작
**Jira URL**: (sync-to-jira 후 추가)

---

## 📝 목적

CrawlTask Trigger 시스템의 전체 흐름을 검증하는 E2E 통합 테스트를 구현한다.
실제 데이터베이스와 메시지 큐를 사용하여 전체 파이프라인을 검증한다.
TestRestTemplate을 사용하여 실제 HTTP 요청/응답을 테스트한다.

---

## 🎯 요구사항

### E2E 시나리오

#### Happy Path
- [ ] **정상 트리거 흐름**
  1. Schedule이 ACTIVE 상태인 경우
  2. POST /api/v1/crawl/tasks/trigger 호출
  3. CrawlTask가 WAITING 상태로 생성됨
  4. SQS 메시지 발행됨 (afterCommit)
  5. 201 Created 응답

- [ ] **Task 조회 흐름**
  1. Task가 존재하는 경우
  2. GET /api/v1/crawl/tasks/{id} 호출
  3. 200 OK + Task 정보 반환

- [ ] **Task 목록 조회 흐름**
  1. Schedule ID로 여러 Task 존재
  2. GET /api/v1/crawl/tasks?crawlScheduleId=X 호출
  3. 200 OK + 페이징된 목록 반환

#### Error Cases
- [ ] **Schedule 비활성 시 실패**
  1. Schedule이 INACTIVE 상태
  2. POST /api/v1/crawl/tasks/trigger 호출
  3. 409 Conflict + SCHEDULER_NOT_ACTIVE

- [ ] **중복 Task 존재 시 실패**
  1. 이미 WAITING/RUNNING Task 존재
  2. POST /api/v1/crawl/tasks/trigger 호출
  3. 409 Conflict + DUPLICATE_CRAWL_TASK

- [ ] **존재하지 않는 Task 조회 시 실패**
  1. 존재하지 않는 Task ID
  2. GET /api/v1/crawl/tasks/{id} 호출
  3. 404 Not Found + CRAWL_TASK_NOT_FOUND

### 테스트 환경 설정
- [ ] **TestContainers 설정**
  - MySQL Container
  - LocalStack (SQS) Container

- [ ] **Flyway 마이그레이션**
  - 테스트 전 스키마 생성
  - 테스트 후 롤백

- [ ] **@Sql 데이터 준비**
  - Schedule 데이터 준비
  - Task 데이터 준비 (필요 시)

### 검증 포인트
- [ ] **데이터베이스 상태 검증**
  - Task 저장 확인
  - 상태 전환 확인
  - 시간 필드 갱신 확인

- [ ] **SQS 메시지 검증**
  - 메시지 발행 확인
  - 메시지 내용 검증
  - Idempotency Key 확인

- [ ] **응답 검증**
  - HTTP Status Code
  - Response Body 구조
  - Error Response 구조

---

## ⚠️ 제약사항

### 테스트 규칙
- [ ] **TestRestTemplate 필수** - MockMvc 금지 (실제 HTTP 테스트)
- [ ] **Flyway vs @Sql 분리** - 스키마는 Flyway, 데이터는 @Sql
- [ ] **TestContainers 사용** - 실제 MySQL, LocalStack
- [ ] **트랜잭션 롤백** - 각 테스트 후 데이터 정리
- [ ] **독립성 보장** - 테스트 간 상태 공유 금지

### Zero-Tolerance 규칙
- [ ] **Lombok 금지** - Pure Java 사용
- [ ] **TestFixture 사용 필수**

---

## 📦 패키지 구조

```
application/src/test/java/
└─ com/company/template/
   └─ crawl/
      └─ task/
         └─ integration/
            ├─ CrawlTaskTriggerIntegrationTest.java
            ├─ CrawlTaskQueryIntegrationTest.java
            └─ fixture/
               ├─ CrawlScheduleFixture.java
               └─ CrawlTaskFixture.java
```

---

## 📋 테스트 데이터

### SQL 데이터 준비 (resources/sql/)

**crawl-schedule-setup.sql**:
```sql
INSERT INTO crawl_schedules (id, seller_id, status, cron_expression, created_at, updated_at)
VALUES
    (1, 100, 'ACTIVE', '0 0 * * * ?', NOW(), NOW()),
    (2, 200, 'INACTIVE', '0 0 * * * ?', NOW(), NOW());
```

**crawl-task-setup.sql**:
```sql
INSERT INTO crawl_tasks (id, crawl_schedule_id, seller_id, base_url, path, status, task_type, retry_count, created_at, updated_at)
VALUES
    (1, 1, 100, 'https://m.web.mustit.co.kr', '/api/...', 'SUCCESS', 'META', 0, NOW(), NOW());
```

---

## ✅ 완료 조건

- [ ] 모든 E2E 시나리오 테스트 통과
- [ ] Happy Path 테스트 완료
- [ ] Error Case 테스트 완료
- [ ] TestContainers 환경 구성 완료
- [ ] 테스트 데이터 준비 완료
- [ ] 코드 리뷰 승인
- [ ] PR 머지 완료

---

## 🔗 관련 문서

- PRD: [docs/prd/tasks/crawl-task-trigger.md](./crawl-task-trigger.md)
- Plan: docs/prd/plans/CRAWL-005-integration-plan.md (create-plan 후 생성)
- Integration Testing Guide: [docs/coding_convention/05-testing/integration-testing/01_integration-testing-overview.md](../../coding_convention/05-testing/integration-testing/01_integration-testing-overview.md)
- Jira: (sync-to-jira 후 추가)

---

## 🧪 TDD 체크리스트

### Happy Path 테스트
- [ ] `test: 정상 트리거 시 201 반환 및 Task 생성`
- [ ] `test: 트리거 후 SQS 메시지 발행 확인`
- [ ] `test: Task 단건 조회 시 200 반환`
- [ ] `test: Task 목록 조회 시 페이징 동작`
- [ ] `test: Task 목록 조회 시 status 필터링 동작`

### Error Case 테스트
- [ ] `test: Schedule 비활성 시 409 반환`
- [ ] `test: 중복 Task 존재 시 409 반환`
- [ ] `test: 존재하지 않는 Schedule ID 시 404 반환`
- [ ] `test: 존재하지 않는 Task ID 조회 시 404 반환`

### 상태 전환 테스트
- [ ] `test: WAITING → PUBLISHED 전환 확인`
- [ ] `test: 상태 전환 시 updatedAt 갱신 확인`

### 트랜잭션 테스트
- [ ] `test: 예외 발생 시 롤백 확인`
- [ ] `test: SQS 발행 실패 시 Task 상태 유지`

### 데이터 무결성 테스트
- [ ] `test: 동시 트리거 시 중복 방지`
- [ ] `test: Idempotency Key로 중복 발행 방지`
