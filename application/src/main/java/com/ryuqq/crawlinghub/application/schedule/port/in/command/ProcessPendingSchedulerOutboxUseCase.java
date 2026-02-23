package com.ryuqq.crawlinghub.application.schedule.port.in.command;

import com.ryuqq.crawlinghub.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.crawlinghub.application.schedule.dto.command.ProcessPendingSchedulerOutboxCommand;

/**
 * PENDING 상태의 스케줄러 아웃박스 처리 UseCase
 *
 * @author development-team
 * @since 1.0.0
 */
public interface ProcessPendingSchedulerOutboxUseCase {

    /**
     * PENDING 상태의 아웃박스를 배치 처리
     *
     * @param command 처리 명령 (batchSize, delaySeconds)
     * @return 스케줄러 배치 처리 결과
     */
    SchedulerBatchProcessingResult execute(ProcessPendingSchedulerOutboxCommand command);
}
