package com.ryuqq.crawlinghub.application.seller.assembler;

import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;
import com.ryuqq.crawlinghub.domain.seller.aggregate.seller.Seller;
import org.springframework.stereotype.Component;

/**
 * Seller Assembler
 *
 * <p>Seller Domain Aggregate와 Application DTO 간 변환 책임</p>
 *
 * <p><strong>Zero-Tolerance 규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Lombok 금지 - Plain Java 사용</li>
 *   <li>✅ Spring Bean 등록 (@Component) - 테스트 용이성</li>
 *   <li>✅ 인스턴스 메서드 사용 (static 금지)</li>
 *   <li>✅ UseCase 내부 변환 로직 위임</li>
 * </ul>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * // UseCase 내부 (생성자 주입)
 * private final SellerAssembler sellerAssembler;
 *
 * Seller seller = loadSellerPort.loadById(sellerId);
 * return sellerAssembler.toResponse(seller);
 * }</pre>
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
@Component
public class SellerAssembler {

    /**
     * Seller Domain → SellerResponse DTO 변환
     *
     * @param seller Seller Domain Aggregate
     * @return SellerResponse DTO
     */
    public SellerResponse toResponse(Seller seller) {
        return new SellerResponse(
            String.valueOf(seller.getSellerId().value()),
            seller.getName(),
            seller.getStatus(),
            seller.getCrawlingIntervalDays(),
            seller.getTotalProductCount(),
            seller.getCreatedAt(),
            seller.getUpdatedAt()
        );
    }
}
