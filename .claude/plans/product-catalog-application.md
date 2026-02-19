# Product Catalog - Application Layer 개발 계획

## 개요

상품 카탈로그 도메인의 Application 레이어 구현 계획.
**Command 8개 + Query 5개 = 총 13개 UseCase**

---

## 도메인 Aggregate 현황 (구현 완료)

| Aggregate | 타입 | 비고 |
|-----------|------|------|
| ProductGroup | Aggregate Root | 이미지, 셀러옵션 포함 |
| ProductGroupDescription | Aggregate Root | ProductGroup 1:1, 대용량 HTML 분리 |
| Product (SKU) | Aggregate Root | 옵션매핑, 가격, 재고 |
| ProductNotice | Aggregate Root | 고시정보 항목 리스트 |
| ChannelOptionMapping | Aggregate Root | salesChannel + canonical → externalCode |
| CanonicalOption | Aggregate Root (read-only) | DB migration으로만 관리 |

---

## Command UseCase (8개)

| # | UseCase | 관련 Aggregate | 복잡도 | 비고 |
|---|---------|----------------|--------|------|
| C1 | RegisterProductGroup | PG+Desc+Product+Notice | 높음 | 일괄 등록, DRAFT |
| C2 | UpdateProductGroup | PG+Desc+Product+Notice | 높음 | 변경감지, SKU 재생성 |
| C3 | ChangeProductGroupStatus | ProductGroup | 중간 | activate/deactivate/markSoldOut |
| C4 | UpdateProductPrice | Product | 낮음 | 가격 정합성 검증 |
| C5 | UpdateProductStock | Product | 낮음 | 재고 수량 변경 |
| C6 | ChangeProductStatus | Product | 낮음 | activate/deactivate/markSoldOut |
| C7 | RegisterChannelOptionMapping | ChannelOptionMapping | 낮음 | 유니크 검증 |
| C8 | UpdateChannelOptionMapping | ChannelOptionMapping | 낮음 | externalCode 변경 |

## Query UseCase (5개)

| # | UseCase | 조회 범위 | 비고 |
|---|---------|----------|------|
| Q1 | GetProductGroup | PG+Desc+Images+Options+Products+Notice | 단건 상세 |
| Q2 | SearchProductGroupByOffset | PG 요약 (Desc 제외) | 목록 페이징 |
| Q3 | GetProduct | Product+OptionMappings | SKU 단건 |
| Q4 | SearchProductsByProductGroup | ProductGroupId별 SKU 목록 | 목록 |
| Q5 | SearchChannelOptionMappingByOffset | 채널 매핑 목록 | 채널별 필터 |

---

## Port Out 인터페이스

### Command Port

```
ProductGroupCommandPort
  - Long persist(ProductGroup)

ProductGroupDescriptionCommandPort
  - Long persist(ProductGroupDescription)

ProductCommandPort
  - Long persist(Product)
  - List<Long> persistAll(List<Product>)

ProductNoticeCommandPort
  - Long persist(ProductNotice)

ChannelOptionMappingCommandPort
  - Long persist(ChannelOptionMapping)
```

### Query Port

```
ProductGroupQueryPort
  - Optional<ProductGroup> findById(ProductGroupId)
  - List<ProductGroup> findByCriteria(ProductGroupSearchCriteria)
  - long countByCriteria(ProductGroupSearchCriteria)

ProductGroupDescriptionQueryPort
  - Optional<ProductGroupDescription> findByProductGroupId(ProductGroupId)

ProductQueryPort
  - Optional<Product> findById(ProductId)
  - List<Product> findByProductGroupId(ProductGroupId)

ProductNoticeQueryPort
  - Optional<ProductNotice> findByProductGroupId(ProductGroupId)

CanonicalOptionQueryPort
  - Optional<CanonicalOptionGroup> findById(CanonicalOptionGroupId)
  - Optional<CanonicalOptionValue> findValueById(CanonicalOptionValueId)

ChannelOptionMappingQueryPort
  - List<ChannelOptionMapping> findByCriteria(ChannelOptionMappingSearchCriteria)
  - long countByCriteria(ChannelOptionMappingSearchCriteria)
  - boolean existsBySalesChannelIdAndCanonicalOptionValueId(SalesChannelId, CanonicalOptionValueId)
```

---

## Manager 목록

```
manager/
  ├── ProductGroupCommandManager          (persist)
  ├── ProductGroupReadManager             (findById, findByCriteria, count)
  ├── ProductGroupDescriptionCommandManager (persist)
  ├── ProductGroupDescriptionReadManager  (findByProductGroupId)
  ├── ProductCommandManager              (persist, persistAll)
  ├── ProductReadManager                 (findById, findByProductGroupId)
  ├── ProductNoticeCommandManager        (persist)
  ├── ProductNoticeReadManager           (findByProductGroupId)
  ├── CanonicalOptionReadManager         (findById, findValueById)
  ├── ChannelOptionMappingCommandManager (persist)
  └── ChannelOptionMappingReadManager    (findByCriteria, count, exists)
```

---

## Factory 목록

```
factory/
  ├── ProductGroupCommandFactory    (RegisterCommand → Domain 변환, Bundle 생성)
  ├── ProductGroupQueryFactory      (SearchParams → Criteria 변환)
  ├── ProductCommandFactory         (옵션 조합 → SKU 생성, 가격/재고 DTO → Domain)
  ├── ProductNoticeCommandFactory   (고시정보 Command → Domain)
  └── ChannelOptionMappingCommandFactory (Command → Domain)
```

---

## Assembler 목록

```
assembler/
  ├── ProductGroupAssembler         (Domain → ProductGroupResult, ProductGroupSummary)
  ├── ProductAssembler              (Domain → ProductResult)
  └── ChannelOptionMappingAssembler (Domain → ChannelOptionMappingResult)
```

---

## Validator 목록

```
validator/
  ├── ProductGroupValidator         (존재 검증, 활성화 검증)
  ├── ProductValidator              (존재 검증, 가격 정합성)
  └── ChannelOptionMappingValidator (중복 검증)
```

---

## Internal (크로스 Aggregate)

```
internal/
  ├── ProductGroupCommandFacade          (@Transactional, 여러 Manager 오케스트레이션)
  ├── ProductGroupRegistrationCoordinator (검증 → Facade 등록)
  └── ProductGroupUpdateCoordinator      (검증 → 변경감지 → Facade 수정)
```

---

## DTO 구조

```
dto/
  ├── command/
  │     ├── RegisterProductGroupCommand    (그룹+설명+이미지+옵션+SKU+고시정보)
  │     ├── UpdateProductGroupCommand      (전체 수정용, 동일 구조)
  │     ├── ChangeProductGroupStatusCommand (targetStatus)
  │     ├── UpdateProductPriceCommand      (productId, 가격 필드들)
  │     ├── UpdateProductStockCommand      (productId, stockQuantity)
  │     ├── ChangeProductStatusCommand     (productId, targetStatus)
  │     ├── RegisterChannelOptionMappingCommand
  │     └── UpdateChannelOptionMappingCommand
  │
  ├── query/
  │     ├── ProductGroupSearchParams       (CommonSearchParams 포함)
  │     ├── ChannelOptionMappingSearchParams
  │     └── (단건 조회는 ID 파라미터로 처리)
  │
  ├── result/
  │     ├── ProductGroupDetailResult       (상세 조회 - 전체 포함)
  │     ├── ProductGroupSummaryResult      (목록 조회 - 요약)
  │     ├── ProductResult
  │     └── ChannelOptionMappingResult
  │
  └── bundle/
        ├── ProductGroupRegistrationBundle (일괄 등록용)
        └── ProductGroupUpdateBundle       (일괄 수정용, 변경감지 포함)
```

---

## 구현 순서

### Phase 1: 기반 구조 (Port + Manager + DTO)
```
1-1. Command Port 인터페이스 (5개)
1-2. Query Port 인터페이스 (6개)
1-3. Command Manager (6개)
1-4. Read Manager (5개)
1-5. Command DTO (8개)
1-6. Query DTO (2개)
1-7. Result DTO (4개)
```

### Phase 2: 단순 Command UseCase
```
2-1. C4: UpdateProductPrice        (Service + Validator)
2-2. C5: UpdateProductStock        (Service)
2-3. C6: ChangeProductStatus       (Service)
2-4. C3: ChangeProductGroupStatus  (Service + Validator)
2-5. C7: RegisterChannelOptionMapping (Service + Factory + Validator)
2-6. C8: UpdateChannelOptionMapping   (Service + Validator)
```

### Phase 3: Query UseCase
```
3-1. Q3: GetProduct                   (Service + Assembler)
3-2. Q4: SearchProductsByProductGroup (Service + Assembler)
3-3. Q5: SearchChannelOptionMappingByOffset (Service + Factory + Assembler)
3-4. Q1: GetProductGroup              (Service + Assembler) ⭐ 복합 조회
3-5. Q2: SearchProductGroupByOffset   (Service + Factory + Assembler)
```

### Phase 4: 핵심 Command UseCase
```
4-1. C1: RegisterProductGroup  (Factory + Coordinator + Facade + Bundle)
4-2. C2: UpdateProductGroup    (Factory + Coordinator + Facade + Bundle + 변경감지)
```

---

## 패턴 규칙 (기존 코드 기준)

| 항목 | 규칙 |
|------|------|
| Service | `@Service`, UseCase 인터페이스 구현 |
| Manager/Factory/Assembler/Validator | `@Component` |
| Facade | `@Component` + `@Transactional` |
| ReadManager | `@Transactional(readOnly = true)` |
| DTO | `public record` (중첩 record 허용) |
| Bundle | `class` (가변, Aggregate 묶음) |
| Factory | TimeProvider 사용처 (APP-TIM-001) |
| Validator | 성공 시 Domain 반환 (APP-VAL-001), 도메인 예외만 (APP-VAL-002) |
| SearchParams | CommonSearchParams 포함 필수 (APP-DTO-003) |
| UseCase 메서드 | `execute()` |

---

## 상태 전이 정리

### ProductGroupStatus
```
DRAFT → ACTIVE (activate, THUMBNAIL 필수)
ACTIVE → INACTIVE (deactivate)
ACTIVE → SOLDOUT (markSoldOut)
INACTIVE → ACTIVE (activate)
SOLDOUT → ACTIVE (activate)
* → DELETED (delete, 이미 DELETED 제외)
```

### ProductStatus
```
ACTIVE (생성 시 기본)
ACTIVE → INACTIVE (deactivate)
ACTIVE → SOLDOUT (markSoldOut)
INACTIVE → ACTIVE (activate)
SOLDOUT → ACTIVE (activate)
* → DELETED (delete, 이미 DELETED 제외)
```
