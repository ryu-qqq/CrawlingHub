package com.ryuqq.crawlinghub.domain.crawler.vo;

/**
 * ScheduleStatus - CrawlingSchedule 상태 Enum
 *
 * <p>CrawlingSchedule의 활성화/비활성화 상태를 나타냅니다.</p>
 *
 * <p><strong>상태 정의:</strong></p>
 * <ul>
 *   <li>✅ ACTIVE: 활성화 (크롤링 스케줄 실행 중)</li>
 *   <li>✅ INACTIVE: 비활성화 (일시 정지)</li>
 *   <li>✅ FAILED: 실패 (스케줄 등록 실패)</li>
 * </ul>
 *
 * <p><strong>Zero-Tolerance 규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Lombok 금지 (Java Enum 사용)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
public enum ScheduleStatus {

    /**
     * 활성화 - 크롤링 스케줄 실행 중
     */
    ACTIVE,

    /**
     * 비활성화 - 일시 정지
     */
    INACTIVE,

    /**
     * 실패 - 스케줄 등록 실패
     */
    FAILED
}
