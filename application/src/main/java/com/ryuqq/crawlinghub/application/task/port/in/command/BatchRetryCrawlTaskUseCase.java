package com.ryuqq.crawlinghub.application.task.port.in.command;

import com.ryuqq.crawlinghub.application.task.dto.command.BatchRetryCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.task.dto.response.BatchRetryResultResponse;

/**
 * CrawlTask 배치 재실행 UseCase
 *
 * <p>여러 실패한 CrawlTask를 일괄로 재실행합니다.
 *
 * <p><strong>비즈니스 규칙:</strong>
 *
 * <ul>
 *   <li>각 Task의 재시도 가능 여부를 개별 검증
 *   <li>실패한 Task가 있어도 성공한 Task는 처리됨
 *   <li>최대 100개의 Task를 한 번에 처리 가능
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public interface BatchRetryCrawlTaskUseCase {

    /**
     * 여러 CrawlTask를 일괄 재실행합니다.
     *
     * @param command 배치 재실행 명령
     * @return 배치 재처리 결과 (성공/실패 목록)
     */
    BatchRetryResultResponse retryBatch(BatchRetryCrawlTaskCommand command);
}
