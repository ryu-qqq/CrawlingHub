package com.ryuqq.crawlinghub.adapter.out.persistence.outbox.mapper;

import com.ryuqq.crawlinghub.adapter.out.persistence.outbox.entity.SellerCrawlScheduleOutboxEntity;
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

        // CommandInfo 생성
        SellerCrawlScheduleOutboxEntity.CommandInfo entityCommandInfo =
                SellerCrawlScheduleOutboxEntity.CommandInfo.of(
                        domain.getDomain(),
                        domain.getEventType(),
                        domain.getBizKey(),
                        domain.getIdemKey()
                );

        // 새로운 Entity 생성 (저장 전)
        // 참고: 이 메서드는 Domain → Entity 변환 시 새로운 Entity를 생성할 때만 사용됩니다.
        // 기존 Entity 업데이트는 JPA의 변경 감지(Dirty Checking)를 사용합니다.
        return new SellerCrawlScheduleOutboxEntity(
                entityCommandInfo,
                domain.getSellerId(),
                domain.getPayload()
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
                mapOperationState(entity.getOperationState()),
                mapWriteAheadState(entity.getWalState()),
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
     * Entity OperationState → Domain OperationState
     */
    private SellerCrawlScheduleOutbox.OperationState mapOperationState(
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
    private SellerCrawlScheduleOutbox.WriteAheadState mapWriteAheadState(
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
