-- V10__add_original_description_markup_column.sql
-- original_description_mark_up 컬럼 추가
--
-- 변경 사항:
-- 1. crawled_product 테이블에 original_description_mark_up 컬럼 추가
--    - 원본 상세 설명 HTML 저장 (비교 기준, 원본 URL 유지)
--    - description_mark_up은 S3 URL로 치환된 버전

-- =====================================================
-- 1. original_description_mark_up 컬럼 추가
-- =====================================================
ALTER TABLE crawled_product
    ADD COLUMN original_description_mark_up LONGTEXT NULL COMMENT '원본 상세 설명 HTML (비교 기준, 원본 URL 유지)' AFTER shipping_info_json;

-- =====================================================
-- 2. 기존 데이터 마이그레이션 (description_mark_up 값 복사)
-- =====================================================
-- 기존 description_mark_up 값을 original_description_mark_up으로 복사
-- (아직 S3 치환이 안 된 상태이므로 동일한 값)
UPDATE crawled_product
SET original_description_mark_up = description_mark_up
WHERE description_mark_up IS NOT NULL;
