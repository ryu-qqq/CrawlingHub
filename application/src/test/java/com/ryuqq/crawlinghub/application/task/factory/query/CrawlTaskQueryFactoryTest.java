package com.ryuqq.crawlinghub.application.task.factory.query;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.application.task.dto.query.CrawlTaskSearchParams;
import com.ryuqq.crawlinghub.domain.task.query.CrawlTaskCriteria;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatus;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("application")
@Tag("factory")
@DisplayName("CrawlTaskQueryFactory 단위 테스트")
class CrawlTaskQueryFactoryTest {

    private CrawlTaskQueryFactory factory;

    @BeforeEach
    void setUp() {
        factory = new CrawlTaskQueryFactory();
    }

    @Nested
    @DisplayName("createCriteria() 메서드는")
    class CreateCriteriaMethod {

        @Test
        @DisplayName("모든 필드가 있는 SearchParams를 Criteria로 변환한다")
        void shouldConvertSearchParamsWithAllFields() {
            // Given
            CrawlTaskSearchParams params =
                    new CrawlTaskSearchParams(
                            List.of(100L), null, List.of("WAITING"), null, null, null, 1, 20);

            // When
            CrawlTaskCriteria criteria = factory.createCriteria(params);

            // Then
            assertThat(criteria.crawlSchedulerIds()).hasSize(1);
            assertThat(criteria.crawlSchedulerIds().get(0).value()).isEqualTo(100L);
            assertThat(criteria.statuses()).containsExactly(CrawlTaskStatus.WAITING);
            assertThat(criteria.page()).isEqualTo(1);
            assertThat(criteria.size()).isEqualTo(20);
        }

        @Test
        @DisplayName("status가 null이어도 Criteria로 변환한다")
        void shouldConvertSearchParamsWithNullStatus() {
            // Given
            CrawlTaskSearchParams params =
                    new CrawlTaskSearchParams(List.of(100L), null, null, null, null, null, 2, 50);

            // When
            CrawlTaskCriteria criteria = factory.createCriteria(params);

            // Then
            assertThat(criteria.crawlSchedulerIds()).hasSize(1);
            assertThat(criteria.crawlSchedulerIds().get(0).value()).isEqualTo(100L);
            assertThat(criteria.statuses()).isNull();
            assertThat(criteria.page()).isEqualTo(2);
            assertThat(criteria.size()).isEqualTo(50);
        }

        @Test
        @DisplayName("RUNNING 상태로 필터링할 수 있다")
        void shouldConvertSearchParamsWithRunningStatus() {
            // Given
            CrawlTaskSearchParams params =
                    new CrawlTaskSearchParams(
                            List.of(200L), null, List.of("RUNNING"), null, null, null, 0, 10);

            // When
            CrawlTaskCriteria criteria = factory.createCriteria(params);

            // Then
            assertThat(criteria.statuses()).containsExactly(CrawlTaskStatus.RUNNING);
        }

        @Test
        @DisplayName("SUCCESS 상태로 필터링할 수 있다")
        void shouldConvertSearchParamsWithSuccessStatus() {
            // Given
            CrawlTaskSearchParams params =
                    new CrawlTaskSearchParams(
                            List.of(300L), null, List.of("SUCCESS"), null, null, null, 0, 10);

            // When
            CrawlTaskCriteria criteria = factory.createCriteria(params);

            // Then
            assertThat(criteria.statuses()).containsExactly(CrawlTaskStatus.SUCCESS);
        }

        @Test
        @DisplayName("FAILED 상태로 필터링할 수 있다")
        void shouldConvertSearchParamsWithFailedStatus() {
            // Given
            CrawlTaskSearchParams params =
                    new CrawlTaskSearchParams(
                            List.of(400L), null, List.of("FAILED"), null, null, null, 0, 10);

            // When
            CrawlTaskCriteria criteria = factory.createCriteria(params);

            // Then
            assertThat(criteria.statuses()).containsExactly(CrawlTaskStatus.FAILED);
        }

        @Test
        @DisplayName("다중 schedulerIds를 변환한다")
        void shouldConvertMultipleSchedulerIds() {
            // Given
            CrawlTaskSearchParams params =
                    new CrawlTaskSearchParams(
                            List.of(100L, 200L, 300L), null, null, null, null, null, 0, 10);

            // When
            CrawlTaskCriteria criteria = factory.createCriteria(params);

            // Then
            assertThat(criteria.crawlSchedulerIds()).hasSize(3);
            assertThat(criteria.hasSchedulerIdFilter()).isTrue();
        }

        @Test
        @DisplayName("다중 sellerIds를 변환한다")
        void shouldConvertMultipleSellerIds() {
            // Given
            CrawlTaskSearchParams params =
                    new CrawlTaskSearchParams(
                            null, List.of(10L, 20L), null, null, null, null, 0, 10);

            // When
            CrawlTaskCriteria criteria = factory.createCriteria(params);

            // Then
            assertThat(criteria.sellerIds()).hasSize(2);
            assertThat(criteria.hasSellerIdFilter()).isTrue();
        }
    }
}
