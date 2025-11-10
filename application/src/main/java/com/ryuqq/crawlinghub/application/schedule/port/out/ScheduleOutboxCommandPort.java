package com.ryuqq.crawlinghub.application.schedule.port.out;

import com.ryuqq.crawlinghub.domain.schedule.outbox.ScheduleOutbox;

/**
 * Schedule Outbox Command Port (CQRS - Write)
 *
 * <p>ScheduleOutbox의 CUD(Create, Update, Delete) 작업을 담당하는 Port입니다.
 * <p>CQRS 패턴에 따라 Command(쓰기) 작업만 포함합니다.
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
public interface ScheduleOutboxCommandPort {

    /**
     * Outbox 저장
     *
     * <p>신규 Outbox를 생성하거나 기존 Outbox를 업데이트합니다.
     *
     * @param outbox 저장할 Outbox
     * @return 저장된 Outbox (ID 포함)
     */
    ScheduleOutbox save(ScheduleOutbox outbox);

    /**
     * Outbox 삭제
     *
     * <p>S3 Phase (Finalize)에서 완료된 Outbox를 정리할 때 사용합니다.
     *
     * @param outbox 삭제할 Outbox
     */
    void delete(ScheduleOutbox outbox);
}
