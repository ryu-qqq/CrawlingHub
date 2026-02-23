package com.ryuqq.crawlinghub.adapter.out.persistence.task.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskOutboxFixture;
import com.ryuqq.crawlinghub.adapter.out.persistence.task.entity.CrawlTaskOutboxJpaEntity;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTaskOutbox;
import com.ryuqq.crawlinghub.domain.task.vo.OutboxStatus;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * CrawlTaskOutboxJpaEntityMapper 단위 테스트
 *
 * <p>Domain ↔ Entity 양방향 변환 검증
 *
 * <p>Outbox 패턴 특수 사항:
 *
 * <ul>
 *   <li>CrawlTaskId가 PK 역할 (1:1 관계)
 *   <li>idempotencyKey로 중복 발행 방지
 *   <li>payload는 JSON 형식
 *   <li>processedAt은 null 가능 (아직 처리 전)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("persistence")
@Tag("mapper")
@DisplayName("CrawlTaskOutboxJpaEntityMapper 단위 테스트")
class CrawlTaskOutboxJpaEntityMapperTest {

    private CrawlTaskOutboxJpaEntityMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CrawlTaskOutboxJpaEntityMapper();
    }

    @Nested
    @DisplayName("toEntity - Domain → Entity 변환")
    class ToEntityTests {

        @Test
        @DisplayName("성공 - PENDING 상태 Outbox 변환")
        void shouldConvertPendingOutboxToEntity() {
            // Given
            CrawlTaskOutbox domain = CrawlTaskOutboxFixture.aPendingOutbox();

            // When
            CrawlTaskOutboxJpaEntity entity = mapper.toEntity(domain);

            // Then
            assertThat(entity).isNotNull();
            assertThat(entity.getCrawlTaskId()).isEqualTo(domain.getCrawlTaskIdValue());
            assertThat(entity.getIdempotencyKey()).isEqualTo(domain.getIdempotencyKey());
            assertThat(entity.getPayload()).isEqualTo(domain.getPayload());
            assertThat(entity.getStatus()).isEqualTo(OutboxStatus.PENDING);
            assertThat(entity.getRetryCount()).isEqualTo(0);
            assertThat(entity.getCreatedAt()).isNotNull();
            assertThat(entity.getProcessedAt()).isNull();
        }

        @Test
        @DisplayName("성공 - SENT 상태 Outbox 변환")
        void shouldConvertSentOutboxToEntity() {
            // Given
            CrawlTaskOutbox domain = CrawlTaskOutboxFixture.aSentOutbox();

            // When
            CrawlTaskOutboxJpaEntity entity = mapper.toEntity(domain);

            // Then
            assertThat(entity.getStatus()).isEqualTo(OutboxStatus.SENT);
            assertThat(entity.getProcessedAt()).isNotNull();
        }

        @Test
        @DisplayName("성공 - FAILED 상태 Outbox 변환")
        void shouldConvertFailedOutboxToEntity() {
            // Given
            CrawlTaskOutbox domain = CrawlTaskOutboxFixture.aFailedOutbox();

            // When
            CrawlTaskOutboxJpaEntity entity = mapper.toEntity(domain);

            // Then
            assertThat(entity.getStatus()).isEqualTo(OutboxStatus.FAILED);
            assertThat(entity.getRetryCount()).isEqualTo(1);
            assertThat(entity.getProcessedAt()).isNotNull();
        }

        @Test
        @DisplayName("성공 - 최대 재시도 횟수 도달 Outbox 변환")
        void shouldConvertMaxRetriedOutboxToEntity() {
            // Given
            CrawlTaskOutbox domain = CrawlTaskOutboxFixture.aMaxRetriedFailedOutbox();

            // When
            CrawlTaskOutboxJpaEntity entity = mapper.toEntity(domain);

            // Then
            assertThat(entity.getStatus()).isEqualTo(OutboxStatus.FAILED);
            assertThat(entity.getRetryCount()).isEqualTo(3);
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
            CrawlTaskOutboxJpaEntity entity =
                    CrawlTaskOutboxJpaEntity.of(
                            1L,
                            "outbox-1-abcd1234",
                            "{\"taskId\": 1, \"sellerId\": 100}",
                            OutboxStatus.PENDING,
                            0,
                            now,
                            null);

            // When
            CrawlTaskOutbox domain = mapper.toDomain(entity);

            // Then
            assertThat(domain).isNotNull();
            assertThat(domain.getCrawlTaskIdValue()).isEqualTo(1L);
            assertThat(domain.getIdempotencyKey()).isEqualTo("outbox-1-abcd1234");
            assertThat(domain.getPayload()).isEqualTo("{\"taskId\": 1, \"sellerId\": 100}");
            assertThat(domain.getStatus()).isEqualTo(OutboxStatus.PENDING);
            assertThat(domain.getRetryCount()).isEqualTo(0);
            assertThat(domain.getProcessedAt()).isNull();
        }

        @Test
        @DisplayName("성공 - SENT Entity를 Domain으로 변환")
        void shouldConvertEntityToSentDomain() {
            // Given
            LocalDateTime createdAt = LocalDateTime.now().minusMinutes(5);
            LocalDateTime processedAt = LocalDateTime.now();
            CrawlTaskOutboxJpaEntity entity =
                    CrawlTaskOutboxJpaEntity.of(
                            2L,
                            "outbox-2-efgh5678",
                            "{\"taskId\": 2}",
                            OutboxStatus.SENT,
                            0,
                            createdAt,
                            processedAt);

            // When
            CrawlTaskOutbox domain = mapper.toDomain(entity);

            // Then
            assertThat(domain.getStatus()).isEqualTo(OutboxStatus.SENT);
            assertThat(domain.getProcessedAt()).isNotNull();
        }

        @Test
        @DisplayName("성공 - FAILED Entity를 Domain으로 변환")
        void shouldConvertEntityToFailedDomain() {
            // Given
            LocalDateTime createdAt = LocalDateTime.now().minusMinutes(10);
            LocalDateTime processedAt = LocalDateTime.now().minusMinutes(5);
            CrawlTaskOutboxJpaEntity entity =
                    CrawlTaskOutboxJpaEntity.of(
                            3L,
                            "outbox-3-ijkl9012",
                            "{\"taskId\": 3}",
                            OutboxStatus.FAILED,
                            2,
                            createdAt,
                            processedAt);

            // When
            CrawlTaskOutbox domain = mapper.toDomain(entity);

            // Then
            assertThat(domain.getStatus()).isEqualTo(OutboxStatus.FAILED);
            assertThat(domain.getRetryCount()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("양방향 변환 일관성")
    class RoundTripTests {

        @Test
        @DisplayName("성공 - PENDING Domain → Entity → Domain 변환 일관성")
        void shouldMaintainConsistencyForPendingOutbox() {
            // Given
            CrawlTaskOutbox original = CrawlTaskOutboxFixture.aPendingOutbox();

            // When
            CrawlTaskOutboxJpaEntity entity = mapper.toEntity(original);
            CrawlTaskOutbox restored = mapper.toDomain(entity);

            // Then
            assertThat(restored.getCrawlTaskIdValue()).isEqualTo(original.getCrawlTaskIdValue());
            assertThat(restored.getIdempotencyKey()).isEqualTo(original.getIdempotencyKey());
            assertThat(restored.getPayload()).isEqualTo(original.getPayload());
            assertThat(restored.getStatus()).isEqualTo(original.getStatus());
            assertThat(restored.getRetryCount()).isEqualTo(original.getRetryCount());
        }

        @Test
        @DisplayName("성공 - SENT Domain → Entity → Domain 변환 일관성")
        void shouldMaintainConsistencyForSentOutbox() {
            // Given
            CrawlTaskOutbox original = CrawlTaskOutboxFixture.aSentOutbox();

            // When
            CrawlTaskOutboxJpaEntity entity = mapper.toEntity(original);
            CrawlTaskOutbox restored = mapper.toDomain(entity);

            // Then
            assertThat(restored.getStatus()).isEqualTo(OutboxStatus.SENT);
            assertThat(restored.getProcessedAt()).isNotNull();
        }

        @Test
        @DisplayName("성공 - 다양한 상태의 양방향 변환")
        void shouldMaintainConsistencyForVariousStatuses() {
            // Given
            CrawlTaskOutbox pending = CrawlTaskOutboxFixture.aPendingOutbox();
            CrawlTaskOutbox sent = CrawlTaskOutboxFixture.aSentOutbox();
            CrawlTaskOutbox failed = CrawlTaskOutboxFixture.aFailedOutbox();

            // When & Then - Pending
            CrawlTaskOutbox restoredPending = mapper.toDomain(mapper.toEntity(pending));
            assertThat(restoredPending.getStatus()).isEqualTo(OutboxStatus.PENDING);

            // When & Then - Sent
            CrawlTaskOutbox restoredSent = mapper.toDomain(mapper.toEntity(sent));
            assertThat(restoredSent.getStatus()).isEqualTo(OutboxStatus.SENT);

            // When & Then - Failed
            CrawlTaskOutbox restoredFailed = mapper.toDomain(mapper.toEntity(failed));
            assertThat(restoredFailed.getStatus()).isEqualTo(OutboxStatus.FAILED);
        }
    }

    @Nested
    @DisplayName("시간 변환")
    class TimeConversionTests {

        @Test
        @DisplayName("성공 - Instant → LocalDateTime → Instant 변환 일관성")
        void shouldConvertTimesConsistently() {
            // Given
            CrawlTaskOutbox domain = CrawlTaskOutboxFixture.aSentOutbox();

            // When
            CrawlTaskOutboxJpaEntity entity = mapper.toEntity(domain);
            CrawlTaskOutbox restored = mapper.toDomain(entity);

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
            CrawlTaskOutbox domain = CrawlTaskOutboxFixture.aPendingOutbox();

            // When
            CrawlTaskOutboxJpaEntity entity = mapper.toEntity(domain);
            CrawlTaskOutbox restored = mapper.toDomain(entity);

            // Then
            assertThat(restored.getProcessedAt()).isNull();
        }
    }
}
