# Application Layer 코딩 컨벤션 검증 결과

**프로젝트**: crawlinghub
**검증 날짜**: 2025-01-23
**검증 범위**: application/src/main/java, application/src/test/java
**검증 명령**: `/cc/application/validate`

---

## ✅ 준수 항목 (통과)

### CQRS 분리
- [✓] Command/Query UseCase 명확히 분리 (`service/command/`, `service/query/`)
- [✓] DTO 네이밍 규칙 준수 (`*Command`, `*Query`, `*Response`)
- [✓] Command UseCase는 상태 변경만, Query UseCase는 조회만

### Assembler 패턴
- [✓] DTO ↔ Domain 변환은 Assembler 사용 (`SellerAssembler`)
- [✓] UseCase에서 직접 변환 없음
- [✓] Assembler 메서드 네이밍 준수 (`toSellerResponse`, `toSellerDetailResponse`)

### Port 패턴
- [✓] Port In 인터페이스 구조 준수 (`port/in/command/`, `port/in/query/`)
- [✓] Port Out 인터페이스 구조 준수 (`port/out/command/`, `port/out/query/`)
- [✓] Port 네이밍 규칙 준수 (`*Port`, `*UseCase`)

### 외부 API 호출
- [✓] `@Transactional` 내부에서 외부 API 호출 없음
- [✓] RestTemplate, WebClient, HttpClient, FeignClient 사용 없음

### Spring 프록시 제약
- [✓] Public 메서드에만 `@Transactional` 사용
- [✓] Private/Final 메서드에 `@Transactional` 없음

---

## ❌ 위반 항목 (리팩토링 필요)

### 1. Transaction 경계 위반 (Zero-Tolerance) 🔴 CRITICAL

**위반 건수**: 4건

#### 위반 1-1: RegisterSellerService에 @Transactional 직접 사용

**파일**: `application/src/main/java/com/ryuqq/crawlinghub/application/seller/service/command/RegisterSellerService.java:45`

```java
// ❌ 위반 (UseCase Service에 @Transactional 직접 사용)
@Service
public class RegisterSellerService implements RegisterSellerUseCase {
    
    @Transactional  // ❌ Zero-Tolerance 위반
    @Override
    public SellerResponse register(RegisterSellerCommand command) {
        // ...
    }
}
```

**개선 방안**:
```java
// ✅ 개선 (TransactionManager 패턴 적용)
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
}

// ✅ TransactionManager 생성
@Component
@Transactional
public class SellerTransactionManager {
    
    private final SellerPersistencePort persistencePort;

    public SellerTransactionManager(SellerPersistencePort persistencePort) {
        this.persistencePort = persistencePort;
    }

    public Seller persist(Seller seller) {
        return persistencePort.persist(seller);
    }
}
```

**심각도**: 🔴 CRITICAL (Zero-Tolerance)
**리팩토링 필요**: 즉시

---

#### 위반 1-2: ChangeSellerStatusService에 @Transactional 직접 사용

**파일**: `application/src/main/java/com/ryuqq/crawlinghub/application/seller/service/command/ChangeSellerStatusService.java:42`

```java
// ❌ 위반
@Service
public class ChangeSellerStatusService implements ChangeSellerStatusUseCase {
    
    @Transactional  // ❌ Zero-Tolerance 위반
    @Override
    public SellerResponse changeStatus(ChangeSellerStatusCommand command) {
        // ...
    }
}
```

**개선 방안**: 위와 동일 (TransactionManager 패턴 적용)

**심각도**: 🔴 CRITICAL (Zero-Tolerance)
**리팩토링 필요**: 즉시

---

#### 위반 1-3: GetSellerService에 @Transactional(readOnly = true) 직접 사용

**파일**: `application/src/main/java/com/ryuqq/crawlinghub/application/seller/service/query/GetSellerService.java:36`

```java
// ❌ 위반 (Query UseCase에도 @Transactional 사용)
@Service
public class GetSellerService implements GetSellerUseCase {
    
    @Transactional(readOnly = true)  // ❌ Zero-Tolerance 위반
    @Override
    public SellerDetailResponse getSeller(GetSellerQuery query) {
        // ...
    }
}
```

**개선 방안**: Query UseCase는 TransactionManager 없이 사용 가능하지만, 일관성을 위해 TransactionManager 패턴 적용 권장

**심각도**: 🔴 CRITICAL (Zero-Tolerance)
**리팩토링 필요**: 즉시

---

#### 위반 1-4: ListSellersService에 @Transactional(readOnly = true) 직접 사용

**파일**: `application/src/main/java/com/ryuqq/crawlinghub/application/seller/service/query/ListSellersService.java:39`

```java
// ❌ 위반
@Service
public class ListSellersService implements ListSellersUseCase {
    
    @Transactional(readOnly = true)  // ❌ Zero-Tolerance 위반
    @Override
    public PageResponse<SellerSummaryResponse> listSellers(ListSellersQuery query) {
        // ...
    }
}
```

**개선 방안**: 위와 동일

**심각도**: 🔴 CRITICAL (Zero-Tolerance)
**리팩토링 필요**: 즉시

---

### 2. TransactionManager 패턴 미적용 🔴 HIGH

**위반 건수**: 1건 (구조적 위반)

**문제점**:
- `application/seller/manager/` 디렉터리 없음
- `SellerTransactionManager` 클래스 없음
- UseCase Service에 직접 `@Transactional` 사용

**개선 방안**:
1. `application/seller/manager/SellerTransactionManager.java` 생성
2. 모든 UseCase Service에서 `@Transactional` 제거
3. TransactionManager를 통한 트랜잭션 관리

**심각도**: 🔴 HIGH
**리팩토링 필요**: 즉시

---

## 📋 리팩토링 우선순위

### Priority 1 (즉시 수정 필요 - Zero-Tolerance)
1. ✅ Transaction 경계 위반 4건
   - RegisterSellerService
   - ChangeSellerStatusService
   - GetSellerService
   - ListSellersService
2. ✅ TransactionManager 패턴 미적용 1건

**총 5건** - 예상 소요 시간: **100분** (약 1시간 40분)

---

## 🎯 리팩토링 PRD 생성

**위반 항목 수**: 5건
**Zero-Tolerance 위반**: 5건

→ **리팩토링 PRD 생성 필수**

---

## 📊 검증 통계

| 항목 | 통과 | 위반 | 심각도 |
|------|------|------|--------|
| Transaction 경계 | 0 | 4 | 🔴 CRITICAL |
| TransactionManager 패턴 | 0 | 1 | 🔴 HIGH |
| CQRS 분리 | ✅ | 0 | - |
| Assembler 패턴 | ✅ | 0 | - |
| Port 패턴 | ✅ | 0 | - |
| 외부 API 호출 | ✅ | 0 | - |
| Spring 프록시 제약 | ✅ | 0 | - |
| **총계** | **5** | **5** | - |

**위반률**: 50% (5/10 검증 항목)
**Zero-Tolerance 위반률**: 100% (5/5 Zero-Tolerance 항목)

---

## 🔍 상세 검증 결과

### Transaction 경계 검증 상세

#### 검증 대상
- `application/src/main/java/**/*Service.java` (UseCase 구현체)

#### 검증 결과
- ❌ `RegisterSellerService`: `@Transactional` 메서드 레벨 사용
- ❌ `ChangeSellerStatusService`: `@Transactional` 메서드 레벨 사용
- ❌ `GetSellerService`: `@Transactional(readOnly = true)` 메서드 레벨 사용
- ❌ `ListSellersService`: `@Transactional(readOnly = true)` 메서드 레벨 사용

#### 규칙 위반 내용
- Zero-Tolerance 규칙: "`@Transactional`은 오직 `*TransactionManager`에만 사용"
- 현재 상태: UseCase Service에 직접 `@Transactional` 사용

---

## ✅ 완료 조건

### Definition of Done
- [ ] 모든 Zero-Tolerance 위반 해결 (5건)
- [ ] `SellerTransactionManager` 생성
- [ ] 모든 UseCase Service에서 `@Transactional` 제거
- [ ] TransactionManager를 통한 트랜잭션 관리
- [ ] 기존 단위 테스트 모두 통과
- [ ] ArchUnit 테스트 통과

### 검증 방법
```bash
# ArchUnit 실행
./gradlew :application:test --tests "*ArchitectureTest"

# 모든 테스트 실행
./gradlew :application:test

# 코딩 컨벤션 재검증
/cc/application/validate
```

---

**생성일**: 2025-01-23
**검증 도구**: `/cc/application/validate`

