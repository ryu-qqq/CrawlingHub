package com.ryuqq.crawlinghub.application.task.port.in.command;

import com.ryuqq.crawlinghub.application.task.dto.command.RetryCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskResult;

/**
 * CrawlTask 재실행 UseCase (Port In - Command)
 *
 * <p>실패한 CrawlTask를 재실행합니다.
 *
 * <p><strong>비즈니스 규칙:</strong>
 *
 * <ul>
 *   <li>재시도 가능한 상태 (FAILED, TIMEOUT)의 Task만 재실행 가능
 *   <li>최대 재시도 횟수(3회) 초과 시 재실행 불가
 *   <li>재실행 시 Task 상태가 RETRY로 변경되고 SQS에 재발행
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public interface RetryCrawlTaskUseCase {

    /**
     * CrawlTask 재실행
     *
     * @param command 재실행 커맨드 (crawlTaskId)
     * @return 재실행된 CrawlTask 응답
     * @throws com.ryuqq.crawlinghub.domain.task.exception.CrawlTaskNotFoundException Task가 존재하지 않을
     *     때
     * @throws com.ryuqq.crawlinghub.domain.task.exception.CrawlTaskRetryException 재시도 불가 상태이거나 최대
     *     재시도 횟수 초과 시
     */
    CrawlTaskResult retry(RetryCrawlTaskCommand command);
}
