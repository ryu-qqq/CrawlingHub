package com.ryuqq.crawlinghub.application.product.assembler;

import com.ryuqq.crawlinghub.application.product.dto.response.CrawledProductDetailResponse;
import com.ryuqq.crawlinghub.application.product.dto.response.CrawledProductDetailResponse.CategoryInfo;
import com.ryuqq.crawlinghub.application.product.dto.response.CrawledProductDetailResponse.CrawlStatusInfo;
import com.ryuqq.crawlinghub.application.product.dto.response.CrawledProductDetailResponse.ImageInfo;
import com.ryuqq.crawlinghub.application.product.dto.response.CrawledProductDetailResponse.ImagesInfo;
import com.ryuqq.crawlinghub.application.product.dto.response.CrawledProductDetailResponse.OptionInfo;
import com.ryuqq.crawlinghub.application.product.dto.response.CrawledProductDetailResponse.OptionsInfo;
import com.ryuqq.crawlinghub.application.product.dto.response.CrawledProductDetailResponse.PriceInfo;
import com.ryuqq.crawlinghub.application.product.dto.response.CrawledProductDetailResponse.ShippingInfoDto;
import com.ryuqq.crawlinghub.application.product.dto.response.CrawledProductDetailResponse.SyncStatusInfo;
import com.ryuqq.crawlinghub.application.product.dto.response.CrawledProductSummaryResponse;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.vo.CrawlCompletionStatus;
import com.ryuqq.crawlinghub.domain.product.vo.ProductCategory;
import com.ryuqq.crawlinghub.domain.product.vo.ProductImage;
import com.ryuqq.crawlinghub.domain.product.vo.ProductImages;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOption;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOptions;
import com.ryuqq.crawlinghub.domain.product.vo.ProductPrice;
import com.ryuqq.crawlinghub.domain.product.vo.ShippingInfo;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * CrawledProduct Assembler
 *
 * <p>Domain → Application DTO 변환 담당
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawledProductAssembler {

    /**
     * Domain → Summary Response 변환
     *
     * @param product CrawledProduct 도메인 객체
     * @return CrawledProductSummaryResponse
     */
    public CrawledProductSummaryResponse toSummaryResponse(CrawledProduct product) {
        CrawlCompletionStatus status = product.getCrawlCompletionStatus();
        ProductPrice price = product.getPrice();

        return new CrawledProductSummaryResponse(
                product.getIdValue(),
                product.getSellerIdValue(),
                product.getItemNo(),
                product.getItemName(),
                product.getBrandName(),
                price != null ? price.price() : 0,
                price != null ? price.discountRate() : 0,
                status != null ? status.getCompletedCount() : 0,
                status != null ? status.getPendingCrawlTypes() : "MINI_SHOP, DETAIL, OPTION",
                product.isNeedsSync(),
                product.getExternalProductId(),
                product.getLastSyncedAt(),
                product.allImagesUploaded(),
                product.getTotalStock(),
                product.getCreatedAt(),
                product.getUpdatedAt());
    }

    /**
     * Domain → Detail Response 변환
     *
     * @param product CrawledProduct 도메인 객체
     * @return CrawledProductDetailResponse
     */
    public CrawledProductDetailResponse toDetailResponse(CrawledProduct product) {
        return new CrawledProductDetailResponse(
                product.getIdValue(),
                product.getSellerIdValue(),
                product.getItemNo(),
                product.getItemName(),
                product.getBrandName(),
                product.getItemStatus(),
                product.getOriginCountry(),
                product.getShippingLocation(),
                product.isFreeShipping(),
                toPriceInfo(product.getPrice()),
                toImagesInfo(product.getImages()),
                toCategoryInfo(product.getCategory()),
                toShippingInfoDto(product.getShippingInfo()),
                toOptionsInfo(product.getOptions()),
                toCrawlStatusInfo(product.getCrawlCompletionStatus()),
                toSyncStatusInfo(product),
                product.getDescriptionMarkUp(),
                product.getCreatedAt(),
                product.getUpdatedAt());
    }

    private PriceInfo toPriceInfo(ProductPrice price) {
        if (price == null) {
            return new PriceInfo(0, 0, 0, 0, 0, 0);
        }
        return new PriceInfo(
                price.price(),
                price.originalPrice(),
                price.normalPrice(),
                price.appPrice(),
                price.discountRate(),
                price.appDiscountRate());
    }

    private ImagesInfo toImagesInfo(ProductImages images) {
        if (images == null) {
            return new ImagesInfo(List.of(), List.of(), 0, 0);
        }

        List<ImageInfo> thumbnails =
                images.getThumbnails().stream().map(this::toImageInfo).toList();

        List<ImageInfo> descriptionImages =
                images.getDescriptionImages().stream().map(this::toImageInfo).toList();

        int totalCount = images.size();
        int uploadedCount = (int) images.getAll().stream().filter(ProductImage::isUploaded).count();

        return new ImagesInfo(thumbnails, descriptionImages, totalCount, uploadedCount);
    }

    private ImageInfo toImageInfo(ProductImage image) {
        return new ImageInfo(
                image.originalUrl(), image.s3Url(), image.status().name(), image.displayOrder());
    }

    private CategoryInfo toCategoryInfo(ProductCategory category) {
        if (category == null) {
            return null;
        }
        return new CategoryInfo(
                category.getFullPath(),
                category.headerCategoryCode(),
                category.headerCategoryName(),
                category.largeCategoryCode(),
                category.largeCategoryName(),
                category.mediumCategoryCode(),
                category.mediumCategoryName());
    }

    private ShippingInfoDto toShippingInfoDto(ShippingInfo shipping) {
        if (shipping == null) {
            return null;
        }
        return new ShippingInfoDto(
                shipping.shippingType(),
                shipping.shippingFee(),
                shipping.shippingFeeType(),
                shipping.averageDeliveryDays(),
                shipping.freeShipping());
    }

    private OptionsInfo toOptionsInfo(ProductOptions options) {
        if (options == null || options.isEmpty()) {
            return new OptionsInfo(List.of(), 0, 0, 0, List.of(), List.of());
        }

        List<OptionInfo> optionInfoList =
                options.getAll().stream().map(this::toOptionInfo).toList();

        return new OptionsInfo(
                optionInfoList,
                options.getTotalStock(),
                options.getInStockOptions().size(),
                options.getSoldOutOptions().size(),
                options.getDistinctColors(),
                options.getDistinctSizes());
    }

    private OptionInfo toOptionInfo(ProductOption option) {
        return new OptionInfo(option.optionNo(), option.color(), option.size(), option.stock());
    }

    private CrawlStatusInfo toCrawlStatusInfo(CrawlCompletionStatus status) {
        if (status == null) {
            return new CrawlStatusInfo(
                    null, null, null, 0, List.of("MINI_SHOP", "DETAIL", "OPTION"));
        }

        List<String> pendingTypes = parsePendingTypes(status.getPendingCrawlTypes());

        return new CrawlStatusInfo(
                status.miniShopCrawledAt(),
                status.detailCrawledAt(),
                status.optionCrawledAt(),
                status.getCompletedCount(),
                pendingTypes);
    }

    private List<String> parsePendingTypes(String pendingTypesStr) {
        if (pendingTypesStr == null || pendingTypesStr.isBlank()) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (String type : pendingTypesStr.split(",")) {
            String trimmed = type.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }

    private SyncStatusInfo toSyncStatusInfo(CrawledProduct product) {
        return new SyncStatusInfo(
                product.getExternalProductId(),
                product.isNeedsSync(),
                product.getLastSyncedAt(),
                product.canSyncToExternalServer());
    }
}
