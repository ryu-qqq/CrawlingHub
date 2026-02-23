package com.ryuqq.crawlinghub.domain.seller.id;

public record SellerId(Long value) {

    public static SellerId forNew() {
        return new SellerId(null);
    }

    public static SellerId of(Long value) {
        if (value == null) {
            throw new IllegalArgumentException("SellerId value is null");
        }
        return new SellerId(value);
    }

    /**
     * 신규 ID 여부 확인 (미할당 상태)
     *
     * @return ID가 미할당이면 true
     */
    public boolean isNew() {
        return value == null;
    }
}
