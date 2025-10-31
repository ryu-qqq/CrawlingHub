package com.ryuqq.crawlinghub.adapter.in.rest.seller;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * IntervalType 검증기
 * <p>
 * 요청된 intervalType 문자열이 유효한 CrawlIntervalType Enum 값인지 검증합니다.
 * </p>
 *
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
public class IntervalTypeValidator implements ConstraintValidator<ValidIntervalType, String> {

    private static final Set<String> VALID_TYPES = Arrays.stream(CrawlIntervalType.values())
            .map(Enum::name)
            .collect(Collectors.toSet());

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        return VALID_TYPES.contains(value.trim().toUpperCase(Locale.ROOT));
    }
}
