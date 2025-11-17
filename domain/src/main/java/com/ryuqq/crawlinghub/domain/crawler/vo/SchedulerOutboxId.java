package com.ryuqq.crawlinghub.domain.crawler.vo;

import java.util.UUID;

/**
 * SchedulerOutbox ID Value Object
 *
 * <p>SchedulerOutbox Aggregate의 고유 식별자입니다.</p>
 *
 * <p><strong>특징:</strong></p>
 * <ul>
 *   <li>✅ UUID 기반 고유 식별자</li>
 *   <li>✅ Record 패턴 (Immutable)</li>
 *   <li>✅ Lombok 금지 (Zero-Tolerance)</li>
 * </ul>
 *
 * @param value UUID 값
 * @author ryu-qqq
 * @since 2025-11-17
 */
public record SchedulerOutboxId(UUID value) {

    /**
     * 새로운 SchedulerOutboxId 생성 (UUID 기반)
     *
     * @return 생성된 SchedulerOutboxId
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public static SchedulerOutboxId generate() {
        return new SchedulerOutboxId(UUID.randomUUID());
    }

    /**
     * 기존 UUID로부터 SchedulerOutboxId 재구성
     *
     * @param value UUID 값
     * @return 재구성된 SchedulerOutboxId
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public static SchedulerOutboxId of(UUID value) {
        return new SchedulerOutboxId(value);
    }
}
