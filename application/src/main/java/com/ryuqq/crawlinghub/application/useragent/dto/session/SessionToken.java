package com.ryuqq.crawlinghub.application.useragent.dto.session;

import java.time.Instant;

/**
 * 세션 토큰 정보
 *
 * <p>외부 사이트에서 발급받은 세션 토큰과 관련 쿠키 정보를 담는 DTO
 *
 * <p><strong>포함 정보</strong>:
 *
 * <ul>
 *   <li>token: 세션 토큰 (mustit.co.kr 세션 쿠키 값)
 *   <li>nid: nid 쿠키 값 (Search API 필수)
 *   <li>mustitUid: mustit_uid 쿠키 값 (Search API 필수)
 *   <li>expiresAt: 세션 만료 시간
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public record SessionToken(String token, String nid, String mustitUid, Instant expiresAt) {
    /**
     * 세션이 유효한지 확인
     *
     * @param now 현재 시간
     * @return 만료되지 않았으면 true
     */
    public boolean isValid(Instant now) {
        return expiresAt != null && now.isBefore(expiresAt);
    }

    /**
     * 세션이 만료되었는지 확인
     *
     * @param now 현재 시간
     * @return 만료되었으면 true
     */
    public boolean isExpired(Instant now) {
        return expiresAt != null && now.isAfter(expiresAt);
    }

    /**
     * Search API용 쿠키가 있는지 확인
     *
     * @return nid와 mustitUid가 모두 있으면 true
     */
    public boolean hasSearchCookies() {
        return nid != null && !nid.isBlank() && mustitUid != null && !mustitUid.isBlank();
    }
}
