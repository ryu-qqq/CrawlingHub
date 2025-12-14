package com.ryuqq.crawlinghub.adapter.out.persistence.schedule.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.CrawlSchedulerHistoryJpaEntity;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerHistory;
import com.ryuqq.crawlinghub.domain.schedule.identifier.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerHistoryId;
import com.ryuqq.crawlinghub.domain.schedule.vo.CronExpression;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerName;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * CrawlSchedulerHistoryJpaEntityMapper 단위 테스트
 *
 * <p>Domain ↔ Entity 양방향 변환 검증
 *
 * <p>CrawlSchedulerHistory 특수 사항:
 *
 * <ul>
 *   <li>불변 객체 - 한 번 저장되면 수정되지 않음
 *   <li>updatedAt 필드 없음
 *   <li>스케줄러 변경 이력 스냅샷 저장
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("persistence")
@Tag("mapper")
@DisplayName("CrawlSchedulerHistoryJpaEntityMapper 단위 테스트")
class CrawlSchedulerHistoryJpaEntityMapperTest {

    private CrawlSchedulerHistoryJpaEntityMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CrawlSchedulerHistoryJpaEntityMapper();
    }

    @Nested
    @DisplayName("toEntity - Domain → Entity 변환")
    class ToEntityTests {

        @Test
        @DisplayName("성공 - ACTIVE 상태 이력 변환")
        void shouldConvertActiveHistoryToEntity() {
            // Given
            Instant now = Instant.now();
            CrawlSchedulerHistory domain =
                    CrawlSchedulerHistory.reconstitute(
                            CrawlSchedulerHistoryId.of(1L),
                            CrawlSchedulerId.of(100L),
                            SellerId.of(200L),
                            SchedulerName.of("test-scheduler"),
                            CronExpression.of("0 0 * * * ?"),
                            SchedulerStatus.ACTIVE,
                            now);

            // When
            CrawlSchedulerHistoryJpaEntity entity = mapper.toEntity(domain);

            // Then
            assertThat(entity).isNotNull();
            assertThat(entity.getId()).isEqualTo(1L);
            assertThat(entity.getCrawlSchedulerId()).isEqualTo(100L);
            assertThat(entity.getSellerId()).isEqualTo(200L);
            assertThat(entity.getSchedulerName()).isEqualTo("test-scheduler");
            assertThat(entity.getCronExpression()).isEqualTo("0 0 * * * ?");
            assertThat(entity.getStatus()).isEqualTo(SchedulerStatus.ACTIVE);
            assertThat(entity.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("성공 - INACTIVE 상태 이력 변환")
        void shouldConvertInactiveHistoryToEntity() {
            // Given
            Instant now = Instant.now();
            CrawlSchedulerHistory domain =
                    CrawlSchedulerHistory.reconstitute(
                            CrawlSchedulerHistoryId.of(2L),
                            CrawlSchedulerId.of(100L),
                            SellerId.of(200L),
                            SchedulerName.of("inactive-scheduler"),
                            CronExpression.of("0 30 * * * ?"),
                            SchedulerStatus.INACTIVE,
                            now);

            // When
            CrawlSchedulerHistoryJpaEntity entity = mapper.toEntity(domain);

            // Then
            assertThat(entity.getStatus()).isEqualTo(SchedulerStatus.INACTIVE);
        }

        @Test
        @DisplayName("성공 - 신규 이력 변환 (ID null)")
        void shouldConvertNewHistoryToEntity() {
            // Given
            Instant now = Instant.now();
            CrawlSchedulerHistory domain =
                    CrawlSchedulerHistory.reconstitute(
                            CrawlSchedulerHistoryId.of(null),
                            CrawlSchedulerId.of(100L),
                            SellerId.of(200L),
                            SchedulerName.of("new-scheduler"),
                            CronExpression.of("0 0 * * * ?"),
                            SchedulerStatus.ACTIVE,
                            now);

            // When
            CrawlSchedulerHistoryJpaEntity entity = mapper.toEntity(domain);

            // Then
            assertThat(entity.getId()).isNull();
            assertThat(entity.getCrawlSchedulerId()).isEqualTo(100L);
        }
    }

    @Nested
    @DisplayName("toDomain - Entity → Domain 변환")
    class ToDomainTests {

        @Test
        @DisplayName("성공 - ACTIVE Entity를 Domain으로 변환")
        void shouldConvertEntityToActiveDomain() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            CrawlSchedulerHistoryJpaEntity entity =
                    CrawlSchedulerHistoryJpaEntity.of(
                            1L,
                            100L,
                            200L,
                            "test-scheduler",
                            "0 0 * * * ?",
                            SchedulerStatus.ACTIVE,
                            now);

            // When
            CrawlSchedulerHistory domain = mapper.toDomain(entity);

            // Then
            assertThat(domain).isNotNull();
            assertThat(domain.getHistoryIdValue()).isEqualTo(1L);
            assertThat(domain.getCrawlSchedulerIdValue()).isEqualTo(100L);
            assertThat(domain.getSellerIdValue()).isEqualTo(200L);
            assertThat(domain.getSchedulerNameValue()).isEqualTo("test-scheduler");
            assertThat(domain.getCronExpressionValue()).isEqualTo("0 0 * * * ?");
            assertThat(domain.getStatus()).isEqualTo(SchedulerStatus.ACTIVE);
            assertThat(domain.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("성공 - INACTIVE Entity를 Domain으로 변환")
        void shouldConvertEntityToInactiveDomain() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            CrawlSchedulerHistoryJpaEntity entity =
                    CrawlSchedulerHistoryJpaEntity.of(
                            2L,
                            100L,
                            200L,
                            "inactive-scheduler",
                            "0 30 * * * ?",
                            SchedulerStatus.INACTIVE,
                            now);

            // When
            CrawlSchedulerHistory domain = mapper.toDomain(entity);

            // Then
            assertThat(domain.getStatus()).isEqualTo(SchedulerStatus.INACTIVE);
        }
    }

    @Nested
    @DisplayName("양방향 변환 일관성")
    class RoundTripTests {

        @Test
        @DisplayName("성공 - Domain → Entity → Domain 변환 일관성")
        void shouldMaintainConsistencyInRoundTrip() {
            // Given
            Instant now = Instant.now();
            CrawlSchedulerHistory original =
                    CrawlSchedulerHistory.reconstitute(
                            CrawlSchedulerHistoryId.of(1L),
                            CrawlSchedulerId.of(100L),
                            SellerId.of(200L),
                            SchedulerName.of("test-scheduler"),
                            CronExpression.of("0 0 * * * ?"),
                            SchedulerStatus.ACTIVE,
                            now);

            // When
            CrawlSchedulerHistoryJpaEntity entity = mapper.toEntity(original);
            CrawlSchedulerHistory restored = mapper.toDomain(entity);

            // Then
            assertThat(restored.getHistoryIdValue()).isEqualTo(original.getHistoryIdValue());
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
            Instant now = Instant.now();
            CrawlSchedulerHistory domain =
                    CrawlSchedulerHistory.reconstitute(
                            CrawlSchedulerHistoryId.of(1L),
                            CrawlSchedulerId.of(100L),
                            SellerId.of(200L),
                            SchedulerName.of("test-scheduler"),
                            CronExpression.of("0 0 * * * ?"),
                            SchedulerStatus.ACTIVE,
                            now);

            // When
            CrawlSchedulerHistoryJpaEntity entity = mapper.toEntity(domain);
            CrawlSchedulerHistory restored = mapper.toDomain(entity);

            // Then - 시간대 변환으로 인한 오차 허용 (1초 이내)
            assertThat(restored.getCreatedAt())
                    .isCloseTo(
                            domain.getCreatedAt(),
                            org.assertj.core.api.Assertions.within(1, ChronoUnit.SECONDS));
        }
    }
}
