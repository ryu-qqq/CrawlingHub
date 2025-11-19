package com.ryuqq.crawlinghub.application.common.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PageResponseTest {

    @Test
    @DisplayName("should create page response via factory")
    void shouldCreatePageResponse() {
        PageResponse<String> response = PageResponse.of(
            List.of("a", "b"),
            1,
            20,
            200L,
            10,
            false,
            true
        );

        assertThat(response.content()).containsExactly("a", "b");
        assertThat(response.page()).isEqualTo(1);
        assertThat(response.size()).isEqualTo(20);
        assertThat(response.totalElements()).isEqualTo(200L);
        assertThat(response.totalPages()).isEqualTo(10);
        assertThat(response.first()).isFalse();
        assertThat(response.last()).isTrue();
    }

    @Test
    @DisplayName("should create empty page response")
    void shouldCreateEmptyPageResponse() {
        PageResponse<String> response = PageResponse.empty(0, 50);

        assertThat(response.content()).isEmpty();
        assertThat(response.totalElements()).isZero();
        assertThat(response.totalPages()).isZero();
        assertThat(response.first()).isTrue();
        assertThat(response.last()).isTrue();
    }
}

