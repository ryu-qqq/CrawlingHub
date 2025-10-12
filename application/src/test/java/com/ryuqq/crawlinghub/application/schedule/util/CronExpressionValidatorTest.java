package com.ryuqq.crawlinghub.application.schedule.util;

import com.ryuqq.crawlinghub.application.schedule.usecase.InvalidCronExpressionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test for CronExpressionValidator
 *
 * Note: Spring CronExpression uses 6-field format: second minute hour day month dayOfWeek
 * This is different from standard Unix cron (5-field) format
 */
class CronExpressionValidatorTest {

    @Test
    @DisplayName("validate 성공 - 유효한 Spring cron 표현식 (6필드)")
    void validateSuccess() {
        // given
        String validCron = "0 0 9 * * *"; // Spring: 초 분 시 일 월 요일

        // when & then
        CronExpressionValidator.validate(validCron);
    }

    @Test
    @DisplayName("validate 실패 - null cron 표현식")
    void validateFailureWithNull() {
        // when & then
        assertThatThrownBy(() -> CronExpressionValidator.validate(null))
                .isInstanceOf(InvalidCronExpressionException.class)
                .hasMessageContaining("cannot be null or blank");
    }

    @Test
    @DisplayName("validate 실패 - 빈 cron 표현식")
    void validateFailureWithBlank() {
        // when & then
        assertThatThrownBy(() -> CronExpressionValidator.validate("   "))
                .isInstanceOf(InvalidCronExpressionException.class)
                .hasMessageContaining("cannot be null or blank");
    }

    @Test
    @DisplayName("validate 실패 - 잘못된 형식")
    void validateFailureWithInvalidFormat() {
        // when & then
        assertThatThrownBy(() -> CronExpressionValidator.validate("invalid"))
                .isInstanceOf(InvalidCronExpressionException.class);
    }

    @ParameterizedTest
    @CsvSource({
            "'0 0 9 * * *', '0 0 9 * * *'",                         // 이미 6필드 - 그대로 반환
            "'0 0 9 1 * *', '0 0 9 1 * *'",                         // 이미 6필드 - 그대로 반환
            "'0 0 0 * * MON', '0 0 0 * * MON'",                     // 이미 6필드 - 그대로 반환
            "'0 */5 * * * *', '0 */5 * * * *'"                      // 이미 6필드 - 그대로 반환
    })
    @DisplayName("convertToAwsCronExpression 성공 - 이미 6필드인 경우 그대로 반환")
    void convertToAwsCronExpressionWith6Fields(String springCron, String expected) {
        // when
        String awsCron = CronExpressionValidator.convertToAwsCronExpression(springCron);

        // then
        assertThat(awsCron).isEqualTo(expected);
    }

    @Test
    @DisplayName("convertToAwsCronExpression 실패 - 잘못된 형식")
    void convertToAwsCronExpressionFailureInvalidFormat() {
        // when & then
        assertThatThrownBy(() -> CronExpressionValidator.convertToAwsCronExpression("invalid"))
                .isInstanceOf(InvalidCronExpressionException.class);
    }

    @Test
    @DisplayName("convertToAwsCronExpression 실패 - 지원하지 않는 필드 개수")
    void convertToAwsCronExpressionFailureUnsupportedFieldCount() {
        // given - 4필드 (너무 적음)
        String invalidCron = "0 0 9 *";

        // when & then
        assertThatThrownBy(() -> CronExpressionValidator.convertToAwsCronExpression(invalidCron))
                .isInstanceOf(InvalidCronExpressionException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "0 0 9 * * *",          // 매일 9시
            "0 */5 * * * *",        // 5분마다
            "0 0 0 1 * *",          // 매월 1일 자정
            "0 0 9 * * MON"         // 매주 월요일 9시
    })
    @DisplayName("isValid 성공 - 유효한 표현식들")
    void isValidSuccess(String validCron) {
        // when
        boolean result = CronExpressionValidator.isValid(validCron);

        // then
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "   ",
            "invalid",
            "0 0 0 0 0 0 0 0" // 너무 많은 필드
    })
    @DisplayName("isValid 실패 - 잘못된 표현식들")
    void isValidFailure(String invalidCron) {
        // when
        boolean result = CronExpressionValidator.isValid(invalidCron);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isValid 실패 - null")
    void isValidFailureWithNull() {
        // when
        boolean result = CronExpressionValidator.isValid(null);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("convertToAwsCronExpression - 다양한 Spring cron 패턴")
    void convertVariousSpringCronPatterns() {
        // 매 시간마다
        assertThat(CronExpressionValidator.convertToAwsCronExpression("0 0 * * * *"))
                .isEqualTo("0 0 * * * *");

        // 매일 자정
        assertThat(CronExpressionValidator.convertToAwsCronExpression("0 0 0 * * *"))
                .isEqualTo("0 0 0 * * *");

        // 평일 9시
        assertThat(CronExpressionValidator.convertToAwsCronExpression("0 0 9 * * MON-FRI"))
                .isEqualTo("0 0 9 * * MON-FRI");

        // 매시 30분
        assertThat(CronExpressionValidator.convertToAwsCronExpression("0 30 * * * *"))
                .isEqualTo("0 30 * * * *");
    }

    @Test
    @DisplayName("validate - Spring cron 6필드 형식 검증")
    void validateSpringCronFormat() {
        // 초 분 시 일 월 요일
        CronExpressionValidator.validate("0 0 9 * * *");        // 매일 9시
        CronExpressionValidator.validate("0 */5 * * * *");      // 5분마다
        CronExpressionValidator.validate("0 0 0 1 * *");        // 매월 1일 자정
        CronExpressionValidator.validate("0 0 9 * * MON");      // 매주 월요일 9시
        CronExpressionValidator.validate("0 30 14 1 1 *");      // 1월 1일 14:30
    }

    @Test
    @DisplayName("convertToAwsCronExpression - 6필드는 검증 후 그대로 반환")
    void convertSixFieldsReturnAsIs() {
        // given
        String springCron = "0 0 9 * * *";

        // when
        String result = CronExpressionValidator.convertToAwsCronExpression(springCron);

        // then
        assertThat(result).isEqualTo(springCron);
        assertThat(result.split(" ")).hasSize(6);
    }
}
