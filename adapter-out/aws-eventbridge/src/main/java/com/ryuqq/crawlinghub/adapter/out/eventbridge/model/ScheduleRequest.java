package com.ryuqq.crawlinghub.adapter.out.eventbridge.model;

/**
 * EventBridge Schedule 요청 DTO
 * <p>
 * Orchestrator Payload JSON에서 역직렬화되는 DTO입니다.
 * </p>
 *
 * @param sellerId       셀러 ID
 * @param cronExpression Cron 표현식 (HOURLY, DAILY, WEEKLY 또는 커스텀)
 * @param operation      작업 타입 (CREATE, UPDATE, DELETE)
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
public record ScheduleRequest(
        Long sellerId,
        String cronExpression,
        String operation
) {

    /**
     * 팩토리 메서드
     *
     * @param sellerId       셀러 ID
     * @param cronExpression Cron 표현식
     * @param operation      작업 타입
     * @return ScheduleRequest 인스턴스
     */
    public static ScheduleRequest of(Long sellerId, String cronExpression, String operation) {
        return new ScheduleRequest(sellerId, cronExpression, operation);
    }

    /**
     * Rule 이름 생성
     * <p>
     * EventBridge Rule 이름은 영문/숫자/하이픈만 허용됩니다.
     * </p>
     *
     * @return Rule 이름 (예: seller-crawl-schedule-12345)
     */
    public String getRuleName() {
        return "seller-crawl-schedule-" + sellerId;
    }
}
