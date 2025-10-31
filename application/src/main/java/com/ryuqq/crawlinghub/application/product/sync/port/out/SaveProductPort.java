package com.ryuqq.crawlinghub.application.product.sync.port.out;

import com.ryuqq.crawlinghub.domain.product.CrawledProduct;

/**
 * 상품 저장 Port
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public interface SaveProductPort {

    /**
     * 상품 저장
     */
    CrawledProduct save(CrawledProduct product);
}
