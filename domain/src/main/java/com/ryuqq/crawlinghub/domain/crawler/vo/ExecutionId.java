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
}
