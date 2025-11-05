package com.ryuqq.crawlinghub.adapter.out.persistence.useragent.mapper;

import com.ryuqq.crawlinghub.adapter.out.persistence.useragent.entity.UserAgentEntity;
import com.ryuqq.crawlinghub.domain.useragent.TokenStatus;
import com.ryuqq.crawlinghub.domain.useragent.UserAgent;
import com.ryuqq.crawlinghub.domain.useragent.UserAgentId;

import org.springframework.stereotype.Component;

/**
 * UserAgent Aggregate와 UserAgentEntity 간 변환을 담당하는 Mapper
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
     *
     * @param userAgent Domain Aggregate
     * @return JPA Entity
     * @throws IllegalArgumentException userAgent가 null인 경우
     */
    public UserAgentEntity toEntity(UserAgent userAgent) {
        if (userAgent == null) {
            throw new IllegalArgumentException("userAgent must not be null");
        }

        // Entity ID가 있으면 reconstitute, 없으면 create
        if (userAgent.getIdValue() != null) {
            return UserAgentEntity.reconstitute(
                    userAgent.getIdValue(),
                    userAgent.getUserAgentString(),
                    userAgent.getCurrentToken(),
                    userAgent.getTokenStatus(),
                    userAgent.getRemainingRequests(),
                    userAgent.getTokenIssuedAt(),
                    userAgent.getRateLimitResetAt()
            );
        } else {
            return UserAgentEntity.create(
                    userAgent.getUserAgentString(),
                    userAgent.getCurrentToken(),
                    userAgent.getTokenStatus(),
                    userAgent.getRemainingRequests(),
                    userAgent.getTokenIssuedAt(),
                    userAgent.getRateLimitResetAt()
            );
        }
    }

    /**
     * JPA Entity → Domain Aggregate 변환
     *
     * @param entity JPA Entity
     * @return Domain Aggregate
     * @throws IllegalArgumentException entity가 null인 경우
     */
    public UserAgent toDomain(UserAgentEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity must not be null");
        }

        return UserAgent.reconstitute(
                UserAgentId.of(entity.getId()),
                entity.getUserAgentString(),
                entity.getCurrentToken(),
                entity.getTokenStatus(),
                entity.getRemainingRequests(),
                entity.getTokenIssuedAt(),
                entity.getRateLimitResetAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}

