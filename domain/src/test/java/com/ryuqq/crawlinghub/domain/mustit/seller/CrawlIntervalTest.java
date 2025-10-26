package com.ryuqq.crawlinghub.domain.mustit.seller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * CrawlInterval Value Object 단위 테스트
 *
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
@DisplayName("CrawlInterval 단위 테스트")
class CrawlIntervalTest {

    @Test
    @DisplayName("유효한 주기로 CrawlInterval을 생성할 수 있다")
    void createCrawlIntervalWithValidInterval() {
        // given
        CrawlIntervalType type = CrawlIntervalType.HOURLY;
        int intervalValue = 2;

        // when
        CrawlInterval crawlInterval = new CrawlInterval(type, intervalValue);

        // then
        assertThat(crawlInterval.getIntervalType()).isEqualTo(type);
        assertThat(crawlInterval.getIntervalValue()).isEqualTo(intervalValue);
        assertThat(crawlInterval.getCronExpression()).isNotBlank();
    }

    @Test
    @DisplayName("주기 값이 0 이하면 예외가 발생한다")
    void throwExceptionWhenIntervalValueIsZeroOrNegative() {
        // given
        CrawlIntervalType type = CrawlIntervalType.DAILY;
        int invalidIntervalValue = 0;

        // when & then
        assertThatThrownBy(() -> new CrawlInterval(type, invalidIntervalValue))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("intervalValue must be greater than 0");
    }

    @Test
    @DisplayName("주기 타입이 null이면 예외가 발생한다")
    void throwExceptionWhenIntervalTypeIsNull() {
        // given
        int intervalValue = 1;

        // when & then
        assertThatThrownBy(() -> new CrawlInterval(null, intervalValue))
                .isInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("provideCronExpressionTestCases")
    @DisplayName("주기 타입과 값에 따라 올바른 cron 표현식이 생성된다")
    void generateCorrectCronExpression(CrawlIntervalType type, int value, String expectedCron) {
        // when
        CrawlInterval crawlInterval = new CrawlInterval(type, value);

        // then
        assertThat(crawlInterval.getCronExpression()).isEqualTo(expectedCron);
    }

    private static Stream<Arguments> provideCronExpressionTestCases() {
        return Stream.of(
                Arguments.of(CrawlIntervalType.HOURLY, 1, "0 0/1 * * ? *"),
                Arguments.of(CrawlIntervalType.HOURLY, 6, "0 0/6 * * ? *"),
                Arguments.of(CrawlIntervalType.DAILY, 1, "0 0 0/1 * ? *"),
                Arguments.of(CrawlIntervalType.DAILY, 3, "0 0 0/3 * ? *"),
                Arguments.of(CrawlIntervalType.WEEKLY, 1, "0 0 0 ? * 1/1 *"),
                Arguments.of(CrawlIntervalType.WEEKLY, 2, "0 0 0 ? * 1/2 *")
        );
    }

    @Test
    @DisplayName("동일한 주기 타입과 값을 가진 CrawlInterval은 동등하다")
    void equalsWithSameIntervalTypeAndValue() {
        // given
        CrawlInterval interval1 = new CrawlInterval(CrawlIntervalType.HOURLY, 2);
        CrawlInterval interval2 = new CrawlInterval(CrawlIntervalType.HOURLY, 2);

        // when & then
        assertThat(interval1).isEqualTo(interval2);
        assertThat(interval1.hashCode()).isEqualTo(interval2.hashCode());
    }

    @Test
    @DisplayName("다른 주기 타입 또는 값을 가진 CrawlInterval은 동등하지 않다")
    void notEqualsWithDifferentIntervalTypeOrValue() {
        // given
        CrawlInterval interval1 = new CrawlInterval(CrawlIntervalType.HOURLY, 2);
        CrawlInterval interval2 = new CrawlInterval(CrawlIntervalType.DAILY, 2);
        CrawlInterval interval3 = new CrawlInterval(CrawlIntervalType.HOURLY, 3);

        // when & then
        assertThat(interval1).isNotEqualTo(interval2);
        assertThat(interval1).isNotEqualTo(interval3);
    }

    @Test
    @DisplayName("toString()은 모든 필드 정보를 포함한다")
    void toStringContainsAllFields() {
        // given
        CrawlInterval interval = new CrawlInterval(CrawlIntervalType.HOURLY, 2);

        // when
        String result = interval.toString();

        // then
        assertThat(result)
                .contains("HOURLY")
                .contains("2")
                .contains("0 0/2 * * ? *");
    }
}
