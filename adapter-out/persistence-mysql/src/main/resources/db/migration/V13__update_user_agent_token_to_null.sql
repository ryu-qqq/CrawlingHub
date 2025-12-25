-- V13: Lazy Token Issuance 전략 - 기존 token 데이터 정리
-- 기존 PLACEHOLDER_TOKEN 또는 유효하지 않은 token을 NULL로 변경

UPDATE user_agent SET token = NULL WHERE token IS NOT NULL;
