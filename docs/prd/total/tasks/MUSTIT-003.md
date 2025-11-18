# MUSTIT-003: Persistence Layer 구현

**Epic**: 머스트잇 셀러 크롤러
**Layer**: Persistence Layer
**브랜치**: feature/MUSTIT-003-persistence
**Jira URL**: (sync-to-jira 후 추가)

---

## 📝 목적

데이터 영속성을 담당하는 Persistence Layer 구현. Application Layer의 Port를 구현하여 MySQL 데이터베이스와 연동합니다.

**핵심 역할**:
- JPA Entity 설계
- Repository 구현 (JPA + QueryDSL)
- Adapter 구현 (Port 구현체)
- 인덱스 최적화 및 N+1 방지

---

## 🎯 요구사항

### 1. JPA Entity 설계

#### SellerJpaEntity

- [ ] **테이블 및 필드**
  - 테이블: `sellers`
  - 필드:
    - id: Long (PK, Auto Increment)
    - seller_id: String (Unique, Not Null, Index)
    - name: String (Not Null)
    - crawling_interval_days: Integer (Not Null, CHECK > 0)
    - status: String (Not Null, Index)
    - total_product_count: Integer (Default 0)
    - created_at, updated_at: LocalDateTime

- [ ] **인덱스**
  - `idx_seller_id` (seller_id) - Unique
  - `idx_status` (status)

- [ ] **제약 조건**
  - CHECK (crawling_interval_days > 0)

#### CrawlerTaskJpaEntity

- [ ] **테이블 및 필드**
  - 테이블: `crawler_tasks`
  - 필드:
    - id: Long (PK, Auto Increment)
    - task_id: String (UUID, Unique, Not Null, Index)
    - seller_id: String (FK, Not Null, Index)
    - task_type: String (Not Null)
    - request_url: String (Not Null)
    - status: String (Not Null, Index)
    - retry_count: Integer (Default 0)
    - error_message: String (Nullable)
    - created_at, published_at, started_at, completed_at: LocalDateTime

- [ ] **인덱스**
  - `idx_task_id` (task_id) - Unique
  - `idx_seller_id_created_at` (seller_id, created_at DESC)
  - `idx_status_created_at` (status, created_at DESC)

- [ ] **파티셔닝 전략 검토**
  - TODO: `created_at` 기준 월별 파티셔닝 (1년 후 데이터 증가 시)

#### UserAgentJpaEntity

- [ ] **테이블 및 필드**
  - 테이블: `user_agents`
  - 필드:
    - id: Long (PK, Auto Increment)
    - user_agent_id: String (UUID, Unique, Not Null, Index)
    - user_agent_string: String (Not Null)
    - token: String (Nullable, 길이 500)
    - status: String (Not Null, Index)
    - request_count: Integer (Default 0)
    - last_request_at, token_issued_at, created_at: LocalDateTime

- [ ] **인덱스**
  - `idx_user_agent_id` (user_agent_id) - Unique
  - `idx_status` (status)

#### ProductJpaEntity

- [ ] **테이블 및 필드**
  - 테이블: `products`
  - 필드:
    - id: Long (PK, Auto Increment)
    - product_id: String (UUID, Unique, Not Null, Index)
    - item_no: Long (Unique, Not Null, Index)
    - seller_id: String (FK, Not Null, Index)
    - minishop_data_hash, detail_data_hash, option_data_hash: String (Nullable, MD5 해시)
    - is_complete: Boolean (Default false, Index)
    - created_at, updated_at: LocalDateTime

- [ ] **인덱스**
  - `idx_product_id` (product_id) - Unique
  - `idx_item_no` (item_no) - Unique
  - `idx_seller_id_is_complete` (seller_id, is_complete)

#### ProductRawDataJpaEntity

- [ ] **테이블 및 필드**
  - 테이블: `product_raw_data`
  - 필드:
    - id: Long (PK, Auto Increment)
    - product_id: String (FK, Not Null, Index)
    - data_type: String (MINISHOP, PRODUCT_DETAIL, PRODUCT_OPTION, Not Null)
    - raw_json: String (TEXT, Not Null)
    - created_at: LocalDateTime (Not Null)

- [ ] **인덱스**
  - `idx_product_id_data_type` (product_id, data_type)

- [ ] **데이터 저장 전략**
  - RDB(MySQL)에만 저장 (S3 사용 안 함)
  - TEXT 타입으로 JSON 저장

#### ProductOutboxJpaEntity

- [ ] **테이블 및 필드**
  - 테이블: `product_outbox`
  - 필드:
    - id: Long (PK, Auto Increment)
    - outbox_id: String (UUID, Unique, Not Null, Index)
    - product_id: String (FK, Not Null, Index)
    - event_type: String (Not Null)
    - payload: String (TEXT, Not Null)
    - status: String (Not Null, Index)
    - retry_count: Integer (Default 0)
    - error_message: String (Nullable)
    - created_at, sent_at: LocalDateTime

- [ ] **인덱스**
  - `idx_outbox_id` (outbox_id) - Unique
  - `idx_status_created_at` (status, created_at ASC) - 배치 처리 (오래된 순)

#### CrawlingScheduleJpaEntity ⬅️ **신규 추가**

- [ ] **테이블 및 필드**
  - 테이블: `crawling_schedules`
  - 필드:
    - id: Long (PK, Auto Increment)
    - schedule_id: String (UUID, Unique, Not Null, Index)
    - seller_id: String (FK, Not Null, Index)
    - schedule_rule: String (Not Null, EventBridge Rule Name)
    - schedule_expression: String (Not Null, Cron 표현식)
    - status: String (Not Null, Index, ACTIVE/INACTIVE/FAILED)
    - created_at, updated_at: LocalDateTime

- [ ] **인덱스**
  - `idx_schedule_id` (schedule_id) - Unique
  - `idx_seller_id` (seller_id) - Unique (1 Seller = 1 Schedule)
  - `idx_status` (status)

#### CrawlingScheduleExecutionJpaEntity ⬅️ **신규 추가**

- [ ] **테이블 및 필드**
  - 테이블: `crawling_schedule_executions`
  - 필드:
    - id: Long (PK, Auto Increment)
    - execution_id: String (UUID, Unique, Not Null, Index)
    - schedule_id: String (FK, Not Null, Index)
    - seller_id: String (FK, Not Null, Index)
    - status: String (Not Null, Index, STARTED/IN_PROGRESS/COMPLETED/FAILED)
    - total_tasks_created: Integer (Default 0)
    - completed_tasks: Integer (Default 0)
    - failed_tasks: Integer (Default 0)
    - progress_rate: Double (진행률 %, Nullable)
    - success_rate: Double (성공률 %, Nullable)
    - started_at: LocalDateTime (Not Null, Index)
    - completed_at: LocalDateTime (Nullable)
    - error_message: String (Nullable, TEXT)

- [ ] **인덱스**
  - `idx_execution_id` (execution_id) - Unique
  - `idx_schedule_id_started_at` (schedule_id, started_at DESC) - 스케줄별 히스토리
  - `idx_seller_id_started_at` (seller_id, started_at DESC) - 셀러별 히스토리
  - `idx_status` (status)

- [ ] **파티셔닝 전략**
  - `started_at` 기준 월별 파티셔닝 (PARTITION BY RANGE)
  - 히스토리 데이터 증가 시 적용

#### SchedulerOutboxJpaEntity ⬅️ **신규 추가**

- [ ] **테이블 및 필드**
  - 테이블: `scheduler_outbox`
  - 필드:
    - id: Long (PK, Auto Increment)
    - outbox_id: String (UUID, Unique, Not Null, Index)
    - schedule_id: String (FK, Not Null, Index)
    - event_type: String (Not Null, SCHEDULE_CREATED/SCHEDULE_UPDATED/SCHEDULE_DELETED)
    - payload: String (TEXT, Not Null, EventBridge API JSON)
    - status: String (Not Null, Index, WAITING/SENDING/COMPLETED/FAILED)
    - retry_count: Integer (Default 0)
    - error_message: String (Nullable, TEXT)
    - created_at: LocalDateTime (Not Null, Index)
    - sent_at: LocalDateTime (Nullable)

- [ ] **인덱스**
  - `idx_outbox_id` (outbox_id) - Unique
  - `idx_status_created_at` (status, created_at ASC) - 배치 처리 (오래된 순)

---

### 2. Repository 구현

#### JPA Repository

- [ ] **SellerJpaRepository**
  ```java
  public interface SellerJpaRepository extends JpaRepository<SellerJpaEntity, Long> {
      Optional<SellerJpaEntity> findBySellerId(String sellerId);
      List<SellerJpaEntity> findByStatus(String status);
      boolean existsBySellerId(String sellerId);
  }
  ```

- [ ] **CrawlerTaskJpaRepository**
  ```java
  public interface CrawlerTaskJpaRepository extends JpaRepository<CrawlerTaskJpaEntity, Long> {
      Optional<CrawlerTaskJpaEntity> findByTaskId(String taskId);
      List<CrawlerTaskJpaEntity> findByStatus(String status, Pageable pageable);
  }
  ```

- [ ] **UserAgentJpaRepository**
  ```java
  public interface UserAgentJpaRepository extends JpaRepository<UserAgentJpaEntity, Long> {
      List<UserAgentJpaEntity> findByStatus(String status);

      @Lock(LockModeType.PESSIMISTIC_WRITE)
      @Query("SELECT ua FROM UserAgentJpaEntity ua WHERE ua.status = 'ACTIVE' ORDER BY ua.lastRequestAt ASC")
      Optional<UserAgentJpaEntity> findFirstActiveUserAgentForUpdate();
  }
  ```

- [ ] **ProductJpaRepository**
  ```java
  public interface ProductJpaRepository extends JpaRepository<ProductJpaEntity, Long> {
      Optional<ProductJpaEntity> findByItemNo(Long itemNo);
      List<ProductJpaEntity> findBySellerIdAndIsComplete(String sellerId, boolean isComplete);
  }
  ```

- [ ] **ProductOutboxJpaRepository**
  ```java
  public interface ProductOutboxJpaRepository extends JpaRepository<ProductOutboxJpaEntity, Long> {
      List<ProductOutboxJpaEntity> findByStatusOrderByCreatedAtAsc(String status, Pageable pageable);
  }
  ```

- [ ] **CrawlingScheduleJpaRepository** ⬅️ **신규 추가**
  ```java
  public interface CrawlingScheduleJpaRepository extends JpaRepository<CrawlingScheduleJpaEntity, Long> {
      Optional<CrawlingScheduleJpaEntity> findByScheduleId(String scheduleId);
      Optional<CrawlingScheduleJpaEntity> findBySellerId(String sellerId);
      List<CrawlingScheduleJpaEntity> findByStatus(String status);
  }
  ```

- [ ] **CrawlingScheduleExecutionJpaRepository** ⬅️ **신규 추가**
  ```java
  public interface CrawlingScheduleExecutionJpaRepository extends JpaRepository<CrawlingScheduleExecutionJpaEntity, Long> {
      Optional<CrawlingScheduleExecutionJpaEntity> findByExecutionId(String executionId);
      List<CrawlingScheduleExecutionJpaEntity> findByScheduleIdOrderByStartedAtDesc(String scheduleId, Pageable pageable);
      List<CrawlingScheduleExecutionJpaEntity> findBySellerIdOrderByStartedAtDesc(String sellerId, Pageable pageable);
      List<CrawlingScheduleExecutionJpaEntity> findByStatus(String status);
  }
  ```

- [ ] **SchedulerOutboxJpaRepository** ⬅️ **신규 추가**
  ```java
  public interface SchedulerOutboxJpaRepository extends JpaRepository<SchedulerOutboxJpaEntity, Long> {
      List<SchedulerOutboxJpaEntity> findByStatusOrderByCreatedAtAsc(String status, Pageable pageable);
      Optional<SchedulerOutboxJpaEntity> findByOutboxId(String outboxId);
  }
  ```

#### QueryDSL Repository

- [ ] **CrawlerTaskQueryDslRepository**
  - `findBySellerIdAndDateRange(sellerId, startDate, endDate)`: 셀러별 기간 조회
  - `countBySellerIdAndStatusAndDate(sellerId, status, date)`: 메트릭 계산용 집계
  - DTO Projection 사용 (N+1 방지)

- [ ] **QueryDSL 설정**
  - QClass 생성
  - JPAQueryFactory 빈 등록
  - 복잡한 조회 쿼리 최적화

---

### 3. Adapter 구현 (Port 구현체)

#### Command Adapters

- [ ] **SellerCommandAdapter**
  - 구현 Port: `SellerCommandPort`
  - 메서드:
    - `save(Seller)`: Seller → SellerJpaEntity 변환 → 저장
    - `delete(SellerId)`: 삭제

- [ ] **CrawlerTaskCommandAdapter**
  - 구현 Port: `CrawlerTaskCommandPort`
  - 메서드:
    - `save(CrawlerTask)`: 저장
    - `saveAll(List<CrawlerTask>)`: Bulk Insert

- [ ] **UserAgentCommandAdapter**
  - 구현 Port: `UserAgentCommandPort`
  - 메서드:
    - `save(UserAgent)`: 저장

- [ ] **ProductCommandAdapter**
  - 구현 Port: `ProductCommandPort`
  - 메서드:
    - `save(Product)`: 저장

- [ ] **ProductOutboxCommandAdapter**
  - 구현 Port: `ProductOutboxCommandPort`
  - 메서드:
    - `save(ProductOutbox)`: 저장

#### Query Adapters

- [ ] **SellerQueryAdapter**
  - 구현 Port: `SellerQueryPort`
  - 메서드:
    - `findById(SellerId)`: 조회
    - `findByStatus(SellerStatus)`: 상태별 조회
    - `existsBySellerId(SellerId)`: 존재 여부 확인

- [ ] **CrawlerTaskQueryAdapter**
  - 구현 Port: `CrawlerTaskQueryPort`
  - 메서드:
    - `findById(TaskId)`: 조회
    - `findByStatus(CrawlerTaskStatus, Pageable)`: 상태별 조회
    - `findBySellerIdAndDateRange(SellerId, startDate, endDate)`: 기간별 조회
    - `countBySellerIdAndStatusAndDate(SellerId, status, date)`: 메트릭 집계

- [ ] **UserAgentQueryAdapter**
  - 구현 Port: `UserAgentQueryPort`
  - 메서드:
    - `findById(UserAgentId)`: 조회
    - `findByStatus(UserAgentStatus)`: 상태별 조회
    - `findFirstActiveForUpdate()`: Pessimistic Lock 조회

- [ ] **ProductQueryAdapter**
  - 구현 Port: `ProductQueryPort`
  - 메서드:
    - `findByItemNo(Long)`: 상품 번호로 조회

- [ ] **ProductOutboxQueryAdapter**
  - 구현 Port: `ProductOutboxQueryPort`
  - 메서드:
    - `findByStatusOrderByCreatedAtAsc(OutboxStatus, Pageable)`: 배치 처리용 조회

---

### 4. Mapper (Domain ↔ Entity 변환)

- [ ] **SellerMapper**
  - `toDomain(SellerJpaEntity)`: Entity → Domain
  - `toEntity(Seller)`: Domain → Entity

- [ ] **CrawlerTaskMapper**
  - `toDomain(CrawlerTaskJpaEntity)`: Entity → Domain
  - `toEntity(CrawlerTask)`: Domain → Entity

- [ ] **UserAgentMapper**
  - `toDomain(UserAgentJpaEntity)`: Entity → Domain
  - `toEntity(UserAgent)`: Domain → Entity

- [ ] **ProductMapper**
  - `toDomain(ProductJpaEntity)`: Entity → Domain
  - `toEntity(Product)`: Domain → Entity

- [ ] **ProductOutboxMapper**
  - `toDomain(ProductOutboxJpaEntity)`: Entity → Domain
  - `toEntity(ProductOutbox)`: Domain → Entity

---

### 5. Flyway 마이그레이션

- [ ] **초기 스키마 생성**
  - `V1__create_sellers_table.sql`
  - `V2__create_crawler_tasks_table.sql`
  - `V3__create_user_agents_table.sql`
  - `V4__create_products_table.sql`
  - `V5__create_product_raw_data_table.sql`
  - `V6__create_product_outbox_table.sql`

- [ ] **인덱스 생성**
  - `V7__create_indexes.sql`

- [ ] **초기 데이터 삽입**
  - `V8__insert_initial_user_agents.sql` (50개 UserAgent)

---

## ⚠️ 제약사항

### Zero-Tolerance 규칙

- [ ] **Long FK 전략**
  - ✅ `private String sellerId;` (String FK)
  - ❌ `@ManyToOne private Seller seller;` (JPA 관계 어노테이션 금지)

- [ ] **QueryDSL 최적화 (N+1 방지)**
  - DTO Projection 사용
  - Join Fetch 사용
  - 불필요한 쿼리 제거

- [ ] **Lombok 금지**
  - Entity는 Pure Java 또는 Record 사용
  - Getter/Setter 직접 구현

- [ ] **Pessimistic Lock 사용**
  - UserAgent 할당 시 `SELECT FOR UPDATE`
  - Race Condition 방지

### 테스트 규칙

- [ ] **Integration Test (TestContainers)**
  - MySQL TestContainer 사용
  - 실제 DB 환경에서 테스트
  - Flyway 마이그레이션 자동 실행

- [ ] **ArchUnit 테스트**
  - Long FK 전략 검증
  - JPA 관계 어노테이션 금지 검증
  - Lombok 사용 금지 검증

- [ ] **테스트 커버리지 > 80%**
  - Repository 메서드 모두 테스트
  - Adapter 변환 로직 테스트
  - Mapper 변환 로직 테스트

---

## ✅ 완료 조건

- [ ] 6개 JPA Entity 구현 완료
- [ ] 5개 JPA Repository 구현 완료
- [ ] 1개 QueryDSL Repository 구현 완료
- [ ] 5개 Command Adapter 구현 완료
- [ ] 5개 Query Adapter 구현 완료
- [ ] 5개 Mapper 구현 완료
- [ ] Flyway 마이그레이션 스크립트 작성 완료
- [ ] Integration Test 작성 완료 (TestContainers, 커버리지 > 80%)
- [ ] ArchUnit 테스트 통과
- [ ] QueryDSL 최적화 검증 (N+1 방지)
- [ ] Pessimistic Lock 동시성 테스트 통과
- [ ] Zero-Tolerance 규칙 준수
- [ ] 코드 리뷰 승인
- [ ] PR 머지 완료

---

## 🔗 관련 문서

- **PRD**: docs/prd/mustit-seller-crawler.md
- **Plan**: docs/prd/plans/MUSTIT-003-persistence-plan.md (create-plan 후 생성)
- **Jira**: (sync-to-jira 후 추가)
- **Persistence Layer 규칙**: docs/coding_convention/04-persistence-layer/

---

## 📚 참고사항

### Flyway 마이그레이션 예시

```sql
-- V1__create_sellers_table.sql
CREATE TABLE sellers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    seller_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    crawling_interval_days INT NOT NULL CHECK (crawling_interval_days > 0),
    status VARCHAR(50) NOT NULL,
    total_product_count INT DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    UNIQUE KEY uk_seller_id (seller_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### QueryDSL DTO Projection 예시

```java
// CrawlerTaskQueryDslRepository
public List<CrawlerTaskMetricsDto> countBySellerIdAndStatusAndDate(
    String sellerId, LocalDate date) {

    LocalDateTime startOfDay = date.atStartOfDay();
    LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

    return queryFactory
        .select(Projections.constructor(
            CrawlerTaskMetricsDto.class,
            crawlerTask.status,
            crawlerTask.count()
        ))
        .from(crawlerTask)
        .where(
            crawlerTask.sellerId.eq(sellerId),
            crawlerTask.createdAt.between(startOfDay, endOfDay)
        )
        .groupBy(crawlerTask.status)
        .fetch();
}
```

### Pessimistic Lock 예시

```java
// UserAgentJpaRepository
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT ua FROM UserAgentJpaEntity ua WHERE ua.status = 'ACTIVE' ORDER BY ua.lastRequestAt ASC")
Optional<UserAgentJpaEntity> findFirstActiveUserAgentForUpdate();
```

### Bulk Insert 최적화

```java
// CrawlerTaskCommandAdapter
@Transactional
public void saveAll(List<CrawlerTask> tasks) {
    List<CrawlerTaskJpaEntity> entities = tasks.stream()
        .map(mapper::toEntity)
        .toList();

    // Batch Insert (한 번에 저장)
    crawlerTaskJpaRepository.saveAll(entities);
}
```

### TestContainers 설정

```java
@SpringBootTest
@Testcontainers
class SellerCommandAdapterTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Test
    void save_seller_success() {
        // Given
        Seller seller = Seller.register("seller_123", "셀러명", 1);

        // When
        sellerCommandAdapter.save(seller);

        // Then
        Optional<Seller> found = sellerQueryAdapter.findById(seller.getSellerId());
        assertThat(found).isPresent();
    }
}
```

### 인덱스 최적화 전략

1. **Unique Index**: seller_id, task_id, user_agent_id, product_id, item_no, outbox_id
2. **Composite Index**: (seller_id, created_at), (status, created_at), (seller_id, is_complete)
3. **Covering Index**: 자주 조회되는 컬럼 포함
4. **파티셔닝**: crawler_tasks 테이블 (1년 후 월별 파티셔닝 검토)
