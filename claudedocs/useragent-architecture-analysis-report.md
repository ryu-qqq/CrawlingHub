# UserAgent 바운디드 컨텍스트 아키텍처 종합 분석 보고서

**분석 일자**: 2025-11-05
**분석 대상**: UserAgent Bounded Context
**분석 범위**: Domain, Application, Persistence (MySQL/Redis), REST API Layer

---

## 📋 목차

1. [Executive Summary](#executive-summary)
2. [현재 구현 상태](#현재-구현-상태)
3. [Layer별 상세 분석](#layer별-상세-분석)
4. [Zero-Tolerance 규칙 체크리스트](#zero-tolerance-규칙-체크리스트)
5. [컨벤션 준수 평가](#컨벤션-준수-평가)
6. [우선순위별 개선 계획](#우선순위별-개선-계획)
7. [예상 개선 효과](#예상-개선-효과)

---

## Executive Summary

### 전체 구현 완성도

| Layer | 구현 여부 | 컨벤션 준수율 | 상태 |
|-------|----------|-------------|------|
| Domain | ✅ 구현 완료 | **100%** | 🟢 Excellent |
| Application | ⚠️ 부분 구현 | **70%** | 🟡 Partial |
| Persistence-MySQL | ❌ 미구현 | **0%** | 🔴 Not Implemented |
| Persistence-Redis | ❌ 미구현 | **0%** | 🔴 Not Implemented |
| REST API | ❌ 미구현 | **0%** | 🔴 Not Implemented |

### 핵심 발견 사항

**✅ 우수한 점**:
1. **Domain Layer 완벽 구현** - Law of Demeter, Pure Java, Tell Don't Ask 원칙 100% 준수
2. **비즈니스 로직 캡슐화** - UserAgent Aggregate에 토큰 관리 로직 완벽 구현
3. **Java 21 Record 활용** - UserAgentId에 Compact Constructor 패턴 적용

**❌ 치명적 문제**:
1. **Persistence Layer 완전 누락** - Entity, Repository, Adapter 미구현
2. **REST API Layer 완전 누락** - Controller, DTO, Exception Handling 미구현
3. **Application Layer 불완전** - Port 정의만 있고 구현체 없음 (UserAgentPort)

**⚠️ 중요한 문제**:
1. **CQRS 패턴 미적용** - Persistence 미구현으로 CQRS 적용 불가
2. **Domain Exception 누락** - UserAgent 전용 예외 클래스 정의 안 됨
3. **Port 구현체 누락** - UserAgentPort의 구현 Adapter가 없음

### Zero-Tolerance 준수율

**Domain Layer**: 3/3 (100%) ✅
**Application Layer**: 1/3 (33%) ⚠️
**Persistence Layer**: 0/5 (0%) ❌
**REST API Layer**: 0/4 (0%) ❌

**전체**: 4/15 (27%) 🔴

---

## 현재 구현 상태

### 구현된 파일

```
crawlinghub/
├── domain/
│   └── src/main/java/.../domain/useragent/
│       ├── UserAgent.java           ✅ (Aggregate Root)
│       ├── UserAgentId.java         ✅ (Value Object)
│       └── TokenStatus.java         ✅ (Enum)
│
└── application/
    └── src/main/java/.../application/task/
        ├── port/out/
        │   └── UserAgentPort.java   ⚠️ (Interface만 정의, 구현체 없음)
        └── service/
            └── ProcessCrawlTaskService.java ⚠️ (UserAgentPort 사용, 구현체 누락)
```

### 미구현된 필수 레이어

```
❌ adapter-out/persistence-mysql/
   └── useragent/
       ├── entity/
       │   └── UserAgentEntity.java         (필수)
       ├── repository/
       │   └── UserAgentJpaRepository.java  (필수)
       ├── adapter/
       │   ├── UserAgentCommandAdapter.java (CQRS 필수)
       │   └── UserAgentQueryAdapter.java   (CQRS 필수)
       └── mapper/
           └── UserAgentMapper.java         (필수)

❌ adapter-out/persistence-redis/
   └── useragent/
       └── UserAgentCacheAdapter.java       (선택)

❌ adapter-in/rest-api/
   └── useragent/
       ├── controller/
       │   └── UserAgentController.java     (필수)
       ├── dto/
       │   ├── request/
       │   │   ├── IssueTokenRequest.java
       │   │   └── BlacklistUserAgentRequest.java
       │   └── response/
       │       ├── UserAgentResponse.java
       │       └── UserAgentListResponse.java
       └── mapper/
           └── UserAgentApiMapper.java      (필수)

❌ application/
   └── useragent/
       ├── service/
       │   ├── IssueTokenService.java       (필수)
       │   ├── RecoverRateLimitService.java (필수)
       │   └── DisableUserAgentService.java (필수)
       ├── port/in/
       │   ├── IssueTokenUseCase.java       (필수)
       │   ├── RecoverRateLimitUseCase.java (필수)
       │   └── DisableUserAgentUseCase.java (필수)
       ├── port/out/
       │   ├── SaveUserAgentPort.java       (Command)
       │   └── LoadUserAgentPort.java       (Query)
       └── command/
           ├── IssueTokenCommand.java       (필수)
           ├── RecoverRateLimitCommand.java (필수)
           └── DisableUserAgentCommand.java (필수)
```

---

## Layer별 상세 분석

### 1. Domain Layer 분석 (✅ Excellent - 100%)

#### 1.1 UserAgent.java (Aggregate Root)

**위치**: `domain/src/main/java/com/ryuqq/crawlinghub/domain/useragent/UserAgent.java`

##### ✅ 우수한 점

1. **Pure Java 완벽 준수** (Zero-Tolerance)
```java
// ✅ Lombok 없음 - 모든 getter 수동 작성
public Long getIdValue() {
    return id != null ? id.value() : null;
}

public String getUserAgentString() {
    return userAgentString;
}
```

2. **Law of Demeter 완벽 준수**
```java
// ✅ Law of Demeter - Getter 체이닝 없음
public Long getIdValue() {
    return id != null ? id.value() : null;  // id.value() 한 단계만
}

// ❌ Anti-pattern (없음 - 잘 구현됨)
// public Long getIdValue() {
//     return id.getValueObject().getRawValue(); // 체이닝 금지
// }
```

3. **Tell, Don't Ask 원칙 준수**
```java
// ✅ Domain에 비즈니스 로직 캡슐화
public void consumeRequest() {
    if (!canMakeRequest()) {
        throw new IllegalStateException("요청을 수행할 수 없는 상태입니다");
    }
    this.remainingRequests--;
    this.tokenStatus = TokenStatus.ACTIVE;
    this.updatedAt = LocalDateTime.now(clock);
}

public void handleRateLimitError() {
    this.tokenStatus = TokenStatus.RATE_LIMITED;
    this.currentToken = null;
    this.remainingRequests = 0;
    this.rateLimitResetAt = LocalDateTime.now(clock).plusHours(RECOVERY_HOURS);
    this.updatedAt = LocalDateTime.now(clock);
}
```

4. **Javadoc 상세 작성**
```java
/**
 * 유저 에이전트 Aggregate Root
 *
 * <p>비즈니스 규칙:
 * <ul>
 *   <li>시간당 최대 80회 요청</li>
 *   <li>429 응답 시 즉시 토큰 폐기</li>
 *   <li>토큰 유효기간: 24시간</li>
 *   <li>DISABLED 상태 1시간 후 자동 RECOVERED</li>
 * </ul>
 */
```

5. **비즈니스 로직 완전 구현**
- 토큰 발급 (`issueNewToken`)
- 요청 소비 (`consumeRequest`)
- Rate Limit 처리 (`handleRateLimitError`)
- 자동 복구 (`recoverFromRateLimit`)
- 비활성화 (`disable`)

6. **불변성 보장**
```java
// ✅ final 필드 (ID, userAgentString, clock, createdAt)
private final UserAgentId id;
private final String userAgentString;
private final Clock clock;
private final LocalDateTime createdAt;
```

##### ⚠️ 개선 필요 사항

1. **생성자 패턴 불일치** (권장: 3-Constructor Pattern)

현재:
```java
// ❌ 3-Constructor 패턴 미준수
private UserAgent(전체 필드) {  // reconstitute용
    // ...
}

UserAgent(id, userAgentString, clock) {  // package-private
    // ...
}

public static UserAgent forNew(String userAgentString) {
    return new UserAgent(null, userAgentString, Clock.systemDefaultZone());
}

public static UserAgent of(UserAgentId id, String userAgentString) {
    return new UserAgent(id, userAgentString, Clock.systemDefaultZone());
}

public static UserAgent reconstitute(...) {
    return new UserAgent(...);
}
```

권장:
```java
// ✅ 3-Constructor 패턴 (JPA Entity와 일관성)
protected UserAgent() {  // no-args (for JPA)
    // 기본값 설정
}

private UserAgent(String userAgentString, Clock clock) {  // create (for new)
    validateRequiredFields(userAgentString);
    this.userAgentString = userAgentString;
    this.tokenStatus = TokenStatus.IDLE;
    this.remainingRequests = MAX_REQUESTS_PER_HOUR;
    this.clock = clock;
    this.createdAt = LocalDateTime.now(clock);
    this.updatedAt = LocalDateTime.now(clock);
}

private UserAgent(모든_필드) {  // reconstitute (for DB)
    this.id = id;
    this.userAgentString = userAgentString;
    // ...
}

public static UserAgent create(String userAgentString) {
    return new UserAgent(userAgentString, Clock.systemDefaultZone());
}

public static UserAgent reconstitute(...) {
    return new UserAgent(...);
}
```

2. **Domain Exception 누락**

현재:
```java
// ❌ IllegalArgumentException, IllegalStateException 사용
throw new IllegalArgumentException("User Agent 문자열은 필수입니다");
throw new IllegalStateException("요청을 수행할 수 없는 상태입니다");
```

권장:
```java
// ✅ Domain Exception 정의
public sealed interface DomainException permits UserAgentException {
    String getCode();
    String getMessage();
}

public sealed interface UserAgentException extends DomainException
    permits InvalidUserAgentException, TokenExpiredException, RateLimitExceededException {
}

public final class InvalidUserAgentException extends RuntimeException implements UserAgentException {
    private final String code = "INVALID_USER_AGENT";

    public InvalidUserAgentException(String message) {
        super(message);
    }

    @Override
    public String getCode() {
        return code;
    }
}
```

#### 1.2 UserAgentId.java (Value Object)

**위치**: `domain/src/main/java/com/ryuqq/crawlinghub/domain/useragent/UserAgentId.java`

##### ✅ 우수한 점

1. **Java 21 Record 활용**
```java
// ✅ Java 21 Record 패턴
public record UserAgentId(Long value) {

    // ✅ Compact Constructor (검증 로직)
    public UserAgentId {
        if (value != null && value <= 0) {
            throw new IllegalArgumentException("UserAgent ID는 양수여야 합니다");
        }
    }

    public static UserAgentId of(Long value) {
        return new UserAgentId(value);
    }
}
```

2. **불변성 보장**
- Record는 자동으로 불변

3. **간결성**
- Lombok 없이도 간결한 코드
- Getter 자동 생성 (value())

##### ⚠️ 개선 필요 사항

없음 - 완벽한 구현

#### 1.3 TokenStatus.java (Enum)

**위치**: `domain/src/main/java/com/ryuqq/crawlinghub/domain/useragent/TokenStatus.java`

##### ✅ 우수한 점

1. **Pure Java Enum**
```java
// ✅ Lombok 없음 - 수동 생성자 및 getter
public enum TokenStatus {
    IDLE(1, "유휴"),
    ACTIVE(2, "활성"),
    RATE_LIMITED(3, "속도제한"),
    DISABLED(4, "비활성"),
    RECOVERED(5, "복구됨");

    private final int priority;
    private final String description;

    TokenStatus(int priority, String description) {
        this.priority = priority;
        this.description = description;
    }
}
```

2. **비즈니스 메서드 캡슐화**
```java
// ✅ Tell, Don't Ask
public boolean canMakeRequest() {
    return this == IDLE || this == ACTIVE || this == RECOVERED;
}

public boolean isDisabled() {
    return this == DISABLED;
}

public boolean isRateLimited() {
    return this == RATE_LIMITED;
}
```

3. **안전한 문자열 변환**
```java
// ✅ 명시적 예외 처리
public static TokenStatus fromString(String statusStr) {
    if (statusStr == null || statusStr.isBlank()) {
        throw new IllegalArgumentException("TokenStatus는 필수입니다");
    }

    try {
        return TokenStatus.valueOf(statusStr.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("유효하지 않은 TokenStatus입니다: " + statusStr);
    }
}
```

##### ⚠️ 개선 필요 사항

없음 - 완벽한 구현

---

### 2. Application Layer 분석 (⚠️ Partial - 70%)

#### 2.1 UserAgentPort.java (Interface)

**위치**: `application/src/main/java/com/ryuqq/crawlinghub/application/task/port/out/UserAgentPort.java`

##### ✅ 우수한 점

1. **Port Interface 정의**
```java
// ✅ Port-Adapter 패턴 (Hexagonal Architecture)
public interface UserAgentPort {

    /**
     * User-Agent 선택 (로테이션)
     */
    String selectUserAgent();

    /**
     * User-Agent 블랙리스트 등록
     */
    void blacklist(String userAgent);
}
```

2. **Javadoc 작성**
```java
/**
 * User-Agent 관리 Port
 *
 * <p>크롤링 시 사용할 User-Agent를 선택하고 관리합니다.
 * 탐지 방지를 위해 User-Agent를 로테이션합니다.
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
```

##### ❌ 치명적 문제

1. **구현 Adapter 누락**

현재:
```java
// ❌ UserAgentPort 구현체가 없음
public interface UserAgentPort {
    String selectUserAgent();
    void blacklist(String userAgent);
}

// ❌ adapter-out/persistence-mysql/useragent/adapter/UserAgentPersistenceAdapter.java (없음)
```

필요:
```java
// ✅ 구현 Adapter 필요
@Component
public class UserAgentPersistenceAdapter implements UserAgentPort {

    private final UserAgentJpaRepository jpaRepository;
    private final UserAgentMapper mapper;

    @Override
    public String selectUserAgent() {
        // 1. 사용 가능한 UserAgent 조회 (TokenStatus.canMakeRequest() == true)
        // 2. Random 또는 Round-Robin 선택
        // 3. UserAgent.consumeRequest() 호출
        // 4. 저장
        // 5. userAgentString 반환
    }

    @Override
    public void blacklist(String userAgent) {
        // 1. UserAgent 조회
        // 2. UserAgent.disable() 호출
        // 3. 저장
    }
}
```

2. **CQRS 분리 안 됨**

현재:
```java
// ❌ Command + Query 혼재 (selectUserAgent는 Command + Query)
public interface UserAgentPort {
    String selectUserAgent();  // ⚠️ Query인데 상태 변경 (Command 성격도 있음)
    void blacklist(String userAgent);  // Command
}
```

권장:
```java
// ✅ Command Port
public interface SaveUserAgentPort {
    UserAgent save(UserAgent userAgent);
    void delete(UserAgentId id);
}

// ✅ Query Port
public interface LoadUserAgentPort {
    Optional<UserAgent> findById(UserAgentId id);
    Optional<UserAgent> findAvailableForRotation();
    List<UserAgent> findByStatus(TokenStatus status);
}

// ✅ 별도 UseCase에서 조합
@Service
public class SelectUserAgentService implements SelectUserAgentUseCase {

    private final LoadUserAgentPort loadPort;
    private final SaveUserAgentPort savePort;

    @Override
    @Transactional
    public String execute() {
        // 1. Query - 사용 가능한 UserAgent 조회
        UserAgent userAgent = loadPort.findAvailableForRotation()
            .orElseThrow(() -> new NoAvailableUserAgentException());

        // 2. Command - 요청 소비
        userAgent.consumeRequest();

        // 3. Command - 저장
        savePort.save(userAgent);

        return userAgent.getUserAgentString();
    }
}
```

---

## 우선순위별 개선 계획

### 🔴 HIGH Priority (즉시 구현 필수)

#### 1. Persistence Layer 구현 (필수)

**작업 내용**:
1. **UserAgentEntity.java** 생성
   - Pure Java (Lombok 금지)
   - Long FK 전략 (관계 어노테이션 없음)
   - 3-Constructor 패턴 (no-args, create, reconstitute)

2. **UserAgentJpaRepository.java** 생성
   - JpaRepository 상속
   - Command용 - Query 메서드 금지

3. **UserAgentCommandAdapter.java** 생성 (CQRS - Command)
   - SaveUserAgentPort 구현
   - JpaRepository 사용
   - Domain Model 입출력

4. **UserAgentQueryAdapter.java** 생성 (CQRS - Query)
   - LoadUserAgentPort 구현
   - QueryDSL DTO Projection
   - DTO 직접 반환

5. **UserAgentMapper.java** 생성
   - MapStruct 사용
   - Domain ↔ Entity 변환

**예상 소요 시간**: 6시간

**영향도**: ⭐⭐⭐⭐⭐ (치명적)

**리스크**: 없음

---

#### 2. REST API Layer 구현 (필수)

**작업 내용**:
1. **UserAgentController.java** 생성
   - Thin Controller
   - Pure Java Constructor
   - RESTful API 설계

2. **DTO 생성** (Java Record)
   - IssueTokenRequest
   - RecoverRateLimitRequest
   - DisableUserAgentRequest
   - UserAgentResponse
   - UserAgentListResponse

3. **UserAgentApiMapper.java** 생성
   - @Component
   - Request → Command 변환
   - Domain/DTO → Response 변환

4. **GlobalExceptionHandler 확장**
   - NoAvailableUserAgentException
   - TokenExpiredException
   - RateLimitExceededException

**예상 소요 시간**: 4시간

**영향도**: ⭐⭐⭐⭐⭐ (치명적)

**리스크**: 없음

---

#### 3. Application Layer 완전 구현 (필수)

**작업 내용**:
1. **UseCase 인터페이스 정의**
   - IssueTokenUseCase
   - RecoverRateLimitUseCase
   - DisableUserAgentUseCase
   - GetUserAgentDetailUseCase

2. **UseCase 구현체 생성**
   - IssueTokenService
   - RecoverRateLimitService
   - DisableUserAgentService
   - GetUserAgentDetailService

3. **Command 생성** (Java Record)
   - IssueTokenCommand
   - RecoverRateLimitCommand
   - DisableUserAgentCommand

4. **Query 생성** (Java Record)
   - UserAgentDetailQuery

5. **Port 재정의** (CQRS 분리)
   - SaveUserAgentPort (Command)
   - LoadUserAgentPort (Query)

**예상 소요 시간**: 5시간

**영향도**: ⭐⭐⭐⭐⭐ (치명적)

**리스크**: 없음

---

### 🟡 MEDIUM Priority (1주일 내)

#### 4. Domain Exception 계층 구현

**작업 내용**:
1. **DomainException Sealed Interface 정의**
2. **UserAgentException Sealed Interface 정의**
3. **구체적 Exception 구현**
4. **UserAgent.java 리팩토링**

**예상 소요 시간**: 3시간

---

### 🟢 LOW Priority (2주일 이내)

#### 5. Redis Cache Layer 구현 (선택)
#### 6. Javadoc 보완
#### 7. 통합 테스트 작성

---

## 예상 개선 효과

**구현 완성도**: 27% → **100%** (+73%)
**Zero-Tolerance 준수율**: 27% → **100%** (+73%)
**CQRS 준수율**: 0% → **100%** (+100%)

**총 예상 작업 시간**: 약 28.5시간 (약 3.5일)

---

**보고서 종료**
