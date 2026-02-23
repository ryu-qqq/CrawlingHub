package com.ryuqq.crawlinghub.application.common.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * SliceResponse 단위 테스트
 *
 * <p>커서 기반 페이징 응답 DTO 검증
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("SliceResponse 테스트")
class SliceResponseTest {

    @Nested
    @DisplayName("of(content, size, hasNext, nextCursor) 테스트")
    class OfWithCursor {

        @Test
        @DisplayName("[성공] 커서를 포함하여 SliceResponse 생성")
        void shouldCreateWithCursor() {
            List<String> content = List.of("a", "b", "c");
            SliceResponse<String> response = SliceResponse.of(content, 20, true, "cursor-123");

            assertThat(response.content()).isEqualTo(content);
            assertThat(response.size()).isEqualTo(20);
            assertThat(response.hasNext()).isTrue();
            assertThat(response.nextCursor()).isEqualTo("cursor-123");
        }

        @Test
        @DisplayName("[성공] hasNext가 false인 마지막 페이지 생성")
        void shouldCreateLastPage() {
            List<String> content = List.of("a");
            SliceResponse<String> response = SliceResponse.of(content, 20, false, null);

            assertThat(response.hasNext()).isFalse();
            assertThat(response.nextCursor()).isNull();
        }
    }

    @Nested
    @DisplayName("of(content, size, hasNext) 테스트")
    class OfWithoutCursor {

        @Test
        @DisplayName("[성공] 커서 없이 SliceResponse 생성")
        void shouldCreateWithoutCursor() {
            List<Integer> content = List.of(1, 2, 3);
            SliceResponse<Integer> response = SliceResponse.of(content, 10, true);

            assertThat(response.content()).isEqualTo(content);
            assertThat(response.size()).isEqualTo(10);
            assertThat(response.hasNext()).isTrue();
            assertThat(response.nextCursor()).isNull();
        }
    }

    @Nested
    @DisplayName("empty(size) 테스트")
    class Empty {

        @Test
        @DisplayName("[성공] 빈 SliceResponse 생성")
        void shouldCreateEmptyResponse() {
            SliceResponse<String> response = SliceResponse.empty(20);

            assertThat(response.content()).isEmpty();
            assertThat(response.size()).isEqualTo(20);
            assertThat(response.hasNext()).isFalse();
            assertThat(response.nextCursor()).isNull();
        }

        @Test
        @DisplayName("[성공] size=0인 빈 SliceResponse 생성")
        void shouldCreateEmptyResponseWithZeroSize() {
            SliceResponse<String> response = SliceResponse.empty(0);
            assertThat(response.content()).isEmpty();
            assertThat(response.size()).isZero();
        }
    }
}
