# Adapter Layer 단위 테스트 대상 목록

> 작성일: 2024-11-27
> 목표 커버리지: 70% 이상

---

## 개요

Adapter Layer는 **외부 시스템과의 통신**을 담당하는 레이어입니다.
헥사고날 아키텍처에서 **Adapter-In**은 외부 요청을 받아 Application으로 전달하고,
**Adapter-Out**은 Application의 요청을 외부 시스템(DB, Redis, AWS 등)으로 전달합니다.

테스트는 **Controller → API Mapper → ErrorMapper → Listener** (Adapter-In),
**Persistence Adapter → JPA Entity Mapper → Cache Adapter** (Adapter-Out) 순서로 작성합니다.

### 현재 테스트 현황

| 모듈 | 소스 파일 | 테스트 파일 | 커버리지 |
|------|----------|------------|---------|
| adapter-in/rest-api | 53개 | 0개 | ❌ 0% |
| adapter-in/sqs-listener | 5개 | 0개 | ❌ 0% |
| adapter-out/persistence-mysql | 60개+ | 0개 | ❌ 0% |
| adapter-out/persistence-redis | 5개 | 0개 | ❌ 0% |
| adapter-out/aws-sqs | 4개 | 0개 | ❌ 0% |
| adapter-out/aws-eventbridge | 3개 | 0개 | ❌ 0% |
| adapter-out/http-client | 4개 | 0개 | ❌ 0% |

---

# Part 1: Adapter-In (입력 어댑터)

---

## 1. REST API Controller 테스트 (P0 - 최우선)

> **테스트 전략**: MockMvc + @WebMvcTest + Mock UseCase
> **문서 참조**: `docs/coding_convention/01-adapter-in-layer/rest-api/controller/controller-test-guide.md`

### 1.1 Seller Controllers

#### SellerCommandController

**파일**: `adapter-in/rest-api/.../seller/controller/SellerCommandController.java`
**테스트 파일**: `SellerCommandControllerTest.java`
**우선순위**: 🔴 P0

| 테스트 항목 | 엔드포인트 | 검증 포인트 | 상태 |
|------------|-----------|------------|------|
| 셀러 등록 | POST /api/v1/sellers | 201 Created, Location Header, Request Validation | ⬜ |
| 셀러 수정 | PUT /api/v1/sellers/{id} | 200 OK, Request Body 변환 | ⬜ |
| 셀러 상태 변경 | PATCH /api/v1/sellers/{id}/status | 200 OK, 상태 전환 검증 | ⬜ |
| 유효성 검증 실패 | POST /api/v1/sellers | 400 Bad Request, 에러 메시지 | ⬜ |

#### SellerQueryController

**파일**: `adapter-in/rest-api/.../seller/controller/SellerQueryController.java`
**테스트 파일**: `SellerQueryControllerTest.java`
**우선순위**: 🔴 P0

| 테스트 항목 | 엔드포인트 | 검증 포인트 | 상태 |
|------------|-----------|------------|------|
| 셀러 단건 조회 | GET /api/v1/sellers/{id} | 200 OK, Response 구조 | ⬜ |
| 셀러 목록 조회 | GET /api/v1/sellers | 200 OK, 페이징, 필터링 | ⬜ |
| 존재하지 않는 셀러 | GET /api/v1/sellers/{id} | 404 Not Found | ⬜ |

---

### 1.2 Schedule Controllers

#### CrawlSchedulerCommandController

**파일**: `adapter-in/rest-api/.../schedule/controller/CrawlSchedulerCommandController.java`
**테스트 파일**: `CrawlSchedulerCommandControllerTest.java`
**우선순위**: 🔴 P0

| 테스트 항목 | 엔드포인트 | 검증 포인트 | 상태 |
|------------|-----------|------------|------|
| 스케줄러 등록 | POST /api/v1/schedules | 201 Created, Location Header | ⬜ |
| 스케줄러 수정 | PUT /api/v1/schedules/{id} | 200 OK, Request 변환 | ⬜ |
| Cron 표현식 검증 | POST /api/v1/schedules | 400 Bad Request (잘못된 Cron) | ⬜ |

#### CrawlSchedulerQueryController

**파일**: `adapter-in/rest-api/.../schedule/controller/CrawlSchedulerQueryController.java`
**테스트 파일**: `CrawlSchedulerQueryControllerTest.java`
**우선순위**: 🔴 P0

| 테스트 항목 | 엔드포인트 | 검증 포인트 | 상태 |
|------------|-----------|------------|------|
| 스케줄러 단건 조회 | GET /api/v1/schedules/{id} | 200 OK, Response 구조 | ⬜ |
| 스케줄러 목록 조회 | GET /api/v1/schedules | 200 OK, 페이징 | ⬜ |

---

### 1.3 Task Controllers

#### CrawlTaskQueryController

**파일**: `adapter-in/rest-api/.../task/controller/CrawlTaskQueryController.java`
**테스트 파일**: `CrawlTaskQueryControllerTest.java`
**우선순위**: 🟡 P1

| 테스트 항목 | 엔드포인트 | 검증 포인트 | 상태 |
|------------|-----------|------------|------|
| 태스크 단건 조회 | GET /api/v1/tasks/{id} | 200 OK, Response 구조 | ⬜ |
| 태스크 목록 조회 | GET /api/v1/tasks | 200 OK, 필터링 (status, schedulerId) | ⬜ |

---

### 1.4 Execution Controllers

#### CrawlExecutionQueryController

**파일**: `adapter-in/rest-api/.../execution/controller/CrawlExecutionQueryController.java`
**테스트 파일**: `CrawlExecutionQueryControllerTest.java`
**우선순위**: 🟡 P1

| 테스트 항목 | 엔드포인트 | 검증 포인트 | 상태 |
|------------|-----------|------------|------|
| 실행 단건 조회 | GET /api/v1/executions/{id} | 200 OK, Response 구조 | ⬜ |
| 실행 목록 조회 | GET /api/v1/executions | 200 OK, 필터링 (taskId, status, from/to) | ⬜ |

---

### 1.5 UserAgent Controllers

#### UserAgentQueryController

**파일**: `adapter-in/rest-api/.../useragent/controller/UserAgentQueryController.java`
**테스트 파일**: `UserAgentQueryControllerTest.java`
**우선순위**: 🟢 P2

| 테스트 항목 | 엔드포인트 | 검증 포인트 | 상태 |
|------------|-----------|------------|------|
| Pool 상태 조회 | GET /api/v1/user-agents/pool-status | 200 OK, Pool 상태 | ⬜ |

#### UserAgentCommandController

**파일**: `adapter-in/rest-api/.../useragent/controller/UserAgentCommandController.java`
**테스트 파일**: `UserAgentCommandControllerTest.java`
**우선순위**: 🟢 P2

| 테스트 항목 | 엔드포인트 | 검증 포인트 | 상태 |
|------------|-----------|------------|------|
| UserAgent 복구 | POST /api/v1/user-agents/recover | 200 OK, 복구 결과 | ⬜ |

---

## 2. API Mapper 테스트 (P1 - 중요)

> **테스트 전략**: 단위 테스트 (순수 Java 테스트)
> **문서 참조**: `docs/coding_convention/01-adapter-in-layer/rest-api/mapper/mapper-test-guide.md`

### 2.1 Command API Mappers

| Mapper | 파일 | 테스트 포인트 | 상태 |
|--------|------|--------------|------|
| SellerCommandApiMapper | `seller/mapper/SellerCommandApiMapper.java` | ApiRequest → Command DTO 변환 | ⬜ |
| CrawlSchedulerCommandApiMapper | `schedule/mapper/CrawlSchedulerCommandApiMapper.java` | ApiRequest → Command DTO 변환 | ⬜ |

### 2.2 Query API Mappers

| Mapper | 파일 | 테스트 포인트 | 상태 |
|--------|------|--------------|------|
| SellerQueryApiMapper | `seller/mapper/SellerQueryApiMapper.java` | Query 파라미터 → Query DTO, Response 변환 | ⬜ |
| CrawlSchedulerQueryApiMapper | `schedule/mapper/CrawlSchedulerQueryApiMapper.java` | Query → Query DTO, Response 변환 | ⬜ |
| CrawlTaskQueryApiMapper | `task/mapper/CrawlTaskQueryApiMapper.java` | Query → Query DTO, Response 변환 | ⬜ |
| CrawlExecutionQueryApiMapper | `execution/mapper/CrawlExecutionQueryApiMapper.java` | Query → Query DTO, Response 변환 | ⬜ |
| UserAgentApiMapper | `useragent/mapper/UserAgentApiMapper.java` | Response 변환 | ⬜ |

### 2.3 Mapper 테스트 예시

```java
@Test
void toCommand_정상_변환() {
    // given
    RegisterSellerApiRequest request = new RegisterSellerApiRequest(
        "test-seller", "https://example.com"
    );

    // when
    RegisterSellerCommand command = mapper.toCommand(request);

    // then
    assertThat(command.sellerName()).isEqualTo("test-seller");
    assertThat(command.siteUrl()).isEqualTo("https://example.com");
}
```

---

## 3. Error Mapper 테스트 (P1 - 중요)

> **테스트 전략**: 단위 테스트
> **문서 참조**: `docs/coding_convention/01-adapter-in-layer/rest-api/error/error-mapper-implementation-guide.md`

### 3.1 도메인별 Error Mappers

| ErrorMapper | 파일 | PREFIX | 테스트 포인트 | 상태 |
|-------------|------|--------|--------------|------|
| SellerErrorMapper | `seller/error/SellerErrorMapper.java` | `SELLER-` | supports(), HTTP 상태 매핑, I18N 메시지 | ⬜ |
| CrawlSchedulerErrorMapper | `schedule/error/CrawlSchedulerErrorMapper.java` | `SCHEDULE-` | supports(), HTTP 상태 매핑, I18N 메시지 | ⬜ |
| CrawlTaskErrorMapper | `task/error/CrawlTaskErrorMapper.java` | `CRAWL-TASK-` | supports(), HTTP 상태 매핑 | ⬜ |
| CrawlExecutionErrorMapper | `execution/error/CrawlExecutionErrorMapper.java` | `CRAWL-EXEC-` | supports(), HTTP 상태 매핑 | ⬜ |

### 3.2 ErrorMapper 테스트 예시

```java
@Test
void supports_SELLER_PREFIX_true() {
    assertThat(errorMapper.supports("SELLER-001")).isTrue();
    assertThat(errorMapper.supports("SELLER-002")).isTrue();
    assertThat(errorMapper.supports("OTHER-001")).isFalse();
}

@Test
void map_SELLER_001_NOT_FOUND() {
    // given
    DomainException exception = new SellerNotFoundException(1L);

    // when
    MappedError error = errorMapper.map(exception, Locale.KOREAN);

    // then
    assertThat(error.status()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(error.type()).hasToString("https://api.example.com/problems/seller/seller-001");
}
```

---

## 4. SQS Listener 테스트 (P1 - 중요)

> **테스트 전략**: 통합 테스트 (LocalStack 또는 Mock SQS)
> **주의사항**: 비동기 메시지 처리, 멱등성 검증

### 4.1 SQS Listeners

| Listener | 파일 | 테스트 포인트 | 상태 |
|----------|------|--------------|------|
| EventBridgeTriggerSqsListener | `sqs/listener/EventBridgeTriggerSqsListener.java` | EventBridge 이벤트 수신, UseCase 위임 | ⬜ |
| CrawlTaskSqsListener | `sqs/listener/CrawlTaskSqsListener.java` | Task 메시지 수신, 실행 트리거 | ⬜ |
| CrawlTaskDlqListener | `sqs/listener/CrawlTaskDlqListener.java` | DLQ 메시지 처리, 재시도/로깅 | ⬜ |

### 4.2 SQS Listener 테스트 예시

```java
@Test
void handleMessage_정상_처리() {
    // given
    String messageBody = "{\"schedulerId\": 1, \"triggeredAt\": \"...\"}";

    // when
    listener.handleMessage(messageBody);

    // then
    verify(triggerCrawlTaskUseCase).execute(any(TriggerCrawlTaskCommand.class));
}

@Test
void handleMessage_중복_메시지_멱등성() {
    // given
    String messageBody = "{\"idempotencyKey\": \"unique-key\"}";

    // when - 동일 메시지 2회 처리
    listener.handleMessage(messageBody);
    listener.handleMessage(messageBody);

    // then - UseCase는 1회만 호출
    verify(useCase, times(1)).execute(any());
}
```

---

## 5. Common 컴포넌트 테스트 (P2 - 권장)

### 5.1 GlobalExceptionHandler

**파일**: `adapter-in/rest-api/.../common/handler/GlobalExceptionHandler.java`
**테스트 파일**: `GlobalExceptionHandlerTest.java`
**우선순위**: 🟢 P2

| 테스트 항목 | 예외 타입 | 검증 포인트 | 상태 |
|------------|----------|------------|------|
| 도메인 예외 처리 | DomainException | RFC 7807 형식, ErrorMapper 선택 | ⬜ |
| Validation 예외 | MethodArgumentNotValidException | 400 Bad Request, 필드 에러 | ⬜ |
| 일반 예외 처리 | Exception | 500 Internal Server Error | ⬜ |

### 5.2 ErrorMapperRegistry

**파일**: `adapter-in/rest-api/.../common/mapper/ErrorMapperRegistry.java`
**테스트 파일**: `ErrorMapperRegistryTest.java`
**우선순위**: 🟢 P2

| 테스트 항목 | 검증 포인트 | 상태 |
|------------|------------|------|
| Mapper 조회 | PREFIX로 올바른 Mapper 선택 | ⬜ |
| 미등록 예외 | 기본 Mapper 반환 또는 예외 | ⬜ |

---

# Part 2: Adapter-Out (출력 어댑터)

---

## 6. Persistence MySQL Adapter 테스트 (P0 - 최우선)

> **테스트 전략**: 통합 테스트 (@DataJpaTest + Testcontainers MySQL)
> **문서 참조**: `docs/coding_convention/04-persistence-layer/mysql/adapter/`

### 6.1 Seller Adapters

#### SellerCommandAdapter

**파일**: `adapter-out/persistence-mysql/.../seller/adapter/SellerCommandAdapter.java`
**테스트 파일**: `SellerCommandAdapterIntegrationTest.java`
**우선순위**: 🔴 P0

| 테스트 항목 | 메서드 | 검증 포인트 | 상태 |
|------------|--------|------------|------|
| 저장 | `save(Seller)` | Entity 변환, ID 반환 | ⬜ |
| 벌크 저장 | `saveAll(List<Seller>)` | 배치 저장 | ⬜ |

#### SellerQueryAdapter

**파일**: `adapter-out/persistence-mysql/.../seller/adapter/SellerQueryAdapter.java`
**테스트 파일**: `SellerQueryAdapterIntegrationTest.java`
**우선순위**: 🔴 P0

| 테스트 항목 | 메서드 | 검증 포인트 | 상태 |
|------------|--------|------------|------|
| ID 조회 | `findById(SellerId)` | Domain 객체 복원 | ⬜ |
| 이름 조회 | `findBySellerName(String)` | Optional 처리 | ⬜ |
| 검색 | `findByCriteria(SellerCriteria)` | 동적 쿼리, 페이징 | ⬜ |
| 활성 셀러 조회 | `findActiveSellerIds()` | 상태 필터링 | ⬜ |

---

### 6.2 Schedule Adapters

#### CrawlSchedulerCommandAdapter

**파일**: `adapter-out/persistence-mysql/.../schedule/adapter/CrawlSchedulerCommandAdapter.java`
**테스트 파일**: `CrawlSchedulerCommandAdapterIntegrationTest.java`
**우선순위**: 🔴 P0

| 테스트 항목 | 메서드 | 검증 포인트 | 상태 |
|------------|--------|------------|------|
| 저장 | `save(CrawlScheduler)` | Entity 변환, ID 반환 | ⬜ |

#### CrawlSchedulerQueryAdapter

**파일**: `adapter-out/persistence-mysql/.../schedule/adapter/CrawlSchedulerQueryAdapter.java`
**테스트 파일**: `CrawlSchedulerQueryAdapterIntegrationTest.java`
**우선순위**: 🔴 P0

| 테스트 항목 | 메서드 | 검증 포인트 | 상태 |
|------------|--------|------------|------|
| ID 조회 | `findById(CrawlSchedulerId)` | Domain 복원 | ⬜ |
| 셀러별 조회 | `findBySellerIdAndStatus(...)` | 동적 쿼리 | ⬜ |
| 검색 | `findByCriteria(CrawlSchedulerCriteria)` | 페이징, 필터링 | ⬜ |

#### CrawlSchedulerHistoryCommandAdapter

**파일**: `adapter-out/persistence-mysql/.../schedule/adapter/CrawlSchedulerHistoryCommandAdapter.java`
**테스트 파일**: `CrawlSchedulerHistoryCommandAdapterIntegrationTest.java`
**우선순위**: 🟡 P1

| 테스트 항목 | 메서드 | 검증 포인트 | 상태 |
|------------|--------|------------|------|
| 히스토리 저장 | `save(CrawlSchedulerHistory)` | 히스토리 기록 | ⬜ |

#### CrawlSchedulerOutBox Adapters

**파일**: `adapter-out/persistence-mysql/.../schedule/adapter/CrawlSchedulerOutBox*Adapter.java`
**우선순위**: 🟡 P1

| Adapter | 테스트 포인트 | 상태 |
|---------|--------------|------|
| CrawlSchedulerOutBoxCommandAdapter | Outbox 저장, 상태 업데이트 | ⬜ |
| CrawlSchedulerOutBoxQueryAdapter | Pending Outbox 조회 | ⬜ |

---

### 6.3 Task Adapters

#### CrawlTaskCommandAdapter

**파일**: `adapter-out/persistence-mysql/.../task/adapter/CrawlTaskCommandAdapter.java`
**테스트 파일**: `CrawlTaskCommandAdapterIntegrationTest.java`
**우선순위**: 🔴 P0

| 테스트 항목 | 메서드 | 검증 포인트 | 상태 |
|------------|--------|------------|------|
| 저장 | `save(CrawlTask)` | Entity 변환, Outbox 포함 | ⬜ |
| 상태 업데이트 | `updateStatus(CrawlTask)` | 상태 전환 | ⬜ |

#### CrawlTaskQueryAdapter

**파일**: `adapter-out/persistence-mysql/.../task/adapter/CrawlTaskQueryAdapter.java`
**테스트 파일**: `CrawlTaskQueryAdapterIntegrationTest.java`
**우선순위**: 🔴 P0

| 테스트 항목 | 메서드 | 검증 포인트 | 상태 |
|------------|--------|------------|------|
| ID 조회 | `findById(CrawlTaskId)` | Domain 복원, Outbox 포함 | ⬜ |
| Idempotency Key 조회 | `findByIdempotencyKey(String)` | 중복 체크 | ⬜ |
| 검색 | `findByCriteria(CrawlTaskCriteria)` | 동적 쿼리 | ⬜ |

#### CrawlTaskOutbox Adapters

**파일**: `adapter-out/persistence-mysql/.../task/adapter/CrawlTaskOutbox*Adapter.java`
**우선순위**: 🟡 P1

| Adapter | 테스트 포인트 | 상태 |
|---------|--------------|------|
| CrawlTaskOutboxCommandAdapter | Outbox 저장, SENT 마킹 | ⬜ |
| CrawlTaskOutboxQueryAdapter | Pending Outbox 조회, 순서 보장 | ⬜ |

---

### 6.4 Execution Adapters

#### CrawlExecutionCommandAdapter

**파일**: `adapter-out/persistence-mysql/.../execution/adapter/CrawlExecutionCommandAdapter.java`
**테스트 파일**: `CrawlExecutionCommandAdapterIntegrationTest.java`
**우선순위**: 🟡 P1

| 테스트 항목 | 메서드 | 검증 포인트 | 상태 |
|------------|--------|------------|------|
| 저장 | `save(CrawlExecution)` | Entity 변환 | ⬜ |
| 완료 업데이트 | `updateCompletion(CrawlExecution)` | 결과, duration | ⬜ |

#### CrawlExecutionQueryAdapter

**파일**: `adapter-out/persistence-mysql/.../execution/adapter/CrawlExecutionQueryAdapter.java`
**테스트 파일**: `CrawlExecutionQueryAdapterIntegrationTest.java`
**우선순위**: 🟡 P1

| 테스트 항목 | 메서드 | 검증 포인트 | 상태 |
|------------|--------|------------|------|
| ID 조회 | `findById(CrawlExecutionId)` | Domain 복원 | ⬜ |
| 검색 | `findByCriteria(CrawlExecutionCriteria)` | 시간 필터, 페이징 | ⬜ |

---

### 6.5 UserAgent Adapters

#### UserAgentCommandAdapter

**파일**: `adapter-out/persistence-mysql/.../useragent/adapter/UserAgentCommandAdapter.java`
**테스트 파일**: `UserAgentCommandAdapterIntegrationTest.java`
**우선순위**: 🟢 P2

| 테스트 항목 | 메서드 | 검증 포인트 | 상태 |
|------------|--------|------------|------|
| 저장 | `save(UserAgent)` | Entity 변환 | ⬜ |
| HealthScore 업데이트 | `updateHealthScore(...)` | 점수 업데이트 | ⬜ |

#### UserAgentQueryAdapter

**파일**: `adapter-out/persistence-mysql/.../useragent/adapter/UserAgentQueryAdapter.java`
**테스트 파일**: `UserAgentQueryAdapterIntegrationTest.java`
**우선순위**: 🟢 P2

| 테스트 항목 | 메서드 | 검증 포인트 | 상태 |
|------------|--------|------------|------|
| 활성 조회 | `findActiveUserAgents()` | 활성 상태 필터 | ⬜ |
| ID 조회 | `findById(UserAgentId)` | Domain 복원 | ⬜ |

---

### 6.6 Product Adapters

#### CrawledRawCommandAdapter

**파일**: `adapter-out/persistence-mysql/.../product/adapter/CrawledRawCommandAdapter.java`
**우선순위**: 🟡 P1

| 테스트 항목 | 메서드 | 검증 포인트 | 상태 |
|------------|--------|------------|------|
| 저장 | `save(CrawledRaw)` | Raw 데이터 저장 | ⬜ |

#### CrawledProductCommandAdapter

**파일**: `adapter-out/persistence-mysql/.../product/adapter/CrawledProductCommandAdapter.java`
**우선순위**: 🟡 P1

| 테스트 항목 | 메서드 | 검증 포인트 | 상태 |
|------------|--------|------------|------|
| 저장 | `save(CrawledProduct)` | Product 저장 | ⬜ |

---

## 7. JPA Entity Mapper 테스트 (P1 - 중요)

> **테스트 전략**: 단위 테스트 (순수 Java)
> **문서 참조**: `docs/coding_convention/04-persistence-layer/mysql/mapper/`

### 7.1 Entity Mappers

| Mapper | 파일 | 테스트 포인트 | 상태 |
|--------|------|--------------|------|
| SellerJpaEntityMapper | `seller/mapper/SellerJpaEntityMapper.java` | Domain ↔ Entity 양방향 변환 | ⬜ |
| CrawlSchedulerJpaEntityMapper | `schedule/mapper/CrawlSchedulerJpaEntityMapper.java` | Domain ↔ Entity, History 포함 | ⬜ |
| CrawlSchedulerHistoryJpaEntityMapper | `schedule/mapper/CrawlSchedulerHistoryJpaEntityMapper.java` | History 변환 | ⬜ |
| CrawlSchedulerOutBoxJpaEntityMapper | `schedule/mapper/CrawlSchedulerOutBoxJpaEntityMapper.java` | Outbox 변환 | ⬜ |
| CrawlTaskJpaEntityMapper | `task/mapper/CrawlTaskJpaEntityMapper.java` | Domain ↔ Entity, Outbox 포함 | ⬜ |
| CrawlTaskOutboxJpaEntityMapper | `task/mapper/CrawlTaskOutboxJpaEntityMapper.java` | Outbox 변환 | ⬜ |
| CrawlExecutionJpaEntityMapper | `execution/mapper/CrawlExecutionJpaEntityMapper.java` | Domain ↔ Entity | ⬜ |
| UserAgentJpaEntityMapper | `useragent/mapper/UserAgentJpaEntityMapper.java` | Domain ↔ Entity | ⬜ |
| CrawledRawJpaEntityMapper | `product/mapper/CrawledRawJpaEntityMapper.java` | Raw 데이터 변환 | ⬜ |
| CrawledProductJpaEntityMapper | `product/mapper/CrawledProductJpaEntityMapper.java` | Product 변환 | ⬜ |

### 7.2 Entity Mapper 테스트 예시

```java
@Test
void toEntity_Domain_to_Entity() {
    // given
    Seller seller = Seller.forNew("test-seller", "https://example.com");

    // when
    SellerJpaEntity entity = mapper.toEntity(seller);

    // then
    assertThat(entity.getSellerName()).isEqualTo("test-seller");
    assertThat(entity.getSiteUrl()).isEqualTo("https://example.com");
}

@Test
void toDomain_Entity_to_Domain() {
    // given
    SellerJpaEntity entity = createEntity();

    // when
    Seller seller = mapper.toDomain(entity);

    // then
    assertThat(seller.getSellerName()).isEqualTo(entity.getSellerName());
}
```

---

## 8. Redis Adapter 테스트 (P1 - 중요)

> **테스트 전략**: 통합 테스트 (Testcontainers Redis)

### 8.1 Redis Adapters

#### RedisDistributedLockAdapter

**파일**: `adapter-out/persistence-redis/.../adapter/RedisDistributedLockAdapter.java`
**테스트 파일**: `RedisDistributedLockAdapterIntegrationTest.java`
**우선순위**: 🟡 P1

| 테스트 항목 | 메서드 | 검증 포인트 | 상태 |
|------------|--------|------------|------|
| 락 획득 | `tryLock(String, Duration)` | 락 획득 성공/실패 | ⬜ |
| 락 해제 | `unlock(String)` | 락 해제 | ⬜ |
| 동시성 | 동시 락 요청 | 단일 획득, 나머지 대기/실패 | ⬜ |
| TTL | 락 만료 | 자동 해제 | ⬜ |

#### UserAgentPoolCacheAdapter

**파일**: `adapter-out/persistence-redis/.../adapter/UserAgentPoolCacheAdapter.java`
**테스트 파일**: `UserAgentPoolCacheAdapterIntegrationTest.java`
**우선순위**: 🟡 P1

| 테스트 항목 | 메서드 | 검증 포인트 | 상태 |
|------------|--------|------------|------|
| Pool 초기화 | `initializePool(List<UserAgent>)` | Redis Set 저장 | ⬜ |
| 소비 | `consume()` | SPOP 동작, 빈 Pool 시 null | ⬜ |
| 반환 | `returnToPool(UserAgent)` | SADD 동작 | ⬜ |
| 상태 조회 | `getPoolStatus()` | Pool 크기, 사용 중 수 | ⬜ |

---

## 9. AWS SQS Adapter 테스트 (P1 - 중요)

> **테스트 전략**: 통합 테스트 (LocalStack SQS)

### 9.1 CrawlTaskSqsAdapter

**파일**: `adapter-out/aws-sqs/.../adapter/CrawlTaskSqsAdapter.java`
**테스트 파일**: `CrawlTaskSqsAdapterIntegrationTest.java`
**우선순위**: 🟡 P1

| 테스트 항목 | 메서드 | 검증 포인트 | 상태 |
|------------|--------|------------|------|
| 메시지 발행 | `sendMessage(CrawlTaskMessage)` | SQS SendMessage 호출 | ⬜ |
| 배치 발행 | `sendBatch(List<CrawlTaskMessage>)` | BatchSendMessage 호출 | ⬜ |
| 재시도 | 발행 실패 시 | 예외 처리, 재시도 로직 | ⬜ |

### 9.2 SqsPublishException

**파일**: `adapter-out/aws-sqs/.../exception/SqsPublishException.java`
**테스트 포인트**: 예외 생성, 메시지 포함

---

## 10. AWS EventBridge Adapter 테스트 (P2 - 권장)

> **테스트 전략**: 통합 테스트 (LocalStack EventBridge)

### 10.1 EventBridgeClientAdapter

**파일**: `adapter-out/aws-eventbridge/.../adapter/EventBridgeClientAdapter.java`
**테스트 파일**: `EventBridgeClientAdapterIntegrationTest.java`
**우선순위**: 🟢 P2

| 테스트 항목 | 메서드 | 검증 포인트 | 상태 |
|------------|--------|------------|------|
| 이벤트 발행 | `putEvent(...)` | PutEvents 호출 | ⬜ |
| Rule 생성 | `createRule(...)` | EventBridge Rule 생성 | ⬜ |
| Target 설정 | `putTargets(...)` | SQS Target 연결 | ⬜ |

---

## 11. HTTP Client Adapter 테스트 (P1 - 중요)

> **테스트 전략**: 단위 테스트 (MockWebServer)

### 11.1 HTTP Adapters

#### WebClientHttpAdapter

**파일**: `adapter-out/http-client/.../adapter/WebClientHttpAdapter.java`
**테스트 파일**: `WebClientHttpAdapterTest.java`
**우선순위**: 🟡 P1

| 테스트 항목 | 메서드 | 검증 포인트 | 상태 |
|------------|--------|------------|------|
| GET 요청 | `get(url, headers)` | 응답 파싱, 헤더 처리 | ⬜ |
| POST 요청 | `post(url, body, headers)` | 요청 Body 전송 | ⬜ |
| 타임아웃 | 응답 지연 시 | 타임아웃 예외 | ⬜ |
| 재시도 | 일시적 실패 | Retry 동작 | ⬜ |
| 에러 처리 | 4xx/5xx 응답 | 예외 변환 | ⬜ |

#### WebClientSessionTokenAdapter

**파일**: `adapter-out/http-client/.../adapter/WebClientSessionTokenAdapter.java`
**테스트 파일**: `WebClientSessionTokenAdapterTest.java`
**우선순위**: 🟡 P1

| 테스트 항목 | 메서드 | 검증 포인트 | 상태 |
|------------|--------|------------|------|
| 토큰 획득 | `getSessionToken()` | 토큰 반환 | ⬜ |
| 토큰 갱신 | `refreshToken()` | 만료 시 갱신 | ⬜ |
| 캐싱 | 동일 토큰 요청 | 캐시 활용 | ⬜ |

---

## 12. Config/Properties 테스트 (P2 - 권장)

> **테스트 전략**: @SpringBootTest + @ConfigurationPropertiesTest

### 12.1 REST API Config

| Config | 파일 | 테스트 포인트 | 상태 |
|--------|------|--------------|------|
| ApiEndpointProperties | `config/properties/ApiEndpointProperties.java` | 엔드포인트 바인딩 | ⬜ |

### 12.2 AWS Config

| Config | 파일 | 테스트 포인트 | 상태 |
|--------|------|--------------|------|
| SqsProperties | `aws-sqs/config/SqsProperties.java` | Queue URL 바인딩 | ⬜ |
| EventBridgeProperties | `aws-eventbridge/config/EventBridgeProperties.java` | EventBus 설정 | ⬜ |

### 12.3 Redis Config

| Config | 파일 | 테스트 포인트 | 상태 |
|--------|------|--------------|------|
| RedisProperties | `persistence-redis/config/RedisProperties.java` | 연결 설정 | ⬜ |
| UserAgentPoolProperties | `persistence-redis/config/UserAgentPoolProperties.java` | Pool 설정 | ⬜ |

### 12.4 HTTP Client Config

| Config | 파일 | 테스트 포인트 | 상태 |
|--------|------|--------------|------|
| HttpClientProperties | `http-client/config/HttpClientProperties.java` | 타임아웃, 재시도 설정 | ⬜ |

---

## 테스트 환경 설정

### Testcontainers 설정

```java
@Testcontainers
class AdapterIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7")
        .withExposedPorts(6379);

    @Container
    static LocalStackContainer localstack = new LocalStackContainer(
        DockerImageName.parse("localstack/localstack:3.0")
    ).withServices(LocalStackContainer.Service.SQS, LocalStackContainer.Service.EVENTS);
}
```

### MockWebServer 설정

```java
class HttpAdapterTest {

    private MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }
}
```

---

## 우선순위 요약

| 우선순위 | 대상 | 테스트 항목 수 |
|---------|------|--------------|
| 🔴 P0 | Controller, Core Adapter | ~25개 |
| 🟡 P1 | Mapper, ErrorMapper, Redis, SQS, HTTP | ~35개 |
| 🟢 P2 | Config, UserAgent, EventBridge | ~15개 |

---

## 다음 단계

1. **P0 Controller 테스트 작성** (MockMvc 기반)
2. **P0 Persistence Adapter 통합 테스트** (Testcontainers)
3. **P1 Mapper 단위 테스트**
4. **P1 Redis/SQS/HTTP 통합 테스트**
5. **P2 Config 테스트**
