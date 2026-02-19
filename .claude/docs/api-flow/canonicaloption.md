# CanonicalOption Domain API Flow Analysis

CanonicalOption(정규 옵션) 도메인의 전체 API 호출 흐름 분석 문서입니다.

> 정규 옵션(CanonicalOption) 데이터는 시스템 관리 데이터로, DB migration을 통해서만 생성/수정됩니다.
> 현재 구현된 API는 **Query(조회) 전용** 2개 엔드포인트만 존재합니다.

---

## 엔드포인트 목록

| HTTP Method | Path | 설명 | Controller Method |
|-------------|------|------|-------------------|
| GET | `/api/v1/market/canonical-option-groups` | 정규 옵션그룹 목록 검색 | `searchCanonicalOptionGroupsByOffset()` |
| GET | `/api/v1/market/canonical-option-groups/{canonicalOptionGroupId}` | 정규 옵션그룹 단건 조회 | `getCanonicalOptionGroup()` |

---

## 1. GET /canonical-option-groups - 정규 옵션그룹 목록 검색

### 호출 흐름 다이어그램

```
[Adapter-In]
CanonicalOptionGroupQueryController.searchCanonicalOptionGroupsByOffset(request)
  ├─ CanonicalOptionGroupQueryApiMapper.toSearchParams(request)        [Params 변환]
  │   └─ CommonSearchParams.of(sortKey, sortDirection, page, size)
  │       └─ CanonicalOptionGroupSearchParams.of(active, searchField, searchWord, commonParams)
  ├─ SearchCanonicalOptionGroupByOffsetUseCase.execute(params)         [Port Interface]
  └─ CanonicalOptionGroupQueryApiMapper.toPageResponse(pageResult)     [Response 변환]

[Application]
SearchCanonicalOptionGroupByOffsetService.execute(params)             [UseCase 구현]
  ├─ CanonicalOptionGroupQueryFactory.createCriteria(params)          [Criteria 생성]
  │   ├─ resolveSortKey(sortKeyString) → CanonicalOptionGroupSortKey
  │   ├─ commonVoFactory.parseSortDirection() → SortDirection
  │   ├─ commonVoFactory.createPageRequest(page, size) → PageRequest
  │   └─ new CanonicalOptionGroupSearchCriteria(active, searchField, searchWord, queryContext)
  ├─ CanonicalOptionGroupReadManager.findByCriteria(criteria)         [목록 조회]
  │   └─ CanonicalOptionGroupQueryPort.findByCriteria()               [Port]
  ├─ CanonicalOptionGroupReadManager.countByCriteria(criteria)        [카운트]
  │   └─ CanonicalOptionGroupQueryPort.countByCriteria()              [Port]
  └─ CanonicalOptionGroupAssembler.toPageResult(results, ...)         [Result 조합]

[Adapter-Out]
CanonicalOptionGroupQueryAdapter.findByCriteria(criteria)             [Port 구현]
  ├─ CanonicalOptionGroupQueryDslRepository.findByCriteria(criteria)
  │   └─ QueryDSL: SELECT + WHERE(active, searchField) + ORDER BY + LIMIT/OFFSET
  ├─ CanonicalOptionGroupQueryDslRepository.findValuesByGroupIds(groupIds)
  │   └─ QueryDSL: SELECT FROM canonical_option_value WHERE group_id IN (...)
  └─ CanonicalOptionGroupJpaEntityMapper.toDomain(groupEntity, valueEntities)

CanonicalOptionGroupQueryAdapter.countByCriteria(criteria)
  └─ CanonicalOptionGroupQueryDslRepository.countByCriteria(criteria)
      └─ QueryDSL: SELECT COUNT(*) + WHERE(active, searchField)

[Database]
- canonical_option_group (그룹 조회 및 카운트)
- canonical_option_value (각 그룹의 옵션 값 목록 조회)
```

### Layer별 상세

#### Adapter-In Layer

- **Controller**: `CanonicalOptionGroupQueryController`
  - 위치: `adapter-in/rest-api/.../canonicaloption/controller/`
  - Method: `searchCanonicalOptionGroupsByOffset(@ParameterObject @Valid SearchCanonicalOptionGroupsApiRequest)`
  - Response: `ResponseEntity<ApiResponse<PageApiResponse<CanonicalOptionGroupApiResponse>>>`
  - 권한: `@RequirePermission("canonical-option-group:read")`
  - HTTP Status: 200 OK

- **Request DTO**: `SearchCanonicalOptionGroupsApiRequest`
  ```java
  public record SearchCanonicalOptionGroupsApiRequest(
      Boolean active,         // 활성 상태 필터 (null: 전체)
      String searchField,     // 검색 필드: CODE, NAME_KO, NAME_EN
      String searchWord,      // 검색어
      String sortKey,         // 정렬 기준: createdAt, code
      String sortDirection,   // 정렬 방향: ASC, DESC
      Integer page,           // 페이지 번호 (0부터, default: 0)
      Integer size            // 페이지 크기 (default: 20)
  )
  ```

- **Response DTO**: `CanonicalOptionGroupApiResponse` (중첩 구조)
  ```java
  public record CanonicalOptionGroupApiResponse(
      Long id,
      String code,
      String nameKo,
      String nameEn,
      boolean active,
      List<CanonicalOptionValueApiResponse> values,  // 중첩: 옵션 값 목록
      String createdAt    // ISO8601 문자열 (DateTimeFormatUtils.formatIso8601)
  )

  public record CanonicalOptionValueApiResponse(
      Long id,
      String code,
      String nameKo,
      String nameEn,
      int sortOrder
  )
  ```

- **ApiMapper**: `CanonicalOptionGroupQueryApiMapper`
  - `toSearchParams(SearchCanonicalOptionGroupsApiRequest)` → `CanonicalOptionGroupSearchParams`
    - page 기본값: 0, size 기본값: 20
    - `CommonSearchParams.of(null, null, null, sortKey, sortDirection, page, size)`
  - `toPageResponse(CanonicalOptionGroupPageResult)` → `PageApiResponse<CanonicalOptionGroupApiResponse>`
    - 내부적으로 `toResponse()` 및 `toValueResponse()` 호출

#### Application Layer

- **UseCase Interface**: `SearchCanonicalOptionGroupByOffsetUseCase`
  ```java
  public interface SearchCanonicalOptionGroupByOffsetUseCase {
      CanonicalOptionGroupPageResult execute(CanonicalOptionGroupSearchParams params);
  }
  ```

- **Service 구현**: `SearchCanonicalOptionGroupByOffsetService`
  1. `CanonicalOptionGroupQueryFactory.createCriteria(params)` → `CanonicalOptionGroupSearchCriteria`
  2. `readManager.findByCriteria(criteria)` → `List<CanonicalOptionGroup>`
  3. `readManager.countByCriteria(criteria)` → `long`
  4. `assembler.toPageResult(results, page, size, totalElements)` → `CanonicalOptionGroupPageResult`

- **Params DTO**: `CanonicalOptionGroupSearchParams`
  ```java
  public record CanonicalOptionGroupSearchParams(
      Boolean active,
      String searchField,
      String searchWord,
      CommonSearchParams commonSearchParams   // page, size, sortKey, sortDirection 포함
  )
  ```

- **Result DTO**: `CanonicalOptionGroupPageResult`
  ```java
  public record CanonicalOptionGroupPageResult(
      List<CanonicalOptionGroupResult> results,
      PageMeta pageMeta   // page, size, totalElements
  )

  public record CanonicalOptionGroupResult(
      Long id,
      String code,
      String nameKo,
      String nameEn,
      boolean active,
      List<CanonicalOptionValueResult> values,
      Instant createdAt
  )

  public record CanonicalOptionValueResult(
      Long id, String code, String nameKo, String nameEn, int sortOrder
  )
  ```

- **Factory**: `CanonicalOptionGroupQueryFactory`
  - `createCriteria(CanonicalOptionGroupSearchParams)` → `CanonicalOptionGroupSearchCriteria`
  - sortKey 문자열 → `CanonicalOptionGroupSortKey` enum 변환 (기본값: `CREATED_AT`)
  - `QueryContext<CanonicalOptionGroupSortKey>` 생성

- **Manager**: `CanonicalOptionGroupReadManager`
  - `@Transactional(readOnly = true)` 적용
  - `findByCriteria(CanonicalOptionGroupSearchCriteria)`: Port 호출
  - `countByCriteria(CanonicalOptionGroupSearchCriteria)`: Port 호출

- **Assembler**: `CanonicalOptionGroupAssembler`
  - `CanonicalOptionGroup` → `CanonicalOptionGroupResult` 변환
  - `CanonicalOptionValue` → `CanonicalOptionValueResult` 변환
  - `toPageResult(results, page, size, totalElements)`: `PageMeta` 포함 결과 생성

#### Domain Layer

- **Port (out)**: `CanonicalOptionGroupQueryPort`
  ```java
  public interface CanonicalOptionGroupQueryPort {
      Optional<CanonicalOptionGroup> findById(CanonicalOptionGroupId id);
      List<CanonicalOptionGroup> findByIds(List<CanonicalOptionGroupId> ids);
      List<CanonicalOptionGroup> findByCriteria(CanonicalOptionGroupSearchCriteria criteria);
      long countByCriteria(CanonicalOptionGroupSearchCriteria criteria);
  }
  ```

- **Criteria**: `CanonicalOptionGroupSearchCriteria`
  ```java
  public record CanonicalOptionGroupSearchCriteria(
      Boolean active,
      String searchField,
      String searchWord,
      QueryContext<CanonicalOptionGroupSortKey> queryContext
  )
  // 편의 메서드: hasActiveFilter(), hasSearchFilter(), size(), offset(), page()
  ```

- **SortKey**: `CanonicalOptionGroupSortKey`
  - `CREATED_AT("createdAt")`: 기본 정렬
  - `CODE("code")`

- **Aggregate**: `CanonicalOptionGroup` (read-only)
  - 시스템 관리 데이터: DB migration으로만 생성/수정
  - `reconstitute()`: 영속성에서 복원 시 사용
  - 불변 컬렉션: `List.copyOf(values)`
  - VO: `CanonicalOptionGroupId`, `CanonicalOptionGroupCode`, `CanonicalOptionGroupName`

- **Child Entity**: `CanonicalOptionValue` (read-only)
  - Aggregate Root(`CanonicalOptionGroup`)를 통해서만 접근
  - VO: `CanonicalOptionValueId`, `CanonicalOptionValueCode`, `CanonicalOptionValueName`

#### Adapter-Out Layer

- **Adapter**: `CanonicalOptionGroupQueryAdapter`
  - `implements CanonicalOptionGroupQueryPort`
  - `findByCriteria()`: 2-Step 쿼리 전략
    1. `queryDslRepository.findByCriteria(criteria)` → 그룹 목록 조회
    2. `queryDslRepository.findValuesByGroupIds(groupIds)` → 값 목록 일괄 조회
    3. `groupBy(canonicalOptionGroupId)` → 메모리에서 그룹별 값 매핑
  - `countByCriteria()`: 별도 COUNT 쿼리 실행

- **QueryDSL Repository**: `CanonicalOptionGroupQueryDslRepository`
  - `findById(Long id)`: `WHERE id = ?`
  - `findByCriteria(criteria)`: 동적 WHERE + ORDER BY + LIMIT/OFFSET
  - `countByCriteria(criteria)`: 동적 WHERE + COUNT
  - `findValuesByGroupId(Long groupId)`: `WHERE group_id = ? ORDER BY sort_order ASC`
  - `findValuesByGroupIds(List<Long> groupIds)`: `WHERE group_id IN (...) ORDER BY sort_order ASC`

- **Condition Builder**: `CanonicalOptionGroupConditionBuilder`
  - `idEq(Long id)`: `canonical_option_group.id = ?`
  - `activeEq(criteria)`: `canonical_option_group.active = ?` (null이면 조건 없음)
  - `searchCondition(criteria)`: searchField 기준 LIKE 조건
    - `CODE`: `code LIKE %?%` (대소문자 무시)
    - `NAME_KO`: `name_ko LIKE %?%`
    - `NAME_EN`: `name_en LIKE %?%`

- **Entity Mapper**: `CanonicalOptionGroupJpaEntityMapper`
  - `toDomain(CanonicalOptionGroupJpaEntity, List<CanonicalOptionValueJpaEntity>)` → `CanonicalOptionGroup`
  - `CanonicalOptionGroup.reconstitute()` 호출하여 Domain 객체 복원

- **Entity Mapper (Value)**: `CanonicalOptionValueJpaEntityMapper`
  - `toDomain(CanonicalOptionValueJpaEntity)` → `CanonicalOptionValue`
  - `CanonicalOptionValue.reconstitute()` 호출

- **JPA Entity**: `CanonicalOptionGroupJpaEntity`
  ```java
  @Entity @Table(name = "canonical_option_group")
  // extends BaseAuditEntity (createdAt, updatedAt as Instant)
  // id, code(unique), nameKo, nameEn, active
  ```

- **JPA Entity**: `CanonicalOptionValueJpaEntity`
  ```java
  @Entity @Table(name = "canonical_option_value")
  // extends BaseAuditEntity
  // id, canonicalOptionGroupId(FK), code, nameKo, nameEn, sortOrder
  ```

- **Database Query**:
  ```sql
  -- 1단계: 그룹 목록 조회
  SELECT *
  FROM canonical_option_group
  WHERE active = ?                        -- 옵션: activeEq
    AND code LIKE %?%                     -- 옵션: searchCondition (CODE)
    AND name_ko LIKE %?%                  -- 또는 NAME_KO
    AND name_en LIKE %?%                  -- 또는 NAME_EN
  ORDER BY created_at DESC                -- 또는 code ASC/DESC
  LIMIT ? OFFSET ?

  -- 2단계: COUNT 쿼리
  SELECT COUNT(*)
  FROM canonical_option_group
  WHERE active = ?
    AND code LIKE %?%

  -- 3단계: 옵션 값 일괄 조회 (N+1 방지)
  SELECT *
  FROM canonical_option_value
  WHERE canonical_option_group_id IN (?, ?, ...)
  ORDER BY sort_order ASC
  ```

---

## 2. GET /canonical-option-groups/{canonicalOptionGroupId} - 정규 옵션그룹 단건 조회

### 호출 흐름 다이어그램

```
[Adapter-In]
CanonicalOptionGroupQueryController.getCanonicalOptionGroup(canonicalOptionGroupId)
  ├─ GetCanonicalOptionGroupUseCase.execute(canonicalOptionGroupId)   [Port Interface]
  └─ CanonicalOptionGroupQueryApiMapper.toResponse(result)           [Response 변환]

[Application]
GetCanonicalOptionGroupService.execute(canonicalOptionGroupId)       [UseCase 구현]
  ├─ CanonicalOptionGroupId.of(canonicalOptionGroupId)               [ID VO 생성]
  ├─ CanonicalOptionGroupReadManager.getById(id)                     [단건 조회]
  │   └─ CanonicalOptionGroupQueryPort.findById(id)                  [Port]
  │       └─ Optional.orElseThrow(CanonicalOptionGroupNotFoundException)
  └─ CanonicalOptionGroupAssembler.toResult(group)                   [Result 변환]

[Adapter-Out]
CanonicalOptionGroupQueryAdapter.findById(id)                        [Port 구현]
  ├─ CanonicalOptionGroupQueryDslRepository.findById(id.value())
  │   └─ QueryDSL: SELECT FROM canonical_option_group WHERE id = ?
  ├─ CanonicalOptionGroupQueryDslRepository.findValuesByGroupId(groupId)
  │   └─ QueryDSL: SELECT FROM canonical_option_value WHERE group_id = ? ORDER BY sort_order ASC
  └─ CanonicalOptionGroupJpaEntityMapper.toDomain(groupEntity, valueEntities)

[Database]
- canonical_option_group (id 기반 단건 조회)
- canonical_option_value (해당 그룹의 옵션 값 목록)
```

### Layer별 상세

#### Adapter-In Layer

- **Controller**: `CanonicalOptionGroupQueryController`
  - Method: `getCanonicalOptionGroup(@PathVariable Long canonicalOptionGroupId)`
  - Response: `ResponseEntity<ApiResponse<CanonicalOptionGroupApiResponse>>`
  - 권한: `@RequirePermission("canonical-option-group:read")`
  - HTTP Status: 200 OK / 404 Not Found

- **Request**: Path Variable `canonicalOptionGroupId` (Long)
  - 상수: `CanonicalOptionAdminEndpoints.PATH_CANONICAL_OPTION_GROUP_ID = "canonicalOptionGroupId"`

- **Response DTO**: `CanonicalOptionGroupApiResponse` (목록 조회와 동일 구조)

- **ApiMapper**: `CanonicalOptionGroupQueryApiMapper`
  - `toResponse(CanonicalOptionGroupResult)` → `CanonicalOptionGroupApiResponse`
  - `createdAt`: `DateTimeFormatUtils.formatIso8601(result.createdAt())` → ISO8601 문자열
  - `values`: `toValueResponse()` 스트림 변환

#### Application Layer

- **UseCase Interface**: `GetCanonicalOptionGroupUseCase`
  ```java
  public interface GetCanonicalOptionGroupUseCase {
      CanonicalOptionGroupResult execute(Long canonicalOptionGroupId);
  }
  ```

- **Service 구현**: `GetCanonicalOptionGroupService`
  1. `CanonicalOptionGroupId.of(canonicalOptionGroupId)` → ID VO 생성
  2. `readManager.getById(id)` → `CanonicalOptionGroup` (없으면 예외)
  3. `assembler.toResult(group)` → `CanonicalOptionGroupResult`

- **Manager**: `CanonicalOptionGroupReadManager`
  - `@Transactional(readOnly = true)` 적용
  - `getById(CanonicalOptionGroupId)`: 조회 후 없으면 `CanonicalOptionGroupNotFoundException` 발생

- **Result DTO**: `CanonicalOptionGroupResult` (목록 조회와 동일)

#### Domain Layer

- **Port (out)**: `CanonicalOptionGroupQueryPort`
  - `findById(CanonicalOptionGroupId)` → `Optional<CanonicalOptionGroup>`

- **Exception**: `CanonicalOptionGroupNotFoundException`
  - `CanonicalOptionException` 상속
  - HTTP Status: 404 Not Found
  - ErrorMapper: `CanonicalOptionErrorMapper` (type: `/errors/canonical-option/{code}`)

- **Aggregate**: `CanonicalOptionGroup` (read-only, 목록 조회와 동일)

#### Adapter-Out Layer

- **Adapter**: `CanonicalOptionGroupQueryAdapter.findById()`
  - 2-Step 쿼리 전략 (단건 기준):
    1. `queryDslRepository.findById(id.value())` → `Optional<CanonicalOptionGroupJpaEntity>`
    2. (그룹 존재 시) `queryDslRepository.findValuesByGroupId(entity.getId())` → `List<CanonicalOptionValueJpaEntity>`
    3. `mapper.toDomain(entity, values)` → `CanonicalOptionGroup`

- **Database Query**:
  ```sql
  -- 1단계: 그룹 단건 조회
  SELECT *
  FROM canonical_option_group
  WHERE id = ?

  -- 2단계: 해당 그룹의 옵션 값 조회
  SELECT *
  FROM canonical_option_value
  WHERE canonical_option_group_id = ?
  ORDER BY sort_order ASC
  ```

---

## 공통 패턴 분석

### 1. Hexagonal Architecture 레이어 격리

| 레이어 | DTO/모델 | 책임 |
|--------|----------|------|
| **Adapter-In** | `SearchCanonicalOptionGroupsApiRequest`, `CanonicalOptionGroupApiResponse` | HTTP 관심사 (Validation, 직렬화, 권한) |
| **Application** | `CanonicalOptionGroupSearchParams`, `CanonicalOptionGroupResult`, `CanonicalOptionGroupPageResult` | 유스케이스 조율, 트랜잭션 경계 |
| **Domain** | `CanonicalOptionGroupSearchCriteria`, `CanonicalOptionGroup`, `CanonicalOptionValue` | 비즈니스 규칙, 불변성 |
| **Adapter-Out** | `CanonicalOptionGroupJpaEntity`, `CanonicalOptionValueJpaEntity` | 영속화 기술 관심사 (JPA, QueryDSL) |

### 2. CQRS 분리

- **Query 전용**: `CanonicalOptionGroupQueryController` + `CanonicalOptionGroupQueryApiMapper`
- Command 없음: CanonicalOption 데이터는 DB migration으로만 관리

### 3. 트랜잭션 경계

| 계층 | @Transactional |
|------|----------------|
| Adapter-In | 없음 |
| Application Service | 없음 |
| **ReadManager** | `@Transactional(readOnly = true)` |
| Adapter-Out | 없음 |

### 4. 2-Step 쿼리 전략 (N+1 방지)

```
[목록 조회]
1. findByCriteria() → List<CanonicalOptionGroupJpaEntity> (그룹 목록)
2. findValuesByGroupIds(groupIds) → List<CanonicalOptionValueJpaEntity> (값 일괄 조회)
3. groupingBy(canonicalOptionGroupId) → Map<Long, List<...>> (메모리 매핑)

[단건 조회]
1. findById(id) → Optional<CanonicalOptionGroupJpaEntity>
2. findValuesByGroupId(groupId) → List<CanonicalOptionValueJpaEntity>
```

JPA Lazy Loading이나 JOIN FETCH 대신, **별도 쿼리 + 메모리 매핑** 방식을 사용합니다.
이는 N+1 문제를 방지하면서 Aggregate 경계를 명확히 유지합니다.

### 5. 변환 체인 (Query)

```
[Request → Params]
SearchCanonicalOptionGroupsApiRequest
  → CanonicalOptionGroupQueryApiMapper.toSearchParams()
    → CommonSearchParams (page, size, sortKey, sortDirection)
      → CanonicalOptionGroupSearchParams (active, searchField, searchWord + commonParams)

[Params → Criteria]
CanonicalOptionGroupSearchParams
  → CanonicalOptionGroupQueryFactory.createCriteria()
    → CanonicalOptionGroupSortKey (enum 변환)
    → SortDirection, PageRequest, QueryContext
      → CanonicalOptionGroupSearchCriteria

[Entity → Domain]
CanonicalOptionGroupJpaEntity + List<CanonicalOptionValueJpaEntity>
  → CanonicalOptionGroupJpaEntityMapper.toDomain()
    → CanonicalOptionValueJpaEntityMapper.toDomain() (각 값)
      → CanonicalOptionGroup.reconstitute() + CanonicalOptionValue.reconstitute()

[Domain → Result]
CanonicalOptionGroup
  → CanonicalOptionGroupAssembler.toResult()
    → CanonicalOptionGroupResult + List<CanonicalOptionValueResult>

[Result → Response]
CanonicalOptionGroupResult
  → CanonicalOptionGroupQueryApiMapper.toResponse()
    → CanonicalOptionGroupApiResponse + List<CanonicalOptionValueApiResponse>
    → createdAt: DateTimeFormatUtils.formatIso8601(Instant) → String
```

### 6. 에러 처리

| 상황 | 예외 | HTTP Status |
|------|------|-------------|
| 존재하지 않는 ID 조회 | `CanonicalOptionGroupNotFoundException` | 404 Not Found |

- `CanonicalOptionGroupNotFoundException` → `CanonicalOptionException` 상속
- `CanonicalOptionErrorMapper`: `ErrorMapper` 구현체로 ProblemDetail 변환
- Error Type URI: `/errors/canonical-option/{errorCode}`

### 7. 도메인 설계 특징

- `CanonicalOptionGroup`은 **read-only Aggregate Root**: 시스템 표준 데이터로 API를 통한 변경 불가
- `CanonicalOptionValue`는 Aggregate Root를 통해서만 접근하는 **불변 Child Entity**
- 모든 필드에 VO(Value Object) 적용: `CanonicalOptionGroupId`, `CanonicalOptionGroupCode`, `CanonicalOptionGroupName`
- `reconstitute()` 팩토리 메서드: 영속성 복원 시 사용 (도메인 생명주기 명확화)

---

## 구성 파일 목록

### Adapter-In
| 파일 | 역할 |
|------|------|
| `CanonicalOptionAdminEndpoints.java` | URL 경로 상수 정의 |
| `controller/CanonicalOptionGroupQueryController.java` | REST 컨트롤러 (CQRS Query) |
| `mapper/CanonicalOptionGroupQueryApiMapper.java` | Request/Response 변환 매퍼 |
| `dto/query/SearchCanonicalOptionGroupsApiRequest.java` | 목록 검색 요청 DTO (Record) |
| `dto/response/CanonicalOptionGroupApiResponse.java` | 응답 DTO (중첩 Record) |
| `dto/response/CanonicalOptionValueApiResponse.java` | 옵션 값 응답 DTO (중첩 Record) |
| `error/CanonicalOptionErrorMapper.java` | 도메인 예외 → HTTP 응답 변환 |

### Application
| 파일 | 역할 |
|------|------|
| `port/in/query/GetCanonicalOptionGroupUseCase.java` | 단건 조회 UseCase 인터페이스 |
| `port/in/query/SearchCanonicalOptionGroupByOffsetUseCase.java` | 목록 조회 UseCase 인터페이스 |
| `port/out/query/CanonicalOptionGroupQueryPort.java` | 영속성 조회 Port 인터페이스 |
| `service/query/GetCanonicalOptionGroupService.java` | 단건 조회 Service 구현 |
| `service/query/SearchCanonicalOptionGroupByOffsetService.java` | 목록 조회 Service 구현 |
| `manager/CanonicalOptionGroupReadManager.java` | 조회 Manager (@Transactional readOnly) |
| `factory/CanonicalOptionGroupQueryFactory.java` | Params → Criteria 변환 Factory |
| `assembler/CanonicalOptionGroupAssembler.java` | Domain → Result DTO 변환 Assembler |
| `dto/query/CanonicalOptionGroupSearchParams.java` | Application 검색 파라미터 DTO |
| `dto/response/CanonicalOptionGroupResult.java` | Application 단건 결과 DTO |
| `dto/response/CanonicalOptionGroupPageResult.java` | Application 페이징 결과 DTO |
| `dto/response/CanonicalOptionValueResult.java` | Application 옵션 값 결과 DTO |

### Domain
| 파일 | 역할 |
|------|------|
| `aggregate/CanonicalOptionGroup.java` | Aggregate Root (read-only) |
| `aggregate/CanonicalOptionValue.java` | Child Entity (read-only) |
| `query/CanonicalOptionGroupSearchCriteria.java` | 도메인 검색 조건 |
| `query/CanonicalOptionGroupSortKey.java` | 정렬 키 Enum |
| `id/CanonicalOptionGroupId.java` | 그룹 ID VO |
| `id/CanonicalOptionValueId.java` | 값 ID VO |
| `vo/CanonicalOptionGroupCode.java` | 그룹 코드 VO |
| `vo/CanonicalOptionGroupName.java` | 그룹 이름 VO |
| `vo/CanonicalOptionValueCode.java` | 값 코드 VO |
| `vo/CanonicalOptionValueName.java` | 값 이름 VO |
| `exception/CanonicalOptionGroupNotFoundException.java` | 404 예외 |

### Adapter-Out
| 파일 | 역할 |
|------|------|
| `adapter/CanonicalOptionGroupQueryAdapter.java` | QueryPort 구현 (2-Step 쿼리) |
| `repository/CanonicalOptionGroupQueryDslRepository.java` | QueryDSL 동적 쿼리 |
| `repository/CanonicalOptionGroupJpaRepository.java` | Spring Data JPA (미사용) |
| `repository/CanonicalOptionValueJpaRepository.java` | Spring Data JPA (미사용) |
| `condition/CanonicalOptionGroupConditionBuilder.java` | QueryDSL 조건 빌더 |
| `mapper/CanonicalOptionGroupJpaEntityMapper.java` | Entity → Domain 변환 |
| `mapper/CanonicalOptionValueJpaEntityMapper.java` | Entity → Domain 변환 (값) |
| `entity/CanonicalOptionGroupJpaEntity.java` | JPA 엔티티 (`canonical_option_group`) |
| `entity/CanonicalOptionValueJpaEntity.java` | JPA 엔티티 (`canonical_option_value`) |

---

**분석 일시**: 2026-02-18
**분석 대상**: `canonicaloption` 도메인 전체 레이어
**총 엔드포인트**: 2개 (Query 2개, Command 0개)
