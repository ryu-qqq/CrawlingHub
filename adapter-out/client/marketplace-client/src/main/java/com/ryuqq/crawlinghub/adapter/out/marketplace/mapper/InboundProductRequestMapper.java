package com.ryuqq.crawlinghub.adapter.out.marketplace.mapper;

import com.ryuqq.crawlinghub.adapter.out.marketplace.dto.request.OptionType;
import com.ryuqq.crawlinghub.adapter.out.marketplace.dto.request.ReceiveInboundProductRequest;
import com.ryuqq.crawlinghub.adapter.out.marketplace.dto.request.ReceiveInboundProductRequest.DescriptionRequest;
import com.ryuqq.crawlinghub.adapter.out.marketplace.dto.request.ReceiveInboundProductRequest.ImageRequest;
import com.ryuqq.crawlinghub.adapter.out.marketplace.dto.request.ReceiveInboundProductRequest.OptionGroupRequest;
import com.ryuqq.crawlinghub.adapter.out.marketplace.dto.request.ReceiveInboundProductRequest.OptionValueRequest;
import com.ryuqq.crawlinghub.adapter.out.marketplace.dto.request.ReceiveInboundProductRequest.ProductRequest;
import com.ryuqq.crawlinghub.adapter.out.marketplace.dto.request.ReceiveInboundProductRequest.SelectedOptionRequest;
import com.ryuqq.crawlinghub.adapter.out.marketplace.dto.request.UpdateDescriptionRequest;
import com.ryuqq.crawlinghub.adapter.out.marketplace.dto.request.UpdateImagesRequest;
import com.ryuqq.crawlinghub.adapter.out.marketplace.dto.request.UpdatePriceRequest;
import com.ryuqq.crawlinghub.adapter.out.marketplace.dto.request.UpdateProductsRequest;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.vo.ProductImages;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOption;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOptions;
import com.ryuqq.crawlinghub.domain.product.vo.ProductPrice;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Domain → MarketPlace API 요청 DTO 변환 Mapper
 *
 * <p>CrawledProduct, Seller, Outbox 정보를 MarketPlace API 스펙에 맞는 요청 DTO로 변환합니다.
 */
@Component
public class InboundProductRequestMapper {

    private static final long INBOUND_SOURCE_ID = 1L;
    private static final String COLOR_GROUP_NAME = "색상";
    private static final String SIZE_GROUP_NAME = "사이즈";

    public ReceiveInboundProductRequest toReceiveRequest(
            CrawledProductSyncOutbox outbox, CrawledProduct product, Seller seller) {

        Long omsSellerId = seller.getOmsSellerId();
        if (omsSellerId == null) {
            throw new IllegalStateException(
                    "omsSellerId가 설정되지 않은 셀러입니다: sellerId=" + seller.getSellerIdValue());
        }

        ResolvedPrice resolvedPrice = resolvePrice(product.getPrice());

        return new ReceiveInboundProductRequest(
                INBOUND_SOURCE_ID,
                String.valueOf(outbox.getItemNo()),
                product.getItemName(),
                String.valueOf(product.getBrandCode()),
                extractCategoryCode(product),
                omsSellerId,
                resolvedPrice.regularPrice(),
                resolvedPrice.currentPrice(),
                resolveOptionType(product).name(),
                toImageRequests(product.getImages()),
                toOptionGroupRequests(product.getOptions()),
                toProductRequests(product.getOptions(), resolvedPrice),
                new DescriptionRequest(product.getDescriptionMarkUp()));
    }

    public UpdatePriceRequest toUpdatePriceRequest(CrawledProduct product) {
        ResolvedPrice resolvedPrice = resolvePrice(product.getPrice());
        return new UpdatePriceRequest(resolvedPrice.regularPrice(), resolvedPrice.currentPrice());
    }

    public UpdateImagesRequest toUpdateImagesRequest(CrawledProduct product) {
        ProductImages images = product.getImages();
        if (images == null || images.isEmpty()) {
            return new UpdateImagesRequest(List.of());
        }

        List<UpdateImagesRequest.ImageEntry> entries = new ArrayList<>();

        AtomicInteger order = new AtomicInteger(0);
        images.getThumbnails()
                .forEach(
                        img ->
                                entries.add(
                                        new UpdateImagesRequest.ImageEntry(
                                                "THUMBNAIL",
                                                img.getEffectiveUrl(),
                                                order.getAndIncrement())));

        images.getDescriptionImages()
                .forEach(
                        img ->
                                entries.add(
                                        new UpdateImagesRequest.ImageEntry(
                                                "DETAIL",
                                                img.getEffectiveUrl(),
                                                order.getAndIncrement())));

        return new UpdateImagesRequest(entries);
    }

    public UpdateDescriptionRequest toUpdateDescriptionRequest(CrawledProduct product) {
        return new UpdateDescriptionRequest(product.getDescriptionMarkUp());
    }

    public UpdateProductsRequest toUpdateProductsRequest(CrawledProduct product) {
        ResolvedPrice resolvedPrice = resolvePrice(product.getPrice());
        ProductOptions options = product.getOptions();

        List<UpdateProductsRequest.OptionGroupRequest> optionGroups =
                toUpdateOptionGroupRequests(options);
        List<UpdateProductsRequest.ProductDataRequest> products =
                toProductDataRequests(options, resolvedPrice);

        return new UpdateProductsRequest(optionGroups, products);
    }

    public long getInboundSourceId() {
        return INBOUND_SOURCE_ID;
    }

    public String getExternalProductCode(CrawledProductSyncOutbox outbox) {
        return String.valueOf(outbox.getItemNo());
    }

    // --- Image 변환 ---

    private List<ImageRequest> toImageRequests(ProductImages images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }

        List<ImageRequest> requests = new ArrayList<>();

        AtomicInteger order = new AtomicInteger(0);
        images.getThumbnails()
                .forEach(
                        img ->
                                requests.add(
                                        new ImageRequest(
                                                "THUMBNAIL",
                                                img.getEffectiveUrl(),
                                                order.getAndIncrement())));

        images.getDescriptionImages()
                .forEach(
                        img ->
                                requests.add(
                                        new ImageRequest(
                                                "DETAIL",
                                                img.getEffectiveUrl(),
                                                order.getAndIncrement())));

        return requests;
    }

    // --- OptionGroup 변환 ---

    private List<OptionGroupRequest> toOptionGroupRequests(ProductOptions options) {
        if (options == null || options.isEmpty()) {
            return List.of();
        }

        List<OptionGroupRequest> groups = new ArrayList<>();

        List<String> colors =
                options.getDistinctColors().stream()
                        .filter(c -> c != null && !c.isEmpty())
                        .collect(Collectors.toList());
        if (!colors.isEmpty()) {
            AtomicInteger order = new AtomicInteger(0);
            List<OptionValueRequest> values =
                    colors.stream()
                            .map(c -> new OptionValueRequest(c, order.getAndIncrement()))
                            .collect(Collectors.toList());
            groups.add(new OptionGroupRequest(COLOR_GROUP_NAME, "PREDEFINED", values));
        }

        List<String> sizes =
                options.getDistinctSizes().stream()
                        .filter(s -> s != null && !s.isEmpty())
                        .collect(Collectors.toList());
        if (!sizes.isEmpty()) {
            AtomicInteger order = new AtomicInteger(0);
            List<OptionValueRequest> values =
                    sizes.stream()
                            .map(s -> new OptionValueRequest(s, order.getAndIncrement()))
                            .collect(Collectors.toList());
            groups.add(new OptionGroupRequest(SIZE_GROUP_NAME, "PREDEFINED", values));
        }

        return groups;
    }

    private List<UpdateProductsRequest.OptionGroupRequest> toUpdateOptionGroupRequests(
            ProductOptions options) {
        if (options == null || options.isEmpty()) {
            return List.of();
        }

        List<UpdateProductsRequest.OptionGroupRequest> groups = new ArrayList<>();

        List<String> colors =
                options.getDistinctColors().stream()
                        .filter(c -> c != null && !c.isEmpty())
                        .collect(Collectors.toList());
        if (!colors.isEmpty()) {
            AtomicInteger order = new AtomicInteger(0);
            List<UpdateProductsRequest.OptionValueRequest> values =
                    colors.stream()
                            .map(
                                    c ->
                                            new UpdateProductsRequest.OptionValueRequest(
                                                    c, order.getAndIncrement()))
                            .collect(Collectors.toList());
            groups.add(
                    new UpdateProductsRequest.OptionGroupRequest(
                            COLOR_GROUP_NAME, "PREDEFINED", values));
        }

        List<String> sizes =
                options.getDistinctSizes().stream()
                        .filter(s -> s != null && !s.isEmpty())
                        .collect(Collectors.toList());
        if (!sizes.isEmpty()) {
            AtomicInteger order = new AtomicInteger(0);
            List<UpdateProductsRequest.OptionValueRequest> values =
                    sizes.stream()
                            .map(
                                    s ->
                                            new UpdateProductsRequest.OptionValueRequest(
                                                    s, order.getAndIncrement()))
                            .collect(Collectors.toList());
            groups.add(
                    new UpdateProductsRequest.OptionGroupRequest(
                            SIZE_GROUP_NAME, "PREDEFINED", values));
        }

        return groups;
    }

    // --- Product 변환 ---

    private List<ProductRequest> toProductRequests(
            ProductOptions options, ResolvedPrice resolvedPrice) {
        if (options == null || options.isEmpty()) {
            return List.of();
        }

        AtomicInteger order = new AtomicInteger(0);
        return options.getAll().stream()
                .map(opt -> toProductRequest(opt, resolvedPrice, order.getAndIncrement()))
                .collect(Collectors.toList());
    }

    private ProductRequest toProductRequest(
            ProductOption option, ResolvedPrice resolvedPrice, int sortOrder) {
        List<SelectedOptionRequest> selectedOptions = new ArrayList<>();

        if (option.color() != null && !option.color().isEmpty()) {
            selectedOptions.add(new SelectedOptionRequest(COLOR_GROUP_NAME, option.color()));
        }
        if (option.size() != null && !option.size().isEmpty()) {
            selectedOptions.add(new SelectedOptionRequest(SIZE_GROUP_NAME, option.size()));
        }

        return new ProductRequest(
                String.valueOf(option.optionNo()),
                resolvedPrice.regularPrice(),
                resolvedPrice.currentPrice(),
                option.stock(),
                sortOrder,
                selectedOptions);
    }

    private List<UpdateProductsRequest.ProductDataRequest> toProductDataRequests(
            ProductOptions options, ResolvedPrice resolvedPrice) {
        if (options == null || options.isEmpty()) {
            return List.of();
        }

        AtomicInteger order = new AtomicInteger(0);
        return options.getAll().stream()
                .map(opt -> toProductDataRequest(opt, resolvedPrice, order.getAndIncrement()))
                .collect(Collectors.toList());
    }

    private UpdateProductsRequest.ProductDataRequest toProductDataRequest(
            ProductOption option, ResolvedPrice resolvedPrice, int sortOrder) {
        List<UpdateProductsRequest.SelectedOptionRequest> selectedOptions = new ArrayList<>();

        if (option.color() != null && !option.color().isEmpty()) {
            selectedOptions.add(
                    new UpdateProductsRequest.SelectedOptionRequest(
                            COLOR_GROUP_NAME, option.color()));
        }
        if (option.size() != null && !option.size().isEmpty()) {
            selectedOptions.add(
                    new UpdateProductsRequest.SelectedOptionRequest(
                            SIZE_GROUP_NAME, option.size()));
        }

        return new UpdateProductsRequest.ProductDataRequest(
                String.valueOf(option.optionNo()),
                resolvedPrice.regularPrice(),
                resolvedPrice.currentPrice(),
                option.stock(),
                sortOrder,
                selectedOptions);
    }

    // --- 공통 유틸 ---

    private String extractCategoryCode(CrawledProduct product) {
        if (product.getCategory() == null) {
            return "";
        }
        return product.getCategory().toExternalCategoryCode();
    }

    private OptionType resolveOptionType(CrawledProduct product) {
        if (product.getOptions() == null || product.getOptions().isEmpty()) {
            return OptionType.NONE;
        }

        boolean hasColor =
                product.getOptions().getDistinctColors().stream()
                        .anyMatch(c -> c != null && !c.isEmpty());
        boolean hasSize =
                product.getOptions().getDistinctSizes().stream()
                        .anyMatch(s -> s != null && !s.isEmpty());

        if (hasColor && hasSize) {
            return OptionType.COMBINATION;
        }
        if (hasColor || hasSize) {
            return OptionType.SINGLE;
        }
        return OptionType.NONE;
    }

    ResolvedPrice resolvePrice(ProductPrice price) {
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

    record ResolvedPrice(int regularPrice, int currentPrice) {}
}
