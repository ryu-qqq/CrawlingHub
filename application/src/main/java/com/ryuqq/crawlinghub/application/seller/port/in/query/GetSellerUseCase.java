package com.ryuqq.crawlinghub.application.seller.port.in.query;

import com.ryuqq.crawlinghub.application.seller.dto.composite.SellerDetailResult;

/**
 * Get Seller Use Case
 *
 * <p>셀러 단건 조회 UseCase (Port In)
 *
 * <p><strong>책임:</strong> 셀러 ID로 단건 조회 요청 처리
 *
 * @author development-team
 * @since 1.0.0
 */
public interface GetSellerUseCase {

    /**
     * 셀러 단건 조회 실행
     *
     * @param sellerId 셀러 ID
     * @return 셀러 상세 정보 (스케줄러, 최근 태스크, 통계 포함)
     */
    SellerDetailResult execute(Long sellerId);
}
