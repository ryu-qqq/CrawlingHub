package com.ryuqq.crawlinghub.application.image.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.ryuqq.crawlinghub.application.common.config.TransactionEventRegistry;
import com.ryuqq.crawlinghub.application.image.manager.command.CrawledProductImageTransactionManager;
import com.ryuqq.crawlinghub.application.image.manager.command.ProductImageOutboxTransactionManager;
import com.ryuqq.crawlinghub.application.product.dto.bundle.ImageUploadData;
import com.ryuqq.crawlinghub.application.product.port.out.query.ImageOutboxQueryPort;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * ImageUploadBundleFactory 단위 테스트
 *
 * <p>Mockist 스타일 테스트: Manager Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ImageUploadBundleFactory 테스트")
class ImageUploadBundleFactoryTest {

    private static final Instant FIXED_TIME = Instant.parse("2025-01-15T10:00:00Z");

    @Mock private CrawledProductImageTransactionManager imageTransactionManager;
    @Mock private ProductImageOutboxTransactionManager outboxTransactionManager;
    @Mock private ImageOutboxQueryPort imageOutboxQueryPort;
    @Mock private TransactionEventRegistry eventRegistry;

    private Clock fixedClock;
    private ImageUploadBundleFactory factory;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(FIXED_TIME, ZoneId.of("UTC"));
        factory =
                new ImageUploadBundleFactory(
                        imageTransactionManager,
                        outboxTransactionManager,
                        imageOutboxQueryPort,
                        eventRegistry,
                        fixedClock);
    }

    @Nested
    @DisplayName("processImageUpload() 테스트")
    class ProcessImageUpload {

        @Test
        @DisplayName("[성공] 이미지 업로드 처리 - 이미지, Outbox, Event 생성 및 등록")
        void shouldProcessImageUpload() {
            // Given
            CrawledProductId productId = CrawledProductId.of(100L);
            List<String> imageUrls =
                    List.of("https://example.com/image1.jpg", "https://example.com/image2.jpg");
            ImageUploadData uploadData =
                    ImageUploadData.of(imageUrls, ImageType.THUMBNAIL)
                            .enrichWithProductId(productId);

            List<CrawledProductImage> savedImages =
                    List.of(
                            createImage(1L, productId, "https://example.com/image1.jpg"),
                            createImage(2L, productId, "https://example.com/image2.jpg"));
            given(imageTransactionManager.saveAll(anyList())).willReturn(savedImages);

            // When
            factory.processImageUpload(uploadData);

            // Then
            verify(imageTransactionManager).saveAll(anyList());
            verify(outboxTransactionManager).persistAll(anyList());
            verify(eventRegistry).registerForPublish(any(ImageUploadRequestedEvent.class));
        }

        @Test
        @DisplayName("[성공] 이미지가 없으면 처리하지 않음")
        void shouldNotProcessWhenNoImages() {
            // Given
            ImageUploadData uploadData = ImageUploadData.of(List.of(), ImageType.THUMBNAIL);

            // When
            factory.processImageUpload(uploadData);

            // Then
            verifyNoInteractions(imageTransactionManager);
            verifyNoInteractions(outboxTransactionManager);
            verifyNoInteractions(eventRegistry);
        }
    }

    @Nested
    @DisplayName("createImages() 테스트")
    class CreateImages {

        @Test
        @DisplayName("[성공] 이미지 객체 생성")
        void shouldCreateImages() {
            // Given
            CrawledProductId productId = CrawledProductId.of(100L);
            List<String> imageUrls =
                    List.of("https://example.com/image1.jpg", "https://example.com/image2.jpg");
            ImageUploadData uploadData =
                    ImageUploadData.of(imageUrls, ImageType.THUMBNAIL)
                            .enrichWithProductId(productId);

            // When
            List<CrawledProductImage> result = factory.createImages(uploadData);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getOriginalUrl()).isEqualTo("https://example.com/image1.jpg");
            assertThat(result.get(1).getOriginalUrl()).isEqualTo("https://example.com/image2.jpg");
        }
    }

    @Nested
    @DisplayName("createOutboxes() 테스트")
    class CreateOutboxes {

        @Test
        @DisplayName("[성공] Outbox 객체 생성")
        void shouldCreateOutboxes() {
            // Given
            CrawledProductId productId = CrawledProductId.of(100L);
            ImageUploadData uploadData =
                    ImageUploadData.of(
                                    List.of("https://example.com/image1.jpg"), ImageType.THUMBNAIL)
                            .enrichWithProductId(productId);
            List<CrawledProductImage> savedImages =
                    List.of(createImage(1L, productId, "https://example.com/image1.jpg"));

            // When
            List<ProductImageOutbox> result = factory.createOutboxes(savedImages, uploadData);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCrawledProductImageId()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("createEvent() 테스트")
    class CreateEvent {

        @Test
        @DisplayName("[성공] Event 객체 생성")
        void shouldCreateEvent() {
            // Given
            CrawledProductId productId = CrawledProductId.of(100L);
            List<String> imageUrls = List.of("https://example.com/image1.jpg");
            ImageUploadData uploadData =
                    ImageUploadData.of(imageUrls, ImageType.THUMBNAIL)
                            .enrichWithProductId(productId);

            // When
            ImageUploadRequestedEvent result = factory.createEvent(uploadData);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.crawledProductId()).isEqualTo(productId);
        }
    }

    // === Helper Methods ===

    private CrawledProductImage createImage(Long id, CrawledProductId productId, String url) {
        return CrawledProductImage.reconstitute(
                id, productId, url, ImageType.THUMBNAIL, 0, null, null, FIXED_TIME, null);
    }
}
