package com.ryuqq.crawlinghub.adapter.persistence.jpa.workflow;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Command Repository for WorkflowStepOutput (CQRS Pattern)
 * Handles only CUD operations using JpaRepository
 * - save(entity): Create or Update
 * - deleteById(id): Delete
 * - existsById(id): Existence check
 *
 * Complex queries should use QueryDSL in a separate QueryRepository if needed
 */
public interface WorkflowStepOutputJpaRepository extends JpaRepository<WorkflowStepOutputEntity, Long> {
    // Inherits basic CRUD methods from JpaRepository
    // - findById(Long id)
    // - save(WorkflowStepOutputEntity entity)
    // - deleteById(Long id)
    // - existsById(Long id)

    /**
     * Find all outputs by step ID for retrieval operations
     * Used when loading step with outputs
     */
    List<WorkflowStepOutputEntity> findByStepId(Long stepId);

    /**
     * Delete all outputs by step ID for cascade operations
     * Used when deleting a step or replacing all outputs
     */
    void deleteByStepId(Long stepId);
}
