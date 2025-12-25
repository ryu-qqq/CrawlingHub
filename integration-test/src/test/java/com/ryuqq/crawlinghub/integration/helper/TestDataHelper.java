package com.ryuqq.crawlinghub.integration.helper;

import com.ryuqq.crawlinghub.application.useragent.dto.cache.CacheStatus;
import com.ryuqq.crawlinghub.application.useragent.dto.cache.CachedUserAgent;
import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentPoolCacheManager;
import com.ryuqq.crawlinghub.application.useragent.port.out.cache.UserAgentPoolCachePort;
import com.ryuqq.crawlinghub.domain.useragent.identifier.UserAgentId;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 통합 테스트용 테스트 데이터 헬퍼
 *
 * <p>JdbcTemplate을 사용하여 테스트 데이터를 삽입합니다. @BeforeEach 이후에 호출되므로 DatabaseCleaner와 충돌하지 않습니다.
 *
 * <p>Redis Pool 초기화도 지원합니다. UserAgentPoolCacheManager를 통해 테스트용 UserAgent를 READY 상태로 추가할 수 있습니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class TestDataHelper {

    private final JdbcTemplate jdbcTemplate;
    private final UserAgentPoolCacheManager userAgentPoolCacheManager;
    private final UserAgentPoolCachePort userAgentPoolCachePort;

    @Autowired
    public TestDataHelper(
            JdbcTemplate jdbcTemplate,
            @Autowired(required = false) UserAgentPoolCacheManager userAgentPoolCacheManager,
            @Autowired(required = false) UserAgentPoolCachePort userAgentPoolCachePort) {
        this.jdbcTemplate = jdbcTemplate;
        this.userAgentPoolCacheManager = userAgentPoolCacheManager;
        this.userAgentPoolCachePort = userAgentPoolCachePort;
    }

    /** Seller 테스트 데이터 삽입 */
    public void insertSellers() {
        jdbcTemplate.execute(
                """
INSERT INTO seller (id, must_it_seller_name, seller_name, status, product_count, created_at, updated_at)
VALUES
    (1, 'test-must-it-seller-1', 'test-seller-1', 'ACTIVE', 100, NOW(), NOW()),
    (2, 'test-must-it-seller-2', 'test-seller-2', 'ACTIVE', 50, NOW(), NOW()),
    (3, 'test-must-it-seller-3', 'test-seller-3', 'INACTIVE', 0, NOW(), NOW())
""");
    }

    /** CrawlScheduler 테스트 데이터 삽입 (Seller 데이터 필요) */
    public void insertSchedulers() {
        jdbcTemplate.execute(
                """
INSERT INTO crawl_scheduler (id, seller_id, scheduler_name, cron_expression, status, created_at, updated_at)
VALUES
    (1, 1, 'daily-product-sync', 'cron(0 2 * * ? *)', 'ACTIVE', NOW(), NOW()),
    (2, 1, 'hourly-price-check', 'cron(0 * * * ? *)', 'ACTIVE', NOW(), NOW()),
    (3, 2, 'weekly-inventory', 'cron(0 0 ? * SUN *)', 'INACTIVE', NOW(), NOW())
""");
    }

    /**
     * CrawlTask 테스트 데이터 삽입 (Seller, Scheduler 데이터 필요)
     *
     * <p>Web API 테스트와 Worker 테스트를 모두 지원하는 테스트 데이터:
     *
     * <ul>
     *   <li>taskId 1,2: PUBLISHED - Worker 테스트용 (META, DETAIL)
     *   <li>taskId 3: SUCCESS - 완료된 태스크
     *   <li>taskId 4: FAILED - Web API 재시도 테스트용
     *   <li>taskId 5: TIMEOUT - Web API 재시도 테스트용
     *   <li>taskId 6: WAITING - 대기 중인 태스크
     *   <li>taskId 7,8: PUBLISHED - Worker 테스트용 (에러 시나리오)
     * </ul>
     *
     * <p>상태 전환 흐름: WAITING → PUBLISHED → RUNNING → SUCCESS/FAILED/TIMEOUT
     */
    public void insertTasks() {
        jdbcTemplate.execute(
                """
INSERT INTO crawl_task (id, crawl_scheduler_id, seller_id, task_type, endpoint_base_url, endpoint_path, endpoint_query_params, status, retry_count, created_at, updated_at)
VALUES
    (1, 1, 1, 'META', 'https://api.example.com', '/products', '{"page": "1", "size": "100"}', 'PUBLISHED', 0, NOW(), NOW()),
    (2, 1, 1, 'DETAIL', 'https://api.example.com', '/products/123', '{}', 'PUBLISHED', 0, NOW(), NOW()),
    (3, 1, 1, 'OPTION', 'https://api.example.com', '/products/123/options', '{}', 'SUCCESS', 0, NOW(), NOW()),
    (4, 2, 1, 'META', 'https://api.example.com', '/prices', '{}', 'FAILED', 1, NOW(), NOW()),
    (5, 2, 1, 'META', 'https://api.example.com', '/prices', '{}', 'TIMEOUT', 0, NOW(), NOW()),
    (6, 3, 2, 'META', 'https://api.seller2.com', '/inventory', '{}', 'WAITING', 0, NOW(), NOW()),
    (7, 1, 1, 'META', 'https://api.example.com', '/slow', '{}', 'PUBLISHED', 0, NOW(), NOW()),
    (8, 1, 1, 'META', 'https://api.example.com', '/error', '{}', 'PUBLISHED', 0, NOW(), NOW())
""");
    }

    /** Task 관련 테스트에 필요한 모든 데이터 삽입 (Seller → Scheduler → Task 순서로 삽입) */
    public void insertTaskTestData() {
        insertSellers();
        insertSchedulers();
        insertTasks();
    }

    /** CrawlExecution 테스트 데이터 삽입 (Task 데이터 필요) */
    public void insertExecutions() {
        jdbcTemplate.execute(
                """
INSERT INTO crawl_execution (id, crawl_task_id, crawl_scheduler_id, seller_id, status, response_body, http_status_code, error_message, started_at, completed_at, duration_ms, created_at)
VALUES
    (1, 1, 1, 1, 'SUCCESS', '{"products": []}', 200, NULL, DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 2 HOUR), 1500, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
    (2, 2, 1, 1, 'RUNNING', NULL, NULL, NULL, NOW(), NULL, NULL, NOW()),
    (3, 3, 1, 1, 'SUCCESS', '{"options": []}', 200, NULL, DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR), 800, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
    (4, 4, 2, 1, 'FAILED', NULL, 500, 'Connection timeout', DATE_SUB(NOW(), INTERVAL 30 MINUTE), DATE_SUB(NOW(), INTERVAL 30 MINUTE), 30000, DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
    (5, 5, 2, 1, 'TIMEOUT', NULL, NULL, 'Request timeout after 30s', DATE_SUB(NOW(), INTERVAL 15 MINUTE), DATE_SUB(NOW(), INTERVAL 15 MINUTE), 30000, DATE_SUB(NOW(), INTERVAL 15 MINUTE)),
    (6, 6, 3, 2, 'SUCCESS', '{"inventory": []}', 200, NULL, DATE_SUB(NOW(), INTERVAL 3 HOUR), DATE_SUB(NOW(), INTERVAL 3 HOUR), 2000, DATE_SUB(NOW(), INTERVAL 3 HOUR))
""");
    }

    /** Execution 관련 테스트에 필요한 모든 데이터 삽입 */
    public void insertExecutionTestData() {
        insertTaskTestData();
        insertExecutions();
    }

    /** UserAgent 테스트 데이터 삽입 */
    public void insertUserAgents() {
        jdbcTemplate.execute(
                """
INSERT INTO user_agent (id, token, user_agent_string, device_type, status, health_score, last_used_at, requests_per_day, created_at, updated_at)
VALUES
    (1, 'test-token-001', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', 'DESKTOP', 'AVAILABLE', 100, NULL, 0, NOW(), NOW()),
    (2, 'test-token-002', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36', 'DESKTOP', 'AVAILABLE', 95, NULL, 0, NOW(), NOW()),
    (3, 'test-token-003', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/119.0', 'DESKTOP', 'SUSPENDED', 50, NOW(), 100, NOW(), NOW()),
    (4, 'test-token-004', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15', 'DESKTOP', 'BLOCKED', 0, NOW(), 500, NOW(), NOW())
""");
    }

    /**
     * CrawledProduct 테스트 데이터 삽입 (Seller 데이터 필요)
     *
     * <p>다양한 상태의 CrawledProduct를 생성합니다:
     *
     * <ul>
     *   <li>id=1: seller1, 모든 크롤링 완료, needs_sync=true (동기화 가능)
     *   <li>id=2: seller1, 모든 크롤링 완료, 외부 서버 등록됨, needs_sync=false
     *   <li>id=3: seller1, MINI_SHOP만 완료, needs_sync=false (동기화 불가)
     *   <li>id=4: seller2, 모든 크롤링 완료, needs_sync=true
     *   <li>id=5: seller2, MINI_SHOP, DETAIL 완료, OPTION 미완료
     * </ul>
     */
    public void insertCrawledProducts() {
        jdbcTemplate.execute(
                """
INSERT INTO crawled_product (
    id, seller_id, item_no, item_name, brand_name,
    original_price, discount_price, discount_rate,
    images_json, free_shipping,
    category_json, shipping_info_json, description_mark_up, original_description_mark_up,
    item_status, origin_country, shipping_location, options_json,
    mini_shop_crawled_at, detail_crawled_at, option_crawled_at,
    external_product_id, last_synced_at, needs_sync,
    created_at, updated_at
)
VALUES
    -- 1: seller1, 모든 크롤링 완료, 동기화 필요
    (1, 1, 10001, '테스트 상품 1', '테스트 브랜드 A',
     100000, 80000, 20,
     '{"thumbnails":["https://example.com/img1.jpg"],"descriptions":[]}', 1,
     '{"categoryId":100,"categoryName":"의류"}', '{"deliveryFee":3000,"freeShippingThreshold":50000}',
     '<p>상품 상세 설명</p>', '<p>상품 상세 설명</p>',
     'ACTIVE', '대한민국', '서울',
     '{"options":[{"optionName":"사이즈","optionValue":"M","stock":10,"price":0}]}',
     DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_SUB(NOW(), INTERVAL 30 MINUTE),
     NULL, NULL, 1,
     DATE_SUB(NOW(), INTERVAL 3 HOUR), NOW()),

    -- 2: seller1, 모든 크롤링 완료, 외부 서버 등록됨
    (2, 1, 10002, '테스트 상품 2', '테스트 브랜드 A',
     50000, 45000, 10,
     '{"thumbnails":["https://example.com/img2.jpg"],"descriptions":[]}', 0,
     '{"categoryId":200,"categoryName":"신발"}', '{"deliveryFee":0,"freeShippingThreshold":0}',
     '<p>신발 상세 설명</p>', '<p>신발 상세 설명</p>',
     'ACTIVE', '중국', '인천',
     '{"options":[{"optionName":"사이즈","optionValue":"270","stock":5,"price":0}]}',
     DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY),
     99001, DATE_SUB(NOW(), INTERVAL 2 DAY), 0,
     DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),

    -- 3: seller1, MINI_SHOP만 완료 (동기화 불가)
    (3, 1, 10003, '테스트 상품 3', '테스트 브랜드 B',
     30000, NULL, NULL,
     '{"thumbnails":["https://example.com/img3.jpg"],"descriptions":[]}', 1,
     NULL, NULL, NULL, NULL,
     NULL, NULL, NULL, NULL,
     NOW(), NULL, NULL,
     NULL, NULL, 0,
     NOW(), NOW()),

    -- 4: seller2, 모든 크롤링 완료, 동기화 필요
    (4, 2, 20001, '셀러2 상품 1', '브랜드 C',
     200000, 150000, 25,
     '{"thumbnails":["https://example.com/img4.jpg"],"descriptions":[]}', 1,
     '{"categoryId":300,"categoryName":"가방"}', '{"deliveryFee":5000,"freeShippingThreshold":100000}',
     '<p>가방 상세 설명</p>', '<p>가방 상세 설명</p>',
     'ACTIVE', '이탈리아', '부산',
     '{"options":[{"optionName":"색상","optionValue":"블랙","stock":3,"price":0}]}',
     DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 12 HOUR), DATE_SUB(NOW(), INTERVAL 6 HOUR),
     NULL, NULL, 1,
     DATE_SUB(NOW(), INTERVAL 2 DAY), NOW()),

    -- 5: seller2, MINI_SHOP, DETAIL 완료, OPTION 미완료
    (5, 2, 20002, '셀러2 상품 2', '브랜드 D',
     75000, 60000, 20,
     '{"thumbnails":["https://example.com/img5.jpg"],"descriptions":[]}', 0,
     '{"categoryId":400,"categoryName":"액세서리"}', '{"deliveryFee":2500,"freeShippingThreshold":30000}',
     '<p>액세서리 상세 설명</p>', '<p>액세서리 상세 설명</p>',
     'ACTIVE', '대한민국', '서울',
     NULL,
     DATE_SUB(NOW(), INTERVAL 4 HOUR), DATE_SUB(NOW(), INTERVAL 2 HOUR), NULL,
     NULL, NULL, 0,
     DATE_SUB(NOW(), INTERVAL 5 HOUR), DATE_SUB(NOW(), INTERVAL 2 HOUR))
""");
    }

    /** CrawledProduct 관련 테스트에 필요한 모든 데이터 삽입 */
    public void insertCrawledProductTestData() {
        insertSellers();
        insertCrawledProducts();
    }

    /**
     * ProductSyncOutbox 테스트 데이터 삽입 (Seller, CrawledProduct 데이터 필요)
     *
     * <p>다양한 상태의 SyncOutbox를 생성합니다:
     *
     * <ul>
     *   <li>id=1: PENDING 상태, 재시도 0회
     *   <li>id=2: PROCESSING 상태
     *   <li>id=3: COMPLETED 상태
     *   <li>id=4: FAILED 상태, 재시도 1회 (재시도 가능)
     *   <li>id=5: FAILED 상태, 재시도 3회 (재시도 불가)
     * </ul>
     */
    public void insertProductSyncOutbox() {
        jdbcTemplate.execute(
                """
INSERT INTO product_sync_outbox (id, crawled_product_id, seller_id, item_no, sync_type, idempotency_key, external_product_id, status, retry_count, error_message, created_at, processed_at)
VALUES
    (1, 1, 1, 10001, 'CREATE', 'sync-1-10001-1000', NULL, 'PENDING', 0, NULL, NOW(), NULL),
    (2, 2, 1, 10002, 'CREATE', 'sync-1-10002-1001', NULL, 'PROCESSING', 0, NULL, NOW(), NULL),
    (3, 4, 2, 20001, 'CREATE', 'sync-2-20001-1002', 99001, 'COMPLETED', 0, NULL, DATE_SUB(NOW(), INTERVAL 1 HOUR), NOW()),
    (4, 1, 1, 10001, 'UPDATE', 'sync-1-10001-1003', NULL, 'FAILED', 1, 'Connection timeout', DATE_SUB(NOW(), INTERVAL 30 MINUTE), DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
    (5, 4, 2, 20001, 'UPDATE', 'sync-2-20001-1004', NULL, 'FAILED', 3, 'Max retry exceeded', DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 2 HOUR))
""");
    }

    /**
     * CrawledProductImage 테스트 데이터 삽입 (CrawledProduct 데이터 필요)
     *
     * <p>V9에서 분리된 crawled_product_image 테이블에 이미지 데이터를 삽입합니다.
     *
     * <ul>
     *   <li>id=1: product 1, THUMBNAIL
     *   <li>id=2: product 1, DESCRIPTION
     *   <li>id=3: product 2, THUMBNAIL (S3 업로드 완료)
     *   <li>id=4: product 1, THUMBNAIL (두 번째)
     *   <li>id=5: product 4, DESCRIPTION
     * </ul>
     */
    public void insertCrawledProductImages() {
        jdbcTemplate.execute(
                """
INSERT INTO crawled_product_image (id, crawled_product_id, original_url, s3_url, file_asset_id, image_type, display_order, created_at, updated_at)
VALUES
    (1, 1, 'https://example.com/img1.jpg', NULL, NULL, 'THUMBNAIL', 0, NOW(), NOW()),
    (2, 1, 'https://example.com/desc1.jpg', NULL, NULL, 'DESCRIPTION', 0, NOW(), NOW()),
    (3, 2, 'https://example.com/img2.jpg', 'https://s3.example.com/img2.jpg', 'asset-001', 'THUMBNAIL', 0, DATE_SUB(NOW(), INTERVAL 1 HOUR), NOW()),
    (4, 1, 'https://example.com/img3.jpg', NULL, NULL, 'THUMBNAIL', 1, NOW(), NOW()),
    (5, 4, 'https://example.com/desc4.jpg', NULL, NULL, 'DESCRIPTION', 0, NOW(), NOW())
""");
    }

    /**
     * ProductImageOutbox 테스트 데이터 삽입 (CrawledProductImage 데이터 필요)
     *
     * <p>다양한 상태의 ImageOutbox를 생성합니다. V11에서 컬럼이 변경되어 crawled_product_image_id를 참조합니다.
     *
     * <ul>
     *   <li>id=1: PENDING 상태, image 1 참조
     *   <li>id=2: PROCESSING 상태, image 2 참조
     *   <li>id=3: COMPLETED 상태, image 3 참조
     *   <li>id=4: FAILED 상태, 재시도 1회 (재시도 가능), image 4 참조
     *   <li>id=5: FAILED 상태, 재시도 3회 (재시도 불가), image 5 참조
     * </ul>
     */
    public void insertProductImageOutbox() {
        jdbcTemplate.execute(
                """
INSERT INTO product_image_outbox (id, crawled_product_image_id, idempotency_key, status, retry_count, error_message, created_at, processed_at)
VALUES
    (1, 1, 'img-1-thumb-1000', 'PENDING', 0, NULL, NOW(), NULL),
    (2, 2, 'img-1-desc-1001', 'PROCESSING', 0, NULL, NOW(), NULL),
    (3, 3, 'img-2-thumb-1002', 'COMPLETED', 0, NULL, DATE_SUB(NOW(), INTERVAL 1 HOUR), NOW()),
    (4, 4, 'img-1-thumb-1003', 'FAILED', 1, 'Upload failed', DATE_SUB(NOW(), INTERVAL 30 MINUTE), DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
    (5, 5, 'img-4-desc-1004', 'FAILED', 3, 'Max retry exceeded', DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 2 HOUR))
""");
    }

    /** ProductOutbox 관련 테스트에 필요한 모든 데이터 삽입 */
    public void insertProductOutboxTestData() {
        insertCrawledProductTestData();
        insertCrawledProductImages();
        insertProductSyncOutbox();
        insertProductImageOutbox();
    }

    /**
     * Redis UserAgent Pool 워밍업
     *
     * <p>테스트용 UserAgent를 READY 상태로 Redis Pool에 추가합니다. MySQL에 UserAgent 데이터를 먼저 삽입한 후 호출해야 합니다.
     *
     * <p>UserAgentPoolCacheManager가 주입되지 않은 경우 (Web API 테스트 등) 무시됩니다.
     *
     * <p><strong>중요</strong>: addToPool()은 항상 SESSION_REQUIRED 상태로 추가합니다. 따라서 warmUp() 후
     * updateSession()을 호출하여 READY 상태로 변경해야 합니다.
     *
     * @param count 추가할 UserAgent 수 (기본 2개 사용 권장)
     */
    public void warmUpUserAgentPool(int count) {
        if (userAgentPoolCacheManager == null || userAgentPoolCachePort == null) {
            return;
        }

        List<CachedUserAgent> cachedUserAgents = new ArrayList<>();
        Instant now = Instant.now();
        Instant sessionExpiresAt = now.plusSeconds(3600); // 1시간 후 만료
        Instant windowEnd = now.plusSeconds(600); // 10분 윈도우

        for (int i = 1; i <= count; i++) {
            CachedUserAgent cachedUserAgent =
                    new CachedUserAgent(
                            (long) i, // userAgentId (MySQL의 user_agent.id와 일치)
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
                                    + " Test-Agent-"
                                    + i, // userAgentValue
                            "test-session-token-" + i, // sessionToken (세션 발급됨)
                            "test-nid-" + i, // nid 쿠키
                            "test-mustit-uid-" + i, // mustitUid 쿠키
                            sessionExpiresAt, // sessionExpiresAt
                            80, // remainingTokens (최대값)
                            80, // maxTokens
                            now, // windowStart
                            windowEnd, // windowEnd
                            100, // healthScore (최대값)
                            CacheStatus.READY, // READY 상태 (즉시 사용 가능)
                            null // suspendedAt (서스펜드되지 않음)
                            );
            cachedUserAgents.add(cachedUserAgent);
        }

        // warmUp()은 addToPool()을 호출하며, addToPool()은 항상 SESSION_REQUIRED 상태로 추가
        userAgentPoolCacheManager.warmUp(cachedUserAgents);

        // updateSession()을 호출하여 READY 상태로 변경 (세션 토큰 설정)
        for (int i = 1; i <= count; i++) {
            userAgentPoolCachePort.updateSession(
                    UserAgentId.of((long) i), "test-session-token-" + i, sessionExpiresAt);
        }
    }

    /**
     * Redis UserAgent Pool 워밍업 (기본 2개)
     *
     * <p>테스트용 UserAgent 2개를 READY 상태로 Redis Pool에 추가합니다.
     */
    public void warmUpUserAgentPool() {
        warmUpUserAgentPool(2);
    }

    /**
     * Worker 통합 테스트용 전체 데이터 삽입
     *
     * <p>MySQL 데이터 삽입 + Redis Pool 워밍업을 한 번에 처리합니다.
     *
     * <ul>
     *   <li>MySQL: Seller, Scheduler, Task, UserAgent 데이터
     *   <li>Redis: READY 상태의 UserAgent Pool
     * </ul>
     */
    public void insertWorkerTestData() {
        insertTaskTestData();
        insertUserAgents();
        warmUpUserAgentPool();
    }
}
