-- V23__drop_unused_user_agent_columns.sql
-- user_agent 테이블에서 사용하지 않는 컬럼 제거
-- token: 항상 NULL (Redis에서 관리, DB에 저장하지 않음)
-- last_used_at: 항상 NULL (Redis에서만 관리)
-- requests_per_day: 항상 0 (Redis에서만 관리)

ALTER TABLE user_agent DROP COLUMN token;
ALTER TABLE user_agent DROP COLUMN last_used_at;
ALTER TABLE user_agent DROP COLUMN requests_per_day;
