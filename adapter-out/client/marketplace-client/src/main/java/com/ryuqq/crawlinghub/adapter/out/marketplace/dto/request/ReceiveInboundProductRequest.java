package com.ryuqq.crawlinghub.adapter.out.marketplace.dto.request;

import java.util.List;

/**
 * 인바운드 상품 수신 요청 DTO
 *
 * <p>MarketPlace POST /api/v1/market/inbound/products 엔드포인트에 대응합니다.
 */
public record ReceiveInboundProductRequest(
        long inboundSourceId,
        String externalProductCode,
        String productName,
        String externalBrandCode,
        String externalCategoryCode,
        long sellerId,
        int regularPrice,
        int currentPrice,
        String optionType,
        List<ImageRequest> images,
        List<OptionGroupRequest> optionGroups,
        List<ProductRequest> products,
        DescriptionRequest description) {

    public ReceiveInboundProductRequest {
        images = images != null ? List.copyOf(images) : List.of();
        optionGroups = optionGroups != null ? List.copyOf(optionGroups) : List.of();
        products = products != null ? List.copyOf(products) : List.of();
    }

    public record ImageRequest(String imageType, String originUrl, int sortOrder) {}

    public record OptionGroupRequest(
            String optionGroupName, String inputType, List<OptionValueRequest> optionValues) {

        public OptionGroupRequest {
            optionValues = optionValues != null ? List.copyOf(optionValues) : List.of();
        }
    }

    public record OptionValueRequest(String optionValueName, int sortOrder) {}

    public record ProductRequest(
            String skuCode,
            int regularPrice,
            int currentPrice,
            int stockQuantity,
            int sortOrder,
            List<SelectedOptionRequest> selectedOptions) {

        public ProductRequest {
            selectedOptions = selectedOptions != null ? List.copyOf(selectedOptions) : List.of();
        }
    }

    public record SelectedOptionRequest(String optionGroupName, String optionValueName) {}

    public record DescriptionRequest(String content) {}
}
