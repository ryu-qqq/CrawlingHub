package com.ryuqq.crawlinghub.domain.product.vo;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * DETAIL 크롤링 데이터 VO
 *
 * <p>CrawledProduct DETAIL 업데이트에 필요한 모든 정보를 담은 불변 객체입니다. Factory 패턴을 통해 ProductDetailInfo에서 변환됩니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public final class DetailCrawlData {

    private final long brandCode;
    private final ProductCategory category;
    private final ShippingInfo shippingInfo;
    private final String descriptionMarkUp;
    private final String itemStatus;
    private final String originCountry;
    private final String shippingLocation;
    private final List<String> descriptionImages;
    private final Instant updatedAt;

    private DetailCrawlData(
            long brandCode,
            ProductCategory category,
            ShippingInfo shippingInfo,
            String descriptionMarkUp,
            String itemStatus,
            String originCountry,
            String shippingLocation,
            List<String> descriptionImages,
            Instant updatedAt) {
        this.brandCode = brandCode;
        this.category = category;
        this.shippingInfo = shippingInfo;
        this.descriptionMarkUp = descriptionMarkUp;
        this.itemStatus = itemStatus;
        this.originCountry = originCountry;
        this.shippingLocation = shippingLocation;
        this.descriptionImages =
                descriptionImages == null ? List.of() : List.copyOf(descriptionImages);
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
    }

    public static DetailCrawlData of(
            long brandCode,
            ProductCategory category,
            ShippingInfo shippingInfo,
            String descriptionMarkUp,
            String itemStatus,
            String originCountry,
            String shippingLocation,
            List<String> descriptionImages,
            Instant updatedAt) {
        return new DetailCrawlData(
                brandCode,
                category,
                shippingInfo,
                descriptionMarkUp,
                itemStatus,
                originCountry,
                shippingLocation,
                descriptionImages,
                updatedAt);
    }

    public long brandCode() {
        return brandCode;
    }

    public ProductCategory category() {
        return category;
    }

    public ShippingInfo shippingInfo() {
        return shippingInfo;
    }

    public String descriptionMarkUp() {
        return descriptionMarkUp;
    }

    public String itemStatus() {
        return itemStatus;
    }

    public String originCountry() {
        return originCountry;
    }

    public String shippingLocation() {
        return shippingLocation;
    }

    public List<String> descriptionImages() {
        return descriptionImages;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DetailCrawlData that = (DetailCrawlData) o;
        return brandCode == that.brandCode
                && Objects.equals(category, that.category)
                && Objects.equals(shippingInfo, that.shippingInfo)
                && Objects.equals(descriptionMarkUp, that.descriptionMarkUp)
                && Objects.equals(itemStatus, that.itemStatus)
                && Objects.equals(originCountry, that.originCountry)
                && Objects.equals(shippingLocation, that.shippingLocation)
                && Objects.equals(descriptionImages, that.descriptionImages)
                && Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                brandCode,
                category,
                shippingInfo,
                descriptionMarkUp,
                itemStatus,
                originCountry,
                shippingLocation,
                descriptionImages,
                updatedAt);
    }

    @Override
    public String toString() {
        return "DetailCrawlData{"
                + "brandCode="
                + brandCode
                + ", category="
                + category
                + ", itemStatus='"
                + itemStatus
                + '\''
                + ", originCountry='"
                + originCountry
                + '\''
                + ", descriptionImagesCount="
                + descriptionImages.size()
                + ", updatedAt="
                + updatedAt
                + '}';
    }
}
