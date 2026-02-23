package com.ryuqq.crawlinghub.application.schedule.manager.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.application.schedule.manager.CrawlSchedulerReadManager;
import com.ryuqq.crawlinghub.application.schedule.port.out.query.CrawlScheduleQueryPort;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.schedule.query.CrawlSchedulerSearchCriteria;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawlSchedulerReadManager 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawlSchedulerReadManager 테스트")
class CrawlSchedulerReadManagerTest {

    @Mock private CrawlScheduleQueryPort crawlScheduleQueryPort;
    @Mock private CrawlScheduler crawlScheduler;
    @Mock private CrawlSchedulerSearchCriteria criteria;

    private CrawlSchedulerReadManager manager;

    @BeforeEach
    void setUp() {
        manager = new CrawlSchedulerReadManager(crawlScheduleQueryPort);
    }

    @Nested
    @DisplayName("findById() 테스트")
    class FindById {

        @Test
        @DisplayName("[성공] ID로 CrawlScheduler 조회")
        void shouldDelegateToQueryPort() {
            // Given
            CrawlSchedulerId crawlSchedulerId = CrawlSchedulerId.of(1L);
            given(crawlScheduleQueryPort.findById(crawlSchedulerId))
                    .willReturn(Optional.of(crawlScheduler));

            // When
            Optional<CrawlScheduler> result = manager.findById(crawlSchedulerId);

            // Then
            assertThat(result).isPresent().contains(crawlScheduler);
            verify(crawlScheduleQueryPort).findById(crawlSchedulerId);
        }

        @Test
        @DisplayName("[성공] 존재하지 않는 경우 empty 반환")
        void shouldReturnEmptyWhenNotFound() {
            // Given
            CrawlSchedulerId crawlSchedulerId = CrawlSchedulerId.of(999L);
            given(crawlScheduleQueryPort.findById(crawlSchedulerId)).willReturn(Optional.empty());

            // When
            Optional<CrawlScheduler> result = manager.findById(crawlSchedulerId);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsBySellerIdAndSchedulerName() 테스트")
    class ExistsBySellerIdAndSchedulerName {

        @Test
        @DisplayName("[성공] Seller ID와 스케줄러 이름으로 존재 확인")
        void shouldDelegateToQueryPort() {
            // Given
            SellerId sellerId = SellerId.of(1L);
            String schedulerName = "daily-crawl";
            given(crawlScheduleQueryPort.existsBySellerIdAndSchedulerName(sellerId, schedulerName))
                    .willReturn(true);

            // When
            boolean result = manager.existsBySellerIdAndSchedulerName(sellerId, schedulerName);

            // Then
            assertThat(result).isTrue();
            verify(crawlScheduleQueryPort)
                    .existsBySellerIdAndSchedulerName(sellerId, schedulerName);
        }
    }

    @Nested
    @DisplayName("findByCriteria() 테스트")
    class FindByCriteria {

        @Test
        @DisplayName("[성공] 조건으로 CrawlScheduler 목록 조회")
        void shouldDelegateToQueryPort() {
            // Given
            given(crawlScheduleQueryPort.findByCriteria(criteria))
                    .willReturn(List.of(crawlScheduler));

            // When
            List<CrawlScheduler> result = manager.findByCriteria(criteria);

            // Then
            assertThat(result).hasSize(1).contains(crawlScheduler);
            verify(crawlScheduleQueryPort).findByCriteria(criteria);
        }
    }

    @Nested
    @DisplayName("countByCriteria() 테스트")
    class CountByCriteria {

        @Test
        @DisplayName("[성공] 조건으로 CrawlScheduler 개수 조회")
        void shouldDelegateToQueryPort() {
            // Given
            given(crawlScheduleQueryPort.countByCriteria(criteria)).willReturn(15L);

            // When
            long result = manager.countByCriteria(criteria);

            // Then
            assertThat(result).isEqualTo(15L);
            verify(crawlScheduleQueryPort).countByCriteria(criteria);
        }
    }

    @Nested
    @DisplayName("findActiveSchedulersBySellerId() 테스트")
    class FindActiveSchedulersBySellerId {

        @Test
        @DisplayName("[성공] Seller ID로 활성 스케줄러 목록 조회")
        void shouldDelegateToQueryPort() {
            // Given
            SellerId sellerId = SellerId.of(1L);
            given(crawlScheduleQueryPort.findActiveSchedulersBySellerId(sellerId))
                    .willReturn(List.of(crawlScheduler));

            // When
            List<CrawlScheduler> result = manager.findActiveSchedulersBySellerId(sellerId);

            // Then
            assertThat(result).hasSize(1).contains(crawlScheduler);
            verify(crawlScheduleQueryPort).findActiveSchedulersBySellerId(sellerId);
        }
    }
}
