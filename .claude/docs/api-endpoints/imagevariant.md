# Image Variant API Endpoints

## 요약

| 분류 | 개수 |
|------|------|
| Query (조회) | 1개 |
| Command (명령) | 1개 |
| **합계** | **2개** |

**Base Path**: `/api/v1/market/image-variants`

---

## Query Endpoints

### Q1. getVariantsByImageId (이미지 Variant 목록 조회)

- **Path**: `GET /api/v1/market/image-variants/product-groups/{productGroupId}/images/{imageId}`
- **Controller**: `ImageVariantQueryController`
- **Method**: `getVariantsByImageId(Long productGroupId, Long imageId)`
- **Request**:
  - `@PathVariable Long productGroupId` - 상품 그룹 ID
  - `@PathVariable Long imageId` - 이미지 ID
- **Response**: `ApiResponse<List<ImageVariantApiResponse>>`
- **UseCase**: `GetImageVariantsByImageIdUseCase`

**설명**: 특정 이미지 ID에 대해 생성된 모든 Variant(WebP 변환 이미지) 목록을 조회합니다.

**권한**: `product-group:read` (본인 소유 상품 그룹이거나 해당 권한 보유)
- `@PreAuthorize("@access.isSellerOwnerOr(#productGroupId, 'product-group:read')")`

**Response 구조**:
```json
[
  {
    "variantType": "SMALL_WEBP",
    "variantUrl": "https://cdn.example.com/variant-small.webp",
    "width": 300,
    "height": 300
  },
  {
    "variantType": "MEDIUM_WEBP",
    "variantUrl": "https://cdn.example.com/variant-medium.webp",
    "width": 600,
    "height": 600
  },
  {
    "variantType": "LARGE_WEBP",
    "variantUrl": "https://cdn.example.com/variant-large.webp",
    "width": 1200,
    "height": 1200
  },
  {
    "variantType": "ORIGINAL_WEBP",
    "variantUrl": "https://cdn.example.com/variant-original.webp",
    "width": 2000,
    "height": 2000
  }
]
```

**Response DTO 필드**:
| 필드 | 타입 | 설명 |
|------|------|------|
| `variantType` | String | Variant 타입 (SMALL_WEBP, MEDIUM_WEBP, LARGE_WEBP, ORIGINAL_WEBP) |
| `variantUrl` | String | 변환된 이미지 CDN URL |
| `width` | Integer | 너비 (px) |
| `height` | Integer | 높이 (px) |

**HTTP Status**:
- `200 OK`: 조회 성공
- `404 NOT FOUND`: 이미지를 찾을 수 없음 (`ImageVariantNotFoundException`)

---

## Command Endpoints

### C1. requestTransform (이미지 변환 수동 요청)

- **Path**: `POST /api/v1/market/image-variants/product-groups/{productGroupId}/transform`
- **Controller**: `ImageVariantCommandController`
- **Method**: `requestTransform(Long productGroupId, RequestImageTransformApiRequest request)`
- **Request**:
  - `@PathVariable Long productGroupId` - 상품 그룹 ID
  - `@RequestBody(required = false) RequestImageTransformApiRequest` - 변환 대상 Variant 타입 목록 (선택)
- **Response**: `Void` (202 Accepted)
- **UseCase**: `RequestImageTransformUseCase`

**설명**: 상품 그룹에 속한 이미지들의 WebP Variant 변환을 수동으로 요청합니다. `variantTypes`가 비어있거나 null이면 전체 Variant 타입(SMALL_WEBP, MEDIUM_WEBP, LARGE_WEBP, ORIGINAL_WEBP)을 대상으로 변환을 요청합니다.

**권한**: `product-group:write` (본인 소유 상품 그룹이거나 해당 권한 보유)
- `@PreAuthorize("@access.isSellerOwnerOr(#productGroupId, 'product-group:write')")`

**Request Body** (선택 - 생략 가능):
```json
{
  "variantTypes": ["SMALL_WEBP", "MEDIUM_WEBP"]
}
```

**Request DTO 필드**:
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `variantTypes` | List\<String\> | X | 변환 대상 Variant 타입 목록. null 또는 빈 배열이면 전체 타입 변환 |

**HTTP Status**:
- `202 Accepted`: 변환 요청 접수 (비동기 처리)
- `404 NOT FOUND`: 상품 그룹을 찾을 수 없음

---

## 엔드포인트 일람표

| # | Method | Path | Controller | UseCase | 분류 |
|---|--------|------|------------|---------|------|
| Q1 | GET | `/image-variants/product-groups/{productGroupId}/images/{imageId}` | ImageVariantQueryController | GetImageVariantsByImageIdUseCase | Query |
| C1 | POST | `/image-variants/product-groups/{productGroupId}/transform` | ImageVariantCommandController | RequestImageTransformUseCase | Command |

---

## 아키텍처 준수사항

### 컨트롤러 규칙
- `API-CTR-001`: `@RestController` 어노테이션 사용
- `API-CTR-003`: UseCase (Port-In) 인터페이스 의존
- `API-CTR-004`: `ResponseEntity<ApiResponse<T>>` 래핑 (Query) / `ResponseEntity<Void>` (Command 202)
- `API-CTR-005`: Controller에 `@Transactional` 없음
- `API-CTR-007`: Controller에 비즈니스 로직 없음
- `API-CTR-010`: CQRS Controller 분리 (QueryController / CommandController)
- `API-CTR-012`: URL 경로 소문자 + 복수형 (`/image-variants`)

### 엔드포인트 규칙
- `API-END-001`: `ImageVariantAdminEndpoints` final class + private 생성자
- `API-END-002`: static final 상수 사용
- `API-END-003`: Path Variable 상수화 (`PATH_PRODUCT_GROUP_ID`, `PATH_IMAGE_ID`)

### 에러 처리
- `ImageVariantNotFoundException` → `404 NOT FOUND` (`ImageVariantErrorMapper`)

---

## 관련 UseCase

### Query UseCases
- `GetImageVariantsByImageIdUseCase`: 이미지 ID로 Variant 목록 조회

### Command UseCases (ImageTransform 도메인)
- `RequestImageTransformUseCase`: 수동 이미지 변환 요청 (Outbox 생성 → 비동기 처리)
- `ProcessPendingImageTransformUseCase`: 대기 중인 변환 처리 (스케줄러)
- `PollProcessingImageTransformUseCase`: 처리 중인 변환 폴링 (스케줄러)
- `RecoverTimeoutImageTransformUseCase`: 타임아웃 변환 복구 (스케줄러)

---

## 도메인 특이사항

- **ImageVariant와 ImageTransform의 분리**: 조회는 `imagevariant` 도메인 UseCase를 사용하고, 변환 요청은 `imagetransform` 도메인 UseCase를 사용합니다. Controller는 `imagevariant` 패키지에 통합되어 있습니다.
- **비동기 처리**: `requestTransform`은 즉시 처리하지 않고 Outbox 패턴으로 변환 작업을 큐에 등록한 후 `202 Accepted`를 반환합니다. 실제 변환은 스케줄러가 처리합니다.
- **Variant 타입**: SMALL_WEBP, MEDIUM_WEBP, LARGE_WEBP, ORIGINAL_WEBP 4가지 WebP 포맷 Variant를 지원합니다.

---

**분석 일시**: 2026-02-18
**분석 대상**: `/adapter-in/rest-api/src/main/java/com/ryuqq/marketplace/adapter/in/rest/imagevariant/`
**총 엔드포인트**: 2개 (Query 1개, Command 1개)
