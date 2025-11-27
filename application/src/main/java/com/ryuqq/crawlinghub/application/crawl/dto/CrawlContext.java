package com.ryuqq.crawlinghub.application.crawl.dto;

import com.ryuqq.crawlinghub.application.useragent.dto.cache.CachedUserAgent;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import java.util.HashMap;
import java.util.Map;

/**
 * 크롤링 컨텍스트 DTO
 *
 * <p>크롤러 실행에 필요한 컨텍스트 정보를 담는 DTO. CrawlTask 도메인 객체와 UserAgent 정보를 조합하여 크롤링에 필요한 정보를 제공.
 *
 * <p><strong>DTO로 설계한 이유</strong>:
 *
 * <ul>
 *   <li>크롤러 실행을 위한 데이터 전달 목적
 *   <li>도메인 규칙을 표현하지 않음 (VO 아님)
 *   <li>Application 레이어 내부에서만 사용
 * </ul>
 *
 * <p><strong>UserAgent 정보 포함</strong>:
 *
 * <ul>
 *   <li>userAgentId: 결과 기록용 식별자
 *   <li>userAgentValue: User-Agent 헤더 값
 *   <li>sessionToken: 세션 토큰 (Cookie)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public class CrawlContext {

    private static final String USER_AGENT_HEADER = "User-Agent";
    private static final String COOKIE_HEADER = "Cookie";

    private final long crawlTaskId;
    private final long schedulerId;
    private final long sellerId;
    private final CrawlTaskType taskType;
    private final String endpoint;
    private final Long userAgentId;
    private final String userAgentValue;
    private final String sessionToken;

    private CrawlContext(
            long crawlTaskId,
            long schedulerId,
            long sellerId,
            CrawlTaskType taskType,
            String endpoint,
            Long userAgentId,
            String userAgentValue,
            String sessionToken) {
        this.crawlTaskId = crawlTaskId;
        this.schedulerId = schedulerId;
        this.sellerId = sellerId;
        this.taskType = taskType;
        this.endpoint = endpoint;
        this.userAgentId = userAgentId;
        this.userAgentValue = userAgentValue;
        this.sessionToken = sessionToken;
    }

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
                userAgent.sessionToken());
    }

    /**
     * HTTP 요청에 필요한 헤더 맵 생성
     *
     * <p>User-Agent 헤더와 세션 토큰(Cookie)을 포함한 헤더 맵을 반환합니다.
     *
     * @return HTTP 헤더 맵
     */
    public Map<String, String> buildHeaders() {
        Map<String, String> headers = new HashMap<>();

        if (userAgentValue != null && !userAgentValue.isBlank()) {
            headers.put(USER_AGENT_HEADER, userAgentValue);
        }

        if (sessionToken != null && !sessionToken.isBlank()) {
            headers.put(COOKIE_HEADER, sessionToken);
        }

        return headers;
    }

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
}
