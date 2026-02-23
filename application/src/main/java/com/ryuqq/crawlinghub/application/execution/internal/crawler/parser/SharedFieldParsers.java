package com.ryuqq.crawlinghub.application.execution.internal.crawler.parser;

import static com.ryuqq.crawlinghub.application.common.utils.JsonNodeReader.getAsText;

import com.fasterxml.jackson.databind.JsonNode;
import com.ryuqq.crawlinghub.domain.product.vo.ItemTag;
import java.util.ArrayList;
import java.util.List;

/**
 * 파서 간 공유 필드 파싱 유틸리티
 *
 * <p>SearchResponseParser에서 사용하는 태그 파싱 로직을 제공합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
final class SharedFieldParsers {

    private SharedFieldParsers() {}

    /**
     * 태그 목록 파싱
     *
     * @param parentNode 태그 배열을 포함하는 JSON 노드
     * @param fieldName 태그 배열 필드명
     * @return ItemTag 목록
     */
    static List<ItemTag> parseTagList(JsonNode parentNode, String fieldName) {
        List<ItemTag> tags = new ArrayList<>();
        JsonNode tagListNode = parentNode.get(fieldName);

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
}
