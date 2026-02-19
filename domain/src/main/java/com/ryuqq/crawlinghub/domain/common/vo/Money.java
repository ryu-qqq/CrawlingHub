package com.ryuqq.crawlinghub.domain.common.vo;

/** 금액을 나타내는 Value Object. 원화 기준으로 정수 단위만 지원합니다. */
public record Money(int value) {

    public Money {
        if (value < 0) {
            throw new IllegalArgumentException("금액은 0 이상이어야 합니다: " + value);
        }
    }

    public static Money of(int value) {
        return new Money(value);
    }

    public static Money zero() {
        return new Money(0);
    }

    public Money add(Money other) {
        return new Money(this.value + other.value);
    }

    public Money subtract(Money other) {
        int result = this.value - other.value;
        if (result < 0) {
            throw new IllegalArgumentException("금액은 음수가 될 수 없습니다");
        }
        return new Money(result);
    }

    public Money multiply(int multiplier) {
        if (multiplier < 0) {
            throw new IllegalArgumentException("곱하기 값은 0 이상이어야 합니다");
        }
        return new Money(this.value * multiplier);
    }

    public boolean isZero() {
        return value == 0;
    }

    public boolean isGreaterThan(Money other) {
        return this.value > other.value;
    }

    public boolean isGreaterThanOrEqual(Money other) {
        return this.value >= other.value;
    }

    public boolean isLessThan(Money other) {
        return this.value < other.value;
    }

    public boolean isLessThanOrEqual(Money other) {
        return this.value <= other.value;
    }

    /**
     * regularPrice 대비 currentPrice 할인율 계산.
     *
     * <p>regularPrice가 0이거나 currentPrice가 regularPrice 이상이면 0을 반환합니다.
     *
     * @param regular 정가
     * @param current 판매가
     * @return 할인율 (0~100)
     */
    public static int discountRate(Money regular, Money current) {
        if (regular.isZero() || current.isGreaterThanOrEqual(regular)) {
            return 0;
        }
        return (int) Math.round(((double) (regular.value - current.value) / regular.value) * 100);
    }
}
