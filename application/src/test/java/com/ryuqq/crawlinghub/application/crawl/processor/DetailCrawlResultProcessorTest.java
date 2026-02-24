package com.ryuqq.crawlinghub.application.crawl.processor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskFixture;
import com.ryuqq.crawlinghub.application.execution.internal.crawler.parser.DetailResponseParser;
import com.ryuqq.crawlinghub.application.execution.internal.crawler.processor.DetailCrawlResultProcessor;
import com.ryuqq.crawlinghub.application.execution.internal.crawler.processor.ProcessingResult;
import com.ryuqq.crawlinghub.application.product.assembler.CrawledRawMapper;
import com.ryuqq.crawlinghub.application.product.manager.CrawledRawTransactionManager;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlResult;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledRaw;
import com.ryuqq.crawlinghub.domain.product.id.CrawledRawId;
import com.ryuqq.crawlinghub.domain.product.vo.ProductDetailInfo;
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
    @Mock private CrawledRawMapper crawledRawMapper;
    @Mock private CrawledRawTransactionManager crawledRawTransactionManager;

    private DetailCrawlResultProcessor processor;

    @BeforeEach
    void setUp() {
        processor =
                new DetailCrawlResultProcessor(
                        detailResponseParser, crawledRawMapper, crawledRawTransactionManager);
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
            verify(crawledRawTransactionManager, never()).save(any());
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

        @Test
        @DisplayName("[성공] 파싱 성공 + CrawledRaw 저장 성공 -> completed(1, 1) 반환")
        void shouldSaveAndReturnCompletedWhenParsingSucceeds() {
            // Given
            CrawlTask task = CrawlTaskFixture.aWaitingTask();
            CrawlResult crawlResult = CrawlResult.success("{\"detail\": {}}", 200);

            ProductDetailInfo detailInfo =
                    ProductDetailInfo.builder()
                            .sellerNo(1001L)
                            .sellerId("seller-id-001")
                            .itemNo(9999L)
                            .itemName("테스트 상품명")
                            .normalPrice(100000)
                            .sellingPrice(90000)
                            .discountPrice(10000)
                            .discountRate(10)
                            .stock(5)
                            .build();

            CrawledRaw crawledRaw = org.mockito.Mockito.mock(CrawledRaw.class);
            CrawledRawId savedId = CrawledRawId.of(200L);

            given(detailResponseParser.parse(anyString(), any()))
                    .willReturn(Optional.of(detailInfo));
            given(crawledRawMapper.toDetailRaw(anyLong(), anyLong(), any(), any()))
                    .willReturn(crawledRaw);
            given(crawledRawTransactionManager.save(crawledRaw)).willReturn(savedId);

            // When
            ProcessingResult result = processor.process(crawlResult, task);

            // Then
            assertThat(result.getParsedItemCount()).isEqualTo(1);
            assertThat(result.getSavedItemCount()).isEqualTo(1);
            assertThat(result.hasFollowUpTasks()).isFalse();
            verify(crawledRawTransactionManager).save(crawledRaw);
        }

        @Test
        @DisplayName("[성공] 파싱 성공 + crawledRawMapper가 null 반환 -> savedCount=0")
        void shouldReturnZeroSavedWhenMapperReturnsNull() {
            // Given
            CrawlTask task = CrawlTaskFixture.aWaitingTask();
            CrawlResult crawlResult = CrawlResult.success("{}", 200);

            ProductDetailInfo detailInfo =
                    ProductDetailInfo.builder()
                            .sellerNo(1001L)
                            .sellerId("seller-id-001")
                            .itemNo(9999L)
                            .itemName("테스트 상품명")
                            .normalPrice(100000)
                            .sellingPrice(90000)
                            .discountPrice(10000)
                            .discountRate(10)
                            .stock(5)
                            .build();

            given(detailResponseParser.parse(anyString(), any()))
                    .willReturn(Optional.of(detailInfo));
            given(crawledRawMapper.toDetailRaw(anyLong(), anyLong(), any(), any()))
                    .willReturn(null);

            // When
            ProcessingResult result = processor.process(crawlResult, task);

            // Then
            assertThat(result.getParsedItemCount()).isEqualTo(1);
            assertThat(result.getSavedItemCount()).isEqualTo(0);
            verify(crawledRawTransactionManager, never()).save(any());
        }

        @Test
        @DisplayName("[성공] 파싱 성공 + 저장 후 savedId가 null -> savedCount=0")
        void shouldReturnZeroSavedWhenSavedIdIsNull() {
            // Given
            CrawlTask task = CrawlTaskFixture.aWaitingTask();
            CrawlResult crawlResult = CrawlResult.success("{}", 200);

            ProductDetailInfo detailInfo =
                    ProductDetailInfo.builder()
                            .sellerNo(1001L)
                            .sellerId("seller-id-001")
                            .itemNo(9999L)
                            .itemName("테스트 상품명")
                            .normalPrice(50000)
                            .sellingPrice(50000)
                            .discountPrice(0)
                            .discountRate(0)
                            .stock(10)
                            .build();

            CrawledRaw crawledRaw = org.mockito.Mockito.mock(CrawledRaw.class);

            given(detailResponseParser.parse(anyString(), any()))
                    .willReturn(Optional.of(detailInfo));
            given(crawledRawMapper.toDetailRaw(anyLong(), anyLong(), any(), any()))
                    .willReturn(crawledRaw);
            given(crawledRawTransactionManager.save(crawledRaw)).willReturn(null);

            // When
            ProcessingResult result = processor.process(crawlResult, task);

            // Then
            assertThat(result.getParsedItemCount()).isEqualTo(1);
            assertThat(result.getSavedItemCount()).isEqualTo(0);
        }
    }
}
