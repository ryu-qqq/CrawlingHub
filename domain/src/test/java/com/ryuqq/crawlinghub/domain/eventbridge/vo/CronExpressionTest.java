package com.ryuqq.crawlinghub.domain.eventbridge.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ryuqq.crawlinghub.domain.eventbridge.exception.InvalidCronExpressionException;
import com.ryuqq.crawlinghub.domain.fixture.eventbridge.CronExpressionFixture;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("CronExpression Value Object 테스트")
class CronExpressionTest {

    @Test
    @DisplayName("AWS EventBridge 형식의 Cron Expression을 생성할 수 있다")
    void shouldCreateCronExpressionWithValidAwsFormat() {
        // given
        String validExpression = CronExpressionFixture.aValidCronExpressionValue();

        // when
        CronExpression cronExpression = CronExpression.of(validExpression);

        // then
        assertAll(
            () -> assertNotNull(cronExpression),
            () -> assertEquals(validExpression, cronExpression.value())
        );
    }

    @Test
    @DisplayName("유효하지 않은 형식이면 InvalidCronExpressionException을 발생시킨다")
    void shouldThrowExceptionWhenInvalidFormat() {
        // given
        String invalidExpression = CronExpressionFixture.anInvalidCronExpressionValue();

        // when & then
        assertThrows(
            InvalidCronExpressionException.class,
            () -> CronExpression.of(invalidExpression)
        );
    }

    @Test
    @DisplayName("1시간 미만 간격이면 InvalidCronExpressionException을 발생시킨다")
    void shouldThrowExceptionWhenIntervalLessThanOneHour() {
        // given
        String invalidIntervalExpression = CronExpressionFixture.anExpressionWithLessThanOneHourInterval();

        // when & then
        assertThrows(
            InvalidCronExpressionException.class,
            () -> CronExpression.of(invalidIntervalExpression)
        );
    }

    @Test
    @DisplayName("null 값을 전달하면 InvalidCronExpressionException을 발생시킨다")
    void shouldThrowExceptionWhenExpressionIsNull() {
        assertThrows(
            InvalidCronExpressionException.class,
            () -> CronExpression.of(null)
        );
    }

    @Test
    @DisplayName("공백 문자열은 허용되지 않는다")
    void shouldThrowExceptionWhenExpressionIsBlank() {
        assertThrows(
            InvalidCronExpressionException.class,
            () -> CronExpression.of("   ")
        );
    }
}

