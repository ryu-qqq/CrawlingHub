package com.ryuqq.crawlinghub.application.execution.internal.crawler.mapper;

import com.ryuqq.crawlinghub.application.useragent.dto.cache.BorrowedUserAgent;
import com.ryuqq.crawlinghub.application.useragent.dto.cache.CachedUserAgent;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlContext;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * CrawlContext 매퍼
 *
 * <p>CrawlTask + CachedUserAgent로부터 CrawlContext를 생성하고, HTTP 요청에 필요한 헤더/엔드포인트 빌드 로직을 제공합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlContextMapper {

    private static final String USER_AGENT_HEADER = "User-Agent";
    private static final String COOKIE_HEADER = "Cookie";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * CrawlTask와 BorrowedUserAgent로부터 CrawlContext 생성
     *
     * @param crawlTask 크롤 태스크 도메인 객체
     * @param agent borrow()로 획득한 UserAgent
     * @return 크롤링 컨텍스트
     */
    public CrawlContext toCrawlContext(CrawlTask crawlTask, BorrowedUserAgent agent) {
        return new CrawlContext(
                crawlTask.getIdValue(),
                crawlTask.getCrawlSchedulerIdValue(),
                crawlTask.getSellerIdValue(),
                crawlTask.getTaskType(),
                crawlTask.getEndpoint().toFullUrl(),
                agent.userAgentId(),
                agent.userAgentValue(),
                agent.sessionToken(),
                agent.nid(),
                agent.mustitUid());
    }

    /**
     * CrawlTask와 CachedUserAgent 정보로부터 CrawlContext 생성 (하위 호환용)
     *
     * @param crawlTask 크롤 태스크 도메인 객체
     * @param userAgent 캐시된 UserAgent 정보
     * @return 크롤링 컨텍스트
     * @deprecated borrow/return 패턴 사용. {@link #toCrawlContext(CrawlTask, BorrowedUserAgent)} 참고
     */
    @Deprecated
    public CrawlContext toCrawlContext(CrawlTask crawlTask, CachedUserAgent userAgent) {
        return new CrawlContext(
                crawlTask.getIdValue(),
                crawlTask.getCrawlSchedulerIdValue(),
                crawlTask.getSellerIdValue(),
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
     * @param context 크롤링 컨텍스트
     * @return HTTP 헤더 맵
     */
    public Map<String, String> buildHeaders(CrawlContext context) {
        Map<String, String> headers = new HashMap<>();

        if (context.userAgentValue() != null && !context.userAgentValue().isBlank()) {
            headers.put(USER_AGENT_HEADER, context.userAgentValue());
        }

        if (context.sessionToken() != null && !context.sessionToken().isBlank()) {
            headers.put(AUTHORIZATION_HEADER, BEARER_PREFIX + context.sessionToken());
        }

        if (context.hasSearchCookies()) {
            String cookieValue = "nid=" + context.nid() + "; mustit_uid=" + context.mustitUid();
            headers.put(COOKIE_HEADER, cookieValue);
        }

        return headers;
    }

    /**
     * Search API용 엔드포인트 반환
     *
     * @param context 크롤링 컨텍스트
     * @return Search API 엔드포인트 URL
     */
    public String buildSearchEndpoint(CrawlContext context) {
        if (!context.hasSearchCookies()) {
            return context.endpoint();
        }

        StringBuilder sb = new StringBuilder(context.endpoint());
        String separator = context.endpoint().contains("?") ? "&" : "?";

        sb.append(separator);
        sb.append("nid=").append(context.nid());
        sb.append("&uid=").append(context.mustitUid());
        sb.append("&adId=").append(context.mustitUid());
        sb.append("&beforeItemType=Normal");

        return sb.toString();
    }
}
