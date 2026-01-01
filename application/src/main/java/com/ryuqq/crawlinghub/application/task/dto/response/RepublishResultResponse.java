package com.ryuqq.crawlinghub.application.task.dto.response;

/**
 * Outbox 재발행 결과 응답 DTO
 *
 * @param crawlTaskId Task ID
 * @param success 성공 여부
 * @param message 결과 메시지
 * @author development-team
 * @since 1.0.0
 */
public record RepublishResultResponse(Long crawlTaskId, boolean success, String message) {

    public static RepublishResultResponse success(Long crawlTaskId) {
        return new RepublishResultResponse(crawlTaskId, true, "SQS 재발행이 완료되었습니다.");
    }

    public static RepublishResultResponse failure(Long crawlTaskId, String reason) {
        return new RepublishResultResponse(crawlTaskId, false, reason);
    }
}
