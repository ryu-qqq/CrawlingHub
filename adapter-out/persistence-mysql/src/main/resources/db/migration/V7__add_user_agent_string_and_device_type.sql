-- V7__add_user_agent_string_and_device_type.sql
-- user_agent 테이블에 user_agent_string, device_type 컬럼 추가

-- =====================================================
-- 1. user_agent_string 컬럼 추가
-- =====================================================
ALTER TABLE user_agent
ADD COLUMN user_agent_string VARCHAR(500) NULL COMMENT 'User-Agent 헤더 문자열 (실제 User-Agent 값)'
AFTER token;

-- =====================================================
-- 2. device_type 컬럼 추가
-- =====================================================
ALTER TABLE user_agent
ADD COLUMN device_type VARCHAR(20) NULL COMMENT '디바이스 타입 (MOBILE/TABLET/DESKTOP)'
AFTER user_agent_string;

-- =====================================================
-- 3. 기존 데이터 마이그레이션 (기본값 설정)
-- =====================================================
-- 기존 데이터에 대해 기본값 설정
-- user_agent_string: 토큰 값을 임시로 사용 (추후 실제 User-Agent로 업데이트 필요)
-- device_type: DESKTOP으로 기본 설정
UPDATE user_agent
SET user_agent_string = CONCAT('Mozilla/5.0 (Migrated from token: ', SUBSTRING(token, 1, 20), '...)'),
    device_type = 'DESKTOP'
WHERE user_agent_string IS NULL;

-- =====================================================
-- 4. NOT NULL 제약조건 추가
-- =====================================================
ALTER TABLE user_agent
MODIFY COLUMN user_agent_string VARCHAR(500) NOT NULL COMMENT 'User-Agent 헤더 문자열 (실제 User-Agent 값)';

ALTER TABLE user_agent
MODIFY COLUMN device_type VARCHAR(20) NOT NULL COMMENT '디바이스 타입 (MOBILE/TABLET/DESKTOP)';

-- =====================================================
-- 5. 인덱스 추가 (선택적 - 디바이스 타입으로 조회가 필요한 경우)
-- =====================================================
CREATE INDEX idx_user_agent_device_type ON user_agent (device_type);
