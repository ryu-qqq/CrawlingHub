# SELLER-001: Seller Domain Layer 구현

**Bounded Context**: Seller
**Sub-Context**: Seller (셀러 자체)
**Layer**: Domain Layer
**브랜치**: feature/SELLER-001-domain

---

## 📝 목적

Seller Aggregate의 비즈니스 핵심 로직을 담당하는 Domain Layer 구현. 외부 의존성 없이 순수한 셀러 비즈니스 규칙과 도메인 개념을 표현합니다.

**핵심 역할**:
- Seller 생명주기 관리 (등록, 활성화, 비활성화)
- Seller 정보 변경 (이름만 수정 가능)
- 비활성화 조건 검증 (Application Layer에서 처리)
- EventBridge와 완전 분리 (스케줄링 정보 없음)

---

## 🎯 요구사항

### 1. Aggregate: Seller (셀러)

- [ ] **Seller Aggregate 구현**
  - sellerId (SellerId VO, String)
  - name (String)
  - status (SellerStatus Enum)
  - totalProductCount (Integer, 선택적)
  - createdAt (LocalDateTime)
  - updatedAt (LocalDateTime)

- [ ] **비즈니스 규칙**
  - Seller 생성 시 상태 INACTIVE (활성화는 별도 프로세스)
  - 이름 변경만 허용 (크롤링 주기는 EventBridge에서 관리)
  - 비활성화 조건: 모든 EventBridge가 먼저 비활성화되어야 함 (Application Layer에서 검증)

- [ ] **Value Objects**
  - SellerId: String (머스트잇 셀러 ID, 고유값)
  - SellerStatus: Enum (ACTIVE, INACTIVE)

- [ ] **Domain 메서드**
  - `create(sellerId, name)`: Seller 생성 (INACTIVE 상태)
  - `updateName(newName)`: 이름만 변경 가능
  - `activate()`: 활성화
  - `deactivate()`: 비활성화 (조건 검증은 Application Layer)
  - `updateTotalProductCount(count)`: 총 상품 수 업데이트

---

## ⚠️ 제약사항

### Zero-Tolerance 규칙

- [ ] **Lombok 금지**: Pure Java 또는 Record 사용
  - ✅ `public record SellerId(String value) {}`
  - ❌ `@Value public class SellerId { ... }`

- [ ] **Law of Demeter 준수**: Getter 체이닝 금지
  - ✅ `seller.getName()`
  - ❌ `seller.getSellerId().getValue()` (내부에서 처리)

- [ ] **Tell Don't Ask**: 내부 상태 기반 판단
  - ✅ `seller.activate()` (내부에서 상태 전환)
  - ❌ `if (seller.getStatus() == INACTIVE) { seller.setStatus(ACTIVE); }` (외부에서 판단)

- [ ] **Long FK 전략**: 관계 어노테이션 금지
  - ✅ `private SellerId sellerId;`
  - ❌ `@ManyToOne private Seller seller;`

### 테스트 규칙

- [ ] **ArchUnit 테스트 필수**
  - Lombok 사용 금지 검증
  - Getter 체이닝 금지 검증
  - 패키지 의존성 검증

- [ ] **TestFixture 패턴 사용**
  - Domain 객체 생성을 위한 Fixture 클래스
  - 테스트 가독성 향상

- [ ] **테스트 커버리지 > 80%**
  - Domain 메서드 모두 테스트
  - 비즈니스 규칙 검증

---

## ✅ 완료 조건

- [ ] Seller Aggregate 구현 완료
- [ ] 모든 Value Object 구현 완료 (SellerId)
- [ ] SellerStatus Enum 구현 완료
- [ ] 모든 Domain 메서드 구현 완료
- [ ] Unit Test 작성 완료 (커버리지 > 80%)
- [ ] ArchUnit 테스트 통과
- [ ] TestFixture 패턴 적용
- [ ] Zero-Tolerance 규칙 준수

---

## 🔗 관련 문서

- **Plan**: docs/prd/seller/seller/plans/SELLER-001-domain-plan.md
- **Domain Layer 규칙**: docs/coding_convention/02-domain-layer/

---

## 📚 참고사항

### Seller Aggregate 구현 예시

```java
public class Seller {
    private final SellerId sellerId;
    private String name;
    private SellerStatus status;
    private Integer totalProductCount;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Factory Method
    public static Seller create(SellerId sellerId, String name) {
        validateName(name);
        return new Seller(
            sellerId,
            name,
            SellerStatus.INACTIVE, // 등록 시 INACTIVE
            0,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
    }

    // 이름 변경 (유일하게 허용되는 수정)
    public void updateName(String newName) {
        validateName(newName);
        this.name = newName;
        this.updatedAt = LocalDateTime.now();
    }

    // 활성화
    public void activate() {
        if (this.status == SellerStatus.ACTIVE) {
            throw new SellerInvalidStateException("Seller is already active");
        }
        this.status = SellerStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    // 비활성화 (조건 검증은 Application Layer)
    public void deactivate() {
        if (this.status == SellerStatus.INACTIVE) {
            throw new SellerInvalidStateException("Seller is already inactive");
        }
        this.status = SellerStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    // 총 상품 수 업데이트
    public void updateTotalProductCount(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("Product count cannot be negative");
        }
        this.totalProductCount = count;
        this.updatedAt = LocalDateTime.now();
    }

    // 이름 검증
    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Seller name cannot be null or blank");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("Seller name cannot exceed 100 characters");
        }
    }

    // Getters (Law of Demeter 준수)
    public String getSellerIdValue() {
        return this.sellerId.value();
    }

    public String getName() {
        return this.name;
    }

    public SellerStatus getStatus() {
        return this.status;
    }

    public boolean isActive() {
        return this.status == SellerStatus.ACTIVE;
    }

    public Integer getTotalProductCount() {
        return this.totalProductCount;
    }
}
```

### SellerId VO 구현 예시

```java
public record SellerId(String value) {
    public SellerId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("SellerId cannot be null or blank");
        }
        if (value.length() > 50) {
            throw new IllegalArgumentException("SellerId cannot exceed 50 characters");
        }
    }
}
```

### SellerStatus Enum

```java
public enum SellerStatus {
    ACTIVE,   // 활성화 (EventBridge 등록 가능)
    INACTIVE  // 비활성화 (EventBridge 등록 불가)
}
```

### Unit Test 예시

```java
class SellerTest {

    @Test
    void 셀러_생성_시_INACTIVE_상태() {
        // When: Seller 생성
        Seller seller = Seller.create(new SellerId("SELLER-001"), "테스트 셀러");

        // Then: INACTIVE 상태
        assertThat(seller.getStatus()).isEqualTo(SellerStatus.INACTIVE);
        assertThat(seller.isActive()).isFalse();
    }

    @Test
    void 이름_변경_성공() {
        // Given: Seller 생성
        Seller seller = SellerFixture.createInactive("SELLER-001", "원래 이름");

        // When: 이름 변경
        seller.updateName("새 이름");

        // Then: 이름 변경됨
        assertThat(seller.getName()).isEqualTo("새 이름");
    }

    @Test
    void 활성화_성공() {
        // Given: INACTIVE Seller
        Seller seller = SellerFixture.createInactive("SELLER-001", "테스트 셀러");

        // When: 활성화
        seller.activate();

        // Then: ACTIVE 상태
        assertThat(seller.getStatus()).isEqualTo(SellerStatus.ACTIVE);
        assertThat(seller.isActive()).isTrue();
    }

    @Test
    void 이미_활성화된_셀러_활성화_시도_시_예외() {
        // Given: ACTIVE Seller
        Seller seller = SellerFixture.createActive("SELLER-001", "테스트 셀러");

        // When & Then: 예외 발생
        assertThatThrownBy(() -> seller.activate())
            .isInstanceOf(SellerInvalidStateException.class)
            .hasMessageContaining("already active");
    }

    @Test
    void 비활성화_성공() {
        // Given: ACTIVE Seller
        Seller seller = SellerFixture.createActive("SELLER-001", "테스트 셀러");

        // When: 비활성화
        seller.deactivate();

        // Then: INACTIVE 상태
        assertThat(seller.getStatus()).isEqualTo(SellerStatus.INACTIVE);
        assertThat(seller.isActive()).isFalse();
    }
}
```

### TestFixture 예시

```java
public class SellerFixture {

    public static Seller createInactive(String sellerId, String name) {
        return Seller.create(new SellerId(sellerId), name);
    }

    public static Seller createActive(String sellerId, String name) {
        Seller seller = Seller.create(new SellerId(sellerId), name);
        seller.activate();
        return seller;
    }
}
```

### 중요 변경사항

⚠️ **CrawlingInterval VO 제거**:
- Seller는 스케줄링 정보를 가지지 않음
- EventBridge Context에서 `crawler.vo.CrawlingInterval` 사용
- Seller와 EventBridge 완전 분리

⚠️ **비활성화 조건 검증**:
- Domain Layer: 상태 전환만 처리
- Application Layer: EventBridge 비활성화 여부 검증
