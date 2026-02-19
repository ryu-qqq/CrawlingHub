# Product (SKU) API Endpoints

## 요약

| 분류 | 개수 |
|------|------|
| Query (조회) | 0개 |
| Command (명령) | 4개 |
| **합계** | **4개** |

**Base Path**: `/api/v1/market/products`

> Query Controller는 현재 미구현 상태입니다. ProductCommandController만 존재하며, 상품 조회는 ProductGroup 단위의 복합 조회로 처리됩니다.

---

## Command Endpoints

### C1. updatePrice (상품 가격 수정)

- **Path**: `PATCH /api/v1/market/products/{productId}/price`
- **Controller**: `ProductCommandController`
- **Method**: `updatePrice(Long productId, UpdateProductPriceApiRequest request)`
- **Request**:
  - `@PathVariable Long productId`
  - `@RequestBody UpdateProductPriceApiRequest`
- **Response**: `Void` (204 No Content)
- **UseCase**: `UpdateProductPriceUseCase`
- **권한**: `@PreAuthorize("@access.isSellerOwnerOr(#productId, 'product:write')")` + `@RequirePermission("product:write")`

**설명**: 개별 상품(SKU)의 정가와 판매가를 수정합니다. 소유자 검증 또는 `product:write` 권한 필요.

**Request Body**:
```json
{
  "regularPrice": 100000,
  "currentPrice": 90000
}
```

**필드 설명**:
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `regularPrice` | Integer | Y | 정가 |
| `currentPrice` | Integer | Y | 판매가 (정가 이하) |

**HTTP Status**:
- `204 NO CONTENT`: 수정 성공
- `400 BAD REQUEST`: 잘못된 요청
- `404 NOT FOUND`: 상품을 찾을 수 없음

---

### C2. updateStock (상품 재고 수정)

- **Path**: `PATCH /api/v1/market/products/{productId}/stock`
- **Controller**: `ProductCommandController`
- **Method**: `updateStock(Long productId, UpdateProductStockApiRequest request)`
- **Request**:
  - `@PathVariable Long productId`
  - `@RequestBody UpdateProductStockApiRequest`
- **Response**: `Void` (204 No Content)
- **UseCase**: `UpdateProductStockUseCase`
- **권한**: `@PreAuthorize("@access.isSellerOwnerOr(#productId, 'product:write')")` + `@RequirePermission("product:write")`

**설명**: 개별 상품(SKU)의 재고 수량을 수정합니다. 소유자 검증 또는 `product:write` 권한 필요.

**Request Body**:
```json
{
  "stockQuantity": 100
}
```

**필드 설명**:
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `stockQuantity` | Integer | Y | 재고 수량 (0 이상) |

**HTTP Status**:
- `204 NO CONTENT`: 수정 성공
- `400 BAD REQUEST`: 잘못된 요청
- `404 NOT FOUND`: 상품을 찾을 수 없음

---

### C3. batchChangeStatus (상품 배치 상태 변경)

- **Path**: `PATCH /api/v1/market/products/product-groups/{productGroupId}/status`
- **Controller**: `ProductCommandController`
- **Method**: `batchChangeStatus(Long productGroupId, BatchChangeProductStatusApiRequest request)`
- **Request**:
  - `@PathVariable Long productGroupId`
  - `@RequestBody BatchChangeProductStatusApiRequest`
- **Response**: `Void` (204 No Content)
- **UseCase**: `BatchChangeProductStatusUseCase`
- **권한**: `@PreAuthorize("hasAuthority('product:write')")` + `@RequirePermission("product:write")`

**설명**: 특정 상품 그룹(ProductGroup) 내 여러 상품(SKU)의 상태를 일괄 변경합니다. 인증 컨텍스트에서 셀러 ID를 추출하여 소유권을 검증합니다.

**Request Body**:
```json
{
  "productIds": [10, 20, 30],
  "targetStatus": "ACTIVE"
}
```

**필드 설명**:
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `productIds` | List\<Long\> | Y | 상태 변경 대상 상품 ID 목록 |
| `targetStatus` | String | Y | 변경할 상태 (`ACTIVE`, `INACTIVE`, `SOLDOUT`) |

**HTTP Status**:
- `204 NO CONTENT`: 상태 변경 성공
- `400 BAD REQUEST`: 잘못된 요청
- `403 FORBIDDEN`: 소유권 검증 실패

---

### C4. updateProducts (상품 일괄 수정)

- **Path**: `PATCH /api/v1/market/products/product-groups/{productGroupId}`
- **Controller**: `ProductCommandController`
- **Method**: `updateProducts(Long productGroupId, UpdateProductsApiRequest request)`
- **Request**:
  - `@PathVariable Long productGroupId`
  - `@RequestBody UpdateProductsApiRequest`
- **Response**: `Void` (204 No Content)
- **UseCase**: `UpdateProductsUseCase`
- **권한**: `@PreAuthorize("@access.isSellerOwnerOr(#productGroupId, 'product:write')")` + `@RequirePermission("product:write")`

**설명**: 상품 그룹 하위 상품들의 가격, 재고, SKU 코드, 정렬 순서를 일괄 수정합니다. 옵션 그룹/값의 추가, 수정, 삭제를 diff 방식으로 처리합니다. 신규 상품 추가 및 기존 상품 삭제도 이 엔드포인트에서 함께 처리됩니다.

**Request Body**:
```json
{
  "optionGroups": [
    {
      "sellerOptionGroupId": 1,
      "optionGroupName": "색상",
      "canonicalOptionGroupId": 10,
      "optionValues": [
        {
          "sellerOptionValueId": 1,
          "optionValueName": "빨강",
          "canonicalOptionValueId": 100,
          "sortOrder": 0
        },
        {
          "sellerOptionValueId": null,
          "optionValueName": "파랑",
          "canonicalOptionValueId": 101,
          "sortOrder": 1
        }
      ]
    }
  ],
  "products": [
    {
      "productId": 10,
      "skuCode": "SKU-001",
      "regularPrice": 100000,
      "currentPrice": 90000,
      "stockQuantity": 100,
      "sortOrder": 1,
      "optionIndices": [0]
    },
    {
      "productId": null,
      "skuCode": "SKU-002",
      "regularPrice": 100000,
      "currentPrice": 90000,
      "stockQuantity": 50,
      "sortOrder": 2,
      "optionIndices": [1]
    }
  ]
}
```

**필드 설명 - optionGroups**:
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `sellerOptionGroupId` | Long | N | 기존 옵션 그룹 ID (신규이면 null) |
| `optionGroupName` | String | Y | 옵션 그룹명 |
| `canonicalOptionGroupId` | Long | N | 표준 옵션 그룹 ID |
| `optionValues` | List | Y | 옵션 값 목록 (최소 1개) |
| `optionValues[].sellerOptionValueId` | Long | N | 기존 옵션 값 ID (신규이면 null) |
| `optionValues[].optionValueName` | String | Y | 옵션 값명 |
| `optionValues[].canonicalOptionValueId` | Long | N | 표준 옵션 값 ID |
| `optionValues[].sortOrder` | Integer | Y | 정렬 순서 (0 이상) |

**필드 설명 - products**:
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `productId` | Long | N | 수정 대상 상품 ID (신규이면 null) |
| `skuCode` | String | Y | SKU 코드 |
| `regularPrice` | Integer | Y | 정가 |
| `currentPrice` | Integer | Y | 판매가 (정가 이하) |
| `stockQuantity` | Integer | Y | 재고 수량 (0 이상) |
| `sortOrder` | Integer | Y | 정렬 순서 (0 이상) |
| `optionIndices` | List\<Integer\> | Y | optionValues 내 인덱스 목록 |

**HTTP Status**:
- `204 NO CONTENT`: 수정 성공
- `400 BAD REQUEST`: 잘못된 요청
- `404 NOT FOUND`: 상품 그룹을 찾을 수 없음

---

## 엔드포인트 일람표

| # | Method | Path | Controller | UseCase | 분류 |
|---|--------|------|------------|---------|------|
| C1 | PATCH | `/products/{productId}/price` | ProductCommandController | UpdateProductPriceUseCase | Command |
| C2 | PATCH | `/products/{productId}/stock` | ProductCommandController | UpdateProductStockUseCase | Command |
| C3 | PATCH | `/products/product-groups/{productGroupId}/status` | ProductCommandController | BatchChangeProductStatusUseCase | Command |
| C4 | PATCH | `/products/product-groups/{productGroupId}` | ProductCommandController | UpdateProductsUseCase | Command |

> 전체 경로: Base Path(`/api/v1/market/products`) + 위 Path

---

## 아키텍처 준수사항

### 컨트롤러 규칙
- ✅ **API-CTR-001**: `@RestController` 어노테이션 사용
- ✅ **API-CTR-002**: DELETE 메서드 금지 (소프트 삭제는 PATCH 사용)
- ✅ **API-CTR-003**: UseCase (Port-In) 인터페이스 의존
- ✅ **API-CTR-004**: `ResponseEntity<Void>` 래핑
- ✅ **API-CTR-005**: Controller에 `@Transactional` 금지
- ✅ **API-CTR-007**: Controller에 비즈니스 로직 포함 금지
- ✅ **API-CTR-009**: `@Valid` 어노테이션 필수
- ✅ **API-CTR-010**: CQRS Controller 분리 (현재 Command만 존재)
- ✅ **API-CTR-012**: URL 경로 소문자 + 복수형 (`/products`)

### DTO 규칙
- ✅ **API-DTO-001**: Record 타입 필수
- ✅ **API-DTO-003**: Validation 어노테이션 사용 (`@NotNull`, `@NotEmpty`, `@Min`)
- ✅ **API-DTO-006**: 복잡한 구조는 중첩 Record로 표현 (`UpdateProductsApiRequest` 내부 Record)
- ✅ **API-DTO-007**: `@Schema` 어노테이션 사용

### 엔드포인트 규칙
- ✅ **API-END-001**: Endpoints final class + private 생성자
- ✅ **API-END-002**: static final 상수 사용
- ✅ **API-END-003**: Path Variable 상수화

---

## 관련 UseCase

### Command UseCases
- `UpdateProductPriceUseCase`: 개별 상품 가격 수정
- `UpdateProductStockUseCase`: 개별 상품 재고 수정
- `BatchChangeProductStatusUseCase`: 상품 배치 상태 변경 (ProductGroup 단위)
- `UpdateProductsUseCase`: 상품 + 옵션 일괄 수정 (ProductGroup 단위)

### Query UseCases (미구현)
- 현재 Product(SKU) 단위 조회 API 없음
- 상품 조회는 ProductGroup 조회 시 함께 반환되는 구조로 처리

---

## 에러 매핑

`ProductErrorMapper`가 `PRD-` 접두사 에러 코드를 처리합니다.

| 예외 클래스 | HTTP Status | Title |
|------------|-------------|-------|
| `ProductNotFoundException` | 404 | Product Not Found |
| `ProductOwnershipViolationException` | 403 | Product Ownership Violation |
| 기타 `DomainException` | 도메인 정의 | Product Error |

---

## 권한 요구사항

| 엔드포인트 | 권한 조건 |
|-----------|----------|
| C1 updatePrice | 상품 소유자 OR `product:write` |
| C2 updateStock | 상품 소유자 OR `product:write` |
| C3 batchChangeStatus | `product:write` (셀러 ID는 인증 컨텍스트에서 추출) |
| C4 updateProducts | 상품 그룹 소유자 OR `product:write` |

- C1, C2, C4: `@access.isSellerOwnerOr(#id, 'product:write')` - 소유자이거나 권한 보유 시 허용
- C3: `hasAuthority('product:write')` - 권한 보유자만 허용 (소유권은 `MarketAccessChecker`로 별도 검증)

---

## 향후 개선사항

1. **Query Controller 구현 필요**: 상품(SKU) 단위 조회 API (`GET /products/{productId}`)
2. **상품 목록 조회**: ProductGroup 하위 상품 목록 페이징 조회 API
3. **상품 검색**: 셀러별/상태별 상품 검색 API

---

**분석 일시**: 2026-02-18
**분석 대상**: `/adapter-in/rest-api/src/main/java/com/ryuqq/marketplace/adapter/in/rest/product/`
**총 엔드포인트**: 4개 (Query 0개, Command 4개)
