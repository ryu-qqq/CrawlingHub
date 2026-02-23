package com.ryuqq.crawlinghub.application.execution.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskFixture;
import com.ryuqq.cralwinghub.domain.fixture.execution.CrawlExecutionFixture;
import com.ryuqq.crawlinghub.application.execution.dto.bundle.CrawlTaskExecutionBundle;
import com.ryuqq.crawlinghub.application.execution.dto.command.ExecuteCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.execution.internal.crawler.Crawler;
import com.ryuqq.crawlinghub.application.execution.internal.crawler.CrawlerProvider;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlContext;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlResult;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawlingProcessor 단위 테스트
 *
 * <p>CrawlingProcessor는 순수 크롤링(HTTP 호출)만 담당합니다. UserAgent 결과 기록은 Coordinator에서 수행합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawlingProcessor 테스트")
class CrawlingProcessorTest {

    @Mock private CrawlerProvider crawlerProvider;

    @Mock private Crawler mockCrawler;

    @InjectMocks private CrawlingProcessor processor;

    @Nested
    @DisplayName("executeCrawling() 테스트")
    class ExecuteCrawling {

        @Test
        @DisplayName("[성공] 크롤링 성공 → 성공 결과 반환 + UserAgent 성공 기록")
        void shouldReturnSuccessResult() {
            // Given
            CrawlTaskExecutionBundle bundle = createEnrichedBundle();
            CrawlResult successResult = CrawlResult.success("{\"data\": []}", 200);

            given(crawlerProvider.getCrawler(any(CrawlTaskType.class))).willReturn(mockCrawler);
            given(mockCrawler.crawl(any(CrawlContext.class))).willReturn(successResult);

            // When
            CrawlResult result = processor.executeCrawling(bundle);

            // Then
            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("[실패] 크롤링 실패 (HTTP 500) → 실패 결과 반환 + UserAgent 실패 기록")
        void shouldReturnFailureResultWhenHttp500() {
            // Given
            CrawlTaskExecutionBundle bundle = createEnrichedBundle();
            CrawlResult failResult = CrawlResult.failure(500, "Internal Server Error");

            given(crawlerProvider.getCrawler(any(CrawlTaskType.class))).willReturn(mockCrawler);
            given(mockCrawler.crawl(any(CrawlContext.class))).willReturn(failResult);

            // When
            CrawlResult result = processor.executeCrawling(bundle);

            // Then
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.httpStatusCode()).isEqualTo(500);
        }

        @Test
        @DisplayName("[실패] Rate Limit (HTTP 429) → 실패 결과 반환 + UserAgent 실패 기록")
        void shouldReturnFailureResultWhenRateLimited() {
            // Given
            CrawlTaskExecutionBundle bundle = createEnrichedBundle();
            CrawlResult rateLimitResult = CrawlResult.failure(429, "Rate limited (429)");

            given(crawlerProvider.getCrawler(any(CrawlTaskType.class))).willReturn(mockCrawler);
            given(mockCrawler.crawl(any(CrawlContext.class))).willReturn(rateLimitResult);

            // When
            CrawlResult result = processor.executeCrawling(bundle);

            // Then
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.httpStatusCode()).isEqualTo(429);
        }

        private CrawlTaskExecutionBundle createEnrichedBundle() {
            CrawlTask task = CrawlTaskFixture.aRunningTask();
            ExecuteCrawlTaskCommand command =
                    new ExecuteCrawlTaskCommand(1L, 1L, 1L, "SEARCH", "https://example.com/api");
            CrawlTaskExecutionBundle bundle =
                    CrawlTaskExecutionBundle.of(
                            task, CrawlExecutionFixture.forNew(), command, Instant.now());
            CrawlContext context =
                    new CrawlContext(
                            1L,
                            1L,
                            1L,
                            CrawlTaskType.SEARCH,
                            "https://example.com/api",
                            1L,
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64)",
                            "session-token-123",
                            null,
                            null);
            return bundle.withCrawlContext(context);
        }
    }
}
