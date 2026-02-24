package com.ryuqq.crawlinghub.adapter.out.persistence.schedule.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.CrawlSchedulerHistoryJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.mapper.CrawlSchedulerHistoryJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.repository.CrawlSchedulerHistoryJpaRepository;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerHistory;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerHistoryId;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.schedule.vo.CronExpression;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerName;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import java.time.Instant;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawlSchedulerHistoryCommandCommandAdapter 단위 테스트
 *
 * <p>CrawlSchedulerHistory Aggregate의 저장 기능 검증
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("persistence")
@Tag("adapter")
@DisplayName("CrawlSchedulerHistoryCommandCommandAdapter 단위 테스트")
@ExtendWith(MockitoExtension.class)
class CrawlSchedulerHistoryCommandAdapterTest {

    @Mock private CrawlSchedulerHistoryJpaRepository jpaRepository;

    @Mock private CrawlSchedulerHistoryJpaEntityMapper mapper;

    private CrawlSchedulerHistoryCommandCommandAdapter commandAdapter;

    @BeforeEach
    void setUp() {
        commandAdapter = new CrawlSchedulerHistoryCommandCommandAdapter(jpaRepository, mapper);
    }

    @Nested
    @DisplayName("persist 메서드 테스트")
    class PersistTests {

        @Test
        @DisplayName("성공 - CrawlSchedulerHistory 저장 시 ID 반환")
        void shouldReturnIdWhenPersist() {
            // Given - 저장할 CrawlSchedulerHistory 도메인 객체
            Instant now = Instant.now();
            CrawlSchedulerHistory domain =
                    CrawlSchedulerHistory.reconstitute(
                            CrawlSchedulerHistoryId.forNew(),
                            CrawlSchedulerId.of(1L),
                            SellerId.of(1L),
                            SchedulerName.of("test-scheduler"),
                            CronExpression.of("cron(0 0 * * ? *)"),
                            SchedulerStatus.ACTIVE,
                            now);

            LocalDateTime nowLdt = LocalDateTime.now();
            CrawlSchedulerHistoryJpaEntity entity =
                    CrawlSchedulerHistoryJpaEntity.of(
                            null,
                            1L,
                            1L,
                            "test-scheduler",
                            "cron(0 0 * * ? *)",
                            SchedulerStatus.ACTIVE,
                            nowLdt);
            CrawlSchedulerHistoryJpaEntity savedEntity =
                    CrawlSchedulerHistoryJpaEntity.of(
                            10L,
                            1L,
                            1L,
                            "test-scheduler",
                            "cron(0 0 * * ? *)",
                            SchedulerStatus.ACTIVE,
                            nowLdt);

            given(mapper.toEntity(domain)).willReturn(entity);
            given(jpaRepository.save(entity)).willReturn(savedEntity);

            // When
            CrawlSchedulerHistoryId result = commandAdapter.persist(domain);

            // Then - 저장된 엔티티의 ID가 반환되어야 함
            assertThat(result).isNotNull();
            assertThat(result.value()).isEqualTo(10L);
        }

        @Test
        @DisplayName("성공 - mapper.toEntity가 호출됨")
        void shouldCallMapperToEntity() {
            // Given
            Instant now = Instant.now();
            CrawlSchedulerHistory domain =
                    CrawlSchedulerHistory.reconstitute(
                            CrawlSchedulerHistoryId.forNew(),
                            CrawlSchedulerId.of(1L),
                            SellerId.of(1L),
                            SchedulerName.of("test-scheduler"),
                            CronExpression.of("cron(0 0 * * ? *)"),
                            SchedulerStatus.ACTIVE,
                            now);

            LocalDateTime nowLdt = LocalDateTime.now();
            CrawlSchedulerHistoryJpaEntity entity =
                    CrawlSchedulerHistoryJpaEntity.of(
                            null,
                            1L,
                            1L,
                            "test-scheduler",
                            "cron(0 0 * * ? *)",
                            SchedulerStatus.ACTIVE,
                            nowLdt);
            CrawlSchedulerHistoryJpaEntity savedEntity =
                    CrawlSchedulerHistoryJpaEntity.of(
                            5L,
                            1L,
                            1L,
                            "test-scheduler",
                            "cron(0 0 * * ? *)",
                            SchedulerStatus.ACTIVE,
                            nowLdt);

            given(mapper.toEntity(domain)).willReturn(entity);
            given(jpaRepository.save(entity)).willReturn(savedEntity);

            // When
            commandAdapter.persist(domain);

            // Then - mapper와 jpaRepository가 각각 호출되어야 함
            then(mapper).should().toEntity(domain);
            then(jpaRepository).should().save(entity);
        }

        @Test
        @DisplayName("성공 - INACTIVE 상태 이력 저장")
        void shouldPersistInactiveHistory() {
            // Given - INACTIVE 상태 이력
            Instant now = Instant.now();
            CrawlSchedulerHistory domain =
                    CrawlSchedulerHistory.reconstitute(
                            CrawlSchedulerHistoryId.forNew(),
                            CrawlSchedulerId.of(2L),
                            SellerId.of(3L),
                            SchedulerName.of("inactive-scheduler"),
                            CronExpression.of("cron(30 0 * * ? *)"),
                            SchedulerStatus.INACTIVE,
                            now);

            LocalDateTime nowLdt = LocalDateTime.now();
            CrawlSchedulerHistoryJpaEntity entity =
                    CrawlSchedulerHistoryJpaEntity.of(
                            null,
                            2L,
                            3L,
                            "inactive-scheduler",
                            "cron(30 0 * * ? *)",
                            SchedulerStatus.INACTIVE,
                            nowLdt);
            CrawlSchedulerHistoryJpaEntity savedEntity =
                    CrawlSchedulerHistoryJpaEntity.of(
                            20L,
                            2L,
                            3L,
                            "inactive-scheduler",
                            "cron(30 0 * * ? *)",
                            SchedulerStatus.INACTIVE,
                            nowLdt);

            given(mapper.toEntity(domain)).willReturn(entity);
            given(jpaRepository.save(entity)).willReturn(savedEntity);

            // When
            CrawlSchedulerHistoryId result = commandAdapter.persist(domain);

            // Then - 저장된 ID가 반환되어야 함
            assertThat(result.value()).isEqualTo(20L);
        }

        @Test
        @DisplayName("성공 - 기존 이력 저장 (ID 있음)")
        void shouldPersistExistingHistory() {
            // Given - ID가 있는 기존 이력
            Instant now = Instant.now();
            CrawlSchedulerHistory domain =
                    CrawlSchedulerHistory.reconstitute(
                            CrawlSchedulerHistoryId.of(100L),
                            CrawlSchedulerId.of(1L),
                            SellerId.of(1L),
                            SchedulerName.of("existing-scheduler"),
                            CronExpression.of("cron(0 0 * * ? *)"),
                            SchedulerStatus.ACTIVE,
                            now);

            LocalDateTime nowLdt = LocalDateTime.now();
            CrawlSchedulerHistoryJpaEntity entity =
                    CrawlSchedulerHistoryJpaEntity.of(
                            100L,
                            1L,
                            1L,
                            "existing-scheduler",
                            "cron(0 0 * * ? *)",
                            SchedulerStatus.ACTIVE,
                            nowLdt);

            given(mapper.toEntity(domain)).willReturn(entity);
            given(jpaRepository.save(entity)).willReturn(entity);

            // When
            CrawlSchedulerHistoryId result = commandAdapter.persist(domain);

            // Then - 기존 ID가 반환되어야 함
            assertThat(result.value()).isEqualTo(100L);
        }
    }
}
