package com.ryuqq.crawlinghub.application.product.sync;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.ryuqq.cralwinghub.domain.fixture.product.CrawledProductSyncOutboxFixture;
import com.ryuqq.crawlinghub.application.product.factory.CrawledProductSyncOutboxFactory;
import com.ryuqq.crawlinghub.application.product.internal.CrawledProductSyncOutboxCoordinator;
import com.ryuqq.crawlinghub.application.product.manager.CrawledProductSyncOutboxCommandManager;
import com.ryuqq.crawlinghub.application.product.validator.CrawledProductSyncOutboxValidator;
import com.ryuqq.crawlinghub.domain.common.vo.DeletionStatus;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox.SyncType;
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
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawledProductSyncOutboxCoordinator 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawledProductSyncOutboxCoordinator 테스트")
class CrawledProductSyncOutboxCoordinatorTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2025-01-01T00:00:00Z");

    @Mock private CrawledProductSyncOutboxValidator validator;
    @Mock private CrawledProductSyncOutboxFactory syncOutboxFactory;
    @Mock private CrawledProductSyncOutboxCommandManager syncOutboxCommandManager;

    private CrawledProductSyncOutboxCoordinator coordinator;

    @BeforeEach
    void setUp() {
        coordinator =
                new CrawledProductSyncOutboxCoordinator(
                        validator, syncOutboxFactory, syncOutboxCommandManager);
    }

    @Test
    @DisplayName("[성공] 활성 Outbox 없음 → 모든 후보 생성 및 영속화")
    void shouldCreateAndPersistAllOutboxesWhenNoActiveExists() {
        // Given
        CrawledProduct product = createProduct();
        CrawledProductSyncOutbox outbox = CrawledProductSyncOutboxFixture.aPendingForCreate();
        List<CrawledProductSyncOutbox> candidates = List.of(outbox);

        given(syncOutboxFactory.createAll(product)).willReturn(candidates);
        given(validator.filterAlreadyActive(product.getId(), List.of(SyncType.CREATE)))
                .willReturn(Set.of());

        // When
        List<CrawledProductSyncOutbox> result = coordinator.createAllIfAbsent(product);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(outbox);
        then(syncOutboxCommandManager).should().persist(outbox);
    }

    @Test
    @DisplayName("[스킵] 모든 후보에 활성 Outbox 존재 → 빈 목록 반환, 영속화 없음")
    void shouldReturnEmptyListWhenAllCandidatesAlreadyActive() {
        // Given
        CrawledProduct product = createProduct();
        CrawledProductSyncOutbox outbox = CrawledProductSyncOutboxFixture.aPendingForCreate();
        List<CrawledProductSyncOutbox> candidates = List.of(outbox);

        given(syncOutboxFactory.createAll(product)).willReturn(candidates);
        given(validator.filterAlreadyActive(product.getId(), List.of(SyncType.CREATE)))
                .willReturn(Set.of(SyncType.CREATE));

        // When
        List<CrawledProductSyncOutbox> result = coordinator.createAllIfAbsent(product);

        // Then
        assertThat(result).isEmpty();
        then(syncOutboxCommandManager).should(never()).persist(any(CrawledProductSyncOutbox.class));
    }

    private CrawledProduct createProduct() {
        CrawlCompletionStatus status =
                CrawlCompletionStatus.initial()
                        .withMiniShopCrawled(FIXED_INSTANT)
                        .withDetailCrawled(FIXED_INSTANT)
                        .withOptionCrawled(FIXED_INSTANT);

        return CrawledProduct.reconstitute(
                CrawledProductId.of(1L),
                SellerId.of(100L),
                10001L,
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
                null,
                null,
                true,
                EnumSet.noneOf(ProductChangeType.class),
                DeletionStatus.active(),
                FIXED_INSTANT,
                FIXED_INSTANT,
                null);
    }
}
