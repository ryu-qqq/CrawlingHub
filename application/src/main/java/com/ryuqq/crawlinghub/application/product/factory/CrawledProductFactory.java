package com.ryuqq.crawlinghub.application.product.factory;

import com.ryuqq.crawlinghub.application.common.time.TimeProvider;
import com.ryuqq.crawlinghub.application.product.dto.bundle.DetailProcessBundle;
import com.ryuqq.crawlinghub.application.product.dto.bundle.ImageUploadData;
import com.ryuqq.crawlinghub.application.product.dto.bundle.MiniShopProcessBundle;
import com.ryuqq.crawlinghub.domain.product.vo.DetailCrawlData;
import com.ryuqq.crawlinghub.domain.product.vo.ImageType;
import com.ryuqq.crawlinghub.domain.product.vo.MiniShopCrawlData;
import com.ryuqq.crawlinghub.domain.product.vo.MiniShopItem;
import com.ryuqq.crawlinghub.domain.product.vo.OptionCrawlData;
import com.ryuqq.crawlinghub.domain.product.vo.ProductDetailInfo;
import com.ryuqq.crawlinghub.domain.product.vo.ProductImages;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOption;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOptions;
import com.ryuqq.crawlinghub.domain.product.vo.ProductPrice;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * CrawledProduct 생성을 위한 Factory
 *
 * <p>MiniShopItem을 MiniShopCrawlData VO로 변환합니다. TimeProvider를 통해 생성 시점을 관리합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawledProductFactory {

    private final TimeProvider timeProvider;

    public CrawledProductFactory(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

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
                timeProvider.now());
    }

    /**
     * ProductDetailInfo를 DetailCrawlData로 변환
     *
     * @param detailInfo DETAIL 크롤링 결과
     * @return DetailCrawlData VO
     */
    public DetailCrawlData createDetailCrawlData(ProductDetailInfo detailInfo) {
        return DetailCrawlData.of(
                detailInfo.category(),
                detailInfo.shipping(),
                detailInfo.descriptionMarkUp(),
                detailInfo.itemStatus(),
                detailInfo.originCountry(),
                null,
                detailInfo.detailImages(),
                timeProvider.now());
    }

    /**
     * ProductOption 목록을 OptionCrawlData로 변환
     *
     * @param options OPTION 크롤링 결과
     * @return OptionCrawlData VO
     */
    public OptionCrawlData createOptionCrawlData(List<ProductOption> options) {
        ProductOptions productOptions = ProductOptions.from(options);
        return OptionCrawlData.of(productOptions, timeProvider.now());
    }

    // === Bundle 생성 메서드 ===

    /**
     * MINI_SHOP 크롤링 데이터를 MiniShopProcessBundle로 변환
     *
     * <p>MiniShopCrawlData와 썸네일 이미지 업로드 데이터를 Bundle로 묶습니다.
     *
     * @param sellerId 판매자 ID
     * @param item MINI_SHOP 크롤링 결과
     * @return MiniShopProcessBundle
     */
    public MiniShopProcessBundle createMiniShopProcessBundle(SellerId sellerId, MiniShopItem item) {
        MiniShopCrawlData crawlData = createMiniShopCrawlData(sellerId, item);
        List<String> pendingUploadUrls = crawlData.images().getPendingUploadUrls();
        ImageUploadData imageUploadData =
                ImageUploadData.of(pendingUploadUrls, ImageType.THUMBNAIL);
        return new MiniShopProcessBundle(crawlData, imageUploadData);
    }

    /**
     * DETAIL 크롤링 데이터를 DetailProcessBundle로 변환
     *
     * <p>DetailCrawlData와 설명 이미지 업로드 데이터를 Bundle로 묶습니다.
     *
     * @param detailInfo DETAIL 크롤링 결과
     * @return DetailProcessBundle
     */
    public DetailProcessBundle createDetailProcessBundle(ProductDetailInfo detailInfo) {
        DetailCrawlData crawlData = createDetailCrawlData(detailInfo);
        List<String> descriptionImages = crawlData.descriptionImages();
        ImageUploadData imageUploadData =
                descriptionImages.isEmpty()
                        ? null
                        : ImageUploadData.of(descriptionImages, ImageType.DESCRIPTION);
        return new DetailProcessBundle(crawlData, imageUploadData);
    }
}
