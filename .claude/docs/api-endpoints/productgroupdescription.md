# ProductGroupDescription API Endpoints

## 요약

| 분류 | 개수 |
|------|------|
| Query (조회) | 1개 |
| Command (명령) | 1개 |
| **합계** | **2개** |

**Base Path**: `/api/v1/market/product-groups`

---

## Query Endpoints

### Q1. getPublishStatus (상세설명 발행 상태 조회)

- **Path**: `GET /api/v1/market/product-groups/{productGroupId}/description/publish-status`
- **Controller**: `ProductGroupDescriptionQueryController`
- **Method**: `getPublishStatus(Long productGroupId)`
- **Request**: `@PathVariable Long productGroupId`
- **Response**: `ApiResponse<DescriptionPublishStatusApiResponse>`
- **UseCase**: `GetDescriptionPublishStatusUseCase`
- **권한**: `@PreAuthorize("@access.isSellerOwnerOr(#productGroupId, 'product-group:read')")`

**설명**: 상품 그룹 상세설명의 발행 상태와 이미지 업로드 상태(완료/대기/실패 건수 포함)를 조회합니다.

**Response 구조**:
```json
{
  "productGroupId": 1,
  "descriptionId": 10,
  "publishStatus": "PUBLISHED",
  "cdnPath": "https://cdn.example.com/descriptions/10",
  "totalImageCount": 5,
  "completedImageCount": 4,
  "pendingImageCount": 0,
  "failedImageCount": 1,
  "images": [
    {
      "imageId": 100,
      "originUrl": "https://origin.example.com/img1.jpg",
      "uploadedUrl": "https://cdn.example.com/img1.jpg",
      "outboxStatus": "COMPLETED",
      "retryCount": 0,
      "errorMessage": null
    },
    {
      "imageId": 101,
      "originUrl": "https://origin.example.com/img2.jpg",
      "uploadedUrl": null,
      "outboxStatus": "FAILED",
      "retryCount": 3,
      "errorMessage": "Connection timeout"
    }
  ]
}
```

**publishStatus 값**:
| 값 | 설명 |
|----|------|
| `PUBLISHED` | 발행 완료 |
| `PENDING` | 발행 대기 중 (이미지 업로드 미완료) |
| `FAILED` | 발행 실패 |

**outboxStatus 값**:
| 값 | 설명 |
|----|------|
| `COMPLETED` | 이미지 업로드 완료 |
| `PENDING` | 업로드 대기 중 |
| `FAILED` | 업로드 실패 |

**HTTP Status**:
- `200 OK`: 조회 성공
- `404 NOT FOUND`: 상품 그룹을 찾을 수 없음

---

## Command Endpoints

### C1. updateDescription (상세설명 수정)

- **Path**: `PUT /api/v1/market/product-groups/{productGroupId}/description`
- **Controller**: `ProductGroupDescriptionCommandController`
- **Method**: `updateDescription(Long productGroupId, UpdateProductGroupDescriptionApiRequest request)`
- **Request**:
  - `@PathVariable Long productGroupId`
  - `@RequestBody @Valid UpdateProductGroupDescriptionApiRequest`
- **Response**: `Void` (204 No Content)
- **UseCase**: `UpdateProductGroupDescriptionUseCase`
- **권한**: `@PreAuthorize("@access.isSellerOwnerOr(#productGroupId, 'product-group:write')")`

**설명**: 상품 그룹의 상세 설명(HTML 콘텐츠)을 수정합니다. 기존 설명이 없으면 새로 생성합니다. 콘텐츠 내 이미지 URL은 이미지 업로드 Outbox를 통해 비동기로 CDN에 업로드됩니다.

**Request Body**:
```json
{
  "content": "<p>상품 상세 설명 HTML 내용입니다.</p><img src=\"https://origin.example.com/img1.jpg\" />"
}
```

**Request 필드**:
| 필드 | 타입 | 필수 | 설명 | 검증 |
|------|------|------|------|------|
| `content` | String | O | 상세 설명 HTML 콘텐츠 | `@NotBlank` |

**HTTP Status**:
- `204 NO CONTENT`: 수정 성공
- `400 BAD REQUEST`: content가 비어있음 (`@NotBlank` 위반)
- `404 NOT FOUND`: 상품 그룹을 찾을 수 없음

---

## 엔드포인트 일람표

| # | Method | Path | Controller | UseCase | 분류 |
|---|--------|------|------------|---------|------|
| Q1 | GET | `/product-groups/{productGroupId}/description/publish-status` | ProductGroupDescriptionQueryController | GetDescriptionPublishStatusUseCase | Query |
| C1 | PUT | `/product-groups/{productGroupId}/description` | ProductGroupDescriptionCommandController | UpdateProductGroupDescriptionUseCase | Command |

---

## 아키텍처 준수사항

### 컨트롤러 규칙
- ✅ **API-CTR-001**: `@RestController` 어노테이션 사용
- ✅ **API-CTR-003**: UseCase(Port-In) 인터페이스 의존
- ✅ **API-CTR-010**: CQRS Controller 분리 (QueryController / CommandController)
- ✅ **API-CTR-004**: `ResponseEntity<ApiResponse<T>>` 래핑 (Query), `ResponseEntity<Void>` (Command)
- ✅ **API-CTR-009**: `@Valid` 어노테이션 사용 (Command 요청)
- ✅ **API-CTR-012**: URL 경로 소문자 + 케밥케이스 (`/product-groups`, `/publish-status`)

### DTO 규칙
- ✅ **API-DTO-001**: Record 타입 사용 (`UpdateProductGroupDescriptionApiRequest`, `DescriptionPublishStatusApiResponse`)
- ✅ **API-DTO-003**: Validation 어노테이션 사용 (`@NotBlank`)
- ✅ **API-DTO-006**: 복잡한 구조는 중첩 Record로 표현 (`DescriptionImageUploadDetailResponse`)

### 엔드포인트 규칙
- ✅ **API-END-001**: `ProductGroupDescriptionAdminEndpoints` final class + private 생성자
- ✅ **API-END-002**: static final 상수 사용
- ✅ **API-END-003**: Path Variable 상수화 (`PATH_PRODUCT_GROUP_ID`)

---

## 관련 UseCase

### Command UseCases (Port-In)
- `UpdateProductGroupDescriptionUseCase`: 상세설명 수정 (API 연결)
- `RegisterProductGroupDescriptionUseCase`: 상세설명 신규 등록 (내부용 - 상품 그룹 등록 흐름에서 사용)
- `PublishPendingDescriptionsUseCase`: 대기 중인 상세설명 발행 처리 (Batch용)

### Query UseCases (Port-In)
- `GetDescriptionPublishStatusUseCase`: 발행 상태 조회 (API 연결)

---

## 에러 처리

`ProductGroupDescriptionErrorMapper`가 다음 예외를 처리합니다:

| 예외 | HTTP Status | 설명 |
|------|-------------|------|
| `ProductGroupDescriptionNotFoundException` | `404 NOT FOUND` | 상세설명을 찾을 수 없음 |

---

## 권한 요구사항

| 엔드포인트 | 권한 표현식 | 설명 |
|-----------|-------------|------|
| Q1 (getPublishStatus) | `isSellerOwnerOr(#productGroupId, 'product-group:read')` | 본인 상품 그룹이거나 read 권한 보유 |
| C1 (updateDescription) | `isSellerOwnerOr(#productGroupId, 'product-group:write')` | 본인 상품 그룹이거나 write 권한 보유 |

`@RequirePermission` 어노테이션으로 AuthHub SDK를 통한 권한 메타데이터도 선언되어 있습니다.

---

## 설계 특이사항

1. **Upsert 방식**: `updateDescription`은 상세설명이 없으면 신규 생성, 있으면 수정하는 Upsert 동작
2. **이미지 비동기 처리**: HTML 콘텐츠 내 이미지는 저장 시점에 Outbox에 등록되어 비동기로 CDN에 업로드됨
3. **발행 상태 분리**: 상세설명 자체의 발행 상태와 개별 이미지의 업로드 상태를 독립적으로 추적
4. **셀러 소유권 검증**: `@PreAuthorize`에서 productGroupId 기반 소유권 검증이 이루어짐

---

**분석 일시**: 2026-02-18
**분석 대상**: `/adapter-in/rest-api/src/main/java/com/ryuqq/marketplace/adapter/in/rest/productgroupdescription/`
**총 엔드포인트**: 2개 (Query 1개, Command 1개)
