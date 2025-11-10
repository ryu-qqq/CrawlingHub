package com.ryuqq.crawlinghub.adapter.persistence.jpa.workflow;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Command Repository for WorkflowStep (CQRS Pattern)
 * Handles only CUD operations using JpaRepository
 * - save(entity): Create or Update
 * - deleteById(id): Delete
 * - existsById(id): Existence check
 *
 * Complex queries should use QueryDSL in a separate QueryRepository if needed
 */
public interface WorkflowStepJpaRepository extends JpaRepository<WorkflowStepEntity, Long> {
    // Inherits basic CRUD methods from JpaRepository
    // - findById(Long id)
    // - save(WorkflowStepEntity entity)
    // - deleteById(Long id)
    // - existsById(Long id)

    /**
     * Find all steps by workflow ID for retrieval operations
     * Used when loading workflow with steps
     */
    List<WorkflowStepEntity> findByWorkflowId(Long workflowId);

    /**
     * Find all steps by multiple workflow IDs for batch retrieval operations
     * Used to solve N+1 query problem when loading multiple workflows with steps
     */
    List<WorkflowStepEntity> findByWorkflowIdIn(List<Long> workflowIds);

    /**
     * Delete all steps by workflow ID for cascade operations
     * Used when deleting a workflow or replacing all steps
     */
    void deleteByWorkflowId(Long workflowId);
}
