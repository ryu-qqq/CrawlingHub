-- ========================================
-- V5: Task 관련 테이블 (5개)
-- ========================================
-- 크롤링 태스크 실행 및 결과 관리 테이블
-- ⚠️ FOREIGN KEY 제약조건 없음 (애플리케이션 레벨에서 관리)
-- ✅ INDEX는 필수 (조회 성능 최적화)
-- ========================================

-- ========================================
-- 1. crawl_task (크롤링 태스크)
-- ========================================
CREATE TABLE crawl_task (
    task_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '태스크 고유 ID',
    execution_id BIGINT NOT NULL COMMENT 'crawl_execution.execution_id 참조',
    step_id BIGINT NOT NULL COMMENT 'workflow_step.step_id 참조',
    task_name VARCHAR(200) NOT NULL COMMENT '태스크 이름',
    task_type VARCHAR(50) NOT NULL COMMENT '태스크 타입 (HTTP_REQUEST, PARSE, TRANSFORM, VALIDATE 등)',
    status VARCHAR(50) NOT NULL COMMENT '태스크 상태 (PENDING, RUNNING, COMPLETED, FAILED, SKIPPED 등)',
    started_at DATETIME(6) COMMENT '시작 시각',
    completed_at DATETIME(6) COMMENT '완료 시각',
    duration_ms BIGINT COMMENT '소요 시간 (밀리초)',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '재시도 횟수',
    max_retry_attempts INT NOT NULL DEFAULT 3 COMMENT '최대 재시도 횟수',
    error_message TEXT COMMENT '에러 메시지',
    error_code VARCHAR(100) COMMENT '에러 코드',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정 시각',
    PRIMARY KEY (task_id),
    INDEX idx_execution_id (execution_id),
    INDEX idx_step_id (step_id),
    INDEX idx_task_name (task_name),
    INDEX idx_task_type (task_type),
    INDEX idx_status (status),
    INDEX idx_started_at (started_at),
    INDEX idx_completed_at (completed_at),
    INDEX idx_created_at (created_at),
    INDEX idx_execution_status (execution_id, status),
    INDEX idx_step_status (step_id, status),
    INDEX idx_status_started (status, started_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='크롤링 태스크';

-- ========================================
-- 2. task_input_param (태스크 입력 파라미터)
-- ========================================
CREATE TABLE task_input_param (
    param_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '파라미터 고유 ID',
    task_id BIGINT NOT NULL COMMENT 'crawl_task.task_id 참조',
    param_name VARCHAR(200) NOT NULL COMMENT '파라미터 이름',
    param_type VARCHAR(50) NOT NULL COMMENT '파라미터 타입 (STRING, INTEGER, JSON 등)',
    param_value TEXT COMMENT '파라미터 값',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    PRIMARY KEY (param_id),
    INDEX idx_task_id (task_id),
    INDEX idx_param_name (param_name),
    INDEX idx_task_param (task_id, param_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='태스크 입력 파라미터';

-- ========================================
-- 3. task_output_data (태스크 출력 데이터)
-- ========================================
CREATE TABLE task_output_data (
    output_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '출력 고유 ID',
    task_id BIGINT NOT NULL COMMENT 'crawl_task.task_id 참조',
    output_name VARCHAR(200) NOT NULL COMMENT '출력 이름',
    output_type VARCHAR(50) NOT NULL COMMENT '출력 타입 (JSON, TEXT, BINARY 등)',
    output_value LONGTEXT COMMENT '출력 값',
    output_size_bytes BIGINT COMMENT '출력 크기 (바이트)',
    is_stored_in_s3 BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'S3 저장 여부',
    s3_path VARCHAR(1000) COMMENT 'S3 경로 (S3 저장 시)',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    PRIMARY KEY (output_id),
    INDEX idx_task_id (task_id),
    INDEX idx_output_name (output_name),
    INDEX idx_output_type (output_type),
    INDEX idx_is_stored_in_s3 (is_stored_in_s3),
    INDEX idx_task_output (task_id, output_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='태스크 출력 데이터';

-- ========================================
-- 4. task_result_metadata (태스크 결과 메타데이터)
-- ========================================
CREATE TABLE task_result_metadata (
    metadata_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '메타데이터 고유 ID',
    task_id BIGINT NOT NULL COMMENT 'crawl_task.task_id 참조',
    metadata_key VARCHAR(200) NOT NULL COMMENT '메타데이터 키',
    metadata_value TEXT COMMENT '메타데이터 값',
    value_type VARCHAR(50) NOT NULL DEFAULT 'STRING' COMMENT '값 타입 (STRING, INTEGER, BOOLEAN, JSON 등)',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    PRIMARY KEY (metadata_id),
    INDEX idx_task_id (task_id),
    INDEX idx_metadata_key (metadata_key),
    INDEX idx_value_type (value_type),
    INDEX idx_task_metadata (task_id, metadata_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='태스크 결과 메타데이터';

-- ========================================
-- 5. crawl_task_attempt (태스크 시도 이력)
-- ========================================
CREATE TABLE crawl_task_attempt (
    attempt_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '시도 고유 ID',
    task_id BIGINT NOT NULL COMMENT 'crawl_task.task_id 참조',
    attempt_number INT NOT NULL COMMENT '시도 번호 (1부터 시작)',
    status VARCHAR(50) NOT NULL COMMENT '시도 상태 (RUNNING, SUCCEEDED, FAILED 등)',
    started_at DATETIME(6) NOT NULL COMMENT '시도 시작 시각',
    completed_at DATETIME(6) COMMENT '시도 완료 시각',
    duration_ms BIGINT COMMENT '소요 시간 (밀리초)',
    error_message TEXT COMMENT '에러 메시지',
    error_code VARCHAR(100) COMMENT '에러 코드',
    http_status_code INT COMMENT 'HTTP 상태 코드 (HTTP 요청인 경우)',
    response_size_bytes BIGINT COMMENT '응답 크기 (바이트)',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    PRIMARY KEY (attempt_id),
    INDEX idx_task_id (task_id),
    INDEX idx_attempt_number (attempt_number),
    INDEX idx_status (status),
    INDEX idx_started_at (started_at),
    INDEX idx_completed_at (completed_at),
    INDEX idx_http_status_code (http_status_code),
    INDEX idx_task_attempt (task_id, attempt_number),
    UNIQUE KEY uk_task_attempt (task_id, attempt_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='태스크 시도 이력';
