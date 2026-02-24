package com.ryuqq.crawlinghub.application.schedule.service.command;

import com.ryuqq.crawlinghub.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.crawlinghub.application.common.metric.annotation.BatchMetric;
import com.ryuqq.crawlinghub.application.schedule.dto.command.RecoverTimeoutSchedulerOutboxCommand;
import com.ryuqq.crawlinghub.application.schedule.manager.CrawlSchedulerOutBoxCommandManager;
import com.ryuqq.crawlinghub.application.schedule.manager.CrawlSchedulerOutBoxReadManager;
import com.ryuqq.crawlinghub.application.schedule.port.in.command.RecoverTimeoutSchedulerOutboxUseCase;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerOutBox;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 타임아웃 아웃박스 복구 Service
 *
 * <p><strong>용도</strong>: PROCESSING 상태에서 일정 시간 초과된 좀비 아웃박스를 PENDING으로 복원
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class RecoverTimeoutSchedulerOutboxService implements RecoverTimeoutSchedulerOutboxUseCase {

    private static final Logger log =
            LoggerFactory.getLogger(RecoverTimeoutSchedulerOutboxService.class);

    private final CrawlSchedulerOutBoxReadManager outBoxReadManager;
    private final CrawlSchedulerOutBoxCommandManager outBoxCommandManager;

    public RecoverTimeoutSchedulerOutboxService(
            CrawlSchedulerOutBoxReadManager outBoxReadManager,
            CrawlSchedulerOutBoxCommandManager outBoxCommandManager) {
        this.outBoxReadManager = outBoxReadManager;
        this.outBoxCommandManager = outBoxCommandManager;
    }

    @BatchMetric(value = "scheduler_outbox", category = "recover_timeout")
    @Override
    public SchedulerBatchProcessingResult execute(RecoverTimeoutSchedulerOutboxCommand command) {
        List<CrawlSchedulerOutBox> staleOutBoxes =
                outBoxReadManager.findStaleProcessing(
                        command.batchSize(), command.timeoutSeconds());

        if (staleOutBoxes.isEmpty()) {
            return SchedulerBatchProcessingResult.empty();
        }

        log.info("좀비 아웃박스 복구 시작: {} 건", staleOutBoxes.size());

        int success = 0;
        int failed = 0;

        for (CrawlSchedulerOutBox outBox : staleOutBoxes) {
            try {
                outBox.resetToPending();
                outBoxCommandManager.persist(outBox);
                success++;
                log.debug("좀비 아웃박스 복구 성공: outBoxId={}", outBox.getOutBoxIdValue());
            } catch (Exception e) {
                failed++;
                log.error(
                        "좀비 아웃박스 복구 실패: outBoxId={}, error={}",
                        outBox.getOutBoxIdValue(),
                        e.getMessage());
            }
        }

        log.info("좀비 아웃박스 복구 완료: 성공={}, 실패={}", success, failed);
        return SchedulerBatchProcessingResult.of(staleOutBoxes.size(), success, failed);
    }
}
