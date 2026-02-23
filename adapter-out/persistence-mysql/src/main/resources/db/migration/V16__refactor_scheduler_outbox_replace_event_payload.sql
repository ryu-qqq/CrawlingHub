ALTER TABLE crawl_scheduler_outbox
    ADD COLUMN scheduler_id BIGINT NOT NULL DEFAULT 0 AFTER history_id,
    ADD COLUMN seller_id BIGINT NOT NULL DEFAULT 0 AFTER scheduler_id,
    ADD COLUMN scheduler_name VARCHAR(100) NOT NULL DEFAULT '' AFTER seller_id,
    ADD COLUMN cron_expression VARCHAR(100) NOT NULL DEFAULT '' AFTER scheduler_name,
    ADD COLUMN scheduler_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' AFTER cron_expression;

ALTER TABLE crawl_scheduler_outbox DROP COLUMN event_payload;
