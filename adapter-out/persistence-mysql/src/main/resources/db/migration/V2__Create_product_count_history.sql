-- =====================================================
-- Flyway Migration Script V2
-- =====================================================
-- 목적: ProductCountHistory 테이블 생성
-- 작성자: ryu-qqq
-- 작성일: 2025-11-05
-- =====================================================

-- =====================================================
-- product_count_history (상품 수 변경 이력)
-- =====================================================
CREATE TABLE `product_count_history` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '이력 PK',
    `seller_id` BIGINT NOT NULL COMMENT '셀러 ID (Long FK)',
    `product_count` INT NOT NULL COMMENT '카운트 된 수',
    `executed_date` DATETIME NOT NULL COMMENT '실행 날짜',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    PRIMARY KEY (`id`),
    INDEX `idx_seller_id_executed_date` (`seller_id`, `executed_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='상품 수 변경 이력';
