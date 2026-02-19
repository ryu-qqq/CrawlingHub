# Product (SKU) Domain API Flow Analysis

Product(SKU) 도메인의 전체 API 호출 흐름 분석 문서입니다.

> **참고**: Product(SKU) Query Controller는 현재 미구현 상태입니다. 상품 조회는 ProductGroup 단위 복합 조회로 처리되며, 이 문서는 Command 엔드포인트 4개를 분석합니다.

---

## 엔드포인트 목록

| HTTP Method | Path | 설명 | Controller Method |
|-------------|------|------|-------------------|
| PATCH | `/api/v1/market/products/{productId}/price` | 개별 상품 가격 수정 | `updatePrice()` |
| PATCH | `/api/v1/market/products/{productId}/stock` | 개별 상품 재고 수정 | `updateStock()` |
| PATCH | `/api/v1/market/products/product-groups/{productGroupId}/status` | 상품 배치 상태 변경 | `batchChangeStatus()` |
| PATCH | `/api/v1/market/products/product-groups/{productGroupId}` | 상품 + 옵션 일괄 수정 | `updateProducts()` |

---

## C1. PATCH /products/{productId}/price - 상품 가격 수정

### 호출 흐름 다이어그램

```
[Adapter-In]
ProductCommandController.updatePrice(productId, UpdateProductPriceApiRequest)
  ├─ @PreAuthorize("@access.isSellerOwnerOr(#productId, 'product:write')")
  ├─ @RequirePermission("product:write")
  ├─ ProductCommandApiMapper.toCommand(productId, request)       [Command 변환]
  └─ UpdateProductPriceUseCase.execute(command)                  [Port Interface]

[Application]
UpdateProductPriceService.execute(UpdateProductPriceCommand)     [UseCase 구현]
  ├─ ProductCommandFactory.createPriceUpdateContext(command)     [Context 생성]
  │   └─ StatusChangeContext<ProductId>(ProductId.of(...), now)  [TimeProvider]
  ├─ ProductReadManager.getById(context.id())                    [readOnly 조회]
  │   └─ ProductQueryPort.findById(ProductId)                    [Port]
  ├─ product.updatePrice(Money.of(regular), Money.of(current), now)  [Domain 로직]
  └─ ProductCommandManager.persist(product)                      [@Transactional]
      └─ ProductCommandPort.persist(product)                     [Port]

[Domain]
Product.updatePrice(Money regularPrice, Money currentPrice, Instant now)
  ├─ validateCurrentPrice()  → ProductInvalidPriceException if current > regular
  ├─ this.regularPrice = regularPrice
  ├─ this.currentPrice = currentPrice
  ├─ this.salePrice = currentPrice         [자동 설정]
  ├─ this.discountRate = Money.discountRate(regular, current)  [자동 계산]
  └─ this.updatedAt = now

[Adapter-Out]
ProductCommandAdapter.persist(product)                           [Port 구현]
  ├─ ProductJpaEntityMapper.toEntity(product)                    [Domain → Entity]
  └─ ProductJpaRepository.save(entity)                          [INSERT or UPDATE]

ProductQueryAdapter.findById(id)                                 [조회 Port 구현]
  └─ ProductQueryDslRepository.findById(id.value())
      ├─ WHERE id = ? AND status != 'DELETED'
      └─ findOptionMappingsByProductId(entity.getId())
          └─ WHERE product_id = ?

[Database]
- SELECT products WHERE id = ? AND status != 'DELETED'
- SELECT product_option_mappings WHERE product_id = ?
- UPDATE products SET regular_price=?, current_price=?, sale_price=?,
                      discount_rate=?, updated_at=? WHERE id=?
```

### Layer별 상세

#### Adapter-In Layer

- **Controller**: `ProductCommandController`
  - Method: `updatePrice(Long productId, UpdateProductPriceApiRequest request)`
  - 경로: `PATCH /api/v1/market/products/{productId}/price`
  - 권한: `@PreAuthorize("@access.isSellerOwnerOr(#productId, 'product:write')")` + `@RequirePermission("product:write")`
  - Response: `ResponseEntity<Void>` (204 No Content)

- **Request DTO**: `UpdateProductPriceApiRequest`
  ```java
  record UpdateProductPriceApiRequest(
    @NotNull Integer regularPrice,   // 정가
    @NotNull Integer currentPrice    // 판매가 (정가 이하)
  )
  ```

- **ApiMapper**: `ProductCommandApiMapper`
  - `toCommand(Long productId, UpdateProductPriceApiRequest)` → `UpdateProductPriceCommand`
  - 단순 필드 복사 변환

#### Application Layer

- **UseCase Interface**: `UpdateProductPriceUseCase`
  - `execute(UpdateProductPriceCommand)` → `void`

- **Command DTO**: `UpdateProductPriceCommand`
  ```java
  record UpdateProductPriceCommand(long productId, int regularPrice, int currentPrice)
  ```

- **Service 구현**: `UpdateProductPriceService`
  1. Factory로 `StatusChangeContext<ProductId>` 생성 (TimeProvider → `now()`)
  2. `ProductReadManager.getById()` → Product 조회 (없으면 `ProductNotFoundException`)
  3. `product.updatePrice()` → 도메인 로직 실행
  4. `ProductCommandManager.persist()` → 저장 (트랜잭션)

- **Factory**: `ProductCommandFactory`
  - `createPriceUpdateContext()`: `ProductId.of(command.productId())` + `timeProvider.now()`
  - `TimeProvider` 직접 사용 금지 규칙(APP-TIM-001) 준수 → Factory에서만 시간 처리

- **Manager (Read)**: `ProductReadManager`
  - `@Transactional(readOnly = true)`
  - `getById(ProductId)`: `ProductQueryPort.findById()` + `ProductNotFoundException` 처리

- **Manager (Command)**: `ProductCommandManager`
  - `@Transactional`
  - `persist(Product)`: `ProductCommandPort.persist()` 위임

#### Domain Layer

- **Port (Query)**: `ProductQueryPort`
  - `findById(ProductId)` → `Optional<Product>`

- **Port (Command)**: `ProductCommandPort`
  - `persist(Product)` → `Long` (productId)

- **Aggregate**: `Product`
  - `updatePrice(Money, Money, Instant)`:
    - 검증: `currentPrice > regularPrice` → `ProductInvalidPriceException`
    - salePrice = currentPrice (자동)
    - discountRate = `Money.discountRate(regular, current)` (자동 계산)

#### Adapter-Out Layer

- **Command Adapter**: `ProductCommandAdapter`
  - `ProductJpaEntityMapper.toEntity(product)` → `ProductJpaEntity`
  - `ProductJpaRepository.save(entity)` → JPA INSERT/UPDATE (id null이면 INSERT)

- **Query Adapter**: `ProductQueryAdapter`
  - `ProductQueryDslRepository.findById()` → `Optional<ProductJpaEntity>`
  - `findOptionMappingsByProductId()` → `List<ProductOptionMappingJpaEntity>`
  - `ProductJpaEntityMapper.toDomain(entity, mappings)` → `Product`

- **QueryDSL 조건**:
  ```java
  // findById
  WHERE product.id = :id AND product.status != 'DELETED'

  // findOptionMappingsByProductId
  WHERE optionMapping.productId = :productId
  ```

- **JPA Entity**: `ProductJpaEntity` (`@Table(name = "products")`)
  - 필드: `id`, `productGroupId`, `skuCode`, `regularPrice`, `currentPrice`, `salePrice`, `discountRate`, `stockQuantity`, `status`, `sortOrder`
  - 상속: `BaseAuditEntity` (`createdAt`, `updatedAt`)

- **Database Query**:
  ```sql
  -- 조회
  SELECT * FROM products
  WHERE id = ? AND status != 'DELETED'

  SELECT * FROM product_option_mappings
  WHERE product_id = ?

  -- 수정 (JPA merge)
  UPDATE products SET
    regular_price = ?, current_price = ?, sale_price = ?,
    discount_rate = ?, updated_at = ?
  WHERE id = ?
  ```

### 에러 처리

| 예외 | HTTP Status | 발생 시점 |
|------|-------------|-----------|
| `ProductNotFoundException` | 404 | `ProductReadManager.getById()` - 상품 미존재 |
| `ProductInvalidPriceException` | 400 | `Product.updatePrice()` - 판매가 > 정가 |
| `AccessDeniedException` | 403 | `@PreAuthorize` - 소유자도 아니고 `product:write` 권한도 없는 경우 |

---

## C2. PATCH /products/{productId}/stock - 상품 재고 수정

### 호출 흐름 다이어그램

```
[Adapter-In]
ProductCommandController.updateStock(productId, UpdateProductStockApiRequest)
  ├─ @PreAuthorize("@access.isSellerOwnerOr(#productId, 'product:write')")
  ├─ @RequirePermission("product:write")
  ├─ ProductCommandApiMapper.toCommand(productId, request)       [Command 변환]
  └─ UpdateProductStockUseCase.execute(command)                  [Port Interface]

[Application]
UpdateProductStockService.execute(UpdateProductStockCommand)     [UseCase 구현]
  ├─ ProductCommandFactory.createStockUpdateContext(command)     [Context 생성]
  │   └─ StatusChangeContext<ProductId>(ProductId.of(...), now)
  ├─ ProductReadManager.getById(context.id())                    [readOnly 조회]
  │   └─ ProductQueryPort.findById(ProductId)                    [Port]
  ├─ product.updateStock(command.stockQuantity(), context.changedAt())  [Domain 로직]
  └─ ProductCommandManager.persist(product)                      [@Transactional]
      └─ ProductCommandPort.persist(product)                     [Port]

[Domain]
Product.updateStock(int stockQuantity, Instant now)
  ├─ validateStockQuantity()  → IllegalArgumentException if stockQuantity < 0
  ├─ this.stockQuantity = stockQuantity
  └─ this.updatedAt = now

[Adapter-Out]
ProductCommandAdapter.persist(product)
  ├─ ProductJpaEntityMapper.toEntity(product)
  └─ ProductJpaRepository.save(entity)

[Database]
- SELECT products WHERE id = ? AND status != 'DELETED'
- SELECT product_option_mappings WHERE product_id = ?
- UPDATE products SET stock_quantity = ?, updated_at = ? WHERE id = ?
```

### Layer별 상세

#### Adapter-In Layer

- **Controller**: `ProductCommandController`
  - Method: `updateStock(Long productId, UpdateProductStockApiRequest request)`
  - 경로: `PATCH /api/v1/market/products/{productId}/stock`
  - 권한: `@PreAuthorize("@access.isSellerOwnerOr(#productId, 'product:write')")` + `@RequirePermission("product:write")`
  - Response: `ResponseEntity<Void>` (204 No Content)

- **Request DTO**: `UpdateProductStockApiRequest`
  ```java
  record UpdateProductStockApiRequest(
    @NotNull @Min(0) Integer stockQuantity   // 재고 수량 (0 이상)
  )
  ```

- **ApiMapper**: `ProductCommandApiMapper`
  - `toCommand(Long productId, UpdateProductStockApiRequest)` → `UpdateProductStockCommand`

#### Application Layer

- **UseCase Interface**: `UpdateProductStockUseCase`
  - `execute(UpdateProductStockCommand)` → `void`

- **Command DTO**: `UpdateProductStockCommand`
  ```java
  record UpdateProductStockCommand(long productId, int stockQuantity)
  ```

- **Service 구현**: `UpdateProductStockService`
  - C1(updatePrice)와 동일한 패턴: Factory → ReadManager → Domain 로직 → CommandManager

- **트랜잭션 경계**:
  - `ProductReadManager.getById()`: `@Transactional(readOnly = true)`
  - `ProductCommandManager.persist()`: `@Transactional`
  - Service 자체에는 `@Transactional` 없음 (조회와 저장이 별도 트랜잭션)

#### Domain Layer

- **Aggregate**: `Product`
  - `updateStock(int stockQuantity, Instant now)`:
    - 검증: `stockQuantity < 0` → `IllegalArgumentException`
    - `this.stockQuantity = stockQuantity`

#### Adapter-Out Layer

- **Database Query**:
  ```sql
  -- 조회 (ReadManager)
  SELECT * FROM products WHERE id = ? AND status != 'DELETED'
  SELECT * FROM product_option_mappings WHERE product_id = ?

  -- 저장 (CommandManager)
  UPDATE products SET stock_quantity = ?, updated_at = ? WHERE id = ?
  ```

### 에러 처리

| 예외 | HTTP Status | 발생 시점 |
|------|-------------|-----------|
| `ProductNotFoundException` | 404 | `ProductReadManager.getById()` - 상품 미존재 |
| `IllegalArgumentException` | 400 | `Product.updateStock()` - 재고 수량 음수 |
| `AccessDeniedException` | 403 | `@PreAuthorize` - 권한 없음 |

---

## C3. PATCH /products/product-groups/{productGroupId}/status - 상품 배치 상태 변경

### 호출 흐름 다이어그램

```
[Adapter-In]
ProductCommandController.batchChangeStatus(productGroupId, BatchChangeProductStatusApiRequest)
  ├─ @PreAuthorize("hasAuthority('product:write')")              [권한 체크]
  ├─ @RequirePermission("product:write")
  ├─ accessChecker.resolveCurrentSellerId()                      [인증 컨텍스트에서 sellerId 추출]
  ├─ ProductCommandApiMapper.toCommand(sellerId, productGroupId, request)  [Command 변환]
  └─ BatchChangeProductStatusUseCase.execute(command)            [Port Interface]

[Application]
BatchChangeProductStatusService.execute(BatchChangeProductStatusCommand)  [UseCase 구현]
  ├─ ProductGroupReadManager.getByIdsAndSellerId([productGroupId], sellerId)
  │   └─ ProductGroupQueryPort.findByIdsAndSellerId(ids, sellerId)  [소유권 검증]
  │       └─ ProductGroupOwnershipViolationException if size mismatch
  ├─ productIds → List<ProductId> 변환
  ├─ ProductReadManager.getByProductGroupIdAndIds(pgId, productIds)
  │   └─ ProductQueryPort.findByProductGroupIdAndIdIn()          [배치 조회]
  │       └─ ProductOwnershipViolationException if size mismatch
  ├─ ProductStatus.valueOf(command.targetStatus())               [상태 enum 변환]
  ├─ Instant changedAt = timeProvider.now()
  ├─ for each product: product.changeStatus(targetStatus, changedAt)  [Domain 로직]
  └─ ProductCommandManager.persistAll(products)                  [@Transactional]
      └─ ProductCommandPort.persist() × N

[Domain]
Product.changeStatus(ProductStatus targetStatus, Instant now)
  ├─ ACTIVE  → activate(now)   : INACTIVE, SOLDOUT에서만 가능
  ├─ INACTIVE → deactivate(now): ACTIVE에서만 가능
  ├─ SOLDOUT  → markSoldOut(now): ACTIVE에서만 가능
  └─ DELETED  → delete(now)    : 각 상태별 canDelete() 검증
  └─ ProductInvalidStatusTransitionException if invalid transition

[Adapter-Out]
ProductQueryAdapter.findByProductGroupIdAndIdIn()                [배치 조회]
  └─ ProductQueryDslRepository.findByProductGroupIdAndIdIn()
      ├─ WHERE product_group_id = ? AND id IN (?) AND status != 'DELETED'
      └─ findOptionMappingsByProductIds(productIds) [IN 쿼리]

ProductCommandAdapter.persist() × N                              [각 Product 저장]
  └─ ProductJpaRepository.save() × N

[Database]
- SELECT products WHERE product_group_id = ? AND id IN (?,?,?) AND status != 'DELETED'
- SELECT product_option_mappings WHERE product_id IN (?,?,?)
- UPDATE products SET status = ?, updated_at = ? WHERE id = ?  × N건
```

### Layer별 상세

#### Adapter-In Layer

- **Controller**: `ProductCommandController`
  - Method: `batchChangeStatus(Long productGroupId, BatchChangeProductStatusApiRequest request)`
  - 경로: `PATCH /api/v1/market/products/product-groups/{productGroupId}/status`
  - 권한: `@PreAuthorize("hasAuthority('product:write')")` (소유자 체크 없음 - 서비스에서 직접 검증)
  - 특이사항: `accessChecker.resolveCurrentSellerId()`로 인증 컨텍스트에서 셀러 ID 추출 후 Command에 포함
  - Response: `ResponseEntity<Void>` (204 No Content)

- **Request DTO**: `BatchChangeProductStatusApiRequest`
  ```java
  record BatchChangeProductStatusApiRequest(
    @NotEmpty List<@NotNull Long> productIds,   // 상품 ID 목록
    @NotBlank String targetStatus               // "ACTIVE" | "INACTIVE" | "SOLDOUT"
  )
  ```

- **ApiMapper**: `ProductCommandApiMapper`
  - `toCommand(long sellerId, Long productGroupId, BatchChangeProductStatusApiRequest)` → `BatchChangeProductStatusCommand`
  - sellerId는 ApiRequest가 아닌 `accessChecker`에서 주입

#### Application Layer

- **UseCase Interface**: `BatchChangeProductStatusUseCase`
  - `execute(BatchChangeProductStatusCommand)` → `void`

- **Command DTO**: `BatchChangeProductStatusCommand`
  ```java
  record BatchChangeProductStatusCommand(
    long sellerId,          // 소유권 검증용
    long productGroupId,
    List<Long> productIds,
    String targetStatus
  )
  ```

- **Service 구현**: `BatchChangeProductStatusService`
  1. **소유권 검증**: `ProductGroupReadManager.getByIdsAndSellerId()` → `sellerId` 기준 ProductGroup 소유 확인
  2. **배치 조회**: `ProductReadManager.getByProductGroupIdAndIds()` → ProductGroup 내 요청 productIds 조회, 수 불일치 시 `ProductOwnershipViolationException`
  3. **상태 enum 변환**: `ProductStatus.valueOf(command.targetStatus())`
  4. **도메인 로직**: 각 Product에 `changeStatus()` 호출
  5. **일괄 저장**: `ProductCommandManager.persistAll()`

- **트랜잭션 특이사항**:
  - `ProductGroupReadManager.getByIdsAndSellerId()`: `@Transactional(readOnly = true)`
  - `ProductReadManager.getByProductGroupIdAndIds()`: `@Transactional(readOnly = true)`
  - `ProductCommandManager.persistAll()`: `@Transactional`
  - Service 자체에 `@Transactional` 없음 → 각 Manager가 독립 트랜잭션으로 실행

#### Domain Layer

- **Aggregate**: `Product.changeStatus(ProductStatus, Instant)`
  - `ACTIVE → activate()`: `status.canActivate()` 검증 (INACTIVE, SOLDOUT에서만)
  - `INACTIVE → deactivate()`: `status.isActive()` 검증 (ACTIVE에서만)
  - `SOLDOUT → markSoldOut()`: `status.isActive()` 검증 (ACTIVE에서만)
  - `DELETED → delete()`: `status.canDelete()` 검증
  - 유효하지 않은 전이: `ProductInvalidStatusTransitionException`

- **Port (Query)**:
  - `ProductGroupQueryPort.findByIdsAndSellerId(List<ProductGroupId>, long)` → `List<ProductGroup>`
  - `ProductQueryPort.findByProductGroupIdAndIdIn(ProductGroupId, List<ProductId>)` → `List<Product>`

#### Adapter-Out Layer

- **QueryDSL 조건**:
  ```java
  // findByProductGroupIdAndIdIn
  WHERE product.productGroupId = :pgId
    AND product.id IN (:ids)
    AND product.status != 'DELETED'

  // findOptionMappingsByProductIds (IN 쿼리 최적화)
  WHERE optionMapping.productId IN (:productIds)
  ```

- **Database Query**:
  ```sql
  -- ProductGroup 소유권 검증
  SELECT * FROM product_groups
  WHERE id IN (?) AND seller_id = ? AND deleted_at IS NULL

  -- Product 배치 조회
  SELECT * FROM products
  WHERE product_group_id = ? AND id IN (?,?,?) AND status != 'DELETED'

  SELECT * FROM product_option_mappings
  WHERE product_id IN (?,?,?)

  -- 상태 변경 (N건)
  UPDATE products SET status = ?, updated_at = ? WHERE id = ?
  ```

### 에러 처리

| 예외 | HTTP Status | 발생 시점 |
|------|-------------|-----------|
| `ProductGroupNotFoundException` | 404 | `ProductGroupReadManager` - ProductGroup 미존재 |
| `ProductGroupOwnershipViolationException` | 403 | `ProductGroupReadManager` - 셀러 소유 ProductGroup 아님 |
| `ProductOwnershipViolationException` | 403 | `ProductReadManager` - 요청 ID 수 vs 조회 수 불일치 |
| `ProductInvalidStatusTransitionException` | 400 | `Product.changeStatus()` - 유효하지 않은 상태 전이 |
| `IllegalArgumentException` | 400 | `ProductStatus.valueOf()` - 유효하지 않은 targetStatus 값 |

---

## C4. PATCH /products/product-groups/{productGroupId} - 상품 + 옵션 일괄 수정

> 가장 복잡한 엔드포인트. 옵션 그룹 diff 수정 → Product diff 수정 순서로 두 도메인을 조율합니다.

### 호출 흐름 다이어그램

```
[Adapter-In]
ProductCommandController.updateProducts(productGroupId, UpdateProductsApiRequest)
  ├─ @PreAuthorize("@access.isSellerOwnerOr(#productGroupId, 'product:write')")
  ├─ @RequirePermission("product:write")
  ├─ ProductCommandApiMapper.toCommand(productGroupId, request)  [Command 변환]
  │   └─ UpdateProductsCommand (OptionGroupData + ProductData 중첩)
  └─ UpdateProductsUseCase.execute(command)                      [Port Interface]

[Application - @Transactional]
UpdateProductsService.execute(UpdateProductsCommand)             [UseCase 구현]
  │
  ├─ [Step 1: 옵션 수정]
  │   UpdateSellerOptionGroupsCommand 변환
  │   └─ SellerOptionCommandCoordinator.update(optionCmd)        [@Transactional]
  │       ├─ SellerOptionGroupFactory.toUpdateData(pgId, groups) [UpdateData 생성]
  │       ├─ SellerOptionGroupValidator.validateCanonicalReferences()
  │       ├─ SellerOptionGroupReadManager.getByProductGroupId()  [기존 옵션 조회]
  │       │   └─ SellerOptionGroupQueryPort.findByProductGroupId()
  │       ├─ existing.update(updateData)                         [SellerOptionGroups.update()]
  │       │   └─ SellerOptionGroupDiff 생성 (added/retained/removed)
  │       └─ SellerOptionPersistFacade.persistDiff(diff)         [@Transactional]
  │           ├─ retained: SellerOptionGroupCommandPort.persist()
  │           ├─ added: SellerOptionGroupCommandPort.persist() (신규 ID 생성)
  │           ├─ removed: soft delete
  │           └─ resolvedIds: persist 후 실제 생성된 ID로 치환
  │       → SellerOptionUpdateResult(resolvedActiveValueIds, occurredAt)
  │
  └─ [Step 2: Product diff 수정]
      toEntries(command.products()) → List<ProductDiffUpdateEntry>
      └─ ProductCommandCoordinator.updateWithDiff(pgId, entries, optionResult) [@Transactional]
          │
          ├─ [기존 Product 전체 조회]
          │   ProductReadManager.findByProductGroupId(pgId)
          │   └─ ProductQueryPort.findByProductGroupId()
          │
          ├─ [diff 분류]
          │   for each entry in entries:
          │     productId != null → retained: product.update(sku, price, stock, sort, now)
          │     productId == null → added: ProductCreationData.toProduct(pgId, allActiveValueIds, now)
          │   remaining (not in entries) → removed: product.delete(now)
          │
          ├─ [retained 저장]
          │   ProductCommandManager.persistAll(retained)
          │   └─ ProductCommandPort.persist() × N
          │
          ├─ [removed 저장]
          │   ProductCommandManager.persistAll(removed)  [status=DELETED]
          │   └─ ProductCommandPort.persist() × N
          │
          └─ [added 등록]
              ProductCommandCoordinator.register(added)  [@Transactional]
              ├─ ProductCommandManager.persistAll(added)
              │   └─ ProductCommandPort.persist() × N
              └─ for each product:
                  ProductOptionMappingCommandManager.persistAll(product.optionMappings())
                  └─ ProductOptionMappingCommandPort.persist() × M

[Domain]
Product.update(SkuCode, Money, Money, int, int, Instant)
  ├─ updatePrice() → 가격 + salePrice + discountRate 재계산
  ├─ updateStock()
  ├─ updateSkuCode()
  └─ updateSortOrder()

Product.delete(Instant now)
  ├─ status.canDelete() 검증
  └─ status = DELETED

ProductCreationData.toProduct(ProductGroupId, List<SellerOptionValueId>, Instant)
  └─ Product.forNew() + ProductOptionMapping 생성 (optionValueIndices 기반 매핑)

[Adapter-Out]
(SellerOption 관련)
SellerOptionGroupQueryAdapter → SellerOptionGroupQueryDslRepository
SellerOptionGroupCommandAdapter → SellerOptionGroupJpaRepository
SellerOptionValueCommandAdapter → SellerOptionValueJpaRepository

(Product 관련)
ProductQueryAdapter.findByProductGroupId()
  └─ ProductQueryDslRepository
      ├─ findByProductGroupId(): WHERE product_group_id=? AND status!='DELETED'
      └─ findOptionMappingsByProductIds(): WHERE product_id IN (?)

ProductCommandAdapter.persist() × N
  └─ ProductJpaRepository.save(entity)

ProductOptionMappingCommandAdapter.persist() × M
  └─ ProductOptionMappingJpaRepository.save(entity)

[Database]
-- 옵션 조회/수정
SELECT * FROM seller_option_groups WHERE product_group_id = ?
UPDATE seller_option_groups SET ... WHERE id = ?
INSERT INTO seller_option_groups (...)
UPDATE seller_option_groups SET deleted_at = ? WHERE id = ?
INSERT/UPDATE seller_option_values (...)

-- Product 전체 조회
SELECT * FROM products WHERE product_group_id = ? AND status != 'DELETED'
SELECT * FROM product_option_mappings WHERE product_id IN (...)

-- retained 수정
UPDATE products SET sku_code=?, regular_price=?, current_price=?, sale_price=?,
  discount_rate=?, stock_quantity=?, sort_order=?, updated_at=? WHERE id=?

-- removed 소프트 삭제
UPDATE products SET status='DELETED', updated_at=? WHERE id=?

-- added 신규 등록
INSERT INTO products (product_group_id, sku_code, regular_price, current_price,
  sale_price, discount_rate, stock_quantity, status, sort_order, created_at, updated_at) VALUES (...)
INSERT INTO product_option_mappings (product_id, seller_option_value_id) VALUES (...)
```

### Layer별 상세

#### Adapter-In Layer

- **Controller**: `ProductCommandController`
  - Method: `updateProducts(Long productGroupId, UpdateProductsApiRequest request)`
  - 경로: `PATCH /api/v1/market/products/product-groups/{productGroupId}`
  - 권한: `@PreAuthorize("@access.isSellerOwnerOr(#productGroupId, 'product:write')")` + `@RequirePermission("product:write")`
  - Response: `ResponseEntity<Void>` (204 No Content)

- **Request DTO**: `UpdateProductsApiRequest`
  ```java
  record UpdateProductsApiRequest(
    @NotEmpty @Valid List<OptionGroupApiRequest> optionGroups,
    @NotEmpty @Valid List<ProductDataApiRequest> products
  )

  record OptionGroupApiRequest(
    Long sellerOptionGroupId,        // 기존이면 non-null, 신규이면 null
    @NotBlank String optionGroupName,
    Long canonicalOptionGroupId,
    @NotEmpty @Valid List<OptionValueApiRequest> optionValues
  )

  record OptionValueApiRequest(
    Long sellerOptionValueId,        // 기존이면 non-null, 신규이면 null
    @NotBlank String optionValueName,
    Long canonicalOptionValueId,
    @NotNull @Min(0) Integer sortOrder
  )

  record ProductDataApiRequest(
    Long productId,                  // 기존이면 non-null, 신규이면 null
    @NotBlank String skuCode,
    @NotNull Integer regularPrice,
    @NotNull Integer currentPrice,
    @NotNull @Min(0) Integer stockQuantity,
    @NotNull @Min(0) Integer sortOrder,
    @NotNull List<Integer> optionIndices  // optionValues 배열 내 인덱스
  )
  ```

- **ApiMapper**: `ProductCommandApiMapper`
  - `toCommand(Long productGroupId, UpdateProductsApiRequest)` → `UpdateProductsCommand`
  - Stream 기반 중첩 DTO 변환 (OptionGroupData, OptionValueData, ProductData)

#### Application Layer

- **UseCase Interface**: `UpdateProductsUseCase`
  - `execute(UpdateProductsCommand)` → `void`

- **Command DTO**: `UpdateProductsCommand`
  ```java
  record UpdateProductsCommand(
    long productGroupId,
    List<OptionGroupData> optionGroups,  // sellerOptionGroupId=null이면 신규
    List<ProductData> products           // productId=null이면 신규
  )
  ```

- **Service 구현**: `UpdateProductsService`
  - `@Transactional` 적용 (전체 흐름을 하나의 트랜잭션으로 묶음)
  - Step 1: 옵션 수정 → `SellerOptionCommandCoordinator.update()`
  - Step 2: Product diff 수정 → `ProductCommandCoordinator.updateWithDiff()`

- **Coordinator (SellerOption)**: `SellerOptionCommandCoordinator`
  - `update(UpdateSellerOptionGroupsCommand)` → `SellerOptionUpdateResult`
  - ID 기반 diff: null=신규, non-null=기존
  - `persistFacade.persistDiff()` 후 신규 ID가 실제 DB 생성 ID로 치환된 `resolvedIds` 반환

- **Coordinator (Product)**: `ProductCommandCoordinator`
  - `updateWithDiff(ProductGroupId, List<ProductDiffUpdateEntry>, SellerOptionUpdateResult)`
  - 기존 Product 전체 조회 → Map으로 변환
  - entries를 순회하며 retained/added 분류
  - 기존에 있었지만 entries에 없는 Product → removed (소프트 삭제)
  - retained: `product.update()` 후 `persistAll()`
  - removed: `product.delete()` 후 `persistAll()`
  - added: `ProductCreationData.toProduct()` → `register()` (Product + OptionMapping 저장)

- **Factory (Product)**: `ProductCommandFactory`
  - `toCreationData(ProductDiffUpdateEntry)` → `ProductCreationData`
  - `ProductCreationData.toProduct(pgId, allActiveValueIds, now)`:
    - `optionValueIndices`로 `allActiveValueIds`에서 실제 `SellerOptionValueId` 선택
    - `Product.forNew()` + `ProductOptionMapping` 생성

#### Domain Layer

- **Aggregate**: `Product`
  - `update(SkuCode, Money, Money, int, int, Instant)`: 가격/재고/SKU/정렬 일괄 수정
  - `delete(Instant)`: 소프트 삭제 (status = DELETED)
  - `forNew(...)`: 신규 Product 생성 (status = ACTIVE)

- **Port (Query)**:
  - `ProductQueryPort.findByProductGroupId(ProductGroupId)` → `List<Product>`
  - `ProductQueryPort.findByProductGroupIdAndIdIn(ProductGroupId, List<ProductId>)` → `List<Product>`

- **Port (Command)**:
  - `ProductCommandPort.persist(Product)` → `Long`
  - `ProductOptionMappingCommandPort.persist(ProductOptionMapping)` → `void`

#### Adapter-Out Layer

- **QueryDSL 조건**:
  ```java
  // findByProductGroupId (전체 조회)
  WHERE product.productGroupId = :pgId
    AND product.status != 'DELETED'

  // findOptionMappingsByProductIds (IN 쿼리)
  WHERE optionMapping.productId IN (:productIds)
  ```

- **JPA Entity**:
  - `ProductJpaEntity`: `@Table(name = "products")`
  - `ProductOptionMappingJpaEntity`: `@Table(name = "product_option_mappings")`
    - FK: `product_id`, `seller_option_value_id` (JPA 관계 어노테이션 없이 Long 전략)

- **Database Query**:
  ```sql
  -- [Step 1] 옵션 조회/수정 (SellerOption 도메인)
  SELECT * FROM seller_option_groups WHERE product_group_id = ? AND deleted_at IS NULL
  SELECT * FROM seller_option_values WHERE seller_option_group_id IN (?)
  UPDATE seller_option_groups SET name=?, updated_at=? WHERE id=?
  INSERT INTO seller_option_groups (product_group_id, name, ...) VALUES (?)
  UPDATE seller_option_groups SET deleted_at=? WHERE id=?
  INSERT INTO seller_option_values (seller_option_group_id, name, ...) VALUES (?)

  -- [Step 2a] Product 전체 조회
  SELECT * FROM products
  WHERE product_group_id = ? AND status != 'DELETED'

  SELECT * FROM product_option_mappings WHERE product_id IN (...)

  -- [Step 2b] retained 수정
  UPDATE products SET
    sku_code=?, regular_price=?, current_price=?, sale_price=?,
    discount_rate=?, stock_quantity=?, sort_order=?, updated_at=?
  WHERE id=?

  -- [Step 2c] removed 소프트 삭제
  UPDATE products SET status='DELETED', updated_at=? WHERE id=?

  -- [Step 2d] added 신규 등록
  INSERT INTO products (
    product_group_id, sku_code, regular_price, current_price, sale_price,
    discount_rate, stock_quantity, status, sort_order, created_at, updated_at
  ) VALUES (...)

  INSERT INTO product_option_mappings (product_id, seller_option_value_id)
  VALUES (?, ?)
  ```

### 트랜잭션 경계 상세

```
UpdateProductsService.execute()  [@Transactional]
  └─ SellerOptionCommandCoordinator.update()  [@Transactional] (nested - 상위 트랜잭션 참여)
      └─ SellerOptionPersistFacade.persistDiff()  [@Transactional] (nested - 상위 트랜잭션 참여)
  └─ ProductCommandCoordinator.updateWithDiff()  [@Transactional] (nested - 상위 트랜잭션 참여)
      └─ ProductCommandManager.persistAll()  [@Transactional] (nested)
      └─ ProductCommandCoordinator.register()  [@Transactional] (nested)
          └─ ProductOptionMappingCommandManager.persistAll()  [@Transactional] (nested)
```

모든 내부 `@Transactional`은 상위 Service의 트랜잭션에 참여(REQUIRED propagation 기본값). 전체 작업이 하나의 원자적 트랜잭션으로 실행됩니다.

### 에러 처리

| 예외 | HTTP Status | 발생 시점 |
|------|-------------|-----------|
| `AccessDeniedException` | 403 | `@PreAuthorize` - 소유자도 아니고 권한도 없는 경우 |
| `SellerOptionGroupNotFoundException` | 404 | 옵션 수정 시 기존 그룹 미존재 |
| `ProductNotFoundException` | 404 | `ProductCommandCoordinator` - productId non-null인데 기존 Product 미존재 |
| `ProductInvalidPriceException` | 400 | `Product.updatePrice()` - 판매가 > 정가 |
| `ProductInvalidStatusTransitionException` | 400 | `Product.delete()` - 삭제 불가 상태 |

---

## 공통 패턴 분석

### 1. Hexagonal Architecture 레이어 격리

| 레이어 | DTO/모델 | 책임 |
|--------|----------|------|
| **Adapter-In** | `*ApiRequest` (Record) | HTTP Validation, JSON 직렬화 |
| **Application** | `*Command`, `*UpdateEntry` | UseCase 조율, 트랜잭션 경계 |
| **Domain** | `Product` Aggregate, `Money`/`SkuCode` VO | 비즈니스 규칙, 불변성 검증 |
| **Adapter-Out** | `ProductJpaEntity` | JPA 영속화 기술 |

### 2. CQRS 분리

- **Command**: `ProductCommandController` → `ProductCommandApiMapper` → Command UseCase → Service
- **Query**: 미구현 (ProductGroup 조회 시 함께 반환되는 구조)

### 3. 트랜잭션 경계

| 클래스 | `@Transactional` | 비고 |
|--------|------------------|------|
| `ProductCommandController` | ❌ 금지 | HTTP 레이어 |
| `UpdateProductPriceService` | ❌ 없음 | Manager에 위임 |
| `UpdateProductStockService` | ❌ 없음 | Manager에 위임 |
| `BatchChangeProductStatusService` | ❌ 없음 | Manager에 위임 |
| `UpdateProductsService` | ✅ `@Transactional` | 전체 흐름 원자적 실행 |
| `ProductReadManager` | ✅ `readOnly = true` | 조회 전용 |
| `ProductCommandManager` | ✅ `@Transactional` | 저장 경계 |
| `ProductCommandCoordinator` | ✅ `@Transactional` | register/updateWithDiff |

### 4. ID 기반 diff 패턴 (C4)

```
Request에서 각 항목의 ID 포함 여부로 신규/기존 판단:
  - sellerOptionGroupId = null  → 신규 옵션 그룹
  - sellerOptionGroupId != null → 기존 옵션 그룹
  - productId = null            → 신규 Product
  - productId != null           → 기존 Product
  - Request에 없는 기존 항목     → soft delete (removed)
```

### 5. optionIndices 매핑 패턴

```
Request의 optionValues 배열 순서 = 인덱스
Product의 optionIndices = [0, 2] 의미:
  → optionValues[0]와 optionValues[2]의 sellerOptionValue가 이 Product에 매핑됨

persist 후 resolvedActiveValueIds(실제 DB ID 리스트)에서
optionIndices로 선택 → ProductOptionMapping 생성
```

### 6. Money VO 패턴

- `Money.of(int value)`: 원시 타입 래핑
- `Money.discountRate(regular, current)`: 자동 할인율 계산
- `currentPrice > regularPrice` → `ProductInvalidPriceException`
- salePrice는 항상 currentPrice와 동일하게 자동 설정

### 7. 변환 체인

```
[C1, C2 - 단순 수정]
ApiRequest → Command → StatusChangeContext<ProductId> → Product.updateXxx() → Entity

[C3 - 배치 상태 변경]
ApiRequest(+authContext) → Command → Product[] → Product.changeStatus() → Entity[]

[C4 - 복합 수정]
ApiRequest → Command
  → UpdateSellerOptionGroupsCommand → SellerOptionGroupDiff → SellerOptionUpdateResult
  → ProductDiffUpdateEntry[] → (retained/added/removed) → Entity
```

---

## 에러 매핑 (`ProductErrorMapper`)

`PRD-` 접두사 에러 코드를 처리합니다.

| 예외 클래스 | HTTP Status | Title |
|------------|-------------|-------|
| `ProductNotFoundException` | 404 | Product Not Found |
| `ProductOwnershipViolationException` | 403 | Product Ownership Violation |
| 기타 `DomainException` (PRD- prefix) | 도메인 정의 | Product Error |

---

## 권한 매트릭스

| 엔드포인트 | 권한 조건 | 소유권 검증 방식 |
|-----------|----------|-----------------|
| C1 updatePrice | 상품 소유자 OR `product:write` | `@PreAuthorize("@access.isSellerOwnerOr(#productId, 'product:write')")` |
| C2 updateStock | 상품 소유자 OR `product:write` | `@PreAuthorize("@access.isSellerOwnerOr(#productId, 'product:write')")` |
| C3 batchChangeStatus | `product:write` 권한 | `accessChecker.resolveCurrentSellerId()` + `ProductGroupReadManager` 소유권 검증 |
| C4 updateProducts | 상품그룹 소유자 OR `product:write` | `@PreAuthorize("@access.isSellerOwnerOr(#productGroupId, 'product:write')")` |

---

## 관련 파일 경로

### Adapter-In Layer
- `/adapter-in/rest-api/src/main/java/com/ryuqq/marketplace/adapter/in/rest/product/controller/ProductCommandController.java`
- `/adapter-in/rest-api/src/main/java/com/ryuqq/marketplace/adapter/in/rest/product/mapper/ProductCommandApiMapper.java`
- `/adapter-in/rest-api/src/main/java/com/ryuqq/marketplace/adapter/in/rest/product/dto/command/UpdateProductPriceApiRequest.java`
- `/adapter-in/rest-api/src/main/java/com/ryuqq/marketplace/adapter/in/rest/product/dto/command/UpdateProductStockApiRequest.java`
- `/adapter-in/rest-api/src/main/java/com/ryuqq/marketplace/adapter/in/rest/product/dto/command/BatchChangeProductStatusApiRequest.java`
- `/adapter-in/rest-api/src/main/java/com/ryuqq/marketplace/adapter/in/rest/product/dto/command/UpdateProductsApiRequest.java`
- `/adapter-in/rest-api/src/main/java/com/ryuqq/marketplace/adapter/in/rest/product/error/ProductErrorMapper.java`
- `/adapter-in/rest-api/src/main/java/com/ryuqq/marketplace/adapter/in/rest/product/ProductAdminEndpoints.java`

### Application Layer
- `/application/src/main/java/com/ryuqq/marketplace/application/product/service/command/UpdateProductPriceService.java`
- `/application/src/main/java/com/ryuqq/marketplace/application/product/service/command/UpdateProductStockService.java`
- `/application/src/main/java/com/ryuqq/marketplace/application/product/service/command/BatchChangeProductStatusService.java`
- `/application/src/main/java/com/ryuqq/marketplace/application/product/service/command/UpdateProductsService.java`
- `/application/src/main/java/com/ryuqq/marketplace/application/product/internal/ProductCommandCoordinator.java`
- `/application/src/main/java/com/ryuqq/marketplace/application/product/factory/ProductCommandFactory.java`
- `/application/src/main/java/com/ryuqq/marketplace/application/product/manager/ProductReadManager.java`
- `/application/src/main/java/com/ryuqq/marketplace/application/product/manager/ProductCommandManager.java`
- `/application/src/main/java/com/ryuqq/marketplace/application/product/manager/ProductOptionMappingCommandManager.java`
- `/application/src/main/java/com/ryuqq/marketplace/application/selleroption/internal/SellerOptionCommandCoordinator.java`

### Domain Layer
- `/domain/src/main/java/com/ryuqq/marketplace/domain/product/aggregate/Product.java`

### Adapter-Out Layer
- `/adapter-out/persistence-mysql/src/main/java/com/ryuqq/marketplace/adapter/out/persistence/product/adapter/ProductCommandAdapter.java`
- `/adapter-out/persistence-mysql/src/main/java/com/ryuqq/marketplace/adapter/out/persistence/product/adapter/ProductQueryAdapter.java`
- `/adapter-out/persistence-mysql/src/main/java/com/ryuqq/marketplace/adapter/out/persistence/product/adapter/ProductOptionMappingCommandAdapter.java`
- `/adapter-out/persistence-mysql/src/main/java/com/ryuqq/marketplace/adapter/out/persistence/product/repository/ProductQueryDslRepository.java`
- `/adapter-out/persistence-mysql/src/main/java/com/ryuqq/marketplace/adapter/out/persistence/product/repository/ProductJpaRepository.java`
- `/adapter-out/persistence-mysql/src/main/java/com/ryuqq/marketplace/adapter/out/persistence/product/repository/ProductOptionMappingJpaRepository.java`
- `/adapter-out/persistence-mysql/src/main/java/com/ryuqq/marketplace/adapter/out/persistence/product/entity/ProductJpaEntity.java`
- `/adapter-out/persistence-mysql/src/main/java/com/ryuqq/marketplace/adapter/out/persistence/product/entity/ProductOptionMappingJpaEntity.java`
- `/adapter-out/persistence-mysql/src/main/java/com/ryuqq/marketplace/adapter/out/persistence/product/mapper/ProductJpaEntityMapper.java`
- `/adapter-out/persistence-mysql/src/main/java/com/ryuqq/marketplace/adapter/out/persistence/product/condition/ProductConditionBuilder.java`

---

**분석 일시**: 2026-02-18
**분석 대상**: Product(SKU) 도메인 Command 엔드포인트 4개
**관련 도메인**: product, selleroption (C4에서 교차 호출)
