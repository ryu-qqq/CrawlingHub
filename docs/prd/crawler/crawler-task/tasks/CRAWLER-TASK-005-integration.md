# CRAWLER-TASK-005: CrawlerTask Integration Test 구현

**Bounded Context**: Crawler
**Sub-Context**: CrawlerTask
**Layer**: Integration Test
**브랜치**: feature/CRAWLER-TASK-005-integration

---

## 📝 목적

CrawlerTask E2E 시나리오 테스트.

---

## 🎯 요구사항

### 1. E2E 시나리오

#### 시나리오: 크롤링 트리거 → 태스크 발행 → 처리 → 상품 저장

- [ ] **Given: Seller 등록**
- [ ] **When: 크롤링 트리거** (TriggerCrawlingUseCase)
- [ ] **Then: MINISHOP 태스크 생성 확인**
- [ ] **When: 태스크 발행** (PublishCrawlerTasksUseCase)
- [ ] **Then: SQS 메시지 발행 확인** (Localstack)
- [ ] **When: 태스크 처리** (ProcessCrawlerTaskUseCase)
- [ ] **Then: 크롤링 결과 저장 확인** (Product 생성)

### 2. SQS 연동 테스트

- [ ] SQS 메시지 발행 테스트 (Localstack)
- [ ] SQS 메시지 폴링 테스트 (Localstack)

---

## ✅ 완료 조건

- [ ] E2E 시나리오 테스트 통과
- [ ] SQS 연동 테스트 통과 (Localstack)

---

## 🔗 관련 문서

- **Plan**: docs/prd/crawler/crawler-task/plans/CRAWLER-TASK-005-integration-plan.md
