package com.ryuqq.crawlinghub.application.common.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SliceResponse")
class SliceResponseTest {

    @Test
    @DisplayName("커서가 있는 SliceResponse를 생성할 수 있다")
    void shouldCreateSliceResponseWithCursor() {
        List<String> content = List.of("item1", "item2", "item3");
        int size = 3;
        boolean hasNext = true;
        String nextCursor = "cursor-123";

        SliceResponse<String> response = SliceResponse.of(content, size, hasNext, nextCursor);

        assertThat(response.content()).isEqualTo(content);
        assertThat(response.size()).isEqualTo(size);
        assertThat(response.hasNext()).isTrue();
        assertThat(response.nextCursor()).isEqualTo(nextCursor);
    }

    @Test
    @DisplayName("커서가 없는 SliceResponse를 생성할 수 있다")
    void shouldCreateSliceResponseWithoutCursor() {
        List<String> content = List.of("item1", "item2");
        int size = 2;
        boolean hasNext = false;

        SliceResponse<String> response = SliceResponse.of(content, size, hasNext);

        assertThat(response.content()).isEqualTo(content);
        assertThat(response.size()).isEqualTo(size);
        assertThat(response.hasNext()).isFalse();
        assertThat(response.nextCursor()).isNull();
    }

    @Test
    @DisplayName("빈 SliceResponse를 생성할 수 있다")
    void shouldCreateEmptySliceResponse() {
        int size = 10;

        SliceResponse<String> response = SliceResponse.empty(size);

        assertThat(response.content()).isEmpty();
        assertThat(response.size()).isEqualTo(size);
        assertThat(response.hasNext()).isFalse();
        assertThat(response.nextCursor()).isNull();
    }

    @Test
    @DisplayName("Record 불변성을 보장한다")
    void shouldBeImmutable() {
        List<String> content1 = List.of("item1");
        List<String> content2 = List.of("item2");

        SliceResponse<String> response1 = SliceResponse.of(content1, 1, false);
        SliceResponse<String> response2 = SliceResponse.of(content2, 1, false);

        assertThat(response1.content()).isNotSameAs(response2.content());
        assertThat(response1.content()).isEqualTo(List.of("item1"));
        assertThat(response2.content()).isEqualTo(List.of("item2"));
    }
}
