package com.ryuqq.crawlinghub.application.product.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.ryuqq.crawlinghub.application.schedule.manager.CrawlSchedulerReadManager;
import com.ryuqq.crawlinghub.application.seller.manager.SellerReadManager;
import com.ryuqq.crawlinghub.application.task.dto.command.CreateCrawlTaskCommand;
import com.ryuqq.crawlinghub.domain.common.vo.DeletionStatus;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.id.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.CrawlCompletionStatus;
import com.ryuqq.crawlinghub.domain.product.vo.ProductChangeType;
import com.ryuqq.crawlinghub.domain.product.vo.ProductImages;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOptions;
import com.ryuqq.crawlinghub.domain.product.vo.ProductPrice;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.schedule.vo.CronExpression;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerName;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import com.ryuqq.crawlinghub.domain.seller.vo.MustItSellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import java.time.Instant;
import java.util.EnumSet;
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
 * CrawledProductRefreshCoordinator 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawledProductRefreshCoordinator 테스트")
class CrawledProductRefreshCoordinatorTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2025-01-01T00:00:00Z");
    private static final SellerId SELLER_ID = SellerId.of(100L);

    @Mock private CrawlSchedulerReadManager crawlSchedulerReadManager;
    @Mock private SellerReadManager sellerReadManager;

    private CrawledProductRefreshCoordinator coordinator;

    @BeforeEach
    void setUp() {
        coordinator =
                new CrawledProductRefreshCoordinator(crawlSchedulerReadManager, sellerReadManager);
    }

    @Nested
    @DisplayName("buildRefreshCommands")
    class BuildRefreshCommands {

        @Test
        @DisplayName("[성공] 상품 2개 → DETAIL + OPTION 커맨드 4개 생성")
        void shouldBuildDetailAndOptionCommandsForEachProduct() {
            // Given
            CrawlScheduler scheduler = createTestScheduler(10L, SELLER_ID);
            Seller seller = createTestSeller(SELLER_ID, "testShop");
            List<CrawledProduct> products =
                    List.of(
                            createTestProduct(1L, 100L, 10001L),
                            createTestProduct(2L, 100L, 10002L));

            given(crawlSchedulerReadManager.findActiveSchedulersBySellerId(SELLER_ID))
                    .willReturn(List.of(scheduler));
            given(sellerReadManager.findById(SELLER_ID)).willReturn(Optional.of(seller));

            // When
            List<CreateCrawlTaskCommand> commands =
                    coordinator.buildRefreshCommands(SELLER_ID, products);

            // Then
            assertThat(commands).hasSize(4);
            assertThat(commands.get(0).taskType()).isEqualTo(CrawlTaskType.DETAIL);
            assertThat(commands.get(0).targetId()).isEqualTo(10001L);
            assertThat(commands.get(0).crawlSchedulerId()).isEqualTo(10L);
            assertThat(commands.get(0).mustItSellerName()).isEqualTo("testShop");
            assertThat(commands.get(1).taskType()).isEqualTo(CrawlTaskType.OPTION);
            assertThat(commands.get(1).targetId()).isEqualTo(10001L);
            assertThat(commands.get(2).taskType()).isEqualTo(CrawlTaskType.DETAIL);
            assertThat(commands.get(2).targetId()).isEqualTo(10002L);
            assertThat(commands.get(3).taskType()).isEqualTo(CrawlTaskType.OPTION);
            assertThat(commands.get(3).targetId()).isEqualTo(10002L);
        }

        @Test
        @DisplayName("[성공] 활성 스케줄러 없음 → 빈 리스트 반환")
        void shouldReturnEmptyWhenNoActiveSchedulers() {
            // Given
            given(crawlSchedulerReadManager.findActiveSchedulersBySellerId(SELLER_ID))
                    .willReturn(List.of());

            // When
            List<CreateCrawlTaskCommand> commands =
                    coordinator.buildRefreshCommands(
                            SELLER_ID, List.of(createTestProduct(1L, 100L, 10001L)));

            // Then
            assertThat(commands).isEmpty();
        }

        @Test
        @DisplayName("[성공] 셀러 조회 실패 → 빈 리스트 반환")
        void shouldReturnEmptyWhenSellerNotFound() {
            // Given
            CrawlScheduler scheduler = createTestScheduler(10L, SELLER_ID);
            given(crawlSchedulerReadManager.findActiveSchedulersBySellerId(SELLER_ID))
                    .willReturn(List.of(scheduler));
            given(sellerReadManager.findById(SELLER_ID)).willReturn(Optional.empty());

            // When
            List<CreateCrawlTaskCommand> commands =
                    coordinator.buildRefreshCommands(
                            SELLER_ID, List.of(createTestProduct(1L, 100L, 10001L)));

            // Then
            assertThat(commands).isEmpty();
        }

        @Test
        @DisplayName("[성공] 상품 1개 → DETAIL + OPTION 커맨드 2개 생성")
        void shouldBuildTwoCommandsForSingleProduct() {
            // Given
            CrawlScheduler scheduler = createTestScheduler(10L, SELLER_ID);
            Seller seller = createTestSeller(SELLER_ID, "testShop");

            given(crawlSchedulerReadManager.findActiveSchedulersBySellerId(SELLER_ID))
                    .willReturn(List.of(scheduler));
            given(sellerReadManager.findById(SELLER_ID)).willReturn(Optional.of(seller));

            // When
            List<CreateCrawlTaskCommand> commands =
                    coordinator.buildRefreshCommands(
                            SELLER_ID, List.of(createTestProduct(1L, 100L, 10001L)));

            // Then
            assertThat(commands).hasSize(2);
            assertThat(commands.get(0).sellerId()).isEqualTo(100L);
            assertThat(commands.get(1).sellerId()).isEqualTo(100L);
        }
    }

    private CrawledProduct createTestProduct(long id, long sellerId, long itemNo) {
        CrawlCompletionStatus status =
                CrawlCompletionStatus.initial()
                        .withMiniShopCrawled(FIXED_INSTANT)
                        .withDetailCrawled(FIXED_INSTANT)
                        .withOptionCrawled(FIXED_INSTANT);

        return CrawledProduct.reconstitute(
                CrawledProductId.of(id),
                SellerId.of(sellerId),
                itemNo,
                "Test Product",
                "Test Brand",
                ProductPrice.of(10000, 12000, 12000, 9000, 10, 10),
                ProductImages.empty(),
                true,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                ProductOptions.empty(),
                status,
                itemNo,
                null,
                false,
                EnumSet.noneOf(ProductChangeType.class),
                DeletionStatus.active(),
                FIXED_INSTANT,
                FIXED_INSTANT,
                null);
    }

    private CrawlScheduler createTestScheduler(long schedulerId, SellerId sellerId) {
        return CrawlScheduler.reconstitute(
                CrawlSchedulerId.of(schedulerId),
                sellerId,
                SchedulerName.of("test-scheduler"),
                CronExpression.of("cron(0 0 * * ? *)"),
                SchedulerStatus.ACTIVE,
                FIXED_INSTANT,
                FIXED_INSTANT);
    }

    private Seller createTestSeller(SellerId sellerId, String mustItSellerName) {
        return Seller.reconstitute(
                sellerId,
                MustItSellerName.of(mustItSellerName),
                SellerName.of("Seller-" + sellerId.value()),
                0L,
                SellerStatus.ACTIVE,
                0,
                FIXED_INSTANT,
                FIXED_INSTANT);
    }
}
