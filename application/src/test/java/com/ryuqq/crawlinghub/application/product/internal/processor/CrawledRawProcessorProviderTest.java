package com.ryuqq.crawlinghub.application.product.internal.processor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledRaw;
import com.ryuqq.crawlinghub.domain.product.vo.CrawlType;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * CrawledRawProcessorProvider 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("CrawledRawProcessorProvider 테스트")
class CrawledRawProcessorProviderTest {

    @Test
    @DisplayName("[성공] 등록된 CrawlType으로 프로세서 조회")
    void shouldReturnProcessorForRegisteredType() {
        // Given
        CrawledRawProcessor miniShopProcessor = new StubProcessor(CrawlType.MINI_SHOP);
        CrawledRawProcessor detailProcessor = new StubProcessor(CrawlType.DETAIL);
        CrawledRawProcessor optionProcessor = new StubProcessor(CrawlType.OPTION);

        CrawledRawProcessorProvider provider =
                new CrawledRawProcessorProvider(
                        List.of(miniShopProcessor, detailProcessor, optionProcessor));

        // When & Then
        assertThat(provider.getProcessor(CrawlType.MINI_SHOP)).isSameAs(miniShopProcessor);
        assertThat(provider.getProcessor(CrawlType.DETAIL)).isSameAs(detailProcessor);
        assertThat(provider.getProcessor(CrawlType.OPTION)).isSameAs(optionProcessor);
    }

    @Test
    @DisplayName("[실패] 미등록 CrawlType 조회 시 예외 발생")
    void shouldThrowExceptionForUnregisteredType() {
        // Given
        CrawledRawProcessorProvider provider =
                new CrawledRawProcessorProvider(List.of(new StubProcessor(CrawlType.MINI_SHOP)));

        // When & Then
        assertThatThrownBy(() -> provider.getProcessor(CrawlType.DETAIL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("지원하지 않는 CrawlType");
    }

    private static class StubProcessor implements CrawledRawProcessor {

        private final CrawlType type;

        StubProcessor(CrawlType type) {
            this.type = type;
        }

        @Override
        public CrawlType supportedType() {
            return type;
        }

        @Override
        public void process(CrawledRaw raw) {
            // no-op
        }
    }
}
