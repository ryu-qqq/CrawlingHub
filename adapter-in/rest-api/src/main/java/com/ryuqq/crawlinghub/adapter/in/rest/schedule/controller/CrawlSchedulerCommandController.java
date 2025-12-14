package com.ryuqq.crawlinghub.adapter.in.rest.schedule.controller;

import com.ryuqq.crawlinghub.adapter.in.rest.auth.paths.ApiPaths;
import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.ApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.command.RegisterCrawlSchedulerApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.command.UpdateCrawlSchedulerApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.response.CrawlSchedulerApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.mapper.CrawlSchedulerCommandApiMapper;
import com.ryuqq.crawlinghub.application.schedule.dto.command.RegisterCrawlSchedulerCommand;
import com.ryuqq.crawlinghub.application.schedule.dto.command.UpdateCrawlSchedulerCommand;
import com.ryuqq.crawlinghub.application.schedule.dto.response.CrawlSchedulerResponse;
import com.ryuqq.crawlinghub.application.schedule.port.in.command.RegisterCrawlSchedulerUseCase;
import com.ryuqq.crawlinghub.application.schedule.port.in.command.UpdateCrawlSchedulerUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * CrawlScheduler Command Controller
 *
 * <p>CrawlScheduler 도메인의 상태 변경 API를 제공합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@RestController
@RequestMapping(ApiPaths.Schedules.BASE)
@Validated
@Tag(name = "Scheduler", description = "크롤 스케줄러 관리 API")
public class CrawlSchedulerCommandController {

    private final RegisterCrawlSchedulerUseCase registerCrawlSchedulerUseCase;
    private final UpdateCrawlSchedulerUseCase updateCrawlSchedulerUseCase;
    private final CrawlSchedulerCommandApiMapper crawlSchedulerCommandApiMapper;

    public CrawlSchedulerCommandController(
            RegisterCrawlSchedulerUseCase registerCrawlSchedulerUseCase,
            UpdateCrawlSchedulerUseCase updateCrawlSchedulerUseCase,
            CrawlSchedulerCommandApiMapper crawlSchedulerCommandApiMapper) {
        this.registerCrawlSchedulerUseCase = registerCrawlSchedulerUseCase;
        this.updateCrawlSchedulerUseCase = updateCrawlSchedulerUseCase;
        this.crawlSchedulerCommandApiMapper = crawlSchedulerCommandApiMapper;
    }

    @PostMapping
    @PreAuthorize("@access.hasPermission('scheduler:create')")
    @Operation(
            summary = "크롤 스케줄러 등록",
            description = "새로운 크롤 스케줄러를 등록합니다. scheduler:create 권한이 필요합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "201",
                description = "스케줄러 등록 성공",
                content =
                        @Content(
                                mediaType = "application/json",
                                schema =
                                        @Schema(implementation = CrawlSchedulerApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 (유효성 검증 실패, 잘못된 cron 표현식)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "권한 없음 (scheduler:create 권한 필요)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "셀러를 찾을 수 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "409",
                description = "중복된 스케줄러명")
    })
    public ResponseEntity<ApiResponse<CrawlSchedulerApiResponse>> registerCrawlScheduler(
            @RequestBody @Valid RegisterCrawlSchedulerApiRequest request) {
        RegisterCrawlSchedulerCommand command = crawlSchedulerCommandApiMapper.toCommand(request);
        CrawlSchedulerResponse useCaseResponse = registerCrawlSchedulerUseCase.register(command);
        CrawlSchedulerApiResponse apiResponse =
                crawlSchedulerCommandApiMapper.toApiResponse(useCaseResponse);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ofSuccess(apiResponse));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("@access.hasPermission('scheduler:update')")
    @Operation(
            summary = "크롤 스케줄러 수정",
            description = "크롤 스케줄러 정보를 수정합니다. scheduler:update 권한이 필요합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "스케줄러 수정 성공",
                content =
                        @Content(
                                mediaType = "application/json",
                                schema =
                                        @Schema(implementation = CrawlSchedulerApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "잘못된 요청"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "권한 없음 (scheduler:update 권한 필요)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "스케줄러를 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<CrawlSchedulerApiResponse>> updateCrawlScheduler(
            @Parameter(description = "스케줄러 ID", required = true, example = "1")
                    @PathVariable
                    @Positive
                    Long id,
            @RequestBody @Valid UpdateCrawlSchedulerApiRequest request) {
        UpdateCrawlSchedulerCommand command = crawlSchedulerCommandApiMapper.toCommand(id, request);
        CrawlSchedulerResponse useCaseResponse = updateCrawlSchedulerUseCase.update(command);
        CrawlSchedulerApiResponse apiResponse =
                crawlSchedulerCommandApiMapper.toApiResponse(useCaseResponse);
        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }
}
