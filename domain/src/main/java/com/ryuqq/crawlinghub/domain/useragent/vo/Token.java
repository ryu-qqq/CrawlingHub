package com.ryuqq.crawlinghub.domain.useragent.vo;

import java.util.regex.Pattern;

/**
 * UserAgent Token Value Object
 *
 * <p>AES-256 암호화된 토큰을 저장합니다. 복호화는 금지되며, 암호화된 상태로만 사용됩니다.
 *
 * <p><strong>Lazy Token Issuance 지원</strong>:
 *
 * <ul>
 *   <li>encryptedValue가 null일 수 있음 (토큰 미발급 상태)
 *   <li>{@link #isEmpty()} 메서드로 토큰 발급 여부 확인
 *   <li>{@link #empty()} 팩토리 메서드로 빈 토큰 생성
 * </ul>
 *
 * <p><strong>보안 규칙</strong>:
 *
 * <ul>
 *   <li>복호화 메서드 없음 - 암호화 상태로만 저장/비교
 *   <li>Base64 형식 검증 (토큰이 있는 경우)
 *   <li>최소 길이 44자 (AES-256 암호화 결과)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public record Token(String encryptedValue) {

    private static final Pattern BASE64_PATTERN = Pattern.compile("^[A-Za-z0-9+/=]{44,}$");
    private static final int MIN_LENGTH = 44;
    private static final Token EMPTY = new Token(null);

    /** Compact Constructor (검증 로직) */
    public Token {
        if (encryptedValue != null && !encryptedValue.isBlank()) {
            if (encryptedValue.length() < MIN_LENGTH) {
                throw new IllegalArgumentException(
                        "Token 길이는 최소 " + MIN_LENGTH + "자 이상이어야 합니다: " + encryptedValue.length());
            }
            if (!BASE64_PATTERN.matcher(encryptedValue).matches()) {
                throw new IllegalArgumentException("Token은 유효한 Base64 형식이어야 합니다.");
            }
        }
    }

    /**
     * 암호화된 토큰으로 생성
     *
     * @param encryptedValue AES-256 암호화된 토큰 (Base64)
     * @return Token
     * @throws IllegalArgumentException encryptedValue가 null이거나 빈 문자열인 경우
     */
    public static Token of(String encryptedValue) {
        if (encryptedValue == null || encryptedValue.isBlank()) {
            throw new IllegalArgumentException(
                    "Token.of()는 null이거나 빈 문자열일 수 없습니다. 빈 토큰은 Token.empty()를 사용하세요.");
        }
        return new Token(encryptedValue);
    }

    /**
     * 빈 토큰 생성 (Lazy Token Issuance용)
     *
     * <p>토큰이 아직 발급되지 않은 상태를 나타냅니다.
     *
     * @return 빈 Token
     */
    public static Token empty() {
        return EMPTY;
    }

    /**
     * nullable 값으로부터 Token 생성
     *
     * <p>DB에서 조회 시 null 값을 처리할 때 사용합니다.
     *
     * @param encryptedValue 암호화된 토큰 (nullable)
     * @return Token (null인 경우 빈 Token 반환)
     */
    public static Token ofNullable(String encryptedValue) {
        if (encryptedValue == null || encryptedValue.isBlank()) {
            return EMPTY;
        }
        return new Token(encryptedValue);
    }

    /**
     * 토큰이 발급되지 않은 상태인지 확인
     *
     * @return 토큰이 비어있으면 true
     */
    public boolean isEmpty() {
        return encryptedValue == null || encryptedValue.isBlank();
    }

    /**
     * 토큰이 발급된 상태인지 확인
     *
     * @return 토큰이 있으면 true
     */
    public boolean isPresent() {
        return !isEmpty();
    }
}
