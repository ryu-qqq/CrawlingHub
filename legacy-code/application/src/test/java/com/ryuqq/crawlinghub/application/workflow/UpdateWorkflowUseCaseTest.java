package com.ryuqq.crawlinghub.application.workflow;

import com.ryuqq.crawlinghub.application.workflow.port.out.LoadWorkflowPort;
import com.ryuqq.crawlinghub.application.workflow.port.out.SaveWorkflowPort;
import com.ryuqq.crawlinghub.application.workflow.usecase.UpdateWorkflowCommand;
import com.ryuqq.crawlinghub.application.workflow.usecase.UpdateWorkflowUseCase;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test for UpdateWorkflowUseCase
 * Tests workflow update operations
 */
@ExtendWith(MockitoExtension.class)
class UpdateWorkflowUseCaseTest {

    @Mock
    private LoadWorkflowPort loadWorkflowPort;

    @Mock
    private SaveWorkflowPort saveWorkflowPort;

    private UpdateWorkflowUseCase updateWorkflowUseCase;

    @BeforeEach
    void setUp() {
        updateWorkflowUseCase = new UpdateWorkflowUseCase(saveWorkflowPort, loadWorkflowPort);
    }

    @Test
    @DisplayName("워크플로우 설명을 업데이트한다")
    void shouldUpdateWorkflowDescription() {
        // given
        Long workflowId = 1L;
        String newDescription = "Updated description";

        CrawlWorkflow existingWorkflow = createTestWorkflow(workflowId, "Old description");
        UpdateWorkflowCommand command = new UpdateWorkflowCommand(
                WorkflowId.of(workflowId),
                newDescription,
                List.of()
        );

        when(loadWorkflowPort.findById(any(WorkflowId.class))).thenReturn(Optional.of(existingWorkflow));
        when(saveWorkflowPort.save(any(CrawlWorkflow.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        updateWorkflowUseCase.execute(command);

        // then
        ArgumentCaptor<CrawlWorkflow> workflowCaptor = ArgumentCaptor.forClass(CrawlWorkflow.class);
        verify(saveWorkflowPort).save(workflowCaptor.capture());

        CrawlWorkflow savedWorkflow = workflowCaptor.getValue();
        assertThat(savedWorkflow.getWorkflowDescription()).isEqualTo(newDescription);
        verify(loadWorkflowPort).findById(WorkflowId.of(workflowId));
    }

    @Test
    @DisplayName("존재하지 않는 워크플로우 업데이트 시 예외 발생")
    void shouldThrowExceptionWhenWorkflowNotFound() {
        // given
        Long workflowId = 999L;
        UpdateWorkflowCommand command = new UpdateWorkflowCommand(
                WorkflowId.of(workflowId),
                "New description",
                List.of()
        );

        when(loadWorkflowPort.findById(any(WorkflowId.class))).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> updateWorkflowUseCase.execute(command))
                .isInstanceOf(WorkflowNotFoundException.class)
                .hasMessageContaining("Workflow not found");

        verify(loadWorkflowPort).findById(WorkflowId.of(workflowId));
        verify(saveWorkflowPort, never()).save(any());
    }

    @Test
    @DisplayName("빈 설명으로 업데이트할 수 있다")
    void shouldAllowEmptyDescription() {
        // given
        Long workflowId = 1L;
        String emptyDescription = "";

        CrawlWorkflow existingWorkflow = createTestWorkflow(workflowId, "Old description");
        UpdateWorkflowCommand command = new UpdateWorkflowCommand(
                WorkflowId.of(workflowId),
                emptyDescription,
                List.of()
        );

        when(loadWorkflowPort.findById(any(WorkflowId.class))).thenReturn(Optional.of(existingWorkflow));
        when(saveWorkflowPort.save(any(CrawlWorkflow.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        updateWorkflowUseCase.execute(command);

        // then
        ArgumentCaptor<CrawlWorkflow> workflowCaptor = ArgumentCaptor.forClass(CrawlWorkflow.class);
        verify(saveWorkflowPort).save(workflowCaptor.capture());

        CrawlWorkflow savedWorkflow = workflowCaptor.getValue();
        assertThat(savedWorkflow.getWorkflowDescription()).isEqualTo(emptyDescription);
    }

    // ========== Helper Methods ==========

    private CrawlWorkflow createTestWorkflow(Long workflowId, String description) {
        return CrawlWorkflow.reconstitute(
                WorkflowId.of(workflowId),
                SiteId.of(1L),
                "Test Workflow",
                description,
                true
        );
    }
}
