package com.ryuqq.crawlinghub.application.execution.internal.crawler;

import com.ryuqq.crawlinghub.application.execution.internal.crawler.dto.HttpRequest;
import com.ryuqq.crawlinghub.application.execution.internal.crawler.dto.HttpResponse;
import com.ryuqq.crawlinghub.application.execution.internal.crawler.mapper.CrawlContextMapper;
import com.ryuqq.crawlinghub.application.execution.internal.crawler.mapper.CrawlResultMapper;
import com.ryuqq.crawlinghub.application.execution.port.out.client.HttpClient;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlContext;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlResult;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Search API 크롤러
 *
 * <p>MustIt Search API를 통해 상품 목록을 무한스크롤 방식으로 크롤링합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class SearchCrawler extends Crawler {

    private static final Logger log = LoggerFactory.getLogger(SearchCrawler.class);

    private final HttpClient httpClient;
    private final CrawlContextMapper crawlContextMapper;
    private final CrawlResultMapper crawlResultMapper;

    public SearchCrawler(
            HttpClient httpClient,
            CrawlContextMapper crawlContextMapper,
            CrawlResultMapper crawlResultMapper) {
        this.httpClient = httpClient;
        this.crawlContextMapper = crawlContextMapper;
        this.crawlResultMapper = crawlResultMapper;
    }

    @Override
    public CrawlTaskType supportedType() {
        return CrawlTaskType.SEARCH;
    }

    @Override
    public CrawlResult crawl(CrawlContext context) {
        String searchEndpoint = crawlContextMapper.buildSearchEndpoint(context);

        log.info(
                "SearchCrawler 요청: endpoint={}, hasSearchCookies={}, userAgentId={}",
                searchEndpoint,
                context.hasSearchCookies(),
                context.userAgentId());

        if (!context.hasSearchCookies()) {
            log.warn(
                    "Search API 호출에 필요한 쿠키 없음: userAgentId={}, nid={}, mustitUid={}",
                    context.userAgentId(),
                    context.nid(),
                    context.mustitUid());
        }

        HttpRequest request =
                HttpRequest.get(searchEndpoint, crawlContextMapper.buildHeaders(context));

        HttpResponse response = httpClient.get(request);

        log.info(
                "SearchCrawler 응답: statusCode={}, userAgentId={}",
                response.statusCode(),
                context.userAgentId());

        return crawlResultMapper.toCrawlResult(response);
    }
}
