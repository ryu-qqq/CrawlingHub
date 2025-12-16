package com.ryuqq.crawlinghub.adapter.in.rest.product.mapper;

import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.PageApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.product.dto.query.SearchCrawledProductsApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.product.dto.response.CrawledProductDetailApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.product.dto.response.CrawledProductDetailApiResponse.CategoryInfo;
import com.ryuqq.crawlinghub.adapter.in.rest.product.dto.response.CrawledProductDetailApiResponse.CrawlStatusInfo;
import com.ryuqq.crawlinghub.adapter.in.rest.product.dto.response.CrawledProductDetailApiResponse.ImageInfo;
import com.ryuqq.crawlinghub.adapter.in.rest.product.dto.response.CrawledProductDetailApiResponse.ImagesInfo;
import com.ryuqq.crawlinghub.adapter.in.rest.product.dto.response.CrawledProductDetailApiResponse.OptionInfo;
import com.ryuqq.crawlinghub.adapter.in.rest.product.dto.response.CrawledProductDetailApiResponse.OptionsInfo;
import com.ryuqq.crawlinghub.adapter.in.rest.product.dto.response.CrawledProductDetailApiResponse.PriceInfo;
import com.ryuqq.crawlinghub.adapter.in.rest.product.dto.response.CrawledProductDetailApiResponse.ShippingInfo;
import com.ryuqq.crawlinghub.adapter.in.rest.product.dto.response.CrawledProductDetailApiResponse.SyncStatusInfo;
import com.ryuqq.crawlinghub.adapter.in.rest.product.dto.response.CrawledProductSummaryApiResponse;
import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.product.dto.query.SearchCrawledProductsQuery;
import com.ryuqq.crawlinghub.application.product.dto.response.CrawledProductDetailResponse;
import com.ryuqq.crawlinghub.application.product.dto.response.CrawledProductSummaryResponse;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * CrawledProductQueryApiMapper - CrawledProduct Query REST API ↔ Application Layer 변환
 *
 * <p>CrawledProduct Query 요청/응답에 대한 DTO 변환을 담당합니다.
 *
 * <p><strong>변환 방향:</strong>
 *
 * <ul>
 *   <li>API Query Request → Application Query (Controller → Application)
 *   <li>Application Response → API Response (Application → Controller)
 * </ul>
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>필드 매핑만 수행 (비즈니스 로직 포함 금지)
 *   <li>API DTO ↔ Application DTO 단순 변환
 *   <li>페이징 응답 변환 (PageResponse → PageApiResponse)
 *   <li>시간 포맷 변환 (Instant → ISO String)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawledProductQueryApiMapper {

    /**
     * SearchCrawledProductsApiRequest → SearchCrawledProductsQuery 변환
     *
     * @param request REST API 검색 요청
     * @return Application Layer 검색 쿼리
     */
    public SearchCrawledProductsQuery toQuery(SearchCrawledProductsApiRequest request) {
        return new SearchCrawledProductsQuery(
                request.sellerId(),
                request.itemNo(),
                request.itemName(),
                request.brandName(),
                request.needsSync(),
                request.allCrawled(),
                request.hasExternalId(),
                request.page(),
                request.size());
    }

    /**
     * CrawledProductSummaryResponse → CrawledProductSummaryApiResponse 변환
     *
     * @param appResponse Application Layer 요약 응답
     * @return REST API 요약 응답
     */
    public CrawledProductSummaryApiResponse toSummaryApiResponse(
            CrawledProductSummaryResponse appResponse) {
        return new CrawledProductSummaryApiResponse(
                appResponse.id(),
                appResponse.sellerId(),
                appResponse.itemNo(),
                appResponse.itemName(),
                appResponse.brandName(),
                appResponse.price(),
                appResponse.discountRate(),
                appResponse.crawlCompletedCount(),
                appResponse.pendingCrawlTypes(),
                appResponse.needsSync(),
                appResponse.externalProductId(),
                toIsoString(appResponse.lastSyncedAt()),
                appResponse.allImagesUploaded(),
                appResponse.totalStock(),
                toIsoString(appResponse.createdAt()),
                toIsoString(appResponse.updatedAt()));
    }

    /**
     * CrawledProductDetailResponse → CrawledProductDetailApiResponse 변환
     *
     * @param appResponse Application Layer 상세 응답
     * @return REST API 상세 응답
     */
    public CrawledProductDetailApiResponse toDetailApiResponse(
            CrawledProductDetailResponse appResponse) {
        return new CrawledProductDetailApiResponse(
                appResponse.id(),
                appResponse.sellerId(),
                appResponse.itemNo(),
                appResponse.itemName(),
                appResponse.brandName(),
                appResponse.itemStatus(),
                appResponse.originCountry(),
                appResponse.shippingLocation(),
                appResponse.freeShipping(),
                toPriceInfo(appResponse.price()),
                toImagesInfo(appResponse.images()),
                toCategoryInfo(appResponse.category()),
                toShippingInfo(appResponse.shipping()),
                toOptionsInfo(appResponse.options()),
                toCrawlStatusInfo(appResponse.crawlStatus()),
                toSyncStatusInfo(appResponse.syncStatus()),
                appResponse.descriptionMarkUp(),
                toIsoString(appResponse.createdAt()),
                toIsoString(appResponse.updatedAt()));
    }

    /**
     * PageResponse<CrawledProductSummaryResponse> →
     * PageApiResponse<CrawledProductSummaryApiResponse> 변환
     *
     * @param appPageResponse Application Layer 페이지 응답
     * @return REST API 페이지 응답
     */
    public PageApiResponse<CrawledProductSummaryApiResponse> toPageApiResponse(
            PageResponse<CrawledProductSummaryResponse> appPageResponse) {
        return PageApiResponse.from(appPageResponse, this::toSummaryApiResponse);
    }

    private String toIsoString(Instant instant) {
        return instant != null ? instant.toString() : null;
    }

    private PriceInfo toPriceInfo(CrawledProductDetailResponse.PriceInfo appPrice) {
        if (appPrice == null) {
            return new PriceInfo(0, 0, 0, 0, 0, 0);
        }
        return new PriceInfo(
                appPrice.price(),
                appPrice.originalPrice(),
                appPrice.normalPrice(),
                appPrice.appPrice(),
                appPrice.discountRate(),
                appPrice.appDiscountRate());
    }

    private ImagesInfo toImagesInfo(CrawledProductDetailResponse.ImagesInfo appImages) {
        if (appImages == null) {
            return new ImagesInfo(List.of(), List.of(), 0, 0);
        }
        List<ImageInfo> thumbnails =
                appImages.thumbnails().stream().map(this::toImageInfo).toList();
        List<ImageInfo> descriptionImages =
                appImages.descriptionImages().stream().map(this::toImageInfo).toList();
        return new ImagesInfo(
                thumbnails, descriptionImages, appImages.totalCount(), appImages.uploadedCount());
    }

    private ImageInfo toImageInfo(CrawledProductDetailResponse.ImageInfo appImage) {
        return new ImageInfo(
                appImage.originalUrl(),
                appImage.s3Url(),
                appImage.status(),
                appImage.displayOrder());
    }

    private CategoryInfo toCategoryInfo(CrawledProductDetailResponse.CategoryInfo appCategory) {
        if (appCategory == null) {
            return null;
        }
        return new CategoryInfo(
                appCategory.fullPath(),
                appCategory.headerCategoryCode(),
                appCategory.headerCategoryName(),
                appCategory.largeCategoryCode(),
                appCategory.largeCategoryName(),
                appCategory.mediumCategoryCode(),
                appCategory.mediumCategoryName());
    }

    private ShippingInfo toShippingInfo(CrawledProductDetailResponse.ShippingInfoDto appShipping) {
        if (appShipping == null) {
            return null;
        }
        return new ShippingInfo(
                appShipping.shippingType(),
                appShipping.shippingFee(),
                appShipping.shippingFeeType(),
                appShipping.averageDeliveryDays(),
                appShipping.freeShipping());
    }

    private OptionsInfo toOptionsInfo(CrawledProductDetailResponse.OptionsInfo appOptions) {
        if (appOptions == null) {
            return new OptionsInfo(List.of(), 0, 0, 0, List.of(), List.of());
        }
        List<OptionInfo> options = appOptions.options().stream().map(this::toOptionInfo).toList();
        return new OptionsInfo(
                options,
                appOptions.totalStock(),
                appOptions.inStockCount(),
                appOptions.soldOutCount(),
                appOptions.distinctColors(),
                appOptions.distinctSizes());
    }

    private OptionInfo toOptionInfo(CrawledProductDetailResponse.OptionInfo appOption) {
        return new OptionInfo(
                appOption.optionNo(), appOption.color(), appOption.size(), appOption.stock());
    }

    private CrawlStatusInfo toCrawlStatusInfo(
            CrawledProductDetailResponse.CrawlStatusInfo appStatus) {
        if (appStatus == null) {
            return new CrawlStatusInfo(null, null, null, 0, List.of());
        }
        return new CrawlStatusInfo(
                toIsoString(appStatus.miniShopCrawledAt()),
                toIsoString(appStatus.detailCrawledAt()),
                toIsoString(appStatus.optionCrawledAt()),
                appStatus.completedCount(),
                appStatus.pendingTypes());
    }

    private SyncStatusInfo toSyncStatusInfo(CrawledProductDetailResponse.SyncStatusInfo appSync) {
        if (appSync == null) {
            return new SyncStatusInfo(null, false, null, false);
        }
        return new SyncStatusInfo(
                appSync.externalProductId(),
                appSync.needsSync(),
                toIsoString(appSync.lastSyncedAt()),
                appSync.canSync());
    }
}
