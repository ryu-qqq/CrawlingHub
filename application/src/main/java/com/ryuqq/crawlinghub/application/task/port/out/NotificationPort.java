package com.ryuqq.crawlinghub.application.crawl.processing.port.out;

/**
 * 알림 발송 Port
 *
 * <p>태스크 실패 시 관리자에게 알림을 발송합니다.
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public interface NotificationPort {

    /**
     * 태스크 실패 알림 발송
     *
     * @param taskId     태스크 ID
     * @param sellerId   셀러 ID
     * @param error      에러 메시지
     * @param retryCount 재시도 횟수
     */
    void notifyTaskFailure(Long taskId, Long sellerId, String error, int retryCount);

    /**
     * 태스크 최종 실패 알림 발송 (재시도 횟수 초과)
     *
     * @param taskId   태스크 ID
     * @param sellerId 셀러 ID
     * @param error    에러 메시지
     */
    void notifyTaskDead(Long taskId, Long sellerId, String error);
}
