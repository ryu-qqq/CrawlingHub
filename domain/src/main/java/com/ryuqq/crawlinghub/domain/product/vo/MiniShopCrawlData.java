package com.ryuqq.crawlinghub.domain.product.vo;

import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.time.Instant;
import java.util.Objects;

/**
 * MINI_SHOP 크롤링 데이터 VO
 *
 * <p>CrawledProduct 생성에 필요한 모든 정보를 담은 불변 객체입니다. Factory 패턴을 통해 MiniShopItem에서 변환됩니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public final class MiniShopCrawlData {

    private final SellerId sellerId;
    private final long itemNo;
    private final String itemName;
    private final String brandName;
    private final ProductPrice price;
    private final ProductImages images;
    private final boolean freeShipping;
    private final Instant createdAt;

    private MiniShopCrawlData(
            SellerId sellerId,
            long itemNo,
            String itemName,
            String brandName,
            ProductPrice price,
            ProductImages images,
            boolean freeShipping,
            Instant createdAt) {
        this.sellerId = Objects.requireNonNull(sellerId, "sellerId must not be null");
        this.itemNo = itemNo;
        this.itemName = Objects.requireNonNull(itemName, "itemName must not be null");
        this.brandName = brandName;
        this.price = Objects.requireNonNull(price, "price must not be null");
        this.images = Objects.requireNonNull(images, "images must not be null");
        this.freeShipping = freeShipping;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
    }

    public static MiniShopCrawlData of(
            SellerId sellerId,
            long itemNo,
            String itemName,
            String brandName,
            ProductPrice price,
            ProductImages images,
            boolean freeShipping,
            Instant createdAt) {
        return new MiniShopCrawlData(
                sellerId, itemNo, itemName, brandName, price, images, freeShipping, createdAt);
    }

    public SellerId sellerId() {
        return sellerId;
    }

    public long itemNo() {
        return itemNo;
    }

    public String itemName() {
        return itemName;
    }

    public String brandName() {
        return brandName;
    }

    public ProductPrice price() {
        return price;
    }

    public ProductImages images() {
        return images;
    }

    public boolean freeShipping() {
        return freeShipping;
    }

    public Instant createdAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MiniShopCrawlData that = (MiniShopCrawlData) o;
        return itemNo == that.itemNo
                && freeShipping == that.freeShipping
                && Objects.equals(sellerId, that.sellerId)
                && Objects.equals(itemName, that.itemName)
                && Objects.equals(brandName, that.brandName)
                && Objects.equals(price, that.price)
                && Objects.equals(images, that.images)
                && Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                sellerId, itemNo, itemName, brandName, price, images, freeShipping, createdAt);
    }

    @Override
    public String toString() {
        return "MiniShopCrawlData{"
                + "sellerId="
                + sellerId
                + ", itemNo="
                + itemNo
                + ", itemName='"
                + itemName
                + '\''
                + ", brandName='"
                + brandName
                + '\''
                + ", freeShipping="
                + freeShipping
                + ", createdAt="
                + createdAt
                + '}';
    }
}
