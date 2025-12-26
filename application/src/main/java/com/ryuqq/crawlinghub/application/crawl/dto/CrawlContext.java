package com.ryuqq.crawlinghub.application.crawl.dto;

import com.ryuqq.crawlinghub.application.useragent.dto.cache.CachedUserAgent;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import java.util.HashMap;
import java.util.Map;

/**
 * 크롤링 컨텍스트 DTO (Record)
 *
 * <p>크롤러 실행에 필요한 컨텍스트 정보를 담는 불변 DTO.
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

    private static final String USER_AGENT_HEADER = "User-Agent";
    private static final String COOKIE_HEADER = "Cookie";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * CrawlTask와 UserAgent 정보로부터 CrawlContext 생성
     *
     * @param crawlTask 크롤 태스크 도메인 객체
     * @param userAgent 캐시된 UserAgent 정보
     * @return 크롤링 컨텍스트
     */
    public static CrawlContext of(CrawlTask crawlTask, CachedUserAgent userAgent) {
        return new CrawlContext(
                crawlTask.getId().value(),
                crawlTask.getCrawlSchedulerId().value(),
                crawlTask.getSellerId().value(),
                crawlTask.getTaskType(),
                crawlTask.getEndpoint().toFullUrl(),
                userAgent.userAgentId(),
                userAgent.userAgentValue(),
                userAgent.sessionToken(),
                userAgent.nid(),
                userAgent.mustitUid());
    }

    /**
     * HTTP 요청에 필요한 헤더 맵 생성
     *
     * <p>User-Agent 헤더와 Authorization Bearer 토큰을 포함한 헤더 맵을 반환합니다.
     *
     * @return HTTP 헤더 맵
     */
    public Map<String, String> buildHeaders() {
        Map<String, String> headers = new HashMap<>();

        if (userAgentValue != null && !userAgentValue.isBlank()) {
            headers.put(USER_AGENT_HEADER, userAgentValue);
        }

        if (sessionToken != null && !sessionToken.isBlank()) {
            headers.put(AUTHORIZATION_HEADER, BEARER_PREFIX + sessionToken);
        }

        if (hasSearchCookies()) {
            String cookieValue = "nid=" + nid + "; mustit_uid=" + mustitUid;
            headers.put(COOKIE_HEADER, cookieValue);
        }

        return headers;
    }

    // === Accessor aliases for backward compatibility ===

    public long getCrawlTaskId() {
        return crawlTaskId;
    }

    public long getSchedulerId() {
        return schedulerId;
    }

    public long getSellerId() {
        return sellerId;
    }

    public CrawlTaskType getTaskType() {
        return taskType;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public Long getUserAgentId() {
        return userAgentId;
    }

    public String getUserAgentValue() {
        return userAgentValue;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public String getNid() {
        return nid;
    }

    public String getMusitUid() {
        return mustitUid;
    }

    /**
     * Search API용 엔드포인트 반환
     *
     * <p>Search 타입의 경우 nid, uid, adId, beforeItemType 파라미터를 추가합니다.
     *
     * @return Search API 엔드포인트 URL
     */
    public String buildSearchEndpoint() {
        if (!hasSearchCookies()) {
            return endpoint;
        }

        StringBuilder sb = new StringBuilder(endpoint);
        String separator = endpoint.contains("?") ? "&" : "?";

        sb.append(separator);
        sb.append("nid=").append(nid);
        sb.append("&uid=").append(mustitUid);
        sb.append("&adId=").append(mustitUid);
        sb.append("&beforeItemType=Normal");

        return sb.toString();
    }

    /**
     * Search API 호출에 필요한 쿠키 존재 여부 확인
     *
     * <p>Search API는 nid, mustit_uid 쿠키가 필요합니다.
     *
     * @return 필수 쿠키 존재 여부
     */
    public boolean hasSearchCookies() {
        return nid != null && !nid.isBlank() && mustitUid != null && !mustitUid.isBlank();
    }
}
