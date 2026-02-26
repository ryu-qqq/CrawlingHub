package com.ryuqq.crawlinghub.adapter.out.persistence.useragent.mapper;

import com.ryuqq.crawlinghub.adapter.out.persistence.useragent.entity.UserAgentJpaEntity;
import com.ryuqq.crawlinghub.domain.useragent.aggregate.UserAgent;
import com.ryuqq.crawlinghub.domain.useragent.id.UserAgentId;
import com.ryuqq.crawlinghub.domain.useragent.vo.BrowserType;
import com.ryuqq.crawlinghub.domain.useragent.vo.CooldownPolicy;
import com.ryuqq.crawlinghub.domain.useragent.vo.DeviceBrand;
import com.ryuqq.crawlinghub.domain.useragent.vo.DeviceType;
import com.ryuqq.crawlinghub.domain.useragent.vo.HealthScore;
import com.ryuqq.crawlinghub.domain.useragent.vo.OsType;
import com.ryuqq.crawlinghub.domain.useragent.vo.Token;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentMetadata;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentString;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.stereotype.Component;

/**
 * UserAgentJpaEntityMapper - Entity ↔ Domain 변환 Mapper
 *
 * <p>Persistence Layer의 JPA Entity와 Domain Layer의 Domain 객체 간 변환을 담당합니다.
 *
 * <p><strong>변환 책임:</strong>
 *
 * <ul>
 *   <li>UserAgent → UserAgentJpaEntity (저장용)
 *   <li>UserAgentJpaEntity → UserAgent (조회용)
 *   <li>Value Object 추출 및 재구성
 * </ul>
 *
 * <p><strong>참고:</strong> token, lastUsedAt, requestsPerDay는 Redis에서만 관리하므로 DB Entity에 포함하지 않습니다.
 * Domain 복원 시 기본값(Token.empty(), null, 0)을 사용합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class UserAgentJpaEntityMapper {

    /**
     * Domain → Entity 변환
     *
     * @param domain UserAgent 도메인
     * @return UserAgentJpaEntity
     */
    public UserAgentJpaEntity toEntity(UserAgent domain) {
        UserAgentMetadata metadata = domain.getMetadata();
        CooldownPolicy cooldownPolicy = domain.getCooldownPolicy();
        return UserAgentJpaEntity.of(
                domain.getIdValue(),
                domain.getUserAgentStringValue(),
                domain.getDeviceType().getTypeName(),
                metadata.getDeviceBrand().name(),
                metadata.getOsType().name(),
                metadata.getOsVersion(),
                metadata.getBrowserType().name(),
                metadata.getBrowserVersion(),
                domain.getStatus(),
                domain.getHealthScoreValue(),
                toLocalDateTime(cooldownPolicy.cooldownUntil()),
                cooldownPolicy.consecutiveRateLimits(),
                toLocalDateTime(domain.getCreatedAt()),
                toLocalDateTime(domain.getUpdatedAt()));
    }

    /**
     * Entity → Domain 변환
     *
     * <p>token, lastUsedAt, requestsPerDay는 DB에 저장하지 않으므로 기본값으로 복원합니다. 이 필드들은 Redis에서 관리되며, Redis
     * 어댑터를 통해 별도로 로드됩니다.
     *
     * @param entity UserAgentJpaEntity
     * @return UserAgent 도메인
     */
    public UserAgent toDomain(UserAgentJpaEntity entity) {
        UserAgentMetadata metadata =
                UserAgentMetadata.of(
                        DeviceBrand.valueOf(entity.getDeviceBrand()),
                        OsType.valueOf(entity.getOsType()),
                        entity.getOsVersion(),
                        BrowserType.valueOf(entity.getBrowserType()),
                        entity.getBrowserVersion());

        CooldownPolicy cooldownPolicy =
                CooldownPolicy.reconstitute(
                        entity.getConsecutiveRateLimits(), toInstant(entity.getCooldownUntil()));

        return UserAgent.reconstitute(
                UserAgentId.of(entity.getId()),
                Token.empty(),
                UserAgentString.of(entity.getUserAgentString()),
                DeviceType.of(entity.getDeviceType()),
                metadata,
                entity.getStatus(),
                HealthScore.of(entity.getHealthScore()),
                cooldownPolicy,
                null,
                0,
                toInstant(entity.getCreatedAt()),
                toInstant(entity.getUpdatedAt()));
    }

    private LocalDateTime toLocalDateTime(Instant instant) {
        if (instant == null) {
            return null;
        }
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    private Instant toInstant(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant();
    }
}
