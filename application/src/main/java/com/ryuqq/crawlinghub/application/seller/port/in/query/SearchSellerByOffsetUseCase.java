package com.ryuqq.crawlinghub.application.seller.port.in.query;

import com.ryuqq.crawlinghub.application.seller.dto.query.SellerSearchParams;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerPageResult;

/**
 * 셀러 오프셋 기반 다건 조회 UseCase (Port In)
 *
 * @author development-team
 * @since 1.0.0
 */
public interface SearchSellerByOffsetUseCase {

    /**
     * 셀러 다건 조회
     *
     * @param params 검색 파라미터
     * @return 셀러 페이지 결과
     */
    SellerPageResult execute(SellerSearchParams params);
}
