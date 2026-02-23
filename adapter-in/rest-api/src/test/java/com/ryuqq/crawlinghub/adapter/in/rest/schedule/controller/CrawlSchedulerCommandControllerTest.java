package com.ryuqq.crawlinghub.adapter.in.rest.schedule.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.command.RegisterCrawlSchedulerApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.command.UpdateCrawlSchedulerApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.response.CrawlSchedulerApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.mapper.CrawlSchedulerCommandApiMapper;
import com.ryuqq.crawlinghub.application.schedule.dto.command.RegisterCrawlSchedulerCommand;
import com.ryuqq.crawlinghub.application.schedule.dto.command.UpdateCrawlSchedulerCommand;
import com.ryuqq.crawlinghub.application.schedule.dto.response.CrawlSchedulerResponse;
import com.ryuqq.crawlinghub.application.schedule.port.in.command.RegisterCrawlSchedulerUseCase;
import com.ryuqq.crawlinghub.application.schedule.port.in.command.UpdateCrawlSchedulerUseCase;
import com.ryuqq.crawlinghub.application.task.dto.command.TriggerCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.task.port.in.command.TriggerCrawlTaskUseCase;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * CrawlSchedulerCommandController 단위 테스트
 *
 * <p>검증 범위:
 *
 * <ul>
 *   <li>HTTP 요청/응답 매핑
 *   <li>Request DTO Validation
 *   <li>Response DTO 직렬화
 *   <li>HTTP Status Code
 *   <li>UseCase 호출 검증
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("CrawlSchedulerCommandController 단위 테스트")
@Tag("unit")
@Tag("adapter-rest")
class CrawlSchedulerCommandControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private RegisterCrawlSchedulerUseCase registerCrawlSchedulerUseCase;
    private UpdateCrawlSchedulerUseCase updateCrawlSchedulerUseCase;
    private TriggerCrawlTaskUseCase triggerCrawlTaskUseCase;
    private CrawlSchedulerCommandApiMapper crawlSchedulerCommandApiMapper;

    @BeforeEach
    void setUp() {
        registerCrawlSchedulerUseCase = mock(RegisterCrawlSchedulerUseCase.class);
        updateCrawlSchedulerUseCase = mock(UpdateCrawlSchedulerUseCase.class);
        triggerCrawlTaskUseCase = mock(TriggerCrawlTaskUseCase.class);
        crawlSchedulerCommandApiMapper = mock(CrawlSchedulerCommandApiMapper.class);

        CrawlSchedulerCommandController controller =
                new CrawlSchedulerCommandController(
                        registerCrawlSchedulerUseCase,
                        updateCrawlSchedulerUseCase,
                        triggerCrawlTaskUseCase,
                        crawlSchedulerCommandApiMapper);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc =
                MockMvcBuilders.standaloneSetup(controller)
                        .addPlaceholderValue("api.endpoints.base-v1", "/api/v1")
                        .setValidator(validator)
                        .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Nested
    @DisplayName("POST /api/v1/crawling/schedules - 스케줄러 등록")
    class RegisterCrawlSchedulerTests {

        @Test
        @DisplayName("성공: 201 Created, 등록된 스케줄러 정보 반환")
        void registerCrawlScheduler_Success() throws Exception {
            // Given
            RegisterCrawlSchedulerApiRequest request =
                    new RegisterCrawlSchedulerApiRequest(1L, "테스트 스케줄러", "0 0 * * ? *");

            RegisterCrawlSchedulerCommand command =
                    new RegisterCrawlSchedulerCommand(1L, "테스트 스케줄러", "0 0 * * ? *");

            given(
                            crawlSchedulerCommandApiMapper.toCommand(
                                    any(RegisterCrawlSchedulerApiRequest.class)))
                    .willReturn(command);
            given(registerCrawlSchedulerUseCase.register(any(RegisterCrawlSchedulerCommand.class)))
                    .willReturn(100L);

            // When & Then
            mockMvc.perform(
                            post("/api/v1/crawling/schedules")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data").value(100));

            // UseCase 호출 검증
            verify(crawlSchedulerCommandApiMapper)
                    .toCommand(any(RegisterCrawlSchedulerApiRequest.class));
            verify(registerCrawlSchedulerUseCase)
                    .register(any(RegisterCrawlSchedulerCommand.class));
        }

        @Test
        @DisplayName("실패: sellerId가 null인 경우 400 Bad Request")
        void registerCrawlScheduler_SellerIdNull_BadRequest() throws Exception {
            // Given
            String invalidRequest =
                    """
                    {
                        "sellerId": null,
                        "schedulerName": "테스트 스케줄러",
                        "cronExpression": "0 0 * * ? *"
                    }
                    """;

            // When & Then
            mockMvc.perform(
                            post("/api/v1/crawling/schedules")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(invalidRequest))
                    .andExpect(status().isBadRequest());

            // UseCase 호출되지 않아야 함
            verify(registerCrawlSchedulerUseCase, never()).register(any());
        }

        @Test
        @DisplayName("실패: schedulerName이 빈 문자열인 경우 400 Bad Request")
        void registerCrawlScheduler_SchedulerNameBlank_BadRequest() throws Exception {
            // Given
            RegisterCrawlSchedulerApiRequest request =
                    new RegisterCrawlSchedulerApiRequest(1L, "", "0 0 * * ? *");

            // When & Then
            mockMvc.perform(
                            post("/api/v1/crawling/schedules")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            // UseCase 호출되지 않아야 함
            verify(registerCrawlSchedulerUseCase, never()).register(any());
        }

        @Test
        @DisplayName("실패: cronExpression이 null인 경우 400 Bad Request")
        void registerCrawlScheduler_CronExpressionNull_BadRequest() throws Exception {
            // Given
            String invalidRequest =
                    """
                    {
                        "sellerId": 1,
                        "schedulerName": "테스트 스케줄러",
                        "cronExpression": null
                    }
                    """;

            // When & Then
            mockMvc.perform(
                            post("/api/v1/crawling/schedules")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(invalidRequest))
                    .andExpect(status().isBadRequest());

            // UseCase 호출되지 않아야 함
            verify(registerCrawlSchedulerUseCase, never()).register(any());
        }

        @Test
        @DisplayName("실패: schedulerName이 100자를 초과하는 경우 400 Bad Request")
        void registerCrawlScheduler_SchedulerNameTooLong_BadRequest() throws Exception {
            // Given
            String longName = "a".repeat(101);
            RegisterCrawlSchedulerApiRequest request =
                    new RegisterCrawlSchedulerApiRequest(1L, longName, "0 0 * * ? *");

            // When & Then
            mockMvc.perform(
                            post("/api/v1/crawling/schedules")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            // UseCase 호출되지 않아야 함
            verify(registerCrawlSchedulerUseCase, never()).register(any());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/crawling/schedules/{id} - 스케줄러 수정")
    class UpdateCrawlSchedulerTests {

        @Test
        @DisplayName("성공: 200 OK, 수정된 스케줄러 정보 반환")
        void updateCrawlScheduler_Success() throws Exception {
            // Given
            Long schedulerId = 100L;
            UpdateCrawlSchedulerApiRequest request =
                    new UpdateCrawlSchedulerApiRequest("수정된 스케줄러", "0 30 * * ? *", true);

            UpdateCrawlSchedulerCommand command =
                    new UpdateCrawlSchedulerCommand(schedulerId, "수정된 스케줄러", "0 30 * * ? *", true);

            Instant now = Instant.now();
            CrawlSchedulerResponse useCaseResponse =
                    new CrawlSchedulerResponse(
                            schedulerId,
                            1L,
                            "수정된 스케줄러",
                            "0 30 * * ? *",
                            SchedulerStatus.ACTIVE,
                            now.minusSeconds(3600),
                            now);

            CrawlSchedulerApiResponse apiResponse =
                    new CrawlSchedulerApiResponse(
                            schedulerId,
                            1L,
                            "수정된 스케줄러",
                            "0 30 * * ? *",
                            "ACTIVE",
                            now.minusSeconds(3600).toString(),
                            now.toString());

            given(
                            crawlSchedulerCommandApiMapper.toCommand(
                                    any(Long.class), any(UpdateCrawlSchedulerApiRequest.class)))
                    .willReturn(command);
            given(updateCrawlSchedulerUseCase.update(any(UpdateCrawlSchedulerCommand.class)))
                    .willReturn(useCaseResponse);
            given(crawlSchedulerCommandApiMapper.toApiResponse(any(CrawlSchedulerResponse.class)))
                    .willReturn(apiResponse);

            // When & Then
            mockMvc.perform(
                            patch("/api/v1/crawling/schedules/{id}", schedulerId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.crawlSchedulerId").value(schedulerId))
                    .andExpect(jsonPath("$.data.schedulerName").value("수정된 스케줄러"))
                    .andExpect(jsonPath("$.data.cronExpression").value("0 30 * * ? *"))
                    .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                    .andExpect(jsonPath("$.data.updatedAt").exists());

            // UseCase 호출 검증
            verify(updateCrawlSchedulerUseCase).update(any(UpdateCrawlSchedulerCommand.class));
        }

        @Test
        @DisplayName("실패: schedulerName이 null인 경우 400 Bad Request")
        void updateCrawlScheduler_SchedulerNameNull_BadRequest() throws Exception {
            // Given
            Long schedulerId = 100L;
            String invalidRequest =
                    """
                    {
                        "schedulerName": null,
                        "cronExpression": "0 30 * * ? *",
                        "active": true
                    }
                    """;

            // When & Then
            mockMvc.perform(
                            patch("/api/v1/crawling/schedules/{id}", schedulerId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(invalidRequest))
                    .andExpect(status().isBadRequest());

            // UseCase 호출되지 않아야 함
            verify(updateCrawlSchedulerUseCase, never()).update(any());
        }

        @Test
        @DisplayName("실패: cronExpression이 null인 경우 400 Bad Request")
        void updateCrawlScheduler_CronExpressionNull_BadRequest() throws Exception {
            // Given
            Long schedulerId = 100L;
            String invalidRequest =
                    """
                    {
                        "schedulerName": "스케줄러",
                        "cronExpression": null,
                        "active": true
                    }
                    """;

            // When & Then
            mockMvc.perform(
                            patch("/api/v1/crawling/schedules/{id}", schedulerId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(invalidRequest))
                    .andExpect(status().isBadRequest());

            // UseCase 호출되지 않아야 함
            verify(updateCrawlSchedulerUseCase, never()).update(any());
        }

        @Test
        @DisplayName("실패: active가 null인 경우 400 Bad Request")
        void updateCrawlScheduler_ActiveNull_BadRequest() throws Exception {
            // Given
            Long schedulerId = 100L;
            String invalidRequest =
                    """
                    {
                        "schedulerName": "스케줄러",
                        "cronExpression": "0 30 * * ? *",
                        "active": null
                    }
                    """;

            // When & Then
            mockMvc.perform(
                            patch("/api/v1/crawling/schedules/{id}", schedulerId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(invalidRequest))
                    .andExpect(status().isBadRequest());

            // UseCase 호출되지 않아야 함
            verify(updateCrawlSchedulerUseCase, never()).update(any());
        }

        @Test
        @DisplayName("실패: schedulerName이 100자를 초과하는 경우 400 Bad Request")
        void updateCrawlScheduler_SchedulerNameTooLong_BadRequest() throws Exception {
            // Given
            Long schedulerId = 100L;
            String longName = "a".repeat(101);
            UpdateCrawlSchedulerApiRequest request =
                    new UpdateCrawlSchedulerApiRequest(longName, "0 30 * * ? *", true);

            // When & Then
            mockMvc.perform(
                            patch("/api/v1/crawling/schedules/{id}", schedulerId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            // UseCase 호출되지 않아야 함
            verify(updateCrawlSchedulerUseCase, never()).update(any());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/crawling/schedules/{id}/trigger - 스케줄러 수동 트리거")
    class TriggerSchedulerTests {

        @Test
        @DisplayName("성공: 204 No Content, 응답 본문 없음")
        void triggerScheduler_Success() throws Exception {
            // Given
            Long schedulerId = 100L;

            // When & Then
            mockMvc.perform(
                            post("/api/v1/crawling/schedules/{id}/trigger", schedulerId)
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            // UseCase 호출 검증
            verify(triggerCrawlTaskUseCase).execute(any(TriggerCrawlTaskCommand.class));
        }
    }
}
