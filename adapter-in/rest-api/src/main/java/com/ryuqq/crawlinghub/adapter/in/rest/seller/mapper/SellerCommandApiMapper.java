package com.ryuqq.crawlinghub.adapter.in.rest.seller.mapper;

import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.command.RegisterSellerApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.command.UpdateSellerApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response.SellerApiResponse;
import com.ryuqq.crawlinghub.application.seller.dto.command.RegisterSellerCommand;
import com.ryuqq.crawlinghub.application.seller.dto.command.UpdateSellerCommand;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;
import org.springframework.stereotype.Component;

/**
 * SellerCommandApiMapper - Seller Command REST API ↔ Application Layer 변환
 *
 * <p>Seller Command 요청/응답에 대한 DTO 변환을 담당합니다.
 *
 * <p><strong>변환 방향:</strong>
 *
 * <ul>
 *   <li>API Command Request → Application Command (Controller → Application)
 *   <li>Application Response → API Response (Application → Controller)
 * </ul>
 *
 * <p><strong>CQRS 패턴 적용:</strong>
 *
 * <ul>
 *   <li>Command: RegisterSeller, UpdateSeller 요청 변환
 * </ul>
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>필드 매핑만 수행 (비즈니스 로직 포함 금지)
 *   <li>API DTO ↔ Application DTO 단순 변환
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class SellerCommandApiMapper {

    /**
     * RegisterSellerApiRequest → RegisterSellerCommand 변환
     *
     * @param request REST API 셀러 등록 요청
     * @return Application Layer 셀러 등록 명령
     */
    public RegisterSellerCommand toCommand(RegisterSellerApiRequest request) {
        return new RegisterSellerCommand(request.mustItSellerName(), request.sellerName());
    }

    /**
     * UpdateSellerApiRequest → UpdateSellerCommand 변환
     *
     * @param sellerId 셀러 ID (PathVariable)
     * @param request REST API 셀러 수정 요청
     * @return Application Layer 셀러 수정 명령
     */
    public UpdateSellerCommand toCommand(Long sellerId, UpdateSellerApiRequest request) {
        return new UpdateSellerCommand(
                sellerId, request.mustItSellerName(), request.sellerName(), request.active());
    }

    /**
     * SellerResponse → SellerApiResponse 변환
     *
     * @param appResponse Application Layer 셀러 응답
     * @return REST API 셀러 응답
     */
    public SellerApiResponse toApiResponse(SellerResponse appResponse) {
        String statusName = appResponse.active() ? "ACTIVE" : "INACTIVE";
        return new SellerApiResponse(
                appResponse.sellerId(),
                appResponse.mustItSellerName(),
                appResponse.sellerName(),
                statusName,
                appResponse.createdAt(),
                appResponse.updatedAt());
    }
}
