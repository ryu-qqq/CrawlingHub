# EVENTBRIDGE-002 TDD Plan

**Task**: EventBridge Application Layer 구현
**Layer**: Application
**브랜치**: feature/EVENTBRIDGE-002-application
**예상 소요 시간**: 300분 (20 사이클 × 15분)

---

## 📝 TDD 사이클 체크리스트

### 1️⃣ Command/Query DTO 구현 (Cycle 1)

#### 🔴 Red: 테스트 작성
- [ ] `RegisterSchedulerCommandTest.java` 파일 생성
- [ ] `shouldCreateCommandWithValidData()` 작성
- [ ] `GetSchedulerQueryTest.java` 파일 생성
- [ ] Record 타입 검증 테스트 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: Command/Query DTO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `RegisterSchedulerCommand.java` Record 생성
- [ ] `UpdateSchedulerCommand.java` Record 생성
- [ ] `DeactivateSchedulerCommand.java` Record 생성
- [ ] `GetSchedulerQuery.java` Record 생성
- [ ] `ListSchedulersQuery.java` Record 생성
- [ ] `GetSchedulerHistoryQuery.java` Record 생성
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: Command/Query DTO 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] DTO Record ArchUnit 테스트 추가 및 통과
- [ ] 검증 로직 추가 (필요 시)
- [ ] 커밋: `struct: Command/Query DTO 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `RegisterSchedulerCommandFixture.java` 생성
- [ ] `GetSchedulerQueryFixture.java` 생성
- [ ] 테스트 → Fixture 사용
- [ ] 커밋: `test: Command/Query DTO Fixture 정리 (Tidy)`

---

### 2️⃣ Response DTO 구현 (Cycle 2)

#### 🔴 Red: 테스트 작성
- [ ] `SchedulerResponseTest.java` 파일 생성
- [ ] `shouldCreateResponseFromDomain()` 작성
- [ ] Record 변환 테스트 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: Response DTO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SchedulerResponse.java` Record 생성
- [ ] `SchedulerDetailResponse.java` Record 생성
- [ ] `SchedulerSummaryResponse.java` Record 생성
- [ ] `SchedulerHistoryResponse.java` Record 생성
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: Response DTO 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] Response DTO ArchUnit 테스트 통과
- [ ] 커밋: `struct: Response DTO 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `SchedulerResponseFixture.java` 생성
- [ ] 커밋: `test: Response DTO Fixture 정리 (Tidy)`

---

### 3️⃣ Port 인터페이스 정의 - Command Ports (Cycle 3)

#### 🔴 Red: 테스트 작성
- [ ] `SchedulerCommandPortTest.java` 파일 생성
- [ ] Port 인터페이스 시그니처 검증 테스트
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: Command Port 인터페이스 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SchedulerCommandPort.java` 인터페이스 생성
  - `Scheduler save(Scheduler scheduler)`
  - `void delete(Long schedulerId)`
- [ ] `OutboxEventCommandPort.java` 인터페이스 생성
  - `OutboxEvent save(OutboxEvent event)`
  - `void deleteByStatusAndCreatedAtBefore(...)`
- [ ] `SchedulerHistoryCommandPort.java` 인터페이스 생성
  - `SchedulerHistory save(SchedulerHistory history)`
- [ ] 테스트 실행 → 통과 확인
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
- [ ] `SchedulerQueryPortTest.java` 파일 생성
- [ ] Query Port 시그니처 검증
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: Query Port 인터페이스 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SchedulerQueryPort.java` 인터페이스 생성
  - `Optional<Scheduler> findById(Long schedulerId)`
  - `Optional<Scheduler> findBySellerIdAndSchedulerName(...)`
  - `List<Scheduler> findBySellerIdAndStatus(...)`
  - `Page<Scheduler> findAllBySellerIdAndStatus(...)`
  - `int countActiveSchedulersBySellerId(Long sellerId)`
- [ ] `OutboxEventQueryPort.java` 인터페이스 생성
- [ ] `SchedulerHistoryQueryPort.java` 인터페이스 생성
- [ ] `SellerQueryPort.java` 인터페이스 생성
- [ ] 커밋: `feat: Query Port 인터페이스 구현 (Green)`

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
- [ ] `EventBridgeClientPortTest.java` 파일 생성
- [ ] 외부 API Port 시그니처 검증
- [ ] 커밋: `test: EventBridgeClientPort 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `EventBridgeClientPort.java` 인터페이스 생성
  - `void createRule(String ruleName, String cronExpression, String target)`
  - `void updateRule(String ruleName, String cronExpression)`
  - `void disableRule(String ruleName)`
- [ ] 커밋: `feat: EventBridgeClientPort 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] Port ArchUnit 테스트 통과
- [ ] 커밋: `struct: EventBridgeClientPort 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] EventBridgeClient Mock Fixture 정리
- [ ] 커밋: `test: EventBridgeClientPort Fixture 정리 (Tidy)`

---

### 6️⃣ SchedulerAssembler 구현 (Cycle 6)

#### 🔴 Red: 테스트 작성
- [ ] `SchedulerAssemblerTest.java` 파일 생성
- [ ] `shouldAssembleDomainToResponse()` 작성
- [ ] `shouldAssembleCommandToDomain()` 작성
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: SchedulerAssembler 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SchedulerAssembler.java` 파일 생성 (@Component)
- [ ] `toResponse(Scheduler)` 메서드 구현
- [ ] `toDetailResponse(Scheduler)` 메서드 구현
- [ ] `toSummaryResponse(Scheduler)` 메서드 구현
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: SchedulerAssembler 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] Assembler ArchUnit 테스트 추가 및 통과
- [ ] DI 주입 확인 (@Component)
- [ ] 커밋: `struct: SchedulerAssembler 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Assembler Fixture 정리
- [ ] 커밋: `test: SchedulerAssembler Fixture 정리 (Tidy)`

---

### 7️⃣ RegisterSchedulerUseCase 구현 (Cycle 7)

#### 🔴 Red: 테스트 작성
- [ ] `RegisterSchedulerUseCaseTest.java` 파일 생성
- [ ] Mock Port 준비 (SchedulerCommandPort, SchedulerQueryPort, SellerQueryPort, OutboxEventCommandPort)
- [ ] `shouldRegisterSchedulerSuccessfully()` 작성
- [ ] `shouldThrowExceptionWhenSellerNotActive()` 작성
- [ ] `shouldThrowExceptionWhenDuplicateName()` 작성
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: RegisterSchedulerUseCase 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `RegisterSchedulerUseCase.java` 파일 생성
- [ ] `@Transactional` 추가
- [ ] Seller 조회 및 ACTIVE 여부 확인
- [ ] CrawlingScheduler.forNew() 생성
- [ ] 중복 체크 (sellerId, schedulerName)
- [ ] Scheduler 저장
- [ ] OutboxEvent 저장 (SCHEDULER_CREATED)
- [ ] SchedulerResponse 반환
- [ ] 테스트 실행 → 통과 확인
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

#### 🔴 Red: 테스트 작성
- [ ] `UpdateSchedulerUseCaseTest.java` 파일 생성
- [ ] `shouldUpdateSchedulerSuccessfully()` 작성
- [ ] `shouldThrowExceptionWhenSchedulerNotFound()` 작성
- [ ] `shouldSaveHistoryWhenUpdated()` 작성
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: UpdateSchedulerUseCase 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `UpdateSchedulerUseCase.java` 파일 생성
- [ ] `@Transactional` 추가
- [ ] Scheduler 조회
- [ ] Scheduler.update() 호출 (Domain)
- [ ] SchedulerHistory 저장
- [ ] OutboxEvent 저장 (SCHEDULER_UPDATED)
- [ ] SchedulerResponse 반환
- [ ] 테스트 실행 → 통과 확인
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
- [ ] `DeactivateSchedulerUseCaseTest.java` 파일 생성
- [ ] `shouldDeactivateSchedulerSuccessfully()` 작성
- [ ] `shouldSaveHistoryWhenDeactivated()` 작성
- [ ] 커밋: `test: DeactivateSchedulerUseCase 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `DeactivateSchedulerUseCase.java` 파일 생성
- [ ] `@Transactional` 추가
- [ ] Scheduler 조회
- [ ] Scheduler.deactivate() 호출 (Domain)
- [ ] SchedulerHistory 저장
- [ ] OutboxEvent 저장 (SCHEDULER_DELETED)
- [ ] 커밋: `feat: DeactivateSchedulerUseCase 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] Port-In Command ArchUnit 테스트 통과
- [ ] 커밋: `struct: DeactivateSchedulerUseCase 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] DeactivateSchedulerUseCase Fixture 정리
- [ ] 커밋: `test: DeactivateSchedulerUseCase Fixture 정리 (Tidy)`

---

### 🔟 GetSchedulerUseCase 구현 (Cycle 10)

#### 🔴 Red: 테스트 작성
- [ ] `GetSchedulerUseCaseTest.java` 파일 생성
- [ ] `shouldGetSchedulerById()` 작성
- [ ] `shouldThrowExceptionWhenNotFound()` 작성
- [ ] 커밋: `test: GetSchedulerUseCase 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `GetSchedulerUseCase.java` 파일 생성
- [ ] `@Transactional(readOnly = true)` 추가
- [ ] SchedulerQueryPort.findById() 호출
- [ ] SchedulerDetailResponse 반환
- [ ] 커밋: `feat: GetSchedulerUseCase 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] Port-In Query ArchUnit 테스트 통과
- [ ] 커밋: `struct: GetSchedulerUseCase 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] GetSchedulerUseCase Fixture 정리
- [ ] 커밋: `test: GetSchedulerUseCase Fixture 정리 (Tidy)`

---

### 1️⃣1️⃣ ListSchedulersUseCase 구현 (Cycle 11)

#### 🔴 Red: 테스트 작성
- [ ] `ListSchedulersUseCaseTest.java` 파일 생성
- [ ] `shouldListSchedulersWithPagination()` 작성
- [ ] `shouldFilterBySellerIdAndStatus()` 작성
- [ ] 커밋: `test: ListSchedulersUseCase 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `ListSchedulersUseCase.java` 파일 생성
- [ ] `@Transactional(readOnly = true)` 추가
- [ ] SchedulerQueryPort.findAllBySellerIdAndStatus() 호출
- [ ] Page<SchedulerSummaryResponse> 반환
- [ ] 커밋: `feat: ListSchedulersUseCase 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] Port-In Query ArchUnit 테스트 통과
- [ ] 커밋: `struct: ListSchedulersUseCase 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] ListSchedulersUseCase Fixture 정리
- [ ] 커밋: `test: ListSchedulersUseCase Fixture 정리 (Tidy)`

---

### 1️⃣2️⃣ GetSchedulerHistoryUseCase 구현 (Cycle 12)

#### 🔴 Red: 테스트 작성
- [ ] `GetSchedulerHistoryUseCaseTest.java` 파일 생성
- [ ] `shouldGetHistoryWithPagination()` 작성
- [ ] `shouldOrderByChangedAtDesc()` 작성
- [ ] 커밋: `test: GetSchedulerHistoryUseCase 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `GetSchedulerHistoryUseCase.java` 파일 생성
- [ ] `@Transactional(readOnly = true)` 추가
- [ ] SchedulerHistoryQueryPort.findBySchedulerId() 호출
- [ ] Page<SchedulerHistoryResponse> 반환
- [ ] 커밋: `feat: GetSchedulerHistoryUseCase 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] Port-In Query ArchUnit 테스트 통과
- [ ] 커밋: `struct: GetSchedulerHistoryUseCase 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] GetSchedulerHistoryUseCase Fixture 정리
- [ ] 커밋: `test: GetSchedulerHistoryUseCase Fixture 정리 (Tidy)`

---

### 1️⃣3️⃣ TransactionSynchronizationAdapter 구현 - 기본 구조 (Cycle 13)

#### 🔴 Red: 테스트 작성
- [ ] `TransactionSynchronizationAdapterTest.java` 파일 생성
- [ ] `shouldProcessOutboxEventAfterCommit()` 작성
- [ ] Mock EventBridgeClientPort 준비
- [ ] 커밋: `test: TransactionSynchronizationAdapter 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `TransactionSynchronizationAdapter.java` 파일 생성
- [ ] `@TransactionalEventListener(phase = AFTER_COMMIT)` 추가
- [ ] OutboxEvent 조회 (PENDING)
- [ ] 비동기 스레드 처리 (`@Async`)
- [ ] 커밋: `feat: TransactionSynchronizationAdapter 기본 구조 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] 비동기 처리 확인
- [ ] 커밋: `struct: TransactionSynchronizationAdapter 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TransactionSynchronizationAdapter Fixture 정리
- [ ] 커밋: `test: TransactionSynchronizationAdapter Fixture 정리 (Tidy)`

---

### 1️⃣4️⃣ TransactionSynchronizationAdapter - AWS 연동 (Cycle 14)

#### 🔴 Red: 테스트 작성
- [ ] `shouldCreateRuleWhenSchedulerCreated()` 작성
- [ ] `shouldUpdateRuleWhenSchedulerUpdated()` 작성
- [ ] `shouldDisableRuleWhenSchedulerDeleted()` 작성
- [ ] 커밋: `test: TransactionSynchronization AWS 연동 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] eventType에 따라 AWS API 호출
  - SCHEDULER_CREATED: createRule
  - SCHEDULER_UPDATED: updateRule
  - SCHEDULER_DELETED: disableRule
- [ ] 성공 시: Outbox.status → PUBLISHED
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: TransactionSynchronization AWS 연동 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] 에러 처리 로직 강화
- [ ] 커밋: `struct: TransactionSynchronization AWS 연동 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] AWS 연동 테스트 Fixture 정리
- [ ] 커밋: `test: AWS 연동 Fixture 정리 (Tidy)`

---

### 1️⃣5️⃣ TransactionSynchronizationAdapter - 실패 처리 (Cycle 15)

#### 🔴 Red: 테스트 작성
- [ ] `shouldMarkAsFailedWhenAwsCallFails()` 작성
- [ ] `shouldSaveErrorMessage()` 작성
- [ ] 커밋: `test: TransactionSynchronization 실패 처리 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] try-catch 블록 추가
- [ ] 실패 시: Outbox.status → FAILED
- [ ] Outbox.errorMessage 업데이트
- [ ] 커밋: `feat: TransactionSynchronization 실패 처리 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] 예외 처리 로직 정리
- [ ] 커밋: `struct: TransactionSynchronization 실패 처리 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 실패 시나리오 Fixture 정리
- [ ] 커밋: `test: 실패 처리 Fixture 정리 (Tidy)`

---

### 1️⃣6️⃣ OutboxEventProcessor 구현 - 기본 스케줄러 (Cycle 16)

#### 🔴 Red: 테스트 작성
- [ ] `OutboxEventProcessorTest.java` 파일 생성
- [ ] `shouldProcessPendingOutboxEvents()` 작성
- [ ] 매 1분 실행 검증
- [ ] 커밋: `test: OutboxEventProcessor 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `OutboxEventProcessor.java` 파일 생성
- [ ] `@Scheduled(fixedDelay = 60000)` 추가
- [ ] PENDING/FAILED 상태 Outbox 조회
- [ ] retryCount < maxRetries 조건 확인
- [ ] 커밋: `feat: OutboxEventProcessor 기본 구조 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] 스케줄러 설정 검증
- [ ] 커밋: `struct: OutboxEventProcessor 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] OutboxEventProcessor Fixture 정리
- [ ] 커밋: `test: OutboxEventProcessor Fixture 정리 (Tidy)`

---

### 1️⃣7️⃣ OutboxEventProcessor - 재시도 로직 (Cycle 17)

#### 🔴 Red: 테스트 작성
- [ ] `shouldIncrementRetryCount()` 작성
- [ ] `shouldMarkAsFailedWhenMaxRetriesExceeded()` 작성
- [ ] 커밋: `test: OutboxEventProcessor 재시도 로직 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] 각 Outbox에 대해 AWS API 호출
- [ ] 성공 시: Outbox.status → PUBLISHED
- [ ] 실패 시: Outbox.retryCount++
- [ ] retryCount >= maxRetries 시: Outbox.status → FAILED (영구)
- [ ] 커밋: `feat: OutboxEventProcessor 재시도 로직 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] Exponential Backoff 전략 추가 고려
- [ ] 커밋: `struct: OutboxEventProcessor 재시도 로직 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 재시도 시나리오 Fixture 정리
- [ ] 커밋: `test: 재시도 로직 Fixture 정리 (Tidy)`

---

### 1️⃣8️⃣ OutboxEventProcessor - Slack 알림 (Cycle 18)

#### 🔴 Red: 테스트 작성
- [ ] `shouldSendSlackNotificationWhenFailed()` 작성
- [ ] Mock SlackClient 준비
- [ ] 커밋: `test: OutboxEventProcessor Slack 알림 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SlackClientPort.java` 인터페이스 생성
- [ ] FAILED (영구) 상태 시 Slack 알림 발송
- [ ] 커밋: `feat: OutboxEventProcessor Slack 알림 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] 알림 메시지 포맷 개선
- [ ] 커밋: `struct: Slack 알림 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Slack 알림 Fixture 정리
- [ ] 커밋: `test: Slack 알림 Fixture 정리 (Tidy)`

---

### 1️⃣9️⃣ Application Layer ArchUnit 테스트 (Cycle 19)

#### 🔴 Red: 테스트 작성
- [ ] `ApplicationLayerArchUnitTest.java` 파일 생성
- [ ] Application Layer는 Domain Layer만 의존 확인
- [ ] Adapter Layer 의존 금지 확인
- [ ] Port 네이밍 규칙 확인 (`*CommandPort`, `*QueryPort`)
- [ ] UseCase 네이밍 규칙 확인 (`*UseCase`)
- [ ] 커밋: `test: Application Layer ArchUnit 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] 모든 ArchUnit 규칙 통과하도록 수정
- [ ] 커밋: `feat: Application Layer ArchUnit 통과 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] 아키텍처 위반 사항 수정
- [ ] 커밋: `struct: Application Layer 아키텍처 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] ArchUnit 테스트 정리
- [ ] 커밋: `test: Application Layer ArchUnit 정리 (Tidy)`

---

### 2️⃣0️⃣ 통합 테스트 - UseCase 전체 플로우 (Cycle 20)

#### 🔴 Red: 테스트 작성
- [ ] `SchedulerUseCaseIntegrationTest.java` 파일 생성
- [ ] Register → Get → Update → Get → Deactivate 전체 플로우 작성
- [ ] Mock Port 준비
- [ ] 커밋: `test: UseCase 통합 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] 전체 플로우 테스트 통과 확인
- [ ] Outbox Pattern 동작 검증
- [ ] 커밋: `feat: UseCase 통합 테스트 통과 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] 테스트 격리 확인
- [ ] 커밋: `struct: UseCase 통합 테스트 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 통합 테스트 Fixture 정리
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
