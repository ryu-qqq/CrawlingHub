package com.ryuqq.crawlinghub.application.crawl.component;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.crawlinghub.application.crawl.dto.CrawlContext;
import com.ryuqq.crawlinghub.application.crawl.dto.CrawlResult;
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
    private TestCrawler metaCrawler;

    @BeforeEach
    void setUp() {
        detailCrawler = new TestCrawler(CrawlTaskType.DETAIL);
        metaCrawler = new TestCrawler(CrawlTaskType.META);
        provider = new CrawlerProvider(List.of(detailCrawler, metaCrawler));
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
        @DisplayName("[성공] META 타입 크롤러 반환")
        void shouldReturnMetaCrawler() {
            // When
            Crawler result = provider.getCrawler(CrawlTaskType.META);

            // Then
            assertThat(result).isEqualTo(metaCrawler);
            assertThat(result.supportedType()).isEqualTo(CrawlTaskType.META);
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
            assertThat(provider.supports(CrawlTaskType.META)).isTrue();
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
