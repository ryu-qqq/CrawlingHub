# EVENTBRIDGE-003: EventBridge Persistence Layer 구현

**Bounded Context**: Seller
**Sub-Context**: EventBridge (스케줄링)
**Layer**: Persistence Layer
**브랜치**: feature/EVENTBRIDGE-003-persistence
**Jira URL**: (sync-to-jira 후 추가)

---

## 📝 목적

EventBridge 스케줄링 데이터 영속성을 담당하는 Persistence Layer 구현.

**핵심 역할**:
- JPA Entity 설계 (CrawlingSchedule, CrawlingScheduleExecution, SchedulerOutbox)
- Repository 구현
- Adapter 구현
- 인덱스 최적화

---

## 🎯 요구사항

### 1. JPA Entity 설계

#### CrawlingScheduleJpaEntity

- [ ] **테이블**: `crawling_schedules`
- [ ] **필드**:
  - id: Long (PK)
  - schedule_id: String (UUID, Unique, Index)
  - seller_id: String (FK, Index, Unique) // 1 Seller = 1 Schedule
  - schedule_rule: String (EventBridge Rule Name)
  - schedule_expression: String (Cron 표현식)
  - status: String (ACTIVE/INACTIVE/FAILED)
  - created_at, updated_at: LocalDateTime

- [ ] **인덱스**:
  - `idx_schedule_id` (schedule_id) - Unique
  - `idx_seller_id` (seller_id) - Unique
  - `idx_status` (status)

#### CrawlingScheduleExecutionJpaEntity

- [ ] **테이블**: `crawling_schedule_executions`
- [ ] **필드**:
  - id: Long (PK)
  - execution_id: String (UUID, Unique, Index)
  - schedule_id: String (FK, Index)
  - seller_id: String (FK, Index)
  - status: String (STARTED/IN_PROGRESS/COMPLETED/FAILED)
  - total_tasks_created, completed_tasks, failed_tasks: Integer
  - progress_rate, success_rate: Double
  - started_at: LocalDateTime (Index)
  - completed_at: LocalDateTime (Nullable)
  - error_message: String (Nullable, TEXT)

- [ ] **인덱스**:
  - `idx_execution_id` (execution_id) - Unique
  - `idx_schedule_id_started_at` (schedule_id, started_at DESC)
  - `idx_seller_id_started_at` (seller_id, started_at DESC)
  - `idx_status` (status)

- [ ] **파티셔닝**: `started_at` 기준 월별 (PARTITION BY RANGE, 1년 후 적용)

#### SchedulerOutboxJpaEntity

- [ ] **테이블**: `scheduler_outbox`
- [ ] **필드**:
  - id: Long (PK)
  - outbox_id: String (UUID, Unique, Index)
  - schedule_id: String (FK, Index)
  - event_type: String (SCHEDULE_CREATED/UPDATED/DELETED)
  - payload: String (TEXT, EventBridge API JSON)
  - status: String (WAITING/SENDING/COMPLETED/FAILED)
  - retry_count: Integer (Default 0)
  - error_message: String (Nullable, TEXT)
  - created_at: LocalDateTime (Index)
  - sent_at: LocalDateTime (Nullable)

- [ ] **인덱스**:
  - `idx_outbox_id` (outbox_id) - Unique
  - `idx_status_created_at` (status, created_at ASC) // 배치 처리 (오래된 순)

---

### 2. Repository 구현

- [ ] **CrawlingScheduleJpaRepository**
- [ ] **CrawlingScheduleExecutionJpaRepository**
- [ ] **SchedulerOutboxJpaRepository**

---

### 3. Adapter 구현

- [ ] **Command/Query Adapters** (각 Entity별)
- [ ] **Mapper** (Domain ↔ Entity 변환)

---

### 4. Flyway 마이그레이션

- [ ] **V2__create_crawling_schedules_table.sql**
- [ ] **V3__create_crawling_schedule_executions_table.sql**
- [ ] **V4__create_scheduler_outbox_table.sql**

---

## ✅ 완료 조건

- [ ] 3개 JPA Entity 구현 완료
- [ ] 3개 JPA Repository 구현 완료
- [ ] Adapter 및 Mapper 구현 완료
- [ ] Flyway 마이그레이션 완료
- [ ] Integration Test 완료 (TestContainers)
- [ ] ArchUnit 테스트 통과

---

## 🔗 관련 문서

- **Plan**: docs/prd/seller/eventbridge/plans/EVENTBRIDGE-003-persistence-plan.md
- **Persistence Layer 규칙**: docs/coding_convention/04-persistence-layer/
