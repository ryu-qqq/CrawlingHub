package com.ryuqq.crawlinghub.application.seller.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.cralwinghub.domain.fixture.seller.SellerFixture;
import com.ryuqq.crawlinghub.application.schedule.manager.query.CrawlSchedulerReadManager;
import com.ryuqq.crawlinghub.application.seller.assembler.SellerAssembler;
import com.ryuqq.crawlinghub.application.seller.dto.query.GetSellerQuery;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerDetailResponse;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerDetailStatistics;
import com.ryuqq.crawlinghub.application.seller.manager.query.SellerReadManager;
import com.ryuqq.crawlinghub.application.task.manager.query.CrawlTaskReadManager;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.exception.SellerNotFoundException;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import java.time.Instant;
import java.util.Collections;
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
 * GetSellerService 단위 테스트
 *
 * <p>Mockist 스타일 테스트: ReadManager 의존성 Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetSellerService 테스트")
class GetSellerServiceTest {

    @Mock private SellerReadManager sellerReadManager;

    @Mock private CrawlSchedulerReadManager crawlSchedulerReadManager;

    @Mock private CrawlTaskReadManager crawlTaskReadManager;

    @Mock private SellerAssembler sellerAssembler;

    @InjectMocks private GetSellerService service;

    @Nested
    @DisplayName("execute() 셀러 조회 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] 존재하는 셀러 조회 시 SellerDetailResponse 반환")
        void shouldReturnSellerDetailResponseWhenSellerExists() {
            // Given
            Long sellerId = 1L;
            GetSellerQuery query = new GetSellerQuery(sellerId);
            Seller seller = SellerFixture.anActiveSeller();
            Instant now = Instant.now();
            List<CrawlScheduler> schedulers = Collections.emptyList();
            List<CrawlTask> recentTasks = Collections.emptyList();
            SellerDetailResponse expectedResponse =
                    new SellerDetailResponse(
                            sellerId,
                            "mustit-seller",
                            "seller-name",
                            true,
                            now,
                            now,
                            Collections.emptyList(),
                            Collections.emptyList(),
                            SellerDetailStatistics.empty());

            given(sellerReadManager.findById(any(SellerId.class))).willReturn(Optional.of(seller));
            given(crawlSchedulerReadManager.findBySellerId(any(SellerId.class)))
                    .willReturn(schedulers);
            given(
                            crawlTaskReadManager.findRecentBySellerId(
                                    any(SellerId.class), any(Integer.class)))
                    .willReturn(recentTasks);
            given(sellerAssembler.toDetailResponse(seller, schedulers, recentTasks))
                    .willReturn(expectedResponse);

            // When
            SellerDetailResponse result = service.execute(query);

            // Then
            assertThat(result).isEqualTo(expectedResponse);
            then(sellerReadManager).should().findById(SellerId.of(sellerId));
            then(crawlSchedulerReadManager).should().findBySellerId(any(SellerId.class));
            then(crawlTaskReadManager)
                    .should()
                    .findRecentBySellerId(any(SellerId.class), any(Integer.class));
            then(sellerAssembler).should().toDetailResponse(seller, schedulers, recentTasks);
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 셀러 조회 시 SellerNotFoundException 발생")
        void shouldThrowExceptionWhenSellerNotFound() {
            // Given
            Long sellerId = 999L;
            GetSellerQuery query = new GetSellerQuery(sellerId);

            given(sellerReadManager.findById(any(SellerId.class))).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> service.execute(query))
                    .isInstanceOf(SellerNotFoundException.class);

            then(sellerReadManager).should().findById(SellerId.of(sellerId));
            then(sellerAssembler).shouldHaveNoInteractions();
        }
    }
}
