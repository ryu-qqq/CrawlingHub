package com.ryuqq.crawlinghub.adapter.out.marketplace.mapper;

import com.ryuqq.crawlinghub.adapter.out.marketplace.dto.request.CreateProductPayload;
import com.ryuqq.crawlinghub.adapter.out.marketplace.dto.request.ProductImageListPayload;
import com.ryuqq.crawlinghub.adapter.out.marketplace.dto.request.ProductImagePayload;
import com.ryuqq.crawlinghub.adapter.out.marketplace.dto.request.ProductOptionListPayload;
import com.ryuqq.crawlinghub.adapter.out.marketplace.dto.request.ProductOptionPayload;
import com.ryuqq.crawlinghub.adapter.out.marketplace.dto.request.ProductShippingPayload;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.vo.ProductCategory;
import com.ryuqq.crawlinghub.domain.product.vo.ProductImage;
import com.ryuqq.crawlinghub.domain.product.vo.ProductImages;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOption;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOptions;
import com.ryuqq.crawlinghub.domain.product.vo.ProductPrice;
import com.ryuqq.crawlinghub.domain.product.vo.ShippingInfo;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * CrawledProduct → CreateProductPayload 변환 Mapper
 *
 * <p>도메인 Aggregate를 외부몰 API 페이로드로 변환합니다.
 *
 * <p><strong>변환 책임:</strong>
 *
 * <ul>
 *   <li>CrawledProduct → CreateProductPayload (전체 페이로드)
 *   <li>ProductImages → ProductImageListPayload (이미지 목록)
 *   <li>ProductOptions → ProductOptionListPayload (옵션 목록)
 *   <li>ShippingInfo → ProductShippingPayload (배송 정보)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CreateProductPayloadMapper {

    /**
     * CrawledProduct → CreateProductPayload 변환
     *
     * @param product CrawledProduct Aggregate
     * @return CreateProductPayload
     */
    public CreateProductPayload toPayload(CrawledProduct product) {
        ResolvedPrice resolvedPrice = resolvePrice(product.getPrice());
        return new CreateProductPayload(
                product.getItemName(),
                product.getBrandName(),
                extractCategoryCode(product.getCategory()),
                extractCategoryName(product.getCategory()),
                resolvedPrice.regularPrice(),
                resolvedPrice.currentPrice(),
                extractDiscountRate(product.getPrice()),
                product.getItemStatus(),
                product.getOriginCountry(),
                product.getDescriptionMarkUp(),
                product.isFreeShipping(),
                toImageListPayload(product.getImages()),
                toOptionListPayload(product.getOptions()),
                toShippingPayload(product.getShippingInfo()));
    }

    /**
     * ProductImages → ProductImageListPayload 변환
     *
     * <p>이미지 수정 API에서도 재사용 가능합니다.
     *
     * @param images ProductImages
     * @return ProductImageListPayload
     */
    public ProductImageListPayload toImageListPayload(ProductImages images) {
        if (images == null || images.isEmpty()) {
            return new ProductImageListPayload(Collections.emptyList(), Collections.emptyList());
        }

        List<ProductImagePayload> thumbnails =
                images.getThumbnails().stream()
                        .map(this::toImagePayload)
                        .collect(Collectors.toList());

        List<ProductImagePayload> descriptionImages =
                images.getDescriptionImages().stream()
                        .map(this::toImagePayload)
                        .collect(Collectors.toList());

        return new ProductImageListPayload(thumbnails, descriptionImages);
    }

    /**
     * ProductOptions → ProductOptionListPayload 변환
     *
     * <p>옵션/재고 수정 API에서도 재사용 가능합니다.
     *
     * @param options ProductOptions
     * @return ProductOptionListPayload
     */
    public ProductOptionListPayload toOptionListPayload(ProductOptions options) {
        if (options == null || options.isEmpty()) {
            return new ProductOptionListPayload(Collections.emptyList(), 0);
        }

        List<ProductOptionPayload> optionPayloads =
                options.getAll().stream().map(this::toOptionPayload).collect(Collectors.toList());

        return new ProductOptionListPayload(optionPayloads, options.getTotalStock());
    }

    /**
     * ShippingInfo → ProductShippingPayload 변환
     *
     * @param shippingInfo ShippingInfo
     * @return ProductShippingPayload (null이면 null 반환)
     */
    public ProductShippingPayload toShippingPayload(ShippingInfo shippingInfo) {
        if (shippingInfo == null) {
            return null;
        }

        return new ProductShippingPayload(
                shippingInfo.shippingType(),
                shippingInfo.shippingFee(),
                shippingInfo.shippingFeeType(),
                shippingInfo.averageDeliveryDays(),
                shippingInfo.freeShipping());
    }

    private ProductImagePayload toImagePayload(ProductImage image) {
        return new ProductImagePayload(
                image.getEffectiveUrl(), image.imageType().name(), image.displayOrder());
    }

    private ProductOptionPayload toOptionPayload(ProductOption option) {
        return new ProductOptionPayload(
                option.optionNo(),
                option.color(),
                option.size(),
                option.stock(),
                option.sizeGuide());
    }

    private String extractCategoryCode(ProductCategory category) {
        return category != null ? category.mediumCategoryCode() : null;
    }

    private String extractCategoryName(ProductCategory category) {
        return category != null ? category.getFullPath() : null;
    }

    /**
     * ProductPrice → regularPrice, currentPrice 결정
     *
     * <p>Detail 크롤링 필드 매핑:
     *
     * <ul>
     *   <li>normalPrice → price.normalPrice()
     *   <li>sellingPrice → price.price()
     *   <li>discountPrice → price.appPrice()
     * </ul>
     *
     * <p>가격 결정 규칙 (example.txt 기준):
     *
     * <ol>
     *   <li>discountPrice만 존재 → regular=discount, current=discount
     *   <li>discount+normal 존재 → regular=normal, current=discount
     *   <li>discount+selling 존재 → regular=selling, current=selling
     *   <li>모두 존재 → regular=normal, current=selling
     * </ol>
     */
    private ResolvedPrice resolvePrice(ProductPrice price) {
        if (price == null) {
            return new ResolvedPrice(0, 0);
        }

        int normalPrice = price.normalPrice();
        int sellingPrice = price.price();
        int discountPrice = price.appPrice();

        if (discountPrice > 0 && normalPrice == 0 && sellingPrice == 0) {
            return new ResolvedPrice(discountPrice, discountPrice);
        }
        if (discountPrice > 0 && normalPrice > 0 && sellingPrice == 0) {
            return new ResolvedPrice(normalPrice, discountPrice);
        }
        if (discountPrice > 0 && normalPrice == 0 && sellingPrice > 0) {
            return new ResolvedPrice(sellingPrice, sellingPrice);
        }
        if (normalPrice > 0 && sellingPrice > 0 && discountPrice > 0) {
            return new ResolvedPrice(normalPrice, sellingPrice);
        }
        return new ResolvedPrice(0, 0);
    }

    private record ResolvedPrice(int regularPrice, int currentPrice) {}

    private int extractDiscountRate(ProductPrice price) {
        return price != null ? price.discountRate() : 0;
    }
}
