package com.ryuqq.crawlinghub.domain.fixture;

import com.ryuqq.crawlinghub.domain.aggregate.ProductOutbox;
import com.ryuqq.crawlinghub.domain.vo.OutboxEventType;
import com.ryuqq.crawlinghub.domain.vo.OutboxId;
import com.ryuqq.crawlinghub.domain.vo.OutboxStatus;
import com.ryuqq.crawlinghub.domain.vo.ProductId;

/**
 * ProductOutbox 관련 테스트 데이터 생성 Fixture
 *
 * <p>ProductOutbox와 관련된 Value Object, Enum, Aggregate의 기본값을 제공합니다.</p>
 *
 * <p>제공 메서드:</p>
 * <ul>
 *   <li>{@link #defaultOutboxId()} - 새로운 OutboxId 생성</li>
 *   <li>{@link #defaultOutboxEventType()} - 기본 이벤트 타입 (PRODUCT_CREATED)</li>
 *   <li>{@link #defaultOutboxStatus()} - 기본 상태 (WAITING)</li>
 *   <li>{@link #waitingOutbox()} - WAITING 상태의 ProductOutbox</li>
 *   <li>{@link #failedOutboxWithRetryCount(int)} - FAILED 상태의 ProductOutbox (지정된 재시도 횟수)</li>
 * </ul>
 */
public class ProductOutboxFixture {

    private static final String DEFAULT_PAYLOAD = "{\"itemNo\":123456,\"name\":\"상품명\"}";

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

    /**
     * OutboxStatus.WAITING 상태의 ProductOutbox 생성
     *
     * @return WAITING 상태의 ProductOutbox
     */
    public static ProductOutbox waitingOutbox() {
        ProductId productId = ProductFixture.defaultProductId();
        OutboxEventType eventType = OutboxEventType.PRODUCT_CREATED;
        return ProductOutbox.create(productId, eventType, DEFAULT_PAYLOAD);
    }

    /**
     * OutboxStatus.FAILED 상태의 ProductOutbox 생성 (지정된 재시도 횟수)
     *
     * <p>재시도 로직 테스트를 위해 사용됩니다.</p>
     * <p>실제 fail() 메서드를 호출하여 retryCount를 설정합니다.</p>
     *
     * @param retryCount 재시도 횟수
     * @return FAILED 상태의 ProductOutbox (지정된 retryCount)
     */
    public static ProductOutbox failedOutboxWithRetryCount(int retryCount) {
        ProductOutbox outbox = waitingOutbox();
        outbox.send();

        // fail() 메서드를 retryCount번 호출하여 retryCount 설정
        for (int i = 0; i < retryCount; i++) {
            outbox.fail("테스트 에러 메시지 " + (i + 1));
            if (i < retryCount - 1) {
                // 마지막 반복이 아니면 다시 SENDING 상태로 전환
                outbox.send();
            }
        }

        return outbox;
    }
}
