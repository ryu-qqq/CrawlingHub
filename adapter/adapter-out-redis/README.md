# adapter-out-redis

Redis 기반 분산 레이트 리미팅 및 토큰 관리 인프라

## 개요

User-Agent 기반 토큰 관리 및 분산 레이트 리미팅 시스템을 위한 Redis Adapter 모듈입니다.

### 주요 기능

- **Token Bucket Rate Limiting**: Lua Script 기반 원자적 레이트 리미팅
- **Circuit Breaker**: Redis Hash 기반 Circuit Breaker 상태 관리
- **User-Agent Pool**: Sorted Set 기반 LRU 선택 로직
- **Distributed Lock**: 분산 락 구현 (데드락 방지)
- **Token Queue**: FIFO 기반 토큰 획득 대기 큐
- **Health Check**: Connection Pool 모니터링 및 Retry 정책

## 기술 스택

- **Redis 7.0+**
- **Lua Script**: 원자적 연산 보장
- **Lettuce**: Redis 클라이언트 (비동기 지원)
- **Spring Data Redis**

## 성능 목표

- Token Bucket 판단: **p99 < 10ms**
- 분산 락 획득: **p99 < 50ms**
- Throughput: **> 10,000 ops/sec**

## 핵심 컴포넌트

### 1. TokenBucketRateLimiter

```java
@Service
public class TokenBucketRateLimiter {
    // 10분당 80 requests (머스트잇 API 제약)
    public RateLimitResult tryConsumeDefault(Long userAgentId)
}
```

**특징**:
- Lua Script 기반 원자적 Token 소비
- 자동 Refill 로직 (경과 시간 기반)
- TTL 자동 관리 (1시간)

### 2. CircuitBreakerManager

```java
@Service
public class CircuitBreakerManager {
    public void recordSuccess(Long userAgentId)
    public void recordFailure(Long userAgentId)
    public boolean tryRecover(Long userAgentId)
}
```

**상태 전이**:
- `CLOSED` → `OPEN`: 연속 실패 3회
- `OPEN` → `HALF_OPEN`: 10분 타임아웃 후
- `HALF_OPEN` → `CLOSED`: 연속 성공 3회

### 3. UserAgentPoolManager

```java
@Service
public class UserAgentPoolManager {
    public Long acquireLeastRecentlyUsed()  // LRU 선택
    public void returnToPool(Long userAgentId)
}
```

**구조**:
- Sorted Set 기반
- Score: last_used_timestamp
- ZPOPMIN을 통한 LRU 선택

### 4. DistributedLockService

```java
@Service
public class DistributedLockService {
    public LockHandle tryLock(String resourceId)
    public boolean unlock(LockHandle lockHandle)
}
```

**보장 사항**:
- 락 소유자만 해제 가능
- 데드락 방지 (TTL 30초)
- 원자적 락 획득/해제

### 5. TokenAcquisitionQueue

```java
@Service
public class TokenAcquisitionQueue {
    public void enqueue(Long userAgentId, String requestId)
    public String dequeue(Long userAgentId)  // FIFO
}
```

**특징**:
- LPUSH/RPOP 기반 FIFO
- 타임아웃 자동 제거 (60초)

## Redis 데이터 구조

### Token Bucket
```
Key: rate_limit:bucket:{user_agent_id}
Type: Hash
Fields:
  - tokens: 현재 토큰 수
  - last_refill_timestamp: 마지막 재충전 시각
  - max_tokens: 최대 토큰 수
  - refill_rate: 재충전 속도
TTL: 1시간
```

### Circuit Breaker
```
Key: circuit_breaker:{user_agent_id}
Type: Hash
Fields:
  - state: CLOSED | OPEN | HALF_OPEN
  - consecutive_failures: 연속 실패 횟수
  - consecutive_successes: 연속 성공 횟수
  - opened_at: OPEN 전환 시각
TTL: 1시간
```

### User-Agent Pool
```
Key: user_agent:availability_pool
Type: Sorted Set
Score: last_used_timestamp (ms)
Members: user_agent_id
```

### Distributed Lock
```
Key: lock:{resource_id}
Type: String
Value: lock_owner (UUID)
TTL: 30초
```

### Token Queue
```
Key: token_queue:{user_agent_id}
Type: List
Values: request_id (FIFO)

Key: token_queue_timeout:{request_id}
Type: String
TTL: 60초
```

## Configuration

### Redis Connection Pool

```properties
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.lettuce.pool.min-idle=10
spring.data.redis.lettuce.pool.max-idle=20
spring.data.redis.lettuce.pool.max-active=50
```

**설정값**:
- Min Idle: 10
- Max Active: 50
- Connect Timeout: 5s
- Command Timeout: 3s

### Health Check

```java
@Autowired
private RedisHealthCheckService healthCheck;

HealthStatus status = healthCheck.checkHealth();
if (!status.isHealthy()) {
    // Failover 처리
}
```

## Lua Scripts

### token_bucket.lua

원자적 Token 소비 및 Refill 로직

```lua
-- KEYS[1]: rate_limit:bucket:{user_agent_id}
-- ARGV[1-5]: tokens_to_consume, timestamp, refill_rate, max_tokens, ttl
-- Returns: {success (1/0), current_tokens, retry_after_ms}
```

### distributed_lock.lua

분산 락 획득

```lua
-- KEYS[1]: lock:{resource_id}
-- ARGV[1-2]: lock_owner, ttl_seconds
-- Returns: 1 (성공) / 0 (실패)
```

### distributed_unlock.lua

분산 락 해제 (소유자 검증)

```lua
-- KEYS[1]: lock:{resource_id}
-- ARGV[1]: lock_owner
-- Returns: 1 (성공) / 0 (실패)
```

## 사용 예제

### Rate Limiting

```java
@Autowired
private TokenBucketRateLimiter rateLimiter;

RateLimitResult result = rateLimiter.tryConsumeDefault(userAgentId);
if (result.isAllowed()) {
    // 요청 허용
} else {
    // 요청 거부, retry_after_ms 후 재시도
    long retryAfterMs = result.getRetryAfterMs();
}
```

### Circuit Breaker

```java
@Autowired
private CircuitBreakerManager circuitBreaker;

// 성공 기록
circuitBreaker.recordSuccess(userAgentId);

// 실패 기록 (3회 연속 실패 시 OPEN)
circuitBreaker.recordFailure(userAgentId);

// 복구 시도 (10분 후)
if (circuitBreaker.tryRecover(userAgentId)) {
    // HALF_OPEN 전환 성공
}
```

### Distributed Lock

```java
@Autowired
private DistributedLockService lockService;

LockHandle lock = lockService.tryLock("my-resource");
if (lock.isAcquired()) {
    try {
        // Critical section
    } finally {
        lockService.unlock(lock);
    }
}
```

## 테스트

### 단위 테스트

```bash
./gradlew :adapter:adapter-out-redis:test
```

### 테스트 구성

- **Testcontainers**: Redis 7.0-alpine 컨테이너 사용
- **Coverage 목표**: 70% (Adapter 모듈 기준)
- **테스트 종류**:
    - TokenBucketRateLimiterTest: Token Bucket 알고리즘 검증
    - CircuitBreakerManagerTest: 상태 전이 로직 검증
    - UserAgentPoolManagerTest: LRU 선택 로직 검증
    - DistributedLockServiceTest: 분산 락 동시성 검증

## 아키텍처

```
┌─────────────────────────────────────┐
│     Application Layer (Ports)      │
└─────────────────────────────────────┘
                ↓
┌─────────────────────────────────────┐
│    adapter-out-redis (Adapters)     │
│  ┌───────────────────────────────┐  │
│  │  TokenBucketRateLimiter      │  │
│  │  CircuitBreakerManager       │  │
│  │  UserAgentPoolManager        │  │
│  │  DistributedLockService      │  │
│  │  TokenAcquisitionQueue       │  │
│  │  RedisHealthCheckService     │  │
│  └───────────────────────────────┘  │
└─────────────────────────────────────┘
                ↓
┌─────────────────────────────────────┐
│         Redis 7.0+ Cluster          │
│  ┌───────────────────────────────┐  │
│  │  Hash, Sorted Set, List       │  │
│  │  Lua Scripts                  │  │
│  │  Connection Pool (Lettuce)    │  │
│  └───────────────────────────────┘  │
└─────────────────────────────────────┘
```

## 관련 문서

- [CRAW-80 Jira Task](https://ryuqqq.atlassian.net/browse/CRAW-80)
- [V7 Database Migration](../adapter-out-persistence-jpa/src/main/resources/db/migration/V7__create_token_rate_limiting_tables.sql)
- [Epic: 토큰 & 레이트 리미팅 시스템](https://ryuqqq.atlassian.net/browse/CRAW-78)

## TODO (향후 확장)

- [ ] Redis Cluster 지원 (Hash Tag 전략)
- [ ] Failover 시나리오 자동 처리
- [ ] Metrics & Monitoring (Micrometer 통합)
- [ ] Redis Sentinel 지원
- [ ] 성능 벤치마크 리포트

## License

Copyright © 2025 CrawlingHub
