package com.ryuqq.crawlinghub.adapter.out.eventbridge.exception;

/**
 * EventBridge 발행 예외
 *
 * <p><strong>용도</strong>: EventBridge 스케줄 생성/수정 실패 시 발생하는 예외
 *
 * <p><strong>처리</strong>: Application 레이어에서 catch하여 Outbox 상태를 FAILED로 변경
 *
 * @author development-team
 * @since 1.0.0
 */
public class EventBridgePublishException extends RuntimeException {

    public EventBridgePublishException(String message) {
        super(message);
    }

    public EventBridgePublishException(String message, Throwable cause) {
        super(message, cause);
    }
}
