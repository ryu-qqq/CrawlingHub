# CRAWL-005: Integration Test

**Epic**: Crawl Task Trigger
**Layer**: Integration Test
**브랜치**: feature/CRAWL-005-integration
**Jira URL**: (sync-to-jira 후 추가)

---

## 📝 목적

전체 파이프라인 (EventBridge → API → DB → SQS)의 E2E 테스트를 구현하여 시스템 통합을 검증합니다.

---

## 🎯 요구사항

### E2E 시나리오

**Happy Path**:
- [ ] 트리거 → Task 생성 → OutBox 생성 → SQS 발행 전체 플로우
  1. Scheduler 데이터 준비 (ACTIVE 상태)
  2. POST /api/v1/crawl/trigger 호출
  3. 201 Created 응답 확인
  4. DB에 CrawlTask 생성 확인 (WAITING → PUBLISHED)
  5. DB에 CrawlTaskOutBox 생성 확인 (PENDING → COMPLETED)
  6. SQS 메시지 발행 확인 (LocalStack 또는 Mock)

**Scheduler 상태 검증**:
- [ ] PAUSED Scheduler → 409 SCHEDULER_NOT_ACTIVE
- [ ] DISABLED Scheduler → 409 SCHEDULER_NOT_ACTIVE
- [ ] 존재하지 않는 Scheduler → 404 SCHEDULER_NOT_FOUND

**중복 트리거 방지**:
- [ ] 기존 WAITING Task 존재 시 → 409 DUPLICATE_TASK_EXISTS
- [ ] 기존 RUNNING Task 존재 시 → 409 DUPLICATE_TASK_EXISTS
- [ ] 기존 SUCCESS Task 존재 시 → 새 Task 생성 (정상)

**Fallback Scheduler 검증**:
- [ ] SQS 발행 실패 시 OutBox PENDING 유지
- [ ] Fallback Scheduler 실행 시 재발행 시도
- [ ] maxRetry 초과 시 FAILED 처리

**동시성 테스트**:
- [ ] 동일 Scheduler에 동시 트리거 10건 → 1건만 성공

### 테스트 환경 설정

**TestContainers**:
- [ ] MySQL Container
- [ ] LocalStack Container (SQS)

**테스트 데이터 준비**:
- [ ] Flyway 마이그레이션 실행
- [ ] @Sql 또는 TestFixture로 데이터 준비

**테스트 클래스 구조**:
```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers
class CrawlTriggerIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = ...;

    @Container
    static LocalStackContainer localstack = ...;

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void shouldTriggerCrawlTask_whenSchedulerIsActive() { ... }

    @Test
    void shouldReturn409_whenSchedulerIsPaused() { ... }

    @Test
    void shouldPreventDuplicateTrigger() { ... }
}
```

### SQS 검증

**LocalStack SQS**:
- [ ] 테스트용 SQS Queue 생성
- [ ] 메시지 발행 후 수신 확인
- [ ] messagePayload 내용 검증 (idempotencyKey, taskId, requestUrl)

### 성능 테스트 (Optional)

- [ ] 트리거 응답 시간 < 200ms (P95)
- [ ] 동시 100건 트리거 처리

---

## ⚠️ 제약사항

### 테스트 규칙
- [ ] TestRestTemplate 사용 (MockMvc 금지)
- [ ] 실제 DB 사용 (TestContainers)
- [ ] @Sql 또는 Flyway로 데이터 준비
- [ ] 테스트 간 격리 보장 (@Transactional 또는 @Sql cleanup)

### Zero-Tolerance 규칙
- [ ] 통합 테스트에서 Mock 최소화
- [ ] 실제 인프라 시뮬레이션 (LocalStack)

---

## ✅ 완료 조건

- [ ] Happy Path E2E 테스트 통과
- [ ] 에러 시나리오 테스트 통과
- [ ] 중복 트리거 방지 테스트 통과
- [ ] Fallback Scheduler 테스트 통과
- [ ] 동시성 테스트 통과
- [ ] SQS 메시지 검증 통과
- [ ] 테스트 환경 설정 완료 (TestContainers)
- [ ] 코드 리뷰 승인
- [ ] PR 머지 완료

---

## 🔗 관련 문서

- PRD: docs/prd/crawl-task-trigger.md
- Plan: docs/prd/plans/CRAWL-005-integration-plan.md (create-plan 후 생성)
- Jira: (sync-to-jira 후 추가)
