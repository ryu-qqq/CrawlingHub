package com.ryuqq.crawlinghub.application.seller.assembler;

import com.ryuqq.crawlinghub.application.seller.dto.command.RegisterSellerCommand;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;
import com.ryuqq.crawlinghub.domain.seller.aggregate.seller.Seller;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;
import org.springframework.stereotype.Component;

/**
 * Seller Assembler
 * - DTO ↔ Domain 변환 전용
 *
 * @author ryu-qqq
 * @since 2025-11-18
 */
@Component
public class SellerAssembler {

    /**
     * RegisterSellerCommand → Seller Domain
     *
     * @param command RegisterSellerCommand
     * @return Seller Domain
     */
    public Seller toDomain(RegisterSellerCommand command) {
        return Seller.forNew(
                SellerId.forNew(),
                command.name()
        );
    }

    /**
     * Seller Domain → SellerResponse
     *
     * @param seller Seller Domain
     * @return SellerResponse
     */
    public SellerResponse toResponse(Seller seller) {
        return SellerResponse.of(
                seller.getSellerId().value(),
                seller.getName(),
                seller.getStatus()
        );
    }
}
