# EVENTBRIDGE-003: Persistence Layer TDD Plan

**Epic**: 머스트잇 셀러 크롤링 스케줄러
**Layer**: Persistence (JPA Entity, Repository, Adapter, Mapper)
**브랜치**: feature/EVENTBRIDGE-003-persistence
**예상 소요 시간**: 375분 (25 사이클 × 15분)

---

## 📋 TDD 사이클 개요

| 사이클 | 대상 | 예상 시간 |
|--------|------|----------|
| 1 | BaseAuditEntity 구현 | 15분 |
| 2 | CrawlingSchedulerJpaEntity 구현 | 15분 |
| 3 | SchedulerHistoryJpaEntity 구현 | 15분 |
| 4 | SchedulerOutboxEventJpaEntity 구현 | 15분 |
| 5 | CrawlingSchedulerJpaRepository (기본 메서드) | 15분 |
| 6 | CrawlingSchedulerJpaRepository (Pessimistic Lock) | 15분 |
| 7 | SchedulerOutboxEventJpaRepository 구현 | 15분 |
| 8 | SchedulerHistoryJpaRepository 구현 | 15분 |
| 9 | CrawlingSchedulerQueryDslRepository - 기본 구조 | 15분 |
| 10 | CrawlingSchedulerQueryDslRepository - DTO Projection | 15분 |
| 11 | CrawlingSchedulerQueryDslRepository - 페이징 조회 | 15분 |
| 12 | SchedulerMapper 구현 | 15분 |
| 13 | OutboxEventMapper 구현 | 15분 |
| 14 | SchedulerHistoryMapper 구현 | 15분 |
| 15 | SchedulerCommandAdapter 구현 | 15분 |
| 16 | SchedulerQueryAdapter - 기본 조회 | 15분 |
| 17 | SchedulerQueryAdapter - 페이징 조회 | 15분 |
| 18 | OutboxEventCommandAdapter 구현 | 15분 |
| 19 | OutboxEventQueryAdapter 구현 | 15분 |
| 20 | SchedulerHistoryCommandAdapter 구현 | 15분 |
| 21 | SchedulerHistoryQueryAdapter 구현 | 15분 |
| 22 | Unique Constraint 통합 테스트 | 15분 |
| 23 | Pessimistic Lock 통합 테스트 | 15분 |
| 24 | Optimistic Lock 통합 테스트 | 15분 |
| 25 | Persistence Layer ArchUnit 테스트 | 15분 |

---

## 🔄 Cycle 1: BaseAuditEntity 구현

**목표**: `createdAt`, `updatedAt` 자동 관리 BaseEntity 구현

#### 🔴 Red: 테스트 작성
- [ ] `BaseAuditEntityTest` 생성
  - `@MappedSuperclass` 어노테이션 검증
  - `@EntityListeners(AuditingEntityListener.class)` 검증
  - `createdAt` `@CreatedDate` 필드 존재 검증
  - `updatedAt` `@LastModifiedDate` 필드 존재 검증
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: BaseAuditEntity 검증 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `BaseAuditEntity` 클래스 생성
  - `@MappedSuperclass`, `@EntityListeners` 어노테이션 추가
  - `private LocalDateTime createdAt;` (`@CreatedDate`)
  - `private LocalDateTime updatedAt;` (`@LastModifiedDate`)
  - Getter 메서드 추가
  - **Setter 금지** (JPA Auditing이 자동 설정)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: BaseAuditEntity 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] Lombok 사용 여부 검증 (절대 금지)
- [ ] Pure Java 확인
- [ ] 커밋: `struct: BaseAuditEntity 구조 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `BaseAuditEntityFixture` 생성 (필요 시)
- [ ] 커밋: `test: BaseAuditEntity Fixture 정리 (Tidy)`

---

## 🔄 Cycle 2: CrawlingSchedulerJpaEntity 구현

**목표**: `crawling_schedulers` 테이블 JPA Entity 구현 (Long FK 전략, BaseAuditEntity 상속)

#### 🔴 Red: 테스트 작성
- [ ] `CrawlingSchedulerJpaEntityTest` 생성
  - BaseAuditEntity 상속 검증
  - Long FK 전략 검증 (`private Long sellerId;`)
  - `@ManyToOne` 금지 검증
  - 필수 필드 검증 (id, sellerId, schedulerName, cronExpression, status)
  - `@Table` 어노테이션 검증 (name = "crawling_schedulers")
  - `@Table.indexes` 검증 (3개 인덱스)
  - `@Table.uniqueConstraints` 검증 ((sellerId, schedulerName))
  - Setter 금지 검증
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: CrawlingSchedulerJpaEntity 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `CrawlingSchedulerJpaEntity` 클래스 생성
  ```java
  @Entity
  @Table(name = "crawling_schedulers",
      indexes = {
          @Index(name = "idx_seller_id_scheduler_name", columnList = "seller_id,scheduler_name", unique = true),
          @Index(name = "idx_seller_id_status", columnList = "seller_id,status"),
          @Index(name = "idx_status", columnList = "status")
      },
      uniqueConstraints = {
          @UniqueConstraint(name = "uk_seller_scheduler", columnNames = {"seller_id", "scheduler_name"}),
          @UniqueConstraint(name = "uk_eventbridge_rule_name", columnNames = {"event_bridge_rule_name"})
      }
  )
  public class CrawlingSchedulerJpaEntity extends BaseAuditEntity {
      @Id
      @GeneratedValue(strategy = GenerationType.IDENTITY)
      private Long id;

      @Column(name = "seller_id", nullable = false)
      private Long sellerId; // Long FK 전략

      @Column(name = "scheduler_name", nullable = false)
      private String schedulerName;

      @Column(name = "cron_expression", nullable = false)
      private String cronExpression;

      @Column(name = "status", nullable = false)
      @Enumerated(EnumType.STRING)
      private SchedulerStatus status;

      @Column(name = "event_bridge_rule_name", unique = true)
      private String eventBridgeRuleName;

      // 생성자, Getter만 (Setter 금지)
      protected CrawlingSchedulerJpaEntity() {} // JPA 기본 생성자

      public CrawlingSchedulerJpaEntity(Long id, Long sellerId, String schedulerName,
                                        String cronExpression, SchedulerStatus status,
                                        String eventBridgeRuleName) {
          this.id = id;
          this.sellerId = sellerId;
          this.schedulerName = schedulerName;
          this.cronExpression = cronExpression;
          this.status = status;
          this.eventBridgeRuleName = eventBridgeRuleName;
      }

      // Getter 메서드들...
  }
  ```
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: CrawlingSchedulerJpaEntity 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] Lombok 미사용 확인
- [ ] Long FK 전략 준수 확인
- [ ] 인덱스 최적화 검증
- [ ] 커밋: `struct: CrawlingSchedulerJpaEntity 구조 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `CrawlingSchedulerJpaEntityFixture` 생성 (Object Mother 패턴)
- [ ] 테스트 → Fixture 사용으로 변경
- [ ] 커밋: `test: CrawlingSchedulerJpaEntityFixture 정리 (Tidy)`

---

## 🔄 Cycle 3: SchedulerHistoryJpaEntity 구현

**목표**: `scheduler_histories` 테이블 JPA Entity 구현

#### 🔴 Red: 테스트 작성
- [ ] `SchedulerHistoryJpaEntityTest` 생성
  - Long FK 전략 검증 (`private Long schedulerId;`)
  - 필수 필드 검증 (id, schedulerId, changedField, newValue, changedAt)
  - `@Table` 어노테이션 검증
  - `@Table.indexes` 검증 (idx_scheduler_id_changed_at DESC)
  - Setter 금지 검증
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: SchedulerHistoryJpaEntity 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SchedulerHistoryJpaEntity` 클래스 생성
  ```java
  @Entity
  @Table(name = "scheduler_histories",
      indexes = {
          @Index(name = "idx_scheduler_id_changed_at", columnList = "scheduler_id,changed_at DESC")
      }
  )
  public class SchedulerHistoryJpaEntity {
      @Id
      @GeneratedValue(strategy = GenerationType.IDENTITY)
      private Long id;

      @Column(name = "scheduler_id", nullable = false)
      private Long schedulerId; // Long FK

      @Column(name = "changed_field", nullable = false)
      private String changedField;

      @Column(name = "old_value")
      private String oldValue;

      @Column(name = "new_value", nullable = false)
      private String newValue;

      @Column(name = "changed_at", nullable = false)
      private LocalDateTime changedAt;

      // 생성자, Getter만 (Setter 금지)
  }
  ```
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: SchedulerHistoryJpaEntity 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] Lombok 미사용 확인
- [ ] Long FK 전략 준수 확인
- [ ] 커밋: `struct: SchedulerHistoryJpaEntity 구조 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `SchedulerHistoryJpaEntityFixture` 생성
- [ ] 커밋: `test: SchedulerHistoryJpaEntityFixture 정리 (Tidy)`

---

## 🔄 Cycle 4: SchedulerOutboxEventJpaEntity 구현

**목표**: `scheduler_outbox_events` 테이블 JPA Entity 구현 (Optimistic Lock 포함)

#### 🔴 Red: 테스트 작성
- [ ] `SchedulerOutboxEventJpaEntityTest` 생성
  - `@Version` 필드 존재 검증 (Optimistic Lock)
  - Long FK 전략 검증 (`private Long schedulerId;`)
  - 필수 필드 검증 (eventType, schedulerId, payload, status, retryCount, maxRetries, createdAt)
  - `@Table.indexes` 검증 (2개)
  - Setter 금지 검증
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: SchedulerOutboxEventJpaEntity 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SchedulerOutboxEventJpaEntity` 클래스 생성
  ```java
  @Entity
  @Table(name = "scheduler_outbox_events",
      indexes = {
          @Index(name = "idx_status_retry_count", columnList = "status,retry_count"),
          @Index(name = "idx_status_created_at", columnList = "status,created_at")
      }
  )
  public class SchedulerOutboxEventJpaEntity {
      @Id
      @GeneratedValue(strategy = GenerationType.IDENTITY)
      private Long id;

      @Version
      private Long version; // Optimistic Lock

      @Column(name = "event_type", nullable = false)
      @Enumerated(EnumType.STRING)
      private OutboxEventType eventType;

      @Column(name = "scheduler_id", nullable = false)
      private Long schedulerId; // Long FK

      @Column(name = "payload", columnDefinition = "TEXT", nullable = false)
      private String payload;

      @Column(name = "status", nullable = false)
      @Enumerated(EnumType.STRING)
      private OutboxStatus status;

      @Column(name = "retry_count", nullable = false)
      private Integer retryCount = 0;

      @Column(name = "max_retries", nullable = false)
      private Integer maxRetries = 3;

      @Column(name = "created_at", nullable = false)
      private LocalDateTime createdAt;

      @Column(name = "processed_at")
      private LocalDateTime processedAt;

      @Column(name = "error_message", columnDefinition = "TEXT")
      private String errorMessage;

      // 생성자, Getter만 (Setter 금지)
  }
  ```
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: SchedulerOutboxEventJpaEntity 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] Optimistic Lock 전략 확인
- [ ] Lombok 미사용 확인
- [ ] 커밋: `struct: SchedulerOutboxEventJpaEntity 구조 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `SchedulerOutboxEventJpaEntityFixture` 생성
- [ ] 커밋: `test: SchedulerOutboxEventJpaEntityFixture 정리 (Tidy)`

---

## 🔄 Cycle 5: CrawlingSchedulerJpaRepository (기본 메서드)

**목표**: JPA Repository 기본 CRUD 메서드 구현

#### 🔴 Red: 테스트 작성
- [ ] `CrawlingSchedulerJpaRepositoryTest` 생성 (Integration Test)
  - TestContainers MySQL 설정
  - `findBySellerIdAndSchedulerName()` 테스트
  - `findBySellerIdAndStatus()` 테스트
  - `existsBySellerIdAndSchedulerName()` 테스트
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: CrawlingSchedulerJpaRepository 기본 메서드 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `CrawlingSchedulerJpaRepository` 인터페이스 생성
  ```java
  public interface CrawlingSchedulerJpaRepository extends JpaRepository<CrawlingSchedulerJpaEntity, Long> {
      Optional<CrawlingSchedulerJpaEntity> findBySellerIdAndSchedulerName(Long sellerId, String schedulerName);
      List<CrawlingSchedulerJpaEntity> findBySellerIdAndStatus(Long sellerId, SchedulerStatus status);
      boolean existsBySellerIdAndSchedulerName(Long sellerId, String schedulerName);
  }
  ```
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: CrawlingSchedulerJpaRepository 기본 메서드 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] 쿼리 메서드 네이밍 규칙 확인
- [ ] 커밋: `struct: CrawlingSchedulerJpaRepository 구조 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 테스트 데이터 → SQL 파일로 이동 (`@Sql`)
- [ ] 커밋: `test: CrawlingSchedulerJpaRepository Fixture 정리 (Tidy)`

---

## 🔄 Cycle 6: CrawlingSchedulerJpaRepository (Pessimistic Lock)

**목표**: 동시성 제어를 위한 Pessimistic Lock 메서드 구현

#### 🔴 Red: 테스트 작성
- [ ] `findBySellerIdWithLock()` 동시성 테스트 작성
  - `@Lock(LockModeType.PESSIMISTIC_WRITE)` 검증
  - 동시 요청 시 대기 검증
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: CrawlingSchedulerJpaRepository Pessimistic Lock 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `findBySellerIdWithLock()` 메서드 추가
  ```java
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT s FROM CrawlingSchedulerJpaEntity s WHERE s.sellerId = :sellerId")
  Optional<CrawlingSchedulerJpaEntity> findBySellerIdWithLock(@Param("sellerId") Long sellerId);
  ```
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: CrawlingSchedulerJpaRepository Pessimistic Lock 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] Lock Timeout 설정 검토
- [ ] 커밋: `struct: Pessimistic Lock 전략 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 동시성 테스트 데이터 정리
- [ ] 커밋: `test: Pessimistic Lock 테스트 Fixture 정리 (Tidy)`

---

## 🔄 Cycle 7: SchedulerOutboxEventJpaRepository 구현

**목표**: Outbox Event JPA Repository 구현

#### 🔴 Red: 테스트 작성
- [ ] `SchedulerOutboxEventJpaRepositoryTest` 생성
  - `findByStatusAndRetryCountLessThan()` 테스트
  - `findByStatusAndCreatedAtBefore()` 테스트
  - `deleteByStatusAndCreatedAtBefore()` 테스트
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: SchedulerOutboxEventJpaRepository 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SchedulerOutboxEventJpaRepository` 인터페이스 생성
  ```java
  public interface SchedulerOutboxEventJpaRepository extends JpaRepository<SchedulerOutboxEventJpaEntity, Long> {
      List<SchedulerOutboxEventJpaEntity> findByStatusAndRetryCountLessThan(OutboxStatus status, Integer maxRetries);
      List<SchedulerOutboxEventJpaEntity> findByStatusAndCreatedAtBefore(OutboxStatus status, LocalDateTime before);
      void deleteByStatusAndCreatedAtBefore(OutboxStatus status, LocalDateTime before);
  }
  ```
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: SchedulerOutboxEventJpaRepository 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] 쿼리 메서드 최적화 검토
- [ ] 커밋: `struct: SchedulerOutboxEventJpaRepository 구조 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Outbox Event 테스트 데이터 SQL 파일로 이동
- [ ] 커밋: `test: SchedulerOutboxEventJpaRepository Fixture 정리 (Tidy)`

---

## 🔄 Cycle 8: SchedulerHistoryJpaRepository 구현

**목표**: History JPA Repository 구현

#### 🔴 Red: 테스트 작성
- [ ] `SchedulerHistoryJpaRepositoryTest` 생성
  - `findBySchedulerIdOrderByChangedAtDesc()` 테스트 (페이징)
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: SchedulerHistoryJpaRepository 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SchedulerHistoryJpaRepository` 인터페이스 생성
  ```java
  public interface SchedulerHistoryJpaRepository extends JpaRepository<SchedulerHistoryJpaEntity, Long> {
      Page<SchedulerHistoryJpaEntity> findBySchedulerIdOrderByChangedAtDesc(Long schedulerId, Pageable pageable);
  }
  ```
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: SchedulerHistoryJpaRepository 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] 페이징 성능 검토
- [ ] 커밋: `struct: SchedulerHistoryJpaRepository 구조 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] History 테스트 데이터 SQL 파일로 이동
- [ ] 커밋: `test: SchedulerHistoryJpaRepository Fixture 정리 (Tidy)`

---

## 🔄 Cycle 9: CrawlingSchedulerQueryDslRepository - 기본 구조

**목표**: QueryDSL Repository 기본 구조 및 설정

#### 🔴 Red: 테스트 작성
- [ ] `CrawlingSchedulerQueryDslRepositoryTest` 생성
  - QueryDSL 설정 검증
  - `JPAQueryFactory` 주입 검증
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: CrawlingSchedulerQueryDslRepository 기본 구조 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `CrawlingSchedulerQueryDslRepository` 클래스 생성
  ```java
  @Repository
  public class CrawlingSchedulerQueryDslRepository {
      private final JPAQueryFactory queryFactory;

      public CrawlingSchedulerQueryDslRepository(JPAQueryFactory queryFactory) {
          this.queryFactory = queryFactory;
      }

      // QueryDSL 쿼리 메서드들...
  }
  ```
- [ ] QueryDSL 설정 추가 (`build.gradle`)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: CrawlingSchedulerQueryDslRepository 기본 구조 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] QueryDSL 설정 최적화
- [ ] 커밋: `struct: QueryDSL 설정 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] QueryDSL 테스트 데이터 정리
- [ ] 커밋: `test: QueryDSL Fixture 정리 (Tidy)`

---

## 🔄 Cycle 10: CrawlingSchedulerQueryDslRepository - DTO Projection

**목표**: QueryDSL DTO Projection 구현 (N+1 방지)

#### 🔴 Red: 테스트 작성
- [ ] DTO Projection 테스트 작성
  - Entity 조회 금지 검증
  - `Projections.constructor()` 사용 검증
  - N+1 방지 검증 (쿼리 개수 확인)
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: QueryDSL DTO Projection 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] DTO Projection 쿼리 구현
  ```java
  public List<SchedulerDto> findAllBySellerIdAsDto(Long sellerId) {
      QCrawlingSchedulerJpaEntity scheduler = QCrawlingSchedulerJpaEntity.crawlingSchedulerJpaEntity;

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
  }
  ```
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: QueryDSL DTO Projection 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] DTO Projection 최적화
- [ ] N+1 방지 전략 재확인
- [ ] 커밋: `struct: QueryDSL DTO Projection 구조 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] DTO Projection 테스트 데이터 정리
- [ ] 커밋: `test: QueryDSL DTO Projection Fixture 정리 (Tidy)`

---

## 🔄 Cycle 11: CrawlingSchedulerQueryDslRepository - 페이징 조회

**목표**: QueryDSL 페이징 조회 구현

#### 🔴 Red: 테스트 작성
- [ ] 페이징 조회 테스트 작성
  - `findAllBySellerIdAndStatus()` 페이징 테스트
  - `findAllByStatus()` 페이징 테스트
  - `countBySellerIdAndStatus()` 테스트
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: QueryDSL 페이징 조회 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] 페이징 쿼리 구현
  ```java
  public Page<SchedulerDto> findAllBySellerIdAndStatus(Long sellerId, SchedulerStatus status, Pageable pageable) {
      QCrawlingSchedulerJpaEntity scheduler = QCrawlingSchedulerJpaEntity.crawlingSchedulerJpaEntity;

      List<SchedulerDto> content = queryFactory
          .select(Projections.constructor(SchedulerDto.class,
              scheduler.id,
              scheduler.sellerId,
              scheduler.schedulerName,
              scheduler.cronExpression,
              scheduler.status
          ))
          .from(scheduler)
          .where(
              scheduler.sellerId.eq(sellerId),
              scheduler.status.eq(status)
          )
          .offset(pageable.getOffset())
          .limit(pageable.getPageSize())
          .fetch();

      Long total = queryFactory
          .select(scheduler.count())
          .from(scheduler)
          .where(
              scheduler.sellerId.eq(sellerId),
              scheduler.status.eq(status)
          )
          .fetchOne();

      return new PageImpl<>(content, pageable, total != null ? total : 0);
  }
  ```
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: QueryDSL 페이징 조회 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] 페이징 쿼리 최적화
- [ ] 커밋: `struct: QueryDSL 페이징 구조 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 페이징 테스트 데이터 정리
- [ ] 커밋: `test: QueryDSL 페이징 Fixture 정리 (Tidy)`

---

## 🔄 Cycle 12: SchedulerMapper 구현

**목표**: JPA Entity ↔ Domain Scheduler 변환 Mapper 구현 (MapStruct 금지)

#### 🔴 Red: 테스트 작성
- [ ] `SchedulerMapperTest` 생성
  - `toJpaEntity()` 변환 테스트
  - `toDomain()` 변환 테스트
  - MapStruct 미사용 검증
  - Static 유틸리티 클래스 금지 검증
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: SchedulerMapper 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SchedulerMapper` 클래스 생성 (Pure Java)
  ```java
  public class SchedulerMapper {
      public CrawlingSchedulerJpaEntity toJpaEntity(Scheduler scheduler) {
          return new CrawlingSchedulerJpaEntity(
              scheduler.getSchedulerId(),
              scheduler.getSellerId(),
              scheduler.getSchedulerName(),
              scheduler.getCronExpression().getValue(),
              scheduler.getStatus(),
              scheduler.getEventBridgeRuleName()
          );
      }

      public Scheduler toDomain(CrawlingSchedulerJpaEntity entity) {
          return Scheduler.reconstitute(
              entity.getId(),
              entity.getSellerId(),
              entity.getSchedulerName(),
              CronExpression.of(entity.getCronExpression()),
              entity.getStatus(),
              entity.getEventBridgeRuleName(),
              entity.getCreatedAt(),
              entity.getUpdatedAt()
          );
      }
  }
  ```
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: SchedulerMapper 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] Mapper 코드 명확성 개선
- [ ] 커밋: `struct: SchedulerMapper 구조 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Mapper 테스트 Fixture 정리
- [ ] 커밋: `test: SchedulerMapper Fixture 정리 (Tidy)`

---

## 🔄 Cycle 13: OutboxEventMapper 구현

**목표**: JPA Entity ↔ Domain OutboxEvent 변환 Mapper 구현

#### 🔴 Red: 테스트 작성
- [ ] `OutboxEventMapperTest` 생성
  - `toJpaEntity()` 변환 테스트
  - `toDomain()` 변환 테스트
  - JSON 직렬화/역직렬화 검증
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: OutboxEventMapper 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `OutboxEventMapper` 클래스 생성
  ```java
  public class OutboxEventMapper {
      public SchedulerOutboxEventJpaEntity toJpaEntity(OutboxEvent event) {
          // Domain → JPA Entity 변환
      }

      public OutboxEvent toDomain(SchedulerOutboxEventJpaEntity entity) {
          // JPA Entity → Domain 변환
      }
  }
  ```
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: OutboxEventMapper 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] JSON 직렬화 전략 개선
- [ ] 커밋: `struct: OutboxEventMapper 구조 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] OutboxEventMapper Fixture 정리
- [ ] 커밋: `test: OutboxEventMapper Fixture 정리 (Tidy)`

---

## 🔄 Cycle 14: SchedulerHistoryMapper 구현

**목표**: JPA Entity ↔ Domain SchedulerHistory 변환 Mapper 구현

#### 🔴 Red: 테스트 작성
- [ ] `SchedulerHistoryMapperTest` 생성
  - `toJpaEntity()` 변환 테스트
  - `toDomain()` 변환 테스트
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: SchedulerHistoryMapper 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SchedulerHistoryMapper` 클래스 생성
  ```java
  public class SchedulerHistoryMapper {
      public SchedulerHistoryJpaEntity toJpaEntity(SchedulerHistory history) {
          // Domain → JPA Entity 변환
      }

      public SchedulerHistory toDomain(SchedulerHistoryJpaEntity entity) {
          // JPA Entity → Domain 변환
      }
  }
  ```
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: SchedulerHistoryMapper 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] Mapper 명확성 개선
- [ ] 커밋: `struct: SchedulerHistoryMapper 구조 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] SchedulerHistoryMapper Fixture 정리
- [ ] 커밋: `test: SchedulerHistoryMapper Fixture 정리 (Tidy)`

---

## 🔄 Cycle 15: SchedulerCommandAdapter 구현

**목표**: SchedulerCommandPort 구현체 작성

#### 🔴 Red: 테스트 작성
- [ ] `SchedulerCommandAdapterTest` 생성
  - `save()` 메서드 테스트
  - `delete()` 메서드 테스트
  - SchedulerCommandPort 인터페이스 구현 검증
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: SchedulerCommandAdapter 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SchedulerCommandAdapter` 클래스 생성
  ```java
  @Component
  public class SchedulerCommandAdapter implements SchedulerCommandPort {
      private final CrawlingSchedulerJpaRepository jpaRepository;
      private final SchedulerMapper mapper;

      public SchedulerCommandAdapter(CrawlingSchedulerJpaRepository jpaRepository, SchedulerMapper mapper) {
          this.jpaRepository = jpaRepository;
          this.mapper = mapper;
      }

      @Override
      public Scheduler save(Scheduler scheduler) {
          CrawlingSchedulerJpaEntity entity = mapper.toJpaEntity(scheduler);
          CrawlingSchedulerJpaEntity saved = jpaRepository.save(entity);
          return mapper.toDomain(saved);
      }

      @Override
      public void delete(Long schedulerId) {
          jpaRepository.deleteById(schedulerId);
      }
  }
  ```
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: SchedulerCommandAdapter 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] Adapter 로직 최적화
- [ ] 커밋: `struct: SchedulerCommandAdapter 구조 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] SchedulerCommandAdapter Fixture 정리
- [ ] 커밋: `test: SchedulerCommandAdapter Fixture 정리 (Tidy)`

---

## 🔄 Cycle 16: SchedulerQueryAdapter - 기본 조회

**목표**: SchedulerQueryPort 구현체 작성 (기본 조회 메서드)

#### 🔴 Red: 테스트 작성
- [ ] `SchedulerQueryAdapterTest` 생성
  - `findById()` 테스트
  - `findBySellerIdAndSchedulerName()` 테스트
  - `findBySellerIdAndStatus()` 테스트
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: SchedulerQueryAdapter 기본 조회 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SchedulerQueryAdapter` 클래스 생성
  ```java
  @Component
  public class SchedulerQueryAdapter implements SchedulerQueryPort {
      private final CrawlingSchedulerJpaRepository jpaRepository;
      private final CrawlingSchedulerQueryDslRepository queryDslRepository;
      private final SchedulerMapper mapper;

      // 생성자...

      @Override
      public Optional<Scheduler> findById(Long schedulerId) {
          return jpaRepository.findById(schedulerId)
              .map(mapper::toDomain);
      }

      @Override
      public Optional<Scheduler> findBySellerIdAndSchedulerName(Long sellerId, String schedulerName) {
          return jpaRepository.findBySellerIdAndSchedulerName(sellerId, schedulerName)
              .map(mapper::toDomain);
      }

      @Override
      public List<Scheduler> findBySellerIdAndStatus(Long sellerId, SchedulerStatus status) {
          return jpaRepository.findBySellerIdAndStatus(sellerId, status).stream()
              .map(mapper::toDomain)
              .toList();
      }
  }
  ```
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: SchedulerQueryAdapter 기본 조회 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] 조회 로직 최적화
- [ ] 커밋: `struct: SchedulerQueryAdapter 구조 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] SchedulerQueryAdapter Fixture 정리
- [ ] 커밋: `test: SchedulerQueryAdapter Fixture 정리 (Tidy)`

---

## 🔄 Cycle 17: SchedulerQueryAdapter - 페이징 조회

**목표**: SchedulerQueryPort 페이징 조회 메서드 구현

#### 🔴 Red: 테스트 작성
- [ ] 페이징 조회 테스트 추가
  - `findAllBySellerIdAndStatus()` (Pageable) 테스트
  - `countActiveSchedulersBySellerId()` 테스트
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: SchedulerQueryAdapter 페이징 조회 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] 페이징 조회 메서드 추가
  ```java
  @Override
  public Page<Scheduler> findAllBySellerIdAndStatus(Long sellerId, SchedulerStatus status, Pageable pageable) {
      Page<SchedulerDto> dtoPage = queryDslRepository.findAllBySellerIdAndStatus(sellerId, status, pageable);
      return dtoPage.map(dto -> mapper.toDomain(dto)); // DTO → Domain 변환
  }

  @Override
  public int countActiveSchedulersBySellerId(Long sellerId) {
      return queryDslRepository.countBySellerIdAndStatus(sellerId, SchedulerStatus.ACTIVE);
  }
  ```
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: SchedulerQueryAdapter 페이징 조회 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] 페이징 성능 최적화
- [ ] 커밋: `struct: SchedulerQueryAdapter 페이징 구조 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 페이징 테스트 Fixture 정리
- [ ] 커밋: `test: SchedulerQueryAdapter 페이징 Fixture 정리 (Tidy)`

---

## 🔄 Cycle 18: OutboxEventCommandAdapter 구현

**목표**: OutboxEventCommandPort 구현체 작성

#### 🔴 Red: 테스트 작성
- [ ] `OutboxEventCommandAdapterTest` 생성
  - `save()` 메서드 테스트
  - `delete()` 메서드 테스트
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: OutboxEventCommandAdapter 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `OutboxEventCommandAdapter` 클래스 생성
  ```java
  @Component
  public class OutboxEventCommandAdapter implements OutboxEventCommandPort {
      private final SchedulerOutboxEventJpaRepository jpaRepository;
      private final OutboxEventMapper mapper;

      // save(), delete() 구현...
  }
  ```
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: OutboxEventCommandAdapter 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] Adapter 로직 최적화
- [ ] 커밋: `struct: OutboxEventCommandAdapter 구조 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] OutboxEventCommandAdapter Fixture 정리
- [ ] 커밋: `test: OutboxEventCommandAdapter Fixture 정리 (Tidy)`

---

## 🔄 Cycle 19: OutboxEventQueryAdapter 구현

**목표**: OutboxEventQueryPort 구현체 작성

#### 🔴 Red: 테스트 작성
- [ ] `OutboxEventQueryAdapterTest` 생성
  - `findPendingEvents()` 테스트
  - `findFailedEventsForRetry()` 테스트
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: OutboxEventQueryAdapter 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `OutboxEventQueryAdapter` 클래스 생성
  ```java
  @Component
  public class OutboxEventQueryAdapter implements OutboxEventQueryPort {
      private final SchedulerOutboxEventJpaRepository jpaRepository;
      private final OutboxEventMapper mapper;

      // 조회 메서드 구현...
  }
  ```
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: OutboxEventQueryAdapter 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] 조회 로직 최적화
- [ ] 커밋: `struct: OutboxEventQueryAdapter 구조 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] OutboxEventQueryAdapter Fixture 정리
- [ ] 커밋: `test: OutboxEventQueryAdapter Fixture 정리 (Tidy)`

---

## 🔄 Cycle 20: SchedulerHistoryCommandAdapter 구현

**목표**: SchedulerHistoryCommandPort 구현체 작성

#### 🔴 Red: 테스트 작성
- [ ] `SchedulerHistoryCommandAdapterTest` 생성
  - `save()` 메서드 테스트
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: SchedulerHistoryCommandAdapter 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SchedulerHistoryCommandAdapter` 클래스 생성
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: SchedulerHistoryCommandAdapter 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] Adapter 로직 최적화
- [ ] 커밋: `struct: SchedulerHistoryCommandAdapter 구조 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] SchedulerHistoryCommandAdapter Fixture 정리
- [ ] 커밋: `test: SchedulerHistoryCommandAdapter Fixture 정리 (Tidy)`

---

## 🔄 Cycle 21: SchedulerHistoryQueryAdapter 구현

**목표**: SchedulerHistoryQueryPort 구현체 작성

#### 🔴 Red: 테스트 작성
- [ ] `SchedulerHistoryQueryAdapterTest` 생성
  - `findBySchedulerId()` (Pageable) 테스트
- [ ] 테스트 실행 → 컴파일 에러 확인
- [ ] 커밋: `test: SchedulerHistoryQueryAdapter 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `SchedulerHistoryQueryAdapter` 클래스 생성
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: SchedulerHistoryQueryAdapter 구현 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] 조회 로직 최적화
- [ ] 커밋: `struct: SchedulerHistoryQueryAdapter 구조 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] SchedulerHistoryQueryAdapter Fixture 정리
- [ ] 커밋: `test: SchedulerHistoryQueryAdapter Fixture 정리 (Tidy)`

---

## 🔄 Cycle 22: Unique Constraint 통합 테스트

**목표**: Unique Constraint 위반 시나리오 통합 테스트

#### 🔴 Red: 테스트 작성
- [ ] `SchedulerUniqueConstraintIntegrationTest` 생성
  - 중복 `(seller_id, scheduler_name)` 저장 시 예외 검증
  - 중복 `event_bridge_rule_name` 저장 시 예외 검증
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: Unique Constraint 통합 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] Flyway Migration 스크립트 작성
  - `V002__Create_crawling_schedulers_table.sql` (Unique Constraint 포함)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: Unique Constraint 통합 테스트 통과 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] Exception Handling 개선
- [ ] 커밋: `struct: Unique Constraint 예외 처리 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Unique Constraint 테스트 데이터 정리
- [ ] 커밋: `test: Unique Constraint Fixture 정리 (Tidy)`

---

## 🔄 Cycle 23: Pessimistic Lock 통합 테스트

**목표**: Pessimistic Lock 동시성 제어 통합 테스트

#### 🔴 Red: 테스트 작성
- [ ] `PessimisticLockIntegrationTest` 생성
  - 동시 스케줄 등록 시 Lock 동작 검증
  - Deadlock 방지 검증
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: Pessimistic Lock 통합 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] Lock Timeout 설정 추가
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: Pessimistic Lock 통합 테스트 통과 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] Lock 전략 최적화
- [ ] 커밋: `struct: Pessimistic Lock 전략 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Pessimistic Lock 테스트 데이터 정리
- [ ] 커밋: `test: Pessimistic Lock Fixture 정리 (Tidy)`

---

## 🔄 Cycle 24: Optimistic Lock 통합 테스트

**목표**: Optimistic Lock 동시성 제어 통합 테스트

#### 🔴 Red: 테스트 작성
- [ ] `OptimisticLockIntegrationTest` 생성
  - Outbox Event 동시 수정 시 `OptimisticLockException` 검증
  - `@Version` 필드 동작 검증
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: Optimistic Lock 통합 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] Optimistic Lock 예외 처리 추가
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: Optimistic Lock 통합 테스트 통과 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] Optimistic Lock 재시도 전략 검토
- [ ] 커밋: `struct: Optimistic Lock 전략 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] Optimistic Lock 테스트 데이터 정리
- [ ] 커밋: `test: Optimistic Lock Fixture 정리 (Tidy)`

---

## 🔄 Cycle 25: Persistence Layer ArchUnit 테스트

**목표**: Persistence Layer 아키텍처 규칙 검증

#### 🔴 Red: 테스트 작성
- [ ] `PersistenceLayerArchUnitTest` 생성
  - Lombok 금지 규칙
  - Long FK 전략 규칙 (`@ManyToOne` 금지)
  - QueryDSL DTO Projection 규칙
  - MapStruct 금지 규칙
  - Setter 금지 규칙
  - BaseAuditEntity 상속 규칙
  - Adapter 네이밍 규칙 (*CommandAdapter, *QueryAdapter)
  - Repository 네이밍 규칙 (*JpaRepository, *QueryDslRepository)
- [ ] 테스트 실행 → 실패 확인
- [ ] 커밋: `test: Persistence Layer ArchUnit 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] ArchUnit 규칙 위반 수정
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: Persistence Layer ArchUnit 테스트 통과 (Green)`

#### ♻️ Refactor: 구조 개선
- [ ] ArchUnit 규칙 강화
- [ ] 커밋: `struct: ArchUnit 규칙 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] ArchUnit 테스트 정리
- [ ] 커밋: `test: ArchUnit Fixture 정리 (Tidy)`

---

## ✅ 완료 조건

- [ ] **25개 TDD 사이클 완료**
- [ ] **JPA Entity 구현 완료** (3개)
  - BaseAuditEntity
  - CrawlingSchedulerJpaEntity
  - SchedulerHistoryJpaEntity
  - SchedulerOutboxEventJpaEntity
- [ ] **Repository 구현 완료** (5개)
  - CrawlingSchedulerJpaRepository
  - CrawlingSchedulerQueryDslRepository
  - SchedulerOutboxEventJpaRepository
  - SchedulerHistoryJpaRepository
- [ ] **Mapper 구현 완료** (3개, Pure Java)
  - SchedulerMapper
  - OutboxEventMapper
  - SchedulerHistoryMapper
- [ ] **Adapter 구현 완료** (6개)
  - SchedulerCommandAdapter
  - SchedulerQueryAdapter
  - OutboxEventCommandAdapter
  - OutboxEventQueryAdapter
  - SchedulerHistoryCommandAdapter
  - SchedulerHistoryQueryAdapter
- [ ] **Integration Test 완료**
  - Unique Constraint 테스트
  - Pessimistic Lock 테스트
  - Optimistic Lock 테스트
  - QueryDSL N+1 방지 테스트
- [ ] **ArchUnit 테스트 완료**
- [ ] **Flyway Migration 스크립트 작성** (3개)
  - `V002__Create_crawling_schedulers_table.sql`
  - `V003__Create_scheduler_histories_table.sql`
  - `V004__Create_scheduler_outbox_events_table.sql`
- [ ] **모든 커밋 메시지 규칙 준수** (test:, feat:, struct:, test:)

---

## 📊 최종 통계

- **총 사이클 수**: 25개
- **예상 소요 시간**: 375분 (6시간 15분)
- **총 체크박스**: 100개 (25 사이클 × 4 단계)
- **커밋 횟수**: 100회 (각 단계마다 커밋)
- **테스트 종류**: Unit Test (Mapper, Repository), Integration Test (동시성, Constraint), ArchUnit Test

---

## 🔗 관련 문서

- **PRD**: `/Users/sangwon-ryu/crawlinghub/docs/prd/eventbridge/EVENTBRIDGE-003-persistence.md`
- **코딩 규칙**: `docs/coding_convention/04-persistence-layer/mysql/`
- **선행 Task**: EVENTBRIDGE-001-domain-plan.md, EVENTBRIDGE-002-application-plan.md
