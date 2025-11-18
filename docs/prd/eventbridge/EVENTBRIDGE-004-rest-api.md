# EVENTBRIDGE-004: EventBridge REST API Layer 구현

**Epic**: 머스트잇 셀러 크롤링 스케줄러
**Bounded Context**: EventBridge (Crawling Scheduler)
**Layer**: REST API (Adapter-In)
**브랜치**: feature/EVENTBRIDGE-004-rest-api
**Jira URL**: (sync-to-jira 후 추가)

---

## 📝 목적

EventBridge 바운더리 컨텍스트의 REST API 엔드포인트를 구현합니다.

**핵심 기능**:
- 스케줄 등록/수정/조회 API
- 스케줄 이력 조회 API
- Custom Validation (Cron Expression)

---

## 🎯 요구사항

### API 엔드포인트

#### 스케줄 등록

- [ ] **Endpoint**: `POST /api/v1/sellers/{sellerId}/schedulers`
- [ ] **Request**: `RegisterSchedulerRequest`
  ```java
  public record RegisterSchedulerRequest(
      @NotBlank
      @Pattern(regexp = "^[a-zA-Z0-9-_]+$", message = "스케줄러 이름은 영문, 숫자, -, _만 사용 가능합니다.")
      String schedulerName,

      @NotBlank
      @CronExpression(type = CronType.AWS_EVENT_BRIDGE)
      String cronExpression
  ) {}
  ```
- [ ] **Response**: `SchedulerResponse` (201 Created)

#### 스케줄 수정

- [ ] **Endpoint**: `PATCH /api/v1/schedulers/{schedulerId}`
- [ ] **Request**: `UpdateSchedulerRequest`
  ```java
  public record UpdateSchedulerRequest(
      String schedulerName,
      String cronExpression,
      SchedulerStatus status
  ) {
      public boolean hasAnyChange() {
          return schedulerName != null || cronExpression != null || status != null;
      }
  }
  ```
- [ ] **Response**: `SchedulerResponse` (200 OK)

#### 스케줄 조회

- [ ] **Endpoint**: `GET /api/v1/schedulers/{schedulerId}`
- [ ] **Response**: `SchedulerDetailResponse` (200 OK)

#### 스케줄 목록 조회

- [ ] **Endpoint**: `GET /api/v1/schedulers`
- [ ] **Query Parameters**:
  - `sellerId`: Long (Optional)
  - `status`: SchedulerStatus (Optional)
  - `page`: Integer (기본값 0)
  - `size`: Integer (기본값 10)
- [ ] **Response**: `PageResponse<SchedulerSummaryResponse>` (200 OK)

#### 스케줄 이력 조회

- [ ] **Endpoint**: `GET /api/v1/schedulers/{schedulerId}/history`
- [ ] **Query Parameters**:
  - `page`: Integer (기본값 0)
  - `size`: Integer (기본값 10)
- [ ] **Response**: `PageResponse<SchedulerHistoryResponse>` (200 OK)

### Request/Response DTO

- [ ] **RegisterSchedulerRequest**
- [ ] **UpdateSchedulerRequest**
- [ ] **SchedulerResponse**
  ```java
  public record SchedulerResponse(
      Long schedulerId,
      Long sellerId,
      String schedulerName,
      String cronExpression,
      SchedulerStatus status,
      String eventBridgeRuleName,
      LocalDateTime createdAt
  ) {}
  ```

- [ ] **SchedulerDetailResponse**
  ```java
  public record SchedulerDetailResponse(
      Long schedulerId,
      Long sellerId,
      String schedulerName,
      String cronExpression,
      SchedulerStatus status,
      String eventBridgeRuleName,
      LocalDateTime createdAt,
      LocalDateTime updatedAt
  ) {}
  ```

- [ ] **SchedulerSummaryResponse**
  ```java
  public record SchedulerSummaryResponse(
      Long schedulerId,
      Long sellerId,
      String schedulerName,
      String cronExpression,
      SchedulerStatus status
  ) {}
  ```

- [ ] **SchedulerHistoryResponse**
  ```java
  public record SchedulerHistoryResponse(
      Long historyId,
      Long schedulerId,
      String changedField,
      String oldValue,
      String newValue,
      LocalDateTime changedAt
  ) {}
  ```

### Custom Validator

#### CronExpressionValidator

- [ ] **Annotation**: `@CronExpression(type = CronType.AWS_EVENT_BRIDGE)`
- [ ] **검증 규칙**:
  - AWS EventBridge Cron 형식 (6자리)
  - 형식: `cron(분 시 일 월 요일 년도)`
  - 예시: `cron(0 0 * * ? *)`
  - 최소 1시간 간격 검증

- [ ] **구현**:
  ```java
  @Target({ElementType.FIELD, ElementType.PARAMETER})
  @Retention(RetentionPolicy.RUNTIME)
  @Constraint(validatedBy = CronExpressionValidator.class)
  public @interface CronExpression {
      CronType type();
      String message() default "유효하지 않은 Cron Expression 형식입니다.";
      Class<?>[] groups() default {};
      Class<? extends Payload>[] payload() default {};
  }

  public enum CronType {
      AWS_EVENT_BRIDGE
  }
  ```

#### SchedulerNameValidator

- [ ] **Annotation**: `@Pattern(regexp = "^[a-zA-Z0-9-_]+$")`
- [ ] **검증 규칙**: 영문, 숫자, -, _ 만 허용

### Exception Handling

#### Global Exception Handler

- [ ] **DuplicateSchedulerNameException** → 409 Conflict

- [ ] **InvalidCronExpressionException** → 400 Bad Request

- [ ] **SellerNotActiveException** → 400 Bad Request

- [ ] **SchedulerNotFoundException** → 404 Not Found

- [ ] **MethodArgumentNotValidException** → 400 Bad Request
  ```json
  {
    "errorCode": "VALIDATION_FAILED",
    "message": "입력값 검증에 실패했습니다.",
    "errors": [
      {
        "field": "cronExpression",
        "message": "유효하지 않은 Cron Expression 형식입니다."
      }
    ],
    "timestamp": "2025-01-18T12:34:56Z",
    "path": "/api/v1/sellers/1/schedulers"
  }
  ```

### Controller 구현

#### CrawlingSchedulerApiController

- [ ] **Endpoint 구현**:
  - `POST /api/v1/sellers/{sellerId}/schedulers`
  - `PATCH /api/v1/schedulers/{schedulerId}`
  - `GET /api/v1/schedulers/{schedulerId}`
  - `GET /api/v1/schedulers`
  - `GET /api/v1/schedulers/{schedulerId}/history`

- [ ] **UseCase 직접 의존 (5-10개 정상)**:
  - `RegisterSchedulerUseCase` (직접 주입)
  - `UpdateSchedulerUseCase` (직접 주입)
  - `GetSchedulerUseCase` (직접 주입)
  - `ListSchedulersUseCase` (직접 주입)
  - `GetSchedulerHistoryUseCase` (직접 주입)
  - **Facade 사용 금지**: Controller는 UseCase 직접 의존

- [ ] **Mapper DI 필수**:
  - `OrderApiMapper` (@Component Bean으로 주입)
  - Static 유틸리티 클래스 금지
  - MessageSource 등 의존성 필요 시 생성자 주입

---

## ⚠️ 제약사항

### Zero-Tolerance 규칙

- [ ] **Pure Java 원칙**
  - Lombok **절대 금지**
  - Request/Response DTO는 Java 21 Record 사용

- [ ] **DI Mapper 사용 필수**
  - Mapper는 **@Component**로 DI
  - Static 유틸리티 클래스 금지
  - 의존성 필요 시 생성자 주입 (MessageSource 등)

- [ ] **RESTful 설계 원칙**
  - 리소스 기반 URI (명사 복수형)
  - HTTP 메서드 활용 (GET, POST, PATCH, DELETE)
  - RPC 스타일 URI 금지 (`/createOrder`, `/getOrders` 등)

- [ ] **RFC 7807 준수 Error Response**
  - Problem Details 표준 준수
  - `errorCode`, `message`, `timestamp`, `path` 필수 포함
  ```json
  {
    "errorCode": "VALIDATION_FAILED",
    "message": "입력값 검증에 실패했습니다.",
    "errors": [...],
    "timestamp": "2025-01-18T12:34:56Z",
    "path": "/api/v1/sellers/1/schedulers"
  }
  ```

- [ ] **Bean Validation 필수**
  - 모든 Request DTO에 `@Valid` + 제약 조건 어노테이션

- [ ] **TestRestTemplate 사용 (Integration Test)**
  - MockMvc 금지 (E2E 테스트)

### 테스트 규칙

- [ ] **Integration Test 필수**
  - TestRestTemplate 사용

- [ ] **성공 케이스 테스트**
  - 201 Created (스케줄 등록)
  - 200 OK (스케줄 조회, 수정)

- [ ] **실패 케이스 테스트**
  - 400 Bad Request (Validation 실패, Cron 형식 오류)
  - 404 Not Found (스케줄 없음)
  - 409 Conflict (중복)

- [ ] **Custom Validator 테스트**
  - CronExpressionValidator 검증
  - SchedulerNameValidator 검증

- [ ] **Exception Handling 테스트**

- [ ] **ArchUnit 테스트**

- [ ] **테스트 커버리지 > 80%**

---

## ✅ 완료 조건

- [ ] Controller 구현 완료
  - CrawlingSchedulerApiController

- [ ] Request/Response DTO 구현 완료

- [ ] Custom Validator 구현 완료
  - CronExpressionValidator

- [ ] Global Exception Handler 구현 완료

- [ ] REST API Integration Test 완료
  - 성공 케이스
  - 실패 케이스
  - Custom Validator 테스트
  - Exception Handling 테스트

- [ ] ArchUnit 테스트 완료

- [ ] Zero-Tolerance 규칙 준수 확인

- [ ] API 문서화 (Spring REST Docs)

- [ ] 코드 리뷰 승인

- [ ] PR 머지 완료

---

## 🔗 관련 문서

- **PRD**: `/Users/sangwon-ryu/crawlinghub/docs/prd/mustit-seller-crawler-scheduler.md`
- **Plan**: `docs/prd/seller/eventbridge/plans/EVENTBRIDGE-004-rest-api-plan.md`
- **Jira**: (sync-to-jira 후 추가)
- **코딩 규칙**: `docs/coding_convention/01-adapter-in-layer/rest-api/`
- **선행 Task**: EVENTBRIDGE-001, EVENTBRIDGE-002, EVENTBRIDGE-003

---

## 📋 다음 단계

1. `/create-plan EVENTBRIDGE-004` - TDD Plan 생성
2. `/kb/rest-api/go` - REST API Layer TDD 시작
