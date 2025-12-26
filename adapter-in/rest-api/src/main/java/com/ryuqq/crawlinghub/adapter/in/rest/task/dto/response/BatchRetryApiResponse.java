package com.ryuqq.crawlinghub.adapter.in.rest.task.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * 배치 재처리 API 응답 DTO
 *
 * @param totalRequested 요청된 총 Task 수
 * @param successCount 성공한 Task 수
 * @param failedCount 실패한 Task 수
 * @param successIds 성공한 Task ID 목록
 * @param failures 실패한 Task 정보 목록
 * @author development-team
 * @since 1.0.0
 */
@Schema(description = "Task 배치 재처리 결과 응답")
public record BatchRetryApiResponse(
        @Schema(description = "요청된 총 Task 수", example = "10") int totalRequested,
        @Schema(description = "성공한 Task 수", example = "8") int successCount,
        @Schema(description = "실패한 Task 수", example = "2") int failedCount,
        @Schema(description = "성공한 Task ID 목록", example = "[1, 2, 3, 4, 5, 6, 7, 8]")
                List<Long> successIds,
        @Schema(description = "실패한 Task 정보 목록") List<RetryFailureItemApiResponse> failures) {

    /** 재처리 실패 항목 */
    @Schema(description = "재처리 실패 항목")
    public record RetryFailureItemApiResponse(
            @Schema(description = "실패한 Task ID", example = "9") Long crawlTaskId,
            @Schema(description = "실패 사유", example = "재시도 불가 - 상태: SUCCESS, 재시도 횟수: 0")
                    String reason) {}
}
