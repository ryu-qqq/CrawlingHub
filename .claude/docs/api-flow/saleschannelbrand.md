# SalesChannelBrand Domain API Flow Analysis

외부채널 브랜드 도메인의 전체 API 호출 흐름 분석 문서입니다.

---

## 엔드포인트 목록

| HTTP Method | Path | 설명 | Controller Method |
|-------------|------|------|-------------------|
| GET | `/api/v1/market/sales-channels/{salesChannelId}/brands` | 외부채널 브랜드 목록 조회 | `searchBrands()` |
| POST | `/api/v1/market/sales-channels/{salesChannelId}/brands` | 외부채널 브랜드 등록 | `registerBrand()` |

### Base Path 구조

```
SalesChannelBrandAdminEndpoints:
  BASE   = /api/v1/market/sales-channels
  BRANDS = BASE + /{salesChannelId}/brands
    → GET  /api/v1/market/sales-channels/{salesChannelId}/brands  (목록 조회)
    → POST /api/v1/market/sales-channels/{salesChannelId}/brands  (등록)
```

---

## 1. GET /api/v1/market/sales-channels/{salesChannelId}/brands - 외부채널 브랜드 목록 조회

### 호출 흐름 다이어그램

```
[Adapter-In]
SalesChannelBrandQueryController.searchBrands(salesChannelId, request)
  ├─ SalesChannelBrandQueryApiMapper.toSearchParams(List.of(salesChannelId), request)
  │   └─ -> SalesChannelBrandSearchParams
  ├─ SearchSalesChannelBrandByOffsetUseCase.execute(params)              [Port Interface]
  └─ SalesChannelBrandQueryApiMapper.toPageResponse(pageResult)          [Response 변환]

[Application]
SearchSalesChannelBrandByOffsetService.execute(params)                   [UseCase 구현]
  ├─ SalesChannelBrandQueryFactory.createCriteria(params)                [Criteria 생성]
  │   ├─ resolveSortKey()                  → SalesChannelBrandSortKey
  │   ├─ commonVoFactory.parseSortDirection()  → SortDirection
  │   ├─ commonVoFactory.createPageRequest()   → PageRequest
  │   ├─ commonVoFactory.createQueryContext()  → QueryContext<SalesChannelBrandSortKey>
  │   ├─ SalesChannelBrandSearchField.fromString()  → SalesChannelBrandSearchField
  │   ├─ SalesChannelBrandStatus.fromString() x N   → List<SalesChannelBrandStatus>
  │   └─ -> SalesChannelBrandSearchCriteria
  ├─ SalesChannelBrandReadManager.findByCriteria(criteria)               [@Transactional(readOnly)]
  │   └─ SalesChannelBrandQueryPort.findByCriteria()                     [Port]
  ├─ SalesChannelBrandReadManager.countByCriteria(criteria)              [@Transactional(readOnly)]
  │   └─ SalesChannelBrandQueryPort.countByCriteria()                    [Port]
  └─ SalesChannelBrandAssembler.toPageResult(brands, page, size, total)
      └─ -> SalesChannelBrandPageResult

[Adapter-Out]
SalesChannelBrandQueryAdapter                                            [Port 구현]
  ├─ SalesChannelBrandQueryDslRepository.findByCriteria(criteria)
  │   └─ QueryDSL: WHERE salesChannelId IN + statusIn + searchCondition
  │                ORDER BY createdAt|externalBrandName ASC|DESC
  │                OFFSET ? LIMIT ?
  ├─ SalesChannelBrandQueryDslRepository.countByCriteria(criteria)
  │   └─ QueryDSL: SELECT COUNT(*) + 동일 WHERE 조건
  └─ SalesChannelBrandJpaEntityMapper.toDomain(entity) x N
      └─ -> List<SalesChannelBrand>

[Database]
- sales_channel_brand (조회 대상)
```

### Layer별 상세

#### Adapter-In Layer

- **Controller**: `SalesChannelBrandQueryController`
  - File: `adapter-in/rest-api/src/main/java/com/ryuqq/marketplace/adapter/in/rest/saleschannelbrand/controller/SalesChannelBrandQueryController.java`
  - Method: `searchBrands(Long salesChannelId, SearchSalesChannelBrandsApiRequest request)`
  - 어노테이션: `@GetMapping`, `@PreAuthorize("@access.superAdmin()")`, `@RequirePermission("sales-channel-brand:read")`
  - Response: `ResponseEntity<ApiResponse<PageApiResponse<SalesChannelBrandApiResponse>>>`
  - HTTP Status: 200 OK

- **Request DTO**: `SearchSalesChannelBrandsApiRequest`
  ```java
  record SearchSalesChannelBrandsApiRequest(
    List<String> statuses,       // 상태 필터 (ACTIVE, INACTIVE), 옵션
    String searchField,          // 검색 필드 (externalBrandCode, externalBrandName), 옵션
    String searchWord,           // 검색어, 옵션
    String sortKey,              // 정렬 키 (createdAt, externalBrandName), 기본값: createdAt
    String sortDirection,        // 정렬 방향 (ASC, DESC), 기본값: DESC
    Integer page,                // 페이지 번호 (0부터 시작), 기본값: 0
    Integer size                 // 페이지 크기, 기본값: 20
  )
  ```

- **Response DTO**: `SalesChannelBrandApiResponse`
  ```java
  record SalesChannelBrandApiResponse(
    Long id,                     // 브랜드 ID
    Long salesChannelId,         // 판매채널 ID
    String externalBrandCode,    // 외부 브랜드 코드
    String externalBrandName,    // 외부 브랜드명
    String status,               // 상태 (ACTIVE, INACTIVE)
    String createdAt,            // 생성일시 (ISO-8601)
    String updatedAt             // 수정일시 (ISO-8601)
  )
  ```

- **ApiMapper**: `SalesChannelBrandQueryApiMapper`
  - `toSearchParams(List<Long> salesChannelIds, SearchSalesChannelBrandsApiRequest)` → `SalesChannelBrandSearchParams`
    - `salesChannelId`를 `List.of(salesChannelId)` 로 래핑 (복수 채널 조회 구조 대비)
    - `CommonSearchParams.of(false, null, null, sortKey, sortDirection, page, size)` 생성
    - 기본값 처리: `sortKey="createdAt"`, `sortDirection="DESC"`, `page=0`, `size=20`
  - `toResponse(SalesChannelBrandResult)` → `SalesChannelBrandApiResponse`
    - `DateTimeFormatUtils.formatIso8601(Instant)`: `Instant` → ISO-8601 문자열 변환
  - `toPageResponse(SalesChannelBrandPageResult)` → `PageApiResponse<SalesChannelBrandApiResponse>`
    - `pageMeta.page()`, `pageMeta.size()`, `pageMeta.totalElements()` 추출

#### Application Layer

- **UseCase Interface**: `SearchSalesChannelBrandByOffsetUseCase`
  - File: `application/src/main/java/com/ryuqq/marketplace/application/saleschannelbrand/port/in/query/SearchSalesChannelBrandByOffsetUseCase.java`
  - `execute(SalesChannelBrandSearchParams params)` → `SalesChannelBrandPageResult`

- **Service 구현**: `SearchSalesChannelBrandByOffsetService`
  - File: `application/src/main/java/com/ryuqq/marketplace/application/saleschannelbrand/service/query/SearchSalesChannelBrandByOffsetService.java`
  - 1) `SalesChannelBrandQueryFactory.createCriteria(params)` 호출
  - 2) `SalesChannelBrandReadManager.findByCriteria(criteria)` 호출
  - 3) `SalesChannelBrandReadManager.countByCriteria(criteria)` 호출
  - 4) `SalesChannelBrandAssembler.toPageResult()` 조합

- **Factory**: `SalesChannelBrandQueryFactory`
  - File: `application/src/main/java/com/ryuqq/marketplace/application/saleschannelbrand/factory/SalesChannelBrandQueryFactory.java`
  - `Params` → `Criteria` 변환 책임
  - `SalesChannelBrandSortKey` 해석: `fieldName` 또는 `enum name` 대소문자 무관 매핑, 미매핑 시 `CREATED_AT` 기본값
  - `SalesChannelBrandStatus.fromString()` x N: 문자열 상태 목록 → `List<SalesChannelBrandStatus>`
  - `SalesChannelBrandSearchField.fromString()`: 문자열 검색 필드 → `SalesChannelBrandSearchField` (미매핑 시 null)
  - `CommonVoFactory`를 통해 `SortDirection`, `PageRequest`, `QueryContext` 생성

- **Manager**: `SalesChannelBrandReadManager`
  - File: `application/src/main/java/com/ryuqq/marketplace/application/saleschannelbrand/manager/SalesChannelBrandReadManager.java`
  - 모든 조회 메서드에 `@Transactional(readOnly = true)` 적용
  - `findByCriteria(SalesChannelBrandSearchCriteria)` → `queryPort.findByCriteria()`
  - `countByCriteria(SalesChannelBrandSearchCriteria)` → `queryPort.countByCriteria()`
  - `existsBySalesChannelIdAndExternalCode()` → `queryPort.existsBySalesChannelIdAndExternalCode()`

- **Assembler**: `SalesChannelBrandAssembler`
  - `SalesChannelBrand` → `SalesChannelBrandResult.from(brand)` (도메인 → Application Result)
  - `toPageResult()`: `List<SalesChannelBrand>` + 페이징 메타 → `SalesChannelBrandPageResult`

- **Params DTO**: `SalesChannelBrandSearchParams`
  ```java
  record SalesChannelBrandSearchParams(
    List<Long> salesChannelIds,    // 불변 복사본
    List<String> statuses,
    String searchField,
    String searchWord,
    CommonSearchParams searchParams
  )
  // 편의 메서드: page(), size(), sortKey(), sortDirection() - searchParams 위임
  ```

- **Result DTO**: `SalesChannelBrandPageResult`
  ```java
  record SalesChannelBrandPageResult(
    List<SalesChannelBrandResult> results,
    PageMeta pageMeta              // page, size, totalElements
  )
  ```

#### Domain Layer

- **Port**: `SalesChannelBrandQueryPort`
  - File: `application/src/main/java/com/ryuqq/marketplace/application/saleschannelbrand/port/out/query/SalesChannelBrandQueryPort.java`
  - `findById(SalesChannelBrandId id)` → `Optional<SalesChannelBrand>`
  - `findByCriteria(SalesChannelBrandSearchCriteria criteria)` → `List<SalesChannelBrand>`
  - `countByCriteria(SalesChannelBrandSearchCriteria criteria)` → `long`
  - `existsBySalesChannelIdAndExternalCode(Long, String)` → `boolean`

- **Criteria**: `SalesChannelBrandSearchCriteria`
  ```java
  record SalesChannelBrandSearchCriteria(
    List<Long> salesChannelIds,                     // 불변 복사본
    List<SalesChannelBrandStatus> statuses,         // 불변 복사본
    SalesChannelBrandSearchField searchField,       // null 허용 (전체 필드 검색)
    String searchWord,
    QueryContext<SalesChannelBrandSortKey> queryContext  // non-null
  )
  // 헬퍼: hasSalesChannelFilter(), hasStatusFilter(), hasSearchCondition(), hasSearchField()
  // 헬퍼: size(), offset(), page() - queryContext 위임
  ```

- **SortKey**: `SalesChannelBrandSortKey`
  - `CREATED_AT("createdAt")` - 기본값
  - `EXTERNAL_NAME("externalBrandName")`

- **SearchField**: `SalesChannelBrandSearchField`
  - `EXTERNAL_CODE("externalBrandCode")`
  - `EXTERNAL_NAME("externalBrandName")`
  - null 반환 시 → ConditionBuilder에서 두 필드 OR 검색

- **Aggregate**: `SalesChannelBrand`
  - `SalesChannelBrandId id` (Value Object)
  - `Long salesChannelId`
  - `String externalBrandCode` (final - 불변)
  - `String externalBrandName`
  - `SalesChannelBrandStatus status` (ACTIVE | INACTIVE)
  - `Instant createdAt` (final - 불변)
  - `Instant updatedAt`

- **Status VO**: `SalesChannelBrandStatus`
  - `ACTIVE`, `INACTIVE`
  - `fromString()`: 대소문자 무관, null/빈 값 → `ACTIVE` 기본값
  - `isActive()`: 활성 여부 확인

#### Adapter-Out Layer

- **Adapter**: `SalesChannelBrandQueryAdapter`
  - File: `adapter-out/persistence-mysql/src/main/java/com/ryuqq/marketplace/adapter/out/persistence/saleschannelbrand/adapter/SalesChannelBrandQueryAdapter.java`
  - `implements SalesChannelBrandQueryPort`
  - `findByCriteria()`: `repository.findByCriteria()` → `mapper.toDomain()` 스트림 변환
  - `countByCriteria()`: `repository.countByCriteria()` 직접 반환
  - `existsBySalesChannelIdAndExternalCode()`: `repository.existsBySalesChannelIdAndExternalCode()` 직접 반환

- **Repository**: `SalesChannelBrandQueryDslRepository`
  - File: `adapter-out/persistence-mysql/src/main/java/com/ryuqq/marketplace/adapter/out/persistence/saleschannelbrand/repository/SalesChannelBrandQueryDslRepository.java`
  - `JPAQueryFactory` + `SalesChannelBrandConditionBuilder` 조합
  - `findByCriteria()`: `selectFrom(brand).where(...).orderBy(...).offset().limit().fetch()`
  - `countByCriteria()`: `select(brand.count()).from(brand).where(...).fetchOne()`
  - `existsBySalesChannelIdAndExternalCode()`: `selectOne().from(brand).where(...).fetchFirst()` (null 여부로 판단)

- **ConditionBuilder**: `SalesChannelBrandConditionBuilder`
  - `salesChannelIdsIn()`: `brand.salesChannelId.in(salesChannelIds)` (비어있으면 null)
  - `statusIn()`: `brand.status.in(statusNames)` (필터 없으면 null)
  - `searchCondition()`: 검색어 없으면 null, 검색 필드 없으면 `externalBrandName LIKE OR externalBrandCode LIKE`
  - `externalBrandCodeEq()`: 정확일치 (중복 확인용)

- **OrderSpecifier 해석**:
  ```java
  switch (sortKey) {
    case CREATED_AT   → brand.createdAt.asc() | brand.createdAt.desc()
    case EXTERNAL_NAME → brand.externalBrandName.asc() | brand.externalBrandName.desc()
  }
  ```

- **JPA Entity**: `SalesChannelBrandJpaEntity`
  - Table: `sales_channel_brand`
  - `@GeneratedValue(IDENTITY)`
  - `BaseAuditEntity` 상속 (`createdAt`, `updatedAt` as `Instant`)
  - 필드: `id`, `salesChannelId`, `externalBrandCode (length=200)`, `externalBrandName (length=500)`, `status (length=20)`

- **EntityMapper**: `SalesChannelBrandJpaEntityMapper`
  - `toDomain(entity)`: `entity.getId()` null 체크 후 `SalesChannelBrand.reconstitute()` 호출
  - `SalesChannelBrandStatus.fromString(entity.getStatus())`: DB 문자열 → Enum 변환

### Database Query 분석

```sql
-- findByCriteria
SELECT *
FROM sales_channel_brand
WHERE sales_channel_id IN (?)           -- salesChannelIds (필수)
  AND status IN ('ACTIVE', ...)         -- 옵션: statuses 필터
  AND external_brand_name LIKE ?        -- 옵션: searchWord + searchField 조합
  AND external_brand_code LIKE ?        -- 또는 두 필드 OR (searchField=null 시)
ORDER BY created_at DESC                -- sortKey + sortDirection
OFFSET ? LIMIT ?                        -- page * size + size

-- countByCriteria
SELECT COUNT(*)
FROM sales_channel_brand
WHERE sales_channel_id IN (?)
  AND status IN (...)
  AND (external_brand_name LIKE ? OR external_brand_code LIKE ?)
```

---

## 2. POST /api/v1/market/sales-channels/{salesChannelId}/brands - 외부채널 브랜드 등록

### 호출 흐름 다이어그램

```
[Adapter-In]
SalesChannelBrandCommandController.registerBrand(salesChannelId, request)
  ├─ SalesChannelBrandCommandApiMapper.toCommand(salesChannelId, request)
  │   └─ -> RegisterSalesChannelBrandCommand
  └─ RegisterSalesChannelBrandUseCase.execute(command)                   [Port Interface]
      └─ -> Long brandId

[Application]
RegisterSalesChannelBrandService.execute(command)                        [UseCase 구현]
  ├─ SalesChannelBrandValidator.validateExternalCodeNotDuplicate(...)    [중복 검증]
  │   └─ SalesChannelBrandReadManager.existsBySalesChannelIdAndExternalCode()
  │       └─ SalesChannelBrandQueryPort.existsBySalesChannelIdAndExternalCode() [Port]
  │           → true: throw SalesChannelBrandCodeDuplicateException (409)
  ├─ SalesChannelBrandCommandFactory.create(command)                     [Domain 생성]
  │   ├─ TimeProvider.now()              → Instant now
  │   └─ SalesChannelBrand.forNew(...)   → SalesChannelBrand (status=ACTIVE)
  └─ SalesChannelBrandCommandManager.persist(brand)                      [@Transactional]
      └─ SalesChannelBrandCommandPort.persist(brand)                     [Port]

[Adapter-Out]
SalesChannelBrandCommandAdapter                                          [Port 구현]
  ├─ SalesChannelBrandJpaEntityMapper.toEntity(brand)
  │   └─ SalesChannelBrandJpaEntity.create(null, salesChannelId, code, name, "ACTIVE", now, now)
  └─ SalesChannelBrandJpaRepository.save(entity)
      └─ -> saved.getId()               → Long brandId

[Database]
- INSERT INTO sales_channel_brand
```

### Layer별 상세

#### Adapter-In Layer

- **Controller**: `SalesChannelBrandCommandController`
  - File: `adapter-in/rest-api/src/main/java/com/ryuqq/marketplace/adapter/in/rest/saleschannelbrand/controller/SalesChannelBrandCommandController.java`
  - Method: `registerBrand(Long salesChannelId, RegisterSalesChannelBrandApiRequest request)`
  - 어노테이션: `@PostMapping`, `@PreAuthorize("@access.superAdmin()")`, `@RequirePermission("sales-channel-brand:write")`
  - Response: `ResponseEntity<ApiResponse<SalesChannelBrandIdApiResponse>>`
  - HTTP Status: **201 Created**

- **Request DTO**: `RegisterSalesChannelBrandApiRequest`
  ```java
  record RegisterSalesChannelBrandApiRequest(
    @NotBlank String externalBrandCode,   // 외부 브랜드 코드 (필수)
    @NotBlank String externalBrandName    // 외부 브랜드명 (필수)
  )
  ```

- **Response DTO**: `SalesChannelBrandIdApiResponse`
  ```java
  record SalesChannelBrandIdApiResponse(List<Long> brandIds) {
    static of(Long brandId)       // → new SalesChannelBrandIdApiResponse(List.of(brandId))
    static of(List<Long> brandIds) // 배치 등록 대비
  }
  ```
  - 단건 등록 결과도 `List<Long>` 래핑 (일관성 유지, 배치 확장 대비)

- **ApiMapper**: `SalesChannelBrandCommandApiMapper`
  - `toCommand(Long salesChannelId, RegisterSalesChannelBrandApiRequest)` → `RegisterSalesChannelBrandCommand`
  - Path Variable `salesChannelId` + Request Body 필드 조합하여 Command 생성

#### Application Layer

- **UseCase Interface**: `RegisterSalesChannelBrandUseCase`
  - File: `application/src/main/java/com/ryuqq/marketplace/application/saleschannelbrand/port/in/command/RegisterSalesChannelBrandUseCase.java`
  - `execute(RegisterSalesChannelBrandCommand command)` → `Long` (brandId)

- **Service 구현**: `RegisterSalesChannelBrandService`
  - File: `application/src/main/java/com/ryuqq/marketplace/application/saleschannelbrand/service/command/RegisterSalesChannelBrandService.java`
  - 단계: 1) 중복 검증 → 2) Domain 생성 → 3) 영속화
  - Coordinator/Facade 없이 Service에서 직접 Validator, Factory, Manager 호출 (단순한 단일 Aggregate 등록이므로)

- **Validator**: `SalesChannelBrandValidator`
  - File: `application/src/main/java/com/ryuqq/marketplace/application/saleschannelbrand/validator/SalesChannelBrandValidator.java`
  - `validateExternalCodeNotDuplicate(Long salesChannelId, String externalBrandCode)`
    - `ReadManager.existsBySalesChannelIdAndExternalCode()` 호출
    - 존재하면 `SalesChannelBrandCodeDuplicateException` 발생 (409 Conflict)
  - `findExistingOrThrow(SalesChannelBrandId)`: 존재 확인 후 Domain 반환 (미래 수정 API 대비)

- **Factory**: `SalesChannelBrandCommandFactory`
  - File: `application/src/main/java/com/ryuqq/marketplace/application/saleschannelbrand/factory/SalesChannelBrandCommandFactory.java`
  - `create(RegisterSalesChannelBrandCommand)` → `SalesChannelBrand`
  - `TimeProvider.now()` 호출 (APP-TIM-001 규칙: Factory에서만 시간 획득)
  - `SalesChannelBrand.forNew(salesChannelId, externalBrandCode, externalBrandName, now)`
    - `id = SalesChannelBrandId.forNew()` (value=null, 영속화 후 DB 생성)
    - `status = ACTIVE` (신규 등록 기본값)

- **Manager**: `SalesChannelBrandCommandManager`
  - File: `application/src/main/java/com/ryuqq/marketplace/application/saleschannelbrand/manager/SalesChannelBrandCommandManager.java`
  - 클래스 레벨 `@Transactional` (쓰기 트랜잭션)
  - `persist(SalesChannelBrand)` → `commandPort.persist(brand)` → `Long`

- **Command DTO**: `RegisterSalesChannelBrandCommand`
  ```java
  record RegisterSalesChannelBrandCommand(
    Long salesChannelId,
    String externalBrandCode,
    String externalBrandName
  )
  ```

#### Domain Layer

- **Port**: `SalesChannelBrandCommandPort`
  - File: `application/src/main/java/com/ryuqq/marketplace/application/saleschannelbrand/port/out/command/SalesChannelBrandCommandPort.java`
  - `persist(SalesChannelBrand brand)` → `Long` (저장된 ID)

- **Aggregate**: `SalesChannelBrand`
  - `forNew()` 팩토리 메서드:
    - `id = SalesChannelBrandId.forNew()` (value=null)
    - `status = SalesChannelBrandStatus.ACTIVE` (고정)
    - `createdAt = updatedAt = now`
  - `reconstitute()` 팩토리 메서드: DB에서 재구성 시 사용 (id 포함)

- **ID VO**: `SalesChannelBrandId`
  ```java
  record SalesChannelBrandId(Long value) {
    static of(Long value)  // null 불가 검증
    static forNew()        // value=null (신규 생성용)
    boolean isNew()        // value == null
  }
  ```

- **예외**:
  - `SalesChannelBrandNotFoundException` (SCBRD-001, 404): 존재하지 않는 브랜드 조회
  - `SalesChannelBrandCodeDuplicateException` (SCBRD-002, 409): 코드 중복 등록 시도

#### Adapter-Out Layer

- **Adapter**: `SalesChannelBrandCommandAdapter`
  - File: `adapter-out/persistence-mysql/src/main/java/com/ryuqq/marketplace/adapter/out/persistence/saleschannelbrand/adapter/SalesChannelBrandCommandAdapter.java`
  - `implements SalesChannelBrandCommandPort`
  - `mapper.toEntity(brand)` → `repository.save(entity)` → `saved.getId()`

- **EntityMapper**: `SalesChannelBrandJpaEntityMapper.toEntity()`
  - `brand.idValue()`: 신규 등록 시 null → JPA `@GeneratedValue(IDENTITY)` 처리
  - `brand.status().name()`: `SalesChannelBrandStatus` → `"ACTIVE"` 문자열

- **JPA Repository**: `SalesChannelBrandJpaRepository`
  - `extends JpaRepository<SalesChannelBrandJpaEntity, Long>`
  - `save(entity)` 만 사용 (단순 CRUD)

### Database Query 분석

```sql
-- 중복 확인 (existsBySalesChannelIdAndExternalCode)
SELECT 1
FROM sales_channel_brand
WHERE sales_channel_id = ?
  AND external_brand_code = ?
LIMIT 1

-- 브랜드 등록
INSERT INTO sales_channel_brand (
  sales_channel_id,
  external_brand_code,
  external_brand_name,
  status,
  created_at,
  updated_at
) VALUES (?, ?, ?, 'ACTIVE', ?, ?)
```

---

## 공통 패턴 분석

### 1. Hexagonal Architecture 레이어 격리

| 레이어 | DTO/모델 | 책임 |
|--------|----------|------|
| **Adapter-In** | `SearchSalesChannelBrandsApiRequest`, `RegisterSalesChannelBrandApiRequest`, `SalesChannelBrandApiResponse`, `SalesChannelBrandIdApiResponse` | HTTP 계층 (Validation, 직렬화, ISO-8601 변환) |
| **Application** | `SalesChannelBrandSearchParams`, `RegisterSalesChannelBrandCommand`, `SalesChannelBrandResult`, `SalesChannelBrandPageResult` | 유스케이스 조율, 트랜잭션 경계 |
| **Domain** | `SalesChannelBrand`, `SalesChannelBrandSearchCriteria`, `SalesChannelBrandStatus`, `SalesChannelBrandId` | 비즈니스 규칙, 불변성 |
| **Adapter-Out** | `SalesChannelBrandJpaEntity` | JPA 영속화 기술 관심사 |

### 2. CQRS 분리

- **Query**: `SalesChannelBrandQueryController` → `SalesChannelBrandQueryApiMapper` → `SearchSalesChannelBrandByOffsetUseCase` → `SearchSalesChannelBrandByOffsetService`
- **Command**: `SalesChannelBrandCommandController` → `SalesChannelBrandCommandApiMapper` → `RegisterSalesChannelBrandUseCase` → `RegisterSalesChannelBrandService`

### 3. 트랜잭션 경계

| 계층 | @Transactional 위치 | 비고 |
|------|---------------------|------|
| Adapter-In | 없음 | |
| Application Service | 없음 | Manager/Manager에 위임 |
| **ReadManager** | `@Transactional(readOnly = true)` | 각 메서드 단위 |
| **CommandManager** | `@Transactional` (클래스 레벨) | 쓰기 트랜잭션 |
| Adapter-Out | 없음 | |

### 4. DTO 변환 체인

```
[Query]
SearchSalesChannelBrandsApiRequest
  ↓ SalesChannelBrandQueryApiMapper.toSearchParams()
SalesChannelBrandSearchParams
  ↓ SalesChannelBrandQueryFactory.createCriteria()
SalesChannelBrandSearchCriteria
  ↓ QueryDSL → SalesChannelBrandJpaEntity
  ↓ SalesChannelBrandJpaEntityMapper.toDomain()
SalesChannelBrand
  ↓ SalesChannelBrandAssembler.toResult()
SalesChannelBrandResult / SalesChannelBrandPageResult
  ↓ SalesChannelBrandQueryApiMapper.toPageResponse()
PageApiResponse<SalesChannelBrandApiResponse>

[Command]
RegisterSalesChannelBrandApiRequest + salesChannelId (PathVariable)
  ↓ SalesChannelBrandCommandApiMapper.toCommand()
RegisterSalesChannelBrandCommand
  ↓ SalesChannelBrandCommandFactory.create()
SalesChannelBrand (forNew)
  ↓ SalesChannelBrandJpaEntityMapper.toEntity()
SalesChannelBrandJpaEntity
  ↓ JpaRepository.save()
saved.getId() → Long
```

### 5. 검색 필드 조건 분기

| searchField 값 | DB 조건 |
|----------------|---------|
| null (미지정) | `external_brand_name LIKE ? OR external_brand_code LIKE ?` |
| `externalBrandCode` 또는 `EXTERNAL_CODE` | `external_brand_code LIKE ?` |
| `externalBrandName` 또는 `EXTERNAL_NAME` | `external_brand_name LIKE ?` |

### 6. Path Variable의 List 변환 설계

`salesChannelId`(단건 Long) → `List.of(salesChannelId)` 변환 후 `SalesChannelBrandSearchParams`에 전달하는 이유:
- `SalesChannelBrandQueryPort.findByCriteria()`가 `salesChannelIds: List<Long>` 기반으로 설계되어 있음
- 향후 여러 채널에 걸친 배치 조회 확장 가능성을 열어둔 구조

### 7. 등록 결과의 List 래핑

`SalesChannelBrandIdApiResponse.of(Long brandId)` → `List.of(brandId)`:
- `brandIds: List<Long>` 응답 타입으로 배치 등록 시나리오 대비
- 단건/복수 등록 응답 구조 통일

---

## 에러 처리

| 에러코드 | HTTP Status | 설명 | 발생 위치 |
|----------|-------------|------|-----------|
| SCBRD-001 | 404 Not Found | 외부 채널 브랜드를 찾을 수 없습니다 | `SalesChannelBrandReadManager.getById()` |
| SCBRD-002 | 409 Conflict | 이미 존재하는 외부 브랜드 코드입니다 | `SalesChannelBrandValidator.validateExternalCodeNotDuplicate()` |

**ErrorMapper**: `SalesChannelBrandErrorMapper`
- `supports(DomainException)`: `instanceof SalesChannelBrandException` 체크
- `map()`: `HttpStatus.valueOf(ex.httpStatus())` + URI `/errors/sales-channel-brand/{code.toLowerCase()}`

---

## 주요 설계 결정

### 장점

1. **레이어 격리**: 각 레이어 독립적 DTO → 변경 격리 (예: API 필드 변경이 Domain에 영향 없음)
2. **단방향 의존성**: Domain은 Application/Adapter를 모름, Application은 Adapter를 모름
3. **중복 검증 선행**: 영속화 전 `existsBySalesChannelIdAndExternalCode()` 호출로 409 조기 반환
4. **시간 획득 단일화**: `TimeProvider.now()`를 Factory에서만 호출 (APP-TIM-001 규칙)
5. **QueryFactory**: Params → Criteria 변환 로직 분리 → Service 단순화
6. **ConditionBuilder**: QueryDSL 조건 생성 로직 분리 → Repository 단순화

### 트레이드오프

1. **DTO 변환 단계 多**: ApiRequest → Params → Criteria → Entity → Domain → Result → ApiResponse (7단계)
2. **Facade/Coordinator 미사용**: 단일 Aggregate 등록이므로 Service에서 직접 Validator + Factory + Manager 호출 (단순성 우선)
3. **salesChannelId 외부 채널 존재 검증 없음**: Path Variable의 `salesChannelId`가 실제 존재하는 채널인지 컨트롤러/서비스 계층에서 검증하지 않음 (UseCase 계층에서 암묵적 처리)

---

## 파일 경로 요약

| 레이어 | 파일 |
|--------|------|
| Adapter-In: Endpoints | `adapter-in/rest-api/src/main/java/com/ryuqq/marketplace/adapter/in/rest/saleschannelbrand/SalesChannelBrandAdminEndpoints.java` |
| Adapter-In: QueryController | `adapter-in/rest-api/src/main/java/com/ryuqq/marketplace/adapter/in/rest/saleschannelbrand/controller/SalesChannelBrandQueryController.java` |
| Adapter-In: CommandController | `adapter-in/rest-api/src/main/java/com/ryuqq/marketplace/adapter/in/rest/saleschannelbrand/controller/SalesChannelBrandCommandController.java` |
| Adapter-In: QueryMapper | `adapter-in/rest-api/src/main/java/com/ryuqq/marketplace/adapter/in/rest/saleschannelbrand/mapper/SalesChannelBrandQueryApiMapper.java` |
| Adapter-In: CommandMapper | `adapter-in/rest-api/src/main/java/com/ryuqq/marketplace/adapter/in/rest/saleschannelbrand/mapper/SalesChannelBrandCommandApiMapper.java` |
| Adapter-In: Request (Query) | `adapter-in/rest-api/src/main/java/com/ryuqq/marketplace/adapter/in/rest/saleschannelbrand/dto/query/SearchSalesChannelBrandsApiRequest.java` |
| Adapter-In: Request (Command) | `adapter-in/rest-api/src/main/java/com/ryuqq/marketplace/adapter/in/rest/saleschannelbrand/dto/command/RegisterSalesChannelBrandApiRequest.java` |
| Adapter-In: Response | `adapter-in/rest-api/src/main/java/com/ryuqq/marketplace/adapter/in/rest/saleschannelbrand/dto/response/SalesChannelBrandApiResponse.java` |
| Adapter-In: Response (ID) | `adapter-in/rest-api/src/main/java/com/ryuqq/marketplace/adapter/in/rest/saleschannelbrand/dto/response/SalesChannelBrandIdApiResponse.java` |
| Adapter-In: ErrorMapper | `adapter-in/rest-api/src/main/java/com/ryuqq/marketplace/adapter/in/rest/saleschannelbrand/error/SalesChannelBrandErrorMapper.java` |
| Application: QueryUseCase | `application/src/main/java/com/ryuqq/marketplace/application/saleschannelbrand/port/in/query/SearchSalesChannelBrandByOffsetUseCase.java` |
| Application: CommandUseCase | `application/src/main/java/com/ryuqq/marketplace/application/saleschannelbrand/port/in/command/RegisterSalesChannelBrandUseCase.java` |
| Application: QueryService | `application/src/main/java/com/ryuqq/marketplace/application/saleschannelbrand/service/query/SearchSalesChannelBrandByOffsetService.java` |
| Application: CommandService | `application/src/main/java/com/ryuqq/marketplace/application/saleschannelbrand/service/command/RegisterSalesChannelBrandService.java` |
| Application: QueryFactory | `application/src/main/java/com/ryuqq/marketplace/application/saleschannelbrand/factory/SalesChannelBrandQueryFactory.java` |
| Application: CommandFactory | `application/src/main/java/com/ryuqq/marketplace/application/saleschannelbrand/factory/SalesChannelBrandCommandFactory.java` |
| Application: ReadManager | `application/src/main/java/com/ryuqq/marketplace/application/saleschannelbrand/manager/SalesChannelBrandReadManager.java` |
| Application: CommandManager | `application/src/main/java/com/ryuqq/marketplace/application/saleschannelbrand/manager/SalesChannelBrandCommandManager.java` |
| Application: Validator | `application/src/main/java/com/ryuqq/marketplace/application/saleschannelbrand/validator/SalesChannelBrandValidator.java` |
| Application: Assembler | `application/src/main/java/com/ryuqq/marketplace/application/saleschannelbrand/assembler/SalesChannelBrandAssembler.java` |
| Application: QueryPort | `application/src/main/java/com/ryuqq/marketplace/application/saleschannelbrand/port/out/query/SalesChannelBrandQueryPort.java` |
| Application: CommandPort | `application/src/main/java/com/ryuqq/marketplace/application/saleschannelbrand/port/out/command/SalesChannelBrandCommandPort.java` |
| Application: SearchParams | `application/src/main/java/com/ryuqq/marketplace/application/saleschannelbrand/dto/query/SalesChannelBrandSearchParams.java` |
| Application: Command | `application/src/main/java/com/ryuqq/marketplace/application/saleschannelbrand/dto/command/RegisterSalesChannelBrandCommand.java` |
| Application: Result | `application/src/main/java/com/ryuqq/marketplace/application/saleschannelbrand/dto/response/SalesChannelBrandResult.java` |
| Application: PageResult | `application/src/main/java/com/ryuqq/marketplace/application/saleschannelbrand/dto/response/SalesChannelBrandPageResult.java` |
| Domain: Aggregate | `domain/src/main/java/com/ryuqq/marketplace/domain/saleschannelbrand/aggregate/SalesChannelBrand.java` |
| Domain: ID | `domain/src/main/java/com/ryuqq/marketplace/domain/saleschannelbrand/id/SalesChannelBrandId.java` |
| Domain: Status | `domain/src/main/java/com/ryuqq/marketplace/domain/saleschannelbrand/vo/SalesChannelBrandStatus.java` |
| Domain: Criteria | `domain/src/main/java/com/ryuqq/marketplace/domain/saleschannelbrand/query/SalesChannelBrandSearchCriteria.java` |
| Domain: SortKey | `domain/src/main/java/com/ryuqq/marketplace/domain/saleschannelbrand/query/SalesChannelBrandSortKey.java` |
| Domain: SearchField | `domain/src/main/java/com/ryuqq/marketplace/domain/saleschannelbrand/query/SalesChannelBrandSearchField.java` |
| Domain: ErrorCode | `domain/src/main/java/com/ryuqq/marketplace/domain/saleschannelbrand/exception/SalesChannelBrandErrorCode.java` |
| Adapter-Out: QueryAdapter | `adapter-out/persistence-mysql/src/main/java/com/ryuqq/marketplace/adapter/out/persistence/saleschannelbrand/adapter/SalesChannelBrandQueryAdapter.java` |
| Adapter-Out: CommandAdapter | `adapter-out/persistence-mysql/src/main/java/com/ryuqq/marketplace/adapter/out/persistence/saleschannelbrand/adapter/SalesChannelBrandCommandAdapter.java` |
| Adapter-Out: QueryDslRepo | `adapter-out/persistence-mysql/src/main/java/com/ryuqq/marketplace/adapter/out/persistence/saleschannelbrand/repository/SalesChannelBrandQueryDslRepository.java` |
| Adapter-Out: JpaRepo | `adapter-out/persistence-mysql/src/main/java/com/ryuqq/marketplace/adapter/out/persistence/saleschannelbrand/repository/SalesChannelBrandJpaRepository.java` |
| Adapter-Out: ConditionBuilder | `adapter-out/persistence-mysql/src/main/java/com/ryuqq/marketplace/adapter/out/persistence/saleschannelbrand/condition/SalesChannelBrandConditionBuilder.java` |
| Adapter-Out: Entity | `adapter-out/persistence-mysql/src/main/java/com/ryuqq/marketplace/adapter/out/persistence/saleschannelbrand/entity/SalesChannelBrandJpaEntity.java` |
| Adapter-Out: EntityMapper | `adapter-out/persistence-mysql/src/main/java/com/ryuqq/marketplace/adapter/out/persistence/saleschannelbrand/mapper/SalesChannelBrandJpaEntityMapper.java` |

---

## 문서 생성 정보

- **분석 일시**: 2026-02-18
- **대상 모듈**: `adapter-in/rest-api`, `application`, `domain`, `adapter-out/persistence-mysql`
- **엔드포인트 Base**: `/api/v1/market/sales-channels/{salesChannelId}/brands`
- **참조 문서**: `.claude/docs/api-endpoints/saleschannelbrand.md`
