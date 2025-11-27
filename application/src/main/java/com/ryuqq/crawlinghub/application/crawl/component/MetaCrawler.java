package com.ryuqq.crawlinghub.application.crawl.component;

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
 * 미니샵 메타 정보 크롤러
 *
 * <p>미니샵의 메타 정보(샵 이름, 설명, 카테고리 등)를 크롤링.
 *
 * <p><strong>HTTP 요청 헤더</strong>:
 *
 * <ul>
 *   <li>User-Agent: CrawlContext에서 제공하는 UserAgent 값
 *   <li>Cookie: CrawlContext에서 제공하는 세션 토큰
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class MetaCrawler extends Crawler {

    private static final Logger log = LoggerFactory.getLogger(MetaCrawler.class);

    private final HttpClientPort httpClientPort;

    public MetaCrawler(HttpClientPort httpClientPort) {
        this.httpClientPort = httpClientPort;
    }

    @Override
    public CrawlTaskType supportedType() {
        return CrawlTaskType.META;
    }

    @Override
    public CrawlResult crawl(CrawlContext context) {
        log.debug(
                "MetaCrawler 실행: endpoint={}, userAgentId={}",
                context.getEndpoint(),
                context.getUserAgentId());

        HttpRequest request = HttpRequest.get(context.getEndpoint(), context.buildHeaders());

        HttpResponse response = httpClientPort.get(request);

        return CrawlResult.from(response);
    }
}
