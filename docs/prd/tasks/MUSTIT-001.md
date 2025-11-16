# MUSTIT-001: Domain Layer 구현

**Epic**: 머스트잇 셀러 크롤러
**Layer**: Domain Layer
**브랜치**: feature/MUSTIT-001-domain
**Jira URL**: (sync-to-jira 후 추가)

---

## 📝 목적

비즈니스 핵심 로직을 담당하는 Domain Layer 구현. 외부 의존성 없이 순수한 비즈니스 규칙과 도메인 개념을 표현합니다.

**핵심 역할**:
- 비즈니스 규칙 및 불변식 구현
- Aggregate, Entity, Value Object 설계
- Tell Don't Ask 원칙 준수
- Law of Demeter 준수

---

## 🎯 요구사항

### 1. Aggregate: Seller (셀러)

- [ ] **Seller Aggregate 구현**
  - sellerId (SellerId VO)
  - name (String)
  - crawlingInterval (CrawlingInterval VO)
  - status (SellerStatus Enum)
  - totalProductCount (Integer)

- [ ] **비즈니스 규칙**
  - 셀러 ID 고유성 검증 (중복 불가)
  - 기본 크롤링 주기 1일
  - 등록 시 상태 ACTIVE
  - 크롤링 주기는 일(day) 단위만 허용 (1-30일)

- [ ] **Value Objects**
  - SellerId: String (머스트잇 셀러 ID)
  - CrawlingInterval: Integer (1-30)
  - SellerStatus: Enum (ACTIVE, INACTIVE)

- [ ] **Domain 메서드**
  - `register(sellerId, name, intervalDays)`: 셀러 등록
  - `updateInterval(newIntervalDays)`: 주기 변경
  - `activate()`: 활성화
  - `deactivate()`: 비활성화
  - `updateTotalProductCount(count)`: 총 상품 수 업데이트

### 2. Aggregate: CrawlerTask (크롤링 태스크)

- [ ] **CrawlerTask Aggregate 구현**
  - taskId (TaskId VO, UUID)
  - sellerId (SellerId VO)
  - taskType (CrawlerTaskType Enum)
  - requestUrl (String)
  - status (CrawlerTaskStatus Enum)
  - retryCount (Integer, 최대 2회)

- [ ] **비즈니스 규칙**
  - 태스크 생성 시 상태 WAITING
  - taskType별 requestUrl 형식 검증
    - MINISHOP: `/mustit-api/facade-api/v1/searchmini-shop-search?...`
    - PRODUCT_DETAIL: `/mustit-api/facade-api/v1/item/{item_no}/detail/top`
    - PRODUCT_OPTION: `/mustit-api/legacy-api/v1/auction_products/{item_no}/options`

- [ ] **상태 전환 로직**
  - WAITING → PUBLISHED → IN_PROGRESS → COMPLETED/FAILED/RETRY
  - 재시도 최대 2회
  - 재시도 초과 시 FAILED

- [ ] **Value Objects**
  - TaskId: UUID
  - CrawlerTaskType: Enum (MINISHOP, PRODUCT_DETAIL, PRODUCT_OPTION)
  - CrawlerTaskStatus: Enum (WAITING, PUBLISHED, IN_PROGRESS, COMPLETED, FAILED, RETRY)

- [ ] **Domain 메서드**
  - `create(sellerId, taskType, requestUrl)`: 태스크 생성
  - `publish()`: 발행 상태로 전환
  - `start()`: 진행 중 상태로 전환
  - `complete()`: 완료
  - `fail(errorMessage)`: 실패 처리
  - `retry()`: 재시도 (retryCount < 2)

### 3. Aggregate: UserAgent (유저 에이전트)

- [ ] **UserAgent Aggregate 구현**
  - userAgentId (UserAgentId VO, UUID)
  - userAgentString (String)
  - token (String, Nullable)
  - status (UserAgentStatus Enum)
  - requestCount (Integer)
  - lastRequestAt (LocalDateTime)
  - tokenIssuedAt (LocalDateTime)

- [ ] **비즈니스 규칙**
  - 50개 미리 정의된 UserAgent 문자열 사용
  - 생성 시 token null, status ACTIVE
  - 시간당 80회 토큰 버킷 리미터
  - 429 응답 시 즉시 SUSPENDED 상태 전환

- [ ] **토큰 버킷 리미터 로직**
  - 1시간 기준 (10:00-11:00)
  - requestCount < 80 && lastRequestAt 1시간 이내 → 허용
  - 1시간 경과 시 requestCount 리셋

- [ ] **Value Objects**
  - UserAgentId: UUID
  - UserAgentStatus: Enum (ACTIVE, SUSPENDED, BLOCKED)

- [ ] **Domain 메서드**
  - `create(userAgentString)`: UserAgent 생성
  - `issueToken(token)`: 토큰 발급
  - `canMakeRequest()`: 요청 가능 여부 확인 (토큰 버킷)
  - `incrementRequestCount()`: 요청 수 증가
  - `resetRequestCount()`: 1시간 경과 시 리셋
  - `suspend()`: 429 응답 시 일시 중지
  - `activate()`: 재활성화

### 4. Aggregate: Product (상품)

- [ ] **Product Aggregate 구현**
  - productId (ProductId VO, UUID)
  - itemNo (Long)
  - sellerId (SellerId VO)
  - minishopDataHash (String)
  - detailDataHash (String)
  - optionDataHash (String)
  - isComplete (Boolean)

- [ ] **비즈니스 규칙**
  - 미니샵 크롤링 시 itemNo 추출하여 생성
  - 초기 isComplete = false
  - 모든 해시값 존재 시 isComplete = true
  - 해시값 비교로 변경 감지 (MD5)

- [ ] **상품 완성 기준**
  - 미니샵 + 상세 + 옵션 각 1번씩 크롤링 완료
  - 모든 해시값 != null

- [ ] **Value Objects**
  - ProductId: UUID
  - ItemNo: Long

- [ ] **Domain 메서드**
  - `create(itemNo, sellerId)`: 상품 생성
  - `updateMinishopData(rawJson)`: 미니샵 데이터 업데이트 및 해시 계산
  - `updateDetailData(rawJson)`: 상세 데이터 업데이트 및 해시 계산
  - `updateOptionData(rawJson)`: 옵션 데이터 업데이트 및 해시 계산
  - `isComplete()`: 완성 여부 확인
  - `hasChanged(oldHash, newHash)`: 변경 감지

### 5. Aggregate: ProductOutbox (상품 외부 전송)

- [ ] **ProductOutbox Aggregate 구현**
  - outboxId (OutboxId VO, UUID)
  - productId (ProductId VO)
  - eventType (OutboxEventType Enum)
  - payload (String, JSON)
  - status (OutboxStatus Enum)
  - retryCount (Integer)

- [ ] **비즈니스 규칙**
  - 상품 변경 감지 시 자동 생성
  - 초기 상태 WAITING
  - 배치 처리 (5분마다)
  - 재시도 최대 5회 (Exponential Backoff)

- [ ] **상태 전환 로직**
  - WAITING → SENDING → COMPLETED/FAILED
  - 재시도 5회 초과 시 FAILED

- [ ] **Value Objects**
  - OutboxId: UUID
  - OutboxEventType: Enum (PRODUCT_CREATED, PRODUCT_UPDATED)
  - OutboxStatus: Enum (WAITING, SENDING, COMPLETED, FAILED)

- [ ] **Domain 메서드**
  - `create(productId, eventType, payload)`: Outbox 생성
  - `send()`: 전송 중 상태로 전환
  - `complete()`: 전송 완료
  - `fail(errorMessage)`: 전송 실패
  - `canRetry()`: 재시도 가능 여부 확인 (retryCount < 5)

---

## ⚠️ 제약사항

### Zero-Tolerance 규칙

- [ ] **Lombok 금지**: Pure Java 또는 Record 사용
  - ✅ `public record SellerId(String value) {}`
  - ❌ `@Value public class SellerId { ... }`

- [ ] **Law of Demeter 준수**: Getter 체이닝 금지
  - ✅ `seller.getCrawlingIntervalDays()`
  - ❌ `seller.getCrawlingInterval().getDays()`

- [ ] **Tell Don't Ask**: 내부 상태 기반 판단
  - ✅ `userAgent.canMakeRequest()` (내부에서 판단)
  - ❌ `if (userAgent.getRequestCount() < 80) { ... }` (외부에서 판단)

- [ ] **Long FK 전략**: 관계 어노테이션 금지
  - ✅ `private SellerId sellerId;`
  - ❌ `@ManyToOne private Seller seller;`

### 테스트 규칙

- [ ] **ArchUnit 테스트 필수**
  - Lombok 사용 금지 검증
  - Getter 체이닝 금지 검증
  - 패키지 의존성 검증

- [ ] **TestFixture 패턴 사용**
  - Domain 객체 생성을 위한 Fixture 클래스
  - 테스트 가독성 향상

- [ ] **테스트 커버리지 > 80%**
  - Domain 메서드 모두 테스트
  - 비즈니스 규칙 검증

---

## ✅ 완료 조건

- [ ] 5개 Aggregate 구현 완료 (Seller, CrawlerTask, UserAgent, Product, ProductOutbox)
- [ ] 모든 Value Object 구현 완료
- [ ] 모든 Enum 구현 완료
- [ ] 모든 Domain 메서드 구현 완료
- [ ] Unit Test 작성 완료 (커버리지 > 80%)
- [ ] ArchUnit 테스트 통과
- [ ] TestFixture 패턴 적용
- [ ] Zero-Tolerance 규칙 준수
- [ ] 코드 리뷰 승인
- [ ] PR 머지 완료

---

## 🔗 관련 문서

- **PRD**: docs/prd/mustit-seller-crawler.md
- **Plan**: docs/prd/plans/MUSTIT-001-domain-plan.md (create-plan 후 생성)
- **Jira**: (sync-to-jira 후 추가)
- **Domain Layer 규칙**: docs/coding_convention/02-domain-layer/

---

## 📚 참고사항

### Domain 메서드 네이밍 규칙

- 생성: `create()`, `register()`
- 상태 전환: `activate()`, `deactivate()`, `publish()`, `start()`, `complete()`, `fail()`
- 검증: `canMakeRequest()`, `isComplete()`, `hasChanged()`
- 업데이트: `update*()`, `increment*()`, `reset*()`

### 해시 계산 전략

- **알고리즘**: MD5 (빠른 성능, 변경 감지 목적)
- **대상**: 전체 JSON 응답 (raw data)
- **구현**: `MessageDigest.getInstance("MD5")`

### 토큰 버킷 리미터 구현

```java
public boolean canMakeRequest() {
    if (token == null) return false;

    LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);

    // 1시간 경과 시 requestCount 리셋
    if (lastRequestAt != null && lastRequestAt.isBefore(oneHourAgo)) {
        this.requestCount = 0;
    }

    return requestCount < 80;
}
```
