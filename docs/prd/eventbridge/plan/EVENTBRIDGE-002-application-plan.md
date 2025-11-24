# EVENTBRIDGE-002 TDD Plan

**Task**: EventBridge Application Layer 구현
**Layer**: Application
**브랜치**: feature/EVENTBRIDGE-002-application
**예상 소요 시간**: 300분 (20 사이클 × 15분)

---

## 📝 TDD 사이클 체크리스트

### 1️⃣ Command/Query DTO 구현 (Cycle 1)

#### 🔴 Red: 테스트 작성
- [x] `RegisterSchedulerCommandTest.java` 파일 생성
- [x] `shouldCreateCommandWithValidData()` 작성
- [x] `GetSchedulerQueryTest.java` 파일 생성
- [x] Record 타입 검증 테스트 작성
- [x] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: Command/Query DTO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `RegisterSchedulerCommand.java` Record 생성
- [x] `UpdateSchedulerCommand.java` Record 생성
- [x] `DeactivateSchedulerCommand.java` Record 생성
- [x] `GetSchedulerQuery.java` Record 생성
- [x] `ListSchedulersQuery.java` Record 생성
- [x] `GetSchedulerHistoryQuery.java` Record 생성
- [x] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: Command/Query DTO 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [x] DTO Record ArchUnit 테스트 추가 및 통과
- [ ] 검증 로직 추가 (필요 시)
- [ ] 커밋: `struct: Command/Query DTO 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [x] `RegisterSchedulerCommandFixture.java` 생성
- [x] `GetSchedulerQueryFixture.java` 생성
- [x] 테스트 → Fixture 사용
- [ ] 커밋: `test: Command/Query DTO Fixture 정리 (Tidy)`

---

### 2️⃣ Response DTO 구현 (Cycle 2)

#### 🔴 Red: 테스트 작성
- [x] `SchedulerResponseTest.java` 파일 생성
- [x] `SchedulerDetailResponseTest.java` 파일 생성
- [x] `SchedulerSummaryResponseTest.java` 파일 생성
- [x] `SchedulerHistoryResponseTest.java` 파일 생성
- [x] Record 변환 테스트 작성
- [x] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: Response DTO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `SchedulerResponse.java` Record 생성
- [x] `SchedulerDetailResponse.java` Record 생성
- [x] `SchedulerSummaryResponse.java` Record 생성
- [x] `SchedulerHistoryResponse.java` Record 생성
- [x] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: Response DTO 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] Response DTO ArchUnit 테스트 통과
- [ ] 커밋: `struct: Response DTO 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [x] `SchedulerResponseFixture.java` 생성
- [x] 커밋: `test: Response DTO Fixture 정리 (Tidy)`

---

### 3️⃣ Port 인터페이스 정의 - Command Ports (Cycle 3)

#### 🔴 Red: 테스트 작성
- [x] `SchedulerCommandPortTest.java` 파일 생성
- [x] Port 인터페이스 시그니처 검증 테스트
- [x] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: Command Port 인터페이스 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `SchedulerCommandPort.java` 인터페이스 생성
  - `Scheduler save(Scheduler scheduler)`
  - `void delete(Long schedulerId)`
- [x] `OutboxEventCommandPort.java` 인터페이스 생성
  - `OutboxEvent save(OutboxEvent event)`
  - `void deleteByStatusAndCreatedAtBefore(...)`
- [x] `SchedulerHistoryCommandPort.java` 인터페이스 생성
  - `SchedulerHistory save(SchedulerHistory history)`
- [x] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: Command Port 인터페이스 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] Port-Out Command ArchUnit 테스트 추가 및 통과
- [ ] 네이밍 규칙 확인 (`*CommandPort`)
- [ ] 커밋: `struct: Command Port 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Port Mock Fixture 정리
- [ ] 커밋: `test: Command Port Fixture 정리 (Tidy)`

---

### 4️⃣ Port 인터페이스 정의 - Query Ports (Cycle 4)

#### 🔴 Red: 테스트 작성
- [x] `SchedulerQueryPortTest.java` 파일 생성
- [x] Query Port 시그니처 검증
- [x] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: Query Port 인터페이스 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `SchedulerQueryPort.java` 인터페이스 생성
  - `Optional<Scheduler> findById(Long schedulerId)`
  - `Optional<Scheduler> findBySellerIdAndSchedulerName(...)`
  - `List<Scheduler> findBySellerIdAndStatus(...)`
  - `Page<Scheduler> findAllBySellerIdAndStatus(...)`
  - `int countActiveSchedulersBySellerId(Long sellerId)`
- [x] `OutboxEventQueryPort.java` 인터페이스 생성
- [x] `SchedulerHistoryQueryPort.java` 인터페이스 생성
- [x] `SellerQueryPort.java` 인터페이스 생성
- [x] 커밋: `feat: Query Port 인터페이스 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] Port-Out Query ArchUnit 테스트 통과
- [ ] 네이밍 규칙 확인 (`*QueryPort`)
- [ ] 커밋: `struct: Query Port 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Query Port Mock Fixture 정리
- [ ] 커밋: `test: Query Port Fixture 정리 (Tidy)`

---

### 5️⃣ EventBridgeClientPort 정의 (Cycle 5)

#### 🔴 Red: 테스트 작성
- [x] `EventBridgeClientPortTest.java` 파일 생성
- [x] 외부 API Port 시그니처 검증
- [x] 커밋: `test: EventBridgeClientPort 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `EventBridgeClientPort.java` 인터페이스 생성
  - `void createRule(String ruleName, String cronExpression, String target)`
  - `void updateRule(String ruleName, String cronExpression)`
  - `void disableRule(String ruleName)`
- [x] 커밋: `feat: EventBridgeClientPort 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] Port ArchUnit 테스트 통과
- [ ] 커밋: `struct: EventBridgeClientPort 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] EventBridgeClient Mock Fixture 정리
- [ ] 커밋: `test: EventBridgeClientPort Fixture 정리 (Tidy)`

---

### 6️⃣ SchedulerAssembler 구현 (Cycle 6)

#### 🔴 Red: 테스트 작성
- [x] `SchedulerAssemblerTest.java` 파일 생성
- [x] `shouldAssembleDomainToResponse()` 작성
- [x] `shouldAssembleCommandToDomain()` 작성
- [x] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: SchedulerAssembler 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `SchedulerAssembler.java` 파일 생성 (@Component)
- [x] `toResponse(Scheduler)` 메서드 구현
- [x] `toDetailResponse(Scheduler)` 메서드 구현
- [x] `toSummaryResponse(Scheduler)` 메서드 구현
- [x] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: SchedulerAssembler 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] Assembler ArchUnit 테스트 추가 및 통과
- [ ] DI 주입 확인 (@Component)
- [ ] 커밋: `struct: SchedulerAssembler 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [x] Assembler Fixture 정리
- [ ] 커밋: `test: SchedulerAssembler Fixture 정리 (Tidy)`

---

### 7️⃣ RegisterSchedulerUseCase 구현 (Cycle 7)

#### 🔴 Red: 테스트 작성
- [x] `RegisterSchedulerUseCaseTest.java` 파일 생성
- [x] Mock Port 준비 (SchedulerCommandPort, SchedulerQueryPort, SellerQueryPort, OutboxEventCommandPort)
- [x] `shouldRegisterSchedulerSuccessfully()` 작성
- [x] `shouldThrowExceptionWhenSellerNotActive()` 작성
- [x] `shouldThrowExceptionWhenDuplicateName()` 작성
- [x] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: RegisterSchedulerUseCase 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `RegisterSchedulerUseCase.java` 파일 생성
- [x] `@Transactional` 추가
- [x] Seller 조회 및 ACTIVE 여부 확인
- [x] CrawlingScheduler.forNew() 생성
- [x] 중복 체크 (sellerId, schedulerName)
- [x] Scheduler 저장
- [x] OutboxEvent 저장 (SCHEDULER_CREATED)
- [x] SchedulerResponse 반환
- [x] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: RegisterSchedulerUseCase 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] Transaction 경계 검증 (외부 API 호출 없는지)
- [ ] Assembler 패턴 적용 확인
- [ ] Port-In Command ArchUnit 테스트 통과
- [ ] 커밋: `struct: RegisterSchedulerUseCase 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] RegisterSchedulerUseCase Fixture 정리
- [ ] 커밋: `test: RegisterSchedulerUseCase Fixture 정리 (Tidy)`

---

### 8️⃣ UpdateSchedulerUseCase 구현 (Cycle 8)

- [x] `UpdateSchedulerUseCaseTest.java` 파일 생성
- [x] `shouldUpdateSchedulerSuccessfully()` 작성
- [x] `shouldThrowExceptionWhenSchedulerNotFound()` 작성
- [x] `shouldSaveHistoryWhenUpdated()` 작성
- [x] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: UpdateSchedulerUseCase 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `UpdateSchedulerUseCase.java` 파일 생성
- [x] `@Transactional` 추가
- [x] Scheduler 조회
- [x] Scheduler.update() 호출 (Domain)
- [x] SchedulerHistory 저장
- [x] OutboxEvent 저장 (SCHEDULER_UPDATED)
- [x] SchedulerResponse 반환
- [x] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: UpdateSchedulerUseCase 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] Transaction 경계 검증
- [ ] Port-In Command ArchUnit 테스트 통과
- [ ] 커밋: `struct: UpdateSchedulerUseCase 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] UpdateSchedulerUseCase Fixture 정리
- [ ] 커밋: `test: UpdateSchedulerUseCase Fixture 정리 (Tidy)`

---

### 9️⃣ DeactivateSchedulerUseCase 구현 (Cycle 9)

#### 🔴 Red: 테스트 작성
- [x] `DeactivateSchedulerUseCaseTest.java` 파일 생성
- [x] `shouldDeactivateSchedulerSuccessfully()` 작성
- [x] `shouldSaveHistoryWhenDeactivated()` 작성
- [x] 커밋: `test: DeactivateSchedulerUseCase 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `DeactivateSchedulerUseCase.java` 파일 생성
- [x] `@Transactional` 추가
- [x] Scheduler 조회
- [x] Scheduler.deactivate() 호출 (Domain)
- [x] SchedulerHistory 저장
- [x] OutboxEvent 저장 (SCHEDULER_DELETED)
- [x] 커밋: `feat: DeactivateSchedulerUseCase 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] Port-In Command ArchUnit 테스트 통과
- [ ] 커밋: `struct: DeactivateSchedulerUseCase 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [x] DeactivateSchedulerUseCase Fixture 정리
- [x] 커밋: `test: DeactivateSchedulerUseCase Fixture 정리 (Tidy)`

---

### 🔟 GetSchedulerUseCase 구현 (Cycle 10)

#### 🔴 Red: 테스트 작성
- [x] `GetSchedulerUseCaseTest.java` 파일 생성
- [x] `shouldGetSchedulerById()` 작성
- [x] `shouldThrowExceptionWhenNotFound()` 작성
- [x] 커밋: `test: GetSchedulerUseCase 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `GetSchedulerUseCase.java` 파일 생성
- [x] `@Transactional(readOnly = true)` 추가
- [x] SchedulerQueryPort.findById() 호출
- [x] SchedulerDetailResponse 반환
- [x] 커밋: `feat: GetSchedulerUseCase 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] Port-In Query ArchUnit 테스트 통과
- [ ] 커밋: `struct: GetSchedulerUseCase 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [x] GetSchedulerUseCase Fixture 정리
- [x] 커밋: `test: GetSchedulerUseCase Fixture 정리 (Tidy)`

---

### 1️⃣1️⃣ ListSchedulersUseCase 구현 (Cycle 11)

#### 🔴 Red: 테스트 작성
- [x] `ListSchedulersUseCaseTest.java` 파일 생성
- [x] `shouldListSchedulersWithPagination()` 작성
- [x] `shouldFilterBySellerIdAndStatus()` 작성
- [ ] 커밋: `test: ListSchedulersUseCase 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `ListSchedulersUseCase.java` 파일 생성
- [x] `@Transactional(readOnly = true)` 추가
- [x] SchedulerQueryPort.findAllBySellerIdAndStatus() 호출
- [x] Page<SchedulerSummaryResponse> 반환
- [ ] 커밋: `feat: ListSchedulersUseCase 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] Port-In Query ArchUnit 테스트 통과
- [ ] 커밋: `struct: ListSchedulersUseCase 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [x] ListSchedulersUseCase Fixture 정리
- [ ] 커밋: `test: ListSchedulersUseCase Fixture 정리 (Tidy)`

---

### 1️⃣2️⃣ GetSchedulerHistoryUseCase 구현 (Cycle 12)

#### 🔴 Red: 테스트 작성
- [x] `GetSchedulerHistoryUseCaseTest.java` 파일 생성
- [x] `shouldGetHistoryWithPagination()` 작성
- [x] `shouldOrderByChangedAtDesc()` 작성
- [ ] 커밋: `test: GetSchedulerHistoryUseCase 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `GetSchedulerHistoryUseCase.java` 파일 생성
- [x] `@Transactional(readOnly = true)` 추가
- [x] SchedulerHistoryQueryPort.findBySchedulerId() 호출
- [x] Page<SchedulerHistoryResponse> 반환
- [ ] 커밋: `feat: GetSchedulerHistoryUseCase 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] Port-In Query ArchUnit 테스트 통과
- [ ] 커밋: `struct: GetSchedulerHistoryUseCase 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [x] GetSchedulerHistoryUseCase Fixture 정리
- [ ] 커밋: `test: GetSchedulerHistoryUseCase Fixture 정리 (Tidy)`

---

### 1️⃣3️⃣ TransactionSynchronizationAdapter 구현 - 기본 구조 (Cycle 13)

#### 🔴 Red: 테스트 작성
- [x] `TransactionSynchronizationAdapterTest.java` 파일 생성
- [x] `shouldProcessOutboxEventAfterCommit()` 작성
- [x] Mock EventBridgeClientPort 준비
- [ ] 커밋: `test: TransactionSynchronizationAdapter 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `TransactionSynchronizationAdapter.java` 파일 생성
- [x] `@TransactionalEventListener(phase = AFTER_COMMIT)` 추가
- [x] OutboxEvent 조회 (PENDING)
- [x] 비동기 스레드 처리 (`@Async`)
- [ ] 커밋: `feat: TransactionSynchronizationAdapter 기본 구조 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] 비동기 처리 확인
- [ ] 커밋: `struct: TransactionSynchronizationAdapter 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [x] TransactionSynchronizationAdapter Fixture 정리
- [ ] 커밋: `test: TransactionSynchronizationAdapter Fixture 정리 (Tidy)`

---

### 1️⃣4️⃣ TransactionSynchronizationAdapter - AWS 연동 (Cycle 14)

#### 🔴 Red: 테스트 작성
- [x] `shouldCreateRuleWhenSchedulerCreated()` 작성
- [x] `shouldUpdateRuleWhenSchedulerUpdated()` 작성
- [x] `shouldDisableRuleWhenSchedulerDeleted()` 작성
- [ ] 커밋: `test: TransactionSynchronization AWS 연동 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] eventType에 따라 AWS API 호출
  - SCHEDULER_CREATED: createRule
  - SCHEDULER_UPDATED: updateRule
  - SCHEDULER_DELETED: disableRule
- [x] 성공 시: Outbox.status → PUBLISHED
- [x] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: TransactionSynchronization AWS 연동 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] 에러 처리 로직 강화
- [ ] 커밋: `struct: TransactionSynchronization AWS 연동 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [x] AWS 연동 테스트 Fixture 정리
- [ ] 커밋: `test: AWS 연동 Fixture 정리 (Tidy)`

---

### 1️⃣5️⃣ TransactionSynchronizationAdapter - 실패 처리 (Cycle 15)

#### 🔴 Red: 테스트 작성
- [x] `shouldMarkAsFailedWhenAwsCallFails()` 작성
- [x] `shouldSaveErrorMessage()` 작성
- [ ] 커밋: `test: TransactionSynchronization 실패 처리 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] try-catch 블록 추가
- [x] 실패 시: Outbox.status → FAILED
- [x] Outbox.errorMessage 업데이트
- [ ] 커밋: `feat: TransactionSynchronization 실패 처리 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] 예외 처리 로직 정리
- [ ] 커밋: `struct: TransactionSynchronization 실패 처리 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [x] 실패 시나리오 Fixture 정리
- [ ] 커밋: `test: 실패 처리 Fixture 정리 (Tidy)`

---

### 1️⃣6️⃣ OutboxEventProcessor 구현 - 기본 스케줄러 (Cycle 16)

#### 🔴 Red: 테스트 작성
- [x] `OutboxEventProcessorTest.java` 파일 생성
- [x] `shouldProcessPendingOutboxEvents()` 작성
- [x] 매 1분 실행 검증
- [ ] 커밋: `test: OutboxEventProcessor 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `OutboxEventProcessor.java` 파일 생성
- [x] `@Scheduled(fixedDelay = 60000)` 추가
- [x] PENDING/FAILED 상태 Outbox 조회
- [x] retryCount < maxRetries 조건 확인
- [ ] 커밋: `feat: OutboxEventProcessor 기본 구조 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] 스케줄러 설정 검증
- [ ] 커밋: `struct: OutboxEventProcessor 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [x] OutboxEventProcessor Fixture 정리
- [ ] 커밋: `test: OutboxEventProcessor Fixture 정리 (Tidy)`

---

### 1️⃣7️⃣ OutboxEventProcessor - 재시도 로직 (Cycle 17)

#### 🔴 Red: 테스트 작성
- [x] `shouldIncrementRetryCount()` 작성
- [x] `shouldMarkAsFailedWhenMaxRetriesExceeded()` 작성
- [ ] 커밋: `test: OutboxEventProcessor 재시도 로직 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] 각 Outbox에 대해 AWS API 호출
- [x] 성공 시: Outbox.status → PUBLISHED
- [x] 실패 시: Outbox.retryCount++
- [x] retryCount >= maxRetries 시: Outbox.status → FAILED (영구)
- [ ] 커밋: `feat: OutboxEventProcessor 재시도 로직 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] Exponential Backoff 전략 추가 고려
- [ ] 커밋: `struct: OutboxEventProcessor 재시도 로직 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [x] 재시도 시나리오 Fixture 정리
- [ ] 커밋: `test: 재시도 로직 Fixture 정리 (Tidy)`

---

### 1️⃣8️⃣ OutboxEventProcessor - Slack 알림 (Cycle 18)

#### 🔴 Red: 테스트 작성
- [x] `shouldSendSlackNotificationWhenFailed()` 작성
- [x] Mock SlackClient 준비
- [ ] 커밋: `test: OutboxEventProcessor Slack 알림 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `SlackClientPort.java` 인터페이스 생성
- [x] FAILED (영구) 상태 시 Slack 알림 발송
- [ ] 커밋: `feat: OutboxEventProcessor Slack 알림 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] 알림 메시지 포맷 개선
- [ ] 커밋: `struct: Slack 알림 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [x] Slack 알림 Fixture 정리
- [ ] 커밋: `test: Slack 알림 Fixture 정리 (Tidy)`

---

### 1️⃣9️⃣ Application Layer ArchUnit 테스트 (Cycle 19)

#### 🔴 Red: 테스트 작성
- [x] `ApplicationLayerArchUnitTest.java` 파일 생성
- [x] Application Layer는 Domain Layer만 의존 확인
- [x] Adapter Layer 의존 금지 확인
- [x] Port 네이밍 규칙 확인 (`*PersistencePort`, `*QueryPort`, `*ClientPort`)
- [x] UseCase 네이밍 규칙 확인 (`*UseCase`)
- [ ] 커밋: `test: Application Layer ArchUnit 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] 모든 ArchUnit 규칙 통과하도록 수정
- [ ] 커밋: `feat: Application Layer ArchUnit 통과 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] 아키텍처 위반 사항 수정
- [ ] 커밋: `struct: Application Layer 아키텍처 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [x] ArchUnit 테스트 정리
- [ ] 커밋: `test: Application Layer ArchUnit 정리 (Tidy)`

---

### 2️⃣0️⃣ 통합 테스트 - UseCase 전체 플로우 (Cycle 20)

#### 🔴 Red: 테스트 작성
- [x] `SchedulerUseCaseIntegrationTest.java` 파일 생성
- [x] Register → Get → Update → Get → Deactivate 전체 플로우 작성
- [x] Mock Port 준비
- [ ] 커밋: `test: UseCase 통합 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] 전체 플로우 테스트 통과 확인
- [x] Outbox Pattern 동작 검증
- [ ] 커밋: `feat: UseCase 통합 테스트 통과 (Green)`

#### ♻️ Refactor: 구조 개선
- [x] 테스트 격리 확인
- [ ] 커밋: `struct: UseCase 통합 테스트 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [x] 통합 테스트 Fixture 정리
- [ ] 커밋: `test: UseCase 통합 테스트 Fixture 정리 (Tidy)`

---

## ✅ 완료 조건

- [ ] 모든 TDD 사이클 완료 (80개 체크박스 모두 ✅)
- [ ] 모든 테스트 통과 (Coverage > 80%)
- [ ] ArchUnit 테스트 통과
  - [ ] Port-In Command ArchUnit
  - [ ] Port-In Query ArchUnit
  - [ ] Port-Out Command ArchUnit
  - [ ] Port-Out Query ArchUnit
  - [ ] Assembler ArchUnit
  - [ ] Application Layer 의존성 검증
- [ ] Zero-Tolerance 규칙 준수
  - [ ] Transaction 경계 엄격 관리 (외부 API 호출 트랜잭션 밖)
  - [ ] Spring Proxy 제약사항 준수
  - [ ] Command/Query 분리 (CQRS)
  - [ ] Port 네이밍 규칙 (`*CommandPort`, `*QueryPort`)
  - [ ] Assembler 패턴 사용 (@Component Bean)
- [ ] TestFixture 모두 정리 (Object Mother 패턴)
  - [ ] Command/Query DTO Fixtures
  - [ ] Response DTO Fixtures
  - [ ] UseCase Fixtures
  - [ ] Outbox Processor Fixtures

---

## 🔗 관련 문서

- **Task**: `/Users/sangwon-ryu/crawlinghub/docs/prd/eventbridge/EVENTBRIDGE-002-application.md`
- **PRD**: `/Users/sangwon-ryu/crawlinghub/docs/prd/mustit-seller-crawler-scheduler.md`
- **코딩 규칙**: `/Users/sangwon-ryu/crawlinghub/docs/coding_convention/03-application-layer/`
- **선행 Task**: EVENTBRIDGE-001 (Domain)

---

## 📋 다음 단계

TDD Plan 완료 후:

```bash
# 1. Kent Beck TDD 시작
/kb/application/go

# 2. Plan 진행 상황 확인
cat docs/prd/eventbridge/plan/EVENTBRIDGE-002-application-plan.md
```

**TDD 진행 방식**:
- `/kb/application/red` - Red Phase 실행 (테스트 작성)
- `/kb/application/green` - Green Phase 실행 (최소 구현)
- `/kb/application/refactor` - Refactor Phase 실행 (구조 개선)
- `/kb/application/go` - 전체 사이클 자동 실행 (Red → Green → Refactor → Tidy)

---

**작성일**: 2025-11-18
**버전**: 1.0.0
