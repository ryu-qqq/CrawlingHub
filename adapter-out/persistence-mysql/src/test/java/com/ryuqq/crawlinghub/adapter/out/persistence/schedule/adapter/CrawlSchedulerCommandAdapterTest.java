package com.ryuqq.crawlinghub.adapter.out.persistence.schedule.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerFixture;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.CrawlSchedulerJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.mapper.CrawlSchedulerJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.repository.CrawlSchedulerJpaRepository;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawlSchedulerCommandAdapter 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("persistence")
@Tag("adapter")
@DisplayName("CrawlSchedulerCommandAdapter 단위 테스트")
@ExtendWith(MockitoExtension.class)
class CrawlSchedulerCommandAdapterTest {

    @Mock private CrawlSchedulerJpaRepository jpaRepository;

    @Mock private CrawlSchedulerJpaEntityMapper mapper;

    private CrawlSchedulerCommandAdapter commandAdapter;

    @BeforeEach
    void setUp() {
        commandAdapter = new CrawlSchedulerCommandAdapter(jpaRepository, mapper);
    }

    @Test
    @DisplayName("성공 - CrawlScheduler 저장 시 ID 반환")
    void shouldReturnIdWhenPersist() {
        // Given
        CrawlScheduler scheduler = CrawlSchedulerFixture.anActiveScheduler();
        LocalDateTime now = LocalDateTime.now();
        CrawlSchedulerJpaEntity entity =
                CrawlSchedulerJpaEntity.of(
                        null, 1L, "test-scheduler", "0 0 * * *", SchedulerStatus.ACTIVE, now, now);
        CrawlSchedulerJpaEntity savedEntity =
                CrawlSchedulerJpaEntity.of(
                        1L, 1L, "test-scheduler", "0 0 * * *", SchedulerStatus.ACTIVE, now, now);

        given(mapper.toEntity(scheduler)).willReturn(entity);
        given(jpaRepository.save(entity)).willReturn(savedEntity);

        // When
        CrawlSchedulerId result = commandAdapter.persist(scheduler);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.value()).isEqualTo(1L);
        verify(mapper).toEntity(scheduler);
        verify(jpaRepository).save(entity);
    }

    @Test
    @DisplayName("성공 - 기존 CrawlScheduler 수정")
    void shouldUpdateExistingScheduler() {
        // Given
        CrawlScheduler scheduler = CrawlSchedulerFixture.anActiveScheduler();
        LocalDateTime now = LocalDateTime.now();
        CrawlSchedulerJpaEntity entity =
                CrawlSchedulerJpaEntity.of(
                        100L,
                        1L,
                        "updated-scheduler",
                        "0 0 * * *",
                        SchedulerStatus.INACTIVE,
                        now,
                        now);

        given(mapper.toEntity(scheduler)).willReturn(entity);
        given(jpaRepository.save(entity)).willReturn(entity);

        // When
        CrawlSchedulerId result = commandAdapter.persist(scheduler);

        // Then
        assertThat(result.value()).isEqualTo(100L);
    }
}
