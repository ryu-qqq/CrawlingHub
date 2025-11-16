# Domain Layer 코딩 컨벤션 검증 결과

**프로젝트**: crawlinghub
**검증 날짜**: 2025-11-16
**검증 범위**: domain/src/main/java, domain/src/test/java

---

## ✅ 준수 항목 (통과)

### Zero-Tolerance 규칙

#### Aggregate
- [✓] **Lombok 금지**: Domain layer에 Lombok 어노테이션 없음
- [✓] **Law of Demeter**: Getter 체이닝 없음 (`getCrawlingIntervalDays()` 패턴 준수)
- [✓] **Setter 금지**: public setter 메서드 없음
- [✓] **Tell Don't Ask**: 비즈니스 로직이 도메인 객체 내부에 캡슐화됨

#### Value Object
- [✓] **Record 사용**: ProductId, OutboxId 등 Record로 구현
- [✓] **불변성**: 모든 VO가 불변 객체
- [✓] **검증 로직**: Compact constructor로 범위 검증 수행

#### Exception
- [✓] **RuntimeException 상속**: DomainException이 RuntimeException 상속
- [✓] **명확한 에러 메시지**: ErrorCode 인터페이스로 일관성 유지

#### 테스트
- [✓] **Given-When-Then 구조**: 모든 테스트가 BDD 스타일 준수
- [✓] **경계값 테스트**: @ParameterizedTest로 범위 외 값 검증
- [✓] **ArchUnit 테스트**: 아키텍처 규칙 자동 검증 시스템 구축

---

## ❌ 위반 항목 (리팩토링 필요)

### 1️⃣ Aggregate Root 컨벤션 위반 (11건)

**심각도**: 🔴 **HIGH** (Zero-Tolerance 규칙 포함)

#### 1.1 정적 팩토리 메서드 패턴 위반

**위반 내용**: Aggregate Root가 `create()` 메서드 사용, 표준 패턴 미준수

**영향 받는 파일**:
- `domain/src/main/java/com/ryuqq/crawlinghub/domain/aggregate/Product.java`
- `domain/src/main/java/com/ryuqq/crawlinghub/domain/aggregate/ProductOutbox.java`
- `domain/src/main/java/com/ryuqq/crawlinghub/domain/aggregate/Seller.java`
- `domain/src/main/java/com/ryuqq/crawlinghub/domain/aggregate/CrawlerTask.java`
- `domain/src/main/java/com/ryuqq/crawlinghub/domain/aggregate/UserAgent.java`

**표준 패턴** (docs/coding_convention/02-domain-layer/aggregate/aggregate-guide.md):
```java
// ✅ 표준 패턴
public static Product forNew(ItemNo itemNo, SellerId sellerId) {
    return new Product(itemNo, sellerId);
}

public static Product of(ProductId productId, ItemNo itemNo, ...) {
    return new Product(productId, itemNo, ...);
}

public static Product reconstitute(ProductId productId, ItemNo itemNo, ...) {
    return new Product(productId, itemNo, ...);
}

// ❌ 현재 구현
public static Product create(ItemNo itemNo, SellerId sellerId) {
    return new Product(itemNo, sellerId);
}
```

**패턴 의미**:
- `forNew()`: 새로운 엔티티 생성 (ID 자동 생성, createdAt/updatedAt 자동 설정)
- `of()`: 불변 속성만으로 재구성 (테스트용, ID 포함)
- `reconstitute()`: DB에서 조회 후 재구성 (모든 필드 포함)

**리팩토링 필요**:
- `create()` → `forNew()`로 이름 변경
- `of()` 메서드 추가 (TestFixture용)
- `reconstitute()` 메서드 추가 (Repository 조회용)

---

#### 1.2 Clock 타입 필드 누락

**위반 내용**: `LocalDateTime.now()` 직접 호출, Clock 주입 미사용

**영향 받는 파일**: 모든 Aggregate Root 파일

**표준 패턴**:
```java
// ✅ 표준 패턴
public class Product {
    private final Clock clock;

    private Product(ItemNo itemNo, SellerId sellerId, Clock clock) {
        this.clock = clock;
        this.createdAt = LocalDateTime.now(clock);
        this.updatedAt = LocalDateTime.now(clock);
    }

    public static Product forNew(ItemNo itemNo, SellerId sellerId) {
        return new Product(itemNo, sellerId, Clock.systemUTC());
    }

    public static Product reconstitute(..., Clock clock) {
        return new Product(..., clock);
    }
}

// ❌ 현재 구현
this.createdAt = LocalDateTime.now();  // 테스트 불가능
```

**이유**:
- **테스트 가능성**: 테스트에서 시간을 고정할 수 있음
- **시간대 일관성**: Clock.systemUTC()로 UTC 기준 통일
- **DDD 원칙**: 시간도 도메인 규칙의 일부

---

#### 1.3 ID 필드 final 누락

**위반 내용**: ProductId, OutboxId 등 ID 필드가 final이 아님

**영향 받는 파일**: 모든 Aggregate Root

```java
// ✅ 표준
private final ProductId productId;

// ❌ 현재
private final ProductId productId;  // (이건 맞음)
// 다른 Aggregate에서는 누락되었을 수 있음
```

---

#### 1.4 비즈니스 메서드 네이밍 위반

**위반 내용**: 일부 메서드가 명확한 동사로 시작하지 않음

**예시**:
```java
// ❌ 모호함
public boolean isComplete()  // 비즈니스 로직인지 getter인지 불명확

// ✅ 명확함
public boolean canRetry()  // Tell Don't Ask - 명확히 비즈니스 로직
public void activate()     // 명령형 동사
```

---

### 2️⃣ Value Object 컨벤션 위반 (4건)

**심각도**: 🟡 **MEDIUM**

#### 2.1 정적 팩토리 메서드 패턴 위반

**위반 내용**: Enum VO가 `of()` 메서드 미제공

**영향 받는 파일**:
- `domain/src/main/java/com/ryuqq/crawlinghub/domain/vo/OutboxStatus.java`
- `domain/src/main/java/com/ryuqq/crawlinghub/domain/vo/SellerStatus.java`
- `domain/src/main/java/com/ryuqq/crawlinghub/domain/vo/CrawlerTaskStatus.java`
- `domain/src/main/java/com/ryuqq/crawlinghub/domain/vo/OutboxEventType.java`

**표준 패턴**:
```java
// ✅ 표준 (Enum VO는 of() 필수)
public enum OutboxStatus {
    WAITING, SENDING, COMPLETED, FAILED;

    public static OutboxStatus of(String value) {
        return Arrays.stream(values())
            .filter(status -> status.name().equals(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Invalid status: " + value));
    }
}
```

---

#### 2.2 ID VO forNew()/isNew() 메서드 누락

**위반 내용**: ID VO가 `forNew()`와 `isNew()` 메서드 미제공

**영향 받는 파일**:
- `ProductId.java`
- `OutboxId.java`
- `SellerId.java`
- `TaskId.java`
- `UserAgentId.java`

**표준 패턴**:
```java
// ✅ 표준 (ID VO는 forNew() + isNew() 필수)
public record ProductId(UUID value) {

    private static final UUID NEW_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    public static ProductId forNew() {
        return new ProductId(NEW_ID);
    }

    public static ProductId generate() {
        return new ProductId(UUID.randomUUID());
    }

    public boolean isNew() {
        return NEW_ID.equals(value);
    }
}

// ❌ 현재 (generate()만 있음)
public static ProductId generate() {
    return new ProductId(UUID.randomUUID());
}
```

**이유**:
- **DDD 패턴**: 신규 엔티티와 영속 엔티티 구분
- **일관성**: 모든 ID VO가 동일한 인터페이스 제공
- **명확성**: `isNew()` 메서드로 신규 여부 명시적 확인

---

### 3️⃣ Exception 컨벤션 위반 (19건)

**심각도**: 🔴 **HIGH** (Zero-Tolerance)

#### 3.1 ErrorCode Enum 누락

**위반 내용**: ErrorCode 인터페이스를 구현한 Enum이 없음

**표준 패턴**:
```java
// ✅ 표준 (docs/coding_convention/02-domain-layer/exception/domain-exception-guide.md)
package com.ryuqq.crawlinghub.domain.seller.exception;

public enum SellerErrorCode implements ErrorCode {
    SELLER_NOT_FOUND("SELLER-001", 404, "Seller not found"),
    INVALID_CRAWLING_INTERVAL("SELLER-002", 400, "Crawling interval must be 1-30 days");

    private final String code;
    private final int httpStatus;
    private final String message;

    SellerErrorCode(String code, int httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public int getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
```

---

#### 3.2 Concrete Exception 클래스 누락

**위반 내용**: DomainException을 상속한 구체적 예외 클래스가 없음

**표준 패턴**:
```java
// ✅ 표준
package com.ryuqq.crawlinghub.domain.seller.exception;

public class SellerException extends DomainException {

    private final SellerErrorCode errorCode;

    public SellerException(SellerErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    @Override
    public String code() {
        return errorCode.getCode();
    }

    @Override
    public Map<String, Object> args() {
        return Map.of();
    }
}
```

---

#### 3.3 패키지 구조 위반

**위반 내용**: ErrorCode와 Exception이 `domain.common`에 있음, Bounded Context별 분리 필요

**표준 패키지 구조**:
```
domain/
├── seller/
│   ├── aggregate/Seller.java
│   ├── exception/
│   │   ├── SellerErrorCode.java
│   │   └── SellerException.java
├── product/
│   ├── aggregate/Product.java
│   ├── exception/
│   │   ├── ProductErrorCode.java
│   │   └── ProductException.java
└── common/
    ├── DomainException.java
    └── ErrorCode.java
```

---

### 4️⃣ TestFixture 컨벤션 위반 (3건)

**심각도**: 🟡 **MEDIUM**

#### 4.1 정적 팩토리 메서드 패턴 위반

**위반 내용**: Fixture가 `default*()` 메서드 사용, 표준 패턴 미준수

**영향 받는 파일**:
- `domain/src/testFixtures/java/com/ryuqq/crawlinghub/domain/fixture/ProductFixture.java`
- `domain/src/testFixtures/java/com/ryuqq/crawlinghub/domain/fixture/ProductOutboxFixture.java`
- `domain/src/testFixtures/java/com/ryuqq/crawlinghub/domain/fixture/SellerFixture.java`

**표준 패턴**:
```java
// ✅ 표준
public class ProductFixture {

    // forNew() - 새 엔티티 생성 (ID 자동 생성)
    public static Product forNew() {
        return Product.forNew(ItemNo.of(123456L), SellerId.forNew());
    }

    // of() - 불변 속성으로 재구성
    public static Product of(ProductId productId, ItemNo itemNo, SellerId sellerId) {
        return Product.of(productId, itemNo, sellerId);
    }

    // reconstitute() - 완전한 엔티티 재구성 (DB 조회 시뮬레이션)
    public static Product reconstitute(ProductId productId, ...) {
        return Product.reconstitute(productId, ...);
    }
}

// ❌ 현재
public static Product defaultProduct() {
    return Product.create(...);
}
```

---

## 📋 리팩토링 우선순위

### Priority 1 (즉시 수정 필요) - Zero-Tolerance 위반

1. **Aggregate Root 정적 팩토리 메서드 패턴** (5 files)
   - `create()` → `forNew()` 이름 변경
   - `of()` 메서드 추가
   - `reconstitute()` 메서드 추가

2. **Clock 타입 필드 추가** (5 files)
   - 모든 Aggregate Root에 Clock 필드 추가
   - `LocalDateTime.now()` → `LocalDateTime.now(clock)` 변경
   - 정적 팩토리 메서드에서 Clock 주입

3. **Exception 체계 구축** (Bounded Context별)
   - `SellerErrorCode.java` 생성
   - `SellerException.java` 생성
   - `ProductErrorCode.java` 생성
   - `ProductException.java` 생성

### Priority 2 (권장)

1. **Value Object of() 메서드 추가** (4 Enum files)
   - OutboxStatus, SellerStatus, CrawlerTaskStatus, OutboxEventType

2. **ID VO forNew()/isNew() 추가** (5 files)
   - ProductId, OutboxId, SellerId, TaskId, UserAgentId

3. **TestFixture 패턴 통일** (3 files)
   - `default*()` → `forNew()/of()/reconstitute()` 변경

### Priority 3 (선택)

1. **비즈니스 메서드 네이밍 개선**
2. **Javadoc 추가 (누락된 메서드)**

---

## 📊 위반 요약

| 카테고리 | Zero-Tolerance | 권장 | 선택 | 합계 |
|---------|----------------|------|------|------|
| **Aggregate Root** | 7건 | 4건 | 0건 | 11건 |
| **Value Object** | 0건 | 4건 | 0건 | 4건 |
| **Exception** | 19건 | 0건 | 0건 | 19건 |
| **TestFixture** | 0건 | 3건 | 0건 | 3건 |
| **합계** | **26건** | **11건** | **0건** | **37건** |

---

## 🎯 리팩토링 PRD 생성 권장

**Zero-Tolerance 위반**: 26건
**총 위반 항목**: 37건

→ **리팩토링 PRD 생성 필수**

---

## ✅ 완료 조건 (Definition of Done)

### 필수 조건
- [ ] 모든 Zero-Tolerance 위반 해결 (26건)
- [ ] ArchUnit 테스트 통과 (52 tests, 0 failed)
- [ ] 기존 단위 테스트 모두 통과
- [ ] Javadoc 업데이트 (패턴 변경사항 반영)

### 검증 방법
```bash
# ArchUnit 전체 실행
./gradlew :domain:test --tests "*ArchTest"

# 단위 테스트 전체 실행
./gradlew :domain:test

# 리팩토링 후 재검증
/cc:domain:validate
```

---

## 📊 예상 메트릭

**예상 커밋 수**: ~50개 (TDD 사이클 기준, Tidy First 포함)
**예상 소요 시간**:
- Priority 1: 26건 × 15분 = 6.5시간
- Priority 2: 11건 × 15분 = 2.75시간
- **총 예상 시간**: 약 9.25시간

**TDD 사이클 예상**:
- Structural Changes (Tidy First): 15개 커밋
- Test (Red): 26개 커밋
- Implementation (Green): 26개 커밋
- 총 67개 커밋

---

## 📌 참고 문서

### 프로젝트 컨벤션
- `docs/coding_convention/02-domain-layer/domain-guide.md`
- `docs/coding_convention/02-domain-layer/aggregate/aggregate-guide.md`
- `docs/coding_convention/02-domain-layer/vo/vo-guide.md`
- `docs/coding_convention/02-domain-layer/exception/domain-exception-guide.md`
- `.claude/CLAUDE.md` (TDD + Tidy First 철학)

### 컨벤션 자동 검증
- `domain/src/test/java/com/ryuqq/crawlinghub/domain/architecture/aggregate/AggregateRootArchTest.java`
- `domain/src/test/java/com/ryuqq/crawlinghub/domain/architecture/vo/VOArchTest.java`
- `domain/src/test/java/com/ryuqq/crawlinghub/domain/architecture/exception/ExceptionArchTest.java`

---

**생성 날짜**: 2025-11-16
**검증 커맨드**: `/cc:domain:validate`
**다음 단계**: 리팩토링 PRD 작성 (`/cc:domain:refactor-prd`)
