package com.ryuqq.crawlinghub.application.image.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.application.image.manager.command.CrawledProductImageTransactionManager;
import com.ryuqq.crawlinghub.application.product.port.out.command.CrawledProductImagePersistencePort;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductImage;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.ImageType;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawledProductImageTransactionManager 단위 테스트
 *
 * <p>Mockist 스타일 테스트: PersistencePort Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawledProductImageTransactionManager 테스트")
class CrawledProductImageTransactionManagerTest {

    private static final Instant FIXED_TIME = Instant.parse("2025-01-15T10:00:00Z");

    @Mock private CrawledProductImagePersistencePort imagePersistencePort;

    private Clock fixedClock;
    private CrawledProductImageTransactionManager manager;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(FIXED_TIME, ZoneId.of("UTC"));
        manager = new CrawledProductImageTransactionManager(imagePersistencePort, fixedClock);
    }

    @Nested
    @DisplayName("save() 테스트")
    class Save {

        @Test
        @DisplayName("[성공] 이미지 단건 저장")
        void shouldSaveImage() {
            // Given
            CrawledProductImage image = createImage(null, 100L);
            CrawledProductImage savedImage = createImage(1L, 100L);
            given(imagePersistencePort.save(image)).willReturn(savedImage);

            // When
            CrawledProductImage result = manager.save(image);

            // Then
            assertThat(result.getId()).isEqualTo(1L);
            verify(imagePersistencePort).save(image);
        }
    }

    @Nested
    @DisplayName("saveAll() 테스트")
    class SaveAll {

        @Test
        @DisplayName("[성공] 이미지 벌크 저장")
        void shouldSaveAllImages() {
            // Given
            List<CrawledProductImage> images =
                    List.of(createImage(null, 100L), createImage(null, 100L));
            List<CrawledProductImage> savedImages =
                    List.of(createImage(1L, 100L), createImage(2L, 100L));
            given(imagePersistencePort.saveAll(images)).willReturn(savedImages);

            // When
            List<CrawledProductImage> result = manager.saveAll(images);

            // Then
            assertThat(result).hasSize(2);
            verify(imagePersistencePort).saveAll(images);
        }

        @Test
        @DisplayName("[성공] 빈 목록 입력 → 빈 목록 반환")
        void shouldReturnEmptyListForEmptyInput() {
            // Given
            List<CrawledProductImage> emptyList = List.of();

            // When
            List<CrawledProductImage> result = manager.saveAll(emptyList);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("[성공] null 입력 → 빈 목록 반환")
        void shouldReturnEmptyListForNullInput() {
            // When
            List<CrawledProductImage> result = manager.saveAll(null);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("completeUpload() 테스트")
    class CompleteUpload {

        @Test
        @DisplayName("[성공] 이미지 업로드 완료 처리")
        void shouldCompleteUpload() {
            // Given
            CrawledProductImage image = createImage(1L, 100L);
            String s3Url = "https://s3.amazonaws.com/bucket/image.jpg";
            String fileAssetId = "file-asset-123";

            // When
            manager.completeUpload(image, s3Url, fileAssetId);

            // Then
            assertThat(image.getS3Url()).isEqualTo(s3Url);
            assertThat(image.getFileAssetId()).isEqualTo(fileAssetId);
            assertThat(image.isUploaded()).isTrue();
            verify(imagePersistencePort).update(image);
        }
    }

    // === Helper Methods ===

    private CrawledProductImage createImage(Long id, Long crawledProductId) {
        return CrawledProductImage.reconstitute(
                id,
                CrawledProductId.of(crawledProductId),
                "https://example.com/image.jpg",
                ImageType.THUMBNAIL,
                1,
                null,
                null,
                FIXED_TIME,
                null);
    }
}
