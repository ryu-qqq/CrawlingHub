# SalesChannel API Endpoints

## 요약

| 분류 | 개수 |
|------|------|
| Query (조회) | 1개 |
| Command (명령) | 2개 |
| **합계** | **3개** |

**Base Path**: `/api/v1/market/sales-channels`

---

## Query Endpoints

### Q1. searchSalesChannels (판매채널 목록 조회)

- **Path**: `GET /api/v1/market/sales-channels`
- **Controller**: `SalesChannelQueryController`
- **Method**: `searchSalesChannels(SearchSalesChannelsApiRequest request)`
- **Request**: `@ParameterObject @Valid SearchSalesChannelsApiRequest` (Query Parameters)
- **Response**: `ApiResponse<PageApiResponse<SalesChannelApiResponse>>`
- **UseCase**: `SearchSalesChannelByOffsetUseCase`
- **권한**: `@PreAuthorize("@access.superAdmin()")` + `@RequirePermission("sales-channel:read")`

**설명**: 복합 조건으로 판매채널 목록을 페이지 기반(Offset)으로 검색합니다.

**Query Parameters**:
| 파라미터 | 타입 | 필수 | 설명 | 예시 |
|---------|------|------|------|------|
| `statuses` | List\<String\> | X | 상태 필터 | `ACTIVE`, `INACTIVE` |
| `searchField` | String | X | 검색 필드 | `CHANNEL_NAME` |
| `searchWord` | String | X | 검색어 | `쿠팡` |
| `sortKey` | String | X | 정렬 기준 | `createdAt`, `channelName` |
| `sortDirection` | String | X | 정렬 방향 | `ASC`, `DESC` |
| `page` | Integer | X | 페이지 번호 (0부터 시작, 기본값: 0) | `0` |
| `size` | Integer | X | 페이지 크기 (1~100) | `20` |

**Response 구조**:
```json
{
  "items": [
    {
      "id": 1,
      "channelName": "쿠팡",
      "status": "ACTIVE",
      "createdAt": "2025-01-23T10:30:00+09:00",
      "updatedAt": "2025-01-23T10:30:00+09:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 10,
  "totalPages": 1
}
```

**HTTP Status**:
- `200 OK`: 조회 성공

---

## Command Endpoints

### C1. registerSalesChannel (판매채널 등록)

- **Path**: `POST /api/v1/market/sales-channels`
- **Controller**: `SalesChannelCommandController`
- **Method**: `registerSalesChannel(RegisterSalesChannelApiRequest request)`
- **Request**: `@Valid @RequestBody RegisterSalesChannelApiRequest`
- **Response**: `ApiResponse<SalesChannelIdApiResponse>`
- **UseCase**: `RegisterSalesChannelUseCase`
- **권한**: `@PreAuthorize("@access.superAdmin()")` + `@RequirePermission("sales-channel:write")`

**설명**: 새로운 판매채널을 등록합니다. 슈퍼 어드민 권한이 필요합니다.

**Request Body**:
```json
{
  "channelName": "쿠팡"
}
```

**Request 필드**:
| 필드 | 타입 | 필수 | 설명 | 유효성 검사 |
|------|------|------|------|------------|
| `channelName` | String | O | 판매채널명 | `@NotBlank` |

**Response**:
```json
{
  "salesChannelId": 1
}
```

**HTTP Status**:
- `201 CREATED`: 등록 성공
- `400 BAD REQUEST`: 잘못된 요청 (channelName 누락/공백)

---

### C2. updateSalesChannel (판매채널 수정)

- **Path**: `PUT /api/v1/market/sales-channels/{salesChannelId}`
- **Controller**: `SalesChannelCommandController`
- **Method**: `updateSalesChannel(Long salesChannelId, UpdateSalesChannelApiRequest request)`
- **Request**:
  - `@PathVariable Long salesChannelId`
  - `@Valid @RequestBody UpdateSalesChannelApiRequest`
- **Response**: `Void` (204 No Content)
- **UseCase**: `UpdateSalesChannelUseCase`
- **권한**: `@PreAuthorize("@access.superAdmin()")` + `@RequirePermission("sales-channel:write")`

**설명**: 판매채널의 이름과 상태(ACTIVE/INACTIVE)를 수정합니다. 슈퍼 어드민 권한이 필요합니다.

**Request Body**:
```json
{
  "channelName": "쿠팡",
  "status": "ACTIVE"
}
```

**Request 필드**:
| 필드 | 타입 | 필수 | 설명 | 유효성 검사 |
|------|------|------|------|------------|
| `channelName` | String | O | 판매채널명 | `@NotBlank` |
| `status` | String | O | 상태 | `@NotBlank`, `@Pattern(ACTIVE\|INACTIVE)` |

**HTTP Status**:
- `204 NO CONTENT`: 수정 성공
- `400 BAD REQUEST`: 잘못된 요청 (필드 누락 또는 유효성 실패)
- `404 NOT FOUND`: 판매채널을 찾을 수 없음

---

## 엔드포인트 일람표

| # | Method | Path | Controller | UseCase | 분류 |
|---|--------|------|------------|---------|------|
| Q1 | GET | `/sales-channels` | SalesChannelQueryController | SearchSalesChannelByOffsetUseCase | Query |
| C1 | POST | `/sales-channels` | SalesChannelCommandController | RegisterSalesChannelUseCase | Command |
| C2 | PUT | `/sales-channels/{salesChannelId}` | SalesChannelCommandController | UpdateSalesChannelUseCase | Command |

---

## 아키텍처 준수사항

### 컨트롤러 규칙
- `@RestController` 어노테이션 사용
- DELETE 메서드 없음 (소프트 삭제 미적용 - 상태 변경은 PUT으로 처리)
- UseCase (Port-In) 인터페이스 의존
- `ResponseEntity<ApiResponse<T>>` 래핑
- Controller에 `@Transactional` 없음
- `@Valid` 어노테이션 적용
- CQRS Controller 분리 (SalesChannelQueryController / SalesChannelCommandController)
- URL 경로 소문자 + 케밥-케이스 (`/sales-channels`)

### DTO 규칙
- Record 타입 사용
- `@NotBlank`, `@Pattern`, `@Min`, `@Max` Validation 어노테이션 적용
- `@Schema` 어노테이션 사용 (Swagger)
- `@Parameter` 어노테이션 사용 (Query 파라미터)

### 엔드포인트 상수 규칙
- `SalesChannelAdminEndpoints` final class + private 생성자
- `static final` 상수로 Path 관리
- Path Variable 상수화 (`PATH_SALES_CHANNEL_ID = "salesChannelId"`)

---

## 관련 UseCase

### Command UseCases
- `RegisterSalesChannelUseCase`: 판매채널 등록
- `UpdateSalesChannelUseCase`: 판매채널 수정 (이름 + 상태)

### Query UseCases
- `SearchSalesChannelByOffsetUseCase`: 판매채널 검색 (Offset 페이징)

---

## 권한 요구사항

모든 엔드포인트가 `SUPER_ADMIN` 권한을 요구합니다.

| 엔드포인트 | 권한 레벨 | Permission |
|-----------|---------|------------|
| Q1 (searchSalesChannels) | SUPER_ADMIN | `sales-channel:read` |
| C1 (registerSalesChannel) | SUPER_ADMIN | `sales-channel:write` |
| C2 (updateSalesChannel) | SUPER_ADMIN | `sales-channel:write` |

---

## 도메인 특이사항

- **단순 구조**: 판매채널은 `channelName`과 `status` 두 필드만 관리하는 단순한 도메인입니다.
- **상태 관리**: 삭제 대신 `status: ACTIVE | INACTIVE`로 활성/비활성을 관리합니다.
- **단건 조회 엔드포인트 없음**: ID 기반 단건 조회 엔드포인트가 구현되어 있지 않습니다. 목록 조회만 제공합니다.

---

**분석 일시**: 2026-02-18
**분석 대상**: `/adapter-in/rest-api/src/main/java/com/ryuqq/marketplace/adapter/in/rest/saleschannel/`
**총 엔드포인트**: 3개 (Query 1개, Command 2개)
