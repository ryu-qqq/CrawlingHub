# EVENTBRIDGE-001 TDD Plan

**Task**: EventBridge Domain Layer 구현
**Layer**: Domain
**브랜치**: feature/EVENTBRIDGE-001-domain
**예상 소요 시간**: 150분 (10 사이클 × 15분)

---

## 📝 TDD 사이클 체크리스트

### 1️⃣ CronExpression Value Object 구현 (Cycle 1)

#### 🔴 Red: 테스트 작성 ✅
- [x] `CronExpressionTest.java` 파일 생성
- [x] `shouldCreateCronExpressionWithValidAwsFormat()` 작성
- [x] `shouldThrowExceptionWhenInvalidFormat()` 작성
- [x] `shouldThrowExceptionWhenIntervalLessThanOneHour()` 작성
- [x] 테스트 실행 → 컴파일 에러 확인
- [x] 커밋: `test: CronExpression VO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [x] `CronExpression.java` 파일 생성 (Record 사용)
- [x] AWS EventBridge 형식 검증 로직 추가 (6자리 `cron(분 시 일 월 요일 년도)`)
- [x] 최소 1시간 간격 검증 로직 추가
- [x] `InvalidCronExpressionException` 생성
- [x] 테스트 실행 → 통과 확인
- [x] 커밋: `feat: CronExpression VO 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [x] Regex 패턴 상수로 추출
- [x] 검증 로직 메서드 분리
- [x] VO ArchUnit 테스트 추가 및 통과
- [x] 테스트 여전히 통과 확인
- [ ] 커밋: `struct: CronExpression VO 구조 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [x] `CronExpressionFixture.java` 생성 (Object Mother 패턴)
- [x] `aCronExpression()`, `anInvalidCronExpression()` 메서드 작성
- [x] `CronExpressionTest` → Fixture 사용으로 리팩토링
- [x] 테스트 여전히 통과 확인
- [ ] 커밋: `test: CronExpressionFixture 정리 (Tidy)`

---

### 2️⃣ SchedulerStatus Enum 구현 (Cycle 2)

#### 🔴 Red: 테스트 작성 ✅
- [x] `SchedulerStatusTest.java` 파일 생성
- [x] `shouldHaveCorrectValues()` 작성 (PENDING, ACTIVE, INACTIVE)
- [x] 테스트 실행 → 컴파일 에러 확인
- [x] 커밋: `test: SchedulerStatus Enum 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현 ✅
- [x] `SchedulerStatus.java` Enum 생성
- [x] `PENDING`, `ACTIVE`, `INACTIVE` 값 정의
- [x] 테스트 실행 → 통과 확인
- [x] 커밋: `feat: SchedulerStatus Enum 구현 (Green)`

#### ♻️ Refactor: 구조 개선
#### ♻️ Refactor: 구조 개선 ✅
- [x] 상태 전이 메서드 추가 (canTransitionTo)
- [x] Domain Event ArchUnit 테스트 통과
- [x] 커밋: `struct: SchedulerStatus 상태 전이 로직 추가 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [x] `SchedulerStatusFixture.java` 생성
- [ ] 커밋: `test: SchedulerStatusFixture 정리 (Tidy)`

---

### 3️⃣ CrawlingScheduler Aggregate Root 구현 - 생성 (Cycle 3)

#### 🔴 Red: 테스트 작성 ✅
- [x] `CrawlingSchedulerTest.java` 파일 생성
- [x] `shouldCreateSchedulerWithForNew()` 작성
- [x] `forNew()` 호출 시 상태가 PENDING인지 검증
- [x] 테스트 실행 → 컴파일 에러 확인
- [x] 커밋: `test: CrawlingScheduler 생성 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현 ✅
- [x] `CrawlingScheduler.java` 파일 생성 (Plain Java, Lombok 금지)
- [x] `forNew()` 정적 팩토리 메서드 구현
- [x] 생성자 작성 (private)
- [x] Getter 메서드 작성 (Law of Demeter 준수)
- [x] 테스트 실행 → 통과 확인
- [x] 커밋: `feat: CrawlingScheduler forNew 구현 (Green)`

#### ♻️ Refactor: 구조 개선 ✅
- [x] 불변성 보장 (final 필드)
- [x] Law of Demeter 준수 확인
- [x] Aggregate ArchUnit 테스트 추가 및 통과
- [x] 커밋: `struct: CrawlingScheduler 불변성 보장 (Refactor)`

#### 🧹 Tidy: TestFixture 정리 ✅
- [x] `CrawlingSchedulerFixture.java` 생성
- [x] `aCrawlingScheduler()` 메서드 작성
- [x] `CrawlingSchedulerTest` → Fixture 사용
- [ ] 커밋: `test: CrawlingSchedulerFixture 정리 (Tidy)`

---

### 4️⃣ CrawlingScheduler - of() 및 reconstitute() 구현 (Cycle 4)

#### 🔴 Red: 테스트 작성 ✅
- [x] `shouldCreateSchedulerWithOf()` 작성 (Update용)
- [x] `shouldReconstituteSchedulerFromPersistence()` 작성 (DB → Domain)
- [x] 테스트 실행 → 실패 확인
- [x] 커밋: `test: CrawlingScheduler of/reconstitute 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현 ✅
- [x] `of()` 정적 팩토리 메서드 구현
- [x] `reconstitute()` 정적 팩토리 메서드 구현
- [x] 테스트 실행 → 통과 확인
- [x] 커밋: `feat: CrawlingScheduler of/reconstitute 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] 생성 메서드 패턴 3종 완성 확인
- [ ] 테스트 여전히 통과 확인
- [ ] 커밋: `struct: CrawlingScheduler 생성 패턴 정리 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [x] Fixture에 `aReconstitutedScheduler()` 메서드 추가
- [ ] 커밋: `test: CrawlingSchedulerFixture 업데이트 (Tidy)`

---

### 5️⃣ CrawlingScheduler - 스케줄 수정 (update) 구현 (Cycle 5)

#### 🔴 Red: 테스트 작성 ✅
- [x] `shouldUpdateSchedulerName()` 작성
- [x] `shouldUpdateCronExpression()` 작성
- [x] `shouldUpdateStatus()` 작성
- [x] `shouldPublishSchedulerUpdatedEvent()` 작성 (Domain Event)
- [x] 테스트 실행 → 실패 확인
- [x] 커밋: `test: CrawlingScheduler update 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현 ✅
- [x] `update()` 메서드 구현 (Tell, Don't Ask 패턴)
- [x] `SchedulerUpdatedEvent` Record 생성
- [x] Domain Event 발행 로직 추가
- [x] 테스트 실행 → 통과 확인
- [x] 커밋: `feat: CrawlingScheduler update 구현 (Green)`

#### ♻️ Refactor: 구조 개선 ✅
- [x] 변경 사항 추적 로직 개선
- [x] Domain Event 발행 메서드 분리
- [ ] 커밋: `struct: CrawlingScheduler update 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [x] `SchedulerUpdatedEventFixture` 생성
- [ ] 커밋: `test: SchedulerUpdatedEventFixture 정리 (Tidy)`

---

### 6️⃣ CrawlingScheduler - 스케줄 비활성화 (deactivate) 구현 (Cycle 6)

#### 🔴 Red: 테스트 작성
- [ ] `shouldDeactivateScheduler()` 작성
- [ ] `shouldPublishSchedulerDeactivatedEvent()` 작성
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: CrawlingScheduler deactivate 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `deactivate()` 메서드 구현
- [ ] 상태를 INACTIVE로 변경
- [ ] `SchedulerDeactivatedEvent` Record 생성
- [ ] Domain Event 발행
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: CrawlingScheduler deactivate 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] 상태 전이 검증 추가
- [ ] 커밋: `struct: CrawlingScheduler deactivate 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `SchedulerDeactivatedEventFixture` 생성
- [ ] 커밋: `test: SchedulerDeactivatedEventFixture 정리 (Tidy)`

---

### 7️⃣ OutboxEventType / OutboxStatus Enum 구현 (Cycle 7)

#### 🔴 Red: 테스트 작성
- [ ] `OutboxEventTypeTest.java` 생성
- [ ] `OutboxStatusTest.java` 생성
- [ ] Enum 값 검증 테스트 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: OutboxEventType/OutboxStatus 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `OutboxEventType.java` Enum 생성 (SCHEDULER_CREATED, SCHEDULER_UPDATED, SCHEDULER_DELETED)
- [ ] `OutboxStatus.java` Enum 생성 (PENDING, PUBLISHED, FAILED)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: OutboxEventType/OutboxStatus 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] Enum ArchUnit 테스트 통과
- [ ] 커밋: `struct: OutboxEventType/OutboxStatus 정리 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Fixture 생성
- [ ] 커밋: `test: Outbox Enum Fixture 정리 (Tidy)`

---

### 8️⃣ SchedulerOutboxEvent Aggregate Root - 생성 (Cycle 8)

#### 🔴 Red: 테스트 작성
- [ ] `SchedulerOutboxEventTest.java` 파일 생성
- [ ] `shouldCreateOutboxEventWithForNew()` 작성
- [ ] 초기 상태가 PENDING인지 검증
- [ ] retryCount가 0인지 검증
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: SchedulerOutboxEvent 생성 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SchedulerOutboxEvent.java` 파일 생성 (Plain Java)
- [ ] `forNew()` 정적 팩토리 메서드 구현
- [ ] 생성자 작성 (private)
- [ ] Getter 메서드 작성
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: SchedulerOutboxEvent forNew 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] 불변성 보장
- [ ] Aggregate ArchUnit 테스트 통과
- [ ] 커밋: `struct: SchedulerOutboxEvent 구조 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `SchedulerOutboxEventFixture.java` 생성
- [ ] `anOutboxEvent()` 메서드 작성
- [ ] 커밋: `test: SchedulerOutboxEventFixture 정리 (Tidy)`

---

### 9️⃣ SchedulerOutboxEvent - 재시도 로직 구현 (Cycle 9)

#### 🔴 Red: 테스트 작성
- [ ] `shouldIncrementRetryCount()` 작성
- [ ] `shouldMarkAsPublished()` 작성
- [ ] `shouldMarkAsFailed()` 작성
- [ ] `shouldNotRetryWhenMaxRetriesExceeded()` 작성
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: SchedulerOutboxEvent 재시도 로직 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `incrementRetryCount()` 메서드 구현
- [ ] `markAsPublished()` 메서드 구현
- [ ] `markAsFailed()` 메서드 구현
- [ ] `canRetry()` 메서드 구현 (retryCount < maxRetries)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: SchedulerOutboxEvent 재시도 로직 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] 재시도 로직 검증 강화
- [ ] Tell, Don't Ask 패턴 준수 확인
- [ ] 커밋: `struct: SchedulerOutboxEvent 재시도 로직 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Fixture에 재시도 관련 메서드 추가
- [ ] 커밋: `test: SchedulerOutboxEvent Fixture 업데이트 (Tidy)`

---

### 🔟 Domain Exception 구현 (Cycle 10)

#### 🔴 Red: 테스트 작성
- [ ] `DomainExceptionTest.java` 파일 생성
- [ ] `DuplicateSchedulerNameException` 테스트 작성
- [ ] `InvalidCronExpressionException` 테스트 작성
- [ ] `SellerNotActiveException` 테스트 작성
- [ ] `SchedulerNotFoundException` 테스트 작성
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: Domain Exception 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `DuplicateSchedulerNameException.java` 생성
- [ ] `InvalidCronExpressionException.java` 생성 (이미 Cycle 1에서 생성)
- [ ] `SellerNotActiveException.java` 생성
- [ ] `SchedulerNotFoundException.java` 생성
- [ ] ErrorCode Enum 정의
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: Domain Exception 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] Exception 메시지 명확화
- [ ] Exception ArchUnit 테스트 통과
- [ ] 커밋: `struct: Domain Exception 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Exception Fixture 정리
- [ ] 커밋: `test: Domain Exception Fixture 정리 (Tidy)`

---

## ✅ 완료 조건

- [ ] 모든 TDD 사이클 완료 (40개 체크박스 모두 ✅)
- [ ] 모든 테스트 통과 (Coverage > 80%)
- [ ] ArchUnit 테스트 통과
  - [ ] Aggregate ArchUnit 테스트
  - [ ] Value Object ArchUnit 테스트
  - [ ] Exception ArchUnit 테스트
  - [ ] Domain Layer는 다른 레이어 의존 금지
- [ ] Zero-Tolerance 규칙 준수
  - [ ] Lombok 금지 확인
  - [ ] Long FK 전략 확인
  - [ ] Law of Demeter 준수 확인
  - [ ] Tell Don't Ask 패턴 준수 확인
- [ ] TestFixture 모두 정리 (Object Mother 패턴)
  - [ ] CronExpressionFixture
  - [ ] SchedulerStatusFixture
  - [ ] CrawlingSchedulerFixture
  - [ ] SchedulerOutboxEventFixture
  - [ ] OutboxEventType/OutboxStatusFixture
  - [ ] Domain Event Fixtures
  - [ ] Exception Fixtures

---

## 🔗 관련 문서

- **Task**: `/Users/sangwon-ryu/crawlinghub/docs/prd/eventbridge/EVENTBRIDGE-001-domain.md`
- **PRD**: `/Users/sangwon-ryu/crawlinghub/docs/prd/mustit-seller-crawler-scheduler.md`
- **코딩 규칙**: `/Users/sangwon-ryu/crawlinghub/docs/coding_convention/02-domain-layer/`

---

## 📋 다음 단계

TDD Plan 완료 후:

```bash
# 1. Kent Beck TDD 시작
/kb/domain/go

# 2. Plan 진행 상황 확인
cat docs/prd/eventbridge/plan/EVENTBRIDGE-001-domain-plan.md
```

**TDD 진행 방식**:
- `/kb/domain/red` - Red Phase 실행 (테스트 작성)
- `/kb/domain/green` - Green Phase 실행 (최소 구현)
- `/kb/domain/refactor` - Refactor Phase 실행 (구조 개선)
- `/kb/domain/go` - 전체 사이클 자동 실행 (Red → Green → Refactor → Tidy)

---

**작성일**: 2025-11-18
**버전**: 1.0.0
