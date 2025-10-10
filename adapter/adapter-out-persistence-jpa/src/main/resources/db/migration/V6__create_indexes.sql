-- ========================================
-- V6: 복합 인덱스 추가
-- ========================================
-- 조회 성능 최적화를 위한 추가 복합 인덱스
-- 자주 사용되는 WHERE 조건 및 JOIN 조건 기반
-- ========================================

-- ========================================
-- Execution 관련 복합 인덱스
-- ========================================

-- 실행 조회 최적화: 상태 + 시작 시각 (이미 V4에 있음, 참고용)
-- INDEX idx_status_started ON crawl_execution(status, started_at);

-- 워크플로우별 실행 조회 최적화: 워크플로우 + 상태 (이미 V4에 있음, 참고용)
-- INDEX idx_workflow_status ON crawl_execution(workflow_id, status);

-- 실행 기간 조회 최적화
CREATE INDEX idx_execution_date_range ON crawl_execution(started_at, completed_at);

-- 실행 타입별 조회 최적화
CREATE INDEX idx_type_status_date ON crawl_execution(execution_type, status, started_at);

-- ========================================
-- Task 관련 복합 인덱스
-- ========================================

-- 실행별 태스크 상태 조회 (이미 V5에 있음, 참고용)
-- INDEX idx_execution_status ON crawl_task(execution_id, status);

-- 스텝별 태스크 상태 조회 (이미 V5에 있음, 참고용)
-- INDEX idx_step_status ON crawl_task(step_id, status);

-- 태스크 실행 기간 조회 최적화
CREATE INDEX idx_task_date_range ON crawl_task(started_at, completed_at);

-- 태스크 타입별 상태 조회
CREATE INDEX idx_task_type_status ON crawl_task(task_type, status, started_at);

-- ========================================
-- Schedule 관련 복합 인덱스
-- ========================================

-- 활성 스케줄 + 다음 실행 시각 (이미 V3에 있음, 참고용)
-- INDEX idx_enabled_next ON crawl_schedule(is_enabled, next_execution_time);

-- 워크플로우별 활성 스케줄 (이미 V3에 있음, 참고용)
-- INDEX idx_workflow_enabled ON crawl_schedule(workflow_id, is_enabled);

-- 우선순위별 스케줄 조회
CREATE INDEX idx_priority_enabled_next ON crawl_schedule(priority, is_enabled, next_execution_time);

-- 스케줄 타입별 조회
CREATE INDEX idx_type_enabled ON crawl_schedule(schedule_type, is_enabled);

-- ========================================
-- Site 관련 복합 인덱스
-- ========================================

-- 활성 사이트 조회 (타입별)
CREATE INDEX idx_site_type_active ON crawl_site(site_type, is_active, created_at);

-- 사이트 엔드포인트 활성 조회
CREATE INDEX idx_endpoint_site_enabled ON site_api_endpoint(site_id, is_enabled, endpoint_name);

-- ========================================
-- Workflow 관련 복합 인덱스
-- ========================================

-- 활성 워크플로우 조회 (사이트별)
CREATE INDEX idx_workflow_site_active ON crawl_workflow(site_id, is_active, version);

-- 워크플로우 스텝 순서 조회 (이미 V2에 있음, 참고용)
-- INDEX idx_workflow_order ON workflow_step(workflow_id, step_order);

-- 워크플로우 활성 스텝 조회 (이미 V2에 있음, 참고용)
-- INDEX idx_workflow_enabled ON workflow_step(workflow_id, is_enabled);

-- 스텝 타입별 조회
CREATE INDEX idx_step_type_enabled ON workflow_step(step_type, is_enabled);

-- ========================================
-- S3 Path 관련 복합 인덱스
-- ========================================

-- 실행별 경로 타입 조회 (이미 V4에 있음, 참고용)
-- INDEX idx_execution_path_type ON execution_s3_path(execution_id, path_type);

-- 만료 예정 파일 조회
CREATE INDEX idx_expiry_bucket ON execution_s3_path(expiry_date, s3_bucket);

-- 업로드 일시별 조회
CREATE INDEX idx_uploaded_bucket_type ON execution_s3_path(uploaded_at, s3_bucket, path_type);

-- ========================================
-- Statistics 관련 복합 인덱스
-- ========================================

-- 실행 통계 조회 (성능 분석)
CREATE INDEX idx_stats_tasks ON execution_statistics(completed_tasks, failed_tasks, total_items_processed);

-- ========================================
-- Task Attempt 관련 복합 인덱스
-- ========================================

-- 태스크별 시도 조회 (이미 V5에 UNIQUE KEY로 있음, 참고용)
-- UNIQUE KEY uk_task_attempt ON crawl_task_attempt(task_id, attempt_number);

-- 실패한 시도 조회
CREATE INDEX idx_attempt_status_http ON crawl_task_attempt(status, http_status_code, started_at);

-- ========================================
-- Full-Text Search 인덱스 (선택사항)
-- ========================================
-- 사이트 이름 및 설명 검색 (필요 시 주석 해제)
-- CREATE FULLTEXT INDEX ft_site_search ON crawl_site(site_name, description);

-- 워크플로우 검색 (필요 시 주석 해제)
-- CREATE FULLTEXT INDEX ft_workflow_search ON crawl_workflow(workflow_name, description);

-- 에러 메시지 검색 (필요 시 주석 해제)
-- CREATE FULLTEXT INDEX ft_error_search ON crawl_execution(error_message);
