package com.ryuqq.crawlinghub.adapter.persistence.jpa.workflow;

import com.ryuqq.crawlinghub.domain.site.SiteId;
import com.ryuqq.crawlinghub.domain.workflow.CrawlWorkflow;
import com.ryuqq.crawlinghub.domain.workflow.WorkflowId;
import org.springframework.stereotype.Component;

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

}
