-- ========================================
-- 파티션 자동 생성 스크립트 - 2026년 1월
-- ========================================
-- token_usage_log 테이블에 새 파티션 추가
-- 매월 실행하여 다음 달 파티션 생성
-- ========================================

-- 2026년 1월 파티션 추가 (2026-01-01 ~ 2026-01-31)
ALTER TABLE token_usage_log
    REORGANIZE PARTITION p_future INTO (
        PARTITION p202601 VALUES LESS THAN (202602),
        PARTITION p_future VALUES LESS THAN MAXVALUE
    );

-- 파티션 확인
SELECT
    PARTITION_NAME,
    PARTITION_EXPRESSION,
    PARTITION_DESCRIPTION,
    TABLE_ROWS
FROM
    INFORMATION_SCHEMA.PARTITIONS
WHERE
    TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'token_usage_log'
ORDER BY
    PARTITION_ORDINAL_POSITION;
