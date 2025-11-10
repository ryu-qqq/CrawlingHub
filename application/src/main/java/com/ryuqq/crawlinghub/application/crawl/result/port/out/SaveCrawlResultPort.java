package com.ryuqq.crawlinghub.application.crawl.result.port.out;

import com.ryuqq.crawlinghub.domain.crawl.result.CrawlResult;

/**
 * CrawlResult 저장 Outbound Port (Application → Persistence)
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
public interface SaveCrawlResultPort {

    /**
     * 크롤링 결과 저장
     *
     * @param crawlResult 크롤링 결과
     * @return 저장된 크롤링 결과 (ID 포함)
     */
    CrawlResult save(CrawlResult crawlResult);
}
