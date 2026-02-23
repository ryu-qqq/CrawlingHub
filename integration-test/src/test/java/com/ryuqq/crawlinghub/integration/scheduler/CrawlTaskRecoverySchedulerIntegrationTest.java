package com.ryuqq.crawlinghub.integration.scheduler;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.crawlinghub.application.task.dto.command.RecoverStuckCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.task.port.in.command.RecoverStuckCrawlTaskUseCase;
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
 * CrawlTask 복구 스케줄러 통합 테스트
 *
 * <p>RUNNING 상태에서 장시간 멈춘 태스크를 복구하는 플로우를 검증합니다:
 *
 * <ul>
 *   <li>RUNNING 상태 + 타임아웃 → RETRY (재시도 가능) 또는 FAILED (재시도 불가) 상태로 변경
 * </ul>
 */
@DisplayName("CrawlTask 복구 스케줄러 통합 테스트")
class CrawlTaskRecoverySchedulerIntegrationTest extends SchedulerIntegrationTest {

    @Autowired private TestDataHelper testDataHelper;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private RecoverStuckCrawlTaskUseCase recoverStuckUseCase;

    @Nested
    @DisplayName("RUNNING 상태 태스크 복구")
    class RecoverStuckTask {

        @Test
        @DisplayName("타임아웃된 RUNNING 태스크가 복구되어야 한다")
        void shouldRecoverStuckRunningTask() {
            // given
            testDataHelper.insertTaskTestData();
            // RUNNING 상태로 30분 전에 시작된 태스크 추가
            jdbcTemplate.execute(
                    """
UPDATE crawl_task SET status = 'RUNNING', updated_at = DATE_SUB(NOW(), INTERVAL 30 MINUTE)
WHERE id = 1
""");

            // when - timeoutSeconds=60 → 30분 전 RUNNING 태스크는 복구 대상
            SchedulerBatchProcessingResult result =
                    recoverStuckUseCase.execute(RecoverStuckCrawlTaskCommand.of(10, 60));

            // then
            assertThat(result.total()).isGreaterThanOrEqualTo(1);

            List<Map<String, Object>> taskRows =
                    jdbcTemplate.queryForList("SELECT status FROM crawl_task WHERE id = 1");
            assertThat(taskRows).isNotEmpty();
            String status = taskRows.get(0).get("status").toString();
            // retryCount=0이면 canRetry()=true → RETRY, 아니면 FAILED
            assertThat(status).isIn("RETRY", "FAILED");
        }

        @Test
        @DisplayName("최근 RUNNING 태스크는 복구하지 않아야 한다")
        void shouldNotRecoverRecentRunningTask() {
            // given
            testDataHelper.insertTaskTestData();
            // RUNNING 상태로 방금 시작된 태스크 (updated_at을 명시적으로 NOW()로 설정)
            jdbcTemplate.execute(
                    "UPDATE crawl_task SET status = 'RUNNING', updated_at = NOW() WHERE id = 1");

            // when - timeoutSeconds=3600 (1시간) → 방금 시작된 태스크는 복구 대상 아님
            SchedulerBatchProcessingResult result =
                    recoverStuckUseCase.execute(RecoverStuckCrawlTaskCommand.of(10, 3600));

            // then
            assertThat(result.total()).isEqualTo(0);
        }

        @Test
        @DisplayName("RUNNING 상태가 아닌 태스크는 복구 대상이 아니어야 한다")
        void shouldNotRecoverNonRunningTask() {
            // given
            testDataHelper.insertTaskTestData();
            // 기본 데이터에 RUNNING 상태 태스크가 없음

            // when
            SchedulerBatchProcessingResult result =
                    recoverStuckUseCase.execute(RecoverStuckCrawlTaskCommand.of(10, 60));

            // then
            assertThat(result.total()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("빈 데이터 시나리오")
    class EmptyDataScenario {

        @Test
        @DisplayName("처리할 태스크가 없으면 빈 결과를 반환해야 한다")
        void shouldReturnEmptyResultWhenNoStuckTask() {
            // given - 데이터 없음

            // when
            SchedulerBatchProcessingResult result =
                    recoverStuckUseCase.execute(RecoverStuckCrawlTaskCommand.of(10, 60));

            // then
            assertThat(result.total()).isEqualTo(0);
        }
    }
}
