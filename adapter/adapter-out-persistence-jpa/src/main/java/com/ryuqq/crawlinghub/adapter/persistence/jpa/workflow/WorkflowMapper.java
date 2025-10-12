package com.ryuqq.crawlinghub.adapter.persistence.jpa.workflow;

import com.ryuqq.crawlinghub.domain.site.SiteId;
import com.ryuqq.crawlinghub.domain.workflow.CrawlWorkflow;
import com.ryuqq.crawlinghub.domain.workflow.StepId;
import com.ryuqq.crawlinghub.domain.workflow.WorkflowId;
import com.ryuqq.crawlinghub.domain.workflow.WorkflowStep;
import com.ryuqq.crawlinghub.domain.workflow.WorkflowStepReconstituteParams;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper between CrawlWorkflow domain model and CrawlWorkflowEntity
 * Handles bidirectional conversion
 */
@Component
public class WorkflowMapper {

    /**
     * Convert domain model to JPA entity
     * @param domain the domain model
     * @return JPA entity
     */
    public CrawlWorkflowEntity toEntity(CrawlWorkflow domain) {
        return CrawlWorkflowEntity.builder()
                .workflowId(domain.getWorkflowId() != null ? domain.getWorkflowId().value() : null)
                .siteId(domain.getSiteId().value())
                .workflowName(domain.getWorkflowName())
                .workflowDescription(domain.getWorkflowDescription())
                .isActive(domain.isActive())
                .build();
    }

    /**
     * Convert JPA entity to domain model
     * @param entity the JPA entity
     * @return domain model
     * @throws IllegalStateException if entity ID is null (should not happen for persisted entities)
     */
    public CrawlWorkflow toDomain(CrawlWorkflowEntity entity) {
        // Defensive: Ensure entity is already persisted (has ID)
        if (entity.getWorkflowId() == null) {
            throw new IllegalStateException("Cannot convert non-persisted entity to domain model. Entity must have an ID.");
        }

        return CrawlWorkflow.reconstitute(
                new WorkflowId(entity.getWorkflowId()),
                new SiteId(entity.getSiteId()),
                entity.getWorkflowName(),
                entity.getWorkflowDescription(),
                entity.getIsActive()
        );
    }

    /**
     * Convert domain WorkflowStep to JPA entity
     * Note: workflowId is not mapped here as it will be set by the adapter layer
     * after the parent workflow is persisted
     * @param domain the domain model
     * @return JPA entity
     */
    public WorkflowStepEntity toStepEntity(WorkflowStep domain) {
        return WorkflowStepEntity.builder()
                .stepId(domain.getStepId() != null ? domain.getStepId().value() : null)
                .stepName(domain.getStepName())
                .stepOrder(domain.getStepOrder())
                .stepType(domain.getStepType())
                .endpointKey(domain.getEndpointKey())
                .parallelExecution(domain.getParallelExecution())
                .stepConfig(domain.getStepConfig())
                .build();
    }

    /**
     * Convert domain WorkflowStep to JPA entity with explicit workflowId
     * @param domain the domain model
     * @param workflowId the workflow ID to set
     * @return JPA entity with workflowId set
     */
    public WorkflowStepEntity toStepEntity(WorkflowStep domain, Long workflowId) {
        return WorkflowStepEntity.builder()
                .stepId(domain.getStepId() != null ? domain.getStepId().value() : null)
                .workflowId(workflowId)
                .stepName(domain.getStepName())
                .stepOrder(domain.getStepOrder())
                .stepType(domain.getStepType())
                .endpointKey(domain.getEndpointKey())
                .parallelExecution(domain.getParallelExecution())
                .stepConfig(domain.getStepConfig())
                .build();
    }

    /**
     * Convert JPA entity to domain WorkflowStep
     * @param entity the JPA entity
     * @return domain model
     * @throws IllegalStateException if entity ID is null (should not happen for persisted entities)
     */
    public WorkflowStep toStepDomain(WorkflowStepEntity entity) {
        // Defensive: Ensure entity is already persisted (has ID)
        if (entity.getStepId() == null) {
            throw new IllegalStateException("Cannot convert non-persisted step entity to domain model. Entity must have an ID.");
        }

        // TODO: Load params and outputs from repositories
        // For now, returning empty lists - needs to be implemented with proper batch loading
        WorkflowStepReconstituteParams params = WorkflowStepReconstituteParams.of(
                new StepId(entity.getStepId()),
                new WorkflowId(entity.getWorkflowId()),
                entity.getStepName(),
                entity.getStepOrder(),
                entity.getStepType(),
                entity.getEndpointKey(),
                entity.getParallelExecution(),
                entity.getStepConfig(),
                java.util.List.of(),  // TODO: Load params from repository
                java.util.List.of()   // TODO: Load outputs from repository
        );

        return WorkflowStep.reconstitute(params);
    }

    /**
     * Convert list of domain WorkflowSteps to JPA entities
     * @param steps the domain models
     * @return list of JPA entities
     */
    public List<WorkflowStepEntity> toStepEntities(List<WorkflowStep> steps) {
        if (steps == null) {
            return List.of();
        }
        return steps.stream()
                .map(this::toStepEntity)
                .collect(Collectors.toList());
    }

    /**
     * Convert list of JPA entities to domain WorkflowSteps
     * @param entities the JPA entities
     * @return list of domain models
     */
    public List<WorkflowStep> toStepDomains(List<WorkflowStepEntity> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(this::toStepDomain)
                .collect(Collectors.toList());
    }

}
