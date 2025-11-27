package com.ryuqq.crawlinghub.application.product.port.out.command;

import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;

/**
 * CrawledProduct 저장 Port (Port Out - Command)
 *
 * <p>CrawledProductManager에서만 사용됩니다.
 * <p>트랜잭션 경계 내에서 CrawledProduct Aggregate의 저장을 담당합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface CrawledProductPersistencePort {

    /**
     * CrawledProduct 저장 (신규 생성 또는 업데이트)
     *
     * @param crawledProduct 저장할 CrawledProduct
     * @return 저장된 CrawledProduct의 ID
     */
    CrawledProductId persist(CrawledProduct crawledProduct);

    /**
     * CrawledProduct 삭제
     *
     * @param crawledProductId 삭제할 CrawledProduct ID
     */
    void delete(CrawledProductId crawledProductId);
}
