# PRD: Crawl Task Trigger

**작성일**: 2025-11-25
**작성자**: Claude
**상태**: Draft
**버전**: 2.0.0

---

## 📋 프로젝트 개요

### 비즈니스 목적

EventBridge 스케줄에 의해 트리거되어 CrawlTask를 생성하고, SQS를 통해 Worker에게 전달하여 MustIt 크롤링을 수행하는 시스템 구축

### 주요 사용자

| 사용자 | 역할 |
|--------|------|
| EventBridge | 자동 트리거 (스케줄 기반) |
| API Server | Task 생성 및 Outbox 저장 |
| ECS Worker | Task 소비 및 크롤링 수행 |

### 성공 기준

- EventBridge → API Server → SQS → Worker 파이프라인 안정적 동작
- 중복 트리거 방지 (Idempotency 보장)
- Worker 수평 확장 가능
- Outbox 패턴으로 메시지 유실 방지

---

## 🏗️ Layer별 상세 설계

### 1. Domain Layer

> **참고**: [Domain Layer Guide](../../coding_convention/02-domain-layer/domain-guide.md)

#### 패키지 구조

```
domain/
└─ crawl/
   └─ task/
      ├─ aggregate/
      │  └─ CrawlTask.java              # Aggregate Root
      ├─ identifier/
      │  └─ CrawlTaskId.java            # Long Value Object (Auto Increment)
      ├─ vo/
      │  ├─ CrawlTaskStatus.java        # Enum VO
      │  ├─ CrawlTaskType.java          # Enum VO
      │  └─ CrawlEndpoint.java          # 크롤링 URL 정보
      └─ exception/
         ├─ CrawlTaskErrorCode.java
         ├─ CrawlTaskNotFoundException.java
         ├─ InvalidCrawlTaskStateException.java
         └─ DuplicateCrawlTaskException.java
```

#### Aggregate: CrawlTask

**클래스 설계**:

```java
/**
 * 크롤링 작업 단위를 나타내는 Aggregate Root.
 *
 * <p>비즈니스 규칙:
 * <ul>
 *   <li>Scheduler가 ACTIVE 상태일 때만 생성 가능</li>
 *   <li>동일 Scheduler에 WAITING/RUNNING Task 존재 시 생성 불가 (중복 방지)</li>
 *   <li>상태 전환은 명시적 비즈니스 메서드를 통해서만 가능</li>
 * </ul>
 *
 * @see CrawlTaskStatus
 * @see CrawlTaskType
 */
public class CrawlTask {

    private final CrawlTaskId crawlTaskId;
    private final Long crawlScheduleId;    // Long FK 전략
    private final Long sellerId;            // Long FK 전략
    private final CrawlEndpoint endpoint;
    private CrawlTaskStatus status;
    private final CrawlTaskType taskType;
    private int retryCount;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 생성자 (private) - 정적 팩토리 메서드로만 생성

    /**
     * 새로운 CrawlTask를 생성한다.
     *
     * @param crawlScheduleId 크롤 스케줄 ID (FK)
     * @param sellerId 셀러 ID (FK)
     * @param endpoint 크롤링 엔드포인트
     * @param taskType 태스크 유형
     * @return 새로운 CrawlTask 인스턴스
     */
    public static CrawlTask forNew(
            Long crawlScheduleId,
            Long sellerId,
            CrawlEndpoint endpoint,
            CrawlTaskType taskType) {
        // WAITING 상태로 초기 생성
    }

    /**
     * 기존 값으로 CrawlTask를 복원한다. (영속성 계층에서 사용)
     */
    public static CrawlTask reconstitute(
            CrawlTaskId crawlTaskId,
            Long crawlScheduleId,
            Long sellerId,
            CrawlEndpoint endpoint,
            CrawlTaskStatus status,
            CrawlTaskType taskType,
            int retryCount,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        // 기존 데이터로 복원
    }

    // 비즈니스 메서드

    /**
     * Task를 발행 상태로 변경한다.
     *
     * @throws InvalidCrawlTaskStateException WAITING 상태가 아닌 경우
     */
    public void markAsPublished() {
        validateStatus(CrawlTaskStatus.WAITING);
        this.status = CrawlTaskStatus.PUBLISHED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Task를 실행 상태로 변경한다.
     *
     * @throws InvalidCrawlTaskStateException PUBLISHED 상태가 아닌 경우
     */
    public void markAsRunning() {
        validateStatus(CrawlTaskStatus.PUBLISHED);
        this.status = CrawlTaskStatus.RUNNING;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Task를 성공 상태로 변경한다.
     */
    public void markAsSuccess() {
        validateStatus(CrawlTaskStatus.RUNNING);
        this.status = CrawlTaskStatus.SUCCESS;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Task를 실패 상태로 변경한다.
     */
    public void markAsFailed() {
        validateStatus(CrawlTaskStatus.RUNNING);
        this.status = CrawlTaskStatus.FAILED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 재시도를 시도한다.
     *
     * @param maxRetryCount 최대 재시도 횟수
     * @return 재시도 가능 여부
     */
    public boolean attemptRetry(int maxRetryCount) {
        if (!canRetry(maxRetryCount)) {
            return false;
        }
        this.retryCount++;
        this.status = CrawlTaskStatus.RETRY;
        this.updatedAt = LocalDateTime.now();
        return true;
    }

    /**
     * 재시도 가능 여부를 판단한다.
     */
    public boolean canRetry(int maxRetryCount) {
        return (status == CrawlTaskStatus.FAILED || status == CrawlTaskStatus.TIMEOUT)
                && retryCount < maxRetryCount;
    }

    /**
     * 진행 중인 상태인지 확인한다.
     */
    public boolean isInProgress() {
        return status == CrawlTaskStatus.WAITING
                || status == CrawlTaskStatus.PUBLISHED
                || status == CrawlTaskStatus.RUNNING;
    }

    // Getter (읽기 전용)
    public CrawlTaskId getCrawlTaskId() { return crawlTaskId; }
    public Long getCrawlScheduleId() { return crawlScheduleId; }
    public Long getSellerId() { return sellerId; }
    public CrawlEndpoint getEndpoint() { return endpoint; }
    public CrawlTaskStatus getStatus() { return status; }
    public CrawlTaskType getTaskType() { return taskType; }
    public int getRetryCount() { return retryCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
```

#### Value Objects

**CrawlTaskId (식별자)**:

```java
/**
 * CrawlTask의 식별자 Value Object.
 *
 * <p>Auto Increment 기반 Long ID를 캡슐화한다.
 */
public record CrawlTaskId(Long value) {

    public CrawlTaskId {
        if (value != null && value <= 0) {
            throw new IllegalArgumentException("CrawlTaskId must be positive");
        }
    }

    /**
     * 새로운 Task용 (ID 미할당)
     */
    public static CrawlTaskId unassigned() {
        return new CrawlTaskId(null);
    }

    /**
     * 기존 ID로 생성
     */
    public static CrawlTaskId of(Long value) {
        return new CrawlTaskId(value);
    }

    public boolean isAssigned() {
        return value != null;
    }
}
```

**CrawlTaskStatus (상태 Enum)**:

```java
/**
 * CrawlTask의 상태를 나타내는 Enum.
 *
 * <pre>
 * 상태 전이:
 * WAITING → PUBLISHED → RUNNING → SUCCESS
 *                          ↓
 *                       FAILED → RETRY → PUBLISHED
 *                          ↓
 *                       TIMEOUT → RETRY
 * </pre>
 */
public enum CrawlTaskStatus {

    /** 대기 중 - 아직 발행되지 않음 */
    WAITING("대기"),

    /** 발행됨 - SQS에 메시지 발행됨 */
    PUBLISHED("발행"),

    /** 실행 중 - Worker가 처리 중 */
    RUNNING("실행"),

    /** 성공 - 크롤링 완료 */
    SUCCESS("성공"),

    /** 실패 - 크롤링 실패 */
    FAILED("실패"),

    /** 재시도 - 재시도 대기 중 */
    RETRY("재시도"),

    /** 타임아웃 - 처리 시간 초과 */
    TIMEOUT("타임아웃");

    private final String description;

    CrawlTaskStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 진행 중인 상태인지 확인한다.
     */
    public boolean isInProgress() {
        return this == WAITING || this == PUBLISHED || this == RUNNING;
    }

    /**
     * 완료된 상태인지 확인한다.
     */
    public boolean isTerminal() {
        return this == SUCCESS || this == FAILED;
    }
}
```

**CrawlTaskType (유형 Enum)**:

```java
/**
 * CrawlTask의 유형을 나타내는 Enum.
 *
 * <pre>
 * 실행 순서:
 * 1. META - 미니샵 메타데이터 (상품 수 확인)
 * 2. MINI_SHOP - 미니샵 상품 목록 (500개 단위)
 * 3. DETAIL - 상품 상세 정보
 * 4. OPTION - 상품 옵션 정보
 * </pre>
 */
public enum CrawlTaskType {

    /** 미니샵 메타데이터 크롤링 */
    META("메타데이터"),

    /** 미니샵 상품 목록 크롤링 */
    MINI_SHOP("미니샵"),

    /** 상품 상세 크롤링 */
    DETAIL("상세"),

    /** 상품 옵션 크롤링 */
    OPTION("옵션");

    private final String description;

    CrawlTaskType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
```

**CrawlEndpoint (엔드포인트 VO)**:

```java
/**
 * 크롤링 대상 엔드포인트를 나타내는 Value Object.
 */
public record CrawlEndpoint(
        String baseUrl,
        String path,
        Map<String, String> queryParams
) {

    private static final String MUSTIT_BASE_URL = "https://m.web.mustit.co.kr";

    public CrawlEndpoint {
        Objects.requireNonNull(baseUrl, "baseUrl must not be null");
        Objects.requireNonNull(path, "path must not be null");
        queryParams = queryParams != null ? Map.copyOf(queryParams) : Map.of();
    }

    /**
     * 미니샵 메타데이터 엔드포인트 생성
     */
    public static CrawlEndpoint forMiniShopMeta(Long sellerId) {
        return new CrawlEndpoint(
                MUSTIT_BASE_URL,
                "/mustit-api/facade-api/v1/search/mini-shop-search",
                Map.of("sellerId", String.valueOf(sellerId))
        );
    }

    /**
     * 미니샵 상품 목록 엔드포인트 생성
     */
    public static CrawlEndpoint forMiniShopList(Long sellerId, int page, int size) {
        return new CrawlEndpoint(
                MUSTIT_BASE_URL,
                "/mustit-api/facade-api/v1/search/mini-shop-search",
                Map.of(
                        "sellerId", String.valueOf(sellerId),
                        "page", String.valueOf(page),
                        "size", String.valueOf(size)
                )
        );
    }

    /**
     * 상품 상세 엔드포인트 생성
     */
    public static CrawlEndpoint forProductDetail(Long itemNo) {
        return new CrawlEndpoint(
                MUSTIT_BASE_URL,
                "/mustit-api/facade-api/v1/item/" + itemNo + "/detail/top",
                Map.of()
        );
    }

    /**
     * 상품 옵션 엔드포인트 생성
     */
    public static CrawlEndpoint forProductOption(Long itemNo) {
        return new CrawlEndpoint(
                MUSTIT_BASE_URL,
                "/mustit-api/legacy-api/v1/auction_products/" + itemNo + "/options",
                Map.of()
        );
    }

    /**
     * 전체 URL을 반환한다.
     */
    public String toFullUrl() {
        StringBuilder sb = new StringBuilder(baseUrl).append(path);
        if (!queryParams.isEmpty()) {
            sb.append("?");
            sb.append(queryParams.entrySet().stream()
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .collect(Collectors.joining("&")));
        }
        return sb.toString();
    }
}
```

#### Exception

**CrawlTaskErrorCode**:

```java
/**
 * CrawlTask 도메인의 에러 코드.
 */
public enum CrawlTaskErrorCode implements ErrorCode {

    CRAWL_TASK_NOT_FOUND("CRAWL_TASK_001", "크롤 태스크를 찾을 수 없습니다"),
    INVALID_CRAWL_TASK_STATE("CRAWL_TASK_002", "유효하지 않은 태스크 상태입니다"),
    DUPLICATE_CRAWL_TASK("CRAWL_TASK_003", "이미 진행 중인 태스크가 존재합니다"),
    SCHEDULER_NOT_ACTIVE("CRAWL_TASK_004", "스케줄러가 활성 상태가 아닙니다"),
    MAX_RETRY_EXCEEDED("CRAWL_TASK_005", "최대 재시도 횟수를 초과했습니다");

    private final String code;
    private final String message;

    CrawlTaskErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
```

#### Zero-Tolerance 규칙 준수 체크리스트

| 규칙 | 상태 | 설명 |
|------|------|------|
| Lombok 금지 | ✅ | Pure Java 사용 |
| Law of Demeter | ✅ | Getter 체이닝 없음 |
| Long FK 전략 | ✅ | `crawlScheduleId`, `sellerId`는 Long 타입 |
| Setter 금지 | ✅ | 비즈니스 메서드로만 상태 변경 |
| 기술 독립성 | ✅ | JPA/Spring 어노테이션 없음 |

---

### 2. Application Layer

> **참고**: [Application Layer Guide](../../coding_convention/03-application-layer/application-guide.md)

#### 패키지 구조

```
application/
└─ crawl/
   └─ task/
      ├─ assembler/
      │  └─ CrawlTaskAssembler.java
      ├─ dto/
      │  ├─ command/
      │  │   └─ TriggerCrawlTaskCommand.java
      │  ├─ query/
      │  │   ├─ GetCrawlTaskQuery.java
      │  │   └─ ListCrawlTasksQuery.java
      │  └─ response/
      │      ├─ CrawlTaskResponse.java
      │      └─ CrawlTaskDetailResponse.java
      ├─ facade/
      │  └─ CrawlTaskTriggerFacade.java
      ├─ manager/
      │  └─ CrawlTaskTransactionManager.java
      ├─ port/
      │  ├─ in/
      │  │   ├─ command/
      │  │   │   └─ TriggerCrawlTaskUseCase.java
      │  │   └─ query/
      │  │       ├─ GetCrawlTaskUseCase.java
      │  │       └─ ListCrawlTasksUseCase.java
      │  └─ out/
      │      ├─ command/
      │      │   └─ CrawlTaskPersistencePort.java
      │      ├─ query/
      │      │   └─ CrawlTaskQueryPort.java
      │      └─ messaging/
      │          └─ CrawlTaskMessagePort.java
      └─ service/
         ├─ command/
         │   └─ TriggerCrawlTaskService.java
         └─ query/
             ├─ GetCrawlTaskService.java
             └─ ListCrawlTasksService.java
```

#### Port-In (Command)

**TriggerCrawlTaskUseCase**:

```java
/**
 * 크롤 태스크 트리거 UseCase.
 *
 * <p>EventBridge에서 호출되어 CrawlTask를 생성하고 SQS에 발행한다.
 */
public interface TriggerCrawlTaskUseCase {

    /**
     * 크롤 태스크를 트리거한다.
     *
     * @param command 트리거 명령
     * @return 생성된 태스크 응답
     * @throws SchedulerNotActiveException 스케줄러가 비활성 상태인 경우
     * @throws DuplicateCrawlTaskException 이미 진행 중인 태스크가 존재하는 경우
     */
    CrawlTaskResponse trigger(TriggerCrawlTaskCommand command);
}
```

#### Port-In (Query)

**GetCrawlTaskUseCase**:

```java
/**
 * 크롤 태스크 단건 조회 UseCase.
 */
public interface GetCrawlTaskUseCase {

    /**
     * 태스크 ID로 조회한다.
     *
     * @param query 조회 쿼리
     * @return 태스크 상세 응답
     * @throws CrawlTaskNotFoundException 태스크를 찾을 수 없는 경우
     */
    CrawlTaskDetailResponse get(GetCrawlTaskQuery query);
}
```

**ListCrawlTasksUseCase**:

```java
/**
 * 크롤 태스크 목록 조회 UseCase.
 */
public interface ListCrawlTasksUseCase {

    /**
     * 스케줄러 ID로 태스크 목록을 조회한다.
     *
     * @param query 목록 조회 쿼리
     * @return 페이징된 태스크 목록
     */
    PageResponse<CrawlTaskResponse> list(ListCrawlTasksQuery query);
}
```

#### Port-Out (Command)

**CrawlTaskPersistencePort**:

```java
/**
 * CrawlTask 영속성 포트.
 */
public interface CrawlTaskPersistencePort {

    /**
     * CrawlTask를 저장한다.
     *
     * @param crawlTask 저장할 태스크
     * @return 저장된 태스크 (ID 할당됨)
     */
    CrawlTask persist(CrawlTask crawlTask);
}
```

**CrawlTaskMessagePort**:

```java
/**
 * CrawlTask 메시징 포트 (SQS 발행).
 */
public interface CrawlTaskMessagePort {

    /**
     * 태스크 메시지를 발행한다.
     *
     * @param crawlTask 발행할 태스크
     * @param idempotencyKey 멱등성 키
     */
    void publish(CrawlTask crawlTask, String idempotencyKey);
}
```

#### Port-Out (Query)

**CrawlTaskQueryPort**:

```java
/**
 * CrawlTask 조회 포트.
 */
public interface CrawlTaskQueryPort {

    /**
     * ID로 태스크를 조회한다.
     */
    Optional<CrawlTask> findById(CrawlTaskId crawlTaskId);

    /**
     * 스케줄러 ID와 상태 목록으로 존재 여부를 확인한다.
     */
    boolean existsByScheduleIdAndStatusIn(Long crawlScheduleId, List<CrawlTaskStatus> statuses);

    /**
     * 스케줄러 ID로 목록을 조회한다.
     */
    Page<CrawlTask> findByScheduleId(Long crawlScheduleId, CrawlTaskStatus status, Pageable pageable);
}
```

#### DTO

**TriggerCrawlTaskCommand**:

```java
/**
 * 크롤 태스크 트리거 Command DTO.
 */
public record TriggerCrawlTaskCommand(
        Long crawlScheduleId
) {
    public TriggerCrawlTaskCommand {
        Objects.requireNonNull(crawlScheduleId, "crawlScheduleId must not be null");
    }
}
```

**CrawlTaskResponse**:

```java
/**
 * 크롤 태스크 응답 DTO.
 */
public record CrawlTaskResponse(
        Long crawlTaskId,
        Long crawlScheduleId,
        Long sellerId,
        String requestUrl,
        String status,
        String taskType,
        int retryCount,
        LocalDateTime createdAt
) {
}
```

#### Service 구현

**TriggerCrawlTaskService**:

```java
/**
 * 크롤 태스크 트리거 서비스.
 *
 * <p>⚠️ Transaction 경계 주의:
 * <ul>
 *   <li>SQS 발행은 반드시 트랜잭션 커밋 후 수행 (afterCommit)</li>
 *   <li>외부 API 호출은 @Transactional 밖에서 수행</li>
 * </ul>
 */
@Service
public class TriggerCrawlTaskService implements TriggerCrawlTaskUseCase {

    private final CrawlTaskTransactionManager transactionManager;
    private final CrawlTaskAssembler assembler;

    public TriggerCrawlTaskService(
            CrawlTaskTransactionManager transactionManager,
            CrawlTaskAssembler assembler) {
        this.transactionManager = transactionManager;
        this.assembler = assembler;
    }

    @Override
    public CrawlTaskResponse trigger(TriggerCrawlTaskCommand command) {
        // 1. Transaction 내에서 Task 생성 및 저장
        CrawlTask savedTask = transactionManager.createAndPersist(command);

        // 2. Transaction 커밋 후 SQS 발행 (afterCommit)
        // → TransactionManager 내부에서 TransactionSynchronization으로 처리

        return assembler.toResponse(savedTask);
    }
}
```

**CrawlTaskTransactionManager**:

```java
/**
 * CrawlTask 트랜잭션 관리자.
 *
 * <p>단일 트랜잭션 내에서 Task 생성/저장/Outbox 처리를 담당한다.
 */
@Component
public class CrawlTaskTransactionManager {

    private final CrawlScheduleQueryPort scheduleQueryPort;
    private final CrawlTaskPersistencePort taskPersistencePort;
    private final CrawlTaskQueryPort taskQueryPort;
    private final CrawlTaskMessagePort messagePort;

    // 생성자 주입

    /**
     * CrawlTask를 생성하고 저장한다.
     *
     * <p>비즈니스 검증:
     * <ol>
     *   <li>Scheduler 조회 및 상태 검증</li>
     *   <li>중복 Task 존재 여부 확인</li>
     *   <li>Task 생성 및 저장</li>
     *   <li>트랜잭션 커밋 후 SQS 발행</li>
     * </ol>
     *
     * @param command 트리거 명령
     * @return 저장된 Task
     */
    @Transactional
    public CrawlTask createAndPersist(TriggerCrawlTaskCommand command) {
        // 1. Scheduler 조회
        CrawlSchedule schedule = scheduleQueryPort.findById(command.crawlScheduleId())
                .orElseThrow(() -> new ScheduleNotFoundException(command.crawlScheduleId()));

        // 2. Scheduler 상태 검증 (Domain에서 판단)
        schedule.validateActive();  // ACTIVE 아니면 예외

        // 3. 중복 Task 확인
        boolean hasInProgressTask = taskQueryPort.existsByScheduleIdAndStatusIn(
                command.crawlScheduleId(),
                List.of(CrawlTaskStatus.WAITING, CrawlTaskStatus.PUBLISHED, CrawlTaskStatus.RUNNING)
        );
        if (hasInProgressTask) {
            throw new DuplicateCrawlTaskException(command.crawlScheduleId());
        }

        // 4. Task 생성
        CrawlTask task = CrawlTask.forNew(
                schedule.getCrawlScheduleId().value(),
                schedule.getSellerId(),
                CrawlEndpoint.forMiniShopMeta(schedule.getSellerId()),
                CrawlTaskType.META
        );

        // 5. Task 저장
        CrawlTask savedTask = taskPersistencePort.persist(task);

        // 6. 트랜잭션 커밋 후 SQS 발행 등록
        String idempotencyKey = generateIdempotencyKey(savedTask);
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        publishToSqs(savedTask, idempotencyKey);
                    }
                }
        );

        return savedTask;
    }

    private void publishToSqs(CrawlTask task, String idempotencyKey) {
        try {
            messagePort.publish(task, idempotencyKey);
            // Outbox 상태 업데이트는 별도 트랜잭션
        } catch (Exception e) {
            // 실패 시 Outbox에서 Fallback Scheduler가 재시도
            log.warn("Failed to publish task to SQS: {}", task.getCrawlTaskId(), e);
        }
    }

    private String generateIdempotencyKey(CrawlTask task) {
        return task.getCrawlTaskId().value() + "_" + System.currentTimeMillis();
    }
}
```

#### Zero-Tolerance 규칙 준수 체크리스트

| 규칙 | 상태 | 설명 |
|------|------|------|
| Lombok 금지 | ✅ | Pure Java 사용 |
| CQRS 분리 | ✅ | Command/Query 패키지 분리 |
| Transaction 경계 | ✅ | SQS 발행은 afterCommit에서 수행 |
| DTO Record | ✅ | 모든 DTO는 Record 타입 |
| Domain 직접 노출 금지 | ✅ | Assembler로 변환 |

---

### 3. Persistence Layer

> **참고**: [Persistence Layer Guide](../../coding_convention/04-persistence-layer/mysql/persistence-mysql-guide.md)

#### 패키지 구조

```
adapter-out/persistence-mysql/
└─ crawl/
   └─ task/
      ├─ adapter/
      │  ├─ CrawlTaskCommandAdapter.java
      │  └─ CrawlTaskQueryAdapter.java
      ├─ entity/
      │  └─ CrawlTaskEntity.java
      ├─ mapper/
      │  └─ CrawlTaskEntityMapper.java
      └─ repository/
         ├─ CrawlTaskJpaRepository.java
         └─ CrawlTaskQueryDslRepository.java
```

#### JPA Entity

**CrawlTaskEntity**:

```java
/**
 * CrawlTask JPA Entity.
 *
 * <p>Zero-Tolerance 규칙:
 * <ul>
 *   <li>Lombok 금지 - 모든 메서드 명시적 작성</li>
 *   <li>Setter 금지 - 생성자/정적 팩토리로만 생성</li>
 *   <li>관계 어노테이션 금지 - Long FK만 사용</li>
 * </ul>
 */
@Entity
@Table(
        name = "crawl_tasks",
        indexes = {
                @Index(name = "idx_schedule_status", columnList = "crawl_schedule_id, status"),
                @Index(name = "idx_status_created", columnList = "status, created_at")
        }
)
public class CrawlTaskEntity extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "crawl_schedule_id", nullable = false)
    private Long crawlScheduleId;  // Long FK 전략

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;  // Long FK 전략

    @Column(name = "base_url", nullable = false)
    private String baseUrl;

    @Column(name = "path", nullable = false)
    private String path;

    @Column(name = "query_params", columnDefinition = "TEXT")
    private String queryParams;  // JSON 문자열

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private CrawlTaskStatus status;

    @Column(name = "task_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private CrawlTaskType taskType;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    // protected 기본 생성자 (JPA 요구사항)
    protected CrawlTaskEntity() {
    }

    // 정적 팩토리 메서드
    public static CrawlTaskEntity from(CrawlTask domain) {
        CrawlTaskEntity entity = new CrawlTaskEntity();
        entity.id = domain.getCrawlTaskId().isAssigned()
                ? domain.getCrawlTaskId().value()
                : null;
        entity.crawlScheduleId = domain.getCrawlScheduleId();
        entity.sellerId = domain.getSellerId();
        entity.baseUrl = domain.getEndpoint().baseUrl();
        entity.path = domain.getEndpoint().path();
        entity.queryParams = toJson(domain.getEndpoint().queryParams());
        entity.status = domain.getStatus();
        entity.taskType = domain.getTaskType();
        entity.retryCount = domain.getRetryCount();
        return entity;
    }

    // Getter만 제공 (Setter 금지)
    public Long getId() { return id; }
    public Long getCrawlScheduleId() { return crawlScheduleId; }
    public Long getSellerId() { return sellerId; }
    public String getBaseUrl() { return baseUrl; }
    public String getPath() { return path; }
    public String getQueryParams() { return queryParams; }
    public CrawlTaskStatus getStatus() { return status; }
    public CrawlTaskType getTaskType() { return taskType; }
    public int getRetryCount() { return retryCount; }

    private static String toJson(Map<String, String> map) {
        // ObjectMapper 사용하여 JSON 변환
    }
}
```

#### Repository

**CrawlTaskJpaRepository (Command용)**:

```java
/**
 * CrawlTask JPA Repository (Command 전용).
 *
 * <p>저장/삭제만 담당. 조회는 QueryDslRepository에서.
 */
public interface CrawlTaskJpaRepository extends JpaRepository<CrawlTaskEntity, Long> {
    // JpaRepository 기본 메서드만 사용
}
```

**CrawlTaskQueryDslRepository (Query용)**:

```java
/**
 * CrawlTask QueryDSL Repository (Query 전용).
 */
@Repository
public class CrawlTaskQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    public CrawlTaskQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    public Optional<CrawlTaskEntity> findById(Long id) {
        QCrawlTaskEntity task = QCrawlTaskEntity.crawlTaskEntity;

        CrawlTaskEntity result = queryFactory
                .selectFrom(task)
                .where(task.id.eq(id))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    public boolean existsByScheduleIdAndStatusIn(Long scheduleId, List<CrawlTaskStatus> statuses) {
        QCrawlTaskEntity task = QCrawlTaskEntity.crawlTaskEntity;

        Integer count = queryFactory
                .selectOne()
                .from(task)
                .where(
                        task.crawlScheduleId.eq(scheduleId),
                        task.status.in(statuses)
                )
                .fetchFirst();

        return count != null;
    }

    public Page<CrawlTaskEntity> findByScheduleId(
            Long scheduleId,
            CrawlTaskStatus status,
            Pageable pageable) {
        QCrawlTaskEntity task = QCrawlTaskEntity.crawlTaskEntity;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(task.crawlScheduleId.eq(scheduleId));
        if (status != null) {
            builder.and(task.status.eq(status));
        }

        List<CrawlTaskEntity> content = queryFactory
                .selectFrom(task)
                .where(builder)
                .orderBy(task.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(task.count())
                .from(task)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }
}
```

#### Adapter

**CrawlTaskCommandAdapter**:

```java
/**
 * CrawlTask Command Adapter.
 *
 * <p>저장 작업만 담당. 비즈니스 로직 금지.
 */
@Component
public class CrawlTaskCommandAdapter implements CrawlTaskPersistencePort {

    private final CrawlTaskJpaRepository jpaRepository;
    private final CrawlTaskEntityMapper mapper;

    public CrawlTaskCommandAdapter(
            CrawlTaskJpaRepository jpaRepository,
            CrawlTaskEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public CrawlTask persist(CrawlTask crawlTask) {
        CrawlTaskEntity entity = CrawlTaskEntity.from(crawlTask);
        CrawlTaskEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
}
```

**CrawlTaskQueryAdapter**:

```java
/**
 * CrawlTask Query Adapter.
 *
 * <p>조회 작업만 담당. 비즈니스 로직 금지.
 */
@Component
public class CrawlTaskQueryAdapter implements CrawlTaskQueryPort {

    private final CrawlTaskQueryDslRepository queryDslRepository;
    private final CrawlTaskEntityMapper mapper;

    public CrawlTaskQueryAdapter(
            CrawlTaskQueryDslRepository queryDslRepository,
            CrawlTaskEntityMapper mapper) {
        this.queryDslRepository = queryDslRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<CrawlTask> findById(CrawlTaskId crawlTaskId) {
        return queryDslRepository.findById(crawlTaskId.value())
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByScheduleIdAndStatusIn(
            Long crawlScheduleId,
            List<CrawlTaskStatus> statuses) {
        return queryDslRepository.existsByScheduleIdAndStatusIn(crawlScheduleId, statuses);
    }

    @Override
    public Page<CrawlTask> findByScheduleId(
            Long crawlScheduleId,
            CrawlTaskStatus status,
            Pageable pageable) {
        return queryDslRepository.findByScheduleId(crawlScheduleId, status, pageable)
                .map(mapper::toDomain);
    }
}
```

#### Zero-Tolerance 규칙 준수 체크리스트

| 규칙 | 상태 | 설명 |
|------|------|------|
| Lombok 금지 | ✅ | Pure Java 사용 |
| Long FK 전략 | ✅ | 관계 어노테이션 없음 |
| CQRS 분리 | ✅ | Command=JPA, Query=QueryDSL |
| Setter 금지 | ✅ | 정적 팩토리 메서드로만 생성 |
| Transaction 금지 | ✅ | Adapter에 @Transactional 없음 |

---

### 4. REST API Layer

> **참고**: [REST API Layer Guide](../../coding_convention/01-adapter-in-layer/rest-api/rest-api-guide.md)

#### 패키지 구조

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

#### Controller

**CrawlTaskCommandController**:

```java
/**
 * CrawlTask Command Controller.
 *
 * <p>Thin Controller - HTTP 요청/응답 처리만. 비즈니스 로직 금지.
 */
@RestController
@RequestMapping("/api/v1/crawl/tasks")
public class CrawlTaskCommandController {

    private final TriggerCrawlTaskUseCase triggerUseCase;
    private final CrawlTaskApiMapper mapper;

    public CrawlTaskCommandController(
            TriggerCrawlTaskUseCase triggerUseCase,
            CrawlTaskApiMapper mapper) {
        this.triggerUseCase = triggerUseCase;
        this.mapper = mapper;
    }

    /**
     * 크롤링 태스크를 트리거한다.
     *
     * @param request 트리거 요청
     * @return 생성된 태스크 응답
     */
    @PostMapping("/trigger")
    public ResponseEntity<ApiResponse<CrawlTaskApiResponse>> trigger(
            @Valid @RequestBody TriggerCrawlTaskApiRequest request) {

        TriggerCrawlTaskCommand command = mapper.toCommand(request);
        CrawlTaskResponse response = triggerUseCase.trigger(command);
        CrawlTaskApiResponse apiResponse = mapper.toApiResponse(response);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(apiResponse));
    }
}
```

**CrawlTaskQueryController**:

```java
/**
 * CrawlTask Query Controller.
 */
@RestController
@RequestMapping("/api/v1/crawl/tasks")
public class CrawlTaskQueryController {

    private final GetCrawlTaskUseCase getUseCase;
    private final ListCrawlTasksUseCase listUseCase;
    private final CrawlTaskApiMapper mapper;

    public CrawlTaskQueryController(
            GetCrawlTaskUseCase getUseCase,
            ListCrawlTasksUseCase listUseCase,
            CrawlTaskApiMapper mapper) {
        this.getUseCase = getUseCase;
        this.listUseCase = listUseCase;
        this.mapper = mapper;
    }

    /**
     * 태스크를 조회한다.
     */
    @GetMapping("/{taskId}")
    public ResponseEntity<ApiResponse<CrawlTaskDetailApiResponse>> get(
            @PathVariable Long taskId) {

        GetCrawlTaskQuery query = new GetCrawlTaskQuery(taskId);
        CrawlTaskDetailResponse response = getUseCase.get(query);
        CrawlTaskDetailApiResponse apiResponse = mapper.toDetailApiResponse(response);

        return ResponseEntity.ok(ApiResponse.success(apiResponse));
    }

    /**
     * 태스크 목록을 조회한다.
     */
    @GetMapping
    public ResponseEntity<PageApiResponse<CrawlTaskApiResponse>> list(
            @Valid ListCrawlTasksApiRequest request) {

        ListCrawlTasksQuery query = mapper.toQuery(request);
        PageResponse<CrawlTaskResponse> response = listUseCase.list(query);

        return ResponseEntity.ok(mapper.toPageApiResponse(response));
    }
}
```

#### DTO

**TriggerCrawlTaskApiRequest**:

```java
/**
 * 크롤 태스크 트리거 API Request.
 */
public record TriggerCrawlTaskApiRequest(
        @NotNull(message = "crawlScheduleId는 필수입니다")
        Long crawlScheduleId
) {
}
```

**CrawlTaskApiResponse**:

```java
/**
 * 크롤 태스크 API Response.
 */
public record CrawlTaskApiResponse(
        Long crawlTaskId,
        Long crawlScheduleId,
        Long sellerId,
        String requestUrl,
        String status,
        String taskType,
        int retryCount,
        LocalDateTime createdAt
) {
}
```

#### API 엔드포인트 정의

| Method | Path | Description | Request | Response | Status |
|--------|------|-------------|---------|----------|--------|
| POST | `/api/v1/crawl/tasks/trigger` | 크롤링 트리거 | TriggerCrawlTaskApiRequest | CrawlTaskApiResponse | 201 |
| GET | `/api/v1/crawl/tasks/{taskId}` | Task 조회 | - | CrawlTaskDetailApiResponse | 200 |
| GET | `/api/v1/crawl/tasks` | Task 목록 조회 | ListCrawlTasksApiRequest | PageApiResponse | 200 |

#### Error Response

| Status | Error Code | Description |
|--------|------------|-------------|
| 404 | SCHEDULE_NOT_FOUND | Schedule 존재하지 않음 |
| 404 | CRAWL_TASK_NOT_FOUND | Task 존재하지 않음 |
| 409 | SCHEDULER_NOT_ACTIVE | Scheduler가 비활성 상태 |
| 409 | DUPLICATE_CRAWL_TASK | 이미 진행 중인 Task 존재 |

#### Zero-Tolerance 규칙 준수 체크리스트

| 규칙 | 상태 | 설명 |
|------|------|------|
| Lombok 금지 | ✅ | Record 타입 사용 |
| Thin Controller | ✅ | HTTP 처리만, UseCase에 위임 |
| Bean Validation | ✅ | @Valid + 제약 조건 |
| RESTful URI | ✅ | 리소스 기반 명사형 |
| Domain 직접 노출 금지 | ✅ | API 전용 DTO 사용 |

---

## 🧪 테스트 전략

### Domain Layer 테스트

```java
class CrawlTaskTest {

    @Test
    @DisplayName("새로운 CrawlTask 생성 시 WAITING 상태로 초기화된다")
    void forNew_shouldCreateWithWaitingStatus() {
        // given
        Long scheduleId = 1L;
        Long sellerId = 100L;
        CrawlEndpoint endpoint = CrawlEndpoint.forMiniShopMeta(sellerId);

        // when
        CrawlTask task = CrawlTask.forNew(scheduleId, sellerId, endpoint, CrawlTaskType.META);

        // then
        assertThat(task.getStatus()).isEqualTo(CrawlTaskStatus.WAITING);
        assertThat(task.getRetryCount()).isZero();
    }

    @Test
    @DisplayName("WAITING 상태에서 PUBLISHED로 전환할 수 있다")
    void markAsPublished_fromWaiting_shouldSucceed() {
        // given
        CrawlTask task = createWaitingTask();

        // when
        task.markAsPublished();

        // then
        assertThat(task.getStatus()).isEqualTo(CrawlTaskStatus.PUBLISHED);
    }

    @Test
    @DisplayName("WAITING이 아닌 상태에서 PUBLISHED로 전환 시 예외가 발생한다")
    void markAsPublished_fromNonWaiting_shouldThrow() {
        // given
        CrawlTask task = createRunningTask();

        // when & then
        assertThatThrownBy(() -> task.markAsPublished())
                .isInstanceOf(InvalidCrawlTaskStateException.class);
    }
}
```

### Application Layer 테스트

```java
@ExtendWith(MockitoExtension.class)
class TriggerCrawlTaskServiceTest {

    @Mock
    private CrawlTaskTransactionManager transactionManager;

    @Mock
    private CrawlTaskAssembler assembler;

    @InjectMocks
    private TriggerCrawlTaskService service;

    @Test
    @DisplayName("트리거 성공 시 Task 응답을 반환한다")
    void trigger_shouldReturnResponse() {
        // given
        TriggerCrawlTaskCommand command = new TriggerCrawlTaskCommand(1L);
        CrawlTask savedTask = createTask();
        CrawlTaskResponse expected = createResponse();

        when(transactionManager.createAndPersist(command)).thenReturn(savedTask);
        when(assembler.toResponse(savedTask)).thenReturn(expected);

        // when
        CrawlTaskResponse result = service.trigger(command);

        // then
        assertThat(result).isEqualTo(expected);
        verify(transactionManager).createAndPersist(command);
    }
}
```

### Persistence Layer 테스트 (Integration)

```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Testcontainers
class CrawlTaskQueryAdapterIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @Autowired
    private CrawlTaskQueryAdapter adapter;

    @Test
    @DisplayName("스케줄 ID와 상태로 존재 여부를 확인한다")
    void existsByScheduleIdAndStatusIn_shouldReturnTrue() {
        // given
        Long scheduleId = 1L;
        // Task 저장...

        // when
        boolean exists = adapter.existsByScheduleIdAndStatusIn(
                scheduleId,
                List.of(CrawlTaskStatus.WAITING)
        );

        // then
        assertThat(exists).isTrue();
    }
}
```

---

## ⚠️ 비기능 요구사항

### 성능

| 항목 | 목표 |
|------|------|
| 트리거 응답 시간 | < 200ms (P95) |
| SQS 발행 지연 | < 100ms |
| 동시 트리거 처리 | 100 requests/sec |

### 안정성

- **Outbox 패턴**: 메시지 유실 방지
- **Idempotency Key**: 중복 발행 방지
- **Fallback Scheduler**: 실패 복구 (1분 주기)

### 확장성

- Worker 수평 확장 지원 (SQS 기반)
- 분산 처리 가능한 아키텍처

---

## 🚀 개발 계획 (TDD 기반)

### Phase 1: Domain Layer

- [ ] CrawlTaskId VO 테스트/구현
- [ ] CrawlTaskStatus Enum 테스트/구현
- [ ] CrawlTaskType Enum 테스트/구현
- [ ] CrawlEndpoint VO 테스트/구현
- [ ] CrawlTask Aggregate 테스트/구현
- [ ] CrawlTaskErrorCode 테스트/구현
- [ ] Domain Exception 테스트/구현

### Phase 2: Application Layer

- [ ] Port-In Interface 정의
- [ ] Port-Out Interface 정의
- [ ] DTO (Command/Query/Response) 구현
- [ ] Assembler 테스트/구현
- [ ] TransactionManager 테스트/구현
- [ ] Service 테스트/구현

### Phase 3: Persistence Layer

- [ ] CrawlTaskEntity 테스트/구현
- [ ] CrawlTaskJpaRepository 구현
- [ ] CrawlTaskQueryDslRepository 테스트/구현
- [ ] CrawlTaskEntityMapper 테스트/구현
- [ ] CommandAdapter 테스트/구현
- [ ] QueryAdapter Integration Test

### Phase 4: REST API Layer

- [ ] API DTO (Request/Response) 구현
- [ ] ApiMapper 테스트/구현
- [ ] CommandController 테스트/구현
- [ ] QueryController 테스트/구현
- [ ] ErrorMapper 구현
- [ ] REST Docs 작성

---

## 📚 참고 문서

- [System Spec](../../guide/system_spec.md)
- [Domain Layer 규칙](../../coding_convention/02-domain-layer/domain-guide.md)
- [Application Layer 규칙](../../coding_convention/03-application-layer/application-guide.md)
- [Persistence Layer 규칙](../../coding_convention/04-persistence-layer/mysql/persistence-mysql-guide.md)
- [REST API Layer 규칙](../../coding_convention/01-adapter-in-layer/rest-api/rest-api-guide.md)

---

## 🔄 시퀀스 다이어그램

```
sequenceDiagram
    participant EB as EventBridge
    participant API as API Server
    participant TM as TransactionManager
    participant DB as RDS
    participant SQS as SQS Queue
    participant Worker as ECS Worker

    EB->>API: POST /api/v1/crawl/tasks/trigger
    API->>TM: trigger(command)

    TM->>DB: [TX Start] Schedule 조회
    alt Schedule 비활성
        TM-->>API: throw SchedulerNotActiveException
        API-->>EB: 409 SCHEDULER_NOT_ACTIVE
    end

    TM->>DB: 기존 Task 존재 확인
    alt 중복 Task 존재
        TM-->>API: throw DuplicateCrawlTaskException
        API-->>EB: 409 DUPLICATE_TASK_EXISTS
    end

    TM->>DB: CrawlTask 저장 (WAITING)
    TM->>DB: [TX Commit]

    TM->>SQS: [afterCommit] 메시지 발행
    TM->>DB: Task 상태 → PUBLISHED

    API-->>EB: 201 Created

    Worker->>SQS: 메시지 폴링
    Worker->>DB: Task 상태 → RUNNING
    Worker->>MustIt: 크롤링 요청
    Worker->>DB: Task 상태 → SUCCESS/FAILED
```

---

**작성자**: Claude
**최종 수정일**: 2025-11-25
**다음 단계**: TDD 사이클에 따라 Domain Layer부터 구현 시작
