package com.ryuqq.crawlinghub.domain.common.vo;

import java.util.Locale;
import java.util.regex.Pattern;

/** 이메일 주소를 나타내는 Value Object. */
public record Email(String value) {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    public Email {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("이메일은 필수입니다");
        }
        value = value.trim().toLowerCase(Locale.ROOT);
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("유효하지 않은 이메일 형식입니다: " + value);
        }
    }

    public static Email of(String value) {
        return new Email(value);
    }

    /** 이메일의 로컬 파트(@ 앞부분)를 반환합니다. */
    public String localPart() {
        return value.substring(0, value.indexOf('@'));
    }

    /** 이메일의 도메인 파트(@ 뒷부분)를 반환합니다. */
    public String domainPart() {
        return value.substring(value.indexOf('@') + 1);
    }
}
