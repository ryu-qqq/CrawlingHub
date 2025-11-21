package com.ryuqq.crawlinghub.application.schedule.port.in.command;

import com.ryuqq.crawlinghub.application.schedule.dto.command.RegisterCrawlSchedulerCommand;
import com.ryuqq.crawlinghub.application.schedule.dto.response.CrawlSchedulerResponse;

/**
 * 크롤 스케줄러 등록 Use Case (Port In).
 *
 * <p><strong>책임:</strong> 크롤 스케줄러 등록 요청 처리
 *
 * @author development-team
 * @since 1.0.0
 */
public interface RegisterCrawlSchedulerUseCase {

    /**
     * 크롤 스케줄러 등록 실행.
     *
     * @param command 등록 명령 (sellerId, schedulerName, cronExpression)
     * @return 등록된 스케줄러 정보
     */
    CrawlSchedulerResponse register(RegisterCrawlSchedulerCommand command);
}
