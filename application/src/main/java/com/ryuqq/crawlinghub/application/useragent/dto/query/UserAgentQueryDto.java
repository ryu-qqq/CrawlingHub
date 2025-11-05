package com.ryuqq.crawlinghub.application.useragent.dto.query;

import com.ryuqq.crawlinghub.domain.useragent.TokenStatus;

import java.time.LocalDateTime;

/**
 * UserAgent Query DTO - Query 전용 DTO
 *
 * <p><strong>CQRS 패턴 적용 - Query 결과 전용 ⭐</strong></p>
 * <ul>
 *   <li>✅ QueryDSL DTO Projection으로 직접 생성</li>
 *   <li>✅ Domain Model을 거치지 않음 (성능 향상)</li>
 *   <li>✅ N+1 문제 방지</li>
 * </ul>
 *
 * <p><strong>사용처:</strong></p>
 * <ul>
 *   <li>LoadUserAgentPort의 findById, findAvailableForRotation 메서드 반환 타입</li>
 *   <li>QueryAdapter에서 QueryDSL Projection으로 직접 생성</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
public record UserAgentQueryDto(
    Long id,
    String userAgentString,
    String currentToken,
    TokenStatus tokenStatus,
    Integer remainingRequests,
    LocalDateTime tokenIssuedAt,
    LocalDateTime rateLimitResetAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}

