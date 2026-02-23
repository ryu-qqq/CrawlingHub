package com.ryuqq.crawlinghub.application.execution.port.in.command;

import com.ryuqq.crawlinghub.application.execution.dto.command.ExecuteCrawlTaskCommand;
import com.ryuqq.crawlinghub.domain.execution.exception.RetryableExecutionException;

/**
 * CrawlTask 실행 UseCase (Port In)
 *
 * <p><strong>용도</strong>: SQS에서 수신한 CrawlTask 메시지를 처리하여 크롤링 실행
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <pre>
 * CrawlTaskSqsListener
 *     ↓
 * CrawlTaskExecutionUseCase.execute()
 *     ↓
 * 1. CrawlTask 상태 업데이트 (RUNNING)
 * 2. 크롤링 실행 (WebClient)
 * 3. 결과 저장
 * 4. CrawlTask 상태 업데이트 (COMPLETED/FAILED)
 * </pre>
 *
 * <p><strong>예외 처리</strong>:
 *
 * <ul>
 *   <li>인프라 오류 (DB, TX) → RetryableExecutionException → SQS 재시도
 *   <li>RUNNING 이후 오류 → safeCompleteWithFailure (내부 처리)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public interface CrawlTaskExecutionUseCase {

    /**
     * CrawlTask 실행
     *
     * @param command 실행 커맨드 (taskId, schedulerId, sellerId, taskType, endpoint)
     * @throws RetryableExecutionException 인프라 오류 시 (SQS 재시도 대상)
     */
    void execute(ExecuteCrawlTaskCommand command);
}
