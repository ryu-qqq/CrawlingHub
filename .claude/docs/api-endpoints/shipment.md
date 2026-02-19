# Shipment API Endpoints

## 요약

| 분류 | 개수 |
|------|------|
| Query (조회) | 3개 |
| Command (명령) | 3개 |
| **합계** | **6개** |

**Base Path**: `/api/v1/market/shipments`

---

## Query Endpoints

### Q1. getSummary (배송 상태별 요약 조회)

- **Path**: `GET /api/v1/market/shipments/summary`
- **Controller**: `ShipmentQueryController`
- **Method**: `getSummary()`
- **Request**: 없음 (파라미터 없음)
- **Response**: `ApiResponse<ShipmentSummaryApiResponse>`
- **UseCase**: `GetShipmentSummaryUseCase`
- **권한**: `shipment:read`

**설명**: 배송 상태별 건수를 요약 조회합니다. 현재 셀러 컨텍스트 기준으로 각 상태(READY, PREPARING, SHIPPED, IN_TRANSIT, DELIVERED, FAILED, CANCELLED)의 건수를 반환합니다.

**Response 구조**:
```json
{
  "ready": 5,
  "preparing": 12,
  "shipped": 34,
  "inTransit": 20,
  "delivered": 150,
  "failed": 2,
  "cancelled": 3
}
```

**HTTP Status**:
- `200 OK`: 조회 성공

---

### Q2. searchShipments (배송 목록 조회)

- **Path**: `GET /api/v1/market/shipments`
- **Controller**: `ShipmentQueryController`
- **Method**: `searchShipments(ShipmentSearchApiRequest request)`
- **Request**: `@ParameterObject @Valid ShipmentSearchApiRequest` (Query Parameters)
- **Response**: `ApiResponse<PageApiResponse<ShipmentListApiResponse>>`
- **UseCase**: `GetShipmentListUseCase`
- **권한**: `shipment:read`

**설명**: 검색 조건에 따라 배송 목록을 페이지 기반(Offset)으로 조회합니다.

**Query Parameters**:
| 파라미터 | 타입 | 필수 | 설명 | 예시 |
|---------|------|------|------|------|
| `statuses` | List\<String\> | X | 배송 상태 필터 | `READY,SHIPPED` |
| `searchField` | String | X | 검색 필드 (shipmentNumber, orderNumber, trackingNumber) | `trackingNumber` |
| `searchWord` | String | X | 검색어 | `1234567890` |
| `dateField` | String | X | 날짜 검색 대상 필드 (createdAt, shippedAt, deliveredAt) | `shippedAt` |
| `sortKey` | String | X | 정렬 키 (createdAt, shippedAt, deliveredAt) | `createdAt` |
| `sortDirection` | String | X | 정렬 방향 (ASC, DESC) | `DESC` |
| `page` | Integer | X | 페이지 번호 (0부터 시작) | `0` |
| `size` | Integer | X | 페이지 크기 | `20` |

**Response 구조**:
```json
{
  "items": [
    {
      "shipmentId": "SHP-00001",
      "shipmentNumber": "SN-20260101-001",
      "orderId": "ORD-00001",
      "orderNumber": "ON-20260101-001",
      "status": "SHIPPED",
      "trackingNumber": "1234567890",
      "courierName": "CJ대한통운",
      "shippedAt": "2026-01-01T10:00:00+09:00",
      "deliveredAt": null,
      "createdAt": "2026-01-01T09:00:00+09:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 100,
  "totalPages": 5
}
```

**HTTP Status**:
- `200 OK`: 조회 성공

---

### Q3. getShipment (배송 상세 조회)

- **Path**: `GET /api/v1/market/shipments/{shipmentId}`
- **Controller**: `ShipmentQueryController`
- **Method**: `getShipment(@PathVariable String shipmentId)`
- **Request**: `@PathVariable String shipmentId`
- **Response**: `ApiResponse<ShipmentDetailApiResponse>`
- **UseCase**: `GetShipmentDetailUseCase`
- **권한**: `shipment:read`

**설명**: 배송 ID로 배송 상세 정보를 조회합니다. 배송 방법(택배사, 배송유형) 정보를 포함한 전체 이력을 반환합니다.

**Response 구조**:
```json
{
  "shipmentId": "SHP-00001",
  "shipmentNumber": "SN-20260101-001",
  "orderId": "ORD-00001",
  "orderNumber": "ON-20260101-001",
  "status": "SHIPPED",
  "shipmentMethod": {
    "type": "PARCEL",
    "courierCode": "CJ",
    "courierName": "CJ대한통운"
  },
  "trackingNumber": "1234567890",
  "orderConfirmedAt": "2026-01-01T09:30:00+09:00",
  "shippedAt": "2026-01-01T10:00:00+09:00",
  "deliveredAt": null,
  "createdAt": "2026-01-01T09:00:00+09:00",
  "updatedAt": "2026-01-01T10:00:00+09:00"
}
```

**HTTP Status**:
- `200 OK`: 조회 성공
- `404 NOT FOUND`: 배송 정보를 찾을 수 없음

---

## Command Endpoints

### C1. confirmBatch (발주확인 일괄 처리)

- **Path**: `POST /api/v1/market/shipments/confirm/batch`
- **Controller**: `ShipmentCommandController`
- **Method**: `confirmBatch(ConfirmShipmentBatchApiRequest request)`
- **Request**: `@RequestBody @Valid ConfirmShipmentBatchApiRequest`
- **Response**: `ApiResponse<BatchResultApiResponse>`
- **UseCase**: `ConfirmShipmentBatchUseCase`
- **권한**: `shipment:write`

**설명**: 선택한 배송건의 발주를 일괄 확인합니다. 처리 결과는 배송 ID별 성공/실패 여부를 포함합니다.

**Request Body**:
```json
{
  "shipmentIds": [
    "SHP-00001",
    "SHP-00002",
    "SHP-00003"
  ]
}
```

**Request 필드**:
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `shipmentIds` | List\<String\> | Y | 발주확인 대상 배송 ID 목록 (최소 1개) |

**Response 구조**:
```json
{
  "totalCount": 3,
  "successCount": 2,
  "failureCount": 1,
  "results": [
    {
      "id": "SHP-00001",
      "success": true,
      "errorCode": null,
      "errorMessage": null
    },
    {
      "id": "SHP-00002",
      "success": true,
      "errorCode": null,
      "errorMessage": null
    },
    {
      "id": "SHP-00003",
      "success": false,
      "errorCode": "SHIPMENT_INVALID_STATUS",
      "errorMessage": "발주확인 가능한 상태가 아닙니다."
    }
  ]
}
```

**HTTP Status**:
- `200 OK`: 처리 완료 (일부 실패 포함 가능, results에서 개별 결과 확인)
- `400 BAD REQUEST`: 잘못된 요청 (shipmentIds가 비어있음)

---

### C2. shipBatch (송장등록 일괄 처리)

- **Path**: `POST /api/v1/market/shipments/ship/batch`
- **Controller**: `ShipmentCommandController`
- **Method**: `shipBatch(ShipBatchApiRequest request)`
- **Request**: `@RequestBody @Valid ShipBatchApiRequest`
- **Response**: `ApiResponse<BatchResultApiResponse>`
- **UseCase**: `ShipBatchUseCase`
- **권한**: `shipment:write`

**설명**: 선택한 배송건에 송장번호와 택배사 정보를 일괄 등록합니다. 발주확인 상태의 배송건에 대해 처리합니다.

**Request Body**:
```json
{
  "items": [
    {
      "shipmentId": "SHP-00001",
      "trackingNumber": "1234567890",
      "courierCode": "CJ",
      "courierName": "CJ대한통운",
      "shipmentMethodType": "PARCEL"
    },
    {
      "shipmentId": "SHP-00002",
      "trackingNumber": "0987654321",
      "courierCode": "LOTTE",
      "courierName": "롯데택배",
      "shipmentMethodType": "PARCEL"
    }
  ]
}
```

**Request 필드 (items 배열 각 항목)**:
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `shipmentId` | String | Y | 배송 ID |
| `trackingNumber` | String | Y | 송장번호 |
| `courierCode` | String | Y | 택배사 코드 |
| `courierName` | String | Y | 택배사명 |
| `shipmentMethodType` | String | Y | 배송 방법 유형 |

**Response 구조**: `BatchResultApiResponse` (C1과 동일한 구조)

**HTTP Status**:
- `200 OK`: 처리 완료 (일부 실패 포함 가능)
- `400 BAD REQUEST`: 잘못된 요청 (items가 비어있음 또는 필수 필드 누락)

---

### C3. shipSingle (단건 송장등록)

- **Path**: `POST /api/v1/market/shipments/orders/{orderId}/ship`
- **Controller**: `ShipmentCommandController`
- **Method**: `shipSingle(@PathVariable String orderId, ShipSingleApiRequest request)`
- **Request**:
  - `@PathVariable String orderId`
  - `@RequestBody @Valid ShipSingleApiRequest`
- **Response**: `ApiResponse<Void>`
- **UseCase**: `ShipSingleUseCase`
- **권한**: `shipment:write`

**설명**: 특정 주문(orderId)에 대해 송장번호와 택배사 정보를 단건으로 등록합니다.

**Request Body**:
```json
{
  "trackingNumber": "1234567890",
  "courierCode": "CJ",
  "courierName": "CJ대한통운",
  "shipmentMethodType": "PARCEL"
}
```

**Request 필드**:
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `trackingNumber` | String | Y | 송장번호 |
| `courierCode` | String | Y | 택배사 코드 |
| `courierName` | String | Y | 택배사명 |
| `shipmentMethodType` | String | Y | 배송 방법 유형 |

**Response**: 본문 없음 (Void)

**HTTP Status**:
- `200 OK`: 등록 성공
- `400 BAD REQUEST`: 잘못된 요청 (필수 필드 누락)
- `404 NOT FOUND`: 주문을 찾을 수 없음

---

## 엔드포인트 일람표

| # | Method | Path | Controller | UseCase | 분류 |
|---|--------|------|------------|---------|------|
| Q1 | GET | `/shipments/summary` | ShipmentQueryController | GetShipmentSummaryUseCase | Query |
| Q2 | GET | `/shipments` | ShipmentQueryController | GetShipmentListUseCase | Query |
| Q3 | GET | `/shipments/{shipmentId}` | ShipmentQueryController | GetShipmentDetailUseCase | Query |
| C1 | POST | `/shipments/confirm/batch` | ShipmentCommandController | ConfirmShipmentBatchUseCase | Command |
| C2 | POST | `/shipments/ship/batch` | ShipmentCommandController | ShipBatchUseCase | Command |
| C3 | POST | `/shipments/orders/{orderId}/ship` | ShipmentCommandController | ShipSingleUseCase | Command |

---

## 아키텍처 준수사항

### 컨트롤러 규칙
- ✅ **API-CTR-001**: `@RestController` 어노테이션 사용
- ✅ **API-CTR-003**: UseCase (Port-In) 인터페이스 의존
- ✅ **API-CTR-004**: `ResponseEntity<ApiResponse<T>>` 래핑
- ✅ **API-CTR-005**: Controller에 `@Transactional` 금지
- ✅ **API-CTR-007**: Controller에 비즈니스 로직 포함 금지
- ✅ **API-CTR-009**: `@Valid` 어노테이션 필수
- ✅ **API-CTR-010**: CQRS Controller 분리 (Query/Command)
- ✅ **API-CTR-011**: 목록 반환 시 `PageApiResponse` 페이징 적용 (Q2)
- ✅ **API-CTR-012**: URL 경로 소문자 + 복수형 (`/shipments`)

### DTO 규칙
- ✅ **API-DTO-001**: Record 타입 필수
- ✅ **API-DTO-003**: Validation 어노테이션 사용 (`@NotEmpty`, `@NotBlank`)
- ✅ **API-DTO-006**: 복잡한 구조는 중첩 Record로 표현 (`ShipmentDetailApiResponse.ShipmentMethodApiResponse`, `ShipBatchApiRequest.ShipBatchItemApiRequest`, `BatchResultApiResponse.BatchResultItemApiResponse`)
- ✅ **API-DTO-007**: `@Schema` 어노테이션 사용

### 엔드포인트 규칙
- ✅ **API-END-001**: `ShipmentEndpoints` final class + private 생성자
- ✅ **API-END-002**: static final 상수 사용
- ✅ **API-END-003**: Path Variable 상수화 (`SHIPMENT_ID`, `SHIP_SINGLE`)

---

## 관련 UseCase

### Command UseCases
- `ConfirmShipmentBatchUseCase`: 발주확인 일괄 처리
- `ShipBatchUseCase`: 송장등록 일괄 처리
- `ShipSingleUseCase`: 단건 송장등록

### Query UseCases
- `GetShipmentSummaryUseCase`: 배송 상태별 요약 조회
- `GetShipmentListUseCase`: 배송 목록 검색 (Offset 페이징)
- `GetShipmentDetailUseCase`: 배송 상세 조회

---

## 권한 요구사항

`@RequirePermission` 어노테이션 (AuthHub SDK)으로 권한 제어가 적용되어 있습니다.

| 권한 | 적용 엔드포인트 |
|------|----------------|
| `shipment:read` | Q1, Q2, Q3 (전체 조회 엔드포인트) |
| `shipment:write` | C1, C2, C3 (전체 명령 엔드포인트) |

---

## 배송 상태 정의

| 상태 | 설명 |
|------|------|
| `READY` | 배송 준비 대기 |
| `PREPARING` | 배송 준비 중 |
| `SHIPPED` | 발송 완료 |
| `IN_TRANSIT` | 배송 중 |
| `DELIVERED` | 배송 완료 |
| `FAILED` | 배송 실패 |
| `CANCELLED` | 취소 |

---

**분석 일시**: 2026-02-18
**분석 대상**: `/adapter-in/rest-api/src/main/java/com/ryuqq/marketplace/adapter/in/rest/shipment/`
**총 엔드포인트**: 6개 (Query 3개, Command 3개)
