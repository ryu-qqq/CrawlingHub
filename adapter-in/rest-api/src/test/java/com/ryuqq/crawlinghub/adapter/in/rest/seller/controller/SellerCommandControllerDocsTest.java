package com.ryuqq.crawlinghub.adapter.in.rest.seller.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ryuqq.crawlinghub.adapter.in.rest.common.RestDocsTestSupport;
import com.ryuqq.crawlinghub.adapter.in.rest.config.TestConfiguration;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.command.RegisterSellerApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.command.UpdateSellerApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response.SellerApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.mapper.SellerCommandApiMapper;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;
import com.ryuqq.crawlinghub.application.seller.port.in.command.RegisterSellerUseCase;
import com.ryuqq.crawlinghub.application.seller.port.in.command.UpdateSellerUseCase;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * SellerCommandController REST Docs 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@WebMvcTest(SellerCommandController.class)
@ContextConfiguration(classes = TestConfiguration.class)
@DisplayName("SellerCommandController REST Docs")
class SellerCommandControllerDocsTest extends RestDocsTestSupport {

    @MockitoBean private RegisterSellerUseCase registerSellerUseCase;

    @MockitoBean private UpdateSellerUseCase updateSellerUseCase;

    @MockitoBean private SellerCommandApiMapper sellerCommandApiMapper;

    @Test
    @DisplayName("POST /api/v1/sellers - 셀러 등록 API 문서")
    void registerSeller() throws Exception {
        // given
        RegisterSellerApiRequest request = new RegisterSellerApiRequest("머스트잇 셀러명", "커머스 셀러명");

        SellerResponse useCaseResponse =
                new SellerResponse(
                        1L,
                        "머스트잇 셀러명",
                        "커머스 셀러명",
                        true,
                        LocalDateTime.of(2025, 11, 19, 10, 30, 0),
                        null);

        SellerApiResponse apiResponse =
                new SellerApiResponse(
                        1L,
                        "머스트잇 셀러명",
                        "커머스 셀러명",
                        "ACTIVE",
                        LocalDateTime.of(2025, 11, 19, 10, 30, 0),
                        null);

        given(sellerCommandApiMapper.toCommand(any())).willReturn(null);
        given(registerSellerUseCase.execute(any())).willReturn(useCaseResponse);
        given(sellerCommandApiMapper.toApiResponse(any())).willReturn(apiResponse);

        // when & then
        mockMvc.perform(
                        post("/api/v1/sellers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sellerId").value(1))
                .andDo(
                        document(
                                "seller-command/register",
                                requestFields(
                                        fieldWithPath("mustItSellerName")
                                                .type(JsonFieldType.STRING)
                                                .description("머스트잇 셀러 이름 (1-100자, 필수)"),
                                        fieldWithPath("sellerName")
                                                .type(JsonFieldType.STRING)
                                                .description("커머스 셀러 이름 (1-100자, 필수)")),
                                responseFields(
                                        fieldWithPath("success")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description("성공 여부"),
                                        fieldWithPath("data")
                                                .type(JsonFieldType.OBJECT)
                                                .description("응답 데이터"),
                                        fieldWithPath("data.sellerId")
                                                .type(JsonFieldType.NUMBER)
                                                .description("셀러 ID"),
                                        fieldWithPath("data.mustItSellerName")
                                                .type(JsonFieldType.STRING)
                                                .description("머스트잇 셀러명"),
                                        fieldWithPath("data.sellerName")
                                                .type(JsonFieldType.STRING)
                                                .description("커머스 셀러명"),
                                        fieldWithPath("data.status")
                                                .type(JsonFieldType.STRING)
                                                .description("상태 (ACTIVE/INACTIVE)"),
                                        fieldWithPath("data.createdAt")
                                                .type(JsonFieldType.STRING)
                                                .description("생성 일시"),
                                        fieldWithPath("data.updatedAt")
                                                .type(JsonFieldType.NULL)
                                                .description("수정 일시")
                                                .optional(),
                                        fieldWithPath("error")
                                                .type(JsonFieldType.NULL)
                                                .description("에러 정보")
                                                .optional(),
                                        fieldWithPath("timestamp")
                                                .type(JsonFieldType.STRING)
                                                .description("응답 시각"),
                                        fieldWithPath("requestId")
                                                .type(JsonFieldType.STRING)
                                                .description("요청 ID"))));
    }

    @Test
    @DisplayName("PATCH /api/v1/sellers/{id} - 셀러 수정 API 문서")
    void updateSeller() throws Exception {
        // given
        Long sellerId = 1L;
        UpdateSellerApiRequest request =
                new UpdateSellerApiRequest("새 머스트잇 셀러명", "새 커머스 셀러명", false);

        SellerResponse useCaseResponse =
                new SellerResponse(
                        1L,
                        "새 머스트잇 셀러명",
                        "새 커머스 셀러명",
                        false,
                        LocalDateTime.of(2025, 11, 19, 10, 30, 0),
                        LocalDateTime.of(2025, 11, 19, 11, 0, 0));

        SellerApiResponse apiResponse =
                new SellerApiResponse(
                        1L,
                        "새 머스트잇 셀러명",
                        "새 커머스 셀러명",
                        "INACTIVE",
                        LocalDateTime.of(2025, 11, 19, 10, 30, 0),
                        LocalDateTime.of(2025, 11, 19, 11, 0, 0));

        given(sellerCommandApiMapper.toCommand(any(), any())).willReturn(null);
        given(updateSellerUseCase.execute(any())).willReturn(useCaseResponse);
        given(sellerCommandApiMapper.toApiResponse(any())).willReturn(apiResponse);

        // when & then
        mockMvc.perform(
                        patch("/api/v1/sellers/{id}", sellerId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("INACTIVE"))
                .andDo(
                        document(
                                "seller-command/update",
                                pathParameters(parameterWithName("id").description("셀러 ID (양수)")),
                                requestFields(
                                        fieldWithPath("mustItSellerName")
                                                .type(JsonFieldType.STRING)
                                                .description("머스트잇 셀러명 (100자 이하, 선택)")
                                                .optional(),
                                        fieldWithPath("sellerName")
                                                .type(JsonFieldType.STRING)
                                                .description("커머스 셀러명 (100자 이하, 선택)")
                                                .optional(),
                                        fieldWithPath("active")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description(
                                                        "활성화 여부 (true=ACTIVE, false=INACTIVE, 선택)")
                                                .optional()),
                                responseFields(
                                        fieldWithPath("success")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description("성공 여부"),
                                        fieldWithPath("data")
                                                .type(JsonFieldType.OBJECT)
                                                .description("응답 데이터"),
                                        fieldWithPath("data.sellerId")
                                                .type(JsonFieldType.NUMBER)
                                                .description("셀러 ID"),
                                        fieldWithPath("data.mustItSellerName")
                                                .type(JsonFieldType.STRING)
                                                .description("머스트잇 셀러명"),
                                        fieldWithPath("data.sellerName")
                                                .type(JsonFieldType.STRING)
                                                .description("커머스 셀러명"),
                                        fieldWithPath("data.status")
                                                .type(JsonFieldType.STRING)
                                                .description("상태 (ACTIVE/INACTIVE)"),
                                        fieldWithPath("data.createdAt")
                                                .type(JsonFieldType.STRING)
                                                .description("생성 일시"),
                                        fieldWithPath("data.updatedAt")
                                                .type(JsonFieldType.STRING)
                                                .description("수정 일시"),
                                        fieldWithPath("error")
                                                .type(JsonFieldType.NULL)
                                                .description("에러 정보")
                                                .optional(),
                                        fieldWithPath("timestamp")
                                                .type(JsonFieldType.STRING)
                                                .description("응답 시각"),
                                        fieldWithPath("requestId")
                                                .type(JsonFieldType.STRING)
                                                .description("요청 ID"))));
    }
}
