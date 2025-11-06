package com.ryuqq.crawlinghub.adapter.out.persistence.schedule.mapper;

import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.ScheduleOutboxEntity;
import com.ryuqq.crawlinghub.domain.schedule.outbox.ScheduleOutbox;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 셀러 크롤링 스케줄 Outbox Mapper
 * <p>
 * Domain Model ↔ JPA Entity 변환을 담당합니다.
 * </p>
 * <p>
 * 변환 규칙:
 * <ul>
 *   <li>Domain → Entity: toEntity()</li>
 *   <li>Entity → Domain: toDomain()</li>
 *   <li>Enum 매핑: OperationState, WriteAheadState</li>
 *   <li>CommandInfo 매핑: Record ↔ Entity fields</li>
 * </ul>
 * </p>
 *
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
@Component
public class ScheduleOutboxMapper {

    /**
     * Domain → Entity 변환
     *
     * @param domain Domain 모델
     * @return JPA Entity
     */
    public ScheduleOutboxEntity toEntity(ScheduleOutbox domain) {
        Objects.requireNonNull(domain, "domain must not be null");

        // 새로운 Entity 생성 (저장 전)
        // 참고: 이 메서드는 Domain → Entity 변환 시 새로운 Entity를 생성할 때만 사용됩니다.
        // 기존 Entity 업데이트는 JPA의 변경 감지(Dirty Checking)를 사용합니다.
        return new ScheduleOutboxEntity(
                domain.getOpId(),
                domain.getSellerId(),
                domain.getIdemKey(),
                domain.getDomain(),
                domain.getEventType(),
                domain.getBizKey(),
                domain.getPayload(),
                domain.getOutcomeJson(),
                mapOperationState(domain.getOperationState()),
                mapWriteAheadState(domain.getWalState()),
                domain.getErrorMessage(),
                domain.getRetryCount(),
                domain.getMaxRetries(),
                domain.getTimeoutMillis(),
                domain.getCompletedAt()
        );
    }

    /**
     * Entity → Domain 변환
     *
     * @param entity JPA Entity
     * @return Domain 모델
     */
    public ScheduleOutbox toDomain(ScheduleOutboxEntity entity) {
        Objects.requireNonNull(entity, "entity must not be null");

        // Domain 모델 생성 (reconstitute 팩토리 메서드 사용)
        return ScheduleOutbox.reconstitute(
                entity.getId(),
                entity.getOpId(),
                entity.getSellerId(),
                entity.getIdemKey(),
                entity.getDomain(),
                entity.getEventType(),
                entity.getBizKey(),
                entity.getPayload(),
                entity.getOutcomeJson(),
                mapToDomainOperationState(entity.getOperationState()),
                mapToDomainWriteAheadState(entity.getWalState()),
                entity.getErrorMessage(),
                entity.getRetryCount(),
                entity.getMaxRetries(),
                entity.getTimeoutMillis(),
                entity.getCompletedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    // ========================================
    // Private Mapping Methods
    // ========================================

    /**
     * Domain OperationState → Entity OperationState (동일한 타입이므로 변환 불필요)
     */
    private ScheduleOutbox.OperationState mapOperationState(
            ScheduleOutbox.OperationState domainState
    ) {
        if (domainState == null) {
            return ScheduleOutbox.OperationState.PENDING;
        }
        return domainState;
    }

    /**
     * Domain WriteAheadState → Entity WriteAheadState (동일한 타입이므로 변환 불필요)
     */
    private ScheduleOutbox.WriteAheadState mapWriteAheadState(
            ScheduleOutbox.WriteAheadState domainState
    ) {
        if (domainState == null) {
            return ScheduleOutbox.WriteAheadState.PENDING;
        }
        return domainState;
    }

    /**
     * Entity OperationState → Domain OperationState (동일한 타입이므로 변환 불필요)
     */
    private ScheduleOutbox.OperationState mapToDomainOperationState(
            ScheduleOutbox.OperationState entityState
    ) {
        return entityState;
    }

    /**
     * Entity WriteAheadState → Domain WriteAheadState (동일한 타입이므로 변환 불필요)
     */
    private ScheduleOutbox.WriteAheadState mapToDomainWriteAheadState(
            ScheduleOutbox.WriteAheadState entityState
    ) {
        return entityState;
    }
}
