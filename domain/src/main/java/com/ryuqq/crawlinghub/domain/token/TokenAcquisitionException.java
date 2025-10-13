package com.ryuqq.crawlinghub.domain.token;

/**
 * 토큰 획득 실패 예외
 *
 * @author crawlinghub
 */
public class TokenAcquisitionException extends RuntimeException {

    public enum Reason {
        POOL_EXHAUSTED("사용 가능한 User-Agent가 없습니다"),
        LOCK_ACQUISITION_FAILED("분산 락 획득에 실패했습니다"),
        CIRCUIT_BREAKER_OPEN("Circuit Breaker가 OPEN 상태입니다"),
        TOKEN_EXPIRED("토큰이 만료되었습니다"),
        RATE_LIMIT_EXCEEDED("Rate Limit을 초과했습니다"),
        INVALID_USER_AGENT("유효하지 않은 User-Agent입니다");

        private final String message;

        Reason(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    private final Reason reason;

    public TokenAcquisitionException(Reason reason) {
        super(reason.getMessage());
        this.reason = reason;
    }

    public TokenAcquisitionException(Reason reason, Throwable cause) {
        super(reason.getMessage(), cause);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}
