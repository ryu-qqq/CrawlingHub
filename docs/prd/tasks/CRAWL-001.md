# CRAWL-001: Domain Layer 구현

**Epic**: Crawl Task Trigger
**Layer**: Domain Layer
**브랜치**: feature/CRAWL-001-domain
**Jira URL**: (sync-to-jira 후 추가)

---

## 📝 목적

CrawlTask 및 CrawlTaskOutBox Aggregate를 구현하여 크롤링 작업의 핵심 도메인 모델을 정의합니다.

---

## 🎯 요구사항

### Aggregate Root: CrawlTask

**속성**:
- [ ] crawlTaskId: CrawlTaskId (Value Object, UUID)
- [ ] crawlSchedulerId: Long (FK)
- [ ] sellerId: Long (FK)
- [ ] requestUrl: String (크롤링 대상 URL)
- [ ] status: CrawlTaskStatus (Enum)
- [ ] retryCount: Integer (재시도 횟수, Default 0)
- [ ] createdAt: LocalDateTime
- [ ] updatedAt: LocalDateTime

**비즈니스 규칙**:
- [ ] Task 생성 시 status는 WAITING으로 시작
- [ ] retryCount는 0부터 시작
- [ ] requestUrl은 null/empty 불가

**상태 전환 메서드**:
- [ ] publish(): WAITING → PUBLISHED
- [ ] start(): PUBLISHED → RUNNING
- [ ] complete(): RUNNING → SUCCESS
- [ ] fail(): RUNNING → FAILED
- [ ] retry(): FAILED/TIMEOUT → RETRY
- [ ] timeout(): RUNNING → TIMEOUT

### Aggregate Root: CrawlTaskOutBox

**속성**:
- [ ] crawlTaskOutBoxId: CrawlTaskOutBoxId (Value Object, UUID)
- [ ] crawlTaskId: CrawlTaskId (FK)
- [ ] idempotencyKey: String (멱등성 키)
- [ ] messagePayload: String (JSON)
- [ ] status: CrawlTaskOutBoxStatus (Enum)
- [ ] retryCount: Integer (Default 0)
- [ ] createdAt: LocalDateTime
- [ ] processedAt: LocalDateTime (Nullable)

**비즈니스 규칙**:
- [ ] 생성 시 status는 PENDING으로 시작
- [ ] idempotencyKey 형식: `{crawlTaskId}_{timestamp}`

**상태 전환 메서드**:
- [ ] complete(): PENDING → COMPLETED (processedAt 기록)
- [ ] fail(): PENDING → FAILED (maxRetry 초과 시)
- [ ] incrementRetry(): retryCount 증가

### Value Objects

**CrawlTaskId**:
- [ ] UUID 기반 식별자
- [ ] 생성 팩토리 메서드: `CrawlTaskId.create()`
- [ ] 문자열 변환: `value()` 메서드

**CrawlTaskOutBoxId**:
- [ ] UUID 기반 식별자
- [ ] 생성 팩토리 메서드: `CrawlTaskOutBoxId.create()`

**CrawlTaskStatus** (Enum):
- [ ] WAITING: 대기 중
- [ ] PUBLISHED: SQS 발행됨
- [ ] RUNNING: 실행 중
- [ ] SUCCESS: 성공
- [ ] FAILED: 실패
- [ ] RETRY: 재시도 대기
- [ ] TIMEOUT: 타임아웃

**CrawlTaskOutBoxStatus** (Enum):
- [ ] PENDING: 발행 대기
- [ ] COMPLETED: 발행 완료
- [ ] FAILED: 발행 실패

### Domain Exception

**CrawlTaskException**:
- [ ] CrawlTaskNotFoundException: Task 조회 실패
- [ ] InvalidTaskStatusTransitionException: 잘못된 상태 전환

**CrawlTaskErrorCode** (Enum):
- [ ] CRAWL_TASK_NOT_FOUND
- [ ] INVALID_TASK_STATUS_TRANSITION
- [ ] INVALID_REQUEST_URL

---

## ⚠️ 제약사항

### Zero-Tolerance 규칙
- [ ] Lombok 금지 - Pure Java/Record 사용
- [ ] Law of Demeter 준수 - Getter 체이닝 금지
- [ ] Tell Don't Ask 패턴 - 상태 변경은 메서드로

### 테스트 규칙
- [ ] ArchUnit 테스트 필수 (AggregateRootArchTest)
- [ ] TestFixture 패턴 사용
- [ ] 테스트 커버리지 > 80%

---

## ✅ 완료 조건

- [ ] CrawlTask Aggregate 구현 완료
- [ ] CrawlTaskOutBox Aggregate 구현 완료
- [ ] 모든 Value Objects 구현 완료
- [ ] Domain Exception 구현 완료
- [ ] Unit Test 통과 (상태 전환, 비즈니스 규칙)
- [ ] ArchUnit Test 통과
- [ ] Zero-Tolerance 규칙 준수
- [ ] 코드 리뷰 승인
- [ ] PR 머지 완료

---

## 🔗 관련 문서

- PRD: docs/prd/crawl-task-trigger.md
- Plan: docs/prd/plans/CRAWL-001-domain-plan.md (create-plan 후 생성)
- Jira: (sync-to-jira 후 추가)
