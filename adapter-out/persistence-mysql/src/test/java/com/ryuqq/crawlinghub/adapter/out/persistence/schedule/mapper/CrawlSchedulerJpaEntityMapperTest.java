package com.ryuqq.crawlinghub.adapter.out.persistence.schedule.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerFixture;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.CrawlSchedulerJpaEntity;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * CrawlSchedulerJpaEntityMapper 단위 테스트
 *
 * <p>Domain ↔ Entity 양방향 변환 검증
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("persistence")
@Tag("mapper")
@DisplayName("CrawlSchedulerJpaEntityMapper 단위 테스트")
class CrawlSchedulerJpaEntityMapperTest {

    private CrawlSchedulerJpaEntityMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CrawlSchedulerJpaEntityMapper();
    }

    @Nested
    @DisplayName("toEntity - Domain → Entity 변환")
    class ToEntityTests {

        @Test
        @DisplayName("성공 - 활성 스케줄러 변환")
        void shouldConvertActiveSchedulerToEntity() {
            // Given
            CrawlScheduler domain = CrawlSchedulerFixture.anActiveScheduler();

            // When
            CrawlSchedulerJpaEntity entity = mapper.toEntity(domain);

            // Then
            assertThat(entity).isNotNull();
            assertThat(entity.getId()).isEqualTo(domain.getCrawlSchedulerIdValue());
            assertThat(entity.getSellerId()).isEqualTo(domain.getSellerIdValue());
            assertThat(entity.getSchedulerName()).isEqualTo(domain.getSchedulerNameValue());
            assertThat(entity.getCronExpression()).isEqualTo(domain.getCronExpressionValue());
            assertThat(entity.getStatus()).isEqualTo(SchedulerStatus.ACTIVE);
            assertThat(entity.getCreatedAt()).isNotNull();
            assertThat(entity.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("성공 - 비활성 스케줄러 변환")
        void shouldConvertInactiveSchedulerToEntity() {
            // Given
            CrawlScheduler domain = CrawlSchedulerFixture.anInactiveScheduler();

            // When
            CrawlSchedulerJpaEntity entity = mapper.toEntity(domain);

            // Then
            assertThat(entity.getStatus()).isEqualTo(SchedulerStatus.INACTIVE);
        }

        @Test
        @DisplayName("성공 - 신규 스케줄러 변환 (ID null)")
        void shouldConvertNewSchedulerToEntity() {
            // Given
            CrawlScheduler domain = CrawlSchedulerFixture.aNewActiveScheduler();

            // When
            CrawlSchedulerJpaEntity entity = mapper.toEntity(domain);

            // Then
            assertThat(entity.getId()).isNull();
            assertThat(entity.getSellerId()).isNotNull();
            assertThat(entity.getSchedulerName()).isNotBlank();
            assertThat(entity.getCronExpression()).isNotBlank();
        }
    }

    @Nested
    @DisplayName("toDomain - Entity → Domain 변환")
    class ToDomainTests {

        @Test
        @DisplayName("성공 - 활성 Entity를 Domain으로 변환")
        void shouldConvertEntityToActiveDomain() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            CrawlSchedulerJpaEntity entity =
                    CrawlSchedulerJpaEntity.of(
                            1L,
                            100L,
                            "test-scheduler",
                            "0 0 * * * ?",
                            SchedulerStatus.ACTIVE,
                            now,
                            now);

            // When
            CrawlScheduler domain = mapper.toDomain(entity);

            // Then
            assertThat(domain).isNotNull();
            assertThat(domain.getCrawlSchedulerIdValue()).isEqualTo(1L);
            assertThat(domain.getSellerIdValue()).isEqualTo(100L);
            assertThat(domain.getSchedulerNameValue()).isEqualTo("test-scheduler");
            assertThat(domain.getCronExpressionValue()).isEqualTo("0 0 * * * ?");
            assertThat(domain.getStatus()).isEqualTo(SchedulerStatus.ACTIVE);
            assertThat(domain.getCreatedAt()).isNotNull();
            assertThat(domain.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("성공 - 비활성 Entity를 Domain으로 변환")
        void shouldConvertEntityToInactiveDomain() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            CrawlSchedulerJpaEntity entity =
                    CrawlSchedulerJpaEntity.of(
                            2L,
                            200L,
                            "inactive-scheduler",
                            "0 30 * * * ?",
                            SchedulerStatus.INACTIVE,
                            now,
                            now);

            // When
            CrawlScheduler domain = mapper.toDomain(entity);

            // Then
            assertThat(domain.getStatus()).isEqualTo(SchedulerStatus.INACTIVE);
            assertThat(domain.isActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("양방향 변환 일관성")
    class RoundTripTests {

        @Test
        @DisplayName("성공 - Domain → Entity → Domain 변환 일관성")
        void shouldMaintainConsistencyInRoundTrip() {
            // Given
            CrawlScheduler original = CrawlSchedulerFixture.anActiveScheduler();

            // When
            CrawlSchedulerJpaEntity entity = mapper.toEntity(original);
            CrawlScheduler restored = mapper.toDomain(entity);

            // Then
            assertThat(restored.getCrawlSchedulerIdValue())
                    .isEqualTo(original.getCrawlSchedulerIdValue());
            assertThat(restored.getSellerIdValue()).isEqualTo(original.getSellerIdValue());
            assertThat(restored.getSchedulerNameValue())
                    .isEqualTo(original.getSchedulerNameValue());
            assertThat(restored.getCronExpressionValue())
                    .isEqualTo(original.getCronExpressionValue());
            assertThat(restored.getStatus()).isEqualTo(original.getStatus());
        }
    }

    @Nested
    @DisplayName("시간 변환")
    class TimeConversionTests {

        @Test
        @DisplayName("성공 - Instant → LocalDateTime → Instant 변환 일관성")
        void shouldConvertTimesConsistently() {
            // Given
            CrawlScheduler domain = CrawlSchedulerFixture.anActiveScheduler();

            // When
            CrawlSchedulerJpaEntity entity = mapper.toEntity(domain);
            CrawlScheduler restored = mapper.toDomain(entity);

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
