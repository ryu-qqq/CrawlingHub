package com.ryuqq.crawlinghub.application.crawl.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.crawlinghub.domain.product.vo.ItemTag;
import com.ryuqq.crawlinghub.domain.product.vo.MiniShopItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * MINI_SHOP 크롤링 응답 파서
 *
 * <p>상품 목록(items 배열) 응답을 파싱하여 Domain VO로 변환합니다.
 *
 * <p><strong>가격 변환</strong>: "1,075,000" → 1075000 (쉼표 제거 후 정수 변환)
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class MiniShopResponseParser {

    private static final Logger log = LoggerFactory.getLogger(MiniShopResponseParser.class);

    private static final String ITEMS_FIELD = "items";
    private static final String ITEM_NO_FIELD = "itemNo";
    private static final String NAME_FIELD = "name";
    private static final String BRAND_NAME_FIELD = "brandName";
    private static final String PRICE_FIELD = "price";
    private static final String ORIGINAL_PRICE_FIELD = "originalPrice";
    private static final String NORMAL_PRICE_FIELD = "normalPrice";
    private static final String DISCOUNT_RATE_FIELD = "discountRate";
    private static final String APP_DISCOUNT_RATE_FIELD = "appDiscountRate";
    private static final String APP_PRICE_FIELD = "appPrice";
    private static final String IMAGE_URL_LIST_FIELD = "imageUrlList";
    private static final String TAG_LIST_FIELD = "tagList";

    private final ObjectMapper objectMapper;

    public MiniShopResponseParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * MINI_SHOP 응답 파싱
     *
     * <p>JSON 응답의 items 배열에서 상품 정보를 추출하여 Domain VO 목록으로 반환합니다.
     *
     * @param responseBody HTTP 응답 본문
     * @return 파싱된 MiniShopItem 목록 (파싱 실패 시 empty)
     */
    public List<MiniShopItem> parse(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            log.warn("MINI_SHOP 응답이 비어있습니다.");
            return Collections.emptyList();
        }

        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode itemsNode = rootNode.get(ITEMS_FIELD);

            if (itemsNode == null || !itemsNode.isArray()) {
                log.warn("MINI_SHOP 응답에 items 배열이 없습니다: {}", truncate(responseBody));
                return Collections.emptyList();
            }

            List<MiniShopItem> products = new ArrayList<>();

            for (JsonNode itemNode : itemsNode) {
                Optional<MiniShopItem> parsedOpt = parseItem(itemNode);
                parsedOpt.ifPresent(products::add);
            }

            log.debug("MINI_SHOP 파싱 완료: 총 {}개 상품", products.size());
            return Collections.unmodifiableList(products);

        } catch (Exception e) {
            log.error("MINI_SHOP 응답 파싱 실패: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 개별 상품 아이템 파싱
     *
     * @param itemNode 상품 JSON 노드
     * @return 파싱된 MiniShopItem (필수 필드 누락 시 empty)
     */
    private Optional<MiniShopItem> parseItem(JsonNode itemNode) {
        try {
            Long itemNo = getAsLong(itemNode, ITEM_NO_FIELD);
            String name = getAsText(itemNode, NAME_FIELD);

            if (itemNo == null || name == null || name.isBlank()) {
                log.debug("상품 필수 필드 누락: itemNo={}, name={}", itemNo, name);
                return Optional.empty();
            }

            String brandName = getAsText(itemNode, BRAND_NAME_FIELD);
            String priceString = getAsText(itemNode, PRICE_FIELD);
            String originalPriceString = getAsText(itemNode, ORIGINAL_PRICE_FIELD);
            String normalPriceString = getAsText(itemNode, NORMAL_PRICE_FIELD);
            String discountRateString = getAsText(itemNode, DISCOUNT_RATE_FIELD);
            String appDiscountRateString = getAsText(itemNode, APP_DISCOUNT_RATE_FIELD);
            String appPriceString = getAsText(itemNode, APP_PRICE_FIELD);
            List<String> imageUrls = parseImageUrls(itemNode);
            List<ItemTag> tagList = parseTagList(itemNode);

            return Optional.of(MiniShopItem.fromStrings(
                    itemNo,
                    imageUrls,
                    brandName,
                    name,
                    priceString,
                    originalPriceString,
                    normalPriceString,
                    discountRateString,
                    appDiscountRateString,
                    appPriceString,
                    tagList));

        } catch (Exception e) {
            log.warn("상품 아이템 파싱 실패: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 이미지 URL 목록 파싱
     */
    private List<String> parseImageUrls(JsonNode itemNode) {
        List<String> imageUrls = new ArrayList<>();
        JsonNode imageUrlListNode = itemNode.get(IMAGE_URL_LIST_FIELD);

        if (imageUrlListNode != null && imageUrlListNode.isArray()) {
            for (JsonNode urlNode : imageUrlListNode) {
                if (urlNode.isTextual() && !urlNode.asText().isBlank()) {
                    imageUrls.add(urlNode.asText());
                }
            }
        }

        return imageUrls;
    }

    /**
     * 태그 목록 파싱
     */
    private List<ItemTag> parseTagList(JsonNode itemNode) {
        List<ItemTag> tags = new ArrayList<>();
        JsonNode tagListNode = itemNode.get(TAG_LIST_FIELD);

        if (tagListNode != null && tagListNode.isArray()) {
            for (JsonNode tagNode : tagListNode) {
                String title = getAsText(tagNode, "title");
                if (title != null && !title.isBlank()) {
                    String textColor = getAsText(tagNode, "textColor");
                    String bgColor = getAsText(tagNode, "bgColor");
                    String borderColor = getAsText(tagNode, "borderColor");
                    tags.add(ItemTag.of(title, textColor, bgColor, borderColor));
                }
            }
        }

        return tags;
    }

    private Long getAsLong(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.get(fieldName);
        if (fieldNode == null || fieldNode.isNull()) {
            return null;
        }
        if (fieldNode.isNumber()) {
            return fieldNode.asLong();
        }
        if (fieldNode.isTextual()) {
            try {
                return Long.parseLong(fieldNode.asText());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private String getAsText(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.get(fieldName);
        if (fieldNode == null || fieldNode.isNull()) {
            return null;
        }
        return fieldNode.asText();
    }

    private String truncate(String str) {
        if (str == null) {
            return "null";
        }
        return str.length() > 200 ? str.substring(0, 200) + "..." : str;
    }
}
