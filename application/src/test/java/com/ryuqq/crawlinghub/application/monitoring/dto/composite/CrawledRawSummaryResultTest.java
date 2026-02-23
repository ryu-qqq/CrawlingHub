package com.ryuqq.crawlinghub.application.monitoring.dto.composite;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * CrawledRawSummaryResult 단위 테스트
 *
 * <p>크롤 원시 데이터 요약 결과 생성 및 불변성 검증
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("CrawledRawSummaryResult 테스트")
class CrawledRawSummaryResultTest {

    @Nested
    @DisplayName("생성 테스트")
    class Constructor {

        @Test
        @DisplayName("[성공] 정상 값으로 생성")
        void shouldCreateWithValidValues() {
            // Given
            Map<String, Long> countsByStatus = Map.of("PENDING", 50L, "PROCESSED", 300L);

            // When
            CrawledRawSummaryResult result = new CrawledRawSummaryResult(countsByStatus, 350L);

            // Then
            assertThat(result.totalRaw()).isEqualTo(350L);
            assertThat(result.countsByStatus()).containsKey("PENDING");
            assertThat(result.countsByStatus()).containsKey("PROCESSED");
        }

        @Test
        @DisplayName("[성공] countsByStatus는 불변 Map으로 저장됨")
        void shouldStoreCountsByStatusAsImmutableMap() {
            // Given
            Map<String, Long> mutableMap = new HashMap<>();
            mutableMap.put("PENDING", 10L);

            CrawledRawSummaryResult result = new CrawledRawSummaryResult(mutableMap, 10L);

            // Then
            assertThatThrownBy(() -> result.countsByStatus().put("NEW", 1L))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("[성공] 빈 countsByStatus로 생성 가능")
        void shouldCreateWithEmptyCountsByStatus() {
            // When
            CrawledRawSummaryResult result = new CrawledRawSummaryResult(Map.of(), 0L);

            // Then
            assertThat(result.totalRaw()).isZero();
            assertThat(result.countsByStatus()).isEmpty();
        }
    }
}
