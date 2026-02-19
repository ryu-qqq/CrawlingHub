# ProductGroup API Endpoints

## 요약

| 분류 | 개수 |
|------|------|
| Query (조회) | 2개 |
| Command (명령) | 5개 |
| **합계** | **7개** |

**Base Path**: `/api/v1/market/product-groups`

---

## Query Endpoints

### Q1. search (상품 그룹 목록 조회)

- **Path**: `GET /api/v1/market/product-groups`
- **Controller**: `ProductGroupQueryController`
- **Method**: `search(SearchProductGroupsApiRequest request)`
- **Request**: `@Valid SearchProductGroupsApiRequest` (Query Parameters / @ModelAttribute)
- **Response**: `ApiResponse<PageApiResponse<ProductGroupListApiResponse>>`
- **UseCase**: `SearchProductGroupByOffsetUseCase`
- **권한**: `product-group:read`

**설명**: 복합 조건으로 상품 그룹 목록을 페이지 기반(Offset)으로 조회합니다.

**Query Parameters**:
| 파라미터 | 타입 | 필수 | 설명 | 예시 |
|---------|------|------|------|------|
| `statuses` | `List<String>` | X | 상태 필터 (DRAFT, ACTIVE, INACTIVE, SOLDOUT, DELETED) | `ACTIVE` |
| `sellerIds` | `List<Long>` | X | 셀러 ID 필터 | `[1, 2]` |
| `brandIds` | `List<Long>` | X | 브랜드 ID 필터 | `[10]` |
| `categoryIds` | `List<Long>` | X | 카테고리 ID 필터 | `[100]` |
| `productGroupIds` | `List<Long>` | X | 상품 그룹 ID 필터 | `[1000]` |
| `searchField` | String | X | 검색 필드 (NAME, CATEGORY_NAME, BRAND_NAME) | `NAME` |
| `searchWord` | String | X | 검색어 | `나이키` |
| `sortKey` | String | X | 정렬 기준 (createdAt, updatedAt, name) | `createdAt` |
| `sortDirection` | String | X | 정렬 방향 (ASC, DESC) | `DESC` |
| `page` | Integer | X | 페이지 번호 (0부터 시작, 기본값: 0) | `0` |
| `size` | Integer | X | 페이지 크기 (1~100) | `20` |

**Response 구조**:
```json
{
  "items": [
    {
      "id": 1001,
      "sellerId": 1,
      "sellerName": "나이키코리아",
      "brandId": 10,
      "brandName": "Nike",
      "categoryId": 100,
      "categoryName": "운동화",
      "categoryDisplayPath": "스포츠/레저 > 신발 > 운동화",
      "categoryDepth": 2,
      "department": "스포츠",
      "categoryGroup": "신발",
      "productGroupName": "나이키 에어맥스 270",
      "optionType": "COMBINATION",
      "status": "ACTIVE",
      "thumbnailUrl": "https://example.com/thumb.jpg",
      "productCount": 12,
      "minPrice": 89000,
      "maxPrice": 139000,
      "maxDiscountRate": 20,
      "optionGroups": [
        { "optionGroupName": "사이즈", "optionValues": ["250", "260", "270"] }
      ],
      "createdAt": "2026-01-15T10:00:00+09:00",
      "updatedAt": "2026-02-01T12:00:00+09:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 150,
  "totalPages": 8
}
```

**HTTP Status**:
- `200 OK`: 조회 성공

---

### Q2. getById (상품 그룹 상세 조회)

- **Path**: `GET /api/v1/market/product-groups/{productGroupId}`
- **Controller**: `ProductGroupQueryController`
- **Method**: `getById(Long productGroupId)`
- **Request**: `@PathVariable Long productGroupId`
- **Response**: `ApiResponse<ProductGroupDetailApiResponse>`
- **UseCase**: `GetProductGroupUseCase`
- **권한**: `product-group:read`

**설명**: 상품 그룹 ID로 상세 정보(기본 정보, 이미지, 옵션-상품 매트릭스, 배송/환불 정책, 상세설명, 고시정보)를 조회합니다.

**Response 구조**:
```json
{
  "id": 1001,
  "sellerId": 1,
  "sellerName": "나이키코리아",
  "brandId": 10,
  "brandName": "Nike",
  "categoryId": 100,
  "categoryName": "운동화",
  "categoryDisplayPath": "스포츠/레저 > 신발 > 운동화",
  "productGroupName": "나이키 에어맥스 270",
  "optionType": "COMBINATION",
  "status": "ACTIVE",
  "createdAt": "2026-01-15T10:00:00+09:00",
  "updatedAt": "2026-02-01T12:00:00+09:00",
  "images": [
    { "imageType": "MAIN", "originUrl": "https://example.com/main.jpg", "sortOrder": 0 }
  ],
  "optionProductMatrix": {
    "optionGroups": [...],
    "products": [...]
  },
  "shippingPolicy": { "id": 5, "name": "기본배송", "shippingFee": 3000 },
  "refundPolicy": { "id": 3, "name": "기본반품", "refundFee": 5000 },
  "description": { "content": "<p>상세설명 HTML...</p>" },
  "productNotice": {
    "noticeCategoryId": 7,
    "noticeCategoryName": "의류",
    "entries": [
      { "noticeFieldId": 1, "fieldName": "소재", "fieldValue": "면 100%" }
    ]
  }
}
```

**HTTP Status**:
- `200 OK`: 조회 성공
- `404 NOT FOUND`: 상품 그룹을 찾을 수 없음

---

## Command Endpoints

### C1. registerProductGroup (상품 그룹 등록)

- **Path**: `POST /api/v1/market/product-groups`
- **Controller**: `ProductGroupCommandController`
- **Method**: `registerProductGroup(RegisterProductGroupApiRequest request)`
- **Request**: `@Valid @RequestBody RegisterProductGroupApiRequest`
- **Response**: `ProductGroupIdApiResponse` (201 Created)
- **UseCase**: `RegisterProductGroupFullUseCase`
- **권한**: `product-group:write` (`@PreAuthorize` + `@RequirePermission`)

**설명**: 상품 그룹(ProductGroup), 상세설명(Description), 고시정보(Notice), 하위 상품(Products)을 한번에 등록합니다.

**Request Body**:
```json
{
  "sellerId": 1,
  "brandId": 10,
  "categoryId": 100,
  "shippingPolicyId": 5,
  "refundPolicyId": 3,
  "productGroupName": "나이키 에어맥스 270",
  "optionType": "COMBINATION",
  "images": [
    { "imageType": "MAIN", "originUrl": "https://example.com/main.jpg", "sortOrder": 0 }
  ],
  "optionGroups": [
    {
      "optionGroupName": "사이즈",
      "canonicalOptionGroupId": 101,
      "optionValues": [
        { "optionValueName": "250", "canonicalOptionValueId": 1001, "sortOrder": 0 },
        { "optionValueName": "260", "canonicalOptionValueId": 1002, "sortOrder": 1 }
      ]
    }
  ],
  "products": [
    {
      "skuCode": "NK-AM270-250",
      "regularPrice": 139000,
      "currentPrice": 111200,
      "stockQuantity": 50,
      "sortOrder": 0,
      "optionIndices": [0]
    }
  ],
  "description": { "content": "<p>상세설명 HTML...</p>" },
  "notice": {
    "noticeCategoryId": 7,
    "entries": [
      { "noticeFieldId": 1, "fieldValue": "면 100%" }
    ]
  }
}
```

**Response**:
```json
{
  "productGroupId": 1001
}
```

**HTTP Status**:
- `201 CREATED`: 등록 성공
- `400 BAD REQUEST`: 잘못된 요청

---

### C2. batchRegisterProductGroups (상품 그룹 배치 등록)

- **Path**: `POST /api/v1/market/product-groups/batch`
- **Controller**: `ProductGroupCommandController`
- **Method**: `batchRegisterProductGroups(BatchRegisterProductGroupApiRequest request)`
- **Request**: `@Valid @RequestBody BatchRegisterProductGroupApiRequest`
- **Response**: `BatchProductGroupResultApiResponse` (200 OK)
- **UseCase**: `BatchRegisterProductGroupFullUseCase`
- **권한**: `product-group:write` (`@PreAuthorize` + `@RequirePermission`)

**설명**: 여러 상품 그룹을 한번에 등록합니다. 각 항목은 독립 트랜잭션으로 처리되며, 일부 실패 시 나머지는 정상 등록됩니다. 최대 100건까지 가능합니다.

**Request Body**:
```json
{
  "items": [
    {
      "sellerId": 1,
      "brandId": 10,
      "categoryId": 100,
      "shippingPolicyId": 5,
      "refundPolicyId": 3,
      "productGroupName": "나이키 에어맥스 270",
      "optionType": "COMBINATION",
      "images": [...],
      "optionGroups": [...],
      "products": [...],
      "description": { "content": "..." },
      "notice": { "noticeCategoryId": 7, "entries": [...] }
    }
  ]
}
```

**Response**:
```json
{
  "totalCount": 3,
  "successCount": 2,
  "failureCount": 1,
  "results": [
    { "index": 0, "productGroupId": 1001, "success": true, "errorCode": null, "errorMessage": null },
    { "index": 1, "productGroupId": 1002, "success": true, "errorCode": null, "errorMessage": null },
    { "index": 2, "productGroupId": null, "success": false, "errorCode": "VALIDATION_ERROR", "errorMessage": "상품 그룹명은 필수입니다" }
  ]
}
```

**HTTP Status**:
- `200 OK`: 배치 처리 완료 (개별 항목별 성공/실패 결과 확인 필요)
- `400 BAD REQUEST`: 요청 자체가 잘못된 경우 (items 누락 등)

---

### C3. updateProductGroupFull (상품 그룹 전체 수정)

- **Path**: `PUT /api/v1/market/product-groups/{productGroupId}`
- **Controller**: `ProductGroupCommandController`
- **Method**: `updateProductGroupFull(Long productGroupId, UpdateProductGroupFullApiRequest request)`
- **Request**:
  - `@PathVariable Long productGroupId`
  - `@Valid @RequestBody UpdateProductGroupFullApiRequest`
- **Response**: `Void` (204 No Content)
- **UseCase**: `UpdateProductGroupFullUseCase`
- **권한**: `@PreAuthorize("@access.isSellerOwnerOr(#productGroupId, 'product-group:write')")` (소유 셀러 또는 write 권한)

**설명**: 상품 그룹과 하위 상품들을 한번에 수정합니다. 기존 상품은 soft delete되고 새로 생성됩니다.

**Request Body**:
```json
{
  "productGroupName": "나이키 에어맥스 270 리뉴얼",
  "brandId": 10,
  "categoryId": 100,
  "shippingPolicyId": 5,
  "refundPolicyId": 3,
  "images": [
    { "imageType": "MAIN", "originUrl": "https://example.com/new-main.jpg", "sortOrder": 0 }
  ],
  "optionGroups": [
    {
      "sellerOptionGroupId": 500,
      "optionGroupName": "사이즈",
      "canonicalOptionGroupId": 101,
      "optionValues": [
        { "sellerOptionValueId": 5001, "optionValueName": "250", "canonicalOptionValueId": 1001, "sortOrder": 0 }
      ]
    }
  ],
  "products": [
    {
      "productId": 2001,
      "skuCode": "NK-AM270-250-V2",
      "regularPrice": 149000,
      "currentPrice": 119200,
      "stockQuantity": 30,
      "sortOrder": 0,
      "optionIndices": [0]
    }
  ],
  "description": { "content": "<p>새 상세설명...</p>" },
  "notice": {
    "noticeCategoryId": 7,
    "entries": [
      { "noticeFieldId": 1, "fieldValue": "폴리에스터 100%" }
    ]
  }
}
```

**HTTP Status**:
- `204 NO CONTENT`: 수정 성공
- `400 BAD REQUEST`: 잘못된 요청
- `404 NOT FOUND`: 상품 그룹을 찾을 수 없음

---

### C4. updateBasicInfo (상품 그룹 기본 정보 수정)

- **Path**: `PATCH /api/v1/market/product-groups/{productGroupId}/basic-info`
- **Controller**: `ProductGroupCommandController`
- **Method**: `updateBasicInfo(Long productGroupId, UpdateProductGroupBasicInfoApiRequest request)`
- **Request**:
  - `@PathVariable Long productGroupId`
  - `@Valid @RequestBody UpdateProductGroupBasicInfoApiRequest`
- **Response**: `Void` (204 No Content)
- **UseCase**: `UpdateProductGroupBasicInfoUseCase`
- **권한**: `@PreAuthorize("@access.isSellerOwnerOr(#productGroupId, 'product-group:write')")` (소유 셀러 또는 write 권한)

**설명**: 상품명, 브랜드, 카테고리, 배송정책, 반품정책만 수정합니다. 이미지/옵션/상품/상세설명/고시정보는 변경하지 않습니다.

**Request Body**:
```json
{
  "productGroupName": "나이키 에어맥스 270 (수정)",
  "brandId": 10,
  "categoryId": 101,
  "shippingPolicyId": 6,
  "refundPolicyId": 4
}
```

**HTTP Status**:
- `204 NO CONTENT`: 수정 성공
- `400 BAD REQUEST`: 잘못된 요청
- `404 NOT FOUND`: 상품 그룹을 찾을 수 없음

---

### C5. batchChangeStatus (상품 그룹 배치 상태 변경)

- **Path**: `PATCH /api/v1/market/product-groups/status`
- **Controller**: `ProductGroupCommandController`
- **Method**: `batchChangeStatus(BatchChangeProductGroupStatusApiRequest request)`
- **Request**: `@Valid @RequestBody BatchChangeProductGroupStatusApiRequest`
- **Response**: `Void` (204 No Content)
- **UseCase**: `BatchChangeProductGroupStatusUseCase`
- **권한**: `product-group:write` (`@PreAuthorize` + `@RequirePermission`)
- **셀러 소유권 검증**: `MarketAccessChecker.resolveCurrentSellerId()` 내부에서 처리

**설명**: 여러 상품 그룹의 상태를 일괄 변경합니다. 현재 인증된 사용자의 셀러 소유권을 검증합니다.

**Request Body**:
```json
{
  "productGroupIds": [1001, 1002, 1003],
  "targetStatus": "INACTIVE"
}
```

**targetStatus 허용값**: `ACTIVE`, `INACTIVE`, `SOLDOUT`, `DELETED`

**HTTP Status**:
- `204 NO CONTENT`: 상태 변경 성공
- `400 BAD REQUEST`: 잘못된 요청
- `403 FORBIDDEN`: 소유권 검증 실패

---

## 엔드포인트 일람표

| # | Method | Path | Controller | UseCase | 분류 |
|---|--------|------|------------|---------|------|
| Q1 | GET | `/product-groups` | ProductGroupQueryController | SearchProductGroupByOffsetUseCase | Query |
| Q2 | GET | `/product-groups/{productGroupId}` | ProductGroupQueryController | GetProductGroupUseCase | Query |
| C1 | POST | `/product-groups` | ProductGroupCommandController | RegisterProductGroupFullUseCase | Command |
| C2 | POST | `/product-groups/batch` | ProductGroupCommandController | BatchRegisterProductGroupFullUseCase | Command |
| C3 | PUT | `/product-groups/{productGroupId}` | ProductGroupCommandController | UpdateProductGroupFullUseCase | Command |
| C4 | PATCH | `/product-groups/{productGroupId}/basic-info` | ProductGroupCommandController | UpdateProductGroupBasicInfoUseCase | Command |
| C5 | PATCH | `/product-groups/status` | ProductGroupCommandController | BatchChangeProductGroupStatusUseCase | Command |

---

## 아키텍처 준수사항

### 컨트롤러 규칙
- **API-CTR-001**: `@RestController` 어노테이션 사용
- **API-CTR-002**: DELETE 메서드 금지 (소프트 삭제는 PATCH/상태변경으로 처리)
- **API-CTR-003**: UseCase (Port-In) 인터페이스 의존
- **API-CTR-004**: `ResponseEntity<T>` 래핑
- **API-CTR-005**: Controller에 `@Transactional` 금지
- **API-CTR-007**: Controller에 비즈니스 로직 포함 금지 (Mapper에서 변환)
- **API-CTR-009**: `@Valid` 어노테이션 필수
- **API-CTR-010**: CQRS Controller 분리 (Query/Command)
- **API-CTR-011**: List 직접 반환 금지 → `PageApiResponse` 페이징
- **API-CTR-012**: URL 경로 소문자 + 복수형 (`/product-groups`)

### DTO 규칙
- **API-DTO-001**: Record 타입 필수
- **API-DTO-003**: Validation 어노테이션 사용 (`@NotNull`, `@NotBlank`, `@Size`, `@Min`, `@Max`)
- **API-DTO-005**: 날짜 String 변환 필수 (ISO 8601)
- **API-DTO-006**: 복잡한 구조는 중첩 Record로 표현 (ImageApiRequest, OptionGroupApiRequest 등)
- **API-DTO-007**: `@Schema` 어노테이션 사용
- **API-DTO-010**: Offset 페이징은 `Search*ApiRequest` 네이밍

### 엔드포인트 규칙
- **API-END-001**: `Endpoints` final class + private 생성자
- **API-END-002**: static final 상수 사용
- **API-END-003**: Path Variable 상수화

---

## 관련 UseCase

### Query UseCases
- `SearchProductGroupByOffsetUseCase`: 상품 그룹 목록 검색 (Offset 페이징)
- `GetProductGroupUseCase`: 상품 그룹 상세 조회

### Command UseCases
- `RegisterProductGroupFullUseCase`: 상품 그룹 전체 등록 (단건)
- `BatchRegisterProductGroupFullUseCase`: 상품 그룹 배치 등록 (최대 100건)
- `UpdateProductGroupFullUseCase`: 상품 그룹 전체 수정 (이미지/옵션/상품 포함)
- `UpdateProductGroupBasicInfoUseCase`: 상품 그룹 기본 정보 수정
- `BatchChangeProductGroupStatusUseCase`: 상품 그룹 배치 상태 변경

---

## 권한 체계

| 엔드포인트 | 권한 | 소유권 검증 |
|-----------|------|-----------|
| Q1 (목록 조회) | `product-group:read` | 없음 |
| Q2 (상세 조회) | `product-group:read` | 없음 |
| C1 (단건 등록) | `product-group:write` | 없음 |
| C2 (배치 등록) | `product-group:write` | 없음 |
| C3 (전체 수정) | `product-group:write` | `@access.isSellerOwnerOr` |
| C4 (기본 정보 수정) | `product-group:write` | `@access.isSellerOwnerOr` |
| C5 (배치 상태 변경) | `product-group:write` | `MarketAccessChecker.resolveCurrentSellerId()` |

---

## 특이사항

### C2 배치 등록 응답 코드
배치 등록(`POST /batch`)은 일부 항목이 실패해도 `200 OK`를 반환합니다. 클라이언트는 응답 본문의 `results[].success` 필드로 개별 항목 성공 여부를 확인해야 합니다.

### C3 상품 soft delete 전략
전체 수정(`PUT /{productGroupId}`)은 기존 하위 상품을 soft delete 처리 후 새로 생성하는 전략을 사용합니다. `productId` 필드를 함께 전달하더라도 내부적으로 새 레코드가 생성됩니다.

### C4 vs C3 차이
- **C4 (PATCH /basic-info)**: 상품명, 브랜드, 카테고리, 배송/반품 정책만 변경 (경량 수정)
- **C3 (PUT)**: 이미지, 옵션, 상품, 상세설명, 고시정보 전체 교체 (전체 수정)

---

**분석 일시**: 2026-02-18
**분석 대상**: `/adapter-in/rest-api/src/main/java/com/ryuqq/marketplace/adapter/in/rest/productgroup/`
**총 엔드포인트**: 7개 (Query 2개, Command 5개)
