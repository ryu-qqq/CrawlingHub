package com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * SliceApiResponse 단위 테스트
 *
 * <p>Cursor 기반 슬라이스 응답 DTO의 defensive copy 및 불변성 보장 로직을 테스트합니다.
 *
 * <p><strong>테스트 범위:</strong>
 *
 * <ul>
 *   <li>Compact Constructor의 defensive copy 검증
 *   <li>of() 팩토리 메서드 검증
 *   <li>content 리스트 불변성 검증
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("rest-api")
@Tag("dto")
@DisplayName("SliceApiResponse 단위 테스트")
class SliceApiResponseTest {

    @Nested
    @DisplayName("생성자 테스트")
    class ConstructorTests {

        @Test
        @DisplayName("content의 defensive copy가 생성된다")
        void shouldCreateDefensiveCopyOfContent() {
            // Given
            List<String> mutableContent = new ArrayList<>(List.of("a", "b", "c"));
            SliceApiResponse<String> response =
                    new SliceApiResponse<>(mutableContent, 20, true, "cursor-123");

            // When
            mutableContent.add("d");

            // Then
            assertThat(response.content()).hasSize(3);
            assertThat(response.content()).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("빈 content 리스트로 생성할 수 있다")
        void shouldCreateWithEmptyContent() {
            // When
            SliceApiResponse<String> response = new SliceApiResponse<>(List.of(), 20, false, null);

            // Then
            assertThat(response.content()).isEmpty();
            assertThat(response.size()).isEqualTo(20);
            assertThat(response.hasNext()).isFalse();
            assertThat(response.nextCursor()).isNull();
        }

        @Test
        @DisplayName("null content로 생성하면 NullPointerException이 발생한다")
        void shouldThrowNullPointerExceptionWhenContentIsNull() {
            // When & Then
            assertThatThrownBy(() -> new SliceApiResponse<>(null, 20, false, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("of() 팩토리 메서드 테스트")
    class OfFactoryMethodTests {

        @Test
        @DisplayName("정상적으로 SliceApiResponse를 생성한다")
        void shouldCreateSliceApiResponseSuccessfully() {
            // Given
            List<String> content = List.of("item1", "item2", "item3");

            // When
            SliceApiResponse<String> response = SliceApiResponse.of(content, 20, false, null);

            // Then
            assertThat(response.content()).containsExactly("item1", "item2", "item3");
            assertThat(response.size()).isEqualTo(20);
            assertThat(response.hasNext()).isFalse();
            assertThat(response.nextCursor()).isNull();
        }

        @Test
        @DisplayName("hasNext가 true이고 nextCursor가 설정된 경우")
        void shouldCreateResponseWithHasNextTrueAndNextCursor() {
            // Given
            List<String> content = List.of("item1", "item2");
            String nextCursor = "cursor-abc-456";

            // When
            SliceApiResponse<String> response = SliceApiResponse.of(content, 2, true, nextCursor);

            // Then
            assertThat(response.content()).containsExactly("item1", "item2");
            assertThat(response.size()).isEqualTo(2);
            assertThat(response.hasNext()).isTrue();
            assertThat(response.nextCursor()).isEqualTo("cursor-abc-456");
        }

        @Test
        @DisplayName("hasNext가 false이고 nextCursor가 null인 경우 (마지막 페이지)")
        void shouldCreateLastPageResponseWithHasNextFalseAndNullCursor() {
            // Given
            List<String> content = List.of("item1");

            // When
            SliceApiResponse<String> response = SliceApiResponse.of(content, 20, false, null);

            // Then
            assertThat(response.hasNext()).isFalse();
            assertThat(response.nextCursor()).isNull();
        }
    }

    @Nested
    @DisplayName("불변성 테스트")
    class ImmutabilityTests {

        @Test
        @DisplayName("content 리스트가 불변이다")
        void shouldReturnUnmodifiableContent() {
            // Given
            SliceApiResponse<String> response =
                    SliceApiResponse.of(List.of("a", "b", "c"), 20, false, null);

            // When & Then
            assertThatThrownBy(() -> response.content().add("d"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
