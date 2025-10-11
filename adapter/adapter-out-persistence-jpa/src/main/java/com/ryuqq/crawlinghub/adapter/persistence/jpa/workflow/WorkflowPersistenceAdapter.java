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

        // 4. Save new steps if present
        if (workflow.getStepsCount() > 0) {

            // Convert domain steps to entities and set the persisted workflow ID
            List<WorkflowStepEntity> stepEntities = workflow.getSteps().stream()
                    .map(step -> {
                        WorkflowStepEntity stepEntity = mapper.toStepEntity(step);
                        // Override workflowId with the persisted ID
                        return WorkflowStepEntity.builder()
                                .stepId(stepEntity.getStepId())
                                .workflowId(workflowId)
                                .stepName(stepEntity.getStepName())
                                .stepOrder(stepEntity.getStepOrder())
                                .stepType(stepEntity.getStepType())
                                .endpointKey(stepEntity.getEndpointKey())
                                .parallelExecution(stepEntity.getParallelExecution())
                                .stepConfig(stepEntity.getStepConfig())
                                .build();
                    })
                    .toList();

            // Save all steps
            stepRepository.saveAll(stepEntities);
        }

        // 3. Return domain model with steps
        return findById(new WorkflowId(savedWorkflow.getWorkflowId()))
                .orElse(mapper.toDomain(savedWorkflow));
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
        return queryRepository.findBySiteId(siteId.value(), pageable)
                .map(this::toDomainWithSteps);
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
        return queryRepository.findActiveWorkflows(pageable)
                .map(this::toDomainWithSteps);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CrawlWorkflow> findActiveWorkflows(Long lastWorkflowId, int pageSize) {
        List<CrawlWorkflowEntity> entities = queryRepository.findActiveWorkflows(lastWorkflowId, pageSize);
        return toDomainWithStepsBatch(entities);
    }

}
