package com.ryuqq.crawlinghub.application.crawl.processor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskFixture;
import com.ryuqq.crawlinghub.application.crawl.dto.CrawlResult;
import com.ryuqq.crawlinghub.application.crawl.parser.MetaResponseParser;
import com.ryuqq.crawlinghub.application.seller.port.in.command.UpdateSellerProductCountUseCase;
import com.ryuqq.crawlinghub.domain.product.vo.ProductCount;
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

/**
 * MetaCrawlResultProcessor 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MetaCrawlResultProcessor 테스트")
class MetaCrawlResultProcessorTest {

    @Mock private MetaResponseParser metaResponseParser;
    @Mock private UpdateSellerProductCountUseCase updateSellerProductCountUseCase;

    private MetaCrawlResultProcessor processor;

    @BeforeEach
    void setUp() {
        processor =
                new MetaCrawlResultProcessor(metaResponseParser, updateSellerProductCountUseCase);
    }

    @Nested
    @DisplayName("supportedType() 테스트")
    class SupportedType {

        @Test
        @DisplayName("[성공] META 타입 반환")
        void shouldReturnMetaType() {
            // When
            CrawlTaskType result = processor.supportedType();

            // Then
            assertThat(result).isEqualTo(CrawlTaskType.META);
        }
    }

    @Nested
    @DisplayName("process() 테스트")
    class Process {

        @Test
        @DisplayName("[성공] META 응답 처리 및 MINI_SHOP Task 생성")
        void shouldProcessMetaResponseAndCreateMiniShopTasks() {
            // Given
            CrawlTask task = CrawlTaskFixture.aWaitingTask();
            CrawlResult crawlResult = CrawlResult.success("{\"count\": 1500}", 200);
            ProductCount productCount = ProductCount.of(1500);

            given(metaResponseParser.parseResponse(anyString()))
                    .willReturn(Optional.of(productCount));

            // When
            ProcessingResult result = processor.process(crawlResult, task);

            // Then
            assertThat(result.hasFollowUpTasks()).isTrue();
            assertThat(result.getFollowUpCommands()).hasSize(3); // 1500/500 = 3 pages (0, 1, 2)
            assertThat(result.getParsedItemCount()).isEqualTo(1);
            assertThat(result.getSavedItemCount()).isEqualTo(1);

            verify(updateSellerProductCountUseCase).execute(anyLong(), anyInt());
        }

        @Test
        @DisplayName("[성공] 상품 수가 적은 경우 페이지 수 계산")
        void shouldCalculateSinglePage() {
            // Given
            CrawlTask task = CrawlTaskFixture.aWaitingTask();
            CrawlResult crawlResult = CrawlResult.success("{\"count\": 100}", 200);
            ProductCount productCount = ProductCount.of(100);

            given(metaResponseParser.parseResponse(anyString()))
                    .willReturn(Optional.of(productCount));

            // When
            ProcessingResult result = processor.process(crawlResult, task);

            // Then
            assertThat(result.hasFollowUpTasks()).isTrue();
            assertThat(result.getFollowUpCommands()).hasSize(1); // 100/500 = 1 page (page 0)
        }

        @Test
        @DisplayName("[실패] 파싱 실패 시 빈 결과 반환")
        void shouldReturnEmptyWhenParsingFails() {
            // Given
            CrawlTask task = CrawlTaskFixture.aWaitingTask();
            CrawlResult crawlResult = CrawlResult.success("invalid", 200);

            given(metaResponseParser.parseResponse(anyString())).willReturn(Optional.empty());

            // When
            ProcessingResult result = processor.process(crawlResult, task);

            // Then
            assertThat(result.hasFollowUpTasks()).isFalse();
            assertThat(result.getParsedItemCount()).isEqualTo(0);
            verify(updateSellerProductCountUseCase, never()).execute(anyLong(), anyInt());
        }

        @Test
        @DisplayName("[성공] 상품 수 0인 경우")
        void shouldHandleZeroProducts() {
            // Given
            CrawlTask task = CrawlTaskFixture.aWaitingTask();
            CrawlResult crawlResult = CrawlResult.success("{\"count\": 0}", 200);
            ProductCount productCount = ProductCount.of(0);

            given(metaResponseParser.parseResponse(anyString()))
                    .willReturn(Optional.of(productCount));

            // When
            ProcessingResult result = processor.process(crawlResult, task);

            // Then
            assertThat(result.hasFollowUpTasks()).isFalse();
            assertThat(result.getFollowUpCommands()).isEmpty();
            verify(updateSellerProductCountUseCase).execute(anyLong(), anyInt());
        }
    }
}
