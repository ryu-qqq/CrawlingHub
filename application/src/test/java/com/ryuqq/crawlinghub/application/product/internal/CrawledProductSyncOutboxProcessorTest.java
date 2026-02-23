package com.ryuqq.crawlinghub.application.product.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.inOrder;

import com.ryuqq.cralwinghub.domain.fixture.product.CrawledProductSyncOutboxFixture;
import com.ryuqq.crawlinghub.application.product.manager.CrawledProductSyncMessageManager;
import com.ryuqq.crawlinghub.application.product.manager.CrawledProductSyncOutboxCommandManager;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawledProductSyncOutboxProcessor 단위 테스트
 *
 * <p>Mockist 스타일 테스트: CommandManager, MessageManager 의존성 Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("application")
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawledProductSyncOutboxProcessor 단위 테스트")
class CrawledProductSyncOutboxProcessorTest {

    @Mock private CrawledProductSyncOutboxCommandManager commandManager;
    @Mock private CrawledProductSyncMessageManager messageManager;

    @InjectMocks private CrawledProductSyncOutboxProcessor sut;

    @Nested
    @DisplayName("processOutbox() 메서드 테스트")
    class ProcessOutboxTest {

        @Test
        @DisplayName("[성공] SQS 발행 성공 시 true 반환 및 순서대로 처리")
        void shouldReturnTrueAndFollowProcessingOrderWhenSuccess() {
            // Given
            CrawledProductSyncOutbox outbox =
                    CrawledProductSyncOutboxFixture.aReconstitutedPending();
            willDoNothing().given(commandManager).markAsProcessing(outbox);
            willDoNothing().given(messageManager).publish(outbox);
            willDoNothing().given(commandManager).markAsSent(outbox);

            // When
            boolean result = sut.processOutbox(outbox);

            // Then
            assertThat(result).isTrue();
            InOrder inOrder = inOrder(commandManager, messageManager);
            inOrder.verify(commandManager).markAsProcessing(outbox);
            inOrder.verify(messageManager).publish(outbox);
            inOrder.verify(commandManager).markAsSent(outbox);
        }

        @Test
        @DisplayName("[실패] SQS 발행 예외 발생 시 false 반환 및 FAILED 처리")
        void shouldReturnFalseAndMarkAsFailedWhenPublishThrowsException() {
            // Given
            CrawledProductSyncOutbox outbox =
                    CrawledProductSyncOutboxFixture.aReconstitutedPending();
            willDoNothing().given(commandManager).markAsProcessing(outbox);
            willThrow(new RuntimeException("SQS 연결 실패")).given(messageManager).publish(outbox);

            // When
            boolean result = sut.processOutbox(outbox);

            // Then
            assertThat(result).isFalse();
            then(commandManager).should().markAsProcessing(outbox);
            then(messageManager).should().publish(outbox);
            then(commandManager)
                    .should()
                    .markAsFailed(
                            org.mockito.ArgumentMatchers.eq(outbox),
                            org.mockito.ArgumentMatchers.contains("SQS 발행 실패"));
            then(commandManager).should(org.mockito.Mockito.never()).markAsSent(outbox);
        }

        @Test
        @DisplayName("[실패] markAsProcessing 예외 발생 시 false 반환 및 FAILED 처리")
        void shouldReturnFalseAndMarkAsFailedWhenMarkAsProcessingThrowsException() {
            // Given
            CrawledProductSyncOutbox outbox =
                    CrawledProductSyncOutboxFixture.aReconstitutedPending();
            willThrow(new RuntimeException("DB 오류")).given(commandManager).markAsProcessing(outbox);

            // When
            boolean result = sut.processOutbox(outbox);

            // Then
            assertThat(result).isFalse();
            then(messageManager).shouldHaveNoInteractions();
            then(commandManager)
                    .should()
                    .markAsFailed(
                            org.mockito.ArgumentMatchers.eq(outbox),
                            org.mockito.ArgumentMatchers.contains("SQS 발행 실패"));
        }
    }
}
