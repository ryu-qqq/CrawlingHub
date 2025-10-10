-- ========================================
-- V2: Workflow 관련 테이블 (4개)
-- ========================================
-- 크롤링 워크플로우 및 스텝 정의 테이블
-- ⚠️ FOREIGN KEY 제약조건 없음 (애플리케이션 레벨에서 관리)
-- ✅ INDEX는 필수 (조회 성능 최적화)
-- ========================================

-- ========================================
-- 1. crawl_workflow (크롤링 워크플로우)
-- ========================================
CREATE TABLE crawl_workflow (
    workflow_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '워크플로우 고유 ID',
    site_id BIGINT NOT NULL COMMENT 'crawl_site.site_id 참조',
    workflow_name VARCHAR(200) NOT NULL COMMENT '워크플로우 이름',
    workflow_type VARCHAR(50) NOT NULL COMMENT '워크플로우 타입 (SEQUENTIAL, PARALLEL, CONDITIONAL 등)',
    description TEXT COMMENT '워크플로우 설명',
    version VARCHAR(50) NOT NULL DEFAULT '1.0.0' COMMENT '워크플로우 버전',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '활성화 여부',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정 시각',
    PRIMARY KEY (workflow_id),
    INDEX idx_site_id (site_id),
    INDEX idx_workflow_name (workflow_name),
    INDEX idx_workflow_type (workflow_type),
    INDEX idx_version (version),
    INDEX idx_is_active (is_active),
    INDEX idx_site_active (site_id, is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='크롤링 워크플로우';

-- ========================================
-- 2. workflow_step (워크플로우 스텝)
-- ========================================
CREATE TABLE workflow_step (
    step_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '스텝 고유 ID',
    workflow_id BIGINT NOT NULL COMMENT 'crawl_workflow.workflow_id 참조',
    step_name VARCHAR(200) NOT NULL COMMENT '스텝 이름',
    step_type VARCHAR(50) NOT NULL COMMENT '스텝 타입 (HTTP_REQUEST, DATA_PARSE, DATA_TRANSFORM, VALIDATION 등)',
    step_order INT NOT NULL COMMENT '스텝 실행 순서',
    step_config JSON COMMENT '스텝 설정 (JSON 형식)',
    depends_on_step_id BIGINT COMMENT '의존 스텝 ID (이전 스텝 완료 후 실행)',
    retry_on_failure BOOLEAN NOT NULL DEFAULT TRUE COMMENT '실패 시 재시도 여부',
    continue_on_error BOOLEAN NOT NULL DEFAULT FALSE COMMENT '에러 발생 시 다음 스텝 계속 진행 여부',
    timeout_seconds INT COMMENT '스텝 타임아웃 (초)',
    description TEXT COMMENT '스텝 설명',
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '활성화 여부',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정 시각',
    PRIMARY KEY (step_id),
    INDEX idx_workflow_id (workflow_id),
    INDEX idx_step_name (step_name),
    INDEX idx_step_type (step_type),
    INDEX idx_step_order (step_order),
    INDEX idx_depends_on_step_id (depends_on_step_id),
    INDEX idx_is_enabled (is_enabled),
    INDEX idx_workflow_order (workflow_id, step_order),
    INDEX idx_workflow_enabled (workflow_id, is_enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='워크플로우 스텝';

-- ========================================
-- 3. workflow_step_param (스텝 입력 파라미터)
-- ========================================
CREATE TABLE workflow_step_param (
    param_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '파라미터 고유 ID',
    step_id BIGINT NOT NULL COMMENT 'workflow_step.step_id 참조',
    param_name VARCHAR(200) NOT NULL COMMENT '파라미터 이름',
    param_type VARCHAR(50) NOT NULL COMMENT '파라미터 타입 (STRING, INTEGER, BOOLEAN, JSON 등)',
    param_value TEXT COMMENT '파라미터 값',
    is_required BOOLEAN NOT NULL DEFAULT TRUE COMMENT '필수 여부',
    default_value TEXT COMMENT '기본값',
    validation_rule VARCHAR(500) COMMENT '검증 규칙 (정규식 등)',
    description VARCHAR(500) COMMENT '파라미터 설명',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정 시각',
    PRIMARY KEY (param_id),
    INDEX idx_step_id (step_id),
    INDEX idx_param_name (param_name),
    INDEX idx_param_type (param_type),
    INDEX idx_is_required (is_required),
    INDEX idx_step_param (step_id, param_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='스텝 입력 파라미터';

-- ========================================
-- 4. workflow_step_output (스텝 출력 정의)
-- ========================================
CREATE TABLE workflow_step_output (
    output_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '출력 고유 ID',
    step_id BIGINT NOT NULL COMMENT 'workflow_step.step_id 참조',
    output_name VARCHAR(200) NOT NULL COMMENT '출력 이름',
    output_type VARCHAR(50) NOT NULL COMMENT '출력 타입 (STRING, INTEGER, JSON, FILE 등)',
    output_path VARCHAR(500) COMMENT '출력 경로 (JSONPath, XPath 등)',
    description VARCHAR(500) COMMENT '출력 설명',
    is_persisted BOOLEAN NOT NULL DEFAULT TRUE COMMENT '영구 저장 여부',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정 시각',
    PRIMARY KEY (output_id),
    INDEX idx_step_id (step_id),
    INDEX idx_output_name (output_name),
    INDEX idx_output_type (output_type),
    INDEX idx_is_persisted (is_persisted),
    INDEX idx_step_output (step_id, output_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='스텝 출력 정의';
