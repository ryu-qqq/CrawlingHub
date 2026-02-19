# SalesChannelCategory API Endpoints

## 요약

| 분류 | 개수 |
|------|------|
| Query (조회) | 1개 |
| Command (명령) | 1개 |
| **합계** | **2개** |

**Base Path**: `/api/v1/market/sales-channels/{salesChannelId}/categories`

---

## Query Endpoints

### Q1. searchCategories (외부채널 카테고리 목록 조회)

- **Path**: `GET /api/v1/market/sales-channels/{salesChannelId}/categories`
- **Controller**: `SalesChannelCategoryQueryController`
- **Method**: `searchCategories(Long salesChannelId, SearchSalesChannelCategoriesApiRequest request)`
- **Request**:
  - `@PathVariable Long salesChannelId` (판매채널 ID)
  - `@ParameterObject @Valid SearchSalesChannelCategoriesApiRequest` (Query Parameters)
- **Response**: `ApiResponse<PageApiResponse<SalesChannelCategoryApiResponse>>`
- **UseCase**: `SearchSalesChannelCategoryByOffsetUseCase`
- **권한**: `@PreAuthorize("@access.superAdmin()")` + `@RequirePermission("sales-channel-category:read")`

**설명**: 특정 판매채널의 외부(연동) 카테고리 목록을 복합 조건으로 페이지 기반(Offset) 조회합니다. 내부 카테고리와의 매핑 여부, 깊이, 부모 카테고리 등 다양한 필터를 지원합니다.

**Query Parameters**:
| 파라미터 | 타입 | 필수 | 설명 | 예시 |
|---------|------|------|------|------|
| `statuses` | List\<String\> | X | 상태 필터 (ACTIVE, INACTIVE) | `ACTIVE` |
| `searchField` | String | X | 검색 필드 (externalCategoryCode, externalCategoryName) | `externalCategoryName` |
| `searchWord` | String | X | 검색어 | `의류` |
| `depth` | Integer | X | 카테고리 깊이 | `0` |
| `parentId` | Long | X | 부모 카테고리 ID | `0` |
| `mapped` | Boolean | X | 내부 카테고리 매핑 여부 | `true` |
| `sortKey` | String | X | 정렬 기준 (createdAt, externalCategoryName, sortOrder) 기본값: `createdAt` | `sortOrder` |
| `sortDirection` | String | X | 정렬 방향 (ASC/DESC) 기본값: `DESC` | `ASC` |
| `page` | Integer | X | 페이지 번호 (0부터 시작, 기본값: 0) | `0` |
| `size` | Integer | X | 페이지 크기 (기본값: 20) | `20` |

**Response 구조**:
```json
{
  "items": [
    {
      "id": 1,
      "salesChannelId": 10,
      "externalCategoryCode": "CAT001",
      "externalCategoryName": "의류",
      "parentId": 0,
      "depth": 0,
      "path": "1",
      "sortOrder": 1,
      "leaf": false,
      "status": "ACTIVE",
      "createdAt": "2025-01-23T10:30:00+09:00",
      "updatedAt": "2025-01-23T10:30:00+09:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 100,
  "totalPages": 5
}
```

**HTTP Status**:
- `200 OK`: 조회 성공

---

## Command Endpoints

### C1. registerCategory (외부채널 카테고리 등록)

- **Path**: `POST /api/v1/market/sales-channels/{salesChannelId}/categories`
- **Controller**: `SalesChannelCategoryCommandController`
- **Method**: `registerCategory(Long salesChannelId, RegisterSalesChannelCategoryApiRequest request)`
- **Request**:
  - `@PathVariable Long salesChannelId` (판매채널 ID)
  - `@Valid @RequestBody RegisterSalesChannelCategoryApiRequest`
- **Response**: `ApiResponse<SalesChannelCategoryIdApiResponse>`
- **UseCase**: `RegisterSalesChannelCategoryUseCase`
- **권한**: `@PreAuthorize("@access.superAdmin()")` + `@RequirePermission("sales-channel-category:write")`

**설명**: 특정 판매채널에 외부 카테고리를 새로 등록합니다. 등록 후 생성된 카테고리 ID를 반환합니다. (최고 마스터 권한 필요)

**Request Body**:
```json
{
  "externalCategoryCode": "CAT001",
  "externalCategoryName": "의류",
  "parentId": 0,
  "depth": 0,
  "path": "1",
  "sortOrder": 1,
  "leaf": false,
  "displayPath": "식품 > 과자 > 스낵 > 젤리"
}
```

**Request Fields**:
| 필드 | 타입 | 필수 | 설명 | 제약 |
|------|------|------|------|------|
| `externalCategoryCode` | String | Y | 외부 카테고리 코드 | NotBlank |
| `externalCategoryName` | String | Y | 외부 카테고리명 | NotBlank |
| `parentId` | Long | Y | 부모 카테고리 ID (최상위는 0) | NotNull |
| `depth` | Integer | Y | 카테고리 깊이 (0부터 시작) | NotNull, Min(0) |
| `path` | String | Y | 카테고리 경로 | NotBlank |
| `sortOrder` | Integer | Y | 정렬 순서 | NotNull, Min(0) |
| `leaf` | Boolean | Y | 리프 노드 여부 | NotNull |
| `displayPath` | String | X | 표시용 이름 경로 (예: "식품 > 과자") | - |

**Response**:
```json
{
  "categoryIds": [1]
}
```

**HTTP Status**:
- `201 CREATED`: 등록 성공
- `400 BAD REQUEST`: 잘못된 요청 (Validation 실패)

---

## 엔드포인트 일람표

| # | Method | Path | Controller | UseCase | 분류 |
|---|--------|------|------------|---------|------|
| Q1 | GET | `/api/v1/market/sales-channels/{salesChannelId}/categories` | SalesChannelCategoryQueryController | SearchSalesChannelCategoryByOffsetUseCase | Query |
| C1 | POST | `/api/v1/market/sales-channels/{salesChannelId}/categories` | SalesChannelCategoryCommandController | RegisterSalesChannelCategoryUseCase | Command |

---

## 아키텍처 준수사항

### 컨트롤러 규칙
- ✅ **API-CTR-001**: `@RestController` 어노테이션 사용
- ✅ **API-CTR-002**: DELETE 메서드 금지 (소프트 삭제 방식 사용)
- ✅ **API-CTR-003**: UseCase (Port-In) 인터페이스 의존
- ✅ **API-CTR-004**: `ResponseEntity<ApiResponse<T>>` 래핑
- ✅ **API-CTR-005**: Controller에 `@Transactional` 금지
- ✅ **API-CTR-007**: Controller에 비즈니스 로직 포함 금지
- ✅ **API-CTR-009**: `@Valid` 어노테이션 필수
- ✅ **API-CTR-010**: CQRS Controller 분리 (Query/Command)
- ✅ **API-CTR-011**: List 직접 반환 금지 → PageApiResponse 페이징 필수
- ✅ **API-CTR-012**: URL 경로 소문자 + 케밥케이스 (`/sales-channels`, `/categories`)
- ✅ **API-CTR-013**: 복합 조건 + Offset 페이징은 `searchCategoriesByOffset` 네이밍 (내부 메서드명은 `searchCategories`)

### DTO 규칙
- ✅ **API-DTO-001**: Record 타입 필수
- ✅ **API-DTO-003**: Validation 어노테이션 사용 (`@NotBlank`, `@NotNull`, `@Min`)
- ✅ **API-DTO-004**: createdAt/updatedAt 포함
- ✅ **API-DTO-005**: 날짜 String 변환 (ISO 8601 포맷)
- ✅ **API-DTO-007**: `@Schema` 어노테이션 사용
- ✅ **API-DTO-010**: Offset 페이징은 `Search{Bc}ApiRequest` 네이밍

### 엔드포인트 규칙
- ✅ **API-END-001**: Endpoints final class + private 생성자
- ✅ **API-END-002**: static final 상수 사용
- ✅ **API-END-003**: Path Variable 상수화 (`PATH_SALES_CHANNEL_ID`)

### 특이사항
- Path Variable `{salesChannelId}`가 Base Path에 포함되어 있어 Query/Command Controller 모두 `@PathVariable`로 채널 ID를 수신
- `SalesChannelCategoryIdApiResponse`는 단건 등록 시 `List.of(categoryId)` 형태로 반환 (향후 배치 등록 확장 고려)
- Query 매퍼에서 `depth`, `parentId` 파라미터는 `SalesChannelCategorySearchParams`로 위임되나 `CommonSearchParams`에 포함되지 않음 (도메인 특화 파라미터)

---

## 관련 UseCase

### Command UseCases
- `RegisterSalesChannelCategoryUseCase`: 외부채널 카테고리 단건 등록

### Query UseCases
- `SearchSalesChannelCategoryByOffsetUseCase`: 외부채널 카테고리 목록 검색 (Offset 페이징)

---

## 권한 요구사항

| 엔드포인트 | 권한 레벨 | Permission |
|----------|----------|------------|
| Q1 (searchCategories) | SUPER_ADMIN | `sales-channel-category:read` |
| C1 (registerCategory) | SUPER_ADMIN | `sales-channel-category:write` |

- 모든 엔드포인트가 `@access.superAdmin()` 권한을 요구합니다.
- `@RequirePermission`은 AuthHub SDK 어노테이션으로 세부 권한을 명시합니다.

---

**분석 일시**: 2026-02-18
**분석 대상**: `/adapter-in/rest-api/src/main/java/com/ryuqq/marketplace/adapter/in/rest/saleschannelcategory/`
**총 엔드포인트**: 2개 (Query 1개, Command 1개)
