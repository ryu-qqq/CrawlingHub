# ProductNotice API Endpoints

## 요약

| 분류 | 개수 |
|------|------|
| Query (조회) | 0개 |
| Command (명령) | 1개 |
| **합계** | **1개** |

**Base Path**: `/api/v1/market/product-groups`

---

## Query Endpoints

해당 도메인에 Query 엔드포인트가 없습니다.

고시정보 조회는 ProductGroup 상세 조회 엔드포인트(`GET /api/v1/market/product-groups/{productGroupId}`)를
통해 복합 조회 방식으로 제공됩니다.

---

## Command Endpoints

### C1. updateNotice (고시정보 수정)

- **Path**: `PUT /api/v1/market/product-groups/{productGroupId}/notice`
- **Controller**: `ProductNoticeCommandController`
- **Method**: `updateNotice(Long productGroupId, UpdateProductNoticeApiRequest request)`
- **Request**:
  - `@PathVariable Long productGroupId`
  - `@RequestBody UpdateProductNoticeApiRequest`
- **Response**: `Void` (204 No Content)
- **UseCase**: `UpdateProductNoticeUseCase`
- **권한**: `@PreAuthorize("@access.isSellerOwnerOr(#productGroupId, 'product-group:write')")`
  + `@RequirePermission("product-group:write")`

**설명**: 상품 그룹의 고시정보를 수정합니다. 기존 고시정보가 없으면 새로 생성합니다.
고시 카테고리 ID와 고시 항목(필드 ID + 값) 목록을 전달합니다.

**Request Body**:
```json
{
  "noticeCategoryId": 1,
  "entries": [
    {
      "noticeFieldId": 101,
      "fieldValue": "면 100%"
    },
    {
      "noticeFieldId": 102,
      "fieldValue": "대한민국"
    }
  ]
}
```

**Request DTO 필드**:
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `noticeCategoryId` | Long | Y | 고시 카테고리 ID |
| `entries` | List | Y | 고시 항목 목록 (1개 이상) |
| `entries[].noticeFieldId` | Long | Y | 고시 필드 ID |
| `entries[].fieldValue` | String | Y | 고시 필드 값 (공백 불가) |

**HTTP Status**:
- `204 NO CONTENT`: 수정 성공
- `400 BAD REQUEST`: 잘못된 요청 (필수 필드 누락, 빈 값 등)
- `404 NOT FOUND`: 상품 그룹을 찾을 수 없음

---

## 엔드포인트 일람표

| # | Method | Path | Controller | UseCase | 분류 |
|---|--------|------|------------|---------|------|
| C1 | PUT | `/api/v1/market/product-groups/{productGroupId}/notice` | ProductNoticeCommandController | UpdateProductNoticeUseCase | Command |

---

## 아키텍처 준수사항

### 컨트롤러 규칙
- API-CTR-001: `@RestController` 어노테이션 사용
- API-CTR-003: UseCase (Port-In) 인터페이스 의존
- API-CTR-005: Controller에 `@Transactional` 금지
- API-CTR-007: Controller에 비즈니스 로직 포함 금지
- API-CTR-009: `@Valid` 어노테이션 필수
- API-CTR-010: CQRS Controller 분리 (CommandController만 존재)

### DTO 규칙
- API-DTO-001: Record 타입 사용 (`UpdateProductNoticeApiRequest`)
- API-DTO-003: jakarta.validation 어노테이션 사용 (`@NotNull`, `@NotBlank`, `@Valid`)
- API-DTO-006: 중첩 Record로 복합 구조 표현 (`NoticeEntryRequest`)

### 엔드포인트 상수
- API-END-001: `ProductNoticeAdminEndpoints` final class + private 생성자
- API-END-002: static final 상수 사용
- API-END-003: Path Variable 상수화 (`PATH_PRODUCT_GROUP_ID = "productGroupId"`)

---

## 관련 UseCase

### Command UseCases
- `UpdateProductNoticeUseCase`: 상품 그룹 고시정보 수정 (Controller에서 사용)
- `RegisterProductNoticeUseCase`: 상품 그룹 고시정보 신규 등록 (내부 연동용, ProductGroup 등록 흐름에서 사용)

---

## 참고

- `ProductNoticeAdminEndpoints` 상수 정의:
  - `PRODUCT_GROUPS = "/api/v1/market/product-groups"`
  - `ID = "/{productGroupId}"`
  - `NOTICE = "/notice"`
  - 조합 결과: `PUT /api/v1/market/product-groups/{productGroupId}/notice`
- Command 흐름: `UpdateProductNoticeApiRequest` → `ProductNoticeCommandApiMapper` → `UpdateProductNoticeCommand` → `UpdateProductNoticeUseCase`
- 에러 코드 접두사: `PRDNTC-` (ProductNoticeErrorMapper 참조)

---

**분석 일시**: 2026-02-18
**분석 대상**: `adapter-in/rest-api/src/main/java/com/ryuqq/marketplace/adapter/in/rest/productnotice/`
**총 엔드포인트**: 1개 (Query 0개, Command 1개)
