package com.ryuqq.crawlinghub.application.execution.internal;

import com.ryuqq.crawlinghub.application.execution.dto.bundle.CrawlTaskExecutionBundle;
import com.ryuqq.crawlinghub.application.execution.internal.crawler.Crawler;
import com.ryuqq.crawlinghub.application.execution.internal.crawler.CrawlerProvider;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlContext;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 크롤링 프로세서
 *
 * <p><strong>책임</strong>: 비-트랜잭션 크롤링 실행 (HTTP 호출)
 *
 * <p><strong>주의</strong>: 이 클래스의 메서드는 @Transactional 외부에서 실행되어야 합니다. UserAgent 소비 및 결과 기록은 {@link
 * CrawlTaskExecutionCoordinator}에서 수행하며, 이 프로세서는 순수 크롤링만 담당합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlingProcessor {

    private static final Logger log = LoggerFactory.getLogger(CrawlingProcessor.class);

    private final CrawlerProvider crawlerProvider;

    public CrawlingProcessor(CrawlerProvider crawlerProvider) {
        this.crawlerProvider = crawlerProvider;
    }

    /**
     * 크롤링 수행 (비-트랜잭션)
     *
     * @param bundle CrawlContext가 포함된 실행 번들
     * @return 크롤링 결과
     */
    public CrawlResult executeCrawling(CrawlTaskExecutionBundle bundle) {
        CrawlContext crawlContext = bundle.crawlContext();

        log.debug(
                "크롤링 실행: taskType={}, endpoint={}, userAgentId={}",
                crawlContext.taskType(),
                crawlContext.endpoint(),
                crawlContext.userAgentId());

        Crawler crawler = crawlerProvider.getCrawler(crawlContext.taskType());
        return crawler.crawl(crawlContext);
    }
}
