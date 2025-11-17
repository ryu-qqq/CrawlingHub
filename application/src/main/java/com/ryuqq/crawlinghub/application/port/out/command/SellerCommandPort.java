package com.ryuqq.crawlinghub.application.port.out.command;

import com.ryuqq.crawlinghub.domain.seller.aggregate.seller.Seller;

/**
 * Seller Command Port (Outbound Port)
 *
 * <p>Application Layer에서 Persistence Layer로의 의존성 역전을 위한 인터페이스</p>
 *
 * <p><strong>Zero-Tolerance 규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Command Port 명명 규칙: Save*, Delete*</li>
 *   <li>✅ Domain Aggregate만 파라미터/반환 타입으로 사용</li>
 *   <li>✅ Infrastructure Layer 의존 금지</li>
 * </ul>
 *
 * <p><strong>구현체 위치:</strong> adapter-out/persistence-*</p>
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
public interface SellerCommandPort {

    /**
     * Seller 저장
     *
     * @param seller 저장할 Seller Aggregate
     * @return 저장된 Seller Aggregate
     */
    Seller save(Seller seller);

    /**
     * Seller 삭제
     *
     * @param sellerId 삭제할 Seller ID
     */
    void delete(String sellerId);
}
