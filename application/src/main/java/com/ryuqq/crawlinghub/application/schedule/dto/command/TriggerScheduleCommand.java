package com.ryuqq.crawlinghub.application.schedule.dto.command;

/**
 * 스케줄 트리거 Command (EventBridge에서 호출)
 *
 * @param scheduleId 스케줄 ID (필수)
 * @author ryu-qqq
 * @since 2025-10-31
 */
public record TriggerScheduleCommand(
    Long scheduleId
) {
    public TriggerScheduleCommand {
        if (scheduleId == null) {
            throw new IllegalArgumentException("스케줄 ID는 필수입니다");
        }
    }
}
