package com.ryuqq.crawlinghub.adapter.in.web.schedule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.crawlinghub.adapter.in.web.common.GlobalExceptionHandler;
import com.ryuqq.crawlinghub.application.schedule.usecase.*;
import com.ryuqq.crawlinghub.domain.common.ParamType;
import com.ryuqq.crawlinghub.domain.schedule.CrawlSchedule;
import com.ryuqq.crawlinghub.domain.schedule.ScheduleId;
import com.ryuqq.crawlinghub.domain.schedule.ScheduleInputParam;
import com.ryuqq.crawlinghub.domain.workflow.WorkflowId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit test for CrawlScheduleController
 * Tests HTTP layer with MockMvc in standalone mode
 *
 * @author Sangwon Ryu (ryuqq@company.com)
 * @since 2025-10-14
 */
@ExtendWith(MockitoExtension.class)
class CrawlScheduleControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private RegisterScheduleUseCase registerScheduleUseCase;

    @Mock
    private GetScheduleUseCase getScheduleUseCase;

    @Mock
    private UpdateScheduleUseCase updateScheduleUseCase;

    @Mock
    private DeleteScheduleUseCase deleteScheduleUseCase;

    @Mock
    private EnableScheduleUseCase enableScheduleUseCase;

    @Mock
    private DisableScheduleUseCase disableScheduleUseCase;

    @InjectMocks
    private CrawlScheduleController controller;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        ExceptionHandlerExceptionResolver exceptionResolver = new ExceptionHandlerExceptionResolver();
        exceptionResolver.setMessageConverters(List.of(new MappingJackson2HttpMessageConverter(objectMapper)));
        exceptionResolver.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setHandlerExceptionResolvers(exceptionResolver)
                .build();
    }

    @Nested
    @DisplayName("POST /api/v1/schedules - Create Schedule")
    class CreateScheduleTests {

        @Test
        @DisplayName("successfully creates a schedule")
        void createSchedule_Success() throws Exception {
            // given
            String requestJson = """
                    {
                        "workflowId": 100,
                        "scheduleName": "Daily Schedule",
                        "cronExpression": "0 0 * * *",
                        "timezone": "Asia/Seoul",
                        "inputParams": [
                            {
                                "paramKey": "category",
                                "paramValue": "electronics",
                                "paramType": "STRING"
                            }
                        ]
                    }
                    """;

            ScheduleId scheduleId = ScheduleId.of(1L);
            CrawlSchedule schedule = createTestSchedule(1L, 100L, "Daily Schedule", true);
            List<ScheduleInputParam> inputParams = List.of(
                    ScheduleInputParam.create(1L, "category", "electronics", ParamType.STATIC)
            );

            when(registerScheduleUseCase.execute(any(RegisterScheduleCommand.class)))
                    .thenReturn(scheduleId);
            when(getScheduleUseCase.getById(scheduleId.value())).thenReturn(schedule);
            when(getScheduleUseCase.getInputParams(scheduleId.value())).thenReturn(inputParams);

            // when & then
            mockMvc.perform(post("/api/v1/schedules")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isCreated())
                    .andExpect(header().exists("Location"))
                    .andExpect(jsonPath("$.scheduleId").value(1))
                    .andExpect(jsonPath("$.workflowId").value(100))
                    .andExpect(jsonPath("$.scheduleName").value("Daily Schedule"))
                    .andExpect(jsonPath("$.isEnabled").value(true))
                    .andExpect(jsonPath("$.inputParams", hasSize(1)));

            verify(registerScheduleUseCase).execute(any(RegisterScheduleCommand.class));
            verify(getScheduleUseCase).getById(scheduleId.value());
            verify(getScheduleUseCase).getInputParams(scheduleId.value());
        }

        @Test
        @DisplayName("returns 400 when required fields are missing")
        void createSchedule_MissingRequiredFields() throws Exception {
            // given - missing scheduleName
            String requestJson = """
                    {
                        "workflowId": 100,
                        "cronExpression": "0 0 * * *",
                        "timezone": "Asia/Seoul"
                    }
                    """;

            // when & then
            mockMvc.perform(post("/api/v1/schedules")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest());

            verify(registerScheduleUseCase, never()).execute(any());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/schedules - Get Schedules List")
    class GetSchedulesTests {

        @Test
        @DisplayName("gets all schedules when no filter is provided")
        void getSchedules_NoFilter() throws Exception {
            // given
            List<CrawlSchedule> schedules = Arrays.asList(
                    createTestSchedule(1L, 100L, "Daily Schedule", true),
                    createTestSchedule(2L, 100L, "Hourly Schedule", false)
            );

            when(getScheduleUseCase.getByFilter(any(ScheduleFilter.class)))
                    .thenReturn(schedules);

            // when & then
            mockMvc.perform(get("/api/v1/schedules"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].scheduleId").value(1))
                    .andExpect(jsonPath("$[1].scheduleId").value(2));

            verify(getScheduleUseCase).getByFilter(any(ScheduleFilter.class));
        }

        @Test
        @DisplayName("filters schedules by workflow ID")
        void getSchedules_FilterByWorkflowId() throws Exception {
            // given
            Long workflowId = 100L;
            List<CrawlSchedule> schedules = Collections.singletonList(
                    createTestSchedule(1L, workflowId, "Daily Schedule", true)
            );

            when(getScheduleUseCase.getByFilter(any(ScheduleFilter.class)))
                    .thenReturn(schedules);

            // when & then
            mockMvc.perform(get("/api/v1/schedules")
                            .param("workflowId", String.valueOf(workflowId)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].workflowId").value(workflowId));

            verify(getScheduleUseCase).getByFilter(any(ScheduleFilter.class));
        }

        @Test
        @DisplayName("filters schedules by enabled status")
        void getSchedules_FilterByIsEnabled() throws Exception {
            // given
            List<CrawlSchedule> schedules = Collections.singletonList(
                    createTestSchedule(1L, 100L, "Daily Schedule", true)
            );

            when(getScheduleUseCase.getByFilter(any(ScheduleFilter.class)))
                    .thenReturn(schedules);

            // when & then
            mockMvc.perform(get("/api/v1/schedules")
                            .param("isEnabled", "true"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].isEnabled").value(true));

            verify(getScheduleUseCase).getByFilter(any(ScheduleFilter.class));
        }

        @Test
        @DisplayName("filters schedules by both workflow ID and enabled status")
        void getSchedules_FilterByBoth() throws Exception {
            // given
            Long workflowId = 100L;
            List<CrawlSchedule> schedules = Collections.singletonList(
                    createTestSchedule(1L, workflowId, "Daily Schedule", true)
            );

            when(getScheduleUseCase.getByFilter(any(ScheduleFilter.class)))
                    .thenReturn(schedules);

            // when & then
            mockMvc.perform(get("/api/v1/schedules")
                            .param("workflowId", String.valueOf(workflowId))
                            .param("isEnabled", "true"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].workflowId").value(workflowId))
                    .andExpect(jsonPath("$[0].isEnabled").value(true));

            verify(getScheduleUseCase).getByFilter(any(ScheduleFilter.class));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/schedules/{scheduleId} - Get Schedule Detail")
    class GetScheduleDetailTests {

        @Test
        @DisplayName("successfully gets schedule detail")
        void getScheduleDetail_Success() throws Exception {
            // given
            Long scheduleId = 1L;
            CrawlSchedule schedule = createTestSchedule(scheduleId, 100L, "Daily Schedule", true);
            List<ScheduleInputParam> inputParams = List.of(
                    ScheduleInputParam.create(scheduleId, "category", "electronics", ParamType.STATIC)
            );

            when(getScheduleUseCase.getById(scheduleId)).thenReturn(schedule);
            when(getScheduleUseCase.getInputParams(scheduleId)).thenReturn(inputParams);

            // when & then
            mockMvc.perform(get("/api/v1/schedules/{scheduleId}", scheduleId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.scheduleId").value(scheduleId))
                    .andExpect(jsonPath("$.scheduleName").value("Daily Schedule"))
                    .andExpect(jsonPath("$.inputParams", hasSize(1)));

            verify(getScheduleUseCase).getById(scheduleId);
            verify(getScheduleUseCase).getInputParams(scheduleId);
        }

        @Test
        @DisplayName("returns 404 when schedule not found")
        void getScheduleDetail_NotFound() throws Exception {
            // given
            Long scheduleId = 999L;
            when(getScheduleUseCase.getById(scheduleId))
                    .thenThrow(new ScheduleNotFoundException(scheduleId));

            // when & then
            mockMvc.perform(get("/api/v1/schedules/{scheduleId}", scheduleId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("SCHEDULE_NOT_FOUND"));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/schedules/{scheduleId} - Update Schedule")
    class UpdateScheduleTests {

        @Test
        @DisplayName("successfully updates a schedule")
        void updateSchedule_Success() throws Exception {
            // given
            Long scheduleId = 1L;
            String requestJson = """
                    {
                        "cronExpression": "0 0 12 * * *",
                        "timezone": "Asia/Seoul",
                        "inputParams": [
                            {
                                "paramKey": "category",
                                "paramValue": "books"
                            }
                        ]
                    }
                    """;

            doNothing().when(updateScheduleUseCase).execute(any(UpdateScheduleCommand.class));

            // when & then
            mockMvc.perform(put("/api/v1/schedules/{scheduleId}", scheduleId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isNoContent());

            verify(updateScheduleUseCase).execute(any(UpdateScheduleCommand.class));
        }

        @Test
        @DisplayName("returns 404 when schedule not found")
        void updateSchedule_NotFound() throws Exception {
            // given
            Long scheduleId = 999L;
            String requestJson = """
                    {
                        "cronExpression": "0 0 12 * * *",
                        "timezone": "Asia/Seoul"
                    }
                    """;

            doThrow(new ScheduleNotFoundException(scheduleId))
                    .when(updateScheduleUseCase).execute(any(UpdateScheduleCommand.class));

            // when & then
            mockMvc.perform(put("/api/v1/schedules/{scheduleId}", scheduleId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("SCHEDULE_NOT_FOUND"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/schedules/{scheduleId}/enable - Enable Schedule")
    class EnableScheduleTests {

        @Test
        @DisplayName("successfully enables a schedule")
        void enableSchedule_Success() throws Exception {
            // given
            Long scheduleId = 1L;
            doNothing().when(enableScheduleUseCase).execute(scheduleId);

            // when & then
            mockMvc.perform(post("/api/v1/schedules/{scheduleId}/enable", scheduleId))
                    .andExpect(status().isNoContent());

            verify(enableScheduleUseCase).execute(scheduleId);
        }

        @Test
        @DisplayName("returns 404 when schedule not found")
        void enableSchedule_NotFound() throws Exception {
            // given
            Long scheduleId = 999L;
            doThrow(new ScheduleNotFoundException(scheduleId))
                    .when(enableScheduleUseCase).execute(scheduleId);

            // when & then
            mockMvc.perform(post("/api/v1/schedules/{scheduleId}/enable", scheduleId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("SCHEDULE_NOT_FOUND"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/schedules/{scheduleId}/disable - Disable Schedule")
    class DisableScheduleTests {

        @Test
        @DisplayName("successfully disables a schedule")
        void disableSchedule_Success() throws Exception {
            // given
            Long scheduleId = 1L;
            doNothing().when(disableScheduleUseCase).execute(scheduleId);

            // when & then
            mockMvc.perform(post("/api/v1/schedules/{scheduleId}/disable", scheduleId))
                    .andExpect(status().isNoContent());

            verify(disableScheduleUseCase).execute(scheduleId);
        }

        @Test
        @DisplayName("returns 404 when schedule not found")
        void disableSchedule_NotFound() throws Exception {
            // given
            Long scheduleId = 999L;
            doThrow(new ScheduleNotFoundException(scheduleId))
                    .when(disableScheduleUseCase).execute(scheduleId);

            // when & then
            mockMvc.perform(post("/api/v1/schedules/{scheduleId}/disable", scheduleId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("SCHEDULE_NOT_FOUND"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/schedules/{scheduleId} - Delete Schedule")
    class DeleteScheduleTests {

        @Test
        @DisplayName("successfully deletes a schedule")
        void deleteSchedule_Success() throws Exception {
            // given
            Long scheduleId = 1L;
            doNothing().when(deleteScheduleUseCase).execute(scheduleId);

            // when & then
            mockMvc.perform(delete("/api/v1/schedules/{scheduleId}", scheduleId))
                    .andExpect(status().isNoContent());

            verify(deleteScheduleUseCase).execute(scheduleId);
        }

        @Test
        @DisplayName("returns 404 when schedule not found")
        void deleteSchedule_NotFound() throws Exception {
            // given
            Long scheduleId = 999L;
            doThrow(new ScheduleNotFoundException(scheduleId))
                    .when(deleteScheduleUseCase).execute(scheduleId);

            // when & then
            mockMvc.perform(delete("/api/v1/schedules/{scheduleId}", scheduleId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("SCHEDULE_NOT_FOUND"));
        }
    }

    // ========== Helper Methods ==========

    private CrawlSchedule createTestSchedule(Long scheduleId, Long workflowId, String name, boolean isEnabled) {
        return CrawlSchedule.reconstitute(
                ScheduleId.of(scheduleId),
                WorkflowId.of(workflowId),
                name,
                "0 0 * * *",
                "Asia/Seoul",
                isEnabled,
                "crawl-schedule-" + name.toLowerCase(),
                LocalDateTime.now().plusHours(1)
        );
    }
}
