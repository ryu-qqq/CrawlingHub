package com.ryuqq.crawlinghub.application.crawl.schedule.port.in;

import com.ryuqq.crawlinghub.application.crawl.schedule.dto.command.UpdateScheduleCommand;
import com.ryuqq.crawlinghub.application.crawl.schedule.dto.response.ScheduleResponse;

/**
 * 크롤링 스케줄 수정 UseCase
 *
 * <p>기존 스케줄의 Cron 표현식을 변경하고 EventBridge를 업데이트합니다.
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public interface UpdateScheduleUseCase {

    /**
     * 스케줄 수정
     *
     * @param command 수정할 스케줄 정보
     * @return 수정된 스케줄 정보
     */
    ScheduleResponse execute(UpdateScheduleCommand command);
}
