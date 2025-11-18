# CRAWLER-TASK-002: CrawlerTask Application Layer 구현

**Bounded Context**: Crawler
**Sub-Context**: CrawlerTask
**Layer**: Application Layer
**브랜치**: feature/CRAWLER-TASK-002-application

---

## 📝 목적

CrawlerTask 관련 Use Case 오케스트레이션.

---

## 🎯 요구사항

### 1. Use Cases

#### TriggerCrawlingUseCase
- Seller 조회 → 미니샵 API 총 상품 수 확인 → MINISHOP 태스크 생성 (Bulk Insert)

#### PublishCrawlerTasksUseCase
- WAITING 태스크 조회 → PUBLISHED 상태 변경 → SQS 발행 (트랜잭션 밖)

#### ProcessCrawlerTaskUseCase
- 태스크 조회 → IN_PROGRESS 상태 → 크롤링 실행 (트랜잭션 밖) → 결과 저장 (COMPLETED/RETRY/FAILED)

#### GetCrawlingMetricsUseCase
- 셀러별 크롤링 메트릭 조회 (성공률, 진행률, 태스크 통계)

---

## ✅ 완료 조건

- [ ] 4개 Use Case 구현 완료
- [ ] Transaction 경계 검증 완료

---

## 🔗 관련 문서

- **Plan**: docs/prd/crawler/crawler-task/plans/CRAWLER-TASK-002-application-plan.md
