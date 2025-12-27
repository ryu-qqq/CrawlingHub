package com.ryuqq.crawlinghub.adapter.out.fileflow.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.adapter.out.fileflow.config.FileflowClientProperties;
import com.ryuqq.crawlinghub.application.product.port.out.client.FileServerClient.ImageUploadRequest;
import com.ryuqq.crawlinghub.application.product.port.out.client.FileServerClient.ImageUploadResult;
import com.ryuqq.fileflow.sdk.api.ExternalDownloadApi;
import com.ryuqq.fileflow.sdk.api.FileAssetApi;
import com.ryuqq.fileflow.sdk.client.FileFlowClient;
import com.ryuqq.fileflow.sdk.exception.FileFlowException;
import com.ryuqq.fileflow.sdk.exception.FileFlowNotFoundException;
import com.ryuqq.fileflow.sdk.model.asset.FileAssetResponse;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * FileflowClientAdapter 단위 테스트
 *
 * <p>FileFlow SDK를 사용한 파일 서버 연동 Adapter의 비즈니스 로직을 검증합니다.
 *
 * <p>테스트 범위:
 *
 * <ul>
 *   <li>requestImageUpload - 외부 다운로드 요청 성공/실패
 *   <li>getUploadStatus - FileAsset 상태 조회 (COMPLETED/FAILED/PENDING)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("adapter")
@Tag("fileflow")
@ExtendWith(MockitoExtension.class)
@DisplayName("FileflowClientAdapter 단위 테스트")
class FileflowClientAdapterTest {

    @Mock private FileFlowClient fileFlowClient;

    @Mock private ExternalDownloadApi externalDownloadApi;

    @Mock private FileAssetApi fileAssetApi;

    private FileflowClientProperties properties;
    private FileflowClientAdapter adapter;

    private static final String BASE_URL = "https://fileflow.example.com";
    private static final String CALLBACK_URL = "https://crawlinghub.example.com/webhook/fileflow";
    private static final String SERVICE_TOKEN = "test-service-token";

    @BeforeEach
    void setUp() {
        properties = createDefaultProperties();
        adapter = new FileflowClientAdapter(fileFlowClient, properties);
    }

    private FileflowClientProperties createDefaultProperties() {
        FileflowClientProperties props = new FileflowClientProperties();
        props.setBaseUrl(BASE_URL);
        props.setCallbackUrl(CALLBACK_URL);
        props.setServiceToken(SERVICE_TOKEN);
        props.setConnectTimeout(5000);
        props.setReadTimeout(10000);
        return props;
    }

    @Nested
    @DisplayName("requestImageUpload - 이미지 업로드 요청")
    class RequestImageUploadTests {

        @Test
        @DisplayName("성공 - SDK를 통해 외부 다운로드 요청")
        void shouldRequestExternalDownloadSuccessfully() {
            // Given
            String idempotencyKey = "test-key-123";
            String originalUrl = "https://example.com/image.jpg";
            ImageUploadRequest request = ImageUploadRequest.of(idempotencyKey, originalUrl, "jpg");

            given(fileFlowClient.externalDownloads()).willReturn(externalDownloadApi);
            given(externalDownloadApi.request(idempotencyKey, originalUrl, CALLBACK_URL))
                    .willReturn("download-id-456");

            // When
            boolean result = adapter.requestImageUpload(request);

            // Then
            assertThat(result).isTrue();
            verify(externalDownloadApi).request(idempotencyKey, originalUrl, CALLBACK_URL);
        }

        @Test
        @DisplayName("실패 - FileFlowException 발생 시 false 반환")
        void shouldReturnFalseWhenFileFlowExceptionOccurs() {
            // Given
            String idempotencyKey = "test-key-123";
            String originalUrl = "https://example.com/image.jpg";
            ImageUploadRequest request = ImageUploadRequest.of(idempotencyKey, originalUrl, "jpg");

            given(fileFlowClient.externalDownloads()).willReturn(externalDownloadApi);
            given(externalDownloadApi.request(anyString(), anyString(), anyString()))
                    .willThrow(new FileFlowException("API Error"));

            // When
            boolean result = adapter.requestImageUpload(request);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("실패 - 일반 예외 발생 시 false 반환")
        void shouldReturnFalseWhenGeneralExceptionOccurs() {
            // Given
            String idempotencyKey = "test-key-123";
            String originalUrl = "https://example.com/image.jpg";
            ImageUploadRequest request = ImageUploadRequest.of(idempotencyKey, originalUrl, "jpg");

            given(fileFlowClient.externalDownloads()).willReturn(externalDownloadApi);
            given(externalDownloadApi.request(anyString(), anyString(), anyString()))
                    .willThrow(new RuntimeException("Network Error"));

            // When
            boolean result = adapter.requestImageUpload(request);

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("getUploadStatus - 이미지 업로드 상태 조회")
    class GetUploadStatusTests {

        @Test
        @DisplayName("성공 - COMPLETED 상태 조회")
        void shouldReturnCompletedStatusWhenFileAssetCompleted() {
            // Given
            String fileAssetId = "asset-123";
            FileAssetResponse response =
                    createFileAssetResponse(fileAssetId, "COMPLETED", "s3/key/image.jpg");

            given(fileFlowClient.fileAssets()).willReturn(fileAssetApi);
            given(fileAssetApi.get(fileAssetId)).willReturn(response);

            // When
            Optional<ImageUploadResult> result = adapter.getUploadStatus(fileAssetId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().isCompleted()).isTrue();
            assertThat(result.get().s3Url()).isEqualTo("s3/key/image.jpg");
        }

        @Test
        @DisplayName("성공 - FAILED 상태 조회")
        void shouldReturnFailedStatusWhenFileAssetFailed() {
            // Given
            String fileAssetId = "asset-123";
            FileAssetResponse response = createFileAssetResponse(fileAssetId, "FAILED", null);

            given(fileFlowClient.fileAssets()).willReturn(fileAssetApi);
            given(fileAssetApi.get(fileAssetId)).willReturn(response);

            // When
            Optional<ImageUploadResult> result = adapter.getUploadStatus(fileAssetId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().isFailed()).isTrue();
        }

        @Test
        @DisplayName("성공 - PENDING 상태 조회 (PROCESSING)")
        void shouldReturnPendingStatusWhenFileAssetProcessing() {
            // Given
            String fileAssetId = "asset-123";
            FileAssetResponse response = createFileAssetResponse(fileAssetId, "PROCESSING", null);

            given(fileFlowClient.fileAssets()).willReturn(fileAssetApi);
            given(fileAssetApi.get(fileAssetId)).willReturn(response);

            // When
            Optional<ImageUploadResult> result = adapter.getUploadStatus(fileAssetId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().isPending()).isTrue();
        }

        @Test
        @DisplayName("실패 - FileAsset을 찾을 수 없으면 빈 Optional 반환")
        void shouldReturnEmptyWhenFileAssetNotFound() {
            // Given
            String fileAssetId = "non-existent-asset";

            given(fileFlowClient.fileAssets()).willReturn(fileAssetApi);
            given(fileAssetApi.get(fileAssetId))
                    .willThrow(new FileFlowNotFoundException("NOT_FOUND", "FileAsset not found"));

            // When
            Optional<ImageUploadResult> result = adapter.getUploadStatus(fileAssetId);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("실패 - FileFlowException 발생 시 빈 Optional 반환")
        void shouldReturnEmptyWhenFileFlowExceptionOccurs() {
            // Given
            String fileAssetId = "asset-123";

            given(fileFlowClient.fileAssets()).willReturn(fileAssetApi);
            given(fileAssetApi.get(fileAssetId)).willThrow(new FileFlowException("API Error"));

            // When
            Optional<ImageUploadResult> result = adapter.getUploadStatus(fileAssetId);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("실패 - 일반 예외 발생 시 빈 Optional 반환")
        void shouldReturnEmptyWhenGeneralExceptionOccurs() {
            // Given
            String fileAssetId = "asset-123";

            given(fileFlowClient.fileAssets()).willReturn(fileAssetApi);
            given(fileAssetApi.get(fileAssetId)).willThrow(new RuntimeException("Network Error"));

            // When
            Optional<ImageUploadResult> result = adapter.getUploadStatus(fileAssetId);

            // Then
            assertThat(result).isEmpty();
        }
    }

    private FileAssetResponse createFileAssetResponse(String id, String status, String s3Key) {
        return new FileAssetResponse(
                id,
                "original-filename.jpg",
                "image/jpeg",
                1024L,
                status,
                "image",
                s3Key,
                null,
                null);
    }
}
