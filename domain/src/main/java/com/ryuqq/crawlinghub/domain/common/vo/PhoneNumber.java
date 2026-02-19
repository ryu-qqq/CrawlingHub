package com.ryuqq.crawlinghub.domain.common.vo;

import java.util.regex.Pattern;

/** 전화번호를 나타내는 Value Object. */
public record PhoneNumber(String value) {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9\\-]{9,20}$");

    public PhoneNumber {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("전화번호는 필수입니다");
        }
        value = value.trim().replaceAll("\\s+", "");
        if (!PHONE_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("유효하지 않은 전화번호 형식입니다: " + value);
        }
    }

    public static PhoneNumber of(String value) {
        return new PhoneNumber(value);
    }

    /** 하이픈이 제거된 숫자만 반환합니다. */
    public String digitsOnly() {
        return value.replaceAll("-", "");
    }
}
