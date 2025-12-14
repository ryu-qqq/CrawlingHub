package com.ryuqq.crawlinghub.adapter.out.persistence.task.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskFixture;
import com.ryuqq.crawlinghub.adapter.out.persistence.task.entity.CrawlTaskJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.task.mapper.CrawlTaskJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.task.repository.CrawlTaskJpaRepository;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.identifier.CrawlTaskId;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatus;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawlTaskCommandAdapter 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("persistence")
@Tag("adapter")
@DisplayName("CrawlTaskCommandAdapter 단위 테스트")
@ExtendWith(MockitoExtension.class)
class CrawlTaskCommandAdapterTest {

    @Mock private CrawlTaskJpaRepository jpaRepository;

    @Mock private CrawlTaskJpaEntityMapper mapper;

    private CrawlTaskCommandAdapter commandAdapter;

    @BeforeEach
    void setUp() {
        commandAdapter = new CrawlTaskCommandAdapter(jpaRepository, mapper);
    }

    @Test
    @DisplayName("성공 - CrawlTask 저장 시 ID 반환")
    void shouldReturnIdWhenPersist() {
        // Given
        CrawlTask crawlTask = CrawlTaskFixture.aWaitingTask();
        LocalDateTime now = LocalDateTime.now();
        CrawlTaskJpaEntity entity =
                CrawlTaskJpaEntity.of(
                        null,
                        1L,
                        1L,
                        CrawlTaskType.MINI_SHOP,
                        "https://example.com",
                        "/api",
                        "{}",
                        CrawlTaskStatus.WAITING,
                        0,
                        now,
                        now);
        CrawlTaskJpaEntity savedEntity =
                CrawlTaskJpaEntity.of(
                        1L,
                        1L,
                        1L,
                        CrawlTaskType.MINI_SHOP,
                        "https://example.com",
                        "/api",
                        "{}",
                        CrawlTaskStatus.WAITING,
                        0,
                        now,
                        now);

        given(mapper.toEntity(crawlTask)).willReturn(entity);
        given(jpaRepository.save(entity)).willReturn(savedEntity);

        // When
        CrawlTaskId result = commandAdapter.persist(crawlTask);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.value()).isEqualTo(1L);
        verify(mapper).toEntity(crawlTask);
        verify(jpaRepository).save(entity);
    }

    @Test
    @DisplayName("성공 - 기존 CrawlTask 수정")
    void shouldUpdateExistingTask() {
        // Given
        CrawlTask crawlTask = CrawlTaskFixture.aSuccessTask();
        LocalDateTime now = LocalDateTime.now();
        CrawlTaskJpaEntity entity =
                CrawlTaskJpaEntity.of(
                        100L,
                        1L,
                        1L,
                        CrawlTaskType.MINI_SHOP,
                        "https://example.com",
                        "/api",
                        "{}",
                        CrawlTaskStatus.SUCCESS,
                        0,
                        now,
                        now);

        given(mapper.toEntity(crawlTask)).willReturn(entity);
        given(jpaRepository.save(entity)).willReturn(entity);

        // When
        CrawlTaskId result = commandAdapter.persist(crawlTask);

        // Then
        assertThat(result.value()).isEqualTo(100L);
    }
}
