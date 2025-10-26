package com.ryuqq.crawlinghub.adapter.in.rest.seller;

import com.ryuqq.crawlinghub.application.mustit.seller.dto.command.RegisterMustitSellerCommand;
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
                seller.getCrawlInterval().getIntervalType().name(),
                seller.getCrawlInterval().getIntervalValue(),
                seller.getCreatedAt()
        );
    }
}
