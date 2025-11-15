package com.ryuqq.crawlinghub.domain.aggregate;

import com.ryuqq.crawlinghub.domain.fixture.ProductFixture;
import com.ryuqq.crawlinghub.domain.fixture.ProductOutboxFixture;
import com.ryuqq.crawlinghub.domain.vo.OutboxEventType;
import com.ryuqq.crawlinghub.domain.vo.OutboxStatus;
import com.ryuqq.crawlinghub.domain.vo.ProductId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ProductOutbox Aggregate Root 테스트
 *
 * TDD Phase: Red → Green
 * - Cycle 23: ProductOutbox 생성 (create) 테스트
 * - Cycle 24: ProductOutbox 상태 전환 (send, complete, fail) 테스트
 * - Cycle 25: ProductOutbox 재시도 로직 (canRetry - Tell Don't Ask) 테스트
 * - 리팩토링: 정적 팩토리 메서드 패턴 (forNew/of/reconstitute) 테스트
 */
class ProductOutboxTest {

    // ========== 리팩토링: 정적 팩토리 메서드 패턴 테스트 ==========

    @Test
    void shouldCreateProductOutboxUsingForNew() {
        // Given
        ProductId productId = ProductFixture.defaultProduct().getProductId();
        OutboxEventType eventType = OutboxEventType.PRODUCT_CREATED;
        String payload = "{\"itemNo\":123456,\"name\":\"상품명\"}";

        // When
        ProductOutbox outbox = ProductOutbox.forNew(productId, eventType, payload);

        // Then
        assertThat(outbox.getOutboxId()).isNotNull();
        assertThat(outbox.getProductId()).isEqualTo(productId);
        assertThat(outbox.getEventType()).isEqualTo(eventType);
        assertThat(outbox.getPayload()).isEqualTo(payload);
        assertThat(outbox.getStatus()).isEqualTo(OutboxStatus.WAITING);
        assertThat(outbox.getRetryCount()).isEqualTo(0);
    }

    @Test
    void shouldCreateProductOutboxUsingOf() {
        // Given
        ProductId productId = ProductFixture.defaultProduct().getProductId();
        OutboxEventType eventType = OutboxEventType.PRODUCT_CREATED;
        String payload = "{\"itemNo\":123456,\"name\":\"상품명\"}";

        // When
        ProductOutbox outbox = ProductOutbox.of(productId, eventType, payload);

        // Then
        assertThat(outbox.getOutboxId()).isNotNull();
        assertThat(outbox.getProductId()).isEqualTo(productId);
        assertThat(outbox.getEventType()).isEqualTo(eventType);
        assertThat(outbox.getPayload()).isEqualTo(payload);
        assertThat(outbox.getStatus()).isEqualTo(OutboxStatus.WAITING);
    }

    @Test
    void shouldReconstituteProductOutboxWithAllFields() {
        // Given
        ProductId productId = ProductFixture.defaultProduct().getProductId();
        OutboxEventType eventType = OutboxEventType.PRODUCT_CREATED;
        String payload = "{\"itemNo\":123456,\"name\":\"상품명\"}";
        OutboxStatus status = OutboxStatus.FAILED;
        Integer retryCount = 3;
        String errorMessage = "Test error";

        // When
        ProductOutbox outbox = ProductOutbox.reconstitute(productId, eventType, payload, status, retryCount, errorMessage);

        // Then
        assertThat(outbox.getProductId()).isEqualTo(productId);
        assertThat(outbox.getEventType()).isEqualTo(eventType);
        assertThat(outbox.getPayload()).isEqualTo(payload);
        assertThat(outbox.getStatus()).isEqualTo(status);
        assertThat(outbox.getRetryCount()).isEqualTo(retryCount);
        assertThat(outbox.getErrorMessage()).isEqualTo(errorMessage);
    }

    // ========== 기존 테스트 (레거시, 유지보수용) ==========

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

    @Test
    void shouldSendOutbox() {
        // Given
        ProductOutbox outbox = ProductOutboxFixture.waitingOutbox();

        // When
        outbox.send();

        // Then
        assertThat(outbox.getStatus()).isEqualTo(OutboxStatus.SENDING);
    }

    @Test
    void shouldCompleteOutbox() {
        // Given
        ProductOutbox outbox = ProductOutboxFixture.waitingOutbox();
        outbox.send();

        // When
        outbox.complete();

        // Then
        assertThat(outbox.getStatus()).isEqualTo(OutboxStatus.COMPLETED);
    }

    @Test
    void shouldFailOutbox() {
        // Given
        ProductOutbox outbox = ProductOutboxFixture.waitingOutbox();
        outbox.send();
        String errorMessage = "HTTP 500 Internal Server Error";

        // When
        outbox.fail(errorMessage);

        // Then
        assertThat(outbox.getStatus()).isEqualTo(OutboxStatus.FAILED);
        assertThat(outbox.getErrorMessage()).isEqualTo(errorMessage);
    }

    @Test
    void shouldAllowRetryWhenCountLessThan5() {
        // Given
        ProductOutbox outbox = ProductOutboxFixture.failedOutboxWithRetryCount(3);

        // When
        boolean canRetry = outbox.canRetry();

        // Then
        assertThat(canRetry).isTrue();
    }

    @Test
    void shouldNotAllowRetryWhenCountExceeds5() {
        // Given
        ProductOutbox outbox = ProductOutboxFixture.failedOutboxWithRetryCount(5);

        // When
        boolean canRetry = outbox.canRetry();

        // Then
        assertThat(canRetry).isFalse();
    }
}
