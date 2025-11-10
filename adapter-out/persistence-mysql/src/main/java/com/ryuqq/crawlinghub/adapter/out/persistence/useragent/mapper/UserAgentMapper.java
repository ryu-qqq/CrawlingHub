package com.ryuqq.crawlinghub.adapter.out.persistence.useragent.mapper;

import com.ryuqq.crawlinghub.adapter.out.persistence.useragent.entity.UserAgentEntity;
import com.ryuqq.crawlinghub.domain.token.Token;
import com.ryuqq.crawlinghub.domain.useragent.UserAgent;
import com.ryuqq.crawlinghub.domain.useragent.UserAgentId;

import org.springframework.stereotype.Component;

/**
 * UserAgent Aggregate와 UserAgentEntity 간 변환을 담당하는 Mapper
 * <p>
 * ⭐ Domain 중심 설계:
 * - Token VO ↔ String 변환
 * - Token 발급 시간 처리
 * </p>
 * <p>
 * Domain 객체와 Persistence Entity 간의 양방향 변환을 제공합니다.
 * </p>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@Component
public class UserAgentMapper {

    /**
     * Domain Aggregate → JPA Entity 변환
     * <p>
     * ⭐ Token VO → String 변환
     * </p>
     *
     * @param userAgent Domain Aggregate
     * @return JPA Entity
     * @throws IllegalArgumentException userAgent가 null인 경우
     */
    public UserAgentEntity toEntity(UserAgent userAgent) {
        if (userAgent == null) {
            throw new IllegalArgumentException("userAgent must not be null");
        }

        // ⭐ Token VO → String 변환
        Token token = userAgent.getCurrentToken();
        String tokenString = (token != null) ? token.getValue() : null;
        java.time.LocalDateTime tokenIssuedAt = (token != null) ? token.getIssuedAt() : null;

        // Entity ID가 있으면 reconstitute, 없으면 create
        if (userAgent.getIdValue() != null) {
            return UserAgentEntity.reconstitute(
                    userAgent.getIdValue(),
                    userAgent.getUserAgentString(),
                    tokenString,
                    userAgent.getTokenStatus(),
                    userAgent.getRemainingRequests(),
                    tokenIssuedAt,
                    userAgent.getRateLimitResetAt()
            );
        } else {
            return UserAgentEntity.create(
                    userAgent.getUserAgentString(),
                    tokenString,
                    userAgent.getTokenStatus(),
                    userAgent.getRemainingRequests(),
                    tokenIssuedAt,
                    userAgent.getRateLimitResetAt()
            );
        }
    }

    /**
     * JPA Entity → Domain Aggregate 변환
     * <p>
     * ⭐ String → Token VO 변환
     * </p>
     *
     * @param entity JPA Entity
     * @return Domain Aggregate
     * @throws IllegalArgumentException entity가 null인 경우
     */
    public UserAgent toDomain(UserAgentEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity must not be null");
        }

        // ⭐ String → Token VO 변환
        Token token = null;
        if (entity.getCurrentToken() != null && entity.getTokenIssuedAt() != null) {
            // Token VO 생성 (만료 시간 = 발급 시간 + 24시간)
            token = Token.of(
                entity.getCurrentToken(),
                entity.getTokenIssuedAt(),
                entity.getTokenIssuedAt().plusHours(24)  // 24시간 유효
            );
        }

        return UserAgent.reconstitute(
                UserAgentId.of(entity.getId()),
                entity.getUserAgentString(),
                token,  // ⭐ Token VO
                entity.getTokenStatus(),
                entity.getRemainingRequests(),
                entity.getRateLimitResetAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}



