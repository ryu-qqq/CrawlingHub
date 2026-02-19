# SalesChannelCategory Domain API Flow Analysis

외부채널 카테고리(SalesChannelCategory) 도메인의 전체 API 호출 흐름 분석 문서입니다.

---

## 엔드포인트 목록

| HTTP Method | Path | 설명 | Controller Method |
|-------------|------|------|-------------------|
| GET | `/api/v1/market/sales-channels/{salesChannelId}/categories` | 외부채널 카테고리 목록 검색 | `searchCategories()` |
| POST | `/api/v1/market/sales-channels/{salesChannelId}/categories` | 외부채널 카테고리 등록 | `registerCategory()` |

---

## 1. GET /api/v1/market/sales-channels/{salesChannelId}/categories - 외부채널 카테고리 목록 조회

### 호출 흐름 다이어그램

```
[Adapter-In]
SalesChannelCategoryQueryController.searchCategories(salesChannelId, request)
  ├─ SalesChannelCategoryQueryApiMapper.toSearchParams(List.of(salesChannelId), request)
  │   ├─ CommonSearchParams.of(sortKey, sortDirection, page, size)   [기본값 처리]
  │   └─ -> SalesChannelCategorySearchParams
  ├─ SearchSalesChannelCategoryByOffsetUseCase.execute(params)       [Port Interface]
  └─ SalesChannelCategoryQueryApiMapper.toPageResponse(pageResult)
      └─ -> PageApiResponse<SalesChannelCategoryApiResponse>

[Application]
SearchSalesChannelCategoryByOffsetService.execute(params)            [UseCase 구현]
  ├─ SalesChannelCategoryQueryFactory.createCriteria(params)         [Criteria 생성]
  │   ├─ SalesChannelCategorySortKey 변환 (resolveSortKey)
  │   ├─ CommonVoFactory.parseSortDirection()
  │   ├─ CommonVoFactory.createPageRequest()
  │   ├─ CommonVoFactory.createQueryContext()
  │   ├─ SalesChannelCategorySearchField.fromString()
  │   ├─ SalesChannelCategoryStatus.fromString() (statuses 변환)
  │   └─ -> SalesChannelCategorySearchCriteria
  ├─ SalesChannelCategoryReadManager.findByCriteria(criteria)        [@Transactional readOnly]
  │   └─ SalesChannelCategoryQueryPort.findByCriteria()              [Port]
  ├─ SalesChannelCategoryReadManager.countByCriteria(criteria)       [@Transactional readOnly]
  │   └─ SalesChannelCategoryQueryPort.countByCriteria()             [Port]
  └─ SalesChannelCategoryAssembler.toPageResult(categories, page, size, totalElements)
      ├─ SalesChannelCategoryResult.from(category) (각 Aggregate 변환)
      └─ -> SalesChannelCategoryPageResult

[Adapter-Out]
SalesChannelCategoryQueryAdapter                                      [Port 구현]
  ├─ SalesChannelCategoryQueryDslRepository.findByCriteria(criteria)
  │   ├─ ConditionBuilder.salesChannelIdsIn()   WHERE sales_channel_id IN (?)
  │   ├─ ConditionBuilder.statusIn()            AND status IN (?)
  │   ├─ ConditionBuilder.searchCondition()     AND (external_category_name LIKE ? OR ...)
  │   ├─ resolveOrderSpecifier()                ORDER BY sortOrder/createdAt/externalCategoryName
  │   └─ offset + limit                         LIMIT ? OFFSET ?
  ├─ SalesChannelCategoryQueryDslRepository.countByCriteria(criteria)
  │   └─ SELECT COUNT(*) ... (동일 조건)
  └─ SalesChannelCategoryJpaEntityMapper.toDomain(entity)            [Entity → Domain]
      └─ SalesChannelCategory.reconstitute(...)

[Database]
- sales_channel_category (조회 대상)
```

### Layer별 상세

#### Adapter-In Layer

- **Controller**: `SalesChannelCategoryQueryController`
  - 위치: `adapter-in/rest-api/.../saleschannelcategory/controller/`
  - 매핑: `@GetMapping` on `SalesChannelCategoryAdminEndpoints.CATEGORIES`
  - Path: `/api/v1/market/sales-channels/{salesChannelId}/categories`
  - 권한: `@PreAuthorize("@access.superAdmin()")` + `@RequirePermission("sales-channel-category:read")`
  - 응답: `ResponseEntity<ApiResponse<PageApiResponse<SalesChannelCategoryApiResponse>>>`

- **Request DTO**: `SearchSalesChannelCategoriesApiRequest` (record)
  ```java
  record SearchSalesChannelCategoriesApiRequest(
      List<String> statuses,      // 상태 필터 (ACTIVE, INACTIVE)
      String searchField,         // 검색 필드 (externalCategoryCode, externalCategoryName)
      String searchWord,          // 검색어
      Integer depth,              // 카테고리 깊이 (현재 QueryFactory에서 미사용)
      Long parentId,              // 부모 카테고리 ID (현재 QueryFactory에서 미사용)
      Boolean mapped,             // 내부 카테고리 매핑 여부 (현재 QueryFactory에서 미사용)
      String sortKey,             // 정렬 기준 (기본값: sortOrder)
      String sortDirection,       // 정렬 방향 (기본값: DESC)
      Integer page,               // 페이지 번호 (기본값: 0)
      Integer size                // 페이지 크기 (기본값: 20)
  )
  ```
  - 참고: `depth`, `parentId`, `mapped` 파라미터는 DTO에 정의되어 있으나 현재 `SalesChannelCategoryQueryFactory.createCriteria()`에서 `SalesChannelCategorySearchCriteria`로 전달되지 않음 (향후 확장 예정 파라미터)

- **Response DTO**: `SalesChannelCategoryApiResponse` (record)
  ```java
  record SalesChannelCategoryApiResponse(
      Long id,                      // 카테고리 ID
      Long salesChannelId,          // 판매채널 ID
      String externalCategoryCode,  // 외부 카테고리 코드
      String externalCategoryName,  // 외부 카테고리명
      Long parentId,                // 부모 카테고리 ID
      int depth,                    // 카테고리 깊이
      String path,                  // 카테고리 경로
      int sortOrder,                // 정렬 순서
      boolean leaf,                 // 리프 노드 여부
      String status,                // 상태 (ACTIVE/INACTIVE)
      String createdAt,             // 생성일시 (ISO 8601)
      String updatedAt              // 수정일시 (ISO 8601)
  )
  ```

- **ApiMapper**: `SalesChannelCategoryQueryApiMapper`
  - `toSearchParams(List<Long> salesChannelIds, SearchSalesChannelCategoriesApiRequest)`:
    - `salesChannelId` → `List.of(salesChannelId)`로 래핑 (단건 → 복수 구조)
    - `CommonSearchParams.of()` 생성: includeDeleted=false, 기본값 처리 (sortKey="createdAt" 기본값, page=0, size=20)
    - 참고: Mapper에서는 sortKey 기본값을 "createdAt"으로 처리하나, QueryFactory에서 최종 변환 시 `SalesChannelCategorySortKey.defaultKey()` = `SORT_ORDER`로 fallback
  - `toPageResponse(SalesChannelCategoryPageResult)`:
    - `List<SalesChannelCategoryApiResponse>` 생성 (ISO 8601 날짜 변환)
    - `PageApiResponse.of(responses, page, size, totalElements)`
  - `DateTimeFormatUtils.formatIso8601()`: `Instant` → ISO 8601 String 변환

- **Endpoints 상수**: `SalesChannelCategoryAdminEndpoints`
  ```java
  CATEGORIES = "/api/v1/market/sales-channels/{salesChannelId}/categories"
  PATH_SALES_CHANNEL_ID = "salesChannelId"
  ```

#### Application Layer

- **UseCase Interface**: `SearchSalesChannelCategoryByOffsetUseCase`
  - `execute(SalesChannelCategorySearchParams)` → `SalesChannelCategoryPageResult`

- **Service 구현**: `SearchSalesChannelCategoryByOffsetService` (`@Service`)
  - QueryFactory로 Params → Criteria 변환
  - ReadManager로 데이터 조회 + 카운트 (각각 별도 트랜잭션)
  - Assembler로 PageResult 조합

- **Params DTO**: `SalesChannelCategorySearchParams` (record)
  ```java
  record SalesChannelCategorySearchParams(
      List<Long> salesChannelIds,   // 판매채널 ID 목록
      List<String> statuses,        // 상태 필터 문자열 목록
      String searchField,           // 검색 필드명
      String searchWord,            // 검색어
      CommonSearchParams searchParams  // 공통 검색 파라미터 (page, size, sort)
  )
  // page(), size(), sortKey(), sortDirection() 위임 메서드 제공
  ```

- **QueryFactory**: `SalesChannelCategoryQueryFactory` (`@Component`)
  - `createCriteria(SalesChannelCategorySearchParams)` → `SalesChannelCategorySearchCriteria`
  - `resolveSortKey(String)`: CREATED_AT / EXTERNAL_NAME / SORT_ORDER 변환 (미매칭 시 `SORT_ORDER` 기본값)
  - `CommonVoFactory`: 공통 VO 생성 위임 (PageRequest, QueryContext, SortDirection)
  - `SalesChannelCategoryStatus.fromString()`: 상태 문자열 → Enum 변환 (미매칭 시 ACTIVE)

- **ReadManager**: `SalesChannelCategoryReadManager` (`@Component`)
  - `findByCriteria(SalesChannelCategorySearchCriteria)` → `List<SalesChannelCategory>`
    - `@Transactional(readOnly = true)`
  - `countByCriteria(SalesChannelCategorySearchCriteria)` → `long`
    - `@Transactional(readOnly = true)`
  - `getById(SalesChannelCategoryId)`: 단건 조회 (존재하지 않으면 `SalesChannelCategoryNotFoundException`)
  - `existsBySalesChannelIdAndExternalCode(Long, String)`: 중복 체크용

- **Assembler**: `SalesChannelCategoryAssembler` (`@Component`)
  - `toPageResult(List<SalesChannelCategory>, int page, int size, long totalElements)` → `SalesChannelCategoryPageResult`
  - `SalesChannelCategoryResult.from(SalesChannelCategory)`: Domain → Result DTO 변환

- **Result DTO**: `SalesChannelCategoryResult` (record)
  - `SalesChannelCategory` Domain 객체에서 직접 변환 (`from()` 팩토리 메서드)
  - `status`: `SalesChannelCategoryStatus.name()` → String

- **PageResult DTO**: `SalesChannelCategoryPageResult` (record)
  ```java
  record SalesChannelCategoryPageResult(
      List<SalesChannelCategoryResult> results,
      PageMeta pageMeta   // page, size, totalElements, totalPages
  )
  ```

#### Domain Layer

- **Port**: `SalesChannelCategoryQueryPort`
  ```java
  Optional<SalesChannelCategory> findById(SalesChannelCategoryId id);
  List<SalesChannelCategory> findByCriteria(SalesChannelCategorySearchCriteria criteria);
  long countByCriteria(SalesChannelCategorySearchCriteria criteria);
  boolean existsBySalesChannelIdAndExternalCode(Long salesChannelId, String externalCategoryCode);
  ```

- **Criteria**: `SalesChannelCategorySearchCriteria` (record)
  ```java
  record SalesChannelCategorySearchCriteria(
      List<Long> salesChannelIds,
      List<SalesChannelCategoryStatus> statuses,
      SalesChannelCategorySearchField searchField,
      String searchWord,
      QueryContext<SalesChannelCategorySortKey> queryContext
  )
  // hasSalesChannelFilter(), hasStatusFilter(), hasSearchCondition(), hasSearchField() 편의 메서드
  // size(), offset(), page() QueryContext 위임
  ```

- **SortKey**: `SalesChannelCategorySortKey` (enum)
  - `CREATED_AT("createdAt")` / `EXTERNAL_NAME("externalCategoryName")` / `SORT_ORDER("sortOrder")`
  - `defaultKey()` = `SORT_ORDER`

- **SearchField**: `SalesChannelCategorySearchField` (enum)
  - `EXTERNAL_CODE("externalCategoryCode")` / `EXTERNAL_NAME("externalCategoryName")`

- **Aggregate**: `SalesChannelCategory`
  - 필드: `id`, `salesChannelId`, `externalCategoryCode`, `externalCategoryName`, `parentId`, `depth(CategoryDepth)`, `path(CategoryPath)`, `sortOrder(SortOrder)`, `leaf`, `status(SalesChannelCategoryStatus)`, `displayPath`, `createdAt`, `updatedAt`
  - `reconstitute()`: DB에서 복원 시 사용 (id 포함)
  - `forNew()`: 신규 생성 시 사용 (id = null, status = ACTIVE)

- **Status VO**: `SalesChannelCategoryStatus` (enum)
  - `ACTIVE` / `INACTIVE`
  - `fromString(String)`: null이거나 미매칭 시 ACTIVE 반환

#### Adapter-Out Layer

- **Adapter**: `SalesChannelCategoryQueryAdapter` (`@Component`, implements `SalesChannelCategoryQueryPort`)
  - `findByCriteria()`: QueryDslRepository → mapper.toDomain() 변환
  - `countByCriteria()`: QueryDslRepository 카운트 위임
  - `existsBySalesChannelIdAndExternalCode()`: QueryDslRepository 위임

- **QueryDSL Repository**: `SalesChannelCategoryQueryDslRepository` (`@Repository`)
  - `findByCriteria(criteria)`: 동적 쿼리 (ConditionBuilder 조합)
  - `countByCriteria(criteria)`: COUNT 쿼리 (동일 WHERE 조건)
  - `resolveOrderSpecifier()`: SortKey enum → QueryDSL OrderSpecifier 변환
  - `findAllByIds(List<Long>)`: 다중 ID 조회 (depth 오름차순 정렬, 내부용)

- **ConditionBuilder**: `SalesChannelCategoryConditionBuilder` (`@Component`)
  - `salesChannelIdsIn(Collection<Long>)`: `sales_channel_id IN (?)`
  - `statusIn(criteria)`: `status IN (?)` (criteria.hasStatusFilter() 체크)
  - `searchCondition(criteria)`:
    - 검색어 없으면 null
    - searchField 없으면: `external_category_name LIKE ? OR external_category_code LIKE ?`
    - EXTERNAL_CODE: `external_category_code LIKE ?`
    - EXTERNAL_NAME: `external_category_name LIKE ?`
  - `externalCategoryCodeEq(String)`: `external_category_code = ?`
  - `salesChannelIdEq(Long)`: `sales_channel_id = ?`

- **Entity Mapper**: `SalesChannelCategoryJpaEntityMapper`
  - `toDomain(entity)`: Entity → `SalesChannelCategory.reconstitute()`
    - id null 체크 (IllegalStateException)
    - `SalesChannelCategoryStatus.fromString(entity.getStatus())`

- **JPA Entity**: `SalesChannelCategoryJpaEntity`
  - 테이블: `sales_channel_category`
  - `@Table(name = "sales_channel_category")`
  - `@GeneratedValue(strategy = IDENTITY)`
  - `BaseAuditEntity` 상속 (createdAt, updatedAt: Instant)

- **Database Query** (findByCriteria):
  ```sql
  SELECT *
  FROM sales_channel_category
  WHERE sales_channel_id IN (?)        -- 채널 ID 필터 (필수)
    AND status IN (?)                   -- 상태 필터 (옵션)
    AND (
        external_category_name LIKE ?   -- 검색 (searchField 없는 경우)
        OR external_category_code LIKE ?
    )
  ORDER BY sort_order DESC              -- 기본: SORT_ORDER DESC
  LIMIT ? OFFSET ?
  ```

- **Database Query** (countByCriteria):
  ```sql
  SELECT COUNT(*)
  FROM sales_channel_category
  WHERE sales_channel_id IN (?)
    AND status IN (?)
    AND external_category_name LIKE ?
  ```

---

## 2. POST /api/v1/market/sales-channels/{salesChannelId}/categories - 외부채널 카테고리 등록

### 호출 흐름 다이어그램

```
[Adapter-In]
SalesChannelCategoryCommandController.registerCategory(salesChannelId, request)
  ├─ SalesChannelCategoryCommandApiMapper.toCommand(salesChannelId, request)
  │   └─ -> RegisterSalesChannelCategoryCommand
  └─ RegisterSalesChannelCategoryUseCase.execute(command)            [Port Interface]
      └─ -> Long (categoryId)
          └─ SalesChannelCategoryIdApiResponse.of(categoryId)       [List.of(id) 래핑]

[Application]
RegisterSalesChannelCategoryService.execute(command)                 [UseCase 구현]
  ├─ SalesChannelCategoryValidator.validateExternalCodeNotDuplicate(salesChannelId, code)
  │   └─ SalesChannelCategoryReadManager.existsBySalesChannelIdAndExternalCode()
  │       └─ SalesChannelCategoryQueryPort.existsBySalesChannelIdAndExternalCode()  [Port]
  │           → true → SalesChannelCategoryCodeDuplicateException 발생
  ├─ SalesChannelCategoryCommandFactory.create(command)              [Domain 객체 생성]
  │   ├─ TimeProvider.now()                                          [현재 시각 주입]
  │   └─ SalesChannelCategory.forNew(...)                            [신규 Aggregate]
  │       ├─ SalesChannelCategoryId.forNew()                         [id = null]
  │       ├─ CategoryDepth.of(depth)
  │       ├─ CategoryPath.of(path)
  │       ├─ SortOrder.of(sortOrder)
  │       └─ status = ACTIVE (고정)
  └─ SalesChannelCategoryCommandManager.persist(category)            [@Transactional]
      └─ SalesChannelCategoryCommandPort.persist()                   [Port]

[Adapter-Out]
SalesChannelCategoryCommandAdapter                                    [Port 구현]
  ├─ SalesChannelCategoryJpaEntityMapper.toEntity(category)          [Domain → Entity]
  └─ SalesChannelCategoryJpaRepository.save(entity)                  [Spring Data JPA]
      └─ -> saved.getId()                                            [자동 생성 ID 반환]

[Database]
- INSERT INTO sales_channel_category
```

### Layer별 상세

#### Adapter-In Layer

- **Controller**: `SalesChannelCategoryCommandController`
  - 위치: `adapter-in/rest-api/.../saleschannelcategory/controller/`
  - 매핑: `@PostMapping` on `SalesChannelCategoryAdminEndpoints.CATEGORIES`
  - Path: `POST /api/v1/market/sales-channels/{salesChannelId}/categories`
  - 권한: `@PreAuthorize("@access.superAdmin()")` + `@RequirePermission("sales-channel-category:write")`
  - 응답: `ResponseEntity<ApiResponse<SalesChannelCategoryIdApiResponse>>` (HTTP 201 Created)

- **Request DTO**: `RegisterSalesChannelCategoryApiRequest` (record)
  ```java
  @Schema(description = "외부채널 카테고리 등록 요청")
  record RegisterSalesChannelCategoryApiRequest(
      @NotBlank String externalCategoryCode,   // 외부 카테고리 코드
      @NotBlank String externalCategoryName,   // 외부 카테고리명
      @NotNull Long parentId,                  // 부모 카테고리 ID (최상위: 0)
      @NotNull @Min(0) Integer depth,          // 카테고리 깊이
      @NotBlank String path,                   // 카테고리 경로
      @NotNull @Min(0) Integer sortOrder,      // 정렬 순서
      @NotNull Boolean leaf,                   // 리프 노드 여부
      String displayPath                       // 표시용 이름 경로 (옵션)
  )
  ```

- **Response DTO**: `SalesChannelCategoryIdApiResponse` (record)
  ```java
  record SalesChannelCategoryIdApiResponse(List<Long> categoryIds)
  // SalesChannelCategoryIdApiResponse.of(Long) → List.of(id)
  // (향후 배치 등록 확장 고려한 List 구조)
  ```

- **ApiMapper**: `SalesChannelCategoryCommandApiMapper`
  - `toCommand(Long salesChannelId, RegisterSalesChannelCategoryApiRequest)` → `RegisterSalesChannelCategoryCommand`
  - 단순 필드 복사 (Validation 완료된 DTO 그대로 Command로 변환)

#### Application Layer

- **UseCase Interface**: `RegisterSalesChannelCategoryUseCase`
  - `execute(RegisterSalesChannelCategoryCommand)` → `Long` (생성된 categoryId)

- **Service 구현**: `RegisterSalesChannelCategoryService` (`@Service`)
  1. Validator로 중복 코드 검증
  2. CommandFactory로 Domain Aggregate 생성
  3. CommandManager로 영속화 (트랜잭션)

- **Command DTO**: `RegisterSalesChannelCategoryCommand` (record)
  ```java
  record RegisterSalesChannelCategoryCommand(
      Long salesChannelId,
      String externalCategoryCode,
      String externalCategoryName,
      Long parentId,
      int depth,
      String path,
      int sortOrder,
      boolean leaf,
      String displayPath
  )
  ```

- **Validator**: `SalesChannelCategoryValidator` (`@Component`)
  - `validateExternalCodeNotDuplicate(Long salesChannelId, String externalCategoryCode)`:
    - ReadManager로 중복 확인
    - 이미 존재하면 `SalesChannelCategoryCodeDuplicateException` 발생

- **CommandFactory**: `SalesChannelCategoryCommandFactory` (`@Component`)
  - `create(RegisterSalesChannelCategoryCommand)` → `SalesChannelCategory`
  - `TimeProvider.now()` 호출 (APP-TIM-001: 시각은 Factory에서만 주입)
  - `SalesChannelCategory.forNew()`: 신규 Aggregate 생성
    - `id = SalesChannelCategoryId.forNew()` (id value = null, 저장 후 DB에서 생성)
    - `status = ACTIVE` 고정
    - `CategoryDepth.of(depth)`, `CategoryPath.of(path)`, `SortOrder.of(sortOrder)` VO 생성

- **CommandManager**: `SalesChannelCategoryCommandManager` (`@Component`)
  - `persist(SalesChannelCategory)` → `Long`
  - **`@Transactional`**: 커맨드 트랜잭션 경계 (이 도메인은 Facade 없이 Manager가 직접 트랜잭션 보유)

#### Domain Layer

- **Port**: `SalesChannelCategoryCommandPort`
  ```java
  Long persist(SalesChannelCategory salesChannelCategory);
  ```

- **Aggregate**: `SalesChannelCategory`
  - `forNew(Long salesChannelId, ...)` 팩토리 메서드:
    - `id = SalesChannelCategoryId.forNew()` (id.value() = null)
    - `status = SalesChannelCategoryStatus.ACTIVE`
    - `CategoryDepth.of(depth)`, `CategoryPath.of(path)`, `SortOrder.of(sortOrder)` VO 래핑
  - `update(SalesChannelCategoryUpdateData, Instant)`: 수정 시 사용

- **ID VO**: `SalesChannelCategoryId`
  - `forNew()`: id = null (DB 저장 전)
  - `of(Long id)`: 기존 ID로 생성

- **예외**:
  - `SalesChannelCategoryCodeDuplicateException`: 외부 카테고리 코드 중복 (HTTP 409)
  - `SalesChannelCategoryNotFoundException`: 카테고리 미존재 (HTTP 404)

#### Adapter-Out Layer

- **Adapter**: `SalesChannelCategoryCommandAdapter` (`@Component`, implements `SalesChannelCategoryCommandPort`)
  - `persist(SalesChannelCategory category)` → `Long`:
    1. `mapper.toEntity(category)` Domain → JPA Entity 변환
    2. `repository.save(entity)` 저장
    3. `saved.getId()` 반환 (IDENTITY 전략으로 DB 자동 생성)

- **Entity Mapper**: `SalesChannelCategoryJpaEntityMapper`
  - `toEntity(SalesChannelCategory)` → `SalesChannelCategoryJpaEntity.create(...)`:
    - `category.idValue()` = null (신규이므로 DB에서 AUTO_INCREMENT)
    - `category.status().name()` → String ("ACTIVE")

- **JPA Repository**: `SalesChannelCategoryJpaRepository`
  - `extends JpaRepository<SalesChannelCategoryJpaEntity, Long>`
  - `save(entity)`: 단순 INSERT (id가 null이므로 새로 INSERT)

- **Database Query**:
  ```sql
  INSERT INTO sales_channel_category (
      sales_channel_id,
      external_category_code,
      external_category_name,
      parent_id,
      depth,
      path,
      sort_order,
      leaf,
      status,
      display_path,
      created_at,
      updated_at
  ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'ACTIVE', ?, ?, ?)
  ```

---

## 공통 패턴 분석

### 1. Hexagonal Architecture 레이어 격리

| 레이어 | DTO/모델 | 책임 |
|--------|----------|------|
| **Adapter-In** | ApiRequest/ApiResponse | HTTP 직렬화, Validation, ISO 8601 날짜 변환 |
| **Application** | Command/Params/Result/PageResult | 유스케이스 조율, 변환 위임 |
| **Domain** | Aggregate, VO, Criteria, SearchField, SortKey | 비즈니스 규칙, 불변성 |
| **Adapter-Out** | Entity | 영속화 기술 관심사 (JPA, QueryDSL) |

### 2. CQRS 분리

- **Query**: `SalesChannelCategoryQueryController` → `SalesChannelCategoryQueryApiMapper` → `SearchSalesChannelCategoryByOffsetUseCase` → `SearchSalesChannelCategoryByOffsetService`
- **Command**: `SalesChannelCategoryCommandController` → `SalesChannelCategoryCommandApiMapper` → `RegisterSalesChannelCategoryUseCase` → `RegisterSalesChannelCategoryService`

### 3. 트랜잭션 경계

| 계층 | `@Transactional` 위치 | 비고 |
|------|----------------------|------|
| Adapter-In | 없음 | 금지 |
| Application Service | 없음 | Service는 조율만 담당 |
| **Application ReadManager** | `@Transactional(readOnly = true)` | 조회 전용 트랜잭션 |
| **Application CommandManager** | `@Transactional` | 쓰기 트랜잭션 경계 (Facade 없이 Manager가 직접 보유) |
| Adapter-Out | 없음 | 금지 |

- 이 도메인은 등록이 단순 단건이므로 **별도 Facade 없이 CommandManager가 트랜잭션 경계**를 보유합니다. 복잡한 다중 Aggregate 트랜잭션이 필요한 도메인(Seller 등)과 달리 단순한 구조입니다.

### 4. 검증 패턴 (Validator)

- `SalesChannelCategoryValidator.validateExternalCodeNotDuplicate()`:
  - `ReadManager.existsBySalesChannelIdAndExternalCode()` → `QueryPort.existsBySalesChannelIdAndExternalCode()`
  - `SalesChannelCategoryConditionBuilder.salesChannelIdEq()` + `externalCategoryCodeEq()` 조합
  - QueryDSL `selectOne().fetchFirst()` → null 여부로 존재 확인

### 5. salesChannelIds 복수 구조

Query 시 Controller에서 `List.of(salesChannelId)` 형태로 래핑하여 전달합니다. `SalesChannelCategorySearchCriteria`와 QueryDSL Repository가 `IN` 절 기반 다중 채널 조회를 지원하므로, 향후 여러 채널 동시 조회 확장이 가능한 설계입니다.

### 6. SalesChannelCategoryIdApiResponse 리스트 구조

`SalesChannelCategoryIdApiResponse`는 단건 등록 시에도 `List<Long> categoryIds = List.of(id)`로 반환합니다. 향후 배치(bulk) 등록 API 확장 시 동일한 응답 구조를 재사용할 수 있도록 설계되어 있습니다.

### 7. 변환 체인

```
[Query]
@PathVariable salesChannelId
  → List.of(salesChannelId)
    → SalesChannelCategorySearchParams (+ CommonSearchParams)
      → SalesChannelCategorySearchCriteria (+ QueryContext, Status enum, SearchField enum)
        → SQL (WHERE sales_channel_id IN, status IN, LIKE, ORDER BY, LIMIT/OFFSET)
          → SalesChannelCategoryJpaEntity
            → SalesChannelCategory (reconstitute)
              → SalesChannelCategoryResult (from)
                → SalesChannelCategoryApiResponse (ISO 8601 날짜 변환)
                  → PageApiResponse<SalesChannelCategoryApiResponse>

[Command]
@Valid RegisterSalesChannelCategoryApiRequest
  → RegisterSalesChannelCategoryCommand
    → [Validator: 중복 코드 체크]
      → SalesChannelCategory.forNew (+ VO 래핑, TimeProvider.now())
        → SalesChannelCategoryJpaEntity.create (Domain → Entity)
          → repository.save(entity)
            → saved.getId()
              → SalesChannelCategoryIdApiResponse.of(id) → List.of(id)
```

### 8. 에러 처리

- **`SalesChannelCategoryErrorMapper`**: `SalesChannelCategoryException` 계층의 예외를 HTTP 응답으로 변환
  - `supports(DomainException)`: `instanceof SalesChannelCategoryException`
  - 에러 타입 URI: `/errors/sales-channel-category/{code}`
  - HTTP Status: 예외의 `httpStatus()` 값 사용

---

## 관련 파일 목록

### Adapter-In (rest-api)
| 파일 | 역할 |
|------|------|
| `SalesChannelCategoryAdminEndpoints.java` | URL 경로 상수 정의 |
| `controller/SalesChannelCategoryQueryController.java` | 조회 컨트롤러 |
| `controller/SalesChannelCategoryCommandController.java` | 커맨드 컨트롤러 |
| `mapper/SalesChannelCategoryQueryApiMapper.java` | 조회 DTO 변환 |
| `mapper/SalesChannelCategoryCommandApiMapper.java` | 커맨드 DTO 변환 |
| `dto/query/SearchSalesChannelCategoriesApiRequest.java` | 검색 요청 DTO |
| `dto/command/RegisterSalesChannelCategoryApiRequest.java` | 등록 요청 DTO |
| `dto/response/SalesChannelCategoryApiResponse.java` | 조회 응답 DTO |
| `dto/response/SalesChannelCategoryIdApiResponse.java` | 등록 응답 DTO |
| `error/SalesChannelCategoryErrorMapper.java` | 도메인 예외 → HTTP 응답 변환 |

### Application
| 파일 | 역할 |
|------|------|
| `port/in/query/SearchSalesChannelCategoryByOffsetUseCase.java` | 조회 UseCase 인터페이스 |
| `port/in/command/RegisterSalesChannelCategoryUseCase.java` | 등록 UseCase 인터페이스 |
| `port/out/query/SalesChannelCategoryQueryPort.java` | 조회 Domain Port |
| `port/out/command/SalesChannelCategoryCommandPort.java` | 커맨드 Domain Port |
| `service/query/SearchSalesChannelCategoryByOffsetService.java` | 조회 Service 구현 |
| `service/command/RegisterSalesChannelCategoryService.java` | 등록 Service 구현 |
| `manager/SalesChannelCategoryReadManager.java` | 조회 Manager (@Transactional readOnly) |
| `manager/SalesChannelCategoryCommandManager.java` | 커맨드 Manager (@Transactional) |
| `validator/SalesChannelCategoryValidator.java` | 도메인 검증 |
| `factory/SalesChannelCategoryQueryFactory.java` | Criteria 생성 Factory |
| `factory/SalesChannelCategoryCommandFactory.java` | Domain 객체 생성 Factory |
| `assembler/SalesChannelCategoryAssembler.java` | Domain → Result DTO 변환 |
| `dto/query/SalesChannelCategorySearchParams.java` | 조회 파라미터 DTO |
| `dto/command/RegisterSalesChannelCategoryCommand.java` | 등록 커맨드 DTO |
| `dto/response/SalesChannelCategoryResult.java` | 단건 조회 결과 DTO |
| `dto/response/SalesChannelCategoryPageResult.java` | 페이징 조회 결과 DTO |

### Domain
| 파일 | 역할 |
|------|------|
| `aggregate/SalesChannelCategory.java` | Aggregate Root |
| `id/SalesChannelCategoryId.java` | ID Value Object |
| `vo/SalesChannelCategoryStatus.java` | 상태 Enum (ACTIVE/INACTIVE) |
| `query/SalesChannelCategorySearchCriteria.java` | 검색 조건 (불변) |
| `query/SalesChannelCategorySearchField.java` | 검색 필드 Enum |
| `query/SalesChannelCategorySortKey.java` | 정렬 키 Enum |
| `exception/SalesChannelCategoryNotFoundException.java` | 미존재 예외 (HTTP 404) |
| `exception/SalesChannelCategoryCodeDuplicateException.java` | 중복 코드 예외 (HTTP 409) |

### Adapter-Out (persistence-mysql)
| 파일 | 역할 |
|------|------|
| `adapter/SalesChannelCategoryQueryAdapter.java` | QueryPort 구현 |
| `adapter/SalesChannelCategoryCommandAdapter.java` | CommandPort 구현 |
| `repository/SalesChannelCategoryQueryDslRepository.java` | QueryDSL 동적 쿼리 |
| `repository/SalesChannelCategoryJpaRepository.java` | Spring Data JPA (save) |
| `condition/SalesChannelCategoryConditionBuilder.java` | WHERE 조건 빌더 |
| `mapper/SalesChannelCategoryJpaEntityMapper.java` | Domain ↔ Entity 변환 |
| `entity/SalesChannelCategoryJpaEntity.java` | JPA Entity (sales_channel_category) |

---

**분석 일시**: 2026-02-18
**분석 대상**: `saleschannelcategory` 도메인 전체 (Query 1개, Command 1개)
**테이블**: `sales_channel_category`
