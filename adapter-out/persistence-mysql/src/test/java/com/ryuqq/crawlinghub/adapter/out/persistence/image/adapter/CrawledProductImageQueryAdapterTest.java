package com.ryuqq.crawlinghub.adapter.out.persistence.image.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.cralwinghub.domain.fixture.product.CrawledProductImageFixture;
import com.ryuqq.crawlinghub.adapter.out.persistence.image.entity.CrawledProductImageJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.image.mapper.CrawledProductImageJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.image.repository.CrawledProductImageQueryDslRepository;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductImage;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.ImageType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawledProductImageQueryAdapter 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("persistence")
@Tag("adapter")
@DisplayName("CrawledProductImageQueryAdapter 단위 테스트")
@ExtendWith(MockitoExtension.class)
class CrawledProductImageQueryAdapterTest {

    @Mock private CrawledProductImageQueryDslRepository queryDslRepository;

    @Mock private CrawledProductImageJpaEntityMapper mapper;

    private CrawledProductImageQueryAdapter queryAdapter;

    @BeforeEach
    void setUp() {
        queryAdapter = new CrawledProductImageQueryAdapter(queryDslRepository, mapper);
    }

    @Test
    @DisplayName("성공 - ID로 이미지 조회")
    void shouldFindById() {
        // Given
        Long imageId = 1L;
        LocalDateTime now = LocalDateTime.now();
        CrawledProductImageJpaEntity entity =
                CrawledProductImageJpaEntity.of(
                        imageId,
                        1L,
                        "https://example.com/thumbnail.jpg",
                        null,
                        null,
                        ImageType.THUMBNAIL,
                        0,
                        now,
                        null);
        CrawledProductImage domain = CrawledProductImageFixture.aReconstitutedThumbnailPending();

        given(queryDslRepository.findById(imageId)).willReturn(Optional.of(entity));
        given(mapper.toDomain(entity)).willReturn(domain);

        // When
        Optional<CrawledProductImage> result = queryAdapter.findById(imageId);

        // Then
        assertThat(result).isPresent();
        verify(queryDslRepository).findById(imageId);
        verify(mapper).toDomain(entity);
    }

    @Test
    @DisplayName("성공 - ID로 이미지 조회 - 없는 경우")
    void shouldReturnEmptyWhenNotFoundById() {
        // Given
        Long imageId = 999L;

        given(queryDslRepository.findById(imageId)).willReturn(Optional.empty());

        // When
        Optional<CrawledProductImage> result = queryAdapter.findById(imageId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("성공 - CrawledProductId로 이미지 목록 조회")
    void shouldFindByCrawledProductId() {
        // Given
        CrawledProductId crawledProductId = CrawledProductId.of(1L);
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
        List<CrawledProductImageJpaEntity> entities = List.of(entity1, entity2);

        CrawledProductImage domain1 =
                CrawledProductImageFixture.aReconstitutedPending(
                        1L, 1L, "https://example.com/thumb1.jpg", ImageType.THUMBNAIL, 0);
        CrawledProductImage domain2 =
                CrawledProductImageFixture.aReconstitutedPending(
                        2L, 1L, "https://example.com/thumb2.jpg", ImageType.THUMBNAIL, 1);

        given(queryDslRepository.findByCrawledProductId(1L)).willReturn(entities);
        given(mapper.toDomain(entity1)).willReturn(domain1);
        given(mapper.toDomain(entity2)).willReturn(domain2);

        // When
        List<CrawledProductImage> results = queryAdapter.findByCrawledProductId(crawledProductId);

        // Then
        assertThat(results).hasSize(2);
        verify(queryDslRepository).findByCrawledProductId(1L);
    }

    @Test
    @DisplayName("성공 - CrawledProductId와 ImageType으로 이미지 목록 조회")
    void shouldFindByCrawledProductIdAndImageType() {
        // Given
        CrawledProductId crawledProductId = CrawledProductId.of(1L);
        ImageType imageType = ImageType.THUMBNAIL;
        LocalDateTime now = LocalDateTime.now();
        CrawledProductImageJpaEntity entity =
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
        CrawledProductImage domain =
                CrawledProductImageFixture.aReconstitutedPending(
                        1L, 1L, "https://example.com/thumb1.jpg", ImageType.THUMBNAIL, 0);

        given(queryDslRepository.findByCrawledProductIdAndImageType(1L, imageType))
                .willReturn(List.of(entity));
        given(mapper.toDomain(entity)).willReturn(domain);

        // When
        List<CrawledProductImage> results =
                queryAdapter.findByCrawledProductIdAndImageType(crawledProductId, imageType);

        // Then
        assertThat(results).hasSize(1);
        verify(queryDslRepository).findByCrawledProductIdAndImageType(1L, imageType);
    }

    @Test
    @DisplayName("성공 - CrawledProductId와 OriginalUrl로 이미지 조회")
    void shouldFindByCrawledProductIdAndOriginalUrl() {
        // Given
        CrawledProductId crawledProductId = CrawledProductId.of(1L);
        String originalUrl = "https://example.com/thumbnail.jpg";
        LocalDateTime now = LocalDateTime.now();
        CrawledProductImageJpaEntity entity =
                CrawledProductImageJpaEntity.of(
                        1L, 1L, originalUrl, null, null, ImageType.THUMBNAIL, 0, now, null);
        CrawledProductImage domain = CrawledProductImageFixture.aReconstitutedThumbnailPending();

        given(queryDslRepository.findByCrawledProductIdAndOriginalUrl(1L, originalUrl))
                .willReturn(Optional.of(entity));
        given(mapper.toDomain(entity)).willReturn(domain);

        // When
        Optional<CrawledProductImage> result =
                queryAdapter.findByCrawledProductIdAndOriginalUrl(crawledProductId, originalUrl);

        // Then
        assertThat(result).isPresent();
        verify(queryDslRepository).findByCrawledProductIdAndOriginalUrl(1L, originalUrl);
    }

    @Test
    @DisplayName("성공 - 기존 원본 URL 목록 조회")
    void shouldFindExistingOriginalUrls() {
        // Given
        CrawledProductId crawledProductId = CrawledProductId.of(1L);
        List<String> originalUrls =
                List.of("https://example.com/a.jpg", "https://example.com/b.jpg");
        List<String> existingUrls = List.of("https://example.com/a.jpg");

        given(queryDslRepository.findExistingOriginalUrls(1L, originalUrls))
                .willReturn(existingUrls);

        // When
        List<String> results =
                queryAdapter.findExistingOriginalUrls(crawledProductId, originalUrls);

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0)).isEqualTo("https://example.com/a.jpg");
        verify(queryDslRepository).findExistingOriginalUrls(1L, originalUrls);
    }

    @Test
    @DisplayName("성공 - 원본 URL 존재 여부 확인")
    void shouldExistsByCrawledProductIdAndOriginalUrl() {
        // Given
        CrawledProductId crawledProductId = CrawledProductId.of(1L);
        String originalUrl = "https://example.com/thumbnail.jpg";

        given(queryDslRepository.existsByCrawledProductIdAndOriginalUrl(1L, originalUrl))
                .willReturn(true);

        // When
        boolean exists =
                queryAdapter.existsByCrawledProductIdAndOriginalUrl(crawledProductId, originalUrl);

        // Then
        assertThat(exists).isTrue();
        verify(queryDslRepository).existsByCrawledProductIdAndOriginalUrl(1L, originalUrl);
    }
}
