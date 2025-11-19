# Application Layer 코딩 컨벤션 검증 결과 및 리팩토링 PRD

**이슈 키**: REFACTOR-APP-001
**생성 날짜**: 2025-01-XX
**우선순위**: CRITICAL
**예상 소요 시간**: 약 4-6시간

---

## 📋 리팩토링 개요

**목적**: Application Layer 코딩 컨벤션 위반 사항 해결
**범위**: `application/src/main/java/com/ryuqq/crawlinghub/application/scheduler/`
**위반 항목 수**: 6건
**Zero-Tolerance 위반**: 6건 (모든 UseCase에 @Transactional 직접 사용)

---

## ✅ 준수 항목 (통과)

### Assembler 패턴
- [✓] DTO ↔ Domain 변환은 Assembler 사용 (`SchedulerAssembler`)
- [✓] Assembler 메서드 네이밍 준수 (`toScheduler`, `toResponse`, `toDetailResponse`)

### CQRS 분리
- [✓] Command/Query UseCase 명확히 분리
  - Command: `RegisterSchedulerUseCase`, `UpdateSchedulerUseCase`, `DeactivateSchedulerUseCase`
  - Query: `GetSchedulerUseCase`, `ListSchedulersUseCase`, `GetSchedulerHistoryUseCase`
- [✓] DTO 네이밍 규칙 준수 (`*Command`, `*Query`, `*Response`)

### Port 패턴
- [✓] Port Out 인터페이스 네이밍 규칙 준수 (`*PersistencePort`, `*QueryPort`, `*ClientPort`)
- [✓] Port In 구조 준수 (UseCase 클래스)

### 외부 API 호출
- [✓] `@Transactional` 내부에서 외부 API 직접 호출 없음
  - `EventBridgeClientPort`는 Outbox 패턴으로 처리 (`TransactionSynchronizationAdapter`)
  - `SlackClientPort`는 `OutboxEventProcessor`에서 비동기 처리

---

## ❌ 위반 항목 (리팩토링 필요)

### 1. Transaction 경계 위반 (Zero-Tolerance) - 6건

**위반 파일들**:
1. `RegisterSchedulerUseCase.java:52`
2. `UpdateSchedulerUseCase.java:45`
3. `DeactivateSchedulerUseCase.java:45`
4. `GetSchedulerUseCase.java:25`
5. `ListSchedulersUseCase.java:29`
6. `GetSchedulerHistoryUseCase.java:29`

**위반 내용**:
```java
// ❌ 위반 (UseCase에 @Transactional 직접 사용)
@Service
public class RegisterSchedulerUseCase {
    @Transactional
    public SchedulerResponse execute(RegisterSchedulerCommand command) {
        // ...
    }
}
```

**코딩 컨벤션 요구사항**:
- `@Transactional`은 오직 `*TransactionManager`에만 사용
- UseCase는 비즈니스 로직만 담당, Transaction 경계는 Manager에서 관리

**심각도**: 🔴 CRITICAL (Zero-Tolerance)
**리팩토링 필요**: 즉시

---

## 🎯 리팩토링 목표

### 필수 목표 (Zero-Tolerance)
- [ ] Transaction 경계 위반 해결 (6건)
  - [ ] `SchedulerTransactionManager` 생성
  - [ ] 모든 UseCase에서 `@Transactional` 제거
  - [ ] TransactionManager에서 UseCase 호출 및 Transaction 경계 관리

---

## 📝 상세 리팩토링 계획

### Task 1: SchedulerTransactionManager 생성 및 Transaction 경계 이동

**파일**: `application/src/main/java/com/ryuqq/crawlinghub/application/scheduler/manager/SchedulerTransactionManager.java` (신규 생성)

**Before**:
```java
// RegisterSchedulerUseCase.java
@Service
public class RegisterSchedulerUseCase {
    @Transactional
    public SchedulerResponse execute(RegisterSchedulerCommand command) {
        // ...
    }
}
```

**After**:
```java
// 1. UseCase에서 @Transactional 제거
@Service
public class RegisterSchedulerUseCase {
    // @Transactional 제거
    public SchedulerResponse execute(RegisterSchedulerCommand command) {
        // ...
    }
}

// 2. TransactionManager 생성
@Component
public class SchedulerTransactionManager {
    private final RegisterSchedulerUseCase registerSchedulerUseCase;
    private final UpdateSchedulerUseCase updateSchedulerUseCase;
    private final DeactivateSchedulerUseCase deactivateSchedulerUseCase;
    private final GetSchedulerUseCase getSchedulerUseCase;
    private final ListSchedulersUseCase listSchedulersUseCase;
    private final GetSchedulerHistoryUseCase getSchedulerHistoryUseCase;

    public SchedulerTransactionManager(
        RegisterSchedulerUseCase registerSchedulerUseCase,
        UpdateSchedulerUseCase updateSchedulerUseCase,
        DeactivateSchedulerUseCase deactivateSchedulerUseCase,
        GetSchedulerUseCase getSchedulerUseCase,
        ListSchedulersUseCase listSchedulersUseCase,
        GetSchedulerHistoryUseCase getSchedulerHistoryUseCase
    ) {
        this.registerSchedulerUseCase = registerSchedulerUseCase;
        this.updateSchedulerUseCase = updateSchedulerUseCase;
        this.deactivateSchedulerUseCase = deactivateSchedulerUseCase;
        this.getSchedulerUseCase = getSchedulerUseCase;
        this.listSchedulersUseCase = listSchedulersUseCase;
        this.getSchedulerHistoryUseCase = getSchedulerHistoryUseCase;
    }

    @Transactional
    public SchedulerResponse registerScheduler(RegisterSchedulerCommand command) {
        return registerSchedulerUseCase.execute(command);
    }

    @Transactional
    public SchedulerResponse updateScheduler(UpdateSchedulerCommand command) {
        return updateSchedulerUseCase.execute(command);
    }

    @Transactional
    public SchedulerResponse deactivateScheduler(DeactivateSchedulerCommand command) {
        return deactivateSchedulerUseCase.execute(command);
    }

    @Transactional(readOnly = true)
    public SchedulerDetailResponse getScheduler(GetSchedulerQuery query) {
        return getSchedulerUseCase.execute(query);
    }

    @Transactional(readOnly = true)
    public PageResult<SchedulerSummaryResponse> listSchedulers(ListSchedulersQuery query) {
        return listSchedulersUseCase.execute(query);
    }

    @Transactional(readOnly = true)
    public PageResult<SchedulerHistoryResponse> getSchedulerHistory(GetSchedulerHistoryQuery query) {
        return getSchedulerHistoryUseCase.execute(query);
    }
}
```

**TDD 사이클**:
1. **Struct**: `struct: @Transactional을 TransactionManager로 이동` (동작 변경 없음)
2. **Test**: `test: SchedulerTransactionManager 트랜잭션 경계 테스트`
3. **Green**: `feat: SchedulerTransactionManager 구현`

**영향 범위**:
- UseCase 클래스 6개 수정 (`@Transactional` 제거)
- TransactionManager 클래스 1개 생성
- Controller/Adapter에서 UseCase 대신 TransactionManager 호출로 변경 필요

---

### Task 2: UseCase에서 @Transactional 제거

**수정 대상 파일**:
1. `RegisterSchedulerUseCase.java`
2. `UpdateSchedulerUseCase.java`
3. `DeactivateSchedulerUseCase.java`
4. `GetSchedulerUseCase.java`
5. `ListSchedulersUseCase.java`
6. `GetSchedulerHistoryUseCase.java`

**변경 사항**:
- `@Transactional` 어노테이션 제거
- `import org.springframework.transaction.annotation.Transactional;` 제거 (사용하지 않는 경우)

---

### Task 3: Controller/Adapter에서 TransactionManager 사용

**영향 범위 확인 필요**:
- REST API Controller에서 UseCase 직접 호출 → TransactionManager 호출로 변경
- 테스트 코드에서 UseCase 직접 호출 → TransactionManager 호출로 변경 (또는 UseCase 직접 테스트 유지)

**참고**: UseCase는 여전히 `@Service`로 등록되어 있으므로, TransactionManager에서 의존성 주입 가능

---

## ✅ 완료 조건

### Definition of Done
- [ ] 모든 UseCase에서 `@Transactional` 제거 완료
- [ ] `SchedulerTransactionManager` 생성 및 모든 UseCase 메서드 래핑
- [ ] Transaction 경계 테스트 통과
- [ ] ArchUnit 테스트 통과
- [ ] 기존 단위 테스트 모두 통과
- [ ] Controller/Adapter에서 TransactionManager 사용 확인

### 검증 방법
```bash
# ArchUnit 실행
./gradlew :application:test --tests "*Arch*Test"

# Transaction 경계 검증
./gradlew :application:test --tests "*Transaction*Test"

# 전체 테스트 실행
./gradlew :application:test

# 코딩 컨벤션 재검증
/cc/application/validate
```

---

## 📊 예상 메트릭

**예상 커밋 수**: 8-10개 (TDD 사이클 + Tidy First)
- Struct: 1개 (TransactionManager 생성)
- Test: 2개 (TransactionManager 테스트, UseCase 테스트 수정)
- Green: 1개 (TransactionManager 구현)
- Refactor: 2개 (UseCase 수정, Controller 수정)
- Tidy: 2개 (테스트 Fixture 정리, 문서 업데이트)

**예상 소요 시간**: 약 4-6시간
- TransactionManager 설계 및 구현: 2시간
- UseCase 수정: 1시간
- 테스트 수정 및 작성: 2시간
- Controller/Adapter 수정: 1시간

**우선순위별 분포**:
- Priority 1 (CRITICAL): 6건 (Transaction 경계 위반)

---

## 🔄 리팩토링 순서

### Phase 1: TransactionManager 생성 (Struct)
1. `SchedulerTransactionManager` 클래스 생성
2. UseCase 의존성 주입
3. `@Transactional` 메서드 래핑

### Phase 2: UseCase 수정 (Struct)
1. 모든 UseCase에서 `@Transactional` 제거
2. 불필요한 import 제거

### Phase 3: 테스트 작성 및 수정 (Test + Green)
1. `SchedulerTransactionManagerTest` 작성
2. 기존 UseCase 테스트 수정 (TransactionManager 사용 또는 UseCase 직접 테스트 유지)

### Phase 4: Controller/Adapter 수정 (Refactor)
1. REST API Controller에서 TransactionManager 사용
2. 통합 테스트 수정

### Phase 5: 정리 (Tidy)
1. 테스트 Fixture 정리
2. 문서 업데이트

---

## 📌 참고 사항

### TransactionManager 패턴
- TransactionManager는 단일 Out Port의 트랜잭션 로직을 캡슐화
- UseCase는 비즈니스 로직만 담당
- Transaction 경계는 Manager에서 관리

### 기존 코드와의 호환성
- UseCase는 여전히 `@Service`로 등록되어 있으므로, 기존 테스트 코드는 UseCase를 직접 테스트할 수 있음
- Controller/Adapter는 TransactionManager를 사용하도록 변경 필요

### 외부 API 호출
- 현재 코드는 `@Transactional` 내부에서 외부 API를 직접 호출하지 않음 (Outbox 패턴 사용)
- 이 부분은 준수하고 있으므로 추가 수정 불필요

---

## 🎯 다음 단계

1. **리팩토링 PR 승인 후 진행**
2. **Phase 1부터 순차적으로 진행**
3. **각 Phase 완료 후 검증 실행**
4. **최종 검증 후 PR 머지**

