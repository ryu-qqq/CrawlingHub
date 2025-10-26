-- =====================================================
-- V9: 모니터링 및 메트릭 테이블 생성
-- Author: Crawling Hub Team
-- Date: 2024-01-20
-- Description: 시스템 모니터링, 에러 추적, 성능 메트릭을 위한 테이블
-- =====================================================

-- 크롤링 메트릭 테이블 (시계열 데이터)
CREATE TABLE IF NOT EXISTS crawling_metrics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    metric_type VARCHAR(50) NOT NULL COMMENT '메트릭 타입 (SUCCESS_RATE, RESPONSE_TIME, THROUGHPUT)',
    metric_name VARCHAR(100) NOT NULL COMMENT '메트릭 이름',
    entity_id BIGINT COMMENT '관련 엔티티 ID',
    entity_type VARCHAR(50) COMMENT '엔티티 타입 (SITE, SELLER, JOB)',
    metric_value DECIMAL(15,4) NOT NULL COMMENT '메트릭 값',
    unit VARCHAR(20) COMMENT '단위 (%, ms, count)',
    aggregation_period ENUM('MINUTE', 'HOUR', 'DAY', 'WEEK', 'MONTH') COMMENT '집계 기간',
    period_start TIMESTAMP NOT NULL COMMENT '기간 시작',
    period_end TIMESTAMP NOT NULL COMMENT '기간 종료',
    dimensions JSON COMMENT '추가 차원 정보',
    metadata JSON COMMENT '메타데이터',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    INDEX idx_metrics_timeseries (metric_type, entity_type, entity_id, period_start DESC),
    INDEX idx_metrics_lookup (metric_name, period_start DESC),
    INDEX idx_metrics_period (period_start, period_end)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='크롤링 성능 메트릭 저장';

-- 에러 로그 테이블
CREATE TABLE IF NOT EXISTS error_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    error_code VARCHAR(100) COMMENT '에러 코드',
    error_level ENUM('DEBUG', 'INFO', 'WARN', 'ERROR', 'FATAL') NOT NULL COMMENT '에러 레벨',
    error_category VARCHAR(200) COMMENT '에러 카테고리 (NETWORK, PARSING, AUTH)',
    error_message TEXT NOT NULL COMMENT '에러 메시지',
    stack_trace TEXT COMMENT '스택 트레이스',
    job_id BIGINT COMMENT '관련 작업 ID',
    task_id BIGINT COMMENT '관련 태스크 ID',
    context_url VARCHAR(1000) COMMENT '발생 URL',
    user_agent_id BIGINT COMMENT '사용된 User-Agent ID',
    error_context JSON COMMENT '에러 컨텍스트',
    is_resolved BOOLEAN DEFAULT FALSE COMMENT '해결 여부',
    resolution_notes TEXT COMMENT '해결 메모',
    resolved_at TIMESTAMP NULL COMMENT '해결 시간',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    FOREIGN KEY fk_error_job (job_id) REFERENCES crawling_jobs(id) ON DELETE SET NULL,
    FOREIGN KEY fk_error_task (task_id) REFERENCES crawling_tasks(id) ON DELETE SET NULL,
    INDEX idx_errors_unresolved (error_level, is_resolved, created_at DESC),
    INDEX idx_errors_category (error_category, created_at DESC),
    INDEX idx_errors_job (job_id, task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='시스템 에러 로그';

-- 시스템 헬스 체크 테이블
CREATE TABLE IF NOT EXISTS system_health (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    component VARCHAR(50) NOT NULL COMMENT '컴포넌트 (CRAWLER, DATABASE, S3, REDIS)',
    check_type VARCHAR(50) NOT NULL COMMENT '체크 타입 (AVAILABILITY, PERFORMANCE)',
    status ENUM('HEALTHY', 'DEGRADED', 'UNHEALTHY') NOT NULL COMMENT '상태',
    availability_percentage DECIMAL(5,2) COMMENT '가용성 백분율',
    response_time_p50 BIGINT COMMENT '응답시간 중간값 (ms)',
    response_time_p95 BIGINT COMMENT '응답시간 95 백분위 (ms)',
    response_time_p99 BIGINT COMMENT '응답시간 99 백분위 (ms)',
    cpu_usage DECIMAL(5,2) COMMENT 'CPU 사용률 (%)',
    memory_usage DECIMAL(5,2) COMMENT '메모리 사용률 (%)',
    disk_usage DECIMAL(5,2) COMMENT '디스크 사용률 (%)',
    metrics JSON COMMENT '추가 메트릭',
    checked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '체크 시간',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    INDEX idx_health_status (component, status, checked_at DESC),
    INDEX idx_health_recent (checked_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='시스템 상태 모니터링';

-- 알림 설정 테이블
CREATE TABLE IF NOT EXISTS alert_configurations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    alert_name VARCHAR(100) NOT NULL COMMENT '알림 이름',
    alert_type ENUM('THRESHOLD', 'ANOMALY', 'PATTERN') NOT NULL COMMENT '알림 타입',
    severity ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') NOT NULL COMMENT '심각도',
    metric_name VARCHAR(100) COMMENT '모니터링 메트릭',
    condition_rules JSON NOT NULL COMMENT '알림 조건',
    notification_channels JSON COMMENT '알림 채널 (Slack, Email)',
    check_interval_seconds INT DEFAULT 300 COMMENT '체크 간격 (초)',
    cooldown_minutes INT DEFAULT 30 COMMENT '재알림 쿨다운 (분)',
    is_active BOOLEAN DEFAULT TRUE COMMENT '활성화 여부',
    last_triggered_at TIMESTAMP NULL COMMENT '마지막 트리거 시간',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    UNIQUE KEY uk_alert_name (alert_name),
    INDEX idx_alerts_active (is_active, severity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='알림 설정';

-- 알림 이력 테이블
CREATE TABLE IF NOT EXISTS alert_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    alert_config_id BIGINT NOT NULL COMMENT '알림 설정 ID',
    status ENUM('TRIGGERED', 'RESOLVED', 'ACKNOWLEDGED') NOT NULL COMMENT '알림 상태',
    metric_value DECIMAL(15,4) COMMENT '트리거 시점 값',
    context JSON COMMENT '알림 컨텍스트',
    message TEXT COMMENT '알림 메시지',
    acknowledged_by VARCHAR(100) COMMENT '확인자',
    acknowledgment_notes TEXT COMMENT '확인 메모',
    triggered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '트리거 시간',
    resolved_at TIMESTAMP NULL COMMENT '해결 시간',
    acknowledged_at TIMESTAMP NULL COMMENT '확인 시간',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    FOREIGN KEY fk_alert_config (alert_config_id) REFERENCES alert_configurations(id) ON DELETE CASCADE,
    INDEX idx_alert_history_status (alert_config_id, status, triggered_at DESC),
    INDEX idx_alert_history_recent (triggered_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='알림 이력';

-- 성능 스냅샷 테이블
CREATE TABLE IF NOT EXISTS performance_snapshots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    snapshot_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '스냅샷 시간',
    active_jobs INT DEFAULT 0 COMMENT '활성 작업 수',
    pending_jobs INT DEFAULT 0 COMMENT '대기 작업 수',
    failed_jobs_1h INT DEFAULT 0 COMMENT '1시간 내 실패 작업',
    avg_response_time DECIMAL(10,2) COMMENT '평균 응답 시간 (ms)',
    success_rate_1h DECIMAL(5,2) COMMENT '1시간 성공률 (%)',
    total_requests_1h BIGINT DEFAULT 0 COMMENT '1시간 총 요청 수',
    data_crawled_mb BIGINT DEFAULT 0 COMMENT '크롤링 데이터 크기 (MB)',
    active_user_agents INT DEFAULT 0 COMMENT '활성 User-Agent 수',
    rate_limited_count INT DEFAULT 0 COMMENT '레이트 제한 발생 수',
    top_errors JSON COMMENT '주요 에러 목록',
    slowest_endpoints JSON COMMENT '느린 엔드포인트 목록',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    INDEX idx_snapshots_time (snapshot_time DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='시스템 성능 스냅샷';

-- 데이터 품질 메트릭 테이블
CREATE TABLE IF NOT EXISTS data_quality_metrics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    seller_id BIGINT NOT NULL COMMENT '셀러 ID',
    check_date DATE NOT NULL COMMENT '체크 날짜',
    total_products INT DEFAULT 0 COMMENT '총 상품 수',
    valid_products INT DEFAULT 0 COMMENT '유효 상품 수',
    missing_images INT DEFAULT 0 COMMENT '이미지 누락 수',
    missing_prices INT DEFAULT 0 COMMENT '가격 누락 수',
    missing_descriptions INT DEFAULT 0 COMMENT '설명 누락 수',
    duplicate_products INT DEFAULT 0 COMMENT '중복 상품 수',
    completeness_score DECIMAL(5,2) COMMENT '완전성 점수 (0-100)',
    accuracy_score DECIMAL(5,2) COMMENT '정확성 점수 (0-100)',
    freshness_score DECIMAL(5,2) COMMENT '신선도 점수 (0-100)',
    field_coverage JSON COMMENT '필드별 커버리지',
    anomalies JSON COMMENT '이상 항목',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    FOREIGN KEY fk_quality_seller (seller_id) REFERENCES sellers(id) ON DELETE CASCADE,
    UNIQUE KEY uk_quality_date (seller_id, check_date),
    INDEX idx_quality_scores (completeness_score, accuracy_score, freshness_score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='데이터 품질 메트릭';

-- 감사 로그 테이블
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    action_type ENUM('CREATE', 'UPDATE', 'DELETE', 'ACCESS') NOT NULL COMMENT '작업 타입',
    entity_type VARCHAR(100) NOT NULL COMMENT '엔티티 타입',
    entity_id BIGINT COMMENT '엔티티 ID',
    performed_by VARCHAR(100) NOT NULL COMMENT '수행자',
    ip_address VARCHAR(50) COMMENT 'IP 주소',
    before_value JSON COMMENT '변경 전 값',
    after_value JSON COMMENT '변경 후 값',
    description TEXT COMMENT '작업 설명',
    performed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '수행 시간',
    INDEX idx_audit_entity (entity_type, entity_id, performed_at DESC),
    INDEX idx_audit_user (performed_by, performed_at DESC),
    INDEX idx_audit_action (action_type, performed_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='감사 로그';

-- 일별 크롤링 통계 테이블
CREATE TABLE IF NOT EXISTS crawling_statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stat_date DATE NOT NULL COMMENT '통계 날짜',
    site_id BIGINT COMMENT '사이트 ID',
    seller_id BIGINT COMMENT '셀러 ID',
    total_jobs INT DEFAULT 0 COMMENT '총 작업 수',
    successful_jobs INT DEFAULT 0 COMMENT '성공 작업 수',
    failed_jobs INT DEFAULT 0 COMMENT '실패 작업 수',
    total_products_crawled BIGINT DEFAULT 0 COMMENT '크롤링된 상품 수',
    new_products_found BIGINT DEFAULT 0 COMMENT '신규 발견 상품',
    updated_products BIGINT DEFAULT 0 COMMENT '업데이트된 상품',
    avg_crawl_time_seconds DECIMAL(10,2) COMMENT '평균 크롤링 시간 (초)',
    total_data_size_mb BIGINT DEFAULT 0 COMMENT '총 데이터 크기 (MB)',
    cost_estimate DECIMAL(10,2) COMMENT '예상 비용',
    hourly_distribution JSON COMMENT '시간대별 분포',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    FOREIGN KEY fk_stats_site (site_id) REFERENCES sites(id) ON DELETE SET NULL,
    FOREIGN KEY fk_stats_seller (seller_id) REFERENCES sellers(id) ON DELETE SET NULL,
    UNIQUE KEY uk_stats_date (stat_date, site_id, seller_id),
    INDEX idx_stats_date (stat_date DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='일별 크롤링 통계';

-- 대시보드 위젯 설정 테이블
CREATE TABLE IF NOT EXISTS dashboard_widgets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    widget_name VARCHAR(100) NOT NULL COMMENT '위젯 이름',
    widget_type ENUM('CHART', 'TABLE', 'METRIC', 'MAP') NOT NULL COMMENT '위젯 타입',
    query_config JSON NOT NULL COMMENT '쿼리 설정',
    display_config JSON COMMENT '디스플레이 설정',
    refresh_interval_seconds INT DEFAULT 300 COMMENT '갱신 간격 (초)',
    display_order INT DEFAULT 0 COMMENT '표시 순서',
    is_active BOOLEAN DEFAULT TRUE COMMENT '활성화 여부',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    INDEX idx_widgets_active (is_active, display_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='대시보드 위젯 설정';

-- Materialized View를 위한 요약 테이블
CREATE TABLE IF NOT EXISTS mv_hourly_metrics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hour_key VARCHAR(16) NOT NULL COMMENT 'YYYY-MM-DD HH:00',
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT,
    metric_type VARCHAR(50) NOT NULL,
    avg_value DECIMAL(15,4),
    min_value DECIMAL(15,4),
    max_value DECIMAL(15,4),
    data_points INT DEFAULT 0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_metrics_hour (hour_key, entity_type, entity_id, metric_type),
    INDEX idx_mv_metrics_lookup (hour_key DESC, metric_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='시간별 메트릭 요약 (Materialized View)';

-- 초기 알림 설정 삽입
INSERT INTO alert_configurations (alert_name, alert_type, severity, metric_name, condition_rules, notification_channels) VALUES
('High Failure Rate', 'THRESHOLD', 'CRITICAL', 'job_success_rate', 
 '{"operator": "LESS_THAN", "threshold": 50, "duration_minutes": 10}',
 '{"slack": {"enabled": true, "channel": "#alerts"}}'),
('Slow Response Time', 'THRESHOLD', 'HIGH', 'response_time_p95',
 '{"operator": "GREATER_THAN", "threshold": 5000, "duration_minutes": 5}',
 '{"slack": {"enabled": true, "channel": "#performance"}}'),
('Low Data Quality', 'THRESHOLD', 'MEDIUM', 'completeness_score',
 '{"operator": "LESS_THAN", "threshold": 80, "duration_minutes": 60}',
 '{"email": {"enabled": true, "recipients": ["admin@crawlinghub.com"]}}');
