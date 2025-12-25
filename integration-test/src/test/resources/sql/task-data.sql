-- ============================================
-- CrawlTask Test Data
-- ============================================
-- Note: MAX_RETRY_COUNT = 2, canRetry() = value < 2
-- So retry_count must be 0 or 1 for a task to be retryable

INSERT INTO crawl_task (id, crawl_scheduler_id, seller_id, task_type, endpoint_base_url, endpoint_path, endpoint_query_params, status, retry_count, created_at, updated_at)
VALUES
    (1, 1, 1, 'META', 'https://api.example.com', '/products', '{"page": "1", "size": "100"}', 'WAITING', 0, NOW(), NOW()),
    (2, 1, 1, 'DETAIL', 'https://api.example.com', '/products/123', '{}', 'RUNNING', 0, NOW(), NOW()),
    (3, 1, 1, 'OPTION', 'https://api.example.com', '/products/123/options', '{}', 'SUCCESS', 0, NOW(), NOW()),
    (4, 2, 1, 'META', 'https://api.example.com', '/prices', '{}', 'FAILED', 1, NOW(), NOW()),
    (5, 2, 1, 'META', 'https://api.example.com', '/prices', '{}', 'TIMEOUT', 0, NOW(), NOW()),
    (6, 3, 2, 'META', 'https://api.seller2.com', '/inventory', '{}', 'WAITING', 0, NOW(), NOW());
