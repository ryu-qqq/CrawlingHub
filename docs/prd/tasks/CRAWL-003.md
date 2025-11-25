# CRAWL-003: Persistence Layer 구현

**Epic**: Crawl Task Trigger
**Layer**: Persistence Layer
**브랜치**: feature/CRAWL-003-persistence
**Jira URL**: (sync-to-jira 후 추가)

---

## 📝 목적

CrawlTask 및 CrawlTaskOutBox의 JPA Entity, Repository, Adapter를 구현하여 데이터 저장소 계층을 담당합니다.

---

## 🎯 요구사항

### JPA Entity

**CrawlTaskJpaEntity**:
- [ ] 테이블: `crawl_tasks`
- [ ] 필드:
  - `id`: Long (PK, Auto Increment)
  - `crawl_task_id`: String (UUID, Unique, Not Null)
  - `crawl_scheduler_id`: Long (FK, Not Null)
  - `seller_id`: Long (FK, Not Null)
  - `request_url`: String (Not Null)
  - `status`: String (Not Null)
  - `retry_count`: Integer (Default 0)
  - `created_at`: LocalDateTime (Not Null)
  - `updated_at`: LocalDateTime (Not Null)
- [ ] BaseAuditEntity 상속
- [ ] Index: `idx_scheduler_status`, `idx_status_created_at`

**CrawlTaskOutBoxJpaEntity**:
- [ ] 테이블: `crawl_task_outbox`
- [ ] 필드:
  - `id`: Long (PK, Auto Increment)
  - `crawl_task_outbox_id`: String (UUID, Unique, Not Null)
  - `crawl_task_id`: String (FK, Not Null)
  - `idempotency_key`: String (Unique, Not Null)
  - `message_payload`: Text (JSON, Not Null)
  - `status`: String (Not Null)
  - `retry_count`: Integer (Default 0)
  - `created_at`: LocalDateTime (Not Null)
  - `processed_at`: LocalDateTime (Nullable)
- [ ] Index: `idx_status_retry`

### Repository

**CrawlTaskJpaRepository**:
```java
public interface CrawlTaskJpaRepository extends JpaRepository<CrawlTaskJpaEntity, Long> {
    Optional<CrawlTaskJpaEntity> findByCrawlTaskId(String crawlTaskId);
    boolean existsByCrawlSchedulerIdAndStatusIn(Long schedulerId, List<String> statuses);
    List<CrawlTaskJpaEntity> findByCrawlSchedulerIdOrderByCreatedAtDesc(Long schedulerId);
}
```

**CrawlTaskOutBoxJpaRepository**:
```java
public interface CrawlTaskOutBoxJpaRepository extends JpaRepository<CrawlTaskOutBoxJpaEntity, Long> {
    Optional<CrawlTaskOutBoxJpaEntity> findByCrawlTaskId(String crawlTaskId);
    List<CrawlTaskOutBoxJpaEntity> findByStatusAndRetryCountLessThan(String status, int maxRetry);
}
```

### QueryDSL Repository (필요시)

**CrawlTaskQueryDslRepository**:
- [ ] 복잡한 조회 쿼리 (페이징, 필터링)
- [ ] DTO Projection 사용

### Adapter

**CrawlTaskCommandAdapter** (implements CrawlTaskPersistPort):
- [ ] `save(CrawlTask): CrawlTask`
- [ ] Domain → Entity 변환 (Mapper 사용)

**CrawlTaskQueryAdapter** (implements CrawlTaskQueryPort):
- [ ] `findById(CrawlTaskId): Optional<CrawlTask>`
- [ ] `existsBySchedulerIdAndStatusIn(Long, List<CrawlTaskStatus>): boolean`
- [ ] Entity → Domain 변환 (Mapper 사용)

**CrawlTaskOutBoxCommandAdapter** (implements CrawlTaskOutBoxPersistPort):
- [ ] `save(CrawlTaskOutBox): CrawlTaskOutBox`
- [ ] `updateStatus(CrawlTaskOutBoxId, CrawlTaskOutBoxStatus): void`

**CrawlTaskOutBoxQueryAdapter** (implements CrawlTaskOutBoxQueryPort):
- [ ] `findPendingWithRetryLessThan(int maxRetry): List<CrawlTaskOutBox>`

### Mapper

**CrawlTaskPersistenceMapper**:
- [ ] `toEntity(CrawlTask): CrawlTaskJpaEntity`
- [ ] `toDomain(CrawlTaskJpaEntity): CrawlTask`

**CrawlTaskOutBoxPersistenceMapper**:
- [ ] `toEntity(CrawlTaskOutBox): CrawlTaskOutBoxJpaEntity`
- [ ] `toDomain(CrawlTaskOutBoxJpaEntity): CrawlTaskOutBox`

### Flyway Migration

**V{version}__create_crawl_task_tables.sql**:
```sql
CREATE TABLE crawl_tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    crawl_task_id VARCHAR(36) NOT NULL UNIQUE,
    crawl_scheduler_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    request_url VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL,
    retry_count INT DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    INDEX idx_scheduler_status (crawl_scheduler_id, status),
    INDEX idx_status_created_at (status, created_at)
);

CREATE TABLE crawl_task_outbox (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    crawl_task_outbox_id VARCHAR(36) NOT NULL UNIQUE,
    crawl_task_id VARCHAR(36) NOT NULL,
    idempotency_key VARCHAR(100) NOT NULL UNIQUE,
    message_payload TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    retry_count INT DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    processed_at DATETIME(6),
    INDEX idx_status_retry (status, retry_count)
);
```

---

## ⚠️ 제약사항

### Zero-Tolerance 규칙
- [ ] Long FK 전략 - JPA 관계 어노테이션 금지
- [ ] Lombok 금지 - Entity도 Pure Java
- [ ] QueryDSL DTO Projection 사용 (N+1 방지)

### 테스트 규칙
- [ ] ArchUnit 테스트 필수 (JpaEntityArchTest, RepositoryArchTest)
- [ ] TestContainers MySQL 사용
- [ ] @DataJpaTest 또는 Integration Test

---

## ✅ 완료 조건

- [ ] JPA Entity 구현 완료
- [ ] Repository 구현 완료
- [ ] Adapter 구현 완료 (Port 구현체)
- [ ] Mapper 구현 완료
- [ ] Flyway Migration 작성 완료
- [ ] Integration Test 통과 (TestContainers)
- [ ] ArchUnit Test 통과
- [ ] Zero-Tolerance 규칙 준수
- [ ] 코드 리뷰 승인
- [ ] PR 머지 완료

---

## 🔗 관련 문서

- PRD: docs/prd/crawl-task-trigger.md
- Plan: docs/prd/plans/CRAWL-003-persistence-plan.md (create-plan 후 생성)
- Jira: (sync-to-jira 후 추가)
