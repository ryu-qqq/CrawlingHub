package com.ryuqq.crawlinghub.domain.token;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 토큰 Value Object
 * <p>
 * 역할:
 * - 토큰 값과 만료 시간을 캡슐화
 * - 토큰 유효성 검증 로직 포함
 * - Immutable 객체 (불변성 보장)
 * </p>
 * <p>
 * ⚠️ Zero-Tolerance 규칙:
 * - No Lombok
 * - Pure Java
 * - Immutable
 * </p>
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
public class Token {

    private final String value;
    private final LocalDateTime issuedAt;
    private final LocalDateTime expiresAt;

    /**
     * Private 생성자 (Factory Method 패턴)
     */
    private Token(String value, LocalDateTime issuedAt, LocalDateTime expiresAt) {
        this.value = value;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
    }

    /**
     * 신규 토큰 생성
     *
     * @param value 토큰 값
     * @param issuedAt 발급 시간
     * @param expiresAt 만료 시간
     * @return Token 인스턴스
     * @throws IllegalArgumentException 유효성 검증 실패 시
     */
    public static Token of(String value, LocalDateTime issuedAt, LocalDateTime expiresAt) {
        validateToken(value, issuedAt, expiresAt);
        return new Token(value, issuedAt, expiresAt);
    }

    /**
     * 유효성 검증
     */
    private static void validateToken(String value, LocalDateTime issuedAt, LocalDateTime expiresAt) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("토큰 값은 필수입니다");
        }
        if (issuedAt == null) {
            throw new IllegalArgumentException("발급 시간은 필수입니다");
        }
        if (expiresAt == null) {
            throw new IllegalArgumentException("만료 시간은 필수입니다");
        }
        if (expiresAt.isBefore(issuedAt)) {
            throw new IllegalArgumentException("만료 시간은 발급 시간 이후여야 합니다");
        }
    }

    /**
     * 토큰 만료 여부 확인
     *
     * @param now 현재 시간
     * @return 만료되었으면 true
     */
    public boolean isExpired(LocalDateTime now) {
        return now.isAfter(expiresAt);
    }

    /**
     * 토큰이 유효한지 확인
     *
     * @param now 현재 시간
     * @return 유효하면 true
     */
    public boolean isValid(LocalDateTime now) {
        return !isExpired(now);
    }

    /**
     * 만료까지 남은 시간 (초)
     *
     * @param now 현재 시간
     * @return 남은 시간 (음수면 이미 만료)
     */
    public long remainingSeconds(LocalDateTime now) {
        return java.time.Duration.between(now, expiresAt).getSeconds();
    }

    /**
     * 토큰 값 반환
     *
     * @return 토큰 값
     */
    public String getValue() {
        return value;
    }

    /**
     * 발급 시간 반환
     *
     * @return 발급 시간
     */
    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    /**
     * 만료 시간 반환
     *
     * @return 만료 시간
     */
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Token token = (Token) o;
        return Objects.equals(value, token.value) &&
               Objects.equals(issuedAt, token.issuedAt) &&
               Objects.equals(expiresAt, token.expiresAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, issuedAt, expiresAt);
    }

    @Override
    public String toString() {
        return "Token{" +
                "value='" + maskToken(value) + '\'' +
                ", issuedAt=" + issuedAt +
                ", expiresAt=" + expiresAt +
                '}';
    }

    /**
     * 토큰 값 마스킹 (로깅용)
     */
    private String maskToken(String token) {
        if (token == null || token.length() < 8) {
            return "***";
        }
        return token.substring(0, 4) + "..." + token.substring(token.length() - 4);
    }
}
