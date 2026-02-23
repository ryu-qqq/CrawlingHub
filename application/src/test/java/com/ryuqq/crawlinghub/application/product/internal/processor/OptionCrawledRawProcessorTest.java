package com.ryuqq.crawlinghub.application.product.internal.processor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.crawlinghub.application.product.assembler.CrawledRawMapper;
import com.ryuqq.crawlinghub.application.product.factory.CrawledProductFactory;
import com.ryuqq.crawlinghub.application.product.internal.CrawledProductCoordinator;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledRaw;
import com.ryuqq.crawlinghub.domain.product.vo.CrawlType;
import com.ryuqq.crawlinghub.domain.product.vo.OptionCrawlData;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOption;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOptions;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * OptionCrawledRawProcessor 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OptionCrawledRawProcessor 테스트")
class OptionCrawledRawProcessorTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2025-01-01T00:00:00Z");
    private static final SellerId SELLER_ID = SellerId.of(100L);
    private static final long ITEM_NO = 10001L;

    @Mock private CrawledRawMapper crawledRawMapper;
    @Mock private CrawledProductFactory crawledProductFactory;
    @Mock private CrawledProductCoordinator coordinator;

    private OptionCrawledRawProcessor processor;

    @BeforeEach
    void setUp() {
        processor =
                new OptionCrawledRawProcessor(crawledRawMapper, crawledProductFactory, coordinator);
    }

    @Test
    @DisplayName("[성공] coordinator.updateExistingAndSync 호출 (sellerId, itemNo 전달)")
    @SuppressWarnings("unchecked")
    void shouldDelegateToCoordinatorUpdateExistingAndSync() {
        // Given
        CrawledRaw raw = createRaw();
        List<ProductOption> options = createProductOptions();
        OptionCrawlData crawlData = createOptionCrawlData();

        given(crawledRawMapper.toProductOptions(raw.getRawData())).willReturn(options);
        given(crawledProductFactory.createOptionCrawlData(options)).willReturn(crawlData);

        // When
        processor.process(raw);

        // Then
        then(coordinator)
                .should()
                .updateExistingAndSync(eq(SELLER_ID), eq(ITEM_NO), any(Consumer.class));
    }

    private CrawledRaw createRaw() {
        return CrawledRaw.forNew(
                1L, 100L, ITEM_NO, CrawlType.OPTION, "[{\"json\":true}]", FIXED_INSTANT);
    }

    private List<ProductOption> createProductOptions() {
        return List.of(
                ProductOption.of(1001L, ITEM_NO, "Black", "S", 10, ""),
                ProductOption.of(1002L, ITEM_NO, "Black", "M", 20, ""));
    }

    private OptionCrawlData createOptionCrawlData() {
        ProductOptions productOptions = ProductOptions.from(createProductOptions());
        return OptionCrawlData.of(productOptions, FIXED_INSTANT);
    }
}
