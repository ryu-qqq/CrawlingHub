# CRAWL-004: REST API Layer 구현

**Epic**: Crawl Task Trigger 시스템
**Layer**: REST API Layer (Adapter-In)
**브랜치**: feature/CRAWL-004-rest-api
**의존성**: CRAWL-002 (Application Layer) 완료 후 시작
**Jira URL**: (sync-to-jira 후 추가)

---

## 📝 목적

CrawlTask UseCase를 REST API로 노출하는 Controller와 DTO를 구현한다.
Thin Controller 패턴을 준수하여 HTTP 처리만 담당하고, 비즈니스 로직은 UseCase에 위임한다.
CQRS 패턴에 따라 Command/Query Controller를 분리한다.

---

## 🎯 요구사항

### Controller (Command)
- [ ] **CrawlTaskCommandController**
  - `@RestController`
  - `@RequestMapping("/api/v1/crawl/tasks")`
  - `POST /trigger` - 크롤링 태스크 트리거
  - TriggerCrawlTaskUseCase 주입
  - CrawlTaskApiMapper로 DTO 변환
  - `ResponseEntity<ApiResponse<CrawlTaskApiResponse>>` 반환
  - HTTP Status 201 Created

### Controller (Query)
- [ ] **CrawlTaskQueryController**
  - `@RestController`
  - `@RequestMapping("/api/v1/crawl/tasks")`
  - `GET /{taskId}` - 단건 조회
  - `GET /` - 목록 조회 (Query Parameter)
  - GetCrawlTaskUseCase, ListCrawlTasksUseCase 주입
  - `ResponseEntity<ApiResponse<>>`, `ResponseEntity<PageApiResponse<>>` 반환

### API DTO (Request)
- [ ] **TriggerCrawlTaskApiRequest**
  - Record 타입
  - `@NotNull` crawlScheduleId

- [ ] **ListCrawlTasksApiRequest**
  - Record 타입
  - crawlScheduleId (필수)
  - status (선택)
  - page, size (기본값)

### API DTO (Response)
- [ ] **CrawlTaskApiResponse**
  - Record 타입
  - crawlTaskId, crawlScheduleId, sellerId, requestUrl, status, taskType, retryCount, createdAt

- [ ] **CrawlTaskDetailApiResponse**
  - Record 타입
  - CrawlTaskApiResponse 필드 + updatedAt, endpoint 상세

### Mapper
- [ ] **CrawlTaskApiMapper**
  - `TriggerCrawlTaskCommand toCommand(TriggerCrawlTaskApiRequest)`
  - `ListCrawlTasksQuery toQuery(ListCrawlTasksApiRequest)`
  - `CrawlTaskApiResponse toApiResponse(CrawlTaskResponse)`
  - `CrawlTaskDetailApiResponse toDetailApiResponse(CrawlTaskDetailResponse)`
  - `PageApiResponse<CrawlTaskApiResponse> toPageApiResponse(PageResponse<CrawlTaskResponse>)`

### Error Handling
- [ ] **CrawlTaskApiErrorMapper**
  - Domain Exception → HTTP Status 매핑
  - CrawlTaskNotFoundException → 404
  - InvalidCrawlTaskStateException → 409
  - DuplicateCrawlTaskException → 409
  - SchedulerNotActiveException → 409

---

## ⚠️ 제약사항

### Zero-Tolerance 규칙
- [ ] **Lombok 금지** - Record 타입 사용
- [ ] **Thin Controller** - HTTP 처리만, UseCase에 위임
- [ ] **Bean Validation** - `@Valid` + 제약 조건
- [ ] **RESTful URI** - 리소스 기반 명사형
- [ ] **Domain 직접 노출 금지** - API 전용 DTO 사용

### 테스트 규칙
- [ ] Controller Unit 테스트 (MockMvc)
- [ ] Mapper 테스트
- [ ] Error Handling 테스트
- [ ] REST Docs 작성
- [ ] TestFixture 사용 필수
- [ ] 테스트 커버리지 > 80%

---

## 📦 패키지 구조

```
adapter-in/rest-api/
└─ crawl/
   └─ task/
      ├─ controller/
      │  ├─ CrawlTaskCommandController.java
      │  └─ CrawlTaskQueryController.java
      ├─ dto/
      │  ├─ command/
      │  │   └─ TriggerCrawlTaskApiRequest.java
      │  ├─ query/
      │  │   └─ ListCrawlTasksApiRequest.java
      │  └─ response/
      │      ├─ CrawlTaskApiResponse.java
      │      └─ CrawlTaskDetailApiResponse.java
      ├─ mapper/
      │  └─ CrawlTaskApiMapper.java
      └─ error/
         └─ CrawlTaskApiErrorMapper.java
```

---

## 📋 API 명세

### POST /api/v1/crawl/tasks/trigger
**크롤링 태스크 트리거**

**Request Body**:
```json
{
  "crawlScheduleId": 123
}
```

**Response (201 Created)**:
```json
{
  "success": true,
  "data": {
    "crawlTaskId": 1,
    "crawlScheduleId": 123,
    "sellerId": 456,
    "requestUrl": "https://m.web.mustit.co.kr/mustit-api/...",
    "status": "WAITING",
    "taskType": "META",
    "retryCount": 0,
    "createdAt": "2025-11-25T10:00:00"
  },
  "error": null
}
```

### GET /api/v1/crawl/tasks/{taskId}
**태스크 단건 조회**

**Response (200 OK)**:
```json
{
  "success": true,
  "data": {
    "crawlTaskId": 1,
    "crawlScheduleId": 123,
    "sellerId": 456,
    "requestUrl": "https://m.web.mustit.co.kr/mustit-api/...",
    "status": "RUNNING",
    "taskType": "META",
    "retryCount": 0,
    "createdAt": "2025-11-25T10:00:00",
    "updatedAt": "2025-11-25T10:05:00"
  },
  "error": null
}
```

### GET /api/v1/crawl/tasks
**태스크 목록 조회**

**Query Parameters**:
- `crawlScheduleId` (필수)
- `status` (선택)
- `page` (기본: 0)
- `size` (기본: 20)

**Response (200 OK)**:
```json
{
  "success": true,
  "data": {
    "content": [...],
    "page": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5
  },
  "error": null
}
```

### Error Responses

| Status | Error Code | Description |
|--------|------------|-------------|
| 404 | CRAWL_TASK_NOT_FOUND | Task를 찾을 수 없음 |
| 409 | SCHEDULER_NOT_ACTIVE | Scheduler가 비활성 상태 |
| 409 | DUPLICATE_CRAWL_TASK | 이미 진행 중인 Task 존재 |

---

## ✅ 완료 조건

- [ ] 모든 요구사항 구현 완료
- [ ] 모든 Unit 테스트 통과
- [ ] ArchUnit 테스트 통과
- [ ] Zero-Tolerance 규칙 준수 확인
- [ ] REST Docs 생성
- [ ] 코드 리뷰 승인
- [ ] PR 머지 완료

---

## 🔗 관련 문서

- PRD: [docs/prd/tasks/crawl-task-trigger.md](./crawl-task-trigger.md)
- Plan: docs/prd/plans/CRAWL-004-rest-api-plan.md (create-plan 후 생성)
- REST API Guide: [docs/coding_convention/01-adapter-in-layer/rest-api/rest-api-guide.md](../../coding_convention/01-adapter-in-layer/rest-api/rest-api-guide.md)
- Jira: (sync-to-jira 후 추가)

---

## 🧪 TDD 체크리스트

### Controller 테스트 (MockMvc)
- [ ] `test: POST /trigger 정상 요청 시 201 반환`
- [ ] `test: POST /trigger crawlScheduleId null 시 400 반환`
- [ ] `test: POST /trigger 중복 Task 시 409 반환`
- [ ] `test: POST /trigger Scheduler 비활성 시 409 반환`
- [ ] `test: GET /{taskId} 정상 요청 시 200 반환`
- [ ] `test: GET /{taskId} 존재하지 않는 ID 시 404 반환`
- [ ] `test: GET / 목록 조회 정상 시 200 반환`
- [ ] `test: GET / 페이징 파라미터 동작 확인`

### Mapper 테스트
- [ ] `test: CrawlTaskApiMapper.toCommand() 변환`
- [ ] `test: CrawlTaskApiMapper.toQuery() 변환`
- [ ] `test: CrawlTaskApiMapper.toApiResponse() 변환`
- [ ] `test: CrawlTaskApiMapper.toDetailApiResponse() 변환`
- [ ] `test: CrawlTaskApiMapper.toPageApiResponse() 변환`

### Error Handling 테스트
- [ ] `test: CrawlTaskNotFoundException → 404 매핑`
- [ ] `test: InvalidCrawlTaskStateException → 409 매핑`
- [ ] `test: DuplicateCrawlTaskException → 409 매핑`

### REST Docs
- [ ] `test: POST /trigger API 문서화`
- [ ] `test: GET /{taskId} API 문서화`
- [ ] `test: GET / API 문서화`
