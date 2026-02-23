package com.ryuqq.crawlinghub.application.product.port.in.command;

import com.ryuqq.crawlinghub.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.crawlinghub.application.product.dto.command.ProcessPendingCrawledRawCommand;

/**
 * PENDING 상태의 CrawledRaw 가공 처리 UseCase
 *
 * <p>스케줄러에서 호출하여 PENDING 상태의 Raw 데이터를 타입별로 가공합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface ProcessPendingCrawledRawUseCase {

    /**
     * PENDING 상태의 CrawledRaw를 가공 처리
     *
     * @param command 처리 커맨드 (크롤링 타입, 배치 크기)
     * @return 배치 처리 결과
     */
    SchedulerBatchProcessingResult execute(ProcessPendingCrawledRawCommand command);
}
