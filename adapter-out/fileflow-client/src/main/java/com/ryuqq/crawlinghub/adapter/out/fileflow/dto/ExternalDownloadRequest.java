package com.ryuqq.crawlinghub.adapter.out.fileflow.dto;

/**
 * Fileflow 외부 다운로드 요청 DTO
 *
 * <p>Fileflow API의 RequestExternalDownloadApiRequest에 매핑됩니다.
 *
 * @param sourceUrl 다운로드할 외부 이미지 URL
 * @param webhookUrl 콜백 웹훅 URL
 * @author development-team
 * @since 1.0.0
 */
public record ExternalDownloadRequest(String sourceUrl, String webhookUrl) {

    public static ExternalDownloadRequest of(String sourceUrl, String webhookUrl) {
        return new ExternalDownloadRequest(sourceUrl, webhookUrl);
    }
}
