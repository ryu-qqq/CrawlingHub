# EVENTBRIDGE-004: REST API Layer TDD Plan

**Epic**: 머스트잇 셀러 크롤링 스케줄러
**Layer**: REST API (Adapter-In)
**브랜치**: feature/EVENTBRIDGE-004-rest-api
**예상 소요 시간**: 255분 (17 사이클 × 15분)

---

## 📋 TDD 사이클 개요

| 사이클 | 대상 | 예상 시간 |
|--------|------|----------|
| 1 | RegisterSchedulerRequest DTO | 15분 |
| 2 | UpdateSchedulerRequest DTO | 15분 |
| 3 | SchedulerResponse DTO | 15분 |
| 4 | SchedulerDetailResponse DTO | 15분 |
| 5 | SchedulerSummaryResponse DTO | 15분 |
| 6 | SchedulerHistoryResponse DTO | 15분 |
| 7 | CronExpressionValidator 구현 | 15분 |
| 8 | SchedulerApiMapper (@Component) | 15분 |
| 9 | CrawlingSchedulerApiController - 스케줄 등록 API | 15분 |
| 10 | CrawlingSchedulerApiController - 스케줄 수정 API | 15분 |
| 11 | CrawlingSchedulerApiController - 스케줄 조회 API | 15분 |
| 12 | CrawlingSchedulerApiController - 스케줄 목록 조회 API | 15분 |
| 13 | CrawlingSchedulerApiController - 스케줄 이력 조회 API | 15분 |
| 14 | Global Exception Handler 구현 | 15분 |
| 15 | REST API Integration Test (성공 케이스) | 15분 |
| 16 | REST API Integration Test (실패 케이스) | 15분 |
| 17 | REST API Layer ArchUnit 테스트 | 15분 |

---

## 🔄 Cycle 1: RegisterSchedulerRequest DTO

**목표**: 스케줄 등록 Request DTO 구현 (Java 21 Record, Bean Validation)

#### 🔴 Red: 테스트 작성
- [ ] `RegisterSchedulerRequestTest` 생성
  - Record 타입 검증
  - `@NotBlank` 검증 (schedulerName, cronExpression)
  - `@Pattern` 검증 (schedulerName: 영문, 숫자, -, _ 만)
  - `@CronExpression` 검증 (AWS EventBridge 형식)
  - Lombok 미사용 검증
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: RegisterSchedulerRequest DTO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `RegisterSchedulerRequest` Record 생성
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
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: RegisterSchedulerRequest DTO 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] Validation 메시지 명확성 개선
- [ ] 커밋: `struct: RegisterSchedulerRequest DTO 구조 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `RegisterSchedulerRequestFixture` 생성
- [ ] 커밋: `test: RegisterSchedulerRequestFixture 정리 (Tidy)`

---

## 🔄 Cycle 2: UpdateSchedulerRequest DTO

**목표**: 스케줄 수정 Request DTO 구현 (Optional 필드 처리)

#### 🔴 Red: 테스트 작성
- [ ] `UpdateSchedulerRequestTest` 생성
  - Record 타입 검증
  - Optional 필드 검증 (schedulerName, cronExpression, status)
  - `hasAnyChange()` 메서드 검증
  - Lombok 미사용 검증
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: UpdateSchedulerRequest DTO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `UpdateSchedulerRequest` Record 생성
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
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: UpdateSchedulerRequest DTO 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] Validation 로직 개선
- [ ] 커밋: `struct: UpdateSchedulerRequest DTO 구조 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `UpdateSchedulerRequestFixture` 생성
- [ ] 커밋: `test: UpdateSchedulerRequestFixture 정리 (Tidy)`

---

## 🔄 Cycle 3: SchedulerResponse DTO

**목표**: 스케줄 등록/수정 Response DTO 구현

#### 🔴 Red: 테스트 작성
- [ ] `SchedulerResponseTest` 생성
  - Record 타입 검증
  - 필수 필드 검증 (schedulerId, sellerId, schedulerName, cronExpression, status, createdAt)
  - Optional 필드 검증 (eventBridgeRuleName)
  - Lombok 미사용 검증
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: SchedulerResponse DTO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SchedulerResponse` Record 생성
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
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: SchedulerResponse DTO 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] DTO 필드 순서 최적화
- [ ] 커밋: `struct: SchedulerResponse DTO 구조 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `SchedulerResponseFixture` 생성
- [ ] 커밋: `test: SchedulerResponseFixture 정리 (Tidy)`

---

## 🔄 Cycle 4: SchedulerDetailResponse DTO

**목표**: 스케줄 상세 조회 Response DTO 구현

#### 🔴 Red: 테스트 작성
- [ ] `SchedulerDetailResponseTest` 생성
  - Record 타입 검증
  - 필수 필드 검증 (updatedAt 포함)
  - Lombok 미사용 검증
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: SchedulerDetailResponse DTO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SchedulerDetailResponse` Record 생성
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
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: SchedulerDetailResponse DTO 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] DTO 구조 최적화
- [ ] 커밋: `struct: SchedulerDetailResponse DTO 구조 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `SchedulerDetailResponseFixture` 생성
- [ ] 커밋: `test: SchedulerDetailResponseFixture 정리 (Tidy)`

---

## 🔄 Cycle 5: SchedulerSummaryResponse DTO

**목표**: 스케줄 목록 조회 Response DTO 구현 (간략 정보)

#### 🔴 Red: 테스트 작성
- [ ] `SchedulerSummaryResponseTest` 생성
  - Record 타입 검증
  - 필수 필드만 포함 검증 (5개 필드)
  - Lombok 미사용 검증
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: SchedulerSummaryResponse DTO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SchedulerSummaryResponse` Record 생성
  ```java
  public record SchedulerSummaryResponse(
      Long schedulerId,
      Long sellerId,
      String schedulerName,
      String cronExpression,
      SchedulerStatus status
  ) {}
  ```
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: SchedulerSummaryResponse DTO 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] DTO 구조 최적화
- [ ] 커밋: `struct: SchedulerSummaryResponse DTO 구조 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `SchedulerSummaryResponseFixture` 생성
- [ ] 커밋: `test: SchedulerSummaryResponseFixture 정리 (Tidy)`

---

## 🔄 Cycle 6: SchedulerHistoryResponse DTO

**목표**: 스케줄 이력 조회 Response DTO 구현

#### 🔴 Red: 테스트 작성
- [ ] `SchedulerHistoryResponseTest` 생성
  - Record 타입 검증
  - 필수 필드 검증 (historyId, schedulerId, changedField, oldValue, newValue, changedAt)
  - Lombok 미사용 검증
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: SchedulerHistoryResponse DTO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SchedulerHistoryResponse` Record 생성
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
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: SchedulerHistoryResponse DTO 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] DTO 구조 최적화
- [ ] 커밋: `struct: SchedulerHistoryResponse DTO 구조 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `SchedulerHistoryResponseFixture` 생성
- [ ] 커밋: `test: SchedulerHistoryResponseFixture 정리 (Tidy)`

---

## 🔄 Cycle 7: CronExpressionValidator 구현

**목표**: AWS EventBridge Cron Expression Custom Validator 구현

#### 🔴 Red: 테스트 작성
- [ ] `CronExpressionValidatorTest` 생성
  - `@CronExpression` 어노테이션 검증
  - AWS EventBridge 형식 검증 (6자리)
  - 유효한 Cron: `cron(0 0 * * ? *)` → 통과
  - 무효한 Cron: `0 0 * * * *` → 실패 (cron() 누락)
  - 최소 1시간 간격 검증
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: CronExpressionValidator 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `@CronExpression` 어노테이션 생성
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
- [ ] `CronExpressionValidator` 클래스 생성
  ```java
  public class CronExpressionValidator implements ConstraintValidator<CronExpression, String> {
      private CronType cronType;

      @Override
      public void initialize(CronExpression constraintAnnotation) {
          this.cronType = constraintAnnotation.type();
      }

      @Override
      public boolean isValid(String value, ConstraintValidatorContext context) {
          if (value == null || value.isBlank()) {
              return false;
          }

          // AWS EventBridge Cron 형식 검증
          // cron(분 시 일 월 요일 년도)
          if (cronType == CronType.AWS_EVENT_BRIDGE) {
              String pattern = "^cron\\(([^)]+)\\)$";
              if (!value.matches(pattern)) {
                  return false;
              }

              // 6개 필드 검증
              String cronContent = value.substring(5, value.length() - 1);
              String[] fields = cronContent.split("\\s+");
              if (fields.length != 6) {
                  return false;
              }

              // 추가 검증 (최소 1시간 간격 등)
              return true;
          }

          return false;
      }
  }
  ```
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: CronExpressionValidator 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] Validation 로직 정교화
- [ ] 에러 메시지 개선
- [ ] 커밋: `struct: CronExpressionValidator 구조 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Validator 테스트 Fixture 정리
- [ ] 커밋: `test: CronExpressionValidator Fixture 정리 (Tidy)`

---

## 🔄 Cycle 8: SchedulerApiMapper (@Component)

**목표**: API DTO ↔ Application DTO 변환 Mapper 구현 (@Component Bean, Static 금지)

#### 🔴 Red: 테스트 작성
- [ ] `SchedulerApiMapperTest` 생성
  - `@Component` 어노테이션 검증
  - Static 유틸리티 클래스 금지 검증
  - Request → Command DTO 변환 테스트
  - Response DTO → API Response 변환 테스트
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: SchedulerApiMapper 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SchedulerApiMapper` 클래스 생성 (@Component)
  ```java
  @Component
  public class SchedulerApiMapper {
      // 의존성 주입 가능 (MessageSource 등)

      public RegisterSchedulerCommand toCommand(Long sellerId, RegisterSchedulerRequest request) {
          return new RegisterSchedulerCommand(
              sellerId,
              request.schedulerName(),
              request.cronExpression()
          );
      }

      public UpdateSchedulerCommand toCommand(Long schedulerId, UpdateSchedulerRequest request) {
          return new UpdateSchedulerCommand(
              schedulerId,
              request.schedulerName(),
              request.cronExpression(),
              request.status()
          );
      }

      public SchedulerResponse toResponse(SchedulerResponseDto dto) {
          return new SchedulerResponse(
              dto.schedulerId(),
              dto.sellerId(),
              dto.schedulerName(),
              dto.cronExpression(),
              dto.status(),
              dto.eventBridgeRuleName(),
              dto.createdAt()
          );
      }

      // 다른 변환 메서드들...
  }
  ```
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: SchedulerApiMapper 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] Mapper 로직 명확성 개선
- [ ] 커밋: `struct: SchedulerApiMapper 구조 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] SchedulerApiMapper Fixture 정리
- [ ] 커밋: `test: SchedulerApiMapper Fixture 정리 (Tidy)`

---

## 🔄 Cycle 9: CrawlingSchedulerApiController - 스케줄 등록 API

**목표**: POST /api/v1/sellers/{sellerId}/schedulers 구현

#### 🔴 Red: 테스트 작성
- [ ] `CrawlingSchedulerApiControllerTest` 생성 (TestRestTemplate)
  - POST 요청 성공 테스트 (201 Created)
  - Request Validation 실패 테스트 (400 Bad Request)
  - `@Valid` 검증
  - UseCase 직접 의존 검증 (Facade 사용 금지)
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: 스케줄 등록 API 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `CrawlingSchedulerApiController` 클래스 생성
  ```java
  @RestController
  @RequestMapping("/api/v1")
  public class CrawlingSchedulerApiController {
      private final RegisterSchedulerUseCase registerSchedulerUseCase;
      private final SchedulerApiMapper mapper;

      public CrawlingSchedulerApiController(
          RegisterSchedulerUseCase registerSchedulerUseCase,
          SchedulerApiMapper mapper
      ) {
          this.registerSchedulerUseCase = registerSchedulerUseCase;
          this.mapper = mapper;
      }

      @PostMapping("/sellers/{sellerId}/schedulers")
      public ResponseEntity<SchedulerResponse> registerScheduler(
          @PathVariable Long sellerId,
          @Valid @RequestBody RegisterSchedulerRequest request
      ) {
          RegisterSchedulerCommand command = mapper.toCommand(sellerId, request);
          SchedulerResponseDto result = registerSchedulerUseCase.execute(command);
          SchedulerResponse response = mapper.toResponse(result);
          return ResponseEntity.status(HttpStatus.CREATED).body(response);
      }
  }
  ```
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: 스케줄 등록 API 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] Controller 로직 명확성 개선
- [ ] 커밋: `struct: 스케줄 등록 API 구조 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] API 테스트 Fixture 정리
- [ ] 커밋: `test: 스케줄 등록 API Fixture 정리 (Tidy)`

---

## 🔄 Cycle 10: CrawlingSchedulerApiController - 스케줄 수정 API

**목표**: PATCH /api/v1/schedulers/{schedulerId} 구현

#### 🔴 Red: 테스트 작성
- [ ] PATCH 요청 성공 테스트 (200 OK)
  - Cron Expression 변경 테스트
  - Status 변경 테스트
  - 변경 사항 없을 시 검증
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: 스케줄 수정 API 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `updateScheduler()` 메서드 추가
  ```java
  private final UpdateSchedulerUseCase updateSchedulerUseCase;

  @PatchMapping("/schedulers/{schedulerId}")
  public ResponseEntity<SchedulerResponse> updateScheduler(
      @PathVariable Long schedulerId,
      @Valid @RequestBody UpdateSchedulerRequest request
  ) {
      if (!request.hasAnyChange()) {
          throw new InvalidRequestException("변경할 내용이 없습니다.");
      }

      UpdateSchedulerCommand command = mapper.toCommand(schedulerId, request);
      SchedulerResponseDto result = updateSchedulerUseCase.execute(command);
      SchedulerResponse response = mapper.toResponse(result);
      return ResponseEntity.ok(response);
  }
  ```
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: 스케줄 수정 API 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] Validation 로직 개선
- [ ] 커밋: `struct: 스케줄 수정 API 구조 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 스케줄 수정 API Fixture 정리
- [ ] 커밋: `test: 스케줄 수정 API Fixture 정리 (Tidy)`

---

## 🔄 Cycle 11: CrawlingSchedulerApiController - 스케줄 조회 API

**목표**: GET /api/v1/schedulers/{schedulerId} 구현

#### 🔴 Red: 테스트 작성
- [ ] GET 요청 성공 테스트 (200 OK)
  - 스케줄 상세 정보 반환 검증
  - 존재하지 않는 schedulerId → 404 Not Found 검증
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: 스케줄 조회 API 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `getScheduler()` 메서드 추가
  ```java
  private final GetSchedulerUseCase getSchedulerUseCase;

  @GetMapping("/schedulers/{schedulerId}")
  public ResponseEntity<SchedulerDetailResponse> getScheduler(@PathVariable Long schedulerId) {
      GetSchedulerQuery query = new GetSchedulerQuery(schedulerId);
      SchedulerDetailResponseDto result = getSchedulerUseCase.execute(query);
      SchedulerDetailResponse response = mapper.toDetailResponse(result);
      return ResponseEntity.ok(response);
  }
  ```
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: 스케줄 조회 API 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] 조회 로직 최적화
- [ ] 커밋: `struct: 스케줄 조회 API 구조 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 스케줄 조회 API Fixture 정리
- [ ] 커밋: `test: 스케줄 조회 API Fixture 정리 (Tidy)`

---

## 🔄 Cycle 12: CrawlingSchedulerApiController - 스케줄 목록 조회 API

**목표**: GET /api/v1/schedulers (Query Parameters, 페이징)

#### 🔴 Red: 테스트 작성
- [ ] GET 요청 성공 테스트 (200 OK)
  - `sellerId` 필터링 테스트
  - `status` 필터링 테스트
  - 페이징 (page, size) 테스트
  - 빈 목록 반환 테스트
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: 스케줄 목록 조회 API 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `listSchedulers()` 메서드 추가
  ```java
  private final ListSchedulersUseCase listSchedulersUseCase;

  @GetMapping("/schedulers")
  public ResponseEntity<PageResponse<SchedulerSummaryResponse>> listSchedulers(
      @RequestParam(required = false) Long sellerId,
      @RequestParam(required = false) SchedulerStatus status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size
  ) {
      ListSchedulersQuery query = new ListSchedulersQuery(sellerId, status, page, size);
      PageResponseDto<SchedulerSummaryResponseDto> result = listSchedulersUseCase.execute(query);
      PageResponse<SchedulerSummaryResponse> response = mapper.toPageResponse(result);
      return ResponseEntity.ok(response);
  }
  ```
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: 스케줄 목록 조회 API 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] 페이징 로직 최적화
- [ ] 커밋: `struct: 스케줄 목록 조회 API 구조 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 스케줄 목록 조회 API Fixture 정리
- [ ] 커밋: `test: 스케줄 목록 조회 API Fixture 정리 (Tidy)`

---

## 🔄 Cycle 13: CrawlingSchedulerApiController - 스케줄 이력 조회 API

**목표**: GET /api/v1/schedulers/{schedulerId}/history (페이징)

#### 🔴 Red: 테스트 작성
- [ ] GET 요청 성공 테스트 (200 OK)
  - 이력 페이징 조회 테스트
  - 변경 내역 시간 역순 정렬 검증
  - 빈 이력 반환 테스트
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: 스케줄 이력 조회 API 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `getSchedulerHistory()` 메서드 추가
  ```java
  private final GetSchedulerHistoryUseCase getSchedulerHistoryUseCase;

  @GetMapping("/schedulers/{schedulerId}/history")
  public ResponseEntity<PageResponse<SchedulerHistoryResponse>> getSchedulerHistory(
      @PathVariable Long schedulerId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size
  ) {
      GetSchedulerHistoryQuery query = new GetSchedulerHistoryQuery(schedulerId, page, size);
      PageResponseDto<SchedulerHistoryResponseDto> result = getSchedulerHistoryUseCase.execute(query);
      PageResponse<SchedulerHistoryResponse> response = mapper.toHistoryPageResponse(result);
      return ResponseEntity.ok(response);
  }
  ```
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: 스케줄 이력 조회 API 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] 이력 조회 로직 최적화
- [ ] 커밋: `struct: 스케줄 이력 조회 API 구조 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 스케줄 이력 조회 API Fixture 정리
- [ ] 커밋: `test: 스케줄 이력 조회 API Fixture 정리 (Tidy)`

---

## 🔄 Cycle 14: Global Exception Handler 구현

**목표**: RFC 7807 준수 Global Exception Handler 구현

#### 🔴 Red: 테스트 작성
- [ ] `GlobalExceptionHandlerTest` 생성
  - `DuplicateSchedulerNameException` → 409 Conflict
  - `InvalidCronExpressionException` → 400 Bad Request
  - `SellerNotActiveException` → 400 Bad Request
  - `SchedulerNotFoundException` → 404 Not Found
  - `MethodArgumentNotValidException` → 400 Bad Request
  - RFC 7807 포맷 검증 (errorCode, message, timestamp, path)
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: Global Exception Handler 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `GlobalExceptionHandler` 클래스 생성
  ```java
  @RestControllerAdvice
  public class GlobalExceptionHandler {

      @ExceptionHandler(DuplicateSchedulerNameException.class)
      public ResponseEntity<ErrorResponse> handleDuplicateSchedulerName(
          DuplicateSchedulerNameException ex,
          HttpServletRequest request
      ) {
          ErrorResponse error = ErrorResponse.of(
              "DUPLICATE_SCHEDULER_NAME",
              ex.getMessage(),
              request.getRequestURI()
          );
          return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
      }

      @ExceptionHandler(InvalidCronExpressionException.class)
      public ResponseEntity<ErrorResponse> handleInvalidCronExpression(
          InvalidCronExpressionException ex,
          HttpServletRequest request
      ) {
          ErrorResponse error = ErrorResponse.of(
              "INVALID_CRON_EXPRESSION",
              ex.getMessage(),
              request.getRequestURI()
          );
          return ResponseEntity.badRequest().body(error);
      }

      @ExceptionHandler(SchedulerNotFoundException.class)
      public ResponseEntity<ErrorResponse> handleSchedulerNotFound(
          SchedulerNotFoundException ex,
          HttpServletRequest request
      ) {
          ErrorResponse error = ErrorResponse.of(
              "SCHEDULER_NOT_FOUND",
              ex.getMessage(),
              request.getRequestURI()
          );
          return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
      }

      @ExceptionHandler(MethodArgumentNotValidException.class)
      public ResponseEntity<ErrorResponse> handleValidationException(
          MethodArgumentNotValidException ex,
          HttpServletRequest request
      ) {
          List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
              .map(error -> new FieldError(error.getField(), error.getDefaultMessage()))
              .toList();

          ErrorResponse error = ErrorResponse.of(
              "VALIDATION_FAILED",
              "입력값 검증에 실패했습니다.",
              request.getRequestURI(),
              fieldErrors
          );
          return ResponseEntity.badRequest().body(error);
      }
  }

  // ErrorResponse Record (RFC 7807 준수)
  public record ErrorResponse(
      String errorCode,
      String message,
      List<FieldError> errors,
      String timestamp,
      String path
  ) {
      public static ErrorResponse of(String errorCode, String message, String path) {
          return new ErrorResponse(
              errorCode,
              message,
              List.of(),
              LocalDateTime.now().toString(),
              path
          );
      }

      public static ErrorResponse of(String errorCode, String message, String path, List<FieldError> errors) {
          return new ErrorResponse(
              errorCode,
              message,
              errors,
              LocalDateTime.now().toString(),
              path
          );
      }
  }

  public record FieldError(String field, String message) {}
  ```
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: Global Exception Handler 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] Exception Handling 로직 개선
- [ ] RFC 7807 준수 재확인
- [ ] 커밋: `struct: Global Exception Handler 구조 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Exception Handler Fixture 정리
- [ ] 커밋: `test: Exception Handler Fixture 정리 (Tidy)`

---

## 🔄 Cycle 15: REST API Integration Test (성공 케이스)

**목표**: 전체 API 성공 시나리오 통합 테스트

#### 🔴 Red: 테스트 작성
- [ ] `SchedulerApiIntegrationTest` 생성 (TestRestTemplate)
  - 스케줄 등록 → 201 Created
  - 스케줄 조회 → 200 OK
  - 스케줄 수정 → 200 OK
  - 스케줄 목록 조회 → 200 OK (페이징)
  - 스케줄 이력 조회 → 200 OK (페이징)
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: REST API 성공 케이스 통합 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] 통합 테스트 환경 구성
  - `@SpringBootTest(webEnvironment = RANDOM_PORT)`
  - TestContainers MySQL
  - `@Sql` 테스트 데이터 삽입
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: REST API 성공 케이스 통합 테스트 통과 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] 테스트 시나리오 명확성 개선
- [ ] 커밋: `struct: 성공 케이스 통합 테스트 구조 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 통합 테스트 데이터 SQL 파일로 이동
- [ ] 커밋: `test: 성공 케이스 통합 테스트 Fixture 정리 (Tidy)`

---

## 🔄 Cycle 16: REST API Integration Test (실패 케이스)

**목표**: 전체 API 실패 시나리오 통합 테스트

#### 🔴 Red: 테스트 작성
- [ ] 실패 케이스 테스트 추가
  - Validation 실패 → 400 Bad Request
  - 중복 schedulerName → 409 Conflict
  - 존재하지 않는 schedulerId → 404 Not Found
  - 잘못된 Cron Expression → 400 Bad Request
  - 비활성화된 Seller → 400 Bad Request
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: REST API 실패 케이스 통합 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] Exception Handling 보완
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: REST API 실패 케이스 통합 테스트 통과 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] 에러 메시지 명확성 개선
- [ ] 커밋: `struct: 실패 케이스 통합 테스트 구조 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 실패 케이스 테스트 데이터 정리
- [ ] 커밋: `test: 실패 케이스 통합 테스트 Fixture 정리 (Tidy)`

---

## 🔄 Cycle 17: REST API Layer ArchUnit 테스트

**목표**: REST API Layer 아키텍처 규칙 검증

#### 🔴 Red: 테스트 작성
- [ ] `RestApiLayerArchUnitTest` 생성
  - Lombok 금지 규칙 (Request/Response DTO)
  - Java 21 Record 사용 규칙
  - DI Mapper 규칙 (`@Component`)
  - Static 유틸리티 클래스 금지 규칙
  - RESTful URI 규칙 (리소스 기반, 명사 복수형)
  - RPC 스타일 URI 금지 (`/createOrder` 등)
  - `@Valid` 필수 규칙 (Request DTO)
  - Controller UseCase 직접 의존 규칙 (Facade 금지)
  - Bean Validation 어노테이션 필수 규칙
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: REST API Layer ArchUnit 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] ArchUnit 규칙 위반 수정
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: REST API Layer ArchUnit 테스트 통과 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] ArchUnit 규칙 강화
- [ ] 커밋: `struct: ArchUnit 규칙 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] ArchUnit 테스트 정리
- [ ] 커밋: `test: ArchUnit Fixture 정리 (Tidy)`

---

## ✅ 완료 조건

- [ ] **17개 TDD 사이클 완료**
- [ ] **Request DTO 구현 완료** (2개, Java 21 Record)
  - RegisterSchedulerRequest
  - UpdateSchedulerRequest
- [ ] **Response DTO 구현 완료** (4개, Java 21 Record)
  - SchedulerResponse
  - SchedulerDetailResponse
  - SchedulerSummaryResponse
  - SchedulerHistoryResponse
- [ ] **Custom Validator 구현 완료**
  - CronExpressionValidator
- [ ] **Mapper 구현 완료** (@Component Bean)
  - SchedulerApiMapper
- [ ] **Controller 구현 완료**
  - CrawlingSchedulerApiController (5개 엔드포인트)
- [ ] **Global Exception Handler 구현 완료** (RFC 7807 준수)
- [ ] **Integration Test 완료** (TestRestTemplate)
  - 성공 케이스
  - 실패 케이스
- [ ] **ArchUnit 테스트 완료**
- [ ] **모든 커밋 메시지 규칙 준수** (test:, feat:, struct:, test:)

---

## 📊 최종 통계

- **총 사이클 수**: 17개
- **예상 소요 시간**: 255분 (4시간 15분)
- **총 체크박스**: 68개 (17 사이클 × 4 단계)
- **커밋 횟수**: 68회 (각 단계마다 커밋)
- **API 엔드포인트**: 5개
  - POST /api/v1/sellers/{sellerId}/schedulers
  - PATCH /api/v1/schedulers/{schedulerId}
  - GET /api/v1/schedulers/{schedulerId}
  - GET /api/v1/schedulers
  - GET /api/v1/schedulers/{schedulerId}/history

---

## 🔗 관련 문서

- **PRD**: `/Users/sangwon-ryu/crawlinghub/docs/prd/eventbridge/EVENTBRIDGE-004-rest-api.md`
- **코딩 규칙**: `docs/coding_convention/01-adapter-in-layer/rest-api/`
- **선행 Task**: EVENTBRIDGE-001-domain-plan.md, EVENTBRIDGE-002-application-plan.md, EVENTBRIDGE-003-persistence-plan.md
