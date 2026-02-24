package com.ryuqq.crawlinghub.application.schedule.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerFixture;
import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerIdFixture;
import com.ryuqq.cralwinghub.domain.fixture.seller.SellerIdFixture;
import com.ryuqq.crawlinghub.application.schedule.port.out.query.CrawlScheduleQueryPort;
import com.ryuqq.crawlinghub.domain.common.vo.QueryContext;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.exception.CrawlSchedulerNotFoundException;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.schedule.query.CrawlSchedulerSearchCriteria;
import com.ryuqq.crawlinghub.domain.schedule.query.CrawlSchedulerSortKey;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawlSchedulerReadManager 단위 테스트
 *
 * <p>CrawlScheduleQueryPort 위임 검증
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawlSchedulerReadManager 테스트")
class CrawlSchedulerReadManagerTest {

    @Mock private CrawlScheduleQueryPort crawlScheduleQueryPort;

    @InjectMocks private CrawlSchedulerReadManager sut;

    @Nested
    @DisplayName("findById() 테스트")
    class FindById {

        @Test
        @DisplayName("[성공] 존재하는 스케줄러 반환")
        void shouldReturnSchedulerWhenFound() {
            CrawlSchedulerId id = CrawlSchedulerIdFixture.anAssignedId();
            CrawlScheduler scheduler = CrawlSchedulerFixture.anActiveScheduler();
            given(crawlScheduleQueryPort.findById(id)).willReturn(Optional.of(scheduler));

            Optional<CrawlScheduler> result = sut.findById(id);

            assertThat(result).contains(scheduler);
        }

        @Test
        @DisplayName("[성공] 존재하지 않으면 empty 반환")
        void shouldReturnEmptyWhenNotFound() {
            CrawlSchedulerId id = CrawlSchedulerIdFixture.anAssignedId();
            given(crawlScheduleQueryPort.findById(id)).willReturn(Optional.empty());

            Optional<CrawlScheduler> result = sut.findById(id);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getById() 테스트")
    class GetById {

        @Test
        @DisplayName("[성공] 존재하는 스케줄러 반환")
        void shouldReturnSchedulerWhenFound() {
            CrawlSchedulerId id = CrawlSchedulerIdFixture.anAssignedId();
            CrawlScheduler scheduler = CrawlSchedulerFixture.anActiveScheduler();
            given(crawlScheduleQueryPort.findById(id)).willReturn(Optional.of(scheduler));

            CrawlScheduler result = sut.getById(id);

            assertThat(result).isEqualTo(scheduler);
        }

        @Test
        @DisplayName("[실패] 존재하지 않으면 CrawlSchedulerNotFoundException 발생")
        void shouldThrowWhenSchedulerNotFound() {
            CrawlSchedulerId id = CrawlSchedulerIdFixture.anAssignedId();
            given(crawlScheduleQueryPort.findById(id)).willReturn(Optional.empty());

            assertThatThrownBy(() -> sut.getById(id))
                    .isInstanceOf(CrawlSchedulerNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("existsBySellerIdAndSchedulerName() 테스트")
    class ExistsBySellerIdAndSchedulerName {

        @Test
        @DisplayName("[성공] 존재하면 true 반환")
        void shouldReturnTrueWhenExists() {
            SellerId sellerId = SellerIdFixture.anAssignedId();
            given(crawlScheduleQueryPort.existsBySellerIdAndSchedulerName(sellerId, "scheduler-1"))
                    .willReturn(true);

            boolean result = sut.existsBySellerIdAndSchedulerName(sellerId, "scheduler-1");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("[성공] 존재하지 않으면 false 반환")
        void shouldReturnFalseWhenNotExists() {
            SellerId sellerId = SellerIdFixture.anAssignedId();
            given(crawlScheduleQueryPort.existsBySellerIdAndSchedulerName(sellerId, "no-such"))
                    .willReturn(false);

            boolean result = sut.existsBySellerIdAndSchedulerName(sellerId, "no-such");

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("findByCriteria() 테스트")
    class FindByCriteria {

        @Test
        @DisplayName("[성공] 조건에 맞는 스케줄러 목록 반환")
        void shouldReturnSchedulerList() {
            CrawlScheduler scheduler = CrawlSchedulerFixture.anActiveScheduler();
            CrawlSchedulerSearchCriteria criteria =
                    CrawlSchedulerSearchCriteria.of(
                            SellerIdFixture.anAssignedId(),
                            null,
                            null,
                            null,
                            QueryContext.defaultOf(CrawlSchedulerSortKey.CREATED_AT));
            given(crawlScheduleQueryPort.findByCriteria(criteria)).willReturn(List.of(scheduler));

            List<CrawlScheduler> result = sut.findByCriteria(criteria);

            assertThat(result).hasSize(1);
            then(crawlScheduleQueryPort).should().findByCriteria(criteria);
        }
    }

    @Nested
    @DisplayName("countByCriteria() 테스트")
    class CountByCriteria {

        @Test
        @DisplayName("[성공] 조건에 맞는 스케줄러 개수 반환")
        void shouldReturnSchedulerCount() {
            CrawlSchedulerSearchCriteria criteria =
                    CrawlSchedulerSearchCriteria.of(
                            SellerIdFixture.anAssignedId(),
                            null,
                            null,
                            null,
                            QueryContext.defaultOf(CrawlSchedulerSortKey.CREATED_AT));
            given(crawlScheduleQueryPort.countByCriteria(criteria)).willReturn(5L);

            long result = sut.countByCriteria(criteria);

            assertThat(result).isEqualTo(5L);
        }
    }

    @Nested
    @DisplayName("findActiveSchedulersBySellerId() 테스트")
    class FindActiveSchedulersBySellerId {

        @Test
        @DisplayName("[성공] 셀러별 활성 스케줄러 목록 반환")
        void shouldReturnActiveSchedulers() {
            SellerId sellerId = SellerIdFixture.anAssignedId();
            CrawlScheduler scheduler = CrawlSchedulerFixture.anActiveScheduler();
            given(crawlScheduleQueryPort.findActiveSchedulersBySellerId(sellerId))
                    .willReturn(List.of(scheduler));

            List<CrawlScheduler> result = sut.findActiveSchedulersBySellerId(sellerId);

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("countBySellerId() 테스트")
    class CountBySellerId {

        @Test
        @DisplayName("[성공] 셀러별 전체 스케줄러 개수 반환")
        void shouldReturnTotalCount() {
            SellerId sellerId = SellerIdFixture.anAssignedId();
            given(crawlScheduleQueryPort.countBySellerId(sellerId)).willReturn(3L);

            long result = sut.countBySellerId(sellerId);

            assertThat(result).isEqualTo(3L);
        }
    }

    @Nested
    @DisplayName("countActiveSchedulersBySellerId() 테스트")
    class CountActiveSchedulersBySellerId {

        @Test
        @DisplayName("[성공] 셀러별 활성 스케줄러 개수 반환")
        void shouldReturnActiveCount() {
            SellerId sellerId = SellerIdFixture.anAssignedId();
            given(crawlScheduleQueryPort.countActiveSchedulersBySellerId(sellerId)).willReturn(2L);

            long result = sut.countActiveSchedulersBySellerId(sellerId);

            assertThat(result).isEqualTo(2L);
        }
    }

    @Nested
    @DisplayName("findBySellerId() 테스트")
    class FindBySellerId {

        @Test
        @DisplayName("[성공] 셀러별 전체 스케줄러 목록 반환")
        void shouldReturnAllSchedulersBySeller() {
            SellerId sellerId = SellerIdFixture.anAssignedId();
            CrawlScheduler scheduler = CrawlSchedulerFixture.anActiveScheduler();
            given(crawlScheduleQueryPort.findBySellerId(sellerId)).willReturn(List.of(scheduler));

            List<CrawlScheduler> result = sut.findBySellerId(sellerId);

            assertThat(result).hasSize(1);
        }
    }
}
