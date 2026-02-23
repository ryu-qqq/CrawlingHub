package com.ryuqq.crawlinghub.application.crawl.processor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.crawlinghub.application.execution.internal.crawler.processor.CrawlResultProcessor;
import com.ryuqq.crawlinghub.application.execution.internal.crawler.processor.CrawlResultProcessorProvider;
import com.ryuqq.crawlinghub.application.execution.internal.crawler.processor.ProcessingResult;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlResult;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * CrawlResultProcessorProvider 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("CrawlResultProcessorProvider 테스트")
class CrawlResultProcessorProviderTest {

    private CrawlResultProcessorProvider provider;
    private TestProcessor searchProcessor;
    private TestProcessor detailProcessor;

    @BeforeEach
    void setUp() {
        searchProcessor = new TestProcessor(CrawlTaskType.SEARCH);
        detailProcessor = new TestProcessor(CrawlTaskType.DETAIL);
        provider = new CrawlResultProcessorProvider(List.of(searchProcessor, detailProcessor));
    }

    @Nested
    @DisplayName("getProcessor() 테스트")
    class GetProcessor {

        @Test
        @DisplayName("[성공] SEARCH 타입 프로세서 반환")
        void shouldReturnSearchProcessor() {
            // When
            CrawlResultProcessor result = provider.getProcessor(CrawlTaskType.SEARCH);

            // Then
            assertThat(result).isEqualTo(searchProcessor);
            assertThat(result.supportedType()).isEqualTo(CrawlTaskType.SEARCH);
        }

        @Test
        @DisplayName("[성공] DETAIL 타입 프로세서 반환")
        void shouldReturnDetailProcessor() {
            // When
            CrawlResultProcessor result = provider.getProcessor(CrawlTaskType.DETAIL);

            // Then
            assertThat(result).isEqualTo(detailProcessor);
            assertThat(result.supportedType()).isEqualTo(CrawlTaskType.DETAIL);
        }

        @Test
        @DisplayName("[실패] 지원하지 않는 타입")
        void shouldThrowExceptionForUnsupportedType() {
            // When & Then
            assertThatThrownBy(() -> provider.getProcessor(CrawlTaskType.OPTION))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("지원하지 않는 CrawlTaskType");
        }
    }

    @Nested
    @DisplayName("supports() 테스트")
    class Supports {

        @Test
        @DisplayName("[성공] 지원하는 타입 확인")
        void shouldReturnTrueForSupportedType() {
            // When & Then
            assertThat(provider.supports(CrawlTaskType.SEARCH)).isTrue();
            assertThat(provider.supports(CrawlTaskType.DETAIL)).isTrue();
        }

        @Test
        @DisplayName("[성공] 지원하지 않는 타입 확인")
        void shouldReturnFalseForUnsupportedType() {
            // When & Then
            assertThat(provider.supports(CrawlTaskType.OPTION)).isFalse();
            assertThat(provider.supports(CrawlTaskType.MINI_SHOP)).isFalse();
        }
    }

    /** 테스트용 프로세서 구현 */
    private static class TestProcessor implements CrawlResultProcessor {
        private final CrawlTaskType type;

        TestProcessor(CrawlTaskType type) {
            this.type = type;
        }

        @Override
        public CrawlTaskType supportedType() {
            return type;
        }

        @Override
        public ProcessingResult process(CrawlResult crawlResult, CrawlTask crawlTask) {
            return ProcessingResult.empty();
        }
    }
}
