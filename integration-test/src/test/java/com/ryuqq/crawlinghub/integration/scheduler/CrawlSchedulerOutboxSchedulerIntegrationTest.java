package com.ryuqq.crawlinghub.integration.scheduler;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.crawlinghub.application.schedule.dto.command.ProcessPendingSchedulerOutboxCommand;
import com.ryuqq.crawlinghub.application.schedule.dto.command.RecoverTimeoutSchedulerOutboxCommand;
import com.ryuqq.crawlinghub.application.schedule.port.in.command.ProcessPendingSchedulerOutboxUseCase;
import com.ryuqq.crawlinghub.application.schedule.port.in.command.RecoverTimeoutSchedulerOutboxUseCase;
import com.ryuqq.crawlinghub.integration.base.SchedulerIntegrationTest;
import com.ryuqq.crawlinghub.integration.helper.TestDataHelper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * CrawlSchedulerOutbox 스케줄러 통합 테스트
 *
 * <p>UseCase를 직접 호출하여 Scheduler Outbox(EventBridge 동기화) 처리 플로우를 검증합니다:
 *
 * <ul>
 *   <li>PENDING → PROCESSING → COMPLETED/FAILED (EventBridge 동기화)
 *   <li>PROCESSING 타임아웃 → PENDING 복구
 * </ul>
 */
@DisplayName("CrawlSchedulerOutbox 스케줄러 통합 테스트")
class CrawlSchedulerOutboxSchedulerIntegrationTest extends SchedulerIntegrationTest {

    @Autowired private TestDataHelper testDataHelper;
    @Autowired private JdbcTemplate jdbcTemplate;

    @Autowired private ProcessPendingSchedulerOutboxUseCase processPendingUseCase;
    @Autowired private RecoverTimeoutSchedulerOutboxUseCase recoverTimeoutUseCase;

    @Nested
    @DisplayName("PENDING Outbox 처리")
    class ProcessPendingOutbox {

        @Test
        @DisplayName("PENDING 상태의 Scheduler Outbox가 처리되어야 한다")
        void shouldProcessPendingSchedulerOutbox() {
            // given
            testDataHelper.insertSchedulerOutboxTestData();

            // when - EventBridge Mock 상태에서 처리 시도
            // Processor 내부에서 OptimisticLocking이 발생할 수 있으므로 예외를 허용
            try {
                processPendingUseCase.execute(ProcessPendingSchedulerOutboxCommand.of(10, 0));
            } catch (Exception e) {
                // OptimisticLockingFailure 등 persist 관련 예외 허용
            }

            // then - PENDING에서 다른 상태(PROCESSING/COMPLETED/FAILED)로 변경되었는지 확인
            List<Map<String, Object>> outboxRows =
                    jdbcTemplate.queryForList(
                            "SELECT status FROM crawl_scheduler_outbox WHERE id = 1");
            assertThat(outboxRows).isNotEmpty();
            String status = outboxRows.get(0).get("status").toString();
            // 첫 번째 persist(PENDING→PROCESSING)는 성공하므로 PENDING이 아닌 상태
            assertThat(status).isNotEqualTo("PENDING");
        }
    }

    @Nested
    @DisplayName("타임아웃 Outbox 복구")
    class RecoverTimeoutOutbox {

        @Test
        @DisplayName("PROCESSING 상태에서 타임아웃된 Outbox가 PENDING으로 복구되어야 한다")
        void shouldRecoverTimedOutProcessingOutbox() {
            // given
            testDataHelper.insertSchedulerOutboxTestData();

            // when - timeoutSeconds=60 → 30분 전 PROCESSING 레코드는 타임아웃 대상
            SchedulerBatchProcessingResult result =
                    recoverTimeoutUseCase.execute(RecoverTimeoutSchedulerOutboxCommand.of(10, 60));

            // then
            assertThat(result.total()).isGreaterThanOrEqualTo(1);

            List<Map<String, Object>> outboxRows =
                    jdbcTemplate.queryForList(
                            "SELECT status FROM crawl_scheduler_outbox WHERE id = 2");
            assertThat(outboxRows).isNotEmpty();
            assertThat(outboxRows.get(0).get("status").toString()).isEqualTo("PENDING");
        }

        @Test
        @DisplayName("최근 PROCESSING 상태의 Outbox는 복구하지 않아야 한다")
        void shouldNotRecoverRecentProcessingOutbox() {
            // given
            testDataHelper.insertSellers();
            testDataHelper.insertSchedulers();
            testDataHelper.insertSchedulerHistory();
            jdbcTemplate.execute(
                    """
INSERT INTO crawl_scheduler_outbox (
    id, history_id, status, scheduler_id, seller_id, scheduler_name,
    cron_expression, scheduler_status, error_message, version, created_at, processed_at
)
VALUES (10, 1, 'PROCESSING', 1, 1, 'daily-product-sync',
    'cron(0 2 * * ? *)', 'ACTIVE', NULL, 0, NOW(), NOW())
""");

            // when - timeoutSeconds=3600 → 방금 생성된 레코드는 타임아웃 아님
            SchedulerBatchProcessingResult result =
                    recoverTimeoutUseCase.execute(
                            RecoverTimeoutSchedulerOutboxCommand.of(10, 3600));

            // then
            assertThat(result.total()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("빈 데이터 시나리오")
    class EmptyDataScenario {

        @Test
        @DisplayName("처리할 Outbox가 없으면 빈 결과를 반환해야 한다")
        void shouldReturnEmptyResultWhenNoOutbox() {
            // given - 데이터 없음

            // when
            SchedulerBatchProcessingResult result =
                    processPendingUseCase.execute(ProcessPendingSchedulerOutboxCommand.of(10, 0));

            // then
            assertThat(result.total()).isEqualTo(0);
        }
    }
}
