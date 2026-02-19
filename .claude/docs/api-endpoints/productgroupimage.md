# ProductGroupImage API Endpoints

## 요약

| 분류 | 개수 |
|------|------|
| Query (조회) | 1개 |
| Command (명령) | 1개 |
| **합계** | **2개** |

**Base Path**: `/api/v1/market/product-groups`

---

## Query Endpoints

### Q1. getUploadStatus (이미지 업로드 상태 조회)

- **Path**: `GET /api/v1/market/product-groups/{productGroupId}/images/upload-status`
- **Controller**: `ProductGroupImageQueryController`
- **Method**: `getUploadStatus(Long productGroupId)`
- **Request**: `@PathVariable Long productGroupId`
- **Response**: `ApiResponse<ProductGroupImageUploadStatusApiResponse>`
- **UseCase**: `GetProductGroupImageUploadStatusUseCase`
- **권한**: `@PreAuthorize("@access.isSellerOwnerOr(#productGroupId, 'product-group:read')")`
  `@RequirePermission("product-group:read")`

**설명**: 상품 그룹에 등록된 이미지들의 비동기 업로드 상태를 조회합니다. 이미지별로 업로드 완료(COMPLETED), 대기(PENDING), 처리중(PROCESSING), 실패(FAILED) 카운트 및 상세 내역을 반환합니다.

**Response 구조**:
```json
{
  "data": {
    "productGroupId": 1,
    "totalCount": 3,
    "completedCount": 2,
    "pendingCount": 0,
    "processingCount": 1,
    "failedCount": 0,
    "images": [
      {
        "imageId": 10,
        "imageType": "MAIN",
        "originUrl": "https://origin.example.com/main.jpg",
        "uploadedUrl": "https://cdn.example.com/main.jpg",
        "outboxStatus": "COMPLETED",
        "retryCount": 0,
        "errorMessage": null
      },
      {
        "imageId": 11,
        "imageType": "SUB",
        "originUrl": "https://origin.example.com/sub1.jpg",
        "uploadedUrl": null,
        "outboxStatus": "PROCESSING",
        "retryCount": 1,
        "errorMessage": null
      }
    ]
  }
}
```

**HTTP Status**:
- `200 OK`: 조회 성공
- `404 NOT FOUND`: 상품 그룹을 찾을 수 없음

---

## Command Endpoints

### C1. updateImages (상품 그룹 이미지 수정)

- **Path**: `PUT /api/v1/market/product-groups/{productGroupId}/images`
- **Controller**: `ProductGroupImageCommandController`
- **Method**: `updateImages(Long productGroupId, UpdateProductGroupImagesApiRequest request)`
- **Request**:
  - `@PathVariable Long productGroupId`
  - `@RequestBody UpdateProductGroupImagesApiRequest`
- **Response**: `Void` (204 No Content)
- **UseCase**: `UpdateProductGroupImagesUseCase`
- **권한**: `@PreAuthorize("@access.isSellerOwnerOr(#productGroupId, 'product-group:write')")`
  `@RequirePermission("product-group:write")`

**설명**: 상품 그룹의 이미지를 전체 교체합니다. 기존 이미지를 삭제하고 요청한 이미지 목록으로 대체하는 full-replace 방식입니다. 각 이미지는 타입(MAIN/SUB 등), 원본 URL, 정렬 순서를 포함합니다.

**Request Body**:
```json
{
  "images": [
    {
      "imageType": "MAIN",
      "originUrl": "https://origin.example.com/main.jpg",
      "sortOrder": 1
    },
    {
      "imageType": "SUB",
      "originUrl": "https://origin.example.com/sub1.jpg",
      "sortOrder": 2
    },
    {
      "imageType": "SUB",
      "originUrl": "https://origin.example.com/sub2.jpg",
      "sortOrder": 3
    }
  ]
}
```

**필드 설명**:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `images` | List | Y | 이미지 목록 (최소 1개) |
| `images[].imageType` | String | Y | 이미지 타입 (예: MAIN, SUB) |
| `images[].originUrl` | String | Y | 원본 이미지 URL |
| `images[].sortOrder` | int | Y | 정렬 순서 |

**HTTP Status**:
- `204 NO CONTENT`: 수정 성공
- `400 BAD REQUEST`: 잘못된 요청 (images 누락, imageType/originUrl 공백 등)
- `404 NOT FOUND`: 상품 그룹을 찾을 수 없음

---

## 엔드포인트 일람표

| # | Method | Path | Controller | UseCase | 분류 |
|---|--------|------|------------|---------|------|
| Q1 | GET | `/product-groups/{productGroupId}/images/upload-status` | ProductGroupImageQueryController | GetProductGroupImageUploadStatusUseCase | Query |
| C1 | PUT | `/product-groups/{productGroupId}/images` | ProductGroupImageCommandController | UpdateProductGroupImagesUseCase | Command |

---

## 아키텍처 준수사항

### 컨트롤러 규칙
- `API-CTR-001`: `@RestController` 어노테이션 사용
- `API-CTR-003`: UseCase (Port-In) 인터페이스 의존
- `API-CTR-004`: Query는 `ResponseEntity<ApiResponse<T>>` 래핑, Command는 `ResponseEntity<Void>`
- `API-CTR-005`: Controller에 `@Transactional` 없음
- `API-CTR-007`: Controller에 비즈니스 로직 없음
- `API-CTR-009`: `@Valid` 어노테이션 사용 (C1)
- `API-CTR-010`: CQRS Controller 분리 (QueryController / CommandController)
- `API-CTR-012`: URL 경로 소문자 + 복수형 (`/product-groups`, `/images`)

### DTO 규칙
- `API-DTO-001`: Record 타입 사용 (`UpdateProductGroupImagesApiRequest`, `ProductGroupImageUploadStatusApiResponse`)
- `API-DTO-003`: Validation 어노테이션 사용 (`@NotNull`, `@NotBlank`)
- `API-DTO-006`: 복잡한 구조는 중첩 Record로 표현 (`ImageRequest`, `ImageUploadDetailResponse`)

### 엔드포인트 상수 규칙
- `API-END-001`: `ProductGroupImageAdminEndpoints` final class + private 생성자
- `API-END-002`: static final 상수 사용 (`PRODUCT_GROUPS`, `IMAGES`, `UPLOAD_STATUS`)
- `API-END-003`: Path Variable 상수화 (`ID = "/{productGroupId}"`, `PATH_PRODUCT_GROUP_ID`)

---

## 관련 UseCase

### Command UseCases
- `UpdateProductGroupImagesUseCase`: 상품 그룹 이미지 전체 교체
- `RegisterProductGroupImagesUseCase`: 상품 그룹 이미지 최초 등록 (내부용, ProductGroup 등록 플로우에서 호출)

### Query UseCases
- `GetProductGroupImageUploadStatusUseCase`: 이미지 업로드 상태 조회

---

## 에러 처리

`ProductGroupImageErrorMapper`에서 다음 예외를 처리합니다:

| 예외 클래스 | HTTP Status | 설명 |
|-------------|-------------|------|
| `ProductGroupImageNotFoundException` | 404 | 상품 그룹 이미지를 찾을 수 없음 |
| `DescriptionImageNotFoundException` | 404 | 설명 이미지를 찾을 수 없음 |

---

## 권한 요구사항

| 엔드포인트 | 권한 키 | 조건 |
|-----------|---------|------|
| Q1 (이미지 업로드 상태 조회) | `product-group:read` | 셀러 본인 소유 또는 해당 권한 보유 |
| C1 (이미지 수정) | `product-group:write` | 셀러 본인 소유 또는 해당 권한 보유 |

권한 체크는 `@access.isSellerOwnerOr(#productGroupId, 'permission')` SpEL 표현식으로 수행합니다.

---

**분석 일시**: 2026-02-18
**분석 대상**: `/adapter-in/rest-api/src/main/java/com/ryuqq/marketplace/adapter/in/rest/productgroupimage/`
**총 엔드포인트**: 2개 (Query 1개, Command 1개)
