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
 * 상품 상세 정보 크롤러
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class DetailCrawler extends Crawler {

    private static final Logger log = LoggerFactory.getLogger(DetailCrawler.class);

    private final HttpClient httpClient;
    private final CrawlContextMapper crawlContextMapper;
    private final CrawlResultMapper crawlResultMapper;

    public DetailCrawler(
            HttpClient httpClient,
            CrawlContextMapper crawlContextMapper,
            CrawlResultMapper crawlResultMapper) {
        this.httpClient = httpClient;
        this.crawlContextMapper = crawlContextMapper;
        this.crawlResultMapper = crawlResultMapper;
    }

    @Override
    public CrawlTaskType supportedType() {
        return CrawlTaskType.DETAIL;
    }

    @Override
    public CrawlResult crawl(CrawlContext context) {
        log.debug(
                "DetailCrawler 실행: endpoint={}, userAgentId={}",
                context.endpoint(),
                context.userAgentId());

        HttpRequest request =
                HttpRequest.get(context.endpoint(), crawlContextMapper.buildHeaders(context));

        HttpResponse response = httpClient.get(request);

        return crawlResultMapper.toCrawlResult(response);
    }
}
