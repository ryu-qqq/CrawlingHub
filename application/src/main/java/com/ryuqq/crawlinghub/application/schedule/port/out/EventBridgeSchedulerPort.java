package com.ryuqq.crawlinghub.application.schedule.port.out;

/**
 * AWS EventBridge Scheduler Port (외부 API)
 *
 * <p>⚠️ 트랜잭션 경계 주의:
 * <ul>
 *   <li>이 Port는 외부 AWS 서비스 호출이므로 트랜잭션 밖에서 실행해야 합니다</li>
 *   <li>UseCase 레벨에서 트랜잭션 분리 필요</li>
 * </ul>
 *
 * <p>EventBridge Adapter에 의해 구현됩니다.
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public interface EventBridgeSchedulerPort {

    /**
     * EventBridge 스케줄 등록
     *
     * <p>⚠️ 외부 API 호출 - 트랜잭션 밖에서 실행
     *
     * <p>Adapter-Out 책임:
     * <ul>
     *   <li>scheduleName 생성: "seller-crawl-schedule-{scheduleId}"</li>
     *   <li>targetArn 내부 관리 (@Value 주입)</li>
     *   <li>Target Input 설정: "{\"sellerId\": {sellerId}}"</li>
     * </ul>
     *
     * @param scheduleId     스케줄 ID (Rule 이름 생성용)
     * @param sellerId       셀러 ID (Target Input 전달용)
     * @param cronExpression Cron 표현식
     * @return EventBridge Schedule Name
     */
    String registerSchedule(Long scheduleId, Long sellerId, String cronExpression);

    /**
     * EventBridge 스케줄 업데이트
     *
     * <p>⚠️ 외부 API 호출 - 트랜잭션 밖에서 실행
     *
     * @param scheduleId     스케줄 ID (Rule 이름 조회용)
     * @param sellerId       셀러 ID (Target Input 전달용)
     * @param cronExpression 새로운 Cron 표현식
     */
    void updateSchedule(Long scheduleId, Long sellerId, String cronExpression);

    /**
     * EventBridge 스케줄 삭제
     *
     * <p>⚠️ 외부 API 호출 - 트랜잭션 밖에서 실행
     *
     * @param scheduleId 스케줄 ID (Rule 이름 조회용)
     * @param sellerId   셀러 ID (Target ID 조회용)
     */
    void deleteSchedule(Long scheduleId, Long sellerId);
}
