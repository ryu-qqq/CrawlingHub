package com.ryuqq.crawlinghub.adapter.in.rest.seller;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 크롤링 주기 타입 검증 어노테이션
 * <p>
 * intervalType이 유효한 CrawlIntervalType Enum 값인지 검증합니다.
 * 허용 값: HOURLY, DAILY, WEEKLY
 * </p>
 *
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = IntervalTypeValidator.class)
@Documented
public @interface ValidIntervalType {

    /**
     * 검증 실패 시 메시지
     *
     * @return 에러 메시지
     */
    String message() default "intervalType은 HOURLY, DAILY, WEEKLY 중 하나여야 합니다";

    /**
     * 그룹
     *
     * @return 그룹 배열
     */
    Class<?>[] groups() default {};

    /**
     * Payload
     *
     * @return Payload 배열
     */
    Class<? extends Payload>[] payload() default {};
}
