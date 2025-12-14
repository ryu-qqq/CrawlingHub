package com.ryuqq.crawlinghub.adapter.in.rest.seller.controller;

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
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.command.RegisterSellerApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.command.UpdateSellerApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response.SellerApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.mapper.SellerCommandApiMapper;
import com.ryuqq.crawlinghub.application.seller.dto.command.RegisterSellerCommand;
import com.ryuqq.crawlinghub.application.seller.dto.command.UpdateSellerCommand;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;
import com.ryuqq.crawlinghub.application.seller.port.in.command.RegisterSellerUseCase;
import com.ryuqq.crawlinghub.application.seller.port.in.command.UpdateSellerUseCase;
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
 * SellerCommandController 단위 테스트
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
@DisplayName("SellerCommandController 단위 테스트")
@Tag("unit")
@Tag("adapter-rest")
class SellerCommandControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private RegisterSellerUseCase registerSellerUseCase;
    private UpdateSellerUseCase updateSellerUseCase;
    private SellerCommandApiMapper sellerCommandApiMapper;

    @BeforeEach
    void setUp() {
        registerSellerUseCase = mock(RegisterSellerUseCase.class);
        updateSellerUseCase = mock(UpdateSellerUseCase.class);
        sellerCommandApiMapper = mock(SellerCommandApiMapper.class);

        SellerCommandController controller =
                new SellerCommandController(
                        registerSellerUseCase, updateSellerUseCase, sellerCommandApiMapper);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc =
                MockMvcBuilders.standaloneSetup(controller)
                        .addPlaceholderValue("api.endpoints.base-v1", "/api/v1")
                        .addPlaceholderValue("api.endpoints.seller.base", "/sellers")
                        .addPlaceholderValue("api.endpoints.seller.by-id", "/{id}")
                        .setValidator(validator)
                        .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Nested
    @DisplayName("POST /api/v1/crawling/sellers - 셀러 등록")
    class RegisterSellerTests {

        @Test
        @DisplayName("성공: 201 Created, Location Header, Response 구조 검증")
        void registerSeller_Success() throws Exception {
            // Given
            RegisterSellerApiRequest request =
                    new RegisterSellerApiRequest("머스트잇 테스트 셀러", "테스트 셀러");

            RegisterSellerCommand command = new RegisterSellerCommand("머스트잇 테스트 셀러", "테스트 셀러");

            SellerResponse useCaseResponse =
                    new SellerResponse(1L, "머스트잇 테스트 셀러", "테스트 셀러", true, Instant.now(), null);

            SellerApiResponse apiResponse =
                    new SellerApiResponse(
                            1L, "머스트잇 테스트 셀러", "테스트 셀러", "ACTIVE", Instant.now().toString(), null);

            given(sellerCommandApiMapper.toCommand(any(RegisterSellerApiRequest.class)))
                    .willReturn(command);
            given(registerSellerUseCase.execute(any(RegisterSellerCommand.class)))
                    .willReturn(useCaseResponse);
            given(sellerCommandApiMapper.toApiResponse(any(SellerResponse.class)))
                    .willReturn(apiResponse);

            // When & Then
            mockMvc.perform(
                            post("/api/v1/crawling/sellers")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.sellerId").value(1))
                    .andExpect(jsonPath("$.data.mustItSellerName").value("머스트잇 테스트 셀러"))
                    .andExpect(jsonPath("$.data.sellerName").value("테스트 셀러"))
                    .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                    .andExpect(jsonPath("$.data.createdAt").exists())
                    .andExpect(jsonPath("$.error").isEmpty());

            // UseCase 호출 검증
            verify(sellerCommandApiMapper).toCommand(any(RegisterSellerApiRequest.class));
            verify(registerSellerUseCase).execute(any(RegisterSellerCommand.class));
            verify(sellerCommandApiMapper).toApiResponse(any(SellerResponse.class));
        }

        @Test
        @DisplayName("실패: mustItSellerName이 null인 경우 400 Bad Request")
        void registerSeller_MustItSellerNameNull_BadRequest() throws Exception {
            // Given
            String invalidRequest =
                    """
                    {
                        "mustItSellerName": null,
                        "sellerName": "테스트 셀러"
                    }
                    """;

            // When & Then
            mockMvc.perform(
                            post("/api/v1/crawling/sellers")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(invalidRequest))
                    .andExpect(status().isBadRequest());

            // UseCase 호출되지 않아야 함
            verify(registerSellerUseCase, never()).execute(any());
        }

        @Test
        @DisplayName("실패: mustItSellerName이 빈 문자열인 경우 400 Bad Request")
        void registerSeller_MustItSellerNameBlank_BadRequest() throws Exception {
            // Given
            RegisterSellerApiRequest request = new RegisterSellerApiRequest("", "테스트 셀러");

            // When & Then
            mockMvc.perform(
                            post("/api/v1/crawling/sellers")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            // UseCase 호출되지 않아야 함
            verify(registerSellerUseCase, never()).execute(any());
        }

        @Test
        @DisplayName("실패: sellerName이 null인 경우 400 Bad Request")
        void registerSeller_SellerNameNull_BadRequest() throws Exception {
            // Given
            String invalidRequest =
                    """
                    {
                        "mustItSellerName": "머스트잇 테스트 셀러",
                        "sellerName": null
                    }
                    """;

            // When & Then
            mockMvc.perform(
                            post("/api/v1/crawling/sellers")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(invalidRequest))
                    .andExpect(status().isBadRequest());

            // UseCase 호출되지 않아야 함
            verify(registerSellerUseCase, never()).execute(any());
        }

        @Test
        @DisplayName("실패: mustItSellerName이 100자를 초과하는 경우 400 Bad Request")
        void registerSeller_MustItSellerNameTooLong_BadRequest() throws Exception {
            // Given
            String longName = "a".repeat(101);
            RegisterSellerApiRequest request = new RegisterSellerApiRequest(longName, "테스트 셀러");

            // When & Then
            mockMvc.perform(
                            post("/api/v1/crawling/sellers")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            // UseCase 호출되지 않아야 함
            verify(registerSellerUseCase, never()).execute(any());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/crawling/sellers/{id} - 셀러 수정")
    class UpdateSellerTests {

        @Test
        @DisplayName("성공: 200 OK, Request Body 변환 검증")
        void updateSeller_Success() throws Exception {
            // Given
            Long sellerId = 1L;
            UpdateSellerApiRequest request =
                    new UpdateSellerApiRequest("수정된 머스트잇 셀러명", "수정된 셀러명", false);

            UpdateSellerCommand command =
                    new UpdateSellerCommand(sellerId, "수정된 머스트잇 셀러명", "수정된 셀러명", false);

            SellerResponse useCaseResponse =
                    new SellerResponse(
                            sellerId,
                            "수정된 머스트잇 셀러명",
                            "수정된 셀러명",
                            false,
                            Instant.now().minus(java.time.Duration.ofDays(1)),
                            Instant.now());

            SellerApiResponse apiResponse =
                    new SellerApiResponse(
                            sellerId,
                            "수정된 머스트잇 셀러명",
                            "수정된 셀러명",
                            "INACTIVE",
                            Instant.now().minus(java.time.Duration.ofDays(1)).toString(),
                            Instant.now().toString());

            given(
                            sellerCommandApiMapper.toCommand(
                                    any(Long.class), any(UpdateSellerApiRequest.class)))
                    .willReturn(command);
            given(updateSellerUseCase.execute(any(UpdateSellerCommand.class)))
                    .willReturn(useCaseResponse);
            given(sellerCommandApiMapper.toApiResponse(any(SellerResponse.class)))
                    .willReturn(apiResponse);

            // When & Then
            mockMvc.perform(
                            patch("/api/v1/crawling/sellers/{id}", sellerId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.sellerId").value(sellerId))
                    .andExpect(jsonPath("$.data.mustItSellerName").value("수정된 머스트잇 셀러명"))
                    .andExpect(jsonPath("$.data.sellerName").value("수정된 셀러명"))
                    .andExpect(jsonPath("$.data.status").value("INACTIVE"))
                    .andExpect(jsonPath("$.data.updatedAt").exists())
                    .andExpect(jsonPath("$.error").isEmpty());

            // UseCase 호출 검증
            verify(sellerCommandApiMapper)
                    .toCommand(any(Long.class), any(UpdateSellerApiRequest.class));
            verify(updateSellerUseCase).execute(any(UpdateSellerCommand.class));
            verify(sellerCommandApiMapper).toApiResponse(any(SellerResponse.class));
        }

        @Test
        @DisplayName("성공: 부분 업데이트 (mustItSellerName만 수정)")
        void updateSeller_PartialUpdate_MustItSellerNameOnly() throws Exception {
            // Given
            Long sellerId = 1L;
            UpdateSellerApiRequest request = new UpdateSellerApiRequest("새로운 머스트잇 셀러명", null, null);

            UpdateSellerCommand command =
                    new UpdateSellerCommand(sellerId, "새로운 머스트잇 셀러명", null, null);

            SellerResponse useCaseResponse =
                    new SellerResponse(
                            sellerId,
                            "새로운 머스트잇 셀러명",
                            "기존 셀러명",
                            true,
                            Instant.now().minus(java.time.Duration.ofDays(1)),
                            Instant.now());

            SellerApiResponse apiResponse =
                    new SellerApiResponse(
                            sellerId,
                            "새로운 머스트잇 셀러명",
                            "기존 셀러명",
                            "ACTIVE",
                            Instant.now().minus(java.time.Duration.ofDays(1)).toString(),
                            Instant.now().toString());

            given(
                            sellerCommandApiMapper.toCommand(
                                    any(Long.class), any(UpdateSellerApiRequest.class)))
                    .willReturn(command);
            given(updateSellerUseCase.execute(any(UpdateSellerCommand.class)))
                    .willReturn(useCaseResponse);
            given(sellerCommandApiMapper.toApiResponse(any(SellerResponse.class)))
                    .willReturn(apiResponse);

            // When & Then
            mockMvc.perform(
                            patch("/api/v1/crawling/sellers/{id}", sellerId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.sellerId").value(sellerId))
                    .andExpect(jsonPath("$.data.mustItSellerName").value("새로운 머스트잇 셀러명"))
                    .andExpect(jsonPath("$.error").isEmpty());

            // UseCase 호출 검증
            verify(updateSellerUseCase).execute(any(UpdateSellerCommand.class));
        }

        @Test
        @DisplayName("성공: 상태만 변경 (active=false)")
        void updateSeller_StatusChangeOnly() throws Exception {
            // Given
            Long sellerId = 1L;
            UpdateSellerApiRequest request = new UpdateSellerApiRequest(null, null, false);

            UpdateSellerCommand command = new UpdateSellerCommand(sellerId, null, null, false);

            SellerResponse useCaseResponse =
                    new SellerResponse(
                            sellerId,
                            "기존 머스트잇 셀러명",
                            "기존 셀러명",
                            false,
                            Instant.now().minus(java.time.Duration.ofDays(1)),
                            Instant.now());

            SellerApiResponse apiResponse =
                    new SellerApiResponse(
                            sellerId,
                            "기존 머스트잇 셀러명",
                            "기존 셀러명",
                            "INACTIVE",
                            Instant.now().minus(java.time.Duration.ofDays(1)).toString(),
                            Instant.now().toString());

            given(
                            sellerCommandApiMapper.toCommand(
                                    any(Long.class), any(UpdateSellerApiRequest.class)))
                    .willReturn(command);
            given(updateSellerUseCase.execute(any(UpdateSellerCommand.class)))
                    .willReturn(useCaseResponse);
            given(sellerCommandApiMapper.toApiResponse(any(SellerResponse.class)))
                    .willReturn(apiResponse);

            // When & Then
            mockMvc.perform(
                            patch("/api/v1/crawling/sellers/{id}", sellerId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.status").value("INACTIVE"))
                    .andExpect(jsonPath("$.error").isEmpty());

            // UseCase 호출 검증
            verify(updateSellerUseCase).execute(any(UpdateSellerCommand.class));
        }

        /**
         * PathVariable @Positive validation은 AOP 기반 MethodValidationInterceptor가 필요합니다.
         * standaloneSetup에서는 지원되지 않으므로 통합 테스트에서 검증합니다.
         */
        @Test
        @DisplayName("실패: sellerId가 0 이하인 경우 400 Bad Request")
        @org.junit.jupiter.api.Disabled("PathVariable validation은 통합 테스트에서 검증")
        void updateSeller_InvalidSellerId_BadRequest() throws Exception {
            // Given
            Long invalidSellerId = 0L;
            UpdateSellerApiRequest request =
                    new UpdateSellerApiRequest("수정된 머스트잇 셀러명", "수정된 셀러명", true);

            // When & Then
            mockMvc.perform(
                            patch("/api/v1/crawling/sellers/{id}", invalidSellerId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            // UseCase 호출되지 않아야 함
            verify(updateSellerUseCase, never()).execute(any());
        }

        @Test
        @DisplayName("실패: mustItSellerName이 100자를 초과하는 경우 400 Bad Request")
        void updateSeller_MustItSellerNameTooLong_BadRequest() throws Exception {
            // Given
            Long sellerId = 1L;
            String longName = "a".repeat(101);
            UpdateSellerApiRequest request = new UpdateSellerApiRequest(longName, "수정된 셀러명", true);

            // When & Then
            mockMvc.perform(
                            patch("/api/v1/crawling/sellers/{id}", sellerId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            // UseCase 호출되지 않아야 함
            verify(updateSellerUseCase, never()).execute(any());
        }
    }
}
