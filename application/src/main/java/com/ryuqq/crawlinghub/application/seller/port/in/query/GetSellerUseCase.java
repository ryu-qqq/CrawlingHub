package com.ryuqq.crawlinghub.application.seller.port.in.query;

import com.ryuqq.crawlinghub.application.seller.dto.query.GetSellerQuery;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;

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
     * @param query 조회 쿼리 (sellerId)
     * @return 셀러 상세 정보
     */
    SellerResponse execute(GetSellerQuery query);
}
