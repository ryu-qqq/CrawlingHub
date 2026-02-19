# BrandPreset Domain API Flow Analysis

브랜드 프리셋 도메인의 전체 API 호출 흐름 분석 문서입니다.

---

## 엔드포인트 목록

| HTTP Method | Path | 설명 | Controller Method |
|-------------|------|------|-------------------|
| GET | `/api/v1/market/brand-presets` | 브랜드 프리셋 목록 조회 (Offset 페이징) | `searchBrandPresets()` |
| GET | `/api/v1/market/brand-presets/{brandPresetId}` | 브랜드 프리셋 상세 조회 | `getBrandPreset()` |
| POST | `/api/v1/market/brand-presets` | 브랜드 프리셋 등록 | `registerBrandPreset()` |
| PUT | `/api/v1/market/brand-presets/{brandPresetId}` | 브랜드 프리셋 수정 | `updateBrandPreset()` |
| DELETE | `/api/v1/market/brand-presets` | 브랜드 프리셋 벌크 삭제(비활성화) | `deleteBrandPresets()` |

---

## 1. GET /api/v1/market/brand-presets - 브랜드 프리셋 목록 조회

### 호출 흐름 다이어그램

```
[Adapter-In]
BrandPresetQueryController.searchBrandPresets(SearchBrandPresetsApiRequest)
  ├─ BrandPresetQueryApiMapper.toSearchParams(request)         [Params 변환]
  │   ├─ CommonSearchParams.of(page=0, size=20, ...)
  │   └─ BrandPresetSearchParams.of(salesChannelIds, statuses, searchField, searchWord, commonParams)
  ├─ SearchBrandPresetByOffsetUseCase.execute(params)          [Port Interface]
  └─ BrandPresetQueryApiMapper.toPageResponse(pageResult)      [Response 변환]

[Application]
SearchBrandPresetByOffsetService.execute(params)               [UseCase 구현]
  ├─ BrandPresetQueryFactory.createCriteria(params)            [Criteria 생성]
  │   └─ BrandPresetSearchCriteria (salesChannelIds, statuses, searchField, searchWord, startDate, endDate, QueryContext)
  ├─ BrandPresetReadManager.findByCriteria(criteria)           [목록 조회]
  │   └─ BrandPresetQueryPort.findByCriteria()                 [Port]
  ├─ BrandPresetReadManager.countByCriteria(criteria)          [카운트]
  │   └─ BrandPresetQueryPort.countByCriteria()                [Port]
  └─ BrandPresetAssembler.toPageResult(results, page, size, totalElements)

[Adapter-Out]
BrandPresetQueryAdapter                                         [Port 구현]
  ├─ BrandPresetQueryDslRepository.findByCriteria(criteria)
  │   └─ QueryDSL: brand_preset JOIN shop JOIN sales_channel_brand JOIN sales_channel
  └─ BrandPresetQueryDslRepository.countByCriteria(criteria)
      └─ QueryDSL: COUNT(*) with same JOIN/WHERE

[Database]
- brand_preset (기본 검색 대상)
- shop (shopName, accountId JOIN)
- sales_channel_brand (externalBrandCode, externalBrandName JOIN)
- sales_channel (channelName JOIN)
```

### Layer별 상세

#### Adapter-In Layer

- **Controller**: `BrandPresetQueryController`
  - Method: `searchBrandPresets(SearchBrandPresetsApiRequest)`
  - Response: `ApiResponse<PageApiResponse<BrandPresetApiResponse>>`
  - HTTP Status: 200 OK
  - 권한: `@PreAuthorize("@access.authenticated()")`, `brand-preset:read`

- **Request DTO**: `SearchBrandPresetsApiRequest`
  ```java
  record SearchBrandPresetsApiRequest(
    List<Long> salesChannelIds,   // 판매채널 ID 목록
    List<String> statuses,        // 상태 필터 (ACTIVE, INACTIVE)
    String searchField,           // 검색 필드 (PRESET_NAME, SHOP_NAME, ACCOUNT_ID, BRAND_NAME, BRAND_CODE)
    String searchWord,            // 검색어
    String startDate,             // 등록일 시작 (YYYY-MM-DD)
    String endDate,               // 등록일 종료 (YYYY-MM-DD)
    String sortKey,               // 정렬 키 (createdAt)
    String sortDirection,         // 정렬 방향 (ASC, DESC)
    Integer page,                 // 페이지 번호 (기본값: 0)
    Integer size                  // 페이지 크기 (기본값: 20)
  )
  ```

- **Response DTO**: `BrandPresetApiResponse`
  ```java
  record BrandPresetApiResponse(
    Long id,
    Long shopId,
    String shopName,
    Long salesChannelId,
    String salesChannelName,
    String accountId,
    String presetName,
    String brandName,
    String brandCode,
    String createdAt    // ISO8601 문자열 (DateTimeFormatUtils 변환)
  )
  ```

- **ApiMapper**: `BrandPresetQueryApiMapper`
  - `toSearchParams()`: `startDate`/`endDate` 문자열 → `LocalDate` 파싱 (실패 시 null), page/size 기본값 처리
  - `toResponses()`: `List<BrandPresetResult>` → `List<BrandPresetApiResponse>` (stream map)
  - `toPageResponse()`: `BrandPresetPageResult` → `PageApiResponse<BrandPresetApiResponse>`

#### Application Layer

- **UseCase Interface**: `SearchBrandPresetByOffsetUseCase`
  - `execute(BrandPresetSearchParams)` → `BrandPresetPageResult`

- **Service 구현**: `SearchBrandPresetByOffsetService`
  - QueryFactory로 Params → Criteria 변환
  - ReadManager를 통해 목록 조회 + 카운트
  - Assembler로 PageResult 생성

- **Params DTO**: `BrandPresetSearchParams`
  - `salesChannelIds`, `statuses`, `searchField`, `searchWord` + `CommonSearchParams`

- **Criteria**: `BrandPresetSearchCriteria`
  ```java
  record BrandPresetSearchCriteria(
    List<Long> salesChannelIds,
    List<String> statuses,
    String searchField,
    String searchWord,
    LocalDate startDate,
    LocalDate endDate,
    QueryContext<BrandPresetSortKey> queryContext
  )
  ```
  - `hasSalesChannelFilter()`, `hasStatusFilter()`, `hasSearchFilter()` 등 필터 존재 여부 메서드 제공

- **Result DTO**: `BrandPresetPageResult`
  ```java
  record BrandPresetPageResult(
    List<BrandPresetResult> results,
    PageMeta pageMeta    // page, size, totalElements
  )
  ```

- **Manager**: `BrandPresetReadManager`
  - `findByCriteria(criteria)`: `@Transactional(readOnly = true)` 적용
  - `countByCriteria(criteria)`: `@Transactional(readOnly = true)` 적용

- **Factory**: `BrandPresetQueryFactory`
  - `BrandPresetSortKey` 해석 (문자열 → enum, 미매칭 시 defaultKey() = `CREATED_AT`)
  - `SortDirection`, `PageRequest`, `QueryContext` 생성 (CommonVoFactory 위임)

#### Domain Layer

- **Port**: `BrandPresetQueryPort`
  - `findByCriteria(BrandPresetSearchCriteria)` → `List<BrandPresetResult>`
  - `countByCriteria(BrandPresetSearchCriteria)` → `long`

- **Domain VO**: `BrandPresetSearchCriteria` (불변 record)

#### Adapter-Out Layer

- **Adapter**: `BrandPresetQueryAdapter`
  - `findByCriteria()`: QueryDslRepository 호출 → `BrandPresetCompositeDto` → `BrandPresetResult` 변환 (`mapper.toResult()`)
  - `countByCriteria()`: QueryDslRepository 직접 위임

- **Repository**: `BrandPresetQueryDslRepository`
  - `BrandPresetConditionBuilder` 사용한 동적 WHERE 절 생성
  - Projections.constructor()로 `BrandPresetCompositeDto` 직접 조회

- **Composite DTO**: `BrandPresetCompositeDto`
  - QueryDSL 조인 결과 → Projection 대상 (id, shopId, shopName, accountId, salesChannelId, salesChannelName, salesChannelBrandId, externalBrandCode, externalBrandName, presetName, status, createdAt)

- **Database Query**:
  ```sql
  -- findByCriteria
  SELECT
    bp.id, bp.shop_id, s.shop_name, s.account_id,
    scb.sales_channel_id, sc.channel_name, bp.sales_channel_brand_id,
    scb.external_brand_code, scb.external_brand_name,
    bp.preset_name, bp.status, bp.created_at
  FROM brand_preset bp
  JOIN shop s ON bp.shop_id = s.id
  JOIN sales_channel_brand scb ON bp.sales_channel_brand_id = scb.id
  JOIN sales_channel sc ON scb.sales_channel_id = sc.id
  WHERE
    scb.sales_channel_id IN (?, ?, ...)   -- salesChannelIds (옵션)
    AND bp.status IN (?, ...)              -- statuses (옵션)
    AND bp.preset_name LIKE ?              -- searchField=PRESET_NAME (옵션)
    AND bp.created_at >= ?                 -- startDate (옵션, Asia/Seoul 기준)
    AND bp.created_at <= ?                 -- endDate (옵션, LocalTime.MAX)
  ORDER BY bp.created_at DESC             -- BrandPresetSortKey.CREATED_AT
  LIMIT ? OFFSET ?

  -- countByCriteria (JOIN + WHERE 동일, SELECT COUNT(*))
  SELECT COUNT(*)
  FROM brand_preset bp
  JOIN shop s ON bp.shop_id = s.id
  JOIN sales_channel_brand scb ON bp.sales_channel_brand_id = scb.id
  WHERE [동일 조건]
  ```

---

## 2. GET /api/v1/market/brand-presets/{brandPresetId} - 브랜드 프리셋 상세 조회

### 호출 흐름 다이어그램

```
[Adapter-In]
BrandPresetQueryController.getBrandPreset(brandPresetId)
  ├─ GetBrandPresetDetailUseCase.execute(brandPresetId)        [Port Interface]
  └─ BrandPresetQueryApiMapper.toDetailResponse(result)        [Response 변환]

[Application]
GetBrandPresetDetailService.execute(brandPresetId)             [UseCase 구현]
  └─ BrandPresetCompositionReadManager.getDetail(brandPresetId)
      └─ BrandPresetCompositionQueryPort.findDetailById()      [Port]
          → orElseThrow(BrandPresetNotFoundException)

[Adapter-Out] (크로스 도메인 2-Step 조회)
BrandPresetCompositionQueryAdapter                             [Port 구현]
  ├─ Step 1: BrandPresetQueryDslRepository.findDetailCompositeById(id)
  │   └─ QueryDSL: brand_preset JOIN shop JOIN sales_channel_brand JOIN sales_channel
  │       → BrandPresetDetailCompositeDto
  └─ Step 2: BrandMappingQueryDslRepository.findMappedBrandsByPresetId(id)
      └─ QueryDSL: brand_mapping JOIN brand (WHERE status = 'ACTIVE')
          → List<BrandMappingWithBrandDto>

[Database]
- brand_preset (프리셋 기본정보)
- shop (shopName, accountId)
- sales_channel_brand (externalBrandCode, externalBrandName)
- sales_channel (channelName)
- brand_mapping (내부 브랜드 매핑)
- brand (내부 브랜드명)
```

### Layer별 상세

#### Adapter-In Layer

- **Controller**: `BrandPresetQueryController`
  - Method: `getBrandPreset(Long brandPresetId)`
  - Response: `ApiResponse<BrandPresetDetailApiResponse>`
  - HTTP Status: 200 OK
  - 권한: `@PreAuthorize("@access.authenticated()")`, `brand-preset:read`

- **Request**: `@PathVariable Long brandPresetId` (DTO 없음)

- **Response DTO**: `BrandPresetDetailApiResponse`
  ```java
  record BrandPresetDetailApiResponse(
    Long id,
    Long shopId,
    String shopName,
    Long salesChannelId,
    String salesChannelName,
    String accountId,
    String presetName,
    MappingBrandResponse mappingBrand,      // 중첩 record
    List<InternalBrandResponse> internalBrands,  // 중첩 record 목록
    String createdAt,
    String updatedAt
  )

  record MappingBrandResponse(String brandCode, String brandName)
  record InternalBrandResponse(Long id, String brandName)
  ```

- **ApiMapper**: `BrandPresetQueryApiMapper`
  - `toDetailResponse(BrandPresetDetailResult)`: 중첩 객체 `MappingBrandResponse`, `List<InternalBrandResponse>` 변환 포함
  - `createdAt`, `updatedAt`: `Instant` → ISO8601 문자열 (DateTimeFormatUtils)

#### Application Layer

- **UseCase Interface**: `GetBrandPresetDetailUseCase`
  - `execute(Long brandPresetId)` → `BrandPresetDetailResult`

- **Service 구현**: `GetBrandPresetDetailService`
  - `BrandPresetCompositionReadManager.getDetail(id)` 단일 위임

- **Manager**: `BrandPresetCompositionReadManager`
  - `getDetail(Long id)`: `@Transactional(readOnly = true)` 적용
  - `BrandPresetCompositionQueryPort.findDetailById()` 호출
  - `Optional.orElseThrow(BrandPresetNotFoundException)`

- **Port**: `BrandPresetCompositionQueryPort`
  - `findDetailById(Long id)` → `Optional<BrandPresetDetailResult>`

- **Result DTO**: `BrandPresetDetailResult`
  ```java
  record BrandPresetDetailResult(
    Long id, Long shopId, String shopName,
    Long salesChannelId, String salesChannelName,
    String accountId, String presetName,
    MappingBrand mappingBrand,
    List<InternalBrand> internalBrands,
    Instant createdAt, Instant updatedAt
  )
  record MappingBrand(String brandCode, String brandName)
  record InternalBrand(Long id, String brandName)
  ```

#### Adapter-Out Layer

- **Adapter**: `BrandPresetCompositionQueryAdapter`
  - 2-Step 조회 패턴:
    1. `BrandPresetQueryDslRepository.findDetailCompositeById(id)` → 프리셋 + Shop + SalesChannelBrand JOIN
    2. `BrandMappingQueryDslRepository.findMappedBrandsByPresetId(id)` → BrandMapping + Brand JOIN
  - 두 결과를 조합하여 `BrandPresetDetailResult` 생성

- **Composite DTO**: `BrandPresetDetailCompositeDto`
  - `BrandPresetCompositeDto`에 `updatedAt` 추가된 확장 버전

- **Database Query**:
  ```sql
  -- Step 1: 프리셋 + Shop + SalesChannelBrand + SalesChannel
  SELECT
    bp.id, bp.shop_id, s.shop_name, s.account_id,
    scb.sales_channel_id, sc.channel_name, bp.sales_channel_brand_id,
    scb.external_brand_code, scb.external_brand_name,
    bp.preset_name, bp.status, bp.created_at, bp.updated_at
  FROM brand_preset bp
  JOIN shop s ON bp.shop_id = s.id
  JOIN sales_channel_brand scb ON bp.sales_channel_brand_id = scb.id
  JOIN sales_channel sc ON scb.sales_channel_id = sc.id
  WHERE bp.id = ?

  -- Step 2: BrandMapping + Brand (내부 브랜드 목록)
  SELECT bm.id, bm.internal_brand_id, b.name_ko
  FROM brand_mapping bm
  JOIN brand b ON bm.internal_brand_id = b.id
  WHERE bm.preset_id = ?
    AND bm.status = 'ACTIVE'
  ```

---

## 3. POST /api/v1/market/brand-presets - 브랜드 프리셋 등록

### 호출 흐름 다이어그램

```
[Adapter-In]
BrandPresetCommandController.registerBrandPreset(RegisterBrandPresetApiRequest)
  ├─ BrandPresetCommandApiMapper.toCommand(request)            [Command 변환]
  └─ RegisterBrandPresetUseCase.execute(command)               [Port Interface]

[Application]
RegisterBrandPresetService.execute(RegisterBrandPresetCommand) [UseCase 구현]
  ├─ BrandPresetValidator.validateSameChannel(shopId, salesChannelBrandId) [검증 1]
  │   ├─ ShopReadManager.getById(ShopId.of(shopId))            → Shop domain
  │   └─ BrandPresetReadManager.findSalesChannelIdBySalesChannelBrandId()
  │       └─ BrandPresetQueryPort.findSalesChannelIdBySalesChannelBrandId()
  │           → shop.salesChannelId() == brandSalesChannelId 비교
  ├─ BrandPresetValidator.validateInternalBrandsExist(internalBrandIds) [검증 2]
  │   └─ BrandReadManager.findAllByIds(internalBrandIds)
  │       → 요청 수 != 조회 수 → BrandPresetInternalBrandNotFoundException
  ├─ BrandPresetCommandFactory.createRegisterBundle(command)   [Bundle 생성]
  │   ├─ BrandPreset.forNew(shopId, salesChannelBrandId, presetName, now)
  │   └─ RegisterBrandPresetBundle(brandPreset, salesChannelBrandId, internalBrandIds, now)
  └─ BrandPresetMappingFacade.registerWithMappings(bundle)     [@Transactional]
      ├─ BrandPresetCommandManager.persist(bundle.brandPreset())
      │   └─ BrandPresetCommandPort.persist()                  [Port]
      │       → presetId 반환
      └─ bundle.createMappings(presetId) → BrandMappingCommandManager.persistAll(mappings)
          └─ BrandMappingCommandPort.persistAll()              [Port]

[Adapter-Out - BrandPreset]
BrandPresetCommandAdapter                                       [Port 구현]
  ├─ BrandPresetJpaEntityMapper.toEntity(brandPreset)
  └─ BrandPresetJpaRepository.save(entity) → presetId 반환

[Adapter-Out - BrandMapping]
BrandMappingCommandAdapter                                      [Port 구현]
  ├─ BrandMappingJpaEntityMapper.toEntity(mapping) × N
  └─ BrandMappingJpaRepository.saveAll(entities)

[Database]
- INSERT INTO brand_preset
- INSERT INTO brand_mapping (N건 saveAll)
```

### Layer별 상세

#### Adapter-In Layer

- **Controller**: `BrandPresetCommandController`
  - Method: `registerBrandPreset(RegisterBrandPresetApiRequest)`
  - Response: `ApiResponse<BrandPresetIdApiResponse>`
  - HTTP Status: 201 Created
  - 권한: `@PreAuthorize("@access.superAdmin()")`, `brand-preset:write`

- **Request DTO**: `RegisterBrandPresetApiRequest`
  ```java
  record RegisterBrandPresetApiRequest(
    @NotNull Long shopId,
    @NotNull Long salesChannelBrandId,
    @NotBlank String presetName,
    @NotEmpty List<Long> internalBrandIds
  )
  ```

- **Response DTO**: `BrandPresetIdApiResponse`
  ```java
  record BrandPresetIdApiResponse(Long id, String createdAt)
  // 현재 BrandPresetIdApiResponse.of(brandPresetId, null) 로 호출 → createdAt 항상 null
  ```

- **ApiMapper**: `BrandPresetCommandApiMapper`
  - `toCommand(RegisterBrandPresetApiRequest)` → `RegisterBrandPresetCommand(shopId, salesChannelBrandId, presetName, internalBrandIds)`

#### Application Layer

- **UseCase Interface**: `RegisterBrandPresetUseCase`
  - `execute(RegisterBrandPresetCommand)` → `Long` (presetId)

- **Service 구현**: `RegisterBrandPresetService`
  1. `validator.validateSameChannel()`: Shop의 salesChannelId == SalesChannelBrand의 salesChannelId 검증
  2. `validator.validateInternalBrandsExist()`: 요청 internalBrandIds 전원 DB 존재 여부 확인
  3. `commandFactory.createRegisterBundle()`: `BrandPreset.forNew()` + `RegisterBrandPresetBundle` 생성
  4. `facade.registerWithMappings()`: 트랜잭션 내 저장

- **Command DTO**: `RegisterBrandPresetCommand`
  ```java
  record RegisterBrandPresetCommand(
    Long shopId, Long salesChannelBrandId, String presetName, List<Long> internalBrandIds
  )
  ```

- **Bundle**: `RegisterBrandPresetBundle`
  - `brandPreset`, `salesChannelBrandId`, `internalBrandIds`, `now(Instant)` 보관
  - `createMappings(presetId)`: persist 후 반환된 presetId로 `BrandMapping.forNew()` 목록 생성

- **Facade**: `BrandPresetMappingFacade`
  - `@Transactional`: BrandPreset 저장 + BrandMapping 벌크 저장 원자적 처리
  - `presetCommandManager.persist()` → presetId 반환 → `bundle.createMappings(presetId)` → `mappingCommandManager.persistAll()`

- **Validator**: `BrandPresetValidator`
  - `validateSameChannel()`: `ShopReadManager` + `BrandPresetReadManager.findSalesChannelIdBySalesChannelBrandId()` 조합
  - `validateInternalBrandsExist()`: `BrandReadManager.findAllByIds()` 조회 후 수량 비교

- **Factory**: `BrandPresetCommandFactory`
  - `TimeProvider.now()` 호출 (APP-TIM-001 규칙: Factory에서만 시간 생성)
  - `BrandPreset.forNew(shopId, salesChannelBrandId, presetName, now)` → 상태: `ACTIVE`

#### Domain Layer

- **Port**:
  - `BrandPresetCommandPort.persist(BrandPreset)` → `Long`
  - `BrandMappingCommandPort.persistAll(List<BrandMapping>)` → `List<Long>`

- **Aggregate**: `BrandPreset`
  - `forNew()`: id=BrandPresetId.forNew() (null), status=ACTIVE, createdAt=updatedAt=now

- **Aggregate**: `BrandMapping`
  - `forNew(presetId, salesChannelBrandId, internalBrandId, now)`: status=ACTIVE

#### Adapter-Out Layer

- **BrandPresetCommandAdapter**:
  - `BrandPresetJpaEntityMapper.toEntity()`: Domain → Entity 변환
  - `BrandPresetJpaRepository.save(entity)` → id 반환 (JPA identity 전략)

- **BrandMappingCommandAdapter**:
  - `BrandMappingJpaEntityMapper.toEntity()` × N
  - `BrandMappingJpaRepository.saveAll(entities)` → id 목록 반환

- **Database Query**:
  ```sql
  INSERT INTO brand_preset
    (shop_id, sales_channel_brand_id, preset_name, status, created_at, updated_at)
  VALUES (?, ?, ?, 'ACTIVE', ?, ?)

  INSERT INTO brand_mapping
    (preset_id, sales_channel_brand_id, internal_brand_id, status, created_at, updated_at)
  VALUES (?, ?, ?, 'ACTIVE', ?, ?)   -- N건 saveAll
  ```

---

## 4. PUT /api/v1/market/brand-presets/{brandPresetId} - 브랜드 프리셋 수정

### 호출 흐름 다이어그램

```
[Adapter-In]
BrandPresetCommandController.updateBrandPreset(brandPresetId, UpdateBrandPresetApiRequest)
  ├─ BrandPresetCommandApiMapper.toCommand(brandPresetId, request) [Command 변환]
  └─ UpdateBrandPresetUseCase.execute(command)                  [Port Interface]

[Application]
UpdateBrandPresetService.execute(UpdateBrandPresetCommand)      [UseCase 구현]
  ├─ BrandPresetValidator.findExistingOrThrow(BrandPresetId.of(brandPresetId)) [존재 확인]
  │   └─ BrandPresetReadManager.getById(id)
  │       └─ BrandPresetQueryPort.findById()                    [Port]
  │           → orElseThrow(BrandPresetNotFoundException)
  ├─ BrandPresetValidator.validateSameChannel(existing.shopId(), salesChannelBrandId) [채널 검증]
  ├─ BrandPresetValidator.validateInternalBrandsExist(internalBrandIds)  [브랜드 존재 검증]
  ├─ BrandPresetCommandFactory.createUpdateBundle(existing, command) [Bundle 생성]
  │   ├─ now = TimeProvider.now()
  │   ├─ createBrandMappings(presetId, salesChannelBrandId, internalBrandIds, now)
  │   │   └─ BrandMapping.forNew() × N
  │   └─ UpdateBrandPresetBundle(existing, presetName, salesChannelBrandId, brandMappings, now)
  └─ BrandPresetMappingFacade.updateWithMappings(bundle)        [@Transactional]
      ├─ brandPreset.update(presetName, salesChannelBrandId, now) [Domain 로직]
      ├─ BrandPresetCommandManager.persist(brandPreset)
      │   └─ BrandPresetCommandPort.persist()                   [Port]
      ├─ BrandMappingCommandManager.deleteAllByPresetId(presetId)
      │   └─ BrandMappingCommandPort.deleteAllByPresetId()      [Port]
      └─ BrandMappingCommandManager.persistAll(bundle.brandMappings())
          └─ BrandMappingCommandPort.persistAll()               [Port]

[Adapter-Out - BrandPreset]
BrandPresetCommandAdapter → BrandPresetJpaRepository.save(entity)  [UPDATE]

[Adapter-Out - BrandMapping]
BrandMappingCommandAdapter
  ├─ BrandMappingJpaRepository.deleteAllByPresetId(presetId)
  └─ BrandMappingJpaRepository.saveAll(newMappings)

[Database]
- UPDATE brand_preset SET preset_name=?, sales_channel_brand_id=?, updated_at=? WHERE id=?
- DELETE FROM brand_mapping WHERE preset_id=?
- INSERT INTO brand_mapping (N건)
```

### Layer별 상세

#### Adapter-In Layer

- **Controller**: `BrandPresetCommandController`
  - Method: `updateBrandPreset(Long brandPresetId, UpdateBrandPresetApiRequest)`
  - Response: `ResponseEntity<Void>` (204 No Content)
  - 권한: `@PreAuthorize("@access.superAdmin()")`, `brand-preset:write`

- **Request DTO**: `UpdateBrandPresetApiRequest`
  ```java
  record UpdateBrandPresetApiRequest(
    String presetName,              // 선택 (null 허용)
    Long salesChannelBrandId,       // 선택 (null 허용)
    List<Long> internalBrandIds     // 선택 (null 허용)
  )
  ```
  - 모든 필드 선택적 (Validation 어노테이션 없음) → partial update 패턴

- **ApiMapper**: `BrandPresetCommandApiMapper`
  - `toCommand(Long brandPresetId, UpdateBrandPresetApiRequest)` → `UpdateBrandPresetCommand`

#### Application Layer

- **UseCase Interface**: `UpdateBrandPresetUseCase`
  - `execute(UpdateBrandPresetCommand)` → `void`

- **Service 구현**: `UpdateBrandPresetService`
  1. `validator.findExistingOrThrow()`: 프리셋 존재 확인 + Domain 객체 반환
  2. `validator.validateSameChannel()`: 기존 프리셋의 shopId 기준으로 채널 일치 검증
  3. `validator.validateInternalBrandsExist()`: 새 internalBrandIds 존재 확인
  4. `commandFactory.createUpdateBundle()`: 수정 Bundle + 새 BrandMapping 목록 생성
  5. `facade.updateWithMappings()`: 트랜잭션 내 수정

- **Command DTO**: `UpdateBrandPresetCommand`
  ```java
  record UpdateBrandPresetCommand(
    Long brandPresetId, String presetName, Long salesChannelBrandId, List<Long> internalBrandIds
  )
  ```

- **Bundle**: `UpdateBrandPresetBundle`
  ```java
  record UpdateBrandPresetBundle(
    BrandPreset brandPreset,
    String presetName,
    Long salesChannelBrandId,
    List<BrandMapping> brandMappings,  // 새로 생성할 매핑 목록
    Instant now
  )
  ```

- **Facade**: `BrandPresetMappingFacade.updateWithMappings()`
  - `@Transactional`: 프리셋 수정 + 기존 매핑 전체 삭제 + 새 매핑 삽입 원자적 처리
  - `brandPreset.update(presetName, salesChannelBrandId, now)`: Domain 상태 변경
  - `deleteAllByPresetId()` → `persistAll()`: 매핑 전체 교체 방식

#### Domain Layer

- **Port**:
  - `BrandPresetQueryPort.findById(BrandPresetId)` → `Optional<BrandPreset>`
  - `BrandPresetCommandPort.persist(BrandPreset)` → `Long`
  - `BrandMappingCommandPort.deleteAllByPresetId(Long)`
  - `BrandMappingCommandPort.persistAll(List<BrandMapping>)` → `List<Long>`

- **Aggregate**: `BrandPreset.update(presetName, salesChannelBrandId, now)`
  - 가변 필드: `presetName`, `salesChannelBrandId`, `updatedAt` 변경
  - `id`, `shopId`, `createdAt`은 불변 유지

#### Adapter-Out Layer

- **BrandPresetCommandAdapter**: `BrandPresetJpaRepository.save()` (JPA Merge → UPDATE)
- **BrandMappingCommandAdapter**:
  - `BrandMappingJpaRepository.deleteAllByPresetId(presetId)`: 벌크 DELETE
  - `BrandMappingJpaRepository.saveAll(newEntities)`: 새 매핑 INSERT

- **Database Query**:
  ```sql
  -- 프리셋 수정
  UPDATE brand_preset
  SET preset_name = ?, sales_channel_brand_id = ?, updated_at = ?
  WHERE id = ?

  -- 기존 매핑 전체 삭제
  DELETE FROM brand_mapping WHERE preset_id = ?

  -- 새 매핑 삽입 (N건)
  INSERT INTO brand_mapping
    (preset_id, sales_channel_brand_id, internal_brand_id, status, created_at, updated_at)
  VALUES (?, ?, ?, 'ACTIVE', ?, ?)
  ```

---

## 5. DELETE /api/v1/market/brand-presets - 브랜드 프리셋 벌크 삭제

### 호출 흐름 다이어그램

```
[Adapter-In]
BrandPresetCommandController.deleteBrandPresets(DeleteBrandPresetsApiRequest)
  ├─ BrandPresetCommandApiMapper.toDeleteCommand(request.ids()) [Command 변환]
  └─ DeleteBrandPresetsUseCase.execute(command)                 [Port Interface]

[Application]
DeleteBrandPresetsService.execute(DeleteBrandPresetsCommand)   [UseCase 구현]
  ├─ BrandPresetCommandFactory.createDeactivateContext(command) [Context 생성]
  │   └─ StatusChangeContext<List<Long>>(ids, now)
  ├─ BrandPresetReadManager.findAllByIds(context.id())         [대상 조회]
  │   └─ BrandPresetQueryPort.findAllByIds()                   [Port]
  └─ BrandPresetMappingFacade.deactivateWithMappings(brandPresets, now) [@Transactional]
      ├─ brandPreset.deactivate(now) × N                        [Domain 로직]
      ├─ BrandPresetCommandManager.persistAll(brandPresets)
      │   └─ BrandPresetCommandPort.persistAll()               [Port]
      └─ BrandMappingCommandManager.deleteAllByPresetIds(presetIds)
          └─ BrandMappingCommandPort.deleteAllByPresetIds()    [Port]

[Adapter-Out - BrandPreset]
BrandPresetCommandAdapter → BrandPresetJpaRepository.saveAll(entities) [UPDATE status=INACTIVE]

[Adapter-Out - BrandMapping]
BrandMappingCommandAdapter → BrandMappingJpaRepository.deleteAllByPresetIdIn(presetIds)

[Database]
- UPDATE brand_preset SET status='INACTIVE', updated_at=? WHERE id IN (?)
- DELETE FROM brand_mapping WHERE preset_id IN (?)
```

### Layer별 상세

#### Adapter-In Layer

- **Controller**: `BrandPresetCommandController`
  - Method: `deleteBrandPresets(DeleteBrandPresetsApiRequest)`
  - Response: `ApiResponse<DeleteBrandPresetsApiResponse>`
  - HTTP Status: 200 OK
  - 권한: `@PreAuthorize("@access.superAdmin()")`, `brand-preset:write`

- **Request DTO**: `DeleteBrandPresetsApiRequest`
  ```java
  record DeleteBrandPresetsApiRequest(
    @NotEmpty List<Long> ids    // 삭제할 프리셋 ID 목록
  )
  ```

- **Response DTO**: `DeleteBrandPresetsApiResponse`
  ```java
  record DeleteBrandPresetsApiResponse(int deletedCount)
  ```

- **ApiMapper**: `BrandPresetCommandApiMapper`
  - `toDeleteCommand(List<Long> ids)` → `DeleteBrandPresetsCommand(ids)`

#### Application Layer

- **UseCase Interface**: `DeleteBrandPresetsUseCase`
  - `execute(DeleteBrandPresetsCommand)` → `int` (처리된 프리셋 수)

- **Service 구현**: `DeleteBrandPresetsService`
  1. `commandFactory.createDeactivateContext()`: `StatusChangeContext<List<Long>>(ids, now)` 생성
  2. `readManager.findAllByIds(ids)`: 실제 존재하는 프리셋 목록 조회 (존재하지 않는 ID는 조용히 무시)
  3. `facade.deactivateWithMappings()`: 트랜잭션 내 비활성화

- **Command DTO**: `DeleteBrandPresetsCommand`
  ```java
  record DeleteBrandPresetsCommand(List<Long> ids)
  ```

- **StatusChangeContext**: 공통 DTO
  ```java
  record StatusChangeContext<T>(T id, Instant changedAt)
  ```

- **Facade**: `BrandPresetMappingFacade.deactivateWithMappings()`
  - `@Transactional`: 프리셋 벌크 비활성화 + 매핑 벌크 삭제 원자적 처리
  - `brandPreset.deactivate(now)` × N: 각 Domain 객체 상태 변경 (status = INACTIVE)
  - `presetCommandManager.persistAll()`: 변경된 프리셋 일괄 저장
  - `mappingCommandManager.deleteAllByPresetIds()`: 관련 BrandMapping 일괄 삭제
  - 반환값: `brandPresets.size()` (실제 존재해서 처리된 건수)

#### Domain Layer

- **Port**:
  - `BrandPresetQueryPort.findAllByIds(List<Long>)` → `List<BrandPreset>`
  - `BrandPresetCommandPort.persistAll(List<BrandPreset>)` → `List<Long>`
  - `BrandMappingCommandPort.deleteAllByPresetIds(List<Long>)`

- **Aggregate**: `BrandPreset.deactivate(now)`
  - `status = BrandPresetStatus.INACTIVE`
  - `updatedAt = now`

#### Adapter-Out Layer

- **BrandPresetCommandAdapter**: `BrandPresetJpaRepository.saveAll(entities)` (JPA Merge → 벌크 UPDATE)
- **BrandMappingCommandAdapter**: `BrandMappingJpaRepository.deleteAllByPresetIdIn(presetIds)` (벌크 DELETE)

- **Database Query**:
  ```sql
  -- 프리셋 벌크 비활성화 (JPA saveAll → 개별 UPDATE)
  UPDATE brand_preset
  SET status = 'INACTIVE', updated_at = ?
  WHERE id = ?   -- saveAll은 개별 UPDATE로 실행됨

  -- BrandMapping 벌크 삭제
  DELETE FROM brand_mapping
  WHERE preset_id IN (?, ?, ...)
  ```

---

## 공통 패턴 분석

### 1. Hexagonal Architecture 레이어 격리

| 레이어 | DTO/모델 | 책임 |
|--------|----------|------|
| **Adapter-In** | ApiRequest / ApiResponse | HTTP 관심사 (Validation, 직렬화, ISO8601 변환) |
| **Application** | Command / Params / Result / Bundle | 유스케이스 조율, 트랜잭션 경계, 시간 생성 |
| **Domain** | BrandPreset / BrandPresetSearchCriteria / BrandPresetStatus | 비즈니스 규칙, 불변성 |
| **Adapter-Out** | BrandPresetJpaEntity / CompositeDto | 영속화 기술 관심사 (JPA, QueryDSL) |

### 2. CQRS 분리

- **Query Side**: `BrandPresetQueryController` → `BrandPresetQueryApiMapper` → QueryUseCase → QueryService → ReadManager
- **Command Side**: `BrandPresetCommandController` → `BrandPresetCommandApiMapper` → CommandUseCase → CommandService → Validator + Factory → Facade

### 3. 트랜잭션 경계

| 계층 | @Transactional | 비고 |
|------|----------------|------|
| Adapter-In | 없음 | HTTP 레이어 |
| Service | 없음 | 조율만 담당 |
| **Facade** | `@Transactional` | 쓰기 트랜잭션 경계 (등록/수정/삭제) |
| **Manager (Read)** | `@Transactional(readOnly = true)` | 조회 최적화 |
| **Manager (Command)** | `@Transactional` | Facade 하위 (PROPAGATION.REQUIRED) |
| Adapter-Out | 없음 | JPA 레이어 |

### 4. BrandPreset + BrandMapping 연동 패턴

BrandPreset은 단독으로 존재하지 않고 항상 BrandMapping과 함께 관리됩니다:

| 작업 | BrandPreset | BrandMapping |
|------|-------------|--------------|
| 등록 | INSERT | INSERT × N (saveAll) |
| 수정 | UPDATE | DELETE 전체 → INSERT × N (교체) |
| 삭제 | UPDATE status=INACTIVE | DELETE WHERE preset_id IN (...) |

### 5. Validator 패턴

- **검증 성공 시 Domain 객체 반환** (APP-VAL-001): `findExistingOrThrow()` → `BrandPreset` 반환
- **도메인 전용 예외 발생** (APP-VAL-002): `BrandPresetNotFoundException`, `BrandPresetChannelMismatchException`, `BrandPresetInternalBrandNotFoundException`
- **Cross-Domain 검증**: Validator가 ShopReadManager, BrandReadManager를 주입받아 도메인 경계를 넘어선 검증 수행

### 6. Factory 패턴

- `BrandPresetCommandFactory`: `TimeProvider.now()` 호출 (APP-TIM-001: Factory에서만 시간 생성)
- `BrandPresetQueryFactory`: Params → Criteria 변환, SortKey 파싱, 페이징 VO 생성

### 7. 상세 조회 2-Step 패턴

`getBrandPreset` 상세 조회는 단일 쿼리가 아닌 2-Step으로 처리합니다:
- Step 1: 프리셋 + Shop + SalesChannelBrand JOIN → 기본 메타데이터
- Step 2: BrandMapping + Brand JOIN → 내부 브랜드 목록
- 이는 M:N 관계(1 프리셋 : N 내부브랜드)의 Cartesian Product 방지 목적

### 8. 삭제 의미론

- **DELETE 엔드포인트지만 실제 동작은 비활성화(Soft Delete)**
- `BrandPreset.deactivate()`: status = INACTIVE (Hard Delete 아님)
- `BrandMapping`: Hard Delete (brand_mapping 레코드 완전 삭제)
- 응답 `deletedCount`: 요청 ID 중 실제 존재하여 처리된 건수 (미존재 ID 조용히 무시)

### 9. 변환 체인

```
[Query]
SearchBrandPresetsApiRequest
  → BrandPresetQueryApiMapper.toSearchParams()
  → BrandPresetSearchParams
  → BrandPresetQueryFactory.createCriteria()
  → BrandPresetSearchCriteria
  → QueryDSL → BrandPresetCompositeDto
  → BrandPresetJpaEntityMapper.toResult()
  → BrandPresetResult
  → BrandPresetQueryApiMapper.toResponse()
  → BrandPresetApiResponse

[Command]
RegisterBrandPresetApiRequest
  → BrandPresetCommandApiMapper.toCommand()
  → RegisterBrandPresetCommand
  → BrandPresetCommandFactory.createRegisterBundle()
  → RegisterBrandPresetBundle (BrandPreset + BrandMapping 생성 재료)
  → BrandPresetJpaEntityMapper.toEntity()
  → BrandPresetJpaEntity → DB
```

---

## 주요 설계 결정

### 장점

1. **레이어 격리**: ApiRequest/Command/Criteria/Entity 각 레이어 전용 DTO 사용으로 변경 격리
2. **원자적 트랜잭션**: Facade가 BrandPreset + BrandMapping을 단일 트랜잭션으로 처리
3. **검증 명확화**: Validator에 Cross-Domain 검증 집중 (채널 일치, 브랜드 존재 확인)
4. **조회 최적화**: BrandPreset 목록 조회 시 필요 데이터를 4-way JOIN으로 단일 쿼리 처리
5. **시간 일관성**: Factory에서 단일 `TimeProvider.now()` 호출로 트랜잭션 내 시간 일관성 보장

### 트레이드오프

1. **상세 조회 2-Step**: N+1 방지보다 쿼리 분리를 선택 (BrandMapping 관계 복잡도 이유)
2. **매핑 전체 교체**: 수정 시 기존 매핑 DELETE 후 INSERT 방식 → 변경 이력 추적 불가
3. **Soft Delete 불일치**: BrandPreset은 비활성화(INACTIVE), BrandMapping은 하드 삭제로 처리 방식 상이
4. **createdAt null 반환**: `registerBrandPreset` 응답의 `createdAt` 필드 미구현 (항상 null)

---

## 문서 생성 정보

- **분석 일시**: 2026-02-18
- **대상 모듈**: `adapter-in/rest-api`, `application`, `domain`, `adapter-out/persistence-mysql`
- **엔드포인트 Base**: `/api/v1/market/brand-presets`
- **관련 도메인**: brandpreset, brandmapping
