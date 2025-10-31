# 🎯 Application Layer (UseCase) 개발 태스크

## 📌 개발 순서 및 우선순위
1. **Seller Management UseCases** (Priority: P0) - 셀러 관리
2. **Schedule Management UseCases** (Priority: P0) - 스케줄 관리
3. **Crawl Orchestration UseCases** (Priority: P0) - 크롤링 오케스트레이션
4. **Task Processing UseCases** (Priority: P0) - 태스크 처리
5. **Product Sync UseCases** (Priority: P1) - 상품 동기화
6. **Monitoring UseCases** (Priority: P1) - 모니터링

---

## 📦 TASK-01: Seller Management UseCases

### UC-01-1: RegisterSellerUseCase
```java
package com.ryuqq.crawlinghub.application.mustit.seller;

@UseCase
@RequiredArgsConstructor
public class RegisterSellerUseCase {
    private final LoadSellerPort loadSellerPort;
    private final SaveSellerPort saveSellerPort;
    private final EventPublisherPort eventPublisher;

    @Transactional
    public SellerResponse execute(RegisterSellerCommand command) {
        // 1. 중복 체크
        // 2. 도메인 생성 (Factory)
        // 3. 저장
        // 4. 이벤트 발행
        // 5. 응답 변환
    }
}
```

### UC-01-2: UpdateSellerStatusUseCase
```java
@UseCase
public class UpdateSellerStatusUseCase {
    @Transactional
    public void execute(UpdateSellerStatusCommand command) {
        // 1. 셀러 조회
        // 2. 상태 변경 (도메인 메서드)
        // 3. 저장
        // 4. 연관 스케줄 처리
    }
}
```

### UC-01-3: GetSellerDetailUseCase
```java
@UseCase
public class GetSellerDetailUseCase {
    @Transactional(readOnly = true)
    public SellerDetailResponse execute(GetSellerQuery query) {
        // 1. 셀러 조회
        // 2. 통계 조회
        // 3. 응답 조합
    }
}
```

### Commands & Queries
- `RegisterSellerCommand`: sellerCode, sellerName
- `UpdateSellerStatusCommand`: sellerId, status
- `GetSellerQuery`: sellerId
- `SellerResponse`: 셀러 정보 응답
- `SellerDetailResponse`: 상세 정보 + 통계

---

## 📦 TASK-02: Schedule Management UseCases

### UC-02-1: CreateScheduleUseCase
```java
@UseCase
public class CreateScheduleUseCase {
    @Transactional
    public ScheduleResponse execute(CreateScheduleCommand command) {
        // 1. 셀러 존재 확인
        // 2. 기존 스케줄 비활성화
        // 3. 새 스케줄 생성
        // 4. EventBridge 등록
        // 5. 저장
    }
}
```

### UC-02-2: UpdateScheduleUseCase
```java
@UseCase
public class UpdateScheduleUseCase {
    @Transactional
    public void execute(UpdateScheduleCommand command) {
        // 1. 스케줄 조회
        // 2. Cron 표현식 검증
        // 3. 다음 실행 시간 계산
        // 4. EventBridge 업데이트
        // 5. 저장
    }
}
```

### UC-02-3: TriggerScheduleUseCase (EventBridge 호출용)
```java
@UseCase
public class TriggerScheduleUseCase {
    @Transactional
    public void execute(TriggerScheduleCommand command) {
        // 1. 스케줄 조회
        // 2. 실행 가능 여부 확인
        // 3. CrawlTask 생성
        // 4. Outbox 저장
        // 5. 다음 실행 시간 업데이트
    }
}
```

### Commands
- `CreateScheduleCommand`: sellerId, cronExpression
- `UpdateScheduleCommand`: scheduleId, cronExpression
- `TriggerScheduleCommand`: scheduleId
- `ScheduleResponse`: 스케줄 정보

---

## 📦 TASK-03: Crawl Orchestration UseCases

### UC-03-1: InitiateCrawlingUseCase
```java
@UseCase
public class InitiateCrawlingUseCase {
    @Transactional
    public void execute(InitiateCrawlingCommand command) {
        // 1. 셀러 상태 확인
        // 2. 초기 미니샵 태스크 생성 (page=0, size=1)
        // 3. Outbox 저장
        // 4. SQS 발행
        // 5. 크롤링 시작 이벤트
    }
}
```

### UC-03-2: ProcessMiniShopResultUseCase
```java
@UseCase
public class ProcessMiniShopResultUseCase {
    @Transactional
    public void execute(MiniShopResultCommand command) {
        // 1. 총 상품 수 추출
        // 2. 페이지 계산 (totalCount / 500)
        // 3. 후속 미니샵 태스크 생성
        // 4. 상품별 상세/옵션 태스크 생성
        // 5. 셀러 상품 수 업데이트
    }
}
```

### UC-03-3: ProcessProductDetailUseCase
```java
@UseCase
public class ProcessProductDetailUseCase {
    @Transactional
    public void execute(ProductDetailCommand command) {
        // 1. 상품 조회/생성
        // 2. 상세 데이터 저장
        // 3. 해시 계산
        // 4. 변경 감지
        // 5. 완성도 체크
    }
}
```

### Commands
- `InitiateCrawlingCommand`: sellerId
- `MiniShopResultCommand`: taskId, responseData
- `ProductDetailCommand`: taskId, itemNo, responseData

---

## 📦 TASK-04: Task Processing UseCases

### UC-04-1: ProcessCrawlTaskUseCase (SQS Consumer)
```java
@UseCase
public class ProcessCrawlTaskUseCase {
    // Transaction 외부에서 실행
    public void execute(ProcessTaskCommand command) {
        // 1. 유저 에이전트 선택
        // 2. 토큰 확인
        // 3. API 호출
        // 4. 결과 처리 분기
        // 5. 상태 업데이트
    }
}
```

### UC-04-2: HandleTaskFailureUseCase
```java
@UseCase
public class HandleTaskFailureUseCase {
    @Transactional
    public void execute(TaskFailureCommand command) {
        // 1. 태스크 조회
        // 2. 재시도 가능 여부 확인
        // 3. 재시도 또는 DLQ 이동
        // 4. 에러 로깅
        // 5. 알림 발송
    }
}
```

### UC-04-3: RetryFailedTasksUseCase
```java
@UseCase
public class RetryFailedTasksUseCase {
    @Transactional
    public void execute(RetryTasksCommand command) {
        // 1. 실패 태스크 조회
        // 2. 재시도 조건 검증
        // 3. 상태 초기화
        // 4. 재발행
    }
}
```

### Commands
- `ProcessTaskCommand`: taskId, sqsMessage
- `TaskFailureCommand`: taskId, error, statusCode
- `RetryTasksCommand`: sellerId, taskType

---

## 📦 TASK-05: Product Sync UseCases

### UC-05-1: DetectProductChangeUseCase
```java
@UseCase
public class DetectProductChangeUseCase {
    @Transactional
    public void execute(DetectChangeCommand command) {
        // 1. 이전 해시 조회
        // 2. 현재 해시 계산
        // 3. 변경 감지
        // 4. 변경 이벤트 생성
        // 5. Outbox 저장
    }
}
```

### UC-05-2: SyncProductToInternalUseCase
```java
@UseCase
public class SyncProductToInternalUseCase {
    // Transaction 없음 (외부 API 호출)
    public void execute(SyncProductCommand command) {
        // 1. 변경 데이터 조회
        // 2. 내부 API 형식 변환
        // 3. API 호출
        // 4. 결과 기록
        // 5. 상태 업데이트
    }
}
```

### UC-05-3: BulkSyncProductsUseCase
```java
@UseCase
public class BulkSyncProductsUseCase {
    public void execute(BulkSyncCommand command) {
        // 1. 대기 중인 변경 조회
        // 2. 배치 그룹화
        // 3. 병렬 전송
        // 4. 결과 집계
    }
}
```

### Commands
- `DetectChangeCommand`: productId, newData
- `SyncProductCommand`: changeId, productData
- `BulkSyncCommand`: sellerId, limit

---

## 📦 TASK-06: Monitoring UseCases

### UC-06-1: CalculateCrawlingStatsUseCase
```java
@UseCase
public class CalculateCrawlingStatsUseCase {
    @Transactional(readOnly = true)
    public CrawlingStatsResponse execute(StatsQuery query) {
        // 1. 기간별 태스크 조회
        // 2. 성공률 계산
        // 3. 진행률 계산
        // 4. 셀러별 집계
        // 5. 응답 생성
    }
}
```

### UC-06-2: GetTaskProgressUseCase
```java
@UseCase
public class GetTaskProgressUseCase {
    @Transactional(readOnly = true)
    public ProgressResponse execute(ProgressQuery query) {
        // 1. 전체 태스크 수 조회
        // 2. 상태별 카운트
        // 3. 퍼센티지 계산
        // 4. 예상 완료 시간
    }
}
```

### UC-06-3: GenerateDailyReportUseCase
```java
@UseCase
public class GenerateDailyReportUseCase {
    @Transactional(readOnly = true)
    public DailyReportResponse execute(DailyReportQuery query) {
        // 1. 일별 데이터 집계
        // 2. 실패 원인 분석
        // 3. 성능 지표 계산
        // 4. 리포트 생성
    }
}
```

### Queries & Responses
- `StatsQuery`: sellerId, dateRange
- `ProgressQuery`: sellerId, taskType
- `DailyReportQuery`: date
- `CrawlingStatsResponse`: 통계 정보
- `ProgressResponse`: 진행 상황
- `DailyReportResponse`: 일일 리포트

---

## 🎯 Port Interfaces 정의

### In Ports (UseCase Interfaces)
```java
package com.ryuqq.crawlinghub.application.port.in;

public interface ManageSellerUseCase {
    SellerResponse registerSeller(RegisterSellerCommand command);
    void updateSellerStatus(UpdateSellerStatusCommand command);
}

public interface ManageScheduleUseCase {
    ScheduleResponse createSchedule(CreateScheduleCommand command);
    void updateSchedule(UpdateScheduleCommand command);
}
```

### Out Ports (Infrastructure Interfaces)
```java
package com.ryuqq.crawlinghub.application.port.out;

public interface LoadSellerPort {
    Optional<MustitSeller> findById(MustitSellerId id);
    Optional<MustitSeller> findByCode(String code);
}

public interface SaveSellerPort {
    MustitSeller save(MustitSeller seller);
}

public interface EventPublisherPort {
    void publish(DomainEvent event);
}

public interface MessageQueuePort {
    void sendToQueue(String queueName, Object message);
}

public interface ExternalApiPort {
    void syncProduct(ProductSyncRequest request);
}
```

---

## 🎯 Transaction 관리 원칙

### @Transactional 사용 규칙
1. **외부 API 호출 금지**: Transaction 내에서 RestTemplate, WebClient 사용 불가
2. **읽기 전용**: 조회 UseCase는 `@Transactional(readOnly = true)`
3. **짧은 트랜잭션**: 가능한 짧게 유지
4. **분리 실행**: 외부 호출은 별도 UseCase로 분리

### 트랜잭션 분리 예시
```java
// ❌ 잘못된 예
@Transactional
public void processAndSync() {
    // DB 작업
    saveToDatabase();
    // 외부 API 호출 (트랜잭션 내)
    callExternalApi(); // 금지!
}

// ✅ 올바른 예
@Transactional
public void processData() {
    // DB 작업만
    saveToDatabase();
    publishEvent(); // 이벤트 발행
}

// 별도 UseCase (트랜잭션 없음)
public void syncExternal() {
    callExternalApi(); // OK
}
```

---

## 🎯 개발 체크리스트

### UseCase별 구현 사항
- [ ] Command/Query 객체 정의
- [ ] UseCase 구현
- [ ] Port Interface 정의
- [ ] Transaction 경계 설정
- [ ] 예외 처리
- [ ] 이벤트 발행 로직
- [ ] 응답 DTO 변환

### 코딩 컨벤션 준수
- [ ] @UseCase 어노테이션
- [ ] Single Responsibility
- [ ] Command/Query 분리
- [ ] Transaction 경계 준수
- [ ] 외부 호출 분리
- [ ] Javadoc 작성

### 테스트 요구사항
- [ ] 정상 플로우 테스트
- [ ] 예외 케이스 테스트
- [ ] Transaction 롤백 테스트
- [ ] Mock Port 테스트

---

## 📊 예상 개발 일정

| Task Category | 예상 시간 | 우선순위 | 병렬 가능 |
|--------------|----------|----------|----------|
| Seller Management | 6h | P0 | ✅ |
| Schedule Management | 8h | P0 | ✅ |
| Crawl Orchestration | 10h | P0 | ❌ (순차) |
| Task Processing | 8h | P0 | ✅ |
| Product Sync | 6h | P1 | ✅ |
| Monitoring | 4h | P1 | ✅ |

**총 예상 시간**: 42시간 (약 5.5일)

---

## 🔗 UseCase 의존 관계

```
Seller Management (독립)
    ↓
Schedule Management (Seller 필요)
    ↓
Crawl Orchestration (Schedule 트리거)
    ↓
Task Processing (Task 처리)
    ↓
Product Sync (처리 결과 동기화)
    ↓
Monitoring (전체 데이터 집계)
```

병렬 개발 가능 그룹:
- **Group 1**: Seller, Schedule, Monitoring
- **Group 2**: Crawl Orchestration, Task Processing
- **Group 3**: Product Sync