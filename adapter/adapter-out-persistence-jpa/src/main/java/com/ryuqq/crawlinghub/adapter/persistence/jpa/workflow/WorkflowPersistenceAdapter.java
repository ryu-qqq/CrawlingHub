package com.ryuqq.crawlinghub.adapter.persistence.jpa.workflow;

import com.ryuqq.crawlinghub.application.workflow.port.out.LoadWorkflowPort;
import com.ryuqq.crawlinghub.application.workflow.port.out.SaveWorkflowPort;
import com.ryuqq.crawlinghub.domain.site.SiteId;
import com.ryuqq.crawlinghub.domain.workflow.CrawlWorkflow;
import com.ryuqq.crawlinghub.domain.workflow.WorkflowId;
import com.ryuqq.crawlinghub.domain.workflow.WorkflowStep;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Persistence Adapter for CrawlWorkflow
 * Implements both Command (Save) and Query (Load) ports
 * Follows CQRS pattern by delegating to appropriate repositories
 * Supports both Offset-Based and No-Offset pagination strategies
 */
@Component
public class WorkflowPersistenceAdapter implements SaveWorkflowPort, LoadWorkflowPort {

    private final CrawlWorkflowJpaRepository jpaRepository;
    private final CrawlWorkflowQueryRepository queryRepository;
    private final WorkflowStepJpaRepository stepRepository;
    private final WorkflowMapper mapper;

    public WorkflowPersistenceAdapter(CrawlWorkflowJpaRepository jpaRepository,
                                       CrawlWorkflowQueryRepository queryRepository,
                                       WorkflowStepJpaRepository stepRepository,
                                       WorkflowMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.queryRepository = queryRepository;
        this.stepRepository = stepRepository;
        this.mapper = mapper;
    }

    // ========================================
    // Command Port Implementation (SaveWorkflowPort)
    // Uses JpaRepository for CUD operations
    // ========================================

    @Override
    @Transactional
    public CrawlWorkflow save(CrawlWorkflow workflow) {
        // 1. Save workflow entity
        CrawlWorkflowEntity entity = mapper.toEntity(workflow);
        CrawlWorkflowEntity savedWorkflow = jpaRepository.save(entity);

        // 2. Get the saved workflow ID
        Long workflowId = savedWorkflow.getWorkflowId();

        // 3. Delete existing steps first (for update operations, even if replacing with empty list)
        stepRepository.deleteByWorkflowId(workflowId);

        List<WorkflowStep> domainSteps = List.of();
        // 4. Save new steps if present
        if (workflow.getStepsCount() > 0) {
            // Convert domain steps to entities with the persisted workflow ID
            List<WorkflowStepEntity> stepEntities = workflow.getSteps().stream()
                    .map(step -> mapper.toStepEntity(step, workflowId))
                    .toList();

            // Save all steps and convert back to domain
            List<WorkflowStepEntity> savedStepEntities = stepRepository.saveAll(stepEntities);
            domainSteps = mapper.toStepDomains(savedStepEntities);
        }

        // 5. Return domain model built from saved entities, avoiding a re-fetch
        return CrawlWorkflow.reconstituteWithSteps(
                new WorkflowId(savedWorkflow.getWorkflowId()),
                new SiteId(savedWorkflow.getSiteId()),
                savedWorkflow.getWorkflowName(),
                savedWorkflow.getWorkflowDescription(),
                savedWorkflow.getIsActive(),
                domainSteps
        );
    }

    @Override
    @Transactional
    public void delete(WorkflowId workflowId) {
        // Delete steps first (foreign key constraint)
        stepRepository.deleteByWorkflowId(workflowId.value());
        // Then delete workflow
        jpaRepository.deleteById(workflowId.value());
    }

    // ========================================
    // Query Port Implementation (LoadWorkflowPort)
    // Uses JpaRepository for simple find, QueryRepository for complex queries
    // ========================================

    @Override
    @Transactional(readOnly = true)
    public Optional<CrawlWorkflow> findById(WorkflowId workflowId) {
        return jpaRepository.findById(workflowId.value())
                .map(this::toDomainWithSteps);
    }

    /**
     * Helper method to convert entity to domain with steps loaded
     * @param workflowEntity the workflow entity
     * @return domain model with steps
     */
    private CrawlWorkflow toDomainWithSteps(CrawlWorkflowEntity workflowEntity) {
        // Load steps for this workflow
        List<WorkflowStepEntity> stepEntities = stepRepository.findByWorkflowId(workflowEntity.getWorkflowId());

        if (stepEntities.isEmpty()) {
            // No steps - use regular reconstitute
            return mapper.toDomain(workflowEntity);
        } else {
            // Has steps - use reconstituteWithSteps
            List<WorkflowStep> steps = mapper.toStepDomains(stepEntities);
            return CrawlWorkflow.reconstituteWithSteps(
                    new WorkflowId(workflowEntity.getWorkflowId()),
                    new SiteId(workflowEntity.getSiteId()),
                    workflowEntity.getWorkflowName(),
                    workflowEntity.getWorkflowDescription(),
                    workflowEntity.getIsActive(),
                    steps
            );
        }
    }

    /**
     * Helper method to convert multiple entities to domains with steps loaded in batch
     * Solves N+1 query problem by loading all steps in a single query
     * @param workflowEntities list of workflow entities
     * @return list of domain models with steps
     */
    private List<CrawlWorkflow> toDomainWithStepsBatch(List<CrawlWorkflowEntity> workflowEntities) {
        if (workflowEntities.isEmpty()) {
            return List.of();
        }

        // Extract all workflow IDs
        List<Long> workflowIds = workflowEntities.stream()
                .map(CrawlWorkflowEntity::getWorkflowId)
                .toList();

        // Load all steps in a single query
        List<WorkflowStepEntity> allSteps = stepRepository.findByWorkflowIdIn(workflowIds);

        // Group steps by workflow ID
        java.util.Map<Long, List<WorkflowStepEntity>> stepsByWorkflowId = allSteps.stream()
                .collect(java.util.stream.Collectors.groupingBy(WorkflowStepEntity::getWorkflowId));

        // Convert to domain objects
        return workflowEntities.stream()
                .map(entity -> {
                    List<WorkflowStepEntity> steps = stepsByWorkflowId.getOrDefault(
                            entity.getWorkflowId(),
                            List.of()
                    );

                    if (steps.isEmpty()) {
                        return mapper.toDomain(entity);
                    } else {
                        List<WorkflowStep> domainSteps = mapper.toStepDomains(steps);
                        return CrawlWorkflow.reconstituteWithSteps(
                                new WorkflowId(entity.getWorkflowId()),
                                new SiteId(entity.getSiteId()),
                                entity.getWorkflowName(),
                                entity.getWorkflowDescription(),
                                entity.getIsActive(),
                                domainSteps
                        );
                    }
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CrawlWorkflow> findBySiteId(SiteId siteId) {
        List<CrawlWorkflowEntity> entities = queryRepository.findBySiteId(siteId.value());
        return toDomainWithStepsBatch(entities);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CrawlWorkflow> findBySiteId(SiteId siteId, Pageable pageable) {
        Page<CrawlWorkflowEntity> entityPage = queryRepository.findBySiteId(siteId.value(), pageable);
        List<CrawlWorkflow> workflows = toDomainWithStepsBatch(entityPage.getContent());
        return new org.springframework.data.domain.PageImpl<>(workflows, pageable, entityPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CrawlWorkflow> findBySiteId(SiteId siteId, Long lastWorkflowId, int pageSize) {
        List<CrawlWorkflowEntity> entities = queryRepository.findBySiteId(siteId.value(), lastWorkflowId, pageSize);
        return toDomainWithStepsBatch(entities);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CrawlWorkflow> findActiveWorkflows() {
        List<CrawlWorkflowEntity> entities = queryRepository.findActiveWorkflows();
        return toDomainWithStepsBatch(entities);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CrawlWorkflow> findActiveWorkflows(Pageable pageable) {
        Page<CrawlWorkflowEntity> entityPage = queryRepository.findActiveWorkflows(pageable);
        List<CrawlWorkflow> workflows = toDomainWithStepsBatch(entityPage.getContent());
        return new org.springframework.data.domain.PageImpl<>(workflows, pageable, entityPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CrawlWorkflow> findActiveWorkflows(Long lastWorkflowId, int pageSize) {
        List<CrawlWorkflowEntity> entities = queryRepository.findActiveWorkflows(lastWorkflowId, pageSize);
        return toDomainWithStepsBatch(entities);
    }

}
