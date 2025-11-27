# CRAWL-002: Application Layer 구현

**Epic**: Crawl Task Trigger 시스템
**Layer**: Application Layer
**브랜치**: feature/CRAWL-002-application
**의존성**: CRAWL-001 (Domain Layer) 완료 후 시작
**Jira URL**: (sync-to-jira 후 추가)

---

## 📝 목적

CrawlTask 도메인을 활용한 UseCase와 비즈니스 흐름을 구현한다.
Port 인터페이스를 정의하고, CQRS 패턴에 따라 Command/Query를 분리한다.
Transaction 경계를 명확히 하여 SQS 발행을 afterCommit에서 처리한다.

---

## 🎯 요구사항

### Port-In (Command)
- [ ] **TriggerCrawlTaskUseCase**
  - `CrawlTaskResponse trigger(TriggerCrawlTaskCommand command)`
  - EventBridge에서 호출되어 CrawlTask 생성 및 SQS 발행

### Port-In (Query)
- [ ] **GetCrawlTaskUseCase**
  - `CrawlTaskDetailResponse get(GetCrawlTaskQuery query)`
  - Task ID로 단건 조회

- [ ] **ListCrawlTasksUseCase**
  - `PageResponse<CrawlTaskResponse> list(ListCrawlTasksQuery query)`
  - Schedule ID로 목록 조회 (페이징)

### Port-Out (Command)
- [ ] **CrawlTaskPersistencePort**
  - `CrawlTask persist(CrawlTask crawlTask)`
  - Task 저장

### Port-Out (Query)
- [ ] **CrawlTaskQueryPort**
  - `Optional<CrawlTask> findById(CrawlTaskId crawlTaskId)`
  - `boolean existsByScheduleIdAndStatusIn(Long crawlScheduleId, List<CrawlTaskStatus> statuses)`
  - `Page<CrawlTask> findByScheduleId(Long crawlScheduleId, CrawlTaskStatus status, Pageable pageable)`

### Port-Out (Messaging)
- [ ] **CrawlTaskMessagePort**
  - `void publish(CrawlTask crawlTask, String idempotencyKey)`
  - SQS 메시지 발행

### DTO (Command)
- [ ] **TriggerCrawlTaskCommand**
  - Record 타입
  - crawlScheduleId (NotNull 검증)

### DTO (Query)
- [ ] **GetCrawlTaskQuery**
  - Record 타입
  - crawlTaskId (NotNull 검증)

- [ ] **ListCrawlTasksQuery**
  - Record 타입
  - crawlScheduleId, status (optional), pageable

### DTO (Response)
- [ ] **CrawlTaskResponse**
  - Record 타입
  - crawlTaskId, crawlScheduleId, sellerId, requestUrl, status, taskType, retryCount, createdAt

- [ ] **CrawlTaskDetailResponse**
  - Record 타입
  - CrawlTaskResponse + updatedAt, endpoint 상세 정보

### Service
- [ ] **TriggerCrawlTaskService**
  - TriggerCrawlTaskUseCase 구현
  - TransactionManager 위임
  - Assembler로 응답 변환

- [ ] **GetCrawlTaskService**
  - GetCrawlTaskUseCase 구현
  - QueryPort 사용
  - NotFoundException 처리

- [ ] **ListCrawlTasksService**
  - ListCrawlTasksUseCase 구현
  - QueryPort 사용
  - 페이징 처리

### TransactionManager
- [ ] **CrawlTaskTransactionManager**
  - `@Transactional` 메서드에서 Task 생성/저장
  - Schedule 상태 검증 (CrawlScheduleQueryPort 필요)
  - 중복 Task 검증
  - afterCommit에서 SQS 발행 등록
  - Idempotency Key 생성

### Assembler
- [ ] **CrawlTaskAssembler**
  - Domain → Response DTO 변환
  - `CrawlTaskResponse toResponse(CrawlTask)`
  - `CrawlTaskDetailResponse toDetailResponse(CrawlTask)`

---

## ⚠️ 제약사항

### Zero-Tolerance 규칙
- [ ] **Lombok 금지** - Pure Java 사용
- [ ] **CQRS 분리** - Command/Query 패키지 분리
- [ ] **Transaction 경계** - SQS 발행은 afterCommit에서 수행
- [ ] **DTO Record** - 모든 DTO는 Record 타입
- [ ] **Domain 직접 노출 금지** - Assembler로 변환

### Transaction 경계 (Critical)
- [ ] `@Transactional` 내에서 외부 API 호출 금지
- [ ] SQS 발행은 `TransactionSynchronizationManager.registerSynchronization()` 사용
- [ ] 발행 실패 시 로그만 남기고 Fallback Scheduler가 재시도

### 테스트 규칙
- [ ] ArchUnit 테스트 필수
- [ ] Service Unit 테스트 (Mock 사용)
- [ ] TransactionManager 테스트 (검증 로직)
- [ ] Assembler 테스트
- [ ] TestFixture 사용 필수
- [ ] 테스트 커버리지 > 80%

---

## 📦 패키지 구조

```
application/
└─ crawl/
   └─ task/
      ├─ assembler/
      │  └─ CrawlTaskAssembler.java
      ├─ dto/
      │  ├─ command/
      │  │   └─ TriggerCrawlTaskCommand.java
      │  ├─ query/
      │  │   ├─ GetCrawlTaskQuery.java
      │  │   └─ ListCrawlTasksQuery.java
      │  └─ response/
      │      ├─ CrawlTaskResponse.java
      │      └─ CrawlTaskDetailResponse.java
      ├─ manager/
      │  └─ CrawlTaskTransactionManager.java
      ├─ port/
      │  ├─ in/
      │  │   ├─ command/
      │  │   │   └─ TriggerCrawlTaskUseCase.java
      │  │   └─ query/
      │  │       ├─ GetCrawlTaskUseCase.java
      │  │       └─ ListCrawlTasksUseCase.java
      │  └─ out/
      │      ├─ command/
      │      │   └─ CrawlTaskPersistencePort.java
      │      ├─ query/
      │      │   └─ CrawlTaskQueryPort.java
      │      └─ messaging/
      │          └─ CrawlTaskMessagePort.java
      └─ service/
         ├─ command/
         │   └─ TriggerCrawlTaskService.java
         └─ query/
             ├─ GetCrawlTaskService.java
             └─ ListCrawlTasksService.java
```

---

## ✅ 완료 조건

- [ ] 모든 요구사항 구현 완료
- [ ] 모든 Unit 테스트 통과
- [ ] ArchUnit 테스트 통과
- [ ] Zero-Tolerance 규칙 준수 확인
- [ ] Transaction 경계 검증 완료
- [ ] 코드 리뷰 승인
- [ ] PR 머지 완료

---

## 🔗 관련 문서

- PRD: [docs/prd/tasks/crawl-task-trigger.md](./crawl-task-trigger.md)
- Plan: docs/prd/plans/CRAWL-002-application-plan.md (create-plan 후 생성)
- Application Guide: [docs/coding_convention/03-application-layer/application-guide.md](../../coding_convention/03-application-layer/application-guide.md)
- Jira: (sync-to-jira 후 추가)

---

## 🧪 TDD 체크리스트

### Port-In 테스트
- [ ] `test: TriggerCrawlTaskUseCase 인터페이스 정의`
- [ ] `test: GetCrawlTaskUseCase 인터페이스 정의`
- [ ] `test: ListCrawlTasksUseCase 인터페이스 정의`

### Port-Out 테스트
- [ ] `test: CrawlTaskPersistencePort 인터페이스 정의`
- [ ] `test: CrawlTaskQueryPort 인터페이스 정의`
- [ ] `test: CrawlTaskMessagePort 인터페이스 정의`

### DTO 테스트
- [ ] `test: TriggerCrawlTaskCommand null 검증`
- [ ] `test: GetCrawlTaskQuery null 검증`
- [ ] `test: CrawlTaskResponse Record 불변성`
- [ ] `test: CrawlTaskDetailResponse Record 불변성`

### Service 테스트
- [ ] `test: TriggerCrawlTaskService 정상 트리거`
- [ ] `test: GetCrawlTaskService 정상 조회`
- [ ] `test: GetCrawlTaskService 존재하지 않는 ID 예외`
- [ ] `test: ListCrawlTasksService 페이징 조회`

### TransactionManager 테스트
- [ ] `test: Schedule 조회 및 상태 검증`
- [ ] `test: 중복 Task 존재 시 예외`
- [ ] `test: Task 생성 및 저장`
- [ ] `test: afterCommit에서 SQS 발행 등록`
- [ ] `test: Idempotency Key 생성`

### Assembler 테스트
- [ ] `test: Domain → CrawlTaskResponse 변환`
- [ ] `test: Domain → CrawlTaskDetailResponse 변환`
