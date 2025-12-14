package com.ryuqq.crawlinghub.adapter.out.persistence.schedule.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerOutBoxFixture;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.CrawlSchedulerOutBoxJpaEntity;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerOutBox;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerOubBoxStatus;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * CrawlSchedulerOutBoxJpaEntityMapper 단위 테스트
 *
 * <p>Domain ↔ Entity 양방향 변환 검증
 *
 * <p>CrawlSchedulerOutBox 특수 사항:
 *
 * <ul>
 *   <li>Optimistic Locking (@Version)
 *   <li>eventPayload는 JSON 형식
 *   <li>errorMessage는 FAILED 상태에서만 값이 있음
 *   <li>processedAt은 null 가능 (아직 처리 전)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("persistence")
@Tag("mapper")
@DisplayName("CrawlSchedulerOutBoxJpaEntityMapper 단위 테스트")
class CrawlSchedulerOutBoxJpaEntityMapperTest {

    private CrawlSchedulerOutBoxJpaEntityMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CrawlSchedulerOutBoxJpaEntityMapper();
    }

    @Nested
    @DisplayName("toEntity - Domain → Entity 변환")
    class ToEntityTests {

        @Test
        @DisplayName("성공 - PENDING 상태 OutBox 변환")
        void shouldConvertPendingOutBoxToEntity() {
            // Given
            CrawlSchedulerOutBox domain = CrawlSchedulerOutBoxFixture.aPendingOutBox();

            // When
            CrawlSchedulerOutBoxJpaEntity entity = mapper.toEntity(domain);

            // Then
            assertThat(entity).isNotNull();
            assertThat(entity.getId()).isEqualTo(domain.getOutBoxIdValue());
            assertThat(entity.getHistoryId()).isEqualTo(domain.getHistoryIdValue());
            assertThat(entity.getStatus()).isEqualTo(CrawlSchedulerOubBoxStatus.PENDING);
            assertThat(entity.getEventPayload()).isEqualTo(domain.getEventPayload());
            assertThat(entity.getErrorMessage()).isNull();
            assertThat(entity.getVersion()).isEqualTo(0L);
            assertThat(entity.getCreatedAt()).isNotNull();
            assertThat(entity.getProcessedAt()).isNull();
        }

        @Test
        @DisplayName("성공 - COMPLETED 상태 OutBox 변환")
        void shouldConvertCompletedOutBoxToEntity() {
            // Given
            CrawlSchedulerOutBox domain = CrawlSchedulerOutBoxFixture.aCompletedOutBox();

            // When
            CrawlSchedulerOutBoxJpaEntity entity = mapper.toEntity(domain);

            // Then
            assertThat(entity.getStatus()).isEqualTo(CrawlSchedulerOubBoxStatus.COMPLETED);
            assertThat(entity.getProcessedAt()).isNotNull();
            assertThat(entity.getVersion()).isEqualTo(1L);
        }

        @Test
        @DisplayName("성공 - FAILED 상태 OutBox 변환 (에러 메시지 포함)")
        void shouldConvertFailedOutBoxToEntity() {
            // Given
            CrawlSchedulerOutBox domain = CrawlSchedulerOutBoxFixture.aFailedOutBox();

            // When
            CrawlSchedulerOutBoxJpaEntity entity = mapper.toEntity(domain);

            // Then
            assertThat(entity.getStatus()).isEqualTo(CrawlSchedulerOubBoxStatus.FAILED);
            assertThat(entity.getErrorMessage()).isEqualTo("EventBridge connection failed");
            assertThat(entity.getProcessedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("toDomain - Entity → Domain 변환")
    class ToDomainTests {

        @Test
        @DisplayName("성공 - PENDING Entity를 Domain으로 변환")
        void shouldConvertEntityToPendingDomain() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            CrawlSchedulerOutBoxJpaEntity entity =
                    CrawlSchedulerOutBoxJpaEntity.of(
                            1L,
                            100L,
                            CrawlSchedulerOubBoxStatus.PENDING,
                            "{\"schedulerId\": 1, \"action\": \"CREATE\"}",
                            null,
                            0L,
                            now,
                            null);

            // When
            CrawlSchedulerOutBox domain = mapper.toDomain(entity);

            // Then
            assertThat(domain).isNotNull();
            assertThat(domain.getOutBoxIdValue()).isEqualTo(1L);
            assertThat(domain.getHistoryIdValue()).isEqualTo(100L);
            assertThat(domain.getStatus()).isEqualTo(CrawlSchedulerOubBoxStatus.PENDING);
            assertThat(domain.getEventPayload())
                    .isEqualTo("{\"schedulerId\": 1, \"action\": \"CREATE\"}");
            assertThat(domain.getErrorMessage()).isNull();
            assertThat(domain.getVersion()).isEqualTo(0L);
            assertThat(domain.getProcessedAt()).isNull();
        }

        @Test
        @DisplayName("성공 - COMPLETED Entity를 Domain으로 변환")
        void shouldConvertEntityToCompletedDomain() {
            // Given
            LocalDateTime createdAt = LocalDateTime.now().minusMinutes(5);
            LocalDateTime processedAt = LocalDateTime.now();
            CrawlSchedulerOutBoxJpaEntity entity =
                    CrawlSchedulerOutBoxJpaEntity.of(
                            2L,
                            200L,
                            CrawlSchedulerOubBoxStatus.COMPLETED,
                            "{\"schedulerId\": 2}",
                            null,
                            1L,
                            createdAt,
                            processedAt);

            // When
            CrawlSchedulerOutBox domain = mapper.toDomain(entity);

            // Then
            assertThat(domain.getStatus()).isEqualTo(CrawlSchedulerOubBoxStatus.COMPLETED);
            assertThat(domain.getProcessedAt()).isNotNull();
            assertThat(domain.getVersion()).isEqualTo(1L);
        }

        @Test
        @DisplayName("성공 - FAILED Entity를 Domain으로 변환")
        void shouldConvertEntityToFailedDomain() {
            // Given
            LocalDateTime createdAt = LocalDateTime.now().minusMinutes(10);
            LocalDateTime processedAt = LocalDateTime.now().minusMinutes(5);
            CrawlSchedulerOutBoxJpaEntity entity =
                    CrawlSchedulerOutBoxJpaEntity.of(
                            3L,
                            300L,
                            CrawlSchedulerOubBoxStatus.FAILED,
                            "{\"schedulerId\": 3}",
                            "EventBridge connection failed",
                            1L,
                            createdAt,
                            processedAt);

            // When
            CrawlSchedulerOutBox domain = mapper.toDomain(entity);

            // Then
            assertThat(domain.getStatus()).isEqualTo(CrawlSchedulerOubBoxStatus.FAILED);
            assertThat(domain.getErrorMessage()).isEqualTo("EventBridge connection failed");
        }
    }

    @Nested
    @DisplayName("양방향 변환 일관성")
    class RoundTripTests {

        @Test
        @DisplayName("성공 - PENDING Domain → Entity → Domain 변환 일관성")
        void shouldMaintainConsistencyForPendingOutBox() {
            // Given
            CrawlSchedulerOutBox original = CrawlSchedulerOutBoxFixture.aPendingOutBox();

            // When
            CrawlSchedulerOutBoxJpaEntity entity = mapper.toEntity(original);
            CrawlSchedulerOutBox restored = mapper.toDomain(entity);

            // Then
            assertThat(restored.getOutBoxIdValue()).isEqualTo(original.getOutBoxIdValue());
            assertThat(restored.getHistoryIdValue()).isEqualTo(original.getHistoryIdValue());
            assertThat(restored.getStatus()).isEqualTo(original.getStatus());
            assertThat(restored.getEventPayload()).isEqualTo(original.getEventPayload());
            assertThat(restored.getVersion()).isEqualTo(original.getVersion());
        }

        @Test
        @DisplayName("성공 - COMPLETED Domain → Entity → Domain 변환 일관성")
        void shouldMaintainConsistencyForCompletedOutBox() {
            // Given
            CrawlSchedulerOutBox original = CrawlSchedulerOutBoxFixture.aCompletedOutBox();

            // When
            CrawlSchedulerOutBoxJpaEntity entity = mapper.toEntity(original);
            CrawlSchedulerOutBox restored = mapper.toDomain(entity);

            // Then
            assertThat(restored.getStatus()).isEqualTo(CrawlSchedulerOubBoxStatus.COMPLETED);
            assertThat(restored.getProcessedAt()).isNotNull();
        }

        @Test
        @DisplayName("성공 - 다양한 상태의 양방향 변환")
        void shouldMaintainConsistencyForVariousStatuses() {
            // Given
            CrawlSchedulerOutBox pending = CrawlSchedulerOutBoxFixture.aPendingOutBox();
            CrawlSchedulerOutBox completed = CrawlSchedulerOutBoxFixture.aCompletedOutBox();
            CrawlSchedulerOutBox failed = CrawlSchedulerOutBoxFixture.aFailedOutBox();

            // When & Then - Pending
            CrawlSchedulerOutBox restoredPending = mapper.toDomain(mapper.toEntity(pending));
            assertThat(restoredPending.getStatus()).isEqualTo(CrawlSchedulerOubBoxStatus.PENDING);

            // When & Then - Completed
            CrawlSchedulerOutBox restoredCompleted = mapper.toDomain(mapper.toEntity(completed));
            assertThat(restoredCompleted.getStatus())
                    .isEqualTo(CrawlSchedulerOubBoxStatus.COMPLETED);

            // When & Then - Failed
            CrawlSchedulerOutBox restoredFailed = mapper.toDomain(mapper.toEntity(failed));
            assertThat(restoredFailed.getStatus()).isEqualTo(CrawlSchedulerOubBoxStatus.FAILED);
        }
    }

    @Nested
    @DisplayName("시간 변환")
    class TimeConversionTests {

        @Test
        @DisplayName("성공 - Instant → LocalDateTime → Instant 변환 일관성")
        void shouldConvertTimesConsistently() {
            // Given
            CrawlSchedulerOutBox domain = CrawlSchedulerOutBoxFixture.aCompletedOutBox();

            // When
            CrawlSchedulerOutBoxJpaEntity entity = mapper.toEntity(domain);
            CrawlSchedulerOutBox restored = mapper.toDomain(entity);

            // Then - 시간대 변환으로 인한 오차 허용 (1초 이내)
            assertThat(restored.getCreatedAt())
                    .isCloseTo(
                            domain.getCreatedAt(),
                            org.assertj.core.api.Assertions.within(1, ChronoUnit.SECONDS));
            assertThat(restored.getProcessedAt())
                    .isCloseTo(
                            domain.getProcessedAt(),
                            org.assertj.core.api.Assertions.within(1, ChronoUnit.SECONDS));
        }

        @Test
        @DisplayName("성공 - null processedAt 처리")
        void shouldHandleNullProcessedAt() {
            // Given
            CrawlSchedulerOutBox domain = CrawlSchedulerOutBoxFixture.aPendingOutBox();

            // When
            CrawlSchedulerOutBoxJpaEntity entity = mapper.toEntity(domain);
            CrawlSchedulerOutBox restored = mapper.toDomain(entity);

            // Then
            assertThat(restored.getProcessedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("Optimistic Locking")
    class OptimisticLockingTests {

        @Test
        @DisplayName("성공 - version 필드 변환")
        void shouldConvertVersionField() {
            // Given - 초기 버전 0
            CrawlSchedulerOutBox pending = CrawlSchedulerOutBoxFixture.aPendingOutBox();
            // Given - 처리 후 버전 1
            CrawlSchedulerOutBox completed = CrawlSchedulerOutBoxFixture.aCompletedOutBox();

            // When
            CrawlSchedulerOutBoxJpaEntity pendingEntity = mapper.toEntity(pending);
            CrawlSchedulerOutBoxJpaEntity completedEntity = mapper.toEntity(completed);

            // Then
            assertThat(pendingEntity.getVersion()).isEqualTo(0L);
            assertThat(completedEntity.getVersion()).isEqualTo(1L);
        }
    }
}
