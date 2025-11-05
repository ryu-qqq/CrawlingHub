package com.ryuqq.crawlinghub.application.task.port.out;

/**
 * 인증 토큰 관리 Port
 *
 * <p>외부 API 호출 시 필요한 인증 토큰을 관리합니다.
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public interface TokenManagerPort {

    /**
     * 셀러의 인증 토큰 조회
     *
     * @param sellerId 셀러 ID
     * @return 인증 토큰 (없으면 null)
     */
    String getToken(Long sellerId);

    /**
     * 토큰 갱신
     *
     * @param sellerId 셀러 ID
     * @param newToken 새로운 토큰
     */
    void refreshToken(Long sellerId, String newToken);

    /**
     * 토큰 만료 여부 확인
     *
     * @param token 인증 토큰
     * @return 만료되었으면 true
     */
    boolean isExpired(String token);
}
