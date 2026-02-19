# BrandPreset API Endpoints

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
| 1 | GET | /api/v1/market/brand-presets | BrandPresetQueryController | searchBrandPresets | SearchBrandPresetByOffsetUseCase |
| 2 | GET | /api/v1/market/brand-presets/{brandPresetId} | BrandPresetQueryController | getBrandPreset | GetBrandPresetDetailUseCase |

---

### Q1. searchBrandPresets - 브랜드 프리셋 목록 조회 (Offset 기반 페이징)

- **Path**: `GET /api/v1/market/brand-presets`
- **Controller**: `BrandPresetQueryController`
- **Request**: `SearchBrandPresetsApiRequest` (@ParameterObject, Query String)
- **Response**: `ApiResponse<PageApiResponse<BrandPresetApiResponse>>`
- **UseCase**: `SearchBrandPresetByOffsetUseCase`
- **권한**: `@PreAuthorize("@access.authenticated()")`, `brand-preset:read`

#### Request Parameters

| 파라미터 | 타입 | 필수 | 설명 | 예시 |
|---------|------|------|------|------|
| salesChannelIds | List\<Long\> | No | 판매채널 ID 목록 | `salesChannelIds=1&salesChannelIds=2` |
| statuses | List\<String\> | No | 상태 필터 (ACTIVE, INACTIVE) | `statuses=ACTIVE` |
| searchField | String | No | 검색 필드 (PRESET_NAME, SHOP_NAME, ACCOUNT_ID, BRAND_NAME, BRAND_CODE) | `searchField=PRESET_NAME` |
| searchWord | String | No | 검색어 | `searchWord=나이키` |
| startDate | String | No | 등록일 시작 (YYYY-MM-DD) | `startDate=2025-01-01` |
| endDate | String | No | 등록일 종료 (YYYY-MM-DD) | `endDate=2025-12-31` |
| sortKey | String | No | 정렬 키 (createdAt) | `sortKey=createdAt` |
| sortDirection | String | No | 정렬 방향 (ASC, DESC) | `sortDirection=DESC` |
| page | Integer | No | 페이지 번호 (0부터 시작, 기본값: 0) | `page=0` |
| size | Integer | No | 페이지 크기 (기본값: 20) | `size=20` |

#### Response Fields

**BrandPresetApiResponse**

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | 프리셋 ID |
| shopId | Long | Shop ID |
| shopName | String | 쇼핑몰명 |
| salesChannelId | Long | 판매채널 ID |
| salesChannelName | String | 판매채널명 |
| accountId | String | 계정 ID |
| presetName | String | 프리셋 이름 |
| brandName | String | 브랜드명 |
| brandCode | String | 브랜드 코드 |
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
        "presetName": "나이키 전송용",
        "brandName": "나이키",
        "brandCode": "NIKE-KR",
        "createdAt": "2025-12-15T10:30:00+09:00"
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
1. BrandPresetQueryController.searchBrandPresets()
   ↓
2. BrandPresetQueryApiMapper.toSearchParams()
   - SearchBrandPresetsApiRequest → BrandPresetSearchParams 변환
   - 기본값 설정: page=0, size=20
   - startDate/endDate: "YYYY-MM-DD" 문자열 → LocalDate 파싱
   ↓
3. SearchBrandPresetByOffsetUseCase.execute()
   - 비즈니스 로직 실행
   ↓
4. BrandPresetPageResult 반환
   ↓
5. BrandPresetQueryApiMapper.toPageResponse()
   - BrandPresetPageResult → PageApiResponse<BrandPresetApiResponse> 변환
   - Instant → ISO-8601 문자열 변환 (DateTimeFormatUtils 사용)
   ↓
6. ResponseEntity<ApiResponse<PageApiResponse<BrandPresetApiResponse>>>
```

---

### Q2. getBrandPreset - 브랜드 프리셋 상세 조회

- **Path**: `GET /api/v1/market/brand-presets/{brandPresetId}`
- **Controller**: `BrandPresetQueryController`
- **Request**: `@PathVariable Long brandPresetId`
- **Response**: `ApiResponse<BrandPresetDetailApiResponse>`
- **UseCase**: `GetBrandPresetDetailUseCase`
- **권한**: `@PreAuthorize("@access.authenticated()")`, `brand-preset:read`

#### Request Parameters

| 파라미터 | 위치 | 타입 | 필수 | 설명 |
|---------|------|------|------|------|
| brandPresetId | Path | Long | Yes | 조회할 브랜드 프리셋 ID |

#### Response Fields

**BrandPresetDetailApiResponse**

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | 프리셋 ID |
| shopId | Long | Shop ID |
| shopName | String | 쇼핑몰명 |
| salesChannelId | Long | 판매채널 ID |
| salesChannelName | String | 판매채널명 |
| accountId | String | 계정 ID |
| presetName | String | 프리셋 이름 |
| mappingBrand | MappingBrandResponse | 매핑된 판매채널 브랜드 정보 |
| internalBrands | List\<InternalBrandResponse\> | 매핑된 내부 브랜드 목록 |
| createdAt | String | 등록일 (ISO-8601) |
| updatedAt | String | 수정일 (ISO-8601) |

**MappingBrandResponse**

| 필드 | 타입 | 설명 |
|------|------|------|
| brandCode | String | 외부 브랜드 코드 |
| brandName | String | 외부 브랜드명 |

**InternalBrandResponse**

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | 내부 브랜드 ID |
| brandName | String | 브랜드명 |

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
    "presetName": "나이키 전송용",
    "mappingBrand": {
      "brandCode": "NK001",
      "brandName": "NIKE"
    },
    "internalBrands": [
      { "id": 100, "brandName": "나이키" },
      { "id": 101, "brandName": "Nike Korea" }
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
1. BrandPresetQueryController.getBrandPreset()
   ↓
2. GetBrandPresetDetailUseCase.execute(brandPresetId)
   ↓
3. BrandPresetDetailResult 반환
   ↓
4. BrandPresetQueryApiMapper.toDetailResponse()
   - BrandPresetDetailResult → BrandPresetDetailApiResponse 변환
   - MappingBrandResponse, List<InternalBrandResponse> 중첩 객체 변환
   - Instant → ISO-8601 문자열 변환
   ↓
5. ResponseEntity<ApiResponse<BrandPresetDetailApiResponse>>
```

---

## Command Endpoints

| # | Method | Path | Controller | Method | UseCase |
|---|--------|------|------------|--------|---------|
| 1 | POST | /api/v1/market/brand-presets | BrandPresetCommandController | registerBrandPreset | RegisterBrandPresetUseCase |
| 2 | PUT | /api/v1/market/brand-presets/{brandPresetId} | BrandPresetCommandController | updateBrandPreset | UpdateBrandPresetUseCase |
| 3 | DELETE | /api/v1/market/brand-presets | BrandPresetCommandController | deleteBrandPresets | DeleteBrandPresetsUseCase |

---

### C1. registerBrandPreset - 브랜드 프리셋 등록

- **Path**: `POST /api/v1/market/brand-presets`
- **Controller**: `BrandPresetCommandController`
- **Request**: `RegisterBrandPresetApiRequest` (@RequestBody)
- **Response**: `ApiResponse<BrandPresetIdApiResponse>` (HTTP 201 Created)
- **UseCase**: `RegisterBrandPresetUseCase`
- **권한**: `@PreAuthorize("@access.superAdmin()")`, `brand-preset:write`

#### Request Body

| 필드 | 타입 | 필수 | 설명 | 예시 |
|------|------|------|------|------|
| shopId | Long | Yes (`@NotNull`) | Shop ID | `1` |
| salesChannelBrandId | Long | Yes (`@NotNull`) | 판매채널 브랜드 ID | `10` |
| presetName | String | Yes (`@NotBlank`) | 프리셋 이름 | `"나이키 전송용"` |
| internalBrandIds | List\<Long\> | Yes (`@NotEmpty`) | 매핑할 내부 브랜드 ID 목록 | `[1, 2, 3]` |

#### Request Body Example

```json
{
  "shopId": 1,
  "salesChannelBrandId": 10,
  "presetName": "나이키 전송용",
  "internalBrandIds": [1, 2, 3]
}
```

#### Response Fields

**BrandPresetIdApiResponse**

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | 생성된 프리셋 ID |
| createdAt | String | 등록일 (현재 null로 응답) |

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
1. BrandPresetCommandController.registerBrandPreset()
   ↓
2. BrandPresetCommandApiMapper.toCommand()
   - RegisterBrandPresetApiRequest → RegisterBrandPresetCommand 변환
   ↓
3. RegisterBrandPresetUseCase.execute(command)
   - 브랜드 프리셋 등록 비즈니스 로직
   ↓
4. Long brandPresetId 반환
   ↓
5. ResponseEntity.status(201).body(ApiResponse.of(BrandPresetIdApiResponse.of(id, null)))
```

#### 특이사항

- **HTTP 201 Created** 반환 (다른 Command와 달리 생성 의미 명시)
- `createdAt` 필드는 현재 항상 `null`로 반환됨 (미구현)
- **superAdmin 전용**: 일반 authenticated 유저는 접근 불가

---

### C2. updateBrandPreset - 브랜드 프리셋 수정

- **Path**: `PUT /api/v1/market/brand-presets/{brandPresetId}`
- **Controller**: `BrandPresetCommandController`
- **Request**: `@PathVariable Long brandPresetId` + `UpdateBrandPresetApiRequest` (@RequestBody)
- **Response**: `Void` (HTTP 204 No Content)
- **UseCase**: `UpdateBrandPresetUseCase`
- **권한**: `@PreAuthorize("@access.superAdmin()")`, `brand-preset:write`

#### Path Variable

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| brandPresetId | Long | Yes | 수정할 브랜드 프리셋 ID |

#### Request Body

| 필드 | 타입 | 필수 | 설명 | 예시 |
|------|------|------|------|------|
| presetName | String | No | 프리셋 이름 | `"수정된 프리셋 이름"` |
| salesChannelBrandId | Long | No | 판매채널 브랜드 ID | `10` |
| internalBrandIds | List\<Long\> | No | 매핑할 내부 브랜드 ID 목록 | `[1, 2, 3]` |

#### Request Body Example

```json
{
  "presetName": "수정된 프리셋 이름",
  "salesChannelBrandId": 10,
  "internalBrandIds": [1, 2, 3]
}
```

#### 처리 흐름

```
1. BrandPresetCommandController.updateBrandPreset()
   ↓
2. BrandPresetCommandApiMapper.toCommand(brandPresetId, request)
   - brandPresetId + UpdateBrandPresetApiRequest → UpdateBrandPresetCommand 변환
   ↓
3. UpdateBrandPresetUseCase.execute(command)
   - 브랜드 프리셋 수정 비즈니스 로직
   ↓
4. ResponseEntity.noContent().build() (HTTP 204)
```

#### 특이사항

- **HTTP 204 No Content** 반환 (응답 바디 없음)
- 모든 Request Body 필드가 선택적 (partial update 가능)
- 프리셋 미존재 시 404 응답

---

### C3. deleteBrandPresets - 브랜드 프리셋 벌크 삭제

- **Path**: `DELETE /api/v1/market/brand-presets`
- **Controller**: `BrandPresetCommandController`
- **Request**: `DeleteBrandPresetsApiRequest` (@RequestBody)
- **Response**: `ApiResponse<DeleteBrandPresetsApiResponse>` (HTTP 200 OK)
- **UseCase**: `DeleteBrandPresetsUseCase`
- **권한**: `@PreAuthorize("@access.superAdmin()")`, `brand-preset:write`

#### Request Body

| 필드 | 타입 | 필수 | 설명 | 예시 |
|------|------|------|------|------|
| ids | List\<Long\> | Yes (`@NotEmpty`) | 삭제할 프리셋 ID 목록 | `[1001, 1002]` |

#### Request Body Example

```json
{
  "ids": [1001, 1002]
}
```

#### Response Fields

**DeleteBrandPresetsApiResponse**

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
1. BrandPresetCommandController.deleteBrandPresets()
   ↓
2. BrandPresetCommandApiMapper.toDeleteCommand(request.ids())
   - List<Long> ids → DeleteBrandPresetsCommand 변환
   ↓
3. DeleteBrandPresetsUseCase.execute(command)
   - 브랜드 프리셋 벌크 삭제 비즈니스 로직
   ↓
4. int deletedCount 반환
   ↓
5. ResponseEntity.ok(ApiResponse.of(DeleteBrandPresetsApiResponse.of(deletedCount)))
```

#### 특이사항

- **벌크 삭제**: 단건이 아닌 복수 ID를 Request Body로 전달
- `deletedCount`로 실제 삭제 건수 응답 (존재하지 않는 ID 포함 시 실제 삭제 건수와 요청 건수가 다를 수 있음)
- **DELETE + @RequestBody** 패턴 사용 (Path Variable 없음)

---

## 아키텍처 매핑

### Hexagonal Architecture Layer 흐름

```
[Adapter-In] BrandPresetQueryController / BrandPresetCommandController
    ↓
[Adapter-In] BrandPresetQueryApiMapper / BrandPresetCommandApiMapper (DTO 변환)
    ↓
[Application] *UseCase (Port-In)
    SearchBrandPresetByOffsetUseCase
    GetBrandPresetDetailUseCase
    RegisterBrandPresetUseCase
    UpdateBrandPresetUseCase
    DeleteBrandPresetsUseCase
    ↓
[Application] *Service (구현체)
    ↓
[Application] BrandPresetQueryPort / BrandPresetCommandPort (Port-Out)
    ↓
[Adapter-Out] BrandPresetQueryAdapter / BrandPresetCommandAdapter
    ↓
[Database] brand_preset 테이블
```

### 엔드포인트 상수 관리

`BrandPresetAdminEndpoints` 클래스에서 경로 상수를 중앙 관리:

```java
private static final String BASE = "/api/v1/market";
public static final String BRAND_PRESETS = BASE + "/brand-presets";       // → /api/v1/market/brand-presets
public static final String BRAND_PRESET_ID = "/{brandPresetId}";          // → /{brandPresetId}
public static final String PATH_BRAND_PRESET_ID = "brandPresetId";        // PathVariable 이름
```

### CQRS 패턴 적용

- **Query Side**: `BrandPresetQueryController` → 조회 전용 (authenticated 접근 가능)
- **Command Side**: `BrandPresetCommandController` → 변경 전용 (superAdmin 전용)
- **명확한 권한 분리**: 조회는 `@access.authenticated()`, 변경은 `@access.superAdmin()`

---

## 권한 체계

| 엔드포인트 | 인증 조건 | 권한 |
|-----------|----------|------|
| GET /brand-presets | authenticated | brand-preset:read |
| GET /brand-presets/{id} | authenticated | brand-preset:read |
| POST /brand-presets | superAdmin | brand-preset:write |
| PUT /brand-presets/{id} | superAdmin | brand-preset:write |
| DELETE /brand-presets | superAdmin | brand-preset:write |

---

## 코드 품질 체크

### 준수 사항

1. **Hexagonal Architecture**: 명확한 Port-In/Port-Out 분리
2. **CQRS 패턴**: Query/Command 컨트롤러 완전 분리
3. **엔드포인트 상수 관리**: `BrandPresetAdminEndpoints`로 경로 하드코딩 방지
4. **DTO 변환**: Mapper를 통한 계층 간 DTO 변환
5. **Validation**: `@NotNull`, `@NotBlank`, `@NotEmpty` 적용
6. **API 문서화**: Swagger `@Operation`, `@Schema`, `@Parameter` 어노테이션
7. **날짜 포맷 통일**: ISO-8601 표준 (DateTimeFormatUtils 사용)
8. **권한 분리**: 조회/변경 권한 명확히 구분 (`authenticated` vs `superAdmin`)

### 개선 가능 사항

1. **createdAt null 반환**: `registerBrandPreset` 응답의 `createdAt` 필드가 항상 `null` (미구현)
2. **페이징 최대값 제한**: `size` 파라미터에 대한 max 제한 없음 (DoS 방지 필요)
3. **searchField enum 제한 없음**: 허용되지 않은 검색 필드 값 입력 가능
4. **partial update 명시**: `PUT`이지만 모든 필드가 선택적 - `PATCH`가 더 적합할 수 있음

---

## 문서 생성 정보

- **분석 일시**: 2026-02-18
- **대상 모듈**: `adapter-in/rest-api`
- **대상 패키지**: `com.ryuqq.marketplace.adapter.in.rest.brandpreset`
- **컨트롤러 파일**: `BrandPresetQueryController.java`, `BrandPresetCommandController.java`
- **엔드포인트 Base**: `/api/v1/market/brand-presets`
