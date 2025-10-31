# 🎯 REST API Layer 개발 태스크

## 📌 개발 순서 및 우선순위
1. **Seller Management APIs** (Priority: P0) - 셀러 관리 API
2. **Schedule Management APIs** (Priority: P0) - 스케줄 관리 API
3. **Crawl Trigger APIs** (Priority: P0) - 크롤링 트리거 API
4. **Task Management APIs** (Priority: P1) - 태스크 관리 API
5. **Monitoring APIs** (Priority: P1) - 모니터링 API
6. **Webhook APIs** (Priority: P1) - 외부 연동 API

---

## 📦 TASK-01: Seller Management APIs

### API-01-1: 셀러 등록 API
```java
package com.ryuqq.crawlinghub.adapter.in.rest.seller;

@RestController
@RequestMapping("/api/v1/sellers")
@RequiredArgsConstructor
public class SellerController {

    private final ManageSellerUseCase manageSellerUseCase;
    private final SellerApiMapper mapper;

    @PostMapping
    @Operation(summary = "머스트잇 셀러 등록")
    public ApiResponse<SellerApiResponse> registerSeller(
            @Valid @RequestBody RegisterSellerApiRequest request) {

        RegisterSellerCommand command = mapper.toCommand(request);
        SellerResponse response = manageSellerUseCase.registerSeller(command);

        return ApiResponse.success(
            mapper.toApiResponse(response),
            "셀러가 성공적으로 등록되었습니다"
        );
    }
}
```

### API-01-2: 셀러 상태 변경 API
```java
@PatchMapping("/{sellerId}/status")
@Operation(summary = "셀러 상태 변경")
public ApiResponse<Void> updateSellerStatus(
        @PathVariable Long sellerId,
        @Valid @RequestBody UpdateStatusApiRequest request) {

    UpdateSellerStatusCommand command = mapper.toCommand(sellerId, request);
    manageSellerUseCase.updateSellerStatus(command);

    return ApiResponse.success("셀러 상태가 변경되었습니다");
}
```

### API-01-3: 셀러 목록 조회 API
```java
@GetMapping
@Operation(summary = "셀러 목록 조회")
public ApiResponse<PageResponse<SellerApiResponse>> getSellers(
        @Valid SellerSearchApiRequest request,
        @PageableDefault(size = 20) Pageable pageable) {

    // 구현
    return ApiResponse.success(pageResponse);
}
```

### API-01-4: 셀러 상세 조회 API
```java
@GetMapping("/{sellerId}")
@Operation(summary = "셀러 상세 조회")
public ApiResponse<SellerDetailApiResponse> getSellerDetail(
        @PathVariable Long sellerId) {

    GetSellerQuery query = new GetSellerQuery(sellerId);
    SellerDetailResponse response = getSellerDetailUseCase.execute(query);

    return ApiResponse.success(mapper.toDetailApiResponse(response));
}
```

### Request/Response DTOs
```java
// Request DTOs
public record RegisterSellerApiRequest(
    @NotBlank String sellerCode,
    @NotBlank String sellerName
) {}

public record UpdateStatusApiRequest(
    @NotNull SellerStatus status
) {}

public record SellerSearchApiRequest(
    String sellerCode,
    String sellerName,
    SellerStatus status
) {}

// Response DTOs
public record SellerApiResponse(
    Long sellerId,
    String sellerCode,
    String sellerName,
    SellerStatus status,
    Integer totalProductCount,
    LocalDateTime lastCrawledAt,
    LocalDateTime createdAt
) {}

public record SellerDetailApiResponse(
    SellerApiResponse seller,
    CrawlingStatsDto stats,
    ScheduleDto currentSchedule
) {}
```

---

## 📦 TASK-02: Schedule Management APIs

### API-02-1: 스케줄 생성 API
```java
@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    @PostMapping
    @Operation(summary = "크롤링 스케줄 생성")
    public ApiResponse<ScheduleApiResponse> createSchedule(
            @Valid @RequestBody CreateScheduleApiRequest request) {

        CreateScheduleCommand command = mapper.toCommand(request);
        ScheduleResponse response = createScheduleUseCase.execute(command);

        return ApiResponse.success(
            mapper.toApiResponse(response),
            "스케줄이 생성되었습니다"
        );
    }
}
```

### API-02-2: 스케줄 수정 API
```java
@PutMapping("/{scheduleId}")
@Operation(summary = "크롤링 스케줄 수정")
public ApiResponse<Void> updateSchedule(
        @PathVariable Long scheduleId,
        @Valid @RequestBody UpdateScheduleApiRequest request) {

    UpdateScheduleCommand command = mapper.toCommand(scheduleId, request);
    updateScheduleUseCase.execute(command);

    return ApiResponse.success("스케줄이 수정되었습니다");
}
```

### API-02-3: 스케줄 일시정지/재개 API
```java
@PostMapping("/{scheduleId}/suspend")
@Operation(summary = "스케줄 일시정지")
public ApiResponse<Void> suspendSchedule(@PathVariable Long scheduleId) {
    // 구현
    return ApiResponse.success("스케줄이 일시정지되었습니다");
}

@PostMapping("/{scheduleId}/resume")
@Operation(summary = "스케줄 재개")
public ApiResponse<Void> resumeSchedule(@PathVariable Long scheduleId) {
    // 구현
    return ApiResponse.success("스케줄이 재개되었습니다");
}
```

### Request/Response DTOs
```java
public record CreateScheduleApiRequest(
    @NotNull Long sellerId,
    @NotBlank @Pattern(regexp = CRON_PATTERN) String cronExpression
) {}

public record UpdateScheduleApiRequest(
    @NotBlank @Pattern(regexp = CRON_PATTERN) String cronExpression
) {}

public record ScheduleApiResponse(
    Long scheduleId,
    Long sellerId,
    String cronExpression,
    ScheduleStatus status,
    LocalDateTime nextExecutionTime,
    LocalDateTime lastExecutedAt
) {}
```

---

## 📦 TASK-03: Crawl Trigger APIs (EventBridge/Manual)

### API-03-1: 크롤링 시작 API (EventBridge 호출용)
```java
@RestController
@RequestMapping("/api/v1/crawl")
@RequiredArgsConstructor
public class CrawlTriggerController {

    @PostMapping("/start")
    @Operation(summary = "크롤링 시작 (EventBridge 트리거)")
    public ApiResponse<Void> startCrawling(
            @Valid @RequestBody CrawlTriggerApiRequest request,
            @RequestHeader("X-EventBridge-Token") String token) {

        // EventBridge 토큰 검증
        validateEventBridgeToken(token);

        InitiateCrawlingCommand command = mapper.toCommand(request);
        initiateCrawlingUseCase.execute(command);

        return ApiResponse.success("크롤링이 시작되었습니다");
    }
}
```

### API-03-2: 수동 크롤링 트리거 API
```java
@PostMapping("/manual/start")
@Operation(summary = "수동 크롤링 시작")
@PreAuthorize("hasRole('ADMIN')")
public ApiResponse<CrawlSessionApiResponse> startManualCrawling(
        @Valid @RequestBody ManualCrawlApiRequest request) {

    // 수동 크롤링 로직
    CrawlSessionResponse response = manualCrawlUseCase.execute(command);

    return ApiResponse.success(
        mapper.toApiResponse(response),
        "수동 크롤링이 시작되었습니다"
    );
}
```

### API-03-3: 크롤링 중지 API
```java
@PostMapping("/stop")
@Operation(summary = "진행 중인 크롤링 중지")
@PreAuthorize("hasRole('ADMIN')")
public ApiResponse<Void> stopCrawling(
        @Valid @RequestBody StopCrawlApiRequest request) {

    StopCrawlingCommand command = mapper.toCommand(request);
    stopCrawlingUseCase.execute(command);

    return ApiResponse.success("크롤링이 중지되었습니다");
}
```

### Request/Response DTOs
```java
public record CrawlTriggerApiRequest(
    @NotNull Long scheduleId
) {}

public record ManualCrawlApiRequest(
    @NotNull Long sellerId,
    @NotNull CrawlScope scope  // FULL, INCREMENTAL
) {}

public record StopCrawlApiRequest(
    @NotNull Long sellerId,
    String reason
) {}

public record CrawlSessionApiResponse(
    String sessionId,
    Long sellerId,
    CrawlStatus status,
    Integer totalTasks,
    LocalDateTime startedAt
) {}
```

---

## 📦 TASK-04: Task Management APIs

### API-04-1: 태스크 목록 조회 API
```java
@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    @GetMapping
    @Operation(summary = "크롤링 태스크 목록 조회")
    public ApiResponse<PageResponse<TaskApiResponse>> getTasks(
            @Valid TaskSearchApiRequest request,
            @PageableDefault(size = 50) Pageable pageable) {

        // 구현
        return ApiResponse.success(pageResponse);
    }
}
```

### API-04-2: 태스크 재시도 API
```java
@PostMapping("/{taskId}/retry")
@Operation(summary = "실패한 태스크 재시도")
@PreAuthorize("hasRole('ADMIN')")
public ApiResponse<Void> retryTask(@PathVariable Long taskId) {

    RetryTaskCommand command = new RetryTaskCommand(taskId);
    retryTaskUseCase.execute(command);

    return ApiResponse.success("태스크 재시도가 요청되었습니다");
}
```

### API-04-3: 태스크 일괄 재시도 API
```java
@PostMapping("/retry/batch")
@Operation(summary = "실패 태스크 일괄 재시도")
@PreAuthorize("hasRole('ADMIN')")
public ApiResponse<BatchRetryApiResponse> retryTasksBatch(
        @Valid @RequestBody BatchRetryApiRequest request) {

    BatchRetryCommand command = mapper.toCommand(request);
    BatchRetryResponse response = batchRetryUseCase.execute(command);

    return ApiResponse.success(mapper.toApiResponse(response));
}
```

### Request/Response DTOs
```java
public record TaskSearchApiRequest(
    Long sellerId,
    TaskType taskType,
    TaskStatus status,
    LocalDateTime fromDate,
    LocalDateTime toDate
) {}

public record BatchRetryApiRequest(
    @NotNull Long sellerId,
    TaskType taskType,
    @Max(100) Integer limit
) {}

public record TaskApiResponse(
    Long taskId,
    Long sellerId,
    TaskType taskType,
    TaskStatus status,
    String requestUrl,
    Integer pageNumber,
    Integer retryCount,
    LocalDateTime scheduledAt,
    LocalDateTime completedAt
) {}

public record BatchRetryApiResponse(
    Integer totalRequested,
    Integer successfullyQueued,
    List<Long> failedTaskIds
) {}
```

---

## 📦 TASK-05: Monitoring APIs

### API-05-1: 크롤링 통계 API
```java
@RestController
@RequestMapping("/api/v1/monitoring")
@RequiredArgsConstructor
public class MonitoringController {

    @GetMapping("/stats")
    @Operation(summary = "크롤링 통계 조회")
    public ApiResponse<CrawlingStatsApiResponse> getCrawlingStats(
            @Valid StatsApiRequest request) {

        StatsQuery query = mapper.toQuery(request);
        CrawlingStatsResponse response = calculateStatsUseCase.execute(query);

        return ApiResponse.success(mapper.toApiResponse(response));
    }
}
```

### API-05-2: 실시간 진행률 API
```java
@GetMapping("/progress")
@Operation(summary = "크롤링 진행률 조회")
public ApiResponse<ProgressApiResponse> getCrawlingProgress(
        @RequestParam Long sellerId) {

    ProgressQuery query = new ProgressQuery(sellerId);
    ProgressResponse response = getProgressUseCase.execute(query);

    return ApiResponse.success(mapper.toApiResponse(response));
}
```

### API-05-3: 일일 리포트 API
```java
@GetMapping("/reports/daily")
@Operation(summary = "일일 크롤링 리포트")
public ApiResponse<DailyReportApiResponse> getDailyReport(
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {

    DailyReportQuery query = new DailyReportQuery(date);
    DailyReportResponse response = generateReportUseCase.execute(query);

    return ApiResponse.success(mapper.toApiResponse(response));
}
```

### API-05-4: 에이전트 상태 API
```java
@GetMapping("/agents/status")
@Operation(summary = "유저 에이전트 상태 조회")
public ApiResponse<List<AgentStatusApiResponse>> getAgentStatus() {

    List<AgentStatusResponse> responses = getAgentStatusUseCase.execute();

    return ApiResponse.success(mapper.toApiResponses(responses));
}
```

### Request/Response DTOs
```java
public record StatsApiRequest(
    Long sellerId,
    @NotNull LocalDateTime fromDate,
    @NotNull LocalDateTime toDate,
    StatsGroupBy groupBy  // SELLER, TASK_TYPE, HOUR, DAY
) {}

public record CrawlingStatsApiResponse(
    Integer totalTasks,
    Integer successCount,
    Integer failedCount,
    Integer retryCount,
    Double successRate,
    Double averageProcessingTime,
    Map<String, Object> breakdown
) {}

public record ProgressApiResponse(
    Long sellerId,
    Integer totalProducts,
    Integer crawledProducts,
    Integer pendingTasks,
    Integer runningTasks,
    Integer completedTasks,
    Double progressPercentage,
    LocalDateTime estimatedCompletionTime
) {}

public record DailyReportApiResponse(
    LocalDate reportDate,
    List<SellerSummary> sellerSummaries,
    OverallStats overallStats,
    List<ErrorSummary> topErrors,
    List<PerformanceMetric> performanceMetrics
) {}

public record AgentStatusApiResponse(
    String agentId,
    String userAgentString,
    TokenStatus tokenStatus,
    Integer remainingRequests,
    LocalDateTime rateLimitResetAt,
    Integer successCount,
    Integer rateLimitCount
) {}
```

---

## 📦 TASK-06: Webhook APIs (외부 연동)

### API-06-1: 상품 변경 Webhook 수신 API
```java
@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    @PostMapping("/product-sync/callback")
    @Operation(summary = "상품 동기화 결과 콜백")
    public ApiResponse<Void> handleProductSyncCallback(
            @Valid @RequestBody ProductSyncCallbackRequest request,
            @RequestHeader("X-Signature") String signature) {

        // 시그니처 검증
        validateSignature(request, signature);

        ProcessSyncResultCommand command = mapper.toCommand(request);
        processSyncResultUseCase.execute(command);

        return ApiResponse.success("콜백이 처리되었습니다");
    }
}
```

### Request DTOs
```java
public record ProductSyncCallbackRequest(
    String requestId,
    String productId,
    SyncStatus status,
    String message,
    LocalDateTime processedAt
) {}
```

---

## 🎯 공통 컴포넌트

### GlobalExceptionHandler
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException e) {
        return ApiResponse.error(e.getErrorCode(), e.getMessage());
    }

    @ExceptionHandler(ValidationException.class)
    public ApiResponse<Void> handleValidationException(ValidationException e) {
        return ApiResponse.error(ErrorCode.INVALID_REQUEST, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception e) {
        log.error("Unexpected error", e);
        return ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR);
    }
}
```

### ApiResponse 표준화
```java
public record ApiResponse<T>(
    boolean success,
    T data,
    String message,
    ErrorInfo error,
    LocalDateTime timestamp
) {
    public static <T> ApiResponse<T> success(T data) {
        return success(data, null);
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message, null, LocalDateTime.now());
    }

    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>(true, null, message, null, LocalDateTime.now());
    }

    public static ApiResponse<Void> error(ErrorCode errorCode) {
        return error(errorCode, errorCode.getMessage());
    }

    public static ApiResponse<Void> error(ErrorCode errorCode, String message) {
        ErrorInfo error = new ErrorInfo(errorCode.getCode(), message);
        return new ApiResponse<>(false, null, null, error, LocalDateTime.now());
    }
}
```

### API Mapper
```java
@Mapper(componentModel = "spring")
public interface SellerApiMapper {

    RegisterSellerCommand toCommand(RegisterSellerApiRequest request);

    UpdateSellerStatusCommand toCommand(Long sellerId, UpdateStatusApiRequest request);

    SellerApiResponse toApiResponse(SellerResponse response);

    SellerDetailApiResponse toDetailApiResponse(SellerDetailResponse response);
}
```

---

## 🎯 API 보안 설정

### Security Configuration
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/crawl/start").hasIpAddress("10.0.0.0/8")  // EventBridge IP
                .requestMatchers("/api/v1/webhooks/**").permitAll()
                .requestMatchers("/api/v1/monitoring/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/api/v1/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/v1/crawl/start", "/api/v1/webhooks/**")
            );

        return http.build();
    }
}
```

---

## 🎯 개발 체크리스트

### Controller별 구현 사항
- [ ] Controller 클래스 생성
- [ ] Request/Response DTO 정의
- [ ] API Mapper 구현
- [ ] Validation 어노테이션
- [ ] OpenAPI 문서화 (@Operation)
- [ ] 권한 설정 (@PreAuthorize)

### 코딩 컨벤션 준수
- [ ] RESTful 원칙
- [ ] ApiResponse 표준화
- [ ] GlobalExceptionHandler 활용
- [ ] DTO 네이밍 규칙 (ApiRequest, ApiResponse)
- [ ] Javadoc 작성

### 테스트 요구사항
- [ ] Controller 단위 테스트 (MockMvc)
- [ ] Request 검증 테스트
- [ ] 예외 처리 테스트
- [ ] 통합 테스트 (@SpringBootTest)
- [ ] API 문서 생성 (RestDocs)

---

## 📊 예상 개발 일정

| API Category | 예상 시간 | 우선순위 | 병렬 가능 |
|-------------|----------|----------|----------|
| Seller Management | 4h | P0 | ✅ |
| Schedule Management | 4h | P0 | ✅ |
| Crawl Trigger | 3h | P0 | ✅ |
| Task Management | 4h | P1 | ✅ |
| Monitoring | 5h | P1 | ✅ |
| Webhooks | 2h | P1 | ✅ |

**총 예상 시간**: 22시간 (약 3일)

---

## 🔗 API 엔드포인트 요약

### Base URL: `/api/v1`

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | /sellers | 셀러 등록 | ADMIN |
| PATCH | /sellers/{id}/status | 셀러 상태 변경 | ADMIN |
| GET | /sellers | 셀러 목록 조회 | USER |
| GET | /sellers/{id} | 셀러 상세 조회 | USER |
| POST | /schedules | 스케줄 생성 | ADMIN |
| PUT | /schedules/{id} | 스케줄 수정 | ADMIN |
| POST | /crawl/start | 크롤링 시작 (EventBridge) | IP |
| POST | /crawl/manual/start | 수동 크롤링 | ADMIN |
| GET | /tasks | 태스크 목록 | USER |
| POST | /tasks/{id}/retry | 태스크 재시도 | ADMIN |
| GET | /monitoring/stats | 통계 조회 | USER |
| GET | /monitoring/progress | 진행률 조회 | USER |
| GET | /monitoring/reports/daily | 일일 리포트 | USER |

병렬 개발 가능 그룹:
- **Group 1**: Seller, Schedule, Monitoring APIs
- **Group 2**: Crawl Trigger, Task APIs
- **Group 3**: Webhooks