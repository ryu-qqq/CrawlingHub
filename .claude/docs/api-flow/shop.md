# Shop Domain API Flow Analysis

외부몰(Shop) 도메인의 전체 API 호출 흐름 분석 문서입니다.

---

## 엔드포인트 목록

| HTTP Method | Path | 설명 | Controller Method |
|-------------|------|------|-------------------|
| GET | `/api/v1/market/shops` | 외부몰 목록 검색 | `searchShops()` |
| POST | `/api/v1/market/shops` | 외부몰 등록 | `registerShop()` |
| PUT | `/api/v1/market/shops/{shopId}` | 외부몰 수정 | `updateShop()` |

---

## 1. GET /api/v1/market/shops - 외부몰 목록 검색

### 호출 흐름 다이어그램

```
[Adapter-In]
ShopQueryController.searchShops(SearchShopsApiRequest)
  ├─ ShopQueryApiMapper.toSearchParams(request)              [Request → Params 변환]
  │   └─ CommonSearchParams.of(sortKey, sortDirection, page, size)
  │       └─ ShopSearchParams.of(salesChannelId, statuses, searchField, searchWord, searchParams)
  ├─ SearchShopByOffsetUseCase.execute(params)               [Port Interface]
  └─ ShopQueryApiMapper.toPageResponse(pageResult)           [Result → Response 변환]
      └─ DateTimeFormatUtils.formatIso8601(createdAt, updatedAt)

[Application]
SearchShopByOffsetService.execute(ShopSearchParams)          [UseCase 구현체]
  ├─ ShopQueryFactory.createCriteria(params)                 [Params → Criteria 변환]
  │   ├─ ShopSortKey.fromString(sortKey)                     [정렬 키 변환]
  │   ├─ SortDirection.parse(sortDirection)                  [정렬 방향 변환]
  │   ├─ PageRequest.of(page, size)                          [페이지 요청 생성]
  │   ├─ ShopSearchField.fromString(searchField)             [검색 필드 변환]
  │   ├─ ShopStatus.fromString(statuses)                     [상태 목록 변환]
  │   └─ ShopSearchCriteria.of(...)                          [Criteria 조합]
  ├─ ShopReadManager.findByCriteria(criteria)                [목록 조회]
  │   └─ ShopQueryPort.findByCriteria(criteria)              [Domain Port]
  ├─ ShopReadManager.countByCriteria(criteria)               [총 개수 조회]
  │   └─ ShopQueryPort.countByCriteria(criteria)             [Domain Port]
  └─ ShopAssembler.toPageResult(shops, page, size, totalElements)
      ├─ ShopResult.from(shop)                               [Domain → Result 변환]
      └─ ShopPageResult.of(results, pageMeta)                [페이징 결과 조합]

[Domain Port]
ShopQueryPort.findByCriteria(ShopSearchCriteria)             [Port 인터페이스]
ShopQueryPort.countByCriteria(ShopSearchCriteria)            [Port 인터페이스]

[Adapter-Out]
ShopQueryAdapter.findByCriteria(ShopSearchCriteria)          [Port 구현체]
  └─ ShopQueryDslRepository.findByCriteria(criteria)
      ├─ ShopConditionBuilder.statusIn(criteria)             [상태 조건: IN (?)]
      ├─ ShopConditionBuilder.searchCondition(criteria)      [검색 조건: LIKE %?%]
      ├─ ShopConditionBuilder.notDeleted()                   [소프트 삭제: deleted_at IS NULL]
      ├─ resolveOrderSpecifier(criteria)                     [정렬: createdAt/updatedAt/shopName]
      ├─ .offset(criteria.offset()).limit(criteria.size())   [페이징]
      └─ ShopJpaEntityMapper.toDomain(entity)               [Entity → Domain 변환]

ShopQueryAdapter.countByCriteria(ShopSearchCriteria)         [Port 구현체]
  └─ ShopQueryDslRepository.countByCriteria(criteria)
      ├─ ShopConditionBuilder.statusIn(criteria)
      ├─ ShopConditionBuilder.searchCondition(criteria)
      └─ ShopConditionBuilder.notDeleted()

[Database]
- shop (단일 테이블 조회)
```

### Layer별 상세

#### Adapter-In Layer

- **Controller**: `ShopQueryController`
  - `@RequestMapping`: `ShopAdminEndpoints.SHOPS` = `/api/v1/market/shops`
  - `@GetMapping` + `@PreAuthorize("@access.superAdmin()")` + `@RequirePermission("shop:read")`
  - Method: `searchShops(@ParameterObject @Valid SearchShopsApiRequest)`
  - Response: `ResponseEntity<ApiResponse<PageApiResponse<ShopApiResponse>>>` (HTTP 200)

- **Request DTO**: `SearchShopsApiRequest` (Record)
  ```java
  record SearchShopsApiRequest(
      Long salesChannelId,      // 판매채널 ID 필터 (선택)
      List<String> statuses,    // 상태 필터 [ACTIVE, INACTIVE] (선택)
      String searchField,       // 검색 필드: SHOP_NAME | ACCOUNT_ID (선택)
      String searchWord,        // 검색어 (선택)
      String sortKey,           // 정렬 키: createdAt | updatedAt | shopName (선택, 기본: createdAt)
      String sortDirection,     // 정렬 방향: ASC | DESC (선택, 기본: DESC)
      Integer page,             // 페이지 번호, 0-based (선택, 기본: 0)
      Integer size              // 페이지 크기 (선택, 기본: 20)
  )
  ```

- **Response DTO**: `ShopApiResponse` (Record)
  ```java
  record ShopApiResponse(
      Long id,              // Shop ID
      Long salesChannelId,  // 판매채널 ID
      String shopName,      // 외부몰명
      String accountId,     // 계정 ID
      String status,        // 상태: ACTIVE | INACTIVE
      String createdAt,     // 생성일시 (ISO8601: "2025-01-23T10:30:00+09:00")
      String updatedAt      // 수정일시 (ISO8601: "2025-01-23T10:30:00+09:00")
  )
  ```

- **ApiMapper**: `ShopQueryApiMapper`
  - `toSearchParams(SearchShopsApiRequest)` → `ShopSearchParams`
    - `sortKey`, `sortDirection`, `page`, `size` 기본값 처리 (null 방어)
    - `CommonSearchParams.of(false, null, null, sortKey, sortDirection, page, size)` 조합
  - `toResponse(ShopResult)` → `ShopApiResponse`
    - `DateTimeFormatUtils.formatIso8601(Instant)` 로 날짜 포맷 변환
  - `toPageResponse(ShopPageResult)` → `PageApiResponse<ShopApiResponse>`
    - `pageMeta.page()`, `pageMeta.size()`, `pageMeta.totalElements()` 추출

#### Application Layer

- **UseCase Interface**: `SearchShopByOffsetUseCase`
  ```java
  interface SearchShopByOffsetUseCase {
      ShopPageResult execute(ShopSearchParams params);
  }
  ```

- **Service 구현**: `SearchShopByOffsetService` (`@Service`)
  - `ShopQueryFactory.createCriteria(params)` → `ShopSearchCriteria`
  - `ShopReadManager.findByCriteria(criteria)` → `List<Shop>`
  - `ShopReadManager.countByCriteria(criteria)` → `long`
  - `ShopAssembler.toPageResult(shops, page, size, totalElements)` → `ShopPageResult`

- **Application DTO - Params**: `ShopSearchParams` (Record)
  ```java
  record ShopSearchParams(
      Long salesChannelId,
      List<String> statuses,
      String searchField,
      String searchWord,
      CommonSearchParams searchParams   // page, size, sortKey, sortDirection 포함
  )
  ```

- **Application DTO - Result**: `ShopPageResult` (Record)
  ```java
  record ShopPageResult(
      List<ShopResult> results,
      PageMeta pageMeta               // page, size, totalElements
  )
  ```

- **Application DTO - Item Result**: `ShopResult` (Record)
  ```java
  record ShopResult(
      Long id,
      Long salesChannelId,
      String shopName,
      String accountId,
      String status,        // ShopStatus.name()
      Instant createdAt,
      Instant updatedAt
  )
  ```

- **Factory**: `ShopQueryFactory`
  - `ShopSortKey resolveSortKey(String)`: enum 순서로 매핑 (defaultKey = CREATED_AT)
  - `ShopSearchField.fromString(String)`: null 허용 (null 반환 시 전체 필드 검색)
  - `ShopStatus.fromString(String)`: 상태 문자열 → enum 리스트 변환
  - `CommonVoFactory.createQueryContext(sortKey, sortDirection, pageRequest, includeDeleted)`: `QueryContext<ShopSortKey>` 생성

- **Manager**: `ShopReadManager` (`@Component`)
  - `@Transactional(readOnly = true)` 적용
  - `getById(ShopId)`: 단건 조회 + 없으면 `ShopNotFoundException`
  - `findByCriteria(ShopSearchCriteria)`: 목록 조회
  - `countByCriteria(ShopSearchCriteria)`: 총 개수
  - `existsBySalesChannelIdAndAccountId(Long, String)`: 중복 확인
  - `existsBySalesChannelIdAndAccountIdExcluding(Long, String, ShopId)`: 수정 시 자기 자신 제외 중복 확인

- **Assembler**: `ShopAssembler` (`@Component`)
  - `toResult(Shop)` → `ShopResult.from(shop)` 위임
  - `toPageResult(List<Shop>, page, size, totalElements)` → `ShopPageResult`

#### Domain Layer

- **Port**: `ShopQueryPort`
  ```java
  interface ShopQueryPort {
      Optional<Shop> findById(ShopId id);
      List<Shop> findByCriteria(ShopSearchCriteria criteria);
      long countByCriteria(ShopSearchCriteria criteria);
      boolean existsBySalesChannelIdAndAccountId(Long salesChannelId, String accountId);
      boolean existsBySalesChannelIdAndAccountIdExcluding(Long salesChannelId, String accountId, ShopId excludeId);
  }
  ```

- **Criteria**: `ShopSearchCriteria` (Record)
  ```java
  record ShopSearchCriteria(
      Long salesChannelId,
      List<ShopStatus> statuses,            // 불변 List로 방어적 복사
      ShopSearchField searchField,
      String searchWord,
      QueryContext<ShopSortKey> queryContext // sortKey, sortDirection, pageRequest, includeDeleted
  )
  ```
  - 헬퍼 메서드: `hasSalesChannelFilter()`, `hasStatusFilter()`, `hasSearchCondition()`, `hasSearchField()`
  - 페이징: `size()`, `offset()`, `page()` (queryContext 위임)

- **Value Objects**:
  - `ShopSortKey` (enum): `CREATED_AT("createdAt")`, `UPDATED_AT("updatedAt")`, `SHOP_NAME("shopName")`
  - `ShopSearchField` (enum): `SHOP_NAME("shopName")`, `ACCOUNT_ID("accountId")`
  - `ShopStatus` (enum): `ACTIVE`, `INACTIVE`

#### Adapter-Out Layer

- **Adapter**: `ShopQueryAdapter` (`@Component`, `implements ShopQueryPort`)
  - `ShopQueryDslRepository` + `ShopJpaEntityMapper` 주입
  - Entity → Domain 변환: `mapper.toDomain(entity)` 위임

- **Repository**: `ShopQueryDslRepository` (`@Repository`)
  - `JPAQueryFactory` + `ShopConditionBuilder` 기반 동적 쿼리

- **ConditionBuilder**: `ShopConditionBuilder` (`@Component`)
  - `notDeleted()`: `shop.deletedAt.isNull()`
  - `statusIn(criteria)`: `shop.status.in(statusNames)` (ShopStatus enum → String 변환)
  - `searchCondition(criteria)`: 검색 필드 기반 LIKE 조건
    - `searchField == null`: `shop.shopName.like(%) OR shop.accountId.like(%)`
    - `SHOP_NAME`: `shop.shopName.like(%)`
    - `ACCOUNT_ID`: `shop.accountId.like(%)`

- **JPA Entity**: `ShopJpaEntity` (`@Entity`, `@Table(name = "shop")`)
  - `extends SoftDeletableEntity` (createdAt, updatedAt, deletedAt)
  - `id` (PK, AUTO_INCREMENT), `sales_channel_id`, `shop_name`, `account_id`, `status`

- **Database Query**:
  ```sql
  -- findByCriteria (목록 조회)
  SELECT *
  FROM shop
  WHERE deleted_at IS NULL
    AND status IN (?)                  -- statuses 필터 (선택)
    AND (shop_name LIKE ? OR account_id LIKE ?)  -- 검색어 (선택, searchField 없을 때 전체)
    AND shop_name LIKE ?               -- searchField = SHOP_NAME 일 때
  ORDER BY created_at DESC             -- sortKey + sortDirection
  LIMIT ? OFFSET ?

  -- countByCriteria (총 개수)
  SELECT COUNT(*)
  FROM shop
  WHERE deleted_at IS NULL
    AND status IN (?)
    AND shop_name LIKE ?
  ```

---

## 2. POST /api/v1/market/shops - 외부몰 등록

### 호출 흐름 다이어그램

```
[Adapter-In]
ShopCommandController.registerShop(@Valid @RequestBody RegisterShopApiRequest)
  ├─ ShopCommandApiMapper.toCommand(request)                 [Request → Command 변환]
  │   └─ new RegisterShopCommand(salesChannelId, shopName, accountId)
  └─ RegisterShopUseCase.execute(command)                    [Port Interface]
      └─ → Long shopId (등록된 ID 반환)

[Application]
RegisterShopService.execute(RegisterShopCommand)             [UseCase 구현체]
  ├─ ShopValidator.validateAccountNotDuplicate(salesChannelId, accountId)
  │   └─ ShopReadManager.existsBySalesChannelIdAndAccountId()
  │       └─ ShopQueryPort.existsBySalesChannelIdAndAccountId()  [Port]
  │           └─ 중복이면 ShopAccountIdDuplicateException (SHP-003, 409)
  ├─ ShopCommandFactory.create(command)                      [Domain 객체 생성]
  │   ├─ TimeProvider.now()                                  [현재 시각 취득]
  │   └─ Shop.forNew(salesChannelId, shopName, accountId, now)
  │       ├─ ShopId.forNew()                                 [ID는 null - DB에서 할당]
  │       ├─ ShopName.of(shopName)                           [1~100자 검증]
  │       ├─ AccountId.of(accountId)                         [1~100자 검증]
  │       ├─ ShopStatus.ACTIVE                               [신규는 ACTIVE 고정]
  │       └─ DeletionStatus.active()                         [deletedAt = null]
  └─ ShopWriteManager.persist(shop)                          [@Transactional 경계]
      └─ ShopCommandPort.persist(shop)                       [Domain Port]

[Domain Port]
ShopCommandPort.persist(Shop)                                [Port 인터페이스]

[Adapter-Out]
ShopCommandAdapter.persist(Shop)                             [Port 구현체]
  ├─ ShopJpaEntityMapper.toEntity(shop)                      [Domain → Entity 변환]
  │   └─ ShopJpaEntity.create(null, salesChannelId, shopName, accountId, "ACTIVE", now, now, null)
  └─ ShopJpaRepository.save(entity)                          [Spring Data JPA]
      └─ → saved.getId()                                     [DB 할당 ID 반환]

[Database]
- INSERT INTO shop
```

### Layer별 상세

#### Adapter-In Layer

- **Controller**: `ShopCommandController`
  - `@PostMapping` + `@PreAuthorize("@access.superAdmin()")` + `@RequirePermission("shop:write")`
  - Method: `registerShop(@Valid @RequestBody RegisterShopApiRequest)`
  - Response: `ResponseEntity<ApiResponse<ShopIdApiResponse>>` (HTTP 201 CREATED)

- **Request DTO**: `RegisterShopApiRequest` (Record)
  ```java
  record RegisterShopApiRequest(
      @NotNull Long salesChannelId,   // 판매채널 ID (필수)
      @NotBlank String shopName,      // 외부몰명 (필수)
      @NotBlank String accountId      // 계정 ID (필수)
  )
  ```

- **Response DTO**: `ShopIdApiResponse` (Record)
  ```java
  record ShopIdApiResponse(Long shopId) {
      static ShopIdApiResponse of(Long shopId) { ... }
  }
  ```

- **ApiMapper**: `ShopCommandApiMapper`
  - `toCommand(RegisterShopApiRequest)` → `RegisterShopCommand`
    - 단순 필드 복사 (1:1 매핑)

#### Application Layer

- **UseCase Interface**: `RegisterShopUseCase`
  ```java
  interface RegisterShopUseCase {
      Long execute(RegisterShopCommand command);
  }
  ```

- **Service 구현**: `RegisterShopService` (`@Service`)
  1. `ShopValidator.validateAccountNotDuplicate()`: 동일 판매채널 내 accountId 중복 검증
  2. `ShopCommandFactory.create(command)`: `Shop` 도메인 객체 생성
  3. `ShopWriteManager.persist(shop)`: 영속화
  4. `DataIntegrityViolationException` 캐치: `uq_sc_account` 유니크 제약 위반 시 `ShopAccountIdDuplicateException` 재발생 (Race Condition 방어)

- **Command DTO**: `RegisterShopCommand` (Record)
  ```java
  record RegisterShopCommand(Long salesChannelId, String shopName, String accountId)
  ```

- **Factory**: `ShopCommandFactory` (`@Component`)
  - `TimeProvider.now()` 호출 (APP-TIM-001: Factory에서만 시간 취득)
  - `Shop.forNew(salesChannelId, shopName, accountId, now)` 호출

- **Validator**: `ShopValidator` (`@Component`)
  - `validateAccountNotDuplicate(Long, String)`: `ShopReadManager.existsBySalesChannelIdAndAccountId()` 호출
  - 중복 시 `ShopAccountIdDuplicateException` (에러코드: SHP-003, HTTP 409)

- **Manager**: `ShopWriteManager` (`@Component`)
  - `@Transactional`: 트랜잭션 경계 (쓰기)
  - `ShopCommandPort.persist(shop)` → `Long` 반환

#### Domain Layer

- **Port**: `ShopCommandPort`
  ```java
  interface ShopCommandPort {
      Long persist(Shop shop);
  }
  ```

- **Aggregate**: `Shop`
  - `Shop.forNew(salesChannelId, shopName, accountId, now)`:
    - `ShopId.forNew()`: ID = null (DB AUTO_INCREMENT)
    - `ShopName.of(shopName)`: 1~100자 검증, 공백 trim
    - `AccountId.of(accountId)`: 1~100자 검증, 공백 trim
    - `ShopStatus.ACTIVE`: 신규 등록 시 항상 ACTIVE
    - `DeletionStatus.active()`: deletedAt = null

- **Value Objects**:
  - `ShopName`: 1~100자, null/blank 불가
  - `AccountId`: 1~100자, null/blank 불가
  - `ShopStatus.ACTIVE`: 신규 고정값

- **Domain Exceptions**:
  - `ShopAccountIdDuplicateException`: SHP-003, HTTP 409

#### Adapter-Out Layer

- **Adapter**: `ShopCommandAdapter` (`@Component`, `implements ShopCommandPort`)
  - `ShopJpaEntityMapper.toEntity(shop)` → `ShopJpaEntity`
  - `ShopJpaRepository.save(entity)` → saved entity
  - `saved.getId()` 반환

- **Mapper**: `ShopJpaEntityMapper` (`@Component`)
  - `toEntity(Shop)` → `ShopJpaEntity.create(id, salesChannelId, shopName, accountId, status, createdAt, updatedAt, deletedAt)`
    - 신규 등록: `id = null` (AUTO_INCREMENT 위임)
    - `status`: `ShopStatus.name()` (ACTIVE/INACTIVE 문자열)

- **Repository**: `ShopJpaRepository` (`extends JpaRepository<ShopJpaEntity, Long>`)
  - `save(entity)`: JPA `persist` 또는 `merge` 처리
  - DB AUTO_INCREMENT로 ID 할당

- **Database Query**:
  ```sql
  INSERT INTO shop (
      sales_channel_id,
      shop_name,
      account_id,
      status,
      created_at,
      updated_at,
      deleted_at
  ) VALUES (?, ?, ?, 'ACTIVE', ?, ?, NULL)

  -- UniqueKey: uq_sc_account (sales_channel_id, account_id)
  ```

---

## 3. PUT /api/v1/market/shops/{shopId} - 외부몰 수정

### 호출 흐름 다이어그램

```
[Adapter-In]
ShopCommandController.updateShop(@PathVariable Long shopId, @Valid @RequestBody UpdateShopApiRequest)
  ├─ ShopCommandApiMapper.toCommand(shopId, request)         [Request → Command 변환]
  │   └─ new UpdateShopCommand(shopId, shopName, accountId, status)
  └─ UpdateShopUseCase.execute(command)                      [Port Interface]

[Application]
UpdateShopService.execute(UpdateShopCommand)                 [UseCase 구현체]
  ├─ ShopCommandFactory.createUpdateContext(command)         [UpdateContext 생성]
  │   ├─ ShopId.of(command.shopId())                         [ID VO 변환]
  │   ├─ ShopStatus.fromString(command.status())             [상태 문자열 → enum]
  │   ├─ ShopUpdateData.of(shopName, accountId, status)      [수정 데이터 묶음]
  │   ├─ TimeProvider.now()                                  [변경 시각 취득]
  │   └─ new UpdateContext<>(shopId, updateData, changedAt)
  ├─ ShopValidator.findExistingOrThrow(shopId)               [존재 확인 + Domain 객체 취득]
  │   └─ ShopReadManager.getById(shopId)
  │       └─ ShopQueryPort.findById(shopId)                  [Port]
  │           └─ 없으면 ShopNotFoundException (SHP-001, 404)
  ├─ ShopValidator.validateAccountNotDuplicateExcluding(salesChannelId, accountId, shopId)
  │   └─ ShopReadManager.existsBySalesChannelIdAndAccountIdExcluding()
  │       └─ ShopQueryPort.existsBySalesChannelIdAndAccountIdExcluding()  [Port]
  │           └─ 중복이면 ShopAccountIdDuplicateException (SHP-003, 409)
  ├─ shop.update(updateData, changedAt)                      [Domain 업데이트 로직]
  │   ├─ ShopName.of(updateData.shopName())                  [VO 재생성 + 검증]
  │   ├─ AccountId.of(updateData.accountId())                [VO 재생성 + 검증]
  │   ├─ this.status = updateData.status()
  │   └─ this.updatedAt = changedAt
  └─ ShopWriteManager.persist(shop)                          [@Transactional 경계]
      └─ ShopCommandPort.persist(shop)                       [Domain Port]

[Domain Port]
ShopQueryPort.findById(ShopId)                               [존재 확인용]
ShopQueryPort.existsBySalesChannelIdAndAccountIdExcluding()  [중복 확인용]
ShopCommandPort.persist(Shop)                                [저장용]

[Adapter-Out]
ShopQueryAdapter.findById(ShopId)                            [Port 구현체]
  └─ ShopQueryDslRepository.findById(id.value())
      ├─ conditionBuilder.idEq(id)                           [id = ?]
      ├─ conditionBuilder.notDeleted()                       [deleted_at IS NULL]
      └─ ShopJpaEntityMapper.toDomain(entity)

ShopQueryAdapter.existsBySalesChannelIdAndAccountIdExcluding()
  └─ ShopQueryDslRepository.existsBySalesChannelIdAndAccountIdExcluding(salesChannelId, accountId, excludeId)
      ├─ conditionBuilder.salesChannelIdEq(salesChannelId)
      ├─ conditionBuilder.accountIdEq(accountId)
      ├─ conditionBuilder.idNe(excludeId)                    [id != ?]
      └─ conditionBuilder.notDeleted()

ShopCommandAdapter.persist(Shop)                             [Port 구현체]
  ├─ ShopJpaEntityMapper.toEntity(shop)                      [Domain → Entity 변환]
  │   └─ ShopJpaEntity.create(id, salesChannelId, shopName, accountId, status, createdAt, updatedAt, null)
  └─ ShopJpaRepository.save(entity)                          [JPA merge (ID 있으므로)]

[Database]
- SELECT FROM shop (존재 확인 쿼리)
- SELECT COUNT FROM shop (중복 확인 쿼리)
- UPDATE shop (수정 쿼리)
```

### Layer별 상세

#### Adapter-In Layer

- **Controller**: `ShopCommandController`
  - `@PutMapping(ShopAdminEndpoints.SHOP_ID)` = `/{shopId}`
  - `@PreAuthorize("@access.superAdmin()")` + `@RequirePermission("shop:write")`
  - Method: `updateShop(@PathVariable Long shopId, @Valid @RequestBody UpdateShopApiRequest)`
  - Response: `ResponseEntity<Void>` (HTTP 204 NO CONTENT)

- **Request DTO**: `UpdateShopApiRequest` (Record)
  ```java
  record UpdateShopApiRequest(
      @NotBlank String shopName,          // 외부몰명 (필수)
      @NotBlank String accountId,         // 계정 ID (필수)
      @NotBlank
      @Pattern(regexp = "ACTIVE|INACTIVE")
      String status                       // 상태 (필수, ACTIVE|INACTIVE)
  )
  ```

- **ApiMapper**: `ShopCommandApiMapper`
  - `toCommand(Long shopId, UpdateShopApiRequest)` → `UpdateShopCommand`
    - shopId는 PathVariable에서, 나머지 필드는 RequestBody에서 수집

#### Application Layer

- **UseCase Interface**: `UpdateShopUseCase`
  ```java
  interface UpdateShopUseCase {
      void execute(UpdateShopCommand command);
  }
  ```

- **Service 구현**: `UpdateShopService` (`@Service`)
  - 처리 순서:
    1. `ShopCommandFactory.createUpdateContext(command)`: `UpdateContext<ShopId, ShopUpdateData>` 생성
    2. `ShopValidator.findExistingOrThrow(shopId)`: Shop 존재 확인 및 Domain 객체 반환
    3. `ShopValidator.validateAccountNotDuplicateExcluding(salesChannelId, accountId, shopId)`: 자기 자신 제외 accountId 중복 확인
    4. `shop.update(updateData, changedAt)`: Domain 객체 상태 변경
    5. `ShopWriteManager.persist(shop)`: 변경된 Domain 객체 영속화

- **Command DTO**: `UpdateShopCommand` (Record)
  ```java
  record UpdateShopCommand(Long shopId, String shopName, String accountId, String status)
  ```

- **UpdateContext**: `UpdateContext<ShopId, ShopUpdateData>` (Generic Record)
  ```java
  record UpdateContext<ID, DATA>(ID id, DATA updateData, Instant changedAt)
  ```
  - FAC-008 규칙: `createUpdateContext()`로 ID, UpdateData, changedAt 한 번에 생성

- **Factory**: `ShopCommandFactory` (`@Component`)
  - `createUpdateContext(UpdateShopCommand)`:
    - `ShopId.of(command.shopId())`: ShopId VO 생성
    - `ShopStatus.fromString(command.status())`: 문자열 → enum 변환
    - `ShopUpdateData.of(shopName, accountId, status)`: 수정 데이터 묶음
    - `TimeProvider.now()`: 변경 시각 (APP-TIM-001 준수)

- **Validator**: `ShopValidator` (`@Component`)
  - `findExistingOrThrow(ShopId)`: `ShopReadManager.getById()` → 없으면 `ShopNotFoundException`
  - `validateAccountNotDuplicateExcluding(salesChannelId, accountId, ShopId)`: 자기 자신 제외 중복 체크

#### Domain Layer

- **Aggregate**: `Shop`
  - `update(ShopUpdateData updateData, Instant now)`:
    - `this.shopName = ShopName.of(updateData.shopName())` (재검증 포함)
    - `this.accountId = AccountId.of(updateData.accountId())` (재검증 포함)
    - `this.status = updateData.status()`
    - `this.updatedAt = now`

- **Value Object**: `ShopUpdateData` (Record)
  ```java
  record ShopUpdateData(String shopName, String accountId, ShopStatus status) {
      static ShopUpdateData of(String shopName, String accountId, ShopStatus status)
  }
  ```

- **Domain Exceptions**:
  - `ShopNotFoundException`: SHP-001, HTTP 404 (존재하지 않는 Shop 조회)
  - `ShopAccountIdDuplicateException`: SHP-003, HTTP 409 (accountId 중복)

#### Adapter-Out Layer

- **Adapter**: `ShopQueryAdapter` (존재 확인 + 중복 확인용)
  - `findById(ShopId)`: `ShopQueryDslRepository.findById(id.value())` + `mapper.toDomain(entity)`
  - `existsBySalesChannelIdAndAccountIdExcluding(salesChannelId, accountId, excludeId)`: 자기 자신 제외 중복 조회

- **Adapter**: `ShopCommandAdapter` (저장용)
  - `persist(Shop)`: `mapper.toEntity(shop)` → `repository.save(entity)` → `saved.getId()`

- **Mapper**: `ShopJpaEntityMapper`
  - `toEntity(Shop)`: 기존 ID가 있으면 JPA merge 동작 → UPDATE 쿼리 발생

- **Database Query**:
  ```sql
  -- 존재 확인
  SELECT *
  FROM shop
  WHERE id = ?
    AND deleted_at IS NULL

  -- 중복 확인 (자기 자신 제외)
  SELECT 1
  FROM shop
  WHERE sales_channel_id = ?
    AND account_id = ?
    AND id != ?
    AND deleted_at IS NULL
  LIMIT 1

  -- 수정 (JPA merge)
  UPDATE shop
  SET shop_name = ?,
      account_id = ?,
      status = ?,
      updated_at = ?
  WHERE id = ?
  ```

---

## 공통 패턴 분석

### 1. Hexagonal Architecture 레이어 격리

| 레이어 | DTO/모델 타입 | 책임 |
|--------|--------------|------|
| **Adapter-In** | `ApiRequest` / `ApiResponse` (Record) | HTTP 계층 관심사 (Validation, 직렬화, 날짜 포맷) |
| **Application** | `Command` / `Params` / `Result` (Record) | 유스케이스 조율, 트랜잭션 경계 |
| **Domain** | `Aggregate`, `VO`, `Criteria`, `UpdateData` | 비즈니스 규칙, 불변성 보장 |
| **Adapter-Out** | `JpaEntity` | 영속화 기술 관심사 (JPA, QueryDSL) |

### 2. CQRS 분리

- **Query**: `ShopQueryController` → `ShopQueryApiMapper` → `SearchShopByOffsetUseCase` → `SearchShopByOffsetService`
- **Command**: `ShopCommandController` → `ShopCommandApiMapper` → `RegisterShopUseCase` / `UpdateShopUseCase` → `RegisterShopService` / `UpdateShopService`

### 3. 트랜잭션 경계

| 계층 | @Transactional 위치 | 비고 |
|------|---------------------|------|
| Adapter-In (Controller) | 금지 | API-CTR-005 |
| Application Service | 금지 | Manager에 위임 |
| **ShopReadManager** | `@Transactional(readOnly = true)` | 모든 조회 메서드 |
| **ShopWriteManager** | `@Transactional` | `persist()` 메서드 |
| Adapter-Out | 금지 | |

### 4. 변환 체인

```
[Query 흐름]
SearchShopsApiRequest
  → (ShopQueryApiMapper) → ShopSearchParams
  → (ShopQueryFactory) → ShopSearchCriteria
  → (QueryDSL) → ShopJpaEntity
  → (ShopJpaEntityMapper) → Shop
  → (ShopAssembler/ShopResult.from) → ShopResult
  → (ShopQueryApiMapper) → ShopApiResponse

[Command 흐름 - 등록]
RegisterShopApiRequest
  → (ShopCommandApiMapper) → RegisterShopCommand
  → (ShopCommandFactory) → Shop.forNew()
  → (ShopJpaEntityMapper) → ShopJpaEntity
  → (JpaRepository.save) → Long shopId

[Command 흐름 - 수정]
UpdateShopApiRequest + shopId(PathVariable)
  → (ShopCommandApiMapper) → UpdateShopCommand
  → (ShopCommandFactory) → UpdateContext<ShopId, ShopUpdateData>
  → (ShopValidator) → Shop (기존 Domain 객체 로드)
  → (shop.update) → 상태 변경된 Shop
  → (ShopJpaEntityMapper) → ShopJpaEntity
  → (JpaRepository.save) → DB UPDATE
```

### 5. Validator 패턴 (APP-VAL-001)

- **등록 시**: `validateAccountNotDuplicate()` - 사전 중복 체크
- **수정 시**: `findExistingOrThrow()` - 존재 확인 + Domain 객체 반환, `validateAccountNotDuplicateExcluding()` - 자기 자신 제외 중복 체크
- **Race Condition 방어**: DB UniqueKey(`uq_sc_account`) 위반 시 `DataIntegrityViolationException` 캐치 → `ShopAccountIdDuplicateException` 변환

### 6. Factory 패턴 (APP-TIM-001, FAC-008)

- `TimeProvider.now()`: Service가 아닌 Factory에서만 호출 (시간 일관성 보장)
- `ShopCommandFactory.create()`: 등록 시 `Shop.forNew()` 생성
- `ShopCommandFactory.createUpdateContext()`: 수정 시 `UpdateContext<ShopId, ShopUpdateData>` 생성 (ID + UpdateData + changedAt 한 번에 묶음)

### 7. 소프트 삭제 전략

- `ShopJpaEntity extends SoftDeletableEntity`: `deleted_at` 컬럼 관리
- `ShopConditionBuilder.notDeleted()`: 모든 조회에 `deleted_at IS NULL` 조건 자동 적용
- `Shop.delete(now)`: 소프트 삭제 메서드 존재하나 현재 API에서 미노출 (INACTIVE 상태 변경으로 대체)

### 8. 에러 코드 체계

| 코드 | HTTP 상태 | 설명 | 발생 시점 |
|------|-----------|------|-----------|
| SHP-001 | 404 | 외부몰을 찾을 수 없습니다 | `updateShop` - 존재하지 않는 shopId |
| SHP-003 | 409 | 해당 판매채널에 이미 존재하는 계정 ID | `registerShop` / `updateShop` - accountId 중복 |

---

## 파일 위치 참조

### Adapter-In
| 파일 | 경로 |
|------|------|
| `ShopAdminEndpoints` | `adapter-in/rest-api/.../shop/ShopAdminEndpoints.java` |
| `ShopQueryController` | `adapter-in/rest-api/.../shop/controller/ShopQueryController.java` |
| `ShopCommandController` | `adapter-in/rest-api/.../shop/controller/ShopCommandController.java` |
| `ShopQueryApiMapper` | `adapter-in/rest-api/.../shop/mapper/ShopQueryApiMapper.java` |
| `ShopCommandApiMapper` | `adapter-in/rest-api/.../shop/mapper/ShopCommandApiMapper.java` |
| `SearchShopsApiRequest` | `adapter-in/rest-api/.../shop/dto/query/SearchShopsApiRequest.java` |
| `RegisterShopApiRequest` | `adapter-in/rest-api/.../shop/dto/command/RegisterShopApiRequest.java` |
| `UpdateShopApiRequest` | `adapter-in/rest-api/.../shop/dto/command/UpdateShopApiRequest.java` |
| `ShopApiResponse` | `adapter-in/rest-api/.../shop/dto/response/ShopApiResponse.java` |
| `ShopIdApiResponse` | `adapter-in/rest-api/.../shop/dto/response/ShopIdApiResponse.java` |

### Application
| 파일 | 경로 |
|------|------|
| `SearchShopByOffsetUseCase` | `application/.../shop/port/in/query/SearchShopByOffsetUseCase.java` |
| `RegisterShopUseCase` | `application/.../shop/port/in/command/RegisterShopUseCase.java` |
| `UpdateShopUseCase` | `application/.../shop/port/in/command/UpdateShopUseCase.java` |
| `ShopQueryPort` | `application/.../shop/port/out/query/ShopQueryPort.java` |
| `ShopCommandPort` | `application/.../shop/port/out/command/ShopCommandPort.java` |
| `SearchShopByOffsetService` | `application/.../shop/service/query/SearchShopByOffsetService.java` |
| `RegisterShopService` | `application/.../shop/service/command/RegisterShopService.java` |
| `UpdateShopService` | `application/.../shop/service/command/UpdateShopService.java` |
| `ShopReadManager` | `application/.../shop/manager/ShopReadManager.java` |
| `ShopWriteManager` | `application/.../shop/manager/ShopWriteManager.java` |
| `ShopCommandFactory` | `application/.../shop/factory/ShopCommandFactory.java` |
| `ShopQueryFactory` | `application/.../shop/factory/ShopQueryFactory.java` |
| `ShopValidator` | `application/.../shop/validator/ShopValidator.java` |
| `ShopAssembler` | `application/.../shop/assembler/ShopAssembler.java` |
| `RegisterShopCommand` | `application/.../shop/dto/command/RegisterShopCommand.java` |
| `UpdateShopCommand` | `application/.../shop/dto/command/UpdateShopCommand.java` |
| `ShopSearchParams` | `application/.../shop/dto/query/ShopSearchParams.java` |
| `ShopResult` | `application/.../shop/dto/response/ShopResult.java` |
| `ShopPageResult` | `application/.../shop/dto/response/ShopPageResult.java` |

### Domain
| 파일 | 경로 |
|------|------|
| `Shop` | `domain/.../shop/aggregate/Shop.java` |
| `ShopUpdateData` | `domain/.../shop/aggregate/ShopUpdateData.java` |
| `ShopId` | `domain/.../shop/id/ShopId.java` |
| `ShopName` | `domain/.../shop/vo/ShopName.java` |
| `AccountId` | `domain/.../shop/vo/AccountId.java` |
| `ShopStatus` | `domain/.../shop/vo/ShopStatus.java` |
| `ShopSearchCriteria` | `domain/.../shop/query/ShopSearchCriteria.java` |
| `ShopSearchField` | `domain/.../shop/query/ShopSearchField.java` |
| `ShopSortKey` | `domain/.../shop/query/ShopSortKey.java` |
| `ShopNotFoundException` | `domain/.../shop/exception/ShopNotFoundException.java` |
| `ShopAccountIdDuplicateException` | `domain/.../shop/exception/ShopAccountIdDuplicateException.java` |
| `ShopErrorCode` | `domain/.../shop/exception/ShopErrorCode.java` |

### Adapter-Out
| 파일 | 경로 |
|------|------|
| `ShopQueryAdapter` | `adapter-out/persistence-mysql/.../shop/adapter/ShopQueryAdapter.java` |
| `ShopCommandAdapter` | `adapter-out/persistence-mysql/.../shop/adapter/ShopCommandAdapter.java` |
| `ShopQueryDslRepository` | `adapter-out/persistence-mysql/.../shop/repository/ShopQueryDslRepository.java` |
| `ShopJpaRepository` | `adapter-out/persistence-mysql/.../shop/repository/ShopJpaRepository.java` |
| `ShopJpaEntityMapper` | `adapter-out/persistence-mysql/.../shop/mapper/ShopJpaEntityMapper.java` |
| `ShopJpaEntity` | `adapter-out/persistence-mysql/.../shop/entity/ShopJpaEntity.java` |
| `ShopConditionBuilder` | `adapter-out/persistence-mysql/.../shop/condition/ShopConditionBuilder.java` |

---

**분석 일시**: 2026-02-18
**분석 대상**: `/api/v1/market/shops` (3개 엔드포인트)
**총 엔드포인트**: 3개 (Query 1개, Command 2개)
