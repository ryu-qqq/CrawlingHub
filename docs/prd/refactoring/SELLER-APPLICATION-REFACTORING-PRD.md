# Application Layer 리팩토링 PRD

**이슈 키**: REFACTOR-SELLER-APP-001
**생성 날짜**: 2025-01-23
**우선순위**: 🔴 CRITICAL
**예상 소요 시간**: 100분 (약 1시간 40분)

---

## 📋 리팩토링 개요

**목적**: Application Layer 코딩 컨벤션 Zero-Tolerance 규칙 위반 사항 해결
**범위**: `application/src/main/java/com/ryuqq/crawlinghub/application/seller/`
**위반 항목 수**: 5건
**Zero-Tolerance 위반**: 5건 (Transaction 경계 위반 4건 + TransactionManager 패턴 미적용 1건)

---

## 🎯 리팩토링 목표

### 필수 목표 (Zero-Tolerance)
- [ ] Transaction 경계 위반 해결 (4건)
  - [ ] RegisterSellerService에서 `@Transactional` 제거
  - [ ] ChangeSellerStatusService에서 `@Transactional` 제거
  - [ ] GetSellerService에서 `@Transactional(readOnly = true)` 제거
  - [ ] ListSellersService에서 `@Transactional(readOnly = true)` 제거
- [ ] TransactionManager 패턴 적용 (1건)
  - [ ] `SellerTransactionManager` 생성
  - [ ] 모든 UseCase Service에서 TransactionManager 사용

---

## 📝 상세 리팩토링 계획

### Task 1: SellerTransactionManager 생성

**목적**: Transaction 경계를 관리하는 전용 클래스 생성

**파일**: `application/src/main/java/com/ryuqq/crawlinghub/application/seller/manager/SellerTransactionManager.java`

**구현 내용**:
```java
package com.ryuqq.crawlinghub.application.seller.manager;

import com.ryuqq.crawlinghub.application.seller.port.out.command.SellerPersistencePort;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seller Transaction Manager
 * - SellerPersistencePort만 의존
 * - 트랜잭션 짧게 유지
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
@Transactional
public class SellerTransactionManager {

    private final SellerPersistencePort persistencePort;

    public SellerTransactionManager(SellerPersistencePort persistencePort) {
        this.persistencePort = persistencePort;
    }

    /**
     * Seller 저장 (트랜잭션)
     *
     * @param seller 저장할 Seller
     * @return 저장된 Seller (ID 포함)
     */
    public Seller persist(Seller seller) {
        return persistencePort.persist(seller);
    }
}
```

**TDD 사이클**:
1. **Test**: `test: SellerTransactionManager 트랜잭션 경계 테스트`
2. **Green**: `feat: SellerTransactionManager 구현`

---

### Task 2: RegisterSellerService 리팩토링

**파일**: `application/src/main/java/com/ryuqq/crawlinghub/application/seller/service/command/RegisterSellerService.java`

**Before**:
```java
@Service
public class RegisterSellerService implements RegisterSellerUseCase {
    
    private final SellerQueryPort sellerQueryPort;
    private final SellerPersistencePort sellerPersistencePort;
    private final SellerAssembler sellerAssembler;

    @Transactional  // ❌ 제거 필요
    @Override
    public SellerResponse register(RegisterSellerCommand command) {
        // ...
        sellerPersistencePort.persist(seller);  // ❌ 직접 호출
        return sellerAssembler.toSellerResponse(seller);
    }
}
```

**After**:
```java
@Service
public class RegisterSellerService implements RegisterSellerUseCase {
    
    private final SellerTransactionManager transactionManager;
    private final SellerQueryPort sellerQueryPort;
    private final SellerAssembler sellerAssembler;

    public RegisterSellerService(
        SellerTransactionManager transactionManager,
        SellerQueryPort sellerQueryPort,
        SellerAssembler sellerAssembler
    ) {
        this.transactionManager = transactionManager;
        this.sellerQueryPort = sellerQueryPort;
        this.sellerAssembler = sellerAssembler;
    }

    @Override  // ✅ @Transactional 제거
    public SellerResponse register(RegisterSellerCommand command) {
        ensureNoDuplicateMustItSellerId(command.mustItSellerId());
        ensureNoDuplicateSellerName(command.sellerName());

        Seller seller = Seller.forNew(
            MustItSellerId.of(command.mustItSellerId()),
            command.sellerName()
        );

        // ✅ TransactionManager 사용
        Seller savedSeller = transactionManager.persist(seller);
        return sellerAssembler.toSellerResponse(savedSeller);
    }
    
    // ... 나머지 메서드 동일
}
```

**TDD 사이클**:
1. **Test**: `test: RegisterSellerService TransactionManager 사용 검증`
2. **Struct**: `struct: @Transactional을 TransactionManager로 이동`
3. **Green**: `feat: RegisterSellerService TransactionManager 적용`

---

### Task 3: ChangeSellerStatusService 리팩토링

**파일**: `application/src/main/java/com/ryuqq/crawlinghub/application/seller/service/command/ChangeSellerStatusService.java`

**Before**:
```java
@Service
public class ChangeSellerStatusService implements ChangeSellerStatusUseCase {
    
    private final SellerQueryPort sellerQueryPort;
    private final SellerPersistencePort sellerPersistencePort;
    private final SchedulerQueryPort schedulerQueryPort;
    private final SellerAssembler sellerAssembler;

    @Transactional  // ❌ 제거 필요
    @Override
    public SellerResponse changeStatus(ChangeSellerStatusCommand command) {
        // ...
        sellerPersistencePort.persist(seller);  // ❌ 직접 호출
        return sellerAssembler.toSellerResponse(seller);
    }
}
```

**After**:
```java
@Service
public class ChangeSellerStatusService implements ChangeSellerStatusUseCase {
    
    private final SellerTransactionManager transactionManager;
    private final SellerQueryPort sellerQueryPort;
    private final SchedulerQueryPort schedulerQueryPort;
    private final SellerAssembler sellerAssembler;

    public ChangeSellerStatusService(
        SellerTransactionManager transactionManager,
        SellerQueryPort sellerQueryPort,
        SchedulerQueryPort schedulerQueryPort,
        SellerAssembler sellerAssembler
    ) {
        this.transactionManager = transactionManager;
        this.sellerQueryPort = sellerQueryPort;
        this.schedulerQueryPort = schedulerQueryPort;
        this.sellerAssembler = sellerAssembler;
    }

    @Override  // ✅ @Transactional 제거
    public SellerResponse changeStatus(ChangeSellerStatusCommand command) {
        Seller seller = sellerQueryPort.findById(SellerId.of(command.sellerId()))
            .orElseThrow(() -> new SellerNotFoundException(command.sellerId()));

        if (command.targetStatus() == SellerStatus.INACTIVE) {
            deactivateSeller(seller, command.sellerId());
        } else {
            seller.activate();
        }

        // ✅ TransactionManager 사용
        Seller savedSeller = transactionManager.persist(seller);
        return sellerAssembler.toSellerResponse(savedSeller);
    }
    
    // ... 나머지 메서드 동일
}
```

**TDD 사이클**:
1. **Test**: `test: ChangeSellerStatusService TransactionManager 사용 검증`
2. **Struct**: `struct: @Transactional을 TransactionManager로 이동`
3. **Green**: `feat: ChangeSellerStatusService TransactionManager 적용`

---

### Task 4: GetSellerService 리팩토링

**파일**: `application/src/main/java/com/ryuqq/crawlinghub/application/seller/service/query/GetSellerService.java`

**Before**:
```java
@Service
public class GetSellerService implements GetSellerUseCase {
    
    @Transactional(readOnly = true)  // ❌ 제거 필요
    @Override
    public SellerDetailResponse getSeller(GetSellerQuery query) {
        // ...
    }
}
```

**After**:
```java
@Service
public class GetSellerService implements GetSellerUseCase {
    
    private final SellerQueryPort sellerQueryPort;
    private final SchedulerQueryPort schedulerQueryPort;
    private final SellerAssembler sellerAssembler;

    // ✅ @Transactional 제거 (Query는 TransactionManager 없이도 가능하지만 일관성 위해 제거)
    @Override
    public SellerDetailResponse getSeller(GetSellerQuery query) {
        // ... 동일
    }
}
```

**참고**: Query UseCase는 읽기 전용이므로 TransactionManager 없이도 동작 가능하지만, 일관성을 위해 `@Transactional` 제거

**TDD 사이클**:
1. **Test**: `test: GetSellerService @Transactional 제거 검증`
2. **Struct**: `struct: Query UseCase에서 @Transactional 제거`
3. **Green**: `feat: GetSellerService TransactionManager 패턴 준수`

---

### Task 5: ListSellersService 리팩토링

**파일**: `application/src/main/java/com/ryuqq/crawlinghub/application/seller/service/query/ListSellersService.java`

**Before**:
```java
@Service
public class ListSellersService implements ListSellersUseCase {
    
    @Transactional(readOnly = true)  // ❌ 제거 필요
    @Override
    public PageResponse<SellerSummaryResponse> listSellers(ListSellersQuery query) {
        // ...
    }
}
```

**After**:
```java
@Service
public class ListSellersService implements ListSellersUseCase {
    
    private final SellerQueryPort sellerQueryPort;
    private final SchedulerQueryPort schedulerQueryPort;
    private final SellerAssembler sellerAssembler;

    // ✅ @Transactional 제거
    @Override
    public PageResponse<SellerSummaryResponse> listSellers(ListSellersQuery query) {
        // ... 동일
    }
}
```

**TDD 사이클**:
1. **Test**: `test: ListSellersService @Transactional 제거 검증`
2. **Struct**: `struct: Query UseCase에서 @Transactional 제거`
3. **Green**: `feat: ListSellersService TransactionManager 패턴 준수`

---

## ✅ 완료 조건

### Definition of Done
- [ ] `SellerTransactionManager` 생성 및 테스트 통과
- [ ] 모든 UseCase Service에서 `@Transactional` 제거
- [ ] Command UseCase에서 TransactionManager 사용
- [ ] Query UseCase에서 `@Transactional` 제거
- [ ] 기존 단위 테스트 모두 통과
- [ ] ArchUnit 테스트 통과
- [ ] 코딩 컨벤션 재검증 통과 (`/cc/application/validate`)

### 검증 방법
```bash
# 모든 테스트 실행
./gradlew :application:test

# ArchUnit 실행
./gradlew :application:test --tests "*ArchitectureTest"

# 코딩 컨벤션 재검증
/cc/application/validate
```

---

## 📊 예상 메트릭

**예상 커밋 수**: 10개
- Task 1: 2개 (Test, Green)
- Task 2: 3개 (Test, Struct, Green)
- Task 3: 3개 (Test, Struct, Green)
- Task 4: 1개 (Struct)
- Task 5: 1개 (Struct)

**예상 소요 시간**: 100분
- Task 1: 20분
- Task 2: 25분
- Task 3: 25분
- Task 4: 15분
- Task 5: 15분

**우선순위별 분포**:
- Priority 1 (CRITICAL): 5건 (100분)

---

## 🔄 리팩토링 순서

1. **Step 1**: SellerTransactionManager 생성 (Task 1)
2. **Step 2**: RegisterSellerService 리팩토링 (Task 2)
3. **Step 3**: ChangeSellerStatusService 리팩토링 (Task 3)
4. **Step 4**: GetSellerService 리팩토링 (Task 4)
5. **Step 5**: ListSellersService 리팩토링 (Task 5)
6. **Step 6**: 최종 검증 (`/cc/application/validate`)

---

## 📌 참고 사항

### TransactionManager 패턴 가이드
- `docs/coding_convention/03-application-layer/manager/transaction-manager-guide.md`
- TransactionManager는 단일 Persistence Port만 의존
- 트랜잭션은 짧게 유지 (저장만 담당)

### 검증 리포트
- `docs/prd/refactoring/SELLER-APPLICATION-VALIDATION-REPORT.md`

---

**작성자**: Development Team
**생성일**: 2025-01-23
**버전**: 1.0.0

