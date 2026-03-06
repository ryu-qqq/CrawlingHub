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
import com.ryuqq.crawlinghub.domain.product.vo.DetailCrawlData;
import com.ryuqq.crawlinghub.domain.product.vo.ProductDetailInfo;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import java.time.Instant;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * DetailCrawledRawProcessor 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DetailCrawledRawProcessor 테스트")
class DetailCrawledRawProcessorTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2025-01-01T00:00:00Z");
    private static final SellerId SELLER_ID = SellerId.of(100L);
    private static final long ITEM_NO = 10001L;

    @Mock private CrawledRawMapper crawledRawMapper;
    @Mock private CrawledProductFactory crawledProductFactory;
    @Mock private CrawledProductCoordinator coordinator;

    private DetailCrawledRawProcessor processor;

    @BeforeEach
    void setUp() {
        processor =
                new DetailCrawledRawProcessor(crawledRawMapper, crawledProductFactory, coordinator);
    }

    @Test
    @DisplayName("[성공] coordinator.updateExistingAndSync 호출 (sellerId, itemNo 전달)")
    @SuppressWarnings("unchecked")
    void shouldDelegateToCoordinatorUpdateExistingAndSync() {
        // Given
        CrawledRaw raw = createRaw();
        ProductDetailInfo detailInfo = createDetailInfo();
        DetailCrawlData crawlData = createDetailCrawlData();

        given(crawledRawMapper.toProductDetailInfo(raw.getRawData())).willReturn(detailInfo);
        given(crawledProductFactory.createDetailCrawlData(detailInfo)).willReturn(crawlData);

        // When
        processor.process(raw);

        // Then
        then(coordinator)
                .should()
                .updateExistingAndSync(eq(SELLER_ID), eq(ITEM_NO), any(Consumer.class));
    }

    private CrawledRaw createRaw() {
        return CrawledRaw.forNew(
                1L, 100L, ITEM_NO, CrawlType.DETAIL, "{\"json\":true}", FIXED_INSTANT);
    }

    private ProductDetailInfo createDetailInfo() {
        return ProductDetailInfo.builder()
                .sellerNo(1L)
                .sellerId("seller-1")
                .itemNo(ITEM_NO)
                .itemName("Test Item")
                .brandName("TestBrand")
                .brandNameKr("테스트브랜드")
                .brandCode(100L)
                .normalPrice(12000)
                .sellingPrice(10000)
                .discountPrice(10000)
                .discountRate(17)
                .stock(17)
                .isSoldOut(false)
                .build();
    }

    private DetailCrawlData createDetailCrawlData() {
        return DetailCrawlData.of(0L, null, null, null, null, null, null, null, FIXED_INSTANT);
    }
}
