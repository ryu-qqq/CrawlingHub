# CRAWLER-TASK-001: CrawlerTask Domain Layer 구현

**Bounded Context**: Crawler
**Sub-Context**: CrawlerTask (크롤링 태스크)
**Layer**: Domain Layer
**브랜치**: feature/CRAWLER-TASK-001-domain
**Jira URL**: (sync-to-jira 후 추가)

---

## 📝 목적

CrawlerTask Aggregate의 비즈니스 핵심 로직을 담당하는 Domain Layer 구현.

**핵심 역할**:
- CrawlerTask 비즈니스 규칙 구현
- 상태 전환 로직 (WAITING → PUBLISHED → IN_PROGRESS → COMPLETED/FAILED)
- RequestUrl VO를 통한 URL 형식 검증
- 재시도 로직 (최대 2회)

---

## 🎯 요구사항

### 1. Aggregate: CrawlerTask (크롤링 태스크)

- [ ] **CrawlerTask Aggregate 구현**
  - taskId (TaskId VO, UUID)
  - sellerId (SellerId VO)
  - taskType (CrawlerTaskType Enum)
  - requestUrl (RequestUrl VO)
  - status (CrawlerTaskStatus Enum)
  - retryCount (Integer, 최대 2회)

- [ ] **비즈니스 규칙**
  - 태스크 생성 시 상태 WAITING
  - RequestUrl VO가 taskType에 따라 자동 검증
    - MINISHOP: `/searchmini-shop-search` 패턴 포함 확인
    - PRODUCT_DETAIL: `/item/{숫자}/detail/top` 정규식 검증
    - PRODUCT_OPTION: `/auction_products/{숫자}/options` 정규식 검증

- [ ] **상태 전환 로직**
  - WAITING → PUBLISHED → IN_PROGRESS → COMPLETED/FAILED/RETRY
  - 재시도 최대 2회
  - 재시도 초과 시 FAILED

- [ ] **Value Objects**
  - TaskId: UUID
  - RequestUrl: String (taskType 기반 URL 형식 검증)
  - CrawlerTaskType: Enum (MINISHOP, PRODUCT_DETAIL, PRODUCT_OPTION)
  - CrawlerTaskStatus: Enum (WAITING, PUBLISHED, IN_PROGRESS, COMPLETED, FAILED, RETRY)

- [ ] **Domain 메서드**
  - `create(sellerId, taskType, requestUrl)`: 태스크 생성
  - `publish()`: 발행 상태로 전환
  - `start()`: 진행 중 상태로 전환
  - `complete()`: 완료
  - `fail(errorMessage)`: 실패 처리
  - `retry()`: 재시도 (retryCount < 2)

---

## ⚠️ 제약사항

### Zero-Tolerance 규칙

- [ ] **Lombok 금지**: Pure Java 또는 Record 사용
- [ ] **Law of Demeter 준수**: Getter 체이닝 금지
- [ ] **Tell Don't Ask**: 내부 상태 기반 판단
- [ ] **Long FK 전략**: 관계 어노테이션 금지

### 테스트 규칙

- [ ] **ArchUnit 테스트 필수**
- [ ] **TestFixture 패턴 사용**
- [ ] **테스트 커버리지 > 80%**

---

## ✅ 완료 조건

- [ ] CrawlerTask Aggregate 구현 완료
- [ ] 모든 Value Object 구현 완료
- [ ] 모든 Domain 메서드 구현 완료
- [ ] Unit Test 작성 완료
- [ ] Zero-Tolerance 규칙 준수

---

## 🔗 관련 문서

- **Plan**: docs/prd/crawler/crawler-task/plans/CRAWLER-TASK-001-domain-plan.md
- **Domain Layer 규칙**: docs/coding_convention/02-domain-layer/

---

## 📚 참고사항

### RequestUrl VO 검증 예시

```java
public record RequestUrl(String value, CrawlerTaskType taskType) {
    public RequestUrl {
        validate(value, taskType);
    }

    private void validate(String url, CrawlerTaskType type) {
        switch (type) {
            case MINISHOP -> {
                if (!url.contains("/searchmini-shop-search")) {
                    throw new InvalidRequestUrlException("MINISHOP URL must contain /searchmini-shop-search");
                }
            }
            case PRODUCT_DETAIL -> {
                if (!url.matches(".*/item/\\d+/detail/top.*")) {
                    throw new InvalidRequestUrlException("PRODUCT_DETAIL URL pattern invalid");
                }
            }
            case PRODUCT_OPTION -> {
                if (!url.matches(".*/auction_products/\\d+/options.*")) {
                    throw new InvalidRequestUrlException("PRODUCT_OPTION URL pattern invalid");
                }
            }
        }
    }
}
```
