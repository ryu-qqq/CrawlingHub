# Application Layer 단위 테스트 대상 목록

> 작성일: 2024-11-27
> 목표 커버리지: 70% 이상

---

## 개요

Application Layer는 UseCase를 조합하고 트랜잭션 경계를 관리하는 레이어입니다.
테스트는 **Service (UseCase 구현) → Assembler → Manager/Facade** 순서로 작성합니다.

### 현재 테스트 현황

| 패키지 | 소스 파일 | 테스트 파일 | 커버리지 |
|--------|----------|------------|---------|
| **전체** | 166개 | 5개 (ArchUnit만) | ❌ 0% |

---

## 1. Service 테스트 (P0 - 최우선)

### 1.1 Seller 서비스

**패키지**: `application/seller/service/`

| 서비스 | 파일 | 의존성 | 테스트 포인트 | 상태 |
|--------|------|--------|--------------|------|
| RegisterSellerService | `command/RegisterSellerService.java` | SellerQueryPort, SellerPersistencePort | 중복 검증, 신규 생성, 저장 | ⬜ |
| UpdateSellerService | `command/UpdateSellerService.java` | SellerQueryPort, SellerPersistencePort | 조회, 수정, 이벤트 발행 | ⬜ |
| UpdateSellerProductCountService | `command/UpdateSellerProductCountService.java` | SellerQueryPort, SellerPersistencePort | 상품 수 업데이트 | ⬜ |
| GetSellerService | `query/GetSellerService.java` | SellerQueryPort, SellerAssembler | 조회, DTO 변환 | ⬜ |
| SearchSellersService | `query/SearchSellersService.java` | SellerQueryPort, SellerAssembler | 검색, 페이징 | ⬜ |

---

### 1.2 Schedule 서비스

**패키지**: `application/schedule/service/`

| 서비스 | 파일 | 의존성 | 테스트 포인트 | 상태 |
|--------|------|--------|--------------|------|
| RegisterCrawlSchedulerService | `command/RegisterCrawlSchedulerService.java` | PersistCrawlSchedulePort, CrawlScheduleQueryPort | 중복 검증, 등록, 이벤트 | ⬜ |
| UpdateCrawlSchedulerService | `command/UpdateCrawlSchedulerService.java` | PersistCrawlSchedulePort, CrawlScheduleQueryPort | 조회, 수정, 이벤트 | ⬜ |
| DeactivateSchedulersBySellerService | `command/DeactivateSchedulersBySellerService.java` | CrawlScheduleQueryPort, PersistCrawlSchedulePort | 셀러별 비활성화 | ⬜ |
| SearchCrawlSchedulersService | `query/SearchCrawlSchedulersService.java` | CrawlScheduleQueryPort, CrawlSchedulerAssembler | 검색, 페이징 | ⬜ |

---

### 1.3 Task 서비스

**패키지**: `application/task/service/`

| 서비스 | 파일 | 의존성 | 테스트 포인트 | 상태 |
|--------|------|--------|--------------|------|
| TriggerCrawlTaskService | `command/TriggerCrawlTaskService.java` | CrawlTaskPersistencePort, CrawlTaskQueryPort | 태스크 트리거 | ⬜ |
| CreateCrawlTaskService | `command/CreateCrawlTaskService.java` | CrawlTaskPersistencePort | 태스크 생성, Outbox | ⬜ |
| GetCrawlTaskService | `query/GetCrawlTaskService.java` | CrawlTaskQueryPort, CrawlTaskAssembler | 조회, DTO 변환 | ⬜ |
| ListCrawlTasksService | `query/ListCrawlTasksService.java` | CrawlTaskQueryPort, CrawlTaskAssembler | 목록 조회, 페이징 | ⬜ |

---

### 1.4 Execution 서비스

**패키지**: `application/execution/service/`

| 서비스 | 파일 | 의존성 | 테스트 포인트 | 상태 |
|--------|------|--------|--------------|------|
| CrawlTaskExecutionService | `CrawlTaskExecutionService.java` | CrawlExecutionPersistencePort, ... | 실행 로직 전체 | ⬜ |
| GetCrawlExecutionService | `query/GetCrawlExecutionService.java` | CrawlExecutionQueryPort, CrawlExecutionAssembler | 조회, DTO 변환 | ⬜ |
| ListCrawlExecutionsService | `query/ListCrawlExecutionsService.java` | CrawlExecutionQueryPort, CrawlExecutionAssembler | 목록 조회, 페이징 | ⬜ |

---

### 1.5 UserAgent 서비스

**패키지**: `application/useragent/service/`

| 서비스 | 파일 | 의존성 | 테스트 포인트 | 상태 |
|--------|------|--------|--------------|------|
| ConsumeUserAgentService | `command/ConsumeUserAgentService.java` | UserAgentPoolCachePort | Pool에서 소비 | ⬜ |
| RecordUserAgentResultService | `command/RecordUserAgentResultService.java` | UserAgentPoolCachePort, UserAgentPersistencePort | 결과 기록, HealthScore 업데이트 | ⬜ |
| RecoverUserAgentService | `command/RecoverUserAgentService.java` | UserAgentQueryPort, UserAgentPersistencePort | 복구 로직 | ⬜ |
| GetUserAgentPoolStatusService | `query/GetUserAgentPoolStatusService.java` | UserAgentPoolCachePort | Pool 상태 조회 | ⬜ |

---

## 2. Assembler 테스트 (P1 - 중요)

Assembler는 **도메인 객체 ↔ DTO 변환**을 담당합니다.

| Assembler | 파일 | 테스트 포인트 | 상태 |
|-----------|------|--------------|------|
| SellerAssembler | `seller/assembler/SellerAssembler.java` | Command→Domain, Domain→Response, Criteria 생성 | ⬜ |
| CrawlSchedulerAssembler | `schedule/assembler/CrawlSchedulerAssembler.java` | Command→Domain, Domain→Response | ⬜ |
| CrawlTaskAssembler | `task/assembler/CrawlTaskAssembler.java` | Query→Criteria, Domain→Response | ⬜ |
| CrawlExecutionAssembler | `execution/assembler/CrawlExecutionAssembler.java` | Query→Criteria, Domain→Response | ⬜ |
| CrawledRawAssembler | `product/assembler/CrawledRawAssembler.java` | Raw 데이터 변환 | ⬜ |

### 2.1 Assembler 테스트 예시

```java
@Test
@DisplayName("[성공] Seller → SellerResponse 변환")
void toResponse_success() {
    // Given
    Seller seller = SellerFixture.createActive();

    // When
    SellerResponse response = assembler.toResponse(seller);

    // Then
    assertThat(response.id()).isEqualTo(seller.getSellerIdValue());
    assertThat(response.name()).isEqualTo(seller.getSellerNameValue());
    assertThat(response.status()).isEqualTo(seller.getStatus().name());
}
```

---

## 3. Manager 테스트 (P1 - 중요)

Manager는 **트랜잭션 조합 및 비즈니스 흐름 제어**를 담당합니다.

### 3.1 Seller Manager

| Manager | 파일 | 테스트 포인트 | 상태 |
|---------|------|--------------|------|
| SellerTransactionManager | `seller/manager/SellerTransactionManager.java` | 트랜잭션 내 저장, 이벤트 발행 | ⬜ |

### 3.2 Schedule Manager

| Manager | 파일 | 테스트 포인트 | 상태 |
|---------|------|--------------|------|
| CrawlerSchedulerManager | `schedule/manager/CrawlerSchedulerManager.java` | 스케줄러 생성/수정 흐름 | ⬜ |
| CrawlerSchedulerHistoryManager | `schedule/manager/CrawlerSchedulerHistoryManager.java` | 히스토리 기록 | ⬜ |
| CrawlerSchedulerOutBoxManager | `schedule/manager/CrawlerSchedulerOutBoxManager.java` | Outbox 관리 | ⬜ |

### 3.3 Task Manager

| Manager | 파일 | 테스트 포인트 | 상태 |
|---------|------|--------------|------|
| CrawlTaskTransactionManager | `task/manager/CrawlTaskTransactionManager.java` | 태스크 트랜잭션 | ⬜ |
| CrawlTaskOutboxTransactionManager | `task/manager/CrawlTaskOutboxTransactionManager.java` | Outbox 트랜잭션 | ⬜ |
| CrawlTaskMessageManager | `task/manager/CrawlTaskMessageManager.java` | 메시지 발행 | ⬜ |

### 3.4 UserAgent Manager

| Manager | 파일 | 테스트 포인트 | 상태 |
|---------|------|--------------|------|
| UserAgentPoolManager | `useragent/manager/UserAgentPoolManager.java` | Pool 초기화, 관리 | ⬜ |

### 3.5 Execution Manager

| Manager | 파일 | 테스트 포인트 | 상태 |
|---------|------|--------------|------|
| CrawlExecutionManager | `execution/manager/CrawlExecutionManager.java` | 실행 시작/완료 관리 | ⬜ |

### 3.6 Product Manager

| Manager | 파일 | 테스트 포인트 | 상태 |
|---------|------|--------------|------|
| CrawledProductManager | `product/manager/CrawledProductManager.java` | 상품 관리 | ⬜ |
| CrawledRawManager | `product/manager/CrawledRawManager.java` | Raw 데이터 관리 | ⬜ |
| ImageOutboxManager | `product/manager/ImageOutboxManager.java` | 이미지 Outbox | ⬜ |
| SyncOutboxManager | `product/manager/SyncOutboxManager.java` | 동기화 Outbox | ⬜ |

---

## 4. Facade 테스트 (P2 - 권장)

Facade는 **여러 UseCase를 조합**하는 역할을 담당합니다.

| Facade | 파일 | 조합하는 UseCase | 상태 |
|--------|------|-----------------|------|
| SellerCommandFacade | `seller/facade/SellerCommandFacade.java` | Register, Update + 중복 검증 | ⬜ |
| CrawlerSchedulerFacade | `schedule/facade/CrawlerSchedulerFacade.java` | Register, Update + EventBridge | ⬜ |
| CrawlTaskFacade | `task/facade/CrawlTaskFacade.java` | Create, Trigger + Outbox | ⬜ |
| CrawlTaskExecutionFacade | `execution/facade/CrawlTaskExecutionFacade.java` | Execute + 결과 처리 | ⬜ |
| CrawledProductFacade | `product/facade/CrawledProductFacade.java` | 상품 처리 흐름 | ⬜ |

---

## 5. Listener 테스트 (P2 - 권장)

Event Listener는 **도메인 이벤트 처리**를 담당합니다.

| Listener | 파일 | 처리 이벤트 | 상태 |
|----------|------|------------|------|
| SellerDeactivatedEventHandler | `seller/listener/SellerDeactivatedEventHandler.java` | SellerDeActiveEvent | ⬜ |
| SchedulerRegisteredEventListener | `schedule/listener/SchedulerRegisteredEventListener.java` | SchedulerRegisteredEvent | ⬜ |
| CrawlTaskRegisteredEventListener | `task/listener/CrawlTaskRegisteredEventListener.java` | CrawlTaskRegisteredEvent | ⬜ |
| SessionRequiredEventListener | `useragent/listener/SessionRequiredEventListener.java` | SessionRequiredEvent | ⬜ |
| ImageUploadEventListener | `product/listener/ImageUploadEventListener.java` | ImageUploadRequestedEvent | ⬜ |
| ExternalSyncEventListener | `product/listener/ExternalSyncEventListener.java` | ExternalSyncRequestedEvent | ⬜ |

---

## 6. Scheduler 테스트 (P2 - 권장)

스케줄러는 **주기적 작업**을 담당합니다.

| Scheduler | 파일 | 실행 주기 | 테스트 포인트 | 상태 |
|-----------|------|----------|--------------|------|
| CrawlSchedulerOutBoxRetryScheduler | `schedule/scheduler/` | 주기적 | Outbox 재시도 로직 | ⬜ |
| CrawlTaskOutboxRetryScheduler | `task/scheduler/` | 주기적 | Outbox 재시도 로직 | ⬜ |
| UserAgentRecoveryScheduler | `useragent/scheduler/` | 주기적 | UA 복구 로직 | ⬜ |
| SessionIssuanceScheduler | `useragent/scheduler/` | 주기적 | 세션 발급 로직 | ⬜ |

---

## 7. Component 테스트 (P1 - 중요)

### 7.1 Crawl 컴포넌트

| Component | 파일 | 테스트 포인트 | 상태 |
|-----------|------|--------------|------|
| CrawlerProvider | `crawl/component/CrawlerProvider.java` | TaskType별 Crawler 반환 | ⬜ |
| MetaCrawler | `crawl/component/MetaCrawler.java` | META 크롤링 로직 | ⬜ |
| MiniShopCrawler | `crawl/component/MiniShopCrawler.java` | MiniShop 크롤링 로직 | ⬜ |
| DetailCrawler | `crawl/component/DetailCrawler.java` | Detail 크롤링 로직 | ⬜ |
| OptionCrawler | `crawl/component/OptionCrawler.java` | Option 크롤링 로직 | ⬜ |

### 7.2 Parser 컴포넌트

| Parser | 파일 | 테스트 포인트 | 상태 |
|--------|------|--------------|------|
| MetaResponseParser | `crawl/parser/MetaResponseParser.java` | META 응답 파싱 | ⬜ |
| MiniShopResponseParser | `crawl/parser/MiniShopResponseParser.java` | MiniShop 응답 파싱 | ⬜ |
| DetailResponseParser | `crawl/parser/DetailResponseParser.java` | Detail 응답 파싱 | ⬜ |
| OptionResponseParser | `crawl/parser/OptionResponseParser.java` | Option 응답 파싱 | ⬜ |

### 7.3 Processor 컴포넌트

| Processor | 파일 | 테스트 포인트 | 상태 |
|-----------|------|--------------|------|
| CrawlResultProcessorProvider | `crawl/processor/CrawlResultProcessorProvider.java` | TaskType별 Processor 반환 | ⬜ |
| MetaCrawlResultProcessor | `crawl/processor/MetaCrawlResultProcessor.java` | META 결과 처리 | ⬜ |
| MiniShopCrawlResultProcessor | `crawl/processor/MiniShopCrawlResultProcessor.java` | MiniShop 결과 처리 | ⬜ |
| DetailCrawlResultProcessor | `crawl/processor/DetailCrawlResultProcessor.java` | Detail 결과 처리 | ⬜ |
| OptionCrawlResultProcessor | `crawl/processor/OptionCrawlResultProcessor.java` | Option 결과 처리 | ⬜ |

### 7.4 기타 컴포넌트

| Component | 파일 | 테스트 포인트 | 상태 |
|-----------|------|--------------|------|
| CrawlTaskPersistenceValidator | `task/component/CrawlTaskPersistenceValidator.java` | 저장 전 검증 | ⬜ |
| DistributedLockExecutor | `common/component/lock/DistributedLockExecutor.java` | 분산 락 실행 | ⬜ |
| StringTruncator | `common/utils/StringTruncator.java` | 문자열 자르기 | ⬜ |

---

## 8. DTO 테스트 (P3 - 선택)

DTO는 주로 **record**로 구현되어 테스트 필요성이 낮지만, 검증 로직이 있는 경우 테스트 필요.

### 8.1 검증 로직 포함 DTO

| DTO | 파일 | 검증 포인트 | 상태 |
|-----|------|------------|------|
| RegisterSellerCommand | `seller/dto/command/` | 필수 필드 검증 | ⬜ |
| RegisterCrawlSchedulerCommand | `schedule/dto/command/` | 필수 필드 검증 | ⬜ |
| CreateCrawlTaskCommand | `task/dto/command/` | 필수 필드 검증 | ⬜ |

---

## 9. 테스트 작성 가이드

### 9.1 Service 테스트 패턴 (Mockist 스타일)

```java
@ExtendWith(MockitoExtension.class)
class GetSellerServiceTest {

    @Mock
    private SellerQueryPort sellerQueryPort;

    @Mock
    private SellerAssembler assembler;

    @InjectMocks
    private GetSellerService service;

    @Test
    @DisplayName("[성공] 셀러 조회")
    void execute_success() {
        // Given
        Long sellerId = 1L;
        Seller seller = SellerFixture.createActive();
        SellerResponse expected = new SellerResponse(...);

        given(sellerQueryPort.findById(sellerId))
            .willReturn(Optional.of(seller));
        given(assembler.toResponse(seller))
            .willReturn(expected);

        // When
        SellerResponse result = service.execute(new GetSellerQuery(sellerId));

        // Then
        assertThat(result).isEqualTo(expected);
        then(sellerQueryPort).should().findById(sellerId);
    }

    @Test
    @DisplayName("[실패] 셀러 미존재 시 예외")
    void execute_notFound_throwsException() {
        // Given
        Long sellerId = 999L;
        given(sellerQueryPort.findById(sellerId))
            .willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> service.execute(new GetSellerQuery(sellerId)))
            .isInstanceOf(SellerNotFoundException.class);
    }
}
```

### 9.2 Assembler 테스트 패턴

```java
class CrawlTaskAssemblerTest {

    private CrawlTaskAssembler assembler = new CrawlTaskAssembler();

    @Test
    @DisplayName("[성공] ListCrawlTasksQuery → CrawlTaskCriteria 변환")
    void toCriteria_success() {
        // Given
        ListCrawlTasksQuery query = new ListCrawlTasksQuery(
            1L, // sellerId
            CrawlTaskStatus.WAITING,
            0, 10
        );

        // When
        CrawlTaskCriteria criteria = assembler.toCriteria(query);

        // Then
        assertThat(criteria.sellerId()).isEqualTo(1L);
        assertThat(criteria.status()).isEqualTo(CrawlTaskStatus.WAITING);
    }
}
```

### 9.3 Manager 테스트 패턴

```java
@ExtendWith(MockitoExtension.class)
class CrawlTaskTransactionManagerTest {

    @Mock
    private CrawlTaskPersistencePort persistencePort;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private CrawlTaskTransactionManager manager;

    @Test
    @DisplayName("[성공] 태스크 저장 및 이벤트 발행")
    void saveAndPublish_success() {
        // Given
        CrawlTask task = CrawlTaskFixture.createWaiting();
        given(persistencePort.save(task)).willReturn(task);

        // When
        manager.saveAndPublishEvent(task);

        // Then
        then(persistencePort).should().save(task);
        then(eventPublisher).should().publishEvent(any(CrawlTaskRegisteredEvent.class));
    }
}
```

---

## 10. 진행 현황 추적

### 10.1 완료 기준

- [ ] 핵심 Service 테스트 완료 (15개)
- [ ] Assembler 테스트 완료 (5개)
- [ ] Manager 테스트 완료 (12개)
- [ ] Facade 테스트 완료 (5개)
- [ ] 전체 커버리지 70% 이상

### 10.2 예상 테스트 파일 수

| 카테고리 | 예상 파일 수 |
|----------|-------------|
| Service | 15개 |
| Assembler | 5개 |
| Manager | 12개 |
| Facade | 5개 |
| Listener | 6개 |
| Component | 15개 |
| **합계** | **58개** |

---

## 변경 이력

| 날짜 | 변경 내용 |
|------|----------|
| 2024-11-27 | 초안 작성 |
