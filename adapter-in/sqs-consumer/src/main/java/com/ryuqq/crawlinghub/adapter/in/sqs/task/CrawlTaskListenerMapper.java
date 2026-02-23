package com.ryuqq.crawlinghub.adapter.in.sqs.task;

import com.ryuqq.crawlinghub.application.execution.dto.command.ExecuteCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.task.dto.messaging.CrawlTaskPayload;
import org.springframework.stereotype.Component;

/**
 * CrawlTask 리스너 매퍼
 *
 * <p><strong>용도</strong>: CrawlTaskPayload → ExecuteCrawlTaskCommand 변환
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlTaskListenerMapper {

    public ExecuteCrawlTaskCommand toCommand(CrawlTaskPayload payload) {
        return new ExecuteCrawlTaskCommand(
                payload.taskId(),
                payload.schedulerId(),
                payload.sellerId(),
                payload.taskType(),
                payload.endpoint());
    }
}
