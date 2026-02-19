# CategoryPreset API Endpoints

## 요약

| 분류 | 개수 |
|------|------|
| Query (조회) | 2개 |
| Command (명령) | 3개 |
| **합계** | **5개** |

---

## Query Endpoints

| # | Method | Path | Controller | Method | UseCase |
|---|--------|------|------------|--------|---------|
| 1 | GET | /api/v1/market/category-presets | CategoryPresetQueryController | searchCategoryPresets | SearchCategoryPresetByOffsetUseCase |
| 2 | GET | /api/v1/market/category-presets/{categoryPresetId} | CategoryPresetQueryController | getCategoryPreset | GetCategoryPresetDetailUseCase |

---

### Q1. searchCategoryPresets - 카테고리 프리셋 목록 조회 (Offset 기반 페이징)

- **Path**: `GET /api/v1/market/category-presets`
- **Controller**: `CategoryPresetQueryController`
- **Request**: `SearchCategoryPresetsApiRequest` (@ParameterObject, Query String)
- **Response**: `ApiResponse<PageApiResponse<CategoryPresetApiResponse>>`
- **UseCase**: `SearchCategoryPresetByOffsetUseCase`
- **권한**: `@PreAuthorize("@access.authenticated()")` + `category-preset:read`

#### Request Parameters

| 파라미터 | 타입 | 필수 | 설명 | 예시 |
|---------|------|------|------|------|
| salesChannelIds | List\<Long\> | No | 판매채널 ID 목록 필터 | `salesChannelIds=1&salesChannelIds=2` |
| statuses | List\<String\> | No | 상태 필터 (ACTIVE, INACTIVE) | `statuses=ACTIVE` |
| searchField | String | No | 검색 필드 (PRESET_NAME, SHOP_NAME, ACCOUNT_ID, CATEGORY_CODE, CATEGORY_PATH) | `searchField=PRESET_NAME` |
| searchWord | String | No | 검색어 | `searchWord=식품` |
| startDate | String | No | 등록일 시작 (YYYY-MM-DD) | `startDate=2025-01-01` |
| endDate | String | No | 등록일 종료 (YYYY-MM-DD) | `endDate=2025-12-31` |
| sortKey | String | No | 정렬 키 (createdAt) | `sortKey=createdAt` |
| sortDirection | String | No | 정렬 방향 (ASC, DESC) | `sortDirection=DESC` |
| page | Integer | No | 페이지 번호 (0부터 시작, 기본값: 0) | `page=0` |
| size | Integer | No | 페이지 크기 (기본값: 20) | `size=20` |

#### Response Fields

**CategoryPresetApiResponse**

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | 프리셋 ID |
| shopId | Long | Shop ID |
| shopName | String | 쇼핑몰명 |
| salesChannelId | Long | 판매채널 ID |
| salesChannelName | String | 판매채널명 |
| accountId | String | 계정 ID |
| presetName | String | 프리셋 이름 |
| categoryPath | String | 카테고리 경로 (예: 식품 > 과자 > 스낵 > 젤리) |
| categoryCode | String | 카테고리 코드 |
| createdAt | String | 등록일 (ISO-8601) |

#### Response Structure

```json
{
  "data": {
    "content": [
      {
        "id": 1001,
        "shopId": 1,
        "shopName": "스마트스토어",
        "salesChannelId": 1,
        "salesChannelName": "네이버",
        "accountId": "trexi001",
        "presetName": "식품 - 과자류 전송용",
        "categoryPath": "식품 > 과자 > 스낵 > 젤리",
        "categoryCode": "50000123",
        "createdAt": "2025-12-15T00:00:00Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 100
  },
  "success": true,
  "message": null
}
```

#### 처리 흐름

```
1. CategoryPresetQueryController.searchCategoryPresets()
   ↓
2. CategoryPresetQueryApiMapper.toSearchParams()
   - SearchCategoryPresetsApiRequest → CategoryPresetSearchParams 변환
   - 날짜 파싱: "yyyy-MM-dd" → LocalDate
   - 기본값 설정: page=0, size=20
   ↓
3. SearchCategoryPresetByOffsetUseCase.execute()
   ↓
4. CategoryPresetPageResult 반환
   ↓
5. CategoryPresetQueryApiMapper.toPageResponse()
   - CategoryPresetPageResult → PageApiResponse<CategoryPresetApiResponse> 변환
   - Instant → ISO-8601 문자열 변환 (DateTimeFormatUtils)
   ↓
6. ResponseEntity<ApiResponse<PageApiResponse<CategoryPresetApiResponse>>> (200 OK)
```

---

### Q2. getCategoryPreset - 카테고리 프리셋 상세 조회

- **Path**: `GET /api/v1/market/category-presets/{categoryPresetId}`
- **Controller**: `CategoryPresetQueryController`
- **Request**: `@PathVariable Long categoryPresetId`
- **Response**: `ApiResponse<CategoryPresetDetailApiResponse>`
- **UseCase**: `GetCategoryPresetDetailUseCase`
- **권한**: `@PreAuthorize("@access.authenticated()")` + `category-preset:read`

#### Request Parameters

| 파라미터 | 타입 | 필수 | 설명 | 예시 |
|---------|------|------|------|------|
| categoryPresetId | Long | Yes | 조회할 프리셋 ID (Path Variable) | `/category-presets/1001` |

#### Response Fields

**CategoryPresetDetailApiResponse**

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | 프리셋 ID |
| shopId | Long | Shop ID |
| shopName | String | 쇼핑몰명 |
| salesChannelId | Long | 판매채널 ID |
| salesChannelName | String | 판매채널명 |
| accountId | String | 계정 ID |
| presetName | String | 프리셋 이름 |
| mappingCategory | MappingCategoryResponse | 매핑된 판매채널 카테고리 정보 |
| internalCategories | List\<InternalCategoryResponse\> | 매핑된 내부 카테고리 목록 |
| createdAt | String | 등록일 (ISO-8601) |
| updatedAt | String | 수정일 (ISO-8601) |

**MappingCategoryResponse**

| 필드 | 타입 | 설명 |
|------|------|------|
| categoryCode | String | 외부(판매채널) 카테고리 코드 |
| categoryPath | String | 카테고리 경로 |

**InternalCategoryResponse**

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | 내부 카테고리 ID |
| categoryPath | String | 내부 카테고리 경로 |

#### Response Structure

```json
{
  "data": {
    "id": 1001,
    "shopId": 1,
    "shopName": "스마트스토어",
    "salesChannelId": 1,
    "salesChannelName": "네이버",
    "accountId": "trexi001",
    "presetName": "식품 - 과자류 전송용",
    "mappingCategory": {
      "categoryCode": "50000123",
      "categoryPath": "식품 > 과자 > 스낵 > 젤리"
    },
    "internalCategories": [
      { "id": 100, "categoryPath": "식품 > 간식 > 과자류" },
      { "id": 101, "categoryPath": "식품 > 스낵" }
    ],
    "createdAt": "2025-12-15T10:30:00+09:00",
    "updatedAt": "2025-12-20T14:00:00+09:00"
  },
  "success": true,
  "message": null
}
```

#### 처리 흐름

```
1. CategoryPresetQueryController.getCategoryPreset()
   ↓
2. GetCategoryPresetDetailUseCase.execute(categoryPresetId)
   ↓
3. CategoryPresetDetailResult 반환
   ↓
4. CategoryPresetQueryApiMapper.toDetailResponse()
   - MappingCategoryResponse, InternalCategoryResponse nested 변환
   - Instant → ISO-8601 문자열 변환
   ↓
5. ResponseEntity<ApiResponse<CategoryPresetDetailApiResponse>> (200 OK)
```

---

## Command Endpoints

| # | Method | Path | Controller | Method | UseCase |
|---|--------|------|------------|--------|---------|
| 1 | POST | /api/v1/market/category-presets | CategoryPresetCommandController | registerCategoryPreset | RegisterCategoryPresetUseCase |
| 2 | PUT | /api/v1/market/category-presets/{categoryPresetId} | CategoryPresetCommandController | updateCategoryPreset | UpdateCategoryPresetUseCase |
| 3 | DELETE | /api/v1/market/category-presets | CategoryPresetCommandController | deleteCategoryPresets | DeleteCategoryPresetsUseCase |

---

### C1. registerCategoryPreset - 카테고리 프리셋 등록

- **Path**: `POST /api/v1/market/category-presets`
- **Controller**: `CategoryPresetCommandController`
- **Request**: `RegisterCategoryPresetApiRequest` (@RequestBody)
- **Response**: `ApiResponse<CategoryPresetIdApiResponse>` (HTTP 201 Created)
- **UseCase**: `RegisterCategoryPresetUseCase`
- **권한**: `@PreAuthorize("@access.superAdmin()")` + `category-preset:write`

#### Request Body

| 필드 | 타입 | 필수 | 설명 | 예시 |
|------|------|------|------|------|
| shopId | Long | Yes (@NotNull) | Shop ID | `1` |
| presetName | String | Yes (@NotBlank) | 프리셋 이름 | `"식품 - 과자류 전송용"` |
| categoryCode | String | Yes (@NotBlank) | 외부 카테고리 코드 | `"50000123"` |
| internalCategoryIds | List\<Long\> | Yes (@NotEmpty) | 매핑할 내부 카테고리 ID 목록 | `[1, 2, 3]` |

#### Response Fields

**CategoryPresetIdApiResponse**

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | 생성된 프리셋 ID |
| createdAt | String | 등록일 (현재는 null로 반환) |

#### Response Structure

```json
{
  "data": {
    "id": 1005,
    "createdAt": null
  },
  "success": true,
  "message": null
}
```

#### 처리 흐름

```
1. CategoryPresetCommandController.registerCategoryPreset()
   ↓
2. CategoryPresetCommandApiMapper.toRegisterCommand()
   - RegisterCategoryPresetApiRequest → RegisterCategoryPresetCommand 변환
   ↓
3. RegisterCategoryPresetUseCase.execute(command)
   - 프리셋 생성 및 내부 카테고리 매핑 처리
   ↓
4. Long categoryPresetId 반환
   ↓
5. ResponseEntity<ApiResponse<CategoryPresetIdApiResponse>> (201 Created)
```

#### 특이사항

- superAdmin 권한 필요 (일반 authenticated 불가)
- `createdAt` 필드는 현재 `null`로 반환 (`CategoryPresetIdApiResponse.of(id, null)`)

---

### C2. updateCategoryPreset - 카테고리 프리셋 수정

- **Path**: `PUT /api/v1/market/category-presets/{categoryPresetId}`
- **Controller**: `CategoryPresetCommandController`
- **Request**: `@PathVariable Long categoryPresetId` + `UpdateCategoryPresetApiRequest` (@RequestBody)
- **Response**: `ResponseEntity<Void>` (HTTP 204 No Content)
- **UseCase**: `UpdateCategoryPresetUseCase`
- **권한**: `@PreAuthorize("@access.superAdmin()")` + `category-preset:write`

#### Path Variable

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| categoryPresetId | Long | Yes | 수정할 프리셋 ID |

#### Request Body

| 필드 | 타입 | 필수 | 설명 | 예시 |
|------|------|------|------|------|
| presetName | String | No | 변경할 프리셋 이름 | `"수정된 프리셋 이름"` |
| categoryCode | String | No | 변경할 외부 카테고리 코드 | `"50000456"` |
| internalCategoryIds | List\<Long\> | No | 변경할 내부 카테고리 ID 목록 | `[1, 2, 3]` |

#### 처리 흐름

```
1. CategoryPresetCommandController.updateCategoryPreset()
   ↓
2. CategoryPresetCommandApiMapper.toUpdateCommand(categoryPresetId, request)
   - (presetId, UpdateCategoryPresetApiRequest) → UpdateCategoryPresetCommand 변환
   ↓
3. UpdateCategoryPresetUseCase.execute(command)
   ↓
4. ResponseEntity<Void> (204 No Content)
```

#### 특이사항

- 모든 Request Body 필드가 선택적 (Optional) - Partial Update 지원
- 성공 시 Body 없이 204 반환
- 존재하지 않는 프리셋 ID의 경우 404 반환

---

### C3. deleteCategoryPresets - 카테고리 프리셋 벌크 삭제

- **Path**: `DELETE /api/v1/market/category-presets`
- **Controller**: `CategoryPresetCommandController`
- **Request**: `DeleteCategoryPresetsApiRequest` (@RequestBody)
- **Response**: `ApiResponse<DeleteCategoryPresetsApiResponse>` (HTTP 200 OK)
- **UseCase**: `DeleteCategoryPresetsUseCase`
- **권한**: `@PreAuthorize("@access.superAdmin()")` + `category-preset:write`

#### Request Body

| 필드 | 타입 | 필수 | 설명 | 예시 |
|------|------|------|------|------|
| ids | List\<Long\> | Yes (@NotEmpty) | 삭제할 프리셋 ID 목록 | `[1001, 1002]` |

#### Response Fields

**DeleteCategoryPresetsApiResponse**

| 필드 | 타입 | 설명 |
|------|------|------|
| deletedCount | int | 실제로 삭제된 프리셋 수 |

#### Response Structure

```json
{
  "data": {
    "deletedCount": 2
  },
  "success": true,
  "message": null
}
```

#### 처리 흐름

```
1. CategoryPresetCommandController.deleteCategoryPresets()
   ↓
2. CategoryPresetCommandApiMapper.toDeleteCommand(request)
   - DeleteCategoryPresetsApiRequest → DeleteCategoryPresetsCommand 변환
   ↓
3. DeleteCategoryPresetsUseCase.execute(command)
   - 다수 프리셋 일괄 삭제
   ↓
4. int deletedCount 반환
   ↓
5. ResponseEntity<ApiResponse<DeleteCategoryPresetsApiResponse>> (200 OK)
```

#### 특이사항

- DELETE 메서드임에도 @RequestBody를 사용하여 ID 목록을 전달 (벌크 삭제 패턴)
- `deletedCount`로 실제 삭제 건수 확인 가능 (전달한 ID 수와 다를 수 있음)

---

## 아키텍처 매핑

### Hexagonal Architecture Layer 흐름

```
[Adapter-In] CategoryPresetQueryController / CategoryPresetCommandController
    ↓
[Adapter-In] CategoryPresetQueryApiMapper / CategoryPresetCommandApiMapper (DTO 변환)
    ↓
[Application] *UseCase (Port-In)
    ↓
[Application] *Service (구현체)
    ↓
[Application] CategoryPresetQueryPort / CategoryPresetCommandPort (Port-Out)
    ↓
[Adapter-Out] CategoryPresetQueryAdapter / CategoryPresetCommandAdapter
    ↓
[Adapter-Out] CategoryPresetQueryDslRepository
    ↓
[Database] category_preset 테이블
```

### CQRS 패턴 적용

- **Query Side**: `CategoryPresetQueryController` - 조회 전용 (searchCategoryPresets, getCategoryPreset)
- **Command Side**: `CategoryPresetCommandController` - 변경 전용 (register, update, delete)
- **UseCase 분리**: Query/Command UseCase가 별도 인터페이스로 완전 분리

### 권한 체계

| 분류 | 권한 레벨 | Permission |
|------|----------|------------|
| Query (조회) | authenticated | category-preset:read |
| Command (등록/수정/삭제) | superAdmin | category-preset:write |

---

## 에러 처리

`CategoryPresetErrorMapper`가 `CategoryPresetException`을 처리합니다.

| 상황 | HTTP Status | Error Type URI |
|------|-------------|----------------|
| 프리셋을 찾을 수 없음 | 404 Not Found | `/errors/category-preset/{code}` |
| 도메인 유효성 오류 | 도메인 정의 상태코드 | `/errors/category-preset/{code}` |

---

## 코드 품질 체크

### 준수 사항

1. **Hexagonal Architecture**: 명확한 Port-In/Port-Out 분리
2. **CQRS 패턴**: Query/Command 컨트롤러, UseCase, Mapper 완전 분리
3. **권한 분리**: 조회(authenticated) vs 변경(superAdmin) 권한 단계적 적용
4. **벌크 삭제**: DELETE + RequestBody 패턴으로 다건 삭제 지원
5. **Partial Update**: PUT에서 모든 필드 Optional 허용
6. **응답 코드 구분**: 등록 201, 수정 204, 조회/삭제 200
7. **날짜 포맷 통일**: ISO-8601 표준 사용 (DateTimeFormatUtils)
8. **DTO 검증**: `@NotNull`, `@NotBlank`, `@NotEmpty` 적용

### 개선 가능 사항

1. **등록 응답의 createdAt**: `CategoryPresetIdApiResponse.of(id, null)` - createdAt이 항상 null로 반환됨
2. **수정 요청 검증 부재**: `UpdateCategoryPresetApiRequest`의 모든 필드가 Optional이나 비즈니스 규칙상 최소 1개 필드는 필요할 수 있음
3. **페이징 최대값 제한**: size에 대한 max 제한 없음 (DoS 방지 고려)
4. **검색 필드 enum 제한**: searchField 값에 대한 enum 타입 미적용

---

## 문서 생성 정보

- **분석 일시**: 2026-02-18
- **대상 모듈**: `adapter-in/rest-api`
- **대상 패키지**: `com.ryuqq.marketplace.adapter.in.rest.categorypreset`
- **컨트롤러 파일**: `CategoryPresetQueryController.java`, `CategoryPresetCommandController.java`
- **엔드포인트 Base**: `/api/v1/market/category-presets`
