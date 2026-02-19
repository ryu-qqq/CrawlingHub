# ImageVariant Domain API Flow Analysis

ImageVariant(이미지 Variant) 도메인의 전체 API 호출 흐름 분석 문서입니다.

이 도메인은 두 개의 서브 도메인으로 구성됩니다:
- **imagevariant**: ImageVariant(변환 완료 이미지) 조회
- **imagetransform**: Outbox 패턴 기반 비동기 이미지 변환 처리

---

## 도메인 특이사항

### 도메인 분리 구조

Controller는 `imagevariant` 패키지에 통합되어 있으나, 내부 의존 도메인이 다릅니다:

| Controller | UseCase 소속 | 처리 도메인 |
|------------|------------|------------|
| `ImageVariantQueryController` | `imagevariant` | ImageVariant 조회 |
| `ImageVariantCommandController` | `imagetransform` | ImageTransformOutbox 생성 (비동기) |

### Outbox 패턴 전체 생명주기

```
[API] requestTransform
  → ImageTransformOutbox 생성 (PENDING)
  → 202 Accepted 반환

[Scheduler 1: ProcessPending]
  PENDING → startProcessing() → PROCESSING
  + FileFlow API 호출 (transformRequestId 획득)

[Scheduler 2: PollProcessing]
  PROCESSING → FileFlow 상태 조회
    COMPLETED → ImageVariant 생성 + COMPLETED
    FAILED    → 재시도 or FAILED
    그 외     → no-op

[Scheduler 3: RecoverTimeout]
  PROCESSING (타임아웃) → PENDING (복구)
```

---

## 엔드포인트 목록

| HTTP Method | Path | 설명 | Controller Method |
|-------------|------|------|-------------------|
| GET | `/api/v1/market/image-variants/product-groups/{productGroupId}/images/{imageId}` | Variant 목록 조회 | `getVariantsByImageId()` |
| POST | `/api/v1/market/image-variants/product-groups/{productGroupId}/transform` | 이미지 변환 수동 요청 | `requestTransform()` |

---

## 1. GET /image-variants/product-groups/{productGroupId}/images/{imageId} - Variant 목록 조회

### 호출 흐름 다이어그램

```
[Adapter-In]
ImageVariantQueryController.getVariantsByImageId(productGroupId, imageId)
  ├─ GetImageVariantsByImageIdUseCase.execute(imageId)              [Port Interface]
  └─ ImageVariantQueryApiMapper.toApiResponses(results)            [Response 변환]

[Application]
GetImageVariantsByImageIdService.execute(imageId)                  [UseCase 구현]
  ├─ ImageVariantReadManager.findBySourceImageId(imageId, PRODUCT_GROUP_IMAGE)
  │   └─ ImageVariantQueryPort.findBySourceImageId()               [Port]
  └─ ImageVariantAssembler.toResults(variants)                     [Result 변환]

[Adapter-Out]
ImageVariantQueryAdapter                                           [Port 구현]
  ├─ ImageVariantQueryDslRepository.findBySourceImageId()
  │   └─ QueryDSL: WHERE source_image_id = ? AND source_type = ?
  └─ ImageVariantJpaEntityMapper.toDomain()                        [Entity → Domain]

[Database]
- image_variants (조회 대상)
```

### Layer별 상세

#### Adapter-In Layer

- **Controller**: `ImageVariantQueryController`
  - Method: `getVariantsByImageId(Long productGroupId, Long imageId)`
  - Response: `ResponseEntity<ApiResponse<List<ImageVariantApiResponse>>>`
  - HTTP Status: 200 OK
  - 권한: `@PreAuthorize("@access.isSellerOwnerOr(#productGroupId, 'product-group:read')")`
  - 주의: `productGroupId`는 권한 확인용으로만 사용. 실제 조회는 `imageId` 기준

- **Request**: Path Variable만 사용 (Request DTO 없음)
  - `@PathVariable Long productGroupId` - 권한 확인용
  - `@PathVariable Long imageId` - 실제 조회 키

- **Response DTO**: `ImageVariantApiResponse`
  ```java
  public record ImageVariantApiResponse(
      String variantType,   // "SMALL_WEBP" | "MEDIUM_WEBP" | "LARGE_WEBP" | "ORIGINAL_WEBP"
      String variantUrl,    // 변환된 이미지 CDN URL
      Integer width,        // 너비 (px)
      Integer height        // 높이 (px)
  )
  ```
  - `ImageVariantApiResponse.from(ImageVariantResult)` 정적 팩토리 메서드로 변환

- **ApiMapper**: `ImageVariantQueryApiMapper`
  - `toApiResponses(List<ImageVariantResult>)` → `List<ImageVariantApiResponse>`
  - 스트림으로 각 Result를 `ImageVariantApiResponse.from()`에 위임

#### Application Layer

- **UseCase Interface**: `GetImageVariantsByImageIdUseCase`
  - `execute(Long sourceImageId)` → `List<ImageVariantResult>`

- **Service 구현**: `GetImageVariantsByImageIdService`
  - `ImageVariantReadManager.findBySourceImageId(imageId, ImageSourceType.PRODUCT_GROUP_IMAGE)` 호출
  - `ImageVariantAssembler.toResults(variants)` 로 Result 변환

- **Manager**: `ImageVariantReadManager`
  - `findBySourceImageId(Long sourceImageId, ImageSourceType sourceType)` → `List<ImageVariant>`
  - **@Transactional(readOnly = true)**

- **Assembler**: `ImageVariantAssembler`
  - `toResults(List<ImageVariant>)` → `List<ImageVariantResult>`
  - `ImageVariantResult.from(ImageVariant)` 위임

- **Result DTO**: `ImageVariantResult`
  ```java
  public record ImageVariantResult(
      ImageVariantType variantType,
      String variantUrl,
      Integer width,
      Integer height
  )
  ```

#### Domain Layer

- **Port**: `ImageVariantQueryPort`
  - `findBySourceImageId(Long sourceImageId, ImageSourceType sourceType)` → `List<ImageVariant>`

- **Aggregate**: `ImageVariant`
  - `id`: `ImageVariantId` (Long 래퍼)
  - `sourceImageId`: `Long` - 원본 이미지 DB ID
  - `sourceType`: `ImageSourceType` - 이미지 소스 타입
  - `variantType`: `ImageVariantType` - Variant 타입 (SMALL_WEBP 등)
  - `resultAssetId`: `ResultAssetId` - FileFlow 변환 결과 에셋 ID
  - `variantUrl`: `ImageUrl` - 변환된 이미지 CDN URL
  - `dimension`: `ImageDimension` - 너비/높이
  - `createdAt`: `Instant`

- **VO**: `ImageVariantType`
  ```java
  public enum ImageVariantType {
      SMALL_WEBP(300, 300, "webp", "RESIZE", 85, "소형 WEBP"),
      MEDIUM_WEBP(600, 600, "webp", "RESIZE", 85, "중형 WEBP"),
      LARGE_WEBP(1200, 1200, "webp", "RESIZE", 85, "대형 WEBP"),
      ORIGINAL_WEBP(null, null, "webp", "CONVERT", 90, "원본 WEBP 변환")
  }
  ```

#### Adapter-Out Layer

- **Adapter**: `ImageVariantQueryAdapter`
  - `implements ImageVariantQueryPort`
  - `ImageVariantQueryDslRepository.findBySourceImageId()` 호출 후 `ImageVariantJpaEntityMapper.toDomain()` 으로 변환

- **Repository**: `ImageVariantQueryDslRepository`
  - QueryDSL 기반 단순 단건 조회

- **Entity**: `ImageVariantJpaEntity` → `@Table(name = "image_variants")`
  ```
  id              BIGINT PK
  source_image_id BIGINT NOT NULL
  source_type     VARCHAR(30) NOT NULL  -- "PRODUCT_GROUP_IMAGE"
  variant_type    VARCHAR(30) NOT NULL  -- "SMALL_WEBP" 등
  result_asset_id VARCHAR(100)
  variant_url     VARCHAR(500) NOT NULL
  width           INT
  height          INT
  created_at      DATETIME NOT NULL
  ```

- **Database Query**:
  ```sql
  SELECT *
  FROM image_variants
  WHERE source_image_id = ?
    AND source_type = 'PRODUCT_GROUP_IMAGE'
  ORDER BY created_at DESC
  ```

---

## 2. POST /image-variants/product-groups/{productGroupId}/transform - 이미지 변환 수동 요청

### 호출 흐름 다이어그램

```
[Adapter-In]
ImageVariantCommandController.requestTransform(productGroupId, request)
  ├─ ImageVariantCommandApiMapper.toCommand(productGroupId, request) [Command 변환]
  └─ RequestImageTransformUseCase.execute(command)                   [Port Interface]

[Application]
RequestImageTransformService.execute(command)                        [UseCase 구현]
  └─ ImageTransformRequestCoordinator.request(command)               [조율]
      ├─ ProductGroupImageReadManager.getByProductGroupId()          [이미지 조회]
      │   └─ ProductGroupImageQueryPort.findByProductGroupId()       [Port]
      ├─ 업로드 완료 이미지 필터 (isUploaded())
      ├─ ImageTransformOutboxReadManager.findActiveVariantTypesBySourceImageIds()
      │   └─ ImageTransformOutboxQueryPort.findActiveVariantTypesBySourceImageIds() [Port]
      │       └─ 활성(PENDING/PROCESSING) Outbox 중복 방지
      ├─ ImageTransformOutboxFactory.createOutboxes()                 [Outbox 생성]
      │   └─ ImageTransformOutbox.forNew()                           [Domain 생성]
      └─ ImageTransformOutboxCommandManager.persistAll()             [Outbox 저장]
          └─ ImageTransformOutboxCommandPort.persist()               [Port]

[Adapter-Out]
ImageTransformOutboxCommandAdapter                                   [Port 구현]
  ├─ ImageTransformOutboxJpaEntityMapper.toEntity()
  └─ ImageTransformOutboxJpaRepository.save()

[Database]
- INSERT INTO image_transform_outboxes (status = 'PENDING')
```

### Layer별 상세

#### Adapter-In Layer

- **Controller**: `ImageVariantCommandController`
  - Method: `requestTransform(Long productGroupId, RequestImageTransformApiRequest request)`
  - Response: `ResponseEntity<Void>`
  - HTTP Status: 202 Accepted
  - 권한: `@PreAuthorize("@access.isSellerOwnerOr(#productGroupId, 'product-group:write')")`
  - `@RequestBody(required = false)` - Request Body 생략 가능

- **Request DTO**: `RequestImageTransformApiRequest`
  ```java
  public record RequestImageTransformApiRequest(
      List<String> variantTypes  // null 또는 빈 배열이면 전체 타입 대상
  )
  ```

- **ApiMapper**: `ImageVariantCommandApiMapper`
  - `toCommand(Long productGroupId, RequestImageTransformApiRequest)` → `RequestImageTransformCommand`
  - `request == null || variantTypes.isEmpty()` → `RequestImageTransformCommand.allVariants(productGroupId)` (전체 타입)
  - 그 외 → `ImageVariantType.valueOf(String)` 변환 후 `RequestImageTransformCommand.of(productGroupId, variantTypes)`

#### Application Layer

- **UseCase Interface**: `RequestImageTransformUseCase`
  - `execute(RequestImageTransformCommand command)` → `void`

- **Service 구현**: `RequestImageTransformService`
  - `ImageTransformRequestCoordinator.request(command)` 에 전임 위임

- **Command DTO**: `RequestImageTransformCommand`
  ```java
  public record RequestImageTransformCommand(
      Long productGroupId,
      List<ImageVariantType> variantTypes  // null이면 전체 타입
  )
  ```
  - `resolvedVariantTypes()`: null 또는 빈 목록이면 `ImageVariantType.values()` 전체 반환

- **Coordinator**: `ImageTransformRequestCoordinator`
  처리 순서:
  1. `ProductGroupImageReadManager.getByProductGroupId()` - 상품 그룹 이미지 목록 조회
  2. 업로드 완료 이미지 필터링 (`isUploaded()`)
  3. 업로드 이미지가 없으면 조기 반환
  4. `ImageTransformOutboxReadManager.findActiveVariantTypesBySourceImageIds()` - 이미 활성 상태(PENDING/PROCESSING)인 Outbox 조회 → 중복 방지
  5. 이미지별 요청 가능 Variant 타입 계산 (activeTypes 제외)
  6. `ImageTransformOutboxFactory.createOutboxes()` - Outbox 생성
  7. `ImageTransformOutboxCommandManager.persistAll()` - Outbox 일괄 저장

- **Factory**: `ImageTransformOutboxFactory`
  - `createOutboxes(sourceImageId, sourceType, uploadedUrl, variantTypes)` → `List<ImageTransformOutbox>`
  - 각 Variant 타입에 대해 `ImageTransformOutbox.forNew()` 호출
  - 멱등키: `ImageTransformOutboxIdempotencyKey.generate(sourceImageId, variantType, now)`

- **Manager**: `ImageTransformOutboxCommandManager`
  - `persistAll(List<ImageTransformOutbox>)`: 목록 순차 저장
  - **@Transactional**

#### Domain Layer

- **Port**: `ImageTransformOutboxCommandPort`
  - `persist(ImageTransformOutbox outbox)` → `Long`

- **Port**: `ImageTransformOutboxQueryPort`
  - `findActiveVariantTypesBySourceImageIds(List<Long>, List<ImageVariantType>)` → `Map<Long, Set<ImageVariantType>>`

- **Aggregate**: `ImageTransformOutbox`
  - 상태: `PENDING → PROCESSING → COMPLETED | FAILED`
  - 도메인 상태 전이 메서드:
    - `startProcessing(now, transformRequestId)` - PENDING → PROCESSING
    - `complete(now)` - PROCESSING → COMPLETED
    - `failAndRetry(errorMessage, now)` - 재시도 (maxRetry 미만이면 PENDING 복귀)
    - `fail(errorMessage, now)` - 즉시 FAILED
    - `recoverFromTimeout(now)` - PROCESSING → PENDING (타임아웃 복구)
  - 동시성 제어: `@Version` 낙관적 락, `idempotencyKey` 중복 방지

#### Adapter-Out Layer

- **Adapter**: `ImageTransformOutboxCommandAdapter`
  - `implements ImageTransformOutboxCommandPort`
  - `ImageTransformOutboxJpaEntityMapper.toEntity()` → `ImageTransformOutboxJpaRepository.save()`

- **Repository**: `ImageTransformOutboxJpaRepository`
  - `JpaRepository<ImageTransformOutboxJpaEntity, Long>` - save/saveAll만 사용

- **Entity**: `ImageTransformOutboxJpaEntity` → `@Table(name = "image_transform_outboxes")`
  ```
  id                  BIGINT PK
  source_image_id     BIGINT NOT NULL
  source_type         VARCHAR(30) NOT NULL
  uploaded_url        VARCHAR(500) NOT NULL
  variant_type        VARCHAR(30) NOT NULL
  transform_request_id VARCHAR(100)          -- FileFlow 변환 요청 ID
  status              VARCHAR(20) NOT NULL   -- PENDING|PROCESSING|COMPLETED|FAILED
  retry_count         INT NOT NULL
  max_retry           INT NOT NULL           -- 기본값 3
  created_at          DATETIME NOT NULL
  updated_at          DATETIME NOT NULL
  processed_at        DATETIME
  error_message       VARCHAR(1000)
  version             BIGINT NOT NULL        -- 낙관적 락
  idempotency_key     VARCHAR(100) NOT NULL UNIQUE
  ```

- **Database Query (중복 Outbox 조회)**:
  ```sql
  -- findActiveOutboxPairs (중복 방지용)
  SELECT source_image_id, variant_type
  FROM image_transform_outboxes
  WHERE source_image_id IN (?, ?, ...)
    AND variant_type IN ('SMALL_WEBP', 'MEDIUM_WEBP', ...)
    AND status IN ('PENDING', 'PROCESSING')
  ```

- **Database Query (Outbox 저장)**:
  ```sql
  INSERT INTO image_transform_outboxes (
    source_image_id, source_type, uploaded_url, variant_type,
    transform_request_id, status, retry_count, max_retry,
    created_at, updated_at, processed_at, error_message, version, idempotency_key
  ) VALUES (?, 'PRODUCT_GROUP_IMAGE', ?, ?, NULL, 'PENDING', 0, 3,
            NOW(), NOW(), NULL, NULL, 0, ?)
  ```

---

## 3. 스케줄러 - ImageTransformOutboxScheduler (비동기 후속 처리)

API 요청으로 생성된 PENDING Outbox를 실제로 처리하는 스케줄러입니다.
Controller를 통한 직접 호출이 아닌 `@Scheduled` 기반 자동 처리입니다.

### 3-1. ProcessPending - PENDING → PROCESSING

#### 호출 흐름

```
[Scheduler]
ImageTransformOutboxScheduler.processPending()
  └─ ProcessPendingImageTransformUseCase.execute(command)           [Port Interface]

[Application]
ProcessPendingImageTransformService.execute(command)               [UseCase 구현]
  ├─ ImageTransformOutboxReadManager.findPendingOutboxes(beforeTime, limit)
  │   └─ ImageTransformOutboxQueryPort.findPendingOutboxes()        [Port]
  └─ ImageTransformOutboxProcessor.processOutbox(outbox) [반복]
      ├─ outbox.startProcessing(now, null)                          [Domain: PENDING → PROCESSING]
      ├─ ImageTransformOutboxCommandManager.persist(outbox)         [PROCESSING 상태 저장]
      ├─ ImageTransformManager.createTransformRequest()             [외부 API 호출]
      │   └─ ImageTransformClient.createTransformRequest()          [Port: Client]
      │       └─ FileFlowTransformAdapter.createTransformRequest()  [구현]
      │           ├─ AssetApi.register()                            [FileFlow SDK]
      │           └─ TransformRequestApi.create()                   [FileFlow SDK]
      │               → transformRequestId 획득
      └─ outbox.startProcessing(now, transformRequestId)            [Domain: requestId 설정]
          └─ ImageTransformOutboxCommandManager.persist(outbox)     [최종 상태 저장]

[Database]
- image_transform_outboxes WHERE status = 'PENDING' AND retry_count < max_retry AND created_at < beforeTime
- UPDATE image_transform_outboxes SET status = 'PROCESSING', transform_request_id = ?, updated_at = ?
```

#### FileFlowTransformAdapter 처리 상세

```
createTransformRequest(uploadedUrl, variantType):
  1. CDN URL에서 S3 Key 추출 (mapper.extractS3Key)
  2. S3 Key에서 파일명 추출 (mapper.extractFileName)
  3. AssetApi.register() → assetId 획득 (S3 파일 → FileFlow Asset 등록)
  4. TransformRequestApi.create() → transformRequestId 획득
     - transformType: RESIZE (크기 변환) | CONVERT (포맷 변환만)
     - width/height: 변환 목표 크기 (ORIGINAL_WEBP는 null)
     - quality: 85 (RESIZE) | 90 (CONVERT)
     - format: WEBP
  5. ImageTransformResponse.pending(transformRequestId) 반환
```

### 3-2. PollProcessing - PROCESSING → COMPLETED | FAILED

#### 호출 흐름

```
[Scheduler]
ImageTransformOutboxScheduler.pollProcessing()
  └─ PollProcessingImageTransformUseCase.execute(command)           [Port Interface]

[Application]
PollProcessingImageTransformService.execute(command)               [UseCase 구현]
  ├─ ImageTransformOutboxReadManager.findProcessingOutboxes(limit)
  │   └─ ImageTransformOutboxQueryPort.findProcessingOutboxes()     [Port]
  └─ ImageTransformPollingProcessor.pollOutbox(outbox) [반복]
      └─ ImageTransformManager.getTransformRequest(transformRequestId)
          └─ ImageTransformClient.getTransformRequest()             [Port: Client]
              └─ FileFlowTransformAdapter.getTransformRequest()     [구현]
                  └─ TransformRequestApi.get(transformRequestId)
                      COMPLETED →
                        AssetApi.get(resultAssetId)                 [CDN URL 조회]
                        AssetApi.getMetadata(resultAssetId)         [width/height 조회]
                        ImageTransformResponse.completed(...)
                      FAILED → ImageTransformResponse.failed(...)
                      기타 → ImageTransformResponse.processing(...)

      COMPLETED:
        ImageTransformCompletionCoordinator.complete(outbox, response, now)  [@Transactional]
          ├─ ImageVariant.forNew()                                  [Domain 생성]
          ├─ ImageVariantCommandManager.persist(variant)            [image_variants INSERT]
          │   └─ ImageVariantCommandPort.persist()                  [Port]
          │       └─ ImageVariantCommandAdapter → ImageVariantJpaRepository.save()
          ├─ outbox.complete(now)                                   [Domain: COMPLETED]
          └─ ImageTransformOutboxCommandManager.persist(outbox)     [image_transform_outboxes UPDATE]

      FAILED:
        outbox.recordFailure(true, errorMessage, now)
          canRetry=true → failAndRetry() → PENDING (재시도) or FAILED (maxRetry 초과)
        ImageTransformOutboxCommandManager.persist(outbox)

[Database]
-- PROCESSING 목록 조회
SELECT * FROM image_transform_outboxes
WHERE status = 'PROCESSING'
ORDER BY updated_at ASC
LIMIT ?

-- COMPLETED 시: ImageVariant 저장 + Outbox 상태 업데이트 (동일 트랜잭션)
INSERT INTO image_variants (source_image_id, source_type, variant_type, result_asset_id,
                             variant_url, width, height, created_at)
VALUES (?, 'PRODUCT_GROUP_IMAGE', ?, ?, ?, ?, ?, NOW())

UPDATE image_transform_outboxes
SET status = 'COMPLETED', processed_at = NOW(), updated_at = NOW(), version = version + 1
WHERE id = ?
```

### 3-3. RecoverTimeout - PROCESSING(타임아웃) → PENDING

#### 호출 흐름

```
[Scheduler]
ImageTransformOutboxScheduler.recoverTimeout()
  └─ RecoverTimeoutImageTransformUseCase.execute(command)           [Port Interface]

[Application]
RecoverTimeoutImageTransformService.execute(command)               [@Transactional]
  ├─ ImageTransformOutboxReadManager.findProcessingTimeoutOutboxes(threshold, limit)
  │   └─ ImageTransformOutboxQueryPort.findProcessingTimeoutOutboxes()
  └─ [각 Outbox에 대해]
      ├─ outbox.recoverFromTimeout(now)                             [Domain: → PENDING]
      └─ ImageTransformOutboxCommandManager.persist(outbox)

[Database]
-- 타임아웃 목록 조회
SELECT * FROM image_transform_outboxes
WHERE status = 'PROCESSING'
  AND updated_at < ? (timeoutThreshold)
ORDER BY updated_at ASC
LIMIT ?

-- 복구 업데이트
UPDATE image_transform_outboxes
SET status = 'PENDING', updated_at = NOW(), error_message = '타임아웃으로 인한 복구'
WHERE id = ?
```

---

## 4. 공통 패턴 분석

### 1. Hexagonal Architecture 레이어 격리

| 레이어 | DTO/모델 | 책임 |
|--------|----------|------|
| **Adapter-In** | ApiRequest / ApiResponse | HTTP 계층 (권한, 직렬화) |
| **Application** | Command / Result | 유스케이스 조율, 트랜잭션 경계 |
| **Domain** | Aggregate, VO | 비즈니스 규칙, 상태 전이 |
| **Adapter-Out (DB)** | JpaEntity | JPA 영속화 |
| **Adapter-Out (Client)** | SDK 모델 | FileFlow 외부 API 호출 |

### 2. CQRS 분리

- **Query**: `ImageVariantQueryController` → `GetImageVariantsByImageIdUseCase` → `GetImageVariantsByImageIdService`
- **Command**: `ImageVariantCommandController` → `RequestImageTransformUseCase` → `RequestImageTransformService`

### 3. 트랜잭션 경계

| 컴포넌트 | @Transactional |
|----------|----------------|
| Controller | 없음 |
| Service | 없음 (Manager/Coordinator에 위임) |
| `ImageVariantReadManager` | readOnly = true |
| `ImageTransformOutboxReadManager` | readOnly = true |
| `ImageVariantCommandManager` | 기본 트랜잭션 |
| `ImageTransformOutboxCommandManager` | 기본 트랜잭션 |
| `ImageTransformCompletionCoordinator.complete()` | 기본 트랜잭션 (ImageVariant INSERT + Outbox UPDATE 원자적) |
| `RecoverTimeoutImageTransformService` | 기본 트랜잭션 |
| Adapter | 없음 |

### 4. 핵심 설계 결정

#### Outbox 패턴 적용 이유
- 이미지 변환은 외부 FileFlow API 호출 → 네트워크 실패/지연 가능
- API 요청 즉시 202 반환 후 Outbox에 작업 등록
- 스케줄러가 안전하게 재시도

#### 중복 Outbox 방지
- 변환 요청 시 PENDING/PROCESSING 상태 Outbox 조회 후 이미 처리 중인 Variant 타입 제외
- `idempotency_key` UNIQUE 제약으로 DB 레벨 중복 방지 (sourceImageId + variantType + 생성시각 해시)

#### 낙관적 락 (Optimistic Locking)
- `@Version` 어노테이션으로 복수 스케줄러 인스턴스 간 충돌 방지
- 동일 Outbox 동시 처리 차단

#### ImageTransformCompletionCoordinator의 @Transactional
- `ImageVariant` 저장 + `ImageTransformOutbox` 상태 완료 업데이트를 하나의 트랜잭션으로 묶음
- 변환 완료 후 Variant가 없거나 Outbox가 미완료 상태로 남는 불일치 방지

### 5. 변환 체인

```
[Query]
PathVariable(imageId)
  → ImageVariantQueryPort.findBySourceImageId()
  → ImageVariant[] (Domain)
  → ImageVariantResult[] (Application DTO)
  → ImageVariantApiResponse[] (API DTO)

[Command]
RequestImageTransformApiRequest
  → RequestImageTransformCommand (productGroupId + variantTypes)
  → ImageTransformOutbox[] (Domain, PENDING 상태)
  → ImageTransformOutboxJpaEntity[]
  → INSERT image_transform_outboxes

[Scheduler: PENDING → PROCESSING]
ImageTransformOutbox[] (PENDING)
  → FileFlow AssetApi.register() + TransformRequestApi.create()
  → outbox.startProcessing(transformRequestId)
  → UPDATE image_transform_outboxes SET status = 'PROCESSING'

[Scheduler: PROCESSING → COMPLETED]
ImageTransformOutbox[] (PROCESSING)
  → FileFlow TransformRequestApi.get() + AssetApi.get/getMetadata()
  → ImageVariant.forNew() (Domain)
  → INSERT image_variants
  → UPDATE image_transform_outboxes SET status = 'COMPLETED'
```

---

## 5. 에러 처리

### 도메인 예외

| 예외 | 에러 코드 | HTTP Status | 발생 조건 |
|------|----------|-------------|----------|
| `ImageVariantNotFoundException` | `IMGVAR-001` | 404 | 이미지 Variant 조회 결과 없음 |

### 에러 매퍼

- **`ImageVariantErrorMapper`**: `ImageVariantNotFoundException` → 404 Not Found
  - 에러 타입: `/errors/image-variant/imgvar-001`

### 스케줄러 에러 처리

| 상황 | 처리 방식 |
|------|----------|
| FileFlow API 호출 실패 | `outbox.recordFailure(canRetry=true, errorMessage)` → PENDING 복귀 (maxRetry 미만) or FAILED |
| maxRetry 초과 | `failAndRetry()` 내 FAILED 상태 전이 |
| PROCESSING 타임아웃 | `recoverFromTimeout()` → PENDING 복귀 (RecoverTimeout 스케줄러) |
| 낙관적 락 충돌 | `OptimisticLockingFailureException` → 해당 Outbox 건너뜀 |

---

## 6. 관련 파일 목록

### Adapter-In (rest-api)
| 파일 | 역할 |
|------|------|
| `ImageVariantAdminEndpoints.java` | URL 상수 관리 |
| `controller/ImageVariantQueryController.java` | Variant 조회 Controller |
| `controller/ImageVariantCommandController.java` | 변환 요청 Controller |
| `dto/command/RequestImageTransformApiRequest.java` | 변환 요청 Request DTO |
| `dto/response/ImageVariantApiResponse.java` | Variant 조회 Response DTO |
| `mapper/ImageVariantQueryApiMapper.java` | Query 응답 매퍼 |
| `mapper/ImageVariantCommandApiMapper.java` | Command 요청 매퍼 |
| `error/ImageVariantErrorMapper.java` | 에러 매퍼 |

### Adapter-In (scheduler)
| 파일 | 역할 |
|------|------|
| `imagetransform/ImageTransformOutboxScheduler.java` | 3개 스케줄러 Job 관리 |

### Application (imagevariant)
| 파일 | 역할 |
|------|------|
| `port/in/query/GetImageVariantsByImageIdUseCase.java` | Query Port-In |
| `service/query/GetImageVariantsByImageIdService.java` | Query UseCase 구현 |
| `manager/ImageVariantReadManager.java` | 읽기 전용 Manager |
| `manager/ImageVariantCommandManager.java` | 쓰기 Manager |
| `assembler/ImageVariantAssembler.java` | Domain → Result 변환 |
| `dto/response/ImageVariantResult.java` | Query Result DTO |
| `port/out/query/ImageVariantQueryPort.java` | Query Port-Out |
| `port/out/command/ImageVariantCommandPort.java` | Command Port-Out |

### Application (imagetransform)
| 파일 | 역할 |
|------|------|
| `port/in/command/RequestImageTransformUseCase.java` | Command Port-In |
| `port/in/command/ProcessPendingImageTransformUseCase.java` | 스케줄러 Port-In |
| `port/in/command/PollProcessingImageTransformUseCase.java` | 스케줄러 Port-In |
| `port/in/command/RecoverTimeoutImageTransformUseCase.java` | 스케줄러 Port-In |
| `service/command/RequestImageTransformService.java` | 수동 요청 UseCase 구현 |
| `service/command/ProcessPendingImageTransformService.java` | PENDING 처리 UseCase 구현 |
| `service/command/PollProcessingImageTransformService.java` | 폴링 UseCase 구현 |
| `service/command/RecoverTimeoutImageTransformService.java` | 타임아웃 복구 UseCase 구현 |
| `internal/ImageTransformRequestCoordinator.java` | 수동 요청 조율 |
| `internal/ImageTransformOutboxProcessor.java` | PENDING → PROCESSING 처리 |
| `internal/ImageTransformPollingProcessor.java` | PROCESSING → COMPLETED/FAILED 폴링 |
| `internal/ImageTransformCompletionCoordinator.java` | 완료 처리 (트랜잭션) |
| `factory/ImageTransformOutboxFactory.java` | Outbox 생성 Factory |
| `manager/ImageTransformOutboxCommandManager.java` | Outbox 쓰기 Manager |
| `manager/ImageTransformOutboxReadManager.java` | Outbox 읽기 Manager |
| `manager/ImageTransformManager.java` | 외부 Client 위임 Manager |
| `port/out/command/ImageTransformOutboxCommandPort.java` | Outbox Command Port-Out |
| `port/out/query/ImageTransformOutboxQueryPort.java` | Outbox Query Port-Out |
| `port/out/client/ImageTransformClient.java` | 외부 API Client Port-Out |
| `dto/command/RequestImageTransformCommand.java` | 수동 요청 Command |
| `dto/response/ImageTransformResponse.java` | 외부 API 응답 DTO |

### Domain (imagevariant)
| 파일 | 역할 |
|------|------|
| `aggregate/ImageVariant.java` | ImageVariant Aggregate Root |
| `vo/ImageVariantType.java` | Variant 타입 Enum (크기/품질/포맷 정보 포함) |
| `vo/ImageDimension.java` | 이미지 크기 VO |
| `vo/ResultAssetId.java` | FileFlow 결과 에셋 ID VO |
| `id/ImageVariantId.java` | Variant ID VO |
| `exception/ImageVariantNotFoundException.java` | 조회 실패 예외 |
| `exception/ImageVariantErrorCode.java` | 에러 코드 Enum |

### Domain (imagetransform)
| 파일 | 역할 |
|------|------|
| `aggregate/ImageTransformOutbox.java` | Outbox Aggregate Root (상태 전이 포함) |
| `vo/ImageTransformOutboxStatus.java` | 상태 Enum (PENDING/PROCESSING/COMPLETED/FAILED) |
| `vo/ImageTransformOutboxIdempotencyKey.java` | 멱등키 VO |
| `id/ImageTransformOutboxId.java` | Outbox ID VO |

### Adapter-Out (persistence-mysql)
| 파일 | 역할 |
|------|------|
| `imagevariant/adapter/ImageVariantQueryAdapter.java` | ImageVariantQueryPort 구현 |
| `imagevariant/adapter/ImageVariantCommandAdapter.java` | ImageVariantCommandPort 구현 |
| `imagevariant/repository/ImageVariantQueryDslRepository.java` | QueryDSL 조회 |
| `imagevariant/repository/ImageVariantJpaRepository.java` | JPA 저장 |
| `imagevariant/entity/ImageVariantJpaEntity.java` | image_variants 테이블 |
| `imagevariant/mapper/ImageVariantJpaEntityMapper.java` | Entity ↔ Domain 변환 |
| `imagetransform/adapter/ImageTransformOutboxQueryAdapter.java` | OutboxQueryPort 구현 |
| `imagetransform/adapter/ImageTransformOutboxCommandAdapter.java` | OutboxCommandPort 구현 |
| `imagetransform/repository/ImageTransformOutboxQueryDslRepository.java` | QueryDSL 조회 |
| `imagetransform/repository/ImageTransformOutboxJpaRepository.java` | JPA 저장 |
| `imagetransform/entity/ImageTransformOutboxJpaEntity.java` | image_transform_outboxes 테이블 |
| `imagetransform/mapper/ImageTransformOutboxJpaEntityMapper.java` | Entity ↔ Domain 변환 |

### Adapter-Out (client: fileflow-client)
| 파일 | 역할 |
|------|------|
| `adapter/FileFlowTransformAdapter.java` | ImageTransformClient 구현 (FileFlow SDK 사용) |

---

**분석 일시**: 2026-02-18
**분석 대상**: imagevariant, imagetransform 도메인
**총 엔드포인트**: 2개 (Query 1개, Command 1개)
**관련 스케줄러**: 3개 (ProcessPending, PollProcessing, RecoverTimeout)
