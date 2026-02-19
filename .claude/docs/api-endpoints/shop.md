# Shop API Endpoints

## 요약

| 분류 | 개수 |
|------|------|
| Query (조회) | 1개 |
| Command (명령) | 2개 |
| **합계** | **3개** |

**Base Path**: `/api/v1/market/shops`

---

## Query Endpoints

### Q1. searchShops (외부몰 목록 조회)

- **Path**: `GET /api/v1/market/shops`
- **Controller**: `ShopQueryController`
- **Method**: `searchShops(SearchShopsApiRequest request)`
- **Request**: `@ParameterObject @Valid SearchShopsApiRequest` (Query Parameters)
- **Response**: `ApiResponse<PageApiResponse<ShopApiResponse>>`
- **UseCase**: `SearchShopByOffsetUseCase`

**설명**: 외부몰(Shop) 목록을 복합 조건으로 페이지 기반(Offset)으로 조회합니다.

**권한**: `@access.superAdmin()` + `shop:read`

**Query Parameters**:
| 파라미터 | 타입 | 필수 | 설명 | 예시 |
|---------|------|------|------|------|
| `salesChannelId` | Long | X | 판매채널 ID | `1` |
| `statuses` | List\<String\> | X | 상태 필터 (ACTIVE, INACTIVE) | `ACTIVE` |
| `searchField` | String | X | 검색 필드 (SHOP_NAME, ACCOUNT_ID) | `SHOP_NAME` |
| `searchWord` | String | X | 검색어 | `쿠팡` |
| `sortKey` | String | X | 정렬 키 (createdAt, updatedAt, shopName) | `createdAt` |
| `sortDirection` | String | X | 정렬 방향 (ASC, DESC) | `DESC` |
| `page` | Integer | X | 페이지 번호 (0부터 시작) | `0` |
| `size` | Integer | X | 페이지 크기 | `20` |

**Response 구조**:
```json
{
  "items": [
    {
      "id": 1,
      "salesChannelId": 1,
      "shopName": "쿠팡",
      "accountId": "coupang_account_01",
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

### C1. registerShop (외부몰 등록)

- **Path**: `POST /api/v1/market/shops`
- **Controller**: `ShopCommandController`
- **Method**: `registerShop(RegisterShopApiRequest request)`
- **Request**: `@Valid @RequestBody RegisterShopApiRequest`
- **Response**: `ApiResponse<ShopIdApiResponse>` (HTTP 201)
- **UseCase**: `RegisterShopUseCase`

**설명**: 새로운 외부몰을 등록합니다.

**권한**: `@access.superAdmin()` + `shop:write`

**Request Body**:
```json
{
  "salesChannelId": 1,
  "shopName": "쿠팡",
  "accountId": "coupang_account_01"
}
```

**Request 필드**:
| 필드 | 타입 | 필수 | 설명 | 예시 |
|------|------|------|------|------|
| `salesChannelId` | Long | O | 판매채널 ID (`@NotNull`) | `1` |
| `shopName` | String | O | 외부몰명 (`@NotBlank`) | `쿠팡` |
| `accountId` | String | O | 계정 ID (`@NotBlank`) | `coupang_account_01` |

**Response**:
```json
{
  "shopId": 1
}
```

**HTTP Status**:
- `201 CREATED`: 등록 성공
- `400 BAD REQUEST`: 잘못된 요청 (필수 필드 누락 등)

---

### C2. updateShop (외부몰 수정)

- **Path**: `PUT /api/v1/market/shops/{shopId}`
- **Controller**: `ShopCommandController`
- **Method**: `updateShop(Long shopId, UpdateShopApiRequest request)`
- **Request**:
  - `@PathVariable Long shopId`
  - `@Valid @RequestBody UpdateShopApiRequest`
- **Response**: `Void` (HTTP 204)
- **UseCase**: `UpdateShopUseCase`

**설명**: 외부몰의 이름, 계정 ID, 상태를 수정합니다.

**권한**: `@access.superAdmin()` + `shop:write`

**Request Body**:
```json
{
  "shopName": "쿠팡",
  "accountId": "coupang_account_01",
  "status": "ACTIVE"
}
```

**Request 필드**:
| 필드 | 타입 | 필수 | 설명 | 허용값 |
|------|------|------|------|--------|
| `shopName` | String | O | 외부몰명 (`@NotBlank`) | 임의 문자열 |
| `accountId` | String | O | 계정 ID (`@NotBlank`) | 임의 문자열 |
| `status` | String | O | 상태 (`@NotBlank`, `@Pattern`) | `ACTIVE`, `INACTIVE` |

**HTTP Status**:
- `204 NO CONTENT`: 수정 성공
- `400 BAD REQUEST`: 잘못된 요청 (유효하지 않은 status 값 등)
- `404 NOT FOUND`: 외부몰을 찾을 수 없음

---

## 엔드포인트 일람표

| # | Method | Path | Controller | UseCase | 분류 |
|---|--------|------|------------|---------|------|
| Q1 | GET | `/api/v1/market/shops` | ShopQueryController | SearchShopByOffsetUseCase | Query |
| C1 | POST | `/api/v1/market/shops` | ShopCommandController | RegisterShopUseCase | Command |
| C2 | PUT | `/api/v1/market/shops/{shopId}` | ShopCommandController | UpdateShopUseCase | Command |

---

## 아키텍처 준수사항

### 컨트롤러 규칙
- **API-CTR-001**: `@RestController` 어노테이션 사용
- **API-CTR-002**: DELETE 메서드 미사용 (소프트 삭제는 상태(INACTIVE) 변경으로 처리)
- **API-CTR-003**: UseCase (Port-In) 인터페이스에만 의존
- **API-CTR-004**: `ResponseEntity<ApiResponse<T>>` 래핑
- **API-CTR-005**: Controller에 `@Transactional` 금지
- **API-CTR-009**: `@Valid` 어노테이션 사용
- **API-CTR-010**: CQRS Controller 분리 (`ShopQueryController` / `ShopCommandController`)
- **API-CTR-011**: 목록 조회는 `PageApiResponse`로 페이징 래핑
- **API-CTR-012**: URL 경로 소문자 + 복수형 (`/shops`)

### DTO 규칙
- **API-DTO-001**: Record 타입 사용
- **API-DTO-003**: `@NotNull`, `@NotBlank`, `@Pattern` 등 Validation 어노테이션 사용
- **API-DTO-005**: 날짜 필드 String 타입으로 반환 (`createdAt`, `updatedAt`)
- **API-DTO-007**: `@Schema` 어노테이션 사용 (Swagger 문서화)
- **API-DTO-010**: Offset 페이징 요청은 `Search{Bc}ApiRequest` 네이밍 (`SearchShopsApiRequest`)

### Endpoints 상수 클래스 규칙
- **API-END-001**: `ShopAdminEndpoints` - `final class` + `private` 생성자
- **API-END-002**: `static final` 상수로 경로 관리 (`SHOPS`, `SHOP_ID`)
- **API-END-003**: Path Variable 이름도 상수화 (`PATH_SHOP_ID = "shopId"`)

---

## 관련 UseCase

### Command UseCases
- `RegisterShopUseCase`: 외부몰 등록
- `UpdateShopUseCase`: 외부몰 정보 수정 (이름, 계정 ID, 상태)

### Query UseCases
- `SearchShopByOffsetUseCase`: 외부몰 목록 검색 (Offset 페이징)

---

## 권한 구조

| 엔드포인트 | Spring Security | AuthHub Permission |
|-----------|-----------------|-------------------|
| Q1 searchShops | `@access.superAdmin()` | `shop:read` |
| C1 registerShop | `@access.superAdmin()` | `shop:write` |
| C2 updateShop | `@access.superAdmin()` | `shop:write` |

- **모든 엔드포인트**: 슈퍼 관리자 전용 (`superAdmin()`)
- Shop은 시스템 핵심 데이터(판매채널-계정 매핑)로 최고 권한 필요

---

## 도메인 설명

**Shop(외부몰)**은 특정 판매채널(SalesChannel)에 연결된 셀러의 계정 단위입니다.

- 하나의 판매채널에 여러 Shop(계정)이 존재할 수 있음
- Shop의 상태는 `ACTIVE` / `INACTIVE` 두 가지로 관리
- 삭제 기능 없음 - 비활성화(`INACTIVE`) 상태 변경으로 대체

**의존성 흐름**:
```
Controller → UseCase (Port-In) → Application Service → Port-Out → Adapter-Out
```

---

**분석 일시**: 2026-02-18
**분석 대상**: `/adapter-in/rest-api/src/main/java/com/ryuqq/marketplace/adapter/in/rest/shop/`
**총 엔드포인트**: 3개 (Query 1개, Command 2개)
