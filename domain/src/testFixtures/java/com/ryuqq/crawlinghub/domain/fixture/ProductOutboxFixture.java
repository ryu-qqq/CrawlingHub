package com.ryuqq.crawlinghub.domain.fixture;

import com.ryuqq.crawlinghub.domain.vo.OutboxEventType;
import com.ryuqq.crawlinghub.domain.vo.OutboxId;
import com.ryuqq.crawlinghub.domain.vo.OutboxStatus;

/**
 * ProductOutbox 관련 테스트 데이터 생성 Fixture
 *
 * <p>ProductOutbox와 관련된 Value Object와 Enum의 기본값을 제공합니다.</p>
 *
 * <p>제공 메서드:</p>
 * <ul>
 *   <li>{@link #defaultOutboxId()} - 새로운 OutboxId 생성</li>
 *   <li>{@link #defaultOutboxEventType()} - 기본 이벤트 타입 (PRODUCT_CREATED)</li>
 *   <li>{@link #defaultOutboxStatus()} - 기본 상태 (WAITING)</li>
 * </ul>
 */
public class ProductOutboxFixture {

    /**
     * 기본 OutboxId 생성
     *
     * @return 새로운 UUID 기반 OutboxId
     */
    public static OutboxId defaultOutboxId() {
        return OutboxId.generate();
    }

    /**
     * 기본 OutboxEventType 반환
     *
     * @return PRODUCT_CREATED 타입
     */
    public static OutboxEventType defaultOutboxEventType() {
        return OutboxEventType.PRODUCT_CREATED;
    }

    /**
     * 기본 OutboxStatus 반환
     *
     * @return WAITING 상태
     */
    public static OutboxStatus defaultOutboxStatus() {
        return OutboxStatus.WAITING;
    }
}
