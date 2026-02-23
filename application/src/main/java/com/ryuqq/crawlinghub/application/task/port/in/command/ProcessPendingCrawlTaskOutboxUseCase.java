package com.ryuqq.crawlinghub.application.task.port.in.command;

import com.ryuqq.crawlinghub.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.crawlinghub.application.task.dto.command.ProcessPendingCrawlTaskOutboxCommand;

/**
 * CrawlTask Outbox PENDING 처리 UseCase (Port In)
 *
 * <p><strong>용도</strong>: 스케줄러에서 PENDING 상태의 CrawlTask Outbox를 배치 처리
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>delaySeconds 이상 경과한 PENDING Outbox 조회
 *   <li>각 Outbox에 대해 CrawlTask 상태 전환 + SQS 발행
 *   <li>성공 시 SENT, 실패 시 FAILED로 변경
 * </ol>
 *
 * @author development-team
 * @since 1.0.0
 */
public interface ProcessPendingCrawlTaskOutboxUseCase {

    /**
     * PENDING 상태의 CrawlTask Outbox 배치 처리
     *
     * @param command 배치 처리 커맨드 (batchSize, delaySeconds)
     * @return 배치 처리 결과
     */
    SchedulerBatchProcessingResult execute(ProcessPendingCrawlTaskOutboxCommand command);
}
