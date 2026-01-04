package com.ryuqq.crawlinghub.adapter.out.persistence.useragent.mapper;

import com.ryuqq.crawlinghub.adapter.out.persistence.useragent.entity.UserAgentJpaEntity;
import com.ryuqq.crawlinghub.domain.useragent.aggregate.UserAgent;
import com.ryuqq.crawlinghub.domain.useragent.identifier.UserAgentId;
import com.ryuqq.crawlinghub.domain.useragent.vo.BrowserType;
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
 * <p><strong>Hexagonal Architecture 관점:</strong>
 *
 * <ul>
 *   <li>Adapter Layer의 책임
 *   <li>Domain과 Infrastructure 기술 분리
 *   <li>Domain은 JPA 의존성 없음
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class UserAgentJpaEntityMapper {

    /**
     * Domain → Entity 변환
     *
     * <p><strong>사용 시나리오:</strong>
     *
     * <ul>
     *   <li>신규 UserAgent 저장 (ID가 null)
     *   <li>기존 UserAgent 수정 (ID가 있음)
     * </ul>
     *
     * <p><strong>변환 규칙:</strong>
     *
     * <ul>
     *   <li>ID: Domain.getId().value() → Entity.id (null 가능)
     *   <li>Token: Domain.getToken().encryptedValue() → Entity.token
     *   <li>DeviceType: Domain.getDeviceType().getTypeName() → Entity.deviceType
     *   <li>Metadata: Domain.getMetadata() → Entity.deviceBrand, osType, osVersion, browserType,
     *       browserVersion
     *   <li>Status: Domain.getStatus() → Entity.status
     *   <li>HealthScore: Domain.getHealthScoreValue() → Entity.healthScore
     *   <li>LastUsedAt: Domain.getLastUsedAt() → Entity.lastUsedAt (null 가능)
     *   <li>RequestsPerDay: Domain.getRequestsPerDay() → Entity.requestsPerDay
     *   <li>CreatedAt: Domain.getCreatedAt() → Entity.createdAt
     *   <li>UpdatedAt: Domain.getUpdatedAt() → Entity.updatedAt
     * </ul>
     *
     * @param domain UserAgent 도메인
     * @return UserAgentJpaEntity
     */
    public UserAgentJpaEntity toEntity(UserAgent domain) {
        UserAgentMetadata metadata = domain.getMetadata();
        Token token = domain.getToken();
        return UserAgentJpaEntity.of(
                domain.getId().value(),
                token != null ? token.encryptedValue() : null,
                domain.getUserAgentString().value(),
                domain.getDeviceType().getTypeName(),
                metadata.getDeviceBrand().name(),
                metadata.getOsType().name(),
                metadata.getOsVersion(),
                metadata.getBrowserType().name(),
                metadata.getBrowserVersion(),
                domain.getStatus(),
                domain.getHealthScoreValue(),
                toLocalDateTime(domain.getLastUsedAt()),
                domain.getRequestsPerDay(),
                toLocalDateTime(domain.getCreatedAt()),
                toLocalDateTime(domain.getUpdatedAt()));
    }

    /**
     * Entity → Domain 변환
     *
     * <p><strong>사용 시나리오:</strong>
     *
     * <ul>
     *   <li>데이터베이스에서 조회한 Entity를 Domain으로 변환
     *   <li>Application Layer로 전달
     * </ul>
     *
     * <p><strong>변환 규칙:</strong>
     *
     * <ul>
     *   <li>ID: Entity.id → Domain.UserAgentId
     *   <li>Token: Entity.token → Domain.Token
     *   <li>DeviceType: Entity.deviceType → Domain.DeviceType
     *   <li>Metadata: Entity.deviceBrand, osType, osVersion, browserType, browserVersion →
     *       Domain.UserAgentMetadata
     *   <li>Status: Entity.status → Domain.UserAgentStatus
     *   <li>HealthScore: Entity.healthScore → Domain.HealthScore
     *   <li>LastUsedAt: Entity.lastUsedAt → Domain.lastUsedAt
     *   <li>RequestsPerDay: Entity.requestsPerDay → Domain.requestsPerDay
     *   <li>CreatedAt/UpdatedAt: Entity → Domain
     * </ul>
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

        return UserAgent.reconstitute(
                UserAgentId.of(entity.getId()),
                Token.ofNullable(entity.getToken()),
                UserAgentString.of(entity.getUserAgentString()),
                DeviceType.of(entity.getDeviceType()),
                metadata,
                entity.getStatus(),
                HealthScore.of(entity.getHealthScore()),
                toInstant(entity.getLastUsedAt()),
                entity.getRequestsPerDay(),
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
