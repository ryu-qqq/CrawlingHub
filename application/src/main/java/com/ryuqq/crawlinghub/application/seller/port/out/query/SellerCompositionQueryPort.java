package com.ryuqq.crawlinghub.application.seller.port.out.query;

import com.ryuqq.crawlinghub.application.seller.dto.composite.SellerDetailResult;
import java.util.Optional;

/**
 * Seller Composite 조회 Port
 *
 * <p>셀러 상세 조회를 위한 크로스 도메인 Composite 쿼리 포트입니다. Seller + Scheduler + Task 정보를 한 번에 조회합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface SellerCompositionQueryPort {

    /**
     * 셀러 상세 정보 조회 (Composite)
     *
     * @param sellerId 셀러 ID
     * @return 셀러 상세 결과 (Optional)
     */
    Optional<SellerDetailResult> findSellerDetailById(Long sellerId);
}
