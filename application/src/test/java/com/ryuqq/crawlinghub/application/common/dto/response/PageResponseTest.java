package com.ryuqq.crawlinghub.application.common.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * PageResponse 단위 테스트
 *
 * <p>오프셋 기반 페이징 응답 DTO 검증
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("PageResponse 테스트")
class PageResponseTest {

    @Nested
    @DisplayName("of() 팩토리 메서드 테스트")
    class Of {

        @Test
        @DisplayName("[성공] 모든 필드로 PageResponse 생성")
        void shouldCreatePageResponseWithAllFields() {
            // Given
            List<String> content = List.of("item1", "item2");

            // When
            PageResponse<String> response = PageResponse.of(content, 0, 20, 100L, 5, true, false);

            // Then
            assertThat(response.content()).containsExactly("item1", "item2");
            assertThat(response.page()).isZero();
            assertThat(response.size()).isEqualTo(20);
            assertThat(response.totalElements()).isEqualTo(100L);
            assertThat(response.totalPages()).isEqualTo(5);
            assertThat(response.first()).isTrue();
            assertThat(response.last()).isFalse();
        }

        @Test
        @DisplayName("[성공] 마지막 페이지 PageResponse 생성")
        void shouldCreateLastPageResponse() {
            // Given
            List<String> content = List.of("lastItem");

            // When
            PageResponse<String> response = PageResponse.of(content, 4, 20, 81L, 5, false, true);

            // Then
            assertThat(response.page()).isEqualTo(4);
            assertThat(response.last()).isTrue();
            assertThat(response.first()).isFalse();
        }
    }

    @Nested
    @DisplayName("empty() 팩토리 메서드 테스트")
    class Empty {

        @Test
        @DisplayName("[성공] 빈 PageResponse 생성")
        void shouldCreateEmptyPageResponse() {
            // When
            PageResponse<String> response = PageResponse.empty(0, 20);

            // Then
            assertThat(response.content()).isEmpty();
            assertThat(response.page()).isZero();
            assertThat(response.size()).isEqualTo(20);
            assertThat(response.totalElements()).isZero();
            assertThat(response.totalPages()).isZero();
            assertThat(response.first()).isTrue();
            assertThat(response.last()).isTrue();
        }
    }
}
