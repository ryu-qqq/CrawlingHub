package com.ryuqq.crawlinghub.application.useragent.assembler;

import com.ryuqq.crawlinghub.application.useragent.dto.query.UserAgentQueryDto;
import com.ryuqq.crawlinghub.application.useragent.dto.response.UserAgentResponse;
import com.ryuqq.crawlinghub.domain.token.Token;
import com.ryuqq.crawlinghub.domain.useragent.UserAgent;
import com.ryuqq.crawlinghub.domain.useragent.UserAgentId;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * UserAgent Assembler
 *
 * <p>Domain 객체와 DTO 간 변환을 담당합니다.
 * Law of Demeter를 준수하여 직접적인 getter 체이닝을 피합니다.
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@Component
public class UserAgentAssembler {

    /**
     * Domain → Response DTO 변환
     *
     * @param userAgent 도메인 UserAgent 객체 (null 불가)
     * @return UserAgentResponse
     * @throws IllegalArgumentException userAgent가 null인 경우
     */
    public static UserAgentResponse toResponse(UserAgent userAgent) {
        if (userAgent == null) {
            throw new IllegalArgumentException("userAgent must not be null");
        }

        return new UserAgentResponse(
            userAgent.getIdValue(),
            userAgent.getUserAgentString(),
            userAgent.getTokenStatus(),
            userAgent.getRemainingRequests(),
            userAgent.getCurrentToken() != null ? userAgent.getCurrentToken().getIssuedAt() : null,
            userAgent.getRateLimitResetAt(),
            userAgent.getCreatedAt(),
            userAgent.getUpdatedAt()
        );
    }

    /**
     * QueryDto → Domain Model 변환
     *
     * <p>Query Port에서 반환된 DTO를 Domain Model로 변환합니다.
     * 비즈니스 로직이 필요한 경우에만 사용합니다.</p>
     *
     * @param dto UserAgent Query DTO (null 불가)
     * @return UserAgent Domain Model
     * @throws IllegalArgumentException dto가 null인 경우
     */
    public UserAgent toDomain(UserAgentQueryDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("dto must not be null");
        }

        Token token = null;
        if (dto.currentToken() != null && dto.tokenIssuedAt() != null) {
            LocalDateTime expiresAt = dto.tokenIssuedAt().plusHours(24);
            token = Token.of(dto.currentToken(), dto.tokenIssuedAt(), expiresAt);
        }

        return UserAgent.reconstitute(
            UserAgentId.of(dto.id()),
            dto.userAgentString(),
            token,
            dto.tokenStatus(),
            dto.remainingRequests(),
            dto.rateLimitResetAt(),
            dto.createdAt(),
            dto.updatedAt()
        );
    }

    /**
     * QueryDto → Response DTO 변환
     *
     * @param dto UserAgent Query DTO (null 불가)
     * @return UserAgentResponse
     * @throws IllegalArgumentException dto가 null인 경우
     */
    public static UserAgentResponse toResponse(UserAgentQueryDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("dto must not be null");
        }

        return new UserAgentResponse(
            dto.id(),
            dto.userAgentString(),
            dto.tokenStatus(),
            dto.remainingRequests(),
            dto.tokenIssuedAt(),
            dto.rateLimitResetAt(),
            dto.createdAt(),
            dto.updatedAt()
        );
    }
}

