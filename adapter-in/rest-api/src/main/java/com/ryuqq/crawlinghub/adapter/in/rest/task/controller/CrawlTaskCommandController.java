package com.ryuqq.crawlinghub.adapter.in.rest.task.controller;

import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.ApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.task.CrawlTaskEndpoints;
import com.ryuqq.crawlinghub.adapter.in.rest.task.dto.response.CrawlTaskApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.task.mapper.CrawlTaskCommandApiMapper;
import com.ryuqq.crawlinghub.application.task.dto.command.RetryCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskResult;
import com.ryuqq.crawlinghub.application.task.port.in.command.RetryCrawlTaskUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * CrawlTask Command Controller
 *
 * <p>CrawlTask 도메인의 상태 변경 API를 제공합니다.
 *
 * <p><strong>제공하는 API:</strong>
 *
 * <ul>
 *   <li>POST /api/v1/crawling/tasks/{id}/retry - 크롤 태스크 재시도
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@RestController
@RequestMapping(CrawlTaskEndpoints.BASE)
@Validated
@Tag(name = "Task", description = "크롤 태스크 관리 API")
public class CrawlTaskCommandController {

    private final RetryCrawlTaskUseCase retryCrawlTaskUseCase;
    private final CrawlTaskCommandApiMapper crawlTaskCommandApiMapper;

    public CrawlTaskCommandController(
            RetryCrawlTaskUseCase retryCrawlTaskUseCase,
            CrawlTaskCommandApiMapper crawlTaskCommandApiMapper) {
        this.retryCrawlTaskUseCase = retryCrawlTaskUseCase;
        this.crawlTaskCommandApiMapper = crawlTaskCommandApiMapper;
    }

    /**
     * 크롤 태스크 재시도
     *
     * <p><strong>API 명세:</strong>
     *
     * <ul>
     *   <li>Method: POST
     *   <li>Path: /api/v1/crawling/tasks/{id}/retry
     *   <li>Status: 200 OK
     * </ul>
     *
     * <p><strong>비즈니스 규칙:</strong>
     *
     * <ul>
     *   <li>재시도 가능한 상태 (FAILED, TIMEOUT)의 Task만 재실행 가능
     *   <li>최대 재시도 횟수(3회) 초과 시 재실행 불가
     *   <li>재실행 시 Task 상태가 RETRY로 변경되고 SQS에 재발행
     * </ul>
     *
     * @param id 크롤 태스크 ID
     * @return 재시도된 크롤 태스크 정보 (200 OK)
     */
    @PostMapping(CrawlTaskEndpoints.RETRY)
    @Operation(
            summary = "크롤 태스크 재시도",
            description =
                    "실패한 크롤 태스크를 재시도합니다. FAILED 또는 TIMEOUT 상태의 태스크만 재시도 가능합니다. task:update 권한이"
                            + " 필요합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "재시도 성공",
                content =
                        @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = CrawlTaskApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 (재시도 불가 상태 또는 최대 재시도 횟수 초과)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "권한 없음 (task:update 권한 필요)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "태스크를 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<CrawlTaskApiResponse>> retryCrawlTask(
            @Parameter(description = "태스크 ID", required = true, example = "1")
                    @PathVariable
                    @Positive
                    Long id) {
        RetryCrawlTaskCommand command = crawlTaskCommandApiMapper.toRetryCommand(id);
        CrawlTaskResult useCaseResult = retryCrawlTaskUseCase.retry(command);
        CrawlTaskApiResponse apiResponse = crawlTaskCommandApiMapper.toApiResponse(useCaseResult);
        return ResponseEntity.ok(ApiResponse.of(apiResponse));
    }
}
