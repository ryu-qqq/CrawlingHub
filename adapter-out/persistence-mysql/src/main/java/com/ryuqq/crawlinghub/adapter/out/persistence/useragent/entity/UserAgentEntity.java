package com.ryuqq.crawlinghub.adapter.out.persistence.useragent.entity;

import com.ryuqq.crawlinghub.adapter.out.persistence.common.entity.BaseAuditEntity;
import com.ryuqq.crawlinghub.domain.useragent.TokenStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * UserAgent JPA Entity
 * <p>
 * Long FK 전략을 사용하여 관계 어노테이션을 배제하고
 * Long 타입의 FK만 사용합니다.
 * </p>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@Entity
@Table(
        name = "user_agent",
        indexes = {
                @Index(name = "idx_token_status", columnList = "token_status"),
                @Index(name = "idx_remaining_requests", columnList = "remaining_requests"),
                @Index(name = "idx_rate_limit_reset_at", columnList = "rate_limit_reset_at")
        }
)
public class UserAgentEntity extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_agent_string", nullable = false, length = 500)
    private String userAgentString;

    @Column(name = "current_token", length = 500)
    private String currentToken;

    @Column(name = "token_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TokenStatus tokenStatus;

    @Column(name = "remaining_requests", nullable = false)
    private Integer remainingRequests;

    @Column(name = "token_issued_at")
    private LocalDateTime tokenIssuedAt;

    @Column(name = "rate_limit_reset_at")
    private LocalDateTime rateLimitResetAt;

    /**
     * JPA 기본 생성자 (protected)
     */
    protected UserAgentEntity() {
        super();
    }

    /**
     * 전체 필드 생성자 (private)
     * <p>
     * Static Factory Method에서만 호출됩니다.
     * </p>
     *
     * @param id                  PK ID
     * @param userAgentString     User-Agent 문자열
     * @param currentToken        현재 토큰
     * @param tokenStatus         토큰 상태
     * @param remainingRequests   남은 요청 수
     * @param tokenIssuedAt       토큰 발급 시간
     * @param rateLimitResetAt    Rate Limit 리셋 시간
     */
    private UserAgentEntity(
            Long id,
            String userAgentString,
            String currentToken,
            TokenStatus tokenStatus,
            Integer remainingRequests,
            LocalDateTime tokenIssuedAt,
            LocalDateTime rateLimitResetAt
    ) {
        super();
        this.id = id;
        this.userAgentString = userAgentString;
        this.currentToken = currentToken;
        this.tokenStatus = tokenStatus;
        this.remainingRequests = remainingRequests;
        this.tokenIssuedAt = tokenIssuedAt;
        this.rateLimitResetAt = rateLimitResetAt;
    }

    /**
     * 신규 Entity 생성 (PK 없음)
     * <p>
     * 새로운 UserAgent를 생성할 때 사용합니다.
     * PK는 JPA가 자동으로 생성합니다.
     * </p>
     *
     * @param userAgentString     User-Agent 문자열
     * @param currentToken        현재 토큰
     * @param tokenStatus         토큰 상태
     * @param remainingRequests   남은 요청 수
     * @param tokenIssuedAt       토큰 발급 시간
     * @param rateLimitResetAt    Rate Limit 리셋 시간
     * @return 생성된 Entity
     * @throws IllegalArgumentException 파라미터 검증 실패 시
     */
    public static UserAgentEntity create(
            String userAgentString,
            String currentToken,
            TokenStatus tokenStatus,
            Integer remainingRequests,
            LocalDateTime tokenIssuedAt,
            LocalDateTime rateLimitResetAt
    ) {
        validateUserAgentString(userAgentString);
        validateTokenStatus(tokenStatus);
        validateRemainingRequests(remainingRequests);

        return new UserAgentEntity(
                null,
                userAgentString,
                currentToken,
                tokenStatus,
                remainingRequests,
                tokenIssuedAt,
                rateLimitResetAt
        );
    }

    /**
     * 기존 Entity 재구성 (조회 후 복원)
     * <p>
     * DB에서 조회된 데이터로 Entity를 재구성할 때 사용합니다.
     * </p>
     *
     * @param id                  PK ID
     * @param userAgentString     User-Agent 문자열
     * @param currentToken        현재 토큰
     * @param tokenStatus         토큰 상태
     * @param remainingRequests   남은 요청 수
     * @param tokenIssuedAt       토큰 발급 시간
     * @param rateLimitResetAt    Rate Limit 리셋 시간
     * @return 재구성된 Entity
     */
    public static UserAgentEntity reconstitute(
            Long id,
            String userAgentString,
            String currentToken,
            TokenStatus tokenStatus,
            Integer remainingRequests,
            LocalDateTime tokenIssuedAt,
            LocalDateTime rateLimitResetAt
    ) {
        return new UserAgentEntity(
                id,
                userAgentString,
                currentToken,
                tokenStatus,
                remainingRequests,
                tokenIssuedAt,
                rateLimitResetAt
        );
    }

    /**
     * userAgentString 유효성 검증
     *
     * @param userAgentString 검증할 User-Agent 문자열
     * @throws IllegalArgumentException userAgentString이 null이거나 빈 문자열이거나 길이 초과인 경우
     */
    private static void validateUserAgentString(String userAgentString) {
        if (userAgentString == null || userAgentString.isBlank()) {
            throw new IllegalArgumentException("userAgentString must not be null or blank");
        }
        if (userAgentString.length() > 500) {
            throw new IllegalArgumentException("userAgentString must not exceed 500 characters");
        }
    }

    /**
     * tokenStatus 유효성 검증
     *
     * @param tokenStatus 검증할 토큰 상태
     * @throws IllegalArgumentException tokenStatus가 null인 경우
     */
    private static void validateTokenStatus(TokenStatus tokenStatus) {
        if (tokenStatus == null) {
            throw new IllegalArgumentException("tokenStatus must not be null");
        }
    }

    /**
     * remainingRequests 유효성 검증
     *
     * @param remainingRequests 검증할 남은 요청 수
     * @throws IllegalArgumentException remainingRequests가 null이거나 음수인 경우
     */
    private static void validateRemainingRequests(Integer remainingRequests) {
        if (remainingRequests == null) {
            throw new IllegalArgumentException("remainingRequests must not be null");
        }
        if (remainingRequests < 0) {
            throw new IllegalArgumentException("remainingRequests must not be negative");
        }
    }

    public Long getId() {
        return id;
    }

    public String getUserAgentString() {
        return userAgentString;
    }

    public String getCurrentToken() {
        return currentToken;
    }

    public TokenStatus getTokenStatus() {
        return tokenStatus;
    }

    public Integer getRemainingRequests() {
        return remainingRequests;
    }

    public LocalDateTime getTokenIssuedAt() {
        return tokenIssuedAt;
    }

    public LocalDateTime getRateLimitResetAt() {
        return rateLimitResetAt;
    }
}



