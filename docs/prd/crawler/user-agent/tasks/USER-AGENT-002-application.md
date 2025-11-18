# USER-AGENT-002: UserAgent Application Layer 구현

**Bounded Context**: Crawler
**Sub-Context**: UserAgent
**Layer**: Application Layer
**브랜치**: feature/USER-AGENT-002-application

---

## 📝 목적

UserAgent 관련 Use Case 오케스트레이션 및 Pool 관리.

---

## 🎯 요구사항

### 1. Manager

#### UserAgentPoolManager
- **목적**: Health Score 기반 UserAgent 할당 및 복구
- **핵심 로직**:
  1. assignHealthiestUserAgent() - 가장 건강한 UserAgent 할당
     - Health Score 기준 내림차순 정렬
     - 첫 번째 AVAILABLE UserAgent 선택
     - Pessimistic Lock으로 동시 할당 방지
     - lastUsedAt 업데이트
  2. Circuit Breaker - Available Rate < 20% 시 차단
     - Total UserAgent 수 조회
     - AVAILABLE 상태 UserAgent 수 조회
     - availableRate = (available / total) * 100
     - availableRate < 20% → CircuitBreakerOpenException
  3. recoverSuspendedUserAgents() - 자동 복구 (1시간마다)
     - SUSPENDED 상태 + lastUsedAt < 1시간 전
     - Health Score 70으로 복구
     - AVAILABLE 상태로 전환

### 2. Query Use Cases

#### GetUserAgentPoolStatusUseCase
- **입력**: 없음
- **출력**: UserAgentPoolStatusResponse
  - totalAgents (총 UserAgent 수)
  - availableAgents (AVAILABLE 수)
  - suspendedAgents (SUSPENDED 수)
  - blockedAgents (BLOCKED 수)
  - availableRate (가용률, %)
  - healthScoreStats (평균, 최소, 최대)

---

## ✅ 완료 조건

- [ ] UserAgentPoolManager 구현 완료
- [ ] GetUserAgentPoolStatusUseCase 구현 완료
- [ ] Transaction 경계 검증 완료
- [ ] Circuit Breaker 테스트 완료
- [ ] Pessimistic Lock Race Condition 테스트 완료

---

## 🔗 관련 문서

- **Plan**: docs/prd/crawler/user-agent/plans/USER-AGENT-002-application-plan.md

---

## 📚 참고사항

### UserAgentPoolManager 구현 예시

```java
@Component
@RequiredArgsConstructor
public class UserAgentPoolManager {
    private final UserAgentQueryPort userAgentQueryPort;
    private final UserAgentCommandPort userAgentCommandPort;

    @Transactional
    public UserAgent assignHealthiestUserAgent() {
        // 1. Circuit Breaker 검증
        checkCircuitBreaker();

        // 2. Pessimistic Lock으로 가장 건강한 UserAgent 조회
        UserAgent userAgent = userAgentQueryPort.findHealthiestWithLock()
            .orElseThrow(() -> new NoAvailableUserAgentException("No available UserAgent"));

        // 3. lastUsedAt 업데이트
        userAgent.markAsUsed();

        // 4. 저장
        userAgentCommandPort.save(userAgent);

        return userAgent;
    }

    private void checkCircuitBreaker() {
        long total = userAgentQueryPort.countAll();
        long available = userAgentQueryPort.countByStatus(UserAgentStatus.AVAILABLE);

        if (total == 0) {
            throw new CircuitBreakerOpenException("No UserAgent configured");
        }

        double availableRate = (double) available / total * 100;
        if (availableRate < 20.0) {
            throw new CircuitBreakerOpenException(
                String.format("UserAgent pool Circuit Breaker OPEN (available rate: %.2f%%)", availableRate)
            );
        }
    }

    @Transactional
    @Scheduled(cron = "0 0 * * * *") // 매 시간 정각
    public void recoverSuspendedUserAgents() {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);

        List<UserAgent> suspendedAgents = userAgentQueryPort.findSuspendedBefore(oneHourAgo);

        for (UserAgent agent : suspendedAgents) {
            agent.recover(); // Health Score 70, AVAILABLE
        }

        userAgentCommandPort.saveAll(suspendedAgents);
    }
}
```

### GetUserAgentPoolStatusUseCase 구현 예시

```java
@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetUserAgentPoolStatusUseCase {
    private final UserAgentQueryPort userAgentQueryPort;

    public UserAgentPoolStatusResponse execute() {
        // 1. 전체 통계 조회 (QueryDSL DTO Projection)
        UserAgentStats stats = userAgentQueryPort.getPoolStats();

        // 2. Response 구성
        return UserAgentPoolStatusResponse.of(stats);
    }
}
```

### Transaction 경계

```java
// ✅ 올바른 예시 - Transaction 내에서 DB 작업만
@Transactional
public UserAgent assignHealthiestUserAgent() {
    // DB 조회 + 업데이트만
    UserAgent userAgent = userAgentQueryPort.findHealthiestWithLock()
        .orElseThrow(...);
    userAgent.markAsUsed();
    userAgentCommandPort.save(userAgent);
    return userAgent;
}

// ❌ 잘못된 예시 - Transaction 내 외부 API 호출
@Transactional
public void recordUserAgentUsage(UserAgentId id, int statusCode) {
    UserAgent agent = userAgentQueryPort.findById(id).orElseThrow(...);
    agent.recordFailure(statusCode);

    // 외부 API 호출 (트랜잭션 내 금지!)
    slackClient.sendAlert("UserAgent suspended: " + id);

    userAgentCommandPort.save(agent);
}

// ✅ 올바른 예시 - Transaction 밖에서 외부 API 호출
public void recordUserAgentUsage(UserAgentId id, int statusCode) {
    UserAgent savedAgent = recordFailureInTransaction(id, statusCode);

    // Transaction 밖에서 외부 API 호출
    if (savedAgent.isSuspended()) {
        slackClient.sendAlert("UserAgent suspended: " + id);
    }
}

@Transactional
private UserAgent recordFailureInTransaction(UserAgentId id, int statusCode) {
    UserAgent agent = userAgentQueryPort.findById(id).orElseThrow(...);
    agent.recordFailure(statusCode);
    return userAgentCommandPort.save(agent);
}
```

### Pessimistic Lock 사용

```java
// Port 인터페이스
public interface UserAgentQueryPort {
    Optional<UserAgent> findHealthiestWithLock();
}

// Persistence Adapter 구현
@Repository
public class UserAgentQueryAdapter implements UserAgentQueryPort {
    @Override
    public Optional<UserAgent> findHealthiestWithLock() {
        // Pessimistic Write Lock
        return userAgentJpaRepository.findTopByStatusOrderByHealthScoreDescWithLock(
            UserAgentStatus.AVAILABLE
        );
    }
}
```
