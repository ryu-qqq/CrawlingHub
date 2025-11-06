package com.ryuqq.crawlinghub.application.schedule.dto.response;

/**
 * 스케줄 Orchestration 결과
 *
 * <p>단순 Record 패턴으로 변경 (Sealed interface 제거)
 *
 * @param success      성공 여부
 * @param message      결과 메시지
 * @param errorCode    에러 코드 (실패 시)
 * @param cause        원인 (실패 시)
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
public record ScheduleOutcome(
    boolean success,
    String message,
    String errorCode,
    String cause
) {
    /**
     * 성공 결과 생성
     */
    public static ScheduleOutcome success(String message) {
        return new ScheduleOutcome(true, message, null, null);
    }

    /**
     * 실패 결과 생성
     */
    public static ScheduleOutcome failure(String errorCode, String message, String cause) {
        return new ScheduleOutcome(false, message, errorCode, cause);
    }

    /**
     * 성공 여부 확인
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * 실패 여부 확인
     */
    public boolean isFailure() {
        return !success;
    }
}


