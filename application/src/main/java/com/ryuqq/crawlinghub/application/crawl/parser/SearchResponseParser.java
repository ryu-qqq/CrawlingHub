package com.ryuqq.crawlinghub.application.crawl.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.crawlinghub.domain.product.vo.ItemTag;
import com.ryuqq.crawlinghub.domain.product.vo.MiniShopItem;
import com.ryuqq.crawlinghub.domain.product.vo.SearchItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Search API 응답 파서
 *
 * <p>무한스크롤 방식의 Search API 응답을 파싱하여 MiniShopItem으로 변환합니다.
 *
 * <p><strong>응답 구조</strong>:
 *
 * <pre>
 * {
 *   "moduleList": [
 *     {
 *       "type": "SearchItemV2",
 *       "data": { itemNo, imageUrlList, brandName, name, price, ... }
 *     }
 *   ],
 *   "nextApiUrl": "/v1/search/items?..."
 * }
 * </pre>
 *
 * <p><strong>종료 조건</strong>: moduleList 비어있음 AND nextApiUrl 없음
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class SearchResponseParser {

    private static final Logger log = LoggerFactory.getLogger(SearchResponseParser.class);

    private static final String MODULE_LIST_FIELD = "moduleList";
    private static final String NEXT_API_URL_FIELD = "nextApiUrl";
    private static final String TYPE_FIELD = "type";
    private static final String DATA_FIELD = "data";
    private static final String SEARCH_ITEM_TYPE = "SearchItemV2";

    private static final String ITEM_NO_FIELD = "itemNo";
    private static final String NAME_FIELD = "name";
    private static final String BRAND_NAME_FIELD = "brandName";
    private static final String PRICE_FIELD = "price";
    private static final String ORIGINAL_PRICE_FIELD = "originalPrice";
    private static final String DISCOUNT_RATE_FIELD = "discountRate";
    private static final String IMAGE_URL_LIST_FIELD = "imageUrlList";
    private static final String TAG_LIST_FIELD = "tagList";
    private static final String SHIPPING_TYPE_FIELD = "shippingType";

    private final ObjectMapper objectMapper;

    public SearchResponseParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Search API 응답 파싱
     *
     * <p>moduleList에서 SearchItemV2 타입의 상품을 추출하여 MiniShopItem으로 변환합니다.
     *
     * @param responseBody HTTP 응답 본문
     * @return 파싱 결과 (상품 목록 + nextApiUrl)
     */
    public SearchParseResult parse(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            log.warn("Search API 응답이 비어있습니다.");
            return SearchParseResult.empty();
        }

        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            String nextApiUrl = extractNextApiUrl(rootNode);
            List<MiniShopItem> items = parseModuleList(rootNode);

            log.debug("Search API 파싱 완료: 상품 {}개, nextApiUrl={}", items.size(), nextApiUrl != null);
            return new SearchParseResult(items, nextApiUrl);

        } catch (Exception e) {
            log.error("Search API 응답 파싱 실패: {}", e.getMessage());
            return SearchParseResult.empty();
        }
    }

    /**
     * nextApiUrl 추출
     *
     * @param rootNode 루트 JSON 노드
     * @return 다음 페이지 URL (없으면 null)
     */
    private String extractNextApiUrl(JsonNode rootNode) {
        JsonNode nextApiUrlNode = rootNode.get(NEXT_API_URL_FIELD);
        if (nextApiUrlNode == null || nextApiUrlNode.isNull()) {
            return null;
        }
        String url = nextApiUrlNode.asText();
        return url.isBlank() ? null : url;
    }

    /**
     * moduleList 배열 파싱
     *
     * @param rootNode 루트 JSON 노드
     * @return MiniShopItem 목록
     */
    private List<MiniShopItem> parseModuleList(JsonNode rootNode) {
        JsonNode moduleListNode = rootNode.get(MODULE_LIST_FIELD);

        if (moduleListNode == null || !moduleListNode.isArray()) {
            log.debug("Search API 응답에 moduleList가 없습니다.");
            return List.of();
        }

        List<MiniShopItem> items = new ArrayList<>();

        for (JsonNode moduleNode : moduleListNode) {
            if (!isSearchItemModule(moduleNode)) {
                continue;
            }

            JsonNode dataNode = moduleNode.get(DATA_FIELD);
            if (dataNode == null) {
                continue;
            }

            parseSearchItem(dataNode)
                    .ifPresent(
                            searchItem -> {
                                MiniShopItem miniShopItem = searchItem.toMiniShopItem();
                                items.add(miniShopItem);
                            });
        }

        return items;
    }

    /**
     * SearchItemV2 타입 모듈인지 확인
     *
     * @param moduleNode 모듈 JSON 노드
     * @return SearchItemV2 타입이면 true
     */
    private boolean isSearchItemModule(JsonNode moduleNode) {
        JsonNode typeNode = moduleNode.get(TYPE_FIELD);
        if (typeNode == null || typeNode.isNull()) {
            return false;
        }
        return SEARCH_ITEM_TYPE.equals(typeNode.asText());
    }

    /**
     * 개별 상품 데이터 파싱
     *
     * @param dataNode data 필드 JSON 노드
     * @return SearchItem (필수 필드 누락 시 empty)
     */
    private Optional<SearchItem> parseSearchItem(JsonNode dataNode) {
        try {
            Long itemNo = getAsLong(dataNode, ITEM_NO_FIELD);
            String name = getAsText(dataNode, NAME_FIELD);

            if (itemNo == null || name == null || name.isBlank()) {
                log.debug("상품 필수 필드 누락: itemNo={}, name={}", itemNo, name);
                return Optional.empty();
            }

            String brandName = getAsText(dataNode, BRAND_NAME_FIELD);
            String price = getAsText(dataNode, PRICE_FIELD);
            String originalPrice = getAsText(dataNode, ORIGINAL_PRICE_FIELD);
            String discountRate = getAsText(dataNode, DISCOUNT_RATE_FIELD);
            String shippingType = getAsText(dataNode, SHIPPING_TYPE_FIELD);
            List<String> imageUrls = parseImageUrls(dataNode);
            List<ItemTag> tagList = parseTagList(dataNode);

            return Optional.of(
                    SearchItem.of(
                            itemNo,
                            imageUrls,
                            brandName,
                            name,
                            price,
                            originalPrice,
                            discountRate,
                            tagList,
                            shippingType));

        } catch (Exception e) {
            log.warn("상품 아이템 파싱 실패: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /** 이미지 URL 목록 파싱 */
    private List<String> parseImageUrls(JsonNode dataNode) {
        List<String> imageUrls = new ArrayList<>();
        JsonNode imageUrlListNode = dataNode.get(IMAGE_URL_LIST_FIELD);

        if (imageUrlListNode != null && imageUrlListNode.isArray()) {
            for (JsonNode urlNode : imageUrlListNode) {
                if (urlNode.isTextual() && !urlNode.asText().isBlank()) {
                    imageUrls.add(urlNode.asText());
                }
            }
        }

        return imageUrls;
    }

    /** 태그 목록 파싱 */
    private List<ItemTag> parseTagList(JsonNode dataNode) {
        List<ItemTag> tags = new ArrayList<>();
        JsonNode tagListNode = dataNode.get(TAG_LIST_FIELD);

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
}
