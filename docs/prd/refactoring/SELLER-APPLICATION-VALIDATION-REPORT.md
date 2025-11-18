# Application Layer 코딩 컨벤션 검증 결과

**프로젝트**: crawlinghub
**검증 날짜**: 2025-11-17
**검증 범위**: `application/src/main/java/com/ryuqq/crawlinghub/application/seller`
**검증 기준**: `docs/coding_convention/03-application-layer/`

---

## ✅ 준수 항목 (통과)

### Transaction 경계 - 외부 API 호출
- [✓] `RegisterSellerService`: 외부 API 호출이 Transaction 외부에서 수행
  - `executeExternalOperations()` 메서드에서 `eventBridgePort.createRule()` 호출
  - `executeInTransaction()` 외부에서 실행 (올바름)
- [✓] `UpdateSellerIntervalService`: 외부 API 호출이 Transaction 외부에서 수행
  - `executeExternalOperations()` 메서드에서 `eventBridgePort.updateRule()` 호출
  - `executeInTransaction()` 외부에서 실행 (올바름)
- [✓] `UpdateSellerNameService`: 외부 API 호출 없음 (DB 작업만)

### Assembler 패턴
- [✓] `SellerAssembler` 존재 및 사용
  - `RegisterSellerService`: `sellerAssembler.toResponse()` 사용
  - `UpdateSellerIntervalService`: `sellerAssembler.toResponse()` 사용
- [✓] Assembler 메서드 네이밍 준수: `toResponse()` (Domain → Response)

### CQRS 분리
- [✓] Command UseCase 명확히 분리
  - `RegisterSellerUseCase` (port/in/command)
  - `UpdateSellerIntervalUseCase` (port/in/command)
  - `UpdateSellerNameUseCase` (port/in/command)
- [✓] DTO 패키지 분리
  - Command: `dto/command/*Command.java`
  - Response: `dto/response/*Response.java`

### Port 패턴
- [✓] Port In 인터페이스 올바른 위치
  - Command UseCase: `port/in/command/`
- [✓] Port Out 인터페이스 네이밍 규칙 준수
  - `SellerPersistencePort` (Command Port)
  - `SellerQueryPort` (Query Port)
  - `EventBridgePort` (External Port)

---

## ❌ 위반 항목 (리팩토링 필요)

### 1. Spring Proxy 제약 위반 (Zero-Tolerance) 🔴 CRITICAL

**파일**: 
- `application/src/main/java/com/ryuqq/crawlinghub/application/seller/service/RegisterSellerService.java:62, 85`
- `application/src/main/java/com/ryuqq/crawlinghub/application/seller/service/UpdateSellerIntervalService.java:63, 86`

**위반 내용**:

```java
// ❌ 위반: 같은 클래스 내부에서 private 메서드에 @Transactional 호출
@Service
public class RegisterSellerService implements RegisterSellerUseCase {
    
    @Override
    public SellerResponse execute(RegisterSellerCommand command) {
        // 같은 클래스 내부에서 private 메서드 호출 → Spring Proxy 우회!
        Seller savedSeller = executeInTransaction(command);  // ❌
        executeExternalOperations(command);
        return sellerAssembler.toResponse(savedSeller);
    }

    @Transactional  // ❌ 이 @Transactional이 작동하지 않음!
    private Seller executeInTransaction(RegisterSellerCommand command) {
        // ...
    }
}
```

**문제점**:
1. Spring AOP는 **프록시를 통해서만** `@Transactional`이 작동합니다
2. 같은 클래스 내부에서 `this.executeInTransaction()`을 호출하면 프록시를 우회합니다
3. 결과적으로 `@Transactional`이 적용되지 않아 **트랜잭션이 시작되지 않습니다**
4. 이는 **Zero-Tolerance 위반**입니다

**개선 방안**:

#### 옵션 1: Public 메서드에 @Transactional 적용 (권장)

```java
// ✅ 개선: Public 메서드에 @Transactional 적용
@Service
public class RegisterSellerService implements RegisterSellerUseCase {
    
    private final SellerQueryPort sellerQueryPort;
    private final SellerPersistencePort sellerPersistencePort;
    private final EventBridgePort eventBridgePort;
    private final SellerAssembler sellerAssembler;

    @Override
    @Transactional
    public SellerResponse execute(RegisterSellerCommand command) {
        // 1. Transaction 내부: DB 저장
        Seller savedSeller = saveSellerInTransaction(command);

        // 2. Transaction 외부: 외부 API 호출 (트랜잭션 커밋 후)
        executeExternalOperations(command);

        return sellerAssembler.toResponse(savedSeller);
    }

    // Private 메서드로 분리 (Transaction 외부)
    private Seller saveSellerInTransaction(RegisterSellerCommand command) {
        validateDuplicateSellerId(command.sellerId());
        
        CrawlingInterval crawlingInterval = new CrawlingInterval(command.crawlingIntervalDays());
        Seller seller = Seller.forNew(
            SellerId.forNew(),
            command.name(),
            crawlingInterval
        );

        SellerId savedSellerId = sellerPersistencePort.persist(seller);
        return Seller.reconstitute(
            savedSellerId,
            command.name(),
            crawlingInterval,
            seller.getStatus(),
            seller.getTotalProductCount()
        );
    }

    private void executeExternalOperations(RegisterSellerCommand command) {
        eventBridgePort.createRule(
            command.sellerId(),
            command.crawlingIntervalDays()
        );
    }

    private void validateDuplicateSellerId(String sellerId) {
        // ...
    }
}
```

#### 옵션 2: TransactionManager 패턴 (복잡한 경우)

```java
// ✅ 개선: TransactionManager로 분리
@Service
public class SellerTransactionManager {
    
    private final SellerQueryPort sellerQueryPort;
    private final SellerPersistencePort sellerPersistencePort;

    @Transactional
    public Seller saveSeller(RegisterSellerCommand command) {
        // Transaction 내부 로직
        // ...
    }
}

@Service
public class RegisterSellerService implements RegisterSellerUseCase {
    
    private final SellerTransactionManager sellerTransactionManager;
    private final EventBridgePort eventBridgePort;
    private final SellerAssembler sellerAssembler;

    @Override
    public SellerResponse execute(RegisterSellerCommand command) {
        // 1. Transaction 내부 (다른 클래스의 public 메서드 호출)
        Seller savedSeller = sellerTransactionManager.saveSeller(command);

        // 2. Transaction 외부
        executeExternalOperations(command);

        return sellerAssembler.toResponse(savedSeller);
    }

    private void executeExternalOperations(RegisterSellerCommand command) {
        eventBridgePort.createRule(
            command.sellerId(),
            command.crawlingIntervalDays()
        );
    }
}
```

**심각도**: 🔴 **CRITICAL** (Zero-Tolerance)
**리팩토링 필요**: **즉시**
**영향**: 트랜잭션이 작동하지 않아 데이터 일관성 보장 실패 위험

---

### 2. Assembler 메서드 누락 (Medium) 🟡 MEDIUM

**파일**: `application/src/main/java/com/ryuqq/crawlinghub/application/seller/assembler/SellerAssembler.java`

**위반 내용**:

```java
// ⚠️ 부분적 구현: toDomain() 메서드 누락
@Component
public class SellerAssembler {
    
    // ✅ 존재: toResponse() 메서드
    public SellerResponse toResponse(Seller seller) {
        // ...
    }

    // ❌ 누락: toDomain() 메서드 (Command → Domain 변환)
    // RegisterSellerService에서 직접 변환하고 있음
}
```

**현재 코드**:

```java
// RegisterSellerService.java:91-96
// ❌ Assembler 없이 직접 변환
CrawlingInterval crawlingInterval = new CrawlingInterval(command.crawlingIntervalDays());
Seller seller = Seller.forNew(
    SellerId.forNew(),
    command.name(),
    crawlingInterval
);
```

**개선 방안**:

```java
// ✅ 개선: SellerAssembler에 toDomain() 메서드 추가
@Component
public class SellerAssembler {
    
    public SellerResponse toResponse(Seller seller) {
        // ...
    }

    // 추가: Command → Domain 변환
    public Seller toDomain(RegisterSellerCommand command) {
        CrawlingInterval crawlingInterval = new CrawlingInterval(command.crawlingIntervalDays());
        return Seller.forNew(
            SellerId.forNew(),
            command.name(),
            crawlingInterval
        );
    }
}

// RegisterSellerService에서 사용
@Service
public class RegisterSellerService implements RegisterSellerUseCase {
    
    @Override
    @Transactional
    public SellerResponse execute(RegisterSellerCommand command) {
        validateDuplicateSellerId(command.sellerId());
        
        // ✅ Assembler 사용
        Seller seller = sellerAssembler.toDomain(command);
        SellerId savedSellerId = sellerPersistencePort.persist(seller);
        
        Seller savedSeller = Seller.reconstitute(
            savedSellerId,
            command.name(),
            seller.getCrawlingInterval(),
            seller.getStatus(),
            seller.getTotalProductCount()
        );

        executeExternalOperations(command);
        return sellerAssembler.toResponse(savedSeller);
    }
}
```

**심각도**: 🟡 **MEDIUM**
**리팩토링 필요**: 권장
**영향**: Assembler 패턴 일관성 유지

---

## 📋 리팩토링 우선순위

### Priority 1 (즉시 수정 필요) - Zero-Tolerance 위반
1. ✅ **Spring Proxy 제약 위반 해결** (2건)
   - `RegisterSellerService`: Public 메서드에 @Transactional 적용
   - `UpdateSellerIntervalService`: Public 메서드에 @Transactional 적용
   - **예상 소요 시간**: 40분 (각 20분)

### Priority 2 (권장)
1. ✅ **Assembler 메서드 추가** (1건)
   - `SellerAssembler.toDomain()` 메서드 추가
   - `RegisterSellerService`에서 Assembler 사용으로 리팩토링
   - **예상 소요 시간**: 20분

---

## ✅ 완료 조건

### Definition of Done
- [ ] 모든 Zero-Tolerance 위반 해결
  - [ ] `RegisterSellerService`: Public 메서드에 @Transactional 적용
  - [ ] `UpdateSellerIntervalService`: Public 메서드에 @Transactional 적용
  - [ ] 트랜잭션 작동 검증 테스트 추가
- [ ] Assembler 패턴 일관성 유지
  - [ ] `SellerAssembler.toDomain()` 메서드 추가
  - [ ] `RegisterSellerService`에서 Assembler 사용
- [ ] 기존 단위 테스트 모두 통과

### 검증 방법
```bash
# 단위 테스트 실행
./gradlew :application:test --tests "*SellerServiceTest"

# ArchUnit 테스트 실행
./gradlew :application:test --tests "*ArchitectureTest"

# 트랜잭션 동작 검증 (Integration Test)
./gradlew :application:test --tests "*TransactionTest"
```

---

## 📊 예상 메트릭

**예상 커밋 수**: 4건
- `struct: RegisterSellerService Transaction 경계 수정` (동작 변경 없음)
- `test: RegisterSellerService 트랜잭션 동작 검증 테스트 추가`
- `feat: UpdateSellerIntervalService Transaction 경계 수정`
- `feat: SellerAssembler.toDomain() 메서드 추가`

**예상 소요 시간**: 60분
- Priority 1: 40분 (Spring Proxy 제약 위반 해결)
- Priority 2: 20분 (Assembler 메서드 추가)

---

## 📌 참고 문서

- `docs/coding_convention/03-application-layer/application-guide.md`
- `docs/coding_convention/03-application-layer/manager/transaction-manager-guide.md`
- `.claude/CLAUDE.md` (Spring 프록시 제약사항)

---

## 🎯 다음 단계

1. **즉시 수정**: Spring Proxy 제약 위반 해결 (Priority 1)
2. **권장 수정**: Assembler 메서드 추가 (Priority 2)
3. **검증**: 테스트 실행 및 트랜잭션 동작 확인

---

**생성 일시**: 2025-11-17
**검증 도구**: Manual Review + Code Analysis

