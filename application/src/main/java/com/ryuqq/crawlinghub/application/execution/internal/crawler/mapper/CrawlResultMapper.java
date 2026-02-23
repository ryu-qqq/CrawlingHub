package com.ryuqq.crawlinghub.application.execution.internal.crawler.mapper;

import com.ryuqq.crawlinghub.application.execution.internal.crawler.dto.HttpResponse;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlResult;
import org.springframework.stereotype.Component;

/**
 * CrawlResult 매퍼
 *
 * <p>HttpResponse로부터 CrawlResult 도메인 VO로 변환합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlResultMapper {

    /**
     * HttpResponse를 CrawlResult로 변환
     *
     * @param response HTTP 응답
     * @return CrawlResult (성공 또는 실패)
     */
    public CrawlResult toCrawlResult(HttpResponse response) {
        if (response.isSuccess()) {
            return CrawlResult.success(response.body(), response.statusCode());
        }
        String errorMessage = buildErrorMessage(response);
        return CrawlResult.failure(response.statusCode(), errorMessage);
    }

    private String buildErrorMessage(HttpResponse response) {
        if (response.isRateLimited()) {
            return "Rate limited (429)";
        } else if (response.isServerError()) {
            return "Server error: " + response.statusCode();
        } else if (response.isClientError()) {
            return "Client error: " + response.statusCode();
        } else {
            return "HTTP error: " + response.statusCode();
        }
    }
}
