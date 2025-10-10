-- ========================================
-- V4: Execution 관련 테이블 (4개)
-- ========================================
-- 크롤링 실행 및 결과 관리 테이블
-- ⚠️ FOREIGN KEY 제약조건 없음 (애플리케이션 레벨에서 관리)
-- ✅ INDEX는 필수 (조회 성능 최적화)
-- ========================================

-- ========================================
-- 1. crawl_execution (크롤링 실행)
-- ========================================
CREATE TABLE crawl_execution (
    execution_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '실행 고유 ID',
    schedule_id BIGINT COMMENT 'crawl_schedule.schedule_id 참조 (스케줄 실행인 경우)',
    workflow_id BIGINT NOT NULL COMMENT 'crawl_workflow.workflow_id 참조',
    execution_type VARCHAR(50) NOT NULL COMMENT '실행 타입 (SCHEDULED, MANUAL, RETRY 등)',
    status VARCHAR(50) NOT NULL COMMENT '실행 상태 (PENDING, RUNNING, COMPLETED, FAILED, CANCELLED 등)',
    started_at DATETIME(6) COMMENT '실행 시작 시각',
    completed_at DATETIME(6) COMMENT '실행 완료 시각',
    duration_ms BIGINT COMMENT '실행 소요 시간 (밀리초)',
    triggered_by VARCHAR(200) COMMENT '실행 트리거 주체 (사용자 ID, 시스템 등)',
    error_message TEXT COMMENT '에러 메시지',
    error_stack_trace TEXT COMMENT '에러 스택 트레이스',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '재시도 횟수',
    parent_execution_id BIGINT COMMENT '부모 실행 ID (재시도인 경우)',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정 시각',
    PRIMARY KEY (execution_id),
    INDEX idx_schedule_id (schedule_id),
    INDEX idx_workflow_id (workflow_id),
    INDEX idx_execution_type (execution_type),
    INDEX idx_status (status),
    INDEX idx_started_at (started_at),
    INDEX idx_completed_at (completed_at),
    INDEX idx_triggered_by (triggered_by),
    INDEX idx_parent_execution_id (parent_execution_id),
    INDEX idx_created_at (created_at),
    INDEX idx_status_started (status, started_at),
    INDEX idx_workflow_status (workflow_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='크롤링 실행';

-- ========================================
-- 2. execution_statistics (실행 통계)
-- ========================================
CREATE TABLE execution_statistics (
    statistics_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '통계 고유 ID',
    execution_id BIGINT NOT NULL COMMENT 'crawl_execution.execution_id 참조',
    total_tasks INT NOT NULL DEFAULT 0 COMMENT '전체 태스크 수',
    completed_tasks INT NOT NULL DEFAULT 0 COMMENT '완료된 태스크 수',
    failed_tasks INT NOT NULL DEFAULT 0 COMMENT '실패한 태스크 수',
    skipped_tasks INT NOT NULL DEFAULT 0 COMMENT '건너뛴 태스크 수',
    total_items_processed BIGINT NOT NULL DEFAULT 0 COMMENT '처리된 총 아이템 수',
    total_items_succeeded BIGINT NOT NULL DEFAULT 0 COMMENT '성공한 아이템 수',
    total_items_failed BIGINT NOT NULL DEFAULT 0 COMMENT '실패한 아이템 수',
    total_bytes_downloaded BIGINT NOT NULL DEFAULT 0 COMMENT '다운로드된 총 바이트 수',
    total_bytes_uploaded BIGINT NOT NULL DEFAULT 0 COMMENT '업로드된 총 바이트 수',
    total_api_calls INT NOT NULL DEFAULT 0 COMMENT '총 API 호출 수',
    avg_response_time_ms DECIMAL(10,2) COMMENT '평균 응답 시간 (밀리초)',
    min_response_time_ms BIGINT COMMENT '최소 응답 시간 (밀리초)',
    max_response_time_ms BIGINT COMMENT '최대 응답 시간 (밀리초)',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정 시각',
    PRIMARY KEY (statistics_id),
    UNIQUE KEY uk_execution_id (execution_id),
    INDEX idx_total_tasks (total_tasks),
    INDEX idx_completed_tasks (completed_tasks),
    INDEX idx_failed_tasks (failed_tasks),
    INDEX idx_total_items_processed (total_items_processed)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='실행 통계';

-- ========================================
-- 3. execution_result_summary (실행 결과 요약)
-- ========================================
CREATE TABLE execution_result_summary (
    result_summary_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '결과 요약 고유 ID',
    execution_id BIGINT NOT NULL COMMENT 'crawl_execution.execution_id 참조',
    result_type VARCHAR(50) NOT NULL COMMENT '결과 타입 (SUCCESS_ITEMS, FAILED_ITEMS, VALIDATION_ERRORS 등)',
    summary_data JSON COMMENT '요약 데이터 (JSON 형식)',
    item_count BIGINT NOT NULL DEFAULT 0 COMMENT '아이템 개수',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정 시각',
    PRIMARY KEY (result_summary_id),
    INDEX idx_execution_id (execution_id),
    INDEX idx_result_type (result_type),
    INDEX idx_item_count (item_count),
    INDEX idx_execution_type (execution_id, result_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='실행 결과 요약';

-- ========================================
-- 4. execution_s3_path (실행 결과 S3 경로)
-- ========================================
CREATE TABLE execution_s3_path (
    s3_path_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'S3 경로 고유 ID',
    execution_id BIGINT NOT NULL COMMENT 'crawl_execution.execution_id 참조',
    path_type VARCHAR(50) NOT NULL COMMENT '경로 타입 (RAW_DATA, PROCESSED_DATA, ERROR_LOG, METADATA 등)',
    s3_bucket VARCHAR(200) NOT NULL COMMENT 'S3 버킷 이름',
    s3_key VARCHAR(1000) NOT NULL COMMENT 'S3 키 (파일 경로)',
    s3_url VARCHAR(1500) COMMENT 'S3 URL (전체 경로)',
    file_size_bytes BIGINT COMMENT '파일 크기 (바이트)',
    file_format VARCHAR(50) COMMENT '파일 형식 (JSON, CSV, PARQUET 등)',
    compression_type VARCHAR(50) COMMENT '압축 타입 (NONE, GZIP, SNAPPY 등)',
    uploaded_at DATETIME(6) COMMENT '업로드 시각',
    expiry_date DATETIME(6) COMMENT '만료일 (S3 lifecycle 정책)',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정 시각',
    PRIMARY KEY (s3_path_id),
    INDEX idx_execution_id (execution_id),
    INDEX idx_path_type (path_type),
    INDEX idx_s3_bucket (s3_bucket),
    INDEX idx_uploaded_at (uploaded_at),
    INDEX idx_expiry_date (expiry_date),
    INDEX idx_execution_path_type (execution_id, path_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='실행 결과 S3 경로';
