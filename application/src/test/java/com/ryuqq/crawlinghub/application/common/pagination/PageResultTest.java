package com.ryuqq.crawlinghub.application.common.pagination;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PageResultTest {

    @Test
    @DisplayName("should compute hasNext based on total elements")
    void shouldComputeHasNext() {
        PageResult<Integer> result = new PageResult<>() {
            @Override
            public List<Integer> content() {
                return List.of(1, 2, 3);
            }

            @Override
            public int page() {
                return 0;
            }

            @Override
            public int size() {
                return 3;
            }

            @Override
            public long totalElements() {
                return 10;
            }
        };

        assertThat(result.hasNext()).isTrue();
    }
}

