package com.ryuqq.crawlinghub.application.monitoring.dto.composite;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.application.monitoring.dto.composite.ExternalSystemHealthResult.SystemHealth;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * ExternalSystemHealthResult 단위 테스트
 *
 * <p>외부 시스템 상태 결과 생성 검증
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("ExternalSystemHealthResult 테스트")
class ExternalSystemHealthResultTest {

    @Nested
    @DisplayName("SystemHealth 생성 테스트")
    class SystemHealthConstructor {

        @Test
        @DisplayName("[성공] SystemHealth 정상 생성")
        void shouldCreateSystemHealth() {
            // When
            SystemHealth health = new SystemHealth("EventBridge", 0L, "HEALTHY");

            // Then
            assertThat(health.system()).isEqualTo("EventBridge");
            assertThat(health.recentFailures()).isZero();
            assertThat(health.status()).isEqualTo("HEALTHY");
        }

        @Test
        @DisplayName("[성공] 실패가 있는 SystemHealth 생성")
        void shouldCreateSystemHealthWithFailures() {
            // When
            SystemHealth health = new SystemHealth("SQS", 3L, "WARNING");

            // Then
            assertThat(health.system()).isEqualTo("SQS");
            assertThat(health.recentFailures()).isEqualTo(3L);
            assertThat(health.status()).isEqualTo("WARNING");
        }
    }

    @Nested
    @DisplayName("ExternalSystemHealthResult 생성 테스트")
    class ExternalSystemHealthResultConstructor {

        @Test
        @DisplayName("[성공] 여러 시스템 상태로 결과 생성")
        void shouldCreateWithMultipleSystems() {
            // Given
            List<SystemHealth> systems =
                    List.of(
                            new SystemHealth("EventBridge", 0L, "HEALTHY"),
                            new SystemHealth("SQS", 2L, "WARNING"),
                            new SystemHealth("S3", 0L, "HEALTHY"));

            // When
            ExternalSystemHealthResult result = new ExternalSystemHealthResult(systems);

            // Then
            assertThat(result.systems()).hasSize(3);
            assertThat(result.systems().get(0).system()).isEqualTo("EventBridge");
        }

        @Test
        @DisplayName("[성공] 빈 시스템 목록으로 생성")
        void shouldCreateWithEmptySystems() {
            // When
            ExternalSystemHealthResult result = new ExternalSystemHealthResult(List.of());

            // Then
            assertThat(result.systems()).isEmpty();
        }
    }
}
