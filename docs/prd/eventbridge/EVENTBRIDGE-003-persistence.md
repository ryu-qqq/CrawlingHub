# EVENTBRIDGE-003: EventBridge Persistence Layer 구현

**Epic**: 머스트잇 셀러 크롤링 스케줄러
**Bounded Context**: EventBridge (Crawling Scheduler)
**Layer**: Persistence
**브랜치**: feature/EVENTBRIDGE-003-persistence
**Jira URL**: (sync-to-jira 후 추가)

---

## 📝 목적

EventBridge 바운더리 컨텍스트의 데이터 저장 및 조회를 Persistence Layer에서 구현합니다.

**핵심 기능**:
- JPA Entity 설계 (Long FK 전략)
- Repository 구현 (JPA + QueryDSL)
- Port Adapter 구현
- 동시성 제어 (Pessimistic Lock)

---

## 🎯 요구사항

### JPA Entity

#### CrawlingSchedulerJpaEntity

- [ ] **테이블**: `crawling_schedulers`
- [ ] **BaseAuditEntity 상속 필수**
  - `createdAt`, `updatedAt` 자동 관리
  - `@MappedSuperclass` 상속

- [ ] **필드**:
  - `id`: Long (PK, Auto Increment)
  - `seller_id`: Long (FK, Not Null, Index)
  - `scheduler_name`: String (Not Null)
  - `cron_expression`: String (Not Null)
  - `status`: String (Not Null, Index)
  - `event_bridge_rule_name`: String (Nullable, Unique)
  - ~~`created_at`, `updated_at`~~: BaseAuditEntity에서 상속

- [ ] **인덱스**:
  - `idx_seller_id_scheduler_name` (seller_id, scheduler_name) - Unique Composite
  - `idx_seller_id_status` (seller_id, status) - 필터링용
  - `idx_status` (status) - Outbox Processor용

- [ ] **Unique Constraint**:
  - `(seller_id, scheduler_name)` - Composite Unique
  - `event_bridge_rule_name` (Nullable Unique)

#### SchedulerHistoryJpaEntity

- [ ] **테이블**: `scheduler_histories`
- [ ] **필드**:
  - `id`: Long (PK, Auto Increment)
  - `scheduler_id`: Long (FK, Not Null, Index)
  - `changed_field`: String (Not Null)
  - `old_value`: String (Nullable)
  - `new_value`: String (Not Null)
  - `changed_at`: LocalDateTime (Not Null, Index)

- [ ] **인덱스**:
  - `idx_scheduler_id_changed_at` (scheduler_id, changed_at DESC)

#### SchedulerOutboxEventJpaEntity

- [ ] **테이블**: `scheduler_outbox_events`
- [ ] **필드**:
  - `id`: Long (PK, Auto Increment)
  - `event_type`: String (Not Null)
  - `scheduler_id`: Long (FK, Not Null, Index)
  - `payload`: Text (JSON, Not Null)
  - `status`: String (Not Null, Index)
  - `retry_count`: Integer (Not Null, Default 0)
  - `max_retries`: Integer (Not Null, Default 3)
  - `created_at`: LocalDateTime (Not Null, Index)
  - `processed_at`: LocalDateTime (Nullable)
  - `error_message`: Text (Nullable)

- [ ] **인덱스**:
  - `idx_status_retry_count` (status, retry_count) - Outbox Processor용
  - `idx_status_created_at` (status, created_at) - 정리용

- [ ] **Optimistic Lock**:
  - `@Version` 필드 추가 (Spring Scheduler 중복 실행 방지)

### Repository 인터페이스

#### CrawlingSchedulerJpaRepository

- [ ] **메서드**:
  ```java
  public interface CrawlingSchedulerJpaRepository extends JpaRepository<CrawlingSchedulerJpaEntity, Long> {
      Optional<CrawlingSchedulerJpaEntity> findBySellerIdAndSchedulerName(Long sellerId, String schedulerName);
      List<CrawlingSchedulerJpaEntity> findBySellerIdAndStatus(Long sellerId, SchedulerStatus status);
      boolean existsBySellerIdAndSchedulerName(Long sellerId, String schedulerName);
  }
  ```

- [ ] **Pessimistic Lock 메서드**:
  ```java
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT s FROM CrawlingSchedulerJpaEntity s WHERE s.sellerId = :sellerId")
  Optional<CrawlingSchedulerJpaEntity> findBySellerIdWithLock(@Param("sellerId") Long sellerId);
  ```

#### CrawlingSchedulerQueryDslRepository

- [ ] **메서드**:
  - `Page<CrawlingSchedulerJpaEntity> findAllBySellerIdAndStatus(sellerId, status, Pageable)`
  - `Page<CrawlingSchedulerJpaEntity> findAllByStatus(status, Pageable)`
  - `int countBySellerIdAndStatus(sellerId, status)`

- [ ] **QueryDSL DTO Projection 필수**
  - Entity 조회 금지 (N+1 발생 가능)
  - DTO로 직접 Projection
  ```java
  return queryFactory
      .select(Projections.constructor(SchedulerDto.class,
          scheduler.id,
          scheduler.sellerId,
          scheduler.schedulerName,
          scheduler.cronExpression,
          scheduler.status
      ))
      .from(scheduler)
      .where(scheduler.sellerId.eq(sellerId))
      .fetch();
  ```

- [ ] **N+1 방지 전략**:
  - Seller 정보 필요 시: 별도 조회 후 Application Layer에서 재조립
  - 연관관계 사용 금지 (Long FK 전략)

#### SchedulerOutboxEventJpaRepository

- [ ] **메서드**:
  ```java
  public interface SchedulerOutboxEventJpaRepository extends JpaRepository<SchedulerOutboxEventJpaEntity, Long> {
      List<SchedulerOutboxEventJpaEntity> findByStatusAndRetryCountLessThan(
          OutboxStatus status,
          Integer maxRetries
      );
      List<SchedulerOutboxEventJpaEntity> findByStatusAndCreatedAtBefore(
          OutboxStatus status,
          LocalDateTime before
      );
      void deleteByStatusAndCreatedAtBefore(
          OutboxStatus status,
          LocalDateTime before
      );
  }
  ```

#### SchedulerHistoryJpaRepository

- [ ] **메서드**:
  ```java
  public interface SchedulerHistoryJpaRepository extends JpaRepository<SchedulerHistoryJpaEntity, Long> {
      Page<SchedulerHistoryJpaEntity> findBySchedulerIdOrderByChangedAtDesc(
          Long schedulerId,
          Pageable pageable
      );
  }
  ```

### Adapter 구현 (Port 구현체)

#### SchedulerCommandAdapter

- [ ] **구현 Port**: `SchedulerCommandPort`
- [ ] **메서드**:
  - `Scheduler save(Scheduler scheduler)`
  - `void delete(Long schedulerId)`

#### SchedulerQueryAdapter

- [ ] **구현 Port**: `SchedulerQueryPort`
- [ ] **메서드**:
  - `Optional<Scheduler> findById(Long schedulerId)`
  - `Optional<Scheduler> findBySellerIdAndSchedulerName(Long sellerId, String schedulerName)`
  - `List<Scheduler> findBySellerIdAndStatus(Long sellerId, SchedulerStatus status)`
  - `Page<Scheduler> findAllBySellerIdAndStatus(Long sellerId, SchedulerStatus status, Pageable)`
  - `int countActiveSchedulersBySellerId(Long sellerId)`

#### OutboxEventCommandAdapter

- [ ] **구현 Port**: `OutboxEventCommandPort`

#### OutboxEventQueryAdapter

- [ ] **구현 Port**: `OutboxEventQueryPort`

#### SchedulerHistoryCommandAdapter

- [ ] **구현 Port**: `SchedulerHistoryCommandPort`

#### SchedulerHistoryQueryAdapter

- [ ] **구현 Port**: `SchedulerHistoryQueryPort`

### Mapper

#### SchedulerMapper

- [ ] **메서드**:
  - `CrawlingSchedulerJpaEntity toJpaEntity(Scheduler scheduler)`
  - `Scheduler toDomain(CrawlingSchedulerJpaEntity entity)`

#### OutboxEventMapper

- [ ] **메서드**:
  - `SchedulerOutboxEventJpaEntity toJpaEntity(OutboxEvent event)`
  - `OutboxEvent toDomain(SchedulerOutboxEventJpaEntity entity)`

#### SchedulerHistoryMapper

- [ ] **메서드**:
  - `SchedulerHistoryJpaEntity toJpaEntity(SchedulerHistory history)`
  - `SchedulerHistory toDomain(SchedulerHistoryJpaEntity entity)`

### 동시성 제어

- [ ] **Scheduler 등록 시 동시 요청 처리**:
  - 전략: Pessimistic Lock (`SELECT FOR UPDATE`)
  - 이유: 동일 Seller 내 동일 schedulerName 중복 방지
  - 적용: `findBySellerIdWithLock`

- [ ] **Outbox Event 처리 시 동시성**:
  - 전략: Optimistic Lock (`@Version`)
  - 이유: Spring Scheduler 중복 실행 방지

---

## ⚠️ 제약사항

### Zero-Tolerance 규칙

- [ ] **Long FK 전략 (필수!)**
  - `private Long sellerId;` (O)
  - `@ManyToOne private Seller seller;` (X)

- [ ] **Lombok 금지**
  - Entity, Mapper, Adapter 모두 Pure Java

- [ ] **MapStruct 금지**
  - Mapper는 명시적 변환 메서드 작성
  - Static 유틸리티 클래스 사용 금지
  - 생성자/정적 팩토리 메서드로 변환

- [ ] **QueryDSL DTO Projection 필수**
  - Entity 조회 후 변환 금지 (N+1 발생)
  - DTO로 직접 Projection

- [ ] **BaseAuditEntity 상속 필수**
  - `createdAt`, `updatedAt` 자동 관리

- [ ] **Setter 금지**
  - Entity는 생성자/정적 팩토리로만 생성
  - 상태 변경은 비즈니스 메서드

### 테스트 규칙

- [ ] **Integration Test 필수**
  - TestContainers MySQL

- [ ] **Unique Constraint 테스트**
  - 중복 `(seller_id, scheduler_name)` 저장 시 예외
  - 중복 `event_bridge_rule_name` 저장 시 예외

- [ ] **Pessimistic Lock 테스트**
  - 동시 스케줄 등록 시 Lock 동작 검증

- [ ] **Optimistic Lock 테스트**
  - Outbox Event 동시 수정 시 예외 발생 검증

- [ ] **QueryDSL 쿼리 테스트**
  - N+1 방지 검증

- [ ] **테스트 커버리지 > 80%**

---

## ✅ 완료 조건

- [ ] JPA Entity 구현 완료
  - CrawlingSchedulerJpaEntity
  - SchedulerHistoryJpaEntity
  - SchedulerOutboxEventJpaEntity

- [ ] Repository 구현 완료
  - CrawlingSchedulerJpaRepository (JPA)
  - CrawlingSchedulerQueryDslRepository (QueryDSL)
  - SchedulerOutboxEventJpaRepository
  - SchedulerHistoryJpaRepository

- [ ] Adapter 구현 완료
  - SchedulerCommandAdapter
  - SchedulerQueryAdapter
  - OutboxEventCommandAdapter
  - OutboxEventQueryAdapter
  - SchedulerHistoryCommandAdapter
  - SchedulerHistoryQueryAdapter

- [ ] Mapper 구현 완료

- [ ] Integration Test 완료
  - CRUD 테스트
  - Unique Constraint 테스트
  - Pessimistic Lock 테스트
  - Optimistic Lock 테스트
  - QueryDSL 쿼리 테스트

- [ ] ArchUnit 테스트 완료

- [ ] Zero-Tolerance 규칙 준수 확인

- [ ] Flyway Migration 스크립트 작성
  - `V002__Create_crawling_schedulers_table.sql`
  - `V003__Create_scheduler_histories_table.sql`
  - `V004__Create_scheduler_outbox_events_table.sql`

- [ ] 코드 리뷰 승인

- [ ] PR 머지 완료

---

## 🔗 관련 문서

- **PRD**: `/Users/sangwon-ryu/crawlinghub/docs/prd/mustit-seller-crawler-scheduler.md`
- **Plan**: `docs/prd/seller/eventbridge/plans/EVENTBRIDGE-003-persistence-plan.md`
- **Jira**: (sync-to-jira 후 추가)
- **코딩 규칙**: `docs/coding_convention/04-persistence-layer/mysql/`
- **선행 Task**: EVENTBRIDGE-001, EVENTBRIDGE-002

---

## 📋 다음 단계

1. `/create-plan EVENTBRIDGE-003` - TDD Plan 생성
2. `/kb/persistence/go` - Persistence Layer TDD 시작
