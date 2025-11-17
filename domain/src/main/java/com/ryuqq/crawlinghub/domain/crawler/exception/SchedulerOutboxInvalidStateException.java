package com.ryuqq.crawlinghub.domain.crawler.exception;

import com.ryuqq.crawlinghub.domain.common.DomainException;

import java.util.Map;
import java.util.UUID;

/**
 * SchedulerOutboxInvalidStateException - SchedulerOutbox 상태 전환 불가 시 발생하는 예외
 *
 * <p>비즈니스 메서드 실행 조건이 맞지 않을 때 발생합니다.</p>
 *
 * <p><strong>사용 시나리오:</strong></p>
 * <ul>
 *   <li>잘못된 상태에서 send() 시도</li>
 *   <li>잘못된 상태에서 complete() 시도</li>
 *   <li>잘못된 상태에서 fail() 시도</li>
 *   <li>잘못된 상태에서 retry() 시도</li>
 *   <li>재시도 횟수 초과</li>
 * </ul>
 *
 * <p><strong>HTTP 응답:</strong></p>
 * <ul>
 *   <li>Status Code: 400 BAD REQUEST</li>
 *   <li>Error Code: CRAWLER-004</li>
 *   <li>Message: "Cannot {action} outbox. Current status: {status}. Reason: {reason}"</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
public class SchedulerOutboxInvalidStateException extends DomainException {

    /**
     * Constructor - 상태 전환 불가 예외 생성 (UUID 기반)
     *
     * @param outboxId Outbox ID (UUID)
     * @param currentStatus 현재 상태
     * @param action 시도한 액션
     * @param reason 불가 사유
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public SchedulerOutboxInvalidStateException(UUID outboxId, String currentStatus,
                                                  String action, String reason) {
        super(String.format("Cannot %s outbox %s. Current status: %s. Reason: %s",
                action, outboxId, currentStatus, reason));
    }

    /**
     * Constructor - 간단한 메시지용
     *
     * @param message 에러 메시지
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public SchedulerOutboxInvalidStateException(String message) {
        super(message);
    }

    @Override
    public String code() {
        return CrawlerErrorCode.INVALID_TASK_STATE.getCode();
    }

    @Override
    public Map<String, Object> args() {
        return Map.of();
    }
}
