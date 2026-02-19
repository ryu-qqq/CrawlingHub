# SalesChannel Domain API Flow Analysis

판매채널 도메인의 전체 API 호출 흐름 분석 문서입니다.

---

## 엔드포인트 목록

| HTTP Method | Path | 설명 | Controller Method |
|-------------|------|------|-------------------|
| GET | `/api/v1/market/sales-channels` | 판매채널 목록 검색 | `searchSalesChannels()` |
| POST | `/api/v1/market/sales-channels` | 판매채널 등록 | `registerSalesChannel()` |
| PUT | `/api/v1/market/sales-channels/{salesChannelId}` | 판매채널 수정 | `updateSalesChannel()` |

---

## 1. GET /api/v1/market/sales-channels - 판매채널 목록 검색

### 호출 흐름 다이어그램

```
[Adapter-In]
SalesChannelQueryController.searchSalesChannels(SearchSalesChannelsApiRequest)
  ├─ SalesChannelQueryApiMapper.toSearchParams(request)       [Params 변환]
  │   └─ CommonSearchParams.of(sortKey, sortDirection, page, size)
  │       └─ -> SalesChannelSearchParams
  ├─ SearchSalesChannelByOffsetUseCase.execute(params)        [Port Interface]
  └─ SalesChannelQueryApiMapper.toPageResponse(pageResult)    [Response 변환]

[Application]
SearchSalesChannelByOffsetService.execute(params)            [UseCase 구현]
  ├─ SalesChannelQueryFactory.createCriteria(params)          [Criteria 생성]
  │   ├─ SalesChannelSortKey 파싱 (createdAt / channelName)
  │   ├─ SortDirection 파싱 (ASC / DESC)
  │   ├─ PageRequest 생성 (page, size)
  │   ├─ SalesChannelSearchField 파싱 (CHANNEL_NAME)
  │   ├─ List<SalesChannelStatus> 변환 (ACTIVE / INACTIVE)
  │   └─ -> SalesChannelSearchCriteria
  ├─ SalesChannelReadManager.findByCriteria(criteria)          [조회]
  │   └─ SalesChannelQueryPort.findByCriteria()               [Port]
  ├─ SalesChannelReadManager.countByCriteria(criteria)         [카운트]
  │   └─ SalesChannelQueryPort.countByCriteria()              [Port]
  └─ SalesChannelAssembler.toPageResult()                      [Result 조합]
      └─ SalesChannelResult.from(salesChannel) × N

[Adapter-Out]
SalesChannelQueryAdapter                                       [Port 구현]
  ├─ findByCriteria: SalesChannelQueryDslRepository.findByCriteria()
  │   └─ QueryDSL: WHERE + ORDER BY + OFFSET/LIMIT
  └─ countByCriteria: SalesChannelQueryDslRepository.countByCriteria()
      └─ QueryDSL: SELECT COUNT(*)

[Database]
- sales_channel (조회 대상 단일 테이블)
```

### Layer별 상세

#### Adapter-In Layer

- **Controller**: `SalesChannelQueryController`
  - File: `adapter-in/rest-api/src/main/java/com/ryuqq/marketplace/adapter/in/rest/saleschannel/controller/SalesChannelQueryController.java`
  - Method: `searchSalesChannels(@ParameterObject @Valid SearchSalesChannelsApiRequest)`
  - HTTP: `GET /api/v1/market/sales-channels`
  - 권한: `@PreAuthorize("@access.superAdmin()")` + `@RequirePermission("sales-channel:read")`
  - Response: `ResponseEntity<ApiResponse<PageApiResponse<SalesChannelApiResponse>>>`

- **Endpoints 상수**: `SalesChannelAdminEndpoints`
  ```java
  BASE = "/api/v1/market"
  SALES_CHANNELS = BASE + "/sales-channels"    // → /api/v1/market/sales-channels
  ```

- **Request DTO**: `SearchSalesChannelsApiRequest` (Record)
  ```java
  record SearchSalesChannelsApiRequest(
    List<String> statuses,       // 상태 필터 (ACTIVE, INACTIVE) - 선택
    String searchField,          // 검색 필드 (CHANNEL_NAME) - 선택
    String searchWord,           // 검색어 - 선택
    String sortKey,              // 정렬 키 (createdAt, channelName) - 선택
    String sortDirection,        // 정렬 방향 (ASC, DESC) - 선택
    @Min(0) Integer page,        // 페이지 번호 (기본값: 0) - 선택
    @Min(1) @Max(100) Integer size  // 페이지 크기 (기본값: 20) - 선택
  )
  ```

- **Response DTO**: `SalesChannelApiResponse` (Record)
  ```java
  record SalesChannelApiResponse(
    Long id,            // 판매채널 ID
    String channelName, // 판매채널명
    String status,      // 상태 (ACTIVE / INACTIVE)
    String createdAt,   // 생성일시 (ISO8601: 2025-01-23T10:30:00+09:00)
    String updatedAt    // 수정일시 (ISO8601)
  )
  ```

- **ApiMapper**: `SalesChannelQueryApiMapper`
  - `toSearchParams(request)`: 기본값 처리
    - `sortKey`: null → `"createdAt"`
    - `sortDirection`: null → `"DESC"`
    - `page`: null → `0`
    - `size`: null → `20`
    - `CommonSearchParams.of(false, null, null, sortKey, sortDirection, page, size)` 생성
  - `toPageResponse(pageResult)`: `PageApiResponse.of(responses, page, size, totalElements)` 생성

#### Application Layer

- **UseCase Interface**: `SearchSalesChannelByOffsetUseCase`
  ```java
  SalesChannelPageResult execute(SalesChannelSearchParams params)
  ```

- **Service 구현**: `SearchSalesChannelByOffsetService` (`@Service`)
  1. `SalesChannelQueryFactory.createCriteria(params)` → `SalesChannelSearchCriteria`
  2. `SalesChannelReadManager.findByCriteria(criteria)` → `List<SalesChannel>`
  3. `SalesChannelReadManager.countByCriteria(criteria)` → `long`
  4. `SalesChannelAssembler.toPageResult(salesChannels, page, size, totalElements)` → `SalesChannelPageResult`

- **Params DTO**: `SalesChannelSearchParams` (Record)
  ```java
  record SalesChannelSearchParams(
    List<String> statuses,
    String searchField,
    String searchWord,
    CommonSearchParams searchParams  // page, size, sortKey, sortDirection, includeDeleted 포함
  )
  // 편의 메서드: page(), size(), sortKey(), sortDirection()
  ```

- **Result DTO**: `SalesChannelPageResult` (Record)
  ```java
  record SalesChannelPageResult(
    List<SalesChannelResult> results,
    PageMeta pageMeta               // page, size, totalElements
  )
  ```

- **Result 항목**: `SalesChannelResult` (Record)
  ```java
  record SalesChannelResult(
    Long id,
    String channelName,
    String status,      // "ACTIVE" / "INACTIVE" (enum.name())
    Instant createdAt,
    Instant updatedAt
  )
  // 생성: SalesChannelResult.from(salesChannel)
  ```

- **Manager**: `SalesChannelReadManager` (`@Component`)
  - `findByCriteria(criteria)`: `@Transactional(readOnly = true)` → `queryPort.findByCriteria(criteria)`
  - `countByCriteria(criteria)`: `@Transactional(readOnly = true)` → `queryPort.countByCriteria(criteria)`

- **Factory**: `SalesChannelQueryFactory` (`@Component`)
  - `createCriteria(params)` 내부 변환 흐름:
    ```
    params.sortKey() → SalesChannelSortKey (CREATED_AT / CHANNEL_NAME)
    params.sortDirection() → SortDirection (ASC / DESC)
    params.page(), params.size() → PageRequest
    QueryContext<SalesChannelSortKey> 생성
    params.searchField() → SalesChannelSearchField.fromString() (CHANNEL_NAME / null)
    params.statuses() → List<SalesChannelStatus>.fromString() (ACTIVE / INACTIVE)
    → SalesChannelSearchCriteria.of(statuses, searchField, searchWord, queryContext)
    ```

- **Assembler**: `SalesChannelAssembler` (`@Component`)
  - `toPageResult(salesChannels, page, size, totalElements)`:
    - `toResults()` → `SalesChannelResult.from(salesChannel)` × N
    - `PageMeta.of(page, size, totalElements)` 생성
    - `SalesChannelPageResult.of(results, pageMeta)` 반환

#### Domain Layer

- **Port**: `SalesChannelQueryPort` (인터페이스)
  ```java
  List<SalesChannel> findByCriteria(SalesChannelSearchCriteria criteria)
  long countByCriteria(SalesChannelSearchCriteria criteria)
  ```

- **Criteria**: `SalesChannelSearchCriteria` (Record, 불변)
  ```java
  record SalesChannelSearchCriteria(
    List<SalesChannelStatus> statuses,              // 방어적 복사 (List.copyOf)
    SalesChannelSearchField searchField,            // null 허용
    String searchWord,                              // null 허용
    QueryContext<SalesChannelSortKey> queryContext  // 필수
  )
  // 편의 메서드: hasStatusFilter(), hasSearchField(), hasSearchCondition(), size(), offset(), page()
  ```

- **SortKey**: `SalesChannelSortKey` (enum)
  ```java
  CREATED_AT("createdAt"),   // 기본값
  CHANNEL_NAME("channelName")
  ```

- **SearchField**: `SalesChannelSearchField` (enum)
  ```java
  CHANNEL_NAME("channelName")
  ```

- **Status**: `SalesChannelStatus` (enum)
  ```java
  ACTIVE, INACTIVE
  // fromString(): null/blank → ACTIVE 기본값, 대소문자 무관
  ```

- **Aggregate**: `SalesChannel`
  - `idValue()`: `Long`
  - `channelName()`: `String` (ChannelName VO에서 추출)
  - `status()`: `SalesChannelStatus`
  - `createdAt()`, `updatedAt()`: `Instant`

#### Adapter-Out Layer

- **Adapter**: `SalesChannelQueryAdapter` (`@Component`)
  - `SalesChannelQueryPort` 구현
  - `findByCriteria()`: `repository.findByCriteria(criteria)` → entity list → `mapper.toDomain()` × N
  - `countByCriteria()`: `repository.countByCriteria(criteria)`

- **Repository**: `SalesChannelQueryDslRepository` (`@Repository`)
  - `JPAQueryFactory` + `SalesChannelConditionBuilder` 사용

- **ConditionBuilder**: `SalesChannelConditionBuilder` (`@Component`)
  - `statusIn(criteria)`: `criteria.hasStatusFilter()` → `salesChannel.status.in(statusNames)`
  - `searchCondition(criteria)`: `criteria.hasSearchCondition()` → `salesChannel.channelName.like('%검색어%')`
  - 검색 필드 지정 없으면 기본적으로 `channelName`에 적용

- **Entity**: `SalesChannelJpaEntity`
  - `@Table(name = "sales_channel")`
  - `@Id @GeneratedValue(strategy = IDENTITY) Long id`
  - `@Column(name = "channel_name", length = 100) String channelName`
  - `@Column(name = "status", length = 20) String status`
  - `BaseAuditEntity` 상속: `createdAt`, `updatedAt` (`Instant`)

- **Mapper**: `SalesChannelJpaEntityMapper`
  - `toDomain(entity)`: `SalesChannel.reconstitute(id, channelName, status, createdAt, updatedAt)`

- **Database Query**:
  ```sql
  -- findByCriteria
  SELECT *
  FROM sales_channel
  WHERE status IN ('ACTIVE', 'INACTIVE')   -- 옵션: statuses 필터
    AND channel_name LIKE '%검색어%'         -- 옵션: searchWord
  ORDER BY created_at DESC                  -- sortKey + sortDirection
  OFFSET ? LIMIT ?                          -- 페이징

  -- countByCriteria
  SELECT COUNT(sc)
  FROM sales_channel sc
  WHERE status IN ('ACTIVE', 'INACTIVE')
    AND channel_name LIKE '%검색어%'
  ```

---

## 2. POST /api/v1/market/sales-channels - 판매채널 등록

### 호출 흐름 다이어그램

```
[Adapter-In]
SalesChannelCommandController.registerSalesChannel(RegisterSalesChannelApiRequest)
  ├─ SalesChannelCommandApiMapper.toCommand(request)          [Command 변환]
  │   └─ -> RegisterSalesChannelCommand(channelName)
  └─ RegisterSalesChannelUseCase.execute(command)             [Port Interface]
      └─ -> Long salesChannelId (응답)

[Application]
RegisterSalesChannelService.execute(command)                  [UseCase 구현]
  ├─ SalesChannelValidator.validateChannelNameNotDuplicate()  [중복 검증]
  │   └─ SalesChannelReadManager.existsByChannelName()
  │       └─ SalesChannelQueryPort.existsByChannelName()      [Port]
  ├─ SalesChannelCommandFactory.create(command)               [Domain 생성]
  │   ├─ TimeProvider.now() → Instant now
  │   └─ SalesChannel.forNew(channelName, now)
  │       └─ ChannelName.of(channelName) [1~100자 검증]
  └─ SalesChannelCommandManager.persist(salesChannel)         [저장]
      └─ SalesChannelCommandPort.persist()                    [Port]

[Adapter-Out]
SalesChannelCommandAdapter                                     [Port 구현]
  ├─ SalesChannelJpaEntityMapper.toEntity(salesChannel)
  │   └─ SalesChannelJpaEntity.create(null, channelName, "ACTIVE", createdAt, updatedAt)
  └─ SalesChannelJpaRepository.save(entity)
      └─ -> entity.getId() (AUTO_INCREMENT ID 반환)

[Database]
- INSERT INTO sales_channel (channel_name, status, created_at, updated_at)
```

### Layer별 상세

#### Adapter-In Layer

- **Controller**: `SalesChannelCommandController`
  - File: `adapter-in/rest-api/src/main/java/com/ryuqq/marketplace/adapter/in/rest/saleschannel/controller/SalesChannelCommandController.java`
  - Method: `registerSalesChannel(@Valid @RequestBody RegisterSalesChannelApiRequest)`
  - HTTP: `POST /api/v1/market/sales-channels`
  - 권한: `@PreAuthorize("@access.superAdmin()")` + `@RequirePermission("sales-channel:write")`
  - Response: `ResponseEntity.status(201).body(ApiResponse.of(SalesChannelIdApiResponse.of(salesChannelId)))`

- **Request DTO**: `RegisterSalesChannelApiRequest` (Record)
  ```java
  record RegisterSalesChannelApiRequest(
    @NotBlank String channelName   // 판매채널명 (필수, 공백 불가)
  )
  ```

- **Response DTO**: `SalesChannelIdApiResponse` (Record)
  ```java
  record SalesChannelIdApiResponse(Long salesChannelId)
  // static factory: SalesChannelIdApiResponse.of(Long)
  ```

- **ApiMapper**: `SalesChannelCommandApiMapper`
  - `toCommand(request)`: `new RegisterSalesChannelCommand(request.channelName())`
  - 단순 필드 복사, 별도 변환 로직 없음

#### Application Layer

- **UseCase Interface**: `RegisterSalesChannelUseCase`
  ```java
  Long execute(RegisterSalesChannelCommand command)
  ```

- **Service 구현**: `RegisterSalesChannelService` (`@Service`)
  1. `validator.validateChannelNameNotDuplicate(command.channelName())`
     - `readManager.existsByChannelName(channelName)` → true면 `SalesChannelNameDuplicateException` 발생
  2. `commandFactory.create(command)` → `SalesChannel` Domain 객체 생성
  3. `commandManager.persist(salesChannel)` → `Long` (생성된 ID) 반환

- **Command DTO**: `RegisterSalesChannelCommand` (Record)
  ```java
  record RegisterSalesChannelCommand(String channelName)
  ```

- **Validator**: `SalesChannelValidator` (`@Component`)
  - `validateChannelNameNotDuplicate(channelName)`:
    - `readManager.existsByChannelName(channelName)` 호출
    - 중복이면 `SalesChannelNameDuplicateException(channelName)` throw (HTTP 409)

- **Factory**: `SalesChannelCommandFactory` (`@Component`)
  - `create(command)`:
    - `timeProvider.now()` → `Instant now` (TimeProvider 주입, APP-TIM-001 준수)
    - `SalesChannel.forNew(command.channelName(), now)` 호출
    - `SalesChannel.forNew()` 내부: `SalesChannelId.forNew()` (null ID), `ChannelName.of()` 검증, `status = ACTIVE`

- **Manager**: `SalesChannelCommandManager` (`@Component`)
  - `persist(salesChannel)`: `@Transactional` → `commandPort.persist(salesChannel)` → `Long`

- **Manager (검증용)**: `SalesChannelReadManager`
  - `existsByChannelName(channelName)`: `@Transactional(readOnly = true)` → `queryPort.existsByChannelName(channelName)`

#### Domain Layer

- **Port (Command)**: `SalesChannelCommandPort`
  ```java
  Long persist(SalesChannel salesChannel)
  ```

- **Port (Query, 검증용)**: `SalesChannelQueryPort`
  ```java
  boolean existsByChannelName(String channelName)
  ```

- **Aggregate**: `SalesChannel`
  - `SalesChannel.forNew(channelName, now)`:
    - `id = SalesChannelId.forNew()` (null)
    - `channelName = ChannelName.of(channelName)` (1~100자 검증)
    - `status = SalesChannelStatus.ACTIVE` (등록 시 항상 ACTIVE)
    - `createdAt = updatedAt = now`

- **VO**: `ChannelName`
  - 1~100자, null/blank 불가
  - trim() 처리

- **예외**:
  - `SalesChannelNameDuplicateException`: SCH-002, HTTP 409

#### Adapter-Out Layer

- **Adapter**: `SalesChannelCommandAdapter` (`@Component`)
  - `SalesChannelCommandPort` 구현
  - `persist(salesChannel)`:
    1. `mapper.toEntity(salesChannel)` → `SalesChannelJpaEntity.create(null, channelName, "ACTIVE", createdAt, updatedAt)`
    2. `repository.save(entity)` → JPA가 AUTO_INCREMENT로 ID 할당
    3. `saved.getId()` 반환

- **Repository**: `SalesChannelJpaRepository` (Spring Data JPA)
  - `JpaRepository<SalesChannelJpaEntity, Long>` 상속
  - `save()` 메서드만 사용 (별도 쿼리 메서드 없음)

- **Mapper**: `SalesChannelJpaEntityMapper`
  - `toEntity(salesChannel)`:
    ```java
    SalesChannelJpaEntity.create(
      salesChannel.idValue(),      // null (신규)
      salesChannel.channelName(),  // String
      salesChannel.status().name(), // "ACTIVE"
      salesChannel.createdAt(),    // Instant
      salesChannel.updatedAt()     // Instant
    )
    ```

- **Database Query**:
  ```sql
  INSERT INTO sales_channel (channel_name, status, created_at, updated_at)
  VALUES ('쿠팡', 'ACTIVE', NOW(), NOW())
  -- AUTO_INCREMENT로 id 생성 후 반환
  ```

---

## 3. PUT /api/v1/market/sales-channels/{salesChannelId} - 판매채널 수정

### 호출 흐름 다이어그램

```
[Adapter-In]
SalesChannelCommandController.updateSalesChannel(salesChannelId, UpdateSalesChannelApiRequest)
  ├─ SalesChannelCommandApiMapper.toCommand(salesChannelId, request)  [Command 변환]
  │   └─ -> UpdateSalesChannelCommand(salesChannelId, channelName, status)
  └─ UpdateSalesChannelUseCase.execute(command)                       [Port Interface]

[Application]
UpdateSalesChannelService.execute(command)                            [UseCase 구현]
  ├─ SalesChannelCommandFactory.createUpdateContext(command)          [UpdateContext 생성]
  │   ├─ SalesChannelId.of(command.salesChannelId())
  │   ├─ SalesChannelUpdateData.of(channelName, SalesChannelStatus.fromString(status))
  │   ├─ TimeProvider.now() → changedAt
  │   └─ -> UpdateContext<SalesChannelId, SalesChannelUpdateData>
  ├─ SalesChannelValidator.findExistingOrThrow(salesChannelId)        [존재 검증]
  │   └─ SalesChannelReadManager.getById(id)
  │       └─ SalesChannelQueryPort.findById()                         [Port]
  ├─ salesChannel.update(updateData, changedAt)                       [Domain 수정]
  │   ├─ channelName = ChannelName.of(updateData.channelName()) [검증]
  │   ├─ status = updateData.status()
  │   └─ updatedAt = changedAt
  └─ SalesChannelCommandManager.persist(salesChannel)                 [저장]
      └─ SalesChannelCommandPort.persist()                            [Port]

[Adapter-Out]
SalesChannelCommandAdapter                                             [Port 구현]
  ├─ SalesChannelJpaEntityMapper.toEntity(salesChannel)
  │   └─ SalesChannelJpaEntity.create(id, channelName, status, createdAt, updatedAt)
  └─ SalesChannelJpaRepository.save(entity)
      └─ JPA merge (ID 존재 → UPDATE)

[Database]
- UPDATE sales_channel SET channel_name=?, status=?, updated_at=? WHERE id=?
```

### Layer별 상세

#### Adapter-In Layer

- **Controller**: `SalesChannelCommandController`
  - Method: `updateSalesChannel(@PathVariable Long salesChannelId, @Valid @RequestBody UpdateSalesChannelApiRequest)`
  - HTTP: `PUT /api/v1/market/sales-channels/{salesChannelId}`
  - 권한: `@PreAuthorize("@access.superAdmin()")` + `@RequirePermission("sales-channel:write")`
  - Response: `ResponseEntity.noContent().build()` (HTTP 204)

- **Path Variable**: `@PathVariable(SalesChannelAdminEndpoints.PATH_SALES_CHANNEL_ID)` = `"salesChannelId"`

- **Request DTO**: `UpdateSalesChannelApiRequest` (Record)
  ```java
  record UpdateSalesChannelApiRequest(
    @NotBlank String channelName,                               // 판매채널명 (필수)
    @NotBlank @Pattern(regexp = "ACTIVE|INACTIVE") String status  // 상태 (필수)
  )
  ```

- **Response**: `ResponseEntity<Void>` (Body 없음, 204 No Content)

- **ApiMapper**: `SalesChannelCommandApiMapper`
  - `toCommand(salesChannelId, request)`:
    ```java
    new UpdateSalesChannelCommand(salesChannelId, request.channelName(), request.status())
    ```

#### Application Layer

- **UseCase Interface**: `UpdateSalesChannelUseCase`
  ```java
  void execute(UpdateSalesChannelCommand command)
  ```

- **Service 구현**: `UpdateSalesChannelService` (`@Service`)
  - 주석에 명시된 규칙:
    - `APP-TIM-001`: TimeProvider 직접 사용 금지, Factory에서 처리
    - `FAC-008`: `createUpdateContext()`로 ID, UpdateData, changedAt 한 번에 생성
    - `APP-VAL-001`: 검증 + Domain 조회는 `Validator.findExistingOrThrow()`로 처리
  1. `commandFactory.createUpdateContext(command)` → `UpdateContext`
  2. `validator.findExistingOrThrow(salesChannelId)` → `SalesChannel` Domain 객체 (없으면 예외)
  3. `salesChannel.update(context.updateData(), context.changedAt())` → Domain 상태 변경
  4. `commandManager.persist(salesChannel)` → DB 저장

- **Command DTO**: `UpdateSalesChannelCommand` (Record)
  ```java
  record UpdateSalesChannelCommand(Long salesChannelId, String channelName, String status)
  ```

- **UpdateContext**: `UpdateContext<SalesChannelId, SalesChannelUpdateData>` (공통 DTO)
  ```java
  record UpdateContext<ID, DATA>(ID id, DATA updateData, Instant changedAt)
  ```

- **UpdateData**: `SalesChannelUpdateData` (Record)
  ```java
  record SalesChannelUpdateData(String channelName, SalesChannelStatus status)
  // static factory: SalesChannelUpdateData.of(channelName, status)
  ```

- **Factory**: `SalesChannelCommandFactory`
  - `createUpdateContext(command)`:
    - `SalesChannelId.of(command.salesChannelId())`: null 불가 검증
    - `SalesChannelUpdateData.of(channelName, SalesChannelStatus.fromString(status))`
    - `timeProvider.now()` → changedAt
    - `new UpdateContext<>(id, updateData, changedAt)` 반환

- **Validator**: `SalesChannelValidator`
  - `findExistingOrThrow(salesChannelId)`:
    - `readManager.getById(id)` 호출
    - 없으면 `SalesChannelNotFoundException(salesChannelId.value())` throw (HTTP 404)
    - 성공 시 Domain 객체 반환

- **Manager**: `SalesChannelCommandManager`
  - `persist(salesChannel)`: `@Transactional` → `commandPort.persist(salesChannel)` → `Long`

- **Manager (검증용)**: `SalesChannelReadManager`
  - `getById(id)`: `@Transactional(readOnly = true)` → `queryPort.findById(id)` → `orElseThrow(SalesChannelNotFoundException)`

#### Domain Layer

- **Port (Query, 검증용)**: `SalesChannelQueryPort`
  ```java
  Optional<SalesChannel> findById(SalesChannelId id)
  ```

- **Port (Command)**: `SalesChannelCommandPort`
  ```java
  Long persist(SalesChannel salesChannel)
  ```

- **Aggregate**: `SalesChannel`
  - `update(SalesChannelUpdateData, Instant)`:
    ```java
    this.channelName = ChannelName.of(updateData.channelName())  // 검증 포함 (1~100자)
    this.status = updateData.status()                            // ACTIVE / INACTIVE
    this.updatedAt = now                                         // 수정 시각
    ```
  - `id`와 `createdAt`은 불변 (final)
  - `activate(now)`, `deactivate(now)` 메서드도 존재

- **ID VO**: `SalesChannelId`
  - `SalesChannelId.of(Long)`: null이면 `IllegalArgumentException`
  - `SalesChannelId.forNew()`: null ID (신규 등록용)

- **예외**:
  - `SalesChannelNotFoundException`: SCH-001, HTTP 404
  - `SalesChannelNameDuplicateException`: SCH-002, HTTP 409 (수정 시 중복 검증은 현재 미적용)

#### Adapter-Out Layer

- **Adapter**: `SalesChannelCommandAdapter`
  - `persist(salesChannel)`:
    1. `mapper.toEntity(salesChannel)` → `SalesChannelJpaEntity.create(id, channelName, status, createdAt, updatedAt)`
    2. `repository.save(entity)` → JPA가 ID 존재 여부 확인 후 UPDATE 실행
    3. `saved.getId()` 반환

- **EntityMapper**: `SalesChannelJpaEntityMapper`
  - `toEntity(salesChannel)`:
    ```java
    SalesChannelJpaEntity.create(
      salesChannel.idValue(),        // Long (기존 ID)
      salesChannel.channelName(),    // String (ChannelName.value())
      salesChannel.status().name(),  // "ACTIVE" / "INACTIVE"
      salesChannel.createdAt(),      // Instant
      salesChannel.updatedAt()       // Instant (changedAt으로 업데이트됨)
    )
    ```

- **Database Query**:
  ```sql
  -- 조회 (findById via QueryDSL)
  SELECT *
  FROM sales_channel
  WHERE id = ?

  -- 수정 (JpaRepository.save → merge)
  UPDATE sales_channel
  SET channel_name = ?,
      status = ?,
      updated_at = ?
  WHERE id = ?
  ```

---

## 공통 패턴 분석

### Hexagonal Architecture 레이어 격리

| 레이어 | DTO/모델 | 책임 |
|--------|----------|------|
| **Adapter-In** | `ApiRequest` / `ApiResponse` | HTTP 계층 관심사 (Validation, 직렬화, ISO8601 변환) |
| **Application** | `Command` / `Params` / `Result` / `PageResult` | 유스케이스 조율, 트랜잭션 경계 |
| **Domain** | `SalesChannel`, `SalesChannelUpdateData`, `Criteria` | 비즈니스 규칙 (ChannelName 1~100자 검증), 불변성 |
| **Adapter-Out** | `SalesChannelJpaEntity` | 영속화 기술 관심사 (JPA, QueryDSL) |

### DTO 변환 체인

```
[Query 흐름]
SearchSalesChannelsApiRequest
  → SalesChannelSearchParams (CommonSearchParams 포함)
    → SalesChannelSearchCriteria (QueryContext<SalesChannelSortKey> 포함)
      → SalesChannelJpaEntity (QueryDSL 조회)
        → SalesChannel (Domain reconstitute)
          → SalesChannelResult (Assembler)
            → SalesChannelApiResponse (Mapper, ISO8601 변환)
              → PageApiResponse<SalesChannelApiResponse>

[Command 흐름 - 등록]
RegisterSalesChannelApiRequest
  → RegisterSalesChannelCommand
    → SalesChannel.forNew() (ChannelName 검증 포함)
      → SalesChannelJpaEntity (Mapper.toEntity)
        → DB INSERT
          → Long (생성 ID)
            → SalesChannelIdApiResponse

[Command 흐름 - 수정]
UpdateSalesChannelApiRequest + salesChannelId
  → UpdateSalesChannelCommand
    → UpdateContext<SalesChannelId, SalesChannelUpdateData>
      → SalesChannel (findById → reconstitute)
        → salesChannel.update() (ChannelName 재검증 포함)
          → SalesChannelJpaEntity (Mapper.toEntity)
            → DB UPDATE
              → 204 No Content
```

### CQRS 분리

- **Query**: `SalesChannelQueryController` → `SalesChannelQueryApiMapper` → `SearchSalesChannelByOffsetUseCase` → `SearchSalesChannelByOffsetService`
- **Command**: `SalesChannelCommandController` → `SalesChannelCommandApiMapper` → Command UseCase → Command Service

### 트랜잭션 경계

| 계층 | `@Transactional` 위치 | 비고 |
|------|----------------------|------|
| Adapter-In (Controller) | 없음 | 금지 |
| Application (Service) | 없음 | Manager에 위임 |
| **Application (ReadManager)** | `readOnly = true` | 조회 시 |
| **Application (CommandManager)** | `@Transactional` | 등록/수정 시 |
| Adapter-Out | 없음 | 금지 |

- 이 도메인은 단일 Aggregate(SalesChannel)만 관여하므로 **Facade 없이** Manager 직접 트랜잭션 관리
- Seller 도메인과 달리 Coordinator/Facade 패턴 미사용 (단순 구조)

### Factory 패턴 역할

| Factory | 메서드 | 역할 |
|---------|--------|------|
| `SalesChannelQueryFactory` | `createCriteria(params)` | Params → Domain Criteria 변환, SortKey/SearchField 파싱 |
| `SalesChannelCommandFactory` | `create(command)` | Command → Domain Aggregate 생성 (`TimeProvider.now()` 사용) |
| `SalesChannelCommandFactory` | `createUpdateContext(command)` | Command → ID + UpdateData + changedAt 번들 생성 |

### Validator 패턴

```
RegisterSalesChannelService
  → SalesChannelValidator.validateChannelNameNotDuplicate()
      → SalesChannelReadManager.existsByChannelName()         [읽기 전용 조회]
          → SalesChannelQueryPort.existsByChannelName()
              → SalesChannelQueryDslRepository.existsByChannelName()
                  → SELECT 1 FROM sales_channel WHERE channel_name = ?

UpdateSalesChannelService
  → SalesChannelValidator.findExistingOrThrow(id)
      → SalesChannelReadManager.getById(id)                   [읽기 전용 조회]
          → SalesChannelQueryPort.findById()
              → SalesChannelQueryDslRepository.findById()
                  → SELECT * FROM sales_channel WHERE id = ?
```

### 도메인 예외 처리

| 예외 클래스 | 에러코드 | HTTP | 발생 조건 |
|-------------|---------|------|----------|
| `SalesChannelNotFoundException` | SCH-001 | 404 | 수정 시 ID 미존재 |
| `SalesChannelNameDuplicateException` | SCH-002 | 409 | 등록 시 채널명 중복 |

- `SalesChannelErrorMapper` (`@Component`): `SalesChannelException` 처리, `/errors/sales-channel/{code}` 형태 URI 반환
- `ChannelName` VO 검증 실패: `IllegalArgumentException` (공백, 1~100자 위반)

### 이 도메인의 특징

1. **단순 구조**: 단일 Aggregate (`SalesChannel`), 단일 테이블 (`sales_channel`), JOIN 없음
2. **Facade/Coordinator 없음**: 복잡한 조율이 불필요하여 Service → Manager → Port 직선 구조
3. **삭제 없음**: 소프트 삭제 미적용, `status: ACTIVE | INACTIVE`로 활성/비활성 관리
4. **단건 조회 엔드포인트 없음**: ID 기반 단건 조회는 수정 처리 내부에서만 사용
5. **등록 시 중복 검증만**: 수정 시 채널명 변경에 대한 중복 검증은 현재 미구현 (`existsByChannelNameExcluding` Port 메서드는 존재하나 Service에서 미사용)

---

**분석 일시**: 2026-02-18
**분석 대상**:
- `adapter-in/rest-api/src/main/java/com/ryuqq/marketplace/adapter/in/rest/saleschannel/`
- `application/src/main/java/com/ryuqq/marketplace/application/saleschannel/`
- `domain/src/main/java/com/ryuqq/marketplace/domain/saleschannel/`
- `adapter-out/persistence-mysql/src/main/java/com/ryuqq/marketplace/adapter/out/persistence/saleschannel/`
