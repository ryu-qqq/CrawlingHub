package com.ryuqq.crawlinghub.application.product.validator;

import com.ryuqq.crawlinghub.application.product.manager.CrawledProductReadManager;
import com.ryuqq.crawlinghub.application.product.manager.CrawledProductSyncOutboxCommandManager;
import com.ryuqq.crawlinghub.application.product.manager.CrawledProductSyncOutboxReadManager;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 외부 서버 동기화 처리 전 검증기
 *
 * <p><strong>검증 항목</strong>:
 *
 * <ul>
 *   <li>Outbox 존재 여부 확인
 *   <li>Outbox 처리 가능 상태 검증 (이미 처리 중이거나 완료된 경우 skip)
 *   <li>CrawledProduct 존재 여부 확인 (미존재 시 Outbox FAILED 처리)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class ProductSyncValidator {

    private static final Logger log = LoggerFactory.getLogger(ProductSyncValidator.class);

    private final CrawledProductSyncOutboxReadManager syncOutboxReadManager;
    private final CrawledProductReadManager crawledProductReadManager;
    private final CrawledProductSyncOutboxCommandManager syncOutboxCommandManager;

    public ProductSyncValidator(
            CrawledProductSyncOutboxReadManager syncOutboxReadManager,
            CrawledProductReadManager crawledProductReadManager,
            CrawledProductSyncOutboxCommandManager syncOutboxCommandManager) {
        this.syncOutboxReadManager = syncOutboxReadManager;
        this.crawledProductReadManager = crawledProductReadManager;
        this.syncOutboxCommandManager = syncOutboxCommandManager;
    }

    /**
     * Outbox 조회 + 상태 검증 + Product 조회를 통합 수행
     *
     * <p>Outbox가 존재하지 않거나, 이미 처리 중이거나 완료된 경우 empty를 반환합니다. CrawledProduct가 존재하지 않으면 Outbox를 FAILED
     * 처리하고 empty를 반환합니다.
     *
     * @param outboxId Outbox ID
     * @return 처리 가능한 SyncTarget (처리 불가 시 empty)
     */
    public Optional<SyncTarget> validateAndResolve(Long outboxId) {
        Optional<CrawledProductSyncOutbox> outboxOpt = syncOutboxReadManager.findById(outboxId);

        if (outboxOpt.isEmpty()) {
            log.warn("Outbox를 찾을 수 없음: outboxId={}", outboxId);
            return Optional.empty();
        }

        CrawledProductSyncOutbox outbox = outboxOpt.get();

        if (outbox.getStatus().isProcessing() || outbox.isCompleted()) {
            log.debug(
                    "이미 처리 중이거나 완료됨 (skip): outboxId={}, status={}",
                    outbox.getId(),
                    outbox.getStatus());
            return Optional.empty();
        }

        Optional<CrawledProduct> productOpt =
                crawledProductReadManager.findById(outbox.getCrawledProductId());

        if (productOpt.isEmpty()) {
            log.warn(
                    "CrawledProduct를 찾을 수 없음: outboxId={}, productId={}",
                    outbox.getId(),
                    outbox.getCrawledProductIdValue());
            syncOutboxCommandManager.markAsFailed(outbox, "CrawledProduct를 찾을 수 없음");
            return Optional.empty();
        }

        return Optional.of(new SyncTarget(outbox, productOpt.get()));
    }

    @SuppressFBWarnings(
            value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"},
            justification = "Record는 불변 DTO로 사용되며, 내부 객체 변경 위험 없음")
    public record SyncTarget(CrawledProductSyncOutbox outbox, CrawledProduct product) {}
}
