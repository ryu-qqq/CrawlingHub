# CanonicalOption API Endpoints

## 요약

| 분류 | 개수 |
|------|------|
| Query (조회) | 2개 |
| Command (명령) | 0개 |
| **합계** | **2개** |

**Base Path**: `/api/v1/market/canonical-option-groups`

---

## Query Endpoints

### Q1. searchCanonicalOptionGroupsByOffset (정규 옵션그룹 목록 조회)

- **Path**: `GET /api/v1/market/canonical-option-groups`
- **Controller**: `CanonicalOptionGroupQueryController`
- **Method**: `searchCanonicalOptionGroupsByOffset(SearchCanonicalOptionGroupsApiRequest request)`
- **Request**: `@ParameterObject @Valid SearchCanonicalOptionGroupsApiRequest` (Query Parameters)
- **Response**: `ApiResponse<PageApiResponse<CanonicalOptionGroupApiResponse>>`
- **UseCase**: `SearchCanonicalOptionGroupByOffsetUseCase`
- **Permission**: `canonical-option-group:read`

**설명**: 복합 조건으로 정규 옵션그룹 목록을 Offset 기반 페이징으로 검색합니다. 활성 상태 필터, 코드/이름 검색, 정렬 조건을 지원합니다.

**Query Parameters**:
| 파라미터 | 타입 | 필수 | 설명 | 예시 |
|---------|------|------|------|------|
| `active` | Boolean | X | 활성 상태 필터 (null: 전체) | `true` |
| `searchField` | String | X | 검색 필드 (CODE, NAME_KO, NAME_EN) | `NAME_KO` |
| `searchWord` | String | X | 검색어 | `색상` |
| `sortKey` | String | X | 정렬 기준 (createdAt, code) | `createdAt` |
| `sortDirection` | String | X | 정렬 방향 (ASC, DESC) | `DESC` |
| `page` | Integer | X | 페이지 번호 (0부터 시작, 기본값: 0) | `0` |
| `size` | Integer | X | 페이지 크기 (기본값: 20) | `20` |

**Response 구조**:
```json
{
  "items": [
    {
      "id": 1,
      "code": "COLOR",
      "nameKo": "색상",
      "nameEn": "Color",
      "active": true,
      "values": [
        {
          "id": 10,
          "code": "RED",
          "nameKo": "빨강",
          "nameEn": "Red",
          "sortOrder": 1
        }
      ],
      "createdAt": "2026-01-01T00:00:00+09:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 50
}
```

**HTTP Status**:
- `200 OK`: 조회 성공

---

### Q2. getCanonicalOptionGroup (정규 옵션그룹 상세 조회)

- **Path**: `GET /api/v1/market/canonical-option-groups/{canonicalOptionGroupId}`
- **Controller**: `CanonicalOptionGroupQueryController`
- **Method**: `getCanonicalOptionGroup(Long canonicalOptionGroupId)`
- **Request**: `@PathVariable Long canonicalOptionGroupId`
- **Response**: `ApiResponse<CanonicalOptionGroupApiResponse>`
- **UseCase**: `GetCanonicalOptionGroupUseCase`
- **Permission**: `canonical-option-group:read`

**설명**: 정규 옵션그룹 ID로 단건 상세 정보를 조회합니다. 해당 그룹에 속한 옵션 값 목록(CanonicalOptionValue)을 함께 반환합니다.

**Path Variables**:
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| `canonicalOptionGroupId` | Long | O | 조회할 정규 옵션그룹 ID |

**Response 구조**:
```json
{
  "id": 1,
  "code": "COLOR",
  "nameKo": "색상",
  "nameEn": "Color",
  "active": true,
  "values": [
    {
      "id": 10,
      "code": "RED",
      "nameKo": "빨강",
      "nameEn": "Red",
      "sortOrder": 1
    },
    {
      "id": 11,
      "code": "BLUE",
      "nameKo": "파랑",
      "nameEn": "Blue",
      "sortOrder": 2
    }
  ],
  "createdAt": "2026-01-01T00:00:00+09:00"
}
```

**HTTP Status**:
- `200 OK`: 조회 성공
- `404 NOT FOUND`: 정규 옵션그룹을 찾을 수 없음

---

## Command Endpoints

현재 구현된 Command 엔드포인트 없음.

> 정규 옵션(CanonicalOption) 데이터는 시스템 관리 데이터로, 현재 API를 통한 생성/수정/삭제는 제공하지 않습니다.

---

## 엔드포인트 일람표

| # | Method | Path | Controller | UseCase | 분류 |
|---|--------|------|------------|---------|------|
| Q1 | GET | `/canonical-option-groups` | CanonicalOptionGroupQueryController | SearchCanonicalOptionGroupByOffsetUseCase | Query |
| Q2 | GET | `/canonical-option-groups/{canonicalOptionGroupId}` | CanonicalOptionGroupQueryController | GetCanonicalOptionGroupUseCase | Query |

---

## 아키텍처 준수사항

### 컨트롤러 규칙
- ✅ **API-CTR-001**: `@RestController` 어노테이션 사용
- ✅ **API-CTR-003**: UseCase (Port-In) 인터페이스 의존 (`GetCanonicalOptionGroupUseCase`, `SearchCanonicalOptionGroupByOffsetUseCase`)
- ✅ **API-CTR-004**: `ResponseEntity<ApiResponse<T>>` 래핑
- ✅ **API-CTR-009**: `@Valid` 어노테이션 적용
- ✅ **API-CTR-010**: CQRS 적용 - QueryController만 존재 (Command 미구현)
- ✅ **API-CTR-011**: List 직접 반환 금지 → `PageApiResponse` 페이징 사용
- ✅ **API-CTR-012**: URL 경로 소문자 + 복수형 (`/canonical-option-groups`)
- ✅ **API-CTR-013**: Offset 페이징 메서드 `searchCanonicalOptionGroupsByOffset` 네이밍

### DTO 규칙
- ✅ **API-DTO-001**: Record 타입 사용 (`SearchCanonicalOptionGroupsApiRequest`, `CanonicalOptionGroupApiResponse`, `CanonicalOptionValueApiResponse`)
- ✅ **API-DTO-005**: 날짜 String 변환 (`DateTimeFormatUtils.formatIso8601`)
- ✅ **API-DTO-006**: 중첩 Record 구조 (`CanonicalOptionGroupApiResponse` 내 `List<CanonicalOptionValueApiResponse>`)
- ✅ **API-DTO-007**: `@Schema` 어노테이션 사용
- ✅ **API-DTO-010**: Offset 페이징 요청 DTO `SearchCanonicalOptionGroupsApiRequest` 네이밍

### 엔드포인트 상수 클래스 규칙
- ✅ **API-END-001**: `CanonicalOptionAdminEndpoints` final class + private 생성자
- ✅ **API-END-002**: static final 상수 사용 (`CANONICAL_OPTION_GROUPS`, `CANONICAL_OPTION_GROUP_ID`)
- ✅ **API-END-003**: Path Variable 상수화 (`PATH_CANONICAL_OPTION_GROUP_ID`)

---

## 관련 UseCase

### Query UseCases
- `GetCanonicalOptionGroupUseCase`: 정규 옵션그룹 ID 기반 단건 조회
- `SearchCanonicalOptionGroupByOffsetUseCase`: 정규 옵션그룹 목록 Offset 페이징 검색

---

## 권한 요구사항

`@RequirePermission` 어노테이션으로 AuthHub SDK 기반 권한 체크가 적용되어 있습니다.

| 엔드포인트 | 권한 코드 | 설명 |
|-----------|----------|------|
| Q1 (목록 조회) | `canonical-option-group:read` | 정규 옵션그룹 목록 조회 |
| Q2 (단건 조회) | `canonical-option-group:read` | 정규 옵션그룹 단건 조회 |

---

**분석 일시**: 2026-02-18
**분석 대상**: `/adapter-in/rest-api/src/main/java/com/ryuqq/marketplace/adapter/in/rest/canonicaloption/`
**총 엔드포인트**: 2개 (Query 2개, Command 0개)
