package com.ryuqq.crawlinghub.application.seller.port.out.command;

/**
 * Scheduler Command Port.
 *
 * <p>EventBridge 바운더리 컨텍스트의 Scheduler를 제어하기 위한 Port입니다.</p>
 */
public interface SchedulerCommandPort {

    /**
     * 스케줄러를 비활성화합니다.
     *
     * @param schedulerId 스케줄러 ID
     */
    void deactivateScheduler(Long schedulerId);
}

