package com.ryuqq.crawlinghub.domain.seller.query;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.domain.seller.vo.MustItSellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
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
@DisplayName("SellerQueryCriteria 단위 테스트")
class SellerQueryCriteriaTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreationTest {

        @Test
        @DisplayName("모든 필드로 생성한다")
        void createWithAllFields() {
            // given
            MustItSellerName mustItSellerName = MustItSellerName.of("TestSeller");
            SellerName sellerName = SellerName.of("테스트셀러");
            List<SellerStatus> statuses = List.of(SellerStatus.ACTIVE);
            Instant from = Instant.now().minusSeconds(3600);
            Instant to = Instant.now();

            // when
            SellerQueryCriteria criteria =
                    new SellerQueryCriteria(
                            mustItSellerName, sellerName, statuses, from, to, 0, 20);

            // then
            assertThat(criteria.mustItSellerName()).isEqualTo(mustItSellerName);
            assertThat(criteria.sellerName()).isEqualTo(sellerName);
            assertThat(criteria.statuses()).hasSize(1);
            assertThat(criteria.createdFrom()).isEqualTo(from);
            assertThat(criteria.createdTo()).isEqualTo(to);
            assertThat(criteria.page()).isEqualTo(0);
            assertThat(criteria.size()).isEqualTo(20);
        }

        @Test
        @DisplayName("모든 필드를 null로 생성한다")
        void createWithAllNullFields() {
            // when
            SellerQueryCriteria criteria =
                    new SellerQueryCriteria(null, null, null, null, null, null, null);

            // then
            assertThat(criteria.mustItSellerName()).isNull();
            assertThat(criteria.sellerName()).isNull();
            assertThat(criteria.statuses()).isNull();
            assertThat(criteria.createdFrom()).isNull();
            assertThat(criteria.createdTo()).isNull();
            assertThat(criteria.page()).isNull();
            assertThat(criteria.size()).isNull();
        }

        @Test
        @DisplayName("statuses 목록은 방어적 복사된다")
        void statusesAreDefensivelyCopied() {
            // given
            List<SellerStatus> mutableStatuses = new ArrayList<>();
            mutableStatuses.add(SellerStatus.ACTIVE);

            // when
            SellerQueryCriteria criteria =
                    new SellerQueryCriteria(null, null, mutableStatuses, null, null, 0, 20);
            mutableStatuses.add(SellerStatus.INACTIVE);

            // then
            assertThat(criteria.statuses()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("hasStatusFilter() 메서드 테스트")
    class HasStatusFilterTest {

        @Test
        @DisplayName("statuses가 있으면 true")
        void hasStatusFilterWhenPresent() {
            // given
            SellerQueryCriteria criteria =
                    new SellerQueryCriteria(
                            null, null, List.of(SellerStatus.ACTIVE), null, null, 0, 20);

            // then
            assertThat(criteria.hasStatusFilter()).isTrue();
        }

        @Test
        @DisplayName("statuses가 null이면 false")
        void hasStatusFilterWhenNull() {
            // given
            SellerQueryCriteria criteria =
                    new SellerQueryCriteria(null, null, null, null, null, 0, 20);

            // then
            assertThat(criteria.hasStatusFilter()).isFalse();
        }

        @Test
        @DisplayName("statuses가 빈 목록이면 false")
        void hasStatusFilterWhenEmpty() {
            // given
            SellerQueryCriteria criteria =
                    new SellerQueryCriteria(null, null, List.of(), null, null, 0, 20);

            // then
            assertThat(criteria.hasStatusFilter()).isFalse();
        }
    }

    @Nested
    @DisplayName("hasDateFilter() 메서드 테스트")
    class HasDateFilterTest {

        @Test
        @DisplayName("createdFrom이 있으면 true")
        void hasDateFilterWhenFromPresent() {
            // given
            SellerQueryCriteria criteria =
                    new SellerQueryCriteria(null, null, null, Instant.now(), null, 0, 20);

            // then
            assertThat(criteria.hasDateFilter()).isTrue();
        }

        @Test
        @DisplayName("createdTo가 있으면 true")
        void hasDateFilterWhenToPresent() {
            // given
            SellerQueryCriteria criteria =
                    new SellerQueryCriteria(null, null, null, null, Instant.now(), 0, 20);

            // then
            assertThat(criteria.hasDateFilter()).isTrue();
        }

        @Test
        @DisplayName("from과 to가 모두 있으면 true")
        void hasDateFilterWhenBothPresent() {
            // given
            SellerQueryCriteria criteria =
                    new SellerQueryCriteria(
                            null,
                            null,
                            null,
                            Instant.now().minusSeconds(3600),
                            Instant.now(),
                            0,
                            20);

            // then
            assertThat(criteria.hasDateFilter()).isTrue();
        }

        @Test
        @DisplayName("from과 to가 모두 없으면 false")
        void hasDateFilterWhenBothNull() {
            // given
            SellerQueryCriteria criteria =
                    new SellerQueryCriteria(null, null, null, null, null, 0, 20);

            // then
            assertThat(criteria.hasDateFilter()).isFalse();
        }
    }

    @Nested
    @DisplayName("status() 메서드 테스트 (하위 호환성)")
    class StatusTest {

        @Test
        @DisplayName("statuses가 있으면 첫 번째 상태를 반환한다")
        void statusReturnsFirstWhenPresent() {
            // given
            SellerQueryCriteria criteria =
                    new SellerQueryCriteria(
                            null,
                            null,
                            List.of(SellerStatus.ACTIVE, SellerStatus.INACTIVE),
                            null,
                            null,
                            0,
                            20);

            // then
            assertThat(criteria.status()).isEqualTo(SellerStatus.ACTIVE);
        }

        @Test
        @DisplayName("statuses가 없으면 null을 반환한다")
        void statusReturnsNullWhenEmpty() {
            // given
            SellerQueryCriteria criteria =
                    new SellerQueryCriteria(null, null, null, null, null, 0, 20);

            // then
            assertThat(criteria.status()).isNull();
        }

        @Test
        @DisplayName("statuses가 빈 목록이면 null을 반환한다")
        void statusReturnsNullWhenStatusesIsEmpty() {
            // given
            SellerQueryCriteria criteria =
                    new SellerQueryCriteria(null, null, List.of(), null, null, 0, 20);

            // then
            assertThat(criteria.status()).isNull();
        }
    }
}
