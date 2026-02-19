# ProductGroup Domain API Flow Analysis

ProductGroup 도메인의 전체 API 호출 흐름 분석 문서입니다.

---

## 엔드포인트 목록

| HTTP Method | Path | 설명 | Controller Method |
|-------------|------|------|-------------------|
| GET | `/api/v1/market/product-groups` | 상품 그룹 목록 조회 | `search()` |
| GET | `/api/v1/market/product-groups/{productGroupId}` | 상품 그룹 상세 조회 | `getById()` |
| POST | `/api/v1/market/product-groups` | 상품 그룹 단건 등록 | `registerProductGroup()` |
| POST | `/api/v1/market/product-groups/batch` | 상품 그룹 배치 등록 | `batchRegisterProductGroups()` |
| PUT | `/api/v1/market/product-groups/{productGroupId}` | 상품 그룹 전체 수정 | `updateProductGroupFull()` |
| PATCH | `/api/v1/market/product-groups/{productGroupId}/basic-info` | 상품 그룹 기본 정보 수정 | `updateBasicInfo()` |
| PATCH | `/api/v1/market/product-groups/status` | 상품 그룹 배치 상태 변경 | `batchChangeStatus()` |

---

## Q1. GET /api/v1/market/product-groups - 상품 그룹 목록 조회

### 호출 흐름 다이어그램

```
[Adapter-In]
ProductGroupQueryController.search(SearchProductGroupsApiRequest)
  ├─ ProductGroupQueryApiMapper.toSearchParams(request)           [Request 변환]
  │   └─ CommonSearchParams 조합 + ProductGroupSearchParams.of()
  ├─ SearchProductGroupByOffsetUseCase.execute(searchParams)      [Port Interface]
  └─ ProductGroupQueryApiMapper.toPageResponse(pageResult)        [Response 변환]
      └─ PageApiResponse.of(responses, page, size, totalElements)

[Application]
SearchProductGroupByOffsetService.execute(ProductGroupSearchParams)  [UseCase 구현]
  ├─ ProductGroupQueryFactory.createCriteria(params)             [Criteria 생성]
  │   └─ ProductGroupSearchCriteria (statuses, sellerIds, brandIds, categoryIds ...)
  ├─ ProductGroupReadFacade.getListBundle(criteria)              [@Transactional(readOnly)]
  │   ├─ ProductGroupCompositionReadManager.findCompositeByCriteria(criteria)
  │   │   └─ ProductGroupCompositionQueryPort.findCompositeByCriteria()
  │   ├─ ProductGroupCompositionReadManager.countByCriteria(criteria)
  │   │   └─ ProductGroupCompositionQueryPort.countByCriteria()
  │   ├─ List<Long> productGroupIds 추출
  │   └─ ProductGroupCompositionReadManager.findEnrichments(productGroupIds)
  │       └─ ProductGroupCompositionQueryPort.findEnrichmentsByProductGroupIds()
  └─ ProductGroupAssembler.toPageResult(bundle, page, size)      [Enrichment 조립]
      ├─ enrichComposites(baseComposites, enrichments)
      │   └─ base.withEnrichment(minPrice, maxPrice, maxDiscountRate, optionGroups)
      └─ ProductGroupPageResult.of(enrichedComposites, page, size, totalElements)

[Adapter-Out]
ProductGroupCompositionQueryAdapter                               [Port 구현]
  └─ ProductGroupCompositionQueryDslRepository

  [findCompositeByCriteria] → SELECT pg, seller, brand, category JOIN
  [countByCriteria]         → SELECT COUNT(*)
  [findEnrichmentsByProductGroupIds]
    ├─ SELECT product_group_id, MIN/MAX(currentPrice), MAX(discountRate) GROUP BY
    └─ SELECT optionGroup + optionValue IN (productGroupIds)

[Database]
- product_groups (기본 정보)
- seller (셀러명 JOIN)
- brand (브랜드명 JOIN)
- category (카테고리명, displayPath, depth, department, categoryGroup JOIN)
- product_group_images (THUMBNAIL URL)
- product (MIN/MAX 가격, MAX 할인율)
- seller_option_groups / seller_option_values (옵션 요약)
```

### Layer별 상세

#### Adapter-In Layer
- **Controller**: `ProductGroupQueryController`
  - Method: `search(@Valid SearchProductGroupsApiRequest request)`
  - Response: `ResponseEntity<ApiResponse<PageApiResponse<ProductGroupListApiResponse>>>`
  - HTTP Status: 200 OK
  - 권한: `@RequirePermission("product-group:read")`

- **Request DTO**: `SearchProductGroupsApiRequest` (Record)
  ```java
  record SearchProductGroupsApiRequest(
    List<String> statuses,           // DRAFT, ACTIVE, INACTIVE, SOLDOUT, DELETED
    List<Long> sellerIds,
    List<Long> brandIds,
    List<Long> categoryIds,
    List<Long> productGroupIds,
    String searchField,              // NAME, CATEGORY_NAME, BRAND_NAME
    String searchWord,
    String sortKey,                  // createdAt, updatedAt, name
    String sortDirection,            // ASC, DESC
    @Min(0) Integer page,
    @Min(1) @Max(100) Integer size
  )
  ```

- **Response DTO**: `ProductGroupListApiResponse` (Record)
  - id, sellerId, sellerName, brandId, brandName
  - categoryId, categoryName, categoryDisplayPath, categoryDepth, department, categoryGroup
  - productGroupName, optionType, status, thumbnailUrl
  - productCount, minPrice, maxPrice, maxDiscountRate
  - `List<OptionGroupSummaryApiResponse>` optionGroups
  - createdAt, updatedAt (ISO8601 문자열)

- **ApiMapper**: `ProductGroupQueryApiMapper`
  - `toSearchParams()`: page 기본값 0, size 기본값 20, CommonSearchParams 조합
  - `toPageResponse()`: List 변환 + PageApiResponse.of() 래핑

#### Application Layer
- **UseCase Interface**: `SearchProductGroupByOffsetUseCase`
  - `execute(ProductGroupSearchParams)` → `ProductGroupPageResult`

- **Service 구현**: `SearchProductGroupByOffsetService`
  - QueryFactory → Criteria 변환
  - ReadFacade → ListBundle 조회
  - Assembler → PageResult 조립

- **ReadFacade**: `ProductGroupReadFacade` (@Transactional readOnly)
  - **2-Step 조회 전략**:
    1. Composition 쿼리로 기본 정보 + 전체 건수 조회 (페이징 포함)
    2. productGroupIds 추출 → Enrichment 배치 조회 (가격/옵션 IN 쿼리 1회)
  - 빈 결과 조기 반환으로 불필요한 Enrichment 쿼리 방지

- **Assembler**: `ProductGroupAssembler`
  - `enrichComposites()`: Map 기반 productGroupId → Enrichment 매핑
  - `base.withEnrichment()`: minPrice, maxPrice, maxDiscountRate, optionGroups 적용

- **Result DTO**: `ProductGroupPageResult`
  ```java
  record ProductGroupPageResult(
    List<ProductGroupListCompositeResult> results,
    PageMeta pageMeta
  )
  ```

#### Domain Layer
- **Port**: `ProductGroupCompositionQueryPort`
  - `findCompositeByCriteria(ProductGroupSearchCriteria)` → `List<ProductGroupListCompositeResult>`
  - `countByCriteria(ProductGroupSearchCriteria)` → `long`
  - `findEnrichmentsByProductGroupIds(List<Long>)` → `List<ProductGroupEnrichmentResult>`

- **Criteria**: `ProductGroupSearchCriteria`
  - statuses, sellerIds, brandIds, categoryIds, productGroupIds
  - searchField, searchWord
  - queryContext (sortKey, sortDirection, page, size, offset)

#### Adapter-Out Layer
- **Adapter**: `ProductGroupCompositionQueryAdapter`
  - `ProductGroupCompositionQueryPort` 구현

- **Repository**: `ProductGroupCompositionQueryDslRepository`

- **Database Query**:
  ```sql
  -- findCompositeByCriteria: 기본 Composition 조회
  SELECT pg.id, pg.seller_id, s.seller_name, pg.brand_id, b.name_ko,
         pg.category_id, c.name_ko, c.display_path, c.depth, c.department, c.category_group,
         pg.product_group_name, pg.option_type, pg.status, pg.created_at, pg.updated_at
  FROM product_groups pg
  LEFT JOIN seller s ON pg.seller_id = s.id
  LEFT JOIN brand b ON pg.brand_id = b.id
  LEFT JOIN category c ON pg.category_id = c.id
  WHERE pg.status != 'DELETED'
    AND pg.seller_id IN (...)     -- 옵션: sellerIds
    AND pg.brand_id IN (...)      -- 옵션: brandIds
    AND pg.category_id IN (...)   -- 옵션: categoryIds
    AND pg.status IN (...)        -- 옵션: statuses
    AND pg.product_group_name LIKE ?  -- 옵션: searchField + searchWord
  ORDER BY pg.created_at DESC
  LIMIT 20 OFFSET 0

  -- findThumbnailUrls (배치): THUMBNAIL 이미지 URL 조회
  SELECT product_group_id, origin_url
  FROM product_group_images
  WHERE product_group_id IN (...) AND image_type = 'THUMBNAIL'

  -- countProductsByGroupIds (배치): 상품 수 집계
  SELECT product_group_id, COUNT(*)
  FROM product
  WHERE product_group_id IN (...)
  GROUP BY product_group_id

  -- countByCriteria
  SELECT COUNT(*) FROM product_groups WHERE ...

  -- findEnrichmentsByProductGroupIds: 가격 enrichment
  SELECT product_group_id, MIN(current_price), MAX(current_price), MAX(discount_rate)
  FROM product
  WHERE product_group_id IN (...)
  GROUP BY product_group_id

  -- 옵션 요약
  SELECT og.id, og.product_group_id, og.option_group_name
  FROM seller_option_groups og
  WHERE og.product_group_id IN (...)
  ORDER BY og.sort_order ASC

  SELECT ov.seller_option_group_id, ov.option_value_name
  FROM seller_option_values ov
  WHERE ov.seller_option_group_id IN (...)
  ORDER BY ov.sort_order ASC
  ```

---

## Q2. GET /api/v1/market/product-groups/{productGroupId} - 상품 그룹 상세 조회

### 호출 흐름 다이어그램

```
[Adapter-In]
ProductGroupQueryController.getById(Long productGroupId)
  ├─ GetProductGroupUseCase.execute(productGroupId)               [Port Interface]
  └─ ProductGroupQueryApiMapper.toDetailResponse(result)          [Response 변환]

[Application]
GetProductGroupService.execute(Long productGroupId)               [UseCase 구현]
  ├─ ProductGroupReadFacade.getDetailBundle(productGroupId)       [@Transactional(readOnly)]
  │   ├─ ProductGroupCompositionReadManager.getDetailCompositeById(productGroupId)
  │   │   └─ ProductGroupCompositionQueryPort.findDetailCompositeById()
  │   │       → [Not Found 시 ProductGroupNotFoundException]
  │   ├─ ProductGroupReadManager.getById(groupId)
  │   │   └─ ProductGroupQueryPort.findById()
  │   │       → ProductGroupQueryAdapter → 이미지 + 옵션 그룹 + 옵션 값 조회
  │   ├─ ProductReadManager.findByProductGroupId(groupId)
  │   │   └─ ProductQueryPort.findByProductGroupId()
  │   ├─ ProductGroupDescriptionReadManager.findByProductGroupId(groupId)
  │   │   └─ ProductGroupDescriptionQueryPort.findByProductGroupId()
  │   └─ ProductNoticeReadManager.findByProductGroupId(groupId)
  │       └─ ProductNoticeQueryPort.findByProductGroupId()
  └─ ProductGroupAssembler.toDetailResult(bundle)                 [Result 조립]
      ├─ images: group.images() → ProductGroupImageResult::from
      ├─ optionProductMatrix: buildOptionProductMatrix(group, products)
      │   ├─ optionGroups: group.sellerOptionGroups() → SellerOptionGroupResult::from
      │   └─ products: optionValueMap 기반 ResolvedProductOptionResult 매핑
      ├─ description: Optional<ProductGroupDescription> → ProductGroupDescriptionResult::from
      └─ notice: Optional<ProductNotice> → ProductNoticeResult::from

[Adapter-Out - Composition]
ProductGroupCompositionQueryAdapter
  └─ ProductGroupCompositionQueryDslRepository.findDetailCompositeById()
      → SELECT pg + seller + brand + category JOIN (기본 정보)
      → findShippingPolicy() : SELECT shipping_policy WHERE id = ?
      → findRefundPolicy()   : SELECT refund_policy WHERE id = ?

[Adapter-Out - ProductGroup Aggregate]
ProductGroupQueryAdapter
  ├─ queryDslRepository.findById(id)            → SELECT FROM product_groups
  ├─ queryDslRepository.findImagesByProductGroupId()  → SELECT FROM product_group_images
  ├─ queryDslRepository.findOptionGroupsByProductGroupId() → SELECT FROM seller_option_groups
  └─ queryDslRepository.findOptionValuesByOptionGroupIds() → SELECT FROM seller_option_values

[Adapter-Out - 연관 Aggregate]
- ProductQueryAdapter       → product (상품 목록)
- ProductGroupDescriptionQueryAdapter → product_group_descriptions
- ProductNoticeQueryAdapter → product_notices + product_notice_entries

[Database]
- product_groups (기본 정보)
- seller, brand, category (JOIN)
- shipping_policy, refund_policy (정책)
- product_group_images (이미지 목록)
- seller_option_groups (옵션 그룹)
- seller_option_values (옵션 값)
- product (상품 목록)
- product_group_descriptions + description_images (상세설명)
- product_notices + product_notice_entries (고시정보)
```

### Layer별 상세

#### Adapter-In Layer
- **Controller**: `ProductGroupQueryController`
  - Method: `getById(@PathVariable Long productGroupId)`
  - Response: `ResponseEntity<ApiResponse<ProductGroupDetailApiResponse>>`
  - HTTP Status: 200 OK, 404 NOT FOUND
  - 권한: `@RequirePermission("product-group:read")`

- **Response DTO**: `ProductGroupDetailApiResponse` (Record)
  - 기본 정보: id, sellerId, sellerName, brandId, brandName
  - categoryId, categoryName, categoryDisplayPath
  - productGroupName, optionType, status, createdAt, updatedAt (ISO8601)
  - `List<ProductGroupImageApiResponse>` images
  - `ProductOptionMatrixApiResponse` optionProductMatrix
    - `List<SellerOptionGroupApiResponse>` optionGroups
    - `List<ProductDetailApiResponse>` products
  - `ShippingPolicyApiResponse` shippingPolicy
  - `RefundPolicyApiResponse` refundPolicy
  - `ProductGroupDescriptionApiResponse` description
  - `ProductNoticeApiResponse` productNotice

- **ApiMapper**: `ProductGroupQueryApiMapper`
  - `toDetailResponse(ProductGroupDetailCompositeResult)` → `ProductGroupDetailApiResponse`
  - 중첩 매핑: 이미지, 옵션매트릭스, 정책, 상세설명, 고시정보 각각 변환
  - null 체크 후 옵션 필드 처리 (shippingPolicy, refundPolicy, description, notice)

#### Application Layer
- **UseCase Interface**: `GetProductGroupUseCase`
  - `execute(Long productGroupId)` → `ProductGroupDetailCompositeResult`

- **Service 구현**: `GetProductGroupService`
  - ReadFacade에서 번들 조회 후 Assembler로 조립

- **ReadFacade**: `ProductGroupReadFacade` (@Transactional readOnly)
  - **5개 조회로 상세 번들 구성**:
    1. Composition 쿼리 (pg + seller + brand + category + 정책 JOIN)
    2. ProductGroup Aggregate (이미지 + 옵션 구조)
    3. Product 목록
    4. ProductGroupDescription (Optional)
    5. ProductNotice (Optional)

- **Error Handling**:
  - `ProductGroupCompositionReadManager.getDetailCompositeById()`: not found → `ProductGroupNotFoundException`

#### Domain Layer
- **Ports**:
  - `ProductGroupCompositionQueryPort.findDetailCompositeById(Long)` → `Optional<ProductGroupDetailCompositeQueryResult>`
  - `ProductGroupQueryPort.findById(ProductGroupId)` → `Optional<ProductGroup>`
  - `ProductQueryPort.findByProductGroupId(ProductGroupId)` → `List<Product>`
  - `ProductGroupDescriptionQueryPort.findByProductGroupId(ProductGroupId)` → `Optional<ProductGroupDescription>`
  - `ProductNoticeQueryPort.findByProductGroupId(ProductGroupId)` → `Optional<ProductNotice>`

- **Aggregate**: `ProductGroup`
  - `images()`: `ProductGroupImages.toList()`
  - `sellerOptionGroups()`: `SellerOptionGroups.groups()`

#### Adapter-Out Layer
- **Adapters**:
  - `ProductGroupCompositionQueryAdapter`: Composition JOIN 쿼리
  - `ProductGroupQueryAdapter`: ProductGroup Aggregate (이미지 + 옵션 포함)
  - `ProductQueryAdapter`: Product 목록
  - `ProductGroupDescriptionQueryAdapter`: 상세설명
  - `ProductNoticeQueryAdapter`: 고시정보

- **Database Query**:
  ```sql
  -- findDetailCompositeById: 기본 정보 + 정책
  SELECT pg.*, s.seller_name, b.name_ko, c.name_ko, c.display_path,
         pg.shipping_policy_id, pg.refund_policy_id
  FROM product_groups pg
  LEFT JOIN seller s ON pg.seller_id = s.id
  LEFT JOIN brand b ON pg.brand_id = b.id
  LEFT JOIN category c ON pg.category_id = c.id
  WHERE pg.id = ? AND pg.status != 'DELETED'

  -- 정책 조회 (순차)
  SELECT * FROM shipping_policy WHERE id = ?
  SELECT * FROM refund_policy WHERE id = ?

  -- ProductGroup Aggregate
  SELECT * FROM product_groups WHERE id = ? AND status != 'DELETED'
  SELECT * FROM product_group_images WHERE product_group_id = ? ORDER BY sort_order ASC
  SELECT * FROM seller_option_groups WHERE product_group_id = ? ORDER BY sort_order ASC
  SELECT * FROM seller_option_values WHERE seller_option_group_id IN (...)
                                          ORDER BY sort_order ASC

  -- Product 목록
  SELECT * FROM product WHERE product_group_id = ? AND deleted_at IS NULL

  -- Description
  SELECT * FROM product_group_descriptions WHERE product_group_id = ?

  -- Notice
  SELECT * FROM product_notices WHERE product_group_id = ?
  SELECT * FROM product_notice_entries WHERE product_notice_id = ?
  ```

---

## C1. POST /api/v1/market/product-groups - 상품 그룹 단건 등록

### 호출 흐름 다이어그램

```
[Adapter-In]
ProductGroupCommandController.registerProductGroup(RegisterProductGroupApiRequest)
  ├─ ProductGroupCommandApiMapper.toCommand(request)              [Command 변환]
  │   └─ RegisterProductGroupCommand (중첩 ImageCommand, OptionGroupCommand, ProductCommand ...)
  └─ RegisterProductGroupFullUseCase.execute(command)             [Port Interface]
      → ProductGroupIdApiResponse.of(productGroupId)

[Application]
RegisterProductGroupFullService.execute(RegisterProductGroupCommand)  [UseCase 구현]
  ├─ ProductGroupBundleFactory.createProductGroupBundle(command)  [Bundle 생성]
  │   ├─ ProductGroup.forNew() → DRAFT 상태로 생성
  │   ├─ RegisterProductGroupImagesCommand 생성
  │   ├─ RegisterSellerOptionGroupsCommand 생성
  │   ├─ RegisterProductGroupDescriptionCommand 생성
  │   ├─ RegisterProductNoticeCommand 생성
  │   └─ RegisterProductsCommand 생성 (optionIndices 포함)
  └─ FullProductGroupRegistrationCoordinator.register(bundle)    [@Transactional]
      ├─ 1. ProductGroupCommandCoordinator.register(productGroup)
      │       ├─ ProductGroupValidator.validateForRegistration(productGroup)
      │       │   └─ ProductGroupValidateReadFacade.validateExternalReferences()
      │       │       (Seller, Brand, Category, ShippingPolicy, RefundPolicy 존재 검증)
      │       └─ ProductGroupCommandManager.persist(productGroup)
      │           └─ ProductGroupCommandPort.persist()            [Port]
      ├─ 2. bundle.bindAll(productGroupId)                        [productGroupId 바인딩]
      ├─ 3. ImageCommandCoordinator.register(imageCommand)
      │       (Factory + persist + 이미지 업로드 Outbox)
      ├─ 4. SellerOptionCommandCoordinator.register(optionGroupCommand)
      │       → List<SellerOptionValueId> allOptionValueIds 반환
      ├─ 5. DescriptionCommandCoordinator.register(descriptionCommand)
      │       (Factory + persist + 이미지 업로드 Outbox)
      ├─ 6. ProductNoticeCommandCoordinator.register(noticeCommand)
      │       (Factory + Validator + persist)
      ├─ 7. productCommandCoordinator.register(productCommand)
      │       (optionIndices → allOptionValueIdValues 매핑)
      └─ 8. ProductGroupInspectionOutboxCommandManager.persist()  [검수 Outbox 저장]

[Adapter-Out - ProductGroup]
ProductGroupCommandAdapter                                        [Port 구현]
  ├─ ProductGroupJpaEntityMapper.toEntity(productGroup)
  └─ ProductGroupJpaRepository.save(entity)                      [INSERT]

[Adapter-Out - 연관]
- ImageCommandAdapter            → product_group_images INSERT
- SellerOptionGroupCommandAdapter → seller_option_groups INSERT
- SellerOptionValueCommandAdapter → seller_option_values INSERT
- DescriptionCommandAdapter      → product_group_descriptions INSERT
- ProductNoticeCommandAdapter    → product_notices INSERT
- ProductNoticeEntryCommandAdapter → product_notice_entries INSERT
- ProductCommandAdapter          → product INSERT
- ProductOptionMappingCommandAdapter → product_option_mappings INSERT
- ProductGroupInspectionOutboxAdapter → product_group_inspection_outboxes INSERT

[Database]
- INSERT INTO product_groups
- INSERT INTO product_group_images
- INSERT INTO seller_option_groups
- INSERT INTO seller_option_values
- INSERT INTO product_group_descriptions
- INSERT INTO description_images (있는 경우)
- INSERT INTO product_notices
- INSERT INTO product_notice_entries
- INSERT INTO product (각 SKU별)
- INSERT INTO product_option_mappings (optionIndices 기반)
- INSERT INTO product_group_inspection_outboxes (PENDING, 스케줄러 비동기 처리)
```

### Layer별 상세

#### Adapter-In Layer
- **Controller**: `ProductGroupCommandController`
  - Method: `registerProductGroup(@Valid @RequestBody RegisterProductGroupApiRequest)`
  - Response: `ResponseEntity<ProductGroupIdApiResponse>` (201 Created)
  - 권한: `@PreAuthorize("hasAuthority('product-group:write')")` + `@RequirePermission`

- **Request DTO**: `RegisterProductGroupApiRequest` (Record)
  ```java
  record RegisterProductGroupApiRequest(
    @NotNull @Min(1) Long sellerId,
    @NotNull @Min(1) Long brandId,
    @NotNull @Min(1) Long categoryId,
    @NotNull @Min(1) Long shippingPolicyId,
    @NotNull @Min(1) Long refundPolicyId,
    @NotBlank @Size(max=200) String productGroupName,
    @NotBlank String optionType,
    @Valid List<ImageApiRequest> images,
    @Valid List<OptionGroupApiRequest> optionGroups,
    @Valid List<ProductApiRequest> products,
    @Valid DescriptionApiRequest description,
    @Valid NoticeApiRequest notice
  )
  ```
  - 중첩 Record: `ImageApiRequest`, `OptionGroupApiRequest`, `OptionValueApiRequest`,
    `ProductApiRequest`, `DescriptionApiRequest`, `NoticeApiRequest`, `NoticeEntryApiRequest`

- **Response DTO**: `ProductGroupIdApiResponse`
  ```java
  record ProductGroupIdApiResponse(Long productGroupId)
  ```

- **ApiMapper**: `ProductGroupCommandApiMapper`
  - `toCommand(RegisterProductGroupApiRequest)` → `RegisterProductGroupCommand`
  - 중첩 Command 변환: ImageCommand, OptionGroupCommand, OptionValueCommand, ProductCommand, DescriptionCommand, NoticeCommand, NoticeEntryCommand

#### Application Layer
- **UseCase Interface**: `RegisterProductGroupFullUseCase`
  - `execute(RegisterProductGroupCommand)` → `Long` (productGroupId)

- **Service 구현**: `RegisterProductGroupFullService`
  - `ProductGroupBundleFactory.createProductGroupBundle()` → Bundle 생성
  - `FullProductGroupRegistrationCoordinator.register()` → 전체 등록 조율

- **Factory**: `ProductGroupBundleFactory`
  - `TimeProvider.now()`로 현재 시각 획득
  - `ProductGroup.forNew()`: DRAFT 상태로 신규 ProductGroup 생성
  - per-package Command 변환 후 `ProductGroupRegistrationBundle` 반환

- **Coordinator**: `FullProductGroupRegistrationCoordinator` (@Transactional)
  - 등록 순서: ProductGroup → bindAll → Images → Options → Description → Notice → Products → InspectionOutbox
  - `bundle.bindAll(productGroupId)`: productGroupId를 per-package Command에 바인딩
  - SellerOptionCommandCoordinator 결과로 얻은 `allOptionValueIds`를 Product 등록 시 전달

- **Validator**: `ProductGroupValidator`
  - `validateForRegistration()`: 외부 FK 5개 (Seller, Brand, Category, ShippingPolicy, RefundPolicy) 존재 여부 검증

- **트랜잭션 경계**:
  - `FullProductGroupRegistrationCoordinator.register()`: 전체 등록 단일 트랜잭션
  - 내부 per-package Coordinator는 상위 트랜잭션에 참여

#### Domain Layer
- **Aggregate**: `ProductGroup`
  - `forNew(sellerId, brandId, categoryId, shippingPolicyId, refundPolicyId, productGroupName, optionType, now)`
  - 초기 상태: `ProductGroupStatus.DRAFT`
  - 이미지/옵션은 빈 목록으로 초기화

- **Port**: `ProductGroupCommandPort.persist(ProductGroup)` → `Long`

#### Adapter-Out Layer
- **Adapter**: `ProductGroupCommandAdapter`
  - `ProductGroupJpaEntityMapper.toEntity()`: Domain → Entity 변환
  - `ProductGroupJpaRepository.save()`: JPA INSERT

- **Entity**: `ProductGroupJpaEntity`
  - Table: `product_groups`
  - Fields: seller_id, brand_id, category_id, shipping_policy_id, refund_policy_id, product_group_name, option_type, status

- **Database Query**:
  ```sql
  INSERT INTO product_groups (
    seller_id, brand_id, category_id, shipping_policy_id, refund_policy_id,
    product_group_name, option_type, status, created_at, updated_at
  ) VALUES (?, ?, ?, ?, ?, ?, ?, 'DRAFT', ?, ?)
  ```

---

## C2. POST /api/v1/market/product-groups/batch - 상품 그룹 배치 등록

### 호출 흐름 다이어그램

```
[Adapter-In]
ProductGroupCommandController.batchRegisterProductGroups(BatchRegisterProductGroupApiRequest)
  ├─ ProductGroupCommandApiMapper.toCommands(request)             [Command 목록 변환]
  │   └─ List<RegisterProductGroupApiRequest>.stream().map(toCommand) → List<RegisterProductGroupCommand>
  └─ BatchRegisterProductGroupFullUseCase.execute(commands)       [Port Interface]
      → BatchProductGroupResultApiResponse.from(result)

[Application]
BatchRegisterProductGroupFullService.execute(List<RegisterProductGroupCommand>)  [UseCase 구현]
  ├─ commands.stream().map(cmd → CompletableFuture.supplyAsync(processOne, batchExecutor))
  └─ processOne(command):                                         [항목별 독립 트랜잭션]
      ├─ ProductGroupBundleFactory.createProductGroupBundle(command)
      └─ FullProductGroupRegistrationCoordinator.register(bundle) [@Transactional 독립]
          → 성공 시 BatchItemResult.success(productGroupId)
          → DomainException 시 BatchItemResult.failure(null, e.code(), e.getMessage())
          → 기타 Exception 시 BatchItemResult.failure(null, "INTERNAL_ERROR", ...)

  └─ CompletableFuture.join() 전체 대기
  └─ BatchProcessingResult.from(results)

[Adapter-Out]
C1과 동일 (항목별 독립 실행)

[Database]
C1과 동일 (항목별 독립 INSERT)
```

### Layer별 상세

#### Adapter-In Layer
- **Controller**: `ProductGroupCommandController`
  - Method: `batchRegisterProductGroups(@Valid @RequestBody BatchRegisterProductGroupApiRequest)`
  - Response: `ResponseEntity<BatchProductGroupResultApiResponse>` (200 OK)
  - 권한: `@PreAuthorize("hasAuthority('product-group:write')")` + `@RequirePermission`

- **Request DTO**: `BatchRegisterProductGroupApiRequest`
  ```java
  record BatchRegisterProductGroupApiRequest(
    @Valid @NotEmpty @Size(max=100) List<RegisterProductGroupApiRequest> items
  )
  ```

- **Response DTO**: `BatchProductGroupResultApiResponse`
  ```java
  record BatchProductGroupResultApiResponse(
    int totalCount,
    int successCount,
    int failureCount,
    List<BatchItemApiResponse> results
  )
  // BatchItemApiResponse: index, productGroupId, success, errorCode, errorMessage
  ```

#### Application Layer
- **UseCase Interface**: `BatchRegisterProductGroupFullUseCase`
  - `execute(List<RegisterProductGroupCommand>)` → `BatchProcessingResult<Long>`

- **Service 구현**: `BatchRegisterProductGroupFullService`
  - `@Transactional` 미적용 (항목별 독립 트랜잭션)
  - `@Qualifier("batchExecutor") ExecutorService`: Virtual Thread Executor 주입
  - `CompletableFuture.supplyAsync()`: 병렬 처리
  - `DomainException` 분기 처리: errorCode, errorMessage 수집
  - 배치 결과: 일부 실패해도 200 OK 반환

---

## C3. PUT /api/v1/market/product-groups/{productGroupId} - 상품 그룹 전체 수정

### 호출 흐름 다이어그램

```
[Adapter-In]
ProductGroupCommandController.updateProductGroupFull(productGroupId, UpdateProductGroupFullApiRequest)
  ├─ ProductGroupCommandApiMapper.toCommand(productGroupId, request) [Command 변환]
  │   └─ UpdateProductGroupFullCommand (productGroupId + 모든 수정 데이터)
  └─ UpdateProductGroupFullUseCase.execute(command)               [Port Interface]
      → ResponseEntity.noContent() (204)

[Application]
UpdateProductGroupFullService.execute(UpdateProductGroupFullCommand)  [UseCase 구현]
  ├─ ProductGroupBundleFactory.createUpdateBundle(command)        [Bundle 생성]
  │   ├─ ProductGroupUpdateData.of() (기본 정보 수정 VO)
  │   ├─ UpdateProductGroupImagesCommand
  │   ├─ UpdateSellerOptionGroupsCommand
  │   ├─ UpdateProductGroupDescriptionCommand
  │   ├─ UpdateProductNoticeCommand
  │   └─ List<ProductDiffUpdateEntry> (productId 기반 diff)
  └─ FullProductGroupUpdateCoordinator.update(bundle)             [@Transactional]
      ├─ 1. ProductGroupCommandCoordinator.update(basicInfoUpdateData)
      │       ├─ ProductGroupValidator.validateForUpdate(updateData)
      │       │   └─ Brand, Category, ShippingPolicy, RefundPolicy 존재 검증
      │       ├─ ProductGroupReadManager.getById(productGroupId)  [ProductGroup 조회]
      │       │   → [Not Found 시 ProductGroupNotFoundException]
      │       ├─ productGroup.update(updateData)                  [Domain 비즈니스 로직]
      │       │   → productGroupName, brandId, categoryId, shippingPolicyId, refundPolicyId 수정
      │       └─ ProductGroupCommandManager.persist(productGroup) [UPDATE]
      ├─ 2. ImageCommandCoordinator.update(imageCommand)          [이미지 교체]
      ├─ 3. SellerOptionCommandCoordinator.update(optionGroupCommand)
      │       → SellerOptionUpdateResult (ID 보존 diff 기반)
      ├─ 4. DescriptionCommandCoordinator.update(descriptionCommand)
      ├─ 5. ProductNoticeCommandCoordinator.update(noticeCommand)
      └─ 6. ProductCommandCoordinator.updateWithDiff(productGroupId, productEntries, optionResult)
              (productId 기반 diff 매칭 → 기존 상품 soft delete + 신규 상품 INSERT)

[Adapter-Out]
- ProductGroupCommandAdapter     → product_groups UPDATE
- ImageCommandAdapter            → product_group_images REPLACE
- SellerOptionGroupCommandAdapter → seller_option_groups UPDATE/INSERT
- SellerOptionValueCommandAdapter → seller_option_values UPDATE/INSERT
- DescriptionCommandAdapter      → product_group_descriptions UPDATE
- ProductCommandAdapter          → product soft delete + INSERT
- ProductOptionMappingCommandAdapter → product_option_mappings INSERT

[Database]
- UPDATE product_groups SET ... WHERE id = ?
- DELETE/INSERT product_group_images (이미지 교체)
- UPDATE/INSERT seller_option_groups, seller_option_values (diff 기반)
- UPDATE product_group_descriptions SET ... WHERE product_group_id = ?
- UPDATE product_notices SET ... WHERE product_group_id = ?
- UPDATE product SET deleted_at = ? WHERE product_group_id = ? (기존 상품 soft delete)
- INSERT INTO product (신규 상품)
```

### Layer별 상세

#### Adapter-In Layer
- **Controller**: `ProductGroupCommandController`
  - Method: `updateProductGroupFull(@PathVariable Long productGroupId, @Valid @RequestBody UpdateProductGroupFullApiRequest)`
  - Response: `ResponseEntity<Void>` (204 No Content)
  - 권한: `@PreAuthorize("@access.isSellerOwnerOr(#productGroupId, 'product-group:write')")`
    - 소유 셀러 또는 admin write 권한 필요

- **Request DTO**: `UpdateProductGroupFullApiRequest`
  - C1의 `RegisterProductGroupApiRequest`와 유사하나 `sellerOptionGroupId`, `sellerOptionValueId`, `productId` 포함

#### Application Layer
- **UseCase Interface**: `UpdateProductGroupFullUseCase`
  - `execute(UpdateProductGroupFullCommand)` → `void`

- **Service 구현**: `UpdateProductGroupFullService`
  - `ProductGroupBundleFactory.createUpdateBundle()` → `ProductGroupUpdateBundle`
  - `FullProductGroupUpdateCoordinator.update()` → 전체 수정 조율

- **Coordinator**: `FullProductGroupUpdateCoordinator` (@Transactional)
  - **6단계 수정**: ProductGroup 기본 정보 → Images → Options → Description → Notice → Products
  - Option 수정 결과(`SellerOptionUpdateResult`)를 Product 수정에 전달 (ID 보존)

- **Key 비즈니스 로직**:
  - `productGroup.update(updateData)`: Domain Aggregate의 `update()` 호출
    - productGroupName, brandId, categoryId, shippingPolicyId, refundPolicyId, updatedAt 변경
  - Product diff 전략: `productId` 기반 매칭 → 기존 상품 soft delete + 신규 INSERT

- **트랜잭션**: `FullProductGroupUpdateCoordinator.update()` 단일 트랜잭션

#### Domain Layer
- **Aggregate**: `ProductGroup.update(ProductGroupUpdateData)`
  ```java
  public void update(ProductGroupUpdateData updateData) {
      this.productGroupName = updateData.productGroupName();
      this.brandId = updateData.brandId();
      this.categoryId = updateData.categoryId();
      this.shippingPolicyId = updateData.shippingPolicyId();
      this.refundPolicyId = updateData.refundPolicyId();
      this.updatedAt = updateData.updatedAt();
  }
  ```

---

## C4. PATCH /api/v1/market/product-groups/{productGroupId}/basic-info - 기본 정보 수정

### 호출 흐름 다이어그램

```
[Adapter-In]
ProductGroupCommandController.updateBasicInfo(productGroupId, UpdateProductGroupBasicInfoApiRequest)
  ├─ ProductGroupCommandApiMapper.toCommand(productGroupId, request) [Command 변환]
  │   └─ UpdateProductGroupBasicInfoCommand (productGroupId, name, brandId, categoryId, policyIds)
  └─ UpdateProductGroupBasicInfoUseCase.execute(command)           [Port Interface]
      → ResponseEntity.noContent() (204)

[Application]
UpdateProductGroupBasicInfoService.execute(UpdateProductGroupBasicInfoCommand)  [UseCase 구현]
  ├─ ProductGroupCommandFactory.createUpdateData(command)          [UpdateData 생성]
  │   └─ ProductGroupUpdateData.of(productGroupId, name, brandId, categoryId, shippingPolicyId, refundPolicyId, now)
  └─ ProductGroupCommandCoordinator.update(updateData)             [@Transactional]
      ├─ ProductGroupValidator.validateForUpdate(updateData)
      │   └─ Brand, Category, ShippingPolicy, RefundPolicy 존재 검증
      ├─ ProductGroupReadManager.getById(updateData.productGroupId())
      │   └─ ProductGroupQueryPort.findById()
      │       → [Not Found 시 ProductGroupNotFoundException]
      ├─ productGroup.update(updateData)                           [Domain 비즈니스 로직]
      └─ ProductGroupCommandManager.persist(productGroup)
          └─ ProductGroupCommandPort.persist()                     [Port]

[Adapter-Out]
ProductGroupCommandAdapter
  └─ ProductGroupJpaRepository.save(entity)                        [UPDATE]

[Database]
UPDATE product_groups SET
  product_group_name = ?, brand_id = ?, category_id = ?,
  shipping_policy_id = ?, refund_policy_id = ?, updated_at = ?
WHERE id = ?
```

### Layer별 상세

#### Adapter-In Layer
- **Controller**: `ProductGroupCommandController`
  - Method: `updateBasicInfo(@PathVariable Long productGroupId, @Valid @RequestBody UpdateProductGroupBasicInfoApiRequest)`
  - Response: `ResponseEntity<Void>` (204 No Content)
  - 권한: `@PreAuthorize("@access.isSellerOwnerOr(#productGroupId, 'product-group:write')")`

- **Request DTO**: `UpdateProductGroupBasicInfoApiRequest`
  ```java
  record UpdateProductGroupBasicInfoApiRequest(
    @NotBlank @Size(max=200) String productGroupName,
    @NotNull @Min(1) Long brandId,
    @NotNull @Min(1) Long categoryId,
    @NotNull @Min(1) Long shippingPolicyId,
    @NotNull @Min(1) Long refundPolicyId
  )
  ```

#### Application Layer
- **UseCase Interface**: `UpdateProductGroupBasicInfoUseCase`
  - `execute(UpdateProductGroupBasicInfoCommand)` → `void`

- **Service 구현**: `UpdateProductGroupBasicInfoService`
  - `ProductGroupCommandFactory.createUpdateData()`: Command → `ProductGroupUpdateData` 변환
  - `ProductGroupCommandCoordinator.update()`: 검증 + 조회 + update + persist

- **트랜잭션**: `ProductGroupCommandCoordinator.update()` (@Transactional)

- **C3 vs C4 비교**:
  | 항목 | C3 (PUT) | C4 (PATCH /basic-info) |
  |------|----------|------------------------|
  | 수정 범위 | 이미지 + 옵션 + 상품 + 설명 + 고시 + 기본 정보 | 기본 정보만 |
  | 트랜잭션 | FullProductGroupUpdateCoordinator | ProductGroupCommandCoordinator |
  | DB 작업 | 다중 테이블 | product_groups 단일 |

---

## C5. PATCH /api/v1/market/product-groups/status - 배치 상태 변경

### 호출 흐름 다이어그램

```
[Adapter-In]
ProductGroupCommandController.batchChangeStatus(BatchChangeProductGroupStatusApiRequest)
  ├─ MarketAccessChecker.resolveCurrentSellerId()                 [인증 컨텍스트에서 sellerId 추출]
  ├─ ProductGroupCommandApiMapper.toCommand(sellerId, request)    [Command 변환]
  │   └─ BatchChangeProductGroupStatusCommand (sellerId, productGroupIds, targetStatus)
  └─ BatchChangeProductGroupStatusUseCase.execute(command)        [Port Interface]
      → ResponseEntity.noContent() (204)

[Application]
BatchChangeProductGroupStatusService.execute(BatchChangeProductGroupStatusCommand)  [UseCase 구현]
  ├─ ProductGroupId 목록 변환 (command.productGroupIds() → List<ProductGroupId>)
  ├─ ProductGroupReadManager.getByIdsAndSellerId(ids, sellerId)   [소유권 검증 포함 조회]
  │   └─ ProductGroupQueryPort.findByIdsAndSellerId()
  │       → sellerId 조건으로 조회 → 소유하지 않은 상품 그룹은 결과에서 제외
  ├─ ProductGroupStatus.valueOf(command.targetStatus())           [상태 변환]
  └─ for each productGroup:
      ├─ productGroup.changeStatus(targetStatus, changedAt)       [Domain 비즈니스 로직]
      │   └─ 상태 전이 검증 → 허용된 전이만 가능
      │       ACTIVE: canActivate() 검증
      │       INACTIVE: isActive() 검증
      │       SOLDOUT: isActive() 검증
      │       DELETED: canDelete() 검증
      │       → 불허 시 ProductGroupInvalidStatusTransitionException
      └─ ProductGroupCommandManager.persist(productGroup)
          └─ ProductGroupCommandPort.persist()                    [Port]

[Adapter-Out]
ProductGroupCommandAdapter
  └─ ProductGroupJpaRepository.save(entity)                       [UPDATE]

[Database]
-- 소유권 검증 조회
SELECT * FROM product_groups
WHERE id IN (?) AND seller_id = ? AND status != 'DELETED'

-- 상태 변경 (건별)
UPDATE product_groups SET status = ?, updated_at = ? WHERE id = ?
```

### Layer별 상세

#### Adapter-In Layer
- **Controller**: `ProductGroupCommandController`
  - Method: `batchChangeStatus(@Valid @RequestBody BatchChangeProductGroupStatusApiRequest)`
  - Response: `ResponseEntity<Void>` (204 No Content)
  - 권한: `@PreAuthorize("hasAuthority('product-group:write')")` + `@RequirePermission`
  - `MarketAccessChecker.resolveCurrentSellerId()`: 인증 토큰에서 sellerId 추출

- **Request DTO**: `BatchChangeProductGroupStatusApiRequest`
  ```java
  record BatchChangeProductGroupStatusApiRequest(
    @NotEmpty List<@NotNull Long> productGroupIds,
    @NotBlank String targetStatus   // ACTIVE, INACTIVE, SOLDOUT, DELETED
  )
  ```

#### Application Layer
- **UseCase Interface**: `BatchChangeProductGroupStatusUseCase`
  - `execute(BatchChangeProductGroupStatusCommand)` → `void`

- **Service 구현**: `BatchChangeProductGroupStatusService`
  - `@Transactional` 없음 (각 persist마다 별도 처리)
  - `readManager.getByIdsAndSellerId()`: sellerId 조건으로 소유권 검증
  - 건별 `changeStatus()` + `persist()` 호출

- **Key 비즈니스 로직**: `ProductGroup.changeStatus(targetStatus, now)`
  ```java
  public void changeStatus(ProductGroupStatus targetStatus, Instant now) {
      switch (targetStatus) {
          case ACTIVE   -> activate(now);   // canActivate() 검증: PROCESSING 또는 INACTIVE/SOLDOUT 상태만
          case INACTIVE -> deactivate(now); // isActive() 검증: ACTIVE 상태만
          case SOLDOUT  -> markSoldOut(now);// isActive() 검증: ACTIVE 상태만
          case DELETED  -> delete(now);     // canDelete() 검증: ACTIVE/INACTIVE/SOLDOUT 상태만
      }
  }
  ```

- **상태 전이 허용 매트릭스**:
  | 현재 상태 | ACTIVE 가능 | INACTIVE 가능 | SOLDOUT 가능 | DELETED 가능 |
  |-----------|------------|---------------|--------------|--------------|
  | DRAFT | X | X | X | X |
  | PROCESSING | X | X | X | X |
  | ACTIVE | X | O | O | O |
  | INACTIVE | O | X | X | O |
  | SOLDOUT | O | X | X | O |
  | REJECTED | X | X | X | X |

- **에러 처리**: 잘못된 상태 전이 → `ProductGroupInvalidStatusTransitionException`
- **소유권 검증**: API 진입 시 `MarketAccessChecker`로 sellerId 추출, 조회 쿼리에 sellerId 조건 포함

---

## 공통 패턴 분석

### 1. Hexagonal Architecture 레이어 격리

| 레이어 | DTO/모델 | 책임 |
|--------|----------|------|
| **Adapter-In** | ApiRequest / ApiResponse | HTTP 계층 (Validation, 직렬화) |
| **Application** | Command / Params / Bundle / Result | 유스케이스 조율, 트랜잭션 경계 |
| **Domain** | Aggregate, VO, Criteria | 비즈니스 규칙, 상태 불변성 |
| **Adapter-Out** | Entity | 영속화 기술 (JPA, QueryDSL) |

### 2. CQRS 분리

- **Query**: `ProductGroupQueryController` → `ProductGroupQueryApiMapper` → QueryUseCase → QueryService
- **Command**: `ProductGroupCommandController` → `ProductGroupCommandApiMapper` → CommandUseCase → CommandService

### 3. 트랜잭션 경계

| 계층 | @Transactional 위치 | 비고 |
|------|---------------------|------|
| Adapter-In | 없음 | 금지 |
| Application Service | 없음 | Coordinator/Facade에 위임 |
| **ProductGroupReadFacade** | @Transactional(readOnly = true) | Q1, Q2 조회 |
| **FullProductGroupRegistrationCoordinator** | @Transactional | C1 전체 등록 단일 TX |
| **FullProductGroupUpdateCoordinator** | @Transactional | C3 전체 수정 단일 TX |
| **ProductGroupCommandCoordinator** | @Transactional | C4 기본 정보 수정 |
| C2 BatchService | 없음 | 항목별 Coordinator TX |
| Adapter-Out | 없음 | 금지 |

### 4. 등록 Coordinator 패턴 (FullProductGroupRegistrationCoordinator)

복잡한 다중 Aggregate 등록은 per-package Coordinator가 조율:
1. **ProductGroup 기본 정보** (검증 + persist)
2. **productGroupId 바인딩** (bundle.bindAll)
3. **Images** (ImageCommandCoordinator)
4. **SellerOptions** (SellerOptionCommandCoordinator)
5. **Description** (DescriptionCommandCoordinator)
6. **Notice** (ProductNoticeCommandCoordinator)
7. **Products** (ProductCommandCoordinator, optionValueIds 전달)
8. **InspectionOutbox** (비동기 검수 등록)

### 5. Query 성능 최적화

**목록 조회 (Q1) - 2-Step 전략**:
- Step 1: Composition 쿼리 (pg + seller + brand + category JOIN) + COUNT
- Step 2: Enrichment 배치 쿼리 (가격/옵션 IN 쿼리 1회로 N+1 방지)

**상세 조회 (Q2) - 다중 Aggregate 개별 조회**:
- Composition 쿼리 (기본 정보 + 정책)
- ProductGroup Aggregate (이미지 + 옵션)
- Product 목록, Description, Notice 개별 조회

### 6. 변환 체인

```
[Query]
ApiRequest → ProductGroupSearchParams → ProductGroupSearchCriteria
  → DB 조회 → Entity/Tuple → ApplicationDTO → Result
  → ProductGroupDetailCompositeResult → ApiResponse

[Command]
ApiRequest → RegisterProductGroupCommand → ProductGroupRegistrationBundle
  → ProductGroup.forNew() (Domain)
  → ProductGroupJpaEntity → DB INSERT
```

### 7. 배치 처리 패턴 (C2)

```
BatchRegisterProductGroupFullService
  ↓ CompletableFuture.supplyAsync (Virtual Thread Pool)
  ↓ processOne() per item
  ↓ FullProductGroupRegistrationCoordinator.register() (독립 @Transactional)
  ↓ try-catch → BatchItemResult (성공/실패 분기)
  ↓ BatchProcessingResult.from(results) (200 OK, 일부 실패 허용)
```

### 8. 소유권 검증 패턴

| 엔드포인트 | 검증 방식 |
|-----------|---------|
| C3, C4 | `@PreAuthorize("@access.isSellerOwnerOr(#productGroupId, 'product-group:write')")` |
| C5 | `MarketAccessChecker.resolveCurrentSellerId()` + 조회 쿼리 sellerId 조건 |

---

## 주요 설계 결정

### 장점

1. **Aggregate 독립성**: ProductGroup, ProductGroupDescription, ProductNotice, Product가 각각 독립 Aggregate로 분리 → 변경 격리
2. **per-package Coordinator**: 각 도메인의 등록/수정 로직이 독립 Coordinator에 캡슐화
3. **Enrichment 분리**: 목록 조회 시 기본 정보와 가격/옵션 enrichment를 분리하여 N+1 방지
4. **배치 병렬 처리**: Virtual Thread 기반 CompletableFuture로 대량 등록 성능 확보
5. **상태 전이 검증**: Domain Aggregate에서 상태 전이 불변식 보장

### 트레이드오프

1. **상세 조회 쿼리 수**: Q2에서 5개 분리 조회 (성능 vs 설계 명확성)
2. **DTO 변환 계층**: ApiRequest → Command → Bundle → Domain → Entity (4단계 이상)
3. **C2 배치 결과**: 일부 실패 시 200 OK 반환 → 클라이언트가 results[] 확인 필요

---

**분석 일시**: 2026-02-18
**분석 대상**: `adapter-in/rest-api`, `application`, `domain`, `adapter-out/persistence-mysql`
**총 엔드포인트**: 7개 (Query 2개, Command 5개)
