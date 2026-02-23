package com.ryuqq.crawlinghub.application.product.service.command;

import com.ryuqq.crawlinghub.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.crawlinghub.application.common.metric.annotation.BatchMetric;
import com.ryuqq.crawlinghub.application.product.dto.command.RecoverTimeoutProductSyncOutboxCommand;
import com.ryuqq.crawlinghub.application.product.manager.CrawledProductSyncOutboxCommandManager;
import com.ryuqq.crawlinghub.application.product.manager.CrawledProductSyncOutboxReadManager;
import com.ryuqq.crawlinghub.application.product.port.in.command.RecoverTimeoutProductSyncOutboxUseCase;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 타임아웃 ProductSyncOutbox 복구 Service
 *
 * <p><strong>용도</strong>: PROCESSING 상태에서 일정 시간 초과된 좀비 아웃박스를 PENDING으로 복원
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class RecoverTimeoutProductSyncOutboxService
        implements RecoverTimeoutProductSyncOutboxUseCase {

    private static final Logger log =
            LoggerFactory.getLogger(RecoverTimeoutProductSyncOutboxService.class);

    private final CrawledProductSyncOutboxReadManager outboxReadManager;
    private final CrawledProductSyncOutboxCommandManager outboxCommandManager;

    public RecoverTimeoutProductSyncOutboxService(
            CrawledProductSyncOutboxReadManager outboxReadManager,
            CrawledProductSyncOutboxCommandManager outboxCommandManager) {
        this.outboxReadManager = outboxReadManager;
        this.outboxCommandManager = outboxCommandManager;
    }

    @BatchMetric(value = "product_sync_outbox", category = "recover_timeout")
    @Override
    public SchedulerBatchProcessingResult execute(RecoverTimeoutProductSyncOutboxCommand command) {
        List<CrawledProductSyncOutbox> staleOutboxes =
                outboxReadManager.findStaleProcessing(
                        command.batchSize(), command.timeoutSeconds());

        if (staleOutboxes.isEmpty()) {
            return SchedulerBatchProcessingResult.empty();
        }

        log.info("좀비 ProductSyncOutbox 복구 시작: {} 건", staleOutboxes.size());

        int success = 0;
        int failed = 0;

        for (CrawledProductSyncOutbox outbox : staleOutboxes) {
            try {
                outboxCommandManager.resetToPending(outbox);
                success++;
                log.debug("좀비 아웃박스 복구 성공: outboxId={}", outbox.getIdValue());
            } catch (Exception e) {
                failed++;
                log.error(
                        "좀비 아웃박스 복구 실패: outboxId={}, error={}",
                        outbox.getIdValue(),
                        e.getMessage());
            }
        }

        log.info("좀비 ProductSyncOutbox 복구 완료: 성공={}, 실패={}", success, failed);
        return SchedulerBatchProcessingResult.of(staleOutboxes.size(), success, failed);
    }
}
