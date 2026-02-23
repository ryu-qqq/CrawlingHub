package com.ryuqq.crawlinghub.domain.execution.exception;

/**
 * 일시적 인프라 오류로 인한 실행 실패 예외
 *
 * <p><strong>용도</strong>: DB 커넥션, 트랜잭션 생성 실패 등 인프라 수준의 일시적 장애를 나타냅니다. 이 예외가 SQS Consumer까지 전파되면
 * 메시지를 NACK하여 SQS 재시도를 트리거합니다.
 *
 * <p><strong>비재시도 대상</strong>: 비즈니스 로직 실패 (상태 전환 오류, 잘못된 데이터 등)는 이 예외로 래핑하지 않습니다. 재시도해도 동일하게 실패하기
 * 때문입니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public class RetryableExecutionException extends RuntimeException {

    public RetryableExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
