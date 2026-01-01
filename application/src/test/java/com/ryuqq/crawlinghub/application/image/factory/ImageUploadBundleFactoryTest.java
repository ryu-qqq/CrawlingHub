package com.ryuqq.crawlinghub.application.image.factory;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.application.product.dto.bundle.ImageUploadData;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductImage;
import com.ryuqq.crawlinghub.domain.product.aggregate.ProductImageOutbox;
import com.ryuqq.crawlinghub.domain.product.event.ImageUploadRequestedEvent;
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

/**
 * ImageUploadBundleFactory 단위 테스트
 *
 * <p>순수 Factory 패턴 테스트: Clock 의존성만 있는 도메인 객체 생성 검증
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("ImageUploadBundleFactory 테스트")
class ImageUploadBundleFactoryTest {

    private static final Instant FIXED_TIME = Instant.parse("2025-01-15T10:00:00Z");
    private static final CrawledProductId PRODUCT_ID = CrawledProductId.of(100L);

    private Clock fixedClock;
    private ImageUploadBundleFactory factory;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(FIXED_TIME, ZoneId.of("UTC"));
        factory = new ImageUploadBundleFactory(fixedClock);
    }

    @Nested
    @DisplayName("createImages() 테스트")
    class CreateImages {

        @Test
        @DisplayName("[성공] 이미지 객체 생성")
        void shouldCreateImages() {
            // Given
            List<String> imageUrls =
                    List.of("https://example.com/image1.jpg", "https://example.com/image2.jpg");
            ImageUploadData uploadData =
                    ImageUploadData.of(imageUrls, ImageType.THUMBNAIL)
                            .enrichWithProductId(PRODUCT_ID);

            // When
            List<CrawledProductImage> result = factory.createImages(uploadData);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getOriginalUrl()).isEqualTo("https://example.com/image1.jpg");
            assertThat(result.get(1).getOriginalUrl()).isEqualTo("https://example.com/image2.jpg");
            assertThat(result.get(0).getCrawledProductId()).isEqualTo(PRODUCT_ID);
        }

        @Test
        @DisplayName("[성공] 빈 이미지 목록으로 생성 시 빈 리스트 반환")
        void shouldReturnEmptyListWhenNoImages() {
            // Given
            ImageUploadData uploadData =
                    ImageUploadData.of(List.of(), ImageType.THUMBNAIL)
                            .enrichWithProductId(PRODUCT_ID);

            // When
            List<CrawledProductImage> result = factory.createImages(uploadData);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("createOutboxes() 테스트")
    class CreateOutboxes {

        @Test
        @DisplayName("[성공] Outbox 객체 생성")
        void shouldCreateOutboxes() {
            // Given
            ImageUploadData uploadData =
                    ImageUploadData.of(
                                    List.of("https://example.com/image1.jpg"), ImageType.THUMBNAIL)
                            .enrichWithProductId(PRODUCT_ID);
            List<CrawledProductImage> savedImages =
                    List.of(createImage(1L, PRODUCT_ID, "https://example.com/image1.jpg"));

            // When
            List<ProductImageOutbox> result = factory.createOutboxes(savedImages, uploadData);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCrawledProductImageId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("[성공] 여러 이미지에 대한 Outbox 생성")
        void shouldCreateOutboxesForMultipleImages() {
            // Given
            List<String> imageUrls =
                    List.of("https://example.com/image1.jpg", "https://example.com/image2.jpg");
            ImageUploadData uploadData =
                    ImageUploadData.of(imageUrls, ImageType.THUMBNAIL)
                            .enrichWithProductId(PRODUCT_ID);
            List<CrawledProductImage> savedImages =
                    List.of(
                            createImage(1L, PRODUCT_ID, "https://example.com/image1.jpg"),
                            createImage(2L, PRODUCT_ID, "https://example.com/image2.jpg"));

            // When
            List<ProductImageOutbox> result = factory.createOutboxes(savedImages, uploadData);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getCrawledProductImageId()).isEqualTo(1L);
            assertThat(result.get(1).getCrawledProductImageId()).isEqualTo(2L);
        }
    }

    @Nested
    @DisplayName("createEvent() 테스트")
    class CreateEvent {

        @Test
        @DisplayName("[성공] Event 객체 생성")
        void shouldCreateEvent() {
            // Given
            List<String> imageUrls = List.of("https://example.com/image1.jpg");
            ImageUploadData uploadData =
                    ImageUploadData.of(imageUrls, ImageType.THUMBNAIL)
                            .enrichWithProductId(PRODUCT_ID);

            // When
            ImageUploadRequestedEvent result = factory.createEvent(uploadData);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.crawledProductId()).isEqualTo(PRODUCT_ID);
            assertThat(result.occurredAt()).isEqualTo(FIXED_TIME);
        }

        @Test
        @DisplayName("[성공] 여러 이미지에 대한 Event 생성")
        void shouldCreateEventWithMultipleImages() {
            // Given
            List<String> imageUrls =
                    List.of("https://example.com/image1.jpg", "https://example.com/image2.jpg");
            ImageUploadData uploadData =
                    ImageUploadData.of(imageUrls, ImageType.THUMBNAIL)
                            .enrichWithProductId(PRODUCT_ID);

            // When
            ImageUploadRequestedEvent result = factory.createEvent(uploadData);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.crawledProductId()).isEqualTo(PRODUCT_ID);
            assertThat(result.targets()).hasSize(2);
            assertThat(
                            result.targets().stream()
                                    .map(ImageUploadRequestedEvent.ImageUploadTarget::originalUrl)
                                    .toList())
                    .containsExactly(
                            "https://example.com/image1.jpg", "https://example.com/image2.jpg");
        }
    }

    // === Helper Methods ===

    private CrawledProductImage createImage(Long id, CrawledProductId productId, String url) {
        return CrawledProductImage.reconstitute(
                id, productId, url, ImageType.THUMBNAIL, 0, null, null, FIXED_TIME, null);
    }
}
