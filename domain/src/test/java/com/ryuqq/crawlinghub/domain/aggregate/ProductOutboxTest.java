package com.ryuqq.crawlinghub.domain.aggregate;

import com.ryuqq.crawlinghub.domain.fixture.ProductFixture;
import com.ryuqq.crawlinghub.domain.vo.OutboxEventType;
import com.ryuqq.crawlinghub.domain.vo.OutboxStatus;
import com.ryuqq.crawlinghub.domain.vo.ProductId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ProductOutbox Aggregate Root 테스트
 *
 * TDD Phase: Red → Green
 * - ProductOutbox 생성 (create) 테스트
 */
class ProductOutboxTest {

    @Test
    void shouldCreateProductOutboxWithWaitingStatus() {
        // Given
        ProductId productId = ProductFixture.defaultProduct().getProductId();
        OutboxEventType eventType = OutboxEventType.PRODUCT_CREATED;
        String payload = "{\"itemNo\":123456,\"name\":\"상품명\"}";

        // When
        ProductOutbox outbox = ProductOutbox.create(productId, eventType, payload);

        // Then
        assertThat(outbox.getOutboxId()).isNotNull();
        assertThat(outbox.getProductId()).isEqualTo(productId);
        assertThat(outbox.getEventType()).isEqualTo(eventType);
        assertThat(outbox.getPayload()).isEqualTo(payload);
        assertThat(outbox.getStatus()).isEqualTo(OutboxStatus.WAITING);
        assertThat(outbox.getRetryCount()).isEqualTo(0);
    }
}
