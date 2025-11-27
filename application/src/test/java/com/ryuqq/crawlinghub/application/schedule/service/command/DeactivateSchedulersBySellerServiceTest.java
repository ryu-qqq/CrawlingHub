package com.ryuqq.crawlinghub.application.schedule.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerFixture;
import com.ryuqq.crawlinghub.application.schedule.facade.CrawlerSchedulerFacade;
import com.ryuqq.crawlinghub.application.schedule.port.out.query.CrawlScheduleQueryPort;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * DeactivateSchedulersBySellerService 단위 테스트
 *
 * <p>Mockist 스타일 테스트: Port 의존성 Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DeactivateSchedulersBySellerService 테스트")
class DeactivateSchedulersBySellerServiceTest {

    @Mock private CrawlScheduleQueryPort crawlScheduleQueryPort;

    @Mock private CrawlerSchedulerFacade crawlerSchedulerFacade;

    @InjectMocks private DeactivateSchedulersBySellerService service;

    @Nested
    @DisplayName("execute() 셀러별 스케줄러 비활성화 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] 활성 스케줄러가 있는 경우 모두 비활성화")
        void shouldDeactivateAllActiveSchedulers() {
            // Given
            Long sellerId = 1L;
            CrawlScheduler scheduler1 = CrawlSchedulerFixture.anActiveScheduler(1L);
            CrawlScheduler scheduler2 = CrawlSchedulerFixture.anActiveScheduler(2L);
            List<CrawlScheduler> activeSchedulers = List.of(scheduler1, scheduler2);

            given(crawlScheduleQueryPort.findActiveSchedulersBySellerId(any(SellerId.class)))
                    .willReturn(activeSchedulers);

            // When
            int result = service.execute(sellerId);

            // Then
            assertThat(result).isEqualTo(2);
            then(crawlScheduleQueryPort)
                    .should()
                    .findActiveSchedulersBySellerId(SellerId.of(sellerId));
            then(crawlerSchedulerFacade).should(times(2)).update(any(CrawlScheduler.class));
        }

        @Test
        @DisplayName("[성공] 활성 스케줄러가 없는 경우 0 반환")
        void shouldReturnZeroWhenNoActiveSchedulers() {
            // Given
            Long sellerId = 1L;

            given(crawlScheduleQueryPort.findActiveSchedulersBySellerId(any(SellerId.class)))
                    .willReturn(Collections.emptyList());

            // When
            int result = service.execute(sellerId);

            // Then
            assertThat(result).isZero();
            then(crawlScheduleQueryPort)
                    .should()
                    .findActiveSchedulersBySellerId(SellerId.of(sellerId));
            then(crawlerSchedulerFacade).should(never()).update(any());
        }

        @Test
        @DisplayName("[성공] 단일 스케줄러 비활성화")
        void shouldDeactivateSingleScheduler() {
            // Given
            Long sellerId = 1L;
            CrawlScheduler scheduler = CrawlSchedulerFixture.anActiveScheduler();
            List<CrawlScheduler> activeSchedulers = List.of(scheduler);

            given(crawlScheduleQueryPort.findActiveSchedulersBySellerId(any(SellerId.class)))
                    .willReturn(activeSchedulers);

            // When
            int result = service.execute(sellerId);

            // Then
            assertThat(result).isEqualTo(1);
            then(crawlerSchedulerFacade).should().update(scheduler);
        }
    }
}
