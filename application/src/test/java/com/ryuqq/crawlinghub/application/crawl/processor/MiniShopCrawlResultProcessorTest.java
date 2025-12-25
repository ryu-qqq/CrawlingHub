package com.ryuqq.crawlinghub.application.crawl.processor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskFixture;
import com.ryuqq.crawlinghub.application.crawl.dto.CrawlResult;
import com.ryuqq.crawlinghub.application.crawl.parser.MiniShopResponseParser;
import com.ryuqq.crawlinghub.application.product.assembler.CrawledRawAssembler;
import com.ryuqq.crawlinghub.application.product.manager.CrawledRawManager;
import com.ryuqq.crawlinghub.application.product.port.in.command.ProcessMiniShopItemUseCase;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledRaw;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledRawId;
import com.ryuqq.crawlinghub.domain.product.vo.MiniShopItem;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * MiniShopCrawlResultProcessor 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MiniShopCrawlResultProcessor 테스트")
class MiniShopCrawlResultProcessorTest {

    @Mock private MiniShopResponseParser miniShopResponseParser;
    @Mock private CrawledRawAssembler crawledRawAssembler;
    @Mock private CrawledRawManager crawledRawManager;
    @Mock private ProcessMiniShopItemUseCase processMiniShopItemUseCase;
    @Mock private CrawledRaw crawledRaw;

    private MiniShopCrawlResultProcessor processor;

    @BeforeEach
    void setUp() {
        processor =
                new MiniShopCrawlResultProcessor(
                        miniShopResponseParser,
                        crawledRawAssembler,
                        crawledRawManager,
                        processMiniShopItemUseCase);
    }

    @Nested
    @DisplayName("supportedType() 테스트")
    class SupportedType {

        @Test
        @DisplayName("[성공] MINI_SHOP 타입 반환")
        void shouldReturnMiniShopType() {
            // When
            CrawlTaskType result = processor.supportedType();

            // Then
            assertThat(result).isEqualTo(CrawlTaskType.MINI_SHOP);
        }
    }

    @Nested
    @DisplayName("process() 테스트")
    class Process {

        @Test
        @DisplayName("[성공] MINI_SHOP 응답 처리 및 DETAIL/OPTION Task 생성")
        void shouldProcessAndCreateDetailOptionTasks() {
            // Given
            CrawlTask task = CrawlTaskFixture.aWaitingTask();
            CrawlResult crawlResult = CrawlResult.success("{\"items\": []}", 200);

            MiniShopItem item1 = createMiniShopItem(1001L);
            MiniShopItem item2 = createMiniShopItem(1002L);
            List<MiniShopItem> items = List.of(item1, item2);

            given(miniShopResponseParser.parse(anyString())).willReturn(items);
            given(crawledRawAssembler.toMiniShopRaws(anyLong(), anyLong(), anyList()))
                    .willReturn(List.of(crawledRaw, crawledRaw));
            given(crawledRawManager.saveAll(anyList()))
                    .willReturn(List.of(CrawledRawId.of(1L), CrawledRawId.of(2L)));

            // When
            ProcessingResult result = processor.process(crawlResult, task);

            // Then
            assertThat(result.hasFollowUpTasks()).isTrue();
            // 2 items * 2 (DETAIL + OPTION) = 4 follow-up commands
            assertThat(result.getFollowUpCommands()).hasSize(4);
            assertThat(result.getParsedItemCount()).isEqualTo(2);
            assertThat(result.getSavedItemCount()).isEqualTo(2);

            verify(processMiniShopItemUseCase, times(2)).process(any(), any());
        }

        @Test
        @DisplayName("[실패] 파싱 결과 빈 경우")
        void shouldReturnEmptyWhenNoItems() {
            // Given
            CrawlTask task = CrawlTaskFixture.aWaitingTask();
            CrawlResult crawlResult = CrawlResult.success("[]", 200);

            given(miniShopResponseParser.parse(anyString())).willReturn(Collections.emptyList());

            // When
            ProcessingResult result = processor.process(crawlResult, task);

            // Then
            assertThat(result.hasFollowUpTasks()).isFalse();
            assertThat(result.getParsedItemCount()).isEqualTo(0);
            verify(crawledRawManager, never()).saveAll(anyList());
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
