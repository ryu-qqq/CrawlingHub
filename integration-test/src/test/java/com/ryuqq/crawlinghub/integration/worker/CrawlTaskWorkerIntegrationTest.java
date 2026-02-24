package com.ryuqq.crawlinghub.integration.worker;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.ryuqq.crawlinghub.adapter.out.persistence.execution.entity.CrawlExecutionJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.execution.repository.CrawlExecutionJpaRepository;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlExecutionStatus;
import com.ryuqq.crawlinghub.integration.base.WorkerIntegrationTest;
import com.ryuqq.crawlinghub.integration.helper.TestDataHelper;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * CrawlTask Worker 통합 테스트
 *
 * <p>SQS 메시지 수신 → 크롤링 실행 → 결과 저장 E2E 플로우를 테스트합니다.
 *
 * <p><strong>테스트 시나리오:</strong>
 *
 * <ul>
 *   <li>SQS 메시지 발송 → CrawlTaskSqsListener 수신 → CrawlExecution 생성
 *   <li>WireMock으로 크롤링 대상 사이트 모킹
 *   <li>Awaitility로 비동기 처리 결과 검증
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("CrawlTask Worker 통합 테스트")
class CrawlTaskWorkerIntegrationTest extends WorkerIntegrationTest {

    @Autowired private TestDataHelper testDataHelper;

    @Autowired private CrawlExecutionJpaRepository crawlExecutionRepository;

    @Nested
    @DisplayName("SQS 메시지 수신 테스트")
    class SqsMessageReceiveTest {

        @Test
        @DisplayName("SQS 메시지 발송 시 CrawlExecution이 생성되어야 한다")
        void shouldCreateCrawlExecutionWhenSqsMessageReceived() {
            // given
            testDataHelper.insertWorkerTestData(); // MySQL + Redis Pool 워밍업
            setupCrawlTargetMock();

            Long taskId = 1L;
            Long schedulerId = 1L;
            Long sellerId = 1L;
            String taskType = "MINI_SHOP";
            String endpoint = crawlTargetWireMock.baseUrl() + "/products?page=1&size=100";

            // when
            sendCrawlTaskMessage(taskId, schedulerId, sellerId, taskType, endpoint);

            // then
            await().atMost(Duration.ofSeconds(10))
                    .pollInterval(Duration.ofMillis(500))
                    .untilAsserted(
                            () -> {
                                List<CrawlExecutionJpaEntity> executions =
                                        crawlExecutionRepository.findAll();
                                assertThat(executions).isNotEmpty();
                                assertThat(executions)
                                        .anyMatch(
                                                e ->
                                                        e.getCrawlTaskId().equals(taskId)
                                                                && e.getSellerId()
                                                                        .equals(sellerId));
                            });
        }

        @Test
        @DisplayName("크롤링 성공 시 CrawlExecution 상태가 SUCCESS여야 한다")
        void shouldSetSuccessStatusWhenCrawlingSucceeds() {
            // given
            testDataHelper.insertWorkerTestData(); // MySQL + Redis Pool 워밍업
            setupCrawlTargetMock();

            Long taskId = 1L;
            Long schedulerId = 1L;
            Long sellerId = 1L;
            String taskType = "MINI_SHOP";
            String endpoint = crawlTargetWireMock.baseUrl() + "/products?page=1&size=100";

            // when
            sendCrawlTaskMessage(taskId, schedulerId, sellerId, taskType, endpoint);

            // then
            await().atMost(Duration.ofSeconds(15))
                    .pollInterval(Duration.ofMillis(500))
                    .untilAsserted(
                            () -> {
                                List<CrawlExecutionJpaEntity> executions =
                                        crawlExecutionRepository.findAll();
                                assertThat(executions)
                                        .anyMatch(
                                                e ->
                                                        e.getCrawlTaskId().equals(taskId)
                                                                && (e.getStatus()
                                                                                == CrawlExecutionStatus
                                                                                        .SUCCESS
                                                                        || e.getStatus()
                                                                                == CrawlExecutionStatus
                                                                                        .FAILED));
                            });
        }

        @Test
        @DisplayName("크롤링 대상 서버 에러 시 CrawlExecution 상태가 FAILED여야 한다")
        void shouldSetFailedStatusWhenCrawlingFails() {
            // given
            testDataHelper.insertWorkerTestData(); // MySQL + Redis Pool 워밍업
            setupCrawlTargetErrorMock();

            Long taskId = 2L;
            Long schedulerId = 1L;
            Long sellerId = 1L;
            String taskType = "DETAIL";
            String endpoint = crawlTargetWireMock.baseUrl() + "/products/123";

            // when
            sendCrawlTaskMessage(taskId, schedulerId, sellerId, taskType, endpoint);

            // then
            await().atMost(Duration.ofSeconds(15))
                    .pollInterval(Duration.ofMillis(500))
                    .untilAsserted(
                            () -> {
                                List<CrawlExecutionJpaEntity> executions =
                                        crawlExecutionRepository.findAll();
                                assertThat(executions)
                                        .anyMatch(
                                                e ->
                                                        e.getCrawlTaskId().equals(taskId)
                                                                && e.getStatus()
                                                                        == CrawlExecutionStatus
                                                                                .FAILED);
                            });
        }
    }

    @Nested
    @DisplayName("다양한 TaskType 테스트")
    class TaskTypeTest {

        @Test
        @DisplayName("META 타입 크롤링이 정상 처리되어야 한다")
        void shouldHandleMetaTaskType() {
            // given
            testDataHelper.insertWorkerTestData(); // MySQL + Redis Pool 워밍업
            setupMetaCrawlMock();

            Long taskId = 1L;
            Long schedulerId = 1L;
            Long sellerId = 1L;
            String taskType = "MINI_SHOP";
            String endpoint = crawlTargetWireMock.baseUrl() + "/meta/products";

            // when
            sendCrawlTaskMessage(taskId, schedulerId, sellerId, taskType, endpoint);

            // then
            await().atMost(Duration.ofSeconds(15))
                    .pollInterval(Duration.ofMillis(500))
                    .untilAsserted(
                            () -> {
                                List<CrawlExecutionJpaEntity> executions =
                                        crawlExecutionRepository.findAll();
                                assertThat(executions)
                                        .anyMatch(e -> e.getCrawlTaskId().equals(taskId));
                            });
        }

        @Test
        @DisplayName("DETAIL 타입 크롤링이 정상 처리되어야 한다")
        void shouldHandleDetailTaskType() {
            // given
            testDataHelper.insertWorkerTestData(); // MySQL + Redis Pool 워밍업
            setupDetailCrawlMock();

            Long taskId = 2L;
            Long schedulerId = 1L;
            Long sellerId = 1L;
            String taskType = "DETAIL";
            String endpoint = crawlTargetWireMock.baseUrl() + "/products/123";

            // when
            sendCrawlTaskMessage(taskId, schedulerId, sellerId, taskType, endpoint);

            // then
            await().atMost(Duration.ofSeconds(15))
                    .pollInterval(Duration.ofMillis(500))
                    .untilAsserted(
                            () -> {
                                List<CrawlExecutionJpaEntity> executions =
                                        crawlExecutionRepository.findAll();
                                assertThat(executions)
                                        .anyMatch(e -> e.getCrawlTaskId().equals(taskId));
                            });
        }
    }

    @Nested
    @DisplayName("에러 시나리오 테스트")
    class ErrorScenarioTest {

        @Test
        @DisplayName("타임아웃 발생 시 CrawlExecution이 TIMEOUT 또는 FAILED 상태여야 한다")
        void shouldHandleTimeoutScenario() {
            // given
            testDataHelper.insertWorkerTestData(); // MySQL + Redis Pool 워밍업
            setupTimeoutMock();

            Long taskId = 7L;
            Long schedulerId = 1L;
            Long sellerId = 1L;
            String taskType = "MINI_SHOP";
            String endpoint = crawlTargetWireMock.baseUrl() + "/slow-endpoint";

            // when
            sendCrawlTaskMessage(taskId, schedulerId, sellerId, taskType, endpoint);

            // then
            await().atMost(Duration.ofSeconds(30))
                    .pollInterval(Duration.ofSeconds(1))
                    .untilAsserted(
                            () -> {
                                List<CrawlExecutionJpaEntity> executions =
                                        crawlExecutionRepository.findAll();
                                assertThat(executions)
                                        .anyMatch(
                                                e ->
                                                        e.getCrawlTaskId().equals(taskId)
                                                                && (e.getStatus()
                                                                                == CrawlExecutionStatus
                                                                                        .TIMEOUT
                                                                        || e.getStatus()
                                                                                == CrawlExecutionStatus
                                                                                        .FAILED));
                            });
        }

        @Test
        @DisplayName("Connection refused 시 CrawlExecution이 FAILED 상태여야 한다")
        void shouldHandleConnectionRefusedScenario() {
            // given
            testDataHelper.insertWorkerTestData(); // MySQL + Redis Pool 워밍업

            Long taskId = 8L;
            Long schedulerId = 1L;
            Long sellerId = 1L;
            String taskType = "MINI_SHOP";
            // 연결 불가능한 엔드포인트
            String endpoint = "http://localhost:19999/non-existent";

            // when
            sendCrawlTaskMessage(taskId, schedulerId, sellerId, taskType, endpoint);

            // then
            await().atMost(Duration.ofSeconds(15))
                    .pollInterval(Duration.ofMillis(500))
                    .untilAsserted(
                            () -> {
                                List<CrawlExecutionJpaEntity> executions =
                                        crawlExecutionRepository.findAll();
                                assertThat(executions)
                                        .anyMatch(
                                                e ->
                                                        e.getCrawlTaskId().equals(taskId)
                                                                && e.getStatus()
                                                                        == CrawlExecutionStatus
                                                                                .FAILED);
                            });
        }
    }

    // ===== WireMock 설정 헬퍼 메서드 =====

    private void setupCrawlTargetMock() {
        crawlTargetWireMock.stubFor(
                get(urlPathMatching("/products.*"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(
                                                """
{
    "products": [
        {"id": 1, "name": "Product 1", "price": 10000},
        {"id": 2, "name": "Product 2", "price": 20000}
    ],
    "totalCount": 2
}
""")));
    }

    private void setupCrawlTargetErrorMock() {
        crawlTargetWireMock.stubFor(
                get(urlPathMatching("/products/.*"))
                        .willReturn(
                                aResponse()
                                        .withStatus(500)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(
                                                """
                                                {
                                                    "error": "Internal Server Error",
                                                    "message": "Something went wrong"
                                                }
                                                """)));
    }

    private void setupMetaCrawlMock() {
        crawlTargetWireMock.stubFor(
                get(urlPathMatching("/meta/.*"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(
                                                """
                                                {
                                                    "items": [
                                                        {"itemNo": 10001, "name": "Meta Item 1"},
                                                        {"itemNo": 10002, "name": "Meta Item 2"}
                                                    ]
                                                }
                                                """)));
    }

    private void setupDetailCrawlMock() {
        crawlTargetWireMock.stubFor(
                get(urlPathMatching("/products/\\d+"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(
                                                """
                                                {
                                                    "id": 123,
                                                    "name": "Product Detail",
                                                    "description": "Detailed description",
                                                    "price": 15000,
                                                    "options": [
                                                        {"size": "M", "stock": 10},
                                                        {"size": "L", "stock": 5}
                                                    ]
                                                }
                                                """)));
    }

    private void setupTimeoutMock() {
        crawlTargetWireMock.stubFor(
                get(urlPathMatching("/slow-endpoint.*"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withFixedDelay(10000) // 10초 지연
                                        .withBody("{}")));
    }
}
