-- ========================================
-- V7: Token & Rate Limiting 시스템 테이블 (7개)
-- ========================================
-- User-Agent 기반 토큰 관리 및 분산 레이트 리미팅 시스템
-- ⚠️ FOREIGN KEY 제약조건 없음 (성능 최적화, 애플리케이션 레벨에서 관리)
-- ✅ INDEX는 필수 (조회 성능 최적화)
-- 📊 파티셔닝: token_usage_log는 월별 파티셔닝 적용
-- ========================================

-- ========================================
-- 1. user_agent_pool (User-Agent 풀 관리)
-- ========================================
-- User-Agent 문자열 및 상태 관리
-- 100개 User-Agent 동시 운영으로 처리량 극대화
CREATE TABLE user_agent_pool (
    agent_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'User-Agent 고유 ID',
    user_agent VARCHAR(500) NOT NULL COMMENT 'User-Agent 문자열',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '활성화 여부',
    is_blocked BOOLEAN NOT NULL DEFAULT FALSE COMMENT '차단 여부 (429 에러 발생 시)',
    blocked_until DATETIME(6) COMMENT '차단 해제 시각 (Circuit Breaker)',
    last_used_at DATETIME(6) COMMENT '마지막 사용 시각',
    usage_count INT NOT NULL DEFAULT 0 COMMENT '총 사용 횟수',
    success_count INT NOT NULL DEFAULT 0 COMMENT '성공 횟수',
    failure_count INT NOT NULL DEFAULT 0 COMMENT '실패 횟수',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정 시각',
    PRIMARY KEY (agent_id),
    UNIQUE KEY uk_user_agent (user_agent(255)),
    INDEX idx_active_unblocked (is_active, is_blocked, blocked_until),
    INDEX idx_last_used (last_used_at),
    INDEX idx_usage_stats (usage_count, success_count, failure_count)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User-Agent 풀 관리';

-- ========================================
-- 2. user_agent_token (User-Agent별 토큰 정보)
-- ========================================
-- User-Agent와 토큰 매핑 및 라이프사이클 관리
CREATE TABLE user_agent_token (
    token_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '토큰 고유 ID',
    agent_id BIGINT NOT NULL COMMENT 'user_agent_pool.agent_id 참조',
    token_value VARCHAR(1000) NOT NULL COMMENT '토큰 값 (암호화 저장 권장)',
    token_type VARCHAR(50) NOT NULL DEFAULT 'BEARER' COMMENT '토큰 타입 (BEARER, API_KEY 등)',
    issued_at DATETIME(6) NOT NULL COMMENT '토큰 발급 시각',
    expires_at DATETIME(6) NOT NULL COMMENT '토큰 만료 시각',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '활성화 여부',
    refresh_count INT NOT NULL DEFAULT 0 COMMENT '갱신 횟수',
    last_refreshed_at DATETIME(6) COMMENT '마지막 갱신 시각',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정 시각',
    PRIMARY KEY (token_id),
    UNIQUE KEY uk_token_value (token_value(255)),
    INDEX idx_agent_id (agent_id),
    INDEX idx_user_agent_active (agent_id, is_active),
    INDEX idx_expires (expires_at, is_active),
    INDEX idx_refresh_time (last_refreshed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User-Agent별 토큰 정보';

-- ========================================
-- 3. token_usage_log (토큰 사용 이력 및 레이트 리미팅 추적)
-- ========================================
-- 토큰 사용 이력 및 레이트 리미팅 추적
-- 🔥 월별 파티셔닝 적용 (대용량 로그 관리)
CREATE TABLE token_usage_log (
    log_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '로그 고유 ID',
    agent_id BIGINT NOT NULL COMMENT 'user_agent_pool.agent_id 참조',
    token_id BIGINT NOT NULL COMMENT 'user_agent_token.token_id 참조',
    request_url VARCHAR(1000) NOT NULL COMMENT '요청 URL',
    http_method VARCHAR(10) NOT NULL DEFAULT 'GET' COMMENT 'HTTP 메서드',
    http_status_code INT COMMENT 'HTTP 응답 상태 코드',
    response_time_ms INT COMMENT '응답 시간 (밀리초)',
    is_success BOOLEAN NOT NULL DEFAULT TRUE COMMENT '성공 여부',
    is_rate_limited BOOLEAN NOT NULL DEFAULT FALSE COMMENT '레이트 리미팅 발생 여부',
    is_429_error BOOLEAN NOT NULL DEFAULT FALSE COMMENT '429 에러 발생 여부',
    error_message TEXT COMMENT '에러 메시지',
    request_timestamp DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '요청 시각',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    PRIMARY KEY (log_id, request_timestamp),
    INDEX idx_agent_id (agent_id),
    INDEX idx_token_id (token_id),
    INDEX idx_user_agent_time (agent_id, request_timestamp),
    INDEX idx_rate_limit (is_rate_limited, request_timestamp),
    INDEX idx_429_errors (is_429_error, agent_id, request_timestamp),
    INDEX idx_success_time (is_success, request_timestamp),
    INDEX idx_http_status (http_status_code, request_timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='토큰 사용 이력 및 레이트 리미팅 추적'
PARTITION BY RANGE (YEAR(request_timestamp) * 100 + MONTH(request_timestamp)) (
    PARTITION p202501 VALUES LESS THAN (202502),
    PARTITION p202502 VALUES LESS THAN (202503),
    PARTITION p202503 VALUES LESS THAN (202504),
    PARTITION p202504 VALUES LESS THAN (202505),
    PARTITION p202505 VALUES LESS THAN (202506),
    PARTITION p202506 VALUES LESS THAN (202507),
    PARTITION p202507 VALUES LESS THAN (202508),
    PARTITION p202508 VALUES LESS THAN (202509),
    PARTITION p202509 VALUES LESS THAN (202510),
    PARTITION p202510 VALUES LESS THAN (202511),
    PARTITION p202511 VALUES LESS THAN (202512),
    PARTITION p202512 VALUES LESS THAN (202601),
    PARTITION p_future VALUES LESS THAN MAXVALUE
);

-- ========================================
-- 4. circuit_breaker_state (Circuit Breaker 상태 관리)
-- ========================================
-- Circuit Breaker 패턴 구현 - 429 연속 발생 시 자동 차단
CREATE TABLE circuit_breaker_state (
    state_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '상태 고유 ID',
    agent_id BIGINT NOT NULL COMMENT 'user_agent_pool.agent_id 참조',
    circuit_state VARCHAR(20) NOT NULL DEFAULT 'CLOSED' COMMENT 'Circuit 상태 (CLOSED, OPEN, HALF_OPEN)',
    failure_count INT NOT NULL DEFAULT 0 COMMENT '연속 실패 횟수',
    last_failure_time DATETIME(6) COMMENT '마지막 실패 시각',
    opened_at DATETIME(6) COMMENT 'Circuit OPEN 시각',
    half_open_at DATETIME(6) COMMENT 'Circuit HALF_OPEN 시각',
    closed_at DATETIME(6) COMMENT 'Circuit CLOSED 시각',
    timeout_duration_seconds INT NOT NULL DEFAULT 600 COMMENT 'Circuit OPEN 유지 시간 (초, 기본 10분)',
    failure_threshold INT NOT NULL DEFAULT 3 COMMENT '실패 임계값 (OPEN 전환)',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정 시각',
    PRIMARY KEY (state_id),
    UNIQUE KEY uk_agent_circuit (agent_id),
    INDEX idx_user_agent_state (agent_id, circuit_state),
    INDEX idx_opened (opened_at),
    INDEX idx_state_failure (circuit_state, failure_count)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Circuit Breaker 상태 관리';

-- ========================================
-- 5. circuit_breaker_event (Circuit Breaker 이벤트 로그)
-- ========================================
-- Circuit Breaker 상태 전환 이벤트 추적
CREATE TABLE circuit_breaker_event (
    event_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '이벤트 고유 ID',
    agent_id BIGINT NOT NULL COMMENT 'user_agent_pool.agent_id 참조',
    event_type VARCHAR(50) NOT NULL COMMENT '이벤트 타입 (STATE_CHANGE, FAILURE, SUCCESS 등)',
    from_state VARCHAR(20) COMMENT '이전 상태',
    to_state VARCHAR(20) COMMENT '변경된 상태',
    failure_count INT COMMENT '실패 횟수',
    error_message TEXT COMMENT '에러 메시지',
    event_timestamp DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '이벤트 발생 시각',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    PRIMARY KEY (event_id),
    INDEX idx_agent_id (agent_id),
    INDEX idx_user_agent_time (agent_id, event_timestamp),
    INDEX idx_event_type (event_type, event_timestamp),
    INDEX idx_state_change (from_state, to_state, event_timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Circuit Breaker 이벤트 로그';

-- ========================================
-- 6. rate_limit_bucket (Token Bucket 상태 백업)
-- ========================================
-- Token Bucket 알고리즘 상태 (Redis 기본, DB는 백업/복구용)
-- 분산 환경에서 Redis 장애 시 복구 데이터로 활용
CREATE TABLE rate_limit_bucket (
    bucket_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Bucket 고유 ID',
    agent_id BIGINT NOT NULL COMMENT 'user_agent_pool.agent_id 참조',
    max_tokens INT NOT NULL DEFAULT 80 COMMENT '최대 토큰 수 (10분당 80 requests)',
    current_tokens DECIMAL(10,2) NOT NULL DEFAULT 80.00 COMMENT '현재 토큰 수',
    refill_rate DECIMAL(10,2) NOT NULL COMMENT '토큰 재충전 속도 (tokens/second)',
    last_refill_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '마지막 재충전 시각',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정 시각',
    PRIMARY KEY (bucket_id),
    UNIQUE KEY uk_agent_bucket (agent_id),
    INDEX idx_last_refill (last_refill_at),
    INDEX idx_token_level (current_tokens, last_refill_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Token Bucket 상태 백업';

-- ========================================
-- 7. token_refresh_schedule (토큰 갱신 스케줄)
-- ========================================
-- 토큰 자동 갱신 스케줄 관리
CREATE TABLE token_refresh_schedule (
    schedule_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '스케줄 고유 ID',
    agent_id BIGINT NOT NULL COMMENT 'user_agent_pool.agent_id 참조',
    token_id BIGINT NOT NULL COMMENT 'user_agent_token.token_id 참조',
    next_refresh_time DATETIME(6) NOT NULL COMMENT '다음 갱신 예정 시각',
    refresh_interval_seconds INT NOT NULL DEFAULT 3600 COMMENT '갱신 주기 (초, 기본 1시간)',
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '활성화 여부',
    last_refresh_attempt_at DATETIME(6) COMMENT '마지막 갱신 시도 시각',
    last_refresh_success_at DATETIME(6) COMMENT '마지막 갱신 성공 시각',
    consecutive_failures INT NOT NULL DEFAULT 0 COMMENT '연속 실패 횟수',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정 시각',
    PRIMARY KEY (schedule_id),
    UNIQUE KEY uk_agent_token_schedule (agent_id, token_id),
    INDEX idx_next_refresh (next_refresh_time, is_enabled),
    INDEX idx_agent_enabled (agent_id, is_enabled),
    INDEX idx_failure_count (consecutive_failures, next_refresh_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='토큰 갱신 스케줄';
