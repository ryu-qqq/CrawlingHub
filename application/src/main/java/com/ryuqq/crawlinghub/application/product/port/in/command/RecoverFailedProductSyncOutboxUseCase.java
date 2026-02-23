package com.ryuqq.crawlinghub.application.product.port.in.command;

import com.ryuqq.crawlinghub.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.crawlinghub.application.product.dto.command.RecoverFailedProductSyncOutboxCommand;

/**
 * FAILED ProductSyncOutbox 복구 UseCase
 *
 * <p>FAILED 상태에서 일정 시간 경과한 아웃박스를 PENDING으로 복원하여 자동 재처리
 *
 * @author development-team
 * @since 1.0.0
 */
public interface RecoverFailedProductSyncOutboxUseCase {
    SchedulerBatchProcessingResult execute(RecoverFailedProductSyncOutboxCommand command);
}
