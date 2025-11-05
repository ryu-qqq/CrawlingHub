package com.ryuqq.crawlinghub.domain.product;

import java.util.Objects;

/**
 * 상품 데이터 Value Object (JSON 원본 데이터 래퍼)
 */
public class ProductData {

    private final String jsonData;

    private ProductData(String jsonData) {
        validateJsonData(jsonData);
        this.jsonData = jsonData;
    }

    public static ProductData of(String jsonData) {
        return new ProductData(jsonData);
    }

    private static void validateJsonData(String jsonData) {
        if (jsonData == null || jsonData.isBlank()) {
            throw new IllegalArgumentException("JSON 데이터는 필수입니다");
        }
    }

    public String getValue() {
        return jsonData;
    }

    public boolean isSameAs(ProductData other) {
        if (other == null) {
            return false;
        }
        return this.jsonData.equals(other.jsonData);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProductData that = (ProductData) o;
        return Objects.equals(jsonData, that.jsonData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jsonData);
    }

    @Override
    public String toString() {
        return jsonData;
    }
}
