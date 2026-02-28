package com.ryuqq.crawlinghub.application.product.factory;

import com.ryuqq.crawlinghub.domain.product.vo.DetailCrawlData;
import com.ryuqq.crawlinghub.domain.product.vo.MiniShopCrawlData;
import com.ryuqq.crawlinghub.domain.product.vo.MiniShopItem;
import com.ryuqq.crawlinghub.domain.product.vo.OptionCrawlData;
import com.ryuqq.crawlinghub.domain.product.vo.ProductDetailInfo;
import com.ryuqq.crawlinghub.domain.product.vo.ProductImages;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOption;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOptions;
import com.ryuqq.crawlinghub.domain.product.vo.ProductPrice;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * CrawledProduct 생성을 위한 Factory
 *
 * <p>MiniShopItem을 MiniShopCrawlData VO로 변환합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawledProductFactory {

    /**
     * MiniShopItem을 MiniShopCrawlData로 변환
     *
     * @param sellerId 판매자 ID
     * @param item MINI_SHOP 크롤링 결과
     * @return MiniShopCrawlData VO
     */
    public MiniShopCrawlData createMiniShopCrawlData(SellerId sellerId, MiniShopItem item) {
        ProductPrice price = item.toProductPrice();
        ProductImages images = ProductImages.fromUrls(item.imageUrls());
        boolean freeShipping = item.hasFreeShippingTag();

        return MiniShopCrawlData.of(
                sellerId,
                item.itemNo(),
                item.name(),
                item.brandName(),
                price,
                images,
                freeShipping,
                Instant.now());
    }

    /**
     * ProductDetailInfo를 DetailCrawlData로 변환
     *
     * @param detailInfo DETAIL 크롤링 결과
     * @return DetailCrawlData VO
     */
    public DetailCrawlData createDetailCrawlData(ProductDetailInfo detailInfo) {
        return DetailCrawlData.of(
                detailInfo.brandCode(),
                detailInfo.category(),
                detailInfo.shipping(),
                detailInfo.descriptionMarkUp(),
                detailInfo.itemStatus(),
                detailInfo.originCountry(),
                null,
                detailInfo.detailImages(),
                Instant.now());
    }

    /**
     * ProductOption 목록을 OptionCrawlData로 변환
     *
     * @param options OPTION 크롤링 결과
     * @return OptionCrawlData VO
     */
    public OptionCrawlData createOptionCrawlData(List<ProductOption> options) {
        ProductOptions productOptions = ProductOptions.from(options);
        return OptionCrawlData.of(productOptions, Instant.now());
    }
}
