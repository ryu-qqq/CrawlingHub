# CRAWLER-TASK-004: CrawlerTask REST API Layer 구현

**Bounded Context**: Crawler
**Sub-Context**: CrawlerTask
**Layer**: REST API Layer
**브랜치**: feature/CRAWLER-TASK-004-rest-api

---

## 📝 목적

크롤링 메트릭 조회 API 구현.

---

## 🎯 요구사항

### 1. API 엔드포인트

#### GET /api/v1/metrics/crawling - 크롤링 메트릭 조회
- Request: `GetCrawlingMetricsRequest` (Query Parameters: sellerId, date)
- Response: `CrawlingMetricsResponse`
- Status Code: 200 OK

### 2. Response DTO

```java
public record CrawlingMetricsResponse(
    String sellerId,
    LocalDate date,
    Double successRate,
    Double progressRate,
    TaskStats taskStats
) {
    public record TaskStats(
        Integer total,
        Integer completed,
        Integer failed,
        Integer inProgress
    ) {}
}
```

---

## ✅ 완료 조건

- [ ] GET /api/v1/metrics/crawling 구현 완료
- [ ] Integration Test 완료

---

## 🔗 관련 문서

- **Plan**: docs/prd/crawler/crawler-task/plans/CRAWLER-TASK-004-rest-api-plan.md
