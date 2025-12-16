package com.ryuqq.crawlinghub.adapter.out.fileflow.dto;

import java.time.Instant;

/**
 * Fileflow 외부 다운로드 응답 DTO
 *
 * <p>Fileflow API의 ExternalDownloadApiResponse에 매핑됩니다.
 *
 * @param id ExternalDownload ID
 * @param status 현재 상태
 * @param createdAt 생성 시간
 * @author development-team
 * @since 1.0.0
 */
public record ExternalDownloadResponse(String id, String status, Instant createdAt) {}
