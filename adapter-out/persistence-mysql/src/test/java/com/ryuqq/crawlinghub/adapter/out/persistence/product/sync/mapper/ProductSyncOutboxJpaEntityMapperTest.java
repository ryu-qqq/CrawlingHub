package com.ryuqq.crawlinghub.adapter.out.persistence.product.sync.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.cralwinghub.domain.fixture.product.CrawledProductSyncOutboxFixture;
import com.ryuqq.crawlinghub.adapter.out.persistence.sync.entity.ProductSyncOutboxJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.sync.mapper.ProductSyncOutboxJpaEntityMapper;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox.SyncType;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * ProductSyncOutboxJpaEntityMapper 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("persistence")
@Tag("mapper")
@DisplayName("ProductSyncOutboxJpaEntityMapper 단위 테스트")
class ProductSyncOutboxJpaEntityMapperTest {

    private ProductSyncOutboxJpaEntityMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ProductSyncOutboxJpaEntityMapper();
    }

    @Test
    @DisplayName("성공 - Domain -> Entity 변환 (PENDING 상태)")
    void shouldConvertDomainToEntityWithPendingStatus() {
        // Given
        CrawledProductSyncOutbox domain = CrawledProductSyncOutboxFixture.aReconstitutedPending();

        // When
        ProductSyncOutboxJpaEntity entity = mapper.toEntity(domain);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(domain.getId());
        assertThat(entity.getCrawledProductId()).isEqualTo(domain.getCrawledProductIdValue());
        assertThat(entity.getSellerId()).isEqualTo(domain.getSellerIdValue());
        assertThat(entity.getItemNo()).isEqualTo(domain.getItemNo());
        assertThat(entity.getSyncType()).isEqualTo(domain.getSyncType());
        assertThat(entity.getIdempotencyKey()).isEqualTo(domain.getIdempotencyKey());
        assertThat(entity.getStatus()).isEqualTo(ProductOutboxStatus.PENDING);
        assertThat(entity.getRetryCount()).isEqualTo(0);
        assertThat(entity.getErrorMessage()).isNull();
    }

    @Test
    @DisplayName("성공 - Domain -> Entity 변환 (COMPLETED 상태)")
    void shouldConvertDomainToEntityWithCompletedStatus() {
        // Given
        CrawledProductSyncOutbox domain = CrawledProductSyncOutboxFixture.aReconstitutedCompleted();

        // When
        ProductSyncOutboxJpaEntity entity = mapper.toEntity(domain);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getStatus()).isEqualTo(ProductOutboxStatus.COMPLETED);
        assertThat(entity.getExternalProductId()).isEqualTo(domain.getExternalProductId());
    }

    @Test
    @DisplayName("성공 - Domain -> Entity 변환 (FAILED 상태)")
    void shouldConvertDomainToEntityWithFailedStatus() {
        // Given
        CrawledProductSyncOutbox domain = CrawledProductSyncOutboxFixture.aReconstitutedFailed();

        // When
        ProductSyncOutboxJpaEntity entity = mapper.toEntity(domain);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getStatus()).isEqualTo(ProductOutboxStatus.FAILED);
        assertThat(entity.getRetryCount()).isEqualTo(1);
        assertThat(entity.getErrorMessage()).isEqualTo("Connection timeout");
    }

    @Test
    @DisplayName("성공 - Entity -> Domain 변환 (PENDING 상태)")
    void shouldConvertEntityToDomainWithPendingStatus() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        ProductSyncOutboxJpaEntity entity =
                ProductSyncOutboxJpaEntity.of(
                        1L,
                        1L,
                        100L,
                        12345L,
                        SyncType.CREATE,
                        "sync-key-123",
                        null,
                        ProductOutboxStatus.PENDING,
                        0,
                        null,
                        now,
                        null);

        // When
        CrawledProductSyncOutbox domain = mapper.toDomain(entity);

        // Then
        assertThat(domain).isNotNull();
        assertThat(domain.getId()).isEqualTo(1L);
        assertThat(domain.getCrawledProductId()).isEqualTo(CrawledProductId.of(1L));
        assertThat(domain.getSellerIdValue()).isEqualTo(100L);
        assertThat(domain.getItemNo()).isEqualTo(12345L);
        assertThat(domain.getSyncType()).isEqualTo(SyncType.CREATE);
        assertThat(domain.getIdempotencyKey()).isEqualTo("sync-key-123");
        assertThat(domain.getStatus()).isEqualTo(ProductOutboxStatus.PENDING);
        assertThat(domain.isPending()).isTrue();
    }

    @Test
    @DisplayName("성공 - Entity -> Domain 변환 (COMPLETED 상태)")
    void shouldConvertEntityToDomainWithCompletedStatus() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        ProductSyncOutboxJpaEntity entity =
                ProductSyncOutboxJpaEntity.of(
                        1L,
                        1L,
                        100L,
                        12345L,
                        SyncType.UPDATE,
                        "sync-key-123",
                        99999L,
                        ProductOutboxStatus.COMPLETED,
                        0,
                        null,
                        now,
                        now);

        // When
        CrawledProductSyncOutbox domain = mapper.toDomain(entity);

        // Then
        assertThat(domain).isNotNull();
        assertThat(domain.getStatus()).isEqualTo(ProductOutboxStatus.COMPLETED);
        assertThat(domain.getExternalProductId()).isEqualTo(99999L);
        assertThat(domain.getSyncType()).isEqualTo(SyncType.UPDATE);
        assertThat(domain.isCompleted()).isTrue();
    }

    @Test
    @DisplayName("성공 - Entity -> Domain 변환 (FAILED 상태)")
    void shouldConvertEntityToDomainWithFailedStatus() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        ProductSyncOutboxJpaEntity entity =
                ProductSyncOutboxJpaEntity.of(
                        1L,
                        1L,
                        100L,
                        12345L,
                        SyncType.CREATE,
                        "sync-key-123",
                        null,
                        ProductOutboxStatus.FAILED,
                        2,
                        "Connection timeout",
                        now,
                        now);

        // When
        CrawledProductSyncOutbox domain = mapper.toDomain(entity);

        // Then
        assertThat(domain).isNotNull();
        assertThat(domain.getStatus()).isEqualTo(ProductOutboxStatus.FAILED);
        assertThat(domain.getRetryCount()).isEqualTo(2);
        assertThat(domain.getErrorMessage()).isEqualTo("Connection timeout");
        assertThat(domain.canRetry()).isTrue();
    }
}
