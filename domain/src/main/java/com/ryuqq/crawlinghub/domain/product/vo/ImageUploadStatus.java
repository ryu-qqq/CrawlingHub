package com.ryuqq.crawlinghub.domain.product.vo;

/**
 * 이미지 업로드 상태
 *
 * <p>외부 이미지를 S3로 업로드하는 과정의 상태를 나타냅니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public enum ImageUploadStatus {

    /**
     * 업로드 대기 중
     *
     * <p>아직 파일 서버로 업로드 요청을 보내지 않은 상태
     */
    PENDING,

    /**
     * 업로드 진행 중
     *
     * <p>파일 서버로 업로드 요청을 보냈고 응답 대기 중인 상태
     */
    UPLOADING,

    /**
     * 업로드 완료
     *
     * <p>S3 업로드가 성공적으로 완료된 상태
     */
    UPLOADED,

    /**
     * 업로드 실패
     *
     * <p>업로드 과정에서 오류가 발생한 상태
     */
    FAILED
}
