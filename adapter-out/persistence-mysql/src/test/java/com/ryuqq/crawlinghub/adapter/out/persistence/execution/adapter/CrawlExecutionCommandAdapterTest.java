package com.ryuqq.crawlinghub.adapter.out.persistence.execution.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.cralwinghub.domain.fixture.execution.CrawlExecutionFixture;
import com.ryuqq.crawlinghub.adapter.out.persistence.execution.entity.CrawlExecutionJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.execution.mapper.CrawlExecutionJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.execution.repository.CrawlExecutionJpaRepository;
import com.ryuqq.crawlinghub.domain.execution.aggregate.CrawlExecution;
import com.ryuqq.crawlinghub.domain.execution.identifier.CrawlExecutionId;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlExecutionStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawlExecutionCommandAdapter 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("persistence")
@Tag("adapter")
@DisplayName("CrawlExecutionCommandAdapter 단위 테스트")
@ExtendWith(MockitoExtension.class)
class CrawlExecutionCommandAdapterTest {

    @Mock private CrawlExecutionJpaRepository jpaRepository;

    @Mock private CrawlExecutionJpaEntityMapper mapper;

    private CrawlExecutionCommandAdapter commandAdapter;

    @BeforeEach
    void setUp() {
        commandAdapter = new CrawlExecutionCommandAdapter(jpaRepository, mapper);
    }

    @Test
    @DisplayName("성공 - CrawlExecution 저장 시 ID 반환")
    void shouldReturnIdWhenPersist() {
        // Given
        CrawlExecution execution = CrawlExecutionFixture.aRunningExecution();
        LocalDateTime now = LocalDateTime.now();
        CrawlExecutionJpaEntity entity =
                CrawlExecutionJpaEntity.of(
                        null,
                        1L,
                        1L,
                        1L,
                        CrawlExecutionStatus.RUNNING,
                        null,
                        null,
                        null,
                        now,
                        null,
                        null,
                        now);
        CrawlExecutionJpaEntity savedEntity =
                CrawlExecutionJpaEntity.of(
                        1L,
                        1L,
                        1L,
                        1L,
                        CrawlExecutionStatus.RUNNING,
                        null,
                        null,
                        null,
                        now,
                        null,
                        null,
                        now);

        given(mapper.toEntity(execution)).willReturn(entity);
        given(jpaRepository.save(entity)).willReturn(savedEntity);

        // When
        CrawlExecutionId result = commandAdapter.persist(execution);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.value()).isEqualTo(1L);
        verify(mapper).toEntity(execution);
        verify(jpaRepository).save(entity);
    }

    @Test
    @DisplayName("성공 - 기존 CrawlExecution 수정")
    void shouldUpdateExistingExecution() {
        // Given
        CrawlExecution execution = CrawlExecutionFixture.aSuccessExecution();
        LocalDateTime now = LocalDateTime.now();
        CrawlExecutionJpaEntity entity =
                CrawlExecutionJpaEntity.of(
                        100L,
                        1L,
                        1L,
                        1L,
                        CrawlExecutionStatus.SUCCESS,
                        "{}",
                        200,
                        null,
                        now,
                        now,
                        1000L,
                        now);

        given(mapper.toEntity(execution)).willReturn(entity);
        given(jpaRepository.save(entity)).willReturn(entity);

        // When
        CrawlExecutionId result = commandAdapter.persist(execution);

        // Then
        assertThat(result.value()).isEqualTo(100L);
    }
}
