package com.ryuqq.crawlinghub.adapter.out.sqs.exception;

/**
 * SQS 발행 예외
 *
 * <p><strong>용도</strong>: SQS 메시지 발행 실패 시 발생하는 예외
 *
 * <p><strong>처리</strong>: Application 레이어에서 catch하여 Outbox 상태를 FAILED로 변경
 *
 * @author development-team
 * @since 1.0.0
 */
public class SqsPublishException extends RuntimeException {

    public SqsPublishException(String message) {
        super(message);
    }

    public SqsPublishException(String message, Throwable cause) {
        super(message, cause);
    }
}
