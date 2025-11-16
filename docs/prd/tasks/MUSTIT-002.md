# MUSTIT-002: Application Layer 구현

**Epic**: 머스트잇 셀러 크롤러
**Layer**: Application Layer
**브랜치**: feature/MUSTIT-002-application
**Jira URL**: (sync-to-jira 후 추가)

---

## 📝 목적

비즈니스 유스케이스 오케스트레이션을 담당하는 Application Layer 구현. Domain과 Infrastructure 사이의 중재자 역할을 수행합니다.

**핵심 역할**:
- Use Case 구현 (Command/Query 분리)
- Transaction 경계 관리
- Port 정의 (In/Out)
- Domain ↔ DTO 변환 (Assembler)

---

## 🎯 요구사항

### 1. Command Use Cases

#### RegisterSellerUseCase (셀러 등록)

- [ ] **Input/Output 정의**
  - Input: `RegisterSellerCommand(sellerId, name, crawlingIntervalDays)`
  - Output: `SellerResponse(sellerId, name, status, crawlingIntervalDays)`

- [ ] **비즈니스 로직**
  1. 셀러 ID 중복 확인 (SellerQueryPort)
  2. Seller Aggregate 생성
  3. DB 저장 (SellerCommandPort)
  4. **트랜잭션 커밋**
  5. EventBridge Rule 생성 (트랜잭션 밖, EventBridgePort)

- [ ] **Transaction 경계**: Yes (DB 저장까지만)

#### UpdateSellerIntervalUseCase (셀러 주기 변경)

- [ ] **Input/Output 정의**
  - Input: `UpdateSellerIntervalCommand(sellerId, newIntervalDays)`
  - Output: `SellerResponse`

- [ ] **비즈니스 로직**
  1. Seller 조회 (SellerQueryPort)
  2. 주기 변경 (Domain 메서드)
  3. DB 저장 (SellerCommandPort)
  4. **트랜잭션 커밋**
  5. EventBridge Rule 업데이트 (트랜잭션 밖)

- [ ] **Transaction 경계**: Yes (DB 저장까지만)

#### TriggerCrawlingUseCase (크롤링 트리거)

- [ ] **Input/Output 정의**
  - Input: `TriggerCrawlingCommand(sellerId)`
  - Output: `CrawlingTriggeredResponse(taskCount)`

- [ ] **비즈니스 로직**
  1. Seller 조회 (ACTIVE 상태만)
  2. 미니샵 API 호출 → 총 상품 수 조회 (MustitApiPort)
     - ⚠️ 빠른 조회(< 1초)이므로 트랜잭션 내 허용
  3. 총 상품 수 업데이트 (Seller Aggregate)
  4. 페이지 수 계산: `Math.ceil(totalProductCount / 500)`
  5. 페이지별 MINISHOP 태스크 생성 (CrawlerTask Aggregate)
  6. DB 저장 (Bulk Insert, CrawlerTaskCommandPort)
  7. **트랜잭션 커밋**
  8. SQS 발행은 다음 UseCase (PublishCrawlerTasksUseCase)

- [ ] **Transaction 경계**: Yes (미니샵 조회 포함)

#### PublishCrawlerTasksUseCase (크롤러 태스크 발행)

- [ ] **Input/Output 정의**
  - Input: `PublishCrawlerTasksCommand(taskIds)`
  - Output: `PublishedTasksResponse(publishedCount)`

- [ ] **비즈니스 로직**
  1. WAITING 상태 태스크 조회 (Batch)
  2. 상태 PUBLISHED로 변경 (Domain 메서드)
  3. publishedAt 기록
  4. DB 저장
  5. **트랜잭션 커밋**
  6. SQS 발행 (트랜잭션 밖, SqsPublisherPort)
     - Batch Send (최대 10개씩)
     - 발행 실패 시 재시도 3회

- [ ] **Transaction 경계**: Yes (상태 업데이트만)

#### ProcessCrawlerTaskUseCase (크롤러 태스크 처리)

- [ ] **Input/Output 정의**
  - Input: `ProcessCrawlerTaskCommand(taskId)`
  - Output: `TaskProcessedResponse(status)`

- [ ] **비즈니스 로직**
  1. 태스크 조회 (PUBLISHED 상태만)
  2. 상태 IN_PROGRESS로 변경
  3. startedAt 기록
  4. DB 저장
  5. **트랜잭션 커밋**
  6. 크롤링 실행 (트랜잭션 밖)
     - UserAgent 할당 (UserAgentPoolManager)
     - 머스트잇 API 호출 (MustitApiPort)
     - 응답 데이터 처리
  7. 트랜잭션 시작
  8. 크롤링 결과에 따라 상태 변경
     - 성공: COMPLETED
     - 실패 (retryCount < 2): RETRY
     - 실패 (retryCount >= 2): FAILED
  9. Product 업데이트 (해시 계산 및 변경 감지)
  10. 변경 감지 시 ProductOutbox 생성
  11. DB 저장
  12. **트랜잭션 커밋**

- [ ] **Transaction 경계**: 2단계 (상태 업데이트 → 크롤링 → 결과 저장)

#### ProcessProductOutboxUseCase (상품 외부 전송)

- [ ] **Input/Output 정의**
  - Input: `ProcessProductOutboxCommand(outboxIds)` (배치)
  - Output: `OutboxProcessedResponse(successCount, failedCount)`

- [ ] **비즈니스 로직**
  1. WAITING 상태 Outbox 조회 (Batch, 최대 100개)
  2. 상태 SENDING으로 변경
  3. DB 저장
  4. **트랜잭션 커밋**
  5. 외부 API 호출 (트랜잭션 밖, ExternalProductApiPort)
     - REST API: 외부 상품 서버
     - Timeout: 5초
  6. 트랜잭션 시작
  7. 결과에 따라 상태 변경
     - 성공: COMPLETED, sentAt 기록
     - 실패: retryCount 증가, WAITING 또는 FAILED
  8. DB 저장
  9. **트랜잭션 커밋**

- [ ] **Transaction 경계**: 2단계 (상태 업데이트 → 외부 전송 → 결과 저장)

---

### 2. Query Use Cases

#### GetSellerUseCase (셀러 조회)

- [ ] **Input/Output 정의**
  - Input: `GetSellerQuery(sellerId)`
  - Output: `SellerDetailResponse`

- [ ] **Transaction**: ReadOnly

#### ListSellersUseCase (셀러 목록 조회)

- [ ] **Input/Output 정의**
  - Input: `ListSellersQuery(status, page, size)`
  - Output: `PageResponse<SellerSummaryResponse>`

- [ ] **페이징**: Offset-based
- [ ] **Transaction**: ReadOnly

#### GetCrawlingMetricsUseCase (크롤링 메트릭 조회)

- [ ] **Input/Output 정의**
  - Input: `GetCrawlingMetricsQuery(sellerId, date)`
  - Output: `CrawlingMetricsResponse(successRate, progressRate, taskStats)`

- [ ] **메트릭 계산 로직**
  - 자정 기준 (00:00-24:00)
  - 성공률: 성공 태스크 / 전체 태스크 * 100
  - 진행률: 완료 상품 / 셀러 총 상품 수 * 100
  - 태스크 통계: COMPLETED, FAILED, IN_PROGRESS 개수

- [ ] **Transaction**: ReadOnly

#### GetUserAgentPoolStatusUseCase (UserAgent 풀 상태 조회)

- [ ] **Input/Output 정의**
  - Input: `GetUserAgentPoolStatusQuery()`
  - Output: `UserAgentPoolStatusResponse(totalCount, activeCount, suspendedCount, blockedCount)`

- [ ] **Transaction**: ReadOnly

---

### 3. Application Service

#### UserAgentPoolManager (UserAgent 풀 관리)

- [ ] **책임**
  - UserAgent 할당 (Round-robin)
  - 토큰 버킷 리미터 검증
  - 429 응답 시 UserAgent 일시 중지
  - 자동 복구 전략 (Scheduled Task)

- [ ] **메서드 구현**
  - `assignUserAgent()`: 사용 가능한 UserAgent 할당
    - Pessimistic Lock 사용 (`SELECT FOR UPDATE`)
    - Round-robin 알고리즘
    - `canMakeRequest()` 검증
  - `releaseUserAgent(userAgentId)`: UserAgent 반환
  - `suspendUserAgent(userAgentId)`: 429 응답 시 일시 중지
  - `recoverSuspendedUserAgents()`: 1시간 경과 UserAgent 복구 (Scheduled)

- [ ] **동시성 제어**
  - UserAgent 할당 시 Race Condition 방지
  - Pessimistic Lock 사용

---

### 4. Port 정의

#### Input Ports (Use Case 인터페이스)

- [ ] **Command Ports**
  - `RegisterSellerUseCase`
  - `UpdateSellerIntervalUseCase`
  - `TriggerCrawlingUseCase`
  - `PublishCrawlerTasksUseCase`
  - `ProcessCrawlerTaskUseCase`
  - `ProcessProductOutboxUseCase`

- [ ] **Query Ports**
  - `GetSellerUseCase`
  - `ListSellersUseCase`
  - `GetCrawlingMetricsUseCase`
  - `GetUserAgentPoolStatusUseCase`

#### Output Ports (Infrastructure 인터페이스)

- [ ] **Persistence Ports**
  - `SellerCommandPort`: save(), delete()
  - `SellerQueryPort`: findById(), findByStatus(), existsBySellerId()
  - `CrawlerTaskCommandPort`: save(), saveAll() (Bulk Insert)
  - `CrawlerTaskQueryPort`: findById(), findByStatus(), findBySellerIdAndDateRange()
  - `UserAgentCommandPort`: save()
  - `UserAgentQueryPort`: findById(), findByStatus(), findFirstActiveForUpdate()
  - `ProductCommandPort`: save()
  - `ProductQueryPort`: findByItemNo()
  - `ProductOutboxCommandPort`: save()
  - `ProductOutboxQueryPort`: findByStatusOrderByCreatedAtAsc()

- [ ] **Infrastructure Ports**
  - `EventBridgePort`: createRule(), updateRule(), deleteRule()
  - `SqsPublisherPort`: sendBatch()
  - `MustitApiPort`: crawlMinishop(), crawlProductDetail(), crawlProductOption()
  - `ExternalProductApiPort`: sendProductCreated(), sendProductUpdated()

---

### 5. DTO 정의

#### Command DTOs

- [ ] **RegisterSellerCommand**
  - sellerId: String
  - name: String
  - crawlingIntervalDays: Integer

- [ ] **UpdateSellerIntervalCommand**
  - sellerId: String
  - newIntervalDays: Integer

- [ ] **TriggerCrawlingCommand**
  - sellerId: String

- [ ] **PublishCrawlerTasksCommand**
  - taskIds: List<String>

- [ ] **ProcessCrawlerTaskCommand**
  - taskId: String

- [ ] **ProcessProductOutboxCommand**
  - outboxIds: List<String>

#### Query DTOs

- [ ] **GetSellerQuery**
  - sellerId: String

- [ ] **ListSellersQuery**
  - status: SellerStatus (Nullable)
  - page: Integer
  - size: Integer

- [ ] **GetCrawlingMetricsQuery**
  - sellerId: String
  - date: LocalDate

- [ ] **GetUserAgentPoolStatusQuery**
  - (파라미터 없음)

#### Response DTOs

- [ ] **SellerResponse**
  - sellerId, name, status, crawlingIntervalDays, totalProductCount, createdAt, updatedAt

- [ ] **SellerDetailResponse**
  - (SellerResponse와 동일 + 추가 상세 정보)

- [ ] **SellerSummaryResponse**
  - sellerId, name, status, crawlingIntervalDays

- [ ] **CrawlingTriggeredResponse**
  - taskCount: Integer

- [ ] **PublishedTasksResponse**
  - publishedCount: Integer

- [ ] **TaskProcessedResponse**
  - status: CrawlerTaskStatus

- [ ] **OutboxProcessedResponse**
  - successCount, failedCount: Integer

- [ ] **CrawlingMetricsResponse**
  - sellerId, date, successRate, progressRate, taskStats

- [ ] **UserAgentPoolStatusResponse**
  - totalCount, activeCount, suspendedCount, blockedCount: Integer

---

### 6. Assembler (DTO ↔ Domain 변환)

- [ ] **SellerAssembler**
  - `toResponse(Seller)`: Domain → Response DTO
  - `toCommand(RegisterSellerRequest)`: Request DTO → Command DTO

- [ ] **CrawlerTaskAssembler**
  - `toResponse(CrawlerTask)`: Domain → Response DTO

- [ ] **ProductAssembler**
  - `toOutboxPayload(Product)`: Domain → Outbox JSON Payload

---

## ⚠️ 제약사항

### Zero-Tolerance 규칙

- [ ] **Command/Query 분리 (CQRS)**
  - Command Use Case: 상태 변경 (Transaction 필수)
  - Query Use Case: 조회만 (ReadOnly Transaction)

- [ ] **Transaction 경계 엄격 관리**
  - ✅ DB 저장/수정: 트랜잭션 내
  - ❌ 외부 API 호출: 트랜잭션 밖
    - EventBridge Rule 생성/업데이트
    - SQS 발행
    - 머스트잇 API 크롤링
    - 외부 상품 서버 전송
  - ⚠️ 예외: 미니샵 총 상품 수 조회 (빠른 조회이므로 허용)

- [ ] **Assembler 패턴 사용**
  - Domain ↔ DTO 변환은 Assembler에서만
  - Use Case는 Assembler 호출만

- [ ] **Spring 프록시 제약사항 준수**
  - `@Transactional` 메서드는 public
  - private/final 메서드에 `@Transactional` 금지
  - 같은 클래스 내부 호출 시 프록시 미작동 주의

### 테스트 규칙

- [ ] **ArchUnit 테스트 필수**
  - Use Case는 반드시 Port만 의존
  - Transaction 경계 검증 (외부 API 호출 금지)
  - Command/Query 분리 검증

- [ ] **Mock 테스트**
  - Port는 Mock 객체 사용
  - Domain 로직은 실제 객체 사용
  - 외부 의존성 격리

- [ ] **테스트 커버리지 > 80%**
  - Use Case 비즈니스 로직 모두 테스트
  - 성공/실패 시나리오 모두 테스트

---

## ✅ 완료 조건

- [ ] 6개 Command Use Case 구현 완료
- [ ] 4개 Query Use Case 구현 완료
- [ ] UserAgentPoolManager 구현 완료
- [ ] 모든 Port 인터페이스 정의 완료
- [ ] 모든 DTO 정의 완료
- [ ] Assembler 구현 완료
- [ ] Unit Test 작성 완료 (Mock, 커버리지 > 80%)
- [ ] ArchUnit 테스트 통과
- [ ] Transaction 경계 검증 완료
- [ ] Zero-Tolerance 규칙 준수
- [ ] 코드 리뷰 승인
- [ ] PR 머지 완료

---

## 🔗 관련 문서

- **PRD**: docs/prd/mustit-seller-crawler.md
- **Plan**: docs/prd/plans/MUSTIT-002-application-plan.md (create-plan 후 생성)
- **Jira**: (sync-to-jira 후 추가)
- **Application Layer 규칙**: docs/coding_convention/03-application-layer/

---

## 📚 참고사항

### Transaction 경계 설계 패턴

**Pattern 1: 단일 트랜잭션**
```java
@Transactional
public SellerResponse registerSeller(RegisterSellerCommand command) {
    // 1. DB 작업만
    Seller seller = Seller.register(...);
    sellerCommandPort.save(seller);
    // 트랜잭션 커밋

    // 2. 외부 API 호출 (트랜잭션 밖)
    eventBridgePort.createRule(seller.getSellerId(), ...);

    return assembler.toResponse(seller);
}
```

**Pattern 2: 2단계 트랜잭션**
```java
public TaskProcessedResponse processTask(ProcessCrawlerTaskCommand command) {
    // 1. 트랜잭션: 상태 업데이트
    updateTaskStatus(command.getTaskId(), IN_PROGRESS);

    // 2. 외부 API 호출 (트랜잭션 밖)
    CrawlingResult result = mustitApiPort.crawl(...);

    // 3. 트랜잭션: 결과 저장
    saveTaskResult(command.getTaskId(), result);

    return ...;
}
```

### Pessimistic Lock 사용 예시

```java
// UserAgentPoolManager
@Transactional
public UserAgent assignUserAgent() {
    // SELECT FOR UPDATE (Pessimistic Lock)
    UserAgent userAgent = userAgentQueryPort.findFirstActiveForUpdate()
        .orElseThrow(() -> new NoAvailableUserAgentException());

    if (!userAgent.canMakeRequest()) {
        throw new RateLimitExceededException();
    }

    userAgent.incrementRequestCount();
    userAgentCommandPort.save(userAgent);

    return userAgent;
}
```

### Bulk Insert 최적화

```java
// TriggerCrawlingUseCase
List<CrawlerTask> tasks = IntStream.range(0, pageCount)
    .mapToObj(page -> CrawlerTask.create(
        sellerId,
        MINISHOP,
        buildMinishopUrl(sellerId, page)
    ))
    .toList();

// Bulk Insert (한 번에 저장)
crawlerTaskCommandPort.saveAll(tasks);
```
