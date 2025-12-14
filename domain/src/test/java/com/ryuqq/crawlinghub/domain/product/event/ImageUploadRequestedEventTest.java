package com.ryuqq.crawlinghub.domain.product.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.crawlinghub.domain.product.event.ImageUploadRequestedEvent.ImageUploadTarget;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.ImageType;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("event")
@DisplayName("ImageUploadRequestedEvent 단위 테스트")
class ImageUploadRequestedEventTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2024-01-15T10:00:00Z");
    private static final Clock FIXED_CLOCK = Clock.fixed(FIXED_INSTANT, ZoneId.of("UTC"));
    private static final CrawledProductId PRODUCT_ID = CrawledProductId.of(1L);

    @Nested
    @DisplayName("ImageUploadTarget")
    class ImageUploadTargetTest {

        @Test
        @DisplayName("유효한 값으로 생성한다")
        void shouldCreateWithValidValues() {
            // When
            ImageUploadTarget target =
                    new ImageUploadTarget("https://example.com/image.jpg", ImageType.THUMBNAIL);

            // Then
            assertThat(target.originalUrl()).isEqualTo("https://example.com/image.jpg");
            assertThat(target.imageType()).isEqualTo(ImageType.THUMBNAIL);
        }

        @Test
        @DisplayName("originalUrl이 null이면 예외를 던진다")
        void shouldThrowWhenOriginalUrlIsNull() {
            // When & Then
            assertThatThrownBy(() -> new ImageUploadTarget(null, ImageType.THUMBNAIL))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("originalUrl");
        }

        @Test
        @DisplayName("originalUrl이 공백이면 예외를 던진다")
        void shouldThrowWhenOriginalUrlIsBlank() {
            // When & Then
            assertThatThrownBy(() -> new ImageUploadTarget("  ", ImageType.THUMBNAIL))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("originalUrl");
        }

        @Test
        @DisplayName("imageType이 null이면 예외를 던진다")
        void shouldThrowWhenImageTypeIsNull() {
            // When & Then
            assertThatThrownBy(() -> new ImageUploadTarget("https://example.com/image.jpg", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("imageType");
        }
    }

    @Nested
    @DisplayName("생성자")
    class Constructor {

        @Test
        @DisplayName("유효한 값으로 이벤트를 생성한다")
        void shouldCreateEventWithValidValues() {
            // Given
            List<ImageUploadTarget> targets =
                    List.of(
                            new ImageUploadTarget(
                                    "https://example.com/img.jpg", ImageType.THUMBNAIL));

            // When
            ImageUploadRequestedEvent event =
                    new ImageUploadRequestedEvent(PRODUCT_ID, targets, FIXED_INSTANT);

            // Then
            assertThat(event.crawledProductId()).isEqualTo(PRODUCT_ID);
            assertThat(event.targets()).hasSize(1);
            assertThat(event.occurredAt()).isEqualTo(FIXED_INSTANT);
        }

        @Test
        @DisplayName("crawledProductId가 null이면 예외를 던진다")
        void shouldThrowWhenProductIdIsNull() {
            // Given
            List<ImageUploadTarget> targets =
                    List.of(
                            new ImageUploadTarget(
                                    "https://example.com/img.jpg", ImageType.THUMBNAIL));

            // When & Then
            assertThatThrownBy(() -> new ImageUploadRequestedEvent(null, targets, FIXED_INSTANT))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("crawledProductId");
        }

        @Test
        @DisplayName("targets가 null이면 예외를 던진다")
        void shouldThrowWhenTargetsIsNull() {
            // When & Then
            assertThatThrownBy(() -> new ImageUploadRequestedEvent(PRODUCT_ID, null, FIXED_INSTANT))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("업로드 대상");
        }

        @Test
        @DisplayName("targets가 비어있으면 예외를 던진다")
        void shouldThrowWhenTargetsIsEmpty() {
            // When & Then
            assertThatThrownBy(
                            () ->
                                    new ImageUploadRequestedEvent(
                                            PRODUCT_ID, Collections.emptyList(), FIXED_INSTANT))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("업로드 대상");
        }

        @Test
        @DisplayName("occurredAt이 null이면 예외를 던진다")
        void shouldThrowWhenOccurredAtIsNull() {
            // Given
            List<ImageUploadTarget> targets =
                    List.of(
                            new ImageUploadTarget(
                                    "https://example.com/img.jpg", ImageType.THUMBNAIL));

            // When & Then
            assertThatThrownBy(() -> new ImageUploadRequestedEvent(PRODUCT_ID, targets, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("occurredAt");
        }
    }

    @Nested
    @DisplayName("of 팩토리 메서드")
    class OfMethod {

        @Test
        @DisplayName("of()로 이벤트를 생성한다")
        void shouldCreateEventWithOf() {
            // Given
            List<ImageUploadTarget> targets =
                    List.of(
                            new ImageUploadTarget(
                                    "https://example.com/img1.jpg", ImageType.THUMBNAIL),
                            new ImageUploadTarget(
                                    "https://example.com/img2.jpg", ImageType.DESCRIPTION));

            // When
            ImageUploadRequestedEvent event =
                    ImageUploadRequestedEvent.of(PRODUCT_ID, targets, FIXED_CLOCK);

            // Then
            assertThat(event.crawledProductId()).isEqualTo(PRODUCT_ID);
            assertThat(event.targets()).hasSize(2);
            assertThat(event.occurredAt()).isEqualTo(FIXED_INSTANT);
        }
    }

    @Nested
    @DisplayName("ofUrls 팩토리 메서드")
    class OfUrlsMethod {

        @Test
        @DisplayName("ofUrls()로 단일 이미지 타입 이벤트를 생성한다")
        void shouldCreateEventWithOfUrls() {
            // Given
            List<String> imageUrls =
                    List.of(
                            "https://example.com/img1.jpg",
                            "https://example.com/img2.jpg",
                            "https://example.com/img3.jpg");

            // When
            ImageUploadRequestedEvent event =
                    ImageUploadRequestedEvent.ofUrls(
                            PRODUCT_ID, imageUrls, ImageType.THUMBNAIL, FIXED_CLOCK);

            // Then
            assertThat(event.crawledProductId()).isEqualTo(PRODUCT_ID);
            assertThat(event.targets()).hasSize(3);
            assertThat(event.targets())
                    .allSatisfy(
                            target ->
                                    assertThat(target.imageType()).isEqualTo(ImageType.THUMBNAIL));
            assertThat(event.occurredAt()).isEqualTo(FIXED_INSTANT);
        }
    }

    @Nested
    @DisplayName("DomainEvent 인터페이스")
    class DomainEventInterface {

        @Test
        @DisplayName("DomainEvent 인터페이스를 구현한다")
        void shouldImplementDomainEvent() {
            // Given
            List<ImageUploadTarget> targets =
                    List.of(
                            new ImageUploadTarget(
                                    "https://example.com/img.jpg", ImageType.THUMBNAIL));
            ImageUploadRequestedEvent event =
                    new ImageUploadRequestedEvent(PRODUCT_ID, targets, FIXED_INSTANT);

            // When & Then
            assertThat(event)
                    .isInstanceOf(com.ryuqq.crawlinghub.domain.common.event.DomainEvent.class);
        }
    }
}
