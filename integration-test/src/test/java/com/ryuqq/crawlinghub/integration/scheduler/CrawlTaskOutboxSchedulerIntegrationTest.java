package com.ryuqq.crawlinghub.integration.scheduler;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.crawlinghub.application.task.dto.command.ProcessPendingCrawlTaskOutboxCommand;
import com.ryuqq.crawlinghub.application.task.dto.command.RecoverFailedCrawlTaskOutboxCommand;
import com.ryuqq.crawlinghub.application.task.dto.command.RecoverTimeoutCrawlTaskOutboxCommand;
import com.ryuqq.crawlinghub.application.task.port.in.command.ProcessPendingCrawlTaskOutboxUseCase;
import com.ryuqq.crawlinghub.application.task.port.in.command.RecoverFailedCrawlTaskOutboxUseCase;
import com.ryuqq.crawlinghub.application.task.port.in.command.RecoverTimeoutCrawlTaskOutboxUseCase;
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
 * CrawlTaskOutbox 스케줄러 통합 테스트
 *
 * <p>UseCase를 직접 호출하여 Outbox 처리 플로우를 검증합니다:
 *
 * <ul>
 *   <li>PENDING → PROCESSING → SENT (SQS 발행)
 *   <li>PROCESSING 타임아웃 → PENDING 복구
 *   <li>FAILED → PENDING 복구
 * </ul>
 */
@DisplayName("CrawlTaskOutbox 스케줄러 통합 테스트")
class CrawlTaskOutboxSchedulerIntegrationTest extends SchedulerIntegrationTest {

    @Autowired private TestDataHelper testDataHelper;
    @Autowired private JdbcTemplate jdbcTemplate;

    @Autowired private ProcessPendingCrawlTaskOutboxUseCase processPendingUseCase;
    @Autowired private RecoverTimeoutCrawlTaskOutboxUseCase recoverTimeoutUseCase;
    @Autowired private RecoverFailedCrawlTaskOutboxUseCase recoverFailedUseCase;

    @Nested
    @DisplayName("PENDING Outbox 처리")
    class ProcessPendingOutbox {

        @Test
        @DisplayName("PENDING 상태의 Outbox가 SQS로 발행되어 SENT 상태로 변경되어야 한다")
        void shouldProcessPendingOutboxToSent() {
            // given
            testDataHelper.insertCrawlTaskOutboxTestData();

            // when
            SchedulerBatchProcessingResult result =
                    processPendingUseCase.execute(ProcessPendingCrawlTaskOutboxCommand.of(10, 0));

            // then
            assertThat(result.total()).isGreaterThanOrEqualTo(1);

            List<Map<String, Object>> outboxRows =
                    jdbcTemplate.queryForList(
                            "SELECT status FROM crawl_task_outbox WHERE crawl_task_id = 1");
            assertThat(outboxRows).isNotEmpty();
            assertThat(outboxRows.get(0).get("status").toString()).isEqualTo("SENT");
        }

        @Test
        @DisplayName("delaySeconds 이내의 Outbox는 처리하지 않아야 한다")
        void shouldNotProcessRecentOutbox() {
            // given
            testDataHelper.insertTaskTestData();
            jdbcTemplate.execute(
                    """
INSERT INTO crawl_task_outbox (crawl_task_id, idempotency_key, payload, status, retry_count, created_at, processed_at)
VALUES (1, 'recent-outbox-uuid', '{"taskId":1}', 'PENDING', 0, UTC_TIMESTAMP(), NULL)
""");

            // when - delaySeconds=600 (10분) → 방금 생성된 레코드는 처리 대상 아님
            SchedulerBatchProcessingResult result =
                    processPendingUseCase.execute(ProcessPendingCrawlTaskOutboxCommand.of(10, 600));

            // then
            assertThat(result.total()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("타임아웃 Outbox 복구")
    class RecoverTimeoutOutbox {

        @Test
        @DisplayName("PROCESSING 상태에서 타임아웃된 Outbox가 PENDING으로 복구되어야 한다")
        void shouldRecoverTimedOutProcessingOutbox() {
            // given
            testDataHelper.insertCrawlTaskOutboxTestData();

            // when - timeoutSeconds=60 → 30분 전 PROCESSING 레코드는 타임아웃 대상
            SchedulerBatchProcessingResult result =
                    recoverTimeoutUseCase.execute(RecoverTimeoutCrawlTaskOutboxCommand.of(10, 60));

            // then
            assertThat(result.total()).isGreaterThanOrEqualTo(1);

            List<Map<String, Object>> outboxRows =
                    jdbcTemplate.queryForList(
                            "SELECT status FROM crawl_task_outbox WHERE crawl_task_id = 2");
            assertThat(outboxRows).isNotEmpty();
            assertThat(outboxRows.get(0).get("status").toString()).isEqualTo("PENDING");
        }

        @Test
        @DisplayName("최근 PROCESSING 상태의 Outbox는 복구하지 않아야 한다")
        void shouldNotRecoverRecentProcessingOutbox() {
            // given
            testDataHelper.insertTaskTestData();
            jdbcTemplate.execute(
                    """
INSERT INTO crawl_task_outbox (crawl_task_id, idempotency_key, payload, status, retry_count, created_at, processed_at)
VALUES (1, 'recent-processing-uuid', '{"taskId":1}', 'PROCESSING', 0, UTC_TIMESTAMP(), UTC_TIMESTAMP())
""");

            // when - timeoutSeconds=3600 (1시간) → 방금 생성된 레코드는 타임아웃 아님
            SchedulerBatchProcessingResult result =
                    recoverTimeoutUseCase.execute(
                            RecoverTimeoutCrawlTaskOutboxCommand.of(10, 3600));

            // then
            assertThat(result.total()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("실패 Outbox 복구")
    class RecoverFailedOutbox {

        @Test
        @DisplayName("FAILED 상태의 Outbox가 PENDING으로 복구되어야 한다")
        void shouldRecoverFailedOutbox() {
            // given
            testDataHelper.insertCrawlTaskOutboxTestData();

            // when - delaySeconds=60 → 30분 전 FAILED 레코드는 복구 대상
            SchedulerBatchProcessingResult result =
                    recoverFailedUseCase.execute(RecoverFailedCrawlTaskOutboxCommand.of(10, 60));

            // then
            assertThat(result.total()).isGreaterThanOrEqualTo(1);

            List<Map<String, Object>> outboxRows =
                    jdbcTemplate.queryForList(
                            "SELECT status FROM crawl_task_outbox WHERE crawl_task_id = 4");
            assertThat(outboxRows).isNotEmpty();
            assertThat(outboxRows.get(0).get("status").toString()).isEqualTo("PENDING");
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
                    processPendingUseCase.execute(ProcessPendingCrawlTaskOutboxCommand.of(10, 0));

            // then
            assertThat(result.total()).isEqualTo(0);
        }
    }
}
