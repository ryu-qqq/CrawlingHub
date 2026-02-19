# Notice Domain API Flow Analysis

고시정보(Notice Category) 도메인의 전체 API 호출 흐름 분석 문서입니다.

---

## 엔드포인트 목록

| HTTP Method | Path | 설명 | Controller Method |
|-------------|------|------|-------------------|
| GET | `/api/v1/market/notice-categories` | 고시정보 카테고리 목록 조회 (Offset 페이징) | `searchNoticeCategoriesByOffset()` |
| GET | `/api/v1/market/notice-categories/category-group/{categoryGroup}` | 카테고리 그룹별 고시정보 단건 조회 | `getNoticeCategoryByCategoryGroup()` |

> Command 엔드포인트 없음. 고시정보 카테고리는 관리자가 시스템 레벨에서 관리하는 마스터 데이터로, REST API를 통한 생성/수정/삭제가 제공되지 않습니다.

---

## 1. GET /api/v1/market/notice-categories - 고시정보 카테고리 목록 조회

### 호출 흐름 다이어그램

```
[Adapter-In]
NoticeCategoryQueryController.searchNoticeCategoriesByOffset(request)
  ├─ NoticeCategoryQueryApiMapper.toSearchParams(request)       [Params 변환]
  │   └─ -> NoticeCategorySearchParams
  ├─ SearchNoticeCategoryByOffsetUseCase.execute(params)        [Port Interface]
  └─ NoticeCategoryQueryApiMapper.toPageResponse(pageResult)    [Response 변환]

[Application]
SearchNoticeCategoryByOffsetService.execute(params)             [UseCase 구현]
  ├─ NoticeCategoryQueryFactory.createCriteria(params)          [Criteria 생성]
  │   └─ -> NoticeCategorySearchCriteria
  ├─ NoticeCategoryReadManager.findByCriteria(criteria)         [@Transactional readOnly]
  │   └─ NoticeCategoryQueryPort.findByCriteria()               [Port]
  ├─ NoticeCategoryReadManager.countByCriteria(criteria)        [@Transactional readOnly]
  │   └─ NoticeCategoryQueryPort.countByCriteria()              [Port]
  └─ NoticeCategoryAssembler.toResult() × N + toPageResult()    [Result 조합]

[Domain]
NoticeCategoryQueryPort (interface)
  ├─ findByCriteria(NoticeCategorySearchCriteria) -> List<NoticeCategory>
  └─ countByCriteria(NoticeCategorySearchCriteria) -> long

[Adapter-Out]
NoticeCategoryQueryAdapter                                       [Port 구현]
  ├─ NoticeCategoryQueryDslRepository.findByCriteria(criteria)
  │   └─ QueryDSL: WHERE active=? AND (code/name LIKE ?) ORDER BY createdAt/code LIMIT ? OFFSET ?
  ├─ [카테고리 ID 목록 추출]
  ├─ NoticeCategoryQueryDslRepository.findFieldsByCategoryIds(categoryIds)
  │   └─ QueryDSL: WHERE notice_category_id IN (?) ORDER BY sort_order ASC
  └─ NoticeCategoryJpaEntityMapper.toDomain(category, fields) × N
      └─ NoticeFieldJpaEntityMapper.toDomain(fieldEntity) × N

[Database]
- notice_category (목록 조회 + 카운트)
- notice_field (카테고리 ID 목록으로 In-Query 일괄 조회)
```

### Layer별 상세

#### Adapter-In Layer

- **Controller**: `NoticeCategoryQueryController`
  - 파일: `adapter-in/rest-api/src/main/java/com/ryuqq/marketplace/adapter/in/rest/notice/controller/NoticeCategoryQueryController.java`
  - Method: `searchNoticeCategoriesByOffset(@ParameterObject @Valid SearchNoticeCategoriesApiRequest)`
  - 권한: `@RequirePermission("notice-category:read")`
  - Response: `ResponseEntity<ApiResponse<PageApiResponse<NoticeCategoryApiResponse>>>`
  - HTTP Status: 200 OK

- **Request DTO**: `SearchNoticeCategoriesApiRequest`
  - 파일: `adapter-in/rest-api/src/main/java/com/ryuqq/marketplace/adapter/in/rest/notice/dto/query/SearchNoticeCategoriesApiRequest.java`
  ```java
  record SearchNoticeCategoriesApiRequest(
    Boolean active,          // 활성 상태 필터 (null이면 전체)
    String searchField,      // 검색 필드: CODE, NAME_KO, NAME_EN
    String searchWord,       // 검색어
    String sortKey,          // 정렬 키: createdAt, code (null이면 createdAt)
    String sortDirection,    // 정렬 방향: ASC, DESC (null이면 DESC)
    Integer page,            // 페이지 번호 (null이면 0)
    Integer size             // 페이지 크기 (null이면 20)
  )
  ```

- **Response DTO**: `NoticeCategoryApiResponse` (inside `PageApiResponse`)
  - 파일: `adapter-in/rest-api/src/main/java/com/ryuqq/marketplace/adapter/in/rest/notice/dto/response/NoticeCategoryApiResponse.java`
  ```java
  record NoticeCategoryApiResponse(
    Long id,                          // 카테고리 ID
    String code,                      // 카테고리 코드
    String nameKo,                    // 한글명
    String nameEn,                    // 영문명
    String targetCategoryGroup,       // 대상 카테고리 그룹 (CategoryGroup enum name)
    boolean active,                   // 활성 상태
    List<NoticeFieldApiResponse> fields,  // 고시정보 필드 목록
    String createdAt                  // ISO 8601 형식
  )

  record NoticeFieldApiResponse(
    Long id,          // 필드 ID
    String fieldCode, // 필드 코드
    String fieldName, // 필드명
    boolean required, // 필수 여부
    int sortOrder     // 정렬 순서
  )
  ```

- **ApiMapper**: `NoticeCategoryQueryApiMapper`
  - 파일: `adapter-in/rest-api/src/main/java/com/ryuqq/marketplace/adapter/in/rest/notice/mapper/NoticeCategoryQueryApiMapper.java`
  - `toSearchParams(request)`: 기본값 처리 (page=0, size=20), `CommonSearchParams` 생성 후 `NoticeCategorySearchParams.of()` 반환
  - `toPageResponse(pageResult)`: `NoticeCategoryResult` 목록 → `NoticeCategoryApiResponse` 목록 변환, `PageApiResponse.of()` 조합
  - `toResponse(result)`: `NoticeCategoryResult` → `NoticeCategoryApiResponse` 변환, `createdAt`은 `DateTimeFormatUtils.formatIso8601()` 처리

#### Application Layer

- **UseCase Interface**: `SearchNoticeCategoryByOffsetUseCase`
  - 파일: `application/src/main/java/com/ryuqq/marketplace/application/notice/port/in/query/SearchNoticeCategoryByOffsetUseCase.java`
  - `execute(NoticeCategorySearchParams)` → `NoticeCategoryPageResult`

- **Service 구현**: `SearchNoticeCategoryByOffsetService`
  - 파일: `application/src/main/java/com/ryuqq/marketplace/application/notice/service/query/SearchNoticeCategoryByOffsetService.java`
  - `@Service`
  - 순서: QueryFactory → ReadManager(find) → ReadManager(count) → Assembler

- **Params DTO**: `NoticeCategorySearchParams`
  - 파일: `application/src/main/java/com/ryuqq/marketplace/application/notice/dto/query/NoticeCategorySearchParams.java`
  ```java
  record NoticeCategorySearchParams(
    Boolean active,
    String searchField,
    String searchWord,
    CommonSearchParams commonSearchParams  // page, size, sortKey, sortDirection 포함
  )
  ```

- **Factory**: `NoticeCategoryQueryFactory`
  - 파일: `application/src/main/java/com/ryuqq/marketplace/application/notice/factory/NoticeCategoryQueryFactory.java`
  - `createCriteria(params)` → `NoticeCategorySearchCriteria`
  - `NoticeCategorySortKey` 파싱 (기본값: `CREATED_AT`)
  - `CommonVoFactory`를 통해 `PageRequest`, `QueryContext` 생성

- **Manager**: `NoticeCategoryReadManager`
  - 파일: `application/src/main/java/com/ryuqq/marketplace/application/notice/manager/NoticeCategoryReadManager.java`
  - `@Component`
  - `findByCriteria(criteria)`: `@Transactional(readOnly = true)`, Port 위임
  - `countByCriteria(criteria)`: `@Transactional(readOnly = true)`, Port 위임

- **Assembler**: `NoticeCategoryAssembler`
  - 파일: `application/src/main/java/com/ryuqq/marketplace/application/notice/assembler/NoticeCategoryAssembler.java`
  - `toResult(NoticeCategory)` → `NoticeCategoryResult`
    - `category.fields()` → 각 `NoticeField` → `NoticeFieldResult` 변환
    - `category.targetCategoryGroup().name()` (enum → String)
  - `toPageResult(results, page, size, totalElements)` → `NoticeCategoryPageResult`

- **Result DTO**: `NoticeCategoryPageResult`
  - 파일: `application/src/main/java/com/ryuqq/marketplace/application/notice/dto/response/NoticeCategoryPageResult.java`
  ```java
  record NoticeCategoryPageResult(
    List<NoticeCategoryResult> results,
    PageMeta pageMeta    // page, size, totalElements
  )

  record NoticeCategoryResult(
    Long id,
    String code,
    String nameKo,
    String nameEn,
    String targetCategoryGroup,  // CategoryGroup.name()
    boolean active,
    List<NoticeFieldResult> fields,
    Instant createdAt
  )

  record NoticeFieldResult(
    Long id, String fieldCode, String fieldName, boolean required, int sortOrder
  )
  ```

#### Domain Layer

- **Port**: `NoticeCategoryQueryPort`
  - 파일: `application/src/main/java/com/ryuqq/marketplace/application/notice/port/out/query/NoticeCategoryQueryPort.java`
  - `findByCriteria(NoticeCategorySearchCriteria)` → `List<NoticeCategory>`
  - `countByCriteria(NoticeCategorySearchCriteria)` → `long`

- **Criteria**: `NoticeCategorySearchCriteria`
  - 파일: `domain/src/main/java/com/ryuqq/marketplace/domain/notice/query/NoticeCategorySearchCriteria.java`
  ```java
  record NoticeCategorySearchCriteria(
    Boolean active,
    String searchField,
    String searchWord,
    QueryContext<NoticeCategorySortKey> queryContext
  )
  // 헬퍼: hasActiveFilter(), hasSearchFilter(), size(), offset(), page()
  ```

- **SortKey**: `NoticeCategorySortKey`
  - `CREATED_AT("createdAt")` (기본값), `CODE("code")`

- **Aggregate Root**: `NoticeCategory`
  - 파일: `domain/src/main/java/com/ryuqq/marketplace/domain/notice/aggregate/NoticeCategory.java`
  - 필드: `NoticeCategoryId`, `NoticeCategoryCode`, `NoticeCategoryName`, `CategoryGroup`, `active`, `List<NoticeField>`, `createdAt`, `updatedAt`
  - `reconstitute()`: 영속성 복원 팩토리 메서드 (ReadOnly 조회 시 사용)
  - `fields()`: 불변 리스트 반환 (`Collections.unmodifiableList`)

- **NoticeField (자식 엔티티)**: `NoticeField`
  - 파일: `domain/src/main/java/com/ryuqq/marketplace/domain/notice/aggregate/NoticeField.java`
  - 필드: `NoticeFieldId`, `NoticeFieldCode`, `NoticeFieldName`, `required`, `sortOrder`
  - `reconstitute()`: 영속성 복원 팩토리 메서드

#### Adapter-Out Layer

- **Adapter**: `NoticeCategoryQueryAdapter`
  - 파일: `adapter-out/persistence-mysql/src/main/java/com/ryuqq/marketplace/adapter/out/persistence/notice/adapter/NoticeCategoryQueryAdapter.java`
  - `@Component`, `implements NoticeCategoryQueryPort`
  - `findByCriteria()` 전략:
    1. `QueryDslRepository.findByCriteria()` → `List<NoticeCategoryJpaEntity>`
    2. 카테고리 ID 목록 추출
    3. `QueryDslRepository.findFieldsByCategoryIds()` → `List<NoticeFieldJpaEntity>` (In-Query 일괄 조회, N+1 방지)
    4. `Collectors.groupingBy(noticeCategoryId)` → `Map<Long, List<NoticeFieldJpaEntity>>`
    5. 카테고리별로 해당 필드 매핑 후 `mapper.toDomain()` 호출

- **QueryDSL Repository**: `NoticeCategoryQueryDslRepository`
  - 파일: `adapter-out/persistence-mysql/src/main/java/com/ryuqq/marketplace/adapter/out/persistence/notice/repository/NoticeCategoryQueryDslRepository.java`
  - `findByCriteria()`: `ConditionBuilder` 조건 + 정렬 + 페이징
  - `countByCriteria()`: 동일 조건으로 `COUNT(*)`
  - `findFieldsByCategoryIds()`: `notice_category_id IN (?)` + `sort_order ASC` 정렬

- **Condition Builder**: `NoticeCategoryConditionBuilder`
  - 파일: `adapter-out/persistence-mysql/src/main/java/com/ryuqq/marketplace/adapter/out/persistence/notice/condition/NoticeCategoryConditionBuilder.java`
  - `activeEq(criteria)`: `active` 필터 (null이면 조건 제외)
  - `searchCondition(criteria)`: `CODE` → `code LIKE ?`, `NAME_KO` → `nameKo LIKE ?`, `NAME_EN` → `nameEn LIKE ?`

- **Mapper**: `NoticeCategoryJpaEntityMapper`
  - 파일: `adapter-out/persistence-mysql/src/main/java/com/ryuqq/marketplace/adapter/out/persistence/notice/mapper/NoticeCategoryJpaEntityMapper.java`
  - `toDomain(NoticeCategoryJpaEntity, List<NoticeFieldJpaEntity>)` → `NoticeCategory`
    - VO 생성: `NoticeCategoryId.of()`, `NoticeCategoryCode.of()`, `NoticeCategoryName.of()`, `CategoryGroup.valueOf()`
    - `NoticeCategory.reconstitute()` 호출
  - 내부적으로 `NoticeFieldJpaEntityMapper.toDomain()` 위임

- **JPA Entity**: `NoticeCategoryJpaEntity`
  - 파일: `adapter-out/persistence-mysql/src/main/java/com/ryuqq/marketplace/adapter/out/persistence/notice/entity/NoticeCategoryJpaEntity.java`
  - 테이블: `notice_category`
  - 주요 컬럼: `id`, `code (UNIQUE)`, `name_ko`, `name_en`, `target_category_group (UNIQUE)`, `active`
  - 상속: `BaseAuditEntity` (`created_at`, `updated_at`)

- **JPA Entity**: `NoticeFieldJpaEntity`
  - 파일: `adapter-out/persistence-mysql/src/main/java/com/ryuqq/marketplace/adapter/out/persistence/notice/entity/NoticeFieldJpaEntity.java`
  - 테이블: `notice_field`
  - 주요 컬럼: `id`, `notice_category_id (FK)`, `field_code`, `field_name`, `required`, `sort_order`

#### Database Query 분석

```sql
-- 목록 조회 (findByCriteria)
SELECT *
FROM notice_category
WHERE active = ?                                  -- 옵션: active 필터 (null이면 제외)
  AND (
      code LIKE '%?%'                             -- searchField=CODE
   OR name_ko LIKE '%?%'                         -- searchField=NAME_KO
   OR name_en LIKE '%?%'                         -- searchField=NAME_EN
  )                                              -- 옵션: 검색 조건 (null이면 제외)
ORDER BY created_at DESC                          -- sortKey=CREATED_AT, sortDirection=DESC (기본값)
    -- OR: code ASC/DESC (sortKey=CODE)
LIMIT ?                                           -- size (기본값 20)
OFFSET ?                                          -- page * size

-- 카운트 쿼리 (countByCriteria) - 동일 WHERE 조건
SELECT COUNT(*)
FROM notice_category
WHERE active = ?
  AND (code/name_ko/name_en LIKE '%?%')

-- 필드 일괄 조회 (findFieldsByCategoryIds) - N+1 방지 In-Query
SELECT *
FROM notice_field
WHERE notice_category_id IN (?, ?, ...)
ORDER BY sort_order ASC
```

---

## 2. GET /api/v1/market/notice-categories/category-group/{categoryGroup} - 카테고리 그룹별 고시정보 단건 조회

### 호출 흐름 다이어그램

```
[Adapter-In]
NoticeCategoryQueryController.getNoticeCategoryByCategoryGroup(categoryGroup)
  ├─ GetNoticeCategoryUseCase.execute(categoryGroup)            [Port Interface]
  └─ NoticeCategoryQueryApiMapper.toResponse(result)            [Response 변환]

[Application]
GetNoticeCategoryService.execute(categoryGroup)                 [UseCase 구현]
  ├─ NoticeCategoryReadManager.getByCategoryGroup(categoryGroup) [@Transactional readOnly]
  │   └─ NoticeCategoryQueryPort.findByCategoryGroup()          [Port]
  │       └─ Optional<NoticeCategory> - 없으면 NoticeCategoryNotFoundException
  └─ NoticeCategoryAssembler.toResult(category)                 [Result 변환]

[Domain]
NoticeCategoryQueryPort (interface)
  └─ findByCategoryGroup(CategoryGroup) -> Optional<NoticeCategory>

[Adapter-Out]
NoticeCategoryQueryAdapter                                       [Port 구현]
  ├─ NoticeCategoryQueryDslRepository.findByTargetCategoryGroup(categoryGroup.name())
  │   └─ QueryDSL: WHERE target_category_group = ?
  └─ [Optional이 present인 경우]
      ├─ NoticeCategoryQueryDslRepository.findFieldsByCategoryId(entity.getId())
      │   └─ QueryDSL: WHERE notice_category_id = ? ORDER BY sort_order ASC
      └─ NoticeCategoryJpaEntityMapper.toDomain(entity, fields)
          └─ NoticeFieldJpaEntityMapper.toDomain(fieldEntity) × N

[Database]
- notice_category (target_category_group 단건 조회)
- notice_field (해당 카테고리의 필드 목록 조회)
```

### Layer별 상세

#### Adapter-In Layer

- **Controller**: `NoticeCategoryQueryController`
  - Method: `getNoticeCategoryByCategoryGroup(@PathVariable CategoryGroup categoryGroup)`
  - Path Variable: `categoryGroup` - Spring MVC가 `CategoryGroup` enum으로 자동 변환
  - 권한: `@RequirePermission("notice-category:read")`
  - Response: `ResponseEntity<ApiResponse<NoticeCategoryApiResponse>>`
  - HTTP Status: 200 OK

- **Request**: Path Variable만 사용, 별도 Request DTO 없음
  - `CategoryGroup` enum: `domain/src/main/java/com/ryuqq/marketplace/domain/category/vo/CategoryGroup.java`

- **Response DTO**: `NoticeCategoryApiResponse`
  ```java
  record NoticeCategoryApiResponse(
    Long id,
    String code,
    String nameKo,
    String nameEn,
    String targetCategoryGroup,
    boolean active,
    List<NoticeFieldApiResponse> fields,
    String createdAt                    // ISO 8601 형식
  )
  ```

- **ApiMapper**: `NoticeCategoryQueryApiMapper`
  - `toResponse(NoticeCategoryResult)` → `NoticeCategoryApiResponse`
  - `fields` 목록: `result.fields().stream().map(this::toFieldResponse).toList()`
  - `createdAt`: `DateTimeFormatUtils.formatIso8601(result.createdAt())`

#### Application Layer

- **UseCase Interface**: `GetNoticeCategoryUseCase`
  - 파일: `application/src/main/java/com/ryuqq/marketplace/application/notice/port/in/query/GetNoticeCategoryUseCase.java`
  - `execute(CategoryGroup categoryGroup)` → `NoticeCategoryResult`

- **Service 구현**: `GetNoticeCategoryService`
  - 파일: `application/src/main/java/com/ryuqq/marketplace/application/notice/service/query/GetNoticeCategoryService.java`
  - `@Service`
  - ReadManager 호출 → Assembler 변환 (단순 2단계)

- **Manager**: `NoticeCategoryReadManager`
  - `getByCategoryGroup(categoryGroup)`: `@Transactional(readOnly = true)`
  - Port 위임 후 `Optional.orElseThrow()` → `NoticeCategoryNotFoundException` (인수 없는 생성자)

- **Assembler**: `NoticeCategoryAssembler`
  - `toResult(NoticeCategory)` → `NoticeCategoryResult`
  - 목록 조회와 동일한 변환 메서드 재사용

#### Domain Layer

- **Port**: `NoticeCategoryQueryPort`
  - `findByCategoryGroup(CategoryGroup)` → `Optional<NoticeCategory>`

- **Aggregate**: `NoticeCategory` (목록 조회와 동일)
  - `reconstitute()` 팩토리 메서드로 복원

- **예외**: `NoticeCategoryNotFoundException`
  - 파일: `domain/src/main/java/com/ryuqq/marketplace/domain/notice/exception/NoticeCategoryNotFoundException.java`
  - 에러 코드: `NOTICE-001`, HTTP 404

#### Adapter-Out Layer

- **Adapter**: `NoticeCategoryQueryAdapter`
  - `findByCategoryGroup(categoryGroup)` → `queryDslRepository.findByTargetCategoryGroup(categoryGroup.name())`
  - `Optional.map()`: entity가 있으면 필드 조회 후 Domain 변환
  - 단건이므로 `findFieldsByCategoryId(entity.getId())` (단일 ID 조회)

- **QueryDSL Repository**:
  - `findByTargetCategoryGroup(String)`: `WHERE target_category_group = ?`, `.fetchOne()`
  - `findFieldsByCategoryId(Long)`: `WHERE notice_category_id = ?`, `ORDER BY sort_order ASC`

#### Database Query 분석

```sql
-- 카테고리 단건 조회 (findByTargetCategoryGroup)
SELECT *
FROM notice_category
WHERE target_category_group = ?   -- CategoryGroup enum name (e.g. 'FASHION')

-- 해당 카테고리의 필드 목록 조회 (findFieldsByCategoryId)
SELECT *
FROM notice_field
WHERE notice_category_id = ?
ORDER BY sort_order ASC
```

---

## 공통 패턴 분석

### Hexagonal Architecture 레이어 격리

| 레이어 | DTO/모델 | 책임 |
|--------|----------|------|
| **Adapter-In** | `SearchNoticeCategoriesApiRequest`, `NoticeCategoryApiResponse`, `NoticeFieldApiResponse` | HTTP 계층 관심사 (Validation, 직렬화, ISO8601 변환) |
| **Application** | `NoticeCategorySearchParams`, `NoticeCategoryPageResult`, `NoticeCategoryResult`, `NoticeFieldResult` | 유스케이스 조율, 트랜잭션 경계 |
| **Domain** | `NoticeCategory`, `NoticeField`, `NoticeCategorySearchCriteria` | 비즈니스 규칙, 불변성, VO |
| **Adapter-Out** | `NoticeCategoryJpaEntity`, `NoticeFieldJpaEntity` | 영속화 기술 관심사 (JPA) |

### CQRS 분리

- **Query Only**: `NoticeCategoryQueryController` → `NoticeCategoryQueryApiMapper` → QueryUseCase → QueryService
- Command 엔드포인트 없음 (마스터 데이터)

### 트랜잭션 경계

| 계층 | @Transactional 위치 |
|------|---------------------|
| Adapter-In | 없음 |
| Application Service | 없음 |
| **Manager** | `@Transactional(readOnly = true)` (조회 메서드마다) |
| Adapter-Out | 없음 |

> Command가 없는 도메인이므로 Facade 트랜잭션 경계가 불필요합니다. ReadManager가 직접 `@Transactional(readOnly = true)` 적용.

### N+1 방지 전략

목록 조회(`findByCriteria`) 시 2-Query 전략으로 N+1을 방지합니다.

```
1차 쿼리: notice_category 목록 조회 → List<NoticeCategoryJpaEntity>
2차 쿼리: notice_field IN (id1, id2, ...) → List<NoticeFieldJpaEntity> (단일 In-Query)
In-Memory: groupingBy(noticeCategoryId) → Map으로 조립
```

단건 조회(`findByCategoryGroup`) 시에는 순차 2-Query 사용.

```
1차 쿼리: notice_category WHERE target_category_group = ?
2차 쿼리: notice_field WHERE notice_category_id = ?
```

### 변환 체인

```
[Query - 목록 조회]
SearchNoticeCategoriesApiRequest
  → NoticeCategoryQueryApiMapper.toSearchParams()
  → NoticeCategorySearchParams (Application DTO)
  → NoticeCategoryQueryFactory.createCriteria()
  → NoticeCategorySearchCriteria (Domain Criteria)
  → NoticeCategoryQueryDslRepository.findByCriteria() → List<NoticeCategoryJpaEntity>
  → NoticeCategoryJpaEntityMapper.toDomain()
  → List<NoticeCategory> (Domain Aggregate)
  → NoticeCategoryAssembler.toResult()
  → List<NoticeCategoryResult> (Application Result)
  → NoticeCategoryQueryApiMapper.toPageResponse()
  → PageApiResponse<NoticeCategoryApiResponse> (API Response)

[Query - 단건 조회]
CategoryGroup (Path Variable)
  → GetNoticeCategoryUseCase.execute(categoryGroup)
  → NoticeCategoryQueryPort.findByCategoryGroup()
  → NoticeCategoryJpaEntityMapper.toDomain()
  → NoticeCategory (Domain Aggregate)
  → NoticeCategoryAssembler.toResult()
  → NoticeCategoryResult (Application Result)
  → NoticeCategoryQueryApiMapper.toResponse()
  → NoticeCategoryApiResponse (API Response)
```

### 에러 처리 체계

| 에러 코드 | HTTP Status | 설명 | 발생 위치 |
|-----------|-------------|------|-----------|
| `NOTICE-001` | 404 | 고시정보 카테고리를 찾을 수 없습니다 | `NoticeCategoryReadManager.getByCategoryGroup()` |
| `NOTICE-002` | 404 | 고시정보 필드를 찾을 수 없습니다 | (예약) |
| `NOTICE-003` | 400 | 고시정보 카테고리에 존재하지 않는 필드입니다 | (예약) |
| `NOTICE-004` | 400 | 필수 고시정보 필드가 누락되었습니다 | (예약) |

- `NoticeException` → `NoticeErrorMapper.map()` → `MappedError(HttpStatus, "Notice Error", message, URI)`
- URI 패턴: `/errors/notice/{error-code-lowercase}`

---

## 파일 경로 요약

### Adapter-In
| 파일 | 역할 |
|------|------|
| `adapter-in/rest-api/.../notice/NoticeAdminEndpoints.java` | URL 상수 정의 |
| `adapter-in/rest-api/.../notice/controller/NoticeCategoryQueryController.java` | REST Controller |
| `adapter-in/rest-api/.../notice/mapper/NoticeCategoryQueryApiMapper.java` | Request/Response 변환 |
| `adapter-in/rest-api/.../notice/dto/query/SearchNoticeCategoriesApiRequest.java` | 목록 검색 요청 DTO |
| `adapter-in/rest-api/.../notice/dto/response/NoticeCategoryApiResponse.java` | 카테고리 응답 DTO |
| `adapter-in/rest-api/.../notice/dto/response/NoticeFieldApiResponse.java` | 필드 응답 DTO |
| `adapter-in/rest-api/.../notice/error/NoticeErrorMapper.java` | 예외 → HTTP 응답 변환 |

### Application
| 파일 | 역할 |
|------|------|
| `application/.../notice/port/in/query/SearchNoticeCategoryByOffsetUseCase.java` | 목록 조회 UseCase (Port-In) |
| `application/.../notice/port/in/query/GetNoticeCategoryUseCase.java` | 단건 조회 UseCase (Port-In) |
| `application/.../notice/port/out/query/NoticeCategoryQueryPort.java` | 영속성 조회 Port (Port-Out) |
| `application/.../notice/service/query/SearchNoticeCategoryByOffsetService.java` | 목록 조회 Service |
| `application/.../notice/service/query/GetNoticeCategoryService.java` | 단건 조회 Service |
| `application/.../notice/manager/NoticeCategoryReadManager.java` | Port 래핑 + 예외 처리 |
| `application/.../notice/factory/NoticeCategoryQueryFactory.java` | Params → Criteria 변환 |
| `application/.../notice/assembler/NoticeCategoryAssembler.java` | Domain → Result 변환 |
| `application/.../notice/dto/query/NoticeCategorySearchParams.java` | 목록 검색 파라미터 DTO |
| `application/.../notice/dto/response/NoticeCategoryPageResult.java` | 목록 조회 결과 DTO |
| `application/.../notice/dto/response/NoticeCategoryResult.java` | 단건 조회 결과 DTO |
| `application/.../notice/dto/response/NoticeFieldResult.java` | 필드 결과 DTO |

### Domain
| 파일 | 역할 |
|------|------|
| `domain/.../notice/aggregate/NoticeCategory.java` | 고시정보 카테고리 Aggregate Root |
| `domain/.../notice/aggregate/NoticeField.java` | 고시정보 필드 엔티티 |
| `domain/.../notice/query/NoticeCategorySearchCriteria.java` | 검색 조건 도메인 객체 |
| `domain/.../notice/query/NoticeCategorySortKey.java` | 정렬 키 enum |
| `domain/.../notice/exception/NoticeCategoryNotFoundException.java` | 카테고리 미존재 예외 |
| `domain/.../notice/exception/NoticeErrorCode.java` | 에러 코드 enum |

### Adapter-Out
| 파일 | 역할 |
|------|------|
| `adapter-out/persistence-mysql/.../notice/adapter/NoticeCategoryQueryAdapter.java` | Port 구현체 |
| `adapter-out/persistence-mysql/.../notice/repository/NoticeCategoryQueryDslRepository.java` | QueryDSL 조회 |
| `adapter-out/persistence-mysql/.../notice/condition/NoticeCategoryConditionBuilder.java` | WHERE 조건 빌더 |
| `adapter-out/persistence-mysql/.../notice/mapper/NoticeCategoryJpaEntityMapper.java` | Entity → Domain 변환 |
| `adapter-out/persistence-mysql/.../notice/mapper/NoticeFieldJpaEntityMapper.java` | 필드 Entity → Domain 변환 |
| `adapter-out/persistence-mysql/.../notice/entity/NoticeCategoryJpaEntity.java` | notice_category 테이블 매핑 |
| `adapter-out/persistence-mysql/.../notice/entity/NoticeFieldJpaEntity.java` | notice_field 테이블 매핑 |
| `adapter-out/persistence-mysql/.../notice/repository/NoticeCategoryJpaRepository.java` | Spring Data JPA (미사용) |
| `adapter-out/persistence-mysql/.../notice/repository/NoticeFieldJpaRepository.java` | Spring Data JPA (미사용) |
