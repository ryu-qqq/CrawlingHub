# CategoryPreset Domain API Flow Analysis

카테고리 프리셋 도메인의 전체 API 호출 흐름 분석 문서입니다.

---

## 엔드포인트 목록

| HTTP Method | Path | 설명 | Controller Method |
|-------------|------|------|-------------------|
| GET | `/api/v1/market/category-presets` | 카테고리 프리셋 목록 조회 | `searchCategoryPresets()` |
| GET | `/api/v1/market/category-presets/{categoryPresetId}` | 카테고리 프리셋 상세 조회 | `getCategoryPreset()` |
| POST | `/api/v1/market/category-presets` | 카테고리 프리셋 등록 | `registerCategoryPreset()` |
| PUT | `/api/v1/market/category-presets/{categoryPresetId}` | 카테고리 프리셋 수정 | `updateCategoryPreset()` |
| DELETE | `/api/v1/market/category-presets` | 카테고리 프리셋 벌크 삭제 | `deleteCategoryPresets()` |

---

## 1. GET /api/v1/market/category-presets - 카테고리 프리셋 목록 조회

### 호출 흐름 다이어그램

```
[Adapter-In]
CategoryPresetQueryController.searchCategoryPresets(request)
  ├─ CategoryPresetQueryApiMapper.toSearchParams(request)       [Params 변환]
  │   - SearchCategoryPresetsApiRequest → CategoryPresetSearchParams
  │   - 날짜 파싱: "yyyy-MM-dd" → LocalDate
  │   - 기본값 설정: page=0, size=20
  ├─ SearchCategoryPresetByOffsetUseCase.execute(params)        [Port Interface]
  └─ CategoryPresetQueryApiMapper.toPageResponse(pageResult)    [Response 변환]
      - CategoryPresetPageResult → PageApiResponse<CategoryPresetApiResponse>
      - Instant → ISO-8601 문자열 변환 (DateTimeFormatUtils)

[Application]
SearchCategoryPresetByOffsetService.execute(params)             [UseCase 구현]
  ├─ CategoryPresetQueryFactory.createCriteria(params)          [Criteria 생성]
  │   - CategoryPresetSearchParams → CategoryPresetSearchCriteria
  │   - SortKey, SortDirection, PageRequest, QueryContext 생성
  ├─ CategoryPresetReadManager.findByCriteria(criteria)         [목록 조회]
  │   └─ CategoryPresetQueryPort.findByCriteria()               [Port]
  ├─ CategoryPresetReadManager.countByCriteria(criteria)        [전체 건수]
  │   └─ CategoryPresetQueryPort.countByCriteria()              [Port]
  └─ CategoryPresetAssembler.toPageResult()                     [Result 조합]
      - CategoryPresetPageResult.of(results, page, size, totalElements)

[Adapter-Out]
CategoryPresetQueryAdapter                                       [Port 구현]
  └─ CategoryPresetQueryDslRepository
      ├─ findByCriteria(): compositeQuery() + WHERE + ORDER BY + LIMIT/OFFSET
      │   - JOIN: category_preset ← shop ← sales_channel_category ← sales_channel
      │   - 결과: CategoryPresetCompositeDto (QueryDSL Projections)
      └─ countByCriteria(): SELECT COUNT(*) + WHERE

[Database]
- category_preset (기본 조회)
- shop (shopName, accountId, salesChannelId)
- sales_channel_category (externalCategoryCode, displayPath)
- sales_channel (channelName)
```

### Layer별 상세

#### Adapter-In Layer
- **Controller**: `CategoryPresetQueryController`
  - Method: `searchCategoryPresets(SearchCategoryPresetsApiRequest)`
  - 권한: `@PreAuthorize("@access.authenticated()")` + `@RequirePermission("category-preset:read")`
  - Response: `ApiResponse<PageApiResponse<CategoryPresetApiResponse>>`
  - HTTP Status: 200 OK

- **Request DTO**: `SearchCategoryPresetsApiRequest`
  ```java
  record SearchCategoryPresetsApiRequest(
    List<Long> salesChannelIds,   // 판매채널 ID 목록 필터
    List<String> statuses,         // ACTIVE, INACTIVE
    String searchField,            // PRESET_NAME, SHOP_NAME, ACCOUNT_ID, CATEGORY_CODE, CATEGORY_PATH
    String searchWord,             // 검색어
    String startDate,              // YYYY-MM-DD
    String endDate,                // YYYY-MM-DD
    String sortKey,                // createdAt
    String sortDirection,          // ASC, DESC
    Integer page,                  // 기본값: 0
    Integer size                   // 기본값: 20
  )
  ```

- **Response DTO**: `CategoryPresetApiResponse`
  ```java
  record CategoryPresetApiResponse(
    Long id,
    Long shopId,
    String shopName,
    Long salesChannelId,
    String salesChannelName,
    String accountId,
    String presetName,
    String categoryPath,
    String categoryCode,
    String createdAt              // ISO-8601
  )
  ```

- **ApiMapper**: `CategoryPresetQueryApiMapper`
  - `toSearchParams()`: `CommonSearchParams.of(...)` + `CategoryPresetSearchParams.of(...)` 조합
  - `toPageResponse()`: `toResponses()` + `PageApiResponse.of(responses, page, size, total)` 조합

#### Application Layer
- **UseCase Interface**: `SearchCategoryPresetByOffsetUseCase`
  - `execute(CategoryPresetSearchParams)` → `CategoryPresetPageResult`

- **Service 구현**: `SearchCategoryPresetByOffsetService`
  - QueryFactory로 Params → Criteria 변환
  - ReadManager 통해 목록 조회 + 카운트
  - Assembler로 PageResult 생성

- **Params DTO**: `CategoryPresetSearchParams`
  - `salesChannelIds`, `statuses`, `searchField`, `searchWord`, `commonSearchParams`

- **Result DTO**: `CategoryPresetPageResult`
  - `results: List<CategoryPresetResult>`, `pageMeta` (page, size, totalElements)

- **QueryFactory**: `CategoryPresetQueryFactory`
  - `createCriteria()`: SortKey 파싱 → `CategoryPresetSortKey`, SortDirection, PageRequest 생성 → `QueryContext` 조합
  - `CategoryPresetSortKey.defaultKey()` = `CREATED_AT`

- **ReadManager**: `CategoryPresetReadManager`
  - `@Transactional(readOnly = true)` 적용
  - Port 호출 + `CategoryPresetNotFoundException` 처리

#### Domain Layer
- **Port**: `CategoryPresetQueryPort`
  - `findByCriteria(CategoryPresetSearchCriteria)` → `List<CategoryPresetResult>`
  - `countByCriteria(CategoryPresetSearchCriteria)` → `long`

- **Criteria**: `CategoryPresetSearchCriteria`
  - `salesChannelIds`, `statuses`, `searchField`, `searchWord`, `startDate`, `endDate`
  - `queryContext: QueryContext<CategoryPresetSortKey>`

#### Adapter-Out Layer
- **Adapter**: `CategoryPresetQueryAdapter`
  - `findByCriteria()`: `repository.findByCriteria()` → `CategoryPresetCompositeDto` → `mapper.toResult()` → `CategoryPresetResult`
  - `countByCriteria()`: `repository.countByCriteria()` 직접 위임

- **Repository**: `CategoryPresetQueryDslRepository`
  - `compositeQuery()`: 4개 테이블 JOIN (category_preset, shop, sales_channel_category, sales_channel)
  - 동적 WHERE 조건: `CategoryPresetConditionBuilder` 활용

- **ConditionBuilder**: `CategoryPresetConditionBuilder`
  - `salesChannelIdsIn()`: `shop.salesChannelId IN (?)`
  - `statusesIn()`: `category_preset.status IN (?)`
  - `searchCondition()`: switch문으로 5개 검색 필드 분기 (LIKE 검색)
  - `createdAtGoe()`, `createdAtLoe()`: Asia/Seoul 타임존 기준 날짜 변환

- **Database Query**:
  ```sql
  SELECT
    cp.id, cp.shop_id, s.shop_name, s.account_id, s.sales_channel_id,
    sc.channel_name, cp.sales_channel_category_id,
    scc.external_category_code, scc.display_path, cp.preset_name, cp.status, cp.created_at
  FROM category_preset cp
  JOIN shop s ON cp.shop_id = s.id
  JOIN sales_channel_category scc ON cp.sales_channel_category_id = scc.id
  JOIN sales_channel sc ON s.sales_channel_id = sc.id
  WHERE
    s.sales_channel_id IN (?)          -- salesChannelIds 필터 (선택)
    AND cp.status IN (?)               -- statuses 필터 (선택)
    AND cp.preset_name ILIKE ?         -- searchField=PRESET_NAME 예시 (선택)
    AND cp.created_at >= ?             -- startDate 필터 (선택)
    AND cp.created_at <= ?             -- endDate 필터 (선택)
  ORDER BY cp.created_at DESC
  LIMIT ? OFFSET ?

  -- COUNT 쿼리
  SELECT COUNT(cp.id)
  FROM category_preset cp
  JOIN shop s ON cp.shop_id = s.id
  JOIN sales_channel_category scc ON cp.sales_channel_category_id = scc.id
  WHERE ...동일 조건...
  ```

---

## 2. GET /api/v1/market/category-presets/{categoryPresetId} - 카테고리 프리셋 상세 조회

### 호출 흐름 다이어그램

```
[Adapter-In]
CategoryPresetQueryController.getCategoryPreset(categoryPresetId)
  ├─ GetCategoryPresetDetailUseCase.execute(categoryPresetId)    [Port Interface]
  └─ CategoryPresetQueryApiMapper.toDetailResponse(result)       [Response 변환]
      - MappingCategoryResponse, InternalCategoryResponse nested 변환
      - Instant → ISO-8601 문자열 변환

[Application]
GetCategoryPresetDetailService.execute(categoryPresetId)        [UseCase 구현]
  └─ CategoryPresetCompositionReadManager.getDetail(id)
      └─ CategoryPresetCompositionQueryPort.findDetailById()     [Port]

[Adapter-Out]
CategoryPresetCompositionQueryAdapter                            [Port 구현]
  ├─ CategoryPresetQueryDslRepository.findDetailCompositeById()
  │   - JOIN: category_preset + shop + sales_channel_category + sales_channel
  │   - 결과: CategoryPresetDetailCompositeDto
  └─ CategoryMappingQueryDslRepository.findMappedCategoriesByPresetId()
      - JOIN: category_mapping + category
      - 결과: List<CategoryMappingWithCategoryDto>

[Database]
- category_preset (프리셋 기본정보)
- shop (shopName, accountId, salesChannelId)
- sales_channel_category (externalCategoryCode, displayPath)
- sales_channel (channelName)
- category_mapping (매핑 테이블)
- category (nameKo, displayPath, code)
```

### Layer별 상세

#### Adapter-In Layer
- **Controller**: `CategoryPresetQueryController`
  - Method: `getCategoryPreset(@PathVariable Long categoryPresetId)`
  - 권한: `@PreAuthorize("@access.authenticated()")` + `@RequirePermission("category-preset:read")`
  - Response: `ApiResponse<CategoryPresetDetailApiResponse>`
  - HTTP Status: 200 OK

- **Response DTO**: `CategoryPresetDetailApiResponse`
  ```java
  record CategoryPresetDetailApiResponse(
    Long id,
    Long shopId,
    String shopName,
    Long salesChannelId,
    String salesChannelName,
    String accountId,
    String presetName,
    MappingCategoryResponse mappingCategory,         // 중첩 DTO
    List<InternalCategoryResponse> internalCategories, // 중첩 DTO 목록
    String createdAt,                                // ISO-8601
    String updatedAt                                 // ISO-8601
  )

  record MappingCategoryResponse(String categoryCode, String categoryPath)
  record InternalCategoryResponse(Long id, String categoryPath)
  ```

- **ApiMapper**: `CategoryPresetQueryApiMapper`
  - `toDetailResponse()`: `MappingCategoryResponse` + `InternalCategoryResponse` 목록 생성 → `CategoryPresetDetailApiResponse` 조합

#### Application Layer
- **UseCase Interface**: `GetCategoryPresetDetailUseCase`
  - `execute(Long categoryPresetId)` → `CategoryPresetDetailResult`

- **Service 구현**: `GetCategoryPresetDetailService`
  - `CategoryPresetCompositionReadManager.getDetail()` 단순 위임

- **Manager**: `CategoryPresetCompositionReadManager`
  - `@Transactional(readOnly = true)` 적용
  - Port 호출 후 Empty이면 `CategoryPresetNotFoundException` throw

- **Result DTO**: `CategoryPresetDetailResult`
  ```java
  record CategoryPresetDetailResult(
    Long id, Long shopId, String shopName,
    Long salesChannelId, String salesChannelName, String accountId,
    String presetName,
    MappingCategory mappingCategory,
    List<InternalCategory> internalCategories,
    Instant createdAt, Instant updatedAt
  )
  record MappingCategory(String categoryCode, String categoryPath)
  record InternalCategory(Long id, String categoryPath)
  ```

#### Domain Layer
- **Port**: `CategoryPresetCompositionQueryPort`
  - `findDetailById(Long)` → `Optional<CategoryPresetDetailResult>`
  - 크로스 도메인 조인 (category_preset + category_mapping + category) 처리

#### Adapter-Out Layer
- **Adapter**: `CategoryPresetCompositionQueryAdapter`
  - `findDetailById()`: 2단계 조회
    1. `presetRepository.findDetailCompositeById()` → 프리셋 + Shop + SalesChannel 정보
    2. `categoryMappingRepository.findMappedCategoriesByPresetId()` → 내부 카테고리 매핑 목록
  - 두 결과를 조합하여 `CategoryPresetDetailResult` 생성

- **Database Query**:
  ```sql
  -- 1단계: 프리셋 상세 + JOIN
  SELECT
    cp.id, cp.shop_id, s.shop_name, s.account_id, s.sales_channel_id,
    sc.channel_name, cp.sales_channel_category_id,
    scc.external_category_code, scc.display_path, cp.preset_name, cp.status,
    cp.created_at, cp.updated_at
  FROM category_preset cp
  JOIN shop s ON cp.shop_id = s.id
  JOIN sales_channel_category scc ON cp.sales_channel_category_id = scc.id
  JOIN sales_channel sc ON s.sales_channel_id = sc.id
  WHERE cp.id = ?

  -- 2단계: 내부 카테고리 매핑 목록
  SELECT
    cm.id, cm.internal_category_id, c.name_ko, c.display_path, c.code
  FROM category_mapping cm
  JOIN category c ON cm.internal_category_id = c.id
  WHERE cm.preset_id = ? AND cm.status = 'ACTIVE'
  ```

---

## 3. POST /api/v1/market/category-presets - 카테고리 프리셋 등록

### 호출 흐름 다이어그램

```
[Adapter-In]
CategoryPresetCommandController.registerCategoryPreset(request)
  ├─ CategoryPresetCommandApiMapper.toRegisterCommand(request)  [Command 변환]
  │   - RegisterCategoryPresetApiRequest → RegisterCategoryPresetCommand
  └─ RegisterCategoryPresetUseCase.execute(command)             [Port Interface]
      → CategoryPresetIdApiResponse.of(id, null) 응답 구성

[Application]
RegisterCategoryPresetService.execute(command)                  [UseCase 구현]
  ├─ CategoryPresetValidator.resolveSalesChannelCategoryId()    [채널 카테고리 검증]
  │   ├─ ShopReadManager.getById(shopId)                       → Shop 조회
  │   ├─ CategoryPresetReadManager.findSalesChannelCategoryIdByCode()
  │   │   └─ CategoryPresetQueryPort.findSalesChannelCategoryIdByCode()
  │   └─ validateSameChannel(shop, salesChannelCategoryId)      → 채널 일치 검증
  │       └─ SalesChannelCategoryReadManager.getById()
  ├─ CategoryPresetValidator.validateInternalCategoriesExist()  [내부 카테고리 검증]
  │   └─ CategoryReadManager.findAllByIds()                    → 존재 여부 확인
  ├─ CategoryPresetCommandFactory.createRegisterBundle()        [Bundle 생성]
  │   ├─ TimeProvider.now()                                    → 현재 시간
  │   └─ CategoryPreset.forNew(shopId, salesChannelCategoryId, presetName, now)
  └─ CategoryPresetMappingFacade.registerWithMappings(bundle)   [트랜잭션]
      ├─ CategoryPresetCommandManager.persist(preset)
      │   └─ CategoryPresetCommandPort.persist()               [Port]
      └─ CategoryMappingCommandManager.persistAll(mappings)
          └─ CategoryMappingCommandPort.persistAll()           [Port]

[Adapter-Out]
CategoryPresetCommandAdapter                                     [Port 구현]
  ├─ CategoryPresetJpaEntityMapper.toEntity(categoryPreset)
  └─ CategoryPresetJpaRepository.save(entity)

CategoryMappingCommandAdapter                                    [Port 구현]
  ├─ CategoryMappingJpaEntityMapper.toEntity(categoryMapping)
  └─ CategoryMappingJpaRepository.saveAll(entities)

[Database]
- INSERT INTO category_preset
- INSERT INTO category_mapping (벌크)
```

### Layer별 상세

#### Adapter-In Layer
- **Controller**: `CategoryPresetCommandController`
  - Method: `registerCategoryPreset(@Valid @RequestBody RegisterCategoryPresetApiRequest)`
  - 권한: `@PreAuthorize("@access.superAdmin()")` + `@RequirePermission("category-preset:write")`
  - Response: `ApiResponse<CategoryPresetIdApiResponse>`
  - HTTP Status: 201 Created
  - 주의: `CategoryPresetIdApiResponse.of(categoryPresetId, null)` - createdAt은 항상 null 반환

- **Request DTO**: `RegisterCategoryPresetApiRequest`
  ```java
  record RegisterCategoryPresetApiRequest(
    @NotNull Long shopId,
    @NotBlank String presetName,
    @NotBlank String categoryCode,
    @NotEmpty List<Long> internalCategoryIds
  )
  ```

- **Response DTO**: `CategoryPresetIdApiResponse`
  ```java
  record CategoryPresetIdApiResponse(Long id, String createdAt)
  ```

- **ApiMapper**: `CategoryPresetCommandApiMapper`
  - `toRegisterCommand()`: 4개 필드 1:1 변환

#### Application Layer
- **UseCase Interface**: `RegisterCategoryPresetUseCase`
  - `execute(RegisterCategoryPresetCommand)` → `Long` (categoryPresetId)

- **Service 구현**: `RegisterCategoryPresetService`
  1. Validator로 salesChannelCategoryId 해소 및 채널 일치 검증
  2. Validator로 내부 카테고리 존재 검증
  3. Factory로 `RegisterCategoryPresetBundle` 생성
  4. Facade로 원자적 저장

- **Command DTO**: `RegisterCategoryPresetCommand`
  - `shopId`, `presetName`, `categoryCode`, `internalCategoryIds`

- **Bundle**: `RegisterCategoryPresetBundle`
  - `categoryPreset: CategoryPreset`, `salesChannelCategoryId`, `internalCategoryIds`, `now`
  - `createMappings(presetId)`: persist 후 반환된 ID로 `CategoryMapping` 목록 생성

- **Validator**: `CategoryPresetValidator`
  - `resolveSalesChannelCategoryId(shopId, categoryCode)`:
    1. `ShopReadManager.getById()` → `Shop` 조회 (salesChannelId 획득)
    2. `CategoryPresetReadManager.findSalesChannelCategoryIdByCode()` → 카테고리 코드로 ID 조회
    3. `validateSameChannel()` → Shop.salesChannelId == SalesChannelCategory.salesChannelId 검증
  - `validateInternalCategoriesExist(ids)`: 요청 ID 수 vs 실제 조회 수 비교

- **Factory**: `CategoryPresetCommandFactory`
  - `createRegisterBundle()`: `TimeProvider.now()` 호출 → `CategoryPreset.forNew()` → Bundle 생성
  - `TimeProvider.now()`는 Factory에서만 호출 (APP-TIM-001 규칙)

- **Facade**: `CategoryPresetMappingFacade`
  - `@Transactional`: CategoryPreset 저장 + CategoryMapping 벌크 저장 원자적 처리
  - `registerWithMappings()`: `persist(preset)` → presetId로 `bundle.createMappings()` → `persistAll(mappings)`

#### Domain Layer
- **Port**:
  - `CategoryPresetCommandPort.persist(CategoryPreset)` → `Long`
  - `CategoryMappingCommandPort.persistAll(List<CategoryMapping>)` → `List<Long>`

- **Aggregate**: `CategoryPreset`
  - `forNew()`: `id = CategoryPresetId.forNew()`, `status = ACTIVE`
  - 불변 ID, 가변 presetName/salesChannelCategoryId/status

#### Adapter-Out Layer
- **Adapter**: `CategoryPresetCommandAdapter`
  - `persist()`: `mapper.toEntity()` → `repository.save()` → `saved.getId()`

- **Adapter**: `CategoryMappingCommandAdapter`
  - `persistAll()`: `mapper.toEntity()` per item → `repository.saveAll()`

- **Entity**: `CategoryPresetJpaEntity` (`@Table(name = "category_preset")`)
  - 필드: `id (AUTO_INCREMENT)`, `shop_id`, `sales_channel_category_id`, `preset_name`, `status`
  - 상속: `BaseAuditEntity` (createdAt, updatedAt as Instant)

- **Database Query**:
  ```sql
  INSERT INTO category_preset (shop_id, sales_channel_category_id, preset_name, status, created_at, updated_at)
  VALUES (?, ?, ?, 'ACTIVE', ?, ?)

  INSERT INTO category_mapping (preset_id, sales_channel_category_id, internal_category_id, status, created_at, updated_at)
  VALUES (?, ?, ?, ...), (?, ?, ?, ...), ...   -- 벌크 INSERT
  ```

---

## 4. PUT /api/v1/market/category-presets/{categoryPresetId} - 카테고리 프리셋 수정

### 호출 흐름 다이어그램

```
[Adapter-In]
CategoryPresetCommandController.updateCategoryPreset(categoryPresetId, request)
  ├─ CategoryPresetCommandApiMapper.toUpdateCommand(categoryPresetId, request)  [Command 변환]
  │   - (presetId, UpdateCategoryPresetApiRequest) → UpdateCategoryPresetCommand
  └─ UpdateCategoryPresetUseCase.execute(command)                               [Port Interface]

[Application]
UpdateCategoryPresetService.execute(command)                    [UseCase 구현]
  ├─ CategoryPresetValidator.findExistingOrThrow(categoryPresetId)
  │   └─ CategoryPresetReadManager.getById()
  │       └─ CategoryPresetQueryPort.findById()                [Port]
  ├─ [categoryCode 변경 시] CategoryPresetValidator.resolveSalesChannelCategoryId()
  │   └─ (등록과 동일한 검증 플로우)
  ├─ CategoryPresetValidator.validateInternalCategoriesExist()
  ├─ CategoryPresetCommandFactory.createUpdateBundle()          [Bundle 생성]
  │   ├─ TimeProvider.now()
  │   └─ CategoryMapping.forNew() × N 개
  └─ CategoryPresetMappingFacade.updateWithMappings(bundle)     [트랜잭션]
      ├─ categoryPreset.update(presetName, salesChannelCategoryId, now)  [Domain 로직]
      ├─ CategoryPresetCommandManager.persist(preset)
      │   └─ CategoryPresetCommandPort.persist()               [Port]
      ├─ CategoryMappingCommandManager.deleteAllByPresetId()   [기존 매핑 전체 삭제]
      │   └─ CategoryMappingCommandPort.deleteAllByPresetId()  [Port]
      └─ CategoryMappingCommandManager.persistAll(newMappings) [신규 매핑 저장]
          └─ CategoryMappingCommandPort.persistAll()           [Port]

[Adapter-Out]
CategoryPresetCommandAdapter → CategoryPresetJpaRepository.save()
CategoryMappingCommandAdapter
  ├─ CategoryMappingJpaRepository.deleteAllByPresetId()        [DELETE]
  └─ CategoryMappingJpaRepository.saveAll()                    [INSERT]

[Database]
- UPDATE category_preset
- DELETE FROM category_mapping WHERE preset_id = ?
- INSERT INTO category_mapping (벌크)
```

### Layer별 상세

#### Adapter-In Layer
- **Controller**: `CategoryPresetCommandController`
  - Method: `updateCategoryPreset(@PathVariable Long categoryPresetId, @Valid @RequestBody UpdateCategoryPresetApiRequest)`
  - 권한: `@PreAuthorize("@access.superAdmin()")` + `@RequirePermission("category-preset:write")`
  - Response: `ResponseEntity<Void>`
  - HTTP Status: 204 No Content

- **Request DTO**: `UpdateCategoryPresetApiRequest`
  ```java
  record UpdateCategoryPresetApiRequest(
    String presetName,             // 선택 (null 허용)
    String categoryCode,           // 선택 (null 허용)
    List<Long> internalCategoryIds // 선택 (null 허용)
  )
  ```
  - 모든 필드가 Optional - Partial Update 지원

- **ApiMapper**: `CategoryPresetCommandApiMapper`
  - `toUpdateCommand(presetId, request)`: `presetId` + 3개 필드 변환 → `UpdateCategoryPresetCommand`

#### Application Layer
- **UseCase Interface**: `UpdateCategoryPresetUseCase`
  - `execute(UpdateCategoryPresetCommand)` → `void`

- **Service 구현**: `UpdateCategoryPresetService`
  1. 기존 프리셋 존재 확인 + Domain 객체 획득
  2. `categoryCode` 변경 시: 새 `salesChannelCategoryId` 해소 (채널 일치 검증 포함)
  3. `categoryCode` 미변경 시: 기존 `existing.salesChannelCategoryId()` 유지
  4. 내부 카테고리 존재 검증
  5. Bundle 생성 → Facade 위임

- **Command DTO**: `UpdateCategoryPresetCommand`
  - `categoryPresetId`, `presetName`, `categoryCode`, `internalCategoryIds`

- **Bundle**: `UpdateCategoryPresetBundle`
  - `categoryPreset: CategoryPreset` (기존 Domain 객체), `presetName`, `salesChannelCategoryId`, `categoryMappings`, `now`

- **Facade**: `CategoryPresetMappingFacade`
  - `updateWithMappings()`:
    1. `categoryPreset.update(presetName, salesChannelCategoryId, now)` - Domain 상태 변경
    2. `persist(categoryPreset)` - 변경된 Aggregate 저장
    3. `deleteAllByPresetId()` - 기존 매핑 전체 삭제 (hard delete)
    4. `persistAll(newMappings)` - 새 매핑 벌크 저장
  - `@Transactional`: 위 4단계 원자적 처리

#### Domain Layer
- **Aggregate**: `CategoryPreset`
  - `update(String presetName, Long salesChannelCategoryId, Instant now)`: 3개 필드 변경

- **Port**:
  - `CategoryPresetCommandPort.persist(CategoryPreset)` → `Long`
  - `CategoryMappingCommandPort.deleteAllByPresetId(Long)`
  - `CategoryMappingCommandPort.persistAll(List<CategoryMapping>)`

#### Adapter-Out Layer
- **Database Query**:
  ```sql
  -- 1. 프리셋 수정
  UPDATE category_preset
  SET preset_name = ?, sales_channel_category_id = ?, updated_at = ?
  WHERE id = ?

  -- 2. 기존 매핑 전체 삭제 (hard delete)
  DELETE FROM category_mapping WHERE preset_id = ?

  -- 3. 새 매핑 벌크 INSERT
  INSERT INTO category_mapping (preset_id, sales_channel_category_id, internal_category_id, status, created_at, updated_at)
  VALUES (?, ?, ?, ...), ...
  ```

---

## 5. DELETE /api/v1/market/category-presets - 카테고리 프리셋 벌크 삭제

### 호출 흐름 다이어그램

```
[Adapter-In]
CategoryPresetCommandController.deleteCategoryPresets(request)
  ├─ CategoryPresetCommandApiMapper.toDeleteCommand(request)    [Command 변환]
  │   - DeleteCategoryPresetsApiRequest → DeleteCategoryPresetsCommand
  └─ DeleteCategoryPresetsUseCase.execute(command)              [Port Interface]
      → DeleteCategoryPresetsApiResponse.of(deletedCount) 응답 구성

[Application]
DeleteCategoryPresetsService.execute(command)                   [UseCase 구현]
  ├─ CategoryPresetCommandFactory.createDeactivateContext(command)
  │   - DeleteCategoryPresetsCommand → StatusChangeContext<List<Long>>
  │   - TimeProvider.now() 호출
  ├─ CategoryPresetReadManager.findAllByIds(ids)
  │   └─ CategoryPresetQueryPort.findAllByIds()                 [Port]
  └─ CategoryPresetMappingFacade.deactivateWithMappings(presets, now)  [트랜잭션]
      ├─ categoryPreset.deactivate(now)  × N                   [Domain 로직]
      ├─ CategoryPresetCommandManager.persistAll(presets)
      │   └─ CategoryPresetCommandPort.persistAll()             [Port]
      └─ CategoryMappingCommandManager.deleteAllByPresetIds(presetIds)
          └─ CategoryMappingCommandPort.deleteAllByPresetIds()  [Port]

[Adapter-Out]
CategoryPresetCommandAdapter → CategoryPresetJpaRepository.saveAll()
CategoryMappingCommandAdapter → CategoryMappingJpaRepository.deleteAllByPresetIdIn()

[Database]
- UPDATE category_preset SET status = 'INACTIVE', updated_at = ? WHERE id IN (?)
- DELETE FROM category_mapping WHERE preset_id IN (?)
```

### Layer별 상세

#### Adapter-In Layer
- **Controller**: `CategoryPresetCommandController`
  - Method: `deleteCategoryPresets(@Valid @RequestBody DeleteCategoryPresetsApiRequest)`
  - 권한: `@PreAuthorize("@access.superAdmin()")` + `@RequirePermission("category-preset:write")`
  - Response: `ApiResponse<DeleteCategoryPresetsApiResponse>`
  - HTTP Status: 200 OK
  - 특이사항: DELETE 메서드임에도 `@RequestBody` 사용 (벌크 삭제 패턴)

- **Request DTO**: `DeleteCategoryPresetsApiRequest`
  ```java
  record DeleteCategoryPresetsApiRequest(@NotEmpty List<Long> ids)
  ```

- **Response DTO**: `DeleteCategoryPresetsApiResponse`
  ```java
  record DeleteCategoryPresetsApiResponse(int deletedCount)
  ```

- **ApiMapper**: `CategoryPresetCommandApiMapper`
  - `toDeleteCommand()`: `request.ids()` → `DeleteCategoryPresetsCommand`

#### Application Layer
- **UseCase Interface**: `DeleteCategoryPresetsUseCase`
  - `execute(DeleteCategoryPresetsCommand)` → `int` (실제 삭제된 프리셋 수)

- **Service 구현**: `DeleteCategoryPresetsService`
  1. Factory로 `StatusChangeContext` 생성 (ids + now)
  2. ReadManager로 실제 존재하는 프리셋 조회 (ACTIVE만)
  3. Facade로 비활성화 + 매핑 삭제

- **Command DTO**: `DeleteCategoryPresetsCommand`
  - `ids: List<Long>`

- **StatusChangeContext**: `StatusChangeContext<List<Long>>`
  - `id: List<Long>`, `changedAt: Instant`

- **Facade**: `CategoryPresetMappingFacade`
  - `deactivateWithMappings()`:
    1. 각 `CategoryPreset.deactivate(now)` 호출 - Domain 상태 변경
    2. `persistAll()` - 변경된 Aggregate 벌크 저장
    3. `deleteAllByPresetIds()` - 연관 매핑 벌크 삭제
    4. `return categoryPresets.size()` - 실제 처리 건수 반환
  - `@Transactional`: 원자적 처리

- **설계 특이사항**:
  - "삭제"는 실제 hard delete가 아닌 status = INACTIVE로 비활성화 (soft delete 방식)
  - 단, `category_mapping`은 hard delete (`DELETE FROM category_mapping WHERE preset_id IN (?)`)
  - 요청한 ID 수 != 실제 처리 수 (존재하지 않거나 이미 비활성 상태인 경우)

#### Domain Layer
- **Aggregate**: `CategoryPreset`
  - `deactivate(Instant now)`: `status = INACTIVE`, `updatedAt = now`

- **Port**:
  - `CategoryPresetCommandPort.persistAll(List<CategoryPreset>)` → `List<Long>`
  - `CategoryMappingCommandPort.deleteAllByPresetIds(List<Long>)`

#### Adapter-Out Layer
- **Database Query**:
  ```sql
  -- 1. 프리셋 목록 조회 (ACTIVE 상태만)
  SELECT * FROM category_preset WHERE id IN (?) AND status = 'ACTIVE'

  -- 2. 프리셋 벌크 비활성화 (JPA saveAll → UPDATE per entity)
  UPDATE category_preset SET status = 'INACTIVE', updated_at = ? WHERE id = ?
  -- (N번 실행)

  -- 3. 연관 매핑 벌크 삭제 (hard delete)
  DELETE FROM category_mapping WHERE preset_id IN (?)
  ```

---

## 공통 패턴 분석

### 1. Hexagonal Architecture 레이어 격리

| 레이어 | DTO/모델 | 책임 |
|--------|----------|------|
| **Adapter-In** | ApiRequest / ApiResponse | HTTP 계층 (Validation, JSON 직렬화) |
| **Application** | Command / Params / Bundle / Result | 유스케이스 조율, 트랜잭션 경계 |
| **Domain** | Aggregate (CategoryPreset), Criteria | 비즈니스 규칙 (update, deactivate) |
| **Adapter-Out** | Entity (CategoryPresetJpaEntity), CompositeDto | 영속화 기술 (JPA, QueryDSL) |

### 2. CQRS 분리

- **Query Side**: `CategoryPresetQueryController` → `CategoryPresetQueryApiMapper` → QueryUseCase → QueryService
- **Command Side**: `CategoryPresetCommandController` → `CategoryPresetCommandApiMapper` → CommandUseCase → CommandService

### 3. 트랜잭션 경계

| 계층 | @Transactional 위치 |
|------|---------------------|
| Adapter-In | 금지 |
| Application Service | 금지 (Facade에 위임) |
| **Facade (CategoryPresetMappingFacade)** | `@Transactional` (쓰기) |
| **ReadManager** | `@Transactional(readOnly = true)` |
| Adapter-Out | 금지 |

### 4. Cross-Domain 처리 패턴

**등록/수정/삭제**는 2개 도메인을 동시에 처리:
- `CategoryPreset` Aggregate (category_preset 테이블)
- `CategoryMapping` Aggregate (category_mapping 테이블)

`CategoryPresetMappingFacade`가 두 도메인의 원자적 처리를 담당.

**상세 조회**는 6개 테이블 크로스 조인:
- `CategoryPresetCompositionQueryAdapter`가 2단계 조회 후 조합
  1. `category_preset + shop + sales_channel_category + sales_channel` (4-way JOIN)
  2. `category_mapping + category` (2-way JOIN)

### 5. Validator 패턴 (크로스 도메인 검증)

`CategoryPresetValidator`는 4개 ReadManager를 의존하여 크로스 도메인 검증 수행:
- `CategoryPresetReadManager`: 프리셋 존재 검증
- `ShopReadManager`: Shop + 판매채널 정보 조회
- `SalesChannelCategoryReadManager`: 채널 일치 검증
- `CategoryReadManager`: 내부 카테고리 존재 검증

### 6. 삭제 전략 비대칭성

| 대상 | 삭제 방식 | 이유 |
|------|----------|------|
| `category_preset` | Soft Delete (status = INACTIVE) | 이력 보존 필요 |
| `category_mapping` | Hard Delete (DELETE) | 매핑은 재생성 가능 |

### 7. 변환 체인

```
[Query]
ApiRequest
  → CategoryPresetSearchParams (Mapper)
  → CategoryPresetSearchCriteria + QueryContext (Factory)
  → CategoryPresetCompositeDto (QueryDSL Projection)
  → CategoryPresetResult (EntityMapper.toResult)
  → CategoryPresetApiResponse (ApiMapper.toResponse)

[Command: 등록]
ApiRequest
  → RegisterCategoryPresetCommand (ApiMapper)
  → [Validator: salesChannelCategoryId 해소]
  → RegisterCategoryPresetBundle + CategoryPreset.forNew() (Factory)
  → CategoryPresetJpaEntity (EntityMapper.toEntity)
  → DB INSERT

[Command: 수정]
ApiRequest + pathVariable
  → UpdateCategoryPresetCommand (ApiMapper)
  → [기존 Aggregate 조회]
  → UpdateCategoryPresetBundle + CategoryPreset.update() (Factory + Facade)
  → CategoryPresetJpaEntity (EntityMapper.toEntity)
  → DB UPDATE + DELETE + INSERT

[Command: 삭제]
ApiRequest
  → DeleteCategoryPresetsCommand (ApiMapper)
  → StatusChangeContext (Factory)
  → [Aggregate 조회 + CategoryPreset.deactivate()] (Facade)
  → CategoryPresetJpaEntity (EntityMapper.toEntity)
  → DB UPDATE (status=INACTIVE) + DELETE (category_mapping)
```

---

## 에러 처리

`CategoryPresetErrorMapper`가 `CategoryPresetException`을 HTTP 응답으로 변환합니다.

| 에러 코드 | HTTP Status | 발생 상황 |
|----------|-------------|----------|
| CATPRE-001 | 404 Not Found | 프리셋을 찾을 수 없음 |
| CATPRE-002 | 400 Bad Request | Shop과 카테고리의 판매채널 불일치 |
| CATPRE-003 | 400 Bad Request | 요청한 내부 카테고리 미존재 |
| CATPRE-004 | 404 Not Found | 판매채널 카테고리(categoryCode)를 찾을 수 없음 |

---

## 주요 설계 결정

### 장점

1. **레이어 격리**: 4단계 DTO 변환으로 각 레이어 독립적 변경 가능
2. **테스트 용이성**: Port 인터페이스로 Mocking 용이
3. **원자성 보장**: Facade의 @Transactional로 CategoryPreset + CategoryMapping 동시 처리
4. **Composition 패턴**: 상세 조회 시 2단계 조회로 N+1 방지
5. **Validator 분리**: 크로스 도메인 검증 로직이 명확히 분리됨

### 트레이드오프

1. **2단계 조회**: 상세 조회에서 쿼리 2번 실행 (단일 쿼리로 최적화 가능하나 유지보수성 우선)
2. **소프트 딜리트 비대칭**: preset은 INACTIVE, mapping은 hard delete (일관성보다 실용성 우선)
3. **createdAt null 반환**: 등록 응답에서 `CategoryPresetIdApiResponse.of(id, null)` 패턴 개선 여지 있음
4. **벌크 UPDATE N회**: deactivate 시 JPA saveAll이 엔티티 수만큼 UPDATE 실행 (JDBC batch 최적화 가능)

---

## 문서 생성 정보

- **분석 일시**: 2026-02-18
- **분석 대상**: `web:CategoryPresetQueryController`, `web:CategoryPresetCommandController`
- **Base Path**: `/api/v1/market/category-presets`
- **엔드포인트 수**: Query 2개, Command 3개 (총 5개)
- **참조 파일**:
  - `adapter-in/rest-api/.../categorypreset/`
  - `application/.../categorypreset/`
  - `domain/.../categorypreset/`
  - `adapter-out/persistence-mysql/.../categorypreset/`
  - `adapter-out/persistence-mysql/.../categorymapping/`
