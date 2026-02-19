package com.ryuqq.crawlinghub.adapter.in.rest.schedule.controller;

import com.ryuqq.authhub.sdk.annotation.RequirePermission;
import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.ApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.CrawlSchedulerEndpoints;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.command.RegisterCrawlSchedulerApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.command.UpdateCrawlSchedulerApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.command.UpdateSchedulerStatusApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.response.CrawlSchedulerApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.mapper.CrawlSchedulerCommandApiMapper;
import com.ryuqq.crawlinghub.adapter.in.rest.task.dto.response.CrawlTaskApiResponse;
import com.ryuqq.crawlinghub.application.schedule.dto.command.RegisterCrawlSchedulerCommand;
import com.ryuqq.crawlinghub.application.schedule.dto.command.UpdateCrawlSchedulerCommand;
import com.ryuqq.crawlinghub.application.schedule.dto.response.CrawlSchedulerResponse;
import com.ryuqq.crawlinghub.application.schedule.port.in.command.RegisterCrawlSchedulerUseCase;
import com.ryuqq.crawlinghub.application.schedule.port.in.command.UpdateCrawlSchedulerUseCase;
import com.ryuqq.crawlinghub.application.task.dto.command.TriggerCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskResponse;
import com.ryuqq.crawlinghub.application.task.port.in.command.TriggerCrawlTaskUseCase;
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
@RequestMapping(CrawlSchedulerEndpoints.BASE)
@Validated
@Tag(name = "Scheduler", description = "크롤 스케줄러 관리 API")
public class CrawlSchedulerCommandController {

    private final RegisterCrawlSchedulerUseCase registerCrawlSchedulerUseCase;
    private final UpdateCrawlSchedulerUseCase updateCrawlSchedulerUseCase;
    private final TriggerCrawlTaskUseCase triggerCrawlTaskUseCase;
    private final CrawlSchedulerCommandApiMapper crawlSchedulerCommandApiMapper;

    public CrawlSchedulerCommandController(
            RegisterCrawlSchedulerUseCase registerCrawlSchedulerUseCase,
            UpdateCrawlSchedulerUseCase updateCrawlSchedulerUseCase,
            TriggerCrawlTaskUseCase triggerCrawlTaskUseCase,
            CrawlSchedulerCommandApiMapper crawlSchedulerCommandApiMapper) {
        this.registerCrawlSchedulerUseCase = registerCrawlSchedulerUseCase;
        this.updateCrawlSchedulerUseCase = updateCrawlSchedulerUseCase;
        this.triggerCrawlTaskUseCase = triggerCrawlTaskUseCase;
        this.crawlSchedulerCommandApiMapper = crawlSchedulerCommandApiMapper;
    }

    @PostMapping
    @PreAuthorize("@access.hasPermission('scheduler:create')")
    @RequirePermission(value = "scheduler:create", description = "크롤 스케줄러 등록")
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
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(apiResponse));
    }

    @PatchMapping(CrawlSchedulerEndpoints.BY_ID)
    @PreAuthorize("@access.hasPermission('scheduler:update')")
    @RequirePermission(value = "scheduler:update", description = "크롤 스케줄러 수정")
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
        return ResponseEntity.ok(ApiResponse.of(apiResponse));
    }

    @PatchMapping(CrawlSchedulerEndpoints.STATUS)
    @PreAuthorize("@access.hasPermission('scheduler:update')")
    @RequirePermission(value = "scheduler:update", description = "크롤 스케줄러 상태 변경")
    @Operation(
            summary = "크롤 스케줄러 상태 변경",
            description = "크롤 스케줄러를 활성화/비활성화합니다. scheduler:update 권한이 필요합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "상태 변경 성공",
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
    public ResponseEntity<ApiResponse<CrawlSchedulerApiResponse>> updateSchedulerStatus(
            @Parameter(description = "스케줄러 ID", required = true, example = "1")
                    @PathVariable
                    @Positive
                    Long id,
            @RequestBody @Valid UpdateSchedulerStatusApiRequest request) {
        UpdateCrawlSchedulerCommand command =
                crawlSchedulerCommandApiMapper.toStatusCommand(id, request);
        CrawlSchedulerResponse useCaseResponse = updateCrawlSchedulerUseCase.update(command);
        CrawlSchedulerApiResponse apiResponse =
                crawlSchedulerCommandApiMapper.toApiResponse(useCaseResponse);
        return ResponseEntity.ok(ApiResponse.of(apiResponse));
    }

    @PostMapping(CrawlSchedulerEndpoints.TRIGGER)
    @PreAuthorize("@access.hasPermission('scheduler:update')")
    @RequirePermission(value = "scheduler:update", description = "크롤 스케줄러 수동 트리거")
    @Operation(
            summary = "크롤 스케줄러 수동 트리거",
            description = "크롤 스케줄러를 수동으로 트리거하여 CrawlTask를 생성합니다. scheduler:update 권한이 필요합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "201",
                description = "트리거 성공 (CrawlTask 생성)",
                content =
                        @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = CrawlTaskApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 (스케줄러가 비활성 상태)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "인증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "권한 없음 (scheduler:update 권한 필요)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "스케줄러를 찾을 수 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "409",
                description = "이미 실행 중인 태스크가 존재")
    })
    public ResponseEntity<ApiResponse<CrawlTaskApiResponse>> triggerScheduler(
            @Parameter(description = "스케줄러 ID", required = true, example = "1")
                    @PathVariable
                    @Positive
                    Long id) {
        TriggerCrawlTaskCommand command = new TriggerCrawlTaskCommand(id);
        CrawlTaskResponse useCaseResponse = triggerCrawlTaskUseCase.execute(command);
        CrawlTaskApiResponse apiResponse =
                crawlSchedulerCommandApiMapper.toTaskApiResponse(useCaseResponse);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(apiResponse));
    }
}
