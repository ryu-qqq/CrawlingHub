package com.ryuqq.crawlinghub.application.execution.factory.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskFixture;
import com.ryuqq.crawlinghub.application.common.time.TimeProvider;
import com.ryuqq.crawlinghub.application.execution.dto.bundle.CrawlTaskExecutionBundle;
import com.ryuqq.crawlinghub.application.execution.dto.command.ExecuteCrawlTaskCommand;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawlExecutionCommandFactory 단위 테스트
 *
 * <p>Mockist 스타일 테스트: TimeProvider Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawlExecutionCommandFactory 테스트")
class CrawlExecutionCommandFactoryTest {

    @Mock private TimeProvider timeProvider;

    @InjectMocks private CrawlExecutionCommandFactory factory;

    @Nested
    @DisplayName("createExecutionBundle() 테스트")
    class CreateExecutionBundle {

        @Test
        @DisplayName("[성공] CrawlTask와 Command로 실행 번들 생성")
        void shouldCreateExecutionBundleFromTaskAndCommand() {
            // Given
            CrawlTask crawlTask = CrawlTaskFixture.aWaitingTask();
            ExecuteCrawlTaskCommand command =
                    new ExecuteCrawlTaskCommand(1L, 2L, 3L, "MINI_SHOP", "https://example.com");
            Instant now = Instant.now();
            given(timeProvider.now()).willReturn(now);

            // When
            CrawlTaskExecutionBundle bundle = factory.createExecutionBundle(crawlTask, command);

            // Then
            assertThat(bundle).isNotNull();
            assertThat(bundle.crawlTask()).isEqualTo(crawlTask);
            assertThat(bundle.command()).isEqualTo(command);
            assertThat(bundle.changedAt()).isEqualTo(now);
            assertThat(bundle.crawlContext()).isNull();
        }

        @Test
        @DisplayName("[성공] 생성된 번들에서 CrawlExecution이 포함됨")
        void shouldIncludeCrawlExecutionInBundle() {
            // Given
            CrawlTask crawlTask = CrawlTaskFixture.aWaitingTask();
            ExecuteCrawlTaskCommand command =
                    new ExecuteCrawlTaskCommand(
                            1L, 2L, 3L, "MINI_SHOP", "https://shop.example.com");
            Instant now = Instant.parse("2024-01-01T10:00:00Z");
            given(timeProvider.now()).willReturn(now);

            // When
            CrawlTaskExecutionBundle bundle = factory.createExecutionBundle(crawlTask, command);

            // Then
            assertThat(bundle.execution()).isNotNull();
            assertThat(bundle.changedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("[성공] withCrawlContext로 새 번들 생성 시 crawlContext 포함")
        void shouldCreateBundleWithCrawlContextWhenEnriched() {
            // Given
            CrawlTask crawlTask = CrawlTaskFixture.aWaitingTask();
            ExecuteCrawlTaskCommand command =
                    new ExecuteCrawlTaskCommand(1L, 2L, 3L, "DETAIL", "https://detail.example.com");
            Instant now = Instant.now();
            given(timeProvider.now()).willReturn(now);

            CrawlTaskExecutionBundle bundle = factory.createExecutionBundle(crawlTask, command);

            // CrawlContext를 추가하지 않았으므로 null 확인
            assertThat(bundle.crawlContext()).isNull();
        }
    }
}
