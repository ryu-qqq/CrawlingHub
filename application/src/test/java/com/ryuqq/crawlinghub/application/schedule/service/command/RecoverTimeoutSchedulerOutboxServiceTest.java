package com.ryuqq.crawlinghub.application.schedule.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.ryuqq.crawlinghub.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.crawlinghub.application.schedule.dto.command.RecoverTimeoutSchedulerOutboxCommand;
import com.ryuqq.crawlinghub.application.schedule.manager.CrawlSchedulerOutBoxCommandManager;
import com.ryuqq.crawlinghub.application.schedule.manager.CrawlSchedulerOutBoxReadManager;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerOutBox;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerHistoryId;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerOutBoxId;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerOubBoxStatus;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * RecoverTimeoutSchedulerOutboxService 단위 테스트
 *
 * <p>Mockist 스타일 테스트: ReadManager, CommandManager Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RecoverTimeoutSchedulerOutboxService 테스트")
class RecoverTimeoutSchedulerOutboxServiceTest {

    @Mock private CrawlSchedulerOutBoxReadManager outBoxReadManager;

    @Mock private CrawlSchedulerOutBoxCommandManager outBoxCommandManager;

    @InjectMocks private RecoverTimeoutSchedulerOutboxService service;

    private CrawlSchedulerOutBox createProcessingOutBox(long id) {
        return CrawlSchedulerOutBox.reconstitute(
                CrawlSchedulerOutBoxId.of(id),
                CrawlSchedulerHistoryId.of(id),
                CrawlSchedulerOubBoxStatus.PROCESSING,
                id,
                1L,
                "test-scheduler-" + id,
                "cron(0 0 * * ? *)",
                SchedulerStatus.ACTIVE,
                null,
                0L,
                Instant.now().minusSeconds(600),
                Instant.now().minusSeconds(400));
    }

    @Nested
    @DisplayName("execute() 타임아웃 아웃박스 복구 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] 좀비 아웃박스가 없으면 빈 결과 반환")
        void shouldReturnEmptyResultWhenNoStaleOutboxes() {
            // Given
            RecoverTimeoutSchedulerOutboxCommand command =
                    RecoverTimeoutSchedulerOutboxCommand.of(10, 300);

            given(outBoxReadManager.findStaleProcessing(10, 300L)).willReturn(List.of());

            // When
            SchedulerBatchProcessingResult result = service.execute(command);

            // Then
            assertThat(result.total()).isZero();
            assertThat(result.success()).isZero();
            assertThat(result.failed()).isZero();
            then(outBoxCommandManager).should(never()).persist(any(CrawlSchedulerOutBox.class));
        }

        @Test
        @DisplayName("[성공] 모든 좀비 아웃박스 복구 성공")
        void shouldRecoverAllStaleOutboxes() {
            // Given
            RecoverTimeoutSchedulerOutboxCommand command =
                    RecoverTimeoutSchedulerOutboxCommand.of(10, 300);

            CrawlSchedulerOutBox outBox1 = createProcessingOutBox(1L);
            CrawlSchedulerOutBox outBox2 = createProcessingOutBox(2L);
            CrawlSchedulerOutBox outBox3 = createProcessingOutBox(3L);
            List<CrawlSchedulerOutBox> staleOutBoxes = List.of(outBox1, outBox2, outBox3);

            given(outBoxReadManager.findStaleProcessing(10, 300L)).willReturn(staleOutBoxes);

            // When
            SchedulerBatchProcessingResult result = service.execute(command);

            // Then
            assertThat(result.total()).isEqualTo(3);
            assertThat(result.success()).isEqualTo(3);
            assertThat(result.failed()).isZero();
            assertThat(outBox1.isPending()).isTrue();
            assertThat(outBox2.isPending()).isTrue();
            assertThat(outBox3.isPending()).isTrue();
            then(outBoxCommandManager).should(times(3)).persist(any(CrawlSchedulerOutBox.class));
        }

        @Test
        @DisplayName("[부분 실패] persist 실패 시 실패 카운트 반영")
        void shouldCountFailedRecoveries() {
            // Given
            RecoverTimeoutSchedulerOutboxCommand command =
                    RecoverTimeoutSchedulerOutboxCommand.of(10, 300);

            CrawlSchedulerOutBox firstOutBox = createProcessingOutBox(1L);
            CrawlSchedulerOutBox secondOutBox = createProcessingOutBox(2L);
            List<CrawlSchedulerOutBox> staleOutBoxes = List.of(firstOutBox, secondOutBox);

            given(outBoxReadManager.findStaleProcessing(10, 300L)).willReturn(staleOutBoxes);
            given(outBoxCommandManager.persist(any(CrawlSchedulerOutBox.class)))
                    .willThrow(new RuntimeException("persist failed"))
                    .willReturn(CrawlSchedulerOutBoxId.of(2L));

            // When
            SchedulerBatchProcessingResult result = service.execute(command);

            // Then
            assertThat(result.total()).isEqualTo(2);
            assertThat(result.success()).isEqualTo(1);
            assertThat(result.failed()).isEqualTo(1);
        }
    }
}
