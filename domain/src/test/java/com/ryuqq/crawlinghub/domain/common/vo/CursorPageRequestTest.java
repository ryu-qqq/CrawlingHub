package com.ryuqq.crawlinghub.domain.common.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("vo")
@DisplayName("CursorPageRequest 단위 테스트")
class CursorPageRequestTest {

    @Nested
    @DisplayName("생성자")
    class Constructor {

        @Test
        @DisplayName("유효한 커서와 크기로 생성한다")
        void shouldCreateWithValidCursorAndSize() {
            // When
            CursorPageRequest<String> request = new CursorPageRequest<>("cursor-123", 30);

            // Then
            assertThat(request.cursor()).isEqualTo("cursor-123");
            assertThat(request.size()).isEqualTo(30);
        }

        @Test
        @DisplayName("0 이하 크기는 기본값으로 정규화된다")
        void shouldNormalizeZeroSizeToDefault() {
            // When
            CursorPageRequest<String> request = new CursorPageRequest<>("cursor", 0);

            // Then
            assertThat(request.size()).isEqualTo(CursorPageRequest.DEFAULT_SIZE);
        }

        @Test
        @DisplayName("최대 크기 초과는 최대값으로 정규화된다")
        void shouldNormalizeOversizedToMax() {
            // When
            CursorPageRequest<String> request = new CursorPageRequest<>("cursor", 200);

            // Then
            assertThat(request.size()).isEqualTo(CursorPageRequest.MAX_SIZE);
        }

        @Test
        @DisplayName("빈 문자열 커서는 null로 정규화된다")
        void shouldNormalizeBlankCursorToNull() {
            // When
            CursorPageRequest<String> request = new CursorPageRequest<>("  ", 20);

            // Then
            assertThat(request.cursor()).isNull();
        }

        @Test
        @DisplayName("null 커서로 생성할 수 있다")
        void shouldCreateWithNullCursor() {
            // When
            CursorPageRequest<String> request = new CursorPageRequest<>(null, 20);

            // Then
            assertThat(request.cursor()).isNull();
        }
    }

    @Nested
    @DisplayName("팩토리 메서드")
    class FactoryMethods {

        @Test
        @DisplayName("of()로 CursorPageRequest를 생성한다")
        void shouldCreateWithOf() {
            // When
            CursorPageRequest<String> request = CursorPageRequest.of("cursor-xyz", 25);

            // Then
            assertThat(request.cursor()).isEqualTo("cursor-xyz");
            assertThat(request.size()).isEqualTo(25);
        }

        @Test
        @DisplayName("first()로 첫 페이지 요청을 생성한다")
        void shouldCreateFirstPage() {
            // When
            CursorPageRequest<String> request = CursorPageRequest.first(30);

            // Then
            assertThat(request.cursor()).isNull();
            assertThat(request.size()).isEqualTo(30);
        }

        @Test
        @DisplayName("defaultPage()로 기본 설정 요청을 생성한다")
        void shouldCreateDefaultPage() {
            // When
            CursorPageRequest<String> request = CursorPageRequest.defaultPage();

            // Then
            assertThat(request.cursor()).isNull();
            assertThat(request.size()).isEqualTo(CursorPageRequest.DEFAULT_SIZE);
        }

        @Test
        @DisplayName("afterId()로 ID 기반 커서 요청을 생성한다")
        void shouldCreateAfterId() {
            // When
            CursorPageRequest<Long> request = CursorPageRequest.afterId(12345L, 20);

            // Then
            assertThat(request.cursor()).isEqualTo(12345L);
            assertThat(request.size()).isEqualTo(20);
        }

        @Test
        @DisplayName("afterId()에 null ID를 전달하면 커서가 null이다")
        void shouldCreateAfterIdWithNullId() {
            // When
            CursorPageRequest<Long> request = CursorPageRequest.afterId(null, 20);

            // Then
            assertThat(request.cursor()).isNull();
        }

        @Test
        @DisplayName("ofString()으로 String 커서 요청을 생성한다")
        void shouldCreateWithOfString() {
            // When
            CursorPageRequest<String> request = CursorPageRequest.ofString("cursor-abc", 15);

            // Then
            assertThat(request.cursor()).isEqualTo("cursor-abc");
            assertThat(request.size()).isEqualTo(15);
        }
    }

    @Nested
    @DisplayName("상태 확인 메서드")
    class StateCheckMethods {

        @Test
        @DisplayName("isFirstPage()는 커서가 없으면 true를 반환한다")
        void shouldReturnTrueForFirstPageWhenNoCursor() {
            // Given
            CursorPageRequest<String> request = CursorPageRequest.first(20);

            // When & Then
            assertThat(request.isFirstPage()).isTrue();
        }

        @Test
        @DisplayName("isFirstPage()는 커서가 있으면 false를 반환한다")
        void shouldReturnFalseForFirstPageWhenHasCursor() {
            // Given
            CursorPageRequest<String> request = CursorPageRequest.of("cursor", 20);

            // When & Then
            assertThat(request.isFirstPage()).isFalse();
        }

        @Test
        @DisplayName("hasCursor()는 커서가 있으면 true를 반환한다")
        void shouldReturnTrueWhenHasCursor() {
            // Given
            CursorPageRequest<String> request = CursorPageRequest.of("cursor", 20);

            // When & Then
            assertThat(request.hasCursor()).isTrue();
        }

        @Test
        @DisplayName("hasCursor()는 커서가 없으면 false를 반환한다")
        void shouldReturnFalseWhenNoCursor() {
            // Given
            CursorPageRequest<String> request = CursorPageRequest.first(20);

            // When & Then
            assertThat(request.hasCursor()).isFalse();
        }
    }

    @Nested
    @DisplayName("페이지 네비게이션")
    class PageNavigation {

        @Test
        @DisplayName("next()는 다음 커서로 새 요청을 생성한다")
        void shouldCreateNextPage() {
            // Given
            CursorPageRequest<String> request = CursorPageRequest.of("cursor-1", 20);

            // When
            CursorPageRequest<String> next = request.next("cursor-2");

            // Then
            assertThat(next.cursor()).isEqualTo("cursor-2");
            assertThat(next.size()).isEqualTo(20);
        }

        @Test
        @DisplayName("Long 커서로 next()를 사용할 수 있다")
        void shouldCreateNextPageWithLongCursor() {
            // Given
            CursorPageRequest<Long> request = CursorPageRequest.afterId(100L, 20);

            // When
            CursorPageRequest<Long> next = request.next(200L);

            // Then
            assertThat(next.cursor()).isEqualTo(200L);
            assertThat(next.size()).isEqualTo(20);
        }

        @Test
        @DisplayName("fetchSize()는 size + 1을 반환한다")
        void shouldReturnFetchSize() {
            // Given
            CursorPageRequest<String> request = CursorPageRequest.of("cursor", 20);

            // When
            int fetchSize = request.fetchSize();

            // Then
            assertThat(fetchSize).isEqualTo(21);
        }
    }
}
