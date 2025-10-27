package com.ryuqq.crawlinghub.adapter.in.rest.seller.mapper;

import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.RegisterSellerApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.RegisterSellerApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.UpdateSellerApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.UpdateSellerApiResponse;
import com.ryuqq.crawlinghub.application.mustit.seller.dto.command.RegisterMustitSellerCommand;
import com.ryuqq.crawlinghub.application.mustit.seller.dto.command.UpdateMustitSellerCommand;
import com.ryuqq.crawlinghub.domain.mustit.seller.CrawlIntervalType;
import com.ryuqq.crawlinghub.domain.mustit.seller.MustitSeller;
import org.springframework.stereotype.Component;

/**
 * Seller API Mapper
 * <p>
 * REST API DTO ↔ Application Command/Query 간 변환을 담당합니다.
 * Stateless하며, 단순 매핑만 수행합니다.
 * </p>
 *
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
@Component
public class SellerApiMapper {

    /**
     * API Request → Application Command 변환
     *
     * @param request API Request
     * @return Application Command
     */
    public RegisterMustitSellerCommand toCommand(RegisterSellerApiRequest request) {
        if (request == null) {
            return null;
        }

        CrawlIntervalType intervalType = CrawlIntervalType.valueOf(request.intervalType());

        return new RegisterMustitSellerCommand(
                request.sellerId(),
                request.name(),
                intervalType,
                request.intervalValue()
        );
    }

    /**
     * Domain Aggregate → API Response 변환
     *
     * @param seller Domain Aggregate
     * @return API Response
     */
    public RegisterSellerApiResponse toResponse(MustitSeller seller) {
        if (seller == null) {
            return null;
        }

        return new RegisterSellerApiResponse(
                seller.getSellerId(),
                seller.getName(),
                seller.isActive(),
                seller.getCrawlIntervalType().name(),
                seller.getCrawlIntervalValue(),
                seller.getCreatedAt()
        );
    }

    /**
     * Update API Request → Application Command 변환
     *
     * @param sellerId 셀러 ID
     * @param request Update API Request
     * @return Update Application Command
     */
    public UpdateMustitSellerCommand toUpdateCommand(String sellerId, UpdateSellerApiRequest request) {
        if (request == null) {
            return null;
        }

        CrawlIntervalType intervalType = null;
        if (request.intervalType() != null) {
            intervalType = CrawlIntervalType.valueOf(request.intervalType());
        }

        return new UpdateMustitSellerCommand(
                sellerId,
                request.isActive(),
                intervalType,
                request.intervalValue()
        );
    }

    /**
     * Domain Aggregate → Update API Response 변환
     *
     * @param seller Domain Aggregate
     * @return Update API Response
     */
    public UpdateSellerApiResponse toUpdateResponse(MustitSeller seller) {
        if (seller == null) {
            return null;
        }

        return new UpdateSellerApiResponse(
                seller.getSellerId(),
                seller.getName(),
                seller.isActive(),
                seller.getCrawlIntervalType().name(),
                seller.getCrawlIntervalValue(),
                seller.getUpdatedAt()
        );
    }
}
