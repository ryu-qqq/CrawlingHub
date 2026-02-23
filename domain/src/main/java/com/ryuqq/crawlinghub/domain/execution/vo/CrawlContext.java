package com.ryuqq.crawlinghub.domain.execution.vo;

import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;

/**
 * 크롤링 컨텍스트 VO
 *
 * <p>크롤러 실행에 필요한 컨텍스트 정보를 담는 불변 도메인 Value Object.
 *
 * @param crawlTaskId CrawlTask ID
 * @param schedulerId Scheduler ID
 * @param sellerId Seller ID
 * @param taskType Task 유형
 * @param endpoint 크롤링 대상 URL
 * @param userAgentId UserAgent ID (결과 기록용)
 * @param userAgentValue User-Agent 헤더 값
 * @param sessionToken 세션 토큰 (Authorization Bearer)
 * @param nid nid 쿠키 값 (Search API용)
 * @param mustitUid mustit_uid 쿠키 값 (Search API용)
 * @author development-team
 * @since 1.0.0
 */
public record CrawlContext(
        long crawlTaskId,
        long schedulerId,
        long sellerId,
        CrawlTaskType taskType,
        String endpoint,
        Long userAgentId,
        String userAgentValue,
        String sessionToken,
        String nid,
        String mustitUid) {

    /**
     * Search API 호출에 필요한 쿠키 존재 여부 확인
     *
     * @return 필수 쿠키 존재 여부
     */
    public boolean hasSearchCookies() {
        return nid != null && !nid.isBlank() && mustitUid != null && !mustitUid.isBlank();
    }
}
