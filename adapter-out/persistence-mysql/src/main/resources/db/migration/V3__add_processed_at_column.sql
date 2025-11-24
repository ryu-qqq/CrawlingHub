-- crawl_scheduler_outbox 테이블에 processed_at 컬럼 추가
ALTER TABLE crawl_scheduler_outbox
ADD COLUMN processed_at TIMESTAMP NULL COMMENT '처리 일시';
