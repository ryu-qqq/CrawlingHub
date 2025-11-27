package com.ryuqq.crawlinghub.application.crawl.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOption;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * OPTION 크롤링 응답 파서
 *
 * <p>상품 옵션 배열 응답을 파싱하여 ProductOption Domain VO 목록으로 변환합니다.
 *
 * <p><strong>응답 형식</strong>:
 * <pre>
 * [
 *   {
 *     "optionNo": 4134224619,
 *     "itemNo": 117005038,
 *     "color": "",
 *     "size": "US 4",
 *     "shippingType": "NONE",
 *     "stock": 1,
 *     "sizeGuide": ""
 *   },
 *   ...
 * ]
 * </pre>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class OptionResponseParser {

    private static final Logger log = LoggerFactory.getLogger(OptionResponseParser.class);

    private static final String OPTION_NO_FIELD = "optionNo";
    private static final String ITEM_NO_FIELD = "itemNo";
    private static final String COLOR_FIELD = "color";
    private static final String SIZE_FIELD = "size";
    private static final String STOCK_FIELD = "stock";
    private static final String SIZE_GUIDE_FIELD = "sizeGuide";

    private final ObjectMapper objectMapper;

    public OptionResponseParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * OPTION 응답 파싱
     *
     * @param responseBody HTTP 응답 본문 (JSON 배열)
     * @param itemNo 상품 번호 (파싱 컨텍스트용)
     * @return 파싱된 ProductOption 목록 (파싱 실패 시 empty)
     */
    public List<ProductOption> parse(String responseBody, Long itemNo) {
        if (responseBody == null || responseBody.isBlank()) {
            log.warn("OPTION 응답이 비어있습니다. itemNo={}", itemNo);
            return Collections.emptyList();
        }

        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);

            if (!rootNode.isArray()) {
                log.warn("OPTION 응답이 배열이 아닙니다. itemNo={}", itemNo);
                return Collections.emptyList();
            }

            List<ProductOption> options = new ArrayList<>();

            for (JsonNode optionNode : rootNode) {
                ProductOption option = parseOption(optionNode, itemNo);
                if (option != null) {
                    options.add(option);
                }
            }

            log.debug("OPTION 파싱 완료: itemNo={}, 옵션 수={}", itemNo, options.size());
            return Collections.unmodifiableList(options);

        } catch (Exception e) {
            log.error("OPTION 응답 파싱 실패. itemNo={}, error={}", itemNo, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 개별 옵션 파싱
     */
    private ProductOption parseOption(JsonNode optionNode, Long contextItemNo) {
        try {
            long optionNo = getAsLongOrDefault(optionNode, OPTION_NO_FIELD, 0);
            long itemNo = getAsLongOrDefault(optionNode, ITEM_NO_FIELD, 0);

            if (optionNo <= 0) {
                log.debug("옵션 번호 누락. contextItemNo={}", contextItemNo);
                return null;
            }

            if (itemNo <= 0 && contextItemNo != null) {
                itemNo = contextItemNo;
            }

            if (itemNo <= 0) {
                log.debug("상품 번호 누락. optionNo={}", optionNo);
                return null;
            }

            String color = getAsTextOrDefault(optionNode, COLOR_FIELD, "");
            String size = getAsTextOrDefault(optionNode, SIZE_FIELD, "");
            int stock = getAsIntOrDefault(optionNode, STOCK_FIELD, 0);
            String sizeGuide = getAsTextOrDefault(optionNode, SIZE_GUIDE_FIELD, "");

            return ProductOption.of(optionNo, itemNo, color, size, stock, sizeGuide);

        } catch (Exception e) {
            log.warn("옵션 파싱 실패. contextItemNo={}, error={}", contextItemNo, e.getMessage());
            return null;
        }
    }

    private String getAsTextOrDefault(JsonNode node, String fieldName, String defaultValue) {
        JsonNode fieldNode = node.get(fieldName);
        if (fieldNode == null || fieldNode.isNull()) {
            return defaultValue;
        }
        return fieldNode.asText(defaultValue);
    }

    private long getAsLongOrDefault(JsonNode node, String fieldName, long defaultValue) {
        JsonNode fieldNode = node.get(fieldName);
        if (fieldNode == null || fieldNode.isNull()) {
            return defaultValue;
        }
        if (fieldNode.isNumber()) {
            return fieldNode.asLong();
        }
        if (fieldNode.isTextual()) {
            try {
                return Long.parseLong(fieldNode.asText());
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private int getAsIntOrDefault(JsonNode node, String fieldName, int defaultValue) {
        JsonNode fieldNode = node.get(fieldName);
        if (fieldNode == null || fieldNode.isNull()) {
            return defaultValue;
        }
        if (fieldNode.isNumber()) {
            return fieldNode.asInt();
        }
        if (fieldNode.isTextual()) {
            try {
                return Integer.parseInt(fieldNode.asText());
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
}
