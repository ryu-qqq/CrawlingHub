-- V14__add_useragent_metadata_columns.sql
-- user_agent 테이블에 메타데이터 컬럼 추가 (DeviceBrand, OsType, BrowserType 정보)

-- =====================================================
-- 1. device_brand 컬럼 추가
-- =====================================================
ALTER TABLE user_agent
ADD COLUMN device_brand VARCHAR(30) NULL COMMENT '디바이스 브랜드 (IPHONE/SAMSUNG/PIXEL/XIAOMI/HUAWEI/OPPO/ONEPLUS/IPAD/GALAXY_TAB/GENERIC)'
AFTER device_type;

-- =====================================================
-- 2. os_type 컬럼 추가
-- =====================================================
ALTER TABLE user_agent
ADD COLUMN os_type VARCHAR(20) NULL COMMENT 'OS 타입 (WINDOWS/MACOS/LINUX/IOS/ANDROID/CHROME_OS)'
AFTER device_brand;

-- =====================================================
-- 3. os_version 컬럼 추가
-- =====================================================
ALTER TABLE user_agent
ADD COLUMN os_version VARCHAR(50) NULL COMMENT 'OS 버전 (예: 17.0, 10.15.7, 14)'
AFTER os_type;

-- =====================================================
-- 4. browser_type 컬럼 추가
-- =====================================================
ALTER TABLE user_agent
ADD COLUMN browser_type VARCHAR(30) NULL COMMENT '브라우저 타입 (CHROME/SAFARI/FIREFOX/EDGE/OPERA/SAMSUNG_BROWSER)'
AFTER os_version;

-- =====================================================
-- 5. browser_version 컬럼 추가
-- =====================================================
ALTER TABLE user_agent
ADD COLUMN browser_version VARCHAR(50) NULL COMMENT '브라우저 버전 (예: 120.0.0.0, 17.0)'
AFTER browser_type;

-- =====================================================
-- 6. 기존 데이터 마이그레이션 (기본값 설정)
-- =====================================================
-- 기존 행에 대해 기본값 설정
-- 실제 값은 Application에서 user_agent_string을 파싱하여 업데이트할 예정
UPDATE user_agent
SET device_brand = 'GENERIC',
    os_type = 'WINDOWS',
    browser_type = 'CHROME'
WHERE device_brand IS NULL;

-- =====================================================
-- 7. NOT NULL 제약조건 적용
-- =====================================================
ALTER TABLE user_agent
MODIFY COLUMN device_brand VARCHAR(30) NOT NULL COMMENT '디바이스 브랜드 (IPHONE/SAMSUNG/PIXEL/XIAOMI/HUAWEI/OPPO/ONEPLUS/IPAD/GALAXY_TAB/GENERIC)';

ALTER TABLE user_agent
MODIFY COLUMN os_type VARCHAR(20) NOT NULL COMMENT 'OS 타입 (WINDOWS/MACOS/LINUX/IOS/ANDROID/CHROME_OS)';

ALTER TABLE user_agent
MODIFY COLUMN browser_type VARCHAR(30) NOT NULL COMMENT '브라우저 타입 (CHROME/SAFARI/FIREFOX/EDGE/OPERA/SAMSUNG_BROWSER)';

-- os_version, browser_version은 NULL 허용 (파싱 실패 가능)

-- =====================================================
-- 8. 인덱스 추가 (검색 성능 최적화)
-- =====================================================
CREATE INDEX idx_user_agent_device_brand ON user_agent (device_brand);
CREATE INDEX idx_user_agent_os_type ON user_agent (os_type);
CREATE INDEX idx_user_agent_browser_type ON user_agent (browser_type);
