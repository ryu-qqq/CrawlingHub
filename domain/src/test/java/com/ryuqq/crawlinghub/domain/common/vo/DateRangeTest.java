package com.ryuqq.crawlinghub.domain.common.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("vo")
@DisplayName("DateRange 단위 테스트")
class DateRangeTest {

    @Nested
    @DisplayName("생성자")
    class Constructor {

        @Test
        @DisplayName("유효한 날짜 범위로 생성한다")
        void shouldCreateWithValidRange() {
            // Given
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate endDate = LocalDate.of(2024, 1, 31);

            // When
            DateRange dateRange = new DateRange(startDate, endDate);

            // Then
            assertThat(dateRange.startDate()).isEqualTo(startDate);
            assertThat(dateRange.endDate()).isEqualTo(endDate);
        }

        @Test
        @DisplayName("시작일과 종료일이 같아도 생성한다")
        void shouldCreateWhenStartEqualsEnd() {
            // Given
            LocalDate date = LocalDate.of(2024, 1, 15);

            // When
            DateRange dateRange = new DateRange(date, date);

            // Then
            assertThat(dateRange.startDate()).isEqualTo(date);
            assertThat(dateRange.endDate()).isEqualTo(date);
        }

        @Test
        @DisplayName("시작일이 종료일보다 이후면 예외를 던진다")
        void shouldThrowWhenStartIsAfterEnd() {
            // Given
            LocalDate startDate = LocalDate.of(2024, 1, 31);
            LocalDate endDate = LocalDate.of(2024, 1, 1);

            // When & Then
            assertThatThrownBy(() -> new DateRange(startDate, endDate))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("시작일")
                    .hasMessageContaining("종료일");
        }

        @Test
        @DisplayName("시작일이 null이면 생성한다")
        void shouldCreateWithNullStartDate() {
            // Given
            LocalDate endDate = LocalDate.of(2024, 1, 31);

            // When
            DateRange dateRange = new DateRange(null, endDate);

            // Then
            assertThat(dateRange.startDate()).isNull();
            assertThat(dateRange.endDate()).isEqualTo(endDate);
        }

        @Test
        @DisplayName("종료일이 null이면 생성한다")
        void shouldCreateWithNullEndDate() {
            // Given
            LocalDate startDate = LocalDate.of(2024, 1, 1);

            // When
            DateRange dateRange = new DateRange(startDate, null);

            // Then
            assertThat(dateRange.startDate()).isEqualTo(startDate);
            assertThat(dateRange.endDate()).isNull();
        }
    }

    @Nested
    @DisplayName("팩토리 메서드")
    class FactoryMethods {

        @Test
        @DisplayName("of()로 DateRange를 생성한다")
        void shouldCreateWithOf() {
            // Given
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate endDate = LocalDate.of(2024, 12, 31);

            // When
            DateRange dateRange = DateRange.of(startDate, endDate);

            // Then
            assertThat(dateRange.startDate()).isEqualTo(startDate);
            assertThat(dateRange.endDate()).isEqualTo(endDate);
        }

        @Test
        @DisplayName("lastDays()로 최근 N일 범위를 생성한다")
        void shouldCreateLastDays() {
            // When
            DateRange dateRange = DateRange.lastDays(7);

            // Then
            LocalDate today = LocalDate.now();
            assertThat(dateRange.startDate()).isEqualTo(today.minusDays(7));
            assertThat(dateRange.endDate()).isEqualTo(today);
        }

        @Test
        @DisplayName("lastDays(0)은 오늘만 포함하는 범위를 생성한다")
        void shouldCreateLastDaysWithZero() {
            // When
            DateRange dateRange = DateRange.lastDays(0);

            // Then
            LocalDate today = LocalDate.now();
            assertThat(dateRange.startDate()).isEqualTo(today);
            assertThat(dateRange.endDate()).isEqualTo(today);
        }

        @Test
        @DisplayName("lastDays()에 음수를 전달하면 예외를 던진다")
        void shouldThrowForNegativeDays() {
            // When & Then
            assertThatThrownBy(() -> DateRange.lastDays(-1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("0 이상");
        }

        @Test
        @DisplayName("thisMonth()로 이번 달 범위를 생성한다")
        void shouldCreateThisMonth() {
            // When
            DateRange dateRange = DateRange.thisMonth();

            // Then
            LocalDate today = LocalDate.now();
            assertThat(dateRange.startDate()).isEqualTo(today.withDayOfMonth(1));
            assertThat(dateRange.endDate()).isEqualTo(today.withDayOfMonth(today.lengthOfMonth()));
        }

        @Test
        @DisplayName("lastMonth()로 지난 달 범위를 생성한다")
        void shouldCreateLastMonth() {
            // When
            DateRange dateRange = DateRange.lastMonth();

            // Then
            LocalDate today = LocalDate.now();
            LocalDate firstDayLastMonth = today.minusMonths(1).withDayOfMonth(1);
            LocalDate lastDayLastMonth = today.withDayOfMonth(1).minusDays(1);
            assertThat(dateRange.startDate()).isEqualTo(firstDayLastMonth);
            assertThat(dateRange.endDate()).isEqualTo(lastDayLastMonth);
        }

        @Test
        @DisplayName("until()로 종료일까지의 범위를 생성한다")
        void shouldCreateUntil() {
            // Given
            LocalDate endDate = LocalDate.of(2024, 6, 30);

            // When
            DateRange dateRange = DateRange.until(endDate);

            // Then
            assertThat(dateRange.startDate()).isNull();
            assertThat(dateRange.endDate()).isEqualTo(endDate);
        }

        @Test
        @DisplayName("from()으로 시작일부터의 범위를 생성한다")
        void shouldCreateFrom() {
            // Given
            LocalDate startDate = LocalDate.of(2024, 1, 1);

            // When
            DateRange dateRange = DateRange.from(startDate);

            // Then
            assertThat(dateRange.startDate()).isEqualTo(startDate);
            assertThat(dateRange.endDate()).isNull();
        }
    }

    @Nested
    @DisplayName("Instant 변환")
    class InstantConversion {

        @Test
        @DisplayName("startInstant()는 시작일의 00:00:00을 반환한다")
        void shouldReturnStartInstant() {
            // Given
            LocalDate startDate = LocalDate.of(2024, 1, 15);
            DateRange dateRange = new DateRange(startDate, LocalDate.of(2024, 1, 31));

            // When
            Instant instant = dateRange.startInstant();

            // Then
            assertThat(instant).isNotNull();
        }

        @Test
        @DisplayName("시작일이 null이면 startInstant()는 null을 반환한다")
        void shouldReturnNullForNullStartDate() {
            // Given
            DateRange dateRange = DateRange.until(LocalDate.of(2024, 1, 31));

            // When
            Instant instant = dateRange.startInstant();

            // Then
            assertThat(instant).isNull();
        }

        @Test
        @DisplayName("endInstant()는 종료일의 23:59:59.999999999를 반환한다")
        void shouldReturnEndInstant() {
            // Given
            LocalDate endDate = LocalDate.of(2024, 1, 31);
            DateRange dateRange = new DateRange(LocalDate.of(2024, 1, 1), endDate);

            // When
            Instant instant = dateRange.endInstant();

            // Then
            assertThat(instant).isNotNull();
        }

        @Test
        @DisplayName("종료일이 null이면 endInstant()는 null을 반환한다")
        void shouldReturnNullForNullEndDate() {
            // Given
            DateRange dateRange = DateRange.from(LocalDate.of(2024, 1, 1));

            // When
            Instant instant = dateRange.endInstant();

            // Then
            assertThat(instant).isNull();
        }
    }

    @Nested
    @DisplayName("유틸리티 메서드")
    class UtilityMethods {

        @Test
        @DisplayName("isEmpty()는 시작일과 종료일이 모두 null이면 true를 반환한다")
        void shouldReturnTrueForEmptyRange() {
            // Given
            DateRange dateRange = new DateRange(null, null);

            // When & Then
            assertThat(dateRange.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("isEmpty()는 시작일만 있으면 false를 반환한다")
        void shouldReturnFalseWhenStartDateExists() {
            // Given
            DateRange dateRange = DateRange.from(LocalDate.of(2024, 1, 1));

            // When & Then
            assertThat(dateRange.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("isEmpty()는 종료일만 있으면 false를 반환한다")
        void shouldReturnFalseWhenEndDateExists() {
            // Given
            DateRange dateRange = DateRange.until(LocalDate.of(2024, 1, 31));

            // When & Then
            assertThat(dateRange.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("contains()는 범위 내 날짜에 대해 true를 반환한다")
        void shouldReturnTrueForDateWithinRange() {
            // Given
            DateRange dateRange = DateRange.of(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
            LocalDate dateWithin = LocalDate.of(2024, 1, 15);

            // When & Then
            assertThat(dateRange.contains(dateWithin)).isTrue();
        }

        @Test
        @DisplayName("contains()는 시작일에 대해 true를 반환한다")
        void shouldReturnTrueForStartDate() {
            // Given
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            DateRange dateRange = DateRange.of(startDate, LocalDate.of(2024, 1, 31));

            // When & Then
            assertThat(dateRange.contains(startDate)).isTrue();
        }

        @Test
        @DisplayName("contains()는 종료일에 대해 true를 반환한다")
        void shouldReturnTrueForEndDate() {
            // Given
            LocalDate endDate = LocalDate.of(2024, 1, 31);
            DateRange dateRange = DateRange.of(LocalDate.of(2024, 1, 1), endDate);

            // When & Then
            assertThat(dateRange.contains(endDate)).isTrue();
        }

        @Test
        @DisplayName("contains()는 범위 밖 날짜에 대해 false를 반환한다")
        void shouldReturnFalseForDateOutsideRange() {
            // Given
            DateRange dateRange = DateRange.of(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
            LocalDate dateOutside = LocalDate.of(2024, 2, 1);

            // When & Then
            assertThat(dateRange.contains(dateOutside)).isFalse();
        }

        @Test
        @DisplayName("contains()는 null 날짜에 대해 false를 반환한다")
        void shouldReturnFalseForNullDate() {
            // Given
            DateRange dateRange = DateRange.of(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));

            // When & Then
            assertThat(dateRange.contains(null)).isFalse();
        }

        @Test
        @DisplayName("contains()는 시작일만 있는 범위에서 시작일 이후 날짜에 대해 true를 반환한다")
        void shouldReturnTrueForDateAfterStartWhenNoEnd() {
            // Given
            DateRange dateRange = DateRange.from(LocalDate.of(2024, 1, 1));
            LocalDate date = LocalDate.of(2025, 12, 31);

            // When & Then
            assertThat(dateRange.contains(date)).isTrue();
        }

        @Test
        @DisplayName("contains()는 종료일만 있는 범위에서 종료일 이전 날짜에 대해 true를 반환한다")
        void shouldReturnTrueForDateBeforeEndWhenNoStart() {
            // Given
            DateRange dateRange = DateRange.until(LocalDate.of(2024, 1, 31));
            LocalDate date = LocalDate.of(2020, 1, 1);

            // When & Then
            assertThat(dateRange.contains(date)).isTrue();
        }
    }
}
