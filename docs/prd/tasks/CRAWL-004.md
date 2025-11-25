# CRAWL-004: REST API Layer 구현

**Epic**: Crawl Task Trigger
**Layer**: REST API Layer (Adapter-In)
**브랜치**: feature/CRAWL-004-rest-api
**Jira URL**: (sync-to-jira 후 추가)

---

## 📝 목적

EventBridge 및 내부 시스템이 호출할 수 있는 크롤링 트리거 API 엔드포인트를 구현합니다.

---

## 🎯 요구사항

### API 엔드포인트

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| POST | /api/v1/crawl/trigger | 크롤링 트리거 | IAM/API Key |
| GET | /api/v1/crawl/tasks/{taskId} | Task 조회 | JWT |
| GET | /api/v1/crawl/tasks | Task 목록 조회 | JWT |

### Controller

**CrawlTriggerController**:
- [ ] `POST /api/v1/crawl/trigger`
  - Request: TriggerCrawlTaskRequest
  - Response: CrawlTaskResponse (201 Created)
  - UseCase: TriggerCrawlTaskUseCase

**CrawlTaskController**:
- [ ] `GET /api/v1/crawl/tasks/{taskId}`
  - Response: CrawlTaskDetailResponse (200 OK)
  - UseCase: GetCrawlTaskUseCase

- [ ] `GET /api/v1/crawl/tasks`
  - Query Params: schedulerId, status, page, size
  - Response: PageResponse<CrawlTaskSummaryResponse> (200 OK)
  - UseCase: ListCrawlTasksUseCase

### Request DTO

**TriggerCrawlTaskRequest**:
```java
public record TriggerCrawlTaskRequest(
    @NotNull(message = "crawlSchedulerId는 필수입니다")
    Long crawlSchedulerId
) {}
```

**ListCrawlTasksRequest**:
```java
public record ListCrawlTasksRequest(
    Long crawlSchedulerId,
    String status,
    @Min(0) Integer page,
    @Min(1) @Max(100) Integer size
) {}
```

### Response DTO

**CrawlTaskResponse**:
```java
public record CrawlTaskResponse(
    String crawlTaskId,
    String status,
    String requestUrl,
    LocalDateTime createdAt
) {}
```

**CrawlTaskDetailResponse**:
```java
public record CrawlTaskDetailResponse(
    String crawlTaskId,
    Long crawlSchedulerId,
    Long sellerId,
    String requestUrl,
    String status,
    Integer retryCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
```

### Mapper

**CrawlTaskRestMapper**:
- [ ] `toCommand(TriggerCrawlTaskRequest): TriggerCrawlTaskCommand`
- [ ] `toResponse(CrawlTaskResponse): CrawlTaskResponse` (Application → REST)

### Error Handling

**HTTP Status Code**:
| Status | Error Code | Description |
|--------|------------|-------------|
| 400 | INVALID_REQUEST | 요청 형식 오류 |
| 404 | SCHEDULER_NOT_FOUND | Scheduler 존재하지 않음 |
| 404 | CRAWL_TASK_NOT_FOUND | Task 존재하지 않음 |
| 409 | SCHEDULER_NOT_ACTIVE | Scheduler 비활성 상태 |
| 409 | DUPLICATE_TASK_EXISTS | 진행 중인 Task 존재 |

**ErrorMapper**:
- [ ] SchedulerNotFoundException → 404
- [ ] SchedulerNotActiveException → 409
- [ ] DuplicateTaskException → 409
- [ ] CrawlTaskNotFoundException → 404

### 인증/인가

**EventBridge 호출** (/api/v1/crawl/trigger):
- [ ] IAM Role 기반 인증 (AWS Signature V4)
- [ ] 또는 API Key 헤더 검증

**내부 호출** (/api/v1/crawl/tasks/**):
- [ ] JWT 인증 (기존 인증 체계 활용)

### API 문서화

**Spring REST Docs**:
- [ ] 트리거 API 문서
- [ ] Task 조회 API 문서
- [ ] 에러 응답 문서

---

## ⚠️ 제약사항

### Zero-Tolerance 규칙
- [ ] RESTful 설계 원칙 준수
- [ ] @Valid 필수 (Bean Validation)
- [ ] 일관된 Error Response 형식 (ErrorInfo)
- [ ] Lombok 금지 (DTO도 Record 사용)

### 테스트 규칙
- [ ] ArchUnit 테스트 필수 (ControllerArchTest)
- [ ] MockMvc 테스트 (WebMvcTest)
- [ ] REST Docs 생성

---

## ✅ 완료 조건

- [ ] Controller 구현 완료
- [ ] Request/Response DTO 구현 완료
- [ ] Mapper 구현 완료
- [ ] ErrorMapper 구현 완료
- [ ] 인증 설정 완료
- [ ] MockMvc Test 통과
- [ ] REST Docs 생성 완료
- [ ] ArchUnit Test 통과
- [ ] Zero-Tolerance 규칙 준수
- [ ] 코드 리뷰 승인
- [ ] PR 머지 완료

---

## 🔗 관련 문서

- PRD: docs/prd/crawl-task-trigger.md
- Plan: docs/prd/plans/CRAWL-004-rest-api-plan.md (create-plan 후 생성)
- Jira: (sync-to-jira 후 추가)
