package com.ryuqq.crawlinghub.adapter.in.rest.schedule.controller;

import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.ApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.PageApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.query.SearchCrawlSchedulersApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.response.CrawlSchedulerSummaryApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.mapper.CrawlSchedulerQueryApiMapper;
import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.schedule.dto.query.SearchCrawlSchedulersQuery;
import com.ryuqq.crawlinghub.application.schedule.dto.response.CrawlSchedulerResponse;
import com.ryuqq.crawlinghub.application.schedule.port.in.query.SearchCrawlSchedulesUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * CrawlScheduler Query Controller
 *
 * <p>CrawlScheduler 도메인의 조회 API를 제공합니다.
 *
 * <p><strong>제공하는 API:</strong>
 *
 * <ul>
 *   <li>GET /api/v1/schedules - 크롤 스케줄러 목록 조회
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
public class CrawlSchedulerQueryController {

    private final SearchCrawlSchedulesUseCase searchCrawlSchedulesUseCase;
    private final CrawlSchedulerQueryApiMapper crawlSchedulerQueryApiMapper;

    /**
     * CrawlSchedulerQueryController 생성자
     *
     * @param searchCrawlSchedulesUseCase 크롤 스케줄러 목록 조회 UseCase
     * @param crawlSchedulerQueryApiMapper CrawlScheduler Query API Mapper
     */
    public CrawlSchedulerQueryController(
            SearchCrawlSchedulesUseCase searchCrawlSchedulesUseCase,
            CrawlSchedulerQueryApiMapper crawlSchedulerQueryApiMapper) {
        this.searchCrawlSchedulesUseCase = searchCrawlSchedulesUseCase;
        this.crawlSchedulerQueryApiMapper = crawlSchedulerQueryApiMapper;
    }

    /**
     * 크롤 스케줄러 목록 조회 (페이징)
     *
     * <p><strong>API 명세:</strong>
     *
     * <ul>
     *   <li>Method: GET
     *   <li>Path: /api/v1/schedules
     *   <li>Status: 200 OK
     * </ul>
     *
     * <p><strong>Query Parameters:</strong>
     *
     * <ul>
     *   <li>sellerId: 셀러 ID 필터 (선택)
     *   <li>status: 스케줄러 상태 필터 (선택, ACTIVE/INACTIVE)
     *   <li>page: 페이지 번호 (선택, 기본값: 0)
     *   <li>size: 페이지 크기 (선택, 기본값: 20, 최대: 100)
     * </ul>
     *
     * <p><strong>Response:</strong>
     *
     * <pre>{@code
     * {
     *   "success": true,
     *   "data": {
     *     "content": [
     *       {
     *         "crawlSchedulerId": 1,
     *         "sellerId": 1,
     *         "schedulerName": "daily-crawl",
     *         "cronExpression": "0 0 9 * * ?",
     *         "status": "ACTIVE"
     *       }
     *     ],
     *     "page": 0,
     *     "size": 20,
     *     "totalElements": 100,
     *     "totalPages": 5,
     *     "first": true,
     *     "last": false
     *   },
     *   "error": null,
     *   "timestamp": "2025-11-20T10:30:00",
     *   "requestId": "req-123456"
     * }
     * }</pre>
     *
     * @param request 크롤 스케줄러 목록 조회 요청 DTO (Bean Validation 적용)
     * @return 크롤 스케줄러 목록 (페이징) (200 OK)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageApiResponse<CrawlSchedulerSummaryApiResponse>>>
            listCrawlSchedulers(@ModelAttribute @Valid SearchCrawlSchedulersApiRequest request) {
        // 1. API Request → UseCase Query 변환 (Mapper)
        SearchCrawlSchedulersQuery query = crawlSchedulerQueryApiMapper.toQuery(request);

        // 2. UseCase 실행 (비즈니스 로직)
        PageResponse<CrawlSchedulerResponse> useCasePageResponse =
                searchCrawlSchedulesUseCase.execute(query);

        // 3. UseCase Response → API Response 변환 (Mapper)
        PageApiResponse<CrawlSchedulerSummaryApiResponse> apiPageResponse =
                crawlSchedulerQueryApiMapper.toPageApiResponse(useCasePageResponse);

        // 4. ResponseEntity<ApiResponse<T>> 래핑
        return ResponseEntity.ok(ApiResponse.ofSuccess(apiPageResponse));
    }
}
