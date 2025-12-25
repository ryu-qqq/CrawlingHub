package com.ryuqq.crawlinghub.application.crawl.processor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskFixture;
import com.ryuqq.crawlinghub.application.crawl.dto.CrawlResult;
import com.ryuqq.crawlinghub.application.crawl.parser.DetailResponseParser;
import com.ryuqq.crawlinghub.application.product.assembler.CrawledRawAssembler;
import com.ryuqq.crawlinghub.application.product.manager.CrawledRawManager;
import com.ryuqq.crawlinghub.application.product.port.in.command.ProcessDetailInfoUseCase;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * DetailCrawlResultProcessor 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("DetailCrawlResultProcessor 테스트")
class DetailCrawlResultProcessorTest {

    @Mock private DetailResponseParser detailResponseParser;
    @Mock private CrawledRawAssembler crawledRawAssembler;
    @Mock private CrawledRawManager crawledRawManager;
    @Mock private ProcessDetailInfoUseCase processDetailInfoUseCase;

    private DetailCrawlResultProcessor processor;

    @BeforeEach
    void setUp() {
        processor =
                new DetailCrawlResultProcessor(
                        detailResponseParser,
                        crawledRawAssembler,
                        crawledRawManager,
                        processDetailInfoUseCase);
    }

    @Nested
    @DisplayName("supportedType() 테스트")
    class SupportedType {

        @Test
        @DisplayName("[성공] DETAIL 타입 반환")
        void shouldReturnDetailType() {
            // When
            CrawlTaskType result = processor.supportedType();

            // Then
            assertThat(result).isEqualTo(CrawlTaskType.DETAIL);
        }
    }

    @Nested
    @DisplayName("process() 테스트")
    class Process {

        @Test
        @DisplayName("[실패] 파싱 실패 시 빈 결과 반환")
        void shouldReturnEmptyWhenParsingFails() {
            // Given
            CrawlTask task = CrawlTaskFixture.aWaitingTask();
            CrawlResult crawlResult = CrawlResult.success("invalid", 200);

            given(detailResponseParser.parse(anyString(), any())).willReturn(Optional.empty());

            // When
            ProcessingResult result = processor.process(crawlResult, task);

            // Then
            assertThat(result.hasFollowUpTasks()).isFalse();
            assertThat(result.getParsedItemCount()).isEqualTo(0);
            verify(crawledRawManager, never()).save(any());
        }

        @Test
        @DisplayName("[성공] 후속 Task 없음 확인")
        void shouldNotCreateFollowUpTasks() {
            // Given
            CrawlTask task = CrawlTaskFixture.aWaitingTask();
            CrawlResult crawlResult = CrawlResult.success("{}", 200);

            given(detailResponseParser.parse(anyString(), any())).willReturn(Optional.empty());

            // When
            ProcessingResult result = processor.process(crawlResult, task);

            // Then
            assertThat(result.hasFollowUpTasks()).isFalse();
            assertThat(result.getFollowUpCommands()).isEmpty();
        }
    }
}
