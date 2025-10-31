package com.ryuqq.crawlinghub.application.mustit.seller.port.out;

import com.ryuqq.crawlinghub.domain.common.DomainEvent;

/**
 * 도메인 이벤트 발행 Port
 *
 * <p>Messaging Adapter (EventBridge, Kafka 등)에 의해 구현됩니다.
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public interface EventPublisherPort {

    /**
     * 도메인 이벤트 발행
     *
     * @param event 발행할 도메인 이벤트 (null 불가)
     * @throws IllegalArgumentException event가 null인 경우
     */
    void publish(DomainEvent event);
}
