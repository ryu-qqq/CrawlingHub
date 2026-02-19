# ProductNotice Domain API Flow Analysis

ProductNotice 도메인의 전체 API 호출 흐름 분석 문서입니다.

---

## 엔드포인트 목록

| HTTP Method | Path | 설명 | Controller Method |
|-------------|------|------|-------------------|
| PUT | `/api/v1/market/product-groups/{productGroupId}/notice` | 고시정보 수정 (없으면 생성) | `updateNotice()` |

> **참고**: `RegisterProductNoticeUseCase`는 내부 연동용 UseCase로 ProductGroup 등록 흐름에서 호출됩니다.
> 독립적인 REST 엔드포인트는 존재하지 않으나, 흐름 완전성을 위해 함께 분석합니다.

---

## C1. PUT /api/v1/market/product-groups/{productGroupId}/notice - 고시정보 수정

### 기본 정보

| 항목 | 값 |
|------|-----|
| HTTP Method | PUT |
| Path | `/api/v1/market/product-groups/{productGroupId}/notice` |
| Controller | `ProductNoticeCommandController` |
| Method | `updateNotice` |
| UseCase | `UpdateProductNoticeUseCase` |
| Service | `UpdateProductNoticeService` |
| 응답 | `ResponseEntity<Void>` (204 No Content) |
| 권한 | `@PreAuthorize("@access.isSellerOwnerOr(#productGroupId, 'product-group:write')")` + `@RequirePermission("product-group:write")` |

### 호출 흐름 다이어그램

```
[Adapter-In]
ProductNoticeCommandController.updateNotice(productGroupId, UpdateProductNoticeApiRequest)
  ├─ ProductNoticeCommandApiMapper.toCommand(productGroupId, request)  [Command 변환]
  │   └─ -> UpdateProductNoticeCommand(productGroupId, noticeCategoryId, entries)
  └─ UpdateProductNoticeUseCase.execute(command)                       [Port Interface]

[Application]
UpdateProductNoticeService.execute(command)                            [UseCase 구현]
  └─ ProductNoticeCommandCoordinator.update(command)                   [조율]
      ├─ ProductNoticeReadManager.getByProductGroupId(pgId)            [기존 Notice 조회]
      │   └─ ProductNoticeQueryPort.findByProductGroupId(pgId)         [Port]
      ├─ ProductNoticeCommandFactory.createUpdateData(command)         [UpdateData 생성]
      │   └─ -> ProductNoticeUpdateData(noticeCategoryId, entries, now)
      ├─ NoticeEntriesValidator.validate(updateData)                   [검증]
      │   └─ NoticeCategoryReadManager.getById(noticeCategoryId)       [카테고리+필드 조회]
      ├─ existing.update(updateData)                                   [Domain 수정]
      └─ ProductNoticeCommandFacade.persist(existing)                  [저장 조율, @Transactional]
          ├─ ProductNoticeCommandManager.persist(productNotice)
          │   └─ ProductNoticeCommandPort.persist(productNotice)       [Port]
          ├─ assignNoticeIdToEntries(noticeId, entries)                [ID 전파]
          └─ ProductNoticeEntryCommandManager.persistAll(entries)
              └─ ProductNoticeEntryCommandPort.persist(entry)          [Port, 항목별 반복]

[Adapter-Out - Query]
ProductNoticeQueryAdapter.findByProductGroupId(productGroupId)         [Port 구현]
  ├─ ProductNoticeQueryDslRepository.findByProductGroupId(productGroupId)
  │   └─ QueryDSL: SELECT FROM product_notices WHERE product_group_id = ?
  └─ ProductNoticeQueryDslRepository.findEntriesByProductNoticeId(noticeId)
      └─ QueryDSL: SELECT FROM product_notice_entries WHERE product_notice_id = ?
  → ProductNoticeJpaEntityMapper.toDomain(entity, entries) → ProductNotice

[Adapter-Out - Command]
ProductNoticeCommandAdapter.persist(productNotice)                     [Port 구현]
  ├─ ProductNoticeJpaEntityMapper.toEntity(domain) → ProductNoticeJpaEntity
  └─ ProductNoticeJpaRepository.save(entity)
      └─ INSERT/UPDATE INTO product_notices

ProductNoticeEntryCommandAdapter.persist(entry)                        [Port 구현, 항목별 반복]
  ├─ ProductNoticeJpaEntityMapper.toEntryEntity(entry) → ProductNoticeEntryJpaEntity
  └─ ProductNoticeEntryJpaRepository.save(entity)
      └─ INSERT INTO product_notice_entries

[Database]
- product_notices (고시정보 헤더)
- product_notice_entries (고시정보 항목, 항목 수만큼 INSERT)
```

### Layer별 상세

#### Adapter-In Layer

- **Controller**: `ProductNoticeCommandController`
  - 위치: `adapter-in/rest-api/.../productnotice/controller/`
  - 어노테이션: `@RestController`, `@RequestMapping(PRODUCT_GROUPS)`
  - Method: `updateNotice(Long productGroupId, @Valid UpdateProductNoticeApiRequest request)`
  - 반환: `ResponseEntity<Void>` (204 No Content)
  - `@Transactional` 없음 (API-CTR-005 준수)

- **Request DTO**: `UpdateProductNoticeApiRequest`
  ```java
  record UpdateProductNoticeApiRequest(
      @NotNull Long noticeCategoryId,       // 고시 카테고리 ID
      @NotNull @Valid List<NoticeEntryRequest> entries  // 고시 항목 목록 (1개 이상)
  )

  record NoticeEntryRequest(
      @NotNull Long noticeFieldId,          // 고시 필드 ID
      @NotBlank String fieldValue           // 고시 필드 값 (공백 불가)
  )
  ```

- **Response DTO**: 없음 (204 No Content)

- **ApiMapper**: `ProductNoticeCommandApiMapper`
  - `toCommand(Long productGroupId, UpdateProductNoticeApiRequest)` → `UpdateProductNoticeCommand`
  - entries 스트림 변환: `NoticeEntryRequest` → `NoticeEntryCommand`
  - 순수 변환 로직만 보유 (`@Component`)

- **Endpoints 상수**: `ProductNoticeAdminEndpoints`
  ```java
  PRODUCT_GROUPS = "/api/v1/market/product-groups"
  ID             = "/{productGroupId}"
  NOTICE         = "/notice"
  PATH_PRODUCT_GROUP_ID = "productGroupId"
  // 조합: PUT /api/v1/market/product-groups/{productGroupId}/notice
  ```

- **Error Mapper**: `ProductNoticeErrorMapper`
  - 지원 코드: `PRDNTC-` 접두사
  - `PRDNTC-001` → 404 Not Found (`ProductNoticeNotFoundException`)

#### Application Layer

- **UseCase Interface**: `UpdateProductNoticeUseCase`
  ```java
  void execute(UpdateProductNoticeCommand command)
  ```

- **Service 구현**: `UpdateProductNoticeService`
  - `@Service`, Coordinator에게 전임 위임
  - 직접 비즈니스 로직 없음 (단순 위임)

- **Command DTO**: `UpdateProductNoticeCommand`
  ```java
  record UpdateProductNoticeCommand(
      long productGroupId,
      long noticeCategoryId,
      List<NoticeEntryCommand> entries
  )

  record NoticeEntryCommand(long noticeFieldId, String fieldValue)
  ```

- **Coordinator**: `ProductNoticeCommandCoordinator` (`@Component`)
  - `update(UpdateProductNoticeCommand)` 메서드 (`@Transactional`)
    1. `ProductNoticeReadManager.getByProductGroupId(pgId)` → 기존 Notice 조회 (없으면 `ProductNoticeNotFoundException`)
    2. `ProductNoticeCommandFactory.createUpdateData(command)` → `ProductNoticeUpdateData` 생성
    3. `NoticeEntriesValidator.validate(updateData)` → 카테고리 필드 검증
    4. `existing.update(updateData)` → Domain 수정 (entries 전체 교체, updatedAt 갱신)
    5. `ProductNoticeCommandFacade.persist(existing)` → 저장

- **Factory**: `ProductNoticeCommandFactory` (`@Component`)
  - `createUpdateData(UpdateProductNoticeCommand)` → `ProductNoticeUpdateData`
    - 각 entry를 `ProductNoticeEntry.forNew(noticeFieldId, fieldValue)` 생성
    - `TimeProvider.now()` 사용 (APP-TIM-001: 시간은 Factory에서만)
  - `create(RegisterProductNoticeCommand)` → `ProductNotice`

- **Validator**: `NoticeEntriesValidator` (`@Component`)
  - `NoticeCategoryReadManager.getById(noticeCategoryId)`: 카테고리 + 필드 목록 조회
  - 단일 패스 검증:
    - 유효하지 않은 fieldId → `NoticeInvalidFieldException`
    - 필수 필드 누락 → `NoticeRequiredFieldMissingException`

- **Facade**: `ProductNoticeCommandFacade` (`@Component`, `@Transactional`)
  - `persist(ProductNotice)`:
    1. `ProductNoticeCommandManager.persist(productNotice)` → notice 저장, noticeId 반환
    2. `assignNoticeIdToEntries(noticeId, entries)`: 각 entry에 `ProductNoticeId` 전파
    3. `ProductNoticeEntryCommandManager.persistAll(entries)`: 항목 전체 저장 (순회 INSERT)

- **Manager**:
  - `ProductNoticeReadManager` (`@Transactional(readOnly = true)`)
    - `findByProductGroupId(ProductGroupId)` → `Optional<ProductNotice>`
    - `getByProductGroupId(ProductGroupId)` → `ProductNotice` (없으면 예외)
  - `ProductNoticeCommandManager` (`@Transactional`)
    - `persist(ProductNotice)` → `Long` (noticeId)
  - `ProductNoticeEntryCommandManager` (`@Transactional`)
    - `persistAll(List<ProductNoticeEntry>)` → entry 순회 저장

- **Ports (출력)**:
  - `ProductNoticeQueryPort.findByProductGroupId(ProductGroupId)` → `Optional<ProductNotice>`
  - `ProductNoticeCommandPort.persist(ProductNotice)` → `Long`
  - `ProductNoticeEntryCommandPort.persist(ProductNoticeEntry)` → `void`

#### Domain Layer

- **Aggregate Root**: `ProductNotice`
  ```java
  class ProductNotice {
      ProductNoticeId id
      ProductGroupId productGroupId
      NoticeCategoryId noticeCategoryId
      List<ProductNoticeEntry> entries
      Instant createdAt
      Instant updatedAt
  }
  ```
  - 주요 메서드:
    - `forNew(productGroupId, noticeCategoryId, entries, now)`: 신규 생성
    - `reconstitute(...)`: 영속성 복원
    - `update(ProductNoticeUpdateData)`: entries 전체 교체 + updatedAt 갱신
    - `assignId(ProductNoticeId)`: 저장 후 ID 전파 (소유 entries에도 전파)

- **Child Entity**: `ProductNoticeEntry`
  ```java
  class ProductNoticeEntry {
      ProductNoticeEntryId id
      ProductNoticeId productNoticeId   // 부모 ID (저장 후 할당)
      NoticeFieldId noticeFieldId
      NoticeFieldValue fieldValue
  }
  ```
  - `forNew(noticeFieldId, fieldValue)`: 신규 항목 생성
  - `assignProductNoticeId(ProductNoticeId)`: 부모 저장 후 ID 할당

- **Value Objects**:
  - `NoticeFieldValue`: 필드 값 VO (null/blank 불가, 500자 제한)
  - `ProductNoticeUpdateData`: 수정 데이터 불변 객체 (noticeCategoryId + entries + updatedAt)
  - `ProductNoticeEntries`: 항목 컬렉션 VO (NoticeCategoryId + List<ProductNoticeEntry>)

- **IDs**:
  - `ProductNoticeId`: 고시정보 루트 ID
  - `ProductNoticeEntryId`: 항목 ID

- **예외**:
  - `ProductNoticeNotFoundException` (`PRDNTC-001`, 404)
    - `ProductGroupId` 기준 조회 실패 시 발생

#### Adapter-Out Layer

- **Query Adapter**: `ProductNoticeQueryAdapter`
  - `findByProductGroupId(ProductGroupId)` → `Optional<ProductNotice>`
  - 2단계 조회 (N+1 방지를 별도 쿼리로 제어):
    1. `ProductNoticeQueryDslRepository.findByProductGroupId()` → `Optional<ProductNoticeJpaEntity>`
    2. (존재 시) `ProductNoticeQueryDslRepository.findEntriesByProductNoticeId()` → `List<ProductNoticeEntryJpaEntity>`
    3. `ProductNoticeJpaEntityMapper.toDomain(entity, entries)` → `ProductNotice`

- **Command Adapters**:
  - `ProductNoticeCommandAdapter` → `ProductNoticeCommandPort`
    - `toEntity(domain)` → `ProductNoticeJpaEntity`
    - `ProductNoticeJpaRepository.save(entity)` → JPA save (INSERT/UPDATE)
  - `ProductNoticeEntryCommandAdapter` → `ProductNoticeEntryCommandPort`
    - `toEntryEntity(entry)` → `ProductNoticeEntryJpaEntity`
    - `ProductNoticeEntryJpaRepository.save(entity)` → JPA save (INSERT)

- **QueryDSL Repository**: `ProductNoticeQueryDslRepository`
  ```java
  // 고시정보 헤더 조회
  queryFactory.selectFrom(productNotice)
              .where(conditionBuilder.productGroupIdEq(productGroupId))
              .fetchOne()

  // 고시정보 항목 조회
  queryFactory.selectFrom(productNoticeEntry)
              .where(productNoticeEntry.productNoticeId.eq(productNoticeId))
              .fetch()
  ```

- **JPA Repositories**:
  - `ProductNoticeJpaRepository extends JpaRepository<ProductNoticeJpaEntity, Long>`
    - `save()` 전용 (커스텀 쿼리 메서드 없음)
  - `ProductNoticeEntryJpaRepository extends JpaRepository<ProductNoticeEntryJpaEntity, Long>`
    - `save()` 외 `deleteByProductNoticeId(Long)` 허용

- **JPA Entities**:
  - `ProductNoticeJpaEntity` → `@Table(name = "product_notices")`
    - `BaseAuditEntity` 상속 (createdAt, updatedAt as `Instant`)
    - FK 컬럼: `product_group_id`, `notice_category_id` (Long 직접 보관)
  - `ProductNoticeEntryJpaEntity` → `@Table(name = "product_notice_entries")`
    - 감사 필드 없음 (독립 관리)
    - FK 컬럼: `product_notice_id`, `notice_field_id`
    - `field_value VARCHAR(500)`

- **Mapper**: `ProductNoticeJpaEntityMapper` (`@Component`)
  - `toEntity(ProductNotice)` → `ProductNoticeJpaEntity`
  - `toEntryEntity(ProductNoticeEntry)` → `ProductNoticeEntryJpaEntity`
  - `toDomain(entity, entryEntities)` → `ProductNotice` (reconstitute 사용)
  - `toEntryDomain(entry)` → `ProductNoticeEntry`

- **Condition Builder**: `ProductNoticeConditionBuilder` (`@Component`)
  - `idEq(Long id)` → `BooleanExpression` (null-safe)
  - `productGroupIdEq(Long productGroupId)` → `BooleanExpression` (null-safe)

### Database Query 분석

```sql
-- 1. 기존 고시정보 헤더 조회
SELECT id, product_group_id, notice_category_id, created_at, updated_at
FROM product_notices
WHERE product_group_id = ?

-- 2. 기존 고시정보 항목 조회
SELECT id, product_notice_id, notice_field_id, field_value
FROM product_notice_entries
WHERE product_notice_id = ?

-- 3. 고시정보 헤더 저장 (신규: INSERT / 기존: UPDATE)
-- 신규 등록 시:
INSERT INTO product_notices (product_group_id, notice_category_id, created_at, updated_at)
VALUES (?, ?, ?, ?)

-- 기존 수정 시 (id가 존재하므로 JPA merge → UPDATE):
UPDATE product_notices
SET notice_category_id = ?, updated_at = ?
WHERE id = ?

-- 4. 고시정보 항목 저장 (항목 수만큼 반복 INSERT)
INSERT INTO product_notice_entries (product_notice_id, notice_field_id, field_value)
VALUES (?, ?, ?)
```

**대상 테이블**:
- `product_notices`: 고시정보 헤더 (productGroupId FK)
- `product_notice_entries`: 고시정보 항목 (productNoticeId FK)

---

## 내부 연동 흐름: RegisterProductNoticeUseCase

> REST 엔드포인트 없음. ProductGroup 등록 흐름(`RegisterProductGroupFullService` 등)에서 호출.

### 호출 흐름 다이어그램

```
[Application - 호출 측]
RegisterProductGroupFullService (또는 ProductGroup 등록 Coordinator)
  └─ RegisterProductNoticeUseCase.execute(RegisterProductNoticeCommand)  [Port Interface]

[Application]
RegisterProductNoticeService.execute(command)                           [UseCase 구현]
  ├─ ProductNoticeCommandFactory.create(command)                        [ProductNotice 생성]
  │   └─ -> ProductNotice.forNew(productGroupId, noticeCategoryId, entries, now)
  ├─ NoticeEntriesValidator.validate(productNotice)                     [카테고리 필드 검증]
  └─ ProductNoticeCommandCoordinator.persist(productNotice)             [저장, @Transactional]
      ├─ ProductNoticeCommandManager.persist(productNotice)
      │   └─ ProductNoticeCommandPort.persist(productNotice)            [Port]
      ├─ productNotice.assignId(ProductNoticeId)                        [ID 전파]
      └─ ProductNoticeEntryCommandManager.persistAll(entries)
          └─ ProductNoticeEntryCommandPort.persist(entry)               [Port, 항목별 반복]

[Adapter-Out]
ProductNoticeCommandAdapter → ProductNoticeJpaRepository.save()
ProductNoticeEntryCommandAdapter → ProductNoticeEntryJpaRepository.save() (반복)
```

### Command DTO: `RegisterProductNoticeCommand`

```java
record RegisterProductNoticeCommand(
    long productGroupId,
    long noticeCategoryId,
    List<NoticeEntryCommand> entries
)

record NoticeEntryCommand(long noticeFieldId, String fieldValue)
```

---

## 공통 패턴 분석

### 1. Hexagonal Architecture 레이어 격리

| 레이어 | DTO/모델 | 책임 |
|--------|----------|------|
| **Adapter-In** | `UpdateProductNoticeApiRequest` | HTTP 계층 (Validation, 직렬화) |
| **Application** | `UpdateProductNoticeCommand`, `ProductNoticeUpdateData` | 유스케이스 조율, 트랜잭션 경계 |
| **Domain** | `ProductNotice`, `ProductNoticeEntry`, `NoticeFieldValue` | 비즈니스 규칙, 불변성 |
| **Adapter-Out** | `ProductNoticeJpaEntity`, `ProductNoticeEntryJpaEntity` | 영속화 기술 (JPA) |

### 2. 트랜잭션 경계

| 계층 | @Transactional 위치 | 비고 |
|------|---------------------|------|
| Controller | 없음 | API-CTR-005 준수 |
| Service | 없음 | Coordinator에 위임 |
| **Coordinator** | `@Transactional` | `update()`, `register()`, `persist()` |
| **Facade** | `@Transactional` | `persist()` - Notice + Entry 원자적 저장 |
| **Manager** | `@Transactional(readOnly=true)` / `@Transactional` | 조회/저장 분리 |
| Adapter-Out | 없음 | PER-ADP 준수 |

### 3. Coordinator 패턴 (update 흐름)

```
update(command):
  1. 기존 Notice 조회 (ReadManager)     ← 없으면 404 예외
  2. UpdateData 생성 (Factory)
  3. 카테고리 필드 검증 (Validator)     ← 유효하지 않은 fieldId → 예외
  4. Domain update() 호출              ← entries 전체 교체
  5. Facade.persist() 호출             ← 저장
```

### 4. 저장 전략: UPDATE 흐름의 Entry 처리

현재 `update` 흐름에서:
- `ProductNotice.update(updateData)`: 도메인 내 entries 목록을 **전체 교체** (clear → addAll)
- `ProductNoticeCommandFacade.persist()`: notice 헤더 저장 후 새 entries INSERT
- 기존 entries는 **별도 삭제 없이** 새 항목 INSERT (`deleteByProductNoticeId` 미호출)

> 주의: `ProductNoticeEntryJpaRepository.deleteByProductNoticeId()`가 선언되어 있으나 현재 update 흐름에서 미사용. 기존 항목이 DB에 남아있는 구조임.

### 5. ID 전파 패턴

신규 등록 시 부모-자식 ID 전파 2가지 경로:
- `ProductNotice.assignId(noticeId)`: 소유 entries 일괄 전파
- `ProductNoticeCommandFacade.assignNoticeIdToEntries()`: Facade에서도 전파 (중복 방지 고려 필요)

### 6. 검증 흐름 (NoticeEntriesValidator)

```
validate(updateData):
  1. NoticeCategoryReadManager.getById(noticeCategoryId) → NoticeCategory + fields 조회
  2. validFieldIds: 카테고리에 속한 fieldId Set 구성
  3. requiredFieldIds: 필수 필드 Set 구성
  4. 단일 패스로:
     - 유효하지 않은 fieldId 수집
     - requiredFieldIds에서 제출된 fieldId 제거
  5. invalidFieldIds 비어있지 않으면 NoticeInvalidFieldException
  6. requiredFieldIds 남아있으면 NoticeRequiredFieldMissingException
```

### 7. 변환 체인

```
[Command 흐름]
UpdateProductNoticeApiRequest (Adapter-In)
  → UpdateProductNoticeCommand (Application)
  → ProductNoticeUpdateData (Domain VO)
  → ProductNotice.update()
  → ProductNoticeJpaEntity + ProductNoticeEntryJpaEntity (Adapter-Out)
  → product_notices + product_notice_entries (Database)
```

---

## 에러 코드

| 코드 | HTTP | 설명 | 발생 위치 |
|------|------|------|-----------|
| `PRDNTC-001` | 404 | 상품 고시정보를 찾을 수 없습니다 | `ProductNoticeReadManager.getByProductGroupId()` |

---

**분석 일시**: 2026-02-18
**분석 대상**: ProductNotice 도메인 전 레이어
**총 엔드포인트**: 1개 (Command 1개, Query 0개)
