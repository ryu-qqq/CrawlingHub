package com.ryuqq.crawlinghub.domain.common.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@Tag("unit")
@Tag("domain")
@Tag("vo")
@DisplayName("SortDirection 단위 테스트")
class SortDirectionTest {

    @Nested
    @DisplayName("Enum 값 검증")
    class EnumValues {

        @Test
        @DisplayName("ASC의 displayName은 '오름차순'이다")
        void shouldHaveCorrectDisplayNameForAsc() {
            // When & Then
            assertThat(SortDirection.ASC.displayName()).isEqualTo("오름차순");
        }

        @Test
        @DisplayName("DESC의 displayName은 '내림차순'이다")
        void shouldHaveCorrectDisplayNameForDesc() {
            // When & Then
            assertThat(SortDirection.DESC.displayName()).isEqualTo("내림차순");
        }
    }

    @Nested
    @DisplayName("방향 확인 메서드")
    class DirectionCheckMethods {

        @Test
        @DisplayName("ASC.isAscending()은 true를 반환한다")
        void shouldReturnTrueForAscIsAscending() {
            // When & Then
            assertThat(SortDirection.ASC.isAscending()).isTrue();
        }

        @Test
        @DisplayName("DESC.isAscending()은 false를 반환한다")
        void shouldReturnFalseForDescIsAscending() {
            // When & Then
            assertThat(SortDirection.DESC.isAscending()).isFalse();
        }

        @Test
        @DisplayName("ASC.isDescending()은 false를 반환한다")
        void shouldReturnFalseForAscIsDescending() {
            // When & Then
            assertThat(SortDirection.ASC.isDescending()).isFalse();
        }

        @Test
        @DisplayName("DESC.isDescending()은 true를 반환한다")
        void shouldReturnTrueForDescIsDescending() {
            // When & Then
            assertThat(SortDirection.DESC.isDescending()).isTrue();
        }
    }

    @Nested
    @DisplayName("reverse 메서드")
    class ReverseMethod {

        @Test
        @DisplayName("ASC.reverse()는 DESC를 반환한다")
        void shouldReturnDescForAscReverse() {
            // When
            SortDirection reversed = SortDirection.ASC.reverse();

            // Then
            assertThat(reversed).isEqualTo(SortDirection.DESC);
        }

        @Test
        @DisplayName("DESC.reverse()는 ASC를 반환한다")
        void shouldReturnAscForDescReverse() {
            // When
            SortDirection reversed = SortDirection.DESC.reverse();

            // Then
            assertThat(reversed).isEqualTo(SortDirection.ASC);
        }
    }

    @Nested
    @DisplayName("defaultDirection 메서드")
    class DefaultDirectionMethod {

        @Test
        @DisplayName("defaultDirection()은 DESC를 반환한다")
        void shouldReturnDescAsDefault() {
            // When
            SortDirection defaultDirection = SortDirection.defaultDirection();

            // Then
            assertThat(defaultDirection).isEqualTo(SortDirection.DESC);
        }
    }

    @Nested
    @DisplayName("fromString 메서드")
    class FromStringMethod {

        @Test
        @DisplayName("'ASC' 문자열은 ASC로 파싱된다")
        void shouldParseAscString() {
            // When
            SortDirection result = SortDirection.fromString("ASC");

            // Then
            assertThat(result).isEqualTo(SortDirection.ASC);
        }

        @Test
        @DisplayName("'DESC' 문자열은 DESC로 파싱된다")
        void shouldParseDescString() {
            // When
            SortDirection result = SortDirection.fromString("DESC");

            // Then
            assertThat(result).isEqualTo(SortDirection.DESC);
        }

        @Test
        @DisplayName("소문자 'asc'도 ASC로 파싱된다")
        void shouldParseLowercaseAsc() {
            // When
            SortDirection result = SortDirection.fromString("asc");

            // Then
            assertThat(result).isEqualTo(SortDirection.ASC);
        }

        @Test
        @DisplayName("소문자 'desc'도 DESC로 파싱된다")
        void shouldParseLowercaseDesc() {
            // When
            SortDirection result = SortDirection.fromString("desc");

            // Then
            assertThat(result).isEqualTo(SortDirection.DESC);
        }

        @Test
        @DisplayName("공백이 있는 ' asc '도 ASC로 파싱된다")
        void shouldParseWithWhitespace() {
            // When
            SortDirection result = SortDirection.fromString(" asc ");

            // Then
            assertThat(result).isEqualTo(SortDirection.ASC);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("null 또는 빈 문자열은 기본값(DESC)을 반환한다")
        void shouldReturnDefaultForNullOrEmpty(String value) {
            // When
            SortDirection result = SortDirection.fromString(value);

            // Then
            assertThat(result).isEqualTo(SortDirection.DESC);
        }

        @ParameterizedTest
        @ValueSource(strings = {"invalid", "ascending", "descending", "up", "down"})
        @DisplayName("유효하지 않은 문자열은 기본값(DESC)을 반환한다")
        void shouldReturnDefaultForInvalidString(String value) {
            // When
            SortDirection result = SortDirection.fromString(value);

            // Then
            assertThat(result).isEqualTo(SortDirection.DESC);
        }
    }
}
