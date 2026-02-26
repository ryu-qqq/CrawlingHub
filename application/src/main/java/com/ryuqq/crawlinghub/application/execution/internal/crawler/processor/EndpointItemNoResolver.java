package com.ryuqq.crawlinghub.application.execution.internal.crawler.processor;

import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 엔드포인트 URL에서 itemNo를 추출하는 유틸리티
 *
 * <p>DETAIL, OPTION 프로세서에서 공통으로 사용합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
final class EndpointItemNoResolver {

    private static final Logger log = LoggerFactory.getLogger(EndpointItemNoResolver.class);

    private EndpointItemNoResolver() {}

    /**
     * CrawlTask의 엔드포인트에서 itemNo 추출
     *
     * @param crawlTask 크롤 태스크
     * @return itemNo (추출 실패 시 null)
     */
    static Long resolve(CrawlTask crawlTask) {
        String endpoint = crawlTask.getEndpoint().toFullUrl();
        return parseItemNoFromEndpoint(endpoint);
    }

    private static Long parseItemNoFromEndpoint(String endpoint) {
        try {
            if (endpoint.contains("/auction_products/")) {
                String[] parts = endpoint.split("/auction_products/");
                if (parts.length > 1) {
                    String afterAuctionProducts = parts[1];
                    String itemNoStr = afterAuctionProducts.split("/")[0].split("\\?")[0];
                    return Long.parseLong(itemNoStr);
                }
            }
            if (endpoint.contains("/products/")) {
                String[] parts = endpoint.split("/products/");
                if (parts.length > 1) {
                    String afterProducts = parts[1];
                    String itemNoStr = afterProducts.split("/")[0].split("\\?")[0];
                    return Long.parseLong(itemNoStr);
                }
            }
            if (endpoint.contains("/item/")) {
                String[] parts = endpoint.split("/item/");
                if (parts.length > 1) {
                    String afterItem = parts[1];
                    String itemNoStr = afterItem.split("/")[0].split("\\?")[0];
                    return Long.parseLong(itemNoStr);
                }
            }
            if (endpoint.contains("itemNo=")) {
                String[] parts = endpoint.split("itemNo=");
                if (parts.length > 1) {
                    String itemNoStr = parts[1].split("&")[0];
                    return Long.parseLong(itemNoStr);
                }
            }
        } catch (NumberFormatException e) {
            log.warn("itemNo 파싱 실패: endpoint={}", endpoint);
        }
        return null;
    }
}
