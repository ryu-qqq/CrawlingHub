package com.ryuqq.crawlinghub.adapter.persistence.jpa.execution;

import com.ryuqq.crawlinghub.domain.execution.CrawlExecution;
import com.ryuqq.crawlinghub.domain.execution.ExecutionId;
import com.ryuqq.crawlinghub.domain.execution.ExecutionReconstituteParams;
import com.ryuqq.crawlinghub.domain.schedule.ScheduleId;
import org.springframework.stereotype.Component;

/**
 * Mapper between CrawlExecution domain model and CrawlExecutionEntity
 * Handles bidirectional conversion
 */
@Component
public class ExecutionMapper {

    /**
     * Convert domain model to JPA entity
     * @param domain the domain model
     * @return JPA entity
     */
    public CrawlExecutionEntity toEntity(CrawlExecution domain) {
        return CrawlExecutionEntity.builder()
                .executionId(domain.getExecutionId() != null ? domain.getExecutionId().value() : null)
                .scheduleId(domain.getScheduleId().value())
                .executionName(domain.getExecutionName())
                .status(domain.getStatus())
                .startedAt(domain.getStartedAt())
                .completedAt(domain.getCompletedAt())
                .errorMessage(domain.getErrorMessage())
                .build();
    }

    /**
     * Convert JPA entity to domain model
     * @param entity the JPA entity
     * @return domain model
     * @throws IllegalStateException if entity ID is null (should not happen for persisted entities)
     */
    public CrawlExecution toDomain(CrawlExecutionEntity entity) {
        // Defensive: Ensure entity is already persisted (has ID)
        if (entity.getExecutionId() == null) {
            throw new IllegalStateException("Cannot convert non-persisted entity to domain model. Entity must have an ID.");
        }

        ExecutionReconstituteParams params = new ExecutionReconstituteParams(
                new ExecutionId(entity.getExecutionId()),
                new ScheduleId(entity.getScheduleId()),
                entity.getExecutionName(),
                entity.getStatus(),
                entity.getStartedAt(),
                entity.getCompletedAt(),
                entity.getErrorMessage()
        );

        return CrawlExecution.reconstitute(params);
    }

}
