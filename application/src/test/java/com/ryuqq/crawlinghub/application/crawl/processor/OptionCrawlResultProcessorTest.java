package com.ryuqq.crawlinghub.application.crawl.processor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.ryuqq.cralwinghub.domain.fixture.common.FixedClock;
import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlEndpointFixture;
import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskIdFixture;
import com.ryuqq.cralwinghub.domain.fixture.crawl.task.RetryCountFixture;
import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerIdFixture;
import com.ryuqq.cralwinghub.domain.fixture.seller.SellerIdFixture;
import com.ryuqq.crawlinghub.application.execution.internal.crawler.parser.OptionResponseParser;
import com.ryuqq.crawlinghub.application.execution.internal.crawler.processor.OptionCrawlResultProcessor;
import com.ryuqq.crawlinghub.application.execution.internal.crawler.processor.ProcessingResult;
import com.ryuqq.crawlinghub.application.product.assembler.CrawledRawMapper;
import com.ryuqq.crawlinghub.application.product.manager.CrawledRawTransactionManager;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlResult;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledRaw;
import com.ryuqq.crawlinghub.domain.product.id.CrawledRawId;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOption;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatus;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
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
 * OptionCrawlResultProcessor 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("OptionCrawlResultProcessor 테스트")
class OptionCrawlResultProcessorTest {

    @Mock private OptionResponseParser optionResponseParser;
    @Mock private CrawledRawMapper crawledRawMapper;
    @Mock private CrawledRawTransactionManager crawledRawTransactionManager;

    private OptionCrawlResultProcessor processor;

    @BeforeEach
    void setUp() {
        processor =
                new OptionCrawlResultProcessor(
                        optionResponseParser, crawledRawMapper, crawledRawTransactionManager);
    }

    /**
     * OPTION 타입 CrawlTask 생성. EndpointItemNoResolver가 itemNo를 정상 추출할 수 있도록 /item/{itemNo} 패턴이 포함된
     * detail endpoint를 활용한다. (aProductOptionEndpoint URL은 /auction_products/ 패턴으로 resolver가 인식 불가)
     */
    private CrawlTask anOptionTask() {
        Instant now = FixedClock.aDefaultClock().instant();
        return CrawlTask.reconstitute(
                CrawlTaskIdFixture.anAssignedId(),
                CrawlSchedulerIdFixture.anAssignedId(),
                SellerIdFixture.anAssignedId(),
                CrawlTaskType.OPTION,
                CrawlEndpointFixture.aProductDetailEndpoint(),
                CrawlTaskStatus.WAITING,
                RetryCountFixture.zero(),
                null,
                now,
                now);
    }

    @Nested
    @DisplayName("supportedType() 테스트")
    class SupportedType {

        @Test
        @DisplayName("[성공] OPTION 타입 반환")
        void shouldReturnOptionType() {
            // When
            CrawlTaskType result = processor.supportedType();

            // Then
            assertThat(result).isEqualTo(CrawlTaskType.OPTION);
        }
    }

    @Nested
    @DisplayName("process() 테스트")
    class Process {

        @Test
        @DisplayName("[실패] 파싱 결과 빈 경우")
        void shouldReturnEmptyWhenNoOptions() {
            // Given
            CrawlTask task = anOptionTask();
            CrawlResult crawlResult = CrawlResult.success("[]", 200);

            given(optionResponseParser.parse(anyString(), any()))
                    .willReturn(Collections.emptyList());

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
            CrawlTask task = anOptionTask();
            CrawlResult crawlResult = CrawlResult.success("{}", 200);

            given(optionResponseParser.parse(anyString(), any()))
                    .willReturn(Collections.emptyList());

            // When
            ProcessingResult result = processor.process(crawlResult, task);

            // Then
            assertThat(result.hasFollowUpTasks()).isFalse();
            assertThat(result.getFollowUpCommands()).isEmpty();
        }

        @Test
        @DisplayName("[성공] 파싱 성공 + CrawledRaw 저장 성공 -> completed 결과 반환")
        void shouldSaveAndReturnCompletedWhenParsingSucceeds() {
            // Given
            CrawlTask task = anOptionTask();
            CrawlResult crawlResult = CrawlResult.success("{\"options\": []}", 200);

            // 재고 있는 옵션 1개, 품절 옵션 1개 준비
            ProductOption inStockOption = ProductOption.of(1001L, 9999L, "RED", "M", 5, null);
            ProductOption outOfStockOption = ProductOption.of(1002L, 9999L, "BLUE", "L", 0, null);
            List<ProductOption> options = List.of(inStockOption, outOfStockOption);

            CrawledRaw crawledRaw = org.mockito.Mockito.mock(CrawledRaw.class);
            CrawledRawId savedId = CrawledRawId.of(100L);

            given(optionResponseParser.parse(anyString(), any())).willReturn(options);
            given(crawledRawMapper.toOptionRaw(anyLong(), anyLong(), anyLong(), any(), any()))
                    .willReturn(crawledRaw);
            given(crawledRawTransactionManager.save(crawledRaw)).willReturn(savedId);

            // When
            ProcessingResult result = processor.process(crawlResult, task);

            // Then
            assertThat(result.getParsedItemCount()).isEqualTo(2);
            assertThat(result.getSavedItemCount()).isEqualTo(1);
            assertThat(result.hasFollowUpTasks()).isFalse();
            verify(crawledRawTransactionManager).save(crawledRaw);
        }

        @Test
        @DisplayName("[성공] 파싱 성공 + crawledRawMapper가 null 반환 -> savedCount=0")
        void shouldReturnZeroSavedWhenMapperReturnsNull() {
            // Given
            CrawlTask task = anOptionTask();
            CrawlResult crawlResult = CrawlResult.success("{}", 200);

            ProductOption option = ProductOption.of(1001L, 9999L, "RED", "M", 5, null);
            List<ProductOption> options = List.of(option);

            given(optionResponseParser.parse(anyString(), any())).willReturn(options);
            given(crawledRawMapper.toOptionRaw(anyLong(), anyLong(), anyLong(), any(), any()))
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
            CrawlTask task = anOptionTask();
            CrawlResult crawlResult = CrawlResult.success("{}", 200);

            ProductOption option = ProductOption.of(1001L, 9999L, null, "ONE SIZE", 3, null);
            List<ProductOption> options = List.of(option);

            CrawledRaw crawledRaw = org.mockito.Mockito.mock(CrawledRaw.class);

            given(optionResponseParser.parse(anyString(), any())).willReturn(options);
            given(crawledRawMapper.toOptionRaw(anyLong(), anyLong(), anyLong(), any(), any()))
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
