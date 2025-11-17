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

    /**
     * 새로운 SchedulerOutboxId 생성 (표준 패턴)
     *
     * @return 새로 생성된 SchedulerOutboxId
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public static SchedulerOutboxId forNew() {
        return generate();
    }

    /**
     * 새로운 ID인지 확인 (표준 패턴)
     *
     * <p>UUID 기반 ID는 생성 시점에서만 의미가 있으므로 항상 true를 반환합니다.</p>
     * <p>실제 영속성 상태는 Aggregate Root에서 관리됩니다.</p>
     *
     * @return 항상 true
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public boolean isNew() {
        return true;
    }
}
