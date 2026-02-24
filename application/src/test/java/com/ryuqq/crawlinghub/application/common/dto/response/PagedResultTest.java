package com.ryuqq.crawlinghub.application.common.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * PagedResult 단위 테스트
 *
 * <p>페이징 결과 응답 래퍼 검증
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("PagedResult 테스트")
class PagedResultTest {

    @Nested
    @DisplayName("생성자 테스트")
    class Constructor {

        @Test
        @DisplayName("[성공] 콘텐츠와 페이지 메타로 생성")
        void shouldCreatePagedResult() {
            // Given
            List<String> content = List.of("item1", "item2");

            // When
            PagedResult<String> result = PagedResult.of(content, 0, 20, 100L);

            // Then
            assertThat(result.content()).hasSize(2);
            assertThat(result.content()).containsExactly("item1", "item2");
        }

        @Test
        @DisplayName("[성공] null 콘텐츠는 빈 리스트로 변환")
        void shouldConvertNullContentToEmptyList() {
            // When
            PagedResult<String> result = new PagedResult<>(null, null);

            // Then
            assertThat(result.content()).isEmpty();
        }
    }

    @Nested
    @DisplayName("of() 팩토리 메서드 테스트")
    class Of {

        @Test
        @DisplayName("[성공] 콘텐츠, 페이지, 사이즈, 전체 요소 수로 생성")
        void shouldCreateWithPageInfo() {
            // Given
            List<String> content = List.of("a", "b", "c");

            // When
            PagedResult<String> result = PagedResult.of(content, 1, 10, 30L);

            // Then
            assertThat(result.content()).hasSize(3);
            assertThat(result.pageMeta()).isNotNull();
        }
    }

    @Nested
    @DisplayName("empty() 팩토리 메서드 테스트")
    class Empty {

        @Test
        @DisplayName("[성공] 빈 PagedResult 생성")
        void shouldCreateEmptyPagedResult() {
            // When
            PagedResult<String> result = PagedResult.empty(20);

            // Then
            assertThat(result.content()).isEmpty();
            assertThat(result.isEmpty()).isTrue();
            assertThat(result.size()).isZero();
        }
    }

    @Nested
    @DisplayName("isEmpty() 테스트")
    class IsEmpty {

        @Test
        @DisplayName("[성공] 비어있으면 true 반환")
        void shouldReturnTrueWhenEmpty() {
            // Given
            PagedResult<String> result = PagedResult.of(List.of(), 0, 10, 0L);

            // Then
            assertThat(result.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("[성공] 비어있지 않으면 false 반환")
        void shouldReturnFalseWhenNotEmpty() {
            // Given
            PagedResult<String> result = PagedResult.of(List.of("item"), 0, 10, 1L);

            // Then
            assertThat(result.isEmpty()).isFalse();
        }
    }

    @Nested
    @DisplayName("size() 테스트")
    class Size {

        @Test
        @DisplayName("[성공] 콘텐츠 크기 반환")
        void shouldReturnContentSize() {
            // Given
            PagedResult<Integer> result = PagedResult.of(List.of(1, 2, 3, 4), 0, 10, 4L);

            // Then
            assertThat(result.size()).isEqualTo(4);
        }
    }
}
