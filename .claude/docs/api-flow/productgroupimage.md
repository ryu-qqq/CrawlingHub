# ProductGroupImage Domain API Flow Analysis

ProductGroupImage 도메인의 전체 API 호출 흐름 분석 문서입니다.

---

## 엔드포인트 목록

| HTTP Method | Path | 설명 | Controller Method |
|-------------|------|------|-------------------|
| GET | `/api/v1/market/product-groups/{productGroupId}/images/upload-status` | 이미지 업로드 상태 조회 | `getUploadStatus()` |
| PUT | `/api/v1/market/product-groups/{productGroupId}/images` | 상품 그룹 이미지 수정 (전체 교체) | `updateImages()` |

---

## 1. GET /product-groups/{productGroupId}/images/upload-status - 이미지 업로드 상태 조회

### 호출 흐름 다이어그램

```
[Adapter-In]
ProductGroupImageQueryController.getUploadStatus(productGroupId)
  ├─ GetProductGroupImageUploadStatusUseCase.execute(productGroupId)   [Port Interface]
  └─ ProductGroupImageQueryApiMapper.toResponse(result)               [Response 변환]
      └─ ProductGroupImageUploadStatusApiResponse.from(result)

[Application]
GetProductGroupImageUploadStatusService.execute(productGroupId)       [UseCase 구현]
  └─ ProductGroupImageCompositeReadManager.getImageUploadStatus(productGroupId)
      └─ ProductGroupImageCompositeQueryPort.findImageUploadStatus()   [Port]
         @Transactional(readOnly = true)

[Adapter-Out - Composite]
ProductGroupImageCompositeQueryAdapter                                 [Port 구현]
  └─ ProductGroupImageCompositeQueryDslRepository.findByProductGroupId(productGroupId)
      ├─ fetchImages(): SELECT FROM product_group_images WHERE product_group_id = ?
      └─ fetchOutboxes(): SELECT FROM image_upload_outboxes WHERE source_id IN (...)
  └─ ProductGroupImageCompositeMapper.toResult(compositeDto)          [Result 변환]
      └─ 이미지별 아웃박스 상태 매핑 + 집계 (completed/pending/processing/failed)

[Database]
- product_group_images (이미지 기본정보)
- image_upload_outboxes (업로드 상태 / 크로스 도메인 JOIN)
```

### Layer별 상세

#### Adapter-In Layer

- **Controller**: `ProductGroupImageQueryController`
  - 위치: `adapter-in/rest-api/.../productgroupimage/controller/`
  - Method: `getUploadStatus(@PathVariable Long productGroupId)`
  - HTTP: `GET /api/v1/market/product-groups/{productGroupId}/images/upload-status`
  - Response: `ResponseEntity<ApiResponse<ProductGroupImageUploadStatusApiResponse>>`
  - HTTP Status: 200 OK
  - 권한: `@PreAuthorize("@access.isSellerOwnerOr(#productGroupId, 'product-group:read')")`
  - `@RequirePermission(value = "product-group:read")`

- **Request DTO**: 없음 (Path Variable만 사용)

- **Response DTO**: `ProductGroupImageUploadStatusApiResponse`
  ```java
  record ProductGroupImageUploadStatusApiResponse(
      Long productGroupId,
      int totalCount,
      int completedCount,
      int pendingCount,
      int processingCount,
      int failedCount,
      List<ImageUploadDetailResponse> images
  )

  record ImageUploadDetailResponse(
      Long imageId,
      String imageType,
      String originUrl,
      String uploadedUrl,
      String outboxStatus,   // COMPLETED | PENDING | PROCESSING | FAILED
      int retryCount,
      String errorMessage
  )
  ```
  - 정적 팩토리 메서드 `from(ProductGroupImageUploadStatusResult)` 내부에서 변환

- **ApiMapper**: `ProductGroupImageQueryApiMapper`
  - `toResponse(ProductGroupImageUploadStatusResult)` → `ProductGroupImageUploadStatusApiResponse`
  - 내부적으로 `ProductGroupImageUploadStatusApiResponse.from(result)` 위임

#### Application Layer

- **UseCase Interface**: `GetProductGroupImageUploadStatusUseCase`
  - `execute(Long productGroupId)` → `ProductGroupImageUploadStatusResult`

- **Service 구현**: `GetProductGroupImageUploadStatusService`
  - `@Service`
  - Manager에게 조회 완전 위임

- **Manager**: `ProductGroupImageCompositeReadManager`
  - `getImageUploadStatus(Long productGroupId)` → `ProductGroupImageUploadStatusResult`
  - `@Transactional(readOnly = true)` 트랜잭션 경계

- **Result DTO**: `ProductGroupImageUploadStatusResult`
  ```java
  record ProductGroupImageUploadStatusResult(
      Long productGroupId,
      int totalCount,
      int completedCount,
      int pendingCount,
      int processingCount,
      int failedCount,
      List<ImageUploadDetail> images
  )

  record ImageUploadDetail(
      Long imageId,
      String imageType,
      String originUrl,
      String uploadedUrl,
      String outboxStatus,
      int retryCount,
      String errorMessage
  )
  ```

- **Port**: `ProductGroupImageCompositeQueryPort`
  - `findImageUploadStatus(Long productGroupId)` → `ProductGroupImageUploadStatusResult`
  - 크로스 도메인 Composite 조회 전용 Port (product_group_images + image_upload_outboxes JOIN)

#### Domain Layer

- **관련 Aggregate**: `ProductGroupImage`
  - 이 흐름에서는 직접 사용하지 않음
  - Composite 조회는 Persistence DTO 프로젝션으로 직접 Result 반환

#### Adapter-Out Layer

- **Adapter**: `ProductGroupImageCompositeQueryAdapter`
  - `implements ProductGroupImageCompositeQueryPort`
  - `@Component`
  - Repository 호출 후 Mapper에 변환 위임

- **Repository**: `ProductGroupImageCompositeQueryDslRepository`
  - 2단계 쿼리 실행 (이미지 조회 → 아웃박스 조회)
  - QueryDSL Projections.constructor() 사용

- **Mapper**: `ProductGroupImageCompositeMapper`
  - `toResult(ProductGroupImageCompositeDto)` → `ProductGroupImageUploadStatusResult`
  - 이미지 ID를 키로 아웃박스 Map 구성 후 이미지별 상태 매핑
  - 상태별 카운트 집계 (completed / pending / processing / failed)

- **Persistence DTO**:
  - `ProductGroupImageCompositeDto`: 이미지 목록 + 아웃박스 목록 묶음
  - `ImageProjectionDto`: `imageId, imageType, originUrl, uploadedUrl`
  - `ImageOutboxProjectionDto`: `sourceId, status, retryCount, errorMessage`

- **Database Query**:
  ```sql
  -- 1단계: 이미지 조회
  SELECT pgi.id, pgi.image_type, pgi.origin_url, pgi.uploaded_url
  FROM product_group_images pgi
  WHERE pgi.product_group_id = ?
    AND pgi.deleted = false

  -- 2단계: 아웃박스 조회 (이미지 ID 목록 IN)
  SELECT iuo.source_id, iuo.status, iuo.retry_count, iuo.error_message
  FROM image_upload_outboxes iuo
  WHERE iuo.source_id IN (?, ?, ...)
    AND iuo.source_type = 'PRODUCT_GROUP_IMAGE'
  ```

---

## 2. PUT /product-groups/{productGroupId}/images - 상품 그룹 이미지 수정

### 호출 흐름 다이어그램

```
[Adapter-In]
ProductGroupImageCommandController.updateImages(productGroupId, request)
  ├─ ProductGroupImageCommandApiMapper.toCommand(productGroupId, request)  [Command 변환]
  │   └─ UpdateProductGroupImagesCommand(productGroupId, List<ImageCommand>)
  └─ UpdateProductGroupImagesUseCase.execute(command)                      [Port Interface]

[Application]
UpdateProductGroupImagesService.execute(command)                           [UseCase 구현]
  └─ ImageCommandCoordinator.update(command)                               [Coordinator]
      @Transactional
      ├─ ProductGroupImageFactory.createUpdateData(command)                [UpdateData 생성]
      │   └─ ProductGroupImages.of(newImages) → 검증(THUMBNAIL 1개) + 정렬
      │   └─ ProductGroupImageUpdateData.of(newImages, now)
      ├─ ProductGroupImageReadManager.getByProductGroupId(pgId)            [기존 이미지 조회]
      │   └─ ProductGroupImageQueryPort.findByProductGroupId()             [Port]
      │   └─ ProductGroupImages.reconstitute(images)
      ├─ existing.update(updateData)                                       [Domain 비교 로직]
      │   ├─ originUrl + imageType 키로 추가/삭제/유지 판단
      │   ├─ removed 이미지 → image.delete(occurredAt)  [soft delete]
      │   ├─ retained 이미지 → image.updateSortOrder()
      │   └─ ProductGroupImageDiff.of(added, removed, retained, occurredAt)
      └─ ImageCommandCoordinator.update(diff)                              [diff 저장]
          ├─ ProductGroupImageCommandManager.persistAll(diff.removed())    [soft delete 반영]
          │   └─ ProductGroupImageCommandPort.persist()                    [Port]
          ├─ ProductGroupImageCommandManager.persistAll(diff.added())      [신규 이미지 저장]
          │   └─ ProductGroupImageCommandPort.persist()                    [Port]
          └─ [added 있을 경우] ImageUploadOutboxCommandManager.persistAll(outboxes) [아웃박스 생성]
              └─ ImageUploadOutboxCommandPort.persist()                    [Port]

[Adapter-Out]
ProductGroupImageQueryAdapter.findByProductGroupId()                       [Port 구현 - 조회]
  └─ ProductGroupImageQueryDslRepository.findByProductGroupId(productGroupId)
      └─ QueryDSL: WHERE product_group_id = ? AND deleted = false ORDER BY sort_order ASC
  └─ ProductGroupJpaEntityMapper.toImageDomain(entity)                    [Entity → Domain]

ProductGroupImageCommandAdapter.persist(image)                             [Port 구현 - 저장]
  └─ ProductGroupJpaEntityMapper.toImageEntity(image)                     [Domain → Entity]
  └─ ProductGroupImageJpaRepository.save(entity)

ImageUploadOutboxCommandAdapter.persist(outbox)                            [Port 구현 - 아웃박스]
  └─ ImageUploadOutboxJpaEntityMapper.toEntity(outbox)
  └─ ImageUploadOutboxJpaRepository.save(entity)

[Database]
- SELECT FROM product_group_images           (기존 이미지 조회)
- UPDATE product_group_images (deleted=true) (removed 이미지 soft delete)
- INSERT INTO product_group_images           (added 신규 이미지)
- INSERT INTO image_upload_outboxes          (신규 이미지 업로드 아웃박스)
```

### Layer별 상세

#### Adapter-In Layer

- **Controller**: `ProductGroupImageCommandController`
  - 위치: `adapter-in/rest-api/.../productgroupimage/controller/`
  - Method: `updateImages(@PathVariable Long productGroupId, @Valid @RequestBody UpdateProductGroupImagesApiRequest)`
  - HTTP: `PUT /api/v1/market/product-groups/{productGroupId}/images`
  - Response: `ResponseEntity<Void>` (204 No Content)
  - 권한: `@PreAuthorize("@access.isSellerOwnerOr(#productGroupId, 'product-group:write')")`
  - `@RequirePermission(value = "product-group:write")`

- **Request DTO**: `UpdateProductGroupImagesApiRequest`
  ```java
  record UpdateProductGroupImagesApiRequest(
      @NotNull @Valid List<ImageRequest> images   // 이미지 목록 (최소 1개)
  )

  record ImageRequest(
      @NotBlank String imageType,   // 이미지 타입 (THUMBNAIL, DETAIL 등)
      @NotBlank String originUrl,   // 원본 이미지 URL
      int sortOrder                 // 정렬 순서
  )
  ```

- **ApiMapper**: `ProductGroupImageCommandApiMapper`
  - `toCommand(Long productGroupId, UpdateProductGroupImagesApiRequest)` → `UpdateProductGroupImagesCommand`
  - `ImageRequest` → `ImageCommand` 1:1 변환
  - PathVariable `productGroupId`를 Command에 포함

#### Application Layer

- **UseCase Interface**: `UpdateProductGroupImagesUseCase`
  - `execute(UpdateProductGroupImagesCommand)` → `void`

- **Command DTO**: `UpdateProductGroupImagesCommand`
  ```java
  record UpdateProductGroupImagesCommand(
      long productGroupId,
      List<ImageCommand> images
  )

  record ImageCommand(
      String imageType,
      String originUrl,
      int sortOrder
  )
  ```

- **Service 구현**: `UpdateProductGroupImagesService`
  - `@Service`
  - `ImageCommandCoordinator`에 완전 위임

- **Coordinator**: `ImageCommandCoordinator`
  - `@Component`
  - `update(UpdateProductGroupImagesCommand command)` 메서드에 `@Transactional` 경계
  - 처리 순서:
    1. `ProductGroupImageFactory.createUpdateData(command)` → `ProductGroupImageUpdateData` 생성
    2. `ProductGroupImageReadManager.getByProductGroupId()` → 기존 이미지 조회
    3. `existing.update(updateData)` → 도메인 diff 계산
    4. `update(diff)` → removed 저장(soft delete) + added 저장 + 아웃박스 생성

- **Factory**: `ProductGroupImageFactory`
  - `createUpdateData(UpdateProductGroupImagesCommand)` → `ProductGroupImageUpdateData`
    - `ProductGroupImage.forNew()` 로 새 이미지 생성
    - `ProductGroupImages.of()` 로 검증(THUMBNAIL 1개 필수) + 정렬 적용
    - `ProductGroupImageUpdateData.of(newImages, timeProvider.now())`
  - `createProductGroupImageOutboxes(imageIds, images)` → `List<ImageUploadOutbox>`
    - 신규 저장된 이미지 ID별로 `ImageUploadOutbox.forNew()` 생성
    - `ImageSourceType.PRODUCT_GROUP_IMAGE` 고정

- **Manager**: `ProductGroupImageReadManager`
  - `getByProductGroupId(ProductGroupId)` → `ProductGroupImages`
  - `@Transactional(readOnly = true)`
  - `ProductGroupImages.reconstitute(images)` (검증 스킵, 복원용)

- **Manager**: `ProductGroupImageCommandManager`
  - `persistAll(List<ProductGroupImage>)` → `List<Long>` (저장된 ID 목록)
  - `@Transactional`
  - Port에 1건씩 반복 호출

- **Manager**: `ImageUploadOutboxCommandManager`
  - `persistAll(List<ImageUploadOutbox>)` → `void`
  - `@Transactional`
  - Port에 1건씩 반복 호출

- **Port (Query)**: `ProductGroupImageQueryPort`
  - `findByProductGroupId(ProductGroupId)` → `List<ProductGroupImage>`

- **Port (Command)**: `ProductGroupImageCommandPort`
  - `persist(ProductGroupImage)` → `Long`

- **Port (Command)**: `ImageUploadOutboxCommandPort`
  - `persist(ImageUploadOutbox)` → `Long`

#### Domain Layer

- **Aggregate**: `ProductGroupImage`
  - `forNew(productGroupId, originUrl, imageType, sortOrder)`: 신규 이미지 생성
  - `reconstitute(...)`: 영속성 복원
  - `delete(Instant occurredAt)`: soft delete (`DeletionStatus` 갱신)
  - `updateSortOrder(int)`: 정렬 순서 변경
  - `isDeleted()`: 삭제 여부 확인
  - `originUrlValue()` / `uploadedUrlValue()` / `imageTypeName()` 등 값 접근자

- **VO**: `ProductGroupImages` (컬렉션 VO)
  - `of(List<ProductGroupImage>)`: 신규 생성 - 검증(THUMBNAIL 1개) + 정렬 적용
  - `reconstitute(List<ProductGroupImage>)`: 복원 - 검증 스킵
  - `update(ProductGroupImageUpdateData)` → `ProductGroupImageDiff`
    - `originUrl + imageType` 조합을 키로 추가/삭제/유지 판별
    - removed → `image.delete(occurredAt)` 호출
    - retained → `image.updateSortOrder()` 호출

- **VO**: `ProductGroupImageDiff`
  ```java
  record ProductGroupImageDiff(
      List<ProductGroupImage> added,    // 신규 추가 이미지
      List<ProductGroupImage> removed,  // soft delete 처리된 이미지
      List<ProductGroupImage> retained, // 유지 (sortOrder 갱신됨)
      Instant occurredAt
  )
  ```
  - `hasNoChanges()`: 변경 없음 여부

- **VO**: `ProductGroupImageUpdateData`
  - `newImages`: 수정할 새 이미지 목록 (`ProductGroupImages`)
  - `updatedAt`: 수정 시각 (`Instant`)

- **VO**: `ProductGroupImageId`
  - `forNew()`: `value = null` (신규)
  - `of(Long)`: null 불허

- **비즈니스 규칙**:
  - THUMBNAIL 이미지가 정확히 1개여야 함 (위반 시 `ProductGroupNoThumbnailException`)
  - THUMBNAIL이 sortOrder 0, 이후 이미지가 1부터 순서대로 정렬
  - 이미지 비교 키: `originUrl + "::" + imageType`

#### Adapter-Out Layer

**조회 경로**:

- **Adapter**: `ProductGroupImageQueryAdapter`
  - `implements ProductGroupImageQueryPort`
  - `findByProductGroupId(ProductGroupId)`: QueryDslRepository 호출 → Entity → Domain 변환

- **Repository**: `ProductGroupImageQueryDslRepository`
  - `findByProductGroupId(Long productGroupId)` → `List<ProductGroupImageJpaEntity>`
  - QueryDSL: `WHERE product_group_id = ? AND deleted = false ORDER BY sort_order ASC`

- **Mapper**: `ProductGroupJpaEntityMapper`
  - `toImageDomain(ProductGroupImageJpaEntity)` → `ProductGroupImage`
    - `ProductGroupImage.reconstitute()` 호출
    - `DeletionStatus.reconstitute(deleted, deletedAt)` 변환
  - `toImageEntity(ProductGroupImage)` → `ProductGroupImageJpaEntity`
    - `ProductGroupImageJpaEntity.create()` 호출
    - 삭제 상태(`isDeleted()`, `deletionStatus().deletedAt()`) 포함

**저장 경로 (이미지)**:

- **Adapter**: `ProductGroupImageCommandAdapter`
  - `implements ProductGroupImageCommandPort`
  - `persist(ProductGroupImage)`: Entity 변환 후 JpaRepository.save()

- **Repository**: `ProductGroupImageJpaRepository`
  - `extends JpaRepository<ProductGroupImageJpaEntity, Long>`
  - `save(entity)` → INSERT (신규) 또는 UPDATE (기존 soft delete 반영)

**저장 경로 (아웃박스)**:

- **Adapter**: `ImageUploadOutboxCommandAdapter`
  - `implements ImageUploadOutboxCommandPort`
  - `persist(ImageUploadOutbox)`: Entity 변환 후 JpaRepository.save()

- **Repository**: `ImageUploadOutboxJpaRepository`
  - `extends JpaRepository<ImageUploadOutboxJpaEntity, Long>`

- **JPA Entity**: `ImageUploadOutboxJpaEntity`
  - 테이블: `image_upload_outboxes`
  - 주요 컬럼: `source_id`, `source_type(PRODUCT_GROUP_IMAGE)`, `origin_url`, `status(PENDING)`, `retry_count`, `max_retry`, `version(낙관적 잠금)`, `idempotency_key(unique)`

- **JPA Entity**: `ProductGroupImageJpaEntity`
  - 테이블: `product_group_images`
  - 주요 컬럼: `product_group_id`, `origin_url`, `uploaded_url`, `image_type`, `sort_order`, `deleted(boolean)`, `deleted_at`

- **Database Query**:
  ```sql
  -- 기존 이미지 조회
  SELECT *
  FROM product_group_images
  WHERE product_group_id = ?
    AND deleted = false
  ORDER BY sort_order ASC

  -- removed 이미지 soft delete 반영 (JPA dirty checking)
  UPDATE product_group_images
  SET deleted = true, deleted_at = ?
  WHERE id = ?

  -- added 신규 이미지 저장
  INSERT INTO product_group_images (
    product_group_id, origin_url, uploaded_url,
    image_type, sort_order, deleted, deleted_at
  ) VALUES (?, ?, null, ?, ?, false, null)

  -- 신규 이미지 업로드 아웃박스 생성
  INSERT INTO image_upload_outboxes (
    source_id, source_type, origin_url, status,
    retry_count, max_retry, created_at, updated_at,
    processed_at, error_message, version, idempotency_key
  ) VALUES (?, 'PRODUCT_GROUP_IMAGE', ?, 'PENDING', 0, ?, NOW(), NOW(), null, null, 0, ?)
  ```

---

## 공통 패턴 분석

### 1. Hexagonal Architecture 레이어 격리

| 레이어 | DTO/모델 | 책임 |
|--------|----------|------|
| **Adapter-In** | `UpdateProductGroupImagesApiRequest`, `ProductGroupImageUploadStatusApiResponse` | HTTP 계층 관심사 (Validation, 직렬화) |
| **Application** | `UpdateProductGroupImagesCommand`, `ProductGroupImageUploadStatusResult` | 유스케이스 조율, 트랜잭션 경계 |
| **Domain** | `ProductGroupImage`, `ProductGroupImages`, `ProductGroupImageDiff` | 비즈니스 규칙, 불변성, diff 계산 |
| **Adapter-Out** | `ProductGroupImageJpaEntity`, `ImageUploadOutboxJpaEntity`, Projection DTO | 영속화 기술 관심사 (JPA, QueryDSL) |

### 2. CQRS 분리

- **Query**: `ProductGroupImageQueryController` → `ProductGroupImageQueryApiMapper` → `GetProductGroupImageUploadStatusUseCase` → `GetProductGroupImageUploadStatusService`
- **Command**: `ProductGroupImageCommandController` → `ProductGroupImageCommandApiMapper` → `UpdateProductGroupImagesUseCase` → `UpdateProductGroupImagesService`

### 3. 트랜잭션 경계

| 컴포넌트 | @Transactional | 비고 |
|----------|---------------|------|
| Controller | 없음 | 금지 |
| Service | 없음 | Coordinator/Manager에 위임 |
| **ImageCommandCoordinator** | `@Transactional` | Command 트랜잭션 경계 |
| **ProductGroupImageReadManager** | `@Transactional(readOnly = true)` | 조회 트랜잭션 |
| **ProductGroupImageCommandManager** | `@Transactional` | 저장 트랜잭션 |
| **ProductGroupImageCompositeReadManager** | `@Transactional(readOnly = true)` | Composite 조회 트랜잭션 |
| Adapter-Out | 없음 | 금지 |

### 4. 이미지 수정 전략 (full-replace + diff)

```
기존 이미지 조회 (productGroupId 기준)
  ↓
새 이미지 목록으로 diff 계산 (originUrl + imageType 키)
  ├─ removed: soft delete (deleted=true, deleted_at=now)
  ├─ retained: sortOrder 갱신 (dirty checking으로 반영)
  └─ added: INSERT + 업로드 아웃박스 생성
```

단순 DELETE + INSERT가 아닌 diff 기반 처리로:
- 기존 이미지 재사용 시 불필요한 아웃박스 재생성 방지
- soft delete로 이력 보존

### 5. Outbox 패턴 (비동기 이미지 업로드)

```
이미지 저장 (product_group_images)
  └─ 동일 트랜잭션 내 ImageUploadOutbox 생성 (image_upload_outboxes)
      └─ 스케줄러가 PENDING 아웃박스 감지 → S3 업로드 → uploaded_url 갱신
```

- `ImageUploadOutbox.status`: `PENDING` → `PROCESSING` → `COMPLETED` / `FAILED`
- `idempotency_key`: 중복 생성 방지 (unique constraint)
- `version`: 낙관적 잠금 (동시 처리 방지)

### 6. Composite 조회 패턴 (크로스 도메인 JOIN)

```
product_group_images + image_upload_outboxes
  → 2단계 쿼리 (이미지 먼저, 그 다음 아웃박스)
  → ProductGroupImageCompositeMapper에서 메모리 조인 + 집계
```

`ProductGroupImageCompositeQueryPort`는 크로스 도메인 조회 전용 Port로 분리하여 도메인 격리 유지.

### 7. 도메인 비즈니스 규칙

- **THUMBNAIL 필수**: `ProductGroupImages.of()` 시 THUMBNAIL 이미지 정확히 1개 검증
  - 위반 시 `ProductGroupNoThumbnailException`
- **자동 정렬**: THUMBNAIL을 sortOrder 0으로 강제, 이후 순서대로 재부여
- **이미지 비교 키**: `originUrl + "::" + imageType` 조합으로 동일 이미지 식별

### 8. 변환 체인

```
[Query]
PathVariable(productGroupId)
  → GetProductGroupImageUploadStatusUseCase.execute(Long)
  → ProductGroupImageCompositeQueryPort.findImageUploadStatus()
  → Composite DTO (2-query result)
  → ProductGroupImageUploadStatusResult
  → ProductGroupImageUploadStatusApiResponse

[Command]
UpdateProductGroupImagesApiRequest
  → UpdateProductGroupImagesCommand
  → ProductGroupImageUpdateData (Factory)
  → ProductGroupImages (Domain)
  → ProductGroupImageDiff (Domain 비교)
  → ProductGroupImageJpaEntity[] (저장)
  → ImageUploadOutboxJpaEntity[] (아웃박스)
```

---

## 에러 처리

`ProductGroupImageErrorMapper`에서 처리하는 예외:

| 예외 클래스 | HTTP Status | 설명 |
|-------------|-------------|------|
| `ProductGroupImageNotFoundException` | 404 | 상품 그룹 이미지를 찾을 수 없음 |
| `DescriptionImageNotFoundException` | 404 | 설명 이미지를 찾을 수 없음 |
| `ProductGroupNoThumbnailException` | (Domain 예외) | THUMBNAIL 이미지 미존재 또는 복수 존재 |

---

## 주요 설계 결정

### 장점

1. **diff 기반 수정**: 변경된 이미지만 처리하여 불필요한 아웃박스 재생성 방지
2. **Outbox 패턴**: 이미지 저장과 업로드 요청을 동일 트랜잭션으로 묶어 일관성 보장
3. **도메인 비즈니스 규칙 내재화**: THUMBNAIL 검증, 정렬 로직이 `ProductGroupImages` VO에 캡슐화
4. **Composite 조회 분리**: 크로스 도메인 조회는 전용 Port/Adapter로 격리
5. **soft delete**: 이미지 이력 보존으로 업로드 상태 추적 가능

### 트레이드오프

1. **2단계 쿼리**: Composite 조회 시 이미지 → 아웃박스 순서로 2번 쿼리 (단일 JOIN 대비 코드 분리)
2. **메모리 조인**: 아웃박스 상태 매핑을 Java 메모리에서 수행 (DB JOIN 대비 유연성 우선)
3. **`persistAll` 반복 호출**: CommandManager에서 `persist()`를 1건씩 반복 (saveAll 미사용)

---

**분석 일시**: 2026-02-18
**분석 대상**: productgroupimage 도메인
**총 엔드포인트**: 2개 (Query 1개, Command 1개)
