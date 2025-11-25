# CRAWL-002: Application Layer 구현

**Epic**: Crawl Task Trigger
**Layer**: Application Layer
**브랜치**: feature/CRAWL-002-application
**Jira URL**: (sync-to-jira 후 추가)

---

## 📝 목적

TriggerCrawlTaskUseCase를 구현하여 EventBridge 트리거를 처리하고, CrawlTask 생성 및 SQS 발행 로직을 담당합니다.

---

## 🎯 요구사항

### Command Use Cases

**TriggerCrawlTaskUseCase**:
- [ ] Input: `TriggerCrawlTaskCommand(crawlSchedulerId: Long)`
- [ ] Output: `CrawlTaskResponse(crawlTaskId, status, requestUrl, createdAt)`
- [ ] Transaction: 필수 (Scheduler 조회 + Task 저장 + OutBox 저장)

**비즈니스 로직**:
1. [ ] Scheduler 조회 (없으면 SchedulerNotFoundException)
2. [ ] Scheduler 상태 확인 (PAUSED/DISABLED → SchedulerNotActiveException)
3. [ ] 기존 WAITING/RUNNING Task 확인 (있으면 DuplicateTaskException)
4. [ ] CrawlTask 생성 (미니샵 메타데이터 URL 구성)
5. [ ] CrawlTaskOutBox 생성 (idempotencyKey, messagePayload)
6. [ ] 트랜잭션 커밋
7. [ ] afterCommit에서 SQS 발행 이벤트 발행

### Query Use Cases

**GetCrawlTaskUseCase**:
- [ ] Input: `GetCrawlTaskQuery(crawlTaskId: String)`
- [ ] Output: `CrawlTaskDetailResponse`
- [ ] Transaction: ReadOnly

**ListCrawlTasksBySchedulerUseCase**:
- [ ] Input: `ListCrawlTasksQuery(crawlSchedulerId, status, page, size)`
- [ ] Output: `PageResponse<CrawlTaskSummaryResponse>`
- [ ] Transaction: ReadOnly

### Port 정의

**Port In (Command)**:
- [ ] TriggerCrawlTaskUseCase: `trigger(TriggerCrawlTaskCommand): CrawlTaskResponse`

**Port In (Query)**:
- [ ] GetCrawlTaskUseCase: `get(GetCrawlTaskQuery): CrawlTaskDetailResponse`
- [ ] ListCrawlTasksUseCase: `list(ListCrawlTasksQuery): PageResponse`

**Port Out (Command)**:
- [ ] CrawlTaskPersistPort: `save(CrawlTask): CrawlTask`
- [ ] CrawlTaskOutBoxPersistPort: `save(CrawlTaskOutBox): CrawlTaskOutBox`
- [ ] SqsPublishPort: `publish(SqsMessage): void`

**Port Out (Query)**:
- [ ] CrawlTaskQueryPort: `findById(CrawlTaskId)`, `existsBySchedulerIdAndStatusIn()`
- [ ] CrawlTaskOutBoxQueryPort: `findPendingWithRetryLessThan()`

### DTO 정의

**Command DTO**:
- [ ] TriggerCrawlTaskCommand(crawlSchedulerId: Long)

**Query DTO**:
- [ ] GetCrawlTaskQuery(crawlTaskId: String)
- [ ] ListCrawlTasksQuery(crawlSchedulerId, status, page, size)

**Response DTO**:
- [ ] CrawlTaskResponse(crawlTaskId, status, requestUrl, createdAt)
- [ ] CrawlTaskDetailResponse(crawlTaskId, schedulerId, sellerId, requestUrl, status, retryCount, createdAt)
- [ ] CrawlTaskSummaryResponse(crawlTaskId, status, createdAt)

### Event Listener

**CrawlTaskCreatedEventListener**:
- [ ] TransactionSynchronization afterCommit에서 실행
- [ ] SQS 메시지 발행
- [ ] 성공 시 OutBox 상태 COMPLETED로 업데이트
- [ ] 실패 시 OutBox PENDING 유지 (Fallback Scheduler가 재시도)

### Scheduler

**CrawlTaskOutBoxRetryScheduler**:
- [ ] 주기: 1분마다 (@Scheduled)
- [ ] PENDING 상태이고 retryCount < maxRetry인 OutBox 조회
- [ ] SQS 재발행 시도
- [ ] 성공 시 COMPLETED, 실패 시 retryCount 증가
- [ ] maxRetry 초과 시 FAILED 처리

### Manager/Facade

**CrawlTaskManager**:
- [ ] Task 생성 로직 캡슐화
- [ ] URL 구성 로직 (`https://m.web.mustit.co.kr/...`)

**CrawlTaskOutBoxManager**:
- [ ] OutBox 생성 및 상태 관리
- [ ] messagePayload JSON 생성

---

## ⚠️ 제약사항

### Zero-Tolerance 규칙
- [ ] Command/Query 분리 (CQRS)
- [ ] **Transaction 경계 엄격 관리** - SQS 발행은 트랜잭션 밖
- [ ] @Transactional 내 외부 API 호출 금지
- [ ] Assembler 패턴 사용 (DTO ↔ Domain 변환)

### 테스트 규칙
- [ ] ArchUnit 테스트 필수
- [ ] Mock Port 사용 (Unit Test)
- [ ] 테스트 커버리지 > 80%

---

## ✅ 완료 조건

- [ ] TriggerCrawlTaskUseCase 구현 완료
- [ ] Query UseCase 구현 완료
- [ ] EventListener 구현 완료 (afterCommit)
- [ ] Fallback Scheduler 구현 완료
- [ ] 모든 Port 정의 완료
- [ ] Unit Test 통과 (UseCase, Manager)
- [ ] ArchUnit Test 통과
- [ ] Zero-Tolerance 규칙 준수
- [ ] 코드 리뷰 승인
- [ ] PR 머지 완료

---

## 🔗 관련 문서

- PRD: docs/prd/crawl-task-trigger.md
- Plan: docs/prd/plans/CRAWL-002-application-plan.md (create-plan 후 생성)
- Jira: (sync-to-jira 후 추가)
