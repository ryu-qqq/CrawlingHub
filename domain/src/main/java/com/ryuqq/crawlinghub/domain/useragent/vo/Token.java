package com.ryuqq.crawlinghub.domain.useragent.vo;

import java.util.regex.Pattern;

/**
 * UserAgent Token Value Object
 *
 * <p>AES-256 암호화된 토큰을 저장합니다. 복호화는 금지되며, 암호화된 상태로만 사용됩니다.
 *
 * <p><strong>보안 규칙</strong>:
 *
 * <ul>
 *   <li>복호화 메서드 없음 - 암호화 상태로만 저장/비교
 *   <li>Base64 형식 검증
 *   <li>최소 길이 44자 (AES-256 암호화 결과)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public record Token(String encryptedValue) {

    private static final Pattern BASE64_PATTERN = Pattern.compile("^[A-Za-z0-9+/=]{44,}$");
    private static final int MIN_LENGTH = 44;

    /** Compact Constructor (검증 로직) */
    public Token {
        if (encryptedValue == null || encryptedValue.isBlank()) {
            throw new IllegalArgumentException("Token은 null이거나 빈 문자열일 수 없습니다.");
        }
        if (encryptedValue.length() < MIN_LENGTH) {
            throw new IllegalArgumentException(
                    "Token 길이는 최소 " + MIN_LENGTH + "자 이상이어야 합니다: " + encryptedValue.length());
        }
        if (!BASE64_PATTERN.matcher(encryptedValue).matches()) {
            throw new IllegalArgumentException("Token은 유효한 Base64 형식이어야 합니다.");
        }
    }

    /**
     * 암호화된 토큰으로 생성
     *
     * @param encryptedValue AES-256 암호화된 토큰 (Base64)
     * @return Token
     */
    public static Token of(String encryptedValue) {
        return new Token(encryptedValue);
    }
}
