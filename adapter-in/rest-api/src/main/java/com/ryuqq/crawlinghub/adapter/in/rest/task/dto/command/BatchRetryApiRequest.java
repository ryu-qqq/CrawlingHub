package com.ryuqq.crawlinghub.adapter.in.rest.task.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 배치 재처리 API 요청 DTO
 *
 * @param crawlTaskIds 재처리할 Task ID 목록 (최대 100개)
 * @author development-team
 * @since 1.0.0
 */
@Schema(description = "Task 배치 재처리 요청")
public record BatchRetryApiRequest(
        @Schema(description = "재처리할 Task ID 목록", example = "[1, 2, 3]")
                @NotEmpty(message = "Task ID 목록은 비어있을 수 없습니다.")
                @Size(max = 100, message = "한 번에 최대 100개의 Task만 재처리할 수 있습니다.")
                List<Long> crawlTaskIds) {}
