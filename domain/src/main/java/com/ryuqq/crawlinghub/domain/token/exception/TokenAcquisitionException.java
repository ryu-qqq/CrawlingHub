package com.ryuqq.crawlinghub.domain.token.exception;

import com.ryuqq.crawlinghub.domain.common.DomainException;

import java.util.HashMap;
import java.util.Map;

/**
 * Token Acquisition Exception
 * <p>
 * 토큰 획득 실패 시 발생하는 예외
 * </p>
 * <p>
 * 컨벤션 준수:
 * <ul>
 *   <li>DomainException 상속 (Domain Layer 예외 계층)</li>
 *   <li>Pure Java (Lombok 금지)</li>
 *   <li>ErrorCode enum으로 분류</li>
 * </ul>
 * </p>
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
public class TokenAcquisitionException extends DomainException {

    private final ErrorCode errorCode;
    private final Map<String, Object> context;

    /**
     * TokenAcquisitionException 생성자
     *
     * @param errorCode 에러 코드
     * @param message 에러 메시지
     */
    public TokenAcquisitionException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.context = new HashMap<>();
    }

    /**
     * TokenAcquisitionException 생성자 (원인 포함)
     *
     * @param errorCode 에러 코드
     * @param message 에러 메시지
     * @param cause 원인 예외
     */
    public TokenAcquisitionException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.context = new HashMap<>();
    }

    /**
     * 컨텍스트 정보 추가
     *
     * @param key 키
     * @param value 값
     * @return this (fluent interface)
     */
    public TokenAcquisitionException withContext(String key, Object value) {
        this.context.put(key, value);
        return this;
    }

    @Override
    public String code() {
        return errorCode.getCode();
    }

    @Override
    public Map<String, Object> args() {
        return new HashMap<>(context);
    }

    /**
     * 에러 코드 반환
     *
     * @return ErrorCode
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * Token Acquisition Error Code
     * <p>
     * 토큰 획득 실패 유형 분류
     * </p>
     */
    public enum ErrorCode {
        /**
         * 사용 가능한 User Agent 없음
         */
        NO_AVAILABLE_USER_AGENT("TOKEN-001", "사용 가능한 User Agent가 없습니다"),

        /**
         * 분산 락 획득 실패
         */
        LOCK_ACQUISITION_FAILED("TOKEN-002", "분산 락 획득에 실패했습니다"),

        /**
         * Circuit Breaker OPEN 상태
         */
        CIRCUIT_BREAKER_OPEN("TOKEN-003", "Circuit Breaker가 OPEN 상태입니다"),

        /**
         * 토큰 발급 실패
         */
        TOKEN_ISSUANCE_FAILED("TOKEN-004", "토큰 발급에 실패했습니다"),

        /**
         * Rate Limit 초과
         */
        RATE_LIMIT_EXCEEDED("TOKEN-005", "Rate Limit을 초과했습니다");

        private final String code;
        private final String defaultMessage;

        ErrorCode(String code, String defaultMessage) {
            this.code = code;
            this.defaultMessage = defaultMessage;
        }

        public String getCode() {
            return code;
        }

        public String getDefaultMessage() {
            return defaultMessage;
        }
    }
}
