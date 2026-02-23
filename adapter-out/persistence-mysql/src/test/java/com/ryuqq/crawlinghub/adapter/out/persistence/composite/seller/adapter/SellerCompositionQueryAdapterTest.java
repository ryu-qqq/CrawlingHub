package com.ryuqq.crawlinghub.adapter.out.persistence.composite.seller.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.crawlinghub.adapter.out.persistence.composite.seller.dto.SellerCompositeDto;
import com.ryuqq.crawlinghub.adapter.out.persistence.composite.seller.dto.SellerSchedulerSummaryDto;
import com.ryuqq.crawlinghub.adapter.out.persistence.composite.seller.dto.SellerTaskStatisticsDto;
import com.ryuqq.crawlinghub.adapter.out.persistence.composite.seller.dto.SellerTaskSummaryDto;
import com.ryuqq.crawlinghub.adapter.out.persistence.composite.seller.mapper.SellerCompositeMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.composite.seller.repository.SellerCompositeQueryDslRepository;
import com.ryuqq.crawlinghub.application.seller.dto.composite.SellerDetailResult;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * SellerCompositionQueryAdapter 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SellerCompositionQueryAdapter 단위 테스트")
@Tag("unit")
@Tag("adapter-persistence")
class SellerCompositionQueryAdapterTest {

    @Mock private SellerCompositeQueryDslRepository compositeRepository;

    @Mock private SellerCompositeMapper compositeMapper;

    @InjectMocks private SellerCompositionQueryAdapter adapter;

    @Nested
    @DisplayName("findSellerDetailById() - 셀러 상세 Composite 조회")
    class FindSellerDetailByIdTests {

        @Test
        @DisplayName("성공: 존재하는 셀러 조회 시 SellerDetailResult 반환")
        void findSellerDetailById_Success() {
            // Given
            Long sellerId = 1L;
            LocalDateTime now = LocalDateTime.now();

            SellerCompositeDto sellerDto =
                    new SellerCompositeDto(sellerId, "머스트잇셀러", "커머스셀러", "ACTIVE", 100, now, now);
            List<SellerSchedulerSummaryDto> schedulerDtos =
                    List.of(new SellerSchedulerSummaryDto(1L, "스케줄러", "ACTIVE", "0 0 9 * * ?"));
            List<SellerTaskSummaryDto> taskDtos =
                    List.of(new SellerTaskSummaryDto(1L, "SUCCESS", "FULL_SYNC", now, now));
            List<SellerTaskStatisticsDto> statsDtos =
                    List.of(new SellerTaskStatisticsDto("SUCCESS", 10L));

            SellerDetailResult expectedResult =
                    new SellerDetailResult(
                            new SellerDetailResult.SellerInfo(
                                    sellerId,
                                    "머스트잇셀러",
                                    "커머스셀러",
                                    "ACTIVE",
                                    100,
                                    Instant.now(),
                                    Instant.now()),
                            List.of(
                                    new SellerDetailResult.SchedulerSummary(
                                            1L, "스케줄러", "ACTIVE", "0 0 9 * * ?")),
                            List.of(
                                    new SellerDetailResult.TaskSummary(
                                            1L,
                                            "SUCCESS",
                                            "FULL_SYNC",
                                            Instant.now(),
                                            Instant.now())),
                            new SellerDetailResult.SellerStatistics(10L, 10L, 0L, 1.0));

            given(compositeRepository.fetchSeller(sellerId)).willReturn(Optional.of(sellerDto));
            given(compositeRepository.fetchSchedulers(sellerId)).willReturn(schedulerDtos);
            given(compositeRepository.fetchRecentTasks(sellerId, 5)).willReturn(taskDtos);
            given(compositeRepository.fetchTaskStatistics(sellerId)).willReturn(statsDtos);
            given(compositeMapper.toResult(sellerDto, schedulerDtos, taskDtos, statsDtos))
                    .willReturn(expectedResult);

            // When
            Optional<SellerDetailResult> result = adapter.findSellerDetailById(sellerId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(expectedResult);

            then(compositeRepository).should().fetchSeller(sellerId);
            then(compositeRepository).should().fetchSchedulers(sellerId);
            then(compositeRepository).should().fetchRecentTasks(sellerId, 5);
            then(compositeRepository).should().fetchTaskStatistics(sellerId);
            then(compositeMapper).should().toResult(sellerDto, schedulerDtos, taskDtos, statsDtos);
        }

        @Test
        @DisplayName("성공: 존재하지 않는 셀러 조회 시 Optional.empty 반환")
        void findSellerDetailById_NotFound() {
            // Given
            Long sellerId = 999L;
            given(compositeRepository.fetchSeller(sellerId)).willReturn(Optional.empty());

            // When
            Optional<SellerDetailResult> result = adapter.findSellerDetailById(sellerId);

            // Then
            assertThat(result).isEmpty();

            then(compositeRepository).should().fetchSeller(sellerId);
            then(compositeRepository).shouldHaveNoMoreInteractions();
            then(compositeMapper).shouldHaveNoInteractions();
        }
    }
}
