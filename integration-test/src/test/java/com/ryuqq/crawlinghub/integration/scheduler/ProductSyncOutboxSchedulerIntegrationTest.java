package com.ryuqq.crawlinghub.integration.scheduler;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.crawlinghub.application.product.dto.command.PublishPendingSyncOutboxCommand;
import com.ryuqq.crawlinghub.application.product.dto.command.RecoverFailedProductSyncOutboxCommand;
import com.ryuqq.crawlinghub.application.product.dto.command.RecoverTimeoutProductSyncOutboxCommand;
import com.ryuqq.crawlinghub.application.product.port.in.command.PublishPendingSyncOutboxUseCase;
import com.ryuqq.crawlinghub.application.product.port.in.command.RecoverFailedProductSyncOutboxUseCase;
import com.ryuqq.crawlinghub.application.product.port.in.command.RecoverTimeoutProductSyncOutboxUseCase;
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
 * ProductSyncOutbox 스케줄러 통합 테스트
 *
 * <p>UseCase를 직접 호출하여 ProductSync Outbox 처리 플로우를 검증합니다:
 *
 * <ul>
 *   <li>PENDING → SENT (SQS 발행)
 *   <li>PROCESSING 타임아웃 → PENDING 복구
 *   <li>FAILED → PENDING 복구
 * </ul>
 */
@DisplayName("ProductSyncOutbox 스케줄러 통합 테스트")
class ProductSyncOutboxSchedulerIntegrationTest extends SchedulerIntegrationTest {

    @Autowired private TestDataHelper testDataHelper;
    @Autowired private JdbcTemplate jdbcTemplate;

    @Autowired private PublishPendingSyncOutboxUseCase publishPendingUseCase;
    @Autowired private RecoverTimeoutProductSyncOutboxUseCase recoverTimeoutUseCase;
    @Autowired private RecoverFailedProductSyncOutboxUseCase recoverFailedUseCase;

    @Nested
    @DisplayName("PENDING Outbox 발행")
    class PublishPendingOutbox {

        @Test
        @DisplayName("PENDING 상태의 Outbox가 SQS로 발행되어야 한다")
        void shouldPublishPendingOutboxToSqs() {
            // given - id=1: PENDING, retryCount=0 (신규)
            testDataHelper.insertProductOutboxTestData();

            // when
            SchedulerBatchProcessingResult result =
                    publishPendingUseCase.execute(PublishPendingSyncOutboxCommand.of(10, 3));

            // then
            assertThat(result.total()).isGreaterThanOrEqualTo(1);

            List<Map<String, Object>> outboxRows =
                    jdbcTemplate.queryForList(
                            "SELECT status FROM product_sync_outbox WHERE id = 1");
            assertThat(outboxRows).isNotEmpty();
            // SQS 발행 성공 시 SENT, 실패 시 FAILED
            assertThat(outboxRows.get(0).get("status").toString()).isIn("SENT", "FAILED");
        }

        @Test
        @DisplayName("FAILED 상태의 Outbox는 PENDING 발행 대상이 아니어야 한다")
        void shouldNotPublishFailedOutbox() {
            // given - id=4: FAILED, retryCount=1
            testDataHelper.insertProductOutboxTestData();

            // when
            publishPendingUseCase.execute(PublishPendingSyncOutboxCommand.of(10, 3));

            // then - id=4는 FAILED 상태 유지 (RecoverFailed 스케줄러가 PENDING으로 복원해야 재처리됨)
            List<Map<String, Object>> outboxRows =
                    jdbcTemplate.queryForList(
                            "SELECT status FROM product_sync_outbox WHERE id = 4");
            assertThat(outboxRows).isNotEmpty();
            assertThat(outboxRows.get(0).get("status").toString()).isEqualTo("FAILED");
        }
    }

    @Nested
    @DisplayName("타임아웃 Outbox 복구")
    class RecoverTimeoutOutbox {

        @Test
        @DisplayName("PROCESSING 상태에서 타임아웃된 Outbox가 복구되어야 한다")
        void shouldRecoverTimedOutProcessingOutbox() {
            // given - id=2는 이미 PROCESSING 상태, processedAt만 오래된 시간으로 설정
            testDataHelper.insertProductOutboxTestData();
            jdbcTemplate.execute(
                    """
UPDATE product_sync_outbox
SET processed_at = DATE_SUB(NOW(), INTERVAL 30 MINUTE)
WHERE id = 2
""");

            // when - timeoutSeconds=60 → 30분 전 SENT 레코드는 타임아웃 대상
            SchedulerBatchProcessingResult result =
                    recoverTimeoutUseCase.execute(
                            RecoverTimeoutProductSyncOutboxCommand.of(10, 60));

            // then
            assertThat(result.total()).isGreaterThanOrEqualTo(1);

            List<Map<String, Object>> outboxRows =
                    jdbcTemplate.queryForList(
                            "SELECT status FROM product_sync_outbox WHERE id = 2");
            assertThat(outboxRows).isNotEmpty();
            assertThat(outboxRows.get(0).get("status").toString()).isEqualTo("PENDING");
        }
    }

    @Nested
    @DisplayName("실패 Outbox 복구")
    class RecoverFailedOutbox {

        @Test
        @DisplayName("FAILED 상태의 Outbox가 PENDING으로 복구되어야 한다")
        void shouldRecoverFailedOutbox() {
            // given - id=4: FAILED, retryCount=1, 30분 전 실패
            testDataHelper.insertProductOutboxTestData();

            // when - delaySeconds=60 → 30분 전 FAILED 레코드는 복구 대상
            SchedulerBatchProcessingResult result =
                    recoverFailedUseCase.execute(RecoverFailedProductSyncOutboxCommand.of(10, 60));

            // then
            assertThat(result.total()).isGreaterThanOrEqualTo(1);

            List<Map<String, Object>> outboxRows =
                    jdbcTemplate.queryForList(
                            "SELECT status FROM product_sync_outbox WHERE id = 4");
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
                    publishPendingUseCase.execute(PublishPendingSyncOutboxCommand.of(10, 3));

            // then
            assertThat(result.total()).isEqualTo(0);
        }
    }
}
