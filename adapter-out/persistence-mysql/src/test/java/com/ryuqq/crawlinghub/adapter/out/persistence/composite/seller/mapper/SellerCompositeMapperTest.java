package com.ryuqq.crawlinghub.adapter.out.persistence.composite.seller.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.adapter.out.persistence.composite.seller.dto.SellerCompositeDto;
import com.ryuqq.crawlinghub.adapter.out.persistence.composite.seller.dto.SellerSchedulerSummaryDto;
import com.ryuqq.crawlinghub.adapter.out.persistence.composite.seller.dto.SellerTaskStatisticsDto;
import com.ryuqq.crawlinghub.adapter.out.persistence.composite.seller.dto.SellerTaskSummaryDto;
import com.ryuqq.crawlinghub.application.seller.dto.composite.SellerDetailResult;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * SellerCompositeMapper 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("SellerCompositeMapper 단위 테스트")
@Tag("unit")
@Tag("adapter-persistence")
class SellerCompositeMapperTest {

    private SellerCompositeMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new SellerCompositeMapper();
    }

    @Nested
    @DisplayName("toResult() - Persistence DTO → SellerDetailResult 변환")
    class ToResultTests {

        @Test
        @DisplayName("성공: 전체 데이터 변환")
        void toResult_FullData() {
            // Given
            LocalDateTime now = LocalDateTime.of(2025, 11, 19, 10, 30, 0);
            SellerCompositeDto sellerDto =
                    new SellerCompositeDto(1L, "머스트잇셀러", "커머스셀러", "ACTIVE", 100, now, now);

            List<SellerSchedulerSummaryDto> schedulerDtos =
                    List.of(
                            new SellerSchedulerSummaryDto(1L, "일일 크롤링", "ACTIVE", "0 0 9 * * ?"),
                            new SellerSchedulerSummaryDto(
                                    2L, "주간 크롤링", "INACTIVE", "0 0 0 * * MON"));

            List<SellerTaskSummaryDto> taskDtos =
                    List.of(
                            new SellerTaskSummaryDto(1L, "SUCCESS", "FULL_SYNC", now, now),
                            new SellerTaskSummaryDto(2L, "RUNNING", "INCREMENTAL_SYNC", now, null));

            List<SellerTaskStatisticsDto> statsDtos =
                    List.of(
                            new SellerTaskStatisticsDto("SUCCESS", 80L),
                            new SellerTaskStatisticsDto("FAILED", 10L),
                            new SellerTaskStatisticsDto("RUNNING", 10L));

            // When
            SellerDetailResult result =
                    mapper.toResult(sellerDto, schedulerDtos, taskDtos, statsDtos);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.seller().sellerId()).isEqualTo(1L);
            assertThat(result.seller().mustItSellerName()).isEqualTo("머스트잇셀러");
            assertThat(result.seller().sellerName()).isEqualTo("커머스셀러");
            assertThat(result.seller().status()).isEqualTo("ACTIVE");
            assertThat(result.seller().productCount()).isEqualTo(100);

            assertThat(result.schedulers()).hasSize(2);
            assertThat(result.schedulers().get(0).schedulerName()).isEqualTo("일일 크롤링");

            assertThat(result.recentTasks()).hasSize(2);
            assertThat(result.recentTasks().get(0).completedAt()).isNotNull();
            assertThat(result.recentTasks().get(1).completedAt()).isNull();

            assertThat(result.statistics().totalProducts()).isEqualTo(100L);
            assertThat(result.statistics().syncedProducts()).isEqualTo(80L);
            assertThat(result.statistics().pendingSyncProducts()).isEqualTo(10L);
            assertThat(result.statistics().successRate()).isEqualTo(0.8);
        }

        @Test
        @DisplayName("성공: 빈 목록 변환")
        void toResult_EmptyLists() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            SellerCompositeDto sellerDto =
                    new SellerCompositeDto(2L, "머스트잇셀러2", "커머스셀러2", "INACTIVE", 0, now, null);

            // When
            SellerDetailResult result = mapper.toResult(sellerDto, List.of(), List.of(), List.of());

            // Then
            assertThat(result).isNotNull();
            assertThat(result.seller().sellerId()).isEqualTo(2L);
            assertThat(result.schedulers()).isEmpty();
            assertThat(result.recentTasks()).isEmpty();
            assertThat(result.statistics().totalProducts()).isEqualTo(0L);
            assertThat(result.statistics().successRate()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("성공: COMPLETED_STATUSES 기반 completedAt 설정")
        void toResult_CompletedAtForCompletedStatuses() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            SellerCompositeDto sellerDto =
                    new SellerCompositeDto(1L, "머스트잇셀러", "셀러", "ACTIVE", 0, now, now);

            List<SellerTaskSummaryDto> taskDtos =
                    List.of(
                            new SellerTaskSummaryDto(1L, "SUCCESS", "FULL_SYNC", now, now),
                            new SellerTaskSummaryDto(2L, "FAILED", "FULL_SYNC", now, now),
                            new SellerTaskSummaryDto(3L, "TIMEOUT", "FULL_SYNC", now, now),
                            new SellerTaskSummaryDto(4L, "RUNNING", "FULL_SYNC", now, now),
                            new SellerTaskSummaryDto(5L, "PENDING", "FULL_SYNC", now, null));

            // When
            SellerDetailResult result = mapper.toResult(sellerDto, List.of(), taskDtos, List.of());

            // Then
            assertThat(result.recentTasks().get(0).completedAt()).isNotNull();
            assertThat(result.recentTasks().get(1).completedAt()).isNotNull();
            assertThat(result.recentTasks().get(2).completedAt()).isNotNull();
            assertThat(result.recentTasks().get(3).completedAt()).isNull();
            assertThat(result.recentTasks().get(4).completedAt()).isNull();
        }
    }
}
