package com.ryuqq.crawlinghub.domain.crawler.vo;

import org.junit.jupiter.api.Test;

import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * CrawlingInterval VO 테스트 (Crawler 패키지)
 *
 * TDD Phase: Red → Green
 * - ChronoUnit 기반 크롤링 주기 검증
 * - HOURS, DAYS 단위 지원
 * - amount 1 이상 검증
 */
class CrawlingIntervalTest {

    @Test
    void shouldCreateCrawlingIntervalWithHours() {
        // When
        CrawlingInterval interval = new CrawlingInterval(12, ChronoUnit.HOURS);

        // Then
        assertThat(interval.amount()).isEqualTo(12);
        assertThat(interval.unit()).isEqualTo(ChronoUnit.HOURS);
    }

    @Test
    void shouldCreateCrawlingIntervalWithDays() {
        // When
        CrawlingInterval interval = new CrawlingInterval(7, ChronoUnit.DAYS);

        // Then
        assertThat(interval.amount()).isEqualTo(7);
        assertThat(interval.unit()).isEqualTo(ChronoUnit.DAYS);
    }

    @Test
    void shouldThrowExceptionWhenAmountIsZero() {
        // When & Then
        assertThatThrownBy(() -> new CrawlingInterval(0, ChronoUnit.HOURS))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("크롤링 주기는 1 이상이어야 합니다");
    }

    @Test
    void shouldThrowExceptionWhenAmountIsNegative() {
        // When & Then
        assertThatThrownBy(() -> new CrawlingInterval(-1, ChronoUnit.DAYS))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("크롤링 주기는 1 이상이어야 합니다");
    }

    @Test
    void shouldThrowExceptionWhenUnitIsMinutes() {
        // When & Then
        assertThatThrownBy(() -> new CrawlingInterval(30, ChronoUnit.MINUTES))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("크롤링 주기는 HOURS 또는 DAYS만 지원합니다");
    }

    @Test
    void shouldThrowExceptionWhenUnitIsWeeks() {
        // When & Then
        assertThatThrownBy(() -> new CrawlingInterval(2, ChronoUnit.WEEKS))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("크롤링 주기는 HOURS 또는 DAYS만 지원합니다");
    }

    @Test
    void shouldBeEqualWhenSameAmountAndUnit() {
        // Given
        CrawlingInterval interval1 = new CrawlingInterval(24, ChronoUnit.HOURS);
        CrawlingInterval interval2 = new CrawlingInterval(24, ChronoUnit.HOURS);

        // When & Then
        assertThat(interval1).isEqualTo(interval2);
        assertThat(interval1.hashCode()).isEqualTo(interval2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenDifferentAmount() {
        // Given
        CrawlingInterval interval1 = new CrawlingInterval(12, ChronoUnit.HOURS);
        CrawlingInterval interval2 = new CrawlingInterval(24, ChronoUnit.HOURS);

        // When & Then
        assertThat(interval1).isNotEqualTo(interval2);
    }

    @Test
    void shouldNotBeEqualWhenDifferentUnit() {
        // Given
        CrawlingInterval interval1 = new CrawlingInterval(1, ChronoUnit.HOURS);
        CrawlingInterval interval2 = new CrawlingInterval(1, ChronoUnit.DAYS);

        // When & Then
        assertThat(interval1).isNotEqualTo(interval2);
    }
}
