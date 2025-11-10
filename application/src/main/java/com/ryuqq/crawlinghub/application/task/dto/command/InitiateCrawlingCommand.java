package com.ryuqq.crawlinghub.application.task.dto.command;

/**
 * 크롤링 시작 Command
 *
 * @param sellerId 셀러 ID (필수)
 * @author ryu-qqq
 * @since 2025-10-31
 */
public record InitiateCrawlingCommand(
    Long sellerId
) {
    public InitiateCrawlingCommand {
        if (sellerId == null) {
            throw new IllegalArgumentException("셀러 ID는 필수입니다");
        }
    }
}



