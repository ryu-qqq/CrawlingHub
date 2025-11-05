package com.ryuqq.crawlinghub.application.mustit.seller.port.in;

import com.ryuqq.crawlinghub.application.mustit.seller.dto.query.GetSellersQuery;
import com.ryuqq.crawlinghub.application.mustit.seller.dto.response.SellerListResponse;

/**
 * 셀러 목록 조회 UseCase
 *
 * <p>기능:
 * <ul>
 *   <li>페이징 처리된 셀러 목록 조회</li>
 *   <li>상태별 필터링 지원</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-02
 */
public interface GetSellersUseCase {

    /**
     * 셀러 목록 조회
     *
     * @param query 조회 조건 (페이징, 필터)
     * @return 셀러 목록 응답 (페이징 정보 포함)
     */
    SellerListResponse getSellers(GetSellersQuery query);
}
