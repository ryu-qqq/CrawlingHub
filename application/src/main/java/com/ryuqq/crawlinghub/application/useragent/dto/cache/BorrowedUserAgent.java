package com.ryuqq.crawlinghub.application.useragent.dto.cache;

/**
 * borrow() 결과로 반환되는 DTO.
 *
 * <p>Execution에서 크롤링에 필요한 최소 정보만 포함. return 시 필요한 userAgentId를 함께 보관하여 반드시 반납할 수 있게 함.
 *
 * @author development-team
 * @since 1.0.0
 */
public record BorrowedUserAgent(
        long userAgentId,
        String userAgentValue,
        String sessionToken,
        String nid,
        String mustitUid,
        int consecutiveRateLimits) {

    /**
     * CachedUserAgent로부터 BorrowedUserAgent 생성
     *
     * @param cached Redis에서 borrow한 CachedUserAgent
     * @return BorrowedUserAgent
     */
    public static BorrowedUserAgent from(CachedUserAgent cached) {
        return new BorrowedUserAgent(
                cached.userAgentId(),
                cached.userAgentValue(),
                cached.sessionToken(),
                cached.nid(),
                cached.mustitUid(),
                cached.consecutiveRateLimits());
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
