package com.ryuqq.crawlinghub.application.product.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.times;

import com.ryuqq.cralwinghub.domain.fixture.product.CrawledProductSyncOutboxFixture;
import com.ryuqq.crawlinghub.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.crawlinghub.application.product.dto.command.RecoverFailedProductSyncOutboxCommand;
import com.ryuqq.crawlinghub.application.product.manager.CrawledProductSyncOutboxCommandManager;
import com.ryuqq.crawlinghub.application.product.manager.CrawledProductSyncOutboxReadManager;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * RecoverFailedProductSyncOutboxService 단위 테스트
 *
 * <p>Mockist 스타일 테스트: ReadManager, CommandManager 의존성 Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("application")
@ExtendWith(MockitoExtension.class)
@DisplayName("RecoverFailedProductSyncOutboxService 단위 테스트")
class RecoverFailedProductSyncOutboxServiceTest {

    @Mock private CrawledProductSyncOutboxReadManager outboxReadManager;
    @Mock private CrawledProductSyncOutboxCommandManager outboxCommandManager;

    @InjectMocks private RecoverFailedProductSyncOutboxService sut;

    @Nested
    @DisplayName("execute() 메서드 테스트")
    class ExecuteTest {

        @Test
        @DisplayName("[성공] 복구 대상 FAILED Outbox가 없으면 empty 결과 반환")
        void shouldReturnEmptyResultWhenNoFailedOutboxes() {
            // Given
            RecoverFailedProductSyncOutboxCommand command =
                    RecoverFailedProductSyncOutboxCommand.of(100, 300);
            given(outboxReadManager.findFailedOlderThan(100, 300)).willReturn(List.of());

            // When
            SchedulerBatchProcessingResult result = sut.execute(command);

            // Then
            assertThat(result.total()).isZero();
            assertThat(result.success()).isZero();
            assertThat(result.failed()).isZero();
            then(outboxCommandManager).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("[성공] FAILED Outbox를 PENDING으로 복구 성공 시 success 카운트 반환")
        void shouldReturnSuccessCountWhenRecoverySucceeds() {
            // Given
            RecoverFailedProductSyncOutboxCommand command =
                    RecoverFailedProductSyncOutboxCommand.of(100, 300);
            CrawledProductSyncOutbox failedOutbox =
                    CrawledProductSyncOutboxFixture.aReconstitutedFailed();
            given(outboxReadManager.findFailedOlderThan(100, 300))
                    .willReturn(List.of(failedOutbox));
            willDoNothing().given(outboxCommandManager).resetToPending(failedOutbox);

            // When
            SchedulerBatchProcessingResult result = sut.execute(command);

            // Then
            assertThat(result.total()).isEqualTo(1);
            assertThat(result.success()).isEqualTo(1);
            assertThat(result.failed()).isZero();
            then(outboxCommandManager).should().resetToPending(failedOutbox);
        }

        @Test
        @DisplayName("[실패] resetToPending 예외 발생 시 failed 카운트 반영")
        void shouldCountFailedWhenResetThrowsException() {
            // Given
            RecoverFailedProductSyncOutboxCommand command =
                    RecoverFailedProductSyncOutboxCommand.of(50, 60);
            CrawledProductSyncOutbox failedOutbox =
                    CrawledProductSyncOutboxFixture.aReconstitutedFailed();
            given(outboxReadManager.findFailedOlderThan(50, 60)).willReturn(List.of(failedOutbox));
            willThrow(new RuntimeException("DB 오류"))
                    .given(outboxCommandManager)
                    .resetToPending(failedOutbox);

            // When
            SchedulerBatchProcessingResult result = sut.execute(command);

            // Then
            assertThat(result.total()).isEqualTo(1);
            assertThat(result.success()).isZero();
            assertThat(result.failed()).isEqualTo(1);
        }

        @Test
        @DisplayName("[성공] 복수 FAILED Outbox 중 일부 성공, 일부 실패 시 부분 성공 결과")
        void shouldReturnPartialResultWhenSomeRecoveriesFail() {
            // Given
            RecoverFailedProductSyncOutboxCommand command =
                    RecoverFailedProductSyncOutboxCommand.of(100, 300);
            CrawledProductSyncOutbox outbox1 =
                    CrawledProductSyncOutboxFixture.aReconstitutedFailed();
            CrawledProductSyncOutbox outbox2 =
                    CrawledProductSyncOutboxFixture.aReconstitutedFailed();
            given(outboxReadManager.findFailedOlderThan(100, 300))
                    .willReturn(List.of(outbox1, outbox2));
            willDoNothing().given(outboxCommandManager).resetToPending(outbox1);
            willThrow(new RuntimeException("복구 실패"))
                    .given(outboxCommandManager)
                    .resetToPending(outbox2);

            // When
            SchedulerBatchProcessingResult result = sut.execute(command);

            // Then
            assertThat(result.total()).isEqualTo(2);
            assertThat(result.success()).isEqualTo(1);
            assertThat(result.failed()).isEqualTo(1);
            then(outboxCommandManager)
                    .should(times(2))
                    .resetToPending(org.mockito.ArgumentMatchers.any());
        }
    }
}
