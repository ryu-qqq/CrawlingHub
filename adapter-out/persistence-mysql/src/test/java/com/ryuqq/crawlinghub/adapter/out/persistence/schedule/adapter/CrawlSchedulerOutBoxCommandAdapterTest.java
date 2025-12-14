package com.ryuqq.crawlinghub.adapter.out.persistence.schedule.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerOutBoxFixture;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.CrawlSchedulerOutBoxJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.mapper.CrawlSchedulerOutBoxJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.repository.CrawlSchedulerOutBoxJpaRepository;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerOutBox;
import com.ryuqq.crawlinghub.domain.schedule.identifier.CrawlSchedulerOutBoxId;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerOubBoxStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawlSchedulerOutBoxCommandAdapter 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("persistence")
@Tag("adapter")
@DisplayName("CrawlSchedulerOutBoxCommandAdapter 단위 테스트")
@ExtendWith(MockitoExtension.class)
class CrawlSchedulerOutBoxCommandAdapterTest {

    @Mock private CrawlSchedulerOutBoxJpaRepository jpaRepository;

    @Mock private CrawlSchedulerOutBoxJpaEntityMapper mapper;

    private CrawlSchedulerOutBoxCommandAdapter commandAdapter;

    @BeforeEach
    void setUp() {
        commandAdapter = new CrawlSchedulerOutBoxCommandAdapter(jpaRepository, mapper);
    }

    @Test
    @DisplayName("성공 - CrawlSchedulerOutBox 저장 시 ID 반환")
    void shouldReturnIdWhenPersist() {
        // Given
        CrawlSchedulerOutBox outBox = CrawlSchedulerOutBoxFixture.aPendingOutBox();
        LocalDateTime now = LocalDateTime.now();
        CrawlSchedulerOutBoxJpaEntity entity =
                CrawlSchedulerOutBoxJpaEntity.of(
                        null, 1L, CrawlSchedulerOubBoxStatus.PENDING, "{}", null, 0L, now, null);
        CrawlSchedulerOutBoxJpaEntity savedEntity =
                CrawlSchedulerOutBoxJpaEntity.of(
                        1L, 1L, CrawlSchedulerOubBoxStatus.PENDING, "{}", null, 0L, now, null);

        given(mapper.toEntity(outBox)).willReturn(entity);
        given(jpaRepository.save(entity)).willReturn(savedEntity);

        // When
        CrawlSchedulerOutBoxId result = commandAdapter.persist(outBox);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.value()).isEqualTo(1L);
        verify(mapper).toEntity(outBox);
        verify(jpaRepository).save(entity);
    }

    @Test
    @DisplayName("성공 - 기존 CrawlSchedulerOutBox 수정")
    void shouldUpdateExistingOutBox() {
        // Given
        CrawlSchedulerOutBox outBox = CrawlSchedulerOutBoxFixture.aCompletedOutBox();
        LocalDateTime now = LocalDateTime.now();
        CrawlSchedulerOutBoxJpaEntity entity =
                CrawlSchedulerOutBoxJpaEntity.of(
                        100L, 1L, CrawlSchedulerOubBoxStatus.COMPLETED, "{}", null, 1L, now, now);

        given(mapper.toEntity(outBox)).willReturn(entity);
        given(jpaRepository.save(entity)).willReturn(entity);

        // When
        CrawlSchedulerOutBoxId result = commandAdapter.persist(outBox);

        // Then
        assertThat(result.value()).isEqualTo(100L);
    }
}
