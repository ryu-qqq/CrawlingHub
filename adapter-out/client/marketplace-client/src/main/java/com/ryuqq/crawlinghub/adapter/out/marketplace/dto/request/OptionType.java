package com.ryuqq.crawlinghub.adapter.out.marketplace.dto.request;

/**
 * 외부몰 상품 옵션 유형
 *
 * @author development-team
 * @since 1.0.0
 */
public enum OptionType {
    NONE("옵션 없음"),
    SINGLE("단일 옵션"),
    COMBINATION("조합 옵션");

    private final String description;

    OptionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
