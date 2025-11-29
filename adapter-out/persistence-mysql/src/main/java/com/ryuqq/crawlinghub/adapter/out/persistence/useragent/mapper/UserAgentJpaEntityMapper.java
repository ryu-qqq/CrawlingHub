package com.ryuqq.crawlinghub.adapter.out.persistence.useragent.mapper;

import com.ryuqq.crawlinghub.adapter.out.persistence.useragent.entity.UserAgentJpaEntity;
import com.ryuqq.crawlinghub.domain.useragent.aggregate.UserAgent;
import com.ryuqq.crawlinghub.domain.useragent.identifier.UserAgentId;
import com.ryuqq.crawlinghub.domain.useragent.vo.DeviceType;
import com.ryuqq.crawlinghub.domain.useragent.vo.HealthScore;
import com.ryuqq.crawlinghub.domain.useragent.vo.Token;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentString;
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
        return UserAgentJpaEntity.of(
                domain.getId().value(),
                domain.getToken().encryptedValue(),
                domain.getUserAgentString().value(),
                domain.getDeviceType().getTypeName(),
                domain.getStatus(),
                domain.getHealthScoreValue(),
                domain.getLastUsedAt(),
                domain.getRequestsPerDay(),
                domain.getCreatedAt(),
                domain.getUpdatedAt());
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
        return UserAgent.reconstitute(
                UserAgentId.of(entity.getId()),
                Token.of(entity.getToken()),
                UserAgentString.of(entity.getUserAgentString()),
                DeviceType.of(entity.getDeviceType()),
                entity.getStatus(),
                HealthScore.of(entity.getHealthScore()),
                entity.getLastUsedAt(),
                entity.getRequestsPerDay(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
