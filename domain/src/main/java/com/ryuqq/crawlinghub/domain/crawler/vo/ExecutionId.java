package com.ryuqq.crawlinghub.domain.crawler.vo;

import java.util.UUID;

/**
 * ExecutionId - 스케줄 실행 ID Value Object
 *
 * <p>CrawlingScheduleExecution의 고유 식별자입니다.</p>
 *
 * <p><strong>생성 방식:</strong></p>
 * <ul>
 *   <li>✅ UUID 기반 자동 생성 (generate)</li>
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
public record ExecutionId(UUID value) {

    /**
     * 새로운 ExecutionId 생성
     *
     * <p>UUID.randomUUID()를 사용하여 고유한 ID를 생성합니다.</p>
     *
     * @return 생성된 ExecutionId
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public static ExecutionId generate() {
        return new ExecutionId(UUID.randomUUID());
    }

    /**
     * 새로운 ExecutionId 생성 (표준 패턴)
     *
     * @return 새로 생성된 ExecutionId
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public static ExecutionId forNew() {
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
