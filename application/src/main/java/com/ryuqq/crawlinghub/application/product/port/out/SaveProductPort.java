package com.ryuqq.crawlinghub.application.product.port.out;

import com.ryuqq.crawlinghub.domain.product.CrawledProduct;

/**
 * Product 저장 Port (Outbound)
 *
 * <p>Command Adapter가 구현
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
public interface SaveProductPort {

    /**
     * Product 저장 (신규 생성 또는 업데이트)
     *
     * @return 저장된 Product (ID 포함)
     */
    CrawledProduct save(CrawledProduct product);
}
