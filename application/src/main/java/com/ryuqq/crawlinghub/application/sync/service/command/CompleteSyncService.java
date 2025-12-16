package com.ryuqq.crawlinghub.application.sync.service.command;

import com.ryuqq.crawlinghub.application.product.manager.command.CrawledProductTransactionManager;
import com.ryuqq.crawlinghub.application.product.port.out.query.CrawledProductQueryPort;
import com.ryuqq.crawlinghub.application.sync.port.in.command.CompleteSyncUseCase;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 외부 서버 동기화 완료 처리 Service
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class CompleteSyncService implements CompleteSyncUseCase {

    private final CrawledProductQueryPort crawledProductQueryPort;
    private final CrawledProductTransactionManager crawledProductManager;

    public CompleteSyncService(
            CrawledProductQueryPort crawledProductQueryPort,
            CrawledProductTransactionManager crawledProductManager) {
        this.crawledProductQueryPort = crawledProductQueryPort;
        this.crawledProductManager = crawledProductManager;
    }

    @Override
    @Transactional
    public void complete(CrawledProductId crawledProductId, Long externalProductId) {
        Optional<CrawledProduct> productOpt = crawledProductQueryPort.findById(crawledProductId);
        productOpt.ifPresent(
                product -> crawledProductManager.markAsSynced(product, externalProductId));
    }
}
