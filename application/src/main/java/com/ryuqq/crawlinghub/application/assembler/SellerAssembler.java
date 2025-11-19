package com.ryuqq.crawlinghub.application.assembler;

import com.ryuqq.crawlinghub.application.seller.dto.response.SellerDetailResponse;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerSummaryResponse;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import org.springframework.stereotype.Component;

/**
 * Seller Aggregate ↔ Response DTO 변환기.
 */
@Component
public class SellerAssembler {

    public SellerResponse toSellerResponse(Seller seller) {
        return new SellerResponse(
            seller.getSellerId().value(),
            stringValueOf(seller),
            seller.getSellerName(),
            seller.getStatus(),
            seller.getCreatedAt()
        );
    }

    public SellerDetailResponse toSellerDetailResponse(Seller seller, int activeSchedulerCount, int totalSchedulerCount) {
        return new SellerDetailResponse(
            seller.getSellerId().value(),
            stringValueOf(seller),
            seller.getSellerName(),
            seller.getStatus(),
            activeSchedulerCount,
            totalSchedulerCount,
            seller.getCreatedAt(),
            seller.getUpdatedAt()
        );
    }

    public SellerSummaryResponse toSellerSummaryResponse(Seller seller, int totalSchedulerCount) {
        return new SellerSummaryResponse(
            seller.getSellerId().value(),
            stringValueOf(seller),
            seller.getSellerName(),
            seller.getStatus(),
            totalSchedulerCount
        );
    }

    private String stringValueOf(Seller seller) {
        Long value = seller.getMustItSellerId().value();
        return value == null ? null : String.valueOf(value);
    }
}

