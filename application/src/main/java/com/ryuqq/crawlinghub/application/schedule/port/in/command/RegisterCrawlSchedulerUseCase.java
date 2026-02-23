package com.ryuqq.crawlinghub.application.schedule.port.in.command;

import com.ryuqq.crawlinghub.application.schedule.dto.command.RegisterCrawlSchedulerCommand;

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
     * @return 등록된 스케줄러 ID
     */
    long register(RegisterCrawlSchedulerCommand command);
}
