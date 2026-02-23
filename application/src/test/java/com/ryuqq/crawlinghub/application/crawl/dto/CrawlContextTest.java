package com.ryuqq.crawlinghub.application.crawl.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.domain.execution.vo.CrawlContext;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("application")
@DisplayName("CrawlContext 도메인 VO 단위 테스트")
class CrawlContextTest {

    @Nested
    @DisplayName("hasSearchCookies() 메서드는")
    class HasSearchCookies {

        @Test
        @DisplayName("nid와 mustitUid가 모두 있으면 true를 반환한다")
        void shouldReturnTrueWhenBothValuesPresent() {
            // Given
            CrawlContext context =
                    new CrawlContext(
                            1L,
                            100L,
                            200L,
                            CrawlTaskType.SEARCH,
                            "endpoint",
                            1L,
                            "Mozilla/5.0",
                            "token",
                            "nid-value",
                            "mustit-uid-value");

            // When & Then
            assertThat(context.hasSearchCookies()).isTrue();
        }

        @Test
        @DisplayName("nid가 null이면 false를 반환한다")
        void shouldReturnFalseWhenNidIsNull() {
            // Given
            CrawlContext context =
                    new CrawlContext(
                            1L,
                            100L,
                            200L,
                            CrawlTaskType.SEARCH,
                            "endpoint",
                            1L,
                            "Mozilla/5.0",
                            "token",
                            null,
                            "mustit-uid-value");

            // When & Then
            assertThat(context.hasSearchCookies()).isFalse();
        }

        @Test
        @DisplayName("mustitUid가 blank이면 false를 반환한다")
        void shouldReturnFalseWhenMusitUidIsBlank() {
            // Given
            CrawlContext context =
                    new CrawlContext(
                            1L,
                            100L,
                            200L,
                            CrawlTaskType.SEARCH,
                            "endpoint",
                            1L,
                            "Mozilla/5.0",
                            "token",
                            "nid-value",
                            "");

            // When & Then
            assertThat(context.hasSearchCookies()).isFalse();
        }
    }
}
