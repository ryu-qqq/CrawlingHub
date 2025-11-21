package com.ryuqq.crawlinghub.application.seller.port.in.query;

import com.ryuqq.crawlinghub.application.seller.dto.query.GetSellerQuery;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerDetailResponse;

/** 셀러 단건 조회 UseCase Port. */
public interface GetSellerUseCase {

    SellerDetailResponse getSeller(GetSellerQuery query);
}
