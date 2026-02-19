package com.ryuqq.crawlinghub.adapter.in.rest.schedule.controller;

import com.ryuqq.authhub.sdk.annotation.RequirePermission;
import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.ApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.PageApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.CrawlSchedulerEndpoints;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.query.SearchCrawlSchedulersApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.response.CrawlSchedulerDetailApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.response.CrawlSchedulerSummaryApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.mapper.CrawlSchedulerQueryApiMapper;
import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.schedule.dto.query.SearchCrawlSchedulersQuery;
import com.ryuqq.crawlinghub.application.schedule.dto.response.CrawlSchedulerDetailResponse;
import com.ryuqq.crawlinghub.application.schedule.dto.response.CrawlSchedulerResponse;
import com.ryuqq.crawlinghub.application.schedule.port.in.query.SearchCrawlScheduleUseCase;
import com.ryuqq.crawlinghub.application.schedule.port.in.query.SearchCrawlSchedulesUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    private final SearchCrawlSchedulesUseCase searchCrawlSchedulesUseCase;
    private final SearchCrawlScheduleUseCase searchCrawlScheduleUseCase;
    private final CrawlSchedulerQueryApiMapper crawlSchedulerQueryApiMapper;

    public CrawlSchedulerQueryController(
            SearchCrawlSchedulesUseCase searchCrawlSchedulesUseCase,
            SearchCrawlScheduleUseCase searchCrawlScheduleUseCase,
            CrawlSchedulerQueryApiMapper crawlSchedulerQueryApiMapper) {
        this.searchCrawlSchedulesUseCase = searchCrawlSchedulesUseCase;
        this.searchCrawlScheduleUseCase = searchCrawlScheduleUseCase;
        this.crawlSchedulerQueryApiMapper = crawlSchedulerQueryApiMapper;
    }

    @GetMapping
    @PreAuthorize("@access.hasPermission('scheduler:read')")
    @RequirePermission(value = "scheduler:read", description = "크롤 스케줄러 목록 조회")
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
        SearchCrawlSchedulersQuery query = crawlSchedulerQueryApiMapper.toQuery(request);
        PageResponse<CrawlSchedulerResponse> useCasePageResponse =
                searchCrawlSchedulesUseCase.execute(query);
        PageApiResponse<CrawlSchedulerSummaryApiResponse> apiPageResponse =
                crawlSchedulerQueryApiMapper.toPageApiResponse(useCasePageResponse);
        return ResponseEntity.ok(ApiResponse.of(apiPageResponse));
    }

    @GetMapping(CrawlSchedulerEndpoints.BY_ID)
    @PreAuthorize("@access.hasPermission('scheduler:read')")
    @RequirePermission(value = "scheduler:read", description = "크롤 스케줄러 상세 조회")
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
                    Long crawlSchedulerId) {
        CrawlSchedulerDetailResponse detailResponse =
                searchCrawlScheduleUseCase.execute(crawlSchedulerId);
        CrawlSchedulerDetailApiResponse apiResponse =
                crawlSchedulerQueryApiMapper.toDetailApiResponse(detailResponse);
        return ResponseEntity.ok(ApiResponse.of(apiResponse));
    }
}
