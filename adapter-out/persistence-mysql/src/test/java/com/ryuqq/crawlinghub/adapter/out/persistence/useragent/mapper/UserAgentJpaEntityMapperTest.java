package com.ryuqq.crawlinghub.adapter.out.persistence.useragent.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.cralwinghub.domain.fixture.useragent.UserAgentFixture;
import com.ryuqq.crawlinghub.adapter.out.persistence.useragent.entity.UserAgentJpaEntity;
import com.ryuqq.crawlinghub.domain.useragent.aggregate.UserAgent;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentStatus;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * UserAgentJpaEntityMapper 단위 테스트
 *
 * <p>Domain ↔ Entity 양방향 변환 검증
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("persistence")
@Tag("mapper")
@DisplayName("UserAgentJpaEntityMapper 단위 테스트")
class UserAgentJpaEntityMapperTest {

    private UserAgentJpaEntityMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new UserAgentJpaEntityMapper();
    }

    @Nested
    @DisplayName("toEntity - Domain → Entity 변환")
    class ToEntityTests {

        @Test
        @DisplayName("성공 - AVAILABLE 상태 UserAgent 변환")
        void shouldConvertAvailableUserAgentToEntity() {
            // Given
            UserAgent domain = UserAgentFixture.anAvailableUserAgent();

            // When
            UserAgentJpaEntity entity = mapper.toEntity(domain);

            // Then
            assertThat(entity).isNotNull();
            assertThat(entity.getId()).isEqualTo(domain.getId().value());
            assertThat(entity.getToken()).isEqualTo(domain.getToken().encryptedValue());
            assertThat(entity.getUserAgentString()).isEqualTo(domain.getUserAgentString().value());
            assertThat(entity.getDeviceType()).isEqualTo(domain.getDeviceType().getTypeName());
            assertThat(entity.getStatus()).isEqualTo(UserAgentStatus.AVAILABLE);
            assertThat(entity.getHealthScore()).isEqualTo(domain.getHealthScoreValue());
            assertThat(entity.getRequestsPerDay()).isEqualTo(domain.getRequestsPerDay());
        }

        @Test
        @DisplayName("성공 - SUSPENDED 상태 UserAgent 변환")
        void shouldConvertSuspendedUserAgentToEntity() {
            // Given
            UserAgent domain = UserAgentFixture.aSuspendedUserAgent();

            // When
            UserAgentJpaEntity entity = mapper.toEntity(domain);

            // Then
            assertThat(entity.getStatus()).isEqualTo(UserAgentStatus.SUSPENDED);
        }

        @Test
        @DisplayName("성공 - BLOCKED 상태 UserAgent 변환")
        void shouldConvertBlockedUserAgentToEntity() {
            // Given
            UserAgent domain = UserAgentFixture.aBlockedUserAgent();

            // When
            UserAgentJpaEntity entity = mapper.toEntity(domain);

            // Then
            assertThat(entity.getStatus()).isEqualTo(UserAgentStatus.BLOCKED);
        }

        @Test
        @DisplayName("성공 - 높은 사용량 UserAgent 변환")
        void shouldConvertHighUsageUserAgentToEntity() {
            // Given
            UserAgent domain = UserAgentFixture.aHighUsageUserAgent();

            // When
            UserAgentJpaEntity entity = mapper.toEntity(domain);

            // Then
            assertThat(entity.getRequestsPerDay()).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("toDomain - Entity → Domain 변환")
    class ToDomainTests {

        @Test
        @DisplayName("성공 - AVAILABLE Entity를 Domain으로 변환")
        void shouldConvertEntityToAvailableDomain() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            UserAgentJpaEntity entity =
                    UserAgentJpaEntity.of(
                            1L,
                            "encrypted-token",
                            "Mozilla/5.0 Test Browser",
                            "DESKTOP",
                            UserAgentStatus.AVAILABLE,
                            100,
                            now,
                            0,
                            now,
                            now);

            // When
            UserAgent domain = mapper.toDomain(entity);

            // Then
            assertThat(domain).isNotNull();
            assertThat(domain.getId().value()).isEqualTo(1L);
            assertThat(domain.getToken().encryptedValue()).isEqualTo("encrypted-token");
            assertThat(domain.getUserAgentString().value()).isEqualTo("Mozilla/5.0 Test Browser");
            assertThat(domain.getDeviceType().getTypeName()).isEqualTo("DESKTOP");
            assertThat(domain.getStatus()).isEqualTo(UserAgentStatus.AVAILABLE);
            assertThat(domain.getHealthScoreValue()).isEqualTo(100);
        }

        @Test
        @DisplayName("성공 - SUSPENDED Entity를 Domain으로 변환")
        void shouldConvertEntityToSuspendedDomain() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            UserAgentJpaEntity entity =
                    UserAgentJpaEntity.of(
                            2L,
                            "token-2",
                            "Mozilla/5.0 Test",
                            "MOBILE",
                            UserAgentStatus.SUSPENDED,
                            25,
                            now,
                            50,
                            now,
                            now);

            // When
            UserAgent domain = mapper.toDomain(entity);

            // Then
            assertThat(domain.getStatus()).isEqualTo(UserAgentStatus.SUSPENDED);
            assertThat(domain.getHealthScoreValue()).isEqualTo(25);
        }

        @Test
        @DisplayName("성공 - lastUsedAt이 null인 Entity 변환")
        void shouldConvertEntityWithNullLastUsedAt() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            UserAgentJpaEntity entity =
                    UserAgentJpaEntity.of(
                            3L,
                            "token-3",
                            "Mozilla/5.0",
                            "DESKTOP",
                            UserAgentStatus.AVAILABLE,
                            100,
                            null, // lastUsedAt is null
                            0,
                            now,
                            now);

            // When
            UserAgent domain = mapper.toDomain(entity);

            // Then
            assertThat(domain.getLastUsedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("양방향 변환 일관성")
    class RoundTripTests {

        @Test
        @DisplayName("성공 - Domain → Entity → Domain 변환 일관성")
        void shouldMaintainConsistencyInRoundTrip() {
            // Given
            UserAgent original = UserAgentFixture.anAvailableUserAgent();

            // When
            UserAgentJpaEntity entity = mapper.toEntity(original);
            UserAgent restored = mapper.toDomain(entity);

            // Then
            assertThat(restored.getId().value()).isEqualTo(original.getId().value());
            assertThat(restored.getToken().encryptedValue())
                    .isEqualTo(original.getToken().encryptedValue());
            assertThat(restored.getUserAgentString().value())
                    .isEqualTo(original.getUserAgentString().value());
            assertThat(restored.getDeviceType().getTypeName())
                    .isEqualTo(original.getDeviceType().getTypeName());
            assertThat(restored.getStatus()).isEqualTo(original.getStatus());
            assertThat(restored.getHealthScoreValue()).isEqualTo(original.getHealthScoreValue());
            assertThat(restored.getRequestsPerDay()).isEqualTo(original.getRequestsPerDay());
        }

        @Test
        @DisplayName("성공 - 다양한 상태의 양방향 변환")
        void shouldMaintainConsistencyForVariousStatuses() {
            // Given
            UserAgent suspended = UserAgentFixture.aSuspendedUserAgent();
            UserAgent blocked = UserAgentFixture.aBlockedUserAgent();

            // When & Then - Suspended
            UserAgent restoredSuspended = mapper.toDomain(mapper.toEntity(suspended));
            assertThat(restoredSuspended.getStatus()).isEqualTo(UserAgentStatus.SUSPENDED);

            // When & Then - Blocked
            UserAgent restoredBlocked = mapper.toDomain(mapper.toEntity(blocked));
            assertThat(restoredBlocked.getStatus()).isEqualTo(UserAgentStatus.BLOCKED);
        }
    }

    @Nested
    @DisplayName("시간 변환")
    class TimeConversionTests {

        @Test
        @DisplayName("성공 - Instant → LocalDateTime → Instant 변환 일관성")
        void shouldConvertTimesConsistently() {
            // Given
            UserAgent domain = UserAgentFixture.anAvailableUserAgent();

            // When
            UserAgentJpaEntity entity = mapper.toEntity(domain);
            UserAgent restored = mapper.toDomain(entity);

            // Then - 시간대 변환으로 인한 오차 허용 (1초 이내)
            assertThat(restored.getCreatedAt())
                    .isCloseTo(
                            domain.getCreatedAt(),
                            org.assertj.core.api.Assertions.within(1, ChronoUnit.SECONDS));
            assertThat(restored.getUpdatedAt())
                    .isCloseTo(
                            domain.getUpdatedAt(),
                            org.assertj.core.api.Assertions.within(1, ChronoUnit.SECONDS));
        }
    }
}
