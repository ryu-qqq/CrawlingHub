-- =====================================================
-- V10: 유연한 크롤링 스키마 재설계
-- Author: Crawling Hub Team
-- Date: 2024-01-20
-- Description: 범용적이고 확장 가능한 크롤링 시스템 스키마
-- =====================================================

-- =====================================================
-- 1. 범용 크롤링 소스 및 타겟
-- =====================================================

-- 크롤링 소스 (사이트, API 등)
CREATE TABLE IF NOT EXISTS crawling_sources (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_code VARCHAR(50) NOT NULL COMMENT '소스 고유 코드',
    name VARCHAR(100) NOT NULL COMMENT '소스 이름',
    base_url VARCHAR(500) COMMENT '기본 URL',
    api_base_url VARCHAR(500) COMMENT 'API 기본 URL',
    source_type ENUM('WEB', 'API', 'RSS', 'FILE', 'DATABASE') NOT NULL COMMENT '소스 타입',
    status ENUM('ACTIVE', 'INACTIVE', 'MAINTENANCE') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    UNIQUE KEY uk_source_code (source_code),
    INDEX idx_sources_status (status, deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='크롤링 소스 정의';

-- 크롤링 대상 타입 정의
CREATE TABLE IF NOT EXISTS target_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type_code VARCHAR(50) NOT NULL COMMENT '타입 코드 (PRODUCT, BRAND, NEWS 등)',
    type_name VARCHAR(100) NOT NULL COMMENT '타입 이름',
    description TEXT COMMENT '타입 설명',
    parent_type_id BIGINT NULL COMMENT '상위 타입 (계층 구조)',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_type_code (type_code),
    FOREIGN KEY fk_parent_type (parent_type_id) REFERENCES target_types(id),
    INDEX idx_types_active (is_active),
    INDEX idx_types_hierarchy (parent_type_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='크롤링 대상 타입 메타데이터';

-- 크롤링 대상
CREATE TABLE IF NOT EXISTS crawling_targets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_id BIGINT NOT NULL COMMENT '소스 ID',
    type_id BIGINT NOT NULL COMMENT '타입 ID',
    target_code VARCHAR(200) NOT NULL COMMENT '대상 고유 코드',
    target_name VARCHAR(500) COMMENT '대상 이름',
    target_url VARCHAR(1000) COMMENT '대상 URL',
    parent_target_id BIGINT NULL COMMENT '상위 대상 (계층 구조)',
    status ENUM('ACTIVE', 'INACTIVE', 'ARCHIVED') DEFAULT 'ACTIVE',
    crawl_priority INT DEFAULT 5 COMMENT '크롤링 우선순위 (1-10)',
    crawl_interval_hours INT DEFAULT 24 COMMENT '크롤링 주기 (시간)',
    last_crawled_at TIMESTAMP NULL COMMENT '마지막 크롤링 시간',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    FOREIGN KEY fk_target_source (source_id) REFERENCES crawling_sources(id),
    FOREIGN KEY fk_target_type (type_id) REFERENCES target_types(id),
    FOREIGN KEY fk_parent_target (parent_target_id) REFERENCES crawling_targets(id),
    UNIQUE KEY uk_target (source_id, type_id, target_code),
    INDEX idx_targets_crawl (status, last_crawled_at, crawl_interval_hours),
    INDEX idx_targets_hierarchy (parent_target_id),
    INDEX idx_targets_priority (crawl_priority, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='크롤링 대상';

-- =====================================================
-- 2. 속성 및 데이터 저장 (EAV 패턴)
-- =====================================================

-- 타겟 속성 정의
CREATE TABLE IF NOT EXISTS target_attributes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type_id BIGINT NOT NULL COMMENT '타입 ID',
    attribute_code VARCHAR(100) NOT NULL COMMENT '속성 코드',
    attribute_name VARCHAR(200) NOT NULL COMMENT '속성 이름',
    data_type ENUM('STRING', 'NUMBER', 'DATE', 'BOOLEAN', 'JSON', 'TEXT', 'URL', 'EMAIL') NOT NULL,
    is_required BOOLEAN DEFAULT FALSE COMMENT '필수 여부',
    is_indexed BOOLEAN DEFAULT FALSE COMMENT '인덱싱 여부',
    is_unique BOOLEAN DEFAULT FALSE COMMENT '유니크 여부',
    validation_pattern VARCHAR(500) COMMENT '유효성 검사 정규식',
    min_value DECIMAL(20,6) NULL COMMENT '최소값 (숫자형)',
    max_value DECIMAL(20,6) NULL COMMENT '최대값 (숫자형)',
    default_value TEXT COMMENT '기본값',
    display_order INT DEFAULT 0 COMMENT '표시 순서',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY fk_attr_type (type_id) REFERENCES target_types(id),
    UNIQUE KEY uk_attribute (type_id, attribute_code),
    INDEX idx_attributes_type (type_id, display_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='타입별 속성 메타데이터';

-- 크롤링 데이터 버전 관리
CREATE TABLE IF NOT EXISTS crawling_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    target_id BIGINT NOT NULL COMMENT '대상 ID',
    job_id BIGINT NOT NULL COMMENT '작업 ID',
    crawled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '크롤링 시간',
    version_hash VARCHAR(64) NOT NULL COMMENT '데이터 버전 해시',
    data_size_bytes BIGINT DEFAULT 0 COMMENT '데이터 크기',
    s3_path VARCHAR(500) COMMENT 'S3 저장 경로',
    is_current BOOLEAN DEFAULT TRUE COMMENT '현재 버전 여부',
    change_type ENUM('CREATE', 'UPDATE', 'NO_CHANGE') DEFAULT 'CREATE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY fk_data_target (target_id) REFERENCES crawling_targets(id),
    FOREIGN KEY fk_data_job (job_id) REFERENCES crawling_jobs(id),
    INDEX idx_data_target (target_id, is_current, crawled_at DESC),
    INDEX idx_data_version (version_hash),
    INDEX idx_data_job (job_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='크롤링 데이터 버전 관리';

-- 크롤링 데이터 값 (EAV)
CREATE TABLE IF NOT EXISTS crawling_data_values (
    data_id BIGINT NOT NULL COMMENT '데이터 버전 ID',
    attribute_id BIGINT NOT NULL COMMENT '속성 ID',
    value_text TEXT COMMENT '텍스트 값',
    value_number DECIMAL(20,6) COMMENT '숫자 값',
    value_date DATETIME COMMENT '날짜 값',
    value_boolean BOOLEAN COMMENT '불린 값',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (data_id, attribute_id),
    FOREIGN KEY fk_value_data (data_id) REFERENCES crawling_data(id) ON DELETE CASCADE,
    FOREIGN KEY fk_value_attr (attribute_id) REFERENCES target_attributes(id),
    INDEX idx_values_attribute (attribute_id, data_id),
    INDEX idx_values_number (attribute_id, value_number),
    INDEX idx_values_date (attribute_id, value_date),
    INDEX idx_values_boolean (attribute_id, value_boolean)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='크롤링된 실제 데이터 값 (EAV)';

-- =====================================================
-- 3. 정규화된 설정 테이블
-- =====================================================

-- 소스별 인증 설정
CREATE TABLE IF NOT EXISTS source_auth_configs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_id BIGINT NOT NULL,
    auth_type ENUM('NONE', 'BASIC', 'BEARER', 'OAUTH', 'API_KEY', 'COOKIE', 'CUSTOM') NOT NULL,
    auth_name VARCHAR(100) COMMENT '인증 이름/설명',
    auth_key VARCHAR(100) COMMENT '인증 키 이름',
    auth_value VARBINARY(1000) COMMENT '인증 값 (암호화)',
    auth_location ENUM('HEADER', 'QUERY', 'BODY', 'COOKIE') DEFAULT 'HEADER',
    expires_at TIMESTAMP NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY fk_auth_source (source_id) REFERENCES crawling_sources(id),
    INDEX idx_auth_source (source_id, auth_type, is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='소스별 인증 설정';

-- HTTP 헤더 설정
CREATE TABLE IF NOT EXISTS crawling_headers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_id BIGINT NOT NULL,
    target_id BIGINT NULL COMMENT '특정 타겟에만 적용',
    header_name VARCHAR(100) NOT NULL,
    header_value TEXT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY fk_header_source (source_id) REFERENCES crawling_sources(id),
    FOREIGN KEY fk_header_target (target_id) REFERENCES crawling_targets(id),
    INDEX idx_headers_source (source_id, is_active),
    INDEX idx_headers_target (target_id, is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='HTTP 헤더 설정';

-- 크롤링 규칙 설정
CREATE TABLE IF NOT EXISTS crawling_rules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_id BIGINT NOT NULL,
    rule_type ENUM('DELAY', 'RETRY', 'TIMEOUT', 'RATE_LIMIT', 'PARALLELISM') NOT NULL,
    rule_name VARCHAR(100),
    rule_value VARCHAR(500) NOT NULL,
    rule_unit VARCHAR(20) COMMENT '단위 (ms, seconds, count 등)',
    apply_condition TEXT COMMENT '적용 조건',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY fk_rule_source (source_id) REFERENCES crawling_sources(id),
    INDEX idx_rules_source (source_id, rule_type, is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='크롤링 규칙 설정';
