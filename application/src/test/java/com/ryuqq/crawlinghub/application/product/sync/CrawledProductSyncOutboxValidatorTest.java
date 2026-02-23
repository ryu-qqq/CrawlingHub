package com.ryuqq.crawlinghub.application.product.sync;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.ryuqq.crawlinghub.application.product.manager.CrawledProductSyncOutboxReadManager;
import com.ryuqq.crawlinghub.application.product.validator.CrawledProductSyncOutboxValidator;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox.SyncType;
import com.ryuqq.crawlinghub.domain.product.id.CrawledProductId;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawledProductSyncOutboxValidator 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawledProductSyncOutboxValidator 테스트")
class CrawledProductSyncOutboxValidatorTest {

    private static final CrawledProductId PRODUCT_ID = CrawledProductId.of(1L);

    @Mock private CrawledProductSyncOutboxReadManager syncOutboxReadManager;

    private CrawledProductSyncOutboxValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CrawledProductSyncOutboxValidator(syncOutboxReadManager);
    }

    @Test
    @DisplayName("[결과] 활성 Outbox가 있는 SyncType만 반환")
    void shouldReturnOnlyActiveSyncTypes() {
        // Given
        List<SyncType> syncTypes = List.of(SyncType.CREATE, SyncType.UPDATE_PRICE);

        given(syncOutboxReadManager.existsActiveOutbox(PRODUCT_ID, SyncType.CREATE))
                .willReturn(true);
        given(syncOutboxReadManager.existsActiveOutbox(PRODUCT_ID, SyncType.UPDATE_PRICE))
                .willReturn(false);

        // When
        Set<SyncType> result = validator.filterAlreadyActive(PRODUCT_ID, syncTypes);

        // Then
        assertThat(result).containsExactly(SyncType.CREATE);
    }

    @Test
    @DisplayName("[결과] 활성 Outbox가 없으면 빈 Set 반환")
    void shouldReturnEmptySetWhenNoActiveSyncTypes() {
        // Given
        List<SyncType> syncTypes = List.of(SyncType.CREATE, SyncType.UPDATE_PRICE);

        given(syncOutboxReadManager.existsActiveOutbox(PRODUCT_ID, SyncType.CREATE))
                .willReturn(false);
        given(syncOutboxReadManager.existsActiveOutbox(PRODUCT_ID, SyncType.UPDATE_PRICE))
                .willReturn(false);

        // When
        Set<SyncType> result = validator.filterAlreadyActive(PRODUCT_ID, syncTypes);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("[결과] 여러 SyncType 중 일부만 활성 → 활성인 것만 반환")
    void shouldReturnOnlyActiveFromMultipleSyncTypes() {
        // Given
        List<SyncType> syncTypes =
                List.of(SyncType.CREATE, SyncType.UPDATE_PRICE, SyncType.UPDATE_IMAGE);

        given(syncOutboxReadManager.existsActiveOutbox(PRODUCT_ID, SyncType.CREATE))
                .willReturn(true);
        given(syncOutboxReadManager.existsActiveOutbox(PRODUCT_ID, SyncType.UPDATE_PRICE))
                .willReturn(false);
        given(syncOutboxReadManager.existsActiveOutbox(PRODUCT_ID, SyncType.UPDATE_IMAGE))
                .willReturn(true);

        // When
        Set<SyncType> result = validator.filterAlreadyActive(PRODUCT_ID, syncTypes);

        // Then
        assertThat(result).containsExactlyInAnyOrder(SyncType.CREATE, SyncType.UPDATE_IMAGE);
    }
}
