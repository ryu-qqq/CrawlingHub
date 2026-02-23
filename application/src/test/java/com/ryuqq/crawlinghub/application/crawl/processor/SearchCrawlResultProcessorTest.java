package com.ryuqq.crawlinghub.application.crawl.processor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskFixture;
import com.ryuqq.crawlinghub.application.execution.internal.crawler.parser.SearchResponseParser;
import com.ryuqq.crawlinghub.application.execution.internal.crawler.processor.ProcessingResult;
import com.ryuqq.crawlinghub.application.execution.internal.crawler.processor.SearchCrawlResultProcessor;
import com.ryuqq.crawlinghub.application.product.assembler.CrawledRawMapper;
import com.ryuqq.crawlinghub.application.product.manager.CrawledRawTransactionManager;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlResult;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledRaw;
import com.ryuqq.crawlinghub.domain.product.id.CrawledRawId;
import com.ryuqq.crawlinghub.domain.product.vo.MiniShopItem;
import com.ryuqq.crawlinghub.domain.product.vo.SearchParseResult;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * SearchCrawlResultProcessor 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SearchCrawlResultProcessor 테스트")
class SearchCrawlResultProcessorTest {

    @Mock private SearchResponseParser searchResponseParser;
    @Mock private CrawledRawMapper crawledRawMapper;
    @Mock private CrawledRawTransactionManager crawledRawTransactionManager;
    @Mock private CrawledRaw crawledRaw;

    private SearchCrawlResultProcessor processor;

    @BeforeEach
    void setUp() {
        processor =
                new SearchCrawlResultProcessor(
                        searchResponseParser, crawledRawMapper, crawledRawTransactionManager);
    }

    @Nested
    @DisplayName("supportedType() 테스트")
    class SupportedType {

        @Test
        @DisplayName("[성공] SEARCH 타입 반환")
        void shouldReturnSearchType() {
            // When
            CrawlTaskType result = processor.supportedType();

            // Then
            assertThat(result).isEqualTo(CrawlTaskType.SEARCH);
        }
    }

    @Nested
    @DisplayName("process() 테스트")
    class Process {

        @Test
        @DisplayName("[성공] SEARCH 응답 처리 및 다음 페이지 + DETAIL/OPTION Task 생성")
        void shouldProcessAndCreateNextPageAndDetailOptionTasks() {
            // Given
            CrawlTask task = CrawlTaskFixture.aWaitingTask();
            CrawlResult crawlResult = CrawlResult.success("{\"moduleList\": []}", 200);

            MiniShopItem item = createMiniShopItem(1001L);
            SearchParseResult parseResult =
                    new SearchParseResult(List.of(item), "/v1/search?page=2");

            given(searchResponseParser.parse(anyString())).willReturn(parseResult);
            given(crawledRawMapper.toMiniShopRaws(anyLong(), anyLong(), anyList(), any()))
                    .willReturn(List.of(crawledRaw));
            given(crawledRawTransactionManager.saveAll(anyList()))
                    .willReturn(List.of(CrawledRawId.of(1L)));

            // When
            ProcessingResult result = processor.process(crawlResult, task);

            // Then
            assertThat(result.hasFollowUpTasks()).isTrue();
            // 1 SEARCH (next page) + 1 DETAIL + 1 OPTION = 3 follow-up commands
            assertThat(result.getFollowUpCommands()).hasSize(3);
            assertThat(result.getParsedItemCount()).isEqualTo(1);
            assertThat(result.getSavedItemCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("[성공] 마지막 페이지 - nextApiUrl 없음")
        void shouldProcessLastPage() {
            // Given
            CrawlTask task = CrawlTaskFixture.aWaitingTask();
            CrawlResult crawlResult = CrawlResult.success("{\"moduleList\": []}", 200);

            MiniShopItem item = createMiniShopItem(1001L);
            SearchParseResult parseResult = new SearchParseResult(List.of(item), null);

            given(searchResponseParser.parse(anyString())).willReturn(parseResult);
            given(crawledRawMapper.toMiniShopRaws(anyLong(), anyLong(), anyList(), any()))
                    .willReturn(List.of(crawledRaw));
            given(crawledRawTransactionManager.saveAll(anyList()))
                    .willReturn(List.of(CrawledRawId.of(1L)));

            // When
            ProcessingResult result = processor.process(crawlResult, task);

            // Then
            // No next page, only DETAIL + OPTION = 2 follow-up commands
            assertThat(result.getFollowUpCommands()).hasSize(2);
        }

        @Test
        @DisplayName("[성공] 종료 조건 - 상품 없음 + nextApiUrl 없음")
        void shouldReturnEmptyWhenStopConditionMet() {
            // Given
            CrawlTask task = CrawlTaskFixture.aWaitingTask();
            CrawlResult crawlResult = CrawlResult.success("{\"moduleList\": []}", 200);

            SearchParseResult parseResult = SearchParseResult.empty();

            given(searchResponseParser.parse(anyString())).willReturn(parseResult);

            // When
            ProcessingResult result = processor.process(crawlResult, task);

            // Then
            assertThat(result.hasFollowUpTasks()).isFalse();
            assertThat(result.getParsedItemCount()).isEqualTo(0);
            verify(crawledRawTransactionManager, never()).saveAll(anyList());
        }
    }

    private MiniShopItem createMiniShopItem(Long itemNo) {
        return new MiniShopItem(
                itemNo,
                List.of("https://img.jpg"),
                "TestBrand",
                "Test Product " + itemNo,
                10000,
                12000,
                12000,
                17,
                17,
                10000,
                List.of());
    }
}
