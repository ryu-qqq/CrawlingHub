package com.ryuqq.crawlinghub.application.product.service.command;

import com.ryuqq.crawlinghub.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.crawlinghub.application.product.dto.command.RecoverFailedProductSyncOutboxCommand;
import com.ryuqq.crawlinghub.application.product.manager.CrawledProductSyncOutboxCommandManager;
import com.ryuqq.crawlinghub.application.product.manager.CrawledProductSyncOutboxReadManager;
import com.ryuqq.crawlinghub.application.product.port.in.command.RecoverFailedProductSyncOutboxUseCase;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * FAILED ProductSyncOutbox 복구 Service
 *
 * <p><strong>용도</strong>: FAILED 상태에서 일정 시간 경과한 아웃박스를 PENDING으로 복원하여 자동 재처리
 *
 * <p><strong>재시도 제한</strong>: CrawledProductSyncOutbox.canRetry()로 최대 재시도 횟수 초과 시 복구하지 않음
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class RecoverFailedProductSyncOutboxService
        implements RecoverFailedProductSyncOutboxUseCase {

    private static final Logger log =
            LoggerFactory.getLogger(RecoverFailedProductSyncOutboxService.class);

    private final CrawledProductSyncOutboxReadManager outboxReadManager;
    private final CrawledProductSyncOutboxCommandManager outboxCommandManager;

    public RecoverFailedProductSyncOutboxService(
            CrawledProductSyncOutboxReadManager outboxReadManager,
            CrawledProductSyncOutboxCommandManager outboxCommandManager) {
        this.outboxReadManager = outboxReadManager;
        this.outboxCommandManager = outboxCommandManager;
    }

    @Override
    public SchedulerBatchProcessingResult execute(RecoverFailedProductSyncOutboxCommand command) {
        List<CrawledProductSyncOutbox> failedOutboxes =
                outboxReadManager.findFailedOlderThan(command.batchSize(), command.delaySeconds());

        if (failedOutboxes.isEmpty()) {
            return SchedulerBatchProcessingResult.empty();
        }

        log.info("FAILED ProductSyncOutbox 복구 시작: {} 건", failedOutboxes.size());

        int success = 0;
        int failed = 0;

        for (CrawledProductSyncOutbox outbox : failedOutboxes) {
            try {
                outboxCommandManager.resetToPending(outbox);
                success++;
                log.debug("FAILED 아웃박스 복구 성공: outboxId={}", outbox.getIdValue());
            } catch (Exception e) {
                failed++;
                log.error(
                        "FAILED 아웃박스 복구 실패: outboxId={}, error={}",
                        outbox.getIdValue(),
                        e.getMessage());
            }
        }

        log.info("FAILED ProductSyncOutbox 복구 완료: 성공={}, 실패={}", success, failed);
        return SchedulerBatchProcessingResult.of(failedOutboxes.size(), success, failed);
    }
}
