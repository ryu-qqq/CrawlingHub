-- Seller 테이블 생성
-- SellerJpaEntity 기반 DDL
-- BaseAuditEntity (createdAt, updatedAt) 포함

CREATE TABLE IF NOT EXISTS seller (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Seller 기본 키 (AUTO_INCREMENT)',
    must_it_seller_name VARCHAR(100) NOT NULL COMMENT '머스트잇 셀러명 (UNIQUE)',
    seller_name VARCHAR(100) NOT NULL COMMENT '커머스 셀러명 (UNIQUE)',
    status VARCHAR(20) NOT NULL COMMENT '셀러 상태 (ACTIVE/INACTIVE)',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',

    -- UNIQUE 제약조건
    UNIQUE KEY uk_must_it_seller_name (must_it_seller_name),
    UNIQUE KEY uk_seller_name (seller_name),

    -- 인덱스
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='셀러 테이블';
