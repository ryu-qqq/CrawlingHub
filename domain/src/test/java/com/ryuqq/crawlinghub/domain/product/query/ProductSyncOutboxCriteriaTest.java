package com.ryuqq.crawlinghub.domain.product.query;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("query")
@DisplayName("ProductSyncOutboxCriteria 단위 테스트")
class ProductSyncOutboxCriteriaTest {

    @Nested
    @DisplayName("생성자 정규화 테스트")
    class NormalizationTest {

        @Test
        @DisplayName("음수 offset은 0으로 정규화된다")
        void normalizeNegativeOffset() {
            // when
            ProductSyncOutboxCriteria criteria =
                    new ProductSyncOutboxCriteria(null, null, null, null, null, -5, 100);

            // then
            assertThat(criteria.offset()).isEqualTo(0);
        }

        @Test
        @DisplayName("0 이하 limit은 DEFAULT_LIMIT(100)으로 정규화된다")
        void normalizeZeroLimit() {
            // when
            ProductSyncOutboxCriteria criteria =
                    new ProductSyncOutboxCriteria(null, null, null, null, null, 0, 0);

            // then
            assertThat(criteria.limit()).isEqualTo(100);
        }

        @Test
        @DisplayName("음수 limit은 DEFAULT_LIMIT(100)으로 정규화된다")
        void normalizeNegativeLimit() {
            // when
            ProductSyncOutboxCriteria criteria =
                    new ProductSyncOutboxCriteria(null, null, null, null, null, 0, -10);

            // then
            assertThat(criteria.limit()).isEqualTo(100);
        }

        @Test
        @DisplayName("statuses 목록은 방어적 복사된다")
        void statusesAreDefensivelyCopied() {
            // given
            List<ProductOutboxStatus> mutableStatuses = new ArrayList<>();
            mutableStatuses.add(ProductOutboxStatus.PENDING);

            // when
            ProductSyncOutboxCriteria criteria =
                    new ProductSyncOutboxCriteria(null, mutableStatuses, null, null, null, 0, 100);
            mutableStatuses.add(ProductOutboxStatus.FAILED);

            // then
            assertThat(criteria.statuses()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("byStatus 팩토리 메서드 테스트")
    class ByStatusTest {

        @Test
        @DisplayName("단일 상태로 조회 조건을 생성한다")
        void createByStatus() {
            // when
            ProductSyncOutboxCriteria criteria =
                    ProductSyncOutboxCriteria.byStatus(ProductOutboxStatus.PENDING, 50);

            // then
            assertThat(criteria.status()).isEqualTo(ProductOutboxStatus.PENDING);
            assertThat(criteria.statuses()).isNull();
            assertThat(criteria.maxRetryCount()).isNull();
            assertThat(criteria.offset()).isEqualTo(0);
            assertThat(criteria.limit()).isEqualTo(50);
        }
    }

    @Nested
    @DisplayName("byStatuses 팩토리 메서드 테스트")
    class ByStatusesTest {

        @Test
        @DisplayName("다중 상태로 조회 조건을 생성한다")
        void createByStatuses() {
            // given
            List<ProductOutboxStatus> statuses =
                    List.of(ProductOutboxStatus.PENDING, ProductOutboxStatus.FAILED);

            // when
            ProductSyncOutboxCriteria criteria =
                    ProductSyncOutboxCriteria.byStatuses(statuses, 100);

            // then
            assertThat(criteria.status()).isNull();
            assertThat(criteria.statuses())
                    .containsExactly(ProductOutboxStatus.PENDING, ProductOutboxStatus.FAILED);
            assertThat(criteria.maxRetryCount()).isNull();
        }
    }

    @Nested
    @DisplayName("pendingOrFailed 팩토리 메서드 테스트")
    class PendingOrFailedTest {

        @Test
        @DisplayName("PENDING 또는 FAILED 상태 조회 조건을 생성한다")
        void createPendingOrFailed() {
            // when
            ProductSyncOutboxCriteria criteria = ProductSyncOutboxCriteria.pendingOrFailed(200);

            // then
            assertThat(criteria.statuses())
                    .containsExactlyInAnyOrder(
                            ProductOutboxStatus.PENDING, ProductOutboxStatus.FAILED);
            assertThat(criteria.maxRetryCount()).isNull();
            assertThat(criteria.offset()).isEqualTo(0);
            assertThat(criteria.limit()).isEqualTo(200);
        }
    }

    @Nested
    @DisplayName("retryable 팩토리 메서드 테스트")
    class RetryableTest {

        @Test
        @DisplayName("재시도 가능한 Outbox 조회 조건을 생성한다")
        void createRetryable() {
            // when
            ProductSyncOutboxCriteria criteria = ProductSyncOutboxCriteria.retryable(3, 100);

            // then
            assertThat(criteria.statuses())
                    .containsExactlyInAnyOrder(
                            ProductOutboxStatus.PENDING, ProductOutboxStatus.FAILED);
            assertThat(criteria.maxRetryCount()).isEqualTo(3);
            assertThat(criteria.hasMaxRetryCountFilter()).isTrue();
        }
    }

    @Nested
    @DisplayName("withDateRange 팩토리 메서드 테스트")
    class WithDateRangeTest {

        @Test
        @DisplayName("다중 상태와 기간으로 조회 조건을 생성한다")
        void createWithDateRange() {
            // given
            List<ProductOutboxStatus> statuses =
                    List.of(ProductOutboxStatus.PENDING, ProductOutboxStatus.FAILED);
            Instant from = Instant.now().minusSeconds(3600);
            Instant to = Instant.now();

            // when
            ProductSyncOutboxCriteria criteria =
                    ProductSyncOutboxCriteria.withDateRange(statuses, from, to, 0, 100);

            // then
            assertThat(criteria.statuses()).hasSize(2);
            assertThat(criteria.createdFrom()).isEqualTo(from);
            assertThat(criteria.createdTo()).isEqualTo(to);
            assertThat(criteria.maxRetryCount()).isNull();
            assertThat(criteria.hasDateRangeFilter()).isTrue();
        }
    }

    @Nested
    @DisplayName("필터 여부 확인 메서드 테스트")
    class FilterCheckTest {

        @Test
        @DisplayName("hasSingleStatusFilter - 단일 상태가 있으면 true")
        void hasSingleStatusFilterWhenPresent() {
            // given
            ProductSyncOutboxCriteria criteria =
                    ProductSyncOutboxCriteria.byStatus(ProductOutboxStatus.PENDING, 100);

            // then
            assertThat(criteria.hasSingleStatusFilter()).isTrue();
            assertThat(criteria.hasStatusFilter()).isTrue();
        }

        @Test
        @DisplayName("hasMultipleStatusFilter - 다중 상태가 있으면 true")
        void hasMultipleStatusFilterWhenPresent() {
            // given
            ProductSyncOutboxCriteria criteria =
                    ProductSyncOutboxCriteria.byStatuses(List.of(ProductOutboxStatus.PENDING), 100);

            // then
            assertThat(criteria.hasMultipleStatusFilter()).isTrue();
            assertThat(criteria.hasStatusFilter()).isTrue();
        }

        @Test
        @DisplayName("hasStatusFilter - 아무 상태도 없으면 false")
        void hasStatusFilterWhenNone() {
            // given
            ProductSyncOutboxCriteria criteria =
                    new ProductSyncOutboxCriteria(null, null, null, null, null, 0, 100);

            // then
            assertThat(criteria.hasStatusFilter()).isFalse();
        }

        @Test
        @DisplayName("hasCreatedFromFilter - createdFrom이 있으면 true")
        void hasCreatedFromFilterWhenPresent() {
            // given
            ProductSyncOutboxCriteria criteria =
                    new ProductSyncOutboxCriteria(null, null, Instant.now(), null, null, 0, 100);

            // then
            assertThat(criteria.hasCreatedFromFilter()).isTrue();
        }

        @Test
        @DisplayName("hasCreatedToFilter - createdTo가 있으면 true")
        void hasCreatedToFilterWhenPresent() {
            // given
            ProductSyncOutboxCriteria criteria =
                    new ProductSyncOutboxCriteria(null, null, null, Instant.now(), null, 0, 100);

            // then
            assertThat(criteria.hasCreatedToFilter()).isTrue();
        }

        @Test
        @DisplayName("hasDateRangeFilter - from 또는 to가 있으면 true")
        void hasDateRangeFilterWhenEitherPresent() {
            // given
            ProductSyncOutboxCriteria onlyFrom =
                    new ProductSyncOutboxCriteria(null, null, Instant.now(), null, null, 0, 100);
            ProductSyncOutboxCriteria onlyTo =
                    new ProductSyncOutboxCriteria(null, null, null, Instant.now(), null, 0, 100);

            // then
            assertThat(onlyFrom.hasDateRangeFilter()).isTrue();
            assertThat(onlyTo.hasDateRangeFilter()).isTrue();
        }

        @Test
        @DisplayName("hasDateRangeFilter - 모두 없으면 false")
        void hasDateRangeFilterWhenNone() {
            // given
            ProductSyncOutboxCriteria criteria =
                    new ProductSyncOutboxCriteria(null, null, null, null, null, 0, 100);

            // then
            assertThat(criteria.hasDateRangeFilter()).isFalse();
        }

        @Test
        @DisplayName("hasMaxRetryCountFilter - maxRetryCount가 있으면 true")
        void hasMaxRetryCountFilterWhenPresent() {
            // given
            ProductSyncOutboxCriteria criteria = ProductSyncOutboxCriteria.retryable(3, 100);

            // then
            assertThat(criteria.hasMaxRetryCountFilter()).isTrue();
        }

        @Test
        @DisplayName("hasMaxRetryCountFilter - maxRetryCount가 없으면 false")
        void hasMaxRetryCountFilterWhenNull() {
            // given
            ProductSyncOutboxCriteria criteria =
                    new ProductSyncOutboxCriteria(null, null, null, null, null, 0, 100);

            // then
            assertThat(criteria.hasMaxRetryCountFilter()).isFalse();
        }
    }
}
