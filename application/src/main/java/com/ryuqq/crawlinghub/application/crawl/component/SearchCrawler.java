package com.ryuqq.crawlinghub.application.crawl.component;

import com.ryuqq.crawlinghub.application.common.metrics.annotation.CrawlMetric;
import com.ryuqq.crawlinghub.application.crawl.dto.CrawlContext;
import com.ryuqq.crawlinghub.application.crawl.dto.CrawlResult;
import com.ryuqq.crawlinghub.application.crawl.dto.HttpRequest;
import com.ryuqq.crawlinghub.application.crawl.dto.HttpResponse;
import com.ryuqq.crawlinghub.application.crawl.port.out.client.HttpClientPort;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Search API 크롤러
 *
 * <p>MustIt Search API를 통해 상품 목록을 무한스크롤 방식으로 크롤링합니다.
 *
 * <p><strong>HTTP 요청 헤더</strong>:
 *
 * <ul>
 *   <li>User-Agent: CrawlContext에서 제공하는 UserAgent 값
 *   <li>Authorization: Bearer 토큰
 *   <li>Cookie: nid, mustit_uid (Search API 필수)
 * </ul>
 *
 * <p><strong>페이지네이션</strong>: 응답의 nextApiUrl 필드로 다음 페이지 요청
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class SearchCrawler extends Crawler {

    private static final Logger log = LoggerFactory.getLogger(SearchCrawler.class);

    private final HttpClientPort httpClientPort;

    public SearchCrawler(HttpClientPort httpClientPort) {
        this.httpClientPort = httpClientPort;
    }

    @Override
    public CrawlTaskType supportedType() {
        return CrawlTaskType.SEARCH;
    }

    @Override
    @CrawlMetric(crawlerType = "search")
    public CrawlResult crawl(CrawlContext context) {
        String searchEndpoint = context.buildSearchEndpoint();

        log.info(
                "SearchCrawler 요청: endpoint={}, hasSearchCookies={}, userAgentId={}",
                searchEndpoint,
                context.hasSearchCookies(),
                context.getUserAgentId());

        if (!context.hasSearchCookies()) {
            log.warn(
                    "Search API 호출에 필요한 쿠키 없음: userAgentId={}, nid={}, mustitUid={}",
                    context.getUserAgentId(),
                    context.getNid(),
                    context.getMusitUid());
        }

        HttpRequest request = HttpRequest.get(searchEndpoint, context.buildHeaders());

        HttpResponse response = httpClientPort.get(request);

        log.info(
                "SearchCrawler 응답: statusCode={}, userAgentId={}",
                response.getStatusCode(),
                context.getUserAgentId());

        return CrawlResult.from(response);
    }
}
