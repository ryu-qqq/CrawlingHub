package com.ryuqq.crawlinghub.adapter.out.persistence.execution.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.cralwinghub.domain.fixture.execution.CrawlExecutionFixture;
import com.ryuqq.crawlinghub.adapter.out.persistence.execution.entity.CrawlExecutionJpaEntity;
import com.ryuqq.crawlinghub.domain.execution.aggregate.CrawlExecution;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlExecutionStatus;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * CrawlExecutionJpaEntityMapper 단위 테스트
 *
 * <p>Domain ↔ Entity 양방향 변환 검증
 *
 * <p>CrawlExecution은 중첩 VO를 포함하므로 다음을 검증:
 *
 * <ul>
 *   <li>CrawlExecutionResult (responseBody, httpStatusCode, errorMessage)
 *   <li>ExecutionDuration (startedAt, completedAt, durationMs)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("persistence")
@Tag("mapper")
@DisplayName("CrawlExecutionJpaEntityMapper 단위 테스트")
class CrawlExecutionJpaEntityMapperTest {

    private CrawlExecutionJpaEntityMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CrawlExecutionJpaEntityMapper();
    }

    @Nested
    @DisplayName("toEntity - Domain → Entity 변환")
    class ToEntityTests {

        @Test
        @DisplayName("성공 - RUNNING 상태 실행 변환")
        void shouldConvertRunningExecutionToEntity() {
            // Given
            CrawlExecution domain = CrawlExecutionFixture.aRunningExecution();

            // When
            CrawlExecutionJpaEntity entity = mapper.toEntity(domain);

            // Then
            assertThat(entity).isNotNull();
            assertThat(entity.getId()).isEqualTo(domain.getId().value());
            assertThat(entity.getCrawlTaskId()).isEqualTo(domain.getCrawlTaskId().value());
            assertThat(entity.getCrawlSchedulerId())
                    .isEqualTo(domain.getCrawlSchedulerId().value());
            assertThat(entity.getSellerId()).isEqualTo(domain.getSellerId().value());
            assertThat(entity.getStatus()).isEqualTo(CrawlExecutionStatus.RUNNING);
            assertThat(entity.getStartedAt()).isNotNull();
            assertThat(entity.getCompletedAt()).isNull();
            assertThat(entity.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("성공 - SUCCESS 상태 실행 변환 (Result 포함)")
        void shouldConvertSuccessExecutionToEntity() {
            // Given
            CrawlExecution domain = CrawlExecutionFixture.aSuccessExecution();

            // When
            CrawlExecutionJpaEntity entity = mapper.toEntity(domain);

            // Then
            assertThat(entity.getStatus()).isEqualTo(CrawlExecutionStatus.SUCCESS);
            assertThat(entity.getResponseBody()).isNotNull();
            assertThat(entity.getHttpStatusCode()).isEqualTo(200);
            assertThat(entity.getErrorMessage()).isNull();
            assertThat(entity.getStartedAt()).isNotNull();
            assertThat(entity.getCompletedAt()).isNotNull();
            assertThat(entity.getDurationMs()).isNotNull();
        }

        @Test
        @DisplayName("성공 - FAILED 상태 실행 변환 (에러 정보 포함)")
        void shouldConvertFailedExecutionToEntity() {
            // Given
            CrawlExecution domain = CrawlExecutionFixture.aFailedExecution();

            // When
            CrawlExecutionJpaEntity entity = mapper.toEntity(domain);

            // Then
            assertThat(entity.getStatus()).isEqualTo(CrawlExecutionStatus.FAILED);
            assertThat(entity.getHttpStatusCode()).isEqualTo(500);
            assertThat(entity.getErrorMessage()).isNotBlank();
        }

        @Test
        @DisplayName("성공 - TIMEOUT 상태 실행 변환")
        void shouldConvertTimeoutExecutionToEntity() {
            // Given
            CrawlExecution domain = CrawlExecutionFixture.aTimeoutExecution();

            // When
            CrawlExecutionJpaEntity entity = mapper.toEntity(domain);

            // Then
            assertThat(entity.getStatus()).isEqualTo(CrawlExecutionStatus.TIMEOUT);
            assertThat(entity.getErrorMessage()).contains("timed out");
        }

        @Test
        @DisplayName("성공 - Rate Limited 실행 변환 (HTTP 429)")
        void shouldConvertRateLimitedExecutionToEntity() {
            // Given
            CrawlExecution domain = CrawlExecutionFixture.aRateLimitedExecution();

            // When
            CrawlExecutionJpaEntity entity = mapper.toEntity(domain);

            // Then
            assertThat(entity.getStatus()).isEqualTo(CrawlExecutionStatus.FAILED);
            assertThat(entity.getHttpStatusCode()).isEqualTo(429);
        }
    }

    @Nested
    @DisplayName("toDomain - Entity → Domain 변환")
    class ToDomainTests {

        @Test
        @DisplayName("성공 - RUNNING Entity를 Domain으로 변환")
        void shouldConvertEntityToRunningDomain() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            CrawlExecutionJpaEntity entity =
                    CrawlExecutionJpaEntity.of(
                            1L,
                            100L,
                            200L,
                            300L,
                            CrawlExecutionStatus.RUNNING,
                            null,
                            null,
                            null,
                            now,
                            null,
                            null,
                            now);

            // When
            CrawlExecution domain = mapper.toDomain(entity);

            // Then
            assertThat(domain).isNotNull();
            assertThat(domain.getId().value()).isEqualTo(1L);
            assertThat(domain.getCrawlTaskId().value()).isEqualTo(100L);
            assertThat(domain.getCrawlSchedulerId().value()).isEqualTo(200L);
            assertThat(domain.getSellerId().value()).isEqualTo(300L);
            assertThat(domain.getStatus()).isEqualTo(CrawlExecutionStatus.RUNNING);
            assertThat(domain.getDuration().isRunning()).isTrue();
        }

        @Test
        @DisplayName("성공 - SUCCESS Entity를 Domain으로 변환")
        void shouldConvertEntityToSuccessDomain() {
            // Given
            LocalDateTime startedAt = LocalDateTime.now().minusSeconds(5);
            LocalDateTime completedAt = LocalDateTime.now();
            CrawlExecutionJpaEntity entity =
                    CrawlExecutionJpaEntity.of(
                            2L,
                            100L,
                            200L,
                            300L,
                            CrawlExecutionStatus.SUCCESS,
                            "{\"status\":\"success\"}",
                            200,
                            null,
                            startedAt,
                            completedAt,
                            5000L,
                            startedAt);

            // When
            CrawlExecution domain = mapper.toDomain(entity);

            // Then
            assertThat(domain.getStatus()).isEqualTo(CrawlExecutionStatus.SUCCESS);
            assertThat(domain.getResult().responseBody()).isEqualTo("{\"status\":\"success\"}");
            assertThat(domain.getResult().httpStatusCode()).isEqualTo(200);
            assertThat(domain.getDuration().durationMs()).isEqualTo(5000L);
        }

        @Test
        @DisplayName("성공 - FAILED Entity를 Domain으로 변환")
        void shouldConvertEntityToFailedDomain() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            CrawlExecutionJpaEntity entity =
                    CrawlExecutionJpaEntity.of(
                            3L,
                            100L,
                            200L,
                            300L,
                            CrawlExecutionStatus.FAILED,
                            null,
                            500,
                            "Internal Server Error",
                            now,
                            now,
                            100L,
                            now);

            // When
            CrawlExecution domain = mapper.toDomain(entity);

            // Then
            assertThat(domain.getStatus()).isEqualTo(CrawlExecutionStatus.FAILED);
            assertThat(domain.getResult().httpStatusCode()).isEqualTo(500);
            assertThat(domain.getResult().errorMessage()).isEqualTo("Internal Server Error");
        }
    }

    @Nested
    @DisplayName("양방향 변환 일관성")
    class RoundTripTests {

        @Test
        @DisplayName("성공 - SUCCESS Domain → Entity → Domain 변환 일관성")
        void shouldMaintainConsistencyForSuccessExecution() {
            // Given
            CrawlExecution original = CrawlExecutionFixture.aSuccessExecution();

            // When
            CrawlExecutionJpaEntity entity = mapper.toEntity(original);
            CrawlExecution restored = mapper.toDomain(entity);

            // Then
            assertThat(restored.getId().value()).isEqualTo(original.getId().value());
            assertThat(restored.getCrawlTaskId().value())
                    .isEqualTo(original.getCrawlTaskId().value());
            assertThat(restored.getCrawlSchedulerId().value())
                    .isEqualTo(original.getCrawlSchedulerId().value());
            assertThat(restored.getSellerId().value()).isEqualTo(original.getSellerId().value());
            assertThat(restored.getStatus()).isEqualTo(original.getStatus());
            assertThat(restored.getResult().responseBody())
                    .isEqualTo(original.getResult().responseBody());
            assertThat(restored.getResult().httpStatusCode())
                    .isEqualTo(original.getResult().httpStatusCode());
            assertThat(restored.getDuration().durationMs())
                    .isEqualTo(original.getDuration().durationMs());
        }

        @Test
        @DisplayName("성공 - FAILED Domain → Entity → Domain 변환 일관성")
        void shouldMaintainConsistencyForFailedExecution() {
            // Given
            CrawlExecution original = CrawlExecutionFixture.aFailedExecution();

            // When
            CrawlExecutionJpaEntity entity = mapper.toEntity(original);
            CrawlExecution restored = mapper.toDomain(entity);

            // Then
            assertThat(restored.getStatus()).isEqualTo(CrawlExecutionStatus.FAILED);
            assertThat(restored.getResult().httpStatusCode())
                    .isEqualTo(original.getResult().httpStatusCode());
            assertThat(restored.getResult().errorMessage())
                    .isEqualTo(original.getResult().errorMessage());
        }

        @Test
        @DisplayName("성공 - RUNNING Domain → Entity → Domain 변환 일관성")
        void shouldMaintainConsistencyForRunningExecution() {
            // Given
            CrawlExecution original = CrawlExecutionFixture.aRunningExecution();

            // When
            CrawlExecutionJpaEntity entity = mapper.toEntity(original);
            CrawlExecution restored = mapper.toDomain(entity);

            // Then
            assertThat(restored.getStatus()).isEqualTo(CrawlExecutionStatus.RUNNING);
            assertThat(restored.getDuration().isRunning()).isTrue();
        }
    }

    @Nested
    @DisplayName("시간 변환")
    class TimeConversionTests {

        @Test
        @DisplayName("성공 - Instant → LocalDateTime → Instant 변환 일관성")
        void shouldConvertTimesConsistently() {
            // Given
            CrawlExecution domain = CrawlExecutionFixture.aSuccessExecution();

            // When
            CrawlExecutionJpaEntity entity = mapper.toEntity(domain);
            CrawlExecution restored = mapper.toDomain(entity);

            // Then - 시간대 변환으로 인한 오차 허용 (1초 이내)
            assertThat(restored.getCreatedAt())
                    .isCloseTo(
                            domain.getCreatedAt(),
                            org.assertj.core.api.Assertions.within(1, ChronoUnit.SECONDS));
            assertThat(restored.getDuration().startedAt())
                    .isCloseTo(
                            domain.getDuration().startedAt(),
                            org.assertj.core.api.Assertions.within(1, ChronoUnit.SECONDS));
            assertThat(restored.getDuration().completedAt())
                    .isCloseTo(
                            domain.getDuration().completedAt(),
                            org.assertj.core.api.Assertions.within(1, ChronoUnit.SECONDS));
        }
    }

    @Nested
    @DisplayName("중첩 VO 변환")
    class NestedVOTests {

        @Test
        @DisplayName("성공 - CrawlExecutionResult null 처리")
        void shouldHandleNullResult() {
            // Given - RUNNING 상태는 Result가 empty
            CrawlExecution domain = CrawlExecutionFixture.aRunningExecution();

            // When
            CrawlExecutionJpaEntity entity = mapper.toEntity(domain);

            // Then - empty result의 경우 null 필드들
            assertThat(entity.getResponseBody()).isNull();
            assertThat(entity.getHttpStatusCode()).isNull();
            assertThat(entity.getErrorMessage()).isNull();
        }

        @Test
        @DisplayName("성공 - ExecutionDuration 완료 상태 변환")
        void shouldConvertCompletedDuration() {
            // Given
            CrawlExecution domain = CrawlExecutionFixture.aSuccessExecution();

            // When
            CrawlExecutionJpaEntity entity = mapper.toEntity(domain);

            // Then
            assertThat(entity.getStartedAt()).isNotNull();
            assertThat(entity.getCompletedAt()).isNotNull();
            assertThat(entity.getDurationMs()).isGreaterThan(0L);
        }

        @Test
        @DisplayName("성공 - ExecutionDuration 실행 중 상태 변환")
        void shouldConvertRunningDuration() {
            // Given
            CrawlExecution domain = CrawlExecutionFixture.aRunningExecution();

            // When
            CrawlExecutionJpaEntity entity = mapper.toEntity(domain);

            // Then
            assertThat(entity.getStartedAt()).isNotNull();
            assertThat(entity.getCompletedAt()).isNull();
            assertThat(entity.getDurationMs()).isNull();
        }
    }
}
