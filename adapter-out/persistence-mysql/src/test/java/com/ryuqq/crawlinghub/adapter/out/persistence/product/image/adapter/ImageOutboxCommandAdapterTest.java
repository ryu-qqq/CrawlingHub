package com.ryuqq.crawlinghub.adapter.out.persistence.product.image.adapter;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.cralwinghub.domain.fixture.product.CrawledProductImageOutboxFixture;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.image.entity.ProductImageOutboxJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.image.mapper.ProductImageOutboxJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.image.repository.ProductImageOutboxJpaRepository;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductImageOutbox;
import com.ryuqq.crawlinghub.domain.product.vo.ImageType;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * ImageOutboxCommandAdapter 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("persistence")
@Tag("adapter")
@DisplayName("ImageOutboxCommandAdapter 단위 테스트")
@ExtendWith(MockitoExtension.class)
class ImageOutboxCommandAdapterTest {

    @Mock private ProductImageOutboxJpaRepository jpaRepository;

    @Mock private ProductImageOutboxJpaEntityMapper mapper;

    private ImageOutboxCommandAdapter commandAdapter;

    @BeforeEach
    void setUp() {
        commandAdapter = new ImageOutboxCommandAdapter(jpaRepository, mapper);
    }

    @Test
    @DisplayName("성공 - CrawledProductImageOutbox 저장")
    void shouldPersistOutbox() {
        // Given
        CrawledProductImageOutbox outbox = CrawledProductImageOutboxFixture.aPendingOutbox();
        LocalDateTime now = LocalDateTime.now();
        ProductImageOutboxJpaEntity entity =
                ProductImageOutboxJpaEntity.of(
                        null,
                        1L,
                        ImageType.THUMBNAIL,
                        "https://example.com/image.jpg",
                        "img-1-12345-abc123",
                        null,
                        ProductOutboxStatus.PENDING,
                        0,
                        null,
                        now,
                        null);

        given(mapper.toEntity(outbox)).willReturn(entity);
        given(jpaRepository.save(entity)).willReturn(entity);

        // When
        commandAdapter.persist(outbox);

        // Then
        verify(mapper).toEntity(outbox);
        verify(jpaRepository).save(entity);
    }

    @Test
    @DisplayName("성공 - CrawledProductImageOutbox 일괄 저장")
    void shouldPersistAllOutboxes() {
        // Given
        CrawledProductImageOutbox outbox1 = CrawledProductImageOutboxFixture.aPendingOutbox();
        CrawledProductImageOutbox outbox2 =
                CrawledProductImageOutboxFixture.aDescriptionPendingOutbox();
        List<CrawledProductImageOutbox> outboxes = List.of(outbox1, outbox2);

        LocalDateTime now = LocalDateTime.now();
        ProductImageOutboxJpaEntity entity1 =
                ProductImageOutboxJpaEntity.of(
                        null,
                        1L,
                        ImageType.THUMBNAIL,
                        "https://example.com/image.jpg",
                        "img-1-12345-abc123",
                        null,
                        ProductOutboxStatus.PENDING,
                        0,
                        null,
                        now,
                        null);
        ProductImageOutboxJpaEntity entity2 =
                ProductImageOutboxJpaEntity.of(
                        null,
                        1L,
                        ImageType.DESCRIPTION,
                        "https://example.com/detail-image.jpg",
                        "img-1-67890-def456",
                        null,
                        ProductOutboxStatus.PENDING,
                        0,
                        null,
                        now,
                        null);

        given(mapper.toEntity(outbox1)).willReturn(entity1);
        given(mapper.toEntity(outbox2)).willReturn(entity2);

        // When
        commandAdapter.persistAll(outboxes);

        // Then
        verify(jpaRepository).saveAll(List.of(entity1, entity2));
    }

    @Test
    @DisplayName("성공 - 기존 CrawledProductImageOutbox 업데이트")
    void shouldUpdateExistingOutbox() {
        // Given
        CrawledProductImageOutbox outbox =
                CrawledProductImageOutboxFixture.aReconstitutedProcessing();
        LocalDateTime now = LocalDateTime.now();
        ProductImageOutboxJpaEntity entity =
                ProductImageOutboxJpaEntity.of(
                        1L,
                        1L,
                        ImageType.THUMBNAIL,
                        "https://example.com/image.jpg",
                        "img-1-12345-abc123",
                        null,
                        ProductOutboxStatus.PROCESSING,
                        0,
                        null,
                        now,
                        now);

        given(mapper.toEntity(outbox)).willReturn(entity);
        given(jpaRepository.save(entity)).willReturn(entity);

        // When
        commandAdapter.update(outbox);

        // Then
        verify(mapper).toEntity(outbox);
        verify(jpaRepository).save(entity);
    }
}
