package com.ryuqq.crawlinghub.application.crawl.schedule.orchestrator;

/**
 * 스케줄 Orchestration 결과
 * <p>
 * Sealed interface 패턴 (Java 17+)
 * </p>
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public sealed interface ScheduleOutcome permits ScheduleOutcome.Ok, ScheduleOutcome.Fail {

    /**
     * 성공 결과
     *
     * @param message 성공 메시지
     */
    record Ok(String message) implements ScheduleOutcome {
        public Ok {
            if (message == null) {
                message = "EventBridge 스케줄 작업 성공";
            }
        }
    }

    /**
     * 실패 결과
     *
     * @param errorCode    에러 코드
     * @param errorMessage 에러 메시지
     * @param cause        원인
     */
    record Fail(String errorCode, String errorMessage, String cause) implements ScheduleOutcome {
        public Fail {
            if (errorCode == null) {
                errorCode = "EVENTBRIDGE_ERROR";
            }
            if (errorMessage == null) {
                errorMessage = "EventBridge 스케줄 작업 실패";
            }
            if (cause == null) {
                cause = "Unknown";
            }
        }
    }

    /**
     * 성공 결과 생성
     */
    static Ok ok() {
        return new Ok(null);
    }

    /**
     * 성공 결과 생성 (메시지 포함)
     */
    static Ok ok(String message) {
        return new Ok(message);
    }

    /**
     * 실패 결과 생성
     */
    static Fail fail(String errorMessage) {
        return new Fail(null, errorMessage, null);
    }

    /**
     * 실패 결과 생성 (상세 정보 포함)
     */
    static Fail fail(String errorCode, String errorMessage, String cause) {
        return new Fail(errorCode, errorMessage, cause);
    }
}
