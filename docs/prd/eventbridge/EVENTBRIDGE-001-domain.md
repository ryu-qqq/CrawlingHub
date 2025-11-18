# EVENTBRIDGE-001: EventBridge Domain Layer 구현

**Epic**: 머스트잇 셀러 크롤링 스케줄러
**Bounded Context**: EventBridge (Crawling Scheduler)
**Layer**: Domain
**브랜치**: feature/EVENTBRIDGE-001-domain
**Jira URL**: (sync-to-jira 후 추가)

---

## 📝 목적

EventBridge 바운더리 컨텍스트의 핵심 비즈니스 로직을 Domain Layer에서 구현합니다.

**핵심 도메인 개념**:
- 크롤링 스케줄 등록/수정/비활성화
- Cron Expression 검증 (AWS EventBridge 형식)
- Outbox Pattern (안전한 외부 API 연동)
- 스케줄 변경 이력 추적

---

## 🎯 요구사항

### Aggregate Root: CrawlingScheduler

- [ ] **CrawlingScheduler Aggregate 구현**
  - `schedulerId`: Long (PK)
  - `sellerId`: Long (FK - Long FK 전략)
  - `schedulerName`: String
  - `cronExpression`: CronExpression (VO)
  - `status`: SchedulerStatus (Enum)
  - `eventBridgeRuleName`: String (Nullable)
  - `createdAt`, `updatedAt`: LocalDateTime

- [ ] **생성 메서드 패턴 (3종 필수)**
  ```java
  // 1. forNew(): 새로운 Aggregate 생성
  public static CrawlingScheduler forNew(Long sellerId, String schedulerName, CronExpression cronExpression) {
      // 비즈니스 검증 수행
      return new CrawlingScheduler(null, sellerId, schedulerName, cronExpression, SchedulerStatus.PENDING, null, null, null);
  }

  // 2. of(): 기존 값으로 Aggregate 생성 (Update용)
  public static CrawlingScheduler of(Long schedulerId, Long sellerId, String schedulerName, CronExpression cronExpression, SchedulerStatus status) {
      return new CrawlingScheduler(schedulerId, sellerId, schedulerName, cronExpression, status, null, null, null);
  }

  // 3. reconstitute(): Persistence Layer에서 재구성 (DB → Domain)
  public static CrawlingScheduler reconstitute(Long schedulerId, Long sellerId, String schedulerName,
                                                CronExpression cronExpression, SchedulerStatus status,
                                                String eventBridgeRuleName, LocalDateTime createdAt, LocalDateTime updatedAt) {
      return new CrawlingScheduler(schedulerId, sellerId, schedulerName, cronExpression, status, eventBridgeRuleName, createdAt, updatedAt);
  }
  ```

### Value Objects

- [ ] **CronExpression VO**
  ```java
  public record CronExpression(String value) {
      public CronExpression {
          validateAwsEventBridgeFormat(value);
          validateMinimumInterval(value); // 최소 1시간
      }
  }
  ```
  - AWS EventBridge Cron 형식 검증 (6자리)
  - 형식: `cron(분 시 일 월 요일 년도)`
  - 예시: `cron(0 0 * * ? *)` (매일 00:00)
  - 최소 실행 간격: 1시간

- [ ] **SchedulerStatus Enum**
  - `PENDING`: AWS Rule 생성 전
  - `ACTIVE`: AWS Rule 생성 완료
  - `INACTIVE`: 비활성화 상태

### Aggregate Root: SchedulerOutboxEvent

- [ ] **SchedulerOutboxEvent Aggregate 구현**
  - `eventId`: Long (PK)
  - `eventType`: OutboxEventType (Enum)
  - `schedulerId`: Long (FK)
  - `payload`: String (JSON)
  - `status`: OutboxStatus (Enum)
  - `retryCount`: Integer (기본값 0)
  - `maxRetries`: Integer (기본값 3)
  - `createdAt`: LocalDateTime
  - `processedAt`: LocalDateTime (Nullable)
  - `errorMessage`: String (Nullable)

- [ ] **생성 메서드 패턴 (3종 필수)**
  ```java
  // 1. forNew(): 새로운 Outbox Event 생성
  public static SchedulerOutboxEvent forNew(OutboxEventType eventType, Long schedulerId, String payload) {
      return new SchedulerOutboxEvent(null, eventType, schedulerId, payload, OutboxStatus.PENDING, 0, 3, null, null, null);
  }

  // 2. of(): 기존 값으로 생성 (상태 변경용)
  public static SchedulerOutboxEvent of(Long eventId, OutboxEventType eventType, Long schedulerId,
                                        String payload, OutboxStatus status, Integer retryCount) {
      return new SchedulerOutboxEvent(eventId, eventType, schedulerId, payload, status, retryCount, 3, null, null, null);
  }

  // 3. reconstitute(): Persistence Layer에서 재구성
  public static SchedulerOutboxEvent reconstitute(Long eventId, OutboxEventType eventType, Long schedulerId,
                                                   String payload, OutboxStatus status, Integer retryCount, Integer maxRetries,
                                                   LocalDateTime createdAt, LocalDateTime processedAt, String errorMessage) {
      return new SchedulerOutboxEvent(eventId, eventType, schedulerId, payload, status, retryCount, maxRetries, createdAt, processedAt, errorMessage);
  }
  ```

- [ ] **OutboxEventType Enum**
  - `SCHEDULER_CREATED`
  - `SCHEDULER_UPDATED`
  - `SCHEDULER_DELETED`

- [ ] **OutboxStatus Enum**
  - `PENDING`: 처리 대기
  - `PUBLISHED`: 처리 완료
  - `FAILED`: 재시도 실패 (영구)

### 비즈니스 규칙

#### 스케줄 등록 (Register Scheduler)

- [ ] **스케줄러 이름 중복 처리**
  - 동일 Seller 내에서 `schedulerName` 중복 불가
  - Unique Constraint: `(sellerId, schedulerName)`
  - 중복 시 예외: `DuplicateSchedulerNameException`
  - 다른 Seller 간에는 같은 `schedulerName` 사용 가능

- [ ] **Cron Expression 검증** (Domain Layer)
  - AWS EventBridge 형식 검증
  - 최소 1시간 간격 검증
  - 검증 실패 시 예외: `InvalidCronExpressionException`

- [ ] **초기 상태**
  - 등록 시 기본 상태: `PENDING`

- [ ] **Seller 상태 확인**
  - INACTIVE Seller의 스케줄 등록 불가
  - 예외: `SellerNotActiveException`

#### 스케줄 수정 (Update Scheduler)

- [ ] **수정 가능 항목**
  - `schedulerName`: 변경 가능 (중복 체크 재수행)
  - `cronExpression`: 변경 가능 (Domain 검증)
  - `status`: 변경 가능 (ACTIVE ↔ INACTIVE, PENDING → ACTIVE)

- [ ] **Domain Event 발행**
  - 수정 시 `SchedulerUpdatedEvent` 발행
  - Event 속성: `schedulerId`, `changedFields`, `occurredAt`

#### 스케줄 비활성화 (Deactivate Scheduler)

- [ ] **개별 비활성화**
  - 개별 스케줄 비활성화 가능
  - Domain Event 발행: `SchedulerDeactivatedEvent`

#### Outbox Pattern 비즈니스 규칙

- [ ] **재시도 전략**
  - 재시도 간격: Exponential Backoff with Jitter
    - 1차: 1분 후
    - 2차: 5분 후
    - 3차: 15분 후
  - 최대 재시도 횟수: 3회

- [ ] **재시도 실패 처리**
  - 모든 재시도 실패 시:
    - Outbox.status → FAILED (영구)
    - 수동 개입 필요 (Slack 알림은 Application Layer에서 처리)

- [ ] **Outbox 정리 규칙**
  - PUBLISHED 상태 이벤트: 30일 후 삭제 가능
  - FAILED 상태 이벤트: 영구 보관
  - PENDING 상태 이벤트: 24시간 경과 시 알림 필요

### Domain Events

- [ ] **SchedulerRegisteredEvent**
  ```java
  public record SchedulerRegisteredEvent(
      Long schedulerId,
      Long sellerId,
      String schedulerName,
      String cronExpression,
      LocalDateTime occurredAt
  ) {}
  ```

- [ ] **SchedulerUpdatedEvent**
  ```java
  public record SchedulerUpdatedEvent(
      Long schedulerId,
      List<String> changedFields,
      LocalDateTime occurredAt
  ) {}
  ```

- [ ] **SchedulerDeactivatedEvent**
  ```java
  public record SchedulerDeactivatedEvent(
      Long schedulerId,
      LocalDateTime occurredAt
  ) {}
  ```

### Domain Exceptions

- [ ] **DuplicateSchedulerNameException**
  - 메시지: "이미 등록된 스케줄러 이름입니다."

- [ ] **InvalidCronExpressionException**
  - 메시지: "유효하지 않은 Cron Expression 형식입니다."

- [ ] **SellerNotActiveException**
  - 메시지: "비활성 상태의 셀러는 스케줄러를 등록할 수 없습니다."

- [ ] **SchedulerNotFoundException**
  - 메시지: "존재하지 않는 스케줄러입니다."

---

## ⚠️ 제약사항

### Zero-Tolerance 규칙

- [ ] **Lombok 금지**
  - Pure Java 또는 Record 사용

- [ ] **Law of Demeter 준수**
  - Getter 체이닝 금지

- [ ] **Long FK 전략**
  - JPA 관계 어노테이션 금지
  - `private Long sellerId;` (O)
  - `@ManyToOne private Seller seller;` (X)

- [ ] **Tell Don't Ask 패턴**
  - Domain 메서드로 비즈니스 로직 캡슐화

### 테스트 규칙

- [ ] **ArchUnit 테스트 필수**
  - Domain Layer는 다른 레이어 의존 금지

- [ ] **TestFixture 사용 필수**
  - CrawlingScheduler, SchedulerOutboxEvent 테스트 데이터

- [ ] **테스트 커버리지 > 80%**

---

## ✅ 완료 조건

- [ ] CrawlingScheduler Aggregate 구현 완료
  - 스케줄 등록 (register)
  - 스케줄 수정 (update)
  - 스케줄 비활성화 (deactivate)
  - Domain Event 발행

- [ ] SchedulerOutboxEvent Aggregate 구현 완료
  - 재시도 로직
  - 상태 관리 (PENDING, PUBLISHED, FAILED)

- [ ] Value Object 구현 완료
  - CronExpression (AWS EventBridge 형식 검증)
  - SchedulerStatus Enum
  - OutboxEventType Enum
  - OutboxStatus Enum

- [ ] Domain Exception 구현 완료
  - DuplicateSchedulerNameException
  - InvalidCronExpressionException
  - SellerNotActiveException
  - SchedulerNotFoundException

- [ ] Domain Unit Test 완료
  - CrawlingScheduler 테스트
  - SchedulerOutboxEvent 테스트
  - CronExpression VO 테스트
  - Domain Event 발행 테스트
  - TestFixture 패턴 적용

- [ ] ArchUnit 테스트 완료

- [ ] Zero-Tolerance 규칙 준수 확인

- [ ] 코드 리뷰 승인

- [ ] PR 머지 완료

---

## 🔗 관련 문서

- **PRD**: `/Users/sangwon-ryu/crawlinghub/docs/prd/mustit-seller-crawler-scheduler.md`
- **Plan**: `docs/prd/seller/eventbridge/plans/EVENTBRIDGE-001-domain-plan.md` (create-plan 후 생성)
- **Jira**: (sync-to-jira 후 추가)
- **코딩 규칙**: `docs/coding_convention/02-domain-layer/`

---

## 📋 다음 단계

1. `/create-plan EVENTBRIDGE-001` - TDD Plan 생성
2. `/kb/domain/go` - Domain Layer TDD 시작
