package com.ryuqq.crawlinghub.application.product.port.in.command;

import com.ryuqq.crawlinghub.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.crawlinghub.application.product.dto.command.PublishPendingSyncOutboxCommand;

/**
 * CrawledProductSyncOutbox PENDING/FAILED SQS 발행 UseCase
 *
 * <p>PENDING 또는 재시도 가능한 FAILED 상태의 CrawledProductSyncOutbox를 SQS로 발행합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface PublishPendingSyncOutboxUseCase {

    /**
     * PENDING/FAILED CrawledProductSyncOutbox SQS 발행
     *
     * @param command 발행 커맨드 (batchSize, maxRetryCount)
     * @return 배치 처리 결과
     */
    SchedulerBatchProcessingResult execute(PublishPendingSyncOutboxCommand command);
}
