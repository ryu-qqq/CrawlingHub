package com.ryuqq.crawlinghub.adapter.in.sqs.scheduler;

import com.ryuqq.crawlinghub.application.task.dto.command.TriggerCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.task.dto.messaging.EventBridgeTriggerPayload;
import org.springframework.stereotype.Component;

/**
 * EventBridge 트리거 리스너 매퍼
 *
 * <p><strong>용도</strong>: EventBridgeTriggerPayload → TriggerCrawlTaskCommand 변환
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class EventBridgeTriggerListenerMapper {

    public TriggerCrawlTaskCommand toCommand(EventBridgeTriggerPayload payload) {
        return new TriggerCrawlTaskCommand(payload.schedulerId());
    }
}
