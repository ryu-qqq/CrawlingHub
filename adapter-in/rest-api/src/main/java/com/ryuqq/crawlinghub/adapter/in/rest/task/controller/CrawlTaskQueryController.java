package com.ryuqq.crawlinghub.adapter.in.rest.task.controller;

import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.ApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.PageApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.task.CrawlTaskEndpoints;
import com.ryuqq.crawlinghub.adapter.in.rest.task.dto.query.SearchCrawlTasksApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.task.dto.response.CrawlTaskApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.task.mapper.CrawlTaskQueryApiMapper;
import com.ryuqq.crawlinghub.application.task.dto.query.CrawlTaskSearchParams;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskPageResult;
import com.ryuqq.crawlinghub.application.task.port.in.query.SearchCrawlTaskByOffsetUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * CrawlTask Query Controller
 *
 * <p>CrawlTask 도메인의 조회 API를 제공합니다.
 *
 * <p><strong>제공하는 API:</strong>
 *
 * <ul>
 *   <li>GET /api/v1/crawling/tasks - 크롤 태스크 목록 조회 (페이징)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@RestController
@RequestMapping(CrawlTaskEndpoints.BASE)
@Validated
@Tag(name = "Task", description = "크롤 태스크 관리 API")
public class CrawlTaskQueryController {

    private final SearchCrawlTaskByOffsetUseCase searchCrawlTaskByOffsetUseCase;
    private final CrawlTaskQueryApiMapper crawlTaskQueryApiMapper;

    public CrawlTaskQueryController(
            SearchCrawlTaskByOffsetUseCase searchCrawlTaskByOffsetUseCase,
            CrawlTaskQueryApiMapper crawlTaskQueryApiMapper) {
        this.searchCrawlTaskByOffsetUseCase = searchCrawlTaskByOffsetUseCase;
        this.crawlTaskQueryApiMapper = crawlTaskQueryApiMapper;
    }

    /**
     * 크롤 태스크 목록 조회 (페이징)
     *
     * @param request 크롤 태스크 목록 조회 요청 DTO (Bean Validation 적용)
     * @return 크롤 태스크 목록 (페이징) (200 OK)
     */
    @GetMapping
    @Operation(
            summary = "크롤 태스크 목록 조회",
            description = "크롤 태스크 목록을 페이징하여 조회합니다. task:read 권한이 필요합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content =
                        @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = CrawlTaskApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 파라미터"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "권한 없음 (task:read 권한 필요)")
    })
    public ResponseEntity<ApiResponse<PageApiResponse<CrawlTaskApiResponse>>> listCrawlTasks(
            @ModelAttribute @Valid SearchCrawlTasksApiRequest request) {
        CrawlTaskSearchParams params = crawlTaskQueryApiMapper.toSearchParams(request);

        CrawlTaskPageResult pageResult = searchCrawlTaskByOffsetUseCase.execute(params);

        PageApiResponse<CrawlTaskApiResponse> apiPageResponse =
                crawlTaskQueryApiMapper.toPageApiResponse(pageResult);

        return ResponseEntity.ok(ApiResponse.of(apiPageResponse));
    }
}
