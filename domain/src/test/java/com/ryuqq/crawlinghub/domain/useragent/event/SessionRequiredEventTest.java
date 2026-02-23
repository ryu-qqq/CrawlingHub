package com.ryuqq.crawlinghub.domain.useragent.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.crawlinghub.domain.useragent.id.UserAgentId;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@Tag("unit")
@Tag("domain")
@Tag("event")
@DisplayName("SessionRequiredEvent 단위 테스트")
class SessionRequiredEventTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2024-01-15T10:00:00Z");
    private static final UserAgentId USER_AGENT_ID = UserAgentId.of(1L);
    private static final String USER_AGENT_VALUE =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";

    @Nested
    @DisplayName("생성자")
    class Constructor {

        @Test
        @DisplayName("유효한 값으로 이벤트를 생성한다")
        void shouldCreateEventWithValidValues() {
            // When
            SessionRequiredEvent event =
                    new SessionRequiredEvent(USER_AGENT_ID, USER_AGENT_VALUE, FIXED_INSTANT);

            // Then
            assertThat(event.userAgentId()).isEqualTo(USER_AGENT_ID);
            assertThat(event.userAgentValue()).isEqualTo(USER_AGENT_VALUE);
            assertThat(event.occurredAt()).isEqualTo(FIXED_INSTANT);
        }

        @Test
        @DisplayName("userAgentId가 null이면 예외를 던진다")
        void shouldThrowWhenUserAgentIdIsNull() {
            // When & Then
            assertThatThrownBy(
                            () -> new SessionRequiredEvent(null, USER_AGENT_VALUE, FIXED_INSTANT))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("userAgentId");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"  ", "\t", "\n"})
        @DisplayName("userAgentValue가 null이거나 공백이면 예외를 던진다")
        void shouldThrowWhenUserAgentValueIsNullOrBlank(String value) {
            // When & Then
            assertThatThrownBy(() -> new SessionRequiredEvent(USER_AGENT_ID, value, FIXED_INSTANT))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("userAgentValue");
        }

        @Test
        @DisplayName("occurredAt이 null이면 예외를 던진다")
        void shouldThrowWhenOccurredAtIsNull() {
            // When & Then
            assertThatThrownBy(
                            () -> new SessionRequiredEvent(USER_AGENT_ID, USER_AGENT_VALUE, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("occurredAt");
        }
    }

    @Nested
    @DisplayName("팩토리 메서드")
    class FactoryMethod {

        @Test
        @DisplayName("of()로 이벤트를 생성한다")
        void shouldCreateEventWithOf() {
            // When
            SessionRequiredEvent event =
                    SessionRequiredEvent.of(USER_AGENT_ID, USER_AGENT_VALUE, FIXED_INSTANT);

            // Then
            assertThat(event.userAgentId()).isEqualTo(USER_AGENT_ID);
            assertThat(event.userAgentValue()).isEqualTo(USER_AGENT_VALUE);
            assertThat(event.occurredAt()).isEqualTo(FIXED_INSTANT);
        }
    }

    @Nested
    @DisplayName("헬퍼 메서드")
    class HelperMethods {

        @Test
        @DisplayName("getUserAgentIdValue()는 ID 값을 반환한다")
        void shouldReturnUserAgentIdValue() {
            // Given
            SessionRequiredEvent event =
                    new SessionRequiredEvent(USER_AGENT_ID, USER_AGENT_VALUE, FIXED_INSTANT);

            // When
            Long idValue = event.getUserAgentIdValue();

            // Then
            assertThat(idValue).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringMethod {

        @Test
        @DisplayName("toString()은 이벤트 정보를 포함한다")
        void shouldContainEventInfo() {
            // Given
            SessionRequiredEvent event =
                    new SessionRequiredEvent(USER_AGENT_ID, USER_AGENT_VALUE, FIXED_INSTANT);

            // When
            String result = event.toString();

            // Then
            assertThat(result).contains("SessionRequiredEvent");
            assertThat(result).contains("1");
            assertThat(result).contains(FIXED_INSTANT.toString());
        }

        @Test
        @DisplayName("긴 userAgentValue는 50자로 잘린다")
        void shouldTruncateLongUserAgentValue() {
            // Given
            String longValue =
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like"
                            + " Gecko) Chrome/120.0.0.0 Safari/537.36";
            SessionRequiredEvent event =
                    new SessionRequiredEvent(USER_AGENT_ID, longValue, FIXED_INSTANT);

            // When
            String result = event.toString();

            // Then
            assertThat(result).contains("...");
        }

        @Test
        @DisplayName("짧은 userAgentValue는 잘리지 않는다")
        void shouldNotTruncateShortUserAgentValue() {
            // Given
            String shortValue = "Mozilla/5.0";
            SessionRequiredEvent event =
                    new SessionRequiredEvent(USER_AGENT_ID, shortValue, FIXED_INSTANT);

            // When
            String result = event.toString();

            // Then
            assertThat(result).contains(shortValue);
            assertThat(result).doesNotContain("...");
        }
    }

    @Nested
    @DisplayName("DomainEvent 인터페이스")
    class DomainEventInterface {

        @Test
        @DisplayName("DomainEvent 인터페이스를 구현한다")
        void shouldImplementDomainEvent() {
            // Given
            SessionRequiredEvent event =
                    new SessionRequiredEvent(USER_AGENT_ID, USER_AGENT_VALUE, FIXED_INSTANT);

            // When & Then
            assertThat(event)
                    .isInstanceOf(com.ryuqq.crawlinghub.domain.common.event.DomainEvent.class);
        }
    }
}
