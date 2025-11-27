# CRAWL-003: Persistence Layer 구현

**Epic**: Crawl Task Trigger 시스템
**Layer**: Persistence Layer (Adapter-Out)
**브랜치**: feature/CRAWL-003-persistence
**의존성**: CRAWL-002 (Application Layer) 완료 후 시작
**Jira URL**: (sync-to-jira 후 추가)

---

## 📝 목적

CrawlTask 도메인을 MySQL에 저장/조회하는 Persistence Adapter를 구현한다.
JPA Entity, Repository, Adapter를 정의하여 Application Layer의 Port를 구현한다.
CQRS 패턴에 따라 Command(JPA)와 Query(QueryDSL)를 분리한다.

---

## 🎯 요구사항

### JPA Entity
- [ ] **CrawlTaskEntity**
  - `@Entity`, `@Table(name = "crawl_tasks")`
  - Auto Increment PK: `id`
  - Long FK 전략: `crawlScheduleId`, `sellerId` (관계 어노테이션 금지)
  - Enum 매핑: `status`, `taskType` (EnumType.STRING)
  - JSON 저장: `queryParams` (TEXT 컬럼)
  - Index: `(crawl_schedule_id, status)`, `(status, created_at)`
  - BaseAuditEntity 상속
  - Protected 기본 생성자
  - 정적 팩토리 메서드: `from(CrawlTask domain)`
  - Getter만 제공 (Setter 금지)

### Repository (Command)
- [ ] **CrawlTaskJpaRepository**
  - `JpaRepository<CrawlTaskEntity, Long>` 확장
  - 기본 메서드만 사용 (save)
  - 추가 쿼리 메서드 금지

### Repository (Query)
- [ ] **CrawlTaskQueryDslRepository**
  - `JPAQueryFactory` 주입
  - `Optional<CrawlTaskEntity> findById(Long id)`
  - `boolean existsByScheduleIdAndStatusIn(Long scheduleId, List<CrawlTaskStatus> statuses)`
  - `Page<CrawlTaskEntity> findByScheduleId(Long scheduleId, CrawlTaskStatus status, Pageable pageable)`
  - DTO Projection 사용 권장

### Mapper
- [ ] **CrawlTaskEntityMapper**
  - `CrawlTask toDomain(CrawlTaskEntity entity)`
  - `CrawlEndpoint` 복원 (JSON → Map 변환)
  - `CrawlTask.reconstitute()` 사용

### Adapter (Command)
- [ ] **CrawlTaskCommandAdapter**
  - `CrawlTaskPersistencePort` 구현
  - `@Component`
  - `@Transactional` 없음 (Application Layer에서 관리)
  - JpaRepository.save() 호출
  - Mapper로 Domain 변환 후 반환

### Adapter (Query)
- [ ] **CrawlTaskQueryAdapter**
  - `CrawlTaskQueryPort` 구현
  - `@Component`
  - `@Transactional` 없음
  - QueryDslRepository 사용
  - Mapper로 Domain 변환 후 반환

---

## ⚠️ 제약사항

### Zero-Tolerance 규칙
- [ ] **Lombok 금지** - Pure Java 사용
- [ ] **Long FK 전략** - `@ManyToOne`, `@OneToMany` 등 관계 어노테이션 금지
- [ ] **CQRS 분리** - Command=JpaRepository, Query=QueryDslRepository
- [ ] **Setter 금지** - 정적 팩토리 메서드로만 Entity 생성
- [ ] **Transaction 금지** - Adapter에 `@Transactional` 없음

### 테스트 규칙
- [ ] ArchUnit 테스트 필수
- [ ] Repository Unit 테스트 (H2 또는 TestContainers)
- [ ] Adapter Integration 테스트
- [ ] Mapper 테스트
- [ ] TestFixture 사용 필수
- [ ] 테스트 커버리지 > 80%

---

## 📦 패키지 구조

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

---

## 📋 데이터베이스 스키마

```sql
CREATE TABLE crawl_tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    crawl_schedule_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    base_url VARCHAR(255) NOT NULL,
    path VARCHAR(512) NOT NULL,
    query_params TEXT,
    status VARCHAR(20) NOT NULL,
    task_type VARCHAR(20) NOT NULL,
    retry_count INT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,

    INDEX idx_schedule_status (crawl_schedule_id, status),
    INDEX idx_status_created (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

## ✅ 완료 조건

- [ ] 모든 요구사항 구현 완료
- [ ] 모든 Unit 테스트 통과
- [ ] Integration 테스트 통과 (TestContainers)
- [ ] ArchUnit 테스트 통과
- [ ] Zero-Tolerance 규칙 준수 확인
- [ ] 코드 리뷰 승인
- [ ] PR 머지 완료

---

## 🔗 관련 문서

- PRD: [docs/prd/tasks/crawl-task-trigger.md](./crawl-task-trigger.md)
- Plan: docs/prd/plans/CRAWL-003-persistence-plan.md (create-plan 후 생성)
- Persistence Guide: [docs/coding_convention/04-persistence-layer/mysql/persistence-mysql-guide.md](../../coding_convention/04-persistence-layer/mysql/persistence-mysql-guide.md)
- Jira: (sync-to-jira 후 추가)

---

## 🧪 TDD 체크리스트

### Entity 테스트
- [ ] `test: CrawlTaskEntity.from() Domain → Entity 변환`
- [ ] `test: CrawlTaskEntity JSON queryParams 직렬화`
- [ ] `test: CrawlTaskEntity Index 확인 (ArchUnit)`

### Repository 테스트
- [ ] `test: CrawlTaskJpaRepository save 정상 저장`
- [ ] `test: CrawlTaskQueryDslRepository.findById() 정상 조회`
- [ ] `test: CrawlTaskQueryDslRepository.findById() 존재하지 않는 ID`
- [ ] `test: CrawlTaskQueryDslRepository.existsByScheduleIdAndStatusIn() true 반환`
- [ ] `test: CrawlTaskQueryDslRepository.existsByScheduleIdAndStatusIn() false 반환`
- [ ] `test: CrawlTaskQueryDslRepository.findByScheduleId() 페이징 조회`
- [ ] `test: CrawlTaskQueryDslRepository.findByScheduleId() status 필터링`

### Mapper 테스트
- [ ] `test: CrawlTaskEntityMapper.toDomain() Entity → Domain 변환`
- [ ] `test: CrawlTaskEntityMapper JSON → Map 변환`
- [ ] `test: CrawlTaskEntityMapper null queryParams 처리`

### Adapter 테스트
- [ ] `test: CrawlTaskCommandAdapter.persist() 저장 및 Domain 반환`
- [ ] `test: CrawlTaskQueryAdapter.findById() 정상 조회`
- [ ] `test: CrawlTaskQueryAdapter.existsByScheduleIdAndStatusIn() 동작 확인`
- [ ] `test: CrawlTaskQueryAdapter.findByScheduleId() 페이징 동작 확인`

### Integration 테스트 (TestContainers)
- [ ] `test: 전체 저장/조회 흐름 검증`
- [ ] `test: 트랜잭션 롤백 검증`
