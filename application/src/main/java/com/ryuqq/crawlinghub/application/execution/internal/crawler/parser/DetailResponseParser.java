package com.ryuqq.crawlinghub.application.execution.internal.crawler.parser;

import static com.ryuqq.crawlinghub.application.common.utils.JsonNodeReader.getAsBooleanOrDefault;
import static com.ryuqq.crawlinghub.application.common.utils.JsonNodeReader.getAsIntOrDefault;
import static com.ryuqq.crawlinghub.application.common.utils.JsonNodeReader.getAsLongOrDefault;
import static com.ryuqq.crawlinghub.application.common.utils.JsonNodeReader.getAsStringList;
import static com.ryuqq.crawlinghub.application.common.utils.JsonNodeReader.getAsText;
import static com.ryuqq.crawlinghub.application.common.utils.JsonNodeReader.getAsTextOrDefault;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.crawlinghub.domain.product.vo.ProductCategory;
import com.ryuqq.crawlinghub.domain.product.vo.ProductDetailInfo;
import com.ryuqq.crawlinghub.domain.product.vo.ShippingInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * DETAIL 크롤링 응답 파서
 *
 * <p>상품 상세 응답(moduleList)을 파싱하여 ProductDetailInfo Domain VO로 변환합니다.
 *
 * <p><strong>파싱 대상 모듈</strong>:
 *
 * <ul>
 *   <li>ProductBannersModule: 배너 이미지
 *   <li>ProductInfoModule: 상품 기본 정보 (가격, 카테고리, 브랜드 등)
 *   <li>ShippingModule: 배송 정보
 *   <li>ProductDetailInfoModule: 상세 설명, 이미지 (descriptionMarkUp)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class DetailResponseParser {

    private static final Logger log = LoggerFactory.getLogger(DetailResponseParser.class);

    private static final String MODULE_LIST_FIELD = "moduleList";
    private static final String TYPE_FIELD = "type";
    private static final String DATA_FIELD = "data";

    private static final String PRODUCT_BANNERS_MODULE = "ProductBannersModule";
    private static final String PRODUCT_INFO_MODULE = "ProductInfoModule";
    private static final String SHIPPING_MODULE = "ShippingModule";
    private static final String PRODUCT_DETAIL_INFO_MODULE = "ProductDetailInfoModule";

    private static final Pattern IMG_SRC_PATTERN =
            Pattern.compile("<img[^>]+src=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE);

    private final ObjectMapper objectMapper;

    public DetailResponseParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * DETAIL 응답 파싱
     *
     * @param responseBody HTTP 응답 본문
     * @param itemNo 상품 번호 (파싱 컨텍스트용)
     * @return 파싱된 ProductDetailInfo (파싱 실패 시 empty)
     */
    public Optional<ProductDetailInfo> parse(String responseBody, Long itemNo) {
        if (responseBody == null || responseBody.isBlank()) {
            log.warn("DETAIL 응답이 비어있습니다. itemNo={}", itemNo);
            return Optional.empty();
        }

        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode moduleListNode = rootNode.get(MODULE_LIST_FIELD);

            if (moduleListNode == null || !moduleListNode.isArray()) {
                log.warn("DETAIL 응답에 moduleList가 없습니다. itemNo={}", itemNo);
                return Optional.empty();
            }

            ProductDetailInfo.Builder builder = ProductDetailInfo.builder();
            boolean hasProductInfo = false;

            for (JsonNode moduleNode : moduleListNode) {
                String type = getAsText(moduleNode, TYPE_FIELD);
                JsonNode dataNode = moduleNode.get(DATA_FIELD);

                if (type == null || dataNode == null) {
                    continue;
                }

                switch (type) {
                    case PRODUCT_BANNERS_MODULE -> parseBannersModule(dataNode, builder);
                    case PRODUCT_INFO_MODULE -> {
                        parseProductInfoModule(dataNode, builder);
                        hasProductInfo = true;
                    }
                    case SHIPPING_MODULE -> parseShippingModule(dataNode, builder);
                    case PRODUCT_DETAIL_INFO_MODULE -> parseDetailInfoModule(dataNode, builder);
                }
            }

            if (!hasProductInfo) {
                log.warn("DETAIL 응답에 ProductInfoModule이 없습니다. itemNo={}", itemNo);
                return Optional.empty();
            }

            ProductDetailInfo detailInfo = builder.build();
            log.debug(
                    "DETAIL 파싱 완료: itemNo={}, itemName={}",
                    detailInfo.itemNo(),
                    detailInfo.itemName());
            return Optional.of(detailInfo);

        } catch (Exception e) {
            log.error("DETAIL 응답 파싱 실패. itemNo={}, error={}", itemNo, e.getMessage());
            return Optional.empty();
        }
    }

    /** ProductBannersModule 파싱 - 배너 이미지 추출 */
    private void parseBannersModule(JsonNode dataNode, ProductDetailInfo.Builder builder) {
        List<String> bannerImages = getAsStringList(dataNode, "images");
        builder.bannerImages(bannerImages);
    }

    /** ProductInfoModule 파싱 - 상품 기본 정보 */
    private void parseProductInfoModule(JsonNode dataNode, ProductDetailInfo.Builder builder) {
        builder.sellerNo(getAsLongOrDefault(dataNode, "sellerNo", 0))
                .sellerId(getAsTextOrDefault(dataNode, "sellerId", ""))
                .itemNo(getAsLongOrDefault(dataNode, "itemNo", 0))
                .itemName(getAsTextOrDefault(dataNode, "itemName", ""))
                .brandName(getAsTextOrDefault(dataNode, "brandName", ""))
                .brandNameKr(getAsTextOrDefault(dataNode, "brandNameKr", ""))
                .brandCode(getAsLongOrDefault(dataNode, "brandCode", 0))
                .normalPrice(getAsIntOrDefault(dataNode, "normalPrice", 0))
                .sellingPrice(getAsIntOrDefault(dataNode, "sellingPrice", 0))
                .discountPrice(getAsIntOrDefault(dataNode, "discountPrice", 0))
                .discountRate(getAsIntOrDefault(dataNode, "discountRate", 0))
                .stock(getAsIntOrDefault(dataNode, "stock", 0))
                .isSoldOut(getAsBooleanOrDefault(dataNode, "isSoldOut", false));

        ProductCategory category =
                ProductCategory.of(
                        getAsTextOrDefault(dataNode, "headerCategoryCode", ""),
                        getAsTextOrDefault(dataNode, "headerCategory", ""),
                        getAsTextOrDefault(dataNode, "largeCategoryCode", ""),
                        getAsTextOrDefault(dataNode, "largeCategory", ""),
                        getAsTextOrDefault(dataNode, "mediumCategoryCode", ""),
                        getAsTextOrDefault(dataNode, "mediumCategory", ""));
        builder.category(category);
    }

    /** ShippingModule 파싱 - 배송 정보 */
    private void parseShippingModule(JsonNode dataNode, ProductDetailInfo.Builder builder) {
        JsonNode itemsNode = dataNode.get("items");

        if (itemsNode != null && itemsNode.isArray() && !itemsNode.isEmpty()) {
            JsonNode firstItem = itemsNode.get(0);
            JsonNode shippingDataNode = firstItem.get(DATA_FIELD);

            if (shippingDataNode != null) {
                String shippingType =
                        getAsTextOrDefault(shippingDataNode, "shippingType", "DOMESTIC");
                int shippingFee = getAsIntOrDefault(shippingDataNode, "shippingFee", 0);
                String shippingFeeType =
                        getAsTextOrDefault(shippingDataNode, "shippingFeeType", "PAID");

                String averageDeliveryText = "";
                JsonNode avgDeliveryNode = shippingDataNode.get("averageDeliveryDay");
                if (avgDeliveryNode != null) {
                    averageDeliveryText = getAsTextOrDefault(avgDeliveryNode, "text", "");
                }

                ShippingInfo shippingInfo =
                        ShippingInfo.fromShippingModule(
                                shippingType, shippingFee, shippingFeeType, averageDeliveryText);
                builder.shipping(shippingInfo);
            }
        }
    }

    /** ProductDetailInfoModule 파싱 - 상세 정보 (원산지, 상태, 상세 이미지, 상세 설명 HTML) */
    private void parseDetailInfoModule(JsonNode dataNode, ProductDetailInfo.Builder builder) {
        builder.originCountry(getAsTextOrDefault(dataNode, "originCountry", ""))
                .itemStatus(getAsTextOrDefault(dataNode, "itemStatus", ""));

        String descriptionMarkUp = getAsTextOrDefault(dataNode, "descriptionMarkUp", "");
        List<String> detailImages = extractImageUrlsFromHtml(descriptionMarkUp);
        builder.detailImages(detailImages).descriptionMarkUp(descriptionMarkUp);
    }

    /** HTML에서 이미지 URL 추출 */
    private List<String> extractImageUrlsFromHtml(String html) {
        List<String> imageUrls = new ArrayList<>();
        if (html == null || html.isBlank()) {
            return imageUrls;
        }

        Matcher matcher = IMG_SRC_PATTERN.matcher(html);
        while (matcher.find()) {
            String url = matcher.group(1);
            if (url != null && !url.isBlank()) {
                imageUrls.add(url);
            }
        }

        return imageUrls;
    }
}
