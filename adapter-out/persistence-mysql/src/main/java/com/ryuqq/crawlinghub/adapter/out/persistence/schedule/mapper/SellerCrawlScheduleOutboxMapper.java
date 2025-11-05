package com.ryuqq.crawlinghub.adapter.out.persistence.schedule.mapper;

import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.SellerCrawlScheduleOutboxEntity;
import com.ryuqq.crawlinghub.domain.schedule.outbox.SellerCrawlScheduleOutbox;
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
public class SellerCrawlScheduleOutboxMapper {

    /**
     * Domain → Entity 변환
     *
     * @param domain Domain 모델
     * @return JPA Entity
     */
    public SellerCrawlScheduleOutboxEntity toEntity(SellerCrawlScheduleOutbox domain) {
        Objects.requireNonNull(domain, "domain must not be null");

        // 새로운 Entity 생성 (저장 전)
        // 참고: 이 메서드는 Domain → Entity 변환 시 새로운 Entity를 생성할 때만 사용됩니다.
        // 기존 Entity 업데이트는 JPA의 변경 감지(Dirty Checking)를 사용합니다.
        return new SellerCrawlScheduleOutboxEntity(
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
    public SellerCrawlScheduleOutbox toDomain(SellerCrawlScheduleOutboxEntity entity) {
        Objects.requireNonNull(entity, "entity must not be null");

        // Domain 모델 생성 (reconstitute 팩토리 메서드 사용)
        return SellerCrawlScheduleOutbox.reconstitute(
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
     * Domain OperationState → Entity OperationState
     */
    private SellerCrawlScheduleOutboxEntity.OperationState mapOperationState(
            SellerCrawlScheduleOutbox.OperationState domainState
    ) {
        if (domainState == null) {
            return SellerCrawlScheduleOutboxEntity.OperationState.PENDING;
        }
        return switch (domainState) {
            case PENDING -> SellerCrawlScheduleOutboxEntity.OperationState.PENDING;
            case IN_PROGRESS -> SellerCrawlScheduleOutboxEntity.OperationState.IN_PROGRESS;
            case COMPLETED -> SellerCrawlScheduleOutboxEntity.OperationState.COMPLETED;
            case FAILED -> SellerCrawlScheduleOutboxEntity.OperationState.FAILED;
        };
    }

    /**
     * Domain WriteAheadState → Entity WriteAheadState
     */
    private SellerCrawlScheduleOutboxEntity.WriteAheadState mapWriteAheadState(
            SellerCrawlScheduleOutbox.WriteAheadState domainState
    ) {
        if (domainState == null) {
            return SellerCrawlScheduleOutboxEntity.WriteAheadState.PENDING;
        }
        return switch (domainState) {
            case PENDING -> SellerCrawlScheduleOutboxEntity.WriteAheadState.PENDING;
            case COMPLETED -> SellerCrawlScheduleOutboxEntity.WriteAheadState.COMPLETED;
        };
    }

    /**
     * Entity OperationState → Domain OperationState
     */
    private SellerCrawlScheduleOutbox.OperationState mapToDomainOperationState(
            SellerCrawlScheduleOutboxEntity.OperationState entityState
    ) {
        if (entityState == null) {
            return null;
        }
        return switch (entityState) {
            case PENDING -> SellerCrawlScheduleOutbox.OperationState.PENDING;
            case IN_PROGRESS -> SellerCrawlScheduleOutbox.OperationState.IN_PROGRESS;
            case COMPLETED -> SellerCrawlScheduleOutbox.OperationState.COMPLETED;
            case FAILED -> SellerCrawlScheduleOutbox.OperationState.FAILED;
        };
    }

    /**
     * Entity WriteAheadState → Domain WriteAheadState
     */
    private SellerCrawlScheduleOutbox.WriteAheadState mapToDomainWriteAheadState(
            SellerCrawlScheduleOutboxEntity.WriteAheadState entityState
    ) {
        if (entityState == null) {
            return null;
        }
        return switch (entityState) {
            case PENDING -> SellerCrawlScheduleOutbox.WriteAheadState.PENDING;
            case COMPLETED -> SellerCrawlScheduleOutbox.WriteAheadState.COMPLETED;
        };
    }
}
