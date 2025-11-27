package com.ryuqq.crawlinghub.application.crawl.parser;

import static com.ryuqq.crawlinghub.application.common.utils.StringTruncator.truncate;

import com.ryuqq.crawlinghub.domain.product.vo.ProductCount;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class MetaResponseParser {

    private static final Logger log = LoggerFactory.getLogger(MetaResponseParser.class);
    private static final String COUNT_FIELD = "count";

    private final ObjectMapper objectMapper;

    public MetaResponseParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * META 응답 파싱
     *
     * <p>JSON 응답에서 count 필드를 추출합니다.
     *
     * @param responseBody HTTP 응답 본문
     * @return 파싱된 ProductCount (실패 시 empty)
     */
    public Optional<ProductCount> parseResponse(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            log.warn("META 응답이 비어있습니다.");
            return Optional.empty();
        }

        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode countNode = rootNode.get(COUNT_FIELD);

            if (countNode == null || !countNode.isNumber()) {
                log.warn("META 응답에 count 필드가 없거나 숫자가 아닙니다: {}", truncate(responseBody, 100));
                return Optional.empty();
            }

            int count = countNode.asInt();
            log.debug("META 파싱 완료: count={}", count);

            return Optional.of(ProductCount.of(count));

        } catch (Exception e) {
            log.error("META 응답 파싱 실패: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
