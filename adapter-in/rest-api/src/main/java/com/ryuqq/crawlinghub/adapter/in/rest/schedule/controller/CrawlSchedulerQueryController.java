package com.ryuqq.crawlinghub.adapter.in.rest.schedule.controller;

import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.ApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.PageApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.CrawlSchedulerEndpoints;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.query.SearchCrawlSchedulersApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.response.CrawlSchedulerDetailApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.response.CrawlSchedulerSummaryApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.mapper.CrawlSchedulerQueryApiMapper;
import com.ryuqq.crawlinghub.application.schedule.dto.composite.CrawlSchedulerDetailResult;
import com.ryuqq.crawlinghub.application.schedule.dto.query.CrawlSchedulerSearchParams;
import com.ryuqq.crawlinghub.application.schedule.dto.response.CrawlSchedulerPageResult;
import com.ryuqq.crawlinghub.application.schedule.port.in.query.SearchCrawlScheduleUseCase;
import com.ryuqq.crawlinghub.application.schedule.port.in.query.SearchCrawlSchedulerByOffsetUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * CrawlScheduler Query Controller
 *
 * <p>CrawlScheduler 도메인의 조회 API를 제공합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@RestController
@RequestMapping(CrawlSchedulerEndpoints.BASE)
@Validated
@Tag(name = "Scheduler", description = "크롤 스케줄러 관리 API")
public class CrawlSchedulerQueryController {

    private final SearchCrawlSchedulerByOffsetUseCase searchCrawlSchedulerByOffsetUseCase;
    private final SearchCrawlScheduleUseCase searchCrawlScheduleUseCase;
    private final CrawlSchedulerQueryApiMapper crawlSchedulerQueryApiMapper;

    public CrawlSchedulerQueryController(
            SearchCrawlSchedulerByOffsetUseCase searchCrawlSchedulerByOffsetUseCase,
            SearchCrawlScheduleUseCase searchCrawlScheduleUseCase,
            CrawlSchedulerQueryApiMapper crawlSchedulerQueryApiMapper) {
        this.searchCrawlSchedulerByOffsetUseCase = searchCrawlSchedulerByOffsetUseCase;
        this.searchCrawlScheduleUseCase = searchCrawlScheduleUseCase;
        this.crawlSchedulerQueryApiMapper = crawlSchedulerQueryApiMapper;
    }

    @GetMapping
    @Operation(
            summary = "크롤 스케줄러 목록 조회",
            description = "크롤 스케줄러 목록을 페이징하여 조회합니다. scheduler:read 권한이 필요합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content =
                        @Content(
                                mediaType = "application/json",
                                schema =
                                        @Schema(
                                                implementation =
                                                        CrawlSchedulerSummaryApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 파라미터"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "권한 없음 (scheduler:read 권한 필요)")
    })
    public ResponseEntity<ApiResponse<PageApiResponse<CrawlSchedulerSummaryApiResponse>>>
            listCrawlSchedulers(@ModelAttribute @Valid SearchCrawlSchedulersApiRequest request) {
        CrawlSchedulerSearchParams params = crawlSchedulerQueryApiMapper.toSearchParams(request);
        CrawlSchedulerPageResult pageResult = searchCrawlSchedulerByOffsetUseCase.execute(params);
        return ResponseEntity.ok(
                ApiResponse.of(crawlSchedulerQueryApiMapper.toPageResponse(pageResult)));
    }

    @GetMapping(CrawlSchedulerEndpoints.BY_ID)
    @Operation(
            summary = "크롤 스케줄러 상세 조회",
            description =
                    "크롤 스케줄러의 상세 정보를 조회합니다. "
                            + "셀러 정보, 실행 정보, 통계, 최근 태스크 목록을 포함합니다. "
                            + "scheduler:read 권한이 필요합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content =
                        @Content(
                                mediaType = "application/json",
                                schema =
                                        @Schema(
                                                implementation =
                                                        CrawlSchedulerDetailApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "권한 없음 (scheduler:read 권한 필요)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "스케줄러를 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<CrawlSchedulerDetailApiResponse>> getCrawlScheduler(
            @Parameter(description = "크롤 스케줄러 ID", required = true, example = "1") @PathVariable
                    Long id) {
        CrawlSchedulerDetailResult detailResult = searchCrawlScheduleUseCase.execute(id);
        CrawlSchedulerDetailApiResponse apiResponse =
                crawlSchedulerQueryApiMapper.toDetailApiResponse(detailResult);
        return ResponseEntity.ok(ApiResponse.of(apiResponse));
    }
}
