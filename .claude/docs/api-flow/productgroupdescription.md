# ProductGroupDescription Domain API Flow Analysis

ProductGroupDescription 도메인의 전체 API 호출 흐름 분석 문서입니다.

---

## 엔드포인트 목록

| HTTP Method | Path | 설명 | Controller Method |
|-------------|------|------|-------------------|
| GET | `/api/v1/market/product-groups/{productGroupId}/description/publish-status` | 상세설명 발행 상태 조회 | `getPublishStatus()` |
| PUT | `/api/v1/market/product-groups/{productGroupId}/description` | 상세설명 수정 (Upsert) | `updateDescription()` |

---

## 1. GET /product-groups/{productGroupId}/description/publish-status - 상세설명 발행 상태 조회

### 호출 흐름 다이어그램

```
[Adapter-In]
ProductGroupDescriptionQueryController.getPublishStatus(productGroupId)
  ├─ GetDescriptionPublishStatusUseCase.execute(productGroupId)     [Port Interface]
  └─ ProductGroupDescriptionQueryApiMapper.toResponse(result)       [Response 변환]
      └─ DescriptionPublishStatusApiResponse.from(result)           [정적 팩토리 변환]

[Application]
GetDescriptionPublishStatusService.execute(productGroupId)          [UseCase 구현]
  └─ DescriptionCompositeReadManager.getPublishStatus(productGroupId)
      └─ DescriptionCompositeQueryPort.findPublishStatus()          [Port]
          [@Transactional(readOnly = true)]

[Adapter-Out - Composite]
ProductGroupDescriptionCompositeQueryAdapter.findPublishStatus()    [Port 구현]
  └─ ProductGroupDescriptionCompositeQueryDslRepository.findByProductGroupId()
      ├─ Step1: fetchDescription(productGroupId)
      │   └─ QueryDSL: SELECT FROM product_group_descriptions WHERE product_group_id = ?
      ├─ Step2: fetchDescriptionImages(descriptionId)
      │   └─ QueryDSL: SELECT FROM description_images WHERE product_group_description_id = ? AND deleted = false
      └─ Step3: fetchOutboxes(imageIds)
          └─ QueryDSL: SELECT FROM image_upload_outboxes WHERE source_id IN (?) AND source_type = 'DESCRIPTION_IMAGE'
  └─ ProductGroupDescriptionCompositeMapper.toResult(dto)
      └─ -> DescriptionPublishStatusResult

[Database]
- product_group_descriptions (발행 상태, CDN 경로)
- description_images (이미지 원본/업로드 URL)
- image_upload_outboxes (크로스 도메인 JOIN)
```

### Layer별 상세

#### Adapter-In Layer

- **Controller**: `ProductGroupDescriptionQueryController`
  - 위치: `adapter-in/rest-api/.../productgroupdescription/controller/`
  - Method: `getPublishStatus(Long productGroupId)`
  - 어노테이션: `@GetMapping`, `@PreAuthorize("@access.isSellerOwnerOr(#productGroupId, 'product-group:read')")`
  - Response: `ResponseEntity<ApiResponse<DescriptionPublishStatusApiResponse>>`
  - HTTP Status: 200 OK

- **Request**: `@PathVariable Long productGroupId` (Request DTO 없음)

- **Response DTO**: `DescriptionPublishStatusApiResponse`
  ```java
  record DescriptionPublishStatusApiResponse(
      Long productGroupId,
      Long descriptionId,
      String publishStatus,       // PUBLISHED / PENDING / FAILED
      String cdnPath,
      int totalImageCount,
      int completedImageCount,
      int pendingImageCount,
      int failedImageCount,
      List<DescriptionImageUploadDetailResponse> images
  )

  record DescriptionImageUploadDetailResponse(
      Long imageId,
      String originUrl,
      String uploadedUrl,
      String outboxStatus,        // COMPLETED / PENDING / FAILED
      int retryCount,
      String errorMessage
  )
  ```

- **ApiMapper**: `ProductGroupDescriptionQueryApiMapper`
  - `toResponse(DescriptionPublishStatusResult)` → `DescriptionPublishStatusApiResponse`
  - 내부적으로 `DescriptionPublishStatusApiResponse.from(result)` 정적 팩토리 위임

#### Application Layer

- **UseCase Interface**: `GetDescriptionPublishStatusUseCase`
  - `execute(Long productGroupId)` → `DescriptionPublishStatusResult`

- **Service 구현**: `GetDescriptionPublishStatusService`
  - `DescriptionCompositeReadManager`에 단순 위임

- **Manager**: `DescriptionCompositeReadManager`
  - `getPublishStatus(Long productGroupId)` → `DescriptionPublishStatusResult`
  - `@Transactional(readOnly = true)`
  - `DescriptionCompositeQueryPort.findPublishStatus()` 호출

- **Result DTO**: `DescriptionPublishStatusResult`
  ```java
  record DescriptionPublishStatusResult(
      Long productGroupId,
      Long descriptionId,
      String publishStatus,
      String cdnPath,
      int totalImageCount,
      int completedImageCount,
      int pendingImageCount,
      int failedImageCount,
      List<DescriptionImageUploadDetail> images
  )
  ```
  - `empty(Long productGroupId)`: 상세설명 미존재 시 빈 결과 반환 정적 팩토리

#### Domain Layer (Port만)

- **Port**: `DescriptionCompositeQueryPort`
  - `findPublishStatus(Long productGroupId)` → `DescriptionPublishStatusResult`
  - Application DTO를 직접 반환하는 Composite Port (크로스 도메인 조인 전용)

#### Adapter-Out Layer

- **Adapter**: `ProductGroupDescriptionCompositeQueryAdapter`
  - `DescriptionCompositeQueryPort` 구현
  - 상세설명 미존재 시 `DescriptionPublishStatusResult.empty(productGroupId)` 반환

- **Repository**: `ProductGroupDescriptionCompositeQueryDslRepository`
  - 3단계 순차 조회 (단일 복합 JOIN 대신 분리 쿼리)
  - Step1: 상세설명 조회 → Step2: 이미지 조회 → Step3: 아웃박스 조회

- **Composite DTO 체인**:
  ```
  DescriptionCompositeDto {
    productGroupId,
    DescriptionProjectionDto { descriptionId, publishStatus, cdnPath },
    List<DescriptionImageProjectionDto> { imageId, originUrl, uploadedUrl },
    List<DescriptionImageOutboxProjectionDto> { sourceId, status, retryCount, errorMessage }
  }
  ```

- **Mapper**: `ProductGroupDescriptionCompositeMapper`
  - `outboxMap`: imageId → DescriptionImageOutboxProjectionDto 맵 구성
  - 이미지별 아웃박스 상태 JOIN 매핑 (인메모리 조합)
  - COMPLETED / PENDING / FAILED 집계

- **Database Query**:
  ```sql
  -- Step1: 상세설명 조회
  SELECT id AS descriptionId, publish_status, cdn_path
  FROM product_group_descriptions
  WHERE product_group_id = ?

  -- Step2: 이미지 조회
  SELECT id AS imageId, origin_url, uploaded_url
  FROM description_images
  WHERE product_group_description_id = ?
    AND deleted = false

  -- Step3: 아웃박스 조회
  SELECT source_id, status, retry_count, error_message
  FROM image_upload_outboxes
  WHERE source_id IN (?, ?, ...)
    AND source_type = 'DESCRIPTION_IMAGE'
  ```

---

## 2. PUT /product-groups/{productGroupId}/description - 상세설명 수정 (Upsert)

### 호출 흐름 다이어그램

```
[Adapter-In]
ProductGroupDescriptionCommandController.updateDescription(productGroupId, request)
  ├─ ProductGroupDescriptionCommandApiMapper.toCommand(productGroupId, request)
  │   └─ -> UpdateProductGroupDescriptionCommand(productGroupId, content)
  └─ UpdateProductGroupDescriptionUseCase.execute(command)          [Port Interface]

[Application]
UpdateProductGroupDescriptionService.execute(command)               [UseCase 구현]
  ├─ ProductGroupDescriptionCommandFactory.createUpdateData(command)
  │   ├─ DescriptionHtml.of(content)
  │   ├─ content.extractImageUrls()                                 [HTML에서 이미지 URL 추출]
  │   └─ -> DescriptionUpdateData(content, newImages, now)
  ├─ ProductGroupDescriptionReadManager.getByProductGroupId(pgId)
  │   └─ ProductGroupDescriptionQueryPort.findByProductGroupId()    [Port - 기존 데이터 조회]
  │       [@Transactional(readOnly = true)]
  ├─ description.update(updateData)                                  [Domain: 이미지 Diff 계산]
  │   └─ -> DescriptionImageDiff(added, removed, retained)
  └─ DescriptionCommandCoordinator.update(description, diff)        [Component]
      ├─ DescriptionCommandFacade.update(description, diff)         [@Transactional]
      │   ├─ ProductGroupDescriptionCommandManager.persist(description)
      │   │   └─ ProductGroupDescriptionCommandPort.persist()       [Port]
      │   │       └─ ProductGroupDescriptionCommandAdapter
      │   │           └─ ProductGroupDescriptionJpaRepository.save()
      │   ├─ DescriptionImageCommandManager.persistAll(diff.removed())
      │   │   └─ DescriptionImageCommandPort.persist() × N          [soft delete 처리]
      │   │       └─ DescriptionImageCommandAdapter
      │   │           └─ DescriptionImageJpaRepository.save()
      │   └─ DescriptionImageCommandManager.persistAll(diff.added())
      │       └─ DescriptionImageCommandPort.persist() × N          [신규 이미지 저장]
      │           └─ DescriptionImageCommandAdapter
      │               └─ DescriptionImageJpaRepository.save()
      ├─ [신규 이미지가 있을 경우] ImageUploadOutbox 생성
      │   ├─ ProductGroupDescriptionCommandFactory.createDescriptionImageOutboxes()
      │   └─ ImageUploadOutboxCommandManager.persistAll(outboxes)   [아웃박스 등록]
      └─ [모든 이미지 업로드 완료 시] description.markPublishReady()
          └─ DescriptionCommandFacade.persistDescription(description)

[Adapter-Out]
ProductGroupDescriptionCommandAdapter                               [Port 구현]
  ├─ ProductGroupDescriptionJpaEntityMapper.toEntity(description)
  └─ ProductGroupDescriptionJpaRepository.save(entity)

DescriptionImageCommandAdapter                                      [Port 구현]
  ├─ ProductGroupDescriptionJpaEntityMapper.toImageEntity(image)
  └─ DescriptionImageJpaRepository.save(entity)

[Database]
- UPDATE product_group_descriptions (content, publish_status, cdn_path, updated_at)
- INSERT/UPDATE description_images (신규 이미지)
- UPDATE description_images SET deleted = true (삭제된 이미지 soft delete)
- INSERT INTO image_upload_outboxes (신규 이미지 업로드 아웃박스)
```

### Layer별 상세

#### Adapter-In Layer

- **Controller**: `ProductGroupDescriptionCommandController`
  - 위치: `adapter-in/rest-api/.../productgroupdescription/controller/`
  - Method: `updateDescription(Long productGroupId, UpdateProductGroupDescriptionApiRequest request)`
  - 어노테이션: `@PutMapping`, `@Valid`, `@PreAuthorize("@access.isSellerOwnerOr(#productGroupId, 'product-group:write')")`
  - Response: `ResponseEntity<Void>` (204 No Content)

- **Request DTO**: `UpdateProductGroupDescriptionApiRequest`
  ```java
  record UpdateProductGroupDescriptionApiRequest(
      @NotBlank(message = "상세 설명 내용은 필수입니다") String content
  )
  ```

- **ApiMapper**: `ProductGroupDescriptionCommandApiMapper`
  - `toCommand(Long productGroupId, UpdateProductGroupDescriptionApiRequest)` → `UpdateProductGroupDescriptionCommand`
  - 단순 필드 복사 (productGroupId + content)

#### Application Layer

- **UseCase Interface**: `UpdateProductGroupDescriptionUseCase`
  - `execute(UpdateProductGroupDescriptionCommand)` → `void`

- **Service 구현**: `UpdateProductGroupDescriptionService`
  - Factory → ReadManager → Domain → Coordinator 순서로 조율
  - 서비스 자체에는 `@Transactional` 없음 (Facade에 위임)

- **Command DTO**: `UpdateProductGroupDescriptionCommand`
  ```java
  record UpdateProductGroupDescriptionCommand(long productGroupId, String content)
  ```

- **Factory**: `ProductGroupDescriptionCommandFactory`
  - `createUpdateData(UpdateProductGroupDescriptionCommand)`:
    - `DescriptionHtml.of(content)` → HTML VO 생성
    - `content.extractImageUrls()` → HTML 파싱으로 이미지 URL 목록 추출
    - `DescriptionImage.forNew(url, sortOrder)` × N → 신규 이미지 도메인 생성
    - `DescriptionUpdateData.of(content, newImages, now)` 반환
  - `createDescriptionImageOutboxes(imageIds, images, createdAt)`:
    - `ImageUploadOutbox.forNew(imageId, DESCRIPTION_IMAGE, originUrl, createdAt)` × N 생성

- **ReadManager**: `ProductGroupDescriptionReadManager`
  - `getByProductGroupId(ProductGroupId)`:
    - `ProductGroupDescriptionQueryPort.findByProductGroupId()` 호출
    - 미존재 시 `ProductGroupDescriptionNotFoundException` throw
    - `@Transactional(readOnly = true)`

- **Domain 처리**: `ProductGroupDescription.update(DescriptionUpdateData)`
  - content, updatedAt 갱신
  - publishStatus → `PENDING`으로 리셋, cdnPath → null 초기화
  - `computeImageDiff()`: 기존 이미지와 새 이미지를 originUrl 기준 비교
    - `retained`: URL이 유지된 이미지 (sortOrder만 업데이트)
    - `added`: 신규 URL의 이미지
    - `removed`: 삭제된 이미지 (`.delete(updatedAt)` 호출 → soft delete)
  - `DescriptionImageDiff` 반환

- **Coordinator**: `DescriptionCommandCoordinator`
  - `update(ProductGroupDescription, DescriptionImageDiff)`:
    1. `DescriptionCommandFacade.update(description, diff)` → 트랜잭션 내 저장
    2. 신규 이미지가 있으면 `ImageUploadOutbox` 생성 후 `ImageUploadOutboxCommandManager.persistAll()`
    3. 모든 이미지 업로드 완료 시 `description.markPublishReady()` + `DescriptionCommandFacade.persistDescription()`

- **Facade**: `DescriptionCommandFacade`
  - `update(ProductGroupDescription, DescriptionImageDiff)`:
    - `@Transactional`
    - `descriptionCommandManager.persist(description)` → description UPDATE
    - `imageCommandManager.persistAll(diff.removed())` → soft delete 처리
    - `imageCommandManager.persistAll(diff.added())` → 신규 이미지 INSERT
    - `DescriptionPersistResult(descriptionId, newImageIds)` 반환
  - `persistDescription(ProductGroupDescription)`:
    - `@Transactional`
    - description만 단독 저장 (publish 상태 업데이트용)

- **Port (Query)**: `ProductGroupDescriptionQueryPort`
  - `findByProductGroupId(ProductGroupId)` → `Optional<ProductGroupDescription>`
  - `findById(Long)` → `Optional<ProductGroupDescription>`
  - `findByPublishStatus(DescriptionPublishStatus, int limit)` → `List<ProductGroupDescription>`

- **Port (Command)**:
  - `ProductGroupDescriptionCommandPort.persist(ProductGroupDescription)` → `Long`
  - `DescriptionImageCommandPort.persist(DescriptionImage)` → `Long`

#### Domain Layer

- **Aggregate Root**: `ProductGroupDescription`
  - `forNew(ProductGroupId, DescriptionHtml, Instant)`: 신규 생성 (status = PENDING)
  - `reconstitute(...)`: 영속성 복원
  - `update(DescriptionUpdateData)`: 수정 + 이미지 Diff 계산
  - `markPublishReady()`: publishStatus → PUBLISH_READY
  - `publish(CdnPath)`: publishStatus → PUBLISHED, cdnPath 설정
  - `buildPublishableHtml()`: 이미지 URL을 CDN URL로 치환한 HTML 생성
  - `isAllImagesUploaded()`: 모든 이미지 업로드 완료 여부 확인
  - `assignId(ProductGroupDescriptionId)`: 저장 후 ID 할당, 소유 이미지에 propagation

- **Child Entity**: `DescriptionImage`
  - `forNew(ImageUrl, sortOrder)`: 신규 이미지 (uploadedUrl = null)
  - `reconstitute(...)`: 영속성 복원
  - `delete(Instant)`: DeletionStatus soft delete 처리
  - `isUploaded()`: uploadedUrl != null 여부
  - `updateSortOrder(int)`: 정렬 순서 변경

- **VO**:
  - `DescriptionHtml`: HTML 콘텐츠 + `extractImageUrls()` (HTML 파싱), `replaceImageUrls(Map)` (URL 치환)
  - `DescriptionPublishStatus`: PENDING / PUBLISH_READY / PUBLISHED
  - `DescriptionImageDiff`: added / removed / retained 이미지 목록
  - `DescriptionUpdateData`: content + newImages + updatedAt
  - `ImageUrl`: 이미지 URL VO
  - `CdnPath`: CDN 경로 VO

#### Adapter-Out Layer

- **Query Adapter**: `ProductGroupDescriptionQueryAdapter`
  - `ProductGroupDescriptionQueryPort` 구현
  - `findByProductGroupId(productGroupId)`:
    1. `queryDslRepository.findByProductGroupId(productGroupId.value())`
    2. 이미지 별도 조회: `queryDslRepository.findImagesByDescriptionId(entity.getId())`
    3. `mapper.toDomain(entity, imageEntities)` → Domain 복원

- **Command Adapter**: `ProductGroupDescriptionCommandAdapter`
  - `ProductGroupDescriptionCommandPort` 구현
  - `persist(ProductGroupDescription)`:
    1. `mapper.toEntity(description)` → Entity 변환
    2. `repository.save(entity)` → INSERT or UPDATE (ID 유무로 판단)
    3. `saved.getId()` 반환

- **Image Command Adapter**: `DescriptionImageCommandAdapter`
  - `DescriptionImageCommandPort` 구현
  - `persist(DescriptionImage)`:
    1. `mapper.toImageEntity(image)` → Entity 변환
    2. `repository.save(entity).getId()` 반환

- **QueryDSL Repository**: `ProductGroupDescriptionQueryDslRepository`
  - `findByProductGroupId(Long)`: `WHERE product_group_id = ?`
  - `findById(Long)`: `WHERE id = ?`
  - `findByPublishStatus(String, int limit)`: `WHERE publish_status = ? LIMIT ?`
  - `findImagesByDescriptionId(Long)`: `WHERE product_group_description_id = ? AND deleted = false ORDER BY sort_order ASC`

- **JPA Repository**:
  - `ProductGroupDescriptionJpaRepository extends JpaRepository<..., Long>` (save/saveAll만)
  - `DescriptionImageJpaRepository extends JpaRepository<..., Long>` + `deleteByProductGroupDescriptionId(Long)`

- **JPA Entity**:

  `ProductGroupDescriptionJpaEntity` (`@Table(name = "product_group_descriptions")`)
  ```
  id                  BIGINT PK AUTO_INCREMENT
  product_group_id    BIGINT NOT NULL (FK)
  content             TEXT
  cdn_path            VARCHAR(500)
  publish_status      VARCHAR(30) NOT NULL
  created_at          DATETIME (BaseAuditEntity)
  updated_at          DATETIME (BaseAuditEntity)
  ```

  `DescriptionImageJpaEntity` (`@Table(name = "description_images")`)
  ```
  id                              BIGINT PK AUTO_INCREMENT
  product_group_description_id    BIGINT NOT NULL (FK)
  origin_url                      VARCHAR(500) NOT NULL
  uploaded_url                    VARCHAR(500)
  sort_order                      INT NOT NULL
  deleted                         BOOLEAN NOT NULL
  deleted_at                      DATETIME
  ```

- **Entity Mapper**: `ProductGroupDescriptionJpaEntityMapper`
  - `toEntity(ProductGroupDescription)` → `ProductGroupDescriptionJpaEntity.create(...)`
  - `toImageEntity(DescriptionImage)` → `DescriptionImageJpaEntity.create(...)`
  - `toDomain(entity, imageEntities)` → `ProductGroupDescription.reconstitute(...)`
  - `toImageDomain(entity)` → `DescriptionImage.reconstitute(...)`

- **Database Query**:
  ```sql
  -- 기존 상세설명 조회 (수정 전 로드)
  SELECT *
  FROM product_group_descriptions
  WHERE product_group_id = ?

  -- 이미지 조회 (description에 포함)
  SELECT *
  FROM description_images
  WHERE product_group_description_id = ?
    AND deleted = false
  ORDER BY sort_order ASC

  -- 상세설명 저장 (INSERT or UPDATE)
  INSERT INTO product_group_descriptions
    (product_group_id, content, cdn_path, publish_status, created_at, updated_at)
  VALUES (?, ?, ?, 'PENDING', ?, ?)
  ON DUPLICATE KEY UPDATE
    content = ?, cdn_path = NULL, publish_status = 'PENDING', updated_at = ?

  -- 신규 이미지 저장
  INSERT INTO description_images
    (product_group_description_id, origin_url, uploaded_url, sort_order, deleted, deleted_at)
  VALUES (?, ?, NULL, ?, false, NULL)

  -- 삭제 이미지 soft delete
  UPDATE description_images
  SET deleted = true, deleted_at = ?
  WHERE id = ?

  -- 아웃박스 등록
  INSERT INTO image_upload_outboxes
    (source_id, source_type, origin_url, status, retry_count, created_at)
  VALUES (?, 'DESCRIPTION_IMAGE', ?, 'PENDING', 0, ?)
  ```

---

## 공통 패턴 분석

### 1. Hexagonal Architecture 레이어 격리

| 레이어 | DTO/모델 | 책임 |
|--------|----------|------|
| **Adapter-In** | ApiRequest / ApiResponse | HTTP 계층 (Validation, 직렬화) |
| **Application** | Command / Result | 유스케이스 조율, 트랜잭션 경계 |
| **Domain** | Aggregate / VO / DescriptionImageDiff | 비즈니스 규칙, 이미지 Diff 계산 |
| **Adapter-Out** | Entity / ProjectionDto | 영속화 기술 관심사 (JPA, QueryDSL) |

### 2. CQRS 분리

- **Query**: `ProductGroupDescriptionQueryController` → `ProductGroupDescriptionQueryApiMapper` → `GetDescriptionPublishStatusUseCase` → `GetDescriptionPublishStatusService`
- **Command**: `ProductGroupDescriptionCommandController` → `ProductGroupDescriptionCommandApiMapper` → `UpdateProductGroupDescriptionUseCase` → `UpdateProductGroupDescriptionService`

### 3. 트랜잭션 경계

| 컴포넌트 | @Transactional 위치 | 비고 |
|----------|---------------------|------|
| Controller | 없음 | Adapter-In은 트랜잭션 금지 |
| Service | 없음 | Coordinator/Facade에 위임 |
| **DescriptionCommandFacade** | `@Transactional` | description + image 원자적 저장 |
| **DescriptionCommandCoordinator** | `@Transactional` | 상위 조율 (outbox 포함) |
| **Manager (Read)** | `@Transactional(readOnly = true)` | 조회 전용 |
| **Manager (Command)** | `@Transactional` | 개별 영속화 |
| Adapter | 없음 | Adapter-Out은 트랜잭션 금지 |

### 4. Coordinator + Facade 조합 패턴

```
DescriptionCommandCoordinator (트랜잭션 + 아웃박스 조율)
  └─ DescriptionCommandFacade (description + image 원자적 저장)
      ├─ ProductGroupDescriptionCommandManager (description 영속화)
      └─ DescriptionImageCommandManager (image 영속화)
```
- **Facade**: description + image의 원자적 저장만 담당
- **Coordinator**: Facade 호출 + 이미지 아웃박스 생성 + 발행 준비 상태 전환 조율
- 외부 HTTP 호출(`DescriptionPublishCoordinator`)은 `@Transactional` 미적용 (외부 호출 격리)

### 5. Composite Query 패턴 (크로스 도메인 조인)

발행 상태 조회는 3개 테이블을 조인해야 하므로 별도 Composite 레이어 사용:

```
DescriptionCompositeQueryPort           ← Application 레벨 Port
  └─ ProductGroupDescriptionCompositeQueryAdapter  ← composite/ 패키지
      └─ ProductGroupDescriptionCompositeQueryDslRepository
          ├─ product_group_descriptions
          ├─ description_images
          └─ image_upload_outboxes (크로스 도메인)
```

일반 단건 조회는 `ProductGroupDescriptionQueryAdapter`가 분리 쿼리 2단계로 처리:
1. description 조회
2. images 조회 (descriptionId 기준)

### 6. 이미지 비동기 업로드 Outbox 패턴

```
updateDescription 호출
  → DescriptionImage 저장 (origin_url만 있음)
  → ImageUploadOutbox 등록 (status = PENDING)
  → 스케줄러: Outbox 처리 → CDN 업로드 → uploaded_url 갱신
  → 모든 이미지 완료 시 description.markPublishReady()
  → 스케줄러: PUBLISH_READY → CDN HTML 업로드 → status = PUBLISHED
```

### 7. Upsert 동작 (updateDescription)

`updateDescription`은 Create/Update를 구분하지 않는 Upsert 방식:
- 기존 상세설명 없음: `ProductGroupDescriptionNotFoundException` 발생 → Service 레벨에서 처리 불가
- 현재 구현: `getByProductGroupId`에서 예외 발생 시 404 응답
- 신규 등록은 `RegisterProductGroupDescriptionUseCase` (내부용) 또는 ProductGroup 등록 흐름에서 처리

### 8. 변환 체인

```
[Query]
productGroupId → DescriptionCompositeQueryPort → DescriptionCompositeDto
  → ProductGroupDescriptionCompositeMapper → DescriptionPublishStatusResult
  → DescriptionPublishStatusApiResponse.from() → ApiResponse<...>

[Command]
UpdateProductGroupDescriptionApiRequest
  → UpdateProductGroupDescriptionCommand (Mapper)
  → DescriptionUpdateData (Factory)
  → ProductGroupDescription.update() → DescriptionImageDiff (Domain)
  → DescriptionCommandFacade.update() (Facade)
  → ProductGroupDescriptionJpaEntity + DescriptionImageJpaEntity (EntityMapper)
  → DB
```

---

## 에러 처리

`ProductGroupDescriptionErrorMapper`가 다음 예외를 HTTP 응답으로 매핑합니다:

| 예외 | HTTP Status | 발생 조건 |
|------|-------------|-----------|
| `ProductGroupDescriptionNotFoundException` | 404 NOT FOUND | getByProductGroupId() - 상세설명 미존재 |

---

## 권한 체계

| 엔드포인트 | PreAuthorize | RequirePermission |
|-----------|--------------|-------------------|
| getPublishStatus | `isSellerOwnerOr(#productGroupId, 'product-group:read')` | `product-group:read` |
| updateDescription | `isSellerOwnerOr(#productGroupId, 'product-group:write')` | `product-group:write` |

---

## 설계 특이사항

1. **Composite Port의 Application DTO 직접 반환**: `DescriptionCompositeQueryPort`는 Domain 객체가 아닌 Application DTO(`DescriptionPublishStatusResult`)를 직접 반환. 3개 테이블 크로스 도메인 조인 결과를 Domain으로 표현하기 어렵기 때문
2. **분리 쿼리 전략**: Composite Repository는 단일 복합 JOIN 대신 3단계 순차 쿼리 사용. 각 테이블이 다른 도메인에 속하고 1:N 관계로 복잡도 제어
3. **인메모리 조합**: `ProductGroupDescriptionCompositeMapper`에서 imageId → outbox 맵 구성 후 이미지별 아웃박스 상태 매핑 (인메모리 JOIN)
4. **이중 트랜잭션 중첩**: `DescriptionCommandCoordinator(@Transactional)` → `DescriptionCommandFacade(@Transactional)` 중첩 구조. Spring의 기본 전파 방식(`REQUIRED`)으로 동일 트랜잭션 공유
5. **publishStatus 리셋**: 수정 시 항상 PENDING으로 리셋하여 재발행 강제. CDN 업로드는 비동기 스케줄러가 처리
6. **soft delete**: `DescriptionImage`는 `deleted = true` + `deleted_at` 방식의 soft delete 적용
7. **이미지 URL 파싱**: `DescriptionHtml.extractImageUrls()`로 HTML 콘텐츠에서 이미지 URL을 추출하는 도메인 로직 포함

---

**분석 일시**: 2026-02-18
**분석 대상**: ProductGroupDescription Domain (Query 1개, Command 1개)
**총 엔드포인트**: 2개
