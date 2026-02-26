package com.ryuqq.crawlinghub.adapter.out.marketplace.dto.request;

import java.util.List;

/**
 * 상품 + 옵션 일괄 수정 요청 DTO
 *
 * <p>MarketPlace PATCH
 * /api/v1/market/inbound/products/{inboundSourceId}/{externalProductCode}/products
 */
public record UpdateProductsRequest(
        List<OptionGroupRequest> optionGroups, List<ProductDataRequest> products) {

    public UpdateProductsRequest {
        optionGroups = optionGroups != null ? List.copyOf(optionGroups) : List.of();
        products = products != null ? List.copyOf(products) : List.of();
    }

    public record OptionGroupRequest(
            String optionGroupName, String inputType, List<OptionValueRequest> optionValues) {

        public OptionGroupRequest {
            optionValues = optionValues != null ? List.copyOf(optionValues) : List.of();
        }
    }

    public record OptionValueRequest(String optionValueName, int sortOrder) {}

    public record ProductDataRequest(
            String skuCode,
            int regularPrice,
            int currentPrice,
            int stockQuantity,
            int sortOrder,
            List<SelectedOptionRequest> selectedOptions) {

        public ProductDataRequest {
            selectedOptions = selectedOptions != null ? List.copyOf(selectedOptions) : List.of();
        }
    }

    public record SelectedOptionRequest(String optionGroupName, String optionValueName) {}
}
