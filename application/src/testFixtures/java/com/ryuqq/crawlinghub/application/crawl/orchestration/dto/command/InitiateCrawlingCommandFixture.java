package com.ryuqq.crawlinghub.application.crawl.orchestration.dto.command;

import com.ryuqq.crawlinghub.application.task.assembler.command.InitiateCrawlingCommand;

/**
 * InitiateCrawlingCommand Test Fixture
 *
 * @author Cascade
 * @since 2025-10-31
 */
public class InitiateCrawlingCommandFixture {

    private static final Long DEFAULT_SELLER_ID = 1L;

    /**
     * 기본 InitiateCrawlingCommand 생성
     *
     * @return InitiateCrawlingCommand
     */
    public static InitiateCrawlingCommand create() {
        return new InitiateCrawlingCommand(DEFAULT_SELLER_ID);
    }

    /**
     * 특정 셀러 ID로 InitiateCrawlingCommand 생성
     *
     * @param sellerId 셀러 ID
     * @return InitiateCrawlingCommand
     */
    public static InitiateCrawlingCommand createWithSellerId(Long sellerId) {
        return new InitiateCrawlingCommand(sellerId);
    }
}
