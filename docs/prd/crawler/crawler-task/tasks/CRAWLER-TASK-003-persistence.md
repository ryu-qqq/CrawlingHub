# CRAWLER-TASK-003: CrawlerTask Persistence Layer 구현

**Bounded Context**: Crawler
**Sub-Context**: CrawlerTask
**Layer**: Persistence Layer
**브랜치**: feature/CRAWLER-TASK-003-persistence

---

## 📝 목적

CrawlerTask 데이터 영속성.

---

## 🎯 요구사항

### 1. JPA Entity

#### CrawlerTaskJpaEntity
- 테이블: `crawler_tasks`
- 인덱스:
  - `idx_task_id` (task_id) - Unique
  - `idx_seller_id_created_at` (seller_id, created_at DESC)
  - `idx_status_created_at` (status, created_at DESC)

### 2. Repository

- CrawlerTaskJpaRepository
- CrawlerTaskQueryDslRepository (메트릭 집계용)

### 3. Flyway

- V5__create_crawler_tasks_table.sql

---

## ✅ 완료 조건

- [ ] CrawlerTaskJpaEntity 구현 완료
- [ ] Repository 구현 완료
- [ ] QueryDSL DTO Projection 완료 (메트릭 집계)

---

## 🔗 관련 문서

- **Plan**: docs/prd/crawler/crawler-task/plans/CRAWLER-TASK-003-persistence-plan.md
