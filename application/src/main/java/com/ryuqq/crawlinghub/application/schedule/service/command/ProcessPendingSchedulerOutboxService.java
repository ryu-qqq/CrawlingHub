package com.ryuqq.crawlinghub.application.schedule.service.command;

import com.ryuqq.crawlinghub.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.crawlinghub.application.schedule.dto.command.ProcessPendingSchedulerOutboxCommand;
import com.ryuqq.crawlinghub.application.schedule.internal.CrawlSchedulerOutBoxProcessor;
import com.ryuqq.crawlinghub.application.schedule.manager.CrawlSchedulerOutBoxReadManager;
import com.ryuqq.crawlinghub.application.schedule.port.in.command.ProcessPendingSchedulerOutboxUseCase;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerOutBox;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * PENDING 상태의 스케줄러 아웃박스 처리 Service
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class ProcessPendingSchedulerOutboxService implements ProcessPendingSchedulerOutboxUseCase {

    private static final Logger log =
            LoggerFactory.getLogger(ProcessPendingSchedulerOutboxService.class);

    private final CrawlSchedulerOutBoxReadManager outBoxReadManager;
    private final CrawlSchedulerOutBoxProcessor processor;

    public ProcessPendingSchedulerOutboxService(
            CrawlSchedulerOutBoxReadManager outBoxReadManager,
            CrawlSchedulerOutBoxProcessor processor) {
        this.outBoxReadManager = outBoxReadManager;
        this.processor = processor;
    }

    @Override
    public SchedulerBatchProcessingResult execute(ProcessPendingSchedulerOutboxCommand command) {
        List<CrawlSchedulerOutBox> outBoxes =
                outBoxReadManager.findPendingOlderThan(command.batchSize(), command.delaySeconds());

        if (outBoxes.isEmpty()) {
            return SchedulerBatchProcessingResult.empty();
        }

        log.info("아웃박스 재처리 시작: {} 건", outBoxes.size());

        int success = 0;
        int failed = 0;

        for (CrawlSchedulerOutBox outBox : outBoxes) {
            boolean result = processor.processOutbox(outBox);
            if (result) {
                success++;
            } else {
                failed++;
            }
        }

        log.info("아웃박스 재처리 완료: 성공={}, 실패={}", success, failed);
        return SchedulerBatchProcessingResult.of(outBoxes.size(), success, failed);
    }
}
