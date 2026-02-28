package com.ryuqq.crawlinghub.application.product.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.ryuqq.crawlinghub.application.execution.internal.FollowUpTaskCreator;
import com.ryuqq.crawlinghub.application.product.internal.CrawledProductRefreshCoordinator;
import com.ryuqq.crawlinghub.application.product.port.out.query.CrawledProductQueryPort;
import com.ryuqq.crawlinghub.application.task.dto.command.CreateCrawlTaskCommand;
import com.ryuqq.crawlinghub.domain.common.vo.DeletionStatus;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.id.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.CrawlCompletionStatus;
import com.ryuqq.crawlinghub.domain.product.vo.ProductChangeType;
import com.ryuqq.crawlinghub.domain.product.vo.ProductImages;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOptions;
import com.ryuqq.crawlinghub.domain.product.vo.ProductPrice;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * RefreshStaleCrawledProductsService 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshStaleCrawledProductsService 테스트")
class RefreshStaleCrawledProductsServiceTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2025-01-01T00:00:00Z");

    @Mock private CrawledProductQueryPort crawledProductQueryPort;
    @Mock private CrawledProductRefreshCoordinator refreshCoordinator;
    @Mock private FollowUpTaskCreator followUpTaskCreator;

    private RefreshStaleCrawledProductsService service;

    @BeforeEach
    void setUp() {
        service =
                new RefreshStaleCrawledProductsService(
                        crawledProductQueryPort, refreshCoordinator, followUpTaskCreator);
    }

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("[성공] stale 상품 없음 → 0 반환, 태스크 생성 없음")
        void shouldReturnZeroWhenNoStaleProducts() {
            // Given
            given(crawledProductQueryPort.findStaleProducts(3000)).willReturn(List.of());

            // When
            int result = service.execute(3000);

            // Then
            assertThat(result).isZero();
            then(refreshCoordinator).shouldHaveNoInteractions();
            then(followUpTaskCreator).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("[성공] 단일 셀러 상품 2개 → coordinator 위임 후 executeBatch 호출")
        void shouldDelegateToCoordinatorAndExecuteBatch() {
            // Given
            CrawledProduct product1 = createTestProduct(1L, 100L, 10001L);
            CrawledProduct product2 = createTestProduct(2L, 100L, 10002L);
            given(crawledProductQueryPort.findStaleProducts(3000))
                    .willReturn(List.of(product1, product2));

            List<CreateCrawlTaskCommand> commands =
                    List.of(
                            CreateCrawlTaskCommand.forDetail(10L, 100L, "testShop", 10001L),
                            CreateCrawlTaskCommand.forOption(10L, 100L, "testShop", 10001L),
                            CreateCrawlTaskCommand.forDetail(10L, 100L, "testShop", 10002L),
                            CreateCrawlTaskCommand.forOption(10L, 100L, "testShop", 10002L));

            given(refreshCoordinator.buildRefreshCommands(any(SellerId.class), anyList()))
                    .willReturn(commands);

            // When
            int result = service.execute(3000);

            // Then
            assertThat(result).isEqualTo(4);

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<CreateCrawlTaskCommand>> captor =
                    ArgumentCaptor.forClass(List.class);
            then(followUpTaskCreator).should().executeBatch(captor.capture());
            assertThat(captor.getValue()).hasSize(4);
        }

        @Test
        @DisplayName("[성공] 복수 셀러 상품 → sellerId별로 그룹핑하여 coordinator 호출")
        void shouldGroupBySellerIdAndCallCoordinator() {
            // Given
            CrawledProduct product1 = createTestProduct(1L, 100L, 10001L);
            CrawledProduct product2 = createTestProduct(2L, 200L, 20001L);
            given(crawledProductQueryPort.findStaleProducts(3000))
                    .willReturn(List.of(product1, product2));

            // seller 100 → 2 commands, seller 200 → 2 commands
            given(refreshCoordinator.buildRefreshCommands(SellerId.of(100L), List.of(product1)))
                    .willReturn(
                            List.of(
                                    CreateCrawlTaskCommand.forDetail(10L, 100L, "shop1", 10001L),
                                    CreateCrawlTaskCommand.forOption(10L, 100L, "shop1", 10001L)));
            given(refreshCoordinator.buildRefreshCommands(SellerId.of(200L), List.of(product2)))
                    .willReturn(
                            List.of(
                                    CreateCrawlTaskCommand.forDetail(20L, 200L, "shop2", 20001L),
                                    CreateCrawlTaskCommand.forOption(20L, 200L, "shop2", 20001L)));

            // When
            int result = service.execute(3000);

            // Then
            assertThat(result).isEqualTo(4);
            then(followUpTaskCreator).should().executeBatch(any());
        }

        @Test
        @DisplayName("[성공] coordinator가 빈 리스트 반환 → executeBatch 미호출")
        void shouldNotCallExecuteBatchWhenNoCommands() {
            // Given
            CrawledProduct product = createTestProduct(1L, 100L, 10001L);
            given(crawledProductQueryPort.findStaleProducts(3000)).willReturn(List.of(product));
            given(refreshCoordinator.buildRefreshCommands(any(SellerId.class), anyList()))
                    .willReturn(List.of());

            // When
            int result = service.execute(3000);

            // Then
            assertThat(result).isZero();
            then(followUpTaskCreator).should(never()).executeBatch(any());
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
}
