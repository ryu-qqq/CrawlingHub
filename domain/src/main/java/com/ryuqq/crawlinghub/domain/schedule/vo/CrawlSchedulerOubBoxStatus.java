package com.ryuqq.crawlinghub.domain.schedule.vo;

/**
 * 크롤 스케줄러 아웃박스 상태
 *
 * <p><strong>도메인 규칙</strong>:
 *
 * <ul>
 *   <li>PENDING: AWS EventBridge 동기화 대기 중
 *   <li>COMPLETED: AWS EventBridge 동기화 완료
 *   <li>FAILED: AWS EventBridge 동기화 실패
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public enum CrawlSchedulerOubBoxStatus {

    /** 동기화 대기 중 */
    PENDING,

    /** 동기화 완료 */
    COMPLETED,

    /** 동기화 실패 */
    FAILED
}
