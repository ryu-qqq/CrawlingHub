package com.ryuqq.crawlinghub.application.execution.port.in;

import com.ryuqq.crawlinghub.application.execution.dto.command.ExecuteCrawlTaskCommand;

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
 * 5. Outbox 상태 업데이트 (PROCESSED/FAILED)
 * </pre>
 *
 * <p><strong>예외 처리</strong>:
 *
 * <ul>
 *   <li>크롤링 실패 (429 등) → RuntimeException → DLQ로 이동
 *   <li>DLQ에서 Outbox FAILED 마킹
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
     * @throws RuntimeException 크롤링 실패 시 (DLQ 처리를 위해 예외 전파)
     */
    void execute(ExecuteCrawlTaskCommand command);
}
