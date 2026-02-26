package com.ryuqq.crawlinghub.adapter.out.marketplace.dto.request;

/**
 * 상품 이미지 페이로드
 *
 * <p>이미지 등록/수정 API에서 공통으로 사용하는 독립 객체
 *
 * @param url 이미지 URL (S3 URL 우선)
 * @param imageType 이미지 타입 (THUMBNAIL, DESCRIPTION)
 * @param displayOrder 표시 순서
 * @author development-team
 * @since 1.0.0
 */
public record ProductImagePayload(String url, String imageType, int displayOrder) {}
