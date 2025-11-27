package com.ryuqq.crawlinghub.adapter.in.rest.task.controller;

import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.ApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.PageApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.task.dto.query.SearchCrawlTasksApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.task.dto.response.CrawlTaskApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.task.dto.response.CrawlTaskDetailApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.task.mapper.CrawlTaskQueryApiMapper;
import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.task.dto.query.GetCrawlTaskQuery;
import com.ryuqq.crawlinghub.application.task.dto.query.ListCrawlTasksQuery;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskDetailResponse;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskResponse;
import com.ryuqq.crawlinghub.application.task.port.in.query.GetCrawlTaskUseCase;
import com.ryuqq.crawlinghub.application.task.port.in.query.ListCrawlTasksUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
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
 *   <li>GET /api/v1/tasks - 크롤 태스크 목록 조회 (페이징)
 *   <li>GET /api/v1/tasks/{id} - 크롤 태스크 상세 조회
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
@RequestMapping("${api.endpoints.base-v1}${api.endpoints.task.base}")
@Validated
public class CrawlTaskQueryController {

    private final ListCrawlTasksUseCase listCrawlTasksUseCase;
    private final GetCrawlTaskUseCase getCrawlTaskUseCase;
    private final CrawlTaskQueryApiMapper crawlTaskQueryApiMapper;

    /**
     * CrawlTaskQueryController 생성자
     *
     * @param listCrawlTasksUseCase 크롤 태스크 목록 조회 UseCase
     * @param getCrawlTaskUseCase 크롤 태스크 단건 조회 UseCase
     * @param crawlTaskQueryApiMapper CrawlTask Query API Mapper
     */
    public CrawlTaskQueryController(
            ListCrawlTasksUseCase listCrawlTasksUseCase,
            GetCrawlTaskUseCase getCrawlTaskUseCase,
            CrawlTaskQueryApiMapper crawlTaskQueryApiMapper) {
        this.listCrawlTasksUseCase = listCrawlTasksUseCase;
        this.getCrawlTaskUseCase = getCrawlTaskUseCase;
        this.crawlTaskQueryApiMapper = crawlTaskQueryApiMapper;
    }

    /**
     * 크롤 태스크 목록 조회 (페이징)
     *
     * <p><strong>API 명세:</strong>
     *
     * <ul>
     *   <li>Method: GET
     *   <li>Path: /api/v1/tasks
     *   <li>Status: 200 OK
     * </ul>
     *
     * <p><strong>Query Parameters:</strong>
     *
     * <ul>
     *   <li>crawlSchedulerId: 크롤 스케줄러 ID 필터 (필수)
     *   <li>sellerId: 셀러 ID 필터 (선택)
     *   <li>status: 태스크 상태 필터 (선택, PENDING/RUNNING/SUCCESS/FAILED/CANCELLED)
     *   <li>taskType: 태스크 유형 필터 (선택, META/MINI_SHOP/DETAIL/OPTION)
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
     *         "crawlTaskId": 1,
     *         "crawlSchedulerId": 1,
     *         "sellerId": 1,
     *         "requestUrl": "https://api.example.com/products",
     *         "status": "PENDING",
     *         "taskType": "META",
     *         "retryCount": 0,
     *         "createdAt": "2025-11-20T10:30:00"
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
     * @param request 크롤 태스크 목록 조회 요청 DTO (Bean Validation 적용)
     * @return 크롤 태스크 목록 (페이징) (200 OK)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageApiResponse<CrawlTaskApiResponse>>> listCrawlTasks(
            @ModelAttribute @Valid SearchCrawlTasksApiRequest request) {
        // 1. API Request → UseCase Query 변환 (Mapper)
        ListCrawlTasksQuery query = crawlTaskQueryApiMapper.toQuery(request);

        // 2. UseCase 실행 (비즈니스 로직)
        PageResponse<CrawlTaskResponse> useCasePageResponse = listCrawlTasksUseCase.execute(query);

        // 3. UseCase Response → API Response 변환 (Mapper)
        PageApiResponse<CrawlTaskApiResponse> apiPageResponse =
                crawlTaskQueryApiMapper.toPageApiResponse(useCasePageResponse);

        // 4. ResponseEntity<ApiResponse<T>> 래핑
        return ResponseEntity.ok(ApiResponse.ofSuccess(apiPageResponse));
    }

    /**
     * 크롤 태스크 상세 조회
     *
     * <p><strong>API 명세:</strong>
     *
     * <ul>
     *   <li>Method: GET
     *   <li>Path: /api/v1/tasks/{id}
     *   <li>Status: 200 OK
     * </ul>
     *
     * <p><strong>Path Variables:</strong>
     *
     * <ul>
     *   <li>id: 크롤 태스크 ID
     * </ul>
     *
     * <p><strong>Response:</strong>
     *
     * <pre>{@code
     * {
     *   "success": true,
     *   "data": {
     *     "crawlTaskId": 1,
     *     "crawlSchedulerId": 1,
     *     "sellerId": 1,
     *     "status": "PENDING",
     *     "taskType": "META",
     *     "retryCount": 0,
     *     "baseUrl": "https://api.example.com",
     *     "path": "/products",
     *     "queryParams": {"page": "1", "size": "100"},
     *     "fullUrl": "https://api.example.com/products?page=1&size=100",
     *     "createdAt": "2025-11-20T10:30:00",
     *     "updatedAt": "2025-11-20T10:30:00"
     *   },
     *   "error": null,
     *   "timestamp": "2025-11-20T10:30:00",
     *   "requestId": "req-123456"
     * }
     * }</pre>
     *
     * @param id 크롤 태스크 ID
     * @return 크롤 태스크 상세 정보 (200 OK)
     */
    @GetMapping("${api.endpoints.task.by-id}")
    public ResponseEntity<ApiResponse<CrawlTaskDetailApiResponse>> getCrawlTask(
            @PathVariable Long id) {
        // 1. Path Variable → UseCase Query 변환 (Mapper)
        GetCrawlTaskQuery query = crawlTaskQueryApiMapper.toGetQuery(id);

        // 2. UseCase 실행 (비즈니스 로직)
        CrawlTaskDetailResponse useCaseResponse = getCrawlTaskUseCase.execute(query);

        // 3. UseCase Response → API Response 변환 (Mapper)
        CrawlTaskDetailApiResponse apiResponse =
                crawlTaskQueryApiMapper.toDetailApiResponse(useCaseResponse);

        // 4. ResponseEntity<ApiResponse<T>> 래핑
        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }
}
