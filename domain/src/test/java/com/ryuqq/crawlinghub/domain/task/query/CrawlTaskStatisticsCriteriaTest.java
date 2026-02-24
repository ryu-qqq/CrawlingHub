package com.ryuqq.crawlinghub.domain.task.query;

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
@DisplayName("CrawlTaskStatisticsCriteria 단위 테스트")
class CrawlTaskStatisticsCriteriaTest {

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
            CrawlTaskStatisticsCriteria criteria =
                    new CrawlTaskStatisticsCriteria(schedulerId, sellerId, from, to);

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
            CrawlTaskStatisticsCriteria criteria =
                    new CrawlTaskStatisticsCriteria(null, null, null, null);

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
            CrawlTaskStatisticsCriteria criteria =
                    new CrawlTaskStatisticsCriteria(CrawlSchedulerId.of(1L), null, null, null);

            // then
            assertThat(criteria.hasSchedulerFilter()).isTrue();
        }

        @Test
        @DisplayName("hasSchedulerFilter - crawlSchedulerId가 없으면 false")
        void hasSchedulerFilterWhenNull() {
            // given
            CrawlTaskStatisticsCriteria criteria =
                    new CrawlTaskStatisticsCriteria(null, null, null, null);

            // then
            assertThat(criteria.hasSchedulerFilter()).isFalse();
        }

        @Test
        @DisplayName("hasSellerFilter - sellerId가 있으면 true")
        void hasSellerFilterWhenPresent() {
            // given
            CrawlTaskStatisticsCriteria criteria =
                    new CrawlTaskStatisticsCriteria(null, SellerId.of(100L), null, null);

            // then
            assertThat(criteria.hasSellerFilter()).isTrue();
        }

        @Test
        @DisplayName("hasSellerFilter - sellerId가 없으면 false")
        void hasSellerFilterWhenNull() {
            // given
            CrawlTaskStatisticsCriteria criteria =
                    new CrawlTaskStatisticsCriteria(null, null, null, null);

            // then
            assertThat(criteria.hasSellerFilter()).isFalse();
        }

        @Test
        @DisplayName("hasPeriodFilter - from과 to가 모두 있으면 true")
        void hasPeriodFilterWhenBothPresent() {
            // given
            Instant from = Instant.now().minusSeconds(3600);
            Instant to = Instant.now();
            CrawlTaskStatisticsCriteria criteria =
                    new CrawlTaskStatisticsCriteria(null, null, from, to);

            // then
            assertThat(criteria.hasPeriodFilter()).isTrue();
        }

        @Test
        @DisplayName("hasPeriodFilter - from만 있으면 false")
        void hasPeriodFilterWhenOnlyFrom() {
            // given
            CrawlTaskStatisticsCriteria criteria =
                    new CrawlTaskStatisticsCriteria(null, null, Instant.now(), null);

            // then
            assertThat(criteria.hasPeriodFilter()).isFalse();
        }

        @Test
        @DisplayName("hasPeriodFilter - to만 있으면 false")
        void hasPeriodFilterWhenOnlyTo() {
            // given
            CrawlTaskStatisticsCriteria criteria =
                    new CrawlTaskStatisticsCriteria(null, null, null, Instant.now());

            // then
            assertThat(criteria.hasPeriodFilter()).isFalse();
        }

        @Test
        @DisplayName("hasPeriodFilter - 모두 없으면 false")
        void hasPeriodFilterWhenBothNull() {
            // given
            CrawlTaskStatisticsCriteria criteria =
                    new CrawlTaskStatisticsCriteria(null, null, null, null);

            // then
            assertThat(criteria.hasPeriodFilter()).isFalse();
        }

        @Test
        @DisplayName("hasFromFilter - from이 있으면 true")
        void hasFromFilterWhenPresent() {
            // given
            CrawlTaskStatisticsCriteria criteria =
                    new CrawlTaskStatisticsCriteria(null, null, Instant.now(), null);

            // then
            assertThat(criteria.hasFromFilter()).isTrue();
        }

        @Test
        @DisplayName("hasFromFilter - from이 없으면 false")
        void hasFromFilterWhenNull() {
            // given
            CrawlTaskStatisticsCriteria criteria =
                    new CrawlTaskStatisticsCriteria(null, null, null, null);

            // then
            assertThat(criteria.hasFromFilter()).isFalse();
        }

        @Test
        @DisplayName("hasToFilter - to가 있으면 true")
        void hasToFilterWhenPresent() {
            // given
            CrawlTaskStatisticsCriteria criteria =
                    new CrawlTaskStatisticsCriteria(null, null, null, Instant.now());

            // then
            assertThat(criteria.hasToFilter()).isTrue();
        }

        @Test
        @DisplayName("hasToFilter - to가 없으면 false")
        void hasToFilterWhenNull() {
            // given
            CrawlTaskStatisticsCriteria criteria =
                    new CrawlTaskStatisticsCriteria(null, null, null, null);

            // then
            assertThat(criteria.hasToFilter()).isFalse();
        }
    }
}
