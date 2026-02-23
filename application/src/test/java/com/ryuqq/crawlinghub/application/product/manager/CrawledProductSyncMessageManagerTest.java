package com.ryuqq.crawlinghub.application.product.manager;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;

import com.ryuqq.cralwinghub.domain.fixture.product.CrawledProductSyncOutboxFixture;
import com.ryuqq.crawlinghub.application.product.port.out.client.CrawledProductSyncMessageClient;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawledProductSyncMessageManager 단위 테스트
 *
 * <p>Mockist 스타일 테스트: MessageClient 의존성 Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("application")
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawledProductSyncMessageManager 단위 테스트")
class CrawledProductSyncMessageManagerTest {

    @Mock private CrawledProductSyncMessageClient messageClient;

    @InjectMocks private CrawledProductSyncMessageManager sut;

    @Nested
    @DisplayName("publish() 메서드 테스트")
    class PublishTest {

        @Test
        @DisplayName("[성공] Outbox 메시지 발행 - MessageClient에 위임")
        void shouldDelegatePublishToMessageClient() {
            // Given
            CrawledProductSyncOutbox outbox =
                    CrawledProductSyncOutboxFixture.aReconstitutedPending();
            willDoNothing().given(messageClient).publish(outbox);

            // When
            sut.publish(outbox);

            // Then
            then(messageClient).should().publish(outbox);
        }

        @Test
        @DisplayName("[실패] MessageClient 예외 발생 시 예외 전파")
        void shouldPropagateExceptionWhenClientFails() {
            // Given
            CrawledProductSyncOutbox outbox =
                    CrawledProductSyncOutboxFixture.aReconstitutedPending();
            willThrow(new RuntimeException("SQS 연결 실패")).given(messageClient).publish(outbox);

            // When & Then
            assertThatThrownBy(() -> sut.publish(outbox))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("SQS 연결 실패");

            then(messageClient).should().publish(outbox);
        }

        @Test
        @DisplayName("[성공] CREATE 타입 Outbox 메시지 발행")
        void shouldPublishCreateTypeOutbox() {
            // Given
            CrawledProductSyncOutbox outbox = CrawledProductSyncOutboxFixture.aPendingForCreate();
            willDoNothing().given(messageClient).publish(outbox);

            // When
            sut.publish(outbox);

            // Then
            then(messageClient).should().publish(outbox);
        }

        @Test
        @DisplayName("[성공] UPDATE 타입 Outbox 메시지 발행")
        void shouldPublishUpdateTypeOutbox() {
            // Given
            CrawledProductSyncOutbox outbox = CrawledProductSyncOutboxFixture.aPendingForUpdate();
            willDoNothing().given(messageClient).publish(outbox);

            // When
            sut.publish(outbox);

            // Then
            then(messageClient).should().publish(outbox);
        }
    }
}
