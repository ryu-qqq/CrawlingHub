package com.ryuqq.crawlinghub.application.execution.dto.command;

/**
 * CrawlTask 실행 Command DTO
 *
 * <p><strong>용도</strong>: SQS에서 수신한 CrawlTask 메시지를 UseCase로 전달
 *
 * @param taskId CrawlTask ID
 * @param schedulerId CrawlScheduler ID
 * @param sellerId Seller ID
 * @param taskType Task 유형 (PRODUCT, CATEGORY 등)
 * @param endpoint 크롤링 대상 URL
 * @author development-team
 * @since 1.0.0
 */
public record ExecuteCrawlTaskCommand(
        Long taskId, Long schedulerId, Long sellerId, String taskType, String endpoint) {
    public ExecuteCrawlTaskCommand {
        if (taskId == null) {
            throw new IllegalArgumentException("taskId는 null일 수 없습니다.");
        }
        if (schedulerId == null) {
            throw new IllegalArgumentException("schedulerId는 null일 수 없습니다.");
        }
        if (sellerId == null) {
            throw new IllegalArgumentException("sellerId는 null일 수 없습니다.");
        }
        if (taskType == null || taskType.isBlank()) {
            throw new IllegalArgumentException("taskType은 null이거나 빈 문자열일 수 없습니다.");
        }
        if (endpoint == null || endpoint.isBlank()) {
            throw new IllegalArgumentException("endpoint는 null이거나 빈 문자열일 수 없습니다.");
        }
    }
}
