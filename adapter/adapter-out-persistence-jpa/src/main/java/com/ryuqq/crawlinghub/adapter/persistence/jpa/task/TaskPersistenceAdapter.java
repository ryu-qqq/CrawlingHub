package com.ryuqq.crawlinghub.adapter.persistence.jpa.task;

import com.ryuqq.crawlinghub.application.task.port.out.LoadTaskPort;
import com.ryuqq.crawlinghub.domain.common.TaskStatus;
import com.ryuqq.crawlinghub.domain.execution.ExecutionId;
import com.ryuqq.crawlinghub.domain.task.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.TaskId;
import com.ryuqq.crawlinghub.domain.workflow.StepId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Persistence Adapter for CrawlTask
 * Implements Query (Load) port only
 * Follows CQRS pattern - read operations using QueryDSL
 */
@Component
public class TaskPersistenceAdapter implements LoadTaskPort {

    private final CrawlTaskJpaRepository jpaRepository;
    private final CrawlTaskQueryRepository queryRepository;
    private final TaskMapper mapper;

    public TaskPersistenceAdapter(CrawlTaskJpaRepository jpaRepository,
                                  CrawlTaskQueryRepository queryRepository,
                                  TaskMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.queryRepository = queryRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<CrawlTask> findById(TaskId taskId) {
        return jpaRepository.findById(taskId.value())
                .map(mapper::toDomain);
    }

    @Override
    public List<CrawlTask> findByExecutionId(ExecutionId executionId) {
        return queryRepository.findByExecutionId(executionId.value()).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<CrawlTask> findByExecutionIdAndStatus(ExecutionId executionId, TaskStatus status) {
        return queryRepository.findByExecutionIdAndStatus(executionId.value(), status).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<CrawlTask> findByExecutionIdAndStepId(ExecutionId executionId, StepId stepId) {
        return queryRepository.findByExecutionIdAndStepId(executionId.value(), stepId.value()).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<CrawlTask> findWithFilters(ExecutionId executionId, TaskStatus status, StepId stepId) {
        Long stepIdValue = stepId != null ? stepId.value() : null;
        return queryRepository.findWithFilters(executionId.value(), status, stepIdValue).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
