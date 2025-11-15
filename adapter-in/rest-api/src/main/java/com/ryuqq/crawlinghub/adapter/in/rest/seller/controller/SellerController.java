package com.ryuqq.crawlinghub.adapter.in.rest.seller.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.ApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.RegisterSellerApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.RegisterSellerApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.UpdateSellerApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.UpdateSellerApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.mapper.SellerApiMapper;
import com.ryuqq.crawlinghub.application.seller.dto.command.RegisterSellerCommand;
import com.ryuqq.crawlinghub.application.seller.dto.command.UpdateSellerStatusCommand;
import com.ryuqq.crawlinghub.application.seller.port.in.RegisterSellerUseCase;
import com.ryuqq.crawlinghub.application.seller.port.in.UpdateSellerStatusUseCase;

import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * SellerController - 셀러 REST API 컨트롤러
 *
 * <p>RESTful API를 제공하는 Inbound Adapter입니다.</p>
 * <p>비즈니스 로직은 포함하지 않으며, UseCase 호출과 응답 변환만 담당합니다.</p>
 *
 * <p><strong>상세 조회 API 확장 (v2) ⭐</strong></p>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@RestController
@RequestMapping("/api/v1/sellers")
@Tag(name = "Seller API", description = "셀러 관리 API")
public class SellerController {

    private final RegisterSellerUseCase registerSellerUseCase;
    private final UpdateSellerStatusUseCase updateSellerStatusUseCase;
    private final SellerApiMapper sellerApiMapper;

    /**
     * 생성자 주입 (Constructor Injection)
     *
     * @param registerSellerUseCase 셀러 등록 UseCase
     * @param updateSellerStatusUseCase   셀러 상태 변경 UseCase
     * @param sellerApiMapper             API Mapper
     */
    public SellerController(
            RegisterSellerUseCase registerSellerUseCase,
            UpdateSellerStatusUseCase updateSellerStatusUseCase,
            SellerApiMapper sellerApiMapper
    ) {
        this.registerSellerUseCase = registerSellerUseCase;
        this.updateSellerStatusUseCase = updateSellerStatusUseCase;
        this.sellerApiMapper = sellerApiMapper;
    }

    /**
     * 셀러 등록 API
     * <p>
     * POST /api/v1/sellers
     * </p>
     *
     * @param request 셀러 등록 Request (Bean Validation 적용)
     * @return ApiResponse로 래핑된 등록된 셀러 정보
     */
    @PostMapping
    public ResponseEntity<ApiResponse<RegisterSellerApiResponse>> registerSeller(
            @Valid @RequestBody RegisterSellerApiRequest request
    ) {
        // 1. API Request → Application Command 변환
        RegisterSellerCommand command = sellerApiMapper.toCommand(request);

        // 2. UseCase 실행 → Application Response 반환
        com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse response =
                registerSellerUseCase.execute(command);

        // 3. Application Response → API Response 변환
        RegisterSellerApiResponse apiResponse = sellerApiMapper.toResponse(response);

        // 4. ApiResponse로 래핑
        ApiResponse<RegisterSellerApiResponse> wrappedResponse = ApiResponse.ofSuccess(apiResponse);

        return ResponseEntity.status(HttpStatus.CREATED).body(wrappedResponse);
    }

    /**
     * 셀러 정보 수정 API
     * <p>
     * PUT /api/v1/sellers/{sellerId}
     * </p>
     * <p>
     * 활성 상태(isActive) 및 크롤링 주기(intervalType, intervalValue)를 수정할 수 있습니다.
     * 모든 필드는 선택(Optional)이며, 제공된 필드만 수정됩니다.
     * </p>
     *
     * @param sellerId 수정할 셀러 ID (Path Variable)
     * @param request  셀러 수정 Request (Bean Validation 적용)
     * @return ApiResponse로 래핑된 수정된 셀러 정보
     */
    @PutMapping("/{sellerId}")
    public ResponseEntity<ApiResponse<UpdateSellerApiResponse>> updateSeller(
            @PathVariable("sellerId") String sellerId,
            @Valid @RequestBody UpdateSellerApiRequest request
    ) {
        // 0. Path Variable 검증
        Objects.requireNonNull(sellerId, "sellerId must not be null");

        // 1. Request 유효성 검증 (최소 1개 필드 필수)
        if (!request.hasAnyUpdate()) {
            throw new IllegalArgumentException("At least one field must be provided for update");
        }

        // 2. API Request → Application Command 변환
        UpdateSellerStatusCommand command = sellerApiMapper.toUpdateCommand(Long.parseLong(sellerId), request);

        // 3. UseCase 실행 → Application Response 반환
        com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse response =
                updateSellerStatusUseCase.execute(command);

        // 4. Application Response → API Response 변환
        UpdateSellerApiResponse apiResponse = sellerApiMapper.toUpdateResponse(response);

        // 5. ApiResponse로 래핑
        ApiResponse<UpdateSellerApiResponse> wrappedResponse = ApiResponse.ofSuccess(apiResponse);

        return ResponseEntity.ok(wrappedResponse);
    }
}
