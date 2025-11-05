package com.ryuqq.crawlinghub.application.schedule.port.in;

import com.ryuqq.crawlinghub.application.schedule.dto.command.CreateScheduleCommand;
import com.ryuqq.crawlinghub.application.schedule.dto.response.ScheduleResponse;

/**
 * 크롤링 스케줄 생성 UseCase
 *
 * <p>신규 스케줄을 생성하고 EventBridge에 등록합니다.
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public interface CreateScheduleUseCase {

    /**
     * 스케줄 생성
     *
     * @param command 생성할 스케줄 정보
     * @return 생성된 스케줄 정보
     */
    ScheduleResponse execute(CreateScheduleCommand command);
}
