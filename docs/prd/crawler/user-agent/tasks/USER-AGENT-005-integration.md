# USER-AGENT-005: UserAgent Integration Test 구현

**Bounded Context**: Crawler
**Sub-Context**: UserAgent
**Layer**: Integration Test
**브랜치**: feature/USER-AGENT-005-integration

---

## 📝 목적

UserAgent E2E 시나리오 테스트.

---

## 🎯 요구사항

### 1. E2E 시나리오

#### 시나리오 1: UserAgent 429 처리 및 자동 복구

- [ ] **Given: UserAgent 등록** (Health Score 100, AVAILABLE)
- [ ] **When: 429 응답 기록** (recordFailure(429))
- [ ] **Then: Health Score -20, SUSPENDED 확인**
- [ ] **When: 1시간 경과** (recoverSuspendedUserAgents 실행)
- [ ] **Then: Health Score 70, AVAILABLE 복구 확인**

#### 시나리오 2: Circuit Breaker 동작

- [ ] **Given: UserAgent 5개 등록** (AVAILABLE 1개, SUSPENDED 4개)
- [ ] **When: assignHealthiestUserAgent 호출**
- [ ] **Then: CircuitBreakerOpenException** (available rate 20%)

#### 시나리오 3: Race Condition 방지 (Pessimistic Lock)

- [ ] **Given: UserAgent 1개 등록** (AVAILABLE)
- [ ] **When: 동시에 2개 스레드에서 할당 요청**
- [ ] **Then: 1개만 성공, 1개는 NoAvailableUserAgentException**

### 2. Redis Rate Limiting 테스트

- [ ] Token Bucket 소진 테스트 (초당 1회 제한)
- [ ] Token 자동 충전 테스트 (1초 후)

---

## ✅ 완료 조건

- [ ] E2E 시나리오 테스트 통과
- [ ] Circuit Breaker 테스트 통과
- [ ] Pessimistic Lock Race Condition 테스트 통과
- [ ] Redis Rate Limiting 테스트 통과

---

## 🔗 관련 문서

- **Plan**: docs/prd/crawler/user-agent/plans/USER-AGENT-005-integration-plan.md

---

## 📚 참고사항

### E2E 시나리오 1: 429 처리 및 자동 복구

```java
@SpringBootTest
@AutoConfigureTestRestTemplate
class UserAgent429RecoveryIntegrationTest {

    @Autowired
    private UserAgentCommandPort userAgentCommandPort;

    @Autowired
    private UserAgentQueryPort userAgentQueryPort;

    @Autowired
    private UserAgentPoolManager userAgentPoolManager;

    @Test
    void 유저에이전트_429_처리_및_자동_복구() {
        // Given: UserAgent 등록
        UserAgent userAgent = UserAgentFixture.createAvailable(100);
        userAgentCommandPort.save(userAgent);

        // When: 429 응답 기록
        userAgent.recordFailure(429);
        userAgentCommandPort.save(userAgent);

        // Then: Health Score -20, SUSPENDED 확인
        UserAgent suspended = userAgentQueryPort.findById(userAgent.getUserAgentId()).orElseThrow();
        assertThat(suspended.getHealthScore()).isEqualTo(80);
        assertThat(suspended.getStatus()).isEqualTo(UserAgentStatus.SUSPENDED);

        // When: 1시간 경과 (Mock)
        suspended.setLastUsedAt(LocalDateTime.now().minusHours(1));
        userAgentCommandPort.save(suspended);

        // When: 자동 복구 실행
        userAgentPoolManager.recoverSuspendedUserAgents();

        // Then: Health Score 70, AVAILABLE 복구 확인
        UserAgent recovered = userAgentQueryPort.findById(userAgent.getUserAgentId()).orElseThrow();
        assertThat(recovered.getHealthScore()).isEqualTo(70);
        assertThat(recovered.getStatus()).isEqualTo(UserAgentStatus.AVAILABLE);
    }
}
```

### E2E 시나리오 2: Circuit Breaker

```java
@SpringBootTest
class CircuitBreakerIntegrationTest {

    @Autowired
    private UserAgentCommandPort userAgentCommandPort;

    @Autowired
    private UserAgentPoolManager userAgentPoolManager;

    @Test
    void 가용률_20퍼센트_미만_시_Circuit_Breaker_동작() {
        // Given: UserAgent 5개 (AVAILABLE 1개, SUSPENDED 4개)
        userAgentCommandPort.save(UserAgentFixture.createAvailable(100));
        userAgentCommandPort.save(UserAgentFixture.createSuspended(20));
        userAgentCommandPort.save(UserAgentFixture.createSuspended(20));
        userAgentCommandPort.save(UserAgentFixture.createSuspended(20));
        userAgentCommandPort.save(UserAgentFixture.createSuspended(20));

        // When: 할당 시도
        // Then: CircuitBreakerOpenException (available rate = 20%)
        assertThatThrownBy(() -> userAgentPoolManager.assignHealthiestUserAgent())
            .isInstanceOf(CircuitBreakerOpenException.class)
            .hasMessageContaining("Circuit Breaker OPEN")
            .hasMessageContaining("20.00%");
    }
}
```

### E2E 시나리오 3: Race Condition 방지

```java
@SpringBootTest
class PessimisticLockIntegrationTest {

    @Autowired
    private UserAgentCommandPort userAgentCommandPort;

    @Autowired
    private UserAgentPoolManager userAgentPoolManager;

    @Test
    void 동시_할당_요청_시_Pessimistic_Lock으로_Race_Condition_방지() throws InterruptedException {
        // Given: UserAgent 1개만 등록
        UserAgent userAgent = UserAgentFixture.createAvailable(100);
        userAgentCommandPort.save(userAgent);

        // When: 2개 스레드에서 동시 할당 요청
        CountDownLatch latch = new CountDownLatch(2);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        Runnable assignTask = () -> {
            try {
                userAgentPoolManager.assignHealthiestUserAgent();
                successCount.incrementAndGet();
            } catch (NoAvailableUserAgentException e) {
                failureCount.incrementAndGet();
            } finally {
                latch.countDown();
            }
        };

        Thread thread1 = new Thread(assignTask);
        Thread thread2 = new Thread(assignTask);

        thread1.start();
        thread2.start();

        latch.await(5, TimeUnit.SECONDS);

        // Then: 1개만 성공, 1개는 실패
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failureCount.get()).isEqualTo(1);
    }
}
```

### Redis Rate Limiting 테스트

```java
@SpringBootTest
@Testcontainers
class RedisRateLimitIntegrationTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
        .withExposedPorts(6379);

    @Autowired
    private RedisRateLimitAdapter redisRateLimitAdapter;

    @Test
    void 초당_1회_Rate_Limit_테스트() {
        // Given: UserAgent ID
        UserAgentId userAgentId = new UserAgentId(UUID.randomUUID());

        // When: 첫 번째 요청
        boolean firstAttempt = redisRateLimitAdapter.tryConsume(userAgentId);

        // Then: 성공
        assertThat(firstAttempt).isTrue();

        // When: 즉시 두 번째 요청
        boolean secondAttempt = redisRateLimitAdapter.tryConsume(userAgentId);

        // Then: 실패 (Rate Limit 초과)
        assertThat(secondAttempt).isFalse();
    }

    @Test
    void Token_자동_충전_테스트() throws InterruptedException {
        // Given: UserAgent ID
        UserAgentId userAgentId = new UserAgentId(UUID.randomUUID());

        // When: Token 소진
        redisRateLimitAdapter.tryConsume(userAgentId);

        // When: 1초 대기
        Thread.sleep(1100);

        // When: Token 충전 확인
        boolean afterRefill = redisRateLimitAdapter.tryConsume(userAgentId);

        // Then: 성공 (Token 충전됨)
        assertThat(afterRefill).isTrue();
    }
}
```
