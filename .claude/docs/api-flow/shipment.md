# Shipment Domain API Flow Analysis

배송 도메인의 전체 API 호출 흐름 분석 문서입니다.

---

## 엔드포인트 목록

| HTTP Method | Path | 설명 | Controller Method |
|-------------|------|------|-------------------|
| GET | `/api/v1/market/shipments/summary` | 배송 상태별 요약 조회 | `getSummary()` |
| GET | `/api/v1/market/shipments` | 배송 목록 검색 | `searchShipments()` |
| GET | `/api/v1/market/shipments/{shipmentId}` | 배송 상세 조회 | `getShipment()` |
| POST | `/api/v1/market/shipments/confirm/batch` | 발주확인 일괄 처리 | `confirmBatch()` |
| POST | `/api/v1/market/shipments/ship/batch` | 송장등록 일괄 처리 | `shipBatch()` |
| POST | `/api/v1/market/shipments/orders/{orderId}/ship` | 단건 송장등록 | `shipSingle()` |

---

## 1. GET /api/v1/market/shipments/summary - 배송 상태별 요약 조회

### 호출 흐름 다이어그램

```
[Adapter-In]
ShipmentQueryController.getSummary()
  ├─ GetShipmentSummaryUseCase.execute()                     [Port Interface]
  └─ ShipmentQueryApiMapper.toSummaryResponse(result)        [Response 변환]

[Application]
GetShipmentSummaryService.execute()                          [UseCase 구현]
  ├─ ShipmentReadManager.countByStatus()
  │   └─ ShipmentQueryPort.countByStatus()                   [Domain Port]
  └─ ShipmentAssembler.toSummaryResult(statusCounts)         [Result 조합]

[Adapter-Out]
ShipmentQueryAdapter.countByStatus()                         [Port 구현]
  └─ ShipmentQueryDslRepository.countByStatus()
      └─ QueryDSL: GROUP BY status + COUNT(*)

[Database]
- shipments (상태별 카운트)
```

### Layer별 상세

#### Adapter-In Layer

- **Controller**: `ShipmentQueryController`
  - Method: `getSummary()`
  - Response: `ResponseEntity<ApiResponse<ShipmentSummaryApiResponse>>`
  - 권한: `@RequirePermission("shipment:read")`
  - HTTP Status: 200 OK

- **Request DTO**: 없음 (파라미터 없음)

- **Response DTO**: `ShipmentSummaryApiResponse`
  ```java
  record ShipmentSummaryApiResponse(
    int ready,       // READY 상태 건수
    int preparing,   // PREPARING 상태 건수
    int shipped,     // SHIPPED 상태 건수
    int inTransit,   // IN_TRANSIT 상태 건수
    int delivered,   // DELIVERED 상태 건수
    int failed,      // FAILED 상태 건수
    int cancelled    // CANCELLED 상태 건수
  )
  ```

- **ApiMapper**: `ShipmentQueryApiMapper`
  - `toSummaryResponse(ShipmentSummaryResult)` → `ShipmentSummaryApiResponse`
  - 상태별 카운트 필드 1:1 매핑

#### Application Layer

- **UseCase Interface**: `GetShipmentSummaryUseCase`
  - `execute()` → `ShipmentSummaryResult`

- **Service 구현**: `GetShipmentSummaryService`
  - ReadManager로 상태별 카운트 조회
  - Assembler로 Result 변환

- **Manager**: `ShipmentReadManager`
  - `countByStatus()`: 상태별 카운트 Map 반환
  - `@Transactional(readOnly = true)`

- **Assembler**: `ShipmentAssembler`
  - `toSummaryResult(Map<ShipmentStatus, Long>)` → `ShipmentSummaryResult`
  - 각 상태별 `getOrDefault(status, 0L)` 처리

- **Result DTO**: `ShipmentSummaryResult`
  ```java
  record ShipmentSummaryResult(
    int ready, int preparing, int shipped,
    int inTransit, int delivered, int failed, int cancelled
  )
  ```

#### Domain Layer

- **Port**: `ShipmentQueryPort`
  - `countByStatus()` → `Map<ShipmentStatus, Long>`

- **Enum**: `ShipmentStatus` (READY, PREPARING, SHIPPED, IN_TRANSIT, DELIVERED, FAILED, CANCELLED)

#### Adapter-Out Layer

- **Adapter**: `ShipmentQueryAdapter`
  - `countByStatus()`: QueryDSL 결과 `Map<String, Long>` → `Map<ShipmentStatus, Long>` 변환
  - String → enum 변환 시 `ShipmentStatus.valueOf()` 사용

- **Repository**: `ShipmentQueryDslRepository`
  - `countByStatus()`: GROUP BY + COUNT 쿼리

- **Database Query**:
  ```sql
  SELECT status, COUNT(*)
  FROM shipments
  WHERE deleted_at IS NULL
  GROUP BY status
  ```

---

## 2. GET /api/v1/market/shipments - 배송 목록 검색

### 호출 흐름 다이어그램

```
[Adapter-In]
ShipmentQueryController.searchShipments(ShipmentSearchApiRequest)
  ├─ ShipmentQueryApiMapper.toSearchParams(request)          [Params 변환]
  ├─ GetShipmentListUseCase.execute(params)                   [Port Interface]
  └─ ShipmentQueryApiMapper.toPageResponse(pageResult)        [Response 변환]

[Application]
GetShipmentListService.execute(params)                       [UseCase 구현]
  ├─ ShipmentQueryFactory.createCriteria(params)             [Criteria 생성]
  ├─ ShipmentReadManager.findByCriteria(criteria)            [목록 조회]
  │   └─ ShipmentQueryPort.findByCriteria(criteria)          [Domain Port]
  ├─ ShipmentReadManager.countByCriteria(criteria)           [카운트 조회]
  │   └─ ShipmentQueryPort.countByCriteria(criteria)         [Domain Port]
  └─ ShipmentAssembler.toPageResult(shipments, page, size, total)

[Adapter-Out]
ShipmentQueryAdapter                                          [Port 구현]
  └─ ShipmentQueryDslRepository
      ├─ findByCriteria(): WHERE + ORDER BY + LIMIT/OFFSET
      └─ countByCriteria(): COUNT(*)

[Database]
- shipments (검색 대상)
```

### Layer별 상세

#### Adapter-In Layer

- **Controller**: `ShipmentQueryController`
  - Method: `searchShipments(@ParameterObject @Valid ShipmentSearchApiRequest)`
  - Response: `ResponseEntity<ApiResponse<PageApiResponse<ShipmentListApiResponse>>>`
  - 권한: `@RequirePermission("shipment:read")`
  - HTTP Status: 200 OK

- **Request DTO**: `ShipmentSearchApiRequest`
  ```java
  record ShipmentSearchApiRequest(
    List<String> statuses,     // 배송 상태 필터
    String searchField,        // 검색 필드 (shipmentNumber, orderNumber, trackingNumber)
    String searchWord,         // 검색어
    String dateField,          // 날짜 검색 대상 (createdAt, shippedAt, deliveredAt)
    String sortKey,            // 정렬 키 (createdAt, shippedAt, deliveredAt)
    String sortDirection,      // 정렬 방향 (ASC, DESC)
    Integer page,              // 페이지 번호 (default: 0)
    Integer size               // 페이지 크기 (default: 20)
  )
  ```

- **Response DTO**: `ShipmentListApiResponse`
  ```java
  record ShipmentListApiResponse(
    String shipmentId,
    String shipmentNumber,
    String orderId,
    String orderNumber,
    String status,
    String trackingNumber,
    String courierName,
    String shippedAt,          // ISO8601 문자열
    String deliveredAt,        // ISO8601 문자열
    String createdAt           // ISO8601 문자열
  )
  ```

- **ApiMapper**: `ShipmentQueryApiMapper`
  - `toSearchParams(ShipmentSearchApiRequest)` → `ShipmentSearchParams`
    - page/size null 시 기본값 적용 (page=0, size=20)
    - `CommonSearchParams.of()` 생성 (sortKey, sortDirection 포함)
  - `toResponse(ShipmentListResult)` → `ShipmentListApiResponse`
    - `DateTimeFormatUtils.formatIso8601()` 날짜 변환
  - `toPageResponse(ShipmentPageResult)` → `PageApiResponse<ShipmentListApiResponse>`

#### Application Layer

- **UseCase Interface**: `GetShipmentListUseCase`
  - `execute(ShipmentSearchParams)` → `ShipmentPageResult`

- **Service 구현**: `GetShipmentListService`
  - Factory로 Params → Criteria 변환
  - ReadManager 통해 목록 + 카운트 조회
  - Assembler로 PageResult 생성

- **Params DTO**: `ShipmentSearchParams`
  ```java
  record ShipmentSearchParams(
    List<String> statuses,
    String searchField,
    String searchWord,
    String dateField,
    CommonSearchParams searchParams  // sortKey, sortDirection, page, size 포함
  )
  ```

- **Factory**: `ShipmentQueryFactory`
  - `createCriteria(ShipmentSearchParams)` → `ShipmentSearchCriteria`
  - `resolveSortKey()`: String → `ShipmentSortKey` enum 변환 (기본값: CREATED_AT)
  - `resolveStatuses()`: List\<String\> → List\<ShipmentStatus\> 변환
  - `resolveDateField()`: String → `ShipmentDateField` enum 변환
  - `CommonVoFactory`로 `QueryContext`, `PageRequest`, `DateRange` 생성

- **Manager**: `ShipmentReadManager`
  - `findByCriteria(ShipmentSearchCriteria)`: `@Transactional(readOnly = true)`
  - `countByCriteria(ShipmentSearchCriteria)`: `@Transactional(readOnly = true)`

- **Assembler**: `ShipmentAssembler`
  - `toListResults(List<Shipment>)` → `List<ShipmentListResult>`
  - `toPageResult(shipments, page, size, totalCount)` → `ShipmentPageResult`
  - `PageMeta.of(page, size, totalCount)` 생성

- **Result DTO**: `ShipmentPageResult`
  ```java
  record ShipmentPageResult(
    List<ShipmentListResult> results,
    PageMeta pageMeta               // page, size, totalElements
  )
  ```

#### Domain Layer

- **Port**: `ShipmentQueryPort`
  - `findByCriteria(ShipmentSearchCriteria)` → `List<Shipment>`
  - `countByCriteria(ShipmentSearchCriteria)` → `long`

- **Criteria**: `ShipmentSearchCriteria`
  - `List<ShipmentStatus> statuses`: 상태 필터
  - `ShipmentSearchField searchField`: 검색 필드 enum
  - `String searchWord`: 검색어
  - `DateRange dateRange`: 날짜 범위
  - `ShipmentDateField dateField`: 날짜 검색 대상 필드
  - `QueryContext<ShipmentSortKey> queryContext`: 정렬 + 페이징

- **Domain Enums**:
  - `ShipmentSortKey`: CREATED_AT, SHIPPED_AT, DELIVERED_AT
  - `ShipmentSearchField`: ORDER_ID, TRACKING_NUMBER, CUSTOMER_NAME
  - `ShipmentDateField`: PAYMENT, ORDER_CONFIRMED, SHIPPED

#### Adapter-Out Layer

- **Adapter**: `ShipmentQueryAdapter`
  - `findByCriteria()`: Entity 목록 → Domain 목록 변환
  - `countByCriteria()`: count 값 반환

- **Repository**: `ShipmentQueryDslRepository`
  - `findByCriteria()`: WHERE + ORDER BY + LIMIT/OFFSET
  - `countByCriteria()`: COUNT(*) 서브쿼리

- **ConditionBuilder**: `ShipmentConditionBuilder`
  - `statusIn()`: `status IN (...)` 조건
  - `searchCondition()`: searchField별 LIKE 조건
  - `dateRange()`: dateField 기준 날짜 범위 조건
  - `notDeleted()`: `deleted_at IS NULL`

- **Database Query**:
  ```sql
  -- findByCriteria
  SELECT *
  FROM shipments
  WHERE deleted_at IS NULL
    AND status IN (...)              -- 옵션: 상태 필터
    AND tracking_number LIKE ?       -- 옵션: 검색 조건 (searchField 기준)
    AND shipped_at >= ?              -- 옵션: 날짜 범위 시작
    AND shipped_at <= ?              -- 옵션: 날짜 범위 끝
  ORDER BY created_at DESC           -- sortKey, sortDirection
  LIMIT ? OFFSET ?                   -- 페이징

  -- countByCriteria
  SELECT COUNT(*)
  FROM shipments
  WHERE deleted_at IS NULL
    AND status IN (...)
    AND tracking_number LIKE ?
    AND shipped_at >= ?
    AND shipped_at <= ?
  ```

---

## 3. GET /api/v1/market/shipments/{shipmentId} - 배송 상세 조회

### 호출 흐름 다이어그램

```
[Adapter-In]
ShipmentQueryController.getShipment(shipmentId)
  ├─ GetShipmentDetailUseCase.execute(shipmentId)            [Port Interface]
  └─ ShipmentQueryApiMapper.toDetailResponse(result)         [Response 변환]

[Application]
GetShipmentDetailService.execute(shipmentId)                 [UseCase 구현]
  ├─ ShipmentReadManager.getById(ShipmentId.of(shipmentId))
  │   └─ ShipmentQueryPort.findById(id)                      [Domain Port]
  │       orElseThrow(ShipmentNotFoundException)
  └─ ShipmentAssembler.toDetailResult(shipment)              [Result 변환]

[Adapter-Out]
ShipmentQueryAdapter.findById(id)                            [Port 구현]
  └─ ShipmentQueryDslRepository.findById(id.value())
      └─ QueryDSL: WHERE id = ? AND deleted_at IS NULL

[Database]
- shipments (단건 조회)
```

### Layer별 상세

#### Adapter-In Layer

- **Controller**: `ShipmentQueryController`
  - Method: `getShipment(@PathVariable String shipmentId)`
  - Response: `ResponseEntity<ApiResponse<ShipmentDetailApiResponse>>`
  - 권한: `@RequirePermission("shipment:read")`
  - HTTP Status: 200 OK / 404 NOT FOUND

- **Request DTO**: 없음 (`@PathVariable String shipmentId`만 사용)

- **Response DTO**: `ShipmentDetailApiResponse`
  ```java
  record ShipmentDetailApiResponse(
    String shipmentId,
    String shipmentNumber,
    String orderId,
    String orderNumber,
    String status,
    ShipmentMethodApiResponse shipmentMethod,  // 중첩 Record
    String trackingNumber,
    String orderConfirmedAt,  // ISO8601 문자열
    String shippedAt,         // ISO8601 문자열
    String deliveredAt,       // ISO8601 문자열
    String createdAt,         // ISO8601 문자열
    String updatedAt          // ISO8601 문자열
  )

  record ShipmentMethodApiResponse(
    String type,          // 배송 방법 유형 (PARCEL, COURIER 등)
    String courierCode,   // 택배사 코드
    String courierName    // 택배사명
  )
  ```

- **ApiMapper**: `ShipmentQueryApiMapper`
  - `toDetailResponse(ShipmentDetailResult)` → `ShipmentDetailApiResponse`
  - `result.shipmentMethod() != null` 체크 후 `ShipmentMethodApiResponse` 생성
  - 모든 날짜 필드 `DateTimeFormatUtils.formatIso8601()` 변환

#### Application Layer

- **UseCase Interface**: `GetShipmentDetailUseCase`
  - `execute(String shipmentId)` → `ShipmentDetailResult`

- **Service 구현**: `GetShipmentDetailService`
  - `ShipmentId.of(shipmentId)`로 Value Object 생성
  - ReadManager로 단건 조회 (없으면 `ShipmentNotFoundException`)
  - Assembler로 Detail Result 변환

- **Manager**: `ShipmentReadManager`
  - `getById(ShipmentId)`: `@Transactional(readOnly = true)`
  - 없으면 `ShipmentNotFoundException` 발생

- **Assembler**: `ShipmentAssembler`
  - `toDetailResult(Shipment)` → `ShipmentDetailResult`
  - `toMethodResult(ShipmentMethod)` → `ShipmentMethodResult` (null 안전 처리)

- **Result DTO**: `ShipmentDetailResult`
  ```java
  record ShipmentDetailResult(
    String shipmentId, String shipmentNumber,
    String orderId, String orderNumber,
    String status,
    ShipmentMethodResult shipmentMethod,  // 중첩 Record
    String trackingNumber,
    Instant orderConfirmedAt, Instant shippedAt,
    Instant deliveredAt, Instant createdAt, Instant updatedAt
  )

  record ShipmentMethodResult(
    String type, String courierCode, String courierName
  )
  ```

#### Domain Layer

- **Port**: `ShipmentQueryPort`
  - `findById(ShipmentId)` → `Optional<Shipment>`

- **Value Object**: `ShipmentId`
  - `ShipmentId.of(String value)` 정적 팩토리

- **Exception**: `ShipmentNotFoundException`
  - 배송을 찾을 수 없을 때 발생

#### Adapter-Out Layer

- **Adapter**: `ShipmentQueryAdapter`
  - `findById(ShipmentId)`: `repository.findById(id.value()).map(mapper::toDomain)`

- **Repository**: `ShipmentQueryDslRepository`
  - `findById(String id)`: 단건 조회

- **Mapper**: `ShipmentJpaEntityMapper`
  - `toDomain(ShipmentJpaEntity)` → `Shipment`
  - `resolveShipmentMethod()`: shipmentMethodType이 null이면 null 반환

- **Database Query**:
  ```sql
  SELECT *
  FROM shipments
  WHERE id = ?
    AND deleted_at IS NULL
  LIMIT 1
  ```

---

## 4. POST /api/v1/market/shipments/confirm/batch - 발주확인 일괄 처리

### 호출 흐름 다이어그램

```
[Adapter-In]
ShipmentCommandController.confirmBatch(ConfirmShipmentBatchApiRequest)
  ├─ ShipmentCommandApiMapper.toConfirmBatchCommand(request) [Command 변환]
  ├─ ConfirmShipmentBatchUseCase.execute(command)            [Port Interface]
  └─ ShipmentCommandApiMapper.toBatchResultResponse(result)  [Response 변환]

[Application]
ConfirmShipmentBatchService.execute(command)                 [UseCase 구현]
  ├─ ShipmentCommandFactory.createConfirmContexts(command)
  │   └─ BulkStatusChangeContext<ShipmentId> 생성 (ids + changedAt)
  └─ [각 shipmentId에 대해 반복]
      ├─ ShipmentReadManager.getById(shipmentId)             [조회 + 트랜잭션]
      │   └─ ShipmentQueryPort.findById()                    [Domain Port]
      ├─ shipment.prepare(changedAt)                         [Domain 비즈니스 로직]
      │   └─ READY → PREPARING 상태 전이 + orderConfirmedAt 설정
      └─ ShipmentCommandManager.persist(shipment)            [저장 + 트랜잭션]
          └─ ShipmentCommandPort.persist()                   [Domain Port]

[Adapter-Out]
ShipmentCommandAdapter.persist(shipment)                     [Port 구현]
  ├─ ShipmentJpaEntityMapper.toEntity(shipment)
  └─ ShipmentJpaRepository.save(entity)

[Database]
- UPDATE shipments (상태, orderConfirmedAt, updatedAt 변경)
```

### Layer별 상세

#### Adapter-In Layer

- **Controller**: `ShipmentCommandController`
  - Method: `confirmBatch(@RequestBody @Valid ConfirmShipmentBatchApiRequest)`
  - Response: `ResponseEntity<ApiResponse<BatchResultApiResponse>>`
  - 권한: `@RequirePermission("shipment:write")`
  - HTTP Status: 200 OK (일부 실패 포함 가능) / 400 BAD REQUEST

- **Request DTO**: `ConfirmShipmentBatchApiRequest`
  ```java
  record ConfirmShipmentBatchApiRequest(
    @NotEmpty List<String> shipmentIds  // 발주확인 대상 배송 ID 목록
  )
  ```

- **Response DTO**: `BatchResultApiResponse`
  ```java
  record BatchResultApiResponse(
    int totalCount,
    int successCount,
    int failureCount,
    List<BatchResultItemApiResponse> results
  )

  record BatchResultItemApiResponse(
    String id,          // 배송 ID
    boolean success,    // 성공 여부
    String errorCode,   // 에러 코드 (실패 시)
    String errorMessage // 에러 메시지 (실패 시)
  )
  ```

- **ApiMapper**: `ShipmentCommandApiMapper`
  - `toConfirmBatchCommand(ConfirmShipmentBatchApiRequest)` → `ConfirmShipmentBatchCommand`
  - `toBatchResultResponse(BatchProcessingResult<String>)` → `BatchResultApiResponse`

#### Application Layer

- **UseCase Interface**: `ConfirmShipmentBatchUseCase`
  - `execute(ConfirmShipmentBatchCommand)` → `BatchProcessingResult<String>`

- **Service 구현**: `ConfirmShipmentBatchService`
  - Factory로 `BulkStatusChangeContext<ShipmentId>` 생성 (ids + changedAt)
  - 각 shipmentId 루프: 조회 → 도메인 로직 → 저장
  - 개별 예외 catch 후 `BatchItemResult.failure()` 기록
  - 최종 `BatchProcessingResult.from(results)` 반환

- **Command DTO**: `ConfirmShipmentBatchCommand`
  ```java
  record ConfirmShipmentBatchCommand(List<String> shipmentIds)
  ```

- **Factory**: `ShipmentCommandFactory`
  - `createConfirmContexts(command)`:
    - `TimeProvider.now()` 호출 (APP-TIM-001: TimeProvider는 Factory에서만 사용)
    - `List<ShipmentId>` 생성
    - `BulkStatusChangeContext<ShipmentId>` 반환

- **Manager**: `ShipmentReadManager`
  - `getById(ShipmentId)`: `@Transactional(readOnly = true)`

- **Manager**: `ShipmentCommandManager`
  - `persist(Shipment)`: `@Transactional`
  - `ShipmentCommandPort.persist()` 호출

#### Domain Layer

- **Aggregate**: `Shipment`
  - `prepare(Instant now)`: 발주확인 상태 전이
    - `validateTransition(READY, PREPARING)` 검증
    - `status = PREPARING`
    - `orderConfirmedAt = now`
    - `updatedAt = now`
  - 현재 상태가 READY가 아니면 `ShipmentException(INVALID_STATUS_TRANSITION)` 발생

- **Port**: `ShipmentQueryPort.findById()`, `ShipmentCommandPort.persist()`

- **Exception**: `ShipmentErrorCode.INVALID_STATUS_TRANSITION`

#### Adapter-Out Layer

- **Adapter**: `ShipmentCommandAdapter`
  - `persist(Shipment)`: Entity 변환 후 JPA save

- **Repository**: `ShipmentJpaRepository`
  - `save(ShipmentJpaEntity)`: Spring Data JPA

- **Mapper**: `ShipmentJpaEntityMapper`
  - `toEntity(Shipment)` → `ShipmentJpaEntity.create(...)`
  - 상태, orderConfirmedAt, updatedAt 변경 반영

- **Database Query**:
  ```sql
  -- shipment.prepare() 후 persist
  UPDATE shipments SET
    status = 'PREPARING',
    order_confirmed_at = ?,
    updated_at = ?
  WHERE id = ?
  ```

---

## 5. POST /api/v1/market/shipments/ship/batch - 송장등록 일괄 처리

### 호출 흐름 다이어그램

```
[Adapter-In]
ShipmentCommandController.shipBatch(ShipBatchApiRequest)
  ├─ ShipmentCommandApiMapper.toShipBatchCommand(request)    [Command 변환]
  ├─ ShipBatchUseCase.execute(command)                       [Port Interface]
  └─ ShipmentCommandApiMapper.toBatchResultResponse(result)  [Response 변환]

[Application]
ShipBatchService.execute(command)                            [UseCase 구현]
  ├─ ShipmentCommandFactory.createShipContexts(command)
  │   └─ List<UpdateContext<ShipmentId, ShipmentShipData>> 생성
  │       (shipmentId + ShipmentShipData + changedAt)
  └─ [각 UpdateContext에 대해 반복]
      ├─ ShipmentReadManager.getById(ctx.id())               [조회]
      │   └─ ShipmentQueryPort.findById()                    [Domain Port]
      ├─ shipment.ship(trackingNumber, method, changedAt)    [Domain 비즈니스 로직]
      │   └─ PREPARING → SHIPPED 상태 전이 + 송장/택배사 정보 설정
      └─ ShipmentCommandManager.persist(shipment)            [저장]
          └─ ShipmentCommandPort.persist()                   [Domain Port]

[Adapter-Out]
ShipmentCommandAdapter.persist(shipment)                     [Port 구현]
  ├─ ShipmentJpaEntityMapper.toEntity(shipment)
  └─ ShipmentJpaRepository.save(entity)

[Database]
- UPDATE shipments (상태, trackingNumber, courierCode, courierName, shippedAt, updatedAt 변경)
```

### Layer별 상세

#### Adapter-In Layer

- **Controller**: `ShipmentCommandController`
  - Method: `shipBatch(@RequestBody @Valid ShipBatchApiRequest)`
  - Response: `ResponseEntity<ApiResponse<BatchResultApiResponse>>`
  - 권한: `@RequirePermission("shipment:write")`
  - HTTP Status: 200 OK (일부 실패 포함 가능) / 400 BAD REQUEST

- **Request DTO**: `ShipBatchApiRequest`
  ```java
  record ShipBatchApiRequest(
    @NotEmpty @Valid List<ShipBatchItemApiRequest> items
  )

  record ShipBatchItemApiRequest(
    @NotBlank String shipmentId,
    @NotBlank String trackingNumber,
    @NotBlank String courierCode,
    @NotBlank String courierName,
    @NotBlank String shipmentMethodType
  )
  ```

- **Response DTO**: `BatchResultApiResponse` (C1과 동일한 구조)

- **ApiMapper**: `ShipmentCommandApiMapper`
  - `toShipBatchCommand(ShipBatchApiRequest)` → `ShipBatchCommand`
    - `items.stream().map(toShipBatchItem)` 변환
  - `toBatchResultResponse(BatchProcessingResult<String>)` → `BatchResultApiResponse`

#### Application Layer

- **UseCase Interface**: `ShipBatchUseCase`
  - `execute(ShipBatchCommand)` → `BatchProcessingResult<String>`

- **Service 구현**: `ShipBatchService`
  - Factory로 `List<UpdateContext<ShipmentId, ShipmentShipData>>` 생성
  - 각 context 루프: 조회 → 도메인 로직 → 저장
  - 개별 예외 catch 후 `BatchItemResult.failure()` 기록

- **Command DTO**: `ShipBatchCommand`
  ```java
  record ShipBatchCommand(List<ShipBatchItem> items)

  record ShipBatchItem(
    String shipmentId, String trackingNumber,
    String courierCode, String courierName, String shipmentMethodType
  )
  ```

- **Factory**: `ShipmentCommandFactory`
  - `createShipContexts(command)`:
    - `TimeProvider.now()` 호출 (단일 changedAt으로 배치 일관성 보장)
    - 각 item을 `ShipmentMethod.of(type, courierCode, courierName)` 생성
    - `ShipmentShipData.of(trackingNumber, method)` 생성
    - `UpdateContext<ShipmentId, ShipmentShipData>` 반환
  - `resolveMethodType(String)`: String → `ShipmentMethodType` enum (기본값: COURIER)

- **Domain VO**: `ShipmentShipData`
  ```java
  // ShipmentShipData.of(trackingNumber, ShipmentMethod)
  ```

#### Domain Layer

- **Aggregate**: `Shipment`
  - `ship(String trackingNumber, ShipmentMethod method, Instant now)`:
    - `validateTransition(PREPARING, SHIPPED)` 검증
    - trackingNumber null/blank 검증 (`TRACKING_NUMBER_REQUIRED` 예외)
    - `status = SHIPPED`
    - `trackingNumber = trackingNumber`
    - `shipmentMethod = method`
    - `shippedAt = now`
    - `updatedAt = now`

- **Value Object**: `ShipmentMethod`
  - `type`: `ShipmentMethodType` (PARCEL, COURIER 등)
  - `courierCode`, `courierName`

#### Adapter-Out Layer

- **Adapter**: `ShipmentCommandAdapter`
  - `persist(Shipment)`: Entity 변환 후 JPA save

- **Mapper**: `ShipmentJpaEntityMapper`
  - `toEntity(Shipment)`:
    - `method != null ? method.type().name() : null`
    - shipmentMethodType, courierCode, courierName, trackingNumber, shippedAt 반영

- **Database Query**:
  ```sql
  -- shipment.ship() 후 persist
  UPDATE shipments SET
    status = 'SHIPPED',
    shipment_method_type = ?,
    courier_code = ?,
    courier_name = ?,
    tracking_number = ?,
    shipped_at = ?,
    updated_at = ?
  WHERE id = ?
  ```

---

## 6. POST /api/v1/market/shipments/orders/{orderId}/ship - 단건 송장등록

### 호출 흐름 다이어그램

```
[Adapter-In]
ShipmentCommandController.shipSingle(orderId, ShipSingleApiRequest)
  ├─ ShipmentCommandApiMapper.toShipSingleCommand(orderId, request)  [Command 변환]
  └─ ShipSingleUseCase.execute(command)                              [Port Interface]

[Application]
ShipSingleService.execute(command)                                   [UseCase 구현]
  ├─ ShipmentCommandFactory.createShipSingleContext(command)
  │   └─ ShipSingleContext (orderId + ShipmentShipData + changedAt)
  ├─ ShipmentReadManager.getByOrderId(context.orderId())             [조회]
  │   └─ ShipmentQueryPort.findByOrderId(orderId)                    [Domain Port]
  │       orElseThrow(ShipmentNotFoundException)
  ├─ shipment.ship(shipData.trackingNumber(), shipData.method(), changedAt)
  │   └─ PREPARING → SHIPPED 상태 전이 (도메인 비즈니스 로직)
  └─ ShipmentCommandManager.persist(shipment)                        [저장]
      └─ ShipmentCommandPort.persist()                               [Domain Port]

[Adapter-Out]
ShipmentCommandAdapter.persist(shipment)                             [Port 구현]
  ├─ ShipmentJpaEntityMapper.toEntity(shipment)
  └─ ShipmentJpaRepository.save(entity)

[Database]
- UPDATE shipments (상태, 송장 정보, shippedAt 변경)
```

### Layer별 상세

#### Adapter-In Layer

- **Controller**: `ShipmentCommandController`
  - Method: `shipSingle(@PathVariable String orderId, @RequestBody @Valid ShipSingleApiRequest)`
  - Response: `ResponseEntity<ApiResponse<Void>>`
  - 권한: `@RequirePermission("shipment:write")`
  - HTTP Status: 200 OK / 400 BAD REQUEST / 404 NOT FOUND

- **Request DTO**: `ShipSingleApiRequest`
  ```java
  record ShipSingleApiRequest(
    @NotBlank String trackingNumber,
    @NotBlank String courierCode,
    @NotBlank String courierName,
    @NotBlank String shipmentMethodType
  )
  ```

- **Response DTO**: 없음 (`ApiResponse<Void>`)

- **ApiMapper**: `ShipmentCommandApiMapper`
  - `toShipSingleCommand(String orderId, ShipSingleApiRequest)` → `ShipSingleCommand`
  - orderId는 PathVariable에서 주입

#### Application Layer

- **UseCase Interface**: `ShipSingleUseCase`
  - `execute(ShipSingleCommand)` → `void`

- **Service 구현**: `ShipSingleService`
  - Factory로 `ShipSingleContext` 생성 (orderId + shipData + changedAt)
  - ReadManager로 orderId 기반 단건 조회 (없으면 404)
  - 도메인 `ship()` 호출
  - CommandManager로 저장

- **Command DTO**: `ShipSingleCommand`
  ```java
  record ShipSingleCommand(
    String orderId,
    String trackingNumber,
    String courierCode,
    String courierName,
    String shipmentMethodType
  )
  ```

- **Factory**: `ShipmentCommandFactory`
  - `createShipSingleContext(command)` → `ShipSingleContext`
    - `TimeProvider.now()` 호출
    - `createShipmentMethod(shipmentMethodType, courierCode, courierName)` 생성
    - `ShipmentShipData.of(trackingNumber, method)` 생성
    - 내부 Record `ShipSingleContext(orderId, shipData, changedAt)` 반환

  ```java
  // Factory 내부 Record
  public record ShipSingleContext(
    String orderId, ShipmentShipData shipData, Instant changedAt
  )
  ```

- **Manager**: `ShipmentReadManager`
  - `getByOrderId(String orderId)`: `@Transactional(readOnly = true)`
  - 없으면 `ShipmentNotFoundException(orderId)` 발생

#### Domain Layer

- **Port**: `ShipmentQueryPort`
  - `findByOrderId(String orderId)` → `Optional<Shipment>`

- **Aggregate**: `Shipment.ship()` (C2와 동일한 도메인 로직)

#### Adapter-Out Layer

- **Repository**: `ShipmentQueryDslRepository`
  - `findByOrderId(String orderId)`: `WHERE order_id = ? AND deleted_at IS NULL`

- **Database Query**:
  ```sql
  -- findByOrderId
  SELECT *
  FROM shipments
  WHERE order_id = ?
    AND deleted_at IS NULL

  -- shipment.ship() 후 persist
  UPDATE shipments SET
    status = 'SHIPPED',
    shipment_method_type = ?,
    courier_code = ?,
    courier_name = ?,
    tracking_number = ?,
    shipped_at = ?,
    updated_at = ?
  WHERE id = ?
  ```

---

## 공통 패턴 분석

### 1. Hexagonal Architecture 레이어 격리

| 레이어 | DTO/모델 | 책임 |
|--------|----------|------|
| **Adapter-In** | `*ApiRequest`, `*ApiResponse` | HTTP 계층 관심사 (Validation, 직렬화, 날짜 포맷) |
| **Application** | `*Command`, `*Params`, `*Result` | 유스케이스 조율, 트랜잭션 경계 |
| **Domain** | `Shipment`, `ShipmentSearchCriteria`, VO | 비즈니스 규칙, 상태 전이 검증 |
| **Adapter-Out** | `ShipmentJpaEntity` | 영속화 기술 관심사 (JPA, QueryDSL) |

### 2. CQRS 분리

- **Query**: `ShipmentQueryController` → `ShipmentQueryApiMapper` → `GetShipment*UseCase` → `GetShipment*Service`
- **Command**: `ShipmentCommandController` → `ShipmentCommandApiMapper` → `*UseCase` → `*Service`

### 3. 트랜잭션 경계

| 계층 | @Transactional 위치 | 비고 |
|------|---------------------|------|
| Adapter-In | 없음 | Controller에 트랜잭션 금지 |
| Application Service | 없음 | Service에서 Manager에 위임 |
| **ReadManager** | `@Transactional(readOnly = true)` | 조회 트랜잭션 |
| **CommandManager** | `@Transactional` | 쓰기 트랜잭션 |
| Adapter-Out | 없음 | Adapter/Repository에 트랜잭션 금지 |

> **특이점**: Seller 도메인과 달리 Facade 계층 없이 Manager 레벨에서 직접 트랜잭션 관리.
> Command Service가 ReadManager + CommandManager를 순차 호출하며 각각 별도 트랜잭션으로 동작.

### 4. Factory 패턴 (APP-TIM-001)

`TimeProvider.now()` 호출은 Factory에서만 허용:
- `ShipmentCommandFactory.createConfirmContexts()`: changedAt 생성
- `ShipmentCommandFactory.createShipContexts()`: changedAt 생성 (배치 일관성)
- `ShipmentCommandFactory.createShipSingleContext()`: changedAt 생성

### 5. 배치 처리 패턴

C1(발주확인), C2(송장등록) 모두 배치 처리:
- **개별 예외 처리**: 각 항목을 try-catch로 감싸 부분 성공 허용
- **결과 집계**: `BatchProcessingResult.from(results)`
- **응답 구조**: totalCount / successCount / failureCount + 항목별 결과

### 6. 도메인 상태 전이 검증

`Shipment` Aggregate가 직접 상태 전이 유효성 검증:
- `prepare()`: `READY → PREPARING` (READY가 아니면 `INVALID_STATUS_TRANSITION`)
- `ship()`: `PREPARING → SHIPPED` (PREPARING이 아니면 `INVALID_STATUS_TRANSITION`)
- 배치 처리에서 상태 위반 시 해당 항목만 실패로 기록

### 7. Domain 조회 방식 비교

| 엔드포인트 | 조회 방식 | Port 메서드 |
|-----------|----------|------------|
| Q1 요약 | GROUP BY 집계 | `countByStatus()` |
| Q2 목록 | Criteria 검색 | `findByCriteria()` + `countByCriteria()` |
| Q3 상세 | ID 단건 | `findById()` |
| C1 발주확인 | ID 단건 (배치 루프) | `findById()` |
| C2 송장등록 | ID 단건 (배치 루프) | `findById()` |
| C3 단건 송장 | orderId 단건 | `findByOrderId()` |

### 8. 변환 체인

```
[Query]
ApiRequest → ShipmentSearchParams → ShipmentSearchCriteria
  → QueryDSL → ShipmentJpaEntity → Shipment (Domain)
  → ShipmentListResult / ShipmentDetailResult / ShipmentSummaryResult
  → ShipmentListApiResponse / ShipmentDetailApiResponse / ShipmentSummaryApiResponse

[Command]
ApiRequest → ShipmentCommand
  → Factory → Context (BulkStatusChangeContext / UpdateContext / ShipSingleContext)
  → Domain.method() → ShipmentJpaEntity → UPDATE
```

---

## JPA Entity 매핑

**테이블**: `shipments` (SoftDeletableEntity 상속: createdAt, updatedAt, deletedAt)

| 컬럼 | 타입 | 설명 |
|------|------|------|
| `id` | VARCHAR(36) | 배송 ID (PK) |
| `shipment_number` | VARCHAR(50) | 배송번호 |
| `order_id` | VARCHAR(36) | 주문 ID |
| `order_number` | VARCHAR(50) | 주문번호 |
| `status` | VARCHAR(20) | 배송 상태 (enum 이름) |
| `shipment_method_type` | VARCHAR(30) | 배송 방법 유형 |
| `courier_code` | VARCHAR(50) | 택배사 코드 |
| `courier_name` | VARCHAR(100) | 택배사명 |
| `tracking_number` | VARCHAR(100) | 송장번호 |
| `order_confirmed_at` | DATETIME | 발주확인일시 |
| `shipped_at` | DATETIME | 발송일시 |
| `delivered_at` | DATETIME | 배송완료일시 |
| `created_at` | DATETIME | 생성일시 |
| `updated_at` | DATETIME | 수정일시 |
| `deleted_at` | DATETIME | 삭제일시 (Soft Delete) |

---

**분석 일시**: 2026-02-18
**분석 대상**: Shipment 도메인 전체 (6개 엔드포인트)
**대상 모듈**: `adapter-in/rest-api` (web), `application`, `domain`, `adapter-out/persistence-mysql`
