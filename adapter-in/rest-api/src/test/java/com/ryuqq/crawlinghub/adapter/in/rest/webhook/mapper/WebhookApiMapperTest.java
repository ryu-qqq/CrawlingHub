package com.ryuqq.crawlinghub.adapter.in.rest.webhook.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.adapter.in.rest.webhook.dto.command.ImageUploadWebhookApiRequest;
import com.ryuqq.crawlinghub.application.image.dto.command.ImageUploadWebhookCommand;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * WebhookApiMapper 단위 테스트
 *
 * <p>Webhook API ↔ Application Layer 변환 로직을 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("rest-api")
@Tag("mapper")
@DisplayName("WebhookApiMapper 단위 테스트")
class WebhookApiMapperTest {

    private WebhookApiMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new WebhookApiMapper();
    }

    @Nested
    @DisplayName("toCommand() 테스트")
    class ToCommandTests {

        @Test
        @DisplayName("성공 웹훅 요청을 Command로 변환한다")
        void toCommand_WithCompleted_ShouldConvertCorrectly() {
            // given
            Instant completedAt = Instant.parse("2025-12-17T10:30:00Z");
            ImageUploadWebhookApiRequest request =
                    new ImageUploadWebhookApiRequest(
                            "img-12345-abc",
                            "COMPLETED",
                            "https://cdn.set-of.com/bucket/image.jpg",
                            "asset-uuid-123",
                            null,
                            completedAt);

            // when
            ImageUploadWebhookCommand result = mapper.toCommand(request);

            // then
            assertThat(result.externalDownloadId()).isEqualTo("img-12345-abc");
            assertThat(result.status()).isEqualTo("COMPLETED");
            assertThat(result.fileUrl()).isEqualTo("https://cdn.set-of.com/bucket/image.jpg");
            assertThat(result.fileAssetId()).isEqualTo("asset-uuid-123");
            assertThat(result.errorMessage()).isNull();
            assertThat(result.completedAt()).isEqualTo(completedAt);
            assertThat(result.isCompleted()).isTrue();
            assertThat(result.isFailed()).isFalse();
        }

        @Test
        @DisplayName("실패 웹훅 요청을 Command로 변환한다")
        void toCommand_WithFailed_ShouldConvertCorrectly() {
            // given
            Instant completedAt = Instant.parse("2025-12-17T10:30:00Z");
            ImageUploadWebhookApiRequest request =
                    new ImageUploadWebhookApiRequest(
                            "img-12345-def", "FAILED", null, null, "Download timeout", completedAt);

            // when
            ImageUploadWebhookCommand result = mapper.toCommand(request);

            // then
            assertThat(result.externalDownloadId()).isEqualTo("img-12345-def");
            assertThat(result.status()).isEqualTo("FAILED");
            assertThat(result.fileUrl()).isNull();
            assertThat(result.fileAssetId()).isNull();
            assertThat(result.errorMessage()).isEqualTo("Download timeout");
            assertThat(result.completedAt()).isEqualTo(completedAt);
            assertThat(result.isCompleted()).isFalse();
            assertThat(result.isFailed()).isTrue();
        }

        @Test
        @DisplayName("모든 필드가 있는 요청을 변환한다")
        void toCommand_WithAllFields_ShouldConvertCorrectly() {
            // given
            Instant completedAt = Instant.parse("2025-12-17T10:30:00Z");
            ImageUploadWebhookApiRequest request =
                    new ImageUploadWebhookApiRequest(
                            "img-99999-xyz",
                            "COMPLETED",
                            "https://cdn.set-of.com/bucket/another.png",
                            "asset-uuid-456",
                            "partial failure occurred",
                            completedAt);

            // when
            ImageUploadWebhookCommand result = mapper.toCommand(request);

            // then
            assertThat(result.externalDownloadId()).isEqualTo("img-99999-xyz");
            assertThat(result.status()).isEqualTo("COMPLETED");
            assertThat(result.fileUrl()).isEqualTo("https://cdn.set-of.com/bucket/another.png");
            assertThat(result.fileAssetId()).isEqualTo("asset-uuid-456");
            assertThat(result.errorMessage()).isEqualTo("partial failure occurred");
            assertThat(result.completedAt()).isEqualTo(completedAt);
        }

        @Test
        @DisplayName("다양한 이벤트 타입을 처리한다")
        void toCommand_WithVariousEventTypes_ShouldConvertCorrectly() {
            // given - unknown event type
            ImageUploadWebhookApiRequest request =
                    new ImageUploadWebhookApiRequest(
                            "img-unknown", "PROCESSING", null, null, null, null);

            // when
            ImageUploadWebhookCommand result = mapper.toCommand(request);

            // then
            assertThat(result.status()).isEqualTo("PROCESSING");
            assertThat(result.isCompleted()).isFalse();
            assertThat(result.isFailed()).isFalse();
        }
    }
}
