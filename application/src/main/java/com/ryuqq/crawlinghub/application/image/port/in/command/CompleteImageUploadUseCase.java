package com.ryuqq.crawlinghub.application.image.port.in.command;

/**
 * 이미지 업로드 완료 처리 UseCase
 *
 * <p>파일 서버에서 업로드 완료 웹훅을 받아 처리합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface CompleteImageUploadUseCase {

    /**
     * 이미지 업로드 완료 처리
     *
     * <p>Outbox 상태를 완료로 변경하고 CrawledProduct의 이미지 URL을 업데이트합니다.
     *
     * @param outboxId ImageOutbox ID
     * @param s3Url 업로드된 S3 URL
     */
    void complete(Long outboxId, String s3Url);

    /**
     * 이미지 업로드 완료 처리 (파일 자산 ID 포함)
     *
     * <p>Outbox 상태를 완료로 변경하고 CrawledProduct의 이미지 URL과 파일 자산 ID를 업데이트합니다.
     *
     * @param outboxId ImageOutbox ID
     * @param s3Url 업로드된 S3 URL
     * @param fileAssetId Fileflow 파일 자산 ID
     */
    void complete(Long outboxId, String s3Url, String fileAssetId);
}
