package com.ryuqq.crawlinghub.adapter.out.fileflow.adapter;

import com.ryuqq.crawlinghub.adapter.out.fileflow.config.FileflowClientProperties;
import com.ryuqq.crawlinghub.adapter.out.fileflow.dto.ExternalDownloadDetailResponse;
import com.ryuqq.crawlinghub.adapter.out.fileflow.dto.ExternalDownloadRequest;
import com.ryuqq.crawlinghub.adapter.out.fileflow.dto.ExternalDownloadResponse;
import com.ryuqq.crawlinghub.adapter.out.fileflow.dto.FileflowApiResponse;
import com.ryuqq.crawlinghub.application.product.port.out.client.FileServerClient;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Fileflow Client Adapter
 *
 * <p>FileServerClient Port의 구현체입니다. Fileflow의 외부 다운로드 API를 호출합니다.
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>외부 다운로드 요청 (POST /api/v1/file/external-downloads)
 *   <li>다운로드 상태 조회 (GET /api/v1/file/external-downloads/{id})
 *   <li>응답 매핑 (Fileflow Response → Port Response)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class FileflowClientAdapter implements FileServerClient {

    private static final Logger log = LoggerFactory.getLogger(FileflowClientAdapter.class);

    private static final String EXTERNAL_DOWNLOAD_PATH = "/api/v1/file/external-downloads";

    private final WebClient fileflowWebClient;
    private final FileflowClientProperties properties;

    public FileflowClientAdapter(WebClient fileflowWebClient, FileflowClientProperties properties) {
        this.fileflowWebClient = fileflowWebClient;
        this.properties = properties;
    }

    /**
     * 이미지 업로드 요청
     *
     * <p>Fileflow의 외부 다운로드 API를 호출하여 이미지 업로드를 요청합니다. callbackUrl은 Properties에서 가져옵니다.
     *
     * @param request 업로드 요청 정보
     * @return 요청 성공 여부
     */
    @Override
    public boolean requestImageUpload(ImageUploadRequest request) {
        try {
            ExternalDownloadRequest fileflowRequest =
                    ExternalDownloadRequest.of(request.originalUrl(), properties.getCallbackUrl());

            log.debug(
                    "Fileflow 외부 다운로드 요청: idempotencyKey={}, sourceUrl={}",
                    request.idempotencyKey(),
                    request.originalUrl());

            FileflowApiResponse<ExternalDownloadResponse> response =
                    fileflowWebClient
                            .post()
                            .uri(EXTERNAL_DOWNLOAD_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(fileflowRequest)
                            .retrieve()
                            .bodyToMono(
                                    new ParameterizedTypeReference<
                                            FileflowApiResponse<ExternalDownloadResponse>>() {})
                            .block();

            if (response == null || !response.isSuccess()) {
                log.warn(
                        "Fileflow 요청 실패: idempotencyKey={}, response={}",
                        request.idempotencyKey(),
                        response);
                return false;
            }

            log.info(
                    "Fileflow 외부 다운로드 요청 성공: idempotencyKey={}, downloadId={}",
                    request.idempotencyKey(),
                    response.data().id());

            return true;

        } catch (WebClientResponseException e) {
            log.error(
                    "Fileflow API 호출 오류: idempotencyKey={}, status={}, body={}",
                    request.idempotencyKey(),
                    e.getStatusCode(),
                    e.getResponseBodyAsString(),
                    e);
            return false;

        } catch (Exception e) {
            log.error(
                    "Fileflow 요청 중 예외 발생: idempotencyKey={}, error={}",
                    request.idempotencyKey(),
                    e.getMessage(),
                    e);
            return false;
        }
    }

    /**
     * 이미지 업로드 상태 조회
     *
     * <p>Fileflow의 외부 다운로드 상태 조회 API를 호출합니다.
     *
     * <p><strong>참고:</strong> idempotencyKey를 downloadId로 사용합니다. Fileflow에서 idempotencyKey가
     * downloadId로 사용되는지 확인이 필요합니다. 실제 구현 시 매핑 로직 조정이 필요할 수 있습니다.
     *
     * @param idempotencyKey 멱등성 키 (또는 다운로드 ID)
     * @return 업로드 결과
     */
    @Override
    public Optional<ImageUploadResult> getUploadStatus(String idempotencyKey) {
        try {
            String url = EXTERNAL_DOWNLOAD_PATH + "/" + idempotencyKey;

            log.debug("Fileflow 상태 조회: idempotencyKey={}", idempotencyKey);

            FileflowApiResponse<ExternalDownloadDetailResponse> response =
                    fileflowWebClient
                            .get()
                            .uri(url)
                            .retrieve()
                            .bodyToMono(
                                    new ParameterizedTypeReference<
                                            FileflowApiResponse<
                                                    ExternalDownloadDetailResponse>>() {})
                            .block();

            if (response == null || !response.isSuccess() || !response.hasData()) {
                log.warn("Fileflow 상태 조회 실패: idempotencyKey={}", idempotencyKey);
                return Optional.empty();
            }

            ExternalDownloadDetailResponse detail = response.data();

            return Optional.of(mapToImageUploadResult(idempotencyKey, detail));

        } catch (WebClientResponseException.NotFound e) {
            log.debug("Fileflow 다운로드를 찾을 수 없음: idempotencyKey={}", idempotencyKey);
            return Optional.empty();

        } catch (Exception e) {
            log.error(
                    "Fileflow 상태 조회 중 예외 발생: idempotencyKey={}, error={}",
                    idempotencyKey,
                    e.getMessage(),
                    e);
            return Optional.empty();
        }
    }

    /**
     * Fileflow 응답을 Port의 ImageUploadResult로 매핑
     *
     * @param idempotencyKey 멱등성 키
     * @param detail Fileflow 상세 응답
     * @return ImageUploadResult
     */
    private ImageUploadResult mapToImageUploadResult(
            String idempotencyKey, ExternalDownloadDetailResponse detail) {
        if (detail.isCompleted()) {
            // fileAssetId를 S3 URL로 사용 (실제로는 FileAsset 조회가 필요할 수 있음)
            return ImageUploadResult.completed(idempotencyKey, detail.fileAssetId());
        } else if (detail.isFailed()) {
            return ImageUploadResult.failed(idempotencyKey, detail.errorMessage());
        } else {
            return ImageUploadResult.pending(idempotencyKey);
        }
    }
}
