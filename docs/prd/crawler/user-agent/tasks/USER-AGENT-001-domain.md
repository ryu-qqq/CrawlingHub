# USER-AGENT-001: UserAgent Domain Layer 구현

**Bounded Context**: Crawler
**Sub-Context**: UserAgent
**Layer**: Domain Layer
**브랜치**: feature/USER-AGENT-001-domain

---

## 📝 목적

UserAgent Aggregate의 비즈니스 핵심 로직을 담당하는 Domain Layer 구현.

**핵심 역할**:
- UserAgent 비즈니스 규칙 구현
- Health Score 기반 상태 관리
- Token VO 암호화 및 관리
- Redis Token Bucket 통합 (Rate Limiting)

---

## 🎯 요구사항

### 1. Aggregate: UserAgent (크롤러 유저 에이전트)

- [ ] **UserAgent Aggregate 구현**
  - userAgentId (UserAgentId VO, UUID)
  - token (Token VO, AES-256 암호화)
  - status (UserAgentStatus Enum)
  - healthScore (Integer, 0-100)
  - lastUsedAt (LocalDateTime)
  - requestsPerDay (Integer, 일일 요청 수)
  - createdAt (LocalDateTime)
  - updatedAt (LocalDateTime)

- [ ] **비즈니스 규칙**
  - UserAgent 생성 시 상태 AVAILABLE
  - Health Score 초기값 100
  - Token은 AES-256 암호화 저장 (복호화 불가)
  - Redis Token Bucket으로 초당 1회 제한
  - Health Score < 30 → 자동 SUSPENDED
  - 429 응답 시 → Health Score -20, SUSPENDED

- [ ] **상태 전환 로직**
  - AVAILABLE → SUSPENDED (Health Score < 30 또는 429 응답)
  - SUSPENDED → AVAILABLE (1시간 후 자동 복구)
  - AVAILABLE/SUSPENDED → BLOCKED (관리자 차단)

- [ ] **Value Objects**
  - UserAgentId: UUID
  - Token: String (AES-256 암호화, 복호화 불가)
  - UserAgentStatus: Enum (AVAILABLE, SUSPENDED, BLOCKED)

- [ ] **Domain 메서드**
  - `create(token)`: UserAgent 생성 (AVAILABLE, Health Score 100)
  - `issueToken()`: Token 발급 (Redis Token Bucket 소진)
  - `recordSuccess()`: 성공 기록 (Health Score +5, 최대 100)
  - `recordFailure(statusCode)`: 실패 기록
    - 429 → Health Score -20, SUSPENDED
    - 500 → Health Score -10
    - 기타 → Health Score -5
  - `suspend()`: 수동 정지
  - `recover()`: 복구 (SUSPENDED → AVAILABLE, Health Score 70)
  - `block()`: 영구 차단

---

## ⚠️ 제약사항

### Zero-Tolerance 규칙

- [ ] **Lombok 금지**: Pure Java 또는 Record 사용
- [ ] **Law of Demeter 준수**: Getter 체이닝 금지
- [ ] **Tell Don't Ask**: 내부 상태 기반 판단
- [ ] **Long FK 전략**: 관계 어노테이션 금지

### 테스트 규칙

- [ ] **ArchUnit 테스트 필수**
- [ ] **TestFixture 패턴 사용**
- [ ] **테스트 커버리지 > 80%**

---

## ✅ 완료 조건

- [ ] UserAgent Aggregate 구현 완료
- [ ] 모든 Value Object 구현 완료
- [ ] 모든 Domain 메서드 구현 완료
- [ ] Unit Test 작성 완료
- [ ] Zero-Tolerance 규칙 준수

---

## 🔗 관련 문서

- **Plan**: docs/prd/crawler/user-agent/plans/USER-AGENT-001-domain-plan.md
- **Domain Layer 규칙**: docs/coding_convention/02-domain-layer/

---

## 📚 참고사항

### Token VO 암호화 예시

```java
public record Token(String encryptedValue) {
    public Token {
        validateFormat(encryptedValue);
    }

    private void validateFormat(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidTokenException("Token cannot be null or blank");
        }
        // AES-256 암호화된 형식 검증
        if (!value.matches("^[A-Za-z0-9+/=]{44,}$")) {
            throw new InvalidTokenException("Invalid encrypted token format");
        }
    }

    // 복호화 금지 - 암호화된 상태로만 사용
    // 비교는 encryptedValue 동일성으로만 가능
}
```

### Health Score 계산 로직

```java
public class UserAgent {
    // Health Score 0-100 범위 유지
    public void recordSuccess() {
        this.healthScore = Math.min(100, this.healthScore + 5);
        this.lastUsedAt = LocalDateTime.now();
    }

    public void recordFailure(int statusCode) {
        if (statusCode == 429) {
            this.healthScore -= 20;
            if (this.healthScore < 30) {
                this.status = UserAgentStatus.SUSPENDED;
            }
        } else if (statusCode >= 500) {
            this.healthScore = Math.max(0, this.healthScore - 10);
        } else {
            this.healthScore = Math.max(0, this.healthScore - 5);
        }
        this.lastUsedAt = LocalDateTime.now();
    }
}
```

### Redis Token Bucket 통합

```java
// Domain Layer에서는 인터페이스만 정의
public interface RateLimitPort {
    boolean tryConsume(UserAgentId userAgentId);
}

// UserAgent Aggregate
public Token issueToken(RateLimitPort rateLimitPort) {
    if (!rateLimitPort.tryConsume(this.userAgentId)) {
        throw new RateLimitExceededException("Rate limit exceeded for UserAgent: " + this.userAgentId);
    }
    this.lastUsedAt = LocalDateTime.now();
    return this.token;
}
```
