# ProductGroup 리팩토링 플랜: 3가지 개선

## Task A: ProductGroupImageFactory instanceof 제거

### 문제
`toProductGroupImage`가 제네릭 `<T>` + `instanceof` 체인으로 구현됨. 3개의 ImageCommand가 동일한 필드(`imageType`, `originUrl`, `sortOrder`)를 가지므로 불필요한 복잡성.

### 변경
**파일**: `application/.../factory/ProductGroupImageFactory.java`

- 제네릭 `<T> toProductGroupImages()`, `<T> toProductGroupImage()` 메서드 삭제
- 각 public 메서드(`create`, `createForUpdate`, `createForImageUpdate`)에서 직접 변환 로직 수행
- 공통 변환은 `(ProductGroupId, String imageType, String originUrl, int sortOrder)` 파라미터를 받는 private 메서드로 추출

```java
private ProductGroupImage toImage(ProductGroupId id, String imageType, String originUrl, int sortOrder) {
    return ProductGroupImage.forNew(id, ImageUrl.of(originUrl), ImageType.valueOf(imageType), sortOrder);
}
```

---

## Task B: ProductGroupException 기본 예외 클래스 생성

### 문제
4개의 ProductGroup 예외가 `DomainException`을 직접 상속. 다른 모듈은 모듈별 기본 예외(BrandException, CategoryException 등)를 사용.

### 변경

**1) 새 파일 생성**: `domain/.../productgroup/exception/ProductGroupException.java`
- `BrandException` 패턴 따름
- `DomainException` 상속, `ProductGroupErrorCode` 기반 생성자 3개

```java
public class ProductGroupException extends DomainException {
    public ProductGroupException(ProductGroupErrorCode errorCode) { super(errorCode); }
    public ProductGroupException(ProductGroupErrorCode errorCode, String customMessage) { super(errorCode, customMessage); }
    public ProductGroupException(ProductGroupErrorCode errorCode, Throwable cause) { super(errorCode, cause); }
}
```

**2) 4개 구체 예외 수정** (extends DomainException → extends ProductGroupException):
- `ProductGroupNotFoundException` → super에 `ProductGroupErrorCode` + 메시지 + args 전달
- `ProductGroupNoThumbnailException` → 동일
- `ProductGroupInvalidStatusTransitionException` → 동일
- `ProductGroupInvalidOptionStructureException` → 동일

**주의**: `DomainException`의 `(ErrorCode, String, Map)` 생성자는 `ProductGroupException`에 없으므로, 구체 예외들은 `ProductGroupException`에 `(ProductGroupErrorCode, String, Map)` 생성자를 추가하거나, 각 구체 예외에서 `ProductGroupException(errorCode, message)` + args 별도 처리가 필요.

→ **방안**: `ProductGroupException`에 4번째 생성자 추가: `(ProductGroupErrorCode, String, Map<String, Object>)` → `super(errorCode, message, args)` 호출. 이렇게 하면 기존 구체 예외들의 super 호출을 최소한만 변경.

---

## Task C: SellerOptionGroups VO 생성

### 문제
`List<SellerOptionGroup>`이 여러 곳에서 직접 사용되며, `validateOptionStructure()` 검증이 `ProductGroup` 내부에 있음.

### 변경

**1) 새 파일 생성**: `domain/.../productgroup/vo/SellerOptionGroups.java`
- `ProductGroupImages` 패턴 따름
- `of(OptionType, List<SellerOptionGroup>)` → 검증 + 정렬 적용
- `reconstitute(List<SellerOptionGroup>)` → 검증 스킵 (DB 복원용)
- 내부 검증: `validateOptionStructure(OptionType, List)` 이동
- 조회 메서드: `toList()`, `size()`, `isEmpty()`, `allOptionValues()`, `isFullyMappedToCanonical()`, `totalOptionValueCount()`

**2) ProductGroup 수정** (`domain/.../aggregate/ProductGroup.java`):
- 필드: `List<SellerOptionGroup>` → `SellerOptionGroups`
- `forNew()`: `List<SellerOptionGroup>` 파라미터 → `SellerOptionGroups` 파라미터, 내부 `validateOptionStructure()` 제거 (VO가 담당)
- `reconstitute()`: `List<SellerOptionGroup>` → `SellerOptionGroups.reconstitute(...)` 호출
- `replaceSellerOptionGroups(List)` → `replaceSellerOptionGroups(SellerOptionGroups)`
- `sellerOptionGroups()` → `sellerOptionGroups.toList()` 반환
- `isFullyMappedToCanonical()`, `totalOptionValueCount()` → `sellerOptionGroups` 위임
- `addSellerOptionGroup()` 제거 또는 VO에 위임 (VO는 불변이므로 replace 패턴 사용)

**3) SellerOptionGroupFactory 수정** (`application/.../factory/SellerOptionGroupFactory.java`):
- 반환 타입: `List<SellerOptionGroup>` → `SellerOptionGroups`
- `OptionType` 파라미터 추가 (검증에 필요)
- 마지막에 `SellerOptionGroups.of(optionType, groups)` 반환

**4) ProductGroupCommandFactory 수정** (`application/.../factory/ProductGroupCommandFactory.java`):
- `optionGroupFactory.create()` 호출 시 `OptionType` 전달
- `List<SellerOptionGroup> optionGroups` → `SellerOptionGroups optionGroups`

**5) ProductGroupUpdateBundle 수정** (`application/.../dto/bundle/ProductGroupUpdateBundle.java`):
- `List<SellerOptionGroup> optionGroups` → `SellerOptionGroups optionGroups`

**6) ProductCreationDataFactory 수정** (`application/.../factory/ProductCreationDataFactory.java`):
- `List<SellerOptionGroup> optionGroups` 파라미터 → `SellerOptionGroups optionGroups`
- `optionGroups.toList()` 또는 `optionGroups.allOptionValues()` 사용

**7) ProductGroupCommandFacade 수정** (`application/.../internal/ProductGroupCommandFacade.java`):
- `productGroup.replaceSellerOptionGroups(bundle.optionGroups())` → VO 타입 전달

**8) ProductGroupJpaEntityMapper 수정** (`adapter-out/.../mapper/ProductGroupJpaEntityMapper.java`):
- `toDomain()` 내 `List<SellerOptionGroup>` → `SellerOptionGroups.reconstitute(...)` 사용

**9) ProductGroup.forNew() 시그니처 변경에 따른 테스트 수정**:
- `ProductGroupTest.java`, `ProductGroupFixtures.java` 등

---

## 실행 순서

1. **Task B** (예외 계층) — 독립적, 다른 태스크에 영향 없음
2. **Task A** (ImageFactory instanceof 제거) — 독립적
3. **Task C** (SellerOptionGroups VO) — 가장 영향 범위 넓음, 마지막에 진행

## 검증

- `./gradlew compileJava`
- `./gradlew spotlessApply`
- `./gradlew :domain:test :application:test :adapter-out:persistence-mysql:test`
