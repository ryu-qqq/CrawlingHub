package com.ryuqq.crawlinghub.application.crawl.task.port.in.command;

import com.ryuqq.crawlinghub.application.crawl.task.dto.command.TriggerCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.crawl.task.dto.response.CrawlTaskResponse;

/**
 * CrawlTask 트리거 UseCase (Port In - Command)
 *
 * <p>EventBridge에서 호출되어 CrawlTask 생성 및 SQS 발행
 *
 * @author development-team
 * @since 1.0.0
 */
public interface TriggerCrawlTaskUseCase {

    /**
     * CrawlTask 트리거 실행
     *
     * @param command 트리거 커맨드 (crawlSchedulerId)
     * @return 생성된 CrawlTask 응답
     */
    CrawlTaskResponse execute(TriggerCrawlTaskCommand command);
}
