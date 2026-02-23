package com.ryuqq.crawlinghub.application.monitoring.dto.composite;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.crawlinghub.application.monitoring.dto.composite.OutboxSummaryResult.OutboxDetail;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * OutboxSummaryResult 단위 테스트
 *
 * <p>아웃박스 요약 결과 생성 및 불변성 검증
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("OutboxSummaryResult 테스트")
class OutboxSummaryResultTest {

    @Nested
    @DisplayName("OutboxDetail 생성 테스트")
    class OutboxDetailConstructor {

        @Test
        @DisplayName("[성공] OutboxDetail 정상 생성")
        void shouldCreateOutboxDetail() {
            // Given
            Map<String, Long> counts = Map.of("PENDING", 10L, "COMPLETED", 90L);

            // When
            OutboxDetail detail = new OutboxDetail(counts, 100L);

            // Then
            assertThat(detail.total()).isEqualTo(100L);
            assertThat(detail.countsByStatus()).containsKey("PENDING");
            assertThat(detail.countsByStatus()).containsKey("COMPLETED");
        }

        @Test
        @DisplayName("[성공] OutboxDetail countsByStatus는 불변 Map으로 저장됨")
        void shouldStoreCountsByStatusAsImmutableMap() {
            // Given
            Map<String, Long> mutableMap = new HashMap<>();
            mutableMap.put("PENDING", 5L);

            OutboxDetail detail = new OutboxDetail(mutableMap, 5L);

            // Then
            assertThatThrownBy(() -> detail.countsByStatus().put("NEW_KEY", 1L))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("OutboxSummaryResult 생성 테스트")
    class OutboxSummaryResultConstructor {

        @Test
        @DisplayName("[성공] 세 가지 아웃박스 상세 정보로 요약 결과 생성")
        void shouldCreateWithThreeOutboxDetails() {
            // Given
            OutboxDetail crawlTask = new OutboxDetail(Map.of("PENDING", 5L), 5L);
            OutboxDetail scheduler = new OutboxDetail(Map.of("COMPLETED", 20L), 20L);
            OutboxDetail productSync = new OutboxDetail(Map.of("FAILED", 3L), 3L);

            // When
            OutboxSummaryResult result = new OutboxSummaryResult(crawlTask, scheduler, productSync);

            // Then
            assertThat(result.crawlTaskOutbox().total()).isEqualTo(5L);
            assertThat(result.schedulerOutbox().total()).isEqualTo(20L);
            assertThat(result.productSyncOutbox().total()).isEqualTo(3L);
        }
    }
}
