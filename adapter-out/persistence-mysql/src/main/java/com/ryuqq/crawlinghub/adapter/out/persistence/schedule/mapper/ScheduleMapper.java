package com.ryuqq.crawlinghub.adapter.out.persistence.schedule.mapper;

import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.ScheduleEntity;
import com.ryuqq.crawlinghub.domain.schedule.CrawlSchedule;
import com.ryuqq.crawlinghub.domain.schedule.CrawlScheduleId;
import com.ryuqq.crawlinghub.domain.schedule.CronExpression;
import com.ryuqq.crawlinghub.domain.seller.MustitSellerId;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * CrawlSchedule Mapper (Pure Java)
 *
 * <p>Entity ↔ Domain 변환을 담당합니다.</p>
 *
 * <p><strong>컨벤션 준수:</strong></p>
 * <ul>
 *   <li>✅ MapStruct 금지 - Pure Java 사용</li>
 *   <li>✅ @Component (Spring Bean 등록)</li>
 *   <li>✅ Value Object 수동 변환</li>
 *   <li>✅ Null 체크 및 검증</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
@Component
public class ScheduleMapper {

    /**
     * Domain → Entity 변환
     *
     * <p>Domain Model의 ID가 null이면 신규 생성 Entity 반환</p>
     *
     * @param schedule Domain Model
     * @return JPA Entity
     */
    public ScheduleEntity toEntity(CrawlSchedule schedule) {
        Objects.requireNonNull(schedule, "schedule must not be null");

        Long id = schedule.getIdValue();
        Long sellerId = schedule.getSellerIdValue();
        String cronExpression = schedule.getCronExpressionValue();
        ScheduleEntity.ScheduleStatus entityStatus = toEntityStatus(schedule.getStatus());

        if (id == null) {
            // 신규 생성 Entity (protected constructor 사용)
            return ScheduleEntity.create(
                sellerId,
                cronExpression,
                entityStatus,
                schedule.getNextExecutionTime()
            );
        } else {
            // DB reconstitute Entity (private constructor는 static factory method 사용)
            return ScheduleEntity.reconstitute(
                id,
                sellerId,
                cronExpression,
                entityStatus,
                schedule.getNextExecutionTime(),
                schedule.getLastExecutedAt(),
                schedule.getCreatedAt(),
                schedule.getUpdatedAt()
            );
        }
    }

    /**
     * Entity → Domain 변환
     *
     * @param entity JPA Entity
     * @return Domain Model
     */
    public CrawlSchedule toDomain(ScheduleEntity entity) {
        Objects.requireNonNull(entity, "entity must not be null");

        return CrawlSchedule.reconstitute(
            CrawlScheduleId.of(entity.getId()),
            MustitSellerId.of(entity.getSellerId()),
            CronExpression.of(entity.getCronExpression()),
            toDomainStatus(entity.getStatus()),
            entity.getNextExecutionTime(),
            entity.getLastExecutedAt(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    /**
     * Entity ScheduleStatus → Domain ScheduleStatus
     */
    private com.ryuqq.crawlinghub.domain.schedule.ScheduleStatus toDomainStatus(ScheduleEntity.ScheduleStatus entityStatus) {
        Objects.requireNonNull(entityStatus, "entityStatus must not be null");

        return switch (entityStatus) {
            case ACTIVE -> com.ryuqq.crawlinghub.domain.schedule.ScheduleStatus.ACTIVE;
            case SUSPENDED -> com.ryuqq.crawlinghub.domain.schedule.ScheduleStatus.SUSPENDED;
            case DELETED -> com.ryuqq.crawlinghub.domain.schedule.ScheduleStatus.DELETED;
        };
    }

    /**
     * Domain ScheduleStatus → Entity ScheduleStatus
     */
    private ScheduleEntity.ScheduleStatus toEntityStatus(com.ryuqq.crawlinghub.domain.schedule.ScheduleStatus domainStatus) {
        Objects.requireNonNull(domainStatus, "domainStatus must not be null");

        return switch (domainStatus) {
            case ACTIVE -> ScheduleEntity.ScheduleStatus.ACTIVE;
            case SUSPENDED -> ScheduleEntity.ScheduleStatus.SUSPENDED;
            case DELETED -> ScheduleEntity.ScheduleStatus.DELETED;
        };
    }
}
