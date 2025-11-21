package com.ryuqq.crawlinghub.application.schedule.port.in.command;

import com.ryuqq.crawlinghub.application.schedule.dto.command.UpdateCrawlSchedulerCommand;
import com.ryuqq.crawlinghub.application.schedule.dto.response.CrawlSchedulerResponse;

/**
 * 크롤 스케줄러 수정 Use Case (Port In).
 *
 * <p><strong>책임:</strong> 크롤 스케줄러 수정 요청 처리
 *
 * @author development-team
 * @since 1.0.0
 */
public interface UpdateCrawlSchedulerUseCase {

    /**
     * 크롤 스케줄러 수정 실행.
     *
     * @param command 수정 명령 (crawlSchedulerId, schedulerName, cronExpression, active)
     * @return 수정된 스케줄러 정보
     */
    CrawlSchedulerResponse update(UpdateCrawlSchedulerCommand command);
}
