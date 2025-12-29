package com.ryuqq.crawlinghub.application.schedule.assembler;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerFixture;
import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.schedule.dto.query.SearchCrawlSchedulersQuery;
import com.ryuqq.crawlinghub.application.schedule.dto.response.CrawlSchedulerResponse;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerQueryCriteria;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * CrawlSchedulerAssembler 단위 테스트
 *
 * <p>Domain → Response 변환 책임만 담당
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("CrawlSchedulerAssembler 테스트")
class CrawlSchedulerAssemblerTest {

    private final CrawlSchedulerAssembler assembler = new CrawlSchedulerAssembler();

    @Nested
    @DisplayName("toResponse() 테스트")
    class ToResponse {

        @Test
        @DisplayName("[성공] CrawlScheduler → CrawlSchedulerResponse 변환")
        void shouldConvertCrawlSchedulerToResponse() {
            // Given
            CrawlScheduler scheduler = CrawlSchedulerFixture.anActiveScheduler();

            // When
            CrawlSchedulerResponse result = assembler.toResponse(scheduler);

            // Then
            assertThat(result.crawlSchedulerId()).isEqualTo(scheduler.getCrawlSchedulerIdValue());
            assertThat(result.sellerId()).isEqualTo(scheduler.getSellerIdValue());
            assertThat(result.schedulerName()).isEqualTo(scheduler.getSchedulerNameValue());
            assertThat(result.cronExpression()).isEqualTo(scheduler.getCronExpressionValue());
            assertThat(result.status()).isEqualTo(SchedulerStatus.ACTIVE);
            assertThat(result.createdAt()).isEqualTo(scheduler.getCreatedAt());
            assertThat(result.updatedAt()).isEqualTo(scheduler.getUpdatedAt());
        }

        @Test
        @DisplayName("[성공] 비활성 CrawlScheduler → CrawlSchedulerResponse 변환")
        void shouldConvertInactiveSchedulerToResponse() {
            // Given
            CrawlScheduler scheduler = CrawlSchedulerFixture.anInactiveScheduler();

            // When
            CrawlSchedulerResponse result = assembler.toResponse(scheduler);

            // Then
            assertThat(result.status()).isEqualTo(SchedulerStatus.INACTIVE);
        }
    }

    @Nested
    @DisplayName("toCriteria() 테스트")
    class ToCriteria {

        @Test
        @DisplayName("[성공] SearchCrawlSchedulersQuery → CrawlSchedulerQueryCriteria 변환 (전체 필드)")
        void shouldConvertQueryToCriteria() {
            // Given
            SearchCrawlSchedulersQuery query =
                    new SearchCrawlSchedulersQuery(
                            1L, List.of(SchedulerStatus.ACTIVE), null, null, 0, 20);

            // When
            CrawlSchedulerQueryCriteria result = assembler.toCriteria(query);

            // Then
            assertThat(result.sellerId()).isNotNull();
            assertThat(result.sellerId().value()).isEqualTo(1L);
            assertThat(result.status()).isEqualTo(SchedulerStatus.ACTIVE);
            assertThat(result.page()).isZero();
            assertThat(result.size()).isEqualTo(20);
        }

        @Test
        @DisplayName("[성공] SearchCrawlSchedulersQuery → CrawlSchedulerQueryCriteria 변환 (null 필드)")
        void shouldConvertQueryToCriteriaWithNullFields() {
            // Given
            SearchCrawlSchedulersQuery query =
                    new SearchCrawlSchedulersQuery(null, null, null, null, 0, 10);

            // When
            CrawlSchedulerQueryCriteria result = assembler.toCriteria(query);

            // Then
            assertThat(result.sellerId()).isNull();
            assertThat(result.status()).isNull();
        }
    }

    @Nested
    @DisplayName("toResponses() 테스트")
    class ToResponses {

        @Test
        @DisplayName("[성공] CrawlScheduler 목록 → CrawlSchedulerResponse 목록 변환")
        void shouldConvertSchedulerListToResponses() {
            // Given
            List<CrawlScheduler> schedulers =
                    List.of(
                            CrawlSchedulerFixture.anActiveScheduler(1L),
                            CrawlSchedulerFixture.anActiveScheduler(2L),
                            CrawlSchedulerFixture.anActiveScheduler(3L));

            // When
            List<CrawlSchedulerResponse> result = assembler.toResponses(schedulers);

            // Then
            assertThat(result).hasSize(3);
            assertThat(result.get(0).crawlSchedulerId()).isEqualTo(1L);
            assertThat(result.get(1).crawlSchedulerId()).isEqualTo(2L);
            assertThat(result.get(2).crawlSchedulerId()).isEqualTo(3L);
        }

        @Test
        @DisplayName("[성공] 빈 목록 → 빈 목록 반환")
        void shouldReturnEmptyListForEmptySchedulers() {
            // Given
            List<CrawlScheduler> schedulers = List.of();

            // When
            List<CrawlSchedulerResponse> result = assembler.toResponses(schedulers);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("toPageResponse() 테스트")
    class ToPageResponse {

        @Test
        @DisplayName("[성공] CrawlScheduler 목록 → PageResponse 변환")
        void shouldConvertSchedulersToPageResponse() {
            // Given
            List<CrawlScheduler> schedulers =
                    List.of(
                            CrawlSchedulerFixture.anActiveScheduler(1L),
                            CrawlSchedulerFixture.anActiveScheduler(2L));
            int page = 0;
            int size = 10;
            long totalElements = 25L;

            // When
            PageResponse<CrawlSchedulerResponse> result =
                    assembler.toPageResponse(schedulers, page, size, totalElements);

            // Then
            assertThat(result.content()).hasSize(2);
            assertThat(result.page()).isZero();
            assertThat(result.size()).isEqualTo(10);
            assertThat(result.totalElements()).isEqualTo(25L);
            assertThat(result.totalPages()).isEqualTo(3);
            assertThat(result.first()).isTrue();
            assertThat(result.last()).isFalse();
        }

        @Test
        @DisplayName("[성공] 마지막 페이지 → last = true")
        void shouldReturnLastPageWhenOnLastPage() {
            // Given
            List<CrawlScheduler> schedulers = List.of(CrawlSchedulerFixture.anActiveScheduler(1L));
            int page = 2;
            int size = 10;
            long totalElements = 25L;

            // When
            PageResponse<CrawlSchedulerResponse> result =
                    assembler.toPageResponse(schedulers, page, size, totalElements);

            // Then
            assertThat(result.first()).isFalse();
            assertThat(result.last()).isTrue();
        }
    }
}
