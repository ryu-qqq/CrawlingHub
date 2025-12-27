package com.ryuqq.crawlinghub.adapter.out.fileflow.adapter;

import com.ryuqq.crawlinghub.adapter.out.fileflow.config.FileflowClientProperties;
import com.ryuqq.crawlinghub.application.product.port.out.client.FileServerClient;
import com.ryuqq.fileflow.sdk.client.FileFlowClient;
import com.ryuqq.fileflow.sdk.exception.FileFlowException;
import com.ryuqq.fileflow.sdk.exception.FileFlowNotFoundException;
import com.ryuqq.fileflow.sdk.model.asset.FileAssetResponse;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Fileflow Client Adapter
 *
 * <p>FileServerClient Port의 구현체입니다. FileFlow SDK를 사용하여 Fileflow API를 호출합니다.
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>외부 다운로드 요청 (POST /api/v1/file/external-downloads)
 *   <li>다운로드 상태 조회 (FileAsset API 활용)
 *   <li>응답 매핑 (SDK Response → Port Response)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class FileflowClientAdapter implements FileServerClient {

    private static final Logger log = LoggerFactory.getLogger(FileflowClientAdapter.class);

    private final FileFlowClient fileFlowClient;
    private final FileflowClientProperties properties;

    public FileflowClientAdapter(
            FileFlowClient fileFlowClient, FileflowClientProperties properties) {
        this.fileFlowClient = fileFlowClient;
        this.properties = properties;
    }

    /**
     * 이미지 업로드 요청
     *
     * <p>FileFlow SDK를 사용하여 외부 다운로드 API를 호출합니다. callbackUrl은 Properties에서 가져옵니다.
     *
     * @param request 업로드 요청 정보
     * @return 요청 성공 여부
     */
    @Override
    public boolean requestImageUpload(ImageUploadRequest request) {
        try {
            log.debug(
                    "Fileflow 외부 다운로드 요청: idempotencyKey={}, sourceUrl={}",
                    request.idempotencyKey(),
                    request.originalUrl());

            String downloadId =
                    fileFlowClient
                            .externalDownloads()
                            .request(
                                    request.idempotencyKey(),
                                    request.originalUrl(),
                                    properties.getCallbackUrl());

            log.info(
                    "Fileflow 외부 다운로드 요청 성공: idempotencyKey={}, downloadId={}",
                    request.idempotencyKey(),
                    downloadId);

            return true;

        } catch (FileFlowException e) {
            log.error(
                    "Fileflow API 호출 오류: idempotencyKey={}, error={}",
                    request.idempotencyKey(),
                    e.getMessage(),
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
     * <p>FileFlow SDK의 FileAsset API를 사용하여 파일 상태를 조회합니다.
     *
     * <p><strong>참고:</strong> idempotencyKey를 fileAssetId로 사용합니다. 외부 다운로드 완료 시 생성된 FileAsset의 ID가
     * 필요합니다.
     *
     * @param fileAssetId FileAsset ID
     * @return 업로드 결과
     */
    @Override
    public Optional<ImageUploadResult> getUploadStatus(String fileAssetId) {
        try {
            log.debug("Fileflow 상태 조회: fileAssetId={}", fileAssetId);

            FileAssetResponse fileAsset = fileFlowClient.fileAssets().get(fileAssetId);

            return Optional.of(mapToImageUploadResult(fileAssetId, fileAsset));

        } catch (FileFlowNotFoundException e) {
            log.debug("Fileflow FileAsset을 찾을 수 없음: fileAssetId={}", fileAssetId);
            return Optional.empty();

        } catch (FileFlowException e) {
            log.error(
                    "Fileflow API 호출 오류: fileAssetId={}, error={}", fileAssetId, e.getMessage(), e);
            return Optional.empty();

        } catch (Exception e) {
            log.error(
                    "Fileflow 상태 조회 중 예외 발생: fileAssetId={}, error={}",
                    fileAssetId,
                    e.getMessage(),
                    e);
            return Optional.empty();
        }
    }

    /**
     * FileAsset 응답을 Port의 ImageUploadResult로 매핑
     *
     * @param fileAssetId FileAsset ID
     * @param fileAsset FileFlow SDK FileAsset 응답
     * @return ImageUploadResult
     */
    private ImageUploadResult mapToImageUploadResult(
            String fileAssetId, FileAssetResponse fileAsset) {
        String status = fileAsset.getStatus();

        if ("COMPLETED".equals(status)) {
            return ImageUploadResult.completed(fileAssetId, fileAsset.getS3Key());
        } else if ("FAILED".equals(status)) {
            return ImageUploadResult.failed(fileAssetId, "File processing failed");
        } else {
            return ImageUploadResult.pending(fileAssetId);
        }
    }
}
