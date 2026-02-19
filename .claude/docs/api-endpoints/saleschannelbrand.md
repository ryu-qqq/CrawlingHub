# SalesChannelBrand API Endpoints

## 요약

| 분류 | 개수 |
|------|------|
| Query (조회) | 1개 |
| Command (명령) | 1개 |
| **합계** | **2개** |

---

## Query Endpoints

| # | Method | Path | Controller | Method | UseCase |
|---|--------|------|------------|--------|---------|
| 1 | GET | /api/v1/market/sales-channels/{salesChannelId}/brands | SalesChannelBrandQueryController | searchBrands | SearchSalesChannelBrandByOffsetUseCase |

---

### Q1. searchBrands - 외부채널 브랜드 목록 조회 (Offset 기반 페이징)

- **Path**: `GET /api/v1/market/sales-channels/{salesChannelId}/brands`
- **Controller**: `SalesChannelBrandQueryController`
- **Request**:
  - `@PathVariable Long salesChannelId` - 판매채널 ID (필수)
  - `SearchSalesChannelBrandsApiRequest` (@ParameterObject, Query String)
- **Response**: `ApiResponse<PageApiResponse<SalesChannelBrandApiResponse>>`
- **UseCase**: `SearchSalesChannelBrandByOffsetUseCase`
- **권한**: `@PreAuthorize("@access.superAdmin()")` + `@RequirePermission("sales-channel-brand:read")`

#### Request Parameters

| 파라미터 | 타입 | 필수 | 설명 | 예시 |
|---------|------|------|------|------|
| salesChannelId | Long | O | 판매채널 ID (Path Variable) | `1` |
| statuses | List\<String\> | X | 상태 필터 (ACTIVE, INACTIVE) | `statuses=ACTIVE` |
| searchField | String | X | 검색 필드 (externalBrandCode, externalBrandName) | `searchField=externalBrandName` |
| searchWord | String | X | 검색어 | `searchWord=나이키` |
| sortKey | String | X | 정렬 키 (createdAt, externalBrandName), 기본값: createdAt | `sortKey=createdAt` |
| sortDirection | String | X | 정렬 방향 (ASC, DESC), 기본값: DESC | `sortDirection=DESC` |
| page | Integer | X | 페이지 번호 (0부터 시작), 기본값: 0 | `page=0` |
| size | Integer | X | 페이지 크기, 기본값: 20 | `size=20` |

#### Response Fields

**SalesChannelBrandApiResponse**

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | 브랜드 ID |
| salesChannelId | Long | 판매채널 ID |
| externalBrandCode | String | 외부 브랜드 코드 |
| externalBrandName | String | 외부 브랜드명 |
| status | String | 상태 (ACTIVE, INACTIVE) |
| createdAt | String | 생성일시 (ISO-8601) |
| updatedAt | String | 수정일시 (ISO-8601) |

#### Response Structure

```json
{
  "data": {
    "content": [
      {
        "id": 1,
        "salesChannelId": 1,
        "externalBrandCode": "BRD001",
        "externalBrandName": "나이키",
        "status": "ACTIVE",
        "createdAt": "2025-01-23T10:30:00+09:00",
        "updatedAt": "2025-01-23T10:30:00+09:00"
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
1. SalesChannelBrandQueryController.searchBrands()
   ↓
2. SalesChannelBrandQueryApiMapper.toSearchParams()
   - List.of(salesChannelId) + SearchSalesChannelBrandsApiRequest → SalesChannelBrandSearchParams 변환
   - 기본값 설정: sortKey=createdAt, sortDirection=DESC, page=0, size=20
   ↓
3. SearchSalesChannelBrandByOffsetUseCase.execute()
   - 비즈니스 로직 실행
   ↓
4. SalesChannelBrandPageResult 반환
   ↓
5. SalesChannelBrandQueryApiMapper.toPageResponse()
   - SalesChannelBrandPageResult → PageApiResponse<SalesChannelBrandApiResponse> 변환
   - Instant → ISO-8601 문자열 변환 (DateTimeFormatUtils)
   ↓
6. ResponseEntity<ApiResponse<PageApiResponse<SalesChannelBrandApiResponse>>>
```

---

## Command Endpoints

| # | Method | Path | Controller | Method | UseCase |
|---|--------|------|------------|--------|---------|
| 1 | POST | /api/v1/market/sales-channels/{salesChannelId}/brands | SalesChannelBrandCommandController | registerBrand | RegisterSalesChannelBrandUseCase |

---

### C1. registerBrand - 외부채널 브랜드 등록

- **Path**: `POST /api/v1/market/sales-channels/{salesChannelId}/brands`
- **Controller**: `SalesChannelBrandCommandController`
- **Request**:
  - `@PathVariable Long salesChannelId` - 판매채널 ID (필수)
  - `RegisterSalesChannelBrandApiRequest` (@RequestBody)
- **Response**: `ApiResponse<SalesChannelBrandIdApiResponse>` (HTTP 201 Created)
- **UseCase**: `RegisterSalesChannelBrandUseCase`
- **권한**: `@PreAuthorize("@access.superAdmin()")` + `@RequirePermission("sales-channel-brand:write")`

#### Request Body

**RegisterSalesChannelBrandApiRequest**

| 필드 | 타입 | 필수 | 설명 | 예시 |
|------|------|------|------|------|
| externalBrandCode | String | O | 외부 브랜드 코드 (@NotBlank) | `"BRD001"` |
| externalBrandName | String | O | 외부 브랜드명 (@NotBlank) | `"나이키"` |

```json
{
  "externalBrandCode": "BRD001",
  "externalBrandName": "나이키"
}
```

#### Response Fields

**SalesChannelBrandIdApiResponse**

| 필드 | 타입 | 설명 |
|------|------|------|
| brandIds | List\<Long\> | 등록된 브랜드 ID 목록 |

#### Response Structure

```json
{
  "data": {
    "brandIds": [42]
  },
  "success": true,
  "message": null
}
```

#### 처리 흐름

```
1. SalesChannelBrandCommandController.registerBrand()
   ↓
2. SalesChannelBrandCommandApiMapper.toCommand()
   - salesChannelId + RegisterSalesChannelBrandApiRequest → RegisterSalesChannelBrandCommand 변환
   ↓
3. RegisterSalesChannelBrandUseCase.execute()
   - 비즈니스 로직 실행 → Long brandId 반환
   ↓
4. SalesChannelBrandIdApiResponse.of(brandId)
   - brandId → List.of(brandId) 래핑
   ↓
5. ResponseEntity.status(201).body(ApiResponse.of(...))
```

---

## 아키텍처 매핑

### Hexagonal Architecture Layer 흐름

```
[Adapter-In] SalesChannelBrandQueryController / SalesChannelBrandCommandController
    ↓
[Adapter-In] SalesChannelBrandQueryApiMapper / SalesChannelBrandCommandApiMapper (DTO 변환)
    ↓
[Application] SearchSalesChannelBrandByOffsetUseCase / RegisterSalesChannelBrandUseCase (Port-In)
    ↓
[Application] SearchSalesChannelBrandByOffsetService / RegisterSalesChannelBrandService (구현체)
    ↓
[Application] SalesChannelBrandQueryPort / SalesChannelBrandCommandPort (Port-Out)
    ↓
[Adapter-Out] SalesChannelBrandQueryAdapter / SalesChannelBrandCommandAdapter
    ↓
[Database] sales_channel_brand 테이블
```

### CQRS 패턴 적용

- **Query Side**: `SalesChannelBrandQueryController` - 판매채널 기준 브랜드 조회 전용
- **Command Side**: `SalesChannelBrandCommandController` - 브랜드 등록 전용
- **Path Variable 공유**: 양쪽 컨트롤러 모두 `{salesChannelId}`를 Path Variable로 받아 채널 컨텍스트 유지

### Base Path 구조

```
SalesChannelBrandAdminEndpoints:
  BASE  = /api/v1/market/sales-channels
  BRANDS = BASE + /{salesChannelId}/brands
    → GET  /api/v1/market/sales-channels/{salesChannelId}/brands  (목록 조회)
    → POST /api/v1/market/sales-channels/{salesChannelId}/brands  (등록)
```

---

## 에러 처리

`SalesChannelBrandErrorMapper`가 등록되어 있으며, `SalesChannelBrandException` 발생 시 아래 형식으로 매핑됩니다.

| 항목 | 값 |
|------|-----|
| 매퍼 클래스 | `SalesChannelBrandErrorMapper` |
| 지원 예외 | `SalesChannelBrandException` |
| Error Type URI Prefix | `/errors/sales-channel-brand` |
| HTTP Status | 예외의 `httpStatus()` 값 기반 |

---

## 코드 품질 체크

### 준수 사항

1. **Hexagonal Architecture**: 명확한 Port-In/Port-Out 분리
2. **CQRS 패턴**: Query/Command 컨트롤러 완전 분리
3. **DTO 변환**: Mapper를 통한 계층 간 DTO 변환
4. **Validation**: `@Valid` + `@NotBlank` 적용
5. **권한 제어**: `@PreAuthorize` + `@RequirePermission` 이중 적용 (superAdmin 전용)
6. **API 문서화**: Swagger `@Tag`, `@Operation`, `@Parameter`, `@Schema` 어노테이션 적용
7. **기본값 처리**: sortKey, sortDirection, page, size 기본값 Mapper에서 처리
8. **날짜 포맷 통일**: ISO-8601 표준 사용 (DateTimeFormatUtils)
9. **응답 코드 명시**: POST 등록 시 201 Created 반환
10. **List 응답 래핑**: 단건 등록 결과도 `brandIds: [id]` 형태로 일관성 유지

### 개선 가능 사항

1. **페이징 최대값 제한**: size에 대한 max 제한 없음 (DoS 방지 필요)
2. **searchField 유효성**: 허용 필드(externalBrandCode, externalBrandName)에 대한 enum 제한 없음
3. **DELETE/PATCH 미구현**: 브랜드 비활성화, 삭제 엔드포인트 없음
4. **salesChannelId 존재 여부**: Path Variable salesChannelId 유효성 검증이 컨트롤러 계층에 없음 (UseCase 위임)

---

## 문서 생성 정보

- **분석 일시**: 2026-02-18
- **대상 모듈**: `adapter-in/rest-api`
- **대상 패키지**: `com.ryuqq.marketplace.adapter.in.rest.saleschannelbrand`
- **컨트롤러 파일**: `SalesChannelBrandQueryController.java`, `SalesChannelBrandCommandController.java`
- **엔드포인트 Base**: `/api/v1/market/sales-channels/{salesChannelId}/brands`
