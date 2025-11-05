package com.ryuqq.crawlinghub.domain.useragent.exception;

import com.ryuqq.crawlinghub.domain.common.DomainException;

/**
 * UserAgent Exception - Sealed Abstract Class
 *
 * <p>UserAgent Bounded Context의 모든 예외를 묶는 Sealed 추상 클래스입니다.</p>
 *
 * <p><strong>예외 계층:</strong></p>
 * <ul>
 *   <li>InvalidUserAgentException - 유효하지 않은 User-Agent 문자열 (400)</li>
 *   <li>TokenExpiredException - 토큰이 만료됨 (400)</li>
 *   <li>RateLimitExceededException - Rate Limit 초과 (429)</li>
 *   <li>NoAvailableUserAgentException - 사용 가능한 User-Agent 없음 (404)</li>
 * </ul>
 *
 * <p><strong>Sealed Classes 장점:</strong></p>
 * <ul>
 *   <li>✅ 허용된 예외만 상속 가능 (컴파일 타임 검증)</li>
 *   <li>✅ Switch Expression에서 Exhaustive Checking (모든 케이스 처리 강제)</li>
 *   <li>✅ 타입 안전성 향상</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
public sealed abstract class UserAgentException extends DomainException
    permits InvalidUserAgentException,
            TokenExpiredException,
            RateLimitExceededException,
            NoAvailableUserAgentException {

    /**
     * UserAgentException 생성자
     *
     * @param message 에러 메시지
     */
    protected UserAgentException(String message) {
        super(message);
    }

    /**
     * UserAgentException 생성자 (원인 포함)
     *
     * @param message 에러 메시지
     * @param cause 원인 예외
     */
    protected UserAgentException(String message, Throwable cause) {
        super(message, cause);
    }
}

