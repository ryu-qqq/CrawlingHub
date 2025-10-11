package com.ryuqq.crawlinghub.adapter.in.web.workflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.crawlinghub.adapter.in.web.common.GlobalExceptionHandler;
import com.ryuqq.crawlinghub.application.workflow.usecase.*;
import com.ryuqq.crawlinghub.domain.site.SiteId;
import com.ryuqq.crawlinghub.domain.workflow.CrawlWorkflow;
import com.ryuqq.crawlinghub.domain.workflow.WorkflowId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit test for CrawlWorkflowController
 * Tests HTTP layer with MockMvc in standalone mode
 */
@ExtendWith(MockitoExtension.class)
class CrawlWorkflowControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private RegisterWorkflowUseCase registerWorkflowUseCase;

    @Mock
    private GetWorkflowUseCase getWorkflowUseCase;

    @Mock
    private UpdateWorkflowUseCase updateWorkflowUseCase;

    @Mock
    private DeleteWorkflowUseCase deleteWorkflowUseCase;

    @InjectMocks
    private CrawlWorkflowController controller;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        // Create ExceptionHandlerExceptionResolver manually for proper exception handling
        ExceptionHandlerExceptionResolver exceptionResolver = new ExceptionHandlerExceptionResolver();
        exceptionResolver.setMessageConverters(List.of(new MappingJackson2HttpMessageConverter(objectMapper)));
        exceptionResolver.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setHandlerExceptionResolvers(exceptionResolver)
                .build();
    }

    @Test
    @DisplayName("워크플로우를 성공적으로 생성한다 (POST /api/v1/workflows)")
    void shouldCreateWorkflowSuccessfully() throws Exception {
        // given
        String requestJson = """
                {
                    "siteId": 1,
                    "workflowName": "Test Workflow",
                    "workflowDescription": "Test description",
                    "steps": []
                }
                """;

        CrawlWorkflow mockWorkflow = createTestWorkflow(1L, 1L, "Test Workflow", true);
        when(registerWorkflowUseCase.execute(any(RegisterWorkflowCommand.class)))
                .thenReturn(mockWorkflow);

        // when & then
        mockMvc.perform(post("/api/v1/workflows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.workflowId").value(1))
                .andExpect(jsonPath("$.workflowName").value("Test Workflow"))
                .andExpect(jsonPath("$.siteId").value(1))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(registerWorkflowUseCase).execute(any(RegisterWorkflowCommand.class));
    }

    @Test
    @DisplayName("필수 필드 누락 시 400 Bad Request를 반환한다")
    void shouldReturn400WhenRequiredFieldsMissing() throws Exception {
        // given - siteId 누락
        String invalidRequestJson = """
                {
                    "workflowName": "Test Workflow",
                    "steps": []
                }
                """;

        // when & then
        mockMvc.perform(post("/api/v1/workflows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestJson))
                .andExpect(status().isBadRequest());

        verify(registerWorkflowUseCase, never()).execute(any());
    }

    @Test
    @DisplayName("존재하지 않는 사이트로 생성 시 404 Not Found를 반환한다")
    void shouldReturn404WhenSiteNotFound() throws Exception {
        // given
        String requestJson = """
                {
                    "siteId": 999,
                    "workflowName": "Test Workflow",
                    "workflowDescription": "Test description",
                    "steps": []
                }
                """;

        when(registerWorkflowUseCase.execute(any(RegisterWorkflowCommand.class)))
                .thenThrow(new com.ryuqq.crawlinghub.application.site.usecase.SiteNotFoundException("Site not found"));

        // when & then
        mockMvc.perform(post("/api/v1/workflows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("SITE_NOT_FOUND"));
    }

    @Test
    @DisplayName("모든 활성 워크플로우 목록을 조회한다 (GET /api/v1/workflows)")
    void shouldGetAllActiveWorkflows() throws Exception {
        // given
        List<CrawlWorkflow> workflows = List.of(
                createTestWorkflow(1L, 1L, "Workflow 1", true),
                createTestWorkflow(2L, 2L, "Workflow 2", true)
        );

        when(getWorkflowUseCase.getAllActiveWorkflows()).thenReturn(workflows);

        // when & then
        mockMvc.perform(get("/api/v1/workflows"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].workflowId").value(1))
                .andExpect(jsonPath("$[0].workflowName").value("Workflow 1"))
                .andExpect(jsonPath("$[1].workflowId").value(2))
                .andExpect(jsonPath("$[1].workflowName").value("Workflow 2"));

        verify(getWorkflowUseCase).getAllActiveWorkflows();
        verify(getWorkflowUseCase, never()).getWorkflowsBySite(any());
    }

    @Test
    @DisplayName("사이트별 워크플로우 목록을 조회한다 (GET /api/v1/workflows?siteId=1)")
    void shouldGetWorkflowsBySite() throws Exception {
        // given
        Long siteId = 1L;
        List<CrawlWorkflow> workflows = List.of(
                createTestWorkflow(1L, siteId, "Workflow 1", true)
        );

        when(getWorkflowUseCase.getWorkflowsBySite(eq(SiteId.of(siteId)))).thenReturn(workflows);

        // when & then
        mockMvc.perform(get("/api/v1/workflows")
                        .param("siteId", String.valueOf(siteId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].workflowId").value(1))
                .andExpect(jsonPath("$[0].siteId").value(siteId));

        verify(getWorkflowUseCase).getWorkflowsBySite(SiteId.of(siteId));
        verify(getWorkflowUseCase, never()).getAllActiveWorkflows();
    }

    @Test
    @DisplayName("워크플로우 상세 정보를 조회한다 (GET /api/v1/workflows/{workflowId})")
    void shouldGetWorkflowDetail() throws Exception {
        // given
        Long workflowId = 1L;
        CrawlWorkflow workflow = createTestWorkflow(workflowId, 1L, "Test Workflow", true);

        when(getWorkflowUseCase.getDetail(eq(WorkflowId.of(workflowId)))).thenReturn(workflow);

        // when & then
        mockMvc.perform(get("/api/v1/workflows/{workflowId}", workflowId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workflowId").value(workflowId))
                .andExpect(jsonPath("$.workflowName").value("Test Workflow"))
                .andExpect(jsonPath("$.siteId").value(1))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(getWorkflowUseCase).getDetail(WorkflowId.of(workflowId));
    }

    @Test
    @DisplayName("존재하지 않는 워크플로우 조회 시 404 Not Found를 반환한다")
    void shouldReturn404WhenWorkflowNotFoundForDetail() throws Exception {
        // given
        Long workflowId = 999L;
        when(getWorkflowUseCase.getDetail(eq(WorkflowId.of(workflowId))))
                .thenThrow(new WorkflowNotFoundException("Workflow not found"));

        // when & then
        mockMvc.perform(get("/api/v1/workflows/{workflowId}", workflowId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("WORKFLOW_NOT_FOUND"));
    }

    @Test
    @DisplayName("워크플로우를 성공적으로 업데이트한다 (PUT /api/v1/workflows/{workflowId})")
    void shouldUpdateWorkflowSuccessfully() throws Exception {
        // given
        Long workflowId = 1L;
        String requestJson = """
                {
                    "workflowDescription": "Updated description",
                    "steps": []
                }
                """;

        doNothing().when(updateWorkflowUseCase).execute(any(UpdateWorkflowCommand.class));

        // when & then
        mockMvc.perform(put("/api/v1/workflows/{workflowId}", workflowId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNoContent());

        verify(updateWorkflowUseCase).execute(any(UpdateWorkflowCommand.class));
    }

    @Test
    @DisplayName("존재하지 않는 워크플로우 업데이트 시 404 Not Found를 반환한다")
    void shouldReturn404WhenWorkflowNotFoundForUpdate() throws Exception {
        // given
        Long workflowId = 999L;
        String requestJson = """
                {
                    "workflowDescription": "Updated description",
                    "steps": []
                }
                """;

        doThrow(new WorkflowNotFoundException("Workflow not found"))
                .when(updateWorkflowUseCase).execute(any(UpdateWorkflowCommand.class));

        // when & then
        mockMvc.perform(put("/api/v1/workflows/{workflowId}", workflowId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("WORKFLOW_NOT_FOUND"));
    }

    @Test
    @DisplayName("워크플로우를 성공적으로 삭제한다 (DELETE /api/v1/workflows/{workflowId})")
    void shouldDeleteWorkflowSuccessfully() throws Exception {
        // given
        Long workflowId = 1L;
        doNothing().when(deleteWorkflowUseCase).execute(any(WorkflowId.class));

        // when & then
        mockMvc.perform(delete("/api/v1/workflows/{workflowId}", workflowId))
                .andExpect(status().isNoContent());

        verify(deleteWorkflowUseCase).execute(WorkflowId.of(workflowId));
    }

    @Test
    @DisplayName("존재하지 않는 워크플로우 삭제 시 404 Not Found를 반환한다")
    void shouldReturn404WhenWorkflowNotFoundForDelete() throws Exception {
        // given
        Long workflowId = 999L;
        doThrow(new WorkflowNotFoundException("Workflow not found"))
                .when(deleteWorkflowUseCase).execute(any(WorkflowId.class));

        // when & then
        mockMvc.perform(delete("/api/v1/workflows/{workflowId}", workflowId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("WORKFLOW_NOT_FOUND"));
    }

    // ========== Helper Methods ==========

    private CrawlWorkflow createTestWorkflow(Long workflowId, Long siteId, String name, boolean isActive) {
        return CrawlWorkflow.reconstitute(
                WorkflowId.of(workflowId),
                SiteId.of(siteId),
                name,
                "Test description",
                isActive
        );
    }
}
