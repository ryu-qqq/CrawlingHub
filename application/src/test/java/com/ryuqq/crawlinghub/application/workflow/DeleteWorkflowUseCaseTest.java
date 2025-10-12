package com.ryuqq.crawlinghub.application.workflow;

import com.ryuqq.crawlinghub.application.workflow.port.out.LoadWorkflowPort;
import com.ryuqq.crawlinghub.application.workflow.port.out.SaveWorkflowPort;
import com.ryuqq.crawlinghub.application.workflow.usecase.DeleteWorkflowUseCase;
import com.ryuqq.crawlinghub.application.workflow.usecase.WorkflowNotFoundException;
import com.ryuqq.crawlinghub.domain.site.SiteId;
import com.ryuqq.crawlinghub.domain.workflow.CrawlWorkflow;
import com.ryuqq.crawlinghub.domain.workflow.WorkflowId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test for DeleteWorkflowUseCase
 * Tests workflow deletion (soft delete via deactivation)
 */
@ExtendWith(MockitoExtension.class)
class DeleteWorkflowUseCaseTest {

    @Mock
    private LoadWorkflowPort loadWorkflowPort;

    @Mock
    private SaveWorkflowPort saveWorkflowPort;

    private DeleteWorkflowUseCase deleteWorkflowUseCase;

    @BeforeEach
    void setUp() {
        deleteWorkflowUseCase = new DeleteWorkflowUseCase(saveWorkflowPort, loadWorkflowPort);
    }

    @Test
    @DisplayName("워크플로우를 비활성화한다 (소프트 삭제)")
    void shouldDeactivateWorkflow() {
        // given
        Long workflowId = 1L;
        CrawlWorkflow activeWorkflow = createTestWorkflow(workflowId, true);

        when(loadWorkflowPort.findById(any(WorkflowId.class))).thenReturn(Optional.of(activeWorkflow));
        when(saveWorkflowPort.save(any(CrawlWorkflow.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        deleteWorkflowUseCase.execute(WorkflowId.of(workflowId));

        // then
        ArgumentCaptor<CrawlWorkflow> workflowCaptor = ArgumentCaptor.forClass(CrawlWorkflow.class);
        verify(saveWorkflowPort).save(workflowCaptor.capture());

        CrawlWorkflow savedWorkflow = workflowCaptor.getValue();
        assertThat(savedWorkflow.isActive()).isFalse();
        verify(loadWorkflowPort).findById(WorkflowId.of(workflowId));
    }

    @Test
    @DisplayName("존재하지 않는 워크플로우 삭제 시 예외 발생")
    void shouldThrowExceptionWhenWorkflowNotFound() {
        // given
        Long workflowId = 999L;
        when(loadWorkflowPort.findById(any(WorkflowId.class))).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> deleteWorkflowUseCase.execute(WorkflowId.of(workflowId)))
                .isInstanceOf(WorkflowNotFoundException.class)
                .hasMessageContaining("Workflow not found");

        verify(loadWorkflowPort).findById(WorkflowId.of(workflowId));
        verify(saveWorkflowPort, never()).save(any());
    }

    @Test
    @DisplayName("이미 비활성화된 워크플로우도 처리할 수 있다")
    void shouldHandleAlreadyDeactivatedWorkflow() {
        // given
        Long workflowId = 1L;
        CrawlWorkflow inactiveWorkflow = createTestWorkflow(workflowId, false);

        when(loadWorkflowPort.findById(any(WorkflowId.class))).thenReturn(Optional.of(inactiveWorkflow));
        when(saveWorkflowPort.save(any(CrawlWorkflow.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        deleteWorkflowUseCase.execute(WorkflowId.of(workflowId));

        // then
        ArgumentCaptor<CrawlWorkflow> workflowCaptor = ArgumentCaptor.forClass(CrawlWorkflow.class);
        verify(saveWorkflowPort).save(workflowCaptor.capture());

        CrawlWorkflow savedWorkflow = workflowCaptor.getValue();
        assertThat(savedWorkflow.isActive()).isFalse();
    }

    // ========== Helper Methods ==========

    private CrawlWorkflow createTestWorkflow(Long workflowId, boolean isActive) {
        return CrawlWorkflow.reconstitute(
                WorkflowId.of(workflowId),
                SiteId.of(1L),
                "Test Workflow",
                "Test description",
                isActive
        );
    }
}
