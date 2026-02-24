package com.ryuqq.crawlinghub.application.monitoring.dto.composite;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * CrawlTaskSummaryResult 단위 테스트
 *
 * <p>불변성 및 생성 검증
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("CrawlTaskSummaryResult 테스트")
class CrawlTaskSummaryResultTest {

    @Nested
    @DisplayName("생성 테스트")
    class Constructor {

        @Test
        @DisplayName("[성공] 정상 값으로 생성")
        void shouldCreateWithValidValues() {
            // Given
            Map<String, Long> countsByStatus = Map.of("WAITING", 5L, "SUCCESS", 100L);

            // When
            CrawlTaskSummaryResult result = new CrawlTaskSummaryResult(countsByStatus, 2L, 107L);

            // Then
            assertThat(result.countsByStatus()).containsKey("WAITING");
            assertThat(result.stuckTasks()).isEqualTo(2L);
            assertThat(result.totalTasks()).isEqualTo(107L);
        }

        @Test
        @DisplayName("[성공] countsByStatus는 불변 Map으로 저장됨")
        void shouldStoreCountsByStatusAsImmutableMap() {
            // Given
            Map<String, Long> mutableMap = new HashMap<>();
            mutableMap.put("WAITING", 10L);

            CrawlTaskSummaryResult result = new CrawlTaskSummaryResult(mutableMap, 0L, 10L);

            // Then - 내부 맵은 불변이어야 함
            assertThatThrownBy(() -> result.countsByStatus().put("NEW_KEY", 1L))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("[성공] 빈 countsByStatus로 생성 가능")
        void shouldCreateWithEmptyCountsByStatus() {
            // When
            CrawlTaskSummaryResult result = new CrawlTaskSummaryResult(Map.of(), 0L, 0L);

            // Then
            assertThat(result.countsByStatus()).isEmpty();
            assertThat(result.totalTasks()).isZero();
        }
    }
}
