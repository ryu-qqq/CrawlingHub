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
    PENDING("대기"),

    /** 처리 중 (EventBridge 동기화 진행 중) */
    PROCESSING("처리중"),

    /** 동기화 완료 */
    COMPLETED("완료"),

    /** 동기화 실패 */
    FAILED("실패");

    private final String displayName;

    CrawlSchedulerOubBoxStatus(String displayName) {
        this.displayName = displayName;
    }

    /**
     * 사용자 표시용 이름 반환
     *
     * @return 표시명
     */
    public String displayName() {
        return displayName;
    }
}
