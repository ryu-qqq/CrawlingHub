package com.ryuqq.crawlinghub.adapter.out.persistence.image.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.cralwinghub.domain.fixture.product.CrawledProductImageFixture;
import com.ryuqq.crawlinghub.adapter.out.persistence.image.entity.CrawledProductImageJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.image.mapper.CrawledProductImageJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.image.repository.CrawledProductImageJpaRepository;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductImage;
import com.ryuqq.crawlinghub.domain.product.vo.ImageType;
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
 * CrawledProductImageCommandAdapter 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("persistence")
@Tag("adapter")
@DisplayName("CrawledProductImageCommandAdapter 단위 테스트")
@ExtendWith(MockitoExtension.class)
class CrawledProductImageCommandAdapterTest {

    @Mock private CrawledProductImageJpaRepository jpaRepository;

    @Mock private CrawledProductImageJpaEntityMapper mapper;

    private CrawledProductImageCommandAdapter commandAdapter;

    @BeforeEach
    void setUp() {
        commandAdapter = new CrawledProductImageCommandAdapter(jpaRepository, mapper);
    }

    @Test
    @DisplayName("성공 - CrawledProductImage 단건 저장 (upsert)")
    void shouldPersistImage() {
        // Given
        CrawledProductImage image = CrawledProductImageFixture.aReconstitutedThumbnailPending();
        LocalDateTime now = LocalDateTime.now();
        CrawledProductImageJpaEntity entity =
                CrawledProductImageJpaEntity.of(
                        null,
                        1L,
                        "https://example.com/thumbnail.jpg",
                        null,
                        null,
                        ImageType.THUMBNAIL,
                        0,
                        now,
                        null);
        CrawledProductImageJpaEntity savedEntity =
                CrawledProductImageJpaEntity.of(
                        1L,
                        1L,
                        "https://example.com/thumbnail.jpg",
                        null,
                        null,
                        ImageType.THUMBNAIL,
                        0,
                        now,
                        null);

        given(mapper.toEntity(image)).willReturn(entity);
        given(jpaRepository.save(entity)).willReturn(savedEntity);
        given(mapper.toDomain(savedEntity)).willReturn(image);

        // When
        CrawledProductImage result = commandAdapter.persist(image);

        // Then
        assertThat(result).isNotNull();
        verify(mapper).toEntity(image);
        verify(jpaRepository).save(entity);
        verify(mapper).toDomain(savedEntity);
    }

    @Test
    @DisplayName("성공 - CrawledProductImage 일괄 저장 (upsert)")
    void shouldPersistAllImages() {
        // Given
        CrawledProductImage image1 =
                CrawledProductImageFixture.aReconstitutedPending(
                        1L, 1L, "https://example.com/thumb1.jpg", ImageType.THUMBNAIL, 0);
        CrawledProductImage image2 =
                CrawledProductImageFixture.aReconstitutedPending(
                        2L, 1L, "https://example.com/thumb2.jpg", ImageType.THUMBNAIL, 1);
        List<CrawledProductImage> images = List.of(image1, image2);

        LocalDateTime now = LocalDateTime.now();
        CrawledProductImageJpaEntity entity1 =
                CrawledProductImageJpaEntity.of(
                        1L,
                        1L,
                        "https://example.com/thumb1.jpg",
                        null,
                        null,
                        ImageType.THUMBNAIL,
                        0,
                        now,
                        null);
        CrawledProductImageJpaEntity entity2 =
                CrawledProductImageJpaEntity.of(
                        2L,
                        1L,
                        "https://example.com/thumb2.jpg",
                        null,
                        null,
                        ImageType.THUMBNAIL,
                        1,
                        now,
                        null);

        given(mapper.toEntity(image1)).willReturn(entity1);
        given(mapper.toEntity(image2)).willReturn(entity2);
        given(jpaRepository.saveAll(List.of(entity1, entity2)))
                .willReturn(List.of(entity1, entity2));
        given(mapper.toDomain(entity1)).willReturn(image1);
        given(mapper.toDomain(entity2)).willReturn(image2);

        // When
        List<CrawledProductImage> results = commandAdapter.persistAll(images);

        // Then
        assertThat(results).hasSize(2);
        verify(jpaRepository).saveAll(List.of(entity1, entity2));
    }
}
