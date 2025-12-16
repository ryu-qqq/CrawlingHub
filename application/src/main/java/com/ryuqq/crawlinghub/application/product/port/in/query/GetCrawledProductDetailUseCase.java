package com.ryuqq.crawlinghub.application.product.port.in.query;

import com.ryuqq.crawlinghub.application.product.dto.response.CrawledProductDetailResponse;

/**
 * CrawledProduct 상세 조회 UseCase
 *
 * <p>단건 크롤링 상품 상세 정보 조회
 *
 * @author development-team
 * @since 1.0.0
 */
public interface GetCrawledProductDetailUseCase {

    /**
     * CrawledProduct 상세 조회
     *
     * @param crawledProductId CrawledProduct ID
     * @return CrawledProduct 상세 정보
     * @throws com.ryuqq.crawlinghub.domain.product.exception.CrawledProductNotFoundException 상품을 찾을
     *     수 없는 경우
     */
    CrawledProductDetailResponse execute(Long crawledProductId);
}
