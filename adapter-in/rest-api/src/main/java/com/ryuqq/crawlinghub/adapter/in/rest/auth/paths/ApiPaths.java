package com.ryuqq.crawlinghub.adapter.in.rest.auth.paths;

/**
 * API 경로 상수 정의
 *
 * <p>모든 REST API 엔드포인트 경로를 중앙 집중 관리합니다. Controller에서 @RequestMapping에 사용됩니다.
 *
 * <p>경로 구조:
 *
 * <ul>
 *   <li>/api/v1/crawling/* - 모든 CrawlingHub API
 *   <li>관리 API: sellers, schedules, tasks, executions, user-agents (@PreAuthorize 권한 검사)
 * </ul>
 *
 * <p>사용 예시:
 *
 * <pre>{@code
 * @RestController
 * @RequestMapping(ApiPaths.Sellers.BASE)
 * public class SellerController { ... }
 * }</pre>
 *
 * @author development-team
 * @since 1.0.0
 */
@SuppressWarnings("PMD.DataClass")
public final class ApiPaths {

    public static final String API_VERSION = "/api/v1";

    /** Crawling 서비스 기본 경로 - Gateway 라우팅용 */
    public static final String CRAWLING_SERVICE_BASE = API_VERSION + "/crawling";

    private ApiPaths() {}

    /**
     * Seller 관련 API 경로
     *
     * <p>Seller 관리 API입니다. @PreAuthorize로 권한 검사를 수행합니다.
     */
    public static final class Sellers {
        public static final String BASE = CRAWLING_SERVICE_BASE + "/sellers";
        public static final String BY_ID = "/{id}";

        private Sellers() {}
    }

    /**
     * Schedule 관련 API 경로
     *
     * <p>크롤링 스케줄 관리 API입니다. @PreAuthorize로 권한 검사를 수행합니다.
     */
    public static final class Schedules {
        public static final String BASE = CRAWLING_SERVICE_BASE + "/schedules";
        public static final String BY_ID = "/{id}";

        private Schedules() {}
    }

    /**
     * Task 관련 API 경로
     *
     * <p>크롤링 작업 관리 API입니다. @PreAuthorize로 권한 검사를 수행합니다.
     */
    public static final class Tasks {
        public static final String BASE = CRAWLING_SERVICE_BASE + "/tasks";
        public static final String BY_ID = "/{id}";

        private Tasks() {}
    }

    /**
     * Execution 관련 API 경로
     *
     * <p>크롤링 실행 내역 조회 API입니다. @PreAuthorize로 권한 검사를 수행합니다.
     */
    public static final class Executions {
        public static final String BASE = CRAWLING_SERVICE_BASE + "/executions";
        public static final String BY_ID = "/{id}";

        private Executions() {}
    }

    /**
     * UserAgent 관련 API 경로
     *
     * <p>UserAgent Pool 관리 API입니다. @PreAuthorize로 권한 검사를 수행합니다.
     */
    public static final class UserAgents {
        public static final String BASE = CRAWLING_SERVICE_BASE + "/user-agents";
        public static final String POOL_STATUS = "/pool-status";
        public static final String RECOVER = "/recover";

        private UserAgents() {}
    }

    /**
     * CrawledProduct 관련 API 경로
     *
     * <p>크롤링된 상품 관리 API입니다. @PreAuthorize로 권한 검사를 수행합니다.
     */
    public static final class CrawledProducts {
        public static final String BASE = CRAWLING_SERVICE_BASE + "/crawled-products";
        public static final String BY_ID = "/{id}";

        private CrawledProducts() {}
    }

    /** 헬스체크 및 모니터링 API 경로 */
    public static final class Actuator {
        public static final String BASE = "/actuator";
        public static final String HEALTH = "/health";
        public static final String INFO = "/info";

        private Actuator() {}
    }

    /** OpenAPI (Swagger) 문서 경로 - Gateway 라우팅 패턴에 맞춤 */
    public static final class OpenApi {
        public static final String DOCS = CRAWLING_SERVICE_BASE + "/api-docs";
        public static final String DOCS_ALL = CRAWLING_SERVICE_BASE + "/api-docs/**";
        public static final String SWAGGER_UI = CRAWLING_SERVICE_BASE + "/swagger-ui/**";
        public static final String SWAGGER_UI_HTML = CRAWLING_SERVICE_BASE + "/swagger-ui.html";
        public static final String SWAGGER_CONFIG = CRAWLING_SERVICE_BASE + "/swagger-config";

        private OpenApi() {}
    }

    /** REST Docs 문서 경로 - Gateway 라우팅 패턴에 맞춤 */
    public static final class Docs {
        public static final String BASE = CRAWLING_SERVICE_BASE + "/docs";
        public static final String ALL = CRAWLING_SERVICE_BASE + "/docs/**";
        public static final String INDEX = CRAWLING_SERVICE_BASE + "/docs/index.html";

        private Docs() {}
    }

    /** Health Check 경로 */
    public static final class Health {
        public static final String CHECK = API_VERSION + "/health";

        private Health() {}
    }
}
