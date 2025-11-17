package com.ryuqq.crawlinghub.application.assembler;

import com.ryuqq.crawlinghub.application.dto.response.SellerResponse;
import com.ryuqq.crawlinghub.domain.seller.aggregate.seller.Seller;

/**
 * Seller Assembler
 *
 * <p>Seller Domain Aggregate와 Application DTO 간 변환 책임</p>
 *
 * <p><strong>Zero-Tolerance 규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Lombok 금지 - Plain Java 사용</li>
 *   <li>✅ Stateless Utility Class (private constructor)</li>
 *   <li>✅ UseCase 내부 변환 로직 위임</li>
 * </ul>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * // UseCase 내부
 * Seller seller = loadSellerPort.loadById(sellerId);
 * return SellerAssembler.toResponse(seller);
 * }</pre>
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
public class SellerAssembler {

    private SellerAssembler() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * Seller Domain → SellerResponse DTO 변환
     *
     * @param seller Seller Domain Aggregate
     * @return SellerResponse DTO
     */
    public static SellerResponse toResponse(Seller seller) {
        return new SellerResponse(
            seller.getSellerId().value(),
            seller.getName(),
            seller.getStatus(),
            seller.getCrawlingIntervalDays(),
            seller.getTotalProductCount(),
            seller.getCreatedAt(),
            seller.getUpdatedAt()
        );
    }
}
