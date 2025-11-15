package com.ryuqq.crawlinghub.adapter.in.rest.seller;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.ryuqq.crawlinghub.adapter.in.rest.seller.controller.SellerController;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.RegisterSellerApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.RegisterSellerApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.UpdateSellerApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.UpdateSellerApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response.SellerDetailApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.mapper.SellerApiMapper;
import com.ryuqq.crawlinghub.application.seller.dto.command.RegisterSellerCommand;
import com.ryuqq.crawlinghub.application.seller.dto.command.UpdateSellerStatusCommand;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerDetailResponse;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;
import com.ryuqq.crawlinghub.application.seller.port.in.RegisterSellerUseCase;
import com.ryuqq.crawlinghub.application.seller.port.in.UpdateSellerStatusUseCase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * SellerController 단위 테스트
 *
 * <p>Mockito를 사용하여 Controller Layer만 격리 테스트합니다.
 * UseCase는 Mock으로 대체하여 Controller 로직만 검증합니다.
 *
 * <p>Spring REST Docs를 사용하여 API 문서를 자동 생성합니다.
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
@ExtendWith({MockitoExtension.class, RestDocumentationExtension.class})
@DisplayName("SellerController 단위 테스트")
class SellerControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private RegisterSellerUseCase registerSellerUseCase;

    @Mock
    private UpdateSellerStatusUseCase updateSellerStatusUseCase;

    @Mock
    private SellerApiMapper sellerApiMapper;

    @InjectMocks
    private SellerController sellerController;

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) {
        mockMvc = MockMvcBuilders
            .standaloneSetup(sellerController)
            .apply(documentationConfiguration(restDocumentation))
            .build();
        objectMapper = new ObjectMapper();
    }

    @Nested
    @DisplayName("POST /api/v1/sellers")
    class Describe_registerSeller {

        @Test
        @DisplayName("유효한 요청으로 셀러를 등록하면 201 Created를 반환한다")
        void it_registers_seller_and_returns_201() throws Exception {
            // Given
            RegisterSellerApiRequest request = new RegisterSellerApiRequest(
                "12345",
                "Test Seller",
                "HOURLY",
                1
            );
            String requestBody = objectMapper.writeValueAsString(request);

            RegisterSellerCommand command = new RegisterSellerCommand(
                "12345",
                "Test Seller"
            );

            LocalDateTime now = LocalDateTime.now();
            SellerResponse response = new SellerResponse(
                12345L,
                "12345",
                "Test Seller",
                com.ryuqq.crawlinghub.domain.seller.SellerStatus.ACTIVE,
                0,
                null,
                now,
                now
            );

            RegisterSellerApiResponse apiResponse = new RegisterSellerApiResponse(
                "12345",
                "Test Seller",
                true,
                "HOURLY",
                1,
                now
            );

            given(sellerApiMapper.toCommand(any(RegisterSellerApiRequest.class)))
                .willReturn(command);
            given(registerSellerUseCase.execute(any(RegisterSellerCommand.class)))
                .willReturn(response);
            given(sellerApiMapper.toResponse(response))
                .willReturn(apiResponse);

            // When & Then
            mockMvc.perform(post("/api/v1/sellers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sellerId").value(12345L))
                .andExpect(jsonPath("$.data.name").value("Test Seller"))
                .andDo(document("seller-register",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    requestFields(
                        fieldWithPath("sellerId").description("셀러 ID (머스트잇 고유 식별자)"),
                        fieldWithPath("name").description("셀러명"),
                        fieldWithPath("intervalType").description("크롤링 주기 타입 (HOURLY, DAILY, WEEKLY)"),
                        fieldWithPath("intervalValue").description("크롤링 주기 값 (양수)")
                    ),
                    responseFields(
                        fieldWithPath("success").description("성공 여부"),
                        fieldWithPath("data").description("응답 데이터"),
                        fieldWithPath("data.sellerId").description("셀러 ID"),
                        fieldWithPath("data.name").description("셀러명"),
                        fieldWithPath("data.isActive").description("활성 상태"),
                        fieldWithPath("data.intervalType").description("크롤링 주기 타입"),
                        fieldWithPath("data.intervalValue").description("크롤링 주기 값"),
                        fieldWithPath("data.createdAt").description("생성 시각"),
                        fieldWithPath("error").description("에러 정보").optional(),
                        fieldWithPath("timestamp").description("응답 타임스탬프"),
                        fieldWithPath("requestId").description("요청 ID")
                    )
                ));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/sellers/{sellerId}")
    class Describe_updateSeller {

        @Test
        @DisplayName("셀러 상태를 수정하면 200 OK를 반환한다")
        void it_updates_seller_status_and_returns_200() throws Exception {
            // Given
            Long sellerId = 12345L;
            UpdateSellerApiRequest request = new UpdateSellerApiRequest(
                false,
                "DAILY",
                2
            );
            String requestBody = objectMapper.writeValueAsString(request);

            UpdateSellerStatusCommand command = new UpdateSellerStatusCommand(
                sellerId,
                com.ryuqq.crawlinghub.domain.seller.SellerStatus.PAUSED
            );

            LocalDateTime now = LocalDateTime.now();
            SellerResponse response = new SellerResponse(
                sellerId,
                "12345",
                "Test Seller",
                com.ryuqq.crawlinghub.domain.seller.SellerStatus.PAUSED,
                0,
                null,
                now,
                now
            );

            UpdateSellerApiResponse apiResponse = new UpdateSellerApiResponse(
                "12345",
                "Test Seller",
                false,
                "DAILY",
                2,
                now
            );

            given(sellerApiMapper.toUpdateCommand(any(Long.class), any(UpdateSellerApiRequest.class)))
                .willReturn(command);
            given(updateSellerStatusUseCase.execute(any(UpdateSellerStatusCommand.class)))
                .willReturn(response);
            given(sellerApiMapper.toUpdateResponse(response))
                .willReturn(apiResponse);

            // When & Then
            mockMvc.perform(put("/api/v1/sellers/{sellerId}", sellerId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sellerId").value(sellerId))
                .andExpect(jsonPath("$.data.isActive").value(false))
                .andDo(document("seller-update",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    pathParameters(
                        parameterWithName("sellerId").description("셀러 ID")
                    ),
                    requestFields(
                        fieldWithPath("isActive").description("활성 상태 (선택)").optional(),
                        fieldWithPath("intervalType").description("크롤링 주기 타입 (선택)").optional(),
                        fieldWithPath("intervalValue").description("크롤링 주기 값 (선택)").optional()
                    ),
                    responseFields(
                        fieldWithPath("success").description("성공 여부"),
                        fieldWithPath("data").description("응답 데이터"),
                        fieldWithPath("data.sellerId").description("셀러 ID"),
                        fieldWithPath("data.name").description("셀러명"),
                        fieldWithPath("data.isActive").description("활성 상태"),
                        fieldWithPath("data.intervalType").description("크롤링 주기 타입"),
                        fieldWithPath("data.intervalValue").description("크롤링 주기 값"),
                        fieldWithPath("data.updatedAt").description("수정 시각"),
                        fieldWithPath("error").description("에러 정보").optional(),
                        fieldWithPath("timestamp").description("응답 타임스탬프"),
                        fieldWithPath("requestId").description("요청 ID")
                    )
                ));
        }
    }

}
