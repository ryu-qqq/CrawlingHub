package com.ryuqq.crawlinghub.domain.common.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("vo")
@DisplayName("PageRequest 단위 테스트")
class PageRequestTest {

    @Nested
    @DisplayName("생성자")
    class Constructor {

        @Test
        @DisplayName("유효한 페이지와 크기로 생성한다")
        void shouldCreateWithValidPageAndSize() {
            // When
            PageRequest pageRequest = new PageRequest(2, 50);

            // Then
            assertThat(pageRequest.page()).isEqualTo(2);
            assertThat(pageRequest.size()).isEqualTo(50);
        }

        @Test
        @DisplayName("음수 페이지는 0으로 정규화된다")
        void shouldNormalizeNegativePageToZero() {
            // When
            PageRequest pageRequest = new PageRequest(-1, 20);

            // Then
            assertThat(pageRequest.page()).isEqualTo(0);
        }

        @Test
        @DisplayName("0 이하 크기는 기본값으로 정규화된다")
        void shouldNormalizeZeroSizeToDefault() {
            // When
            PageRequest pageRequest = new PageRequest(0, 0);

            // Then
            assertThat(pageRequest.size()).isEqualTo(PageRequest.DEFAULT_SIZE);
        }

        @Test
        @DisplayName("최대 크기 초과는 최대값으로 정규화된다")
        void shouldNormalizeOversizedToMax() {
            // When
            PageRequest pageRequest = new PageRequest(0, 200);

            // Then
            assertThat(pageRequest.size()).isEqualTo(PageRequest.MAX_SIZE);
        }
    }

    @Nested
    @DisplayName("팩토리 메서드")
    class FactoryMethods {

        @Test
        @DisplayName("of()로 PageRequest를 생성한다")
        void shouldCreateWithOf() {
            // When
            PageRequest pageRequest = PageRequest.of(3, 25);

            // Then
            assertThat(pageRequest.page()).isEqualTo(3);
            assertThat(pageRequest.size()).isEqualTo(25);
        }

        @Test
        @DisplayName("first()로 첫 페이지 요청을 생성한다")
        void shouldCreateFirstPage() {
            // When
            PageRequest pageRequest = PageRequest.first(30);

            // Then
            assertThat(pageRequest.page()).isEqualTo(0);
            assertThat(pageRequest.size()).isEqualTo(30);
        }

        @Test
        @DisplayName("defaultPage()로 기본 설정 요청을 생성한다")
        void shouldCreateDefaultPage() {
            // When
            PageRequest pageRequest = PageRequest.defaultPage();

            // Then
            assertThat(pageRequest.page()).isEqualTo(0);
            assertThat(pageRequest.size()).isEqualTo(PageRequest.DEFAULT_SIZE);
        }
    }

    @Nested
    @DisplayName("페이지네이션 계산")
    class PaginationCalculation {

        @Test
        @DisplayName("offset()은 page * size를 반환한다")
        void shouldCalculateOffset() {
            // Given
            PageRequest pageRequest = PageRequest.of(3, 20);

            // When
            long offset = pageRequest.offset();

            // Then
            assertThat(offset).isEqualTo(60L);
        }

        @Test
        @DisplayName("첫 페이지의 offset은 0이다")
        void shouldReturnZeroOffsetForFirstPage() {
            // Given
            PageRequest pageRequest = PageRequest.first(20);

            // When
            long offset = pageRequest.offset();

            // Then
            assertThat(offset).isEqualTo(0L);
        }

        @Test
        @DisplayName("totalPages()는 전체 페이지 수를 계산한다")
        void shouldCalculateTotalPages() {
            // Given
            PageRequest pageRequest = PageRequest.of(0, 20);

            // When
            int totalPages = pageRequest.totalPages(95);

            // Then
            assertThat(totalPages).isEqualTo(5);
        }

        @Test
        @DisplayName("totalPages()는 정확히 나누어 떨어지는 경우도 처리한다")
        void shouldCalculateTotalPagesForExactDivision() {
            // Given
            PageRequest pageRequest = PageRequest.of(0, 20);

            // When
            int totalPages = pageRequest.totalPages(100);

            // Then
            assertThat(totalPages).isEqualTo(5);
        }

        @Test
        @DisplayName("totalPages()는 0개 항목에 대해 0을 반환한다")
        void shouldReturnZeroTotalPagesForEmptyResult() {
            // Given
            PageRequest pageRequest = PageRequest.of(0, 20);

            // When
            int totalPages = pageRequest.totalPages(0);

            // Then
            assertThat(totalPages).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("페이지 네비게이션")
    class PageNavigation {

        @Test
        @DisplayName("next()는 다음 페이지 요청을 반환한다")
        void shouldReturnNextPage() {
            // Given
            PageRequest pageRequest = PageRequest.of(2, 20);

            // When
            PageRequest next = pageRequest.next();

            // Then
            assertThat(next.page()).isEqualTo(3);
            assertThat(next.size()).isEqualTo(20);
        }

        @Test
        @DisplayName("previous()는 이전 페이지 요청을 반환한다")
        void shouldReturnPreviousPage() {
            // Given
            PageRequest pageRequest = PageRequest.of(3, 20);

            // When
            PageRequest previous = pageRequest.previous();

            // Then
            assertThat(previous.page()).isEqualTo(2);
            assertThat(previous.size()).isEqualTo(20);
        }

        @Test
        @DisplayName("첫 페이지에서 previous()는 현재 페이지를 반환한다")
        void shouldReturnSamePageWhenPreviousFromFirst() {
            // Given
            PageRequest pageRequest = PageRequest.first(20);

            // When
            PageRequest previous = pageRequest.previous();

            // Then
            assertThat(previous.page()).isEqualTo(0);
            assertThat(previous).isSameAs(pageRequest);
        }

        @Test
        @DisplayName("isFirst()는 첫 페이지인지 확인한다")
        void shouldCheckIfFirstPage() {
            // Given
            PageRequest first = PageRequest.first(20);
            PageRequest notFirst = PageRequest.of(1, 20);

            // When & Then
            assertThat(first.isFirst()).isTrue();
            assertThat(notFirst.isFirst()).isFalse();
        }

        @Test
        @DisplayName("isLast()는 마지막 페이지인지 확인한다")
        void shouldCheckIfLastPage() {
            // Given
            PageRequest pageRequest = PageRequest.of(4, 20);

            // When & Then
            assertThat(pageRequest.isLast(100)).isTrue();
            assertThat(pageRequest.isLast(101)).isFalse();
        }
    }
}
