package com.ryuqq.crawlinghub.application.seller.port.in.query;

import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.seller.dto.query.ListSellersQuery;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerSummaryResponse;

/** 셀러 목록 조회 UseCase Port. */
public interface ListSellersUseCase {

    PageResponse<SellerSummaryResponse> listSellers(ListSellersQuery query);
}
