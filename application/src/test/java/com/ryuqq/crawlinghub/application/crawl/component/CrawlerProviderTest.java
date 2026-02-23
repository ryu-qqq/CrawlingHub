package com.ryuqq.crawlinghub.application.crawl.component;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.crawlinghub.application.execution.internal.crawler.Crawler;
import com.ryuqq.crawlinghub.application.execution.internal.crawler.CrawlerProvider;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlContext;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlResult;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * CrawlerProvider 단위 테스트
 *
 * <p>전략 패턴 기반 크롤러 제공자 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("CrawlerProvider 테스트")
class CrawlerProviderTest {

    private CrawlerProvider provider;
    private TestCrawler detailCrawler;
    private TestCrawler searchCrawler;

    @BeforeEach
    void setUp() {
        detailCrawler = new TestCrawler(CrawlTaskType.DETAIL);
        searchCrawler = new TestCrawler(CrawlTaskType.SEARCH);
        provider = new CrawlerProvider(List.of(detailCrawler, searchCrawler));
    }

    @Nested
    @DisplayName("getCrawler() 테스트")
    class GetCrawler {

        @Test
        @DisplayName("[성공] DETAIL 타입 크롤러 반환")
        void shouldReturnDetailCrawler() {
            // When
            Crawler result = provider.getCrawler(CrawlTaskType.DETAIL);

            // Then
            assertThat(result).isEqualTo(detailCrawler);
            assertThat(result.supportedType()).isEqualTo(CrawlTaskType.DETAIL);
        }

        @Test
        @DisplayName("[성공] SEARCH 타입 크롤러 반환")
        void shouldReturnSearchCrawler() {
            // When
            Crawler result = provider.getCrawler(CrawlTaskType.SEARCH);

            // Then
            assertThat(result).isEqualTo(searchCrawler);
            assertThat(result.supportedType()).isEqualTo(CrawlTaskType.SEARCH);
        }

        @Test
        @DisplayName("[실패] 지원하지 않는 타입 - IllegalArgumentException")
        void shouldThrowExceptionForUnsupportedType() {
            // When & Then
            assertThatThrownBy(() -> provider.getCrawler(CrawlTaskType.OPTION))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("No crawler found for type");
        }
    }

    @Nested
    @DisplayName("supports() 테스트")
    class Supports {

        @Test
        @DisplayName("[성공] 등록된 타입 지원 확인")
        void shouldSupportRegisteredType() {
            // When & Then
            assertThat(provider.supports(CrawlTaskType.DETAIL)).isTrue();
            assertThat(provider.supports(CrawlTaskType.SEARCH)).isTrue();
        }

        @Test
        @DisplayName("[성공] 미등록 타입 미지원 확인")
        void shouldNotSupportUnregisteredType() {
            // When & Then
            assertThat(provider.supports(CrawlTaskType.OPTION)).isFalse();
            assertThat(provider.supports(CrawlTaskType.MINI_SHOP)).isFalse();
        }
    }

    // === Test Double ===

    private static class TestCrawler extends Crawler {
        private final CrawlTaskType type;

        TestCrawler(CrawlTaskType type) {
            this.type = type;
        }

        @Override
        public CrawlTaskType supportedType() {
            return type;
        }

        @Override
        public CrawlResult crawl(CrawlContext context) {
            return CrawlResult.success("{}", 200);
        }
    }
}
