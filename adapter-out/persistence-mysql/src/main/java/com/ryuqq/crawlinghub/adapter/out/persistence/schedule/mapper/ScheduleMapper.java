package com.ryuqq.crawlinghub.adapter.out.persistence.schedule.mapper;

import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.dto.ScheduleQueryDto;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.ScheduleEntity;
import com.ryuqq.crawlinghub.domain.schedule.CrawlSchedule;
import com.ryuqq.crawlinghub.domain.schedule.CrawlScheduleId;
import com.ryuqq.crawlinghub.domain.schedule.CronExpression;
import com.ryuqq.crawlinghub.domain.schedule.ScheduleStatus;
import com.ryuqq.crawlinghub.domain.seller.MustItSellerId;
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
        ScheduleStatus status = schedule.getStatus(); // Entity와 Domain이 같은 enum 사용

        if (id == null) {
            // 신규 생성 Entity (protected constructor 사용)
            return ScheduleEntity.create(
                sellerId,
                cronExpression,
                status,
                schedule.getNextExecutionTime()
            );
        } else {
            // DB reconstitute Entity (private constructor는 static factory method 사용)
            return ScheduleEntity.reconstitute(
                id,
                sellerId,
                cronExpression,
                status,
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
            MustItSellerId.of(entity.getSellerId()),
            CronExpression.of(entity.getCronExpression()),
            entity.getStatus(), // Entity와 Domain이 같은 enum 사용
            entity.getNextExecutionTime(),
            entity.getLastExecutedAt(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    /**
     * DTO → Domain 변환
     *
     * @param dto ScheduleQueryDto
     * @return CrawlSchedule
     */
    public CrawlSchedule toDomain(ScheduleQueryDto dto) {
        Objects.requireNonNull(dto, "dto must not be null");

        return CrawlSchedule.reconstitute(
            CrawlScheduleId.of(dto.id()),
            MustItSellerId.of(dto.sellerId()),
            com.ryuqq.crawlinghub.domain.schedule.CronExpression.of(dto.cronExpression()),
            dto.status(),
            dto.nextExecutionTime(),
            dto.lastExecutedAt(),
            dto.createdAt(),
            dto.updatedAt()
        );
    }

}
