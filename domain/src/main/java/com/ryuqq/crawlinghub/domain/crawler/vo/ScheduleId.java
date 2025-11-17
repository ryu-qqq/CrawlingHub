package com.ryuqq.crawlinghub.domain.crawler.vo;

import java.util.UUID;

/**
 * ScheduleId - CrawlingSchedule 식별자 Value Object
 *
 * <p>CrawlingSchedule Aggregate의 고유 식별자입니다.</p>
 *
 * <p><strong>특징:</strong></p>
 * <ul>
 *   <li>✅ UUID 기반 (데이터베이스 독립적)</li>
 *   <li>✅ 불변성 (Record 패턴)</li>
 *   <li>✅ Factory Method (generate)</li>
 * </ul>
 *
 * <p><strong>Zero-Tolerance 규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Lombok 금지 (Record 패턴 사용)</li>
 *   <li>✅ 불변성 (Immutable)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
public record ScheduleId(UUID value) {

    /**
     * 새로운 ScheduleId 생성
     *
     * <p>UUID.randomUUID()를 사용하여 고유한 식별자를 생성합니다.</p>
     *
     * @return 새로운 ScheduleId 인스턴스
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public static ScheduleId generate() {
        return new ScheduleId(UUID.randomUUID());
    }
}
