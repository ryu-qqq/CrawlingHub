package com.ryuqq.crawlinghub.domain.common.vo;

/** 주소를 나타내는 Value Object. */
public record Address(String zipcode, String line1, String line2) {

    public Address {
        if (zipcode == null || zipcode.isBlank()) {
            throw new IllegalArgumentException("우편번호는 필수입니다");
        }
        if (line1 == null || line1.isBlank()) {
            throw new IllegalArgumentException("기본 주소는 필수입니다");
        }
        zipcode = zipcode.trim();
        line1 = line1.trim();
        line2 = line2 != null ? line2.trim() : null;
    }

    public static Address of(String zipcode, String line1, String line2) {
        return new Address(zipcode, line1, line2);
    }

    public static Address of(String zipcode, String line1) {
        return new Address(zipcode, line1, null);
    }

    /** 전체 주소 문자열을 반환합니다. */
    public String fullAddress() {
        if (line2 == null || line2.isBlank()) {
            return line1;
        }
        return line1 + " " + line2;
    }
}
