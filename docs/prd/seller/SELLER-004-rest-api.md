# SELLER-004: Seller REST API Layer 구현

**Epic**: 머스트잇 셀러 크롤링 스케줄러
**Bounded Context**: Seller
**Layer**: REST API (Adapter-In)
**브랜치**: feature/SELLER-004-rest-api
**Jira URL**: (sync-to-jira 후 추가)

---

## 📝 목적

Seller 바운더리 컨텍스트의 REST API 엔드포인트를 구현합니다.

**핵심 기능**:
- 셀러 등록/조회/상태 변경 API
- Request/Response DTO 정의
- Validation 및 Exception Handling

---

## 🎯 요구사항

### API 엔드포인트

#### 셀러 등록

- [ ] **Endpoint**: `POST /api/v1/sellers`
- [ ] **Request**: `RegisterSellerApiRequest`  ⬅️ **API DTO 네이밍 규칙**
  ```java
  public record RegisterSellerApiRequest(
      @NotBlank String mustItSellerId,
      @NotBlank String sellerName
  ) {}
  ```
- [ ] **Response**: `SellerApiResponse` (201 Created)  ⬅️ **API DTO 네이밍 규칙**
- [ ] **비즈니스 로직**:
  - RegisterSellerUseCase 호출

#### 셀러 상태 변경

- [ ] **Endpoint**: `PATCH /api/v1/sellers/{sellerId}/status`
- [ ] **Request**: `ChangeSellerStatusApiRequest`
  ```java
  public record ChangeSellerStatusApiRequest(
      @NotNull SellerStatus targetStatus
  ) {}
  ```
- [ ] **Response**: `SellerApiResponse` (200 OK)
- [ ] **비즈니스 로직**:
  - ChangeSellerStatusUseCase 호출

#### 셀러 조회

- [ ] **Endpoint**: `GET /api/v1/sellers/{sellerId}`
- [ ] **Response**: `SellerDetailApiResponse` (200 OK)
  ```java
  public record SellerDetailApiResponse(
      Long sellerId,
      String mustItSellerId,
      String sellerName,
      SellerStatus status,
      Integer activeSchedulerCount,
      Integer totalSchedulerCount,
      LocalDateTime createdAt,
      LocalDateTime updatedAt
  ) {}
  ```
- [ ] **비즈니스 로직**:
  - GetSellerUseCase 호출

#### 셀러 목록 조회

- [ ] **Endpoint**: `GET /api/v1/sellers`
- [ ] **Query Parameters**:
  - `status`: SellerStatus (Optional)
  - `page`: Integer (기본값 0)
  - `size`: Integer (기본값 10)
- [ ] **Response**: `PageApiResponse<SellerSummaryApiResponse>` (200 OK)
  ```java
  public record SellerSummaryApiResponse(
      Long sellerId,
      String mustItSellerId,
      String sellerName,
      SellerStatus status,
      Integer totalSchedulerCount
  ) {}
  ```
- [ ] **비즈니스 로직**:
  - ListSellersUseCase 호출

### Request/Response DTO

- [ ] **RegisterSellerApiRequest**
- [ ] **ChangeSellerStatusApiRequest**
- [ ] **SellerApiResponse**
- [ ] **SellerDetailApiResponse**
- [ ] **SellerSummaryApiResponse**
- [ ] **PageApiResponse<T>** (공통)

### Validation

#### Bean Validation

- [ ] `@NotBlank` - mustItSellerId, sellerName
- [ ] `@NotNull` - targetStatus

#### Custom Validator

- [ ] **SellerIdValidator** (Optional)
  - sellerId > 0 검증

### Exception Handling

#### Global Exception Handler (RFC 7807 준수)

⚠️ **RFC 7807 Problem Details 표준 준수 필수**

- [ ] **DuplicateMustItSellerIdException** → 409 Conflict
  ```json
  {
    "type": "https://api.example.com/problems/duplicate-mustit-seller-id",
    "title": "Duplicate MustIt Seller ID",
    "status": 409,
    "detail": "이미 등록된 머스트잇 셀러 ID입니다.",
    "instance": "/api/v1/sellers",
    "timestamp": "2025-01-18T12:34:56Z"
  }
  ```

- [ ] **DuplicateSellerNameException** → 409 Conflict
  ```json
  {
    "type": "https://api.example.com/problems/duplicate-seller-name",
    "title": "Duplicate Seller Name",
    "status": 409,
    "detail": "이미 등록된 셀러 이름입니다.",
    "instance": "/api/v1/sellers",
    "timestamp": "2025-01-18T12:34:56Z"
  }
  ```

- [ ] **SellerHasActiveSchedulersException** → 400 Bad Request
  ```json
  {
    "type": "https://api.example.com/problems/seller-has-active-schedulers",
    "title": "Seller Has Active Schedulers",
    "status": 400,
    "detail": "활성 상태의 스케줄러가 존재하여 셀러를 비활성화할 수 없습니다.",
    "instance": "/api/v1/sellers/1/status",
    "timestamp": "2025-01-18T12:34:56Z"
  }
  ```

- [ ] **SellerNotFoundException** → 404 Not Found
  ```json
  {
    "type": "https://api.example.com/problems/seller-not-found",
    "title": "Seller Not Found",
    "status": 404,
    "detail": "존재하지 않는 셀러입니다.",
    "instance": "/api/v1/sellers/999",
    "timestamp": "2025-01-18T12:34:56Z"
  }
  ```

- [ ] **MethodArgumentNotValidException** → 400 Bad Request
  ```json
  {
    "type": "https://api.example.com/problems/validation-failed",
    "title": "Validation Failed",
    "status": 400,
    "detail": "입력값 검증에 실패했습니다.",
    "instance": "/api/v1/sellers",
    "errors": [
      {
        "field": "mustItSellerId",
        "message": "머스트잇 셀러 ID는 필수입니다."
      }
    ],
    "timestamp": "2025-01-18T12:34:56Z"
  }
  ```

### Controller 구현

#### CQRS Controller 분리 (필수!)

**SellerCommandController**
- [ ] **Endpoint 구현** (상태 변경):
  - `POST /api/v1/sellers`
  - `PATCH /api/v1/sellers/{sellerId}/status`

- [ ] **UseCase 직접 의존** (5-10개 의존성은 정상):
  - `RegisterSellerUseCase`
  - `ChangeSellerStatusUseCase`

**SellerQueryController**
- [ ] **Endpoint 구현** (조회):
  - `GET /api/v1/sellers/{sellerId}`
  - `GET /api/v1/sellers`

- [ ] **UseCase 직접 의존**:
  - `GetSellerUseCase`
  - `ListSellersUseCase`

#### Mapper 구현 (DI 패턴)

**SellerApiMapper**
- [ ] **@Component Bean으로 DI** (Static 메서드 금지)
  - Request DTO → Application Command/Query DTO 변환
  - Application Response DTO → API Response DTO 변환
  - 의존성 주입 가능 (MessageSource 등)

- [ ] **예시**:
  ```java
  @Component
  public class SellerApiMapper {
      public RegisterSellerCommand toCommand(RegisterSellerApiRequest request) {
          return new RegisterSellerCommand(
              request.mustItSellerId(),
              request.sellerName()
          );
      }

      public SellerApiResponse toResponse(SellerResponse response) {
          return new SellerApiResponse(
              response.sellerId(),
              response.mustItSellerId(),
              response.sellerName(),
              response.status(),
              response.createdAt()
          );
      }
  }
  ```

---

## ⚠️ 제약사항

### Zero-Tolerance 규칙

- [ ] **RESTful 설계 원칙**
  - HTTP Method 적절히 사용 (POST, GET, PATCH)
  - HTTP Status Code 적절히 사용 (200, 201, 400, 404, 409)
  - URI 설계 (복수형 사용: `/sellers`)

- [ ] **RFC 7807 Problem Details 준수 (필수!)**
  - 모든 예외는 RFC 7807 표준 형식으로 반환
  - `type`, `title`, `status`, `detail`, `instance`, `timestamp` 필드 포함
  - Content-Type: `application/problem+json`

- [ ] **Bean Validation 필수**
  - 모든 Request DTO에 Validation 적용

- [ ] **TestRestTemplate 사용 (Integration Test)**
  - MockMvc 금지 (헥사고날 아키텍처 위반)

- [ ] **Mapper DI 패턴 (필수!)**
  - Mapper는 `@Component` Bean으로 DI
  - Static 메서드 금지
  - 의존성 주입 가능 (MessageSource, Validator 등)

- [ ] **CQRS Controller 분리 (필수!)**
  - Command Controller: 상태 변경 (POST, PUT, PATCH, DELETE)
  - Query Controller: 조회 (GET)
  - 의존성 관리 용이, 책임 명확화

### 테스트 규칙

- [ ] **Integration Test 필수**
  - TestRestTemplate 사용
  - 실제 HTTP 요청/응답 검증

- [ ] **성공 케이스 테스트**
  - 201 Created (셀러 등록)
  - 200 OK (셀러 조회, 상태 변경)

- [ ] **실패 케이스 테스트**
  - 400 Bad Request (Validation 실패)
  - 404 Not Found (셀러 없음)
  - 409 Conflict (중복)

- [ ] **Validation 테스트**
  - `@NotBlank` 검증
  - `@NotNull` 검증

- [ ] **Exception Handling 테스트**
  - GlobalExceptionHandler 검증

- [ ] **ArchUnit 테스트**
  - Controller는 Application Layer만 의존
  - Domain Layer 직접 의존 금지

- [ ] **테스트 커버리지 > 80%**

---

## ✅ 완료 조건

- [ ] Controller 구현 완료
  - SellerApiController

- [ ] Request/Response DTO 구현 완료

- [ ] Global Exception Handler 구현 완료

- [ ] REST API Integration Test 완료
  - 성공 케이스 (201, 200)
  - 실패 케이스 (400, 404, 409)
  - Validation 테스트
  - Exception Handling 테스트

- [ ] ArchUnit 테스트 완료
  - REST API Layer 의존성 검증

- [ ] Zero-Tolerance 규칙 준수 확인
  - RESTful 설계 원칙
  - 일관된 Error Response 형식
  - Bean Validation 필수
  - TestRestTemplate 사용

- [ ] API 문서화 (Spring REST Docs)
  - 각 API 엔드포인트 문서화

- [ ] 코드 리뷰 승인

- [ ] PR 머지 완료

---

## 🔗 관련 문서

- **PRD**: `/Users/sangwon-ryu/crawlinghub/docs/prd/mustit-seller-crawler-scheduler.md`
- **Plan**: `docs/prd/seller/plans/SELLER-004-rest-api-plan.md` (create-plan 후 생성)
- **Jira**: (sync-to-jira 후 추가)
- **코딩 규칙**: `docs/coding_convention/01-adapter-in-layer/rest-api/`
- **선행 Task**: SELLER-001, SELLER-002, SELLER-003

---

## 📋 다음 단계

1. `/create-plan SELLER-004` - TDD Plan 생성
2. `/kb/rest-api/go` - REST API Layer TDD 시작
