-- V15__alter_useragent_token_nullable.sql
-- UserAgent token 컬럼을 nullable로 변경 (Lazy Token Issuance 지원)
--
-- 변경 사유:
-- - Lazy Token Issuance 패턴 도입
-- - UserAgent 등록 시 토큰 없이 생성 가능
-- - 토큰은 필요한 시점에 별도로 발급
--
-- 롤백:
-- ALTER TABLE user_agent MODIFY COLUMN token VARCHAR(500) NOT NULL;

ALTER TABLE user_agent MODIFY COLUMN token VARCHAR(500) NULL;

-- 인덱스 추가: 토큰 미발급 UserAgent 조회용 (선택적)
-- CREATE INDEX idx_user_agent_token_null ON user_agent (token) WHERE token IS NULL;
