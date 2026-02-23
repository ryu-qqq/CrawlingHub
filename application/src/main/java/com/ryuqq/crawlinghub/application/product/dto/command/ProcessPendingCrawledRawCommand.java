package com.ryuqq.crawlinghub.application.product.dto.command;

import com.ryuqq.crawlinghub.domain.product.vo.CrawlType;

/**
 * PENDING 상태의 CrawledRaw 가공 처리 커맨드
 *
 * @param crawlType 크롤링 타입 (MINI_SHOP, DETAIL, OPTION)
 * @param batchSize 배치 크기
 */
public record ProcessPendingCrawledRawCommand(CrawlType crawlType, int batchSize) {

    public static ProcessPendingCrawledRawCommand of(CrawlType crawlType, int batchSize) {
        return new ProcessPendingCrawledRawCommand(crawlType, batchSize);
    }
}
