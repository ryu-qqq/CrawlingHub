package com.ryuqq.crawlinghub.domain.crawler.vo;

/**
 * Token - UserAgent 인증 토큰 Value Object
 *
 * <p>UserAgent가 Mustit API를 호출할 때 사용하는 인증 토큰입니다.</p>
 *
 * <p><strong>검증 규칙:</strong></p>
 * <ul>
 *   <li>✅ null 금지</li>
 *   <li>✅ 빈 문자열 금지</li>
 *   <li>✅ 공백만 있는 문자열 금지</li>
 * </ul>
 *
 * <p><strong>Zero-Tolerance 규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Lombok 금지 (Record 패턴 사용)</li>
 *   <li>✅ 불변성 (Immutable)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
public record Token(String value) {

    /**
     * Compact Constructor - Token 생성 시 검증
     *
     * <p>Token이 null이거나 빈 문자열인지 검증합니다.</p>
     *
     * @throws IllegalArgumentException Token이 null이거나 빈 문자열일 때
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public Token {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Token은 비어있을 수 없습니다");
        }
    }
}
