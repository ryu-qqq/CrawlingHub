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

import com.ryuqq.crawlinghub.adapter.in.rest.common.RestDocsSecuritySnippets;
import com.ryuqq.crawlinghub.adapter.in.rest.common.RestDocsTestSupport;
import com.ryuqq.crawlinghub.adapter.in.rest.config.TestConfiguration;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.command.RegisterSellerApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.command.UpdateSellerApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.mapper.SellerCommandApiMapper;
import com.ryuqq.crawlinghub.application.seller.port.in.command.RegisterSellerUseCase;
import com.ryuqq.crawlinghub.application.seller.port.in.command.UpdateSellerUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
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
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = TestConfiguration.class)
@DisplayName("SellerCommandController REST Docs")
class SellerCommandControllerDocsTest extends RestDocsTestSupport {

    @MockitoBean private RegisterSellerUseCase registerSellerUseCase;

    @MockitoBean private UpdateSellerUseCase updateSellerUseCase;

    @MockitoBean private SellerCommandApiMapper sellerCommandApiMapper;

    @Test
    @DisplayName("POST /api/v1/crawling/sellers - 셀러 등록 API 문서")
    void registerSeller() throws Exception {
        // given
        RegisterSellerApiRequest request = new RegisterSellerApiRequest("머스트잇 셀러명", "커머스 셀러명");

        given(sellerCommandApiMapper.toCommand(any())).willReturn(null);
        given(registerSellerUseCase.execute(any())).willReturn(1L);

        // when & then
        mockMvc.perform(
                        post("/api/v1/crawling/sellers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data").value(1))
                .andDo(
                        document(
                                "seller-command/register",
                                RestDocsSecuritySnippets.authorization("seller:create"),
                                requestFields(
                                        fieldWithPath("mustItSellerName")
                                                .type(JsonFieldType.STRING)
                                                .description("머스트잇 셀러 이름 (1-100자, 필수)"),
                                        fieldWithPath("sellerName")
                                                .type(JsonFieldType.STRING)
                                                .description("커머스 셀러 이름 (1-100자, 필수)")),
                                responseFields(
                                        fieldWithPath("data")
                                                .type(JsonFieldType.NUMBER)
                                                .description("생성된 셀러 ID"),
                                        fieldWithPath("timestamp")
                                                .type(JsonFieldType.STRING)
                                                .description("응답 시각"),
                                        fieldWithPath("requestId")
                                                .type(JsonFieldType.STRING)
                                                .description("요청 ID"))));
    }

    @Test
    @DisplayName("PATCH /api/v1/crawling/sellers/{id} - 셀러 수정 API 문서")
    void updateSeller() throws Exception {
        // given
        Long sellerId = 1L;
        UpdateSellerApiRequest request =
                new UpdateSellerApiRequest("새 머스트잇 셀러명", "새 커머스 셀러명", false);

        given(sellerCommandApiMapper.toCommand(any(), any())).willReturn(null);

        // when & then
        mockMvc.perform(
                        patch("/api/v1/crawling/sellers/{id}", sellerId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "seller-command/update",
                                RestDocsSecuritySnippets.authorization("seller:update"),
                                pathParameters(parameterWithName("id").description("셀러 ID (양수)")),
                                requestFields(
                                        fieldWithPath("mustItSellerName")
                                                .type(JsonFieldType.STRING)
                                                .description("머스트잇 셀러명 (1-100자, 필수)"),
                                        fieldWithPath("sellerName")
                                                .type(JsonFieldType.STRING)
                                                .description("커머스 셀러명 (1-100자, 필수)"),
                                        fieldWithPath("active")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description(
                                                        "활성화 여부 (true=ACTIVE, false=INACTIVE,"
                                                                + " 필수)")),
                                responseFields(
                                        fieldWithPath("data")
                                                .type(JsonFieldType.NULL)
                                                .description("응답 데이터 (없음)")
                                                .optional(),
                                        fieldWithPath("timestamp")
                                                .type(JsonFieldType.STRING)
                                                .description("응답 시각"),
                                        fieldWithPath("requestId")
                                                .type(JsonFieldType.STRING)
                                                .description("요청 ID"))));
    }
}
