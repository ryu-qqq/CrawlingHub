# CRAWL-001: Domain Layer 구현

**Epic**: Crawl Task Trigger 시스템
**Layer**: Domain Layer
**브랜치**: feature/CRAWL-001-domain
**Jira URL**: (sync-to-jira 후 추가)

---

## 📝 목적

CrawlTask 도메인의 핵심 비즈니스 규칙과 불변식을 구현한다.
Aggregate Root, Value Objects, 도메인 예외를 정의하여 순수 비즈니스 로직을 캡슐화한다.

---

## 🎯 요구사항

### Aggregate Root
- [ ] **CrawlTask** Aggregate 구현
  - 정적 팩토리 메서드: `forNew()`, `reconstitute()`
  - 상태 전환 메서드: `markAsPublished()`, `markAsRunning()`, `markAsSuccess()`, `markAsFailed()`
  - 재시도 메서드: `attemptRetry()`, `canRetry()`
  - 상태 확인 메서드: `isInProgress()`
  - Private 생성자 (정적 팩토리만 허용)

### Value Objects
- [ ] **CrawlTaskId** (식별자 VO)
  - Record 타입
  - Compact Constructor로 검증 (value > 0)
  - 정적 팩토리: `unassigned()`, `of()`
  - `isAssigned()` 메서드

- [ ] **CrawlTaskStatus** (상태 Enum)
  - 상태: WAITING, PUBLISHED, RUNNING, SUCCESS, FAILED, RETRY, TIMEOUT
  - description 필드
  - `isInProgress()`, `isTerminal()` 메서드

- [ ] **CrawlTaskType** (유형 Enum)
  - 유형: META, MINI_SHOP, DETAIL, OPTION
  - description 필드

- [ ] **CrawlEndpoint** (엔드포인트 VO)
  - Record 타입: baseUrl, path, queryParams
  - 정적 팩토리: `forMiniShopMeta()`, `forMiniShopList()`, `forProductDetail()`, `forProductOption()`
  - `toFullUrl()` 메서드
  - Immutable Map 사용 (Map.copyOf)

### 비즈니스 규칙
- [ ] WAITING 상태에서만 PUBLISHED로 전환 가능
- [ ] PUBLISHED 상태에서만 RUNNING으로 전환 가능
- [ ] RUNNING 상태에서만 SUCCESS/FAILED로 전환 가능
- [ ] FAILED/TIMEOUT 상태에서만 재시도 가능 (maxRetryCount 이내)
- [ ] 상태 전환 시 updatedAt 자동 갱신

### Domain Exception
- [ ] **CrawlTaskErrorCode** (ErrorCode 구현)
  - CRAWL_TASK_NOT_FOUND
  - INVALID_CRAWL_TASK_STATE
  - DUPLICATE_CRAWL_TASK
  - SCHEDULER_NOT_ACTIVE
  - MAX_RETRY_EXCEEDED

- [ ] **CrawlTaskNotFoundException**
- [ ] **InvalidCrawlTaskStateException**
- [ ] **DuplicateCrawlTaskException**

---

## ⚠️ 제약사항

### Zero-Tolerance 규칙
- [ ] **Lombok 금지** - Pure Java 사용
- [ ] **Law of Demeter 준수** - Getter 체이닝 금지
- [ ] **Setter 금지** - 비즈니스 메서드로만 상태 변경
- [ ] **기술 독립성** - JPA/Spring 어노테이션 없음
- [ ] **Long FK 전략** - crawlScheduleId, sellerId는 Long 타입

### 테스트 규칙
- [ ] ArchUnit 테스트 필수
- [ ] 모든 Value Object 테스트 (생성, 검증, 동등성)
- [ ] Aggregate 상태 전환 테스트 (정상/예외)
- [ ] TestFixture 사용 필수
- [ ] 테스트 커버리지 > 80%

---

## 📦 패키지 구조

```
domain/
└─ crawl/
   └─ task/
      ├─ aggregate/
      │  └─ CrawlTask.java              # Aggregate Root
      ├─ identifier/
      │  └─ CrawlTaskId.java            # Long Value Object (Auto Increment)
      ├─ vo/
      │  ├─ CrawlTaskStatus.java        # Enum VO
      │  ├─ CrawlTaskType.java          # Enum VO
      │  └─ CrawlEndpoint.java          # 크롤링 URL 정보
      └─ exception/
         ├─ CrawlTaskErrorCode.java
         ├─ CrawlTaskNotFoundException.java
         ├─ InvalidCrawlTaskStateException.java
         └─ DuplicateCrawlTaskException.java
```

---

## ✅ 완료 조건

- [ ] 모든 요구사항 구현 완료
- [ ] 모든 Unit 테스트 통과
- [ ] ArchUnit 테스트 통과
- [ ] Zero-Tolerance 규칙 준수 확인
- [ ] 코드 리뷰 승인
- [ ] PR 머지 완료

---

## 🔗 관련 문서

- PRD: [docs/prd/tasks/crawl-task-trigger.md](./crawl-task-trigger.md)
- Plan: docs/prd/plans/CRAWL-001-domain-plan.md (create-plan 후 생성)
- Domain Guide: [docs/coding_convention/02-domain-layer/domain-guide.md](../../coding_convention/02-domain-layer/domain-guide.md)
- Jira: (sync-to-jira 후 추가)

---

## 🧪 TDD 체크리스트

### CrawlTaskId 테스트
- [ ] `test: CrawlTaskId 양수 값으로 생성 성공`
- [ ] `test: CrawlTaskId 0 이하 값으로 생성 시 예외`
- [ ] `test: CrawlTaskId.unassigned()는 null 값 반환`
- [ ] `test: CrawlTaskId.of()로 기존 ID 생성`
- [ ] `test: CrawlTaskId.isAssigned() 동작 확인`

### CrawlTaskStatus 테스트
- [ ] `test: CrawlTaskStatus.isInProgress() 동작 확인`
- [ ] `test: CrawlTaskStatus.isTerminal() 동작 확인`

### CrawlTaskType 테스트
- [ ] `test: CrawlTaskType description 확인`

### CrawlEndpoint 테스트
- [ ] `test: CrawlEndpoint 생성 시 null 검증`
- [ ] `test: CrawlEndpoint.forMiniShopMeta() 정상 생성`
- [ ] `test: CrawlEndpoint.forMiniShopList() 정상 생성`
- [ ] `test: CrawlEndpoint.forProductDetail() 정상 생성`
- [ ] `test: CrawlEndpoint.forProductOption() 정상 생성`
- [ ] `test: CrawlEndpoint.toFullUrl() 쿼리 파라미터 포함`
- [ ] `test: CrawlEndpoint queryParams Immutable 검증`

### CrawlTask 테스트
- [ ] `test: CrawlTask.forNew() WAITING 상태로 생성`
- [ ] `test: CrawlTask.reconstitute() 기존 데이터 복원`
- [ ] `test: CrawlTask.markAsPublished() WAITING → PUBLISHED 전환`
- [ ] `test: CrawlTask.markAsPublished() 다른 상태에서 호출 시 예외`
- [ ] `test: CrawlTask.markAsRunning() PUBLISHED → RUNNING 전환`
- [ ] `test: CrawlTask.markAsRunning() 다른 상태에서 호출 시 예외`
- [ ] `test: CrawlTask.markAsSuccess() RUNNING → SUCCESS 전환`
- [ ] `test: CrawlTask.markAsFailed() RUNNING → FAILED 전환`
- [ ] `test: CrawlTask.attemptRetry() 재시도 횟수 증가`
- [ ] `test: CrawlTask.attemptRetry() maxRetryCount 초과 시 false 반환`
- [ ] `test: CrawlTask.canRetry() 조건 확인`
- [ ] `test: CrawlTask.isInProgress() 동작 확인`
