package com.ryuqq.crawlinghub.application.crawl.orchestration.dto.command;

/**
 * 미니샵 크롤링 결과 처리 Command
 *
 * @param taskId       태스크 ID (필수)
 * @param responseData 미니샵 API 응답 데이터 (JSON) (필수)
 * @author ryu-qqq
 * @since 2025-10-31
 */
public record MiniShopResultCommand(
    Long taskId,
    String responseData
) {
    public MiniShopResultCommand {
        if (taskId == null) {
            throw new IllegalArgumentException("태스크 ID는 필수입니다");
        }
        if (responseData == null || responseData.isBlank()) {
            throw new IllegalArgumentException("응답 데이터는 필수입니다");
        }
    }
}
