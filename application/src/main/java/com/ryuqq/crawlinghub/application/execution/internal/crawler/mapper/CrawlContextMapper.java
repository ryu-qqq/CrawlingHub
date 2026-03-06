package com.ryuqq.crawlinghub.application.execution.internal.crawler.mapper;

import com.ryuqq.crawlinghub.application.useragent.dto.cache.BorrowedUserAgent;
import com.ryuqq.crawlinghub.application.useragent.dto.cache.CachedUserAgent;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlContext;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
    private static final String X_ROUTE_TOKEN_HEADER = "X-Route-Token";
    private static final String X_ROUTE_TOKEN_VALUE = "Next-Route-Token";

    private static final String MUSTIT_BASE_URL = "https://m.web.mustit.co.kr";
    private static final String BFF_SEARCH_PATH = "/v2/api/facade/searchItems";
    private static final String V1_API_PREFIX = "/mustit-api/facade-api";

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

        if (context.taskType() == CrawlTaskType.SEARCH) {
            headers.put(X_ROUTE_TOKEN_HEADER, X_ROUTE_TOKEN_VALUE);
        }

        return headers;
    }

    /**
     * Search API용 BFF 엔드포인트 생성
     *
     * <p>v1 내부 API 경로를 MUSTIT BFF 엔드포인트(/v2/api/facade/searchItems)로 래핑합니다. BFF가 Cookie에서 nid/uid를
     * 읽어 내부 API에 전달하므로, URL 파라미터로 추가하지 않습니다.
     *
     * @param context 크롤링 컨텍스트
     * @return BFF 래핑된 Search API 엔드포인트 URL
     */
    public String buildSearchEndpoint(CrawlContext context) {
        String v1FullUrl = context.endpoint();
        String v1RelativePath = extractV1RelativePath(v1FullUrl);
        String encodedPath = URLEncoder.encode(v1RelativePath, StandardCharsets.UTF_8);
        return MUSTIT_BASE_URL
                + BFF_SEARCH_PATH
                + "?keyword=&sort=POPULAR2&nextApiUrl="
                + encodedPath;
    }

    /**
     * 전체 v1 URL에서 상대 경로 추출
     *
     * <p>예: "https://m.web.mustit.co.kr/mustit-api/facade-api/v1/search/items?keyword=test" →
     * "/v1/search/items?keyword=test"
     *
     * @param fullUrl v1 API 전체 URL
     * @return v1 API 상대 경로 (/v1/... 부터)
     */
    String extractV1RelativePath(String fullUrl) {
        int prefixIndex = fullUrl.indexOf(V1_API_PREFIX);
        if (prefixIndex >= 0) {
            return fullUrl.substring(prefixIndex + V1_API_PREFIX.length());
        }
        // V1_API_PREFIX가 없는 경우 도메인 이후 전체 경로 반환
        int schemeEnd = fullUrl.indexOf("://");
        if (schemeEnd >= 0) {
            int pathStart = fullUrl.indexOf('/', schemeEnd + 3);
            if (pathStart >= 0) {
                return fullUrl.substring(pathStart);
            }
        }
        return fullUrl;
    }
}
