package com.ryuqq.crawlinghub.application.product.port.in.command;

import com.ryuqq.crawlinghub.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.crawlinghub.application.product.dto.command.RecoverTimeoutProductSyncOutboxCommand;

/**
 * 타임아웃 ProductSyncOutbox 복구 UseCase
 *
 * <p>PROCESSING 상태에서 일정 시간 초과된 좀비 아웃박스를 PENDING으로 복원
 *
 * @author development-team
 * @since 1.0.0
 */
public interface RecoverTimeoutProductSyncOutboxUseCase {
    SchedulerBatchProcessingResult execute(RecoverTimeoutProductSyncOutboxCommand command);
}
