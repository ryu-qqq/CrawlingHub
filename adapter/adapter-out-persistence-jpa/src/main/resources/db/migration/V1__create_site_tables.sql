-- ========================================
-- V1: Site 관련 테이블 (6개)
-- ========================================
-- 크롤링 사이트 기본 정보 및 설정 테이블
-- ⚠️ FOREIGN KEY 제약조건 없음 (애플리케이션 레벨에서 관리)
-- ✅ INDEX는 필수 (조회 성능 최적화)
-- ========================================

-- ========================================
-- 1. crawl_site (크롤링 사이트 기본 정보)
-- ========================================
CREATE TABLE crawl_site (
    site_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '사이트 고유 ID',
    site_name VARCHAR(200) NOT NULL COMMENT '사이트 이름',
    base_url VARCHAR(500) NOT NULL COMMENT '기본 URL',
    site_type VARCHAR(50) NOT NULL COMMENT '사이트 타입 (API, WEB, HYBRID 등)',
    description TEXT COMMENT '사이트 설명',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '활성화 여부',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정 시각',
    PRIMARY KEY (site_id),
    INDEX idx_site_name (site_name),
    INDEX idx_site_type (site_type),
    INDEX idx_is_active (is_active),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='크롤링 사이트 기본 정보';

-- ========================================
-- 2. site_api_endpoint (API 엔드포인트 정보)
-- ========================================
CREATE TABLE site_api_endpoint (
    endpoint_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '엔드포인트 고유 ID',
    site_id BIGINT NOT NULL COMMENT 'crawl_site.site_id 참조',
    endpoint_name VARCHAR(200) NOT NULL COMMENT '엔드포인트 이름',
    endpoint_url VARCHAR(500) NOT NULL COMMENT '엔드포인트 URL',
    http_method VARCHAR(10) NOT NULL DEFAULT 'GET' COMMENT 'HTTP 메서드 (GET, POST, PUT, DELETE 등)',
    request_body_template TEXT COMMENT '요청 본문 템플릿 (JSON 등)',
    description TEXT COMMENT '엔드포인트 설명',
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '활성화 여부',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정 시각',
    PRIMARY KEY (endpoint_id),
    INDEX idx_site_id (site_id),
    INDEX idx_endpoint_name (endpoint_name),
    INDEX idx_http_method (http_method),
    INDEX idx_is_enabled (is_enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='API 엔드포인트 정보';

-- ========================================
-- 3. site_api_header (API 헤더 정보)
-- ========================================
CREATE TABLE site_api_header (
    header_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '헤더 고유 ID',
    site_id BIGINT NOT NULL COMMENT 'crawl_site.site_id 참조',
    endpoint_id BIGINT COMMENT 'site_api_endpoint.endpoint_id 참조 (NULL이면 사이트 전체 적용)',
    header_key VARCHAR(200) NOT NULL COMMENT '헤더 키',
    header_value TEXT NOT NULL COMMENT '헤더 값',
    is_sensitive BOOLEAN NOT NULL DEFAULT FALSE COMMENT '민감 정보 여부 (암호화 필요)',
    description VARCHAR(500) COMMENT '헤더 설명',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정 시각',
    PRIMARY KEY (header_id),
    INDEX idx_site_id (site_id),
    INDEX idx_endpoint_id (endpoint_id),
    INDEX idx_header_key (header_key),
    INDEX idx_is_sensitive (is_sensitive)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='API 헤더 정보';

-- ========================================
-- 4. site_auth_config (사이트 인증 설정)
-- ========================================
CREATE TABLE site_auth_config (
    auth_config_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '인증 설정 고유 ID',
    site_id BIGINT NOT NULL COMMENT 'crawl_site.site_id 참조',
    auth_type VARCHAR(50) NOT NULL COMMENT '인증 타입 (NONE, BASIC, BEARER, OAUTH2, API_KEY 등)',
    auth_credential TEXT COMMENT '인증 자격증명 (암호화 저장)',
    token_endpoint VARCHAR(500) COMMENT '토큰 발급 엔드포인트',
    refresh_token_endpoint VARCHAR(500) COMMENT '토큰 갱신 엔드포인트',
    token_expiry_seconds INT COMMENT '토큰 만료 시간 (초)',
    additional_params JSON COMMENT '추가 인증 파라미터 (JSON 형식)',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '활성화 여부',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정 시각',
    PRIMARY KEY (auth_config_id),
    INDEX idx_site_id (site_id),
    INDEX idx_auth_type (auth_type),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사이트 인증 설정';

-- ========================================
-- 5. site_rate_limit_config (Rate Limit 설정)
-- ========================================
CREATE TABLE site_rate_limit_config (
    rate_limit_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Rate Limit 설정 고유 ID',
    site_id BIGINT NOT NULL COMMENT 'crawl_site.site_id 참조',
    max_requests_per_second INT COMMENT '초당 최대 요청 수',
    max_requests_per_minute INT COMMENT '분당 최대 요청 수',
    max_requests_per_hour INT COMMENT '시간당 최대 요청 수',
    max_concurrent_requests INT COMMENT '최대 동시 요청 수',
    backoff_strategy VARCHAR(50) COMMENT '백오프 전략 (FIXED, EXPONENTIAL, LINEAR 등)',
    backoff_base_delay_ms INT COMMENT '백오프 기본 지연 시간 (밀리초)',
    backoff_max_delay_ms INT COMMENT '백오프 최대 지연 시간 (밀리초)',
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '활성화 여부',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정 시각',
    PRIMARY KEY (rate_limit_id),
    INDEX idx_site_id (site_id),
    INDEX idx_is_enabled (is_enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Rate Limit 설정';

-- ========================================
-- 6. site_retry_policy (재시도 정책)
-- ========================================
CREATE TABLE site_retry_policy (
    retry_policy_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '재시도 정책 고유 ID',
    site_id BIGINT NOT NULL COMMENT 'crawl_site.site_id 참조',
    max_retry_attempts INT NOT NULL DEFAULT 3 COMMENT '최대 재시도 횟수',
    retry_delay_ms INT NOT NULL DEFAULT 1000 COMMENT '재시도 지연 시간 (밀리초)',
    retry_strategy VARCHAR(50) NOT NULL DEFAULT 'EXPONENTIAL' COMMENT '재시도 전략 (FIXED, EXPONENTIAL, LINEAR 등)',
    retry_on_status_codes JSON COMMENT '재시도할 HTTP 상태 코드 (JSON 배열)',
    retry_on_exceptions JSON COMMENT '재시도할 예외 타입 (JSON 배열)',
    timeout_ms INT NOT NULL DEFAULT 30000 COMMENT '요청 타임아웃 (밀리초)',
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '활성화 여부',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정 시각',
    PRIMARY KEY (retry_policy_id),
    INDEX idx_site_id (site_id),
    INDEX idx_retry_strategy (retry_strategy),
    INDEX idx_is_enabled (is_enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='재시도 정책';
