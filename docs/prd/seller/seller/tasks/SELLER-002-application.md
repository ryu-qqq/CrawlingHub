# SELLER-002: Seller Application Layer 구현

**Bounded Context**: Seller
**Sub-Context**: Seller (셀러 자체)
**Layer**: Application Layer
**브랜치**: feature/SELLER-002-application

---

## 📝 목적

Seller 관련 Use Case 오케스트레이션.

---

## 🎯 요구사항

### 1. Command Use Cases

#### RegisterSellerUseCase
- **입력**: RegisterSellerCommand (sellerId, name)
- **출력**: SellerId
- **로직**:
  1. 중복 sellerId 검증
  2. Seller 생성 (INACTIVE 상태)
  3. DB 저장

#### UpdateSellerNameUseCase
- **입력**: UpdateSellerNameCommand (sellerId, newName)
- **출력**: void
- **로직**:
  1. Seller 조회
  2. 이름 변경
  3. DB 저장

#### ActivateSellerUseCase
- **입력**: ActivateSellerCommand (sellerId)
- **출력**: void
- **로직**:
  1. Seller 조회
  2. 활성화
  3. DB 저장

#### DeactivateSellerUseCase
- **입력**: DeactivateSellerCommand (sellerId)
- **출력**: void
- **로직**:
  1. Seller 조회
  2. **해당 Seller의 모든 EventBridge가 비활성화되었는지 검증** (CrawlingScheduleQueryPort)
  3. 모든 EventBridge가 비활성화되지 않았으면 예외 발생
  4. 비활성화
  5. DB 저장

### 2. Query Use Cases

#### GetSellerUseCase
- **입력**: SellerId
- **출력**: SellerResponse
- **로직**: sellerId로 Seller 조회

#### ListSellersUseCase
- **입력**: Pagination (page, size)
- **출력**: Page<SellerResponse>
- **로직**: 전체 Seller 페이징 조회

---

## ✅ 완료 조건

- [ ] 4개 Command UseCase 구현 완료
- [ ] 2개 Query UseCase 구현 완료
- [ ] Transaction 경계 검증 완료
- [ ] DeactivateSeller 비즈니스 규칙 검증 완료

---

## 🔗 관련 문서

- **Plan**: docs/prd/seller/seller/plans/SELLER-002-application-plan.md

---

## 📚 참고사항

### RegisterSellerUseCase 구현 예시

```java
@UseCase
@RequiredArgsConstructor
public class RegisterSellerUseCase {
    private final SellerQueryPort sellerQueryPort;
    private final SellerCommandPort sellerCommandPort;

    @Transactional
    public SellerId execute(RegisterSellerCommand command) {
        // 1. 중복 sellerId 검증
        if (sellerQueryPort.existsBySellerId(command.sellerId())) {
            throw new DuplicateSellerIdException("Seller ID already exists: " + command.sellerId());
        }

        // 2. Seller 생성 (INACTIVE 상태)
        Seller seller = Seller.create(
            new SellerId(command.sellerId()),
            command.name()
        );

        // 3. DB 저장
        sellerCommandPort.save(seller);

        return seller.getSellerId();
    }
}

public record RegisterSellerCommand(
    String sellerId,
    String name
) {}
```

### UpdateSellerNameUseCase 구현 예시

```java
@UseCase
@RequiredArgsConstructor
public class UpdateSellerNameUseCase {
    private final SellerQueryPort sellerQueryPort;
    private final SellerCommandPort sellerCommandPort;

    @Transactional
    public void execute(UpdateSellerNameCommand command) {
        // 1. Seller 조회
        Seller seller = sellerQueryPort.findBySellerId(command.sellerId())
            .orElseThrow(() -> new SellerNotFoundException("Seller not found: " + command.sellerId()));

        // 2. 이름 변경
        seller.updateName(command.newName());

        // 3. DB 저장
        sellerCommandPort.save(seller);
    }
}

public record UpdateSellerNameCommand(
    String sellerId,
    String newName
) {}
```

### ActivateSellerUseCase 구현 예시

```java
@UseCase
@RequiredArgsConstructor
public class ActivateSellerUseCase {
    private final SellerQueryPort sellerQueryPort;
    private final SellerCommandPort sellerCommandPort;

    @Transactional
    public void execute(ActivateSellerCommand command) {
        // 1. Seller 조회
        Seller seller = sellerQueryPort.findBySellerId(command.sellerId())
            .orElseThrow(() -> new SellerNotFoundException("Seller not found: " + command.sellerId()));

        // 2. 활성화
        seller.activate();

        // 3. DB 저장
        sellerCommandPort.save(seller);
    }
}

public record ActivateSellerCommand(
    String sellerId
) {}
```

### DeactivateSellerUseCase 구현 예시 (비즈니스 규칙 검증)

```java
@UseCase
@RequiredArgsConstructor
public class DeactivateSellerUseCase {
    private final SellerQueryPort sellerQueryPort;
    private final SellerCommandPort sellerCommandPort;
    private final CrawlingScheduleQueryPort crawlingScheduleQueryPort; // EventBridge 조회

    @Transactional
    public void execute(DeactivateSellerCommand command) {
        // 1. Seller 조회
        Seller seller = sellerQueryPort.findBySellerId(command.sellerId())
            .orElseThrow(() -> new SellerNotFoundException("Seller not found: " + command.sellerId()));

        // 2. 해당 Seller의 모든 EventBridge가 비활성화되었는지 검증
        boolean hasActiveSchedules = crawlingScheduleQueryPort.existsActiveBySellerId(command.sellerId());
        if (hasActiveSchedules) {
            throw new SellerDeactivationNotAllowedException(
                "Cannot deactivate seller. Active EventBridge schedules exist for seller: " + command.sellerId()
            );
        }

        // 3. 비활성화
        seller.deactivate();

        // 4. DB 저장
        sellerCommandPort.save(seller);
    }
}

public record DeactivateSellerCommand(
    String sellerId
) {}
```

### CrawlingScheduleQueryPort (EventBridge 조회용)

```java
public interface CrawlingScheduleQueryPort {
    // Seller의 활성화된 EventBridge 존재 여부
    boolean existsActiveBySellerId(String sellerId);
}
```

### GetSellerUseCase 구현 예시

```java
@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetSellerUseCase {
    private final SellerQueryPort sellerQueryPort;

    public SellerResponse execute(String sellerId) {
        Seller seller = sellerQueryPort.findBySellerId(new SellerId(sellerId))
            .orElseThrow(() -> new SellerNotFoundException("Seller not found: " + sellerId));

        return SellerResponse.from(seller);
    }
}
```

### ListSellersUseCase 구현 예시

```java
@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListSellersUseCase {
    private final SellerQueryPort sellerQueryPort;

    public Page<SellerResponse> execute(Pageable pageable) {
        Page<Seller> sellers = sellerQueryPort.findAll(pageable);
        return sellers.map(SellerResponse::from);
    }
}
```

### SellerResponse DTO

```java
public record SellerResponse(
    String sellerId,
    String name,
    String status,
    Integer totalProductCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static SellerResponse from(Seller seller) {
        return new SellerResponse(
            seller.getSellerIdValue(),
            seller.getName(),
            seller.getStatus().name(),
            seller.getTotalProductCount(),
            seller.getCreatedAt(),
            seller.getUpdatedAt()
        );
    }
}
```

### Port 인터페이스

```java
// Command Port (저장)
public interface SellerCommandPort {
    void save(Seller seller);
    void delete(Seller seller);
}

// Query Port (조회)
public interface SellerQueryPort {
    Optional<Seller> findBySellerId(SellerId sellerId);
    boolean existsBySellerId(String sellerId);
    Page<Seller> findAll(Pageable pageable);
}
```

### Transaction 경계

```java
// ✅ 올바른 예시 - Transaction 내에서 DB 작업만
@Transactional
public void execute(DeactivateSellerCommand command) {
    Seller seller = sellerQueryPort.findBySellerId(command.sellerId()).orElseThrow(...);

    // EventBridge 조회 (DB 조회)
    boolean hasActiveSchedules = crawlingScheduleQueryPort.existsActiveBySellerId(command.sellerId());
    if (hasActiveSchedules) {
        throw new SellerDeactivationNotAllowedException(...);
    }

    seller.deactivate();
    sellerCommandPort.save(seller);
}

// ❌ 잘못된 예시 - Transaction 내 외부 API 호출
@Transactional
public void execute(DeactivateSellerCommand command) {
    Seller seller = sellerQueryPort.findBySellerId(command.sellerId()).orElseThrow(...);

    // 외부 API 호출 (트랜잭션 내 금지!)
    eventBridgeClient.checkActiveRules(seller.getSellerId());

    seller.deactivate();
    sellerCommandPort.save(seller);
}
```

### 중요 변경사항

⚠️ **UpdateSellerIntervalUseCase 제거**:
- Seller는 스케줄링 정보를 관리하지 않음
- EventBridge Context에서 RegisterScheduleUseCase, UpdateScheduleUseCase로 대체

⚠️ **DeactivateSeller 비즈니스 규칙**:
- 모든 EventBridge가 먼저 비활성화되어야 Seller 비활성화 가능
- CrawlingScheduleQueryPort를 통해 활성화된 스케줄 존재 여부 검증
