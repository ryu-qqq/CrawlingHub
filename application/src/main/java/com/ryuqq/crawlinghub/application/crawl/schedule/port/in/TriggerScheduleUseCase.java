package com.ryuqq.crawlinghub.application.crawl.schedule.port.in;

import com.ryuqq.crawlinghub.application.crawl.schedule.dto.command.TriggerScheduleCommand;

/**
 * 스케줄 트리거 UseCase (EventBridge에서 호출)
 *
 * <p>실제 크롤링 태스크를 생성하고 Outbox에 저장합니다.
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public interface TriggerScheduleUseCase {

    /**
     * 스케줄 트리거 (크롤링 시작)
     *
     * @param command 트리거할 스케줄 ID
     */
    void execute(TriggerScheduleCommand command);
}
