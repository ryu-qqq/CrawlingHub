package com.ryuqq.crawlinghub.adapter.in.rest.product.outbox.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ryuqq.crawlinghub.adapter.in.rest.common.RestDocsSecuritySnippets;
import com.ryuqq.crawlinghub.adapter.in.rest.common.RestDocsTestSupport;
import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.PageApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.config.TestConfiguration;
import com.ryuqq.crawlinghub.adapter.in.rest.product.outbox.dto.response.ProductImageOutboxApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.product.outbox.dto.response.ProductSyncOutboxApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.product.outbox.mapper.ProductOutboxQueryApiMapper;
import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.product.dto.response.ProductImageOutboxResponse;
import com.ryuqq.crawlinghub.application.product.dto.response.ProductSyncOutboxResponse;
import com.ryuqq.crawlinghub.application.product.port.in.query.SearchProductImageOutboxUseCase;
import com.ryuqq.crawlinghub.application.product.port.in.query.SearchProductSyncOutboxUseCase;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * ProductOutboxQueryController REST Docs 테스트
 *
 * <p>검증 범위:
 *
 * <ul>
 *   <li>SyncOutbox 목록 조회 API 문서화
 *   <li>ImageOutbox 목록 조회 API 문서화
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@WebMvcTest(ProductOutboxQueryController.class)
@ContextConfiguration(classes = TestConfiguration.class)
@DisplayName("ProductOutboxQueryController REST Docs")
class ProductOutboxQueryControllerDocsTest extends RestDocsTestSupport {

    @MockitoBean private SearchProductSyncOutboxUseCase searchSyncOutboxUseCase;

    @MockitoBean private SearchProductImageOutboxUseCase searchImageOutboxUseCase;

    @MockitoBean private ProductOutboxQueryApiMapper mapper;

    @Test
    @DisplayName("GET /api/v1/crawling/product-outbox/sync - SyncOutbox 목록 조회 API 문서")
    void searchSyncOutbox() throws Exception {
        // given
        Instant now = Instant.now();

        ProductSyncOutboxResponse response1 =
                new ProductSyncOutboxResponse(
                        1L,
                        100L,
                        10L,
                        12345L,
                        "CREATE",
                        "sync-10-12345-1234567890",
                        null,
                        ProductOutboxStatus.PENDING,
                        0,
                        null,
                        true,
                        now,
                        null);

        ProductSyncOutboxResponse response2 =
                new ProductSyncOutboxResponse(
                        2L,
                        101L,
                        10L,
                        12346L,
                        "UPDATE",
                        "sync-10-12346-1234567891",
                        1001L,
                        ProductOutboxStatus.FAILED,
                        3,
                        "Connection timeout",
                        true,
                        now.minusSeconds(3600),
                        now);

        PageResponse<ProductSyncOutboxResponse> useCaseResponse =
                new PageResponse<>(List.of(response1, response2), 0, 20, 2, 1, true, true);

        ProductSyncOutboxApiResponse apiResponse1 =
                new ProductSyncOutboxApiResponse(
                        1L,
                        100L,
                        10L,
                        12345L,
                        "CREATE",
                        "sync-10-12345-1234567890",
                        null,
                        "PENDING",
                        0,
                        null,
                        true,
                        now,
                        null);

        ProductSyncOutboxApiResponse apiResponse2 =
                new ProductSyncOutboxApiResponse(
                        2L,
                        101L,
                        10L,
                        12346L,
                        "UPDATE",
                        "sync-10-12346-1234567891",
                        1001L,
                        "FAILED",
                        3,
                        "Connection timeout",
                        true,
                        now.minusSeconds(3600),
                        now);

        PageApiResponse<ProductSyncOutboxApiResponse> apiPageResponse =
                new PageApiResponse<>(List.of(apiResponse1, apiResponse2), 0, 20, 2, 1, true, true);

        given(searchSyncOutboxUseCase.execute(any())).willReturn(useCaseResponse);
        given(mapper.toSyncQuery(any())).willReturn(null);
        given(mapper.toSyncPageApiResponse(any())).willReturn(apiPageResponse);

        // when & then
        mockMvc.perform(
                        get("/api/v1/crawling/product-outbox/sync")
                                .param("crawledProductId", "100")
                                .param("sellerId", "10")
                                .param("status", "FAILED")
                                .param("page", "0")
                                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andDo(
                        document(
                                "product-outbox-query/search-sync",
                                RestDocsSecuritySnippets.authorization("outbox:read"),
                                queryParameters(
                                        parameterWithName("crawledProductId")
                                                .description("CrawledProduct ID 필터 (선택)")
                                                .optional(),
                                        parameterWithName("sellerId")
                                                .description("Seller ID 필터 (선택)")
                                                .optional(),
                                        parameterWithName("status")
                                                .description(
                                                        "상태 필터 (PENDING, PROCESSING, COMPLETED,"
                                                                + " FAILED) (선택)")
                                                .optional(),
                                        parameterWithName("page")
                                                .description("페이지 번호 (0부터 시작, 기본값: 0)")
                                                .optional(),
                                        parameterWithName("size")
                                                .description("페이지 크기 (기본값: 20, 최대: 100)")
                                                .optional()),
                                responseFields(
                                        fieldWithPath("success")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description("성공 여부"),
                                        fieldWithPath("data")
                                                .type(JsonFieldType.OBJECT)
                                                .description("페이징 응답 데이터"),
                                        fieldWithPath("data.content")
                                                .type(JsonFieldType.ARRAY)
                                                .description("SyncOutbox 목록"),
                                        fieldWithPath("data.content[].id")
                                                .type(JsonFieldType.NUMBER)
                                                .description("Outbox ID"),
                                        fieldWithPath("data.content[].crawledProductId")
                                                .type(JsonFieldType.NUMBER)
                                                .description("CrawledProduct ID"),
                                        fieldWithPath("data.content[].sellerId")
                                                .type(JsonFieldType.NUMBER)
                                                .description("Seller ID"),
                                        fieldWithPath("data.content[].itemNo")
                                                .type(JsonFieldType.NUMBER)
                                                .description("상품 번호"),
                                        fieldWithPath("data.content[].syncType")
                                                .type(JsonFieldType.STRING)
                                                .description("동기화 타입 (CREATE/UPDATE)"),
                                        fieldWithPath("data.content[].idempotencyKey")
                                                .type(JsonFieldType.STRING)
                                                .description("멱등성 키"),
                                        fieldWithPath("data.content[].externalProductId")
                                                .type(JsonFieldType.NUMBER)
                                                .description("외부 상품 ID")
                                                .optional(),
                                        fieldWithPath("data.content[].status")
                                                .type(JsonFieldType.STRING)
                                                .description(
                                                        "상태 (PENDING, PROCESSING, COMPLETED,"
                                                                + " FAILED)"),
                                        fieldWithPath("data.content[].retryCount")
                                                .type(JsonFieldType.NUMBER)
                                                .description("재시도 횟수"),
                                        fieldWithPath("data.content[].errorMessage")
                                                .type(JsonFieldType.STRING)
                                                .description("에러 메시지")
                                                .optional(),
                                        fieldWithPath("data.content[].canRetry")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description("재시도 가능 여부"),
                                        fieldWithPath("data.content[].createdAt")
                                                .type(JsonFieldType.STRING)
                                                .description("생성 시각"),
                                        fieldWithPath("data.content[].processedAt")
                                                .type(JsonFieldType.STRING)
                                                .description("처리 시각")
                                                .optional(),
                                        fieldWithPath("data.page")
                                                .type(JsonFieldType.NUMBER)
                                                .description("현재 페이지 번호"),
                                        fieldWithPath("data.size")
                                                .type(JsonFieldType.NUMBER)
                                                .description("페이지 크기"),
                                        fieldWithPath("data.totalElements")
                                                .type(JsonFieldType.NUMBER)
                                                .description("전체 요소 수"),
                                        fieldWithPath("data.totalPages")
                                                .type(JsonFieldType.NUMBER)
                                                .description("전체 페이지 수"),
                                        fieldWithPath("data.last")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description("마지막 페이지 여부"),
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
    @DisplayName("GET /api/v1/crawling/product-outbox/image - ImageOutbox 목록 조회 API 문서")
    void searchImageOutbox() throws Exception {
        // given
        Instant now = Instant.now();

        ProductImageOutboxResponse response1 =
                new ProductImageOutboxResponse(
                        1L,
                        100L,
                        "image-100-1234567890",
                        ProductOutboxStatus.PENDING,
                        0,
                        null,
                        true,
                        now,
                        null);

        ProductImageOutboxResponse response2 =
                new ProductImageOutboxResponse(
                        2L,
                        101L,
                        "image-101-1234567891",
                        ProductOutboxStatus.FAILED,
                        3,
                        "Upload failed: Network error",
                        true,
                        now.minusSeconds(3600),
                        now);

        PageResponse<ProductImageOutboxResponse> useCaseResponse =
                new PageResponse<>(List.of(response1, response2), 0, 20, 2, 1, true, true);

        ProductImageOutboxApiResponse apiResponse1 =
                new ProductImageOutboxApiResponse(
                        1L, 100L, "image-100-1234567890", "PENDING", 0, null, true, now, null);

        ProductImageOutboxApiResponse apiResponse2 =
                new ProductImageOutboxApiResponse(
                        2L,
                        101L,
                        "image-101-1234567891",
                        "FAILED",
                        3,
                        "Upload failed: Network error",
                        true,
                        now.minusSeconds(3600),
                        now);

        PageApiResponse<ProductImageOutboxApiResponse> apiPageResponse =
                new PageApiResponse<>(List.of(apiResponse1, apiResponse2), 0, 20, 2, 1, true, true);

        given(searchImageOutboxUseCase.execute(any())).willReturn(useCaseResponse);
        given(mapper.toImageQuery(any())).willReturn(null);
        given(mapper.toImagePageApiResponse(any())).willReturn(apiPageResponse);

        // when & then
        mockMvc.perform(
                        get("/api/v1/crawling/product-outbox/image")
                                .param("crawledProductImageId", "100")
                                .param("status", "FAILED")
                                .param("page", "0")
                                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andDo(
                        document(
                                "product-outbox-query/search-image",
                                RestDocsSecuritySnippets.authorization("outbox:read"),
                                queryParameters(
                                        parameterWithName("crawledProductImageId")
                                                .description("CrawledProductImage ID 필터 (선택)")
                                                .optional(),
                                        parameterWithName("status")
                                                .description(
                                                        "상태 필터 (PENDING, PROCESSING, COMPLETED,"
                                                                + " FAILED) (선택)")
                                                .optional(),
                                        parameterWithName("page")
                                                .description("페이지 번호 (0부터 시작, 기본값: 0)")
                                                .optional(),
                                        parameterWithName("size")
                                                .description("페이지 크기 (기본값: 20, 최대: 100)")
                                                .optional()),
                                responseFields(
                                        fieldWithPath("success")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description("성공 여부"),
                                        fieldWithPath("data")
                                                .type(JsonFieldType.OBJECT)
                                                .description("페이징 응답 데이터"),
                                        fieldWithPath("data.content")
                                                .type(JsonFieldType.ARRAY)
                                                .description("ImageOutbox 목록"),
                                        fieldWithPath("data.content[].id")
                                                .type(JsonFieldType.NUMBER)
                                                .description("Outbox ID"),
                                        fieldWithPath("data.content[].crawledProductImageId")
                                                .type(JsonFieldType.NUMBER)
                                                .description("CrawledProductImage ID"),
                                        fieldWithPath("data.content[].idempotencyKey")
                                                .type(JsonFieldType.STRING)
                                                .description("멱등성 키"),
                                        fieldWithPath("data.content[].status")
                                                .type(JsonFieldType.STRING)
                                                .description(
                                                        "상태 (PENDING, PROCESSING, COMPLETED,"
                                                                + " FAILED)"),
                                        fieldWithPath("data.content[].retryCount")
                                                .type(JsonFieldType.NUMBER)
                                                .description("재시도 횟수"),
                                        fieldWithPath("data.content[].errorMessage")
                                                .type(JsonFieldType.STRING)
                                                .description("에러 메시지")
                                                .optional(),
                                        fieldWithPath("data.content[].canRetry")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description("재시도 가능 여부"),
                                        fieldWithPath("data.content[].createdAt")
                                                .type(JsonFieldType.STRING)
                                                .description("생성 시각"),
                                        fieldWithPath("data.content[].processedAt")
                                                .type(JsonFieldType.STRING)
                                                .description("처리 시각")
                                                .optional(),
                                        fieldWithPath("data.page")
                                                .type(JsonFieldType.NUMBER)
                                                .description("현재 페이지 번호"),
                                        fieldWithPath("data.size")
                                                .type(JsonFieldType.NUMBER)
                                                .description("페이지 크기"),
                                        fieldWithPath("data.totalElements")
                                                .type(JsonFieldType.NUMBER)
                                                .description("전체 요소 수"),
                                        fieldWithPath("data.totalPages")
                                                .type(JsonFieldType.NUMBER)
                                                .description("전체 페이지 수"),
                                        fieldWithPath("data.last")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description("마지막 페이지 여부"),
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
