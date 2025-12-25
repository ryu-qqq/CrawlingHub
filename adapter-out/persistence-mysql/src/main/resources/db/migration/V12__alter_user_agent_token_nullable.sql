-- V12: Lazy Token Issuance 전략 적용
-- token 컬럼을 nullable로 변경 (세션 토큰은 Redis에서 Lazy 발급)

ALTER TABLE user_agent
    MODIFY COLUMN token VARCHAR(500) NULL COMMENT 'AES-256 암호화된 토큰 (Lazy Token 전략으로 nullable)';
