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
import com.ryuqq.crawlinghub.domain.product.vo.MiniShopCrawlData;
import com.ryuqq.crawlinghub.domain.product.vo.MiniShopItem;
import com.ryuqq.crawlinghub.domain.product.vo.ProductImages;
import com.ryuqq.crawlinghub.domain.product.vo.ProductPrice;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * MiniShopCrawledRawProcessor 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MiniShopCrawledRawProcessor 테스트")
class MiniShopCrawledRawProcessorTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2025-01-01T00:00:00Z");
    private static final SellerId SELLER_ID = SellerId.of(100L);
    private static final long ITEM_NO = 10001L;

    @Mock private CrawledRawMapper crawledRawMapper;
    @Mock private CrawledProductFactory crawledProductFactory;
    @Mock private CrawledProductCoordinator coordinator;

    private MiniShopCrawledRawProcessor processor;

    @BeforeEach
    void setUp() {
        processor =
                new MiniShopCrawledRawProcessor(
                        crawledRawMapper, crawledProductFactory, coordinator);
    }

    @Test
    @DisplayName("[성공] coordinator.createOrUpdate 호출 (sellerId, itemNo 전달)")
    @SuppressWarnings("unchecked")
    void shouldDelegateToCoordinatorCreateOrUpdate() {
        // Given
        CrawledRaw raw = createRaw();
        MiniShopItem item = createMiniShopItem();
        MiniShopCrawlData crawlData = createMiniShopCrawlData();

        given(crawledRawMapper.toMiniShopItem(raw.getRawData())).willReturn(item);
        given(crawledProductFactory.createMiniShopCrawlData(SELLER_ID, item)).willReturn(crawlData);

        // When
        processor.process(raw);

        // Then
        then(coordinator)
                .should()
                .createOrUpdate(
                        eq(SELLER_ID), eq(ITEM_NO), any(Consumer.class), any(Supplier.class));
    }

    private CrawledRaw createRaw() {
        return CrawledRaw.forNew(
                1L, 100L, ITEM_NO, CrawlType.MINI_SHOP, "{\"json\":true}", FIXED_INSTANT);
    }

    private MiniShopItem createMiniShopItem() {
        return new MiniShopItem(
                ITEM_NO,
                List.of("https://img.jpg"),
                "TestBrand",
                "Test Product",
                10000,
                12000,
                12000,
                17,
                17,
                10000,
                List.of());
    }

    private MiniShopCrawlData createMiniShopCrawlData() {
        return MiniShopCrawlData.of(
                SELLER_ID,
                ITEM_NO,
                "Test Product",
                "TestBrand",
                ProductPrice.of(10000, 12000, 12000, 9000, 10, 10),
                ProductImages.fromUrls(List.of("https://img.jpg")),
                true,
                FIXED_INSTANT);
    }
}
