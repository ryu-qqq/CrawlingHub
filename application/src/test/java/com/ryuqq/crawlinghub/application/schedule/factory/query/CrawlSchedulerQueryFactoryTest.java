package com.ryuqq.crawlinghub.application.schedule.factory.query;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.application.schedule.dto.query.SearchCrawlSchedulersQuery;
import com.ryuqq.crawlinghub.domain.schedule.query.CrawlSchedulerPageCriteria;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("application")
@Tag("factory")
@DisplayName("CrawlSchedulerQueryFactory 단위 테스트")
class CrawlSchedulerQueryFactoryTest {

    private CrawlSchedulerQueryFactory factory;

    @BeforeEach
    void setUp() {
        factory = new CrawlSchedulerQueryFactory();
    }

    @Nested
    @DisplayName("createCriteria() 메서드는")
    class CreateCriteriaMethod {

        @Test
        @DisplayName("모든 필드가 있는 Query를 Criteria로 변환한다")
        void shouldConvertQueryWithAllFields() {
            // Given
            SearchCrawlSchedulersQuery query =
                    new SearchCrawlSchedulersQuery(
                            100L, List.of(SchedulerStatus.ACTIVE), null, null, 1, 20);

            // When
            CrawlSchedulerPageCriteria criteria = factory.createCriteria(query);

            // Then
            assertThat(criteria.sellerId().value()).isEqualTo(100L);
            assertThat(criteria.status()).isEqualTo(SchedulerStatus.ACTIVE);
            assertThat(criteria.pageRequest().page()).isEqualTo(1);
            assertThat(criteria.size()).isEqualTo(20);
        }

        @Test
        @DisplayName("sellerId가 null이면 Criteria에도 null로 설정한다")
        void shouldSetNullSellerIdWhenQueryHasNull() {
            // Given
            SearchCrawlSchedulersQuery query =
                    new SearchCrawlSchedulersQuery(
                            null, List.of(SchedulerStatus.ACTIVE), null, null, 1, 10);

            // When
            CrawlSchedulerPageCriteria criteria = factory.createCriteria(query);

            // Then
            assertThat(criteria.sellerId()).isNull();
            assertThat(criteria.status()).isEqualTo(SchedulerStatus.ACTIVE);
        }

        @Test
        @DisplayName("status가 null이어도 Criteria로 변환한다")
        void shouldConvertQueryWithNullStatus() {
            // Given
            SearchCrawlSchedulersQuery query =
                    new SearchCrawlSchedulersQuery(100L, null, null, null, 2, 50);

            // When
            CrawlSchedulerPageCriteria criteria = factory.createCriteria(query);

            // Then
            assertThat(criteria.sellerId().value()).isEqualTo(100L);
            assertThat(criteria.status()).isNull();
            assertThat(criteria.pageRequest().page()).isEqualTo(2);
            assertThat(criteria.size()).isEqualTo(50);
        }

        @Test
        @DisplayName("INACTIVE 상태로 필터링할 수 있다")
        void shouldConvertQueryWithInactiveStatus() {
            // Given
            SearchCrawlSchedulersQuery query =
                    new SearchCrawlSchedulersQuery(
                            200L, List.of(SchedulerStatus.INACTIVE), null, null, 0, 10);

            // When
            CrawlSchedulerPageCriteria criteria = factory.createCriteria(query);

            // Then
            assertThat(criteria.status()).isEqualTo(SchedulerStatus.INACTIVE);
        }

        @Test
        @DisplayName("모든 검색 조건이 null이어도 페이지네이션은 유지한다")
        void shouldKeepPaginationWhenAllSearchConditionsAreNull() {
            // Given
            SearchCrawlSchedulersQuery query =
                    new SearchCrawlSchedulersQuery(null, null, null, null, 5, 50);

            // When
            CrawlSchedulerPageCriteria criteria = factory.createCriteria(query);

            // Then
            assertThat(criteria.sellerId()).isNull();
            assertThat(criteria.status()).isNull();
            assertThat(criteria.pageRequest().page()).isEqualTo(5);
            assertThat(criteria.size()).isEqualTo(50);
        }
    }
}
