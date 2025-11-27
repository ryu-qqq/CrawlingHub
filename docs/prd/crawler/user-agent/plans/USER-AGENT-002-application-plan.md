# USER-AGENT-002: Application Layer 구현 계획

## 📝 개요

Redis 기반 UserAgent Pool 관리 시스템 구현

---

## 🎯 핵심 요구사항

### 1. Redis UserAgent Pool
- **목적**: DB가 아닌 Redis에서 UserAgent Pool 관리
- **토큰 제한**: 각 UserAgent당 1시간에 80회 요청
- **Sliding Window**: 최초 사용 시점 기준 1시간 윈도우
- **원자성**: Lua Script로 동시성 제어

### 2. 토큰 소비 Flow
```
consume() 호출
    ↓
Redis에서 tokens > 0인 UserAgent 선택 (Lua Script)
    ↓
tokens-- (atomic decrement)
    ↓
최초 사용이면 windowEnd = now + 1h 설정
    ↓
Token 반환 → 크롤링 실행
```

### 3. 결과 기록
- **성공**: Health Score +5 (최대 100)
- **429 응답**: 즉시 SUSPENDED (Pool에서 제외)
- **기타 에러**: 단순 로깅 (무시)

### 4. 복구 조건
- SUSPENDED 상태
- 1시간 경과
- Health Score ≥ 30

---

## 📁 구현할 파일 구조

```
application/src/main/java/.../useragent/
├── port/
│   ├── in/
│   │   ├── command/
│   │   │   ├── ConsumeUserAgentUseCase.java          # 토큰 소비
│   │   │   ├── RecordUserAgentResultUseCase.java     # 결과 기록
│   │   │   └── RecoverUserAgentUseCase.java          # 수동 복구
│   │   └── query/
│   │       └── GetUserAgentPoolStatusUseCase.java    # Pool 상태 조회
│   └── out/
│       ├── cache/
│       │   └── UserAgentPoolCachePort.java           # Redis (Lua Script)
│       ├── command/
│       │   └── UserAgentPersistencePort.java         # DB 저장
│       └── query/
│           └── UserAgentQueryPort.java               # DB 조회
│
├── manager/
│   └── UserAgentPoolManager.java                     # 핵심 비즈니스 로직
│
├── dto/
│   ├── cache/
│   │   └── CachedUserAgent.java                      # Redis DTO
│   ├── command/
│   │   └── RecordUserAgentResultCommand.java
│   └── response/
│       └── UserAgentPoolStatusResponse.java
│
├── scheduler/
│   └── UserAgentRecoveryScheduler.java               # 자동 복구
│
└── service/
    ├── command/
    │   ├── ConsumeUserAgentService.java
    │   ├── RecordUserAgentResultService.java
    │   └── RecoverUserAgentService.java
    └── query/
        └── GetUserAgentPoolStatusService.java
```

---

## 🔑 핵심 Port 인터페이스

### UserAgentPoolCachePort (Redis)

```java
public interface UserAgentPoolCachePort {

    /**
     * 토큰 소비 (Lua Script - atomic)
     * - tokens > 0인 UserAgent 선택
     * - tokens--
     * - 최초 사용 시 windowEnd 설정
     *
     * @return 선택된 UserAgent (없으면 empty)
     */
    Optional<CachedUserAgent> consumeToken();

    /**
     * 성공 기록 (Lua Script - atomic)
     * - Health Score +5 (최대 100)
     */
    void recordSuccess(UserAgentId id);

    /**
     * 실패 기록 (Lua Script - atomic)
     * - 429: 즉시 SUSPENDED (Pool에서 제외)
     * - 기타: 단순 로깅
     */
    void recordFailure(UserAgentId id, int httpStatusCode);

    /**
     * Pool에 UserAgent 추가
     */
    void addToPool(CachedUserAgent userAgent);

    /**
     * Pool에서 UserAgent 제거 (SUSPENDED 시)
     */
    void removeFromPool(UserAgentId id);

    /**
     * Pool에 UserAgent 복구
     * - Health Score 70, tokens 80 리셋
     */
    void restoreToPool(UserAgentId id);

    /**
     * 복구 대상 조회
     * - SUSPENDED + 1시간 경과 + Health Score ≥ 30
     */
    List<UserAgentId> getRecoverableUserAgents();

    /**
     * Pool 통계 조회
     */
    PoolStats getPoolStats();
}
```

### UserAgentQueryPort (DB)

```java
public interface UserAgentQueryPort {

    /**
     * 활성화된 UserAgent 전체 조회 (Pool 초기화용)
     */
    List<UserAgent> findAllAvailable();

    /**
     * ID로 조회
     */
    Optional<UserAgent> findById(UserAgentId id);

    /**
     * 상태별 개수 조회
     */
    long countByStatus(UserAgentStatus status);

    /**
     * 전체 개수 조회
     */
    long countAll();
}
```

### UserAgentPersistencePort (DB)

```java
public interface UserAgentPersistencePort {

    /**
     * UserAgent 저장
     */
    UserAgentId save(UserAgent userAgent);

    /**
     * 상태 변경 (SUSPENDED 등)
     */
    void updateStatus(UserAgentId id, UserAgentStatus status);
}
```

---

## 📦 DTO 정의

### CachedUserAgent (Redis 저장용)

```java
public record CachedUserAgent(
    Long userAgentId,
    String encryptedToken,
    int remainingTokens,       // 남은 토큰 수 (초기 80)
    int maxTokens,             // 최대 토큰 (80)
    LocalDateTime windowStart, // 윈도우 시작 시점
    LocalDateTime windowEnd,   // 윈도우 종료 시점 (시작 + 1h)
    int healthScore,           // 0-100
    UserAgentStatus status     // AVAILABLE, SUSPENDED
) {
    public static CachedUserAgent forNew(UserAgent userAgent) {
        return new CachedUserAgent(
            userAgent.getId().value(),
            userAgent.getToken().encryptedValue(),
            80,  // 초기 토큰
            80,
            null, // 최초 사용 시 설정
            null,
            userAgent.getHealthScoreValue(),
            UserAgentStatus.AVAILABLE
        );
    }

    public boolean hasTokens() {
        return remainingTokens > 0;
    }

    public boolean isWindowExpired() {
        return windowEnd != null && LocalDateTime.now().isAfter(windowEnd);
    }
}
```

### RecordUserAgentResultCommand

```java
public record RecordUserAgentResultCommand(
    Long userAgentId,
    int httpStatusCode,
    boolean success
) {
    public static RecordUserAgentResultCommand success(Long userAgentId) {
        return new RecordUserAgentResultCommand(userAgentId, 200, true);
    }

    public static RecordUserAgentResultCommand failure(Long userAgentId, int httpStatusCode) {
        return new RecordUserAgentResultCommand(userAgentId, httpStatusCode, false);
    }

    public boolean isRateLimited() {
        return httpStatusCode == 429;
    }
}
```

### UserAgentPoolStatusResponse

```java
public record UserAgentPoolStatusResponse(
    long totalAgents,
    long availableAgents,
    long suspendedAgents,
    double availableRate,
    HealthScoreStats healthScoreStats
) {
    public record HealthScoreStats(
        double avg,
        int min,
        int max
    ) {}

    public boolean isCircuitBreakerOpen() {
        return availableRate < 20.0;
    }
}
```

---

## 🔧 Manager 핵심 로직

### UserAgentPoolManager

```java
@Component
public class UserAgentPoolManager {

    private final UserAgentPoolCachePort cachePort;
    private final UserAgentQueryPort queryPort;
    private final UserAgentPersistencePort persistencePort;

    private static final double CIRCUIT_BREAKER_THRESHOLD = 20.0;

    /**
     * 토큰 소비 (핵심!)
     */
    public CachedUserAgent consume() {
        // 1. Circuit Breaker 체크
        checkCircuitBreaker();

        // 2. Redis에서 토큰 소비 (Lua Script)
        return cachePort.consumeToken()
            .orElseThrow(() -> new NoAvailableUserAgentException());
    }

    /**
     * 결과 기록
     */
    public void recordResult(RecordUserAgentResultCommand command) {
        UserAgentId id = UserAgentId.of(command.userAgentId());

        if (command.success()) {
            cachePort.recordSuccess(id);
        } else if (command.isRateLimited()) {
            // 429: 즉시 SUSPENDED
            cachePort.removeFromPool(id);
            persistencePort.updateStatus(id, UserAgentStatus.SUSPENDED);
        }
        // 기타 에러는 무시 (로깅만)
    }

    /**
     * Circuit Breaker 체크
     */
    private void checkCircuitBreaker() {
        PoolStats stats = cachePort.getPoolStats();

        if (stats.total() == 0) {
            throw new CircuitBreakerOpenException(0);
        }

        double availableRate = (double) stats.available() / stats.total() * 100;
        if (availableRate < CIRCUIT_BREAKER_THRESHOLD) {
            throw new CircuitBreakerOpenException(availableRate);
        }
    }

    /**
     * SUSPENDED UserAgent 복구
     */
    public void recoverSuspendedUserAgents() {
        List<UserAgentId> recoverableIds = cachePort.getRecoverableUserAgents();

        for (UserAgentId id : recoverableIds) {
            cachePort.restoreToPool(id);
            persistencePort.updateStatus(id, UserAgentStatus.AVAILABLE);
        }
    }
}
```

---

## ⏰ Scheduler

### UserAgentRecoveryScheduler

```java
@Component
public class UserAgentRecoveryScheduler {

    private final UserAgentPoolManager poolManager;

    /**
     * 매 시간 정각에 SUSPENDED UserAgent 복구
     * - 조건: 1시간 경과 + Health Score ≥ 30
     */
    @Scheduled(cron = "0 0 * * * *")
    public void recoverSuspendedUserAgents() {
        poolManager.recoverSuspendedUserAgents();
    }
}
```

---

## 🔄 상태 전환 다이어그램

```
┌─────────────────────────────────────────────────────────────┐
│                    Redis Pool (AVAILABLE)                   │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ UserAgent A: tokens=75, health=100, window=14:00~15:00│   │
│  │ UserAgent B: tokens=40, health=85,  window=13:30~14:30│   │
│  │ UserAgent C: tokens=80, health=70,  window=null       │   │
│  └─────────────────────────────────────────────────────┘   │
└──────────────────────────┬──────────────────────────────────┘
                           │
         consume()         │         recordSuccess()
             ↓             │              ↑
    ┌────────────────────────────────────────────────────┐
    │              크롤링 실행 중                         │
    └────────────────────────┬───────────────────────────┘
                             │
           recordFailure(statusCode)
                             │
              ┌──────────────┴──────────────┐
              │                             │
         429 응답                       기타 에러
              │                             │
              ↓                             ↓
    ┌─────────────────┐              (무시/로깅)
    │ 즉시 SUSPENDED  │
    │ Pool에서 제거   │
    │ DB 상태 변경    │
    └────────┬────────┘
             │
             ↓
┌─────────────────────────────────────────────────────────────┐
│                 SUSPENDED (Redis 별도 저장)                 │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ UserAgent D: health=60, suspendedAt=13:00           │   │
│  │ UserAgent E: health=25, suspendedAt=12:30 (복구X)   │   │
│  └─────────────────────────────────────────────────────┘   │
└──────────────────────────┬──────────────────────────────────┘
                           │
         1시간 경과 + Health ≥ 30
                           │
                           ↓
                    restoreToPool()
              (Health 70, tokens 80 리셋)
                           │
                           ↓
                   Redis Pool로 복귀
```

---

## 📊 Redis 키 구조 (참고)

```
# Pool (Set)
user_agent:pool:available    → {1, 2, 3, 5}  (AVAILABLE UserAgent IDs)

# 개별 UserAgent (Hash)
user_agent:{id}:data → {
    token: "encrypted...",
    remainingTokens: 75,
    maxTokens: 80,
    windowStart: "2024-01-01T14:00:00",
    windowEnd: "2024-01-01T15:00:00",
    healthScore: 100,
    status: "AVAILABLE"
}

# SUSPENDED (Sorted Set - suspendedAt 기준)
user_agent:pool:suspended    → {(id=4, score=timestamp), ...}
```

---

## 🚀 개발 순서

1. **DTO** (cache, command, response)
2. **Port 인터페이스** (out → in)
3. **Manager** (핵심 로직)
4. **Service** (UseCase 구현)
5. **Scheduler** (자동 복구)

---

## ⚠️ 주의사항

1. **Lua Script 원자성**: consume, recordSuccess, recordFailure 모두 Lua Script로 구현
2. **DB 동기화**: 일단 보류 (추후 결정)
3. **Circuit Breaker**: 가용률 < 20% 시 예외 발생
4. **복구 조건**: 1시간 + Health Score ≥ 30

---

## 🔗 관련 문서

- Domain Layer: `docs/prd/crawler/user-agent/tasks/USER-AGENT-001-domain.md`
- Persistence Layer: `docs/prd/crawler/user-agent/tasks/USER-AGENT-003-persistence.md`
