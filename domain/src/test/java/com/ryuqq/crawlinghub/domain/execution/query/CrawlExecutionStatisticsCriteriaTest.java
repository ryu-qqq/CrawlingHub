package com.ryuqq.crawlinghub.domain.execution.query;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("query")
@DisplayName("CrawlExecutionStatisticsCriteria 단위 테스트")
class CrawlExecutionStatisticsCriteriaTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreationTest {

        @Test
        @DisplayName("모든 필드로 생성한다")
        void createWithAllFields() {
            // given
            CrawlSchedulerId schedulerId = CrawlSchedulerId.of(1L);
            SellerId sellerId = SellerId.of(100L);
            Instant from = Instant.now().minusSeconds(3600);
            Instant to = Instant.now();

            // when
            CrawlExecutionStatisticsCriteria criteria =
                    new CrawlExecutionStatisticsCriteria(schedulerId, sellerId, from, to);

            // then
            assertThat(criteria.crawlSchedulerId()).isEqualTo(schedulerId);
            assertThat(criteria.sellerId()).isEqualTo(sellerId);
            assertThat(criteria.from()).isEqualTo(from);
            assertThat(criteria.to()).isEqualTo(to);
        }

        @Test
        @DisplayName("모든 필드를 null로 생성한다")
        void createWithAllNullFields() {
            // when
            CrawlExecutionStatisticsCriteria criteria =
                    new CrawlExecutionStatisticsCriteria(null, null, null, null);

            // then
            assertThat(criteria.crawlSchedulerId()).isNull();
            assertThat(criteria.sellerId()).isNull();
            assertThat(criteria.from()).isNull();
            assertThat(criteria.to()).isNull();
        }
    }

    @Nested
    @DisplayName("필터 여부 확인 메서드 테스트")
    class FilterCheckTest {

        @Test
        @DisplayName("hasSchedulerFilter - crawlSchedulerId가 있으면 true")
        void hasSchedulerFilterWhenPresent() {
            // given
            CrawlExecutionStatisticsCriteria criteria =
                    new CrawlExecutionStatisticsCriteria(CrawlSchedulerId.of(1L), null, null, null);

            // then
            assertThat(criteria.hasSchedulerFilter()).isTrue();
        }

        @Test
        @DisplayName("hasSchedulerFilter - crawlSchedulerId가 없으면 false")
        void hasSchedulerFilterWhenNull() {
            // given
            CrawlExecutionStatisticsCriteria criteria =
                    new CrawlExecutionStatisticsCriteria(null, null, null, null);

            // then
            assertThat(criteria.hasSchedulerFilter()).isFalse();
        }

        @Test
        @DisplayName("hasSellerFilter - sellerId가 있으면 true")
        void hasSellerFilterWhenPresent() {
            // given
            CrawlExecutionStatisticsCriteria criteria =
                    new CrawlExecutionStatisticsCriteria(null, SellerId.of(100L), null, null);

            // then
            assertThat(criteria.hasSellerFilter()).isTrue();
        }

        @Test
        @DisplayName("hasSellerFilter - sellerId가 없으면 false")
        void hasSellerFilterWhenNull() {
            // given
            CrawlExecutionStatisticsCriteria criteria =
                    new CrawlExecutionStatisticsCriteria(null, null, null, null);

            // then
            assertThat(criteria.hasSellerFilter()).isFalse();
        }

        @Test
        @DisplayName("hasPeriodFilter - from과 to가 모두 있으면 true")
        void hasPeriodFilterWhenBothPresent() {
            // given
            CrawlExecutionStatisticsCriteria criteria =
                    new CrawlExecutionStatisticsCriteria(
                            null, null, Instant.now().minusSeconds(3600), Instant.now());

            // then
            assertThat(criteria.hasPeriodFilter()).isTrue();
        }

        @Test
        @DisplayName("hasPeriodFilter - from만 있으면 false")
        void hasPeriodFilterWhenOnlyFrom() {
            // given
            CrawlExecutionStatisticsCriteria criteria =
                    new CrawlExecutionStatisticsCriteria(null, null, Instant.now(), null);

            // then
            assertThat(criteria.hasPeriodFilter()).isFalse();
        }

        @Test
        @DisplayName("hasFromFilter - from이 있으면 true")
        void hasFromFilterWhenPresent() {
            // given
            CrawlExecutionStatisticsCriteria criteria =
                    new CrawlExecutionStatisticsCriteria(null, null, Instant.now(), null);

            // then
            assertThat(criteria.hasFromFilter()).isTrue();
        }

        @Test
        @DisplayName("hasFromFilter - from이 없으면 false")
        void hasFromFilterWhenNull() {
            // given
            CrawlExecutionStatisticsCriteria criteria =
                    new CrawlExecutionStatisticsCriteria(null, null, null, null);

            // then
            assertThat(criteria.hasFromFilter()).isFalse();
        }

        @Test
        @DisplayName("hasToFilter - to가 있으면 true")
        void hasToFilterWhenPresent() {
            // given
            CrawlExecutionStatisticsCriteria criteria =
                    new CrawlExecutionStatisticsCriteria(null, null, null, Instant.now());

            // then
            assertThat(criteria.hasToFilter()).isTrue();
        }

        @Test
        @DisplayName("hasToFilter - to가 없으면 false")
        void hasToFilterWhenNull() {
            // given
            CrawlExecutionStatisticsCriteria criteria =
                    new CrawlExecutionStatisticsCriteria(null, null, null, null);

            // then
            assertThat(criteria.hasToFilter()).isFalse();
        }
    }
}
