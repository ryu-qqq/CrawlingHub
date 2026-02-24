package com.ryuqq.crawlinghub.integration.worker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.crawlinghub.application.task.dto.command.TriggerCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.task.port.in.command.TriggerCrawlTaskUseCase;
import com.ryuqq.crawlinghub.domain.common.exception.DomainException;
import com.ryuqq.crawlinghub.integration.base.WorkerIntegrationTest;
import com.ryuqq.crawlinghub.integration.helper.TestDataHelper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * EventBridgeTrigger 통합 테스트
 *
 * <p>EventBridge에서 발행한 트리거로 CrawlTask를 생성하는 플로우를 검증합니다:
 *
 * <ul>
 *   <li>TriggerCrawlTaskUseCase 실행 → CrawlTask + CrawlTaskOutbox 생성
 *   <li>INACTIVE 스케줄러 → DomainException 발생
 *   <li>중복 Task 방지 검증
 * </ul>
 *
 * <p>UseCase 직접 호출 방식으로 핵심 비즈니스 로직을 검증합니다. SQS 메시지 역직렬화는 단위 테스트에서 검증합니다.
 */
@DisplayName("EventBridgeTrigger Worker 통합 테스트")
class EventBridgeTriggerWorkerIntegrationTest extends WorkerIntegrationTest {

    @Autowired private TestDataHelper testDataHelper;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private TriggerCrawlTaskUseCase triggerCrawlTaskUseCase;

    @Nested
    @DisplayName("EventBridge 트리거 실행")
    class EventBridgeTriggerExecution {

        @Test
        @DisplayName("ACTIVE 스케줄러 트리거 시 CrawlTask가 생성되어야 한다")
        void shouldCreateCrawlTaskWhenTriggered() {
            // given
            testDataHelper.insertSellers();
            testDataHelper.insertSchedulers();
            testDataHelper.insertUserAgents();
            testDataHelper.warmUpUserAgentPool();

            Long schedulerId = 1L;

            // when
            triggerCrawlTaskUseCase.execute(new TriggerCrawlTaskCommand(schedulerId));

            // then
            List<Map<String, Object>> tasks =
                    jdbcTemplate.queryForList(
                            "SELECT * FROM crawl_task WHERE crawl_scheduler_id = ?", schedulerId);
            assertThat(tasks).isNotEmpty();
            assertThat(tasks.get(0).get("status").toString()).isEqualTo("WAITING");
            assertThat(tasks.get(0).get("task_type").toString()).isEqualTo("SEARCH");

            // Outbox도 함께 생성되어야 한다
            Long taskId = ((Number) tasks.get(0).get("id")).longValue();
            List<Map<String, Object>> outboxRows =
                    jdbcTemplate.queryForList(
                            "SELECT * FROM crawl_task_outbox WHERE crawl_task_id = ?", taskId);
            assertThat(outboxRows).isNotEmpty();
            assertThat(outboxRows.get(0).get("status").toString()).isEqualTo("PENDING");
        }

        @Test
        @DisplayName("INACTIVE 스케줄러 트리거 시 DomainException이 발생해야 한다")
        void shouldThrowExceptionForInactiveScheduler() {
            // given
            testDataHelper.insertSellers();
            testDataHelper.insertSchedulers();

            Long inactiveSchedulerId = 3L;

            // when & then
            assertThatThrownBy(
                            () ->
                                    triggerCrawlTaskUseCase.execute(
                                            new TriggerCrawlTaskCommand(inactiveSchedulerId)))
                    .isInstanceOf(DomainException.class);

            // 태스크가 생성되지 않았는지 확인
            List<Map<String, Object>> tasks =
                    jdbcTemplate.queryForList(
                            "SELECT * FROM crawl_task WHERE crawl_scheduler_id = ?",
                            inactiveSchedulerId);
            assertThat(tasks).isEmpty();
        }

        @Test
        @DisplayName("동일 스케줄러에 진행 중인 Task가 있으면 중복 생성을 방지해야 한다")
        void shouldPreventDuplicateTask() {
            // given
            testDataHelper.insertSellers();
            testDataHelper.insertSchedulers();
            testDataHelper.insertUserAgents();
            testDataHelper.warmUpUserAgentPool();

            Long schedulerId = 1L;

            // 첫 번째 트리거 - 성공
            triggerCrawlTaskUseCase.execute(new TriggerCrawlTaskCommand(schedulerId));

            // when & then - 두 번째 트리거 - 중복 방지
            assertThatThrownBy(
                            () ->
                                    triggerCrawlTaskUseCase.execute(
                                            new TriggerCrawlTaskCommand(schedulerId)))
                    .isInstanceOf(DomainException.class);

            // 태스크가 1개만 생성되었는지 확인
            List<Map<String, Object>> tasks =
                    jdbcTemplate.queryForList(
                            "SELECT * FROM crawl_task WHERE crawl_scheduler_id = ?", schedulerId);
            assertThat(tasks).hasSize(1);
        }
    }
}
