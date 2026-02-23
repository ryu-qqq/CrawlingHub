package com.ryuqq.crawlinghub.adapter.in.rest.task.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.adapter.in.rest.task.dto.response.CrawlTaskApiResponse;
import com.ryuqq.crawlinghub.application.task.dto.command.RetryCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskResult;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * CrawlTaskCommandApiMapper 단위 테스트
 *
 * <p>CrawlTask Command API ↔ Application Layer 변환 로직을 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("rest-api")
@Tag("mapper")
@DisplayName("CrawlTaskCommandApiMapper 단위 테스트")
class CrawlTaskCommandApiMapperTest {

    private CrawlTaskCommandApiMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CrawlTaskCommandApiMapper();
    }

    @Nested
    @DisplayName("toRetryCommand() 테스트")
    class ToRetryCommandTests {

        @Test
        @DisplayName("crawlTaskId로 재시도 명령을 생성한다")
        void toRetryCommand_ShouldCreateCommand() {
            // given
            Long crawlTaskId = 123L;

            // when
            RetryCrawlTaskCommand result = mapper.toRetryCommand(crawlTaskId);

            // then
            assertThat(result.crawlTaskId()).isEqualTo(123L);
        }

        @Test
        @DisplayName("다른 crawlTaskId로 재시도 명령을 생성한다")
        void toRetryCommand_WithDifferentId_ShouldCreateCommand() {
            // given
            Long crawlTaskId = 999L;

            // when
            RetryCrawlTaskCommand result = mapper.toRetryCommand(crawlTaskId);

            // then
            assertThat(result.crawlTaskId()).isEqualTo(999L);
        }
    }

    @Nested
    @DisplayName("toApiResponse() 테스트")
    class ToApiResponseTests {

        @Test
        @DisplayName("CrawlTaskResult를 API 응답으로 변환한다")
        void toApiResponse_ShouldConvertCorrectly() {
            // given
            Instant now = Instant.now();
            CrawlTaskResult appResult =
                    new CrawlTaskResult(
                            1L,
                            10L,
                            100L,
                            "https://example.com/products",
                            "https://example.com",
                            "/products",
                            Map.of(),
                            "RETRY",
                            "MINI_SHOP",
                            2,
                            now,
                            now);

            // when
            CrawlTaskApiResponse result = mapper.toApiResponse(appResult);

            // then
            assertThat(result.crawlTaskId()).isEqualTo(1L);
            assertThat(result.crawlSchedulerId()).isEqualTo(10L);
            assertThat(result.sellerId()).isEqualTo(100L);
            assertThat(result.requestUrl()).isEqualTo("https://example.com/products");
            assertThat(result.baseUrl()).isEqualTo("https://example.com");
            assertThat(result.path()).isEqualTo("/products");
            assertThat(result.status()).isEqualTo("RETRY");
            assertThat(result.taskType()).isEqualTo("MINI_SHOP");
            assertThat(result.retryCount()).isEqualTo(2);
            assertThat(result.createdAt()).isNotNull();
        }

        @Test
        @DisplayName("다양한 상태의 응답을 변환한다")
        void toApiResponse_WithVariousStatuses_ShouldConvertCorrectly() {
            // given
            Instant now = Instant.now();
            CrawlTaskResult appResult =
                    new CrawlTaskResult(
                            2L,
                            20L,
                            200L,
                            "https://example.com/products",
                            "https://example.com",
                            "/products",
                            Map.of(),
                            "TIMEOUT",
                            "DETAIL",
                            3,
                            now,
                            now);

            // when
            CrawlTaskApiResponse result = mapper.toApiResponse(appResult);

            // then
            assertThat(result.status()).isEqualTo("TIMEOUT");
            assertThat(result.taskType()).isEqualTo("DETAIL");
            assertThat(result.retryCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("null createdAt을 처리한다")
        void toApiResponse_WithNullCreatedAt_ShouldHandleNullValue() {
            // given
            CrawlTaskResult appResult =
                    new CrawlTaskResult(
                            1L,
                            10L,
                            100L,
                            "https://example.com",
                            "https://example.com",
                            "",
                            Map.of(),
                            "FAILED",
                            "OPTION",
                            1,
                            null,
                            null);

            // when
            CrawlTaskApiResponse result = mapper.toApiResponse(appResult);

            // then
            assertThat(result.createdAt()).isNull();
        }

        @Test
        @DisplayName("모든 TaskType을 올바르게 변환한다")
        void toApiResponse_WithAllTaskTypes_ShouldConvertCorrectly() {
            // given
            Instant now = Instant.now();

            // when & then - META
            CrawlTaskResult metaResult =
                    new CrawlTaskResult(
                            1L,
                            10L,
                            100L,
                            "https://example.com",
                            "https://example.com",
                            "",
                            Map.of(),
                            "SUCCESS",
                            "META",
                            0,
                            now,
                            now);
            assertThat(mapper.toApiResponse(metaResult).taskType()).isEqualTo("META");

            // when & then - OPTION
            CrawlTaskResult optionResult =
                    new CrawlTaskResult(
                            2L,
                            10L,
                            100L,
                            "https://example.com",
                            "https://example.com",
                            "",
                            Map.of(),
                            "SUCCESS",
                            "OPTION",
                            0,
                            now,
                            now);
            assertThat(mapper.toApiResponse(optionResult).taskType()).isEqualTo("OPTION");
        }
    }
}
