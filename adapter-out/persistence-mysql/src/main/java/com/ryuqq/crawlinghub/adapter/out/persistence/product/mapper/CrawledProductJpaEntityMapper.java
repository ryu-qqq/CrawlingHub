package com.ryuqq.crawlinghub.adapter.out.persistence.product.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.entity.CrawledProductJpaEntity;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.CrawlCompletionStatus;
import com.ryuqq.crawlinghub.domain.product.vo.ProductCategory;
import com.ryuqq.crawlinghub.domain.product.vo.ProductImage;
import com.ryuqq.crawlinghub.domain.product.vo.ProductImages;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOption;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOptions;
import com.ryuqq.crawlinghub.domain.product.vo.ProductPrice;
import com.ryuqq.crawlinghub.domain.product.vo.ShippingInfo;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * CrawledProductJpaEntityMapper - Entity ↔ Domain 변환 Mapper
 *
 * <p>Persistence Layer의 JPA Entity와 Domain Layer의 Domain 객체 간 변환을 담당합니다.
 *
 * <p><strong>JSON 변환 책임:</strong>
 *
 * <ul>
 *   <li>ProductImages, ProductOptions, ProductCategory, ShippingInfo ↔ JSON
 *   <li>Jackson ObjectMapper 사용
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawledProductJpaEntityMapper {

    private static final Logger log = LoggerFactory.getLogger(CrawledProductJpaEntityMapper.class);

    private final ObjectMapper objectMapper;

    public CrawledProductJpaEntityMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Domain → Entity 변환
     *
     * @param domain CrawledProduct 도메인
     * @return CrawledProductJpaEntity
     */
    public CrawledProductJpaEntity toEntity(CrawledProduct domain) {
        ProductPrice price = domain.getPrice();
        CrawlCompletionStatus completionStatus = domain.getCrawlCompletionStatus();

        return CrawledProductJpaEntity.of(
                domain.getIdValue(),
                domain.getSellerIdValue(),
                domain.getItemNo(),
                domain.getItemName(),
                domain.getBrandName(),
                price != null ? (long) price.originalPrice() : null,
                price != null ? (long) price.discountPrice() : null,
                price != null ? price.discountRate() : null,
                toImagesJson(domain.getImages()),
                domain.isFreeShipping(),
                toCategoryJson(domain.getCategory()),
                toShippingInfoJson(domain.getShippingInfo()),
                domain.getDescriptionMarkUp(),
                domain.getItemStatus(),
                domain.getOriginCountry(),
                domain.getShippingLocation(),
                toOptionsJson(domain.getOptions()),
                completionStatus != null ? completionStatus.miniShopCrawledAt() : null,
                completionStatus != null ? completionStatus.detailCrawledAt() : null,
                completionStatus != null ? completionStatus.optionCrawledAt() : null,
                domain.getExternalProductId(),
                domain.getLastSyncedAt(),
                domain.isNeedsSync(),
                domain.getCreatedAt(),
                domain.getUpdatedAt());
    }

    /**
     * Entity → Domain 변환
     *
     * @param entity CrawledProductJpaEntity
     * @return CrawledProduct 도메인
     */
    public CrawledProduct toDomain(CrawledProductJpaEntity entity) {
        ProductPrice price = toProductPrice(entity);
        ProductImages images = fromImagesJson(entity.getImagesJson());
        ProductCategory category = fromCategoryJson(entity.getCategoryJson());
        ShippingInfo shippingInfo = fromShippingInfoJson(entity.getShippingInfoJson());
        ProductOptions options = fromOptionsJson(entity.getOptionsJson());
        CrawlCompletionStatus completionStatus = new CrawlCompletionStatus(
                entity.getMiniShopCrawledAt(),
                entity.getDetailCrawledAt(),
                entity.getOptionCrawledAt());

        return CrawledProduct.reconstitute(
                CrawledProductId.of(entity.getId()),
                SellerId.of(entity.getSellerId()),
                entity.getItemNo(),
                entity.getItemName(),
                entity.getBrandName(),
                price,
                images,
                entity.isFreeShipping(),
                category,
                shippingInfo,
                entity.getDescriptionMarkUp(),
                entity.getItemStatus(),
                entity.getOriginCountry(),
                entity.getShippingLocation(),
                options,
                completionStatus,
                entity.getExternalProductId(),
                entity.getLastSyncedAt(),
                entity.isNeedsSync(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    // === JSON 직렬화 ===

    private String toImagesJson(ProductImages images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(images.getAll());
        } catch (JsonProcessingException e) {
            log.error("ProductImages JSON 직렬화 실패", e);
            return null;
        }
    }

    private String toCategoryJson(ProductCategory category) {
        if (category == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(category);
        } catch (JsonProcessingException e) {
            log.error("ProductCategory JSON 직렬화 실패", e);
            return null;
        }
    }

    private String toShippingInfoJson(ShippingInfo shippingInfo) {
        if (shippingInfo == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(shippingInfo);
        } catch (JsonProcessingException e) {
            log.error("ShippingInfo JSON 직렬화 실패", e);
            return null;
        }
    }

    private String toOptionsJson(ProductOptions options) {
        if (options == null || options.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(options.getAll());
        } catch (JsonProcessingException e) {
            log.error("ProductOptions JSON 직렬화 실패", e);
            return null;
        }
    }

    // === JSON 역직렬화 ===

    private ProductImages fromImagesJson(String json) {
        if (json == null || json.isBlank()) {
            return ProductImages.empty();
        }
        try {
            List<ProductImage> images = objectMapper.readValue(json, new TypeReference<List<ProductImage>>() {});
            return ProductImages.of(images);
        } catch (JsonProcessingException e) {
            log.error("ProductImages JSON 역직렬화 실패: {}", json, e);
            return ProductImages.empty();
        }
    }

    private ProductCategory fromCategoryJson(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, ProductCategory.class);
        } catch (JsonProcessingException e) {
            log.error("ProductCategory JSON 역직렬화 실패: {}", json, e);
            return null;
        }
    }

    private ShippingInfo fromShippingInfoJson(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, ShippingInfo.class);
        } catch (JsonProcessingException e) {
            log.error("ShippingInfo JSON 역직렬화 실패: {}", json, e);
            return null;
        }
    }

    private ProductOptions fromOptionsJson(String json) {
        if (json == null || json.isBlank()) {
            return ProductOptions.empty();
        }
        try {
            List<ProductOption> options = objectMapper.readValue(json, new TypeReference<List<ProductOption>>() {});
            return ProductOptions.of(options);
        } catch (JsonProcessingException e) {
            log.error("ProductOptions JSON 역직렬화 실패: {}", json, e);
            return ProductOptions.empty();
        }
    }

    // === 가격 변환 ===

    private ProductPrice toProductPrice(CrawledProductJpaEntity entity) {
        Long originalPrice = entity.getOriginalPrice();
        Long discountPrice = entity.getDiscountPrice();
        Integer discountRate = entity.getDiscountRate();

        if (originalPrice == null && discountPrice == null) {
            return null;
        }

        int origPrice = originalPrice != null ? originalPrice.intValue() : 0;
        int discPrice = discountPrice != null ? discountPrice.intValue() : origPrice;
        int discRate = discountRate != null ? discountRate : 0;

        return ProductPrice.of(
                discPrice,
                origPrice,
                origPrice,
                discPrice,
                discRate,
                discRate);
    }
}
