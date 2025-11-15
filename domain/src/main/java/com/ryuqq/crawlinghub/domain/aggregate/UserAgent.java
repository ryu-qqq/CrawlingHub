package com.ryuqq.crawlinghub.domain.aggregate;

import com.ryuqq.crawlinghub.domain.vo.UserAgentId;
import com.ryuqq.crawlinghub.domain.vo.UserAgentStatus;

import java.time.LocalDateTime;

/**
 * UserAgent Aggregate Root
 *
 * <p>머스트잇 크롤링에 사용되는 User Agent를 관리하는 Aggregate Root입니다.</p>
 *
 * <p>Zero-Tolerance Rules 준수:</p>
 * <ul>
 *   <li>Lombok 금지 - Plain Java 사용</li>
 *   <li>Tell, Don't Ask - 비즈니스 로직은 UserAgent 내부에 캡슐화</li>
 *   <li>Long FK 전략 - JPA 관계 어노테이션 없음</li>
 * </ul>
 *
 * <p>비즈니스 규칙:</p>
 * <ul>
 *   <li>생성 시 상태는 항상 ACTIVE</li>
 *   <li>생성 시 requestCount는 0</li>
 *   <li>token은 생성 시점에는 null (별도 발급 필요)</li>
 * </ul>
 */
public class UserAgent {

    private final UserAgentId userAgentId;
    private final String userAgentString;
    private String token;
    private UserAgentStatus status;
    private Integer requestCount;
    private LocalDateTime lastRequestAt;
    private LocalDateTime tokenIssuedAt;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Private constructor - 정적 팩토리 메서드를 통해서만 생성
     *
     * @param userAgentString User Agent 문자열
     */
    private UserAgent(String userAgentString) {
        if (userAgentString == null || userAgentString.isBlank()) {
            throw new IllegalArgumentException("UserAgent 문자열은 비어있을 수 없습니다");
        }
        this.userAgentId = UserAgentId.generate();
        this.userAgentString = userAgentString;
        this.status = UserAgentStatus.ACTIVE;
        this.requestCount = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 새로운 UserAgent 생성
     *
     * <p>비즈니스 규칙:</p>
     * <ul>
     *   <li>초기 상태: ACTIVE</li>
     *   <li>초기 requestCount: 0</li>
     *   <li>token은 null (별도 발급 필요)</li>
     * </ul>
     *
     * @param userAgentString User Agent 문자열
     * @return 새로 생성된 UserAgent
     * @throws IllegalArgumentException userAgentString이 null 또는 blank인 경우
     */
    public static UserAgent create(String userAgentString) {
        return new UserAgent(userAgentString);
    }

    // Getters (필요한 것만)
    public UserAgentId getUserAgentId() {
        return userAgentId;
    }

    public String getUserAgentString() {
        return userAgentString;
    }

    public String getToken() {
        return token;
    }

    public UserAgentStatus getStatus() {
        return status;
    }

    public Integer getRequestCount() {
        return requestCount;
    }
}
