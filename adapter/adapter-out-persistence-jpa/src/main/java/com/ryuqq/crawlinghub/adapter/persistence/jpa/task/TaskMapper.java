package com.ryuqq.crawlinghub.adapter.persistence.jpa.task;

import com.ryuqq.crawlinghub.domain.execution.ExecutionId;
import com.ryuqq.crawlinghub.domain.task.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.TaskId;
import com.ryuqq.crawlinghub.domain.task.TaskReconstituteParams;
import com.ryuqq.crawlinghub.domain.workflow.StepId;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between CrawlTask domain and CrawlTaskEntity persistence models
 */
@Component
public class TaskMapper {

    /**
     * Convert Entity to Domain
     * @param entity the task entity
     * @return domain model
     */
    public CrawlTask toDomain(CrawlTaskEntity entity) {
        TaskReconstituteParams params = new TaskReconstituteParams(
                entity.getTaskId() != null ? TaskId.of(entity.getTaskId()) : null,
                ExecutionId.of(entity.getExecutionId()),
                StepId.of(entity.getStepId()),
                entity.getParentTaskId() != null ? TaskId.of(entity.getParentTaskId()) : null,
                entity.getTaskName(),
                entity.getStatus(),
                entity.getRetryCount(),
                entity.getMaxRetryCount(),
                entity.getQueuedAt(),
                entity.getStartedAt(),
                entity.getCompletedAt(),
                entity.getErrorMessage()
        );
        return CrawlTask.reconstitute(params);
    }

    /**
     * Convert Domain to Entity
     * @param domain the task domain
     * @return entity model
     */
    public CrawlTaskEntity toEntity(CrawlTask domain) {
        return CrawlTaskEntity.builder()
                .taskId(domain.getTaskId() != null ? domain.getTaskId().value() : null)
                .executionId(domain.getExecutionId().value())
                .stepId(domain.getStepId().value())
                .parentTaskId(domain.getParentTaskId() != null ? domain.getParentTaskId().value() : null)
                .taskName(domain.getTaskName())
                .status(domain.getStatus())
                .retryCount(domain.getRetryCount())
                .maxRetryCount(domain.getMaxRetryCount())
                .queuedAt(domain.getQueuedAt())
                .startedAt(domain.getStartedAt())
                .completedAt(domain.getCompletedAt())
                .errorMessage(domain.getErrorMessage())
                .build();
    }
}
