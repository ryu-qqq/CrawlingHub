package com.ryuqq.crawlinghub.adapter.in.rest.product.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ryuqq.crawlinghub.adapter.in.rest.common.RestDocsSecuritySnippets;
import com.ryuqq.crawlinghub.adapter.in.rest.common.RestDocsTestSupport;
import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.PageApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.config.TestConfiguration;
import com.ryuqq.crawlinghub.adapter.in.rest.product.dto.response.CrawledProductDetailApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.product.dto.response.CrawledProductDetailApiResponse.CategoryInfo;
import com.ryuqq.crawlinghub.adapter.in.rest.product.dto.response.CrawledProductDetailApiResponse.CrawlStatusInfo;
import com.ryuqq.crawlinghub.adapter.in.rest.product.dto.response.CrawledProductDetailApiResponse.ImageInfo;
import com.ryuqq.crawlinghub.adapter.in.rest.product.dto.response.CrawledProductDetailApiResponse.ImagesInfo;
import com.ryuqq.crawlinghub.adapter.in.rest.product.dto.response.CrawledProductDetailApiResponse.OptionInfo;
import com.ryuqq.crawlinghub.adapter.in.rest.product.dto.response.CrawledProductDetailApiResponse.OptionsInfo;
import com.ryuqq.crawlinghub.adapter.in.rest.product.dto.response.CrawledProductDetailApiResponse.PriceInfo;
import com.ryuqq.crawlinghub.adapter.in.rest.product.dto.response.CrawledProductDetailApiResponse.ShippingInfo;
import com.ryuqq.crawlinghub.adapter.in.rest.product.dto.response.CrawledProductDetailApiResponse.SyncStatusInfo;
import com.ryuqq.crawlinghub.adapter.in.rest.product.dto.response.CrawledProductSummaryApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.product.mapper.CrawledProductQueryApiMapper;
import com.ryuqq.crawlinghub.application.product.port.in.query.GetCrawledProductDetailUseCase;
import com.ryuqq.crawlinghub.application.product.port.in.query.SearchCrawledProductsUseCase;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * CrawledProductQueryController REST Docs 테스트
 *
 * <p>검증 범위:
 *
 * <ul>
 *   <li>크롤링 상품 목록 조회 API 문서화
 *   <li>크롤링 상품 상세 조회 API 문서화
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@WebMvcTest(CrawledProductQueryController.class)
@ContextConfiguration(classes = TestConfiguration.class)
@DisplayName("CrawledProductQueryController REST Docs")
class CrawledProductQueryControllerDocsTest extends RestDocsTestSupport {

    @MockitoBean private SearchCrawledProductsUseCase searchCrawledProductsUseCase;

    @MockitoBean private GetCrawledProductDetailUseCase getCrawledProductDetailUseCase;

    @MockitoBean private CrawledProductQueryApiMapper crawledProductQueryApiMapper;

    @Test
    @DisplayName("GET /api/v1/crawling/crawled-products - 크롤링 상품 목록 조회 API 문서")
    void searchCrawledProducts() throws Exception {
        // given
        List<CrawledProductSummaryApiResponse> content =
                List.of(
                        new CrawledProductSummaryApiResponse(
                                1L,
                                100L,
                                12345L,
                                "테스트 상품 1",
                                "테스트 브랜드",
                                50000,
                                10,
                                3,
                                "",
                                true,
                                null,
                                null,
                                true,
                                100,
                                "2025-01-01T10:00:00Z",
                                "2025-01-02T15:30:00Z"),
                        new CrawledProductSummaryApiResponse(
                                2L,
                                100L,
                                12346L,
                                "테스트 상품 2",
                                "테스트 브랜드",
                                75000,
                                15,
                                2,
                                "OPTION",
                                false,
                                999L,
                                "2025-01-01T12:00:00Z",
                                false,
                                50,
                                "2025-01-01T11:00:00Z",
                                "2025-01-02T16:30:00Z"));

        PageApiResponse<CrawledProductSummaryApiResponse> apiPageResponse =
                new PageApiResponse<>(content, 0, 20, 2, 1, true, true);

        given(crawledProductQueryApiMapper.toQuery(any())).willReturn(null);
        given(searchCrawledProductsUseCase.execute(any())).willReturn(null);
        given(crawledProductQueryApiMapper.toPageApiResponse(any())).willReturn(apiPageResponse);

        // when & then
        mockMvc.perform(
                        get("/api/v1/crawling/crawled-products")
                                .param("sellerId", "100")
                                .param("needsSync", "true")
                                .param("page", "0")
                                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andDo(
                        document(
                                "crawled-product-query/search",
                                RestDocsSecuritySnippets.authorization("product:read"),
                                queryParameters(
                                        parameterWithName("sellerId")
                                                .description("셀러 ID 필터 (1 이상, 선택)")
                                                .optional(),
                                        parameterWithName("itemNo")
                                                .description("상품 번호 필터 (1 이상, 선택)")
                                                .optional(),
                                        parameterWithName("itemName")
                                                .description("상품명 부분 일치 검색 (선택)")
                                                .optional(),
                                        parameterWithName("brandName")
                                                .description("브랜드명 부분 일치 검색 (선택)")
                                                .optional(),
                                        parameterWithName("needsSync")
                                                .description("동기화 필요 여부 필터 (true/false, 선택)")
                                                .optional(),
                                        parameterWithName("allCrawled")
                                                .description("모든 크롤링 완료 여부 필터 (true/false, 선택)")
                                                .optional(),
                                        parameterWithName("hasExternalId")
                                                .description("외부 상품 ID 존재 여부 필터 (true/false, 선택)")
                                                .optional(),
                                        parameterWithName("page")
                                                .description("페이지 번호 (0부터 시작, 기본값: 0)")
                                                .optional(),
                                        parameterWithName("size")
                                                .description("페이지 크기 (1-100, 기본값: 20)")
                                                .optional()),
                                responseFields(
                                        fieldWithPath("success")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description("성공 여부"),
                                        fieldWithPath("data")
                                                .type(JsonFieldType.OBJECT)
                                                .description("응답 데이터"),
                                        fieldWithPath("data.content")
                                                .type(JsonFieldType.ARRAY)
                                                .description("크롤링 상품 목록"),
                                        fieldWithPath("data.content[].id")
                                                .type(JsonFieldType.NUMBER)
                                                .description("크롤링 상품 ID"),
                                        fieldWithPath("data.content[].sellerId")
                                                .type(JsonFieldType.NUMBER)
                                                .description("셀러 ID"),
                                        fieldWithPath("data.content[].itemNo")
                                                .type(JsonFieldType.NUMBER)
                                                .description("상품 번호"),
                                        fieldWithPath("data.content[].itemName")
                                                .type(JsonFieldType.STRING)
                                                .description("상품명"),
                                        fieldWithPath("data.content[].brandName")
                                                .type(JsonFieldType.STRING)
                                                .description("브랜드명"),
                                        fieldWithPath("data.content[].price")
                                                .type(JsonFieldType.NUMBER)
                                                .description("판매가"),
                                        fieldWithPath("data.content[].discountRate")
                                                .type(JsonFieldType.NUMBER)
                                                .description("할인율 (%)"),
                                        fieldWithPath("data.content[].completedCrawlCount")
                                                .type(JsonFieldType.NUMBER)
                                                .description("완료된 크롤링 수"),
                                        fieldWithPath("data.content[].pendingCrawlTypes")
                                                .type(JsonFieldType.STRING)
                                                .description("대기 중인 크롤링 유형 (빈 문자열이면 모두 완료)"),
                                        fieldWithPath("data.content[].needsSync")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description("동기화 필요 여부"),
                                        fieldWithPath("data.content[].externalProductId")
                                                .type(JsonFieldType.NUMBER)
                                                .description("외부 상품 ID")
                                                .optional(),
                                        fieldWithPath("data.content[].lastSyncedAt")
                                                .type(JsonFieldType.STRING)
                                                .description("마지막 동기화 시간")
                                                .optional(),
                                        fieldWithPath("data.content[].allImagesUploaded")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description("모든 이미지 업로드 완료 여부"),
                                        fieldWithPath("data.content[].totalStock")
                                                .type(JsonFieldType.NUMBER)
                                                .description("전체 재고 수량"),
                                        fieldWithPath("data.content[].createdAt")
                                                .type(JsonFieldType.STRING)
                                                .description("생성 일시"),
                                        fieldWithPath("data.content[].updatedAt")
                                                .type(JsonFieldType.STRING)
                                                .description("수정 일시"),
                                        fieldWithPath("data.page")
                                                .type(JsonFieldType.NUMBER)
                                                .description("현재 페이지 번호"),
                                        fieldWithPath("data.size")
                                                .type(JsonFieldType.NUMBER)
                                                .description("페이지 크기"),
                                        fieldWithPath("data.totalElements")
                                                .type(JsonFieldType.NUMBER)
                                                .description("전체 데이터 개수"),
                                        fieldWithPath("data.totalPages")
                                                .type(JsonFieldType.NUMBER)
                                                .description("전체 페이지 수"),
                                        fieldWithPath("data.first")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description("첫 페이지 여부"),
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
    @DisplayName("GET /api/v1/crawling/crawled-products/{id} - 크롤링 상품 상세 조회 API 문서")
    void getCrawledProductDetail() throws Exception {
        // given
        Long crawledProductId = 1L;

        CrawledProductDetailApiResponse apiResponse =
                new CrawledProductDetailApiResponse(
                        crawledProductId,
                        100L,
                        12345L,
                        "테스트 상품",
                        "테스트 브랜드",
                        "ACTIVE",
                        "KR",
                        "서울",
                        true,
                        new PriceInfo(50000, 60000, 55000, 48000, 10, 12),
                        new ImagesInfo(
                                List.of(
                                        new ImageInfo(
                                                "https://example.com/thumb1.jpg",
                                                "https://s3.example.com/thumb1.jpg",
                                                "COMPLETED",
                                                1)),
                                List.of(
                                        new ImageInfo(
                                                "https://example.com/detail1.jpg",
                                                "https://s3.example.com/detail1.jpg",
                                                "COMPLETED",
                                                1)),
                                2,
                                2),
                        new CategoryInfo(
                                "패션 > 남성의류 > 티셔츠", "100", "패션", "110", "남성의류", "111", "티셔츠"),
                        new ShippingInfo("PARCEL", 3000, "CONDITIONAL_FREE", 3, false),
                        new OptionsInfo(
                                List.of(
                                        new OptionInfo(1L, "블랙", "M", 50),
                                        new OptionInfo(2L, "블랙", "L", 30)),
                                80,
                                2,
                                0,
                                List.of("블랙"),
                                List.of("M", "L")),
                        new CrawlStatusInfo(
                                "2025-01-01T10:00:00Z",
                                "2025-01-01T11:00:00Z",
                                "2025-01-01T12:00:00Z",
                                3,
                                List.of()),
                        new SyncStatusInfo(null, true, null, true),
                        "<p>상품 상세 설명 HTML</p>",
                        "2025-01-01T10:00:00Z",
                        "2025-01-02T15:30:00Z");

        given(getCrawledProductDetailUseCase.execute(anyLong())).willReturn(null);
        given(crawledProductQueryApiMapper.toDetailApiResponse(any())).willReturn(apiResponse);

        // when & then
        mockMvc.perform(get("/api/v1/crawling/crawled-products/{id}", crawledProductId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(crawledProductId))
                .andExpect(jsonPath("$.data.itemName").value("테스트 상품"))
                .andDo(
                        document(
                                "crawled-product-query/detail",
                                RestDocsSecuritySnippets.authorization("product:read"),
                                pathParameters(
                                        parameterWithName("id").description("크롤링 상품 ID (양수, 필수)")),
                                responseFields(
                                        fieldWithPath("success")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description("성공 여부"),
                                        fieldWithPath("data")
                                                .type(JsonFieldType.OBJECT)
                                                .description("응답 데이터"),
                                        fieldWithPath("data.id")
                                                .type(JsonFieldType.NUMBER)
                                                .description("크롤링 상품 ID"),
                                        fieldWithPath("data.sellerId")
                                                .type(JsonFieldType.NUMBER)
                                                .description("셀러 ID"),
                                        fieldWithPath("data.itemNo")
                                                .type(JsonFieldType.NUMBER)
                                                .description("상품 번호"),
                                        fieldWithPath("data.itemName")
                                                .type(JsonFieldType.STRING)
                                                .description("상품명"),
                                        fieldWithPath("data.brandName")
                                                .type(JsonFieldType.STRING)
                                                .description("브랜드명"),
                                        fieldWithPath("data.itemStatus")
                                                .type(JsonFieldType.STRING)
                                                .description("상품 상태"),
                                        fieldWithPath("data.originCountry")
                                                .type(JsonFieldType.STRING)
                                                .description("원산지"),
                                        fieldWithPath("data.shippingLocation")
                                                .type(JsonFieldType.STRING)
                                                .description("배송지"),
                                        fieldWithPath("data.freeShipping")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description("무료배송 여부"),
                                        // Price Info
                                        fieldWithPath("data.price")
                                                .type(JsonFieldType.OBJECT)
                                                .description("가격 정보"),
                                        fieldWithPath("data.price.price")
                                                .type(JsonFieldType.NUMBER)
                                                .description("판매가"),
                                        fieldWithPath("data.price.originalPrice")
                                                .type(JsonFieldType.NUMBER)
                                                .description("원가"),
                                        fieldWithPath("data.price.normalPrice")
                                                .type(JsonFieldType.NUMBER)
                                                .description("정상가"),
                                        fieldWithPath("data.price.appPrice")
                                                .type(JsonFieldType.NUMBER)
                                                .description("앱 가격"),
                                        fieldWithPath("data.price.discountRate")
                                                .type(JsonFieldType.NUMBER)
                                                .description("할인율 (%)"),
                                        fieldWithPath("data.price.appDiscountRate")
                                                .type(JsonFieldType.NUMBER)
                                                .description("앱 할인율 (%)"),
                                        // Images Info
                                        fieldWithPath("data.images")
                                                .type(JsonFieldType.OBJECT)
                                                .description("이미지 정보"),
                                        fieldWithPath("data.images.thumbnails")
                                                .type(JsonFieldType.ARRAY)
                                                .description("썸네일 이미지 목록"),
                                        fieldWithPath("data.images.thumbnails[].originalUrl")
                                                .type(JsonFieldType.STRING)
                                                .description("원본 URL"),
                                        fieldWithPath("data.images.thumbnails[].s3Url")
                                                .type(JsonFieldType.STRING)
                                                .description("S3 업로드 URL"),
                                        fieldWithPath("data.images.thumbnails[].status")
                                                .type(JsonFieldType.STRING)
                                                .description("업로드 상태"),
                                        fieldWithPath("data.images.thumbnails[].displayOrder")
                                                .type(JsonFieldType.NUMBER)
                                                .description("표시 순서"),
                                        fieldWithPath("data.images.descriptionImages")
                                                .type(JsonFieldType.ARRAY)
                                                .description("상세 이미지 목록"),
                                        fieldWithPath("data.images.descriptionImages[].originalUrl")
                                                .type(JsonFieldType.STRING)
                                                .description("원본 URL"),
                                        fieldWithPath("data.images.descriptionImages[].s3Url")
                                                .type(JsonFieldType.STRING)
                                                .description("S3 업로드 URL"),
                                        fieldWithPath("data.images.descriptionImages[].status")
                                                .type(JsonFieldType.STRING)
                                                .description("업로드 상태"),
                                        fieldWithPath(
                                                        "data.images.descriptionImages[].displayOrder")
                                                .type(JsonFieldType.NUMBER)
                                                .description("표시 순서"),
                                        fieldWithPath("data.images.totalCount")
                                                .type(JsonFieldType.NUMBER)
                                                .description("전체 이미지 수"),
                                        fieldWithPath("data.images.uploadedCount")
                                                .type(JsonFieldType.NUMBER)
                                                .description("업로드 완료 수"),
                                        // Category Info
                                        fieldWithPath("data.category")
                                                .type(JsonFieldType.OBJECT)
                                                .description("카테고리 정보"),
                                        fieldWithPath("data.category.fullPath")
                                                .type(JsonFieldType.STRING)
                                                .description("전체 경로"),
                                        fieldWithPath("data.category.headerCategoryCode")
                                                .type(JsonFieldType.STRING)
                                                .description("대분류 코드"),
                                        fieldWithPath("data.category.headerCategoryName")
                                                .type(JsonFieldType.STRING)
                                                .description("대분류명"),
                                        fieldWithPath("data.category.largeCategoryCode")
                                                .type(JsonFieldType.STRING)
                                                .description("중분류 코드"),
                                        fieldWithPath("data.category.largeCategoryName")
                                                .type(JsonFieldType.STRING)
                                                .description("중분류명"),
                                        fieldWithPath("data.category.mediumCategoryCode")
                                                .type(JsonFieldType.STRING)
                                                .description("소분류 코드"),
                                        fieldWithPath("data.category.mediumCategoryName")
                                                .type(JsonFieldType.STRING)
                                                .description("소분류명"),
                                        // Shipping Info
                                        fieldWithPath("data.shipping")
                                                .type(JsonFieldType.OBJECT)
                                                .description("배송 정보"),
                                        fieldWithPath("data.shipping.shippingType")
                                                .type(JsonFieldType.STRING)
                                                .description("배송 유형"),
                                        fieldWithPath("data.shipping.shippingFee")
                                                .type(JsonFieldType.NUMBER)
                                                .description("배송비"),
                                        fieldWithPath("data.shipping.shippingFeeType")
                                                .type(JsonFieldType.STRING)
                                                .description("배송비 유형"),
                                        fieldWithPath("data.shipping.averageDeliveryDays")
                                                .type(JsonFieldType.NUMBER)
                                                .description("평균 배송일"),
                                        fieldWithPath("data.shipping.freeShipping")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description("무료배송 여부"),
                                        // Options Info
                                        fieldWithPath("data.options")
                                                .type(JsonFieldType.OBJECT)
                                                .description("옵션 정보"),
                                        fieldWithPath("data.options.options")
                                                .type(JsonFieldType.ARRAY)
                                                .description("옵션 목록"),
                                        fieldWithPath("data.options.options[].optionNo")
                                                .type(JsonFieldType.NUMBER)
                                                .description("옵션 번호"),
                                        fieldWithPath("data.options.options[].color")
                                                .type(JsonFieldType.STRING)
                                                .description("색상"),
                                        fieldWithPath("data.options.options[].size")
                                                .type(JsonFieldType.STRING)
                                                .description("사이즈"),
                                        fieldWithPath("data.options.options[].stock")
                                                .type(JsonFieldType.NUMBER)
                                                .description("재고"),
                                        fieldWithPath("data.options.totalStock")
                                                .type(JsonFieldType.NUMBER)
                                                .description("전체 재고"),
                                        fieldWithPath("data.options.inStockCount")
                                                .type(JsonFieldType.NUMBER)
                                                .description("재고 있는 옵션 수"),
                                        fieldWithPath("data.options.soldOutCount")
                                                .type(JsonFieldType.NUMBER)
                                                .description("품절 옵션 수"),
                                        fieldWithPath("data.options.distinctColors")
                                                .type(JsonFieldType.ARRAY)
                                                .description("색상 종류"),
                                        fieldWithPath("data.options.distinctSizes")
                                                .type(JsonFieldType.ARRAY)
                                                .description("사이즈 종류"),
                                        // Crawl Status Info
                                        fieldWithPath("data.crawlStatus")
                                                .type(JsonFieldType.OBJECT)
                                                .description("크롤링 상태 정보"),
                                        fieldWithPath("data.crawlStatus.miniShopCrawledAt")
                                                .type(JsonFieldType.STRING)
                                                .description("미니샵 크롤링 완료 시간"),
                                        fieldWithPath("data.crawlStatus.detailCrawledAt")
                                                .type(JsonFieldType.STRING)
                                                .description("상세 크롤링 완료 시간"),
                                        fieldWithPath("data.crawlStatus.optionCrawledAt")
                                                .type(JsonFieldType.STRING)
                                                .description("옵션 크롤링 완료 시간"),
                                        fieldWithPath("data.crawlStatus.completedCount")
                                                .type(JsonFieldType.NUMBER)
                                                .description("완료된 크롤링 수"),
                                        fieldWithPath("data.crawlStatus.pendingTypes")
                                                .type(JsonFieldType.ARRAY)
                                                .description("대기 중인 크롤링 유형"),
                                        // Sync Status Info
                                        fieldWithPath("data.syncStatus")
                                                .type(JsonFieldType.OBJECT)
                                                .description("동기화 상태 정보"),
                                        fieldWithPath("data.syncStatus.externalProductId")
                                                .type(JsonFieldType.NUMBER)
                                                .description("외부 상품 ID")
                                                .optional(),
                                        fieldWithPath("data.syncStatus.needsSync")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description("동기화 필요 여부"),
                                        fieldWithPath("data.syncStatus.lastSyncedAt")
                                                .type(JsonFieldType.STRING)
                                                .description("마지막 동기화 시간")
                                                .optional(),
                                        fieldWithPath("data.syncStatus.canSync")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description("동기화 가능 여부"),
                                        // Description
                                        fieldWithPath("data.descriptionMarkUp")
                                                .type(JsonFieldType.STRING)
                                                .description("상품 상세 설명 HTML"),
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
