package com.ryuqq.crawlinghub.adapter.in.rest.schedule.controller;

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
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
 * <p><strong>제공하는 API:</strong>
 *
 * <ul>
 *   <li>POST /api/v1/schedules - 크롤 스케줄러 등록
 *   <li>PATCH /api/v1/schedules/{id} - 크롤 스케줄러 수정
 * </ul>
 *
 * <p><strong>Controller 책임:</strong>
 *
 * <ul>
 *   <li>HTTP 요청 수신 및 유효성 검증 (@Valid)
 *   <li>API DTO → UseCase DTO 변환 (Mapper)
 *   <li>UseCase 실행 위임
 *   <li>UseCase DTO → API DTO 변환 (Mapper)
 *   <li>HTTP 응답 반환 (ResponseEntity)
 * </ul>
 *
 * <p><strong>금지 사항:</strong>
 *
 * <ul>
 *   <li>❌ 비즈니스 로직 (UseCase 책임)
 *   <li>❌ Domain 객체 직접 생성/조작 (Domain Layer 책임)
 *   <li>❌ Transaction 관리 (UseCase 책임)
 *   <li>❌ 예외 처리 (GlobalExceptionHandler 책임)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@RestController
@RequestMapping("${api.endpoints.base-v1}${api.endpoints.schedule.base}")
@Validated
public class CrawlSchedulerCommandController {

    private final RegisterCrawlSchedulerUseCase registerCrawlSchedulerUseCase;
    private final UpdateCrawlSchedulerUseCase updateCrawlSchedulerUseCase;
    private final CrawlSchedulerCommandApiMapper crawlSchedulerCommandApiMapper;

    /**
     * CrawlSchedulerCommandController 생성자
     *
     * @param registerCrawlSchedulerUseCase 크롤 스케줄러 등록 UseCase
     * @param updateCrawlSchedulerUseCase 크롤 스케줄러 수정 UseCase
     * @param crawlSchedulerCommandApiMapper CrawlScheduler Command API Mapper
     */
    public CrawlSchedulerCommandController(
            RegisterCrawlSchedulerUseCase registerCrawlSchedulerUseCase,
            UpdateCrawlSchedulerUseCase updateCrawlSchedulerUseCase,
            CrawlSchedulerCommandApiMapper crawlSchedulerCommandApiMapper) {
        this.registerCrawlSchedulerUseCase = registerCrawlSchedulerUseCase;
        this.updateCrawlSchedulerUseCase = updateCrawlSchedulerUseCase;
        this.crawlSchedulerCommandApiMapper = crawlSchedulerCommandApiMapper;
    }

    /**
     * 크롤 스케줄러 등록
     *
     * <p><strong>API 명세:</strong>
     *
     * <ul>
     *   <li>Method: POST
     *   <li>Path: /api/v1/schedules
     *   <li>Status: 201 Created
     * </ul>
     *
     * <p><strong>Request Body:</strong>
     *
     * <pre>{@code
     * {
     *   "sellerId": 1,
     *   "schedulerName": "daily-crawl",
     *   "cronExpression": "0 0 9 * * ?"
     * }
     * }</pre>
     *
     * <p><strong>Response:</strong>
     *
     * <pre>{@code
     * {
     *   "success": true,
     *   "data": {
     *     "crawlSchedulerId": 1,
     *     "sellerId": 1,
     *     "schedulerName": "daily-crawl",
     *     "cronExpression": "0 0 9 * * ?",
     *     "status": "ACTIVE",
     *     "createdAt": "2025-11-20T10:30:00",
     *     "updatedAt": null
     *   },
     *   "error": null,
     *   "timestamp": "2025-11-20T10:30:00",
     *   "requestId": "req-123456"
     * }
     * }</pre>
     *
     * @param request 크롤 스케줄러 등록 요청 DTO (Bean Validation 적용)
     * @return 크롤 스케줄러 등록 결과 (201 Created)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CrawlSchedulerApiResponse>> registerCrawlScheduler(
            @RequestBody @Valid RegisterCrawlSchedulerApiRequest request) {
        // 1. API Request → UseCase Command 변환 (Mapper)
        RegisterCrawlSchedulerCommand command = crawlSchedulerCommandApiMapper.toCommand(request);

        // 2. UseCase 실행 (비즈니스 로직)
        CrawlSchedulerResponse useCaseResponse = registerCrawlSchedulerUseCase.register(command);

        // 3. UseCase Response → API Response 변환 (Mapper)
        CrawlSchedulerApiResponse apiResponse =
                crawlSchedulerCommandApiMapper.toApiResponse(useCaseResponse);

        // 4. ResponseEntity<ApiResponse<T>> 래핑
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ofSuccess(apiResponse));
    }

    /**
     * 크롤 스케줄러 수정
     *
     * <p><strong>API 명세:</strong>
     *
     * <ul>
     *   <li>Method: PATCH
     *   <li>Path: /api/v1/schedules/{id}
     *   <li>Status: 200 OK
     * </ul>
     *
     * <p><strong>Request Body:</strong>
     *
     * <pre>{@code
     * {
     *   "schedulerName": "new-daily-crawl",  // 선택적
     *   "cronExpression": "0 0 10 * * ?",    // 선택적
     *   "active": false                      // 선택적 (true=ACTIVE, false=INACTIVE)
     * }
     * }</pre>
     *
     * <p><strong>Response:</strong>
     *
     * <pre>{@code
     * {
     *   "success": true,
     *   "data": {
     *     "crawlSchedulerId": 1,
     *     "sellerId": 1,
     *     "schedulerName": "new-daily-crawl",
     *     "cronExpression": "0 0 10 * * ?",
     *     "status": "INACTIVE",
     *     "createdAt": "2025-11-20T10:30:00",
     *     "updatedAt": "2025-11-20T11:00:00"
     *   },
     *   "error": null,
     *   "timestamp": "2025-11-20T11:00:00",
     *   "requestId": "req-123457"
     * }
     * }</pre>
     *
     * @param id 크롤 스케줄러 ID (양수, PathVariable)
     * @param request 크롤 스케줄러 수정 요청 DTO
     * @return 크롤 스케줄러 수정 결과 (200 OK)
     */
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<CrawlSchedulerApiResponse>> updateCrawlScheduler(
            @PathVariable @Positive Long id,
            @RequestBody @Valid UpdateCrawlSchedulerApiRequest request) {
        // 1. API Request → UseCase Command 변환 (Mapper)
        UpdateCrawlSchedulerCommand command = crawlSchedulerCommandApiMapper.toCommand(id, request);

        // 2. UseCase 실행 (비즈니스 로직)
        CrawlSchedulerResponse useCaseResponse = updateCrawlSchedulerUseCase.update(command);

        // 3. UseCase Response → API Response 변환 (Mapper)
        CrawlSchedulerApiResponse apiResponse =
                crawlSchedulerCommandApiMapper.toApiResponse(useCaseResponse);

        // 4. ResponseEntity<ApiResponse<T>> 래핑
        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }
}
