package com.ryuqq.crawlinghub.adapter.out.marketplace.dto.response;

/**
 * 외부몰 API 표준 응답 래퍼
 *
 * <p>외부몰 서버가 rest-api의 ApiResponse와 동일한 형식으로 응답합니다.
 *
 * @param data 응답 데이터
 * @param timestamp 응답 시각
 * @param requestId 요청 추적 ID
 * @param <T> 데이터 타입
 * @author development-team
 * @since 1.0.0
 */
public record MarketPlaceApiResponse<T>(T data, String timestamp, String requestId) {}
