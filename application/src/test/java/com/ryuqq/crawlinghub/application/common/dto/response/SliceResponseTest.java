package com.ryuqq.crawlinghub.application.common.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SliceResponseTest {

    @Test
    @DisplayName("should create slice response with cursor")
    void shouldCreateSliceResponseWithCursor() {
        SliceResponse<String> response = SliceResponse.of(
            List.of("item1"),
            10,
            true,
            "cursor-1"
        );

        assertThat(response.content()).containsExactly("item1");
        assertThat(response.size()).isEqualTo(10);
        assertThat(response.hasNext()).isTrue();
        assertThat(response.nextCursor()).isEqualTo("cursor-1");
    }

    @Test
    @DisplayName("should create empty slice response")
    void shouldCreateEmptySliceResponse() {
        SliceResponse<String> response = SliceResponse.empty(25);

        assertThat(response.content()).isEmpty();
        assertThat(response.hasNext()).isFalse();
        assertThat(response.nextCursor()).isNull();
    }
}

