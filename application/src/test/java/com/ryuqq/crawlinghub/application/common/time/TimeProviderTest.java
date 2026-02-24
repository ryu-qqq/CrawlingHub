package com.ryuqq.crawlinghub.application.common.time;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * TimeProvider 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TimeProvider 테스트")
class TimeProviderTest {

    @Mock private Clock clock;

    @InjectMocks private TimeProvider timeProvider;

    @Nested
    @DisplayName("now() 테스트")
    class Now {

        @Test
        @DisplayName("[성공] Clock에서 현재 Instant 반환")
        void shouldReturnCurrentInstantFromClock() {
            // Given
            Instant fixedInstant = Instant.parse("2024-01-15T10:00:00Z");
            given(clock.instant()).willReturn(fixedInstant);

            // When
            Instant result = timeProvider.now();

            // Then
            assertThat(result).isEqualTo(fixedInstant);
        }

        @Test
        @DisplayName("[성공] 고정 Clock으로 결정론적 시간 반환")
        void shouldReturnDeterministicTimeWithFixedClock() {
            // Given
            Clock fixedClock = Clock.fixed(Instant.parse("2024-06-15T12:30:00Z"), ZoneId.of("UTC"));
            TimeProvider provider = new TimeProvider(fixedClock);

            // When
            Instant first = provider.now();
            Instant second = provider.now();

            // Then
            assertThat(first).isEqualTo(second);
            assertThat(first).isEqualTo(Instant.parse("2024-06-15T12:30:00Z"));
        }

        @Test
        @DisplayName("[성공] 두 번 호출 시 Clock.instant() 두 번 호출")
        void shouldCallClockInstantOnEachInvocation() {
            // Given
            Instant first = Instant.parse("2024-01-01T00:00:00Z");
            Instant second = Instant.parse("2024-01-01T00:01:00Z");
            given(clock.instant()).willReturn(first, second);

            // When
            Instant result1 = timeProvider.now();
            Instant result2 = timeProvider.now();

            // Then
            assertThat(result1).isEqualTo(first);
            assertThat(result2).isEqualTo(second);
        }
    }
}
