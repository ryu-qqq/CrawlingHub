package com.ryuqq.crawlinghub.adapter.in.rest.seller.controller;

import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.ApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.command.RegisterSellerApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.command.UpdateSellerApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response.SellerApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.mapper.SellerCommandApiMapper;
import com.ryuqq.crawlinghub.application.seller.dto.command.RegisterSellerCommand;
import com.ryuqq.crawlinghub.application.seller.dto.command.UpdateSellerCommand;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;
import com.ryuqq.crawlinghub.application.seller.port.in.command.RegisterSellerUseCase;
import com.ryuqq.crawlinghub.application.seller.port.in.command.UpdateSellerUseCase;
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
 * Seller Command Controller
 *
 * <p>Seller 도메인의 상태 변경 API를 제공합니다.
 *
 * <p><strong>제공하는 API:</strong>
 *
 * <ul>
 *   <li>POST /api/v1/sellers - 셀러 등록
 *   <li>PATCH /api/v1/sellers/{id} - 셀러 수정 (이름, 상태 등)
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
@RequestMapping("${api.endpoints.base-v1}${api.endpoints.seller.base}")
@Validated
public class SellerCommandController {

    private final RegisterSellerUseCase registerSellerUseCase;
    private final UpdateSellerUseCase updateSellerUseCase;
    private final SellerCommandApiMapper sellerCommandApiMapper;

    /**
     * SellerCommandController 생성자
     *
     * @param registerSellerUseCase 셀러 등록 UseCase
     * @param updateSellerUseCase 셀러 수정 UseCase
     * @param sellerCommandApiMapper Seller Command API Mapper
     */
    public SellerCommandController(
            RegisterSellerUseCase registerSellerUseCase,
            UpdateSellerUseCase updateSellerUseCase,
            SellerCommandApiMapper sellerCommandApiMapper) {
        this.registerSellerUseCase = registerSellerUseCase;
        this.updateSellerUseCase = updateSellerUseCase;
        this.sellerCommandApiMapper = sellerCommandApiMapper;
    }

    /**
     * 셀러 등록
     *
     * <p><strong>API 명세:</strong>
     *
     * <ul>
     *   <li>Method: POST
     *   <li>Path: /api/v1/sellers
     *   <li>Status: 201 Created
     * </ul>
     *
     * <p><strong>Request Body:</strong>
     *
     * <pre>{@code
     * {
     *   "mustItSellerName": "머스트잇 셀러명",
     *   "sellerName": "커머스 셀러명"
     * }
     * }</pre>
     *
     * <p><strong>Response:</strong>
     *
     * <pre>{@code
     * {
     *   "success": true,
     *   "data": {
     *     "sellerId": 1,
     *     "mustItSellerName": "머스트잇 셀러명",
     *     "sellerName": "커머스 셀러명",
     *     "status": "ACTIVE",
     *     "createdAt": "2025-11-19T10:30:00",
     *     "updatedAt": null
     *   },
     *   "error": null,
     *   "timestamp": "2025-11-19T10:30:00",
     *   "requestId": "req-123456"
     * }
     * }</pre>
     *
     * @param request 셀러 등록 요청 DTO (Bean Validation 적용)
     * @return 셀러 등록 결과 (201 Created)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<SellerApiResponse>> registerSeller(
            @RequestBody @Valid RegisterSellerApiRequest request) {
        // 1. API Request → UseCase Command 변환 (Mapper)
        RegisterSellerCommand command = sellerCommandApiMapper.toCommand(request);

        // 2. UseCase 실행 (비즈니스 로직)
        SellerResponse useCaseResponse = registerSellerUseCase.execute(command);

        // 3. UseCase Response → API Response 변환 (Mapper)
        SellerApiResponse apiResponse = sellerCommandApiMapper.toApiResponse(useCaseResponse);

        // 4. ResponseEntity<ApiResponse<T>> 래핑
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ofSuccess(apiResponse));
    }

    /**
     * 셀러 수정
     *
     * <p><strong>API 명세:</strong>
     *
     * <ul>
     *   <li>Method: PATCH
     *   <li>Path: /api/v1/sellers/{id}
     *   <li>Status: 200 OK
     * </ul>
     *
     * <p><strong>Request Body:</strong>
     *
     * <pre>{@code
     * {
     *   "mustItSellerName": "새 머스트잇 셀러명",  // 선택적
     *   "sellerName": "새 커머스 셀러명",          // 선택적
     *   "active": false                          // 선택적 (true=ACTIVE, false=INACTIVE)
     * }
     * }</pre>
     *
     * <p><strong>Response:</strong>
     *
     * <pre>{@code
     * {
     *   "success": true,
     *   "data": {
     *     "sellerId": 1,
     *     "mustItSellerName": "새 머스트잇 셀러명",
     *     "sellerName": "새 커머스 셀러명",
     *     "status": "INACTIVE",
     *     "createdAt": "2025-11-19T10:30:00",
     *     "updatedAt": "2025-11-19T11:00:00"
     *   },
     *   "error": null,
     *   "timestamp": "2025-11-19T11:00:00",
     *   "requestId": "req-123457"
     * }
     * }</pre>
     *
     * @param id 셀러 ID (양수, PathVariable)
     * @param request 셀러 수정 요청 DTO
     * @return 셀러 수정 결과 (200 OK)
     */
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<SellerApiResponse>> updateSeller(
            @PathVariable @Positive Long id, @RequestBody @Valid UpdateSellerApiRequest request) {
        // 1. API Request → UseCase Command 변환 (Mapper)
        UpdateSellerCommand command = sellerCommandApiMapper.toCommand(id, request);

        // 2. UseCase 실행 (비즈니스 로직)
        SellerResponse useCaseResponse = updateSellerUseCase.execute(command);

        // 3. UseCase Response → API Response 변환 (Mapper)
        SellerApiResponse apiResponse = sellerCommandApiMapper.toApiResponse(useCaseResponse);

        // 4. ResponseEntity<ApiResponse<T>> 래핑
        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }
}
