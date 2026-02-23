# 크롤링 실행 파이프라인 분석

> 분석일: 2026-02-21
> 대상: EventBridge 트리거 → CrawledProduct 가공까지 전체 흐름

---

## 1. 전체 흐름도

```
EventBridge (cron)
  → SQS → EventBridgeTriggerSqsListener [분산락]
    → TriggerCrawlTaskService
      → CrawlTask(WAITING) + CrawlTaskOutbox(PENDING) 저장 [@Transactional]
        → CrawlTaskOutboxScheduler (크론)
          → CrawlTaskOutboxProcessor
            → Outbox(PENDING→PROCESSING) → Task(WAITING→PUBLISHED) → SQS 발행 → Outbox(SENT)
              → CrawlTaskSqsListener
                → CrawlTaskExecutionCoordinator
                  → A. UserAgent 획득 (실패 시 RetryableException → SQS 재시도)
                  → B. Task(PUBLISHED→RUNNING) + CrawlExecution 생성 [@Transactional]
                  → C. Crawler.crawl() (HTTP 호출, 비-트랜잭션)
                  → D. UserAgent 결과 기록 (Redis)
                  → E. 성공: Task(SUCCESS) + CrawledRaw(PENDING) + 후속Task+Outbox 저장
                       실패: Task(FAILED)
                    → (후속 Task가 있으면 반복)
                      → CrawledRawProcessingScheduler (크론)
                        → ProcessPendingCrawledRawService
                          → CrawledRaw(PENDING) 역직렬화
                          → Process*UseCase → CrawledProduct 생성/업데이트
                          → CrawledRaw(PROCESSED/FAILED)
```

---

## 2. 단계별 상세

### 2.1 EventBridge 트리거 (1단계)

| 항목 | 상세 |
|------|------|
| **클래스** | `EventBridgeTriggerSqsListener` |
| **분산락** | `LockType.CRAWL_TRIGGER` (schedulerId 기준) |
| **멱등성** | 분산락이 동일 schedulerId 중복 처리 방지 |
| **에러** | 예외 재전파 → SQS NACK → visibility timeout 후 재시도 |

### 2.2 CrawlTask + Outbox 생성 (2단계)

| 항목 | 상세 |
|------|------|
| **클래스** | `TriggerCrawlTaskService` → `CrawlTaskCommandFacade` |
| **트랜잭션** | `@Transactional` (CrawlTask + Outbox 원자적 저장) |
| **검증** | Scheduler ACTIVE 상태, 중복 Task 존재 여부 |
| **상태** | CrawlTask: `WAITING`, Outbox: `PENDING` |
| **멱등성 키** | `"outbox-{taskId}"` |

### 2.3 Outbox → SQS 발행 (3단계)

| 항목 | 상세 |
|------|------|
| **클래스** | `CrawlTaskOutboxScheduler` → `CrawlTaskOutboxProcessor` |
| **조건** | PENDING + createdAt < (now - delaySeconds) |
| **선점** | `markAsProcessing()` 먼저 저장 (중복 발행 방지) |
| **상태 전이** | Outbox: `PENDING → PROCESSING → SENT/FAILED` |
| **Task 전이** | `WAITING → PUBLISHED` 또는 `RETRY → PUBLISHED` |

### 2.4 크롤링 실행 (4단계)

| 항목 | 상세 |
|------|------|
| **클래스** | `CrawlTaskSqsListener` → `CrawlTaskExecutionService` → `CrawlTaskExecutionCoordinator` |
| **멱등성** | `CrawlTaskExecutionValidator.validateAndGet()` (이미 처리된 Task 스킵) |
| **에러 분류** | `RetryableExecutionException` → SQS 재시도, 그 외 → `failSafely()` |
| **UserAgent** | Redis 토큰 소비 → 실패 시 DB 폴백 → CircuitBreaker (가용률 < 20%) |

**실행 순서**:
```
A. consumeUserAgent()        — Task 상태 변경 전 (실패 시 안전)
B. prepareExecution()        — PUBLISHED → RUNNING [@Transactional]
C. executeCrawling()         — HTTP 호출 (비-트랜잭션)
D. recordUserAgentResult()   — Redis health score 갱신
E. completeExecution()       — SUCCESS/FAILED [@Transactional]
F. processResult()           — CrawledRaw 저장 + 후속 Task 생성 (성공 시만)
```

### 2.5 결과 처리 (5단계)

| Processor | 입력 | 출력 |
|-----------|------|------|
| `SearchCrawlResultProcessor` | 상품 목록 HTML | CrawledRaw(MINI_SHOP) + 후속 Task(MINI_SHOP, DETAIL, OPTION) |
| `MiniShopCrawlResultProcessor` | MiniShop JSON | CrawledRaw(MINI_SHOP) |
| `DetailCrawlResultProcessor` | Detail JSON | CrawledRaw(DETAIL) |
| `OptionCrawlResultProcessor` | Option JSON | CrawledRaw(OPTION) |

모든 CrawledRaw는 **PENDING** 상태로 저장.

### 2.6 CrawledRaw 가공 (6단계)

| 항목 | 상세 |
|------|------|
| **클래스** | `CrawledRawProcessingScheduler` → `ProcessPendingCrawledRawService` |
| **순서** | MINI_SHOP → DETAIL → OPTION (주석으로만 명시) |
| **처리** | `findPendingByType()` → 역직렬화 → `Process*UseCase` → CrawledProduct |
| **상태 전이** | CrawledRaw: `PENDING → PROCESSED/FAILED` |

---

## 3. 복구 메커니즘

| 스케줄러 | 대상 | 복구 |
|---------|------|------|
| `RecoverStuckCrawlTaskService` | RUNNING 타임아웃 | RUNNING → FAILED → RETRY + Outbox PENDING |
| `RecoverTimeoutCrawlTaskOutboxService` | PROCESSING 좀비 | PROCESSING → PENDING |
| `RecoverFailedCrawlTaskOutboxService` | FAILED 아웃박스 | FAILED → PENDING (재발행) |

---

## 4. 상태 전이 맵

### CrawlTask
```
WAITING → PUBLISHED → RUNNING → SUCCESS
                             → FAILED → RETRY → PUBLISHED (재시도)
                                      → FAILED (최대 재시도 초과)
```

### CrawlTaskOutbox
```
PENDING → PROCESSING → SENT    (성공)
                     → FAILED  (실패, retryCount++)
```

### CrawledRaw
```
PENDING → PROCESSED  (성공)
        → FAILED     (실패)
```

---

## 5. 잠재적 문제점

### 🔴 CRITICAL

| # | 문제 | 위치 | 설명 |
|---|------|------|------|
| 1 | **Outbox SQS 발행 실패 시 CrawlTask 상태 불일치** | `CrawlTaskOutboxProcessor` | `markAsPublished()` 후 SQS 실패 → Task=PUBLISHED, 메시지 없음. 복구 로직 없음 |
| 2 | **failDirectly 실패 시 PUBLISHED 고아** | `CrawlTaskSqsListener.failSafely()` | 영구적 오류인데 failDirectly까지 실패 → Task가 PUBLISHED로 영원히 방치 |

### 🟡 MAJOR

| # | 문제 | 위치 | 설명 |
|---|------|------|------|
| 3 | **RUNNING 고아 복구 지연** | `safeCompleteWithFailure()` | persist 실패 → RUNNING 고아. RecoverStuck 스케줄러 타임아웃 대기 필요 |
| 4 | **CrawledRaw 처리 순서 미보장** | `CrawledRawProcessingScheduler` | 3개 크론 독립 실행 → DETAIL이 MINI_SHOP보다 먼저 가능 → CrawledProduct 부재 |
| 5 | **Outbox FAILED 무한 재시도** | `RecoverFailedCrawlTaskOutboxService` | `canRetry()` 체크 없이 FAILED→PENDING 반복 |
| 6 | **CircuitBreaker Open 시 전체 중지** | `UserAgentPoolValidator` | 가용률 <20% → 모든 크롤링 SQS 재시도 폭증 |

### 🟢 MINOR

| # | 문제 | 위치 | 설명 |
|---|------|------|------|
| 7 | **CrawledRaw FAILED 재처리 전략 없음** | `ProcessPendingCrawledRawService` | FAILED Raw 방치 (수동 개입 필요) |
| 8 | **RetryCount 하드코딩** | `CrawlTaskOutbox`, `CrawlTask` | MAX_RETRY_COUNT=3, 환경별 조정 불가 |

---

## 6. 현재 안전장치

| 메커니즘 | 보호 대상 | 평가 |
|---------|----------|------|
| 분산락 (LockType.CRAWL_TRIGGER) | 트리거 중복 방지 | ✅ |
| 멱등성 검증 (CrawlTaskExecutionValidator) | 중복 실행 방지 | ✅ |
| Outbox PROCESSING 선점 | 중복 SQS 발행 방지 | ✅ |
| RetryableException 분류 | 일시적/영구적 오류 분리 | ✅ |
| RecoverStuckTask 스케줄러 | RUNNING 고아 복구 | ⚠️ 지연 있음 |
| RecoverTimeout Outbox 스케줄러 | PROCESSING 좀비 복구 | ⚠️ Task 상태 미복구 |
| RecoverFailed Outbox 스케줄러 | FAILED 아웃박스 재시도 | ⚠️ canRetry 체크 없음 |
| UserAgent DB 폴백 | Redis 장애 대비 | ✅ |

---

## 7. 누락된 안전장치

1. **PUBLISHED 고아 복구 스케줄러** — Outbox SENT인데 Task가 PUBLISHED인 경우 감지/복구
2. **CrawledRaw 처리 순서 보장** — 단일 스케줄러 순차 처리 또는 선행 처리 체크
3. **Outbox FAILED 재시도 횟수 제한** — `canRetry()` 활용 필요
4. **CrawledRaw FAILED 자동 재처리** — FAILED → PENDING 복구 전략
5. **failDirectly 실패 모니터링** — 메트릭/알림 발생 필요
