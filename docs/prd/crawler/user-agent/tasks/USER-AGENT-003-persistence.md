# USER-AGENT-003: UserAgent Persistence Layer 구현

**Bounded Context**: Crawler
**Sub-Context**: UserAgent
**Layer**: Persistence Layer
**브랜치**: feature/USER-AGENT-003-persistence

---

## 📝 목적

UserAgent 데이터 영속성 및 Redis Rate Limiting.

---

## 🎯 요구사항

### 1. JPA Entity

#### UserAgentJpaEntity
- 테이블: `user_agents`
- 인덱스:
  - `idx_user_agent_id` (user_agent_id) - Unique
  - `idx_status_health_score` (status, health_score DESC) - 할당 성능 최적화
  - `idx_status_last_used_at` (status, last_used_at) - 복구 쿼리 최적화

### 2. Repository

- UserAgentJpaRepository (JPA 기본)
  - `findTopByStatusOrderByHealthScoreDescWithLock()` - Pessimistic Lock
  - `findByStatusAndLastUsedAtBefore()` - 복구 대상 조회
  - `countByStatus()` - 상태별 집계
- UserAgentQueryDslRepository (통계 조회)
  - `getPoolStats()` - UserAgent Pool 통계 (DTO Projection)

### 3. Redis Integration

- RedisRateLimitAdapter (RateLimitPort 구현)
  - Token Bucket 알고리즘
  - 초당 1회 제한
  - Key: `rate_limit:user_agent:{userAgentId}`

### 4. Flyway

- V6__create_user_agents_table.sql

---

## ✅ 완료 조건

- [ ] UserAgentJpaEntity 구현 완료
- [ ] Repository 구현 완료
- [ ] QueryDSL DTO Projection 완료 (Pool 통계)
- [ ] Redis Rate Limiting 구현 완료
- [ ] Pessimistic Lock 테스트 완료

---

## 🔗 관련 문서

- **Plan**: docs/prd/crawler/user-agent/plans/USER-AGENT-003-persistence-plan.md

---

## 📚 참고사항

### UserAgentJpaEntity

```java
@Entity
@Table(
    name = "user_agents",
    indexes = {
        @Index(name = "idx_user_agent_id", columnList = "user_agent_id", unique = true),
        @Index(name = "idx_status_health_score", columnList = "status, health_score DESC"),
        @Index(name = "idx_status_last_used_at", columnList = "status, last_used_at")
    }
)
public class UserAgentJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_agent_id", nullable = false, unique = true, length = 36)
    private String userAgentId; // UUID

    @Column(name = "encrypted_token", nullable = false, length = 500)
    private String encryptedToken; // AES-256 암호화

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserAgentStatus status;

    @Column(name = "health_score", nullable = false)
    private Integer healthScore;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "requests_per_day", nullable = false)
    private Integer requestsPerDay;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Getters, Setters 생략
}
```

### Pessimistic Lock Repository

```java
public interface UserAgentJpaRepository extends JpaRepository<UserAgentJpaEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM UserAgentJpaEntity u WHERE u.status = :status ORDER BY u.healthScore DESC")
    Optional<UserAgentJpaEntity> findTopByStatusOrderByHealthScoreDescWithLock(
        @Param("status") UserAgentStatus status
    );

    List<UserAgentJpaEntity> findByStatusAndLastUsedAtBefore(
        UserAgentStatus status,
        LocalDateTime lastUsedAt
    );

    long countByStatus(UserAgentStatus status);
}
```

### QueryDSL DTO Projection

```java
@Repository
@RequiredArgsConstructor
public class UserAgentQueryDslRepository {
    private final JPAQueryFactory queryFactory;

    public UserAgentStats getPoolStats() {
        QUserAgentJpaEntity ua = QUserAgentJpaEntity.userAgentJpaEntity;

        return queryFactory
            .select(Projections.constructor(
                UserAgentStats.class,
                ua.count(),
                ua.status.when(UserAgentStatus.AVAILABLE).then(1L).otherwise(0L).sum(),
                ua.status.when(UserAgentStatus.SUSPENDED).then(1L).otherwise(0L).sum(),
                ua.status.when(UserAgentStatus.BLOCKED).then(1L).otherwise(0L).sum(),
                ua.healthScore.avg(),
                ua.healthScore.min(),
                ua.healthScore.max()
            ))
            .from(ua)
            .fetchOne();
    }
}

public record UserAgentStats(
    Long totalAgents,
    Long availableAgents,
    Long suspendedAgents,
    Long blockedAgents,
    Double avgHealthScore,
    Integer minHealthScore,
    Integer maxHealthScore
) {
    public double getAvailableRate() {
        if (totalAgents == 0) return 0.0;
        return (double) availableAgents / totalAgents * 100;
    }
}
```

### Redis Rate Limiting

```java
@Component
@RequiredArgsConstructor
public class RedisRateLimitAdapter implements RateLimitPort {
    private final StringRedisTemplate redisTemplate;
    private static final String KEY_PREFIX = "rate_limit:user_agent:";
    private static final long CAPACITY = 1; // 초당 1회
    private static final long REFILL_RATE = 1; // 1초마다 1개 충전

    @Override
    public boolean tryConsume(UserAgentId userAgentId) {
        String key = KEY_PREFIX + userAgentId.value();

        // Token Bucket 알고리즘
        Long currentTokens = redisTemplate.opsForValue().get(key) != null
            ? Long.parseLong(redisTemplate.opsForValue().get(key))
            : CAPACITY;

        if (currentTokens > 0) {
            // Token 소진
            redisTemplate.opsForValue().decrement(key);
            redisTemplate.expire(key, Duration.ofSeconds(1));
            return true;
        }

        return false; // Rate limit 초과
    }

    @Scheduled(fixedDelay = 1000) // 1초마다 Token 충전
    public void refillTokens() {
        // 모든 UserAgent Token Bucket 충전
        Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");
        if (keys != null) {
            for (String key : keys) {
                redisTemplate.opsForValue().set(key, String.valueOf(CAPACITY));
            }
        }
    }
}
```

### Flyway Migration

```sql
-- V6__create_user_agents_table.sql
CREATE TABLE user_agents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_agent_id VARCHAR(36) NOT NULL UNIQUE COMMENT 'UUID',
    encrypted_token VARCHAR(500) NOT NULL COMMENT 'AES-256 암호화 토큰',
    status VARCHAR(20) NOT NULL COMMENT 'AVAILABLE, SUSPENDED, BLOCKED',
    health_score INT NOT NULL DEFAULT 100 CHECK (health_score BETWEEN 0 AND 100),
    last_used_at DATETIME COMMENT '마지막 사용 시각',
    requests_per_day INT NOT NULL DEFAULT 0 COMMENT '일일 요청 수',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_agent_id (user_agent_id),
    INDEX idx_status_health_score (status, health_score DESC),
    INDEX idx_status_last_used_at (status, last_used_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='크롤러 UserAgent 풀';
```
