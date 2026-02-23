package com.ryuqq.crawlinghub.application.product.manager;

import com.ryuqq.crawlinghub.application.product.port.out.command.CrawledProductPersistencePort;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.id.CrawledProductId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * CrawledProduct 영속성 관리자
 *
 * <p><strong>책임</strong>: CrawledProduct Aggregate의 저장만 담당합니다.
 *
 * <p>도메인 로직(상태 변경)은 도메인 메서드를 직접 호출한 뒤 persist()로 영속화합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawledProductCommandManager {

    private final CrawledProductPersistencePort crawledProductPersistencePort;

    public CrawledProductCommandManager(
            CrawledProductPersistencePort crawledProductPersistencePort) {
        this.crawledProductPersistencePort = crawledProductPersistencePort;
    }

    /**
     * CrawledProduct 저장 (신규 생성 또는 업데이트)
     *
     * @param product 저장할 CrawledProduct
     * @return 저장된 CrawledProduct ID
     */
    @Transactional
    public CrawledProductId persist(CrawledProduct product) {
        return crawledProductPersistencePort.persist(product);
    }
}
