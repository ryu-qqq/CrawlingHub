-- ============================================
-- CrawlScheduler Test Data
-- ============================================

INSERT INTO crawl_scheduler (id, seller_id, scheduler_name, cron_expression, status, created_at, updated_at)
VALUES
    (1, 1, 'daily-product-sync', '0 0 2 * * *', 'ACTIVE', NOW(), NOW()),
    (2, 1, 'hourly-price-check', '0 0 * * * *', 'ACTIVE', NOW(), NOW()),
    (3, 2, 'weekly-inventory', '0 0 0 * * SUN', 'INACTIVE', NOW(), NOW());
