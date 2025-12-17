package com.ryuqq.crawlinghub.domain.product.exception;

import com.ryuqq.crawlinghub.domain.common.exception.DomainException;
import java.util.Map;

/** 존재하지 않는 크롤링 상품을 조회했을 때 발생하는 예외입니다. */
public final class CrawledProductNotFoundException extends DomainException {

    private static final CrawledProductErrorCode ERROR_CODE =
            CrawledProductErrorCode.CRAWLED_PRODUCT_NOT_FOUND;

    public CrawledProductNotFoundException(long crawledProductId) {
        super(
                ERROR_CODE,
                String.format("존재하지 않는 크롤링 상품입니다. ID: %d", crawledProductId),
                Map.of("crawledProductId", crawledProductId));
    }
}
